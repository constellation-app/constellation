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
package au.gov.asd.tac.constellation.visual.vulkan;

import au.gov.asd.tac.constellation.utilities.graphics.Vector3i;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKRenderer.debugging;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_NEVER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_SRGB;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D_ARRAY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;


public class CVKIconTextureAtlas {
    public static final int ICON_WIDTH = 256;
    public static final int ICON_HEIGHT = 256;
    public static final int ICON_COMPONENTS = 4; //ARGB, 1 byte each
    public static final int ICON_SIZE_PIXELS = ICON_WIDTH * ICON_HEIGHT;    
    public static final int ICON_SIZE_BYTES = ICON_SIZE_PIXELS * ICON_COMPONENTS;
    
    public static int textureWidth = 2048; //copied from JOGL but should be calculated, will mean adding more params to UBOs
    public static int textureHeight = 2048;      
    public static int iconsPerLayer = 0;
    public static int iconsPerRow = 0;
    public static int rowsPerLayer = 0;
    public static int textureLayers = 1;
    
    // These icons must be permanently present at these pre-defined indexes.
    // The shaders expect them to be there.
    public static final int HIGHLIGHTED_ICON_INDEX = 0;
    public static final String HIGHLIGHTED_ICON = DefaultIconProvider.HIGHLIGHTED.getExtendedName();
    public static final int UNKNOWN_ICON_INDEX = 1;
    public static final String UNKNOWN_ICON = DefaultIconProvider.UNKNOWN.getExtendedName();

    // Icons for drawing loops.
    public static final int LOOP_DIRECTED_ICON_INDEX = 2;
    public static final String LOOP_DIRECTED_ICON = DefaultIconProvider.LOOP_DIRECTED.getExtendedName();
    public static final int LOOP_UNDIRECTED_ICON_INDEX = 3;
    public static final String LOOP_UNDIRECTED_ICON = DefaultIconProvider.LOOP_UNDIRECTED.getExtendedName();

    // Noise indicator to be drawn when there are too many icons for the texture array.
    public static final int NOISE_ICON_INDEX = 4;
    public static final String NOISE_ICON = DefaultIconProvider.NOISE.getExtendedName();

    // Transparency.
    public static final int TRANSPARENT_ICON_INDEX = 5;
    public static final String TRANSPARENT_ICON = DefaultIconProvider.TRANSPARENT.getExtendedName();
    
    
    // Rather than a shared drawable like the GL renderer uses we'll just access this as a singleton.
    //private static final CVKIconTextureAtlas instance = new CVKIconTextureAtlas();
           
    // Instance members
    private final CVKDevice cvkDevice;
    private CVKImage cvkAtlasImage = null;
    private long hAtlasSampler = VK_NULL_HANDLE;
    private long hAtlasImageView = VK_NULL_HANDLE;
    private final LinkedHashMap<String, Integer> loadedIcons = new LinkedHashMap<>();
//    private boolean requiresReload = false;
    private int maxIcons = Short.MAX_VALUE; //replace this with a calculated value
    private int lastTransferedIconCount = 0;
    
    
    public long GetAtlasImageViewHandle() { return hAtlasImageView; }
    public long GetAtlasSamplerHandle() { return hAtlasSampler; }
    
    
    // This could be replaced with a templated Pair type
    private class IndexedConstellationIcon {
        public final int index;
        public final ConstellationIcon icon;
        IndexedConstellationIcon(int index, ConstellationIcon icon) {
            this.index = index;
            this.icon = icon;     
            CVKIconTextureAtlas.iconsPerRow = textureWidth/ICON_WIDTH;
            CVKIconTextureAtlas.rowsPerLayer = textureHeight/ICON_HEIGHT;
            CVKIconTextureAtlas.iconsPerLayer = CVKIconTextureAtlas.iconsPerRow * CVKIconTextureAtlas.rowsPerLayer;
            
        }
    }
    
    
    public CVKIconTextureAtlas(CVKDevice cvkDevice) {
        this.cvkDevice = cvkDevice;
        
        // These icons are guaranteed to be in the iconMap in this order.
        // They must be at these pre-defined indices so other code (in particular the shaders) can use them.
        // See *_INDEX constants above.
        for (final String iconName : new String[]{HIGHLIGHTED_ICON, UNKNOWN_ICON, LOOP_DIRECTED_ICON, LOOP_UNDIRECTED_ICON, NOISE_ICON, TRANSPARENT_ICON}) {
            AddIcon(iconName);
        }        
    }
    
    
    private void Destroy() {
        vkDestroyImageView(cvkDevice.GetDevice(), cvkAtlasImage.GetImageHandle(), null);
        //vkDestroyImage(cvkDevice.GetDevice(), textureArray.image, nullptr);
        cvkAtlasImage.Destroy();
        vkDestroySampler(cvkDevice.GetDevice(), hAtlasSampler, null);    
    }
    
