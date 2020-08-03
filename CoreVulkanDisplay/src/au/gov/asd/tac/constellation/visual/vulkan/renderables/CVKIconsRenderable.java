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
package au.gov.asd.tac.constellation.visual.vulkan.renderables;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4i;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_COMPILATION;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_MODULE;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.LoadFileToDirectBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8_SINT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;
import static org.lwjgl.vulkan.VK10.vkCreateBufferView;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyBufferView;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;


public class CVKIconsRenderable extends CVKRenderable{
    // Static so we recreate descriptor layouts and shaders for each graph
    private static boolean staticInitialised = false;
    
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    private static final int POSITION_STRIDE = 8 * Float.BYTES;
    private static final int FLAGS_STRIDE = Byte.BYTES;
    public static final int SELECTED_BIT = 1;
    public static final int DIMMED_BIT = 2;
    
    private long hVertexShader = VK_NULL_HANDLE;
    private long hGeometryShader = VK_NULL_HANDLE;
    private long hFragmentShader = VK_NULL_HANDLE;
    private static ByteBuffer vsBytes = null;
    private static ByteBuffer gsBytes = null;
    private static ByteBuffer fsBytes = null;    
    
    private long hDescriptorLayout = VK_NULL_HANDLE; 
    private long hPipelineLayout = VK_NULL_HANDLE; 

    // Resource states. The atlas sampler handle is cached so we know the atlas 
    // state, ie if it doesn't match the one returned by the atlas, we know we
    // need to recreate our descriptors to point to the new one.
    private CVKRenderableResourceState vertexBufferState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState positionBufferState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexFlagsBufferState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexUBOState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState geometryUBOState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState fragmentUBOState = CVK_RESOURCE_CLEAN;
    private long hAtlasSampler = VK_NULL_HANDLE;
    private long hAtlasImageView = VK_NULL_HANDLE;    
        
    // Resources recreated with the swap chain (dependent on the image count)    
    private LongBuffer pDescriptorSets = null; 
    private List<Long> pipelines = null;
    private List<CVKCommandBuffer> commandBuffers = null;    
    private List<CVKBuffer> vertexBuffers = null;   
    private List<CVKBuffer> vertexUniformBuffers = null;
    private List<CVKBuffer> geometryUniformBuffers = null;    
    private List<CVKBuffer> fragmentUniformBuffers = null;        
    
    // The UBO staging buffers are a know size so created outside user events
    private CVKBuffer cvkVertexUBStagingBuffer = null;
    private CVKBuffer cvkGeometryUBStagingBuffer = null;
    private CVKBuffer cvkFragmentUBStagingBuffer = null;
    private VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject();
    private FragmentUniformBufferObject fragmentUBO = new FragmentUniformBufferObject();    
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;
    private CVKBuffer cvkPositionStagingBuffer = null;
    private CVKBuffer cvkPositionBuffer = null;    
    private CVKBuffer cvkVertexFlagsStagingBuffer = null;
    private CVKBuffer cvkVertexFlagsBuffer = null;    
    private long hPositionBufferView = VK_NULL_HANDLE;
    private long hVertexFlagsBufferView = VK_NULL_HANDLE;
    
    // The vertex, position and flags staging buffers are used by both the event
    // thread and rendering thread so much be synchronised.
    private ReentrantLock vertexStagingBufferLock = new ReentrantLock();
    private ReentrantLock positionStagingBufferLock = new ReentrantLock();
    private ReentrantLock vertexFlagsStagingBufferLock = new ReentrantLock();
    
    
    /*
    
    What resources do icons have
    - vs: vertex buffer (icon indexes and bkg colour)
    - vs: xyzw buffer (icon positions)
    - vs: vertex ubo (camera vars)
    - gs: flag buffer (vertex flags)
    - gs: geometry ubo (proj mtx and render data)
    - fs: atlas texture sampler (
    - fs: fragment ubo (hit test flag)
    */
    
    
    // ========================> Classes <======================== \\
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int SIZEOF = 4 * Float.BYTES + 4 * Integer.BYTES;
        private static final int OFFSETOF_DATA = 4 * Float.BYTES;
        private static final int OFFSET_BKGCLR = 0;
        private static final int BINDING = 0;
        private Vector4f backgroundIconColour = new Vector4f();
        private Vector4i data = new Vector4i();
        
        public Vertex() {}

        public Vertex(Vector4i inData, Vector4f inColour) {
            data = inData;
            backgroundIconColour = inColour;
        }
        
        public void SetBackgroundIconColour(ConstellationColor colour) {
            backgroundIconColour.a[0] = colour.getRed();
            backgroundIconColour.a[1] = colour.getGreen();
            backgroundIconColour.a[2] = colour.getBlue();
        }
        
        public void SetVertexVisibility(float visibility) {
            backgroundIconColour.a[3] = visibility;
        }
        
        public void SetIconData(int mainIconIndices, int decoratorWestIconIndices, int decoratorEastIconIndices, int vertexIndex) {
            data.set(mainIconIndices, decoratorWestIconIndices, decoratorEastIconIndices, vertexIndex);
        }   
        
        public void CopyTo(ByteBuffer buffer) {
            buffer.putFloat(backgroundIconColour.a[0]);
            buffer.putFloat(backgroundIconColour.a[1]);
            buffer.putFloat(backgroundIconColour.a[2]);
            buffer.putFloat(backgroundIconColour.a[3]);
            buffer.putInt(data.a[0]);
            buffer.putInt(data.a[1]);
            buffer.putInt(data.a[2]);
            buffer.putInt(data.a[3]);              
        }
        
//        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
//            for (Vertex vertex : vertices) {  
//                vertex.CopyTo(buffer);               
//            }
//        }        

