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
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
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
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryBarrier;


public class CVKCommandBuffer {
    private VkCommandBuffer vkCommandBuffer = null;
    private CVKDevice cvkDevice = null;
    
    //TODO: REMOVE THIS
    public String DEBUGNAME = "";


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
            cvkDevice.VerifyInRenderThread();
            
//            if (CVK_DEBUGGING) {
//                --CVK_VKALLOCATIONS;
//                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS(%d-) vkFreeCommandBuffers for %s 0x%016X", 
//                        CVK_VKALLOCATIONS, DEBUGNAME, vkCommandBuffer.address());                
//            }             
            
            vkFreeCommandBuffers(cvkDevice.GetDevice(), cvkDevice.GetCommandPoolHandle(), vkCommandBuffer);
            vkCommandBuffer = null;
        }
    }
    
    public int Begin(int flags) {	
        cvkDevice.VerifyInRenderThread();
        
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
        try(MemoryStack stack = stackPush()) {
            ret = vkEndCommandBuffer(vkCommandBuffer);
            if (VkFailed(ret)) { return ret; }

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.callocStack(1, stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(vkCommandBuffer));

            ret = vkQueueSubmit(cvkDevice.GetQueue(), submitInfo, VK_NULL_HANDLE);
            if (VkFailed(ret)) { return ret; }
            ret = vkQueueWaitIdle(cvkDevice.GetQueue());
        }  
        return ret;
    }
    
    public void BeginRecordSecondary(int flags, VkCommandBufferInheritanceInfo inheritanceInfo) {
	VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc();
        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
        beginInfo.pNext(0);
        beginInfo.flags(flags);
        beginInfo.pInheritanceInfo(inheritanceInfo);

        int err = vkBeginCommandBuffer(vkCommandBuffer, beginInfo);

        if (err != VK_SUCCESS) {
                throw new AssertionError("Failed to begin record command buffer: ");
        }

        beginInfo.free();
    }
    
    public void BeginRecordSecondary(int flags, long framebuffer, long renderPass, int subpass) {

        VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc();
        inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
        inheritanceInfo.pNext(0);
        inheritanceInfo.framebuffer(framebuffer);
        inheritanceInfo.renderPass(renderPass);
        inheritanceInfo.subpass(subpass);
        inheritanceInfo.occlusionQueryEnable(false);
        inheritanceInfo.queryFlags(0);
        inheritanceInfo.pipelineStatistics(0);

        BeginRecordSecondary(flags, inheritanceInfo);
        
    }
	
    public void FinishRecord(){

        int err = vkEndCommandBuffer(vkCommandBuffer);

        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to finish record command buffer: ");
        }
    }
	
	public void beginRenderPassCmd(long renderPass, long frameBuffer,
			int width, int height, int colorAttachmentCount, int depthAttachment,
			int contentsFlag){
		
		VkClearValue.Buffer clearValues = VkClearValue.calloc(
				colorAttachmentCount + depthAttachment);
		
		for (int i=0; i<colorAttachmentCount; i++){
			clearValues.put(CVKUtils.getClearValueColor(new Vector3f(0,0,0)));
		}
		if (depthAttachment == 1){
			clearValues.put(CVKUtils.getClearValueDepth());
		}
		clearValues.flip();
		
		beginRenderPassCmd(renderPass, frameBuffer, width, height,
				contentsFlag, clearValues);
		
		clearValues.free();
	}
