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
package au.gov.asd.tac.constellation.visual.vulkan;


import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdClearColorImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCmdDispatch;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkResetCommandBuffer;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.*;
import au.gov.asd.tac.constellation.visual.vulkan.maths.*;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;


public class CVKCommandBuffer {
	private VkCommandBuffer vkCommandBuffer;
	private CVKDevice cvkDevice;
        
        
        private CVKCommandBuffer() {}
  
    
        public VkCommandBuffer GetVKCommandBuffer(){ return vkCommandBuffer; }
		
               
        @Override
        public void finalize() throws Throwable {		
            vkFreeCommandBuffers(cvkDevice.GetDevice(), cvkDevice.GetCommandPoolHandle(), vkCommandBuffer);
            super.finalize();
	}
	
	public int BeginRecord(int flags) {		
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
//	
//	public void BeginRecordSecondary(int flags, long framebuffer, long renderPass, int subpass) {
//		
//		VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc()
//				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
//				.pNext(0)
//				.framebuffer(framebuffer)
//				.renderPass(renderPass)
//				.subpass(subpass)
//				.occlusionQueryEnable(false)
//				.queryFlags(0)
//				.pipelineStatistics(0);
//		
//		VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc()
//                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
//                .pNext(0)
//                .flags(flags)
//                .pInheritanceInfo(inheritanceInfo);
//			
//		int err = vkBeginCommandBuffer(vkCommandBuffer, beginInfo);
//
//		if (err != VK_SUCCESS) {
//			throw new AssertionError("Failed to begin record command buffer: ");
//		}
//        
//        beginInfo.free();
//	}
//	
//	public void finishRecord(){
//			
//		int err = vkEndCommandBuffer(vkCommandBuffer);
//		
//        if (err != VK_SUCCESS) {
//            throw new AssertionError("Failed to finish record command buffer: ");
//        }
//	}
//	
//	public void beginRenderPassCmd(long renderPass, long frameBuffer,
//			int width, int height, int colorAttachmentCount, int depthAttachment,
//			int contentsFlag){
//		
//		VkClearValue.Buffer clearValues = VkClearValue.calloc(
//				colorAttachmentCount + depthAttachment);
//		
//		for (int i=0; i<colorAttachmentCount; i++){
//			clearValues.put(CVKUtils.getClearValueColor(new Vec3f(0,0,0)));
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
//	private void beginRenderPassCmd(long renderPass, long frameBuffer,
//			int width, int height, int flags, VkClearValue.Buffer clearValues){
//		
//		VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
//				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
//				.pNext(0)
//				.renderPass(renderPass)
//				.pClearValues(clearValues)
//				.framebuffer(frameBuffer);
//		
//		VkRect2D renderArea = renderPassBeginInfo.renderArea();
//		renderArea.offset().set(0, 0);
//		renderArea.extent().set(width, height);
//		
//		vkCmdBeginRenderPass(vkCommandBuffer, renderPassBeginInfo, flags);
//		
//		renderPassBeginInfo.free();
//	}
//	
//	public void endRenderPassCmd(){
//		
//		vkCmdEndRenderPass(vkCommandBuffer);
//	}
//	
//	public void bindComputePipelineCmd(long pipeline){
//		
//		vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
//	}
//	
//	public void bindGraphicsPipelineCmd(long pipeline){
//		
//		vkCmdBindPipeline(vkCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
//	}
//	
//	public void viewPortCmd(){
//		
//		// TODO
//	}
//	
//	public void scissorCmd(){
//		
//		// TODO
//	}
//	
//	public void pushConstantsCmd(long pipelineLayout, int stageFlags, ByteBuffer data){
//		
//		vkCmdPushConstants(vkCommandBuffer,
//				pipelineLayout,
//				stageFlags,
//				0,
//				data);
//	}
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
//	public void bindVertexInputCmd(long vertexBuffer){
//		
//		LongBuffer offsets = memAllocLong(1);
//		offsets.put(0, 0L);
//		LongBuffer pVertexBuffers = memAllocLong(1);
//		pVertexBuffers.put(0, vertexBuffer);
//		
//		vkCmdBindVertexBuffers(vkCommandBuffer, 0, pVertexBuffers, offsets);
//		
//		memFree(pVertexBuffers);
//		memFree(offsets);
//	}
//	
//	public void bindComputeDescriptorSetsCmd(long pipelinyLayout, long[] descriptorSets){
//
//		bindDescriptorSetsCmd(pipelinyLayout, descriptorSets,
//				VK_PIPELINE_BIND_POINT_COMPUTE);
//	}
//	
//	public void bindGraphicsDescriptorSetsCmd(long pipelinyLayout, long[] descriptorSets){
//		
//		bindDescriptorSetsCmd(pipelinyLayout, descriptorSets,
//				VK_PIPELINE_BIND_POINT_GRAPHICS);
//	}
//	
//	private void bindDescriptorSetsCmd(long pipelinyLayout, long[] descriptorSets,
//			int pipelineBindPoint){
//		
//		vkCmdBindDescriptorSets(vkCommandBuffer, pipelineBindPoint,
//				pipelinyLayout, 0, descriptorSets, null);
//	}
//	
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
//	public void pipelineImageMemoryBarrierCmd(long image, int oldLayout, int newLayout,
//			int srcAccessMask, int dstAccessMask, int srcStageMask, int dstStageMask,
//			int baseMipLevel, int mipLevelCount){
//		
//		VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1)
//				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
//				.oldLayout(oldLayout)
//				.newLayout(newLayout)
//				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
//				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
//				.image(image)
//				.srcAccessMask(srcAccessMask)
//				.dstAccessMask(dstAccessMask);
//		
//		barrier.subresourceRange()
//				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
//				.baseMipLevel(baseMipLevel)
//				.levelCount(mipLevelCount)
//				.baseArrayLayer(0)
//				.layerCount(1);
//	
//		vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
//				VK_DEPENDENCY_BY_REGION_BIT, null, null, barrier);
//		
//		barrier.free();
//	}
//	
//	public void pipelineImageMemoryBarrierCmd(long image, int srcStageMask, int dstStageMask,
//			VkImageMemoryBarrier.Buffer barrier){
//	
//		vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
//				VK_DEPENDENCY_BY_REGION_BIT, null, null, barrier);
//	}
//	
//	public void pipelineMemoryBarrierCmd(int srcAccessMask, int dstAccessMask,
//			int srcStageMask, int dstStageMask){
//		
//		VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc(1)
//				.sType(VK_STRUCTURE_TYPE_MEMORY_BARRIER)
//				.srcAccessMask(srcAccessMask)
//				.dstAccessMask(dstAccessMask);
//		
//		vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
//				VK_DEPENDENCY_BY_REGION_BIT, barrier, null, null);
//	}
//	
//	public void pipelineBarrierCmd(int srcStageMask, int dstStageMask){
//		
//		vkCmdPipelineBarrier(vkCommandBuffer, srcStageMask, dstStageMask,
//				VK_DEPENDENCY_BY_REGION_BIT, null, null, null);
//	}
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
//        public static CVKCommandBuffer Create(CVKDevice cvkDevice, int level) {
//            assert(cvkDevice != null);
//            assert(cvkDevice.GetDevice() != null);
//
//            int ret;
//            CVKCommandBuffer cvkCommandBuffer = new CVKCommandBuffer();
//            cvkCommandBuffer.cvkDevice = cvkDevice;
//            try (MemoryStack stack = stackPush()) {
//                VkCommandBufferAllocateInfo vkAllocateInfo = VkCommandBufferAllocateInfo.callocStack(stack);
//                vkAllocateInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
//                vkAllocateInfo.commandPool(cvkDevice.GetCommandPoolHandle());
//                vkAllocateInfo.level(level);
//                vkAllocateInfo.commandBufferCount(1);
//
//                PointerBuffer pCommandBuffer = stack.mallocPointer(1);
//                ret = vkAllocateCommandBuffers(cvkDevice.GetDevice(), vkAllocateInfo, pCommandBuffer);
//                checkVKret(ret);
//
//
//                cvkCommandBuffer.vkCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), cvkDevice.GetDevice());
//            }
//            return cvkCommandBuffer;
//	}	
}
