/*
 * Copyright 2010-2020 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.visual.vulkan.resourcetypes;

import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import au.gov.asd.tac.constellation.utilities.glyphs.GlyphManager;
import au.gov.asd.tac.constellation.utilities.glyphs.GlyphManagerBI;
import au.gov.asd.tac.constellation.utilities.glyphs.GlyphStreamContext;
import au.gov.asd.tac.constellation.utilities.glyphs.LabelFontsPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3i;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;


public class CVKGlyphTextureAtlas {
    private static CVKGlyphTextureAtlas cvkGlyphTextureAtlas = null;
    
    private static GlyphManagerBI glyphManager = null;
    public static int BACKGROUND_GLYPH_INDEX = 0;    
    private static final int FLOATS_PER_GLYPH = 4;

    // Calculated once the device limits are known
    public int texture2DDimension = 0;  
    
    // Instance members
    private CVKImage cvkAtlasImage = null;
    private CVKBuffer cvkCoordinateBuffer = null;
    private CVKBuffer cvkCoordinateStagingBuffer = null;
    private long hCoordinateBufferView = VK_NULL_HANDLE;
    private long hAtlasSampler = VK_NULL_HANDLE;
    private boolean needsSaveToFile = false;
    private File fileToSave = null;
    private int glyphCount = 0;
      
    
    // ========================> Lifetime <======================== \\    
    
    public static boolean IsInstantiated() {
        return cvkGlyphTextureAtlas != null;
    }
    
    public static CVKGlyphTextureAtlas GetInstance() {  
        if (cvkGlyphTextureAtlas == null) {
            cvkGlyphTextureAtlas = new CVKGlyphTextureAtlas();      
        }
        return cvkGlyphTextureAtlas;
    }
    
    private CVKGlyphTextureAtlas() {    
        if (glyphManager == null) {
            // If the first graph loaded is loaded from a file then this atlas is created
            // before the device is initialised.  In that case we just default the size of
            // the glyph manager texture (2048)
            if (CVKDevice.GetVkDevice() != null) {
                texture2DDimension = Math.max(CVKDevice.GetMax2DDimension() / 2, GlyphManagerBI.DEFAULT_TEXTURE_BUFFER_SIZE);
            } else {
                texture2DDimension = GlyphManagerBI.DEFAULT_TEXTURE_BUFFER_SIZE;
            }
            glyphManager = new GlyphManagerBI(LabelFontsPreferenceKeys.getFontInfo(),
                                              texture2DDimension,
                                              GlyphManagerBI.DEFAULT_BUFFER_TYPE);
            BACKGROUND_GLYPH_INDEX = glyphManager.createBackgroundGlyph(0.5f);
        }          
    }

    
    private int CreateAtlas() {
        CVKAssert(glyphCount == 0);
        CVKAssertNull(cvkAtlasImage);
        CVKAssertNull(hAtlasSampler);
        CVKAssertNull(cvkCoordinateBuffer);
        CVKAssertNull(cvkCoordinateStagingBuffer);
        int ret = VK_SUCCESS;
        
        // TODO: optimise this by not rewriting existing pages in the atlas texture (pre-allocation policy?)
        //       and only appending to the coordinate buffer (again, pre-allocation?)
        glyphCount = glyphManager.getGlyphCount();
        if (glyphCount > 0) {
            // Atlas texture
            final int width = glyphManager.getTextureWidth();
            final int height = glyphManager.getTextureHeight();
            final int pageCount = glyphManager.getGlyphPageCount();   
            final int pageSize = width * height * Byte.BYTES; //source BufferedImage format will be TYPE_BYTE_GRAY
            CVKAssert(width == texture2DDimension);
            CVKAssert(height == texture2DDimension);
                       
            // Create destination image 
            cvkAtlasImage = CVKImage.Create(texture2DDimension, 
                                            texture2DDimension, 
                                            pageCount,
                                            VK_FORMAT_R8_SRGB,           //GL_RED in the OpenGL renderer
                                            VK_IMAGE_VIEW_TYPE_2D_ARRAY, //regardless how how many layers are in the image, the shaders that use that atlas use a sampler2DArray
                                            VK_IMAGE_TILING_OPTIMAL,     //we usually sample rectangles rather than long straight lines
                                            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                                            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                            VK_IMAGE_ASPECT_COLOR_BIT,
                                            null,
                                            "CVKGlyphTextureAtlas cvkAtlasImage");
            CVKAssertNotNull(cvkAtlasImage);
            
            // Transition image from undefined to transfer destination optimal
            ret = cvkAtlasImage.Transition(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            if (VkFailed(ret)) { return ret; }
                        
            CVKBuffer atlasStagingBuffer = CVKBuffer.Create(pageSize, 
                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                            null,
                                                            "CVKGlyphTextureAtlas.CreateAtlas atlasStagingBuffer");              
            for (int page = 0; page < pageCount; ++ page) {
                // Last page will likely be incomplete
                if (page == (pageCount - 1)) {
                    atlasStagingBuffer.ZeroMemory();
                }
                
                // Copy from the glyph managers BufferedImage into our host visible staging buffer
                ByteBuffer pMemory = atlasStagingBuffer.StartMemoryMap(0, pageSize);
                {
                    glyphManager.readGlyphTexturePage(page, pMemory);
                }
                atlasStagingBuffer.EndMemoryMap();
                pMemory = null;
                                
                // Copy it in.  Note this is blocking.  If this is highlighted as a performance issue use the
                // add a version of CVKImage.CopyFrom that takes a command buffer but doesn't submit it and
                // doesn't do any image layout transitions.                
                Vector3i dstOffset = new Vector3i(0, 0, page);
                Vector3i dstExtent = new Vector3i(texture2DDimension, texture2DDimension, 1);
                           
                ret = cvkAtlasImage.CopyFrom(atlasStagingBuffer, 0, dstOffset, dstExtent);
                if (VkFailed(ret)) { return ret; }                  
            }
            
            // Now the image is populated, transition it for reading
            ret = cvkAtlasImage.Transition(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            if (VkFailed(ret)) { return ret; }   
            
            // No longer needed
            atlasStagingBuffer.Destroy();
            atlasStagingBuffer = null;
            
            // Create a sampler to match the image.  Note the sampler allows us to sample
            // an image but isn't tied to a specific image, note the lack of image or 
            // imageview parameters below.
            try (MemoryStack stack = stackPush()) {
                VkSamplerCreateInfo vkSamplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);                        
                vkSamplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
                vkSamplerCreateInfo.maxAnisotropy(1.0f);
                vkSamplerCreateInfo.magFilter(VK_FILTER_LINEAR);
                vkSamplerCreateInfo.minFilter(VK_FILTER_LINEAR);
                vkSamplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
                vkSamplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
                vkSamplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
                vkSamplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
                vkSamplerCreateInfo.mipLodBias(0.0f);
                vkSamplerCreateInfo.maxAnisotropy(8);
                vkSamplerCreateInfo.compareOp(VK_COMPARE_OP_NEVER);
                vkSamplerCreateInfo.minLod(0.0f);
                vkSamplerCreateInfo.maxLod(0.0f);
                vkSamplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);

                LongBuffer pTextureSampler = stack.mallocLong(1);
                ret = vkCreateSampler(CVKDevice.GetVkDevice(), vkSamplerCreateInfo, null, pTextureSampler);
                checkVKret(ret);
                hAtlasSampler = pTextureSampler.get(0);
                CVKAssertNotNull(hAtlasSampler);       
            }
            
            // Coordinates staging buffer.  The staging buffer and coordinate buffer
            // must be separate buffers as they allocate memory from different pools,
            // the first is host visible which we can copy other CPU buffers into, the
            // second is device local which is not readable or writable from the CPU.  
            // This is where the staging buffer comes in as it can be written by the
            // CPU but then read by the GPU and copied into the our device local
            // buffer.
            final int coordinateBufferSize = glyphCount * FLOATS_PER_GLYPH * Float.BYTES;
            CVKBuffer coordinateStagingBuffer = CVKBuffer.Create(coordinateBufferSize, 
                                                                 VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                                 VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                                 GetLogger(),
                                                                 "CVKGlyphTextureAtlas.CreateAtlas coordinateStagingBuffer");            
            ByteBuffer pMemory = coordinateStagingBuffer.StartMemoryMap(0, coordinateBufferSize);
            {
                float[] glyphCoordinates = glyphManager.getGlyphTextureCoordinates();
                for (int i = 0; i < glyphCount * FLOATS_PER_GLYPH; ++i) {
                    pMemory.putFloat(glyphCoordinates[i]);
                }
            }
            coordinateStagingBuffer.EndMemoryMap();
            pMemory = null; // now unmapped, do not use              

            // Coordinates buffer
            cvkCoordinateBuffer = CVKBuffer.Create(coordinateStagingBuffer.GetBufferSize(), 
                                                   VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                   VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                   GetLogger(),
                                                   "CVKGlyphTextureAtlas.cvkCoordinateBuffer"); 
            ret = cvkCoordinateBuffer.CopyFrom(coordinateStagingBuffer);     
            if (VkFailed(ret)) { return ret; }
            coordinateStagingBuffer.Destroy();
            coordinateStagingBuffer = null;
            
            // View (for the geometry shader sampler)
            try (MemoryStack stack = stackPush()) {
                // NB: we have already checked VK_FORMAT_R32G32B32A32_SFLOAT can be used as a texel buffer
                // format in CVKDevice.  If the format is changed here we need to check for its support in
                // CVKDevice.
                VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
                vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
                vkViewInfo.buffer(cvkCoordinateBuffer.GetBufferHandle());
                vkViewInfo.format(VK_FORMAT_R32G32B32A32_SFLOAT);
                vkViewInfo.offset(0);
                vkViewInfo.range(VK_WHOLE_SIZE);

                LongBuffer pBufferView = stack.mallocLong(1);
                ret = vkCreateBufferView(CVKDevice.GetVkDevice(), vkViewInfo, null, pBufferView);
                if (VkFailed(ret)) { return ret; }
                hCoordinateBufferView = pBufferView.get(0);            
                GetLogger().info("Created CVKGlyphTextureAtlas.hCoordinateBufferView: 0x%016X", hCoordinateBufferView);
            }               
        }

        return ret;    
    }     
        
    private void Destroy() {
        if (cvkAtlasImage != null) {
            cvkAtlasImage.Destroy();
            cvkAtlasImage = null;            
        }

        if (hAtlasSampler != VK_NULL_HANDLE) {
            vkDestroySampler(CVKDevice.GetVkDevice(), hAtlasSampler, null);    
            hAtlasSampler = VK_NULL_HANDLE;
        }
        
        if (cvkCoordinateBuffer != null) {
            cvkCoordinateBuffer.Destroy();
            cvkCoordinateBuffer = null;
        }
        
        if (cvkCoordinateStagingBuffer != null) {
            cvkCoordinateStagingBuffer.Destroy();
            cvkCoordinateStagingBuffer = null;
        }      
        
        glyphCount = 0;
    }      
       
    
    // ========================> Display <======================== \\
    
    public int DisplayUpdate() {
        int ret = VK_SUCCESS;
        
        if (needsSaveToFile) {
            ret = SaveToFile(fileToSave);
            needsSaveToFile = false;
        }
        
        if (glyphManager.getGlyphCount() > glyphCount) {
            Destroy();
            ret = CreateAtlas();
            if (VkFailed(ret)) { return ret; }
        }
        
        return ret;
    }
    
    
    // ========================> Tasks <======================== \\
    
    public CVKRenderUpdateTask TaskSaveToFile(File file) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            needsSaveToFile = true;
            fileToSave = file;
        };
    }
    
    
    // ========================> Helpers <======================== \\
    
    public void RenderTextAsLigatures(final String text, GlyphManager.GlyphStream glyphStream, GlyphStreamContext context) { 
        glyphManager.renderTextAsLigatures(text, glyphStream, context);
    }
    
    public float GetWidthScalingFactor() { return glyphManager.getWidthScalingFactor(); }
    
    public float GetHeightScalingFactor() { return glyphManager.getHeightScalingFactor(); }
    
    public int GetAtlasElementCount() { return glyphCount; }
    
    public long GetAtlasImageViewHandle() { return cvkAtlasImage.GetImageViewHandle(); }
    
    public long GetAtlasSamplerHandle() { return hAtlasSampler; }
    
    public long GetCoordinateBufferViewHandle() { return hCoordinateBufferView; }
            
    public long GetCoordinateBufferHandle() { return cvkCoordinateBuffer.GetBufferHandle(); }
    
    public long GetCoordinateBufferSize() { return cvkCoordinateBuffer.GetBufferSize(); }
      
    public int SaveToFile(File file) { return cvkAtlasImage.SaveToFile(file); }             
    
    private CVKGraphLogger GetLogger() { return CVKGraphLogger.GetStaticLogger(); }
}
