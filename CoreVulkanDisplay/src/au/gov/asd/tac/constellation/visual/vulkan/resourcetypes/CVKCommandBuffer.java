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
package au.gov.asd.tac.constellation.visual.vulkan.resourcetypes;


import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKMissingEnums;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_LABEL_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkCmdBeginDebugUtilsLabelEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkCmdEndDebugUtilsLabelEXT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkResetCommandBuffer;
import org.lwjgl.vulkan.VkDebugUtilsLabelEXT;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier;


public class CVKCommandBuffer {
    private VkCommandBuffer vkCommandBuffer = null;
    private CVKGraphLogger cvkGraphLogger = null;
    private String DEBUGNAME;
    private VkDebugUtilsLabelEXT vkDebugLabel = null;
    private CVKGraphLogger GetLogger() { return cvkGraphLogger != null ? cvkGraphLogger : CVKGraphLogger.GetStaticLogger(); }

    private CVKCommandBuffer() {}


    public VkCommandBuffer GetVKCommandBuffer(){ return vkCommandBuffer; }

    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {	
        //TODO remove the if, only here for CVK_DEBUGGING, its checked in Destroy()
        if (vkCommandBuffer != null) {
            Destroy();
        }
        super.finalize();
    }

    public void Destroy(){        
        if (vkCommandBuffer != null) {
            if (CVK_DEBUGGING) {
                final CVKGraphLogger logger = GetLogger();
                if (logger.isLoggable(CVK_ALLOCATION_LOG_LEVEL)) {
                    --CVK_VKALLOCATIONS;
                    logger.log(CVK_ALLOCATION_LOG_LEVEL, "CVK_VKALLOCATIONS(%d-) vkFreeCommandBuffers for %s 0x%016X", 
                            CVK_VKALLOCATIONS, DEBUGNAME, vkCommandBuffer.address());    
                }
            }             
            
            vkFreeCommandBuffers(CVKDevice.GetVkDevice(), CVKDevice.GetCommandPoolHandle(), vkCommandBuffer);
            vkCommandBuffer = null;
        }
    }
    
