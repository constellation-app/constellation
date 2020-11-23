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

import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.GetParentMethodName;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang3.mutable.MutableLong;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_DST_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

public abstract class CVKRenderable {
    
    protected enum CVKRenderableResourceState {
        CVK_RESOURCE_CLEAN,
        CVK_RESOURCE_NEEDS_UPDATE,
        CVK_RESOURCE_NEEDS_REBUILD               
    }
    
    protected final CVKVisualProcessor cvkVisualProcessor;
    protected CVKDescriptorPool cvkDescriptorPool = null;
    protected CVKSwapChain cvkSwapChain = null;
    protected boolean descriptorPoolResourcesDirty = false;
    protected boolean swapChainImageCountChanged = true;
    protected boolean swapChainResourcesDirty = false;
    protected boolean isInitialised = false;
    protected long hDescriptorLayout = VK_NULL_HANDLE; 
    protected long hPipelineLayout = VK_NULL_HANDLE;  
    protected Long hVertexShaderModule = VK_NULL_HANDLE;
    protected Long hGeometryShaderModule = VK_NULL_HANDLE;
    protected Long hFragmentShaderModule = VK_NULL_HANDLE;    
    protected List<Long> displayPipelines = null;
    
    // Render states that can be overridden by each subclass
    protected boolean colourBlend = true;
    protected boolean depthTest = true;
    protected boolean depthWrite = true;
    protected boolean logicOpEnable = false;
    protected int cullMode = VK_CULL_MODE_NONE;
    protected int depthCompareOperation = VK_COMPARE_OP_LESS_OR_EQUAL;
    protected int assemblyTopology = VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
    protected int srcColourBlendFactor = VK_BLEND_FACTOR_SRC_ALPHA;
    protected int dstColourBlendFactor = VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
    protected int colourBlendOp = VK_BLEND_OP_ADD;
    protected int srcAlphaBlendFactor = VK_BLEND_FACTOR_SRC_ALPHA;
    protected int dstAlphaBlendFactor = VK_BLEND_FACTOR_DST_ALPHA;
    protected int alphaBlendOp = VK_BLEND_OP_ADD;    
    protected int logicOp = VK_LOGIC_OP_COPY;
    protected int colourWriteMask = VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT;
    
    // Resource states, not every type will be used by each renderable
    protected CVKRenderableResourceState vertexUBOState = CVK_RESOURCE_CLEAN;
    protected CVKRenderableResourceState geometryUBOState = CVK_RESOURCE_CLEAN;
    protected CVKRenderableResourceState vertexBuffersState = CVK_RESOURCE_CLEAN;
    protected CVKRenderableResourceState commandBuffersState = CVK_RESOURCE_CLEAN;
    protected CVKRenderableResourceState descriptorSetsState = CVK_RESOURCE_CLEAN;
    protected CVKRenderableResourceState pipelinesState = CVK_RESOURCE_CLEAN;        
    
    
    // ========================> Debuggering <======================== \\
    
    public boolean DEBUG_skipRender = false;
    public boolean DebugSkipRender() { return DEBUG_skipRender; }
    