    public int AddIcon(final String label) {
        final Integer iconIndex = loadedIcons.get(label);
        if (iconIndex == null) {
            final int index = loadedIcons.size();
            if (index >= maxIcons) {
                // Too many icons: return NOISE icon.
                return NOISE_ICON_INDEX;
            }

            loadedIcons.put(label, index);
            return index;
        }

        return iconIndex;
    }
    
    /**
     * Converts an icon index into column, row and layer indices
     * 
     * Example where 
     *   iconsPerRow   = 4
     *   rowsPerLayer  = 5
     *   iconsPerLayer = 20
     * 
     *  0  1  2  3
     *  4  5  6  7
      * 8  9 10 11
     * 12 13 14 15
     * 16 17 18 19
     * 
     * 20 21 22 23
     * 24 25 26 27
     * 28 29 30 31
     * 32 33 34 35
     * 36 37 38 39
     * 
     * 40 41 42 43
     * 44 45 46 47
     * 48 49 50 51
     * 52 53 54 55
     * 56 57 58 59
     * 
     * index = 27
     * x = 27 % 4	 = 3       
     * y = (27 % 20) / 4 = 1
     * z = 27 / 20       = 1
     * 
     * index = 38
     * x = 38 % 4        = 2       
     * y = (38 % 20) / 4 = 4
     * z = 38 / 20       = 1
     * 
     * index = 48
     * x = 48 % 4	 = 0       
     * y = (48 % 20) / 4 = 2
     * z = 48 / 20       = 2
     * 
     * @param index
     * @return vector of lookup indices
     */
    public Vector3i IndexToTextureIndices(int index) {
        return new Vector3i(index % CVKIconTextureAtlas.iconsPerRow,
                            (index % CVKIconTextureAtlas.iconsPerLayer) / CVKIconTextureAtlas.iconsPerRow,
                            index / iconsPerLayer);     
    }
    
    public long IndexToBufferOffset(int index) {
        long offset = 0;
        Vector3i texIndices = IndexToTextureIndices(index);  
        
        // add size of previous icons on this row
        offset += texIndices.getX() * ICON_SIZE_BYTES;
        
        // add size of previous rows on this layer
        offset += texIndices.getY() * iconsPerRow * ICON_SIZE_BYTES;
                
        // add size of previous layers
        offset += texIndices.getZ() * iconsPerLayer * ICON_SIZE_BYTES;
        
        int povCalc = index * ICON_SIZE_BYTES;
        assert(offset == povCalc);
        
        return offset;
    }
    
