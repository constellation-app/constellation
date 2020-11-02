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

import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.*;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;


public class CVKPointsRenderable extends CVKRenderable {    
    // The UBO staging buffers are a known size so created outside user events
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private CVKBuffer cvkVertexUBStagingBuffer = null;    
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;    
    private CVKBuffer cvkVertexBuffer = null;
    
    // Swapchain dependent resources
    private List<CVKCommandBuffer> displayCommandBuffers = null;    
    
    // The vertex staging buffer are used by both the event
    // thread and rendering thread so must be synchronised.
    private ReentrantLock vertexStagingBufferLock = new ReentrantLock();            
    
    private ByteBuffer pushConstants = null;
     
        
    // ========================> Classes <======================== \\         
        
    private static class Vertex {
        private static final int BYTES = Vector3f.BYTES;
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

        private static VkVertexInputBindingDescription.Buffer GetBindingDescription() {
            VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.callocStack(1);
            bindingDescription.binding(0);
            bindingDescription.stride(Vertex.BYTES);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
            return bindingDescription;
        }

        private static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {
            VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(1);
            VkVertexInputAttributeDescription vertexDescription = attributeDescriptions.get(0);
            vertexDescription.binding(0);
            vertexDescription.location(0);
            vertexDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            vertexDescription.offset(0);
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
    protected String GetVertexShaderName() { return "PassThruPoint.vs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "PassThruPoint.fs"; }            
    
    
    // ========================> Lifetime <======================== \\
    
    public CVKPointsRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
    }          
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(VertexUniformBufferObject.BYTES,
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                    GetLogger(),
                                                    "CVKPointRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer");     
    }
    
    @Override
    public int Initialise() {
        int ret = super.Initialise();  
        if (VkFailed(ret)) { return ret; }       
        
        // Initialise push constants to identity mtx
        CreatePushConstants();                            
        
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
              
        CVKAssert(displayPipelines == null);
        CVKAssert(hPipelineLayout == VK_NULL_HANDLE);
        CVKAssert(cvkVertexBuffer == null);
        CVKAssert(displayCommandBuffers == null);     
        CVKAssert(pushConstants == null);            
    }
    
       
    // ========================> Swap chain <======================== \\
       
    @Override
    protected int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (displayPipelines != null && swapChainImageCountChanged) {  
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
            int vertexBufferSizeBytes = Vertex.BYTES * vertexCount;
            cvkVertexBuffer = CVKBuffer.Create(vertexBufferSizeBytes,
                                               VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                               GetLogger(),
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
        pushConstants = memAlloc(VertexUniformBufferObject.BYTES);
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
        
        displayCommandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKPointRenderable %d", i));
            displayCommandBuffers.add(buffer);
        }
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }   
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {
        return displayCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }       
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        CVKAssertNotNull(cvkSwapChain);
        cvkVisualProcessor.VerifyInRenderThread();
        int ret;
                     
        CVKCommandBuffer commandBuffer = displayCommandBuffers.get(imageIndex);
        CVKAssert(commandBuffer != null);
        CVKAssert(displayPipelines.get(imageIndex) != null);

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
        if (null != displayCommandBuffers) {
            displayCommandBuffers.forEach(el -> {el.Destroy();});
            displayCommandBuffers.clear();
            displayCommandBuffers = null;
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
        CVKAssert(CVKDevice.GetVkDevice() != null);
               
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
            CVKAssert(hPipelineLayout != VK_NULL_HANDLE);                
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
            displayPipelines = new ArrayList<>(cvkSwapChain.GetImageCount());
            ret = CreatePipelines(cvkSwapChain.GetRenderPassHandle(), displayPipelines);
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
                int vertexBufferSizeBytes = Vertex.BYTES * newVertexCount;
                cvkVertexStagingBuffer = CVKBuffer.Create(vertexBufferSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                          GetLogger(),
                                                          "CVKPointRenderable.TaskCreateIcons cvkVertexStagingBuffer");               
                
                ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartMemoryMap(0, vertexBufferSizeBytes);
                for (int pos = 0; pos < newVertexCount; pos++) {
                    final int offset = Vertex.BYTES * pos;
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
                final int offset = Vertex.BYTES * pos;
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
