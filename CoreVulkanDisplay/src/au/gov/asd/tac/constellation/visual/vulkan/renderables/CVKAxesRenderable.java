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

import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.vulkan.VkPushConstantRange;


public class CVKAxesRenderable extends CVKRenderable {
    // FROM AxesRenderable...
    private static final float LEN = 0.5f;
    private static final float HEAD = 0.05f;
    private static final int AXES_OFFSET = 50;
    private static final Vector4f XCOLOR = new Vector4f(1, 0.5f, 0.5f, 0.75f);
    private static final Vector4f YCOLOR = new Vector4f(0.5f, 1, 0.5f, 0.75f);
    private static final Vector4f ZCOLOR = new Vector4f(0, 0.5f, 1, 0.75f);


    // All the verts are manually calculated for the Axes in CreateVertexBuffer():
    // - (3 x 2) = X, Y, Z lines for Axes
    // - (4, 4, 4) =  Arrows at the end of the Axes
    // - (4, 6, 6) = X, Y, Z labels
    private static final int NUMBER_OF_VERTICES = 3 * 2 + 4 + 4 + 4 + 4 + 6 + 6;
    
    private final Vector3f topRightCorner = new Vector3f();
    private float pScale = 0;
    
    private final Vertex[] vertices = new Vertex[NUMBER_OF_VERTICES];
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private CVKBuffer cvkVertexBuffer = null;
    private List<CVKCommandBuffer> displayCommandBuffers = null;
    private boolean needsDisplayUpdate = false;
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
    
    public CVKAxesRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
        colourBlend = false;
        depthTest = false;
        depthWrite = false;     
        assemblyTopology = VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
    }
        
    @Override
    public int Initialise() {
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
        } else {
            // This is the resize path, image count is unchanged.       
            UpdatePushConstants();
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
        int ret = VK_SUCCESS;
        
        // Size to upper limit, we don't have to draw each one.
        int size = vertices.length * Vertex.BYTES;
        
        // Converted from AxesRenderable.java. Keeping comments for reference.
        int i =  0;
        // x axis
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);      
        vertices[i++] = new Vertex(new Vector3f(LEN,0f,0f), XCOLOR);
        
        // arrow
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN - HEAD, HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN - HEAD, HEAD, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN, 0f, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN, 0f, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN - HEAD, -HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN - HEAD, -HEAD, 0f), XCOLOR);

        // X
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, HEAD, HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, -HEAD, -HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, HEAD, -HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, -HEAD, HEAD), XCOLOR);

        // y axis
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // arrow
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN - HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN - HEAD, HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN - HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN - HEAD, -HEAD), YCOLOR);
        // Y
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, LEN + HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, LEN + HEAD, -HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, LEN + HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, LEN + HEAD, -HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, HEAD), YCOLOR);

        // z axis
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // arrow
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, 0, LEN - HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, 0f, LEN - HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, 0, LEN - HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, 0f, LEN - HEAD), ZCOLOR);
        // Z
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        
         
        // Staging buffer so our VB can be device local (most performant memory)
        CVKBuffer cvkStagingBuffer = CVKBuffer.Create(size,
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      GetLogger(),
                                                      "CVKAxesRenderable.CreateVertexBuffer cvkStagingBuffer");

        PointerBuffer data = stack.mallocPointer(1);
        vkMapMemory(CVKDevice.GetVkDevice(), cvkStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
        if (VkFailed(ret)) { return ret; }
        {
            Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
        }
        vkUnmapMemory(CVKDevice.GetVkDevice(), cvkStagingBuffer.GetMemoryBufferHandle());
        
        // Create and stage into the actual VB which will be device local
        cvkVertexBuffer = CVKBuffer.Create(size,
                                           VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                           VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                           GetLogger(),
                                           "CVKAxesRenderable.CreateVertexBuffers cvkStagingBuffer");
        cvkVertexBuffer.CopyFrom(cvkStagingBuffer);
        
        // Cleanup
        cvkStagingBuffer.Destroy();
        
        return ret;  
    }
    
    @Override
    public int GetVertexCount() { return NUMBER_OF_VERTICES; }    
    
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
            
        final int[] viewport = cvkSwapChain.GetViewport();             
        final int dx = cvkSwapChain.GetWidth() / 2 - AXES_OFFSET;
        final int dy = cvkSwapChain.GetHeight() / 2 - AXES_OFFSET;
        pScale = CalculateProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, topRightCorner);
        
        // LIFTED FROM AxesRenerable.display(...)
        // Extract the rotation matrix from the mvp matrix.
        final Matrix44f rotationMatrix = new Matrix44f();
        cvkVisualProcessor.getDisplayModelViewProjectionMatrix().getRotationMatrix(rotationMatrix);

        // Scale down to size.
        final Matrix44f scalingMatrix = new Matrix44f();
        scalingMatrix.makeScalingMatrix(pScale, pScale, 0);
        final Matrix44f srMatrix = new Matrix44f();
        srMatrix.multiply(scalingMatrix, rotationMatrix);

        // Translate to the top right corner.
        final Matrix44f translationMatrix = new Matrix44f();
        translationMatrix.makeTranslationMatrix(topRightCorner.getX(), 
                                                topRightCorner.getY(), 
                                                topRightCorner.getZ());       
        
        // Calculate the model-view-projection matrix
        vertexUBO.mvpMatrix.multiply(translationMatrix, srMatrix);
        
        // Update the push constants data
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
                                                              String.format("CVKAxesRenderable %d", i));
            displayCommandBuffers.add(buffer);
        }
        
        GetLogger().info("Init Command Buffer - CVKAxesRenderable");
        
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
        int ret;
                    
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
        // No descriptor sets required because axes use push constants instead of descriptor bound uniform buffers.
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
        return needsDisplayUpdate | descriptorPoolResourcesDirty | swapChainResourcesDirty;
    }

    @Override
    public int DisplayUpdate() { 
        cvkVisualProcessor.VerifyInRenderThread();
        
        int ret = VK_SUCCESS;    
        
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }           

        UpdatePushConstants();
 
        needsDisplayUpdate = false;
        return ret;
    }
    
    
    // ========================> Tasks <======================== \\
    
    public CVKRenderUpdateTask TaskUpdateCamera() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//

        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            cvkVisualProcessor.VerifyInRenderThread();                      
            needsDisplayUpdate = true;
        };
    }  


    // ========================> Helpers <======================== \\ 
         
    private float CalculateProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        final Vector4f projectedOrigin = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, projectedOrigin);
        final Vector4f projectedIdentity = new Vector4f();
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, projectedIdentity);
        float yScale = projectedIdentity.a[1] - projectedOrigin.a[1];
        
        // Vulkan flips the Y compared to GL, this has no effect in world space but when we are premultiplying
        // by the projection matrix we enter clip space, and here we do need to flip Y.
        yScale = -yScale;

        return 25.0f / yScale;
    } 
}