    private static boolean IsEmpty(ByteBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); ++i) {
            if (buffer.get(i) != 0) {
                return false;
            }
        }
        return true;
    }
    
    private int AddIconsToAtlas(List<IndexedConstellationIcon> icons) {
        int ret = VK_SUCCESS;
        
        
        try (MemoryStack stack = stackPush()) {
            // Allocate one big staging buffer for all of the images.  This will be inefficient if
            // this function is called often with a growing list of icons.  If that happens the best
            // solution will be to better manage the updating of the atlas rather than minimise the
            // staging buffer size.
            int requiredLayers = (icons.size() / iconsPerLayer) + 1;
            int stagingBufferSize = iconsPerLayer * requiredLayers * ICON_SIZE_BYTES;
            
            // Create destination image            
            cvkAtlasImage = CVKImage.Create(cvkDevice, 
                                            textureWidth, 
                                            textureHeight, 
                                            requiredLayers, 
                                            VK_FORMAT_R8G8B8A8_SRGB, //non-linear format with better use than 
                                            VK_IMAGE_TILING_OPTIMAL, //source data is linear, will this bite us?
                                            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                                            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            
            
//===================================================================================================================================            
            
           // Command to copy pixels and transition formats
            CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            ret = cvkCopyCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            checkVKret(ret);            
            
            // Transition image from undefined to transfer destination optimal
            ret = cvkAtlasImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            checkVKret(ret);            

            int numIcons = icons.size();
            List<CVKBuffer> iconStagingBuffers = new ArrayList<>(numIcons);
            List<VkBufferImageCopy.Buffer> bufferCopyBuffers = new ArrayList<>(numIcons);
            
            for (int iIcon = 0; iIcon < numIcons; ++iIcon) {
                IndexedConstellationIcon el = icons.get(iIcon);
                BufferedImage iconImage = el.icon.buildBufferedImage();     
               
                CVKBuffer cvkStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                              ICON_SIZE_BYTES, 
                                                              VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                              VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);  
                
               // Convert the buffered image if its not in our desired state.
                if (TYPE_4BYTE_ABGR != iconImage.getType()) {
                    BufferedImage convertedImg = new BufferedImage(iconImage.getWidth(), iconImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    convertedImg.getGraphics().drawImage(iconImage, 0, 0, null);
                    iconImage = convertedImg;
                }     
                int width = iconImage.getWidth();
                int height = iconImage.getHeight();
                assert(width <= ICON_WIDTH);
                assert(height <= ICON_HEIGHT);
                
                // Get pixel data into a direct buffer
                ByteBuffer pixels = ByteBuffer.wrap(((DataBufferByte) iconImage.getRaster().getDataBuffer()).getData());
                if (debugging) {
                    if (IsEmpty(pixels) && el.index != TRANSPARENT_ICON_INDEX) {
                        CVKLOGGER.warning(String.format("Icon %d is empty", el.index));
                    }
                }
                             
                // Copy pixels, note for undersized icons we need extra offsets to pad the top and sides                    
                if (width == ICON_WIDTH && height == ICON_HEIGHT) {
                    assert(pixels.capacity() == ICON_SIZE_BYTES);
                    cvkStagingBuffer.Put(pixels, 0, ICON_SIZE_BYTES);
                } else {
                    // Zero this buffer so undersized icons are padded with transparent pixels
                    cvkStagingBuffer.ZeroMemory();     
                    
                    // Offsets to centre the icon are in pixels
                    int colOffset = (ICON_WIDTH - width) / 2;
                    int colPadding = (ICON_WIDTH - width) - colOffset;
                    int rowOffset = (ICON_HEIGHT / 2) * ICON_WIDTH;
                    
                    // Adjust the start position to the right row
                    for (int iRow = rowOffset; iRow < height; ++iRow) {       
                        // offset to the start of this row
                        int writePos = iRow * ICON_WIDTH * ICON_COMPONENTS;
                        assert(((iRow + 1) * ICON_WIDTH * ICON_COMPONENTS) <= ICON_SIZE_BYTES);
                        
                        // offset from the start of the row to the start of the icon
                        writePos += colPadding * ICON_COMPONENTS;
                        cvkStagingBuffer.Put(pixels, (int)writePos, width * ICON_COMPONENTS);
                    }
                }
                iconStagingBuffers.add(cvkStagingBuffer);
                                              
                // Calculate offset into staging buffer for the current image layer
                Vector3i texIndices = IndexToTextureIndices(el.index);

                // Setup a buffer image copy structure for the current image layer
                VkBufferImageCopy.Buffer copyLayerBuffer = VkBufferImageCopy.callocStack(1, stack);
                VkBufferImageCopy copyLayer = copyLayerBuffer.get(0);
                copyLayer.bufferOffset(0);
                copyLayer.bufferRowLength(ICON_WIDTH);//0);    // Tightly packed
                copyLayer.bufferImageHeight(ICON_HEIGHT);//0);  // Tightly packed
                copyLayer.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                copyLayer.imageSubresource().mipLevel(0);
                copyLayer.imageSubresource().baseArrayLayer(texIndices.getW());
                copyLayer.imageSubresource().layerCount(1);
                copyLayer.imageOffset().set(texIndices.getU() * ICON_WIDTH,
                                            texIndices.getV() * ICON_HEIGHT, 
                                            0);
                copyLayer.imageExtent(VkExtent3D.callocStack(stack).set(textureWidth, textureHeight, requiredLayers));
                bufferCopyBuffers.add(copyLayerBuffer);
                
                // Copy staging buffer to atlas texture
                vkCmdCopyBufferToImage(cvkCopyCmd.GetVKCommandBuffer(),
                                       cvkStagingBuffer.GetBufferHandle(),
                                       cvkAtlasImage.GetImageHandle(),
                                       VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                       copyLayerBuffer);                
                
                ++iIcon;
            }
            
            // Now the image is populated, transition it for reading
            ret = cvkAtlasImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            checkVKret(ret);      
            
            // Ok nothing has actually happened yet, time to execute the transitions and copy
            ret = cvkCopyCmd.EndAndSubmit();
            checkVKret(ret);       
            
            // We've finished with the staging buffer
            //cvkStagingBuffer.Destroy();               

//===================================================================================================================================
            
//            CVKBuffer cvkStagingBuffer = CVKBuffer.Create(cvkDevice, 
//                                                          stagingBufferSize, 
//                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
//                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
//            
//            // Zero this buffer so undersized icons are padded with transparent pixels
//            cvkStagingBuffer.ZeroMemory();
//            
//            // Copy each icon's pixel data into the staging buffer
//            icons.forEach((IndexedConstellationIcon el) -> {               
//                BufferedImage iconImage = el.icon.buildBufferedImage();
//                
//                // Convert the buffered image if its not in our desired state.
//                if (TYPE_4BYTE_ABGR != iconImage.getType()) {
//                    BufferedImage convertedImg = new BufferedImage(iconImage.getWidth(), iconImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
//                    convertedImg.getGraphics().drawImage(iconImage, 0, 0, null);
//                    iconImage = convertedImg;
//                }     
//                int width = iconImage.getWidth();
//                int height = iconImage.getHeight();
//                assert(width <= ICON_WIDTH);
//                assert(height <= ICON_HEIGHT);
//                
//                // Get pixel data into a direct buffer
//                ByteBuffer pixels = ByteBuffer.wrap(((DataBufferByte) iconImage.getRaster().getDataBuffer()).getData());
//                if (debugging) {
//                    if (IsEmpty(pixels)) {
//                        CVKLOGGER.warning(String.format("Icon %d is empty", el.index));
//                    }
//                }
//                
//                // Calculate the offset to write these pixels
//                long offset = IndexToBufferOffset(el.index);
//                long endIndex = offset + ICON_SIZE_BYTES;
//                assert((offset + ICON_SIZE_BYTES) <= stagingBufferSize);
//                CVKLOGGER.info(String.format("Icon %d: %d -> %d", 
//                        el.index, offset, endIndex -1));
//                
//                // Copy pixels, note for undersized icons we need extra offsets to pad the top and sides                    
//                if (width == ICON_WIDTH && height == ICON_HEIGHT) {
//                    assert(pixels.capacity() == ICON_SIZE_BYTES);
//                    cvkStagingBuffer.Put(pixels, (int)offset, ICON_SIZE_BYTES);
//                } else {
//                    // Offsets to centre the icon are in pixels
//                    int colOffset = (ICON_WIDTH - width) / 2;
//                    int colPadding = (ICON_WIDTH - width) - colOffset;
//                    int rowOffset = (ICON_HEIGHT / 2) * ICON_WIDTH;
//                    
//                    // Adjust the start position to the right row
//                    offset += rowOffset * ICON_COMPONENTS;
//                    for (int iRow = 0; iRow < height; ++iRow) {                        
//                        offset += colPadding * ICON_COMPONENTS;
//                        cvkStagingBuffer.Put(pixels, (int)offset, width * ICON_COMPONENTS);
//                        offset += colPadding * ICON_COMPONENTS;
//                        assert(offset <= endIndex);
//                    }
//                }
//            });           
//            
//            // Source data is ready, now create structs to copy it layer by layer
//            VkBufferImageCopy.Buffer bufferCopyLayers = VkBufferImageCopy.callocStack(requiredLayers, stack);
//            for (int iLayer = 0; iLayer < requiredLayers; ++iLayer) {
//                VkBufferImageCopy bufferCopyLayer = bufferCopyLayers.get(iLayer);
//                
//                // Calculate offset into staging buffer for the current image layer
//                long offset = iLayer * iconsPerLayer * ICON_SIZE_BYTES;
//
//                // Setup a buffer image copy structure for the current image layer
//                bufferCopyLayer.bufferOffset(offset);
//                bufferCopyLayer.bufferRowLength(textureWidth);//0);    // Tightly packed
//                bufferCopyLayer.bufferImageHeight(textureHeight/ICON_HEIGHT);//0);  // Tightly packed
//                bufferCopyLayer.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
//                bufferCopyLayer.imageSubresource().mipLevel(0);
//                bufferCopyLayer.imageSubresource().baseArrayLayer(iLayer);
//                bufferCopyLayer.imageSubresource().layerCount(1);
//                bufferCopyLayer.imageOffset().set(0, 0, iLayer);
//                bufferCopyLayer.imageExtent(VkExtent3D.callocStack(stack).set(textureWidth, textureHeight, requiredLayers));                               
//            }      
            
//            // Command to copy pixels and transition formats
//            CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
//            ret = cvkCopyCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
//            checkVKret(ret);            
//            
//            // Transition image from undefined to transfer destination optimal
//            ret = cvkAtlasImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
//            checkVKret(ret);
//            
//            // Copy staging buffer to atlas texture
//            vkCmdCopyBufferToImage(cvkCopyCmd.GetVKCommandBuffer(),
//                                   cvkStagingBuffer.GetBufferHandle(),
//                                   cvkAtlasImage.GetImageHandle(),
//                                   VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
//                                   bufferCopyLayers);
//            
//            // Now the image is populated, transition it for reading
//            ret = cvkAtlasImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
//            checkVKret(ret);      
//            
//            // Ok nothing has actually happened yet, time to execute the transitions and copy
//            ret = cvkCopyCmd.EndAndSubmit();
//            checkVKret(ret);       
//            
//            // We've finished with the staging buffer
//            cvkStagingBuffer.Destroy();            
            
            // Image view
            VkImageViewCreateInfo vkViewInfo = VkImageViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            vkViewInfo.image(cvkAtlasImage.GetImageHandle());
            vkViewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D_ARRAY);
            vkViewInfo.format(cvkAtlasImage.GetFormat());
            vkViewInfo.subresourceRange().aspectMask(cvkAtlasImage.GetAspectMask());
            vkViewInfo.subresourceRange().baseMipLevel(0);
            vkViewInfo.subresourceRange().levelCount(1);
            vkViewInfo.subresourceRange().baseArrayLayer(0);
            vkViewInfo.subresourceRange().layerCount(requiredLayers);

            LongBuffer pImageView = stack.mallocLong(1);
            ret = vkCreateImageView(cvkDevice.GetDevice(), vkViewInfo, null, pImageView);
            checkVKret(ret);
            hAtlasImageView = pImageView.get(0);   
            
            // Create a sampler to match the image.  Note the sampler allows us to sample
            // an image but isn't tied to a specific image, note the lack of image or 
            // imageview parameters below.
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
            ret = vkCreateSampler(cvkDevice.GetDevice(), vkSamplerCreateInfo, null, pTextureSampler);
            checkVKret(ret);
            hAtlasSampler = pTextureSampler.get(0);
            assert(hAtlasSampler != VK_NULL_HANDLE);                       
        }
        
        return ret;
    }
    
    private void DestroyAtlas() {
        
    }
    
    
    public int Init() {
        assert(lastTransferedIconCount == 0);
        assert(cvkAtlasImage == null);
        assert(hAtlasSampler == VK_NULL_HANDLE);
        assert(hAtlasImageView == VK_NULL_HANDLE);
        
        int ret = VK_SUCCESS;
        if (loadedIcons.size() > 0) {
            List<IndexedConstellationIcon> allIcons = new ArrayList<>();
            loadedIcons.entrySet().forEach(entry -> {                
                allIcons.add(new IndexedConstellationIcon(entry.getValue(), IconManager.getIcon(entry.getKey()))); 
            });
                        
            ret = AddIconsToAtlas(allIcons);
            checkVKret(ret);
            lastTransferedIconCount = loadedIcons.size();  
        }
        return ret;
    }
    
    public int DisplayUpdate() {
        int ret = VK_SUCCESS;
        
        // TODO_TT: This needs to be synchronised
        // Maybe not, I think the icon cache is only ever added to.  In C++ I'd worry about
        // relocations invalidating pointers but I don't think that's an issue in Java. Also
        // this wasn't synchronised in GLTools
        
        if (loadedIcons.size() > lastTransferedIconCount) {
//            final int nIcons = loadedIcons.size();
//            final List<ConstellationIcon> iconList = new ArrayList<>(nIcons);
//            for (int i = 0; i < nIcons; i++) {
//                iconList.add(null);
//            }

            // We're only interested icons we haven't consumed yet
            //List<IndexedConstellationIcon> newIcons = new ArrayList<>();
            List<IndexedConstellationIcon> allIcons = new ArrayList<>();
            loadedIcons.entrySet().forEach(entry -> {                
                //if (entry.getValue() > lastTransferedIconIndex) {
                //    newIcons.add(new IndexedConstellationIcon(entry.getValue(), IconManager.getIcon(entry.getKey()))); 
                //}
                allIcons.add(new IndexedConstellationIcon(entry.getValue(), IconManager.getIcon(entry.getKey()))); 
            });
            
            //TODO_TT: we have to wait until all of the renderer's swap chain frames are waiting,
            // otherwise we will be ripping out the atlas image, view and sampler from under any
            // in flight frames.
            DestroyAtlas();
            
            ret = AddIconsToAtlas(allIcons);
            checkVKret(ret);
            lastTransferedIconCount = loadedIcons.size();
        }
        
        return ret;
    }
    
    