        /**
         * A VkVertexInputBindingDescription defines the rate at which data is
         * consumed by vertex shader (per vertex or per instance).  
         * The input rate determines whether to move to the next data entry after
         * each vertex or after each instance.
         * The binding description also defines the vertex stride, the number of
         * bytes that must be stepped from vertex n-1 to vertex n.
         * 
         * @return Binding description for the FPS vertex type
         */
        private static VkVertexInputBindingDescription.Buffer GetBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            // If we bind multiple vertex buffers with different descriptions
            // this is the index of this description occupies in the array of
            // bound descriptions.
            bindingDescription.binding(BINDING);
            bindingDescription.stride(Vertex.SIZEOF);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            return bindingDescription;
        }
        
        /**
         * A VkVertexInputAttributeDescription describes each element int the
         * vertex buffer.
         * binding:  matches the binding member of VkVertexInputBindingDescription
         * location: corresponds to the layout(location = #) in the vertex shader
         *           for this element (0 for data, 1 for bkgClr).
         * format:   format the shader will interpret this as.
         * offset:   bytes from the start of the vertex this attribute starts at
         * 
         * @return 
         */
        private static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // backgroundIconColor
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            posDescription.offset(OFFSETOF_DATA);

            // data
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SINT);
            colorDescription.offset(OFFSET_BKGCLR);

            return attributeDescriptions.rewind();
        }
    }  
    
    private static class VertexUniformBufferObject {
        private static final int SIZEOF = (16 + 1 + 1 + 1) * Float.BYTES;

        public Matrix44f mvMatrix = new Matrix44f();
        public float morphMix = 0;
        public float visibilityLow = 0;
        public float visibilityHigh = 0;                 
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(mvMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(morphMix);
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);
        }         
    }
    
    private static class GeometryUniformBufferObject {
        private static final int SIZEOF = 16 * Float.BYTES + 1 * Float.BYTES + 16 * Float.BYTES + 1 * Integer.BYTES;

        public Matrix44f pMatrix = new Matrix44f();
        public float pixelDensity = 0;
        public Matrix44f highlightColor = Matrix44f.identity();
        public int drawHitTest = 0;           
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(pixelDensity);
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(highlightColor.get(iRow, iCol));
                }
            }            
            buffer.putInt(drawHitTest);
        }         
    }  
    
    private static class FragmentUniformBufferObject {
        private static final int SIZEOF = 1 * Integer.BYTES;

        public int drawHitTest = 0;           
        
        private void CopyTo(ByteBuffer buffer) {
            buffer.putInt(drawHitTest);
        }         
    }    
                      
    
    // ========================> Static init <======================== \\
    
    private static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try {
            if (vsBytes == null) {
                vsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.vs.spv");
                if (vsBytes == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to load precompiled CVKIconsRenderable shader: VertexIcon.vs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (gsBytes == null) {
                gsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.gs.spv");
                if (gsBytes == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to load precompiled CVKIconsRenderable shader: VertexIcon.gs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (fsBytes == null) {
                fsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.fs.spv");
                if (fsBytes == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to load precompiled CVKIconsRenderable shader: VertexIcon.fs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
         
        } catch (IOException e) {
            CVKLOGGER.log(Level.SEVERE, "Failed to compile FPSRenderable shaders: {0}", e.toString());
            ret = CVK_ERROR_SHADER_COMPILATION;
        }
        
        return ret;            
    }  

    public static int StaticInitialise(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        if (!staticInitialised) {
            ret = LoadShaders(cvkDevice);
            if (VkFailed(ret)) { return ret; }            
            staticInitialised = true;
        }
        return ret;
    }   
    
    public static void DestroyStaticResources() {
        if (vsBytes != null) {
            MemoryUtil.memFree(vsBytes);
            vsBytes = null;
        }       
        
        if (gsBytes != null) {
            MemoryUtil.memFree(gsBytes);
            gsBytes = null;
        }

        if (fsBytes != null) {
            MemoryUtil.memFree(fsBytes);
            fsBytes = null;
        }
        
        staticInitialised = false;
    }
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKIconsRenderable(CVKVisualProcessor inParent) {
        parent = inParent;
    }  
    
    private int CreateShaderModules() {
        int ret = VK_SUCCESS;
        
        try{           
            hVertexShader = CVKShaderUtils.createShaderModule(vsBytes, cvkDevice.GetDevice());
            if (hVertexShader == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: VertexIcon.vs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hGeometryShader = CVKShaderUtils.createShaderModule(gsBytes, cvkDevice.GetDevice());
            if (hGeometryShader == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: VertexIcon.gs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hFragmentShader = CVKShaderUtils.createShaderModule(fsBytes, cvkDevice.GetDevice());
            if (hFragmentShader == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: VertexIcon.fs");
                return CVK_ERROR_SHADER_MODULE;
            }
        } catch(Exception ex){
            CVKLOGGER.log(Level.SEVERE, "Failed to create shader module CVKIconsRenderable: {0}", ex.toString());
            ret = CVK_ERROR_SHADER_MODULE;
            return ret;
        }
        
        CVKLOGGER.log(Level.INFO, "Shader modules created for CVKIconsRenderable class");
        return ret;
    }   
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                    VertexUniformBufferObject.SIZEOF,
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        cvkVertexUBStagingBuffer.DEBUGNAME = "CVKIconsRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer";   
        cvkGeometryUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                      GeometryUniformBufferObject.SIZEOF,
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        cvkGeometryUBStagingBuffer.DEBUGNAME = "CVKIconsRenderable.CreateUBOStagingBuffers cvkGeometryUBStagingBuffer"; 
        cvkFragmentUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                      FragmentUniformBufferObject.SIZEOF,
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        cvkFragmentUBStagingBuffer.DEBUGNAME = "CVKIconsRenderable.CreateUBOStagingBuffers cvkFragmentUBStagingBuffer";                                
    }
    
    @Override
    public int Initialise(CVKDevice cvkDevice) {
        CVKAssert(cvkDevice != null);
        // Check for double initialisation
        CVKAssert(hVertexShader == VK_NULL_HANDLE);
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);
        
        int ret;        
        this.cvkDevice = cvkDevice;
        
        ret = CreateShaderModules();
        if (VkFailed(ret)) { return ret; }             
        
        ret = CreateDescriptorLayout();
        if (VkFailed(ret)) { return ret; }   
                
        CreateUBOStagingBuffers();
        
        return ret;
    }        
    
    private void DestroyStagingBuffers() {        
        if (cvkVertexStagingBuffer != null) {
            try {
                vertexStagingBufferLock.lock();
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            } finally {
                vertexStagingBufferLock.unlock();
            }
        }
        if (cvkPositionStagingBuffer != null) {
            try {
                positionStagingBufferLock.lock();
                cvkPositionStagingBuffer.Destroy();
                cvkPositionStagingBuffer = null;
            } finally {
                positionStagingBufferLock.unlock();
            }
        }
        if (cvkVertexFlagsStagingBuffer != null) {
            try {
                vertexFlagsStagingBufferLock.lock();
                cvkVertexFlagsStagingBuffer.Destroy();
                cvkVertexFlagsStagingBuffer = null;
            } finally {
                vertexFlagsStagingBufferLock.unlock();
            }
        }
        
        if (cvkVertexUBStagingBuffer != null) {
            cvkVertexUBStagingBuffer.Destroy();
            cvkVertexUBStagingBuffer = null;
        }
        if (cvkGeometryUBStagingBuffer != null) {
            cvkGeometryUBStagingBuffer.Destroy();
            cvkGeometryUBStagingBuffer = null;
        }        
        if (cvkFragmentUBStagingBuffer != null) {
            cvkFragmentUBStagingBuffer.Destroy();
            cvkFragmentUBStagingBuffer = null;
        }        
    }
    
    @Override
    public void Destroy() {
        DestroyVertexBuffers();
        DestroyPositionBuffer();
        DestroyVertexFlagsBuffer();
        DestroyUniformBuffers();
        DestroyDescriptorSets();
        DestroyDescriptorLayout();
        DestroyCommandBuffers();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyCommandBuffers();
        DestroyStagingBuffers();
        
        CVKAssert(vertexBuffers == null);
        CVKAssert(cvkPositionBuffer == null);
        CVKAssert(hPositionBufferView == VK_NULL_HANDLE); 
        CVKAssert(cvkVertexFlagsBuffer == null);
        CVKAssert(hVertexFlagsBufferView == VK_NULL_HANDLE); 
        CVKAssert(vertexUniformBuffers == null);
        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(fragmentUniformBuffers == null); 
        CVKAssert(pDescriptorSets == null);
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);  
        CVKAssert(commandBuffers == null);        
        CVKAssert(pipelines == null);
        CVKAssert(hPipelineLayout == VK_NULL_HANDLE);           
    }
    
       
    // ========================> Swap chain <======================== \\
    
    private int CreateSwapChainResources() { 
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkDescriptorPool != null);
        int ret;
        
        try (MemoryStack stack = stackPush()) {  
            
            // We only need to recreate these resources if the number of images in 
            // the swapchain changes or if this is the first call after the initial
            // swapchain is created.
            if (swapChainImageCountChanged) {                               
                ret = CreateUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }

                ret = CreateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; } 

                ret = CreateVertexBuffers();
                if (VkFailed(ret)) { return ret; }   

                ret = CreateCommandBuffers();
                if (VkFailed(ret)) { return ret; }            

                ret = CreatePipelines();
                if (VkFailed(ret)) { return ret; }            
            }
//            } else {
//                
//                // This is the resize path, image count is unchanged.  We need to recreate
//                // pipelines as Vulkan doesn't have a good mechanism to update them and as
//                // they define the viewport and scissor rect they are now out of date.  We
//                // also need to update the uniform buffer as a new image size will mean a
//                // different position for our FPS.  After updating the uniform buffers we
//                // need to update the descriptor sets that bind the uniform buffers as well.                  
//                ret = CreatePipelines();
//                if (VkFailed(ret)) { return ret; }                           
//
//                ret = UpdateUniformBuffers(stack);
//                if (VkFailed(ret)) { return ret; }
//                
//                ret = UpdateDescriptorSets(stack);
//                if (VkFailed(ret)) { return ret; }              
//            }
        }
        
        swapChainImageCountChanged = false;
        swapChainResourcesDirty = false;
        
        return VK_SUCCESS;
    }    
    
    @Override
    public int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines != null && swapChainImageCountChanged) {  
            DestroyVertexBuffers();
            DestroyUniformBuffers();
            DestroyDescriptorSets();
            DestroyCommandBuffers();     
            DestroyPipelines();
            DestroyCommandBuffers();                                  
        }
