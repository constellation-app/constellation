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

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger.CVKLOGGER;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.ShaderKind.FRAGMENT_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.ShaderKind.VERTEX_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.compileShaderFile;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_COMPILATION;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_MODULE;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_VKALLOCATIONS;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.GetParentMethodName;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_NEVER;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
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
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;



public class CVKPointsRenderable extends CVKRenderable {
    // Static so we recreate descriptor layouts and shaders for each graph
    private static boolean staticInitialised = false;
    
    // Compiled Shader modules
    private long hVertexShaderModule = VK_NULL_HANDLE;
    private long hFragmentShaderModule = VK_NULL_HANDLE;
    
    private static CVKShaderUtils.SPIRV vertexShaderSPIRV = null;
    private static CVKShaderUtils.SPIRV fragmentShaderSPIRV = null;
    
    // The UBO staging buffers are a known size so created outside user events
    private VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private CVKBuffer cvkVertexUBStagingBuffer = null;    
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;    
    private CVKBuffer cvkVertexBuffer = null;
    
    // Swapchain dependent resources
    private List<CVKCommandBuffer> commandBuffers = null;   
    private List<Long> pipelines = null;
    
    // Templates
    private long hPipelineLayout = VK_NULL_HANDLE;    
    
    // The vertex staging buffer are used by both the event
    // thread and rendering thread so must be synchronised.
    private ReentrantLock vertexStagingBufferLock = new ReentrantLock();    
    
    // Resource states.
    private CVKRenderableResourceState vertexUBOState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexBuffersState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState commandBuffersState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState descriptorSetsState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState pipelinesState = CVK_RESOURCE_CLEAN;        
    
    private ByteBuffer pushConstants = null;
    
    
    // ========================> Debuggering <======================== \\
       