/**
     * Load an array of icon textures.
     * <p>
     * We assume that the textures being loaded are icons, and therefore are
     * roughly the same size (with a maximum of (width,height).
     * <p>
     * The array is limited to GL_MAX_ARRAY_TEXTURE_LAYERS layers. This can be
     * fairly low (512 on low-end systems), so icons are loaded into an 8x8 icon
     * matrix in each layer, thus giving a maximum of 512x8x8=32768 icons. (This
     * assumes that GL_MAX_3D_TEXTURE_SIZE is big enough to take that many
     * pixels. With the current icon size of 256x256, then
     * GL_MAX_3D_TEXTURE_SIZE must be at least 2048.)
     * <p>
     * Icons that are smaller than (width,height) are offset so they are
     * centred, so the shader can just draw the icons without worrying about
     * where in the texture they are.
     * <p>
     * It appears that the images must have a row length that is a multiple of
     * four. This is probably due to the particular format we're using, and
     * could probably be worked around, but the simple fix is to check your row
     * length.
     *
     * @param glCurrent the current OpenGL context.
     * @param icons a list of icons that need to added to the buffer.
     * @param width the width of each icon.
     * @param height the height of each icon.
     *
     * @return the id of the texture buffer.
     */
//    public static int loadSharedIconTextures(final List<ConstellationIcon> icons, final int width, final int height) {
//        final int[] v = new int[1];
//        glCurrent.glGetIntegerv(GL3.GL_MAX_ARRAY_TEXTURE_LAYERS, v, 0);
//        final int maxIcons = v[0] * 64;
//        if (icons.size() > maxIcons) {
//            System.out.printf("****\n**** Warning: nIcons %d > GL_MAX_ARRAY_TEXTURE_LAYERS %d\n****\n", icons.size(), maxIcons);
//        }
//
//        final int nIcons = Math.min(icons.size(), maxIcons);
//
//        glCurrent.getContext().release();
//        final GL3 gl = (GL3) SharedDrawable.getSharedAutoDrawable().getGL();
//        final int result = gl.getContext().makeCurrent();
//        if (result == GLContext.CONTEXT_NOT_CURRENT) {
//            glCurrent.getContext().makeCurrent();
//            throw new RenderException("Could not make texture context current.");
//        }
//
//        final int[] textureName = new int[1];
//        try {
//            textureName[0] = SharedDrawable.getIconTextureName();
//            gl.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, textureName[0]);
//            gl.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
//            gl.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
//            gl.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
//            gl.glTexParameteri(GL3.GL_TEXTURE_2D_ARRAY, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
//            gl.glTexImage3D(GL3.GL_TEXTURE_2D_ARRAY, 0, GL.GL_RGBA, width * 8, height * 8, (nIcons + 63) / 64, 0, GL.GL_RGBA, GL3.GL_UNSIGNED_BYTE, null);
//
//            final Iterator<ConstellationIcon> iconIterator = icons.iterator();
//            for (int i = 0; i < nIcons; i++) {
//                final ConstellationIcon icon = iconIterator.next();
//                try {
//                    BufferedImage iconImage = icon.buildBufferedImage();
//
//                    if (iconImage != null) {
//                        // Appears to be a bug in JOGL where texture provider for PNG files does not flip the texture.
//                        final TextureData data = AWTTextureIO.newTextureData(gl.getGLProfile(), iconImage, false);
//
//                        if (data.getWidth() > width || data.getHeight() > height) {
//                            throw new RenderException(String.format("Image %d is too large (width %d>%d, height %d>%d)", i, data.getWidth(), width, data.getHeight(), height));
//                        }
//
//                        // Offset each icon into an 8x8 matrix.
//                        // There are multiple icons in each
//                        // Allow for icons that are smaller than width,height.
//                        final int xoffset = (width - data.getWidth()) / 2 + (width * (i & 7));
//                        final int yoffset = (height - data.getHeight()) / 2 + (height * ((i >>> 3) & 7));
//                        final int zoffset = i >>> 6;
//                        gl.glTexSubImage3D(GL3.GL_TEXTURE_2D_ARRAY, 0, xoffset, yoffset, zoffset, data.getWidth(), data.getHeight(), 1, data.getPixelFormat(), GL3.GL_UNSIGNED_BYTE, data.getBuffer());
//                        data.destroy();
//                    }
//                } catch (final RuntimeException ex) {
//                    System.out.printf("##%n## GLTools.loadTextures() icon %d throwable: %s%n##%n", i, ex);
//                    LOGGER.log(Level.SEVERE, null, ex);
//                }
//            }
//        } finally {
//            gl.getContext().release();
//            glCurrent.getContext().makeCurrent();
//        }
//
//        return textureName[0];
//    }
//
//    // These icons must be permanently present at these pre-defined indexes.
//    // The shaders expect them to be there.
//    public static final int HIGHLIGHTED_ICON_INDEX = 0;
//    public static final String HIGHLIGHTED_ICON = DefaultIconProvider.HIGHLIGHTED.getExtendedName();
//    public static final int UNKNOWN_ICON_INDEX = 1;
//    public static final String UNKNOWN_ICON = DefaultIconProvider.UNKNOWN.getExtendedName();
//
//    // Icons for drawing loops.
//    public static final int LOOP_DIRECTED_ICON_INDEX = 2;
//    public static final String LOOP_DIRECTED_ICON = DefaultIconProvider.LOOP_DIRECTED.getExtendedName();
//    public static final int LOOP_UNDIRECTED_ICON_INDEX = 3;
//    public static final String LOOP_UNDIRECTED_ICON = DefaultIconProvider.LOOP_UNDIRECTED.getExtendedName();
//
//    // Noise indicator to be drawn when there are too many icons for the texture array.
//    public static final int NOISE_ICON_INDEX = 4;
//    public static final String NOISE_ICON = DefaultIconProvider.NOISE.getExtendedName();
//
//    // Transparency.
//    public static final int TRANSPARENT_ICON_INDEX = 5;
//    public static final String TRANSPARENT_ICON = DefaultIconProvider.TRANSPARENT.getExtendedName();
//
//    /**
//     * Singleton holder of list of used icons.
//     * <p>
//     * As new icon names are seen in the graph, they are added to this data
//     * structure, which maintains a mapping between an icon name and its index.
//     * <p>
//     * Note: a LinkedHashMap is used here to maintain the insertion order.
//     * Because the texture array that uses this is shared between multiple
//     * drawables, and some icons must be at predefined indexes (for instance
//     * "highlighted" must be at index 0, "unknown" at index 1), the order of the
//     * icons must not change: a drawable that uses an icon at index 17 (for
//     * example) can't have that icon changing due to a different drawable being
//     * created. Therefore, whenever new icons are added, they are always
//     * appended.
//     */
//    public static final class LoadedIconHelper {
//
//        private final LinkedHashMap<String, Integer> loadedIcons;
//        private boolean requiresReload;
//
//        // We use a texture array to store icons.
//        // If we have too many icons (more than GL_MAX_ARRAY_TEXTURE_LAYERS), then OpenGL won't like it.
//        // What happens next is probably driver/hardware dependent, but one possibility is just displaying
//        // whatever icon it feels like. Yuck.
//        // We don't want this to happen, so we have a maximum number of icons.
//        // If an attempt is made to add any more icons, you'll get the noise icon as an indicator if icon overflow.
//        private int maxNIcons;
//
//        private LoadedIconHelper() {
//            maxNIcons = Integer.MAX_VALUE;
//            loadedIcons = new LinkedHashMap<>();
//
//            // These icons are guaranteed to be in the iconMap in this order.
//            // They must be at these pre-defined indices so other code (in particular the shaders) can use them.
//            // See *_INDEX constants above.
//            for (final String iconName : new String[]{HIGHLIGHTED_ICON, UNKNOWN_ICON, LOOP_DIRECTED_ICON, LOOP_UNDIRECTED_ICON, NOISE_ICON, TRANSPARENT_ICON}) {
//                addIcon(iconName);
//            }
//        }
//
//        /**
//         * Add an icon label to the index map and return the index of that icon.
//         * <p>
//         * If the label already exists, return the existing index. Null labels
//         * and empty labels (ie "") return the index of the transparent icon.
//         * Therefore, a valid icon index (&lt;=0 &amp;&amp; &gt;=MAX_ICON_INDEX)
//         * will always be returned.
//         *
//         * @param label The index of an icon.
//         *
//         * @return the index of the icon.
//         */
//        public int addIcon(final String label) {
//            final Integer iconIndex = loadedIcons.get(label);
//            if (iconIndex == null) {
//                final int index = loadedIcons.size();
//                if (index >= maxNIcons) {
//                    // Too many icons: return NOISE icon.
//                    return NOISE_ICON_INDEX;
//                }
//
//                loadedIcons.put(label, index);
//                requiresReload = true;
//                return index;
//            }
//
//            return iconIndex;
//        }
//
//        public boolean isEmpty() {
//            return loadedIcons.isEmpty();
//        }
//
//        public int size() {
//            return loadedIcons.size();
//        }
//
//        public void reset() {
//            loadedIcons.clear();
//            requiresReload = false;
//        }
//
//        public int getMaximumIcons() {
//            return maxNIcons;
//        }
//
//        public void setMaximumTextureLayers(final int maxTextureLayers) {
//            this.maxNIcons = maxTextureLayers * 64;
//        }
//    }
//
//    public static final LoadedIconHelper LOADED_ICON_HELPER = new LoadedIconHelper();
//
//    /**
//     * Load the icon textures into a texture array.
//     * <p>
//     * This texture array is shared amongst all of the OpenGL drawables, so once
//     * an icon has been added to the list of icons, its index must not change.
//     *
//     * @param glCurrent the current OpenGL context.
//     * @param width the width of each icon.
//     * @param height the height of each icon.
//     *
//     * @return the id of the texture buffer.
//     */
//    public static int loadSharedIconTextures(final GL3 glCurrent, final int width, final int height) {
//        // Do we have new icons to be loaded?
//        // If so, reload the lot.
//        if (LOADED_ICON_HELPER.requiresReload) {
//            final int nIcons = LOADED_ICON_HELPER.loadedIcons.size();
//            final List<ConstellationIcon> iconList = new ArrayList<>(nIcons);
//            for (int i = 0; i < nIcons; i++) {
//                iconList.add(null);
//            }
//
//            for (final Map.Entry<String, Integer> entry : LOADED_ICON_HELPER.loadedIcons.entrySet()) {
//                final String iconLabel = entry.getKey();
//                final int iconIndex = entry.getValue();
//
//                ConstellationIcon icon = IconManager.getIcon(iconLabel);
//                if (icon == null) {
//                    icon = DefaultIconProvider.UNKNOWN;
//                }
//
//                iconList.set(iconIndex, icon);
//            }
//
//            LOADED_ICON_HELPER.requiresReload = false;
//
//            final long t0 = System.currentTimeMillis();
//            final int iconTextureArray = loadSharedIconTextures(glCurrent, iconList, width, height);
//            final long t1 = System.currentTimeMillis();
//            LOGGER.log(Level.FINE, "Time to load icon textures: {0} msec\n", (t1 - t0));
//
//            return iconTextureArray;
//        }
//
//        return SharedDrawable.getIconTextureName();
//    }
}