//        } else {
//            //TODO_TT: asks Hydra41 about this
//            DestroyPipelines();
//        }
        
        return VK_SUCCESS; 
    }        
    
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffers() {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
    
        // We can only create vertex buffers if we have something to put in them
        if (vertexCount > 0) {
            int imageCount = cvkSwapChain.GetImageCount();               
            vertexBuffers = new ArrayList<>();
            
            int vertexBufferSizeBytes = CVKIconsRenderable.Vertex.SIZEOF * vertexCount;
            for (int i = 0; i < imageCount; ++i) {   
                CVKBuffer cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                             vertexBufferSizeBytes,
                                                             VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
                cvkVertexBuffer.DEBUGNAME = String.format("CVKIconsRenderable cvkVertexBuffer %d", i);
                vertexBuffers.add(cvkVertexBuffer);        
            }

            // Populate them with some values
            return UpdateVertexBuffers();
        }
        
        return ret;  
    }    
    
    private int UpdateVertexBuffers() {
        VerifyInRenderThread();
        CVKAssert(cvkVertexStagingBuffer != null);
        CVKAssert(vertexBuffers != null);
        CVKAssert(vertexBuffers.size() > 0);
        CVKAssert(cvkVertexStagingBuffer.GetBufferSize() == vertexBuffers.get(0).GetBufferSize());
        int ret = VK_SUCCESS;
        
        try {
            vertexStagingBufferLock.lock();
            for (int i = 0; i < vertexBuffers.size(); ++i) {   
                CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
                cvkVertexBuffer.CopyFrom(cvkVertexStagingBuffer);
            }
        } finally {
            vertexStagingBufferLock.unlock();
        }     
        
        // Note the staging buffer is not freed as we can simplify the update tasks
        // by just updating it and then copying it over again during DisplayUpdate().
        vertexBufferState = CVK_RESOURCE_CLEAN;
        
        return ret;         
    }  
    
    @Override
    public int GetVertexCount() { return 0; }// vertexCount; }      
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }    
    
    
    // ========================> Texel buffers <======================== \\
    
    private int CreatePositionBuffer() {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkPositionBuffer == null);
        VerifyInRenderThread();        
        int ret = VK_SUCCESS;
                
        cvkPositionBuffer = CVKBuffer.Create(cvkDevice, 
                                              cvkPositionStagingBuffer.GetBufferSize(), 
                                              VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                              VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        cvkPositionBuffer.DEBUGNAME = "CVKIconsRenderable.CreatePositionBuffer cvkPositionBuffer";       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R32G32B32A32_SFLOAT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkPositionBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(cvkDevice.GetDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hPositionBufferView = pBufferView.get(0);
        }       
        
        return UpdatePositionBuffer();
    }
    
    private int UpdatePositionBuffer() {
        int ret;
        
        try {
            positionStagingBufferLock.lock();
        
            ret = cvkPositionBuffer.CopyFrom(cvkPositionStagingBuffer);             
            positionBufferState = CVK_RESOURCE_CLEAN;
        } finally {
            positionStagingBufferLock.unlock();
        }
        
        return ret;                               
    }   
    
    private void DestroyPositionBuffer() {
        if (cvkPositionBuffer != null) {
            cvkPositionBuffer.Destroy();
            cvkPositionBuffer = null;
        }   
        if (hPositionBufferView != VK_NULL_HANDLE) {
            vkDestroyBufferView(cvkDevice.GetDevice(), hPositionBufferView, null);
            hPositionBufferView = VK_NULL_HANDLE;
        }
    }
    
    private int CreateVertexFlagsBuffer() {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkVertexFlagsBuffer == null);
        VerifyInRenderThread();        
        int ret = VK_SUCCESS;
        
        cvkVertexFlagsBuffer = CVKBuffer.Create(cvkDevice, 
                                                cvkVertexFlagsStagingBuffer.GetBufferSize(), 
                                                VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        cvkVertexFlagsBuffer.DEBUGNAME = "CVKIconsRenderable.CreateVertexFlagsBuffer cvkVertexFlagsBuffer";       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R8_SINT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkPositionBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R8_SINT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(cvkDevice.GetDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hVertexFlagsBufferView = pBufferView.get(0);
        }       
        
        return UpdateVertexFlagsBuffer();
    }
    
    private int UpdateVertexFlagsBuffer() {
        int ret;
        
        try {
            vertexFlagsStagingBufferLock.lock();
        
            ret = cvkVertexFlagsBuffer.CopyFrom(cvkVertexFlagsStagingBuffer);             
            vertexFlagsBufferState = CVK_RESOURCE_CLEAN;
        } finally {
            vertexFlagsStagingBufferLock.unlock();
        }
        
        return ret; 
    }
    
    private void DestroyVertexFlagsBuffer() {
        if (cvkVertexFlagsBuffer != null) {
            cvkVertexFlagsBuffer.Destroy();
            cvkVertexFlagsBuffer = null;
        }   
        if (hVertexFlagsBufferView != VK_NULL_HANDLE) {
            vkDestroyBufferView(cvkDevice.GetDevice(), hVertexFlagsBufferView, null);
            hVertexFlagsBufferView = VK_NULL_HANDLE;
        }        
    }
    
    
    // ========================> Uniform buffers <======================== \\
    
    // TODO: make sure these are called when the camera changes etc
    
    private int CreateUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(vertexUniformBuffers == null);
        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(fragmentUniformBuffers == null);
        int ret;
        
        int imageCount = cvkSwapChain.GetImageCount();        

        vertexUniformBuffers = new ArrayList<>();
        geometryUniformBuffers = new ArrayList<>(); 
        fragmentUniformBuffers = new ArrayList<>();
        for (int i = 0; i < imageCount; ++i) {   
            CVKBuffer vertexUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                             VertexUniformBufferObject.SIZEOF,
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            vertexUniformBuffer.DEBUGNAME = String.format("CVKIconsRenderable vertexUniformBuffer %d", i);   
            vertexUniformBuffers.add(vertexUniformBuffer);            
            
            CVKBuffer geometryUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                               GeometryUniformBufferObject.SIZEOF,
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            geometryUniformBuffer.DEBUGNAME = String.format("CVKIconsRenderable geometryUniformBuffer %d", i);                                    
            geometryUniformBuffers.add(geometryUniformBuffer);     
            
            CVKBuffer fragmentUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                               FragmentUniformBufferObject.SIZEOF,
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            fragmentUniformBuffer.DEBUGNAME = String.format("CVKIconsRenderable fragmentUniformBuffer %d", i);                                    
            fragmentUniformBuffers.add(fragmentUniformBuffer);              
        }
        
        ret = UpdateVertexUniformBuffers(stack);
        if (VkFailed(ret)) { return ret; }
        ret = UpdateGeometryUniformBuffers(stack);
        if (VkFailed(ret)) { return ret; }
        ret = UpdateFragmentUniformBuffers(stack);
        if (VkFailed(ret)) { return ret; }
        
        return ret;
    }
        
    private int UpdateVertexUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
        
        // Populate the UBO.  This is easy to deal with, but not super efficient
        // as we are effectively staging into the staging buffer below.
        vertexUBO.mvMatrix = parent.getDisplayModelViewMatrix();
        vertexUBO.morphMix = parent.getDisplayCamera().getMix();
        
        // TODO: replace with constants.  In the JOGL version these were in a static var CAMERA that never changed
        vertexUBO.visibilityLow = parent.getDisplayCamera().getVisibilityLow();
        vertexUBO.visibilityHigh = parent.getDisplayCamera().getVisibilityHigh();            

        // Staging buffer so our VBO can be device local (most performant memory)
        final int size = VertexUniformBufferObject.SIZEOF;
        PointerBuffer pData = stack.mallocPointer(1);        
        
        // Map staging buffer into host (CPU) rw memory and copy our UBO into it
        ret = vkMapMemory(cvkDevice.GetDevice(), cvkVertexUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            vertexUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkVertexUBStagingBuffer.GetMemoryBufferHandle());   
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = vertexUniformBuffers.get(i).CopyFrom(cvkVertexUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }
        
        // We are done, reset the resource state
        vertexUBOState = CVK_RESOURCE_CLEAN;

        return ret;
    }    
    
    private int UpdateGeometryUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
        
        // Populate the UBO.  This is easy to deal with, but not super efficient
        // as we are effectively staging into the staging buffer below.
        geometryUBO.pMatrix = parent.GetProjectionMatrix();
        geometryUBO.pixelDensity = parent.GetPixelDensity();
        // geometryUBO.highlightColor is set by a task
        geometryUBO.drawHitTest = 0; // TODO: Hydra41 hit test
        
        // Staging buffer so our VBO can be device local (most performant memory)
        final int size = GeometryUniformBufferObject.SIZEOF;
        PointerBuffer pData = stack.mallocPointer(1);        
        
        // Map staging buffer into host (CPU) rw memory and copy our UBO into it
        ret = vkMapMemory(cvkDevice.GetDevice(), cvkGeometryUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            geometryUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkGeometryUBStagingBuffer.GetMemoryBufferHandle());   
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = geometryUniformBuffers.get(i).CopyFrom(cvkGeometryUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }       
                    
        // We are done, reset the resource state
        geometryUBOState = CVK_RESOURCE_CLEAN;

        return ret;
    }  
    
    private int UpdateFragmentUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
     
        fragmentUBO.drawHitTest = 0; // TODO: Hydra41 hit test
        
        // Staging buffer so our VBO can be device local (most performant memory)
        final int size = FragmentUniformBufferObject.SIZEOF;
        PointerBuffer pData = stack.mallocPointer(1);        
        
        // Map staging buffer into host (CPU) rw memory and copy our UBO into it
        ret = vkMapMemory(cvkDevice.GetDevice(), cvkFragmentUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            fragmentUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkFragmentUBStagingBuffer.GetMemoryBufferHandle());   
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = fragmentUniformBuffers.get(i).CopyFrom(cvkFragmentUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }       
                    
        // We are done, reset the resource state        
        fragmentUBOState = CVK_RESOURCE_CLEAN;

        return ret;
    }  
    
    private void DestroyUniformBuffers() {
        if (vertexUniformBuffers != null) {
            vertexUniformBuffers.forEach(el -> {el.Destroy();});
            vertexUniformBuffers = null;
        }
        
        if (geometryUniformBuffers != null) {
            geometryUniformBuffers.forEach(el -> {el.Destroy();});
            geometryUniformBuffers = null;
        }       
        
        if (fragmentUniformBuffers != null) {
            fragmentUniformBuffers.forEach(el -> {el.Destroy();});
            fragmentUniformBuffers = null;
        }           
    }      
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
//        int imageCount = cvkSwapChain.GetImageCount();
//        
//        commandBuffers = new ArrayList<>(imageCount);
//
//        for (int i = 0; i < imageCount; ++i) {
//            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_SECONDARY);
//            buffer.DEBUGNAME = String.format("CVKFPSRenderable %d", i);
//            commandBuffers.add(buffer);
//        }
//        
//        CVKLOGGER.log(Level.INFO, "Init Command Buffer - FPSRenderable");
        
        return ret;
    }   
    
    @Override
    public int RecordCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int index){
        VerifyInRenderThread();
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain != null);
                
        int ret = VK_SUCCESS;
     