//	
//	public void beginRenderPassCmd(long renderPass, long frameBuffer,
//			int width, int height, int colorAttachmentCount, int depthAttachment,
//			int contentsFlag, Vec3f clearColor){
//		
//		VkClearValue.Buffer clearValues = VkClearValue.calloc(
//				colorAttachmentCount + depthAttachment);
//		
//		for (int i=0; i<colorAttachmentCount; i++){
//			clearValues.put(CVKUtils.getClearValueColor(clearColor));
//		}
//		if (depthAttachment == 1){
//			clearValues.put(CVKUtils.getClearValueDepth());
//		}
//		clearValues.flip();
//		
//		beginRenderPassCmd(renderPass, frameBuffer, width, height,
//				contentsFlag, clearValues);
//		
//		clearValues.free();
//	}
//	
	private void beginRenderPassCmd(long renderPass, long frameBuffer,
			int width, int height, int flags, VkClearValue.Buffer clearValues){
		
            VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc();
            renderPassBeginInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassBeginInfo.pNext(0);
            renderPassBeginInfo.renderPass(renderPass);
            renderPassBeginInfo.pClearValues(clearValues);
            renderPassBeginInfo.framebuffer(frameBuffer);

            VkRect2D renderArea = renderPassBeginInfo.renderArea();
            renderArea.offset().set(0, 0);
            renderArea.extent().set(width, height);

            vkCmdBeginRenderPass(vkCommandBuffer, renderPassBeginInfo, flags);

            renderPassBeginInfo.free();
	}
	
	public void endRenderPassCmd(){
		
	    vkCmdEndRenderPass(vkCommandBuffer);
	}
	
	public void BindComputePipelineCmd(long pipeline){
		
	    vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
	}
	
	public void BindGraphicsPipelineCmd(long pipeline){
		
	    vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
	}
	
	public void viewPortCmd(int width, int height, MemoryStack stack){
		
	    VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(width);
            viewport.height(height);
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);
            
            vkCmdSetViewport(vkCommandBuffer, 0, viewport);
	}
	
	public void scissorCmd(VkExtent2D extent, MemoryStack stack){
		
	    VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(extent);

            vkCmdSetScissor(vkCommandBuffer, 0, scissor);
	}
	
	public void pushConstantsCmd(long pipelineLayout, int stageFlags, ByteBuffer data){
		
		vkCmdPushConstants(vkCommandBuffer, // The buffer to push the matrix to
				pipelineLayout,     // The pipeline layout
				stageFlags,         // Flags
				0,                  // Offset
				data);              // Push constant data
	}
//	
//	public void bindVertexInputCmd(long vertexBuffer, long indexBuffer){
//		
//		LongBuffer offsets = memAllocLong(1);
//		offsets.put(0, 0L);
//		LongBuffer pVertexBuffers = memAllocLong(1);
//		pVertexBuffers.put(0, vertexBuffer);
//		
//		vkCmdBindVertexBuffers(vkCommandBuffer, 0, pVertexBuffers, offsets);
//		vkCmdBindIndexBuffer(vkCommandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT32);
//		
//		memFree(pVertexBuffers);
//		memFree(offsets);
//	}
//	
	public void bindVertexInputCmd(long vertexBuffer){
		
		LongBuffer offsets = memAllocLong(1);
		offsets.put(0, 0L);
		LongBuffer pVertexBuffers = memAllocLong(1);
		pVertexBuffers.put(0, vertexBuffer);
		
		vkCmdBindVertexBuffers(vkCommandBuffer, 0, pVertexBuffers, offsets);
		
		memFree(pVertexBuffers);
		memFree(offsets);
	}
	
	public void bindComputeDescriptorSetsCmd(long pipelinyLayout, long[] descriptorSets){

		bindDescriptorSetsCmd(pipelinyLayout, descriptorSets,
				VK_PIPELINE_BIND_POINT_COMPUTE);
	}
//	
//	public void bindGraphicsDescriptorSetsCmd(long pipelinyLayout, long[] descriptorSets){
//		
//		bindDescriptorSetsCmd(pipelinyLayout, descriptorSets,
//				VK_PIPELINE_BIND_POINT_GRAPHICS);
//	}
//	
	private void bindDescriptorSetsCmd(long pipelinyLayout, long[] descriptorSets,
			int pipelineBindPoint){
		
		vkCmdBindDescriptorSets(vkCommandBuffer, pipelineBindPoint,
				pipelinyLayout, 0, descriptorSets, null);
	}
	
