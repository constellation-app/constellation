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

import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKIconTextureAtlas;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SINT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_COMPILATION;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_MODULE;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.LoadFileToDirectBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;


public class CVKFPSRenderable extends CVKRenderable {
    // Static so we recreate descriptor layouts and shaders for each graph
    private static boolean staticInitialised = false;
    
    private static final int MAX_DIGITS = 4;
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;    
    private static final int DIGIT_ICON_OFFSET = 4;
    private static final int FPS_OFFSET = 50;
    private static final Matrix44f IDENTITY_44F = Matrix44f.identity();
    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    
    private long hVertexShaderModule = VK_NULL_HANDLE;
    private long hGeometryShaderModule = VK_NULL_HANDLE;
    private long hFragmentShaderModule = VK_NULL_HANDLE;
    private static ByteBuffer vsBytes = null;
    private static ByteBuffer gsBytes = null;
    private static ByteBuffer fsBytes = null;
    
    private long hDescriptorLayout = VK_NULL_HANDLE;    
    
    private final Vector3f bottomRightCorner = new Vector3f();
    private float pyScale = 0;
    private float pxScale = 0;         
    private List<Long> pipelines = null;
    private long hPipelineLayout = VK_NULL_HANDLE;
    private final Vertex[] vertices = new Vertex[MAX_DIGITS];
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private final GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject();
    private LongBuffer pDescriptorSets = null;
    private List<Integer> currentFPS = null;  
    
    // TODO: Candidates to be moved to CVKRenderable
    private List<CVKBuffer> geometryUniformBuffers = null;
    private List<CVKBuffer> vertexBuffers = null;
    private List<CVKCommandBuffer> displayCommandBuffers = null;   
    private CVKBuffer cvkStagingBuffer = null;
   
    // Cache image view and sampler handles so we know when they've been recreated
    // so we can recreate our descriptors
    private long hAtlasSampler = VK_NULL_HANDLE;
    private long hAtlasImageView = VK_NULL_HANDLE;
    
    private ByteBuffer vertexPushConstants = null;
    
    private int counter = 0;


    // ========================> Classes <======================== \\ 
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int SIZEOF = 2 * Integer.BYTES + 4 * Float.BYTES;
        private static final int OFFSETOF_DATA = 0;
        private static final int OFFSET_BKGCLR = 2 * Integer.BYTES;
        private static final int BINDING = 0;

        private int[] data = new int[2];
        private Vector4f backgroundIconColor = new Vector4f();