    private static boolean LOGSTATECHANGE = false;
    private void SetVertexUBOState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info(String.format("%d\t vertexUBOState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), vertexUBOState.name(), state.name(), GetParentMethodName()));  
        }
        vertexUBOState = state;
    }    
    private void SetVertexBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info(String.format("%d\t vertexBuffersState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), vertexBuffersState.name(), state.name(), GetParentMethodName()));        
        }
        vertexBuffersState = state;
    }
    private void SetCommandBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info(String.format("%d\t commandBuffersState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), commandBuffersState.name(), state.name(), GetParentMethodName())); 
        }
        commandBuffersState = state;
    }
    private void SetDescriptorSetsState(final CVKRenderableResourceState state) {
        CVKAssert(!(descriptorSetsState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info(String.format("%d\t descriptorSetsState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), descriptorSetsState.name(), state.name(), GetParentMethodName()));   
        }
        descriptorSetsState = state;
    }
    private void SetPipelinesState(final CVKRenderableResourceState state) {
        CVKAssert(!(pipelinesState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info(String.format("%d\t pipelinesState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), pipelinesState.name(), state.name(), GetParentMethodName()));   
        }
        pipelinesState = state;
    }      
    
    
    // ========================> Classes <======================== \\         
        
    private static class Vertex {
        private static final int SIZEOF = 3 * Float.BYTES;
        private final Vector3f vertex;

        public Vertex(final Vector3f vertex) {
            this.vertex = vertex;
        }
        
        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
            for(Vertex vertex : vertices) {
                buffer.putFloat(vertex.vertex.getX());
                buffer.putFloat(vertex.vertex.getY());
                buffer.putFloat(vertex.vertex.getZ());
            }
        }

        private static VkVertexInputBindingDescription.Buffer getBindingDescription() {
            VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.callocStack(1);
            bindingDescription.binding(0);
            bindingDescription.stride(Vertex.SIZEOF);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
            return bindingDescription;
        }

        private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {
            VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(1);
            VkVertexInputAttributeDescription vertexDescription = attributeDescriptions.get(0);
            vertexDescription.binding(0);
            vertexDescription.location(0);
            vertexDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            vertexDescription.offset(0);
            return attributeDescriptions.rewind();
        }
    }
    
    private static class VertexUniformBufferObject {
        private static final int SIZEOF = 16 * Float.BYTES;
        public Matrix44f mvpMatrix;
      
        public VertexUniformBufferObject() {
            mvpMatrix = new Matrix44f();
        }
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(mvpMatrix.get(iRow, iCol));
                }
            }
        }         
    }
    
    
    // ========================> Static init <======================== \\
    
    private static int LoadShaders() {       
        int ret = VK_SUCCESS;

        try{
            if (vertexShaderSPIRV == null) {
                vertexShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThruPoint.vs", VERTEX_SHADER);
                if (vertexShaderSPIRV == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to compile CVKPointRenderable shaders: PassThruPoint.vs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (fragmentShaderSPIRV == null) {
                fragmentShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThruPoint.fs", FRAGMENT_SHADER);
                if (fragmentShaderSPIRV == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to compile CVKPointRenderable shaders: PassThru.fs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
        } catch(Exception ex){
            CVKLOGGER.log(Level.SEVERE, "Failed to compile CVKPointRenderable shaders: {0}", ex.toString());
            ret = CVK_ERROR_SHADER_COMPILATION;
            return ret;
        }
        
        CVKLOGGER.log(Level.INFO, "Static shaders loaded for CVKPointRenderable class");
        return ret;
    }             
    
    public static int StaticInitialise() {
        int ret = VK_SUCCESS;
        if (!staticInitialised) {
            ret = LoadShaders();
            if (VkFailed(ret)) { return ret; }
            staticInitialised = true;
        }
        return ret;
    }
    
    public static void DestroyStaticResources() {
        if (vertexShaderSPIRV != null) {
            vertexShaderSPIRV.free();
            vertexShaderSPIRV = null;
        }
        
        if (fragmentShaderSPIRV != null) {
            fragmentShaderSPIRV.free();
            fragmentShaderSPIRV = null;
        }
        
        staticInitialised = false;
    }   
    
    
    // ========================> Lifetime <======================== \\
    
    public CVKPointsRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
    }  
    
    private int CreateShaderModules() {
        int ret = VK_SUCCESS;
        
        try{           
            hVertexShaderModule = CVKShaderUtils.CreateShaderModule(vertexShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            if (hVertexShaderModule == VK_NULL_HANDLE) {
                GetLogger().log(Level.SEVERE, "Failed to create shader module for: VertexIcon.vs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hFragmentShaderModule = CVKShaderUtils.CreateShaderModule(fragmentShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            if (hFragmentShaderModule == VK_NULL_HANDLE) {
                GetLogger().log(Level.SEVERE, "Failed to create shader module for: VertexIcon.fs");
                return CVK_ERROR_SHADER_MODULE;
            }
        } catch(Exception ex){
            GetLogger().log(Level.SEVERE, "Failed to create shader module CVKPointRenderable: {0}", ex.toString());
            ret = CVK_ERROR_SHADER_MODULE;
            return ret;
        }
        
        GetLogger().info("Shader modules created for CVKPointsRenderable class:\n\tVertex: 0x%016x\n\tFragment: 0x%016x",
                hVertexShaderModule, hFragmentShaderModule);
        
        return ret;
    }   
    
    private void DestroyShaderModules() {
        if (hVertexShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(cvkDevice.GetDevice(), hVertexShaderModule, null);
            hVertexShaderModule = VK_NULL_HANDLE;
        }
        if (hFragmentShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(cvkDevice.GetDevice(), hFragmentShaderModule, null);
            hFragmentShaderModule = VK_NULL_HANDLE;
        }
    }       
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                    VertexUniformBufferObject.SIZEOF,
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                    "CVKPointRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer");     
    }
    
    @Override
    public int Initialise(CVKDevice cvkDevice) {
        CVKAssert(cvkDevice != null);
        // Check for double initialisation
        CVKAssert(hVertexShaderModule == VK_NULL_HANDLE);
        
        int ret;        
        this.cvkDevice = cvkDevice;
        
        // Initialise push constants to identity mtx
        CreatePushConstants();        
        
        ret = CreateShaderModules();
        if (VkFailed(ret)) { return ret; }                     
        
        ret = CreatePipelineLayout();
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
        if (cvkVertexUBStagingBuffer != null) {
            cvkVertexUBStagingBuffer.Destroy();
            cvkVertexUBStagingBuffer = null;
        }       
    }
    
    @Override
    public void Destroy() {
        DestroyCommandBuffers();
        DestroyVertexBuffer();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyPushConstants();
        DestroyStagingBuffers();
        DestroyShaderModules();
              
        CVKAssert(pipelines == null);
        CVKAssert(hPipelineLayout == VK_NULL_HANDLE);
        CVKAssert(cvkVertexBuffer == null);
        CVKAssert(commandBuffers == null);     
        CVKAssert(pushConstants == null);     
        CVKAssert(hVertexShaderModule == VK_NULL_HANDLE);
        CVKAssert(hFragmentShaderModule == VK_NULL_HANDLE);        
    }
    
       
    // ========================> Swap chain <======================== \\
       
    @Override
    protected int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines != null && swapChainImageCountChanged) {  
            DestroyVertexBuffer();
            DestroyCommandBuffers();     
            DestroyPipelines();                            
        }
       
        return VK_SUCCESS; 
    }      
    
    @Override
    public int SetNewSwapChain(CVKSwapChain newSwapChain) {
        int ret = super.SetNewSwapChain(newSwapChain);
        if (VkFailed(ret)) { return ret; }
        
        if (swapChainImageCountChanged) {
            // The number of images has changed, we need to rebuild all image
            // buffered resources
            SetVertexUBOState(CVK_RESOURCE_NEEDS_REBUILD);       
            SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetCommandBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetDescriptorSetsState(CVK_RESOURCE_NEEDS_REBUILD);
            SetPipelinesState(CVK_RESOURCE_NEEDS_REBUILD);
        } else {
            // View frustum and projection matrix likely have changed.  We don't
            // need to rebuild our pipelines as the frustum is set by dynamic
            // state in RecordDisplayCommandBuffer
            if (vertexUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexUBOState(CVK_RESOURCE_NEEDS_UPDATE); 
            }
        }
        
        return ret;
    } 
    
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
    
        // We can only create vertex buffers if we have something to put in them
        if (vertexCount > 0) {
            int vertexBufferSizeBytes = Vertex.SIZEOF * vertexCount;
            cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                vertexBufferSizeBytes,
                                                VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                "CVKPointRenderable cvkVertexBuffer");

            // Populate them with some values
            return UpdateVertexBuffer();
        }
        
        return ret;  
    }    
    
    private int UpdateVertexBuffer() {
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssert(cvkVertexStagingBuffer != null);
        CVKAssert(cvkVertexBuffer != null);
        CVKAssert(cvkVertexStagingBuffer.GetBufferSize() == cvkVertexBuffer.GetBufferSize());
        int ret = VK_SUCCESS;
        
        try {
            vertexStagingBufferLock.lock();
            
            List<CVKBuffer.DEBUG_CVKBufferElementDescriptor> DEBUG_vertexDescriptors = new ArrayList<>();
            DEBUG_vertexDescriptors.add(new CVKBuffer.DEBUG_CVKBufferElementDescriptor("x", Float.TYPE));
            DEBUG_vertexDescriptors.add(new CVKBuffer.DEBUG_CVKBufferElementDescriptor("y", Float.TYPE));
            DEBUG_vertexDescriptors.add(new CVKBuffer.DEBUG_CVKBufferElementDescriptor("z", Float.TYPE));
            cvkVertexStagingBuffer.DEBUGPRINT(DEBUG_vertexDescriptors);            
            
            cvkVertexBuffer.CopyFrom(cvkVertexStagingBuffer);
        } finally {
            vertexStagingBufferLock.unlock();
        }     
        
        // Note the staging buffer is not freed as we can simplify the update tasks
        // by just updating it and then copying it over again during ProcessRenderTasks().
        SetVertexBuffersState(CVK_RESOURCE_CLEAN);

        return ret;         
    }  
    
    @Override
    public int GetVertexCount() { return vertexCount; }      
    
    private void DestroyVertexBuffer() {
        if (cvkVertexBuffer != null) {
            cvkVertexBuffer.Destroy();
            cvkVertexBuffer = null;
        }
    }         

    // ========================> Push constants <======================== \\
    
    private void CreatePushConstants() {
        // Initialise push constants to identity mtx
        pushConstants = memAlloc(VertexUniformBufferObject.SIZEOF);
        UpdatePushConstants();
    }
    
    private void UpdatePushConstants(){
                   
        vertexUBO.mvpMatrix = cvkVisualProcessor.getDisplayModelViewProjectionMatrix();
        
        // Update the push constants data
        vertexUBO.CopyTo(pushConstants);
        pushConstants.flip();  
        
        vertexUBOState = CVK_RESOURCE_CLEAN;
    }
    
    private void DestroyPushConstants() {
        if (pushConstants != null) {
            memFree(pushConstants);
            pushConstants = null;
        }
    } 
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        commandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, 
                    VK_COMMAND_BUFFER_LEVEL_SECONDARY, 
                    String.format("CVKPointRenderable %d", i));
            commandBuffers.add(buffer);
        }
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }   
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) {
        return commandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }       
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        CVKAssertNotNull(cvkSwapChain);
        cvkVisualProcessor.VerifyInRenderThread();
        int ret;
        
        try (MemoryStack stack = stackPush()) {
              
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.pNext(0);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);  // hard coding this for now
            beginInfo.pInheritanceInfo(inheritanceInfo);     

            VkCommandBuffer commandBuffer = commandBuffers.get(imageIndex).GetVKCommandBuffer();
            CVKAssert(commandBuffer != null);
            CVKAssert(pipelines.get(imageIndex) != null);
         
            ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
            checkVKret(ret);    

	    // Set the dynamic viewport and scissor
            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(cvkSwapChain.GetWidth());
            viewport.height(cvkSwapChain.GetHeight());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(cvkDevice.GetCurrentSurfaceExtent());

            vkCmdSetViewport(commandBuffer, 0, viewport);           
            vkCmdSetScissor(commandBuffer, 0, scissor);
            
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelines.get(imageIndex));

            LongBuffer pVertexBuffers = stack.longs(cvkVertexBuffer.GetBufferHandle());
            LongBuffer offsets = stack.longs(0);
            
            // Bind verts
            vkCmdBindVertexBuffers(commandBuffer, 0, pVertexBuffers, offsets);
                        
            // Push MVP matrix to the shader
            vkCmdPushConstants(commandBuffer,               // The buffer to push the matrix to
				hPipelineLayout,            // The pipeline layout
				VK_SHADER_STAGE_VERTEX_BIT, // Flags
				0,                          // Offset
				pushConstants);             // Matrix buffer
            
            // Copy draw commands
            vkCmdDraw(commandBuffer, GetVertexCount(), 1, 0, 0);
            
            ret = vkEndCommandBuffer(commandBuffer);
            checkVKret(ret);
        }
        return ret;
    }       
    
    private void DestroyCommandBuffers() {         
        if (null != commandBuffers) {
            commandBuffers.forEach(el -> {el.Destroy();});
            commandBuffers.clear();
            commandBuffers = null;
        }      
    } 


    // ========================> Descriptors <======================== \\
           
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPool.CVKDescriptorPoolRequirements reqs, CVKDescriptorPool.CVKDescriptorPoolRequirements perImageReqs) {
        // No descriptor sets required because axes use push constants instead of descriptor bound uniform buffers.
    }
  
    @Override
    public int DestroyDescriptorPoolResources() {         
        return VK_SUCCESS; 
    }
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
               
        int ret;       
        try (MemoryStack stack = stackPush()) {      
            VkPushConstantRange.Buffer pushConstantRange;
            pushConstantRange = VkPushConstantRange.callocStack(1, stack);
            pushConstantRange.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            pushConstantRange.size(VertexUniformBufferObject.SIZEOF);
            pushConstantRange.offset(0);                     
            
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);  
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssert(hPipelineLayout != VK_NULL_HANDLE);                
        }        
        return ret;        
    }    
    
    private void DestroyPipelineLayout() {
        if (hPipelineLayout != VK_NULL_HANDLE) {
            vkDestroyPipelineLayout(cvkDevice.GetDevice(), hPipelineLayout, null);
            hPipelineLayout = VK_NULL_HANDLE;
        }
    }     
    
    private int CreatePipelines() {
        CVKAssertNotNull(cvkDevice);
        CVKAssertNotNull(cvkDevice.GetDevice());
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(cvkSwapChain.GetSwapChainHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain.GetRenderPassHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkDescriptorPool.GetDescriptorPoolHandle() != VK_NULL_HANDLE);
        CVKAssert(hVertexShaderModule != VK_NULL_HANDLE);
        CVKAssert(hFragmentShaderModule != VK_NULL_HANDLE);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {           
            int imageCount = cvkSwapChain.GetImageCount();
             // A complete pipeline for each swapchain image.  Wasteful?
            pipelines = new ArrayList<>(imageCount);            
            for (int i = 0; i < imageCount; ++i) {       
                
                // ===> SHADER STAGE <===
                ByteBuffer entryPoint = stack.UTF8("main");
                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);
                
                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(hVertexShaderModule);
                vertShaderStageInfo.pName(entryPoint);
                
                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(hFragmentShaderModule);
                fragShaderStageInfo.pName(entryPoint);

                // ===> VERTEX STAGE <===
                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(Vertex.getBindingDescription());         // From Vertex struct
                vertexInputInfo.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions());    // From Vertex struct

                // ===> ASSEMBLY STAGE <===
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_POINT_LIST);
                inputAssembly.primitiveRestartEnable(false);

                // ===> VIEWPORT & SCISSOR <===
                VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
                viewport.x(0.0f);
                viewport.y(0.0f);
                viewport.width(cvkSwapChain.GetWidth());
                viewport.height(cvkSwapChain.GetHeight());
                viewport.minDepth(0.0f);
                viewport.maxDepth(1.0f);

                VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
                scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
                scissor.extent(cvkDevice.GetCurrentSurfaceExtent());

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
                rasterizer.cullMode(VK_CULL_MODE_NONE);
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
                depthStencil.depthCompareOp(VK_COMPARE_OP_NEVER);
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
                ret = vkCreateGraphicsPipelines(cvkDevice.GetDevice(), 
                                                VK_NULL_HANDLE, 
                                                pipelineInfo, 
                                                null, 
                                                pGraphicsPipeline);
                if (VkFailed(ret)) { return ret; }
                
                if (CVK_DEBUGGING) {
                    ++CVK_VKALLOCATIONS;
                    cvkDevice.GetLogger().info("CVK_VKALLOCATIONS(%d+) vkCreateGraphicsPipelines for CVKPointBuffer.pipeline_%d 0x%016X", 
                            CVK_VKALLOCATIONS, i, pGraphicsPipeline.get(0));                
                }                
                
                pipelines.add(pGraphicsPipeline.get(0));
                CVKAssert(pipelines.get(i) != VK_NULL_HANDLE);
            }
        }
        SetPipelinesState(CVK_RESOURCE_CLEAN);
        GetLogger().log(Level.INFO, "Graphics Pipeline created for AxesRenderable class.");
        
        return ret;
    }
    
    private void DestroyPipelines() {     
        if (pipelines != null) {
            for (int i = 0; i < pipelines.size(); ++i) {
                if (CVK_DEBUGGING) {
                    --CVK_VKALLOCATIONS;
                    cvkDevice.GetLogger().info("CVK_VKALLOCATIONS(%d-) vkDestroyPipeline for CVKPointBuffer.pipeline_%d 0x%016X", 
                            CVK_VKALLOCATIONS, i, pipelines.get(i));                
                }                     
                
                vkDestroyPipeline(cvkDevice.GetDevice(), pipelines.get(i), null);
                pipelines.set(i, VK_NULL_HANDLE);
            }
            pipelines.clear();
            pipelines = null;
        }       
    }   
        

    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() {         
        return vertexCount > 0 &&
               (vertexUBOState != CVK_RESOURCE_CLEAN ||           
                vertexBuffersState != CVK_RESOURCE_CLEAN ||
                commandBuffersState != CVK_RESOURCE_CLEAN ||
                descriptorSetsState != CVK_RESOURCE_CLEAN ||
                pipelinesState != CVK_RESOURCE_CLEAN); 
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        cvkVisualProcessor.VerifyInRenderThread();
                          
        // Update vertex buffers
        if (vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyVertexBuffer();
            ret = CreateVertexBuffer();
            if (VkFailed(ret)) { return ret; }         
        } else if (vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = UpdateVertexBuffer();
            if (VkFailed(ret)) { return ret; }           
        }

        // Vertex uniform buffer (camera guff)
        if (vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyPushConstants();
            CreatePushConstants();
        } else if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
            UpdatePushConstants();          
        }
        
        // Command buffers (rendering commands enqueued on the GPU)
        if (commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyCommandBuffers();
            ret = CreateCommandBuffers();
            if (VkFailed(ret)) { return ret; }  
        }           

        // Pipelines (all the render state and resources in one object)
        if (pipelinesState == CVK_RESOURCE_NEEDS_REBUILD) {
            DestroyPipelines();            
            ret = CreatePipelines();
            if (VkFailed(ret)) { return ret; }  
        }                                                                             
        
        return ret;
    }        
    
    
    // ========================> Tasks <======================== \\   
    
    private void RebuildVertexStagingBuffer(final VisualAccess access, final int newVertexCount) {
        // Note this will be called from the visual processer thread, not the render thread
        try {
            // Vertices are modified by the event thread
            vertexStagingBufferLock.lock(); 
            
            // Destroy old staging buffer if it exists
            if (cvkVertexStagingBuffer != null) {
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            }                
            
            if (newVertexCount > 0) {
                int vertexBufferSizeBytes = Vertex.SIZEOF * newVertexCount;
                cvkVertexStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                          vertexBufferSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                          "CVKPointRenderable.TaskCreateIcons cvkVertexStagingBuffer");               
                
                ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartMemoryMap(0, vertexBufferSizeBytes);
                for (int pos = 0; pos < newVertexCount; pos++) {
                    final int offset = Vertex.SIZEOF * pos;
                    pVertexMemory.position(offset);
                    pVertexMemory.putFloat(access.getX(pos));
                    pVertexMemory.putFloat(access.getY(pos));
                    pVertexMemory.putFloat(access.getZ(pos));
                }
                int vertMemPos = pVertexMemory.position();
                CVKAssert(vertMemPos == vertexBufferSizeBytes);
                cvkVertexStagingBuffer.EndMemoryMap();
                pVertexMemory = null; // now unmapped, do not use
            }
        } finally {
            vertexStagingBufferLock.unlock();
        }        
    }
    
    public CVKRenderUpdateTask TaskRebuildPoints(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        final int newVertexCount = access.getVertexCount();
        GetLogger().info(String.format("TaskRebuildPoints frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount()));
        RebuildVertexStagingBuffer(access, newVertexCount);
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            // We can't update the position buffer here as it is needed to render each image
            // in the swap chain.  If we recreate it for image 1 it will be likely be in
            // flight for presenting image 0.  The shared resource recreation path is
            // synchronised for all images so we need to do it there.
            SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);                  
            
            vertexCount = newVertexCount;
        };
    }    
    
    public CVKRenderUpdateTask TaskUpdatePoints(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        // If this fires investigate why we didn't get a rebuild task first
        final int newVertexCount = access.getVertexCount();
        GetLogger().info(String.format("TaskUpdatePoints frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount()));
        CVKAssert(vertexCount == newVertexCount); //REMOVE AFTER TESTING
        
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if the vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexStagingBuffer == null || 
                                        vertexCount != newVertexCount;
        if (rebuildRequired) {
            RebuildVertexStagingBuffer(access, newVertexCount);
        }
        
        try {
            vertexStagingBufferLock.lock();

            // We map the whole range as GraphVisualAccess applies any per vertex change to all 
            // vertices in the accessGraph so the change will contain all vertices anyway.
            ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartMemoryMap(0, (int)cvkVertexStagingBuffer.GetBufferSize());
            final int numChanges = change.getSize();
            for (int i = 0; i < numChanges; ++i) {
                int pos = change.getElement(i);
                final int offset = Vertex.SIZEOF * pos;
                pVertexMemory.position(offset);
                pVertexMemory.putFloat(access.getX(pos));
                pVertexMemory.putFloat(access.getY(pos));
                pVertexMemory.putFloat(access.getZ(pos));
            }
        } finally {
            vertexStagingBufferLock.unlock();
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
                
                vertexCount = newVertexCount;
            } else if (vertexBuffersState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };        
    }
    
    public CVKRenderUpdateTask TaskUpdateCamera() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
                                                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {     
            if (vertexUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexUBOState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };           
    }    
}