//	public void clearColorImageCmd(long image, int imageLayout){
//		
//		VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc()
//				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
//				.baseMipLevel(0)
//				.levelCount(1)
//				.baseArrayLayer(0)
//				.layerCount(1);
//		
//		vkCmdClearColorImage(vkCommandBuffer, image, imageLayout,
//				CVKUtils.getClearColorValue(), subresourceRange);
//	}
//	
//	public void drawIndexedCmd(int indexCount){
//		
//		vkCmdDrawIndexed(vkCommandBuffer, indexCount, 1, 0, 0, 0);
//	}
//	
//	public void drawCmd(int vertexCount){
//		
//		vkCmdDraw(vkCommandBuffer, vertexCount, 1, 0, 0);
//	}
//	
//	public void dispatchCmd(int groupCountX, int groupCountY, int groupCountZ){
//		
//		vkCmdDispatch(vkCommandBuffer, groupCountX, groupCountY, groupCountZ);
//	}
//	
//	public void copyBufferCmd(long srcBuffer, long dstBuffer,
//								    long srcOffset, long dstOffset,
//								    long size){
//		
//		VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1)
//					.srcOffset(srcOffset)
//					.dstOffset(dstOffset)
//					.size(size);
//		
//		vkCmdCopyBuffer(vkCommandBuffer, srcBuffer, dstBuffer, copyRegion);
//	}
//	
//	public void copyBufferToImageCmd(long srcBuffer, long dstImage, int width, int height, int depth){
//		
//		VkBufferImageCopy.Buffer copyRegion = VkBufferImageCopy.calloc(1)
//					.bufferOffset(0)
//					.bufferRowLength(0)
//					.bufferImageHeight(0);
//		
//		VkImageSubresourceLayers subresource = VkImageSubresourceLayers.calloc()
//					.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
//					.mipLevel(0)
//					.baseArrayLayer(0)
//					.layerCount(1);
//		
//		VkExtent3D extent = VkExtent3D.calloc()
//					.width(width)
//					.height(height)
//					.depth(depth);
//		
//		VkOffset3D offset = VkOffset3D.calloc()
//					.x(0)
//					.y(0)
//					.z(0);
//		
//		copyRegion.imageSubresource(subresource);
//		copyRegion.imageExtent(extent);
//		copyRegion.imageOffset(offset);
//	
//		vkCmdCopyBufferToImage(vkCommandBuffer, srcBuffer, dstImage,
//				VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, copyRegion);
//	}
//	
	public void pipelineImageMemoryBarrierCmd(long image, int oldLayout, int newLayout,
			int srcAccessMask, int dstAccessMask, int srcStageMask, int dstStageMask,
			int baseMipLevel, int mipLevelCount){
		
            // TODO change to callocStack
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1);
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

            barrier.free();
	}
	
	public void pipelineImageMemoryBarrierCmd(long image, int srcStageMask, int dstStageMask,
			VkImageMemoryBarrier.Buffer barrier){
	
            vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
                            VK_DEPENDENCY_BY_REGION_BIT, null, null, barrier);
	}
	
	public void pipelineMemoryBarrierCmd(int srcAccessMask, int dstAccessMask,
			int srcStageMask, int dstStageMask){
		
            VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc(1);
            barrier.sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER);
            barrier.srcAccessMask(srcAccessMask);
            barrier.dstAccessMask(dstAccessMask);

            vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
                            VK_DEPENDENCY_BY_REGION_BIT, barrier, null, null);
	}
	
	public void pipelineBarrierCmd(int srcStageMask, int dstStageMask){
		
	    vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
				VK_DEPENDENCY_BY_REGION_BIT, null, null, null);
	}
//	
//	public void recordSecondaryCmdBuffers(PointerBuffer secondaryCmdBuffers){
//		
//		vkCmdExecuteCommands(vkCommandBuffer, secondaryCmdBuffers);
//	}
//	
//	public void reset(){
//		
//		vkResetCommandBuffer(vkCommandBuffer, VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
//	}
//	
    public static CVKCommandBuffer Create(CVKDevice cvkDevice, int level, final String debugName) {
        assert(cvkDevice != null);
        assert(cvkDevice.GetDevice() != null);

        int ret;
        CVKCommandBuffer cvkCommandBuffer = new CVKCommandBuffer();
        cvkCommandBuffer.cvkDevice = cvkDevice;
        try (MemoryStack stack = stackPush()) {
            VkCommandBufferAllocateInfo vkAllocateInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            vkAllocateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            vkAllocateInfo.commandPool(cvkDevice.GetCommandPoolHandle());
            vkAllocateInfo.level(level);
            vkAllocateInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            ret = vkAllocateCommandBuffers(cvkDevice.GetDevice(), vkAllocateInfo, pCommandBuffer);
            checkVKret(ret);

            cvkCommandBuffer.vkCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), cvkDevice.GetDevice());
            
//            if (CVK_DEBUGGING) {
//                cvkCommandBuffer.DEBUGNAME = debugName;
//                ++CVK_VKALLOCATIONS;
//                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS(%d+) vkAllocateCommandBuffers for %s 0x%016X", 
//                        CVK_VKALLOCATIONS, cvkCommandBuffer.DEBUGNAME, cvkCommandBuffer.vkCommandBuffer.address());                
//            }              
        }
        return cvkCommandBuffer;
    }	
}
