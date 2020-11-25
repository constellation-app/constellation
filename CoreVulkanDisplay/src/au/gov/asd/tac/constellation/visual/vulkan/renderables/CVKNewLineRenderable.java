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

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.visual.NewLineModel;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkPushConstantRange;


/*******************************************************************************
 * CVKNewLineRenderable
 * 
 * This class renders the indicator line when the user has selected a node in 
 * the add connection mode.  It is not the line for the connection (which uses
 * different shaders).  There is only ever one indicator line.  This renderable
 * is very similar to CVKAxesRenderable in that is draws its line in screen
 * space and uses the same shaders.
 * 
 * This is the equivalent of au.gov.asd.tac.constellation.graph.interaction.
 * visual.renderables.NewLineRenderable in the JOGL display version.
 *******************************************************************************/

public class CVKNewLineRenderable extends CVKRenderable {
    // From CoreInteractiveGraph\src\au\gov\asd\tac\constellation\graph\interaction\visual\renderables\NewLineRenderable.java    
    public static final int NEW_LINE_WIDTH = 2;
    public static final Vector4f NEW_LINE_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final int NUMBER_OF_VERTICES = 2;

    // Copied from NewLineRenderable this is the mechanism for synchronising input
    // and rendering threads.  Only the last 'model' is used.
    private NewLineModel model = null;
    private final BlockingDeque<NewLineModel> modelQueue = new LinkedBlockingDeque<>();    
    
    private final Vertex[] vertices = new Vertex[NUMBER_OF_VERTICES];
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private CVKBuffer cvkStagingBuffer = null;
    private CVKBuffer cvkVertexBuffer = null;
    private List<CVKCommandBuffer> displayCommandBuffers = null;
    private ByteBuffer pushConstants = null;
 
    
    // ========================> Classes <======================== \\    
    
    private static class Vertex {

        private static final int BYTES = Vector3f.BYTES + Vector4f.BYTES;
        private static final int OFFSETOF_POS = 0;
        private static final int OFFSETOF_COLOR = Vector3f.BYTES;

        private final Vector3f vertex;
        private final Vector4f color;

        public Vertex(final Vector3f vertex, final Vector4f color) {
            this.vertex = vertex;
            this.color = color;
        }
        
        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
            for(Vertex vertex : vertices) {
                buffer.putFloat(vertex.vertex.getX());
                buffer.putFloat(vertex.vertex.getY());
                buffer.putFloat(vertex.vertex.getZ());
                                
                buffer.putFloat(vertex.color.a[0]);
                buffer.putFloat(vertex.color.a[1]);
                buffer.putFloat(vertex.color.a[2]);
                buffer.putFloat(vertex.color.a[3]);
            }
        }