    public int Begin(int flags) {	  
        int ret;            
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferBeginInfo vkBeginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            vkBeginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            vkBeginInfo.pNext(0);
            vkBeginInfo.flags(flags);
            ret = vkBeginCommandBuffer(vkCommandBuffer, vkBeginInfo);
        }
        return ret;
    }
    
    public int EndAndSubmit() {
        int ret;
        try (MemoryStack stack = stackPush()) {
            ret = vkEndCommandBuffer(vkCommandBuffer);
            if (VkFailed(ret)) { return ret; }

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.callocStack(1, stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(vkCommandBuffer));

            ret = vkQueueSubmit(CVKDevice.GetVkQueue(), submitInfo, VK_NULL_HANDLE);
            if (VkFailed(ret)) { return ret; }
            ret = vkQueueWaitIdle(CVKDevice.GetVkQueue());
        }  
        return ret;
    }
    
    public int BeginRecordSecondary(int flags, VkCommandBufferInheritanceInfo inheritanceInfo) {
	int ret;    
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.pNext(0);
            beginInfo.flags(flags);
            beginInfo.pInheritanceInfo(inheritanceInfo);

            ret = vkBeginCommandBuffer(vkCommandBuffer, beginInfo);
            if (VkFailed(ret)) { return ret; }
            
            if (CVK_DEBUGGING && vkDebugLabel != null) {
                vkCmdBeginDebugUtilsLabelEXT(vkCommandBuffer, vkDebugLabel);
            }
        }
        
        return ret;
    }
    
    public int BeginRecordSecondary(int flags, long framebuffer, long renderPass, int subpass) {
        int ret;      
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack);
            inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
            inheritanceInfo.pNext(0);
            inheritanceInfo.framebuffer(framebuffer);
            inheritanceInfo.renderPass(renderPass);
            inheritanceInfo.subpass(subpass);
            inheritanceInfo.occlusionQueryEnable(false);
            inheritanceInfo.queryFlags(0);
            inheritanceInfo.pipelineStatistics(0);
            
            ret = BeginRecordSecondary(flags, inheritanceInfo);  
        }
        return ret;
    }
	
    public int FinishRecord() {
        int ret;
        
        if (CVK_DEBUGGING && vkDebugLabel != null) {
            vkCmdEndDebugUtilsLabelEXT(vkCommandBuffer);
        }

        ret = vkEndCommandBuffer(vkCommandBuffer);
        if (VkFailed(ret)) { return ret; }

        return ret;
    }
	
    public void BeginRenderPass(long renderPass, long frameBuffer,
                    int width, int height, int colorAttachmentCount, int depthAttachment,
                    int contentsFlag) {

        VkClearValue.Buffer clearValues = VkClearValue.calloc(colorAttachmentCount + depthAttachment);

        for (int i=0; i<colorAttachmentCount; i++) {
            clearValues.put(CVKUtils.getClearValueColor(new Vector3f(0,0,0)));
        }
        if (depthAttachment == 1) {
            clearValues.put(CVKUtils.getClearValueDepth());
        }
        clearValues.flip();

        BeginRenderPass(renderPass, frameBuffer, width, height, contentsFlag, clearValues);

        clearValues.free();
    }
	
    private void BeginRenderPass(long renderPass, long frameBuffer,
                    int width, int height, int flags, VkClearValue.Buffer clearValues) {

        try (MemoryStack stack = stackPush()) {
            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassBeginInfo.pNext(0);
            renderPassBeginInfo.renderPass(renderPass);
            renderPassBeginInfo.pClearValues(clearValues);
            renderPassBeginInfo.framebuffer(frameBuffer);

            VkRect2D renderArea = renderPassBeginInfo.renderArea();
            renderArea.offset().set(0, 0);
            renderArea.extent().set(width, height);

            vkCmdBeginRenderPass(vkCommandBuffer, renderPassBeginInfo, flags);
        }
    }
    
    public void EndRenderPass(){
        vkCmdEndRenderPass(vkCommandBuffer);
    }
	
    public void BindComputePipeline(long pipeline) {
        vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
    }
	
    public void BindGraphicsPipeline(long pipeline) {
        vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
    }
	
    public void SetViewPort(int width, int height) {
        try (MemoryStack stack = stackPush()) {
            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(width);
            viewport.height(height);
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            vkCmdSetViewport(vkCommandBuffer, 0, viewport);
        }
    }
	
    public void SetScissor(VkExtent2D extent) {
        try (MemoryStack stack = stackPush()) {
            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(extent);

            vkCmdSetScissor(vkCommandBuffer, 0, scissor);
        }
    }
	
    public void PushConstants(long pipelineLayout, int stageFlags, int offset, ByteBuffer data) {

        vkCmdPushConstants(vkCommandBuffer, // The buffer to push the matrix to
                        pipelineLayout,     // The pipeline layout
                        stageFlags,         // Flags
                        offset,             // Offset
                        data);              // Push constant data
    }
	
    public void BindVertexInput(long vertexBuffer, long indexBuffer) {
        
        try (MemoryStack stack = stackPush()) {
            LongBuffer pVertexBuffers = stack.longs(vertexBuffer);
            LongBuffer offsets = stack.longs(0);

            vkCmdBindVertexBuffers(vkCommandBuffer, 0, pVertexBuffers, offsets);
            vkCmdBindIndexBuffer(vkCommandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT32);
        }
    }
	
    public void BindVertexInput(long vertexBuffer) {
        
        try (MemoryStack stack = stackPush()) {
            
            LongBuffer pVertexBuffers = stack.longs(vertexBuffer);
            LongBuffer offsets = stack.longs(0);
            
            // Bind verts
            vkCmdBindVertexBuffers(vkCommandBuffer, 0, pVertexBuffers, offsets);
            
        }
    }
	
    public void BindComputeDescriptorSets(long pipelinyLayout, long descriptorSets) {

        BindDescriptorSets(pipelinyLayout, descriptorSets,
                            VK_PIPELINE_BIND_POINT_COMPUTE);
    }
	
    public void BindGraphicsDescriptorSets(long pipelinyLayout, long descriptorSets){

        BindDescriptorSets(pipelinyLayout, descriptorSets,
                            VK_PIPELINE_BIND_POINT_GRAPHICS);
    }

    private void BindDescriptorSets(long pipelinyLayout, long descriptorSets, int pipelineBindPoint){
        
        try (MemoryStack stack = stackPush()) {
            vkCmdBindDescriptorSets(vkCommandBuffer, pipelineBindPoint,
                            pipelinyLayout, 0, stack.longs(descriptorSets), null);
        }
    }
    
    public void DrawIndexed(int indexCount) {
        vkCmdDrawIndexed(vkCommandBuffer, indexCount, 1, 0, 0, 0);
    }

    public void Draw(int vertexCount) {
        
        vkCmdDraw(vkCommandBuffer,
                    vertexCount,    // number of verts
                    1,              // no instancing, but we must draw at least 1 point
                    0,              // first vert index
                    0);             // first instance index (N/A)  
    }
	
    public void PipelineImageMemoryBarrier(long image, int oldLayout, int newLayout,
			int srcAccessMask, int dstAccessMask, int srcStageMask, int dstStageMask,
			int baseMipLevel, int mipLevelCount) {
       
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.oldLayout(oldLayout);
        barrier.newLayout(newLayout);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.image(image);
        barrier.srcAccessMask(srcAccessMask);
        barrier.dstAccessMask(dstAccessMask);

        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(baseMipLevel);
        barrier.subresourceRange().levelCount(mipLevelCount);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);

        vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
                        VK_DEPENDENCY_BY_REGION_BIT, null, null, barrier);

    }
	
    public void PipelineImageMemoryBarrier(long image, int srcStageMask, int dstStageMask,
                    VkImageMemoryBarrier.Buffer barrier) {

        vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
                        VK_DEPENDENCY_BY_REGION_BIT, null, null, barrier);
    }
	
    public void PipelineMemoryBarrier(int srcAccessMask, int dstAccessMask,
                    int srcStageMask, int dstStageMask) {

        VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc(1);
        barrier.sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER);
        barrier.srcAccessMask(srcAccessMask);
        barrier.dstAccessMask(dstAccessMask);

        vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
                        VK_DEPENDENCY_BY_REGION_BIT, barrier, null, null);
    }
	
    public void PipelineBarrier(int srcStageMask, int dstStageMask) {

        vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
                            VK_DEPENDENCY_BY_REGION_BIT, null, null, null);
    }
	
    public void Reset() {

        vkResetCommandBuffer(vkCommandBuffer, VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
    }
	
    public static CVKCommandBuffer Create(int level, CVKGraphLogger graphLogger, final String debugName) {
        CVKAssertNotNull(CVKDevice.GetVkDevice());

        int ret;
        CVKCommandBuffer cvkCommandBuffer = new CVKCommandBuffer();
        cvkCommandBuffer.cvkGraphLogger   = graphLogger;
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo vkAllocateInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            vkAllocateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            vkAllocateInfo.commandPool(CVKDevice.GetCommandPoolHandle());
            vkAllocateInfo.level(level);
            vkAllocateInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            ret = vkAllocateCommandBuffers(CVKDevice.GetVkDevice(), vkAllocateInfo, pCommandBuffer);
            checkVKret(ret);

            cvkCommandBuffer.vkCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), CVKDevice.GetVkDevice());
            
            if (CVK_DEBUGGING) {
                cvkCommandBuffer.DEBUGNAME = debugName;
                final CVKGraphLogger logger = cvkCommandBuffer.GetLogger();
                if (logger.isLoggable(CVK_ALLOCATION_LOG_LEVEL)) {                
                    ++CVK_VKALLOCATIONS;
                    logger.log(CVK_ALLOCATION_LOG_LEVEL, "CVK_VKALLOCATIONS(%d+) vkAllocateCommandBuffers for %s 0x%016X", 
                            CVK_VKALLOCATIONS, cvkCommandBuffer.DEBUGNAME, cvkCommandBuffer.vkCommandBuffer.address());   
                }
                ret = CVKDevice.GetInstance().SetDebugName(cvkCommandBuffer.vkCommandBuffer.address(),
                                                           CVKMissingEnums.VkObjectType.VK_OBJECT_TYPE_COMMAND_BUFFER.Value(),
                                                           debugName);
                if (VkFailed(ret)) {
                    logger.warning("Failed to set debug object name for command buffer '%s'", debugName);
                }
                
                if (CVKDevice.IsVkCmdBeginDebugUtilsLabelEXTAvailable()) {
                    cvkCommandBuffer.vkDebugLabel = VkDebugUtilsLabelEXT.malloc();
                    cvkCommandBuffer.vkDebugLabel.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_LABEL_EXT);
                    cvkCommandBuffer.vkDebugLabel.pNext(VK_NULL_HANDLE);
                    cvkCommandBuffer.vkDebugLabel.pLabelName(MemoryUtil.memASCII(debugName));
                    cvkCommandBuffer.vkDebugLabel.color(0, 1.0f);
                    cvkCommandBuffer.vkDebugLabel.color(1, 1.0f);
                    cvkCommandBuffer.vkDebugLabel.color(2, 1.0f);
                    cvkCommandBuffer.vkDebugLabel.color(3, 1.0f);
                }
            }              
        }
        return cvkCommandBuffer;
    }	
}