//        try (MemoryStack stack = stackPush()) {
// 
//            // Fill
//            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
//            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
//            beginInfo.pNext(0);
//            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);  // hard coding this for now
//            beginInfo.pInheritanceInfo(inheritanceInfo);             
//
//            VkCommandBuffer commandBuffer = commandBuffers.get(index).GetVKCommandBuffer();
//            ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
//            checkVKret(ret);
//
//            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelines.get(index));
//
//            LongBuffer pVertexBuffers = stack.longs(vertexBuffers.get(index).GetBufferHandle());
//            LongBuffer offsets = stack.longs(0);
//
//            // Bind verts
//            vkCmdBindVertexBuffers(commandBuffer, 0, pVertexBuffers, offsets);
//
//            // Bind descriptors
//            vkCmdBindDescriptorSets(commandBuffer, 
//                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
//                                    pipelineLayouts.get(index), 
//                                    0, 
//                                    stack.longs(pDescriptorSets.get(index)), 
//                                    null);
//            vkCmdDraw(commandBuffer,
//                      GetVertexCount(),  //number of verts == number of digits
//                      1,  //no instancing, but we must draw at least 1 point
//                      0,  //first vert index
//                      0); //first instance index (N/A)                         
//            ret = vkEndCommandBuffer(commandBuffer);
//            checkVKret(ret);
//        }
        
        return ret;
    }       
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return null; }   
    
    private void DestroyCommandBuffers() {         
//        if (null != commandBuffers) {
//            commandBuffers.forEach(el -> {el.Destroy();});
//            commandBuffers.clear();
//            commandBuffers = null;
//        }      
    }        
    
        
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout() {
        int ret;
        
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(6, stack);

            // 0: Vertex uniform buffer
            VkDescriptorSetLayoutBinding vertexUBDSLB = bindings.get(0);
            vertexUBDSLB.binding(0);
            vertexUBDSLB.descriptorCount(1);
            vertexUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            vertexUBDSLB.pImmutableSamplers(null);
            vertexUBDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            
            // 1: Vertex samplerBuffer
            VkDescriptorSetLayoutBinding vertexSamplerDSLB = bindings.get(1);
            vertexSamplerDSLB.binding(1);
            vertexSamplerDSLB.descriptorCount(1);
            vertexSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER);
            vertexSamplerDSLB.pImmutableSamplers(null);
            vertexSamplerDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);            
            
            // 2: Geometry uniform buffer
            VkDescriptorSetLayoutBinding geometryUBDSLB = bindings.get(2);
            geometryUBDSLB.binding(2);
            geometryUBDSLB.descriptorCount(1);
            geometryUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geometryUBDSLB.pImmutableSamplers(null);
            geometryUBDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);      
            
            // 3: Geometry isamplerBuffer
            VkDescriptorSetLayoutBinding geometrySamplerDSLB = bindings.get(3);
            geometrySamplerDSLB.binding(3);
            geometrySamplerDSLB.descriptorCount(1);
            geometrySamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER);
            geometrySamplerDSLB.pImmutableSamplers(null);
            geometrySamplerDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);               

            // 4: Fragment sampler2Darray
            VkDescriptorSetLayoutBinding fragmentSamplerDSLB = bindings.get(4);
            fragmentSamplerDSLB.binding(4);
            fragmentSamplerDSLB.descriptorCount(1);
            fragmentSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            fragmentSamplerDSLB.pImmutableSamplers(null);
            fragmentSamplerDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            
            // 5: Fragment uniform buffer
            VkDescriptorSetLayoutBinding fragmentUBDSLB = bindings.get(5);
            fragmentUBDSLB.binding(5);
            fragmentUBDSLB.descriptorCount(1);
            fragmentUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            fragmentUBDSLB.pImmutableSamplers(null);
            fragmentUBDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);                  

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(cvkDevice.GetDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        vkDestroyDescriptorSetLayout(cvkDevice.GetDevice(), hDescriptorLayout, null);
        hDescriptorLayout = VK_NULL_HANDLE;
    }
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain != null);
//        
//        int ret;    
//        int imageCount = cvkSwapChain.GetImageCount();
//
//        // The same layout is used for each descriptor set (each descriptor set is
//        // identical but allow the GPU and CPU to desynchronise_.
//        LongBuffer layouts = stack.mallocLong(imageCount);
//        for (int i = 0; i < imageCount; ++i) {
//            layouts.put(i, hDescriptorLayout);
//        }
//
//        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
//        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
//        allocInfo.descriptorPool(cvkSwapChain.GetDescriptorPoolHandle());
//        allocInfo.pSetLayouts(layouts);            
//
//        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
//        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
//        ret = vkAllocateDescriptorSets(cvkDevice.GetDevice(), allocInfo, pDescriptorSets);
//        if (VkFailed(ret)) { return ret; }   

        descriptorPoolResourcesDirty = false;
        
        return UpdateDescriptorSets(stack);
    }
    
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
//     
//        int imageCount = cvkSwapChain.GetImageCount();
//
//        // Struct for the size of the uniform buffer used by SimpleIcon.vs (we fill the actual buffer below)
//        VkDescriptorBufferInfo.Buffer vertBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
//        vertBufferInfo.offset(0);
//        vertBufferInfo.range(CVKFPSRenderable.SIZEOF);
//
//        // Struct for the size of the uniform buffer used by SimpleIcon.gs (we fill the actual buffer below)
//        VkDescriptorBufferInfo.Buffer geomBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
//        geomBufferInfo.offset(0);
//        geomBufferInfo.range(CVKFPSRenderable.SIZEOF);      
//
//        // Struct for the size of the image sampler used by SimpleIcon.fs
//        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
//        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
//        imageInfo.imageView(parent.GetTextureAtlas().GetAtlasImageViewHandle());
//        imageInfo.sampler(parent.GetTextureAtlas().GetAtlasSamplerHandle());            
//
//        // We need 3 write descriptors, 2 for uniform buffers (vs + gs) and one for texture (fs)
//        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(3, stack);
//
//        VkWriteDescriptorSet vertUBDescriptorWrite = descriptorWrites.get(0);
//        vertUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
//        vertUBDescriptorWrite.dstBinding(0);
//        vertUBDescriptorWrite.dstArrayElement(0);
//        vertUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//        vertUBDescriptorWrite.descriptorCount(1);
//        vertUBDescriptorWrite.pBufferInfo(vertBufferInfo);
//
//        VkWriteDescriptorSet geomUBDescriptorWrite = descriptorWrites.get(1);
//        geomUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
//        geomUBDescriptorWrite.dstBinding(1);
//        geomUBDescriptorWrite.dstArrayElement(0);
//        geomUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//        geomUBDescriptorWrite.descriptorCount(1);
//        geomUBDescriptorWrite.pBufferInfo(geomBufferInfo);            
//
//        VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(2);
//        samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
//        samplerDescriptorWrite.dstBinding(2);
//        samplerDescriptorWrite.dstArrayElement(0);
//        samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
//        samplerDescriptorWrite.descriptorCount(1);
//        samplerDescriptorWrite.pImageInfo(imageInfo);                                
//
//
//        for (int i = 0; i < imageCount; ++i) {
//            long descriptorSet = pDescriptorSets.get(i);
//
//            vertBufferInfo.buffer(vertexUniformBuffers.get(i).GetBufferHandle());
//            geomBufferInfo.buffer(geometryUniformBuffers.get(i).GetBufferHandle());
// 
//            vertUBDescriptorWrite.dstSet(descriptorSet);
//            geomUBDescriptorWrite.dstSet(descriptorSet);
//            samplerDescriptorWrite.dstSet(descriptorSet);
//
//            // Update the descriptors with a write and no copy
//            vkUpdateDescriptorSets(cvkDevice.GetDevice(), descriptorWrites, null);
//        }
//        
//        // Cache atlas handles so we know when to recreate descriptors
//        hAtlasSampler = parent.GetTextureAtlas().GetAtlasSamplerHandle();
//        hAtlasImageView = parent.GetTextureAtlas().GetAtlasImageViewHandle();            
        
        return ret;
    }
        
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
//        if (pDescriptorSets != null) {
//            CVKLOGGER.info(String.format("CVKFPSRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity()));
//            
//            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
//            ret = vkFreeDescriptorSets(cvkDevice.GetDevice(), cvkSwapChain.GetDescriptorPoolHandle(), pDescriptorSets);
//            pDescriptorSets = null;
//            checkVKret(ret);
//        }
//        
        return ret;
    }    
    
    @Override
    public int DestroyDescriptorPoolResources() {
        int ret = VK_SUCCESS;
        
        if (cvkDescriptorPool != null) {
            return DestroyDescriptorSets();
        }
        
        return ret;        
    }
    
    private int CreateDescriptorPoolResources() {
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain != null);
        try (MemoryStack stack = stackPush()) {
            return CreateDescriptorSets(stack);
        }
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {}      
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(hDescriptorLayout != VK_NULL_HANDLE);
               
        int ret;       
        try (MemoryStack stack = stackPush()) {           
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssert(hPipelineLayout != VK_NULL_HANDLE);                
        }        
        return ret;        
    }    
    
    private int CreatePipelines() {
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain.GetSwapChainHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain.GetRenderPassHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkDescriptorPool.GetDescriptorPoolHandle() != VK_NULL_HANDLE);
        CVKAssert(hVertexShader != VK_NULL_HANDLE);
        CVKAssert(hGeometryShader != VK_NULL_HANDLE);
        CVKAssert(hFragmentShader != VK_NULL_HANDLE);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        final int imageCount = cvkSwapChain.GetImageCount();                
        int ret = VK_SUCCESS;
