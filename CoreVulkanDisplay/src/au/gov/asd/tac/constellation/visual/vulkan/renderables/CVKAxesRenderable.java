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
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
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
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
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
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
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
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import au.gov.asd.tac.constellation.utilities.graphics.Vector2f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class CVKAxesRenderable implements CVKRenderable {
    protected final CVKScene scene;
    
    // Compiled Shader modules
    protected static long vertShaderModule = VK_NULL_HANDLE;
    protected static long fragShaderModule = VK_NULL_HANDLE;
    
    // Compiled Shaders
    protected static SPIRV vertShaderSPIRV;
    protected static SPIRV fragShaderSPIRV;
    
    private long pipelineLayout;
    private long graphicsPipeline;
    protected List<VkCommandBuffer> commandBuffers = null;
    private PointerBuffer handlePointer;
    
    private long vertexBuffer;
    private long vertexBufferMemory;
        
    protected List<CVKBuffer> vertUniformBuffers = null;
    protected List<CVKBuffer> geomUniformBuffers = null;
    protected List<CVKBuffer> vertBuffers = null;
    
    protected boolean isDirty = true;
    
    private static class Vertex {

        private static final int SIZEOF = (2 + 3) * Float.BYTES;
        private static final int OFFSETOF_POS = 0;
        private static final int OFFSETOF_COLOR = 2 * Float.BYTES;

        private Vector2f pos;
        private Vector3f color;

        public Vertex(Vector2f pos, Vector3f color) {
            this.pos = pos;
            this.color = color;
        }

        private static VkVertexInputBindingDescription.Buffer getBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            bindingDescription.binding(0);
            bindingDescription.stride(Vertex.SIZEOF);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            return bindingDescription;
        }

        private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // Position
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(0);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32_SFLOAT);
            posDescription.offset(OFFSETOF_POS);

            // Color
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(0);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            colorDescription.offset(OFFSETOF_COLOR);

            return attributeDescriptions.rewind();
        }
    }
    
    private static final Vertex[] VERTICES = {
                new Vertex(new Vector2f(0.0f, -0.5f), new Vector3f(1.0f, 0.0f, 0.0f)),
                new Vertex(new Vector2f(0.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f)),
                new Vertex(new Vector2f(-0.5f, 0.5f), new Vector3f(1.0f, 0.0f, 1.0f))
    };
     
    public CVKAxesRenderable(CVKScene inScene) {
        scene = inScene;
    }
    
    void Cleanup(CVKDevice cvkDevice) {
        // Destory pipeline
        
        // Destroy vertex buffers
        //vkDestroyBuffer(cvkDevice, vertexBuffer);
        //vkFreeMemory(cvkDevice, vertexBufferMemory);
    }
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int index){
        assert(index < commandBuffers.size());
        return commandBuffers.get(index); 
    }
    
    @Override
    public long GetGraphicsPipeline(){return graphicsPipeline; }
    
    @Override
    public int GetVertexCount(){return VERTICES.length; }
    
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
    public boolean IsDirty(){return isDirty; }
    
    public int CreatePipeline(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        // TODO Add param checking
        
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
            
           // ret = CreateUniformBuffer(cvkDevice, cvkSwapChain);
          //  checkVKret(ret);
            
           // ret = CreateDescriptorSet(cvkDevice, cvkSwapChain);
           // checkVKret(ret);     
            
            ret = CreateVertexBuffer(cvkDevice);
            checkVKret(ret);   
            
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
            vertexInputInfo.pVertexBindingDescriptions(Vertex.getBindingDescription());         // From Vertex struct
            vertexInputInfo.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions());    // From Vertex struct
            
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
        }
        
        CVKLOGGER.log(Level.INFO, "Graphics Pipeline created for AxesRenderable class.");
        
        return ret;
    }
    
    private int CreateVertexBuffer(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {

            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(Vertex.SIZEOF * VERTICES.length);
            bufferInfo.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
            bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pVertexBuffer = stack.mallocLong(1);

            if(vkCreateBuffer(cvkDevice.GetDevice(), bufferInfo, null, pVertexBuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create vertex buffer");
            }
            vertexBuffer = pVertexBuffer.get(0);

            // Find the right memory requirements
            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetBufferMemoryRequirements(cvkDevice.GetDevice(), vertexBuffer, memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(),
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, cvkDevice));

            LongBuffer pVertexBufferMemory = stack.mallocLong(1);

            // Allocate memory
            if(vkAllocateMemory(cvkDevice.GetDevice(), allocInfo, null, pVertexBufferMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate vertex buffer memory");
            }
            vertexBufferMemory = pVertexBufferMemory.get(0);

            vkBindBufferMemory(cvkDevice.GetDevice(), vertexBuffer, vertexBufferMemory, 0);

            PointerBuffer data = stack.mallocPointer(1);

            // Fill the memory with the data from the Vertex buffer
            vkMapMemory(cvkDevice.GetDevice(), vertexBufferMemory, 0, bufferInfo.size(), 0, data);
            {
                memcpy(data.getByteBuffer(0, (int) bufferInfo.size()), VERTICES);
            }
            vkUnmapMemory(cvkDevice.GetDevice(), vertexBufferMemory);
        }
        
        return ret;
    }
    
    /*
    device: VK device
    commandPool: handles the memory for command buffers
    level: Can be PRIMARY or SECONDAY
    */
    @Override 
    public int InitCommandBuffer(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain){
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {
            int imageCount = cvkSwapChain.GetImageCount();
            commandBuffers = new ArrayList<>(imageCount);

            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc();
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.commandPool(cvkDevice.GetCommandPoolHandle());
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_SECONDARY);
            allocInfo.commandBufferCount(imageCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(imageCount);
            ret = vkAllocateCommandBuffers(cvkDevice.GetDevice(), allocInfo, pCommandBuffers);
            checkVKret(ret);

            for (int i = 0; i < imageCount; ++i) {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), cvkDevice.GetDevice()));
            }

            allocInfo.free();

            CVKLOGGER.log(Level.INFO, "Init Command Buffer - AxesRenderable");
        }
        return ret;
    }
       
    // Called from Record in the Renderer
    @Override
    public int RecordCommandBuffer(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, VkCommandBufferInheritanceInfo inheritanceInfo, int index) {	
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
   
            int imageCount = cvkSwapChain.GetImageCount();
            
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc();
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.pNext(0);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);  // hard coding this for now
            beginInfo.pInheritanceInfo(inheritanceInfo);     

            //for (int i = 0; i < imageCount; ++i) {
                VkCommandBuffer commandBuffer = commandBuffers.get(index);
         
                ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
                checkVKret(ret);

            // TODO! This is weird, example on setting viewort and scissors without the pipeline layout?
            // if the viewport changes i think we rebuild the pipeline?
            // Viewport
            //            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            //            viewport.x(0.0f);
            //            viewport.y(0.0f);
            //            viewport.width(cvkSwapChain.GetWidth());
            //            viewport.height(cvkSwapChain.GetHeight());
            //            viewport.minDepth(0.0f);
            //            viewport.maxDepth(1.0f);
            //         
            //            // Scissor
            //            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            //            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            //            scissor.extent(cvkDevice.GetCurrentSurfaceExtent()); 
            //
            //            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
            //            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            //            viewportState.pViewports(viewport);
            //            viewportState.pScissors(scissor);            

            // Record stuff here
            //vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
            //vkCmdDraw(commandBuffer, GetVertexCount(), 1, 0, 0);

                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

                LongBuffer vertexBuffers = stack.longs(vertexBuffer);
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
                vkCmdDraw(commandBuffer, GetVertexCount(), 1, 0, 0);

                ret = vkEndCommandBuffer(commandBuffer);
                checkVKret(ret);
            //}
            
            beginInfo.free();
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
            InitCommandBuffer(cvkDevice, cvkSwapChain);
        }
        return ret;
    }
    
    public static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;

        try{
            vertShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "17_shader_vertexbuffer.vert", VERTEX_SHADER);
            fragShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "17_shader_vertexbuffer.frag", FRAGMENT_SHADER);

            vertShaderModule = CVKShaderUtils.createShaderModule(vertShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            fragShaderModule = CVKShaderUtils.createShaderModule(fragShaderSPIRV.bytecode(), cvkDevice.GetDevice());
        } catch(Exception ex){
            CVKLOGGER.log(Level.WARNING, "Failed to compile AxesRenderable shaders: {0}", ex.toString());
        }
        
        CVKLOGGER.log(Level.INFO, "Static shaders loaded for AxesRenderable class");
        return ret;
    }
    
    @Override
    public int DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
        // UPDATE CODE
        
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
    public void Display(MemoryStack stack, CVKFrame frame, CVKRenderer cvkRenderer, CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
        //assert(commandBuffer != null);
        //VkCommandBuffer vkCommandBuffer = commandBuffer;
        //cvkRenderer.ExecuteCommandBuffer(stack, frame, vkCommandBuffer);
    }
    
     private void memcpy(ByteBuffer buffer, Vertex[] vertices) {
        for(Vertex vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
        }
    }

    private int findMemoryType(int typeFilter, int properties, CVKDevice cvkDevice) {

        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
        vkGetPhysicalDeviceMemoryProperties(cvkDevice.GetDevice().getPhysicalDevice(), memProperties);

        for(int i = 0;i < memProperties.memoryTypeCount();i++) {
            if((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }

        throw new RuntimeException("Failed to find suitable memory type");
    }
}