        public Vertex(int[] inData, Vector4f inColour) {
            data = inData;
            backgroundIconColor = inColour;
        }
        
        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
            for(Vertex vertex : vertices) {
                buffer.putInt(vertex.data[0]);
                buffer.putInt(vertex.data[1]);
                                
                buffer.putFloat(vertex.backgroundIconColor.a[0]);
                buffer.putFloat(vertex.backgroundIconColor.a[1]);
                buffer.putFloat(vertex.backgroundIconColor.a[2]);
                buffer.putFloat(vertex.backgroundIconColor.a[3]);
            }
        }        

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

            // data
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32_SINT);
            posDescription.offset(OFFSETOF_DATA);

            // backgroundIconColor
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            colorDescription.offset(OFFSET_BKGCLR);

            return attributeDescriptions.rewind();
        }
    }
        
    private static class VertexUniformBufferObject {
        private static final int SIZEOF = (16 + 1 + 1) * Float.BYTES;

        public Matrix44f mvMatrix;
        public float visibilityLow = 0;
        public float visibilityHigh = 0;
        

        public VertexUniformBufferObject() {
            mvMatrix = new Matrix44f();
        }
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(mvMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);
        }         
    }
        
    private static class GeometryUniformBufferObject {
        private static final int SIZEOF = (16 + 1 + 1) * Float.BYTES;

        public Matrix44f pMatrix;
        public float pixelDensity = 0;
        public float pScale = 0;        

        public GeometryUniformBufferObject() {
            pMatrix = new Matrix44f();
        }
        
        private void CopyTo(ByteBuffer buffer) {
            // TODO_TT: convert to a blat
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(pixelDensity);
            buffer.putFloat(pScale);
        }             
    }    
    
    
    // ========================> Static resources <======================== \\
    
    private static int LoadShaders() {
        int ret = VK_SUCCESS;
        
        try {
            if (vsBytes == null) {
                vsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.vs.spv");
                if (vsBytes == null) {
                    CVKGraphLogger.GetStaticLogger().log(Level.SEVERE, "Failed to compile FPSRenderable shader: SimpleIcon.vs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (gsBytes == null) {
                gsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.gs.spv");
                if (gsBytes == null) {
                    CVKGraphLogger.GetStaticLogger().log(Level.SEVERE, "Failed to compile FPSRenderable shader: SimpleIcon.gs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (fsBytes == null) {
                fsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.fs.spv");
                if (fsBytes == null) {
                    CVKGraphLogger.GetStaticLogger().log(Level.SEVERE, "Failed to compile FPSRenderable shader: SimpleIcon.fs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
         
        } catch (Exception e) {
            CVKGraphLogger.GetStaticLogger().LogException(e, "Failed to compile CVKFPSRenderable shaders");
            ret = CVK_ERROR_SHADER_COMPILATION;
        }
        
        return ret;
    }
            
    public static int StaticInitialise() {
        int ret = VK_SUCCESS;
        if (!staticInitialised) {
            LoadShaders();
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
    
    public CVKFPSRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
        
        currentFPS = new ArrayList<>();
        currentFPS.add(7);
        currentFPS.add(3);
        currentFPS.add(0);  // unused
        currentFPS.add(0);  // unused
    }  
    
    private int CreateShaderModules() {
        int ret = VK_SUCCESS;
        
        try{           
            hVertexShaderModule = CVKShaderUtils.CreateShaderModule(vsBytes, CVKDevice.GetVkDevice());
            if (hVertexShaderModule == VK_NULL_HANDLE) {
                GetLogger().log(Level.SEVERE, "Failed to create shader module for: SimpleIcon.vs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hGeometryShaderModule = CVKShaderUtils.CreateShaderModule(gsBytes, CVKDevice.GetVkDevice());
            if (hGeometryShaderModule == VK_NULL_HANDLE) {
                GetLogger().log(Level.SEVERE, "Failed to create shader module for: SimpleIcon.gs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hFragmentShaderModule = CVKShaderUtils.CreateShaderModule(fsBytes, CVKDevice.GetVkDevice());
            if (hFragmentShaderModule == VK_NULL_HANDLE) {
                GetLogger().log(Level.SEVERE, "Failed to create shader module for: SimpleIcon.fs");
                return CVK_ERROR_SHADER_MODULE;
            }
        } catch(Exception ex){
            GetLogger().log(Level.SEVERE, "Failed to create shader module FPSRenderable: %s", ex.toString());
            ret = CVK_ERROR_SHADER_MODULE;
            return ret;
        }
        
        GetLogger().info("Shader modules created for CVKFPSRenderable class:\n\tVertex:   0x%016x\n\tGeometry: 0x%016x\n\tFragment: 0x%016x",
                hVertexShaderModule, hGeometryShaderModule, hFragmentShaderModule);
        return ret;
    }
       
    private void DestroyShaderModules() {
        if (hVertexShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(CVKDevice.GetVkDevice(), hVertexShaderModule, null);
            hVertexShaderModule = VK_NULL_HANDLE;
        }
        if (hGeometryShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(CVKDevice.GetVkDevice(), hGeometryShaderModule, null);
            hGeometryShaderModule = VK_NULL_HANDLE;
        }
        if (hFragmentShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(CVKDevice.GetVkDevice(), hFragmentShaderModule, null);
            hFragmentShaderModule = VK_NULL_HANDLE;
        }
    }
    
    @Override
    public int Initialise() {
        // Check for double initialisation
        CVKAssertNull(hVertexShaderModule);
        CVKAssertNull(hDescriptorLayout);
        
        int ret;        
        
        ret = CreateShaderModules();
        if (VkFailed(ret)) { return ret; }
         
        ret = CreateDescriptorLayout();
        if (VkFailed(ret)) { return ret; }
        
        ret = CreatePipelineLayout();
        if (VkFailed(ret)) { return ret; }
         
        for (int digit = 0; digit < 10; ++digit) {
            // Returns the index of the icon, not a success code
            CVKIconTextureAtlas.GetInstance().AddIcon(Integer.toString(digit));
        }
        
        ret = CreatePushConstants();
        if (VkFailed(ret)) { return ret; }
         
        return ret;
    }
    
    @Override
    public void Destroy() {                   
        DestroyVertexBuffers();
        DestroyUniformBuffers();
        DestroyDescriptorSets();
        DestroyDescriptorLayout();
        DestroyCommandBuffers();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyCommandBuffers(); 
        DestroyPushConstants(); 
        DestroyShaderModules();
        DestroyStagingBuffer();
               
        CVKAssertNull(pipelines);
        CVKAssertNull(hPipelineLayout);
        CVKAssertNull(pDescriptorSets);
        CVKAssertNull(geometryUniformBuffers);
        CVKAssertNull(vertexBuffers);
        CVKAssertNull(displayCommandBuffers);
        CVKAssertNull(hVertexShaderModule);
        CVKAssertNull(hGeometryShaderModule);
        CVKAssertNull(hFragmentShaderModule);        
        CVKAssertNull(vertexPushConstants);
        CVKAssertNull(cvkStagingBuffer);
    }     
    
    
    // ========================> Swap chain <======================== \\
    
    protected int CreateSwapChainResources() { 
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        
        int ret;
        
        try (MemoryStack stack = stackPush()) {  
            
            // We only need to recreate these resources if the number of images in 
            // the swapchain changes or if this is the first call after the initial
            // swapchain is created.
            if (swapChainImageCountChanged) {
                ret = CreateStagingBuffer();
                if (VkFailed(ret)) { return ret; }
                
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
                
            } else {
                
                // We need to update the uniform buffer as a new image size will mean a
                // different position for our FPS.  After updating the uniform buffers we
                // need to update the descriptor sets that bind the uniform buffers as well.
                ret = UpdateUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }

                ret = UpdateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; }            
            }
        }
        
        swapChainResourcesDirty = false;
        swapChainImageCountChanged = false;
        
        return ret; 
    }
   
    @Override
    protected int DestroySwapChainResources(){
        CVKAssertNotNull(cvkSwapChain);
        
        cvkVisualProcessor.VerifyInRenderThread();
        
        int ret = VK_SUCCESS;
        
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

            CVKAssertNull(pipelines);
            CVKAssertNull(pDescriptorSets);
            CVKAssertNull(geometryUniformBuffers);
            CVKAssertNull(vertexBuffers);
            CVKAssertNull(displayCommandBuffers);
            swapChainImageCountChanged = true;
         } 
        
        cvkSwapChain = null;
        return ret;
    }
    
    
    // ========================> Staging buffers <======================== \\
    
    private int CreateStagingBuffer() {
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        
        // Maximum vertex count is fixed so create the staging buffer here
        int size = vertices.length * Vertex.SIZEOF;
        cvkStagingBuffer = CVKBuffer.Create(size,
                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                            GetLogger(),
                                            "CVKFPSRenderable cvkStagingBuffer");
        
        return VK_SUCCESS;
    }
    
    private void DestroyStagingBuffer() {
        if (cvkStagingBuffer != null) {
            cvkStagingBuffer.Destroy();
            cvkStagingBuffer = null;
        }
    }
    
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
    
        int imageCount = cvkSwapChain.GetImageCount();               
        vertexBuffers = new ArrayList<>();
        
        // Size to upper limit, we don't have to draw each one.
        int size = vertices.length * Vertex.SIZEOF;
        
        //TODO_TT: most if not all of Constellation's vertex buffers won't change after creation
        // so they should probably be allocated as VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT and staged
        // to once to fill them (staging buffer this is host visible then copied to the device local)
        for (int i = 0; i < imageCount; ++i) {   
            CVKBuffer cvkVertexBuffer = CVKBuffer.Create(size,
                                                         VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                         VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                         GetLogger(),
                                                         String.format("CVKFPSRenderable cvkVertexBuffer %d", i));
            vertexBuffers.add(cvkVertexBuffer);        
        }
        
        // Populate them with some values
        UpdateVertexBuffers();
        
        return ret;  
    }
        
    private int UpdateVertexBuffers() {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {                          
            // Size to upper limit, we don't have to draw each one.
            for (int i = 0; i < vertices.length; ++i) {
                int data[] = new int[2];

                int digit = currentFPS.get(i);
                
                final int foregroundIconIndex;
                if (digit >= 0 && digit < 10) {
                    foregroundIconIndex = CVKIconTextureAtlas.GetInstance().AddIcon(Integer.toString(digit));
                } else {
                    foregroundIconIndex = digit;
                }
               
                final int backgroundIconIndex = CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;

                // packed icon indices
                data[0] = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);

                // offset which is used for this digit's position in SimpleIcon.vs
                data[1] = i * DIGIT_ICON_OFFSET;

                // colour which is inexplicably converted to a 4x4 matrix in the vert shader
                Vector4f colour = new Vector4f(1.0f,1.0f,1.0f,1.0f);

                vertices[i] = new Vertex(data, colour);
            }
            
            // Copy to our staging buffer (host read/write)
            int size = vertices.length * Vertex.SIZEOF;         
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(CVKDevice.GetVkDevice(), cvkStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
            {
                Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
            }
            vkUnmapMemory(CVKDevice.GetVkDevice(), cvkStagingBuffer.GetMemoryBufferHandle());

            // Populate
            for (int i = 0; i < vertexBuffers.size(); ++i) {   
                CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
                cvkVertexBuffer.CopyFrom(cvkStagingBuffer);
            }
        }
        
        return ret;         
    }
    
    @Override
    public int GetVertexCount(){ return 4; }      
    
    private void DestroyVertexBuffers() {
        if (null != vertexBuffers) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }
    
    
    // ========================> Uniform buffers <======================== \\
        
    private int CreateUniformBuffers(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        
        int imageCount = cvkSwapChain.GetImageCount();        

        geometryUniformBuffers = new ArrayList<>();        
        for (int i = 0; i < imageCount; ++i) {
            CVKBuffer geomUniformBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SIZEOF,
                                                           VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                           VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                           GetLogger(),
                                                           String.format("CVKFPSRenderable geomUniformBuffer %d", i));
            geometryUniformBuffers.add(geomUniformBuffer);            
        }
        return UpdateUniformBuffers(stack);                
    }
        
    private int UpdateUniformBuffers(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
     
        // LIFTED FROM FPSRenderable.reshape(...)
        //TT: the logic here seems to be the FPS text needs to be 50 pixels from the 
        // edges, the calculation of dx and dy implies that the viewport is 
        //-width/2, -height/2, width/2, height/2
        
        // whenever the drawable shape changes, recalculate the place where the fps is drawn
        
        // This is a GL viewport where the screen space origin is in the bottom left corner
        //final int[] viewport = new int[]{0, 0, cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight()};
        
        // In Vulkan the screen space origin is in the top left hand corner.  Note we put the origin at 0, H and 
        // the viewport dimensions are W and -H.  The -H means we we still have a 0->H range, just running in the
        // opposite direction to GL.
        final int[] viewport = cvkSwapChain.GetViewport();
        
        final int dx = cvkSwapChain.GetWidth() / 2 - FPS_OFFSET;
        final int dy = cvkSwapChain.GetHeight() / 2 - FPS_OFFSET;
        pxScale = calculateXProjectionScale(viewport);
        pyScale = calculateYProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, bottomRightCorner);

        
        // set the number of pixels per world unit at distance 1
        geometryUBO.pixelDensity = cvkVisualProcessor.GetPixelDensity();
        geometryUBO.pScale = pyScale;
             
        
        // LIFTED FROM FPSRenerable.display(...)
        // Initialise source data to sensible values   
        final Matrix44f scalingMatrix = new Matrix44f();
        scalingMatrix.makeScalingMatrix(pxScale, pyScale, 0);
        final Matrix44f srMatrix = new Matrix44f();
        srMatrix.multiply(scalingMatrix, IDENTITY_44F);

        // build the fps matrix by translating the sr matrix
        final Matrix44f translationMatrix = new Matrix44f();
        translationMatrix.makeTranslationMatrix(bottomRightCorner.getX(),
                                                bottomRightCorner.getY(), 
                                                bottomRightCorner.getZ());
        vertexUBO.mvMatrix.multiply(translationMatrix, srMatrix);        
                      
                
        // In the JOGL version these were in a static var CAMERA that never changed
        vertexUBO.visibilityLow = cvkVisualProcessor.getDisplayCamera().getVisibilityLow();
        vertexUBO.visibilityHigh = cvkVisualProcessor.getDisplayCamera().getVisibilityHigh();
        
        // Update the push constants data
        vertexUBO.CopyTo(vertexPushConstants);
        vertexPushConstants.flip();

        // Get the projection matrix from our cvkVisualProcessor
        geometryUBO.pMatrix.set(cvkVisualProcessor.GetProjectionMatrix());
        

        // Fill of the geometry uniform buffer
        PointerBuffer pData = stack.mallocPointer(1);
        int size = GeometryUniformBufferObject.SIZEOF;
        CVKBuffer cvkGeomUBStagingBuffer = CVKBuffer.Create(size,
                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                            GetLogger(),
                                                            "CVKFPSRenderable.UpdateUniformBuffers cvkGeomUBStagingBuffer");  
        ret = vkMapMemory(CVKDevice.GetVkDevice(), cvkGeomUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            geometryUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(CVKDevice.GetVkDevice(), cvkGeomUBStagingBuffer.GetMemoryBufferHandle());          
                
        // Copy the UBOs in VK buffers we can bind to a descriptor set  
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {                          
            geometryUniformBuffers.get(i).CopyFrom(cvkGeomUBStagingBuffer);                                           
        }
        cvkGeomUBStagingBuffer.Destroy();

        return ret;
    }
    
    private void DestroyUniformBuffers() {
        
        if (geometryUniformBuffers != null) {
            geometryUniformBuffers.forEach(el -> {el.Destroy();});
            geometryUniformBuffers = null;
        }        
    }
    
    
    // ========================> Push constants <======================== \\
 
    private int CreatePushConstants() {
        // Initialise push constants to identity mtx
        vertexPushConstants = memAlloc(VertexUniformBufferObject.SIZEOF);
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                vertexPushConstants.putFloat(IDENTITY_44F.get(iRow, iCol));
            }
        }
        vertexPushConstants.putFloat(0.0f);
        vertexPushConstants.putFloat(1.0f);
        vertexPushConstants.flip();
        
        return VK_SUCCESS;
    }

        
    private void DestroyPushConstants() {
        if (vertexPushConstants != null) {
            memFree(vertexPushConstants);
            vertexPushConstants = null;
        }
    }
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        displayCommandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, 
                                                              GetLogger(),
                                                              String.format("CVKFPSRenderable %d", i));
            displayCommandBuffers.add(buffer);
        }
        
        return ret;
    }
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {
        return displayCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }    
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(CVKDevice.GetCommandPoolHandle());
        CVKAssertNotNull(cvkSwapChain);
                
        int ret;

        CVKCommandBuffer commandBuffer = displayCommandBuffers.get(imageIndex);

        ret = commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
                                                       inheritanceInfo);
        if (VkFailed(ret)) { return ret; }

        commandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        commandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());

        commandBuffer.BindGraphicsPipeline(pipelines.get(imageIndex));
        commandBuffer.BindVertexInput(vertexBuffers.get(imageIndex).GetBufferHandle());

        commandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));
        commandBuffer.PushConstants(hPipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, vertexPushConstants);

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }
        
        return ret;
    }   
    
    private void DestroyCommandBuffers() {         
        if (null != displayCommandBuffers) {
            displayCommandBuffers.forEach(el -> {el.Destroy();});
            displayCommandBuffers.clear();
            displayCommandBuffers = null;
        }      
    }
    
    
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout() {
        int ret;
        
        try(MemoryStack stack = stackPush()) {
            /*
            Vertex shader is updated via push constants
            Geometry shader needs a different uniform buffer.
            Fragment shader needs a sampler2Darray
            */

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(2, stack);
       
            VkDescriptorSetLayoutBinding geomUBOLayout = bindings.get(1);
            geomUBOLayout.binding(0);
            geomUBOLayout.descriptorCount(1);
            geomUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geomUBOLayout.pImmutableSamplers(null);
            geomUBOLayout.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);            

            VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(0);
            samplerLayoutBinding.binding(1);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(CVKDevice.GetVkDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
                GetLogger().info("CVKFPSRenderable created hDescriptorLayout: 0x%016X", hDescriptorLayout);
            }
        }        
        return ret;
    }
    
    private void DestroyDescriptorLayout() {
        GetLogger().info("CVKFPSRenderable destroying hDescriptorLayout: 0x%016X", hDescriptorLayout);
        vkDestroyDescriptorSetLayout(CVKDevice.GetVkDevice(), hDescriptorLayout, null);
        hDescriptorLayout = VK_NULL_HANDLE;
    }
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        
        int ret;    

        // The same layout is used for each descriptor set (each descriptor set is
        // identical but allow the GPU and CPU to desynchronise.
        final int imageCount = cvkSwapChain.GetImageCount();
        LongBuffer layouts = stack.mallocLong(imageCount);
        for (int i = 0; i < imageCount; ++i) {
            layouts.put(i, hDescriptorLayout);
        }

        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool(cvkDescriptorPool.GetDescriptorPoolHandle());
        allocInfo.pSetLayouts(layouts);

        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
        ret = vkAllocateDescriptorSets(CVKDevice.GetVkDevice(), allocInfo, pDescriptorSets);
        if (VkFailed(ret)) { return ret; }
        
        for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
            GetLogger().info("CVKFPSRenderable allocated hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
        }        
        
        descriptorPoolResourcesDirty = false;
        
        return UpdateDescriptorSets(stack);
    }
    
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle());
        CVKAssertNotNull(CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle());
        
        int ret = VK_SUCCESS;
     
        int imageCount = cvkSwapChain.GetImageCount();

        // Struct for the size of the uniform buffer used by SimpleIcon.gs (we fill the actual buffer below)
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(GeometryUniformBufferObject.SIZEOF);      

        // Struct for the size of the image sampler used by SimpleIcon.fs
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle());
        imageInfo.sampler(CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle());            

        // We need 2 write descriptors, 1 for the geometry stage uniform buffer and one for fragment stage texture
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(2, stack);

        VkWriteDescriptorSet geomUBDescriptorWrite = descriptorWrites.get(0);
        geomUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        geomUBDescriptorWrite.dstBinding(0);
        geomUBDescriptorWrite.dstArrayElement(0);
        geomUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        geomUBDescriptorWrite.descriptorCount(1);
        geomUBDescriptorWrite.pBufferInfo(geometryUniformBufferInfo);            

        VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1);
        samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        samplerDescriptorWrite.dstBinding(1);
        samplerDescriptorWrite.dstArrayElement(0);
        samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        samplerDescriptorWrite.descriptorCount(1);
        samplerDescriptorWrite.pImageInfo(imageInfo);                                


        for (int i = 0; i < imageCount; ++i) {
            long descriptorSet = pDescriptorSets.get(i);

            geometryUniformBufferInfo.buffer(geometryUniformBuffers.get(i).GetBufferHandle());
            geomUBDescriptorWrite.dstSet(descriptorSet);
            samplerDescriptorWrite.dstSet(descriptorSet);

            // Update the descriptors with a write and no copy
            vkUpdateDescriptorSets(CVKDevice.GetVkDevice(), descriptorWrites, null);
        }
        
        // Cache atlas handles so we know when to recreate descriptors
        hAtlasSampler = CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle();
        hAtlasImageView = CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle();            
        
        return ret;
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {
        // SimpleIcon.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        // SimpleIcon.fs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
        
        // One set per image
        ++perImageReqs.poolDesciptorSetCount;
    } 
        
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
        if (pDescriptorSets != null) {
            CVKAssertNotNull(cvkDescriptorPool);
            CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());             
            GetLogger().fine("CVKFPSRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity());
            
            for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
                GetLogger().info("CVKFPSRenderable freeing hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
            }            
            
            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
            ret = vkFreeDescriptorSets(CVKDevice.GetVkDevice(), cvkDescriptorPool.GetDescriptorPoolHandle(), pDescriptorSets);
            pDescriptorSets = null;
            checkVKret(ret);
        }
        
        return ret;
    }
    
    @Override
    protected int DestroyDescriptorPoolResources() { 
        int ret = VK_SUCCESS;
        
        if (cvkDescriptorPool != null) {
            return DestroyDescriptorSets();
        }
        
        return ret; 
    }     

    private int CreateDescriptorPoolResources() {
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);

        try (MemoryStack stack = stackPush()) {
            return CreateDescriptorSets(stack);
        }
    }         
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssertNotNull(CVKDevice.GetVkDevice());   
        CVKAssertNotNull(hDescriptorLayout);
        
        int ret;
        try (MemoryStack stack = stackPush()) {
            VkPushConstantRange.Buffer pushConstantRange;
            pushConstantRange = VkPushConstantRange.callocStack(1, stack);
            pushConstantRange.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            pushConstantRange.size(VertexUniformBufferObject.SIZEOF);
            pushConstantRange.offset(0);

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
            pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(CVKDevice.GetVkDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssertNotNull(hPipelineLayout);
        }
        return ret;
    }
    
    private void DestroyPipelineLayout() {
        if (hPipelineLayout != VK_NULL_HANDLE) {
            vkDestroyPipelineLayout(CVKDevice.GetVkDevice(), hPipelineLayout, null);
            hPipelineLayout = VK_NULL_HANDLE;
        }
    }    
    
    private int CreatePipelines() {
        CVKAssertNotNull(hPipelineLayout);
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain.GetSwapChainHandle());
        CVKAssertNotNull(cvkSwapChain.GetRenderPassHandle());
        CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());
        CVKAssertNotNull(hVertexShaderModule);
        CVKAssertNotNull(hGeometryShaderModule);
        CVKAssertNotNull(hFragmentShaderModule);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        final int imageCount = cvkSwapChain.GetImageCount();                
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {                 
            // A complete pipeline for each swapchain image.  Wasteful?
            pipelines = new ArrayList<>(imageCount);            
            for (int i = 0; i < imageCount; ++i) {                              
                // prepare vertex attributes

                //From the GL FPSBatcher and FPSRenderable and shaders:
                // 1 vertex per digit.
                // Vert inputs:
                // int[2] data {icon indexes (encoded to int), digit index * 4)
                // float[4] backgroundIconColor
                // Vert outputs:
                // flat out ivec2 gData; this is data passed through
                // out mat4 gBackgroundIconColor; backgroundIconColor in a 4x4 matrix
                // flat out float gRadius; 1 if visible, -1 otherwise
                // gl_Position = mvMatrix * vec4(digitPosition, 1); where digitPosition is (digit index * 4, 0, 0)

                // A bunch of uniforms:
                // SimpleIcon.vs:
                // uniform mat4 mvMatrix;
                // uniform float visibilityLow;
                // uniform float visibilityHigh;
                // uniform float offset;

                // SimpleIcon.gs:
                // Input:
                // uniform mat4 pMatrix;
                // uniform float pixelDensity;
                // uniform float pScale;     
                // Ouput:
                // flat out mat4 iconColor;
                // noperspective centroid out vec3 textureCoords;
                // layout(triangle_strip, max_vertices=28) out;     


            
                // TODO: Generalize map of shaders and type per renderable
                // Then can dynamically set here? Think I saw something like
                // this in the Oreon engine.
                ByteBuffer entryPoint = stack.UTF8("main");

                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);

                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(hVertexShaderModule);
                vertShaderStageInfo.pName(entryPoint);

                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
                geomShaderStageInfo.module(hGeometryShaderModule);
                geomShaderStageInfo.pName(entryPoint);            

                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(hFragmentShaderModule);
                fragShaderStageInfo.pName(entryPoint);

                // ===> VERTEX STAGE <===
                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(Vertex.GetBindingDescription());
                vertexInputInfo.pVertexAttributeDescriptions(Vertex.GetAttributeDescriptions());

                // ===> ASSEMBLY STAGE <===
                // Each point becomes two triangles in the geometry shader, but our input is a point list
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_POINT_LIST);
                inputAssembly.primitiveRestartEnable(false);

                // ===> VIEWPORT & SCISSOR
                VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
                viewport.x(0.0f);
                viewport.y(0.0f);
                viewport.width(cvkSwapChain.GetWidth());
                viewport.height(cvkSwapChain.GetHeight());
                viewport.minDepth(0.0f);
                viewport.maxDepth(1.0f);

                VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
                scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
                scissor.extent(cvkSwapChain.GetExtent());

                VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
                viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
                viewportState.pViewports(viewport);
                viewportState.pScissors(scissor);

                // ===> RASTERIZATION STAGE <===
                VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
                rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
                rasterizer.depthClampEnable(false);
                rasterizer.rasterizerDiscardEnable(false);
                rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
                rasterizer.lineWidth(1.0f);
                rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
                rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
                rasterizer.depthBiasEnable(false);

                // ===> MULTISAMPLING <===
                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
                multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
                multisampling.sampleShadingEnable(false);
                multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
                
                // ===> DEPTH <===
                // Even though we don't test depth, the renderpass created by CVKSwapChain is used by
                // each renderable and it was created to have a depth attachment
                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
                depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
                depthStencil.depthTestEnable(false);
                depthStencil.depthWriteEnable(false);
                depthStencil.depthCompareOp(VK_COMPARE_OP_ALWAYS);
                depthStencil.depthBoundsTestEnable(false);
                depthStencil.minDepthBounds(0.0f); // Optional
                depthStencil.maxDepthBounds(1.0f); // Optional
                depthStencil.stencilTestEnable(false);                       

                // ===> COLOR BLENDING <===
                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
                colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
                colorBlendAttachment.blendEnable(false);

                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
                colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
                colorBlending.logicOpEnable(false);
                colorBlending.logicOp(VK_LOGIC_OP_COPY);
                colorBlending.pAttachments(colorBlendAttachment);
                colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

                // ===> DYNAMIC PROPERTIES CREATION <===
                IntBuffer pDynamicStates = memAllocInt(2);
                pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT);
                pDynamicStates.put(VK_DYNAMIC_STATE_SCISSOR);
                pDynamicStates.flip();
                VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.callocStack(stack);
                dynamicState.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
                dynamicState.pDynamicStates(pDynamicStates);
                                
                // ===> PIPELINE CREATION <===
                VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
                pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
                pipelineInfo.pStages(shaderStages);
                pipelineInfo.pVertexInputState(vertexInputInfo);
                pipelineInfo.pInputAssemblyState(inputAssembly);
                pipelineInfo.pViewportState(viewportState);
                pipelineInfo.pRasterizationState(rasterizer);
                pipelineInfo.pMultisampleState(multisampling);
                pipelineInfo.pDepthStencilState(depthStencil);
                pipelineInfo.pColorBlendState(colorBlending);
                pipelineInfo.layout(hPipelineLayout);
                pipelineInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
                pipelineInfo.subpass(0);
                pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
                pipelineInfo.basePipelineIndex(-1);
                pipelineInfo.pDynamicState(dynamicState);

                LongBuffer pGraphicsPipeline = stack.mallocLong(1);
                ret = vkCreateGraphicsPipelines(CVKDevice.GetVkDevice(), 
                                                VK_NULL_HANDLE, 
                                                pipelineInfo, 
                                                null, 
                                                pGraphicsPipeline);
                if (VkFailed(ret)) { return ret; }
                CVKAssertNotNull(pGraphicsPipeline.get(0));  
                pipelines.add(pGraphicsPipeline.get(0));                      
            }
        }
        
        GetLogger().info("Graphics Pipeline created for CVKFPSRenderable class.");
        return ret;
    }        
   
    private void DestroyPipelines() {
        if (pipelines != null) {
            for (int i = 0; i < pipelines.size(); ++i) {
                vkDestroyPipeline(CVKDevice.GetVkDevice(), pipelines.get(i), null);
                pipelines.set(i, VK_NULL_HANDLE);
            }
            pipelines.clear();
            pipelines = null;
        }        
    }   
    
    
    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() { 
        cvkVisualProcessor.VerifyInRenderThread();               
        
        return true;
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        cvkVisualProcessor.VerifyInRenderThread();
        
        DebugUpdateFPS();
        
        boolean atlasChanged =  hAtlasSampler != CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle() ||
                                hAtlasImageView != CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle();   
        
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }
        
        if (descriptorPoolResourcesDirty) {
            ret = CreateDescriptorPoolResources();
            if (VkFailed(ret)) { return ret; }
        }        
                        
        // We only need to update descriptors if the atlas has generated a new texture
        if (atlasChanged) {
            try (MemoryStack stack = stackPush()) {
                ret = UpdateDescriptorSets(stack);
                if (VkFailed(ret)) {
                    return ret;
                }                               
            }           
        }
        
        // We update this constantly as the FPS changes constantly
        ret = UpdateVertexBuffers();
        if (VkFailed(ret)) {
            return ret;
        }        
        
        return ret;
    }    
    
    
    // ========================> Helpers <======================== \\  
    
    // LIFTED FROM FPSRenderable.java
    private float calculateXProjectionScale(final int[] viewport) {
        // calculate the number of pixels a cvkScene object of y-length 1 projects to.
        final Vector4f proj1 = new Vector4f();
        //TT: Projects 0,0,0 into an identity matrix scaled to the width and 
        // height of the viewport (in pixels) and a z of 0->1.  This will lead to
        // proj1 being width/2, height/2, 0.5
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        final Vector3f unitPosition = new Vector3f(1, 0, 0);
        //TT: Projecting 1,0,0 into the same space yields width, height/2, 0.5
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        //TT: the above seems like a lot of messing around to arrive at 
        // xScale = width/2
        final float xScale = proj2.getX() - proj1.getX();
        //TT: 4/(width/2), what are the 256 and 64?  Magic numbers rock.  Maybe
        // dimensions of the generated icon texture?  8/width.
        // 256 is the icon width and height in the atlas texture, 64 is the number
        // of icons you can fit in a 2048x2048 texture.
        return (256.0f / 64) / xScale;
    }
    
    private float calculateYProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.getY() - proj1.getY();
        return (256.0f / 64) / yScale;
    }    
    
    private void DebugUpdateFPS() {

        // Debug code to update every 100 frames
        if (++counter % 20 != 0) { return; }

        currentFPS.set(0, GetRandom(0,9));
        currentFPS.set(1, GetRandom(0,9));
        currentFPS.set(2, GetRandom(10, CVKIconTextureAtlas.GetInstance().GetAtlasIconCount()));
        currentFPS.set(3, GetRandom(10, CVKIconTextureAtlas.GetInstance().GetAtlasIconCount()));       

    }
    
    private int GetRandom(int min, int max){
        return Math.min(max, (int)(Math.random() * ((max - min) + 1)) + min);
    }
}