//        try (MemoryStack stack = stackPush()) {                 
//            // A complete pipeline for each swapchain image.  Wasteful?
//            pipelines = new ArrayList<>(imageCount);            
//            pipelineLayouts = new ArrayList<>(imageCount);   
//            for (int i = 0; i < imageCount; ++i) {                              
//                // prepare vertex attributes
//
//                //From the GL FPSBatcher and FPSRenderable and shaders:
//                // 1 vertex per digit.
//                // Vert inputs:
//                // int[2] data {icon indexes (encoded to int), digit index * 4)
//                // float[4] backgroundIconColor
//                // Vert outputs:
//                // flat out ivec2 gData; this is data passed through
//                // out mat4 gBackgroundIconColor; backgroundIconColor in a 4x4 matrix
//                // flat out float gRadius; 1 if visible, -1 otherwise
//                // gl_Position = mvMatrix * vec4(digitPosition, 1); where digitPosition is (digit index * 4, 0, 0)
//
//                // A bunch of uniforms:
//                // SimpleIcon.vs:
//                // uniform mat4 mvMatrix;
//                // uniform float visibilityLow;
//                // uniform float visibilityHigh;
//                // uniform float offset;
//
//                // SimpleIcon.gs:
//                // Input:
//                // uniform mat4 pMatrix;
//                // uniform float pixelDensity;
//                // uniform float pScale;     
//                // Ouput:
//                // flat out mat4 iconColor;
//                // noperspective centroid out vec3 textureCoords;
//                // layout(triangle_strip, max_vertices=28) out;     
//
//
//            
//                // TODO: Generalize map of shaders and type per renderable
//                // Then can dynamically set here? Think I saw something like
//                // this in the Oreon engine.
//                ByteBuffer entryPoint = stack.UTF8("main");
//
//                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);
//
//                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
//
//                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
//                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
//                vertShaderStageInfo.module(hVertexShader);
//                vertShaderStageInfo.pName(entryPoint);
//
//                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
//                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
//                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
//                geomShaderStageInfo.module(hGeometryShader);
//                geomShaderStageInfo.pName(entryPoint);            
//
//                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
//                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
//                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
//                fragShaderStageInfo.module(hFragmentShader);
//                fragShaderStageInfo.pName(entryPoint);
//
//                // ===> VERTEX STAGE <===
//                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
//                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
//                vertexInputInfo.pVertexBindingDescriptions(CVKFPSRenderable.Vertex.GetBindingDescription());
//                vertexInputInfo.pVertexAttributeDescriptions(CVKFPSRenderable.Vertex.GetAttributeDescriptions());
//
//                // ===> ASSEMBLY STAGE <===
//                // Triangle list is stipulated by the layout of the out attribute of
//                // SimpleIcon.gs
//                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
//                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
////                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
//                
//                // Generalize me! Parameter?
//                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_POINT_LIST);
//                inputAssembly.primitiveRestartEnable(false);
//
//                // ===> VIEWPORT & SCISSOR
//                VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
//                viewport.x(0.0f);
//                viewport.y(0.0f);
//                viewport.width(cvkSwapChain.GetWidth());
//                viewport.height(cvkSwapChain.GetHeight());
//                viewport.minDepth(0.0f);
//                viewport.maxDepth(1.0f);
//
//                VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
//                scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
//                scissor.extent(cvkSwapChain.GetExtent());
//
//                VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
//                viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
//                viewportState.pViewports(viewport);
//                viewportState.pScissors(scissor);
//
//                // ===> RASTERIZATION STAGE <===
//                VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
//                rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
//                rasterizer.depthClampEnable(false);
//                rasterizer.rasterizerDiscardEnable(false);
//                rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
//                rasterizer.lineWidth(1.0f);
//                rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
//                rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
//                rasterizer.depthBiasEnable(false);
//
//                // ===> MULTISAMPLING <===
//                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
//                multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
//                multisampling.sampleShadingEnable(false);
//                multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
//                
//                // ===> DEPTH <===
//
//                // Even though we don't test depth, the renderpass created by CVKSwapChain is used by
//                // each renderable and it was created to have a depth attachment
//                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
//                depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
//                depthStencil.depthTestEnable(false);
//                depthStencil.depthWriteEnable(false);
//                depthStencil.depthCompareOp(VK_COMPARE_OP_ALWAYS);
//                depthStencil.depthBoundsTestEnable(false);
//                depthStencil.minDepthBounds(0.0f); // Optional
//                depthStencil.maxDepthBounds(1.0f); // Optional
//                depthStencil.stencilTestEnable(false);                       
//
//                // ===> COLOR BLENDING <===
//                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
//                colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
//                colorBlendAttachment.blendEnable(false);
//
//                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
//                colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
//                colorBlending.logicOpEnable(false);
//                colorBlending.logicOp(VK_LOGIC_OP_COPY);
//                colorBlending.pAttachments(colorBlendAttachment);
//                colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
//
//                // ===> PIPELINE LAYOUT CREATION <===
//                VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
//                pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
//                pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
//
//                LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
//
//                ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
//                checkVKret(ret);
//
//                long hPipelineLayout = pPipelineLayout.get(0);
//                CVKAssert(hPipelineLayout != VK_NULL_HANDLE);
//                pipelineLayouts.add(hPipelineLayout);
//                
//                VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
//                pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
//                pipelineInfo.pStages(shaderStages);
//                pipelineInfo.pVertexInputState(vertexInputInfo);
//                pipelineInfo.pInputAssemblyState(inputAssembly);
//                pipelineInfo.pViewportState(viewportState);
//                pipelineInfo.pRasterizationState(rasterizer);
//                pipelineInfo.pMultisampleState(multisampling);
//                pipelineInfo.pDepthStencilState(depthStencil);
//                pipelineInfo.pColorBlendState(colorBlending);
//                pipelineInfo.layout(hPipelineLayout);
//                pipelineInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
//                pipelineInfo.subpass(0);
//                pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
//                pipelineInfo.basePipelineIndex(-1);
//
//                LongBuffer pGraphicsPipeline = stack.mallocLong(1);
//
//
//                ret = vkCreateGraphicsPipelines(cvkDevice.GetDevice(), 
//                                                VK_NULL_HANDLE, 
//                                                pipelineInfo, 
//                                                null, 
//                                                pGraphicsPipeline);
//                if (VkFailed(ret)) { return ret; }
//                CVKAssert(pGraphicsPipeline.get(0) != VK_NULL_HANDLE);  
//                pipelines.add(pGraphicsPipeline.get(0));                      
//            }
//        }
        
        CVKLOGGER.log(Level.INFO, "Graphics Pipeline created for FPSRenderable class.");
        return ret;
    }
    
    private void DestroyPipelines() {
//        if (pipelines != null) {
//            for (int i = 0; i < pipelines.size(); ++i) {
//                vkDestroyPipeline(cvkDevice.GetDevice(), pipelines.get(i), null);
//                pipelines.set(i, VK_NULL_HANDLE);
//            }
//            pipelines.clear();
//            pipelines = null;
//        }        
    }
    
    private void DestroyPipelineLayout() {
//        if (pipelineLayouts != null) {
//            for (int i = 0; i < pipelineLayouts.size(); ++i) {
//                vkDestroyPipelineLayout(cvkDevice.GetDevice(), pipelineLayouts.get(i), null);
//                pipelineLayouts.set(i, VK_NULL_HANDLE);
//            }
//            pipelineLayouts.clear();
//            pipelineLayouts = null;
//        }
    }      


    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() { 
        return descriptorPoolResourcesDirty || 
                swapChainResourcesDirty ||
                vertexBufferState != CVK_RESOURCE_CLEAN ||
                positionBufferState != CVK_RESOURCE_CLEAN ||
                vertexFlagsBufferState != CVK_RESOURCE_CLEAN ||
                vertexUBOState != CVK_RESOURCE_CLEAN ||
                geometryUBOState != CVK_RESOURCE_CLEAN ||
                fragmentUBOState != CVK_RESOURCE_CLEAN; 
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        VerifyInRenderThread();
                
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }
        
        if (descriptorPoolResourcesDirty) {
            ret = CreateDescriptorPoolResources();
            if (VkFailed(ret)) { return ret; }
        }
        
        // Update vertex buffers
        if (vertexBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyVertexBuffers();
            ret = CreateVertexBuffers();
            if (VkFailed(ret)) { return ret; }         
        } else if (vertexBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = UpdateVertexBuffers();
            if (VkFailed(ret)) { return ret; }           
        }
        
        // Update position buffer
        if (positionBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyPositionBuffer();
            ret = CreatePositionBuffer();
            if (VkFailed(ret)) { return ret; }            
        } else if (positionBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = UpdatePositionBuffer();
            if (VkFailed(ret)) { return ret; }            
        }
        
        // Update vertex flags buffer
        if (vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyVertexFlagsBuffer();
            ret = CreateVertexFlagsBuffer();
            if (VkFailed(ret)) { return ret; }            
        } else if (vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = UpdateVertexFlagsBuffer();
            if (VkFailed(ret)) { return ret; }            
        }        
        
        try (MemoryStack stack = stackPush()) {
            // Update vertex uniform buffer
            CVKAssert(vertexUBOState != CVK_RESOURCE_NEEDS_REBUILD);
            if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }

            // Update geometry uniform buffer
            CVKAssert(geometryUBOState != CVK_RESOURCE_NEEDS_REBUILD);
            if (geometryUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateGeometryUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }

            // Update fragment uniform buffer
            CVKAssert(fragmentUBOState != CVK_RESOURCE_NEEDS_REBUILD);
            if (fragmentUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateFragmentUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }  
        }       
        
        return ret;
    }        
    
    
    // ========================> Tasks <======================== \\    
          
    private void RebuildIconStagingBuffers(final VisualAccess access) {
        // Note this will be called from the visual processer thread, not the render thread
        vertexCount = access.getVertexCount();
        try {
            // Vertices are modified by the event thread
            vertexStagingBufferLock.lock(); 
            positionStagingBufferLock.lock();
            
            // Destroy old staging buffer if it exists
            if (cvkVertexStagingBuffer != null) {
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            }       
            if (cvkPositionBuffer != null) {
                cvkPositionBuffer.Destroy();
                cvkPositionBuffer = null;
            }            
            
            if (vertexCount > 0) {
                int vertexBufferSizeBytes = CVKIconsRenderable.Vertex.SIZEOF * vertexCount;
                cvkVertexStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                          vertexBufferSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkVertexStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkVertexStagingBuffer";
                
                int positionBufferSizeBytes = POSITION_STRIDE * vertexCount;
                cvkPositionStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                            positionBufferSizeBytes, 
                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkPositionStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkPositionBuffer";
                
                ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartWrite(0, vertexBufferSizeBytes);
                ByteBuffer pPositionMemory = cvkPositionStagingBuffer.StartWrite(0, positionBufferSizeBytes);
                CVKIconsRenderable.Vertex vertex = new CVKIconsRenderable.Vertex();
                for (int pos = 0; pos < vertexCount; pos++) {
                    SetColorInfo(pos, vertex, access);
                    SetIconIndexes(pos, vertex, access);
                    vertex.CopyTo(pVertexMemory);
                    SetVertexPosition(pos, pPositionMemory, access);
                }
                int vertMemPos = pVertexMemory.position();
                CVKAssert(vertMemPos == vertexBufferSizeBytes);
                cvkVertexStagingBuffer.EndWrite();
                pVertexMemory = null; // now unmapped, do not use
                int positionMemPos = pPositionMemory.position();
                CVKAssert(positionMemPos == positionBufferSizeBytes);
                cvkPositionStagingBuffer.EndWrite();
                pPositionMemory = null; // now unmapped, do not use                
            }
        } finally {
            vertexStagingBufferLock.unlock();
            positionStagingBufferLock.unlock();
        }        
    }
    
    public CVKRenderableUpdateTask TaskRebuildIcons(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        RebuildIconStagingBuffers(access);
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (imageIndex) -> {
            // We can't update the xyzw texture here as it is needed to render each image
            // in the swap chain.  If we recreate it for image 1 it will be likely be in
            // flight for presenting image 0.  The shared resource recreation path is
            // synchronised for all images so we need to do it there.
            vertexBufferState = CVK_RESOURCE_NEEDS_REBUILD;
            positionBufferState = CVK_RESOURCE_NEEDS_REBUILD;
        };
    }    
    
    public CVKRenderableUpdateTask TaskUpdateIcons(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        // If this fires investigate why we didn't get a rebuild task first
        CVKAssert(vertexCount != access.getVertexCount()); //REMOVE AFTER TESTING
        
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if we our vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexStagingBuffer == null || vertexCount != access.getVertexCount();
        if (rebuildRequired) {
            RebuildIconStagingBuffers(access);
        }
        
        try {
            vertexStagingBufferLock.lock();

            // We map the whole range as GraphVisualAccess applies any per vertex change to all 
            // vertices in the accessGraph so the change will contain all vertices anyway.
            ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartWrite(0, (int)cvkVertexStagingBuffer.GetBufferSize());
            final int numChanges = change.getSize();
            for (int i = 0; i < numChanges; ++i) {
                int pos = change.getElement(i);
                Vector4i iconIndexes = MakeIconIndexes(pos, access);
                final int offset = (Vertex.SIZEOF * pos) + Vertex.OFFSETOF_DATA;
                pVertexMemory.position(offset);
                pVertexMemory.putInt(iconIndexes.getX());
                pVertexMemory.putInt(iconIndexes.getY());
                pVertexMemory.putInt(iconIndexes.getZ());
                pVertexMemory.putInt(iconIndexes.getW());
            }
        } finally {
            vertexStagingBufferLock.unlock();
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (imageIndex) -> {
            if (rebuildRequired) {
                vertexBufferState = CVK_RESOURCE_NEEDS_REBUILD;
            } else if (vertexBufferState != CVK_RESOURCE_NEEDS_REBUILD) {
                vertexBufferState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        };        
    }  
    
    private void RebuildVertexFlagsStagingBuffers(final VisualAccess access) {
        int currentVertexCount = access.getVertexCount();
        try {
            // Vertices are modified by the event thread
            vertexFlagsStagingBufferLock.lock(); 
            
            // Destroy old staging buffer if it exists
            if (cvkVertexFlagsStagingBuffer != null) {
                cvkVertexFlagsStagingBuffer.Destroy();
                cvkVertexFlagsStagingBuffer = null;
            }       
            
            if (currentVertexCount > 0) {
                int vertexFlagsBufferSizeBytes = currentVertexCount * FLAGS_STRIDE;
                cvkVertexFlagsStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                               vertexFlagsBufferSizeBytes, 
                                                               VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                               VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkVertexFlagsStagingBuffer.DEBUGNAME = "CVKIconsRenderable.RebuildVertexFlagsStagingBuffers cvkVertexFlagsStagingBuffer";
                
                ByteBuffer pVertexFlagsMemory = cvkVertexFlagsStagingBuffer.StartWrite(0, vertexFlagsBufferSizeBytes);
                for (int pos = 0; pos < vertexCount; pos++) {
                    SetVertexFlags(pos, 0, pVertexFlagsMemory, access);
                }
                cvkVertexFlagsStagingBuffer.EndWrite();
                pVertexFlagsMemory = null; // now unmapped, do not use           
            }
        } finally {
            vertexFlagsStagingBufferLock.unlock();
        }         
    }
    
    public CVKRenderableUpdateTask TaskRebuildVertexFlags(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        RebuildVertexFlagsStagingBuffers(access);
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (imageIndex) -> {
            vertexFlagsBufferState = CVK_RESOURCE_NEEDS_REBUILD;
        };     
    }
    
    public CVKRenderableUpdateTask TaskUpdateVertexFlags(final VisualChange change, final VisualAccess access) {
        // If this fires investigate why we didn't get a rebuild task first
        CVKAssert(vertexCount != access.getVertexCount()); //REMOVE AFTER TESTING
        
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if we our vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexFlagsStagingBuffer == null || vertexCount != access.getVertexCount();
        if (rebuildRequired) {
            RebuildVertexFlagsStagingBuffers(access);
        } else {
            int minMax[] = change.getRange();
            CVKAssert(minMax != null);
            final int numChanges = change.getSize();
            try {
                vertexFlagsStagingBufferLock.lock();
                
                final int mappingStart = minMax[0] * FLAGS_STRIDE;
                final int mappingSize  = ((minMax[1] - minMax[0]) + 1) * FLAGS_STRIDE;
                ByteBuffer pVertexFlagsMemory = cvkVertexFlagsStagingBuffer.StartWrite(mappingStart, mappingSize);
                for (int i = 0; i < numChanges; ++i) {
                    int pos = change.getElement(i);
                    SetVertexFlags(pos, minMax[0], pVertexFlagsMemory, access);                               
                }
                cvkVertexFlagsStagingBuffer.EndWrite();
                pVertexFlagsMemory = null; // now unmapped, do not use
            } finally {
                vertexFlagsStagingBufferLock.unlock();
            }   
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (imageIndex) -> {
            if (rebuildRequired) {
                vertexFlagsBufferState = CVK_RESOURCE_NEEDS_REBUILD;
            } else if (vertexFlagsBufferState != CVK_RESOURCE_NEEDS_REBUILD) {
                vertexFlagsBufferState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        };         
    }
    
    public CVKRenderableUpdateTask TaskUpdateColours(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
       
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (imageIndex) -> {
            if (vertexBufferState != CVK_RESOURCE_NEEDS_REBUILD) {
                vertexBufferState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        };             
    }   
    
    public CVKRenderableUpdateTask TaskSetHighLightColour(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        final ConstellationColor highlightColor = access.getHighlightColor();
                                                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (imageIndex) -> {
            geometryUBO.highlightColor.set(0, 0, highlightColor.getRed());
            geometryUBO.highlightColor.set(1, 1, highlightColor.getGreen());
            geometryUBO.highlightColor.set(2, 2, highlightColor.getBlue());
          
            geometryUBOState = CVK_RESOURCE_NEEDS_UPDATE;
        };             
    }
    
    
    // ========================> Helpers <======================== \\      
    
    private Vector4i MakeIconIndexes(final int pos, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(pos < access.getVertexCount());
        
        final String foregroundIconName = access.getForegroundIcon(pos);
        final String backgroundIconName = access.getBackgroundIcon(pos);
        final int foregroundIconIndex = parent.GetTextureAtlas().AddIcon(foregroundIconName);
        final int backgroundIconIndex = parent.GetTextureAtlas().AddIcon(backgroundIconName);

        final String nWDecoratorName = access.getNWDecorator(pos);
        final String sWDecoratorName = access.getSWDecorator(pos);
        final String sEDecoratorName = access.getSEDecorator(pos);
        final String nEDecoratorName = access.getNEDecorator(pos);
        final int nWDecoratorIndex = nWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sWDecoratorIndex = sWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sEDecoratorIndex = sEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int nEDecoratorIndex = nEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);

        return new Vector4i(icons, decoratorsWest, decoratorsEast, access.getVertexId(pos));
    }      
    
    private void SetIconIndexes(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(vertex != null);        
        vertex.data.set(MakeIconIndexes(pos, access));
    }    
    
    private void SetColorInfo(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(vertex != null);
        CVKAssert(pos < access.getVertexCount());
        
        vertex.SetBackgroundIconColour(access.getVertexColor(pos));
        vertex.SetVertexVisibility(access.getVertexVisibility(pos));
    }    
    
    // TODO_TT: find out more about the second coord
    // TODO_TT: see if anything ever uses the radius   - yes the blaze batcher 
    private void SetVertexPosition(final int pos, ByteBuffer buffer, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(buffer.remaining() >= POSITION_STRIDE);    
        
        buffer.putFloat(access.getX(pos));
        buffer.putFloat(access.getY(pos));
        buffer.putFloat(access.getZ(pos));
        buffer.putFloat(access.getRadius(pos));
        buffer.putFloat(access.getX2(pos));
        buffer.putFloat(access.getY2(pos));
        buffer.putFloat(access.getZ2(pos));
        buffer.putFloat(access.getRadius(pos));  
    }  
    
    private void SetVertexFlags(final int vertexIndex, final int bufferStartIndex, ByteBuffer buffer, final VisualAccess access) {
        final boolean isSelected = access.getVertexSelected(vertexIndex);
        final boolean isDimmed = access.getVertexDimmed(vertexIndex);
        byte flags = (byte)((isDimmed ? DIMMED_BIT : 0) | (isSelected ? SELECTED_BIT : 0));
        
        // buffer may be partially only mapped so we need to offset the write location
        int writeLocation = vertexIndex - (bufferStartIndex * FLAGS_STRIDE);
        CVKAssert(writeLocation >= 0);
        CVKAssert(writeLocation < buffer.capacity());
        
        buffer.put(writeLocation, flags);
    }    
}