        private static VkVertexInputBindingDescription.Buffer GetBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            bindingDescription.binding(0);
            bindingDescription.stride(Vertex.BYTES);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            return bindingDescription;
        }

        private static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // Vertex
            VkVertexInputAttributeDescription vertexDescription = attributeDescriptions.get(0);
            vertexDescription.binding(0);
            vertexDescription.location(0);
            vertexDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            vertexDescription.offset(OFFSETOF_POS);

            // Color
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(0);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            colorDescription.offset(OFFSETOF_COLOR);

            return attributeDescriptions.rewind();
        }
    }
    
    @Override
    protected VkVertexInputBindingDescription.Buffer GetVertexBindingDescription() {
        return Vertex.GetBindingDescription();
    }
    
    @Override
    protected VkVertexInputAttributeDescription.Buffer GetVertexAttributeDescriptions() {
        return Vertex.GetAttributeDescriptions();
    }     
    
    private static class VertexUniformBufferObject {
        private static final int BYTES = Matrix44f.BYTES;
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
    
    
    // ========================> Shaders <======================== \\
    
    @Override
    protected String GetVertexShaderName() { return "PassThru.vs"; }
    
    @Override
    protected String GetGeometryShaderName() { return "PassThruLine.gs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "PassThru.fs"; }   
    
    
    // ========================> Lifetime <======================== \\
    
    public CVKNewLineRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
        colourBlend = true;
        depthTest = true;
        depthWrite = true;     
        depthCompareOperation = VK_COMPARE_OP_ALWAYS;
        assemblyTopology = VK_PRIMITIVE_TOPOLOGY_LINE_LIST;       
        colourWriteMask = VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT;      
    }
        
    @Override
    public int Initialise() {
        // Do this here rather than ctor as the CVKDevice won't be initialised
        // during the ctor call.
        if (CVKDevice.AreVkLogicOpsSupported()) {
            logicOpEnable = true;
            logicOp = VK_LOGIC_OP_INVERT;
        }
        
        int ret = super.Initialise();
        if (VkFailed(ret)) { return ret; }       

        // Initialise push constants to identity mtx
        CreatePushConstants();
        
        ret = CreatePipelineLayout();
        if (VkFailed(ret)) { return ret; }                
               
        return VK_SUCCESS;
    }
    
    @Override
    public void Destroy() {
        DestroyCommandBuffers();
        DestroyVertexBuffer();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyPushConstants();
              
        CVKAssertNull(displayPipelines);
        CVKAssertNull(hPipelineLayout);
        CVKAssertNull(cvkVertexBuffer);
        CVKAssertNull(displayCommandBuffers);
        CVKAssertNull(pushConstants);
    }
    
    
    // ========================> Swap chain <======================== \\
    
    private int CreateSwapChainResources() {
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(cvkSwapChain);
        int ret = VK_SUCCESS;
                
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (swapChainImageCountChanged) {
            try (MemoryStack stack = stackPush()) {

                ret = CreateVertexBuffer(stack);
                if (VkFailed(ret)) { return ret; }   

                ret = CreateCommandBuffers();
                if (VkFailed(ret)) { return ret; }            

                displayPipelines = new ArrayList<>(cvkSwapChain.GetImageCount());
                ret = CreatePipelines(cvkSwapChain.GetRenderPassHandle(), displayPipelines);
                if (VkFailed(ret)) { return ret; }                                                       
            }      
        }
        
        swapChainResourcesDirty = false;
        swapChainImageCountChanged = false;
        
        return ret;
    }  
    
    @Override
    protected int DestroySwapChainResources(){
        cvkVisualProcessor.VerifyInRenderThread();
        int ret = VK_SUCCESS;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (displayPipelines != null && swapChainImageCountChanged) {        
            DestroyVertexBuffer();
            DestroyCommandBuffers();
            DestroyPipelines();
            DestroyCommandBuffers(); 

            CVKAssertNull(displayPipelines);
            CVKAssertNull(cvkVertexBuffer);
            CVKAssertNull(displayCommandBuffers);
         }
        
        return ret;
    }
      
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffer(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        
        // Allocate the vertex objects
        vertices[0] = new Vertex(ZERO_3F, NEW_LINE_COLOR);
        vertices[1] = new Vertex(ZERO_3F, NEW_LINE_COLOR);        
         
        // Staging buffer so our VB can be device local (most performant memory)
        final int size = vertices.length * Vertex.BYTES;
        cvkStagingBuffer = CVKBuffer.Create(size,
                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                            GetLogger(),
                                            "CVKNewLineRenderable.CreateVertexBuffer cvkStagingBuffer");
        
        // Create the actual VB which will be device local
        cvkVertexBuffer = CVKBuffer.Create(size,
                                           VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                           VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                           GetLogger(),
                                           "CVKNewLineRenderable.CreateVertexBuffers cvkStagingBuffer");
        cvkVertexBuffer.CopyFrom(cvkStagingBuffer);
       
        return UpdateVertexBuffer(stack);  
    }
    
    private int UpdateVertexBuffer(MemoryStack stack) {
        int ret = VK_SUCCESS;
        
        final Camera camera = cvkVisualProcessor.getDisplayCamera();
        NewLineModel updatedModel = modelQueue.peek();
        
        // Logic copied from NewLineRenderable.  Looks like it is purging the 
        // queue of any models with a different camera to the current one.
        while (updatedModel != null && updatedModel.getCamera() != camera) {
            modelQueue.remove();
            updatedModel = modelQueue.peek();
        }     
        
        // This looks like it is finding the last (most recent) model that matches
        // the current camera, adding it back to the queue after removing all other
        // model.
        if (updatedModel != null) {
            updatedModel = modelQueue.remove();
            NewLineModel nextModel = modelQueue.peek();
            while (nextModel != null && nextModel.getCamera() == camera) {
                updatedModel = modelQueue.remove();
                nextModel = modelQueue.peek();
            }
            modelQueue.addFirst(updatedModel);
        }
        model = updatedModel;
        
        if (model != null && !model.isClear()) {        
            // Update our vertex objects.  Note there was a weird bug when instead
            // of setting a vertex to a new object vertices[n].vertex.set() was
            // used.  The result would move the axes renderable.  I didn't have
            // time to investigate but there could be something hokey going with
            // memory on the Java heap?
            vertices[0] = new Vertex(model.getStartLocation(), NEW_LINE_COLOR);
            vertices[1] = new Vertex(model.getEndLocation(), NEW_LINE_COLOR);
            // BROKEN.  Hopefully someone more knowledgable about Java might be
            // able to spot the problem.
//            vertices[0].vertex.set(model.getStartLocation());
//            vertices[1].vertex.set(model.getEndLocation());

            // Update the staging buffer
            final int size = vertices.length * Vertex.BYTES;
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(CVKDevice.GetVkDevice(), cvkStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
            if (VkFailed(ret)) { return ret; }
            {
                Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
            }
            vkUnmapMemory(CVKDevice.GetVkDevice(), cvkStagingBuffer.GetMemoryBufferHandle());     

            // Update the vertex buffer
            cvkVertexBuffer.CopyFrom(cvkStagingBuffer);
        }
        
        return ret;
    }
            
    @Override
    public int GetVertexCount() { 
        if (model != null && !model.isClear()) {
            return NUMBER_OF_VERTICES;
        } else {
            return 0;
        }
    }    
    
    private void DestroyVertexBuffer() {
        if (null != cvkVertexBuffer) {
            cvkVertexBuffer.Destroy();
            cvkVertexBuffer = null;
        }
    }    
   
    
    // ========================> Push constants <======================== \\
    
    private int CreatePushConstants() {
        // Initialise push constants to identity mtx
        pushConstants = memAlloc(VertexUniformBufferObject.BYTES);
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                pushConstants.putFloat(IDENTITY_44F.get(iRow, iCol));
            }
        }
        pushConstants.flip();
         
        return VK_SUCCESS;
    }
    
    private void UpdatePushConstants(){
        CVKAssertNotNull(cvkSwapChain);
        
        vertexUBO.mvpMatrix.set(cvkVisualProcessor.getDisplayModelViewProjectionMatrix());        
        vertexUBO.CopyTo(pushConstants);
        pushConstants.flip();        
    }
    
    private void DestroyPushConstants() {
        if (pushConstants != null) {
            memFree(pushConstants);
            pushConstants = null;
        }
    }
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers() {       
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        displayCommandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY,
                                                              GetLogger(),
                                                              String.format("CVKNewLineRenderable %d", i));
            displayCommandBuffers.add(buffer);
        }
        
        GetLogger().info("Init Command Buffer - CVKNewLineRenderable");
        
        return ret;
    }
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {        
        return displayCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }     
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex) {
        CVKAssertNotNull(cvkSwapChain);
        cvkVisualProcessor.VerifyInRenderThread();
        int ret = VK_SUCCESS;
                    
        CVKCommandBuffer commandBuffer = displayCommandBuffers.get(imageIndex);
        CVKAssertNotNull(commandBuffer);
        CVKAssertNotNull(displayPipelines.get(imageIndex));

        ret = commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
                                                       inheritanceInfo);
        if (VkFailed(ret)) { return ret; }

        commandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        commandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());
        commandBuffer.BindGraphicsPipeline(displayPipelines.get(imageIndex));
        commandBuffer.BindVertexInput(cvkVertexBuffer.GetBufferHandle());
        commandBuffer.PushConstants(hPipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, pushConstants);

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }

        return ret;
    }
    
    private void DestroyCommandBuffers() {
        if (null != displayCommandBuffers && displayCommandBuffers.size() > 0) {
            displayCommandBuffers.forEach(el -> {el.Destroy();});
            displayCommandBuffers.clear();
            displayCommandBuffers = null;
        }       
    }
    
    
    // ========================> Descriptors <======================== \\  
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {
        // No descriptor sets required because this class uses push constants instead of descriptor bound uniform buffers.
    }
  
    @Override
    public int DestroyDescriptorPoolResources() {         
        return VK_SUCCESS; 
    }       
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssertNotNull(CVKDevice.GetVkDevice());
               
        int ret;       
        try (MemoryStack stack = stackPush()) {      
            VkPushConstantRange.Buffer pushConstantRange;
            pushConstantRange = VkPushConstantRange.callocStack(1, stack);
            pushConstantRange.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            pushConstantRange.size(VertexUniformBufferObject.BYTES);
            pushConstantRange.offset(0);                     
            
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
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
    
    
    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() {
        return  modelQueue.size() > 0;
    }

    @Override
    public int DisplayUpdate() { 
        cvkVisualProcessor.VerifyInRenderThread();
        
        int ret = VK_SUCCESS;    
        
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }    
        try (MemoryStack stack = stackPush()) {
            ret = UpdateVertexBuffer(stack);
            if (VkFailed(ret)) { return ret; }
        }    

        UpdatePushConstants(); 
        return ret;
    }
    
    
    // ========================> Tasks <======================== \\
    
    public void queueModel(final NewLineModel model) {
        modelQueue.add(model);
        cvkVisualProcessor.RequestRedraw();
    }
}