    protected static boolean LOGSTATECHANGE = false;
    protected void SetVertexUBOState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t vertexUBOState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), vertexUBOState.name(), state.name(), GetParentMethodName());        
        }
        vertexUBOState = state;
    }
    protected void SetGeometryUBOState(final CVKRenderableResourceState state) {
        CVKAssert(!(geometryUBOState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t geometryUBOState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), geometryUBOState.name(), state.name(), GetParentMethodName());        
        }
        geometryUBOState = state;
    }
    protected void SetVertexBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t vertexBuffersState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), vertexBuffersState.name(), state.name(), GetParentMethodName()); 
        }
        vertexBuffersState = state;
    }
    protected void SetCommandBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t commandBuffersState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), commandBuffersState.name(), state.name(), GetParentMethodName());
        }
        commandBuffersState = state;
    }
    protected void SetDescriptorSetsState(final CVKRenderableResourceState state) {
        CVKAssert(!(descriptorSetsState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t descriptorSetsState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), descriptorSetsState.name(), state.name(), GetParentMethodName());
        }
        descriptorSetsState = state;
    }
    protected void SetPipelinesState(final CVKRenderableResourceState state) {
        CVKAssert(!(pipelinesState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t pipelinesState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), pipelinesState.name(), state.name(), GetParentMethodName());   
        }
        pipelinesState = state;
    }  
    
    
    // ========================> Shaders <======================== \\
    
    protected String GetVertexShaderName()   { return null; }
    protected String GetGeometryShaderName() { return null; }
    protected String GetFragmentShaderName() { return null; }
    
    
    // ========================> Lifetime <======================== \\
    
    public CVKRenderable(CVKVisualProcessor visualProcessor) {
        cvkVisualProcessor = visualProcessor;
    }
           

    
    /**
     * This is called either when a new renderable is added to CVKRenderer or
     * it the renderer has not been initialised itself at that point, called
     * when the renderer is initialised.
     * 
     * @return
     */
    public int Initialise() {
        int ret = VK_SUCCESS;
        
        MutableLong shaderModule = new MutableLong(VK_NULL_HANDLE);
        final String vertexShaderName = GetVertexShaderName();
        if (vertexShaderName != null) {            
            ret = CVKShaderUtils.LoadShader(vertexShaderName, shaderModule);            
            if (VkFailed(ret)) { return ret; }
            hVertexShaderModule = shaderModule.longValue();
        }
        final String geometryShaderName = GetGeometryShaderName();
        if (geometryShaderName != null) {
             ret = CVKShaderUtils.LoadShader(geometryShaderName, shaderModule);
             if (VkFailed(ret)) { return ret; }
             hGeometryShaderModule = shaderModule.longValue();
        }            
        final String fragmentShaderName = GetFragmentShaderName();
        if (fragmentShaderName != null) {
             ret = CVKShaderUtils.LoadShader(fragmentShaderName, shaderModule);
             if (VkFailed(ret)) { return ret; }
             hFragmentShaderModule = shaderModule.longValue();
        }            
           
        return ret;
    }
    
    /**
     * Cleanup, terminal, called when a graph is closing
     */
    public abstract void Destroy();
    
    protected CVKGraphLogger GetLogger() { return cvkVisualProcessor.GetLogger(); }
    
    /**
     * Returns the command buffer for the current Image being sent
     * to the GFX drivers
     */
    public abstract VkCommandBuffer GetDisplayCommandBuffer(int imageIndex);        
    
    /**
     * Returns the hit test command buffer for the current Image being sent
     * to the GFX drivers
     */
    public VkCommandBuffer GetHitTestCommandBuffer(int imageIndex){ return null; }
        
    /**
     * Called just before the swapchain is about to be destroyed allowing the
     * object to cleanup its resources.
     * 
     * @return error code
     */
    protected abstract int DestroySwapChainResources();
    
    /**
     * 
     * Called just after the swapchain has been recreated
     * 
     * @param cvkSwapChain
     * @return error code
    */
    //public abstract int CreateSwapChainResources(CVKSwapChain cvkSwapChain);
    
    /**
     * Called just before the descriptor pool is about to be destroyed allowing the
     * object to cleanup its descriptors.
     * 
     * @return error code
     */    
    protected abstract int DestroyDescriptorPoolResources();
    
    /**
     * 
     * Called just after a new descriptor pool has been created but before the
     * old one has been destroyed.  This gives us a chance to cleanup resources
     * created from the old pool and remember the new pool.  Note we don't create
     * the new descriptor pool resources until the next call to DisplayUpdate as
     * at the point we are called the swapchain may also be pending recreation.
     * 
     * @param newDescriptorPool
     * @return error code
    */
    public int SetNewDescriptorPool(CVKDescriptorPool newDescriptorPool) {
        int ret = VK_SUCCESS;
        
        // If this isn't the initial update, release swapchain resources
        if (cvkDescriptorPool != null) {
            ret = DestroyDescriptorPoolResources();
            if (VkFailed(ret)) { return ret; }
        }        
              
        cvkDescriptorPool = newDescriptorPool;
        descriptorPoolResourcesDirty = true;
        
        return ret;
    }
    
    /**
     * 
     * Called just after a new swapchain has been created but before the
     * old one has been destroyed.  This gives us a chance to cleanup resources
     * created for the old swapchain and remember the new swapchain.  Note we 
     * don't create the new swapchain resources until the next call to DisplayUpdate 
     * as at the point we are called the descriptor pool may also be pending recreation.
     * 
     * @param newSwapChain
     * @return error code
    */    
    public int SetNewSwapChain(CVKSwapChain newSwapChain) {
        int ret = VK_SUCCESS;
        
        swapChainImageCountChanged = cvkSwapChain == null || 
                                     newSwapChain == null ||
                                     newSwapChain.GetImageCount() != cvkSwapChain.GetImageCount();
        
        // If this isn't the initial update, release swapchain resources
        if (cvkSwapChain != null) {            
            ret = DestroySwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }
                     
        cvkSwapChain = newSwapChain;
        swapChainResourcesDirty = true;
        
        return ret;
    }    
    
    public abstract void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs);
    public abstract int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int index);
    public int RecordHitTestCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int index) { return VK_SUCCESS; }
    public int OffscreenRender(List<CVKRenderable> hitTestRenderables){ return VK_SUCCESS; }

    /**
     * @return Returns the number of vertices used in the vertex buffer
     */
    public abstract int GetVertexCount();

    
    public boolean NeedsDisplayUpdate() { return false; }
    public int DisplayUpdate() { return VK_SUCCESS; }
    
    
    // ========================> Pipelines <======================== \\
    protected abstract VkVertexInputBindingDescription.Buffer GetVertexBindingDescription();
    protected abstract VkVertexInputAttributeDescription.Buffer GetVertexAttributeDescriptions();     
    
    protected int CreatePipelines(long renderPassHandle, List<Long> pipelines) {
        CVKAssert(hPipelineLayout != VK_NULL_HANDLE);
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain.GetSwapChainHandle());
        CVKAssertNotNull(renderPassHandle != VK_NULL_HANDLE);
        CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());
        CVKAssertNotNull(hVertexShaderModule);
        CVKAssertNotNull(hGeometryShaderModule);
        CVKAssertNotNull(hFragmentShaderModule);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);       
        
        int shaderStageCount = 0;
        shaderStageCount += hVertexShaderModule   != VK_NULL_HANDLE ? 1 : 0;
        shaderStageCount += hGeometryShaderModule != VK_NULL_HANDLE ? 1 : 0;
        shaderStageCount += hFragmentShaderModule != VK_NULL_HANDLE ? 1 : 0;
               
        final int imageCount = cvkSwapChain.GetImageCount();                
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {                 
            // A complete pipeline for each swapchain image.  Wasteful?
            for (int i = 0; i < imageCount; ++i) {                                               
                final ByteBuffer entryPoint = stack.UTF8("main");

                int iShaderStage = 0;
                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(shaderStageCount, stack);

                if (hVertexShaderModule != VK_NULL_HANDLE) {
                    VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(iShaderStage++);
                    vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                    vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                    vertShaderStageInfo.module(hVertexShaderModule);
                    vertShaderStageInfo.pName(entryPoint);
                }

                if (hGeometryShaderModule != VK_NULL_HANDLE) {
                    VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(iShaderStage++);
                    geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                    geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
                    geomShaderStageInfo.module(hGeometryShaderModule);
                    geomShaderStageInfo.pName(entryPoint);   
                }

                if (hFragmentShaderModule != VK_NULL_HANDLE) {
                    VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(iShaderStage++);
                    fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                    fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                    fragShaderStageInfo.module(hFragmentShaderModule);
                    fragShaderStageInfo.pName(entryPoint);
                }

                // ===> VERTEX STAGE <===
                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(GetVertexBindingDescription());
                vertexInputInfo.pVertexAttributeDescriptions(GetVertexAttributeDescriptions());

                // ===> ASSEMBLY STAGE <===
                // Each point becomes two or more triangles in the geometry shader, but our input is a point list
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(assemblyTopology);
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
                rasterizer.cullMode(cullMode);
                rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
                rasterizer.depthBiasEnable(false);

// TODO: hook this up.  See the block in CVKDevice where we query physical device caps.                
//                VkPipelineRasterizationLineStateCreateInfoEXT lineRasterInfo = VkPipelineRasterizationLineStateCreateInfoEXT.callocStack(stack);
//                lineRasterInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_LINE_STATE_CREATE_INFO_EXT);
//                lineRasterInfo.lineRasterizationMode(VK_LINE_RASTERIZATION_MODE_RECTANGULAR_SMOOTH_EXT);
//                lineRasterInfo.stippledLineEnable(false);                               
//                rasterizer.pNext(lineRasterInfo.address());                

                // ===> MULTISAMPLING <===
                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
                multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
                multisampling.sampleShadingEnable(false);
                multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
                
                // ===> DEPTH <===
                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
                depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
                depthStencil.depthTestEnable(depthTest);
                depthStencil.depthWriteEnable(depthWrite);
                depthStencil.depthCompareOp(depthCompareOperation);
                depthStencil.depthBoundsTestEnable(false);
                depthStencil.stencilTestEnable(false);                       
    
                // ===> COLOR BLENDING <===
                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
                colorBlendAttachment.colorWriteMask(colourWriteMask);
                colorBlendAttachment.blendEnable(colourBlend);
                if (colourBlend) {
                    colorBlendAttachment.srcColorBlendFactor(srcColourBlendFactor);
                    colorBlendAttachment.dstColorBlendFactor(dstColourBlendFactor);
                    colorBlendAttachment.colorBlendOp(colourBlendOp);
                    colorBlendAttachment.srcAlphaBlendFactor(srcAlphaBlendFactor);
                    colorBlendAttachment.dstAlphaBlendFactor(dstAlphaBlendFactor);
                    colorBlendAttachment.alphaBlendOp(alphaBlendOp);  
                }

                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
                colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
                colorBlending.logicOpEnable(logicOpEnable);
                colorBlending.logicOp(logicOp);
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
                pipelineInfo.renderPass(renderPassHandle);
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
                CVKAssert(pGraphicsPipeline.get(0) != VK_NULL_HANDLE);  
                pipelines.add(pGraphicsPipeline.get(0));                      
            }
        }
        
        SetPipelinesState(CVK_RESOURCE_CLEAN);
        if (CVK_DEBUGGING) {
            GetLogger().log(Level.INFO, "Graphics Pipeline created for class %s", getClass().getName());
        }
        return ret;
    }        
    
    protected void DestroyPipelines() {
        if (displayPipelines != null) {
            for (int i = 0; i < displayPipelines.size(); ++i) {
                vkDestroyPipeline(CVKDevice.GetVkDevice(), displayPipelines.get(i), null);
                displayPipelines.set(i, VK_NULL_HANDLE);
            }
            displayPipelines.clear();
            displayPipelines = null;
        }        
    }    
}
