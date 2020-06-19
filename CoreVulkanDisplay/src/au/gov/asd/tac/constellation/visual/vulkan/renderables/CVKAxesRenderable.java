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
import au.gov.asd.tac.constellation.visual.AutoDrawable;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKFrame;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderer;
import au.gov.asd.tac.constellation.visual.vulkan.CVKScene;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.SPIRV;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.ShaderKind.FRAGMENT_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.ShaderKind.VERTEX_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.compileShaderFile;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
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
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;


public class CVKAxesRenderable implements CVKRenderable {
    protected final CVKScene scene;
    
    // Compiled Shader modules
    protected static long vertShaderModule = 0;
    protected static long fragShaderModule = 0;
    
    private long pipelineLayout;
    private long graphicsPipeline;
    public VkCommandBuffer commandBuffer;
    private PointerBuffer handlePointer;
    
    // DESCRIPTOR SET
    
    protected static SPIRV vertShaderSPIRV;
    protected static SPIRV fragShaderSPIRV;
    
    
    public CVKAxesRenderable(CVKScene inScene) {
        scene = inScene;
    }
    
    
    @Override
    public VkCommandBuffer GetCommandBuffer(){return commandBuffer; }
    
    @Override
    public long GetGraphicsPipeline(){return graphicsPipeline; }
    
    @Override
    public int GetVertex(){return 3; }
    
    @Override
    public int getPriority() { if (true) throw new UnsupportedOperationException(""); else return 0; }
    @Override
    public void dispose(final AutoDrawable drawable) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void init(final AutoDrawable drawable) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void reshape(final int x, final int y, final int width, final int height) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void update(final AutoDrawable drawable) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void display(final AutoDrawable drawable, final Matrix44f pMatrix) {
        
    }
    
    @Override
    public void draw(VkCommandBuffer commandBuffer){

            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
            vkCmdDraw(commandBuffer, GetVertex(), 1, 0, 0);
            
            // TODO Draw indexed
            //VkDeviceSize offsets[1] = { 0 };
            //vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, *pipeline);
            //vkCmdBindVertexBuffers(cmdBuffer, VERTEX_BUFFER_BIND_ID, 1, &model.vertices.buffer, offsets);
            //vkCmdBindIndexBuffer(cmdBuffer, model.indices.buffer, 0, VK_INDEX_TYPE_UINT32);
            //vkCmdDrawIndexed(cmdBuffer, model.indexCount, 1, 0, 0, 0);
    }

    public int CreatePipeline(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        // TODO Add param checking
        
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
                       
            ByteBuffer entryPoint = stack.UTF8("main");

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);

            VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

            vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
            vertShaderStageInfo.module(vertShaderModule);
            vertShaderStageInfo.pName(entryPoint);

            VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);

            fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
            fragShaderStageInfo.module(fragShaderModule);
            fragShaderStageInfo.pName(entryPoint);

            // ===> VERTEX STAGE <===

            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);

            // ===> ASSEMBLY STAGE <===

            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            inputAssembly.primitiveRestartEnable(false);

            // ===> VIEWPORT & SCISSOR

            // TODO Make this the top right hand corner only
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
            rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
            rasterizer.frontFace(VK_FRONT_FACE_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            // ===> MULTISAMPLING <===

            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(false);
            multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

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

            // ===> PIPELINE LAYOUT CREATION <===

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

            if(vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create pipeline layout");
            }

            pipelineLayout = pPipelineLayout.get(0);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.layout(pipelineLayout);
            pipelineInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
            pipelineInfo.subpass(0);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);

            if(vkCreateGraphicsPipelines(cvkDevice.GetDevice(), VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create graphics pipeline");
            }

            graphicsPipeline = pGraphicsPipeline.get(0);

            // ===> RELEASE RESOURCES <===

            vkDestroyShaderModule(cvkDevice.GetDevice(), vertShaderModule, null);
            vkDestroyShaderModule(cvkDevice.GetDevice(), fragShaderModule, null);

            vertShaderSPIRV.free();
            fragShaderSPIRV.free();
        }
        return ret;
    }
    
    /*
    device: VK device
    commandPool: handles the memory for command buffers
    level: Can be PRIMARY or SECONDAY
    */
    public int CreateCommandBuffer(CVKDevice cvkDevice, long commandPool, int level){
        VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
	                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
	                .commandPool(commandPool)
	                .level(VK_COMMAND_BUFFER_LEVEL_SECONDARY)  // Testing out secondary buffers
	                .commandBufferCount(1);
		 
        handlePointer = memAllocPointer(1);
        int err = vkAllocateCommandBuffers(cvkDevice.GetDevice(), cmdBufAllocateInfo, handlePointer);

        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to allocate command buffer: ");
        }

        commandBuffer = new VkCommandBuffer(handlePointer.get(0), cvkDevice.GetDevice());

        cmdBufAllocateInfo.free();
        
        return 1;
    }
    
    // Testing secondary command buffers for each object
    @Override
    public int RecordCommandBuffer(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {	
        int ret = VK_SUCCESS;
        
        VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc()
                            .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                            .pNext(0)
                            .framebuffer(0)  // can be null but can have better performance if set
                            .renderPass(cvkSwapChain.GetRenderPassHandle())
                            .subpass(0)  // think i read this has to be set...
                            .occlusionQueryEnable(false)
                            .queryFlags(0)
                            .pipelineStatistics(0);

        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pNext(0)
                .flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT)  // hard coding this for now
                .pInheritanceInfo(inheritanceInfo);

        int err = vkBeginCommandBuffer(commandBuffer, beginInfo);

        if (err != VK_SUCCESS) {
                throw new AssertionError("Failed to begin record command buffer: ");
        }

        // Record stuff here
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

        // Check these params with the Oren engine, think they are different for secondary buffers
        vkCmdDraw(commandBuffer, 3, 1, 0, 0);
                
        beginInfo.free();

        ret = vkEndCommandBuffer(commandBuffer);
        if (ret != VK_SUCCESS) {
            throw new AssertionError("Failed to finish record command buffer: ");
        }
       
        return ret;
    }
    
    //@Override
    public int RecordPrimaryCommandBuffer(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        // TODO add param checking 
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
            
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);

            renderPassInfo.renderPass(cvkSwapChain.GetRenderPassHandle());

            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(cvkSwapChain.GetExtent());
            renderPassInfo.renderArea(renderArea);

            VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
            clearValues.color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);

            if(vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                throw new RuntimeException("Failed to begin recording command buffer");
            }

            renderPassInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(0));

            // I believe the render pass should only be done on primary command buffers
            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

                // 3 = number of verts, should store this
                vkCmdDraw(commandBuffer, 3, 1, 0, 0);
            }
            vkCmdEndRenderPass(commandBuffer);


            if(vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to record command buffer");
            }
        }
        return ret;
    }

    
    public int DestroyPipeline(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {
            // Destroy the command buffer
        }
        return ret;
    }
    
    
    @Override
    public int SwapChainRezied(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        int ret = DestroyPipeline(cvkDevice, cvkSwapChain);
        if (VkSucceeded(ret)) {
            ret = CreatePipeline(cvkDevice, cvkSwapChain);
            //CreateCommandBuffer(cvkDevice, cvkDevice.GetCommandPoolHandle(), 1);
            //RecordCommandBuffer(cvkDevice, cvkSwapChain);
        }
        return ret;
    }
    
    
    public static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;

        try{
            vertShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "09_shader_base.vert", VERTEX_SHADER);
            fragShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "09_shader_base.frag", FRAGMENT_SHADER);

            vertShaderModule = CVKShaderUtils.createShaderModule(vertShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            fragShaderModule = CVKShaderUtils.createShaderModule(fragShaderSPIRV.bytecode(), cvkDevice.GetDevice());
        } catch(Exception ex){
            CVKLOGGER.log(Level.WARNING, "Failed to compile AxesRenderable shaders: {0}", ex.toString());
        }
        return ret;
    }
    
    @Override
    public int DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
        return VK_SUCCESS;
    }    
    
    @Override
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[]) {
        assert(descriptorTypeCounts.length == (VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT + 1));
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
    }    
    
    
    public static int CreateDescriptorLayout(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {
            /*
            Vertex shader needs a uniform buffer.
            Geometry shader needs a different uniform buffer.
            Fragment shader needs a sampler2Darray
            */

//            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(3, stack);
//
//            VkDescriptorSetLayoutBinding vertexUBOLayout = bindings.get(0);
//            vertexUBOLayout.binding(0);
//            vertexUBOLayout.descriptorCount(1);
//            vertexUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//            vertexUBOLayout.pImmutableSamplers(null);
//            vertexUBOLayout.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
//            
//            VkDescriptorSetLayoutBinding geomUBOLayout = bindings.get(1);
//            geomUBOLayout.binding(1);
//            geomUBOLayout.descriptorCount(1);
//            geomUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//            geomUBOLayout.pImmutableSamplers(null);
//            geomUBOLayout.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);            
//
//            VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(2);
//            samplerLayoutBinding.binding(2);
//            samplerLayoutBinding.descriptorCount(1);
//            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
//            samplerLayoutBinding.pImmutableSamplers(null);
//            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
//
//            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
//            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
//            layoutInfo.pBindings(bindings);
//
//            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
//
//            ret = vkCreateDescriptorSetLayout(cvkDevice.GetDevice(), layoutInfo, null, pDescriptorSetLayout);
//            if (VkSucceeded(ret)) {
//                hDescriptorLayout = pDescriptorSetLayout.get(0);
//            }
        }        
        return ret;
    }    
    
    @Override
    public void Display(MemoryStack stack, CVKFrame frame, CVKRenderer cvkRenderer, CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {}
}
