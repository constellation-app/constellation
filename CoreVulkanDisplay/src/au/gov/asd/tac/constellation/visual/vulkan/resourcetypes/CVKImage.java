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

import au.gov.asd.tac.constellation.utilities.graphics.Vector3i;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKFormatUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_BUFFER_TOO_SMALL_FOR_COPY;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_INVALID_ARGS;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_STENCIL_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_1D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_1D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_1D_ARRAY;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D_ARRAY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_INVALID_IMAGE;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKFormatUtils.VkFormatByteDepth;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;

public class CVKImage {
    private CVKDevice cvkDevice     = null;
    private LongBuffer pImage       = MemoryUtil.memAllocLong(1);
    private LongBuffer pImageView   = MemoryUtil.memAllocLong(1);
    private LongBuffer pImageMemory = MemoryUtil.memAllocLong(1);    
    private int width               = 0;
    private int height              = 0;
    private int layers              = 0;
    private int format              = 0;
    private int tiling              = 0;
    private int usage               = 0;
    private int properties          = 0;   
    private int aspectMask          = 0;
    private int imageType           = 0;
    private int viewType            = 0;
    private int layout              = 0;    
    
    private CVKImage() {}
    
    public long GetImageHandle() { return pImage.get(0); }
    public long GetImageViewHandle() { return pImageView.get(0); }
    public long GetMemoryImageHandle() { return pImageMemory.get(0); }
    public int GetFormat() { return format; }
    public int GetAspectMask() { return aspectMask; }
      
    public void Destroy() {
        if (pImage.get(0) != VK_NULL_HANDLE) {
            vkDestroyImage(cvkDevice.GetDevice(), pImage.get(0), null);
            pImage.put(0, VK_NULL_HANDLE);
        }
        if (pImageView.get(0) != VK_NULL_HANDLE) {
            vkDestroyImageView(cvkDevice.GetDevice(), pImageView.get(0), null);
            pImageView.put(0, VK_NULL_HANDLE);
        }        
        if (pImageMemory.get(0) != VK_NULL_HANDLE) {
            vkFreeMemory(cvkDevice.GetDevice(), pImageMemory.get(0), null);
            pImageMemory.put(0, VK_NULL_HANDLE);
        }
        cvkDevice = null;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {
        Destroy();        
        super.finalize();
    }
    
    
    public int Transition(int newLayout) {
        int ret;
        
        CVKCommandBuffer cvkTransitionCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
        cvkTransitionCmd.DEBUGNAME = "CVKImage.Transition cvkTransitionCmd";
        ret = cvkTransitionCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
        if (VkFailed(ret)) { return ret; }
        ret = Transition(cvkTransitionCmd, newLayout);
        if (VkSucceeded(ret)) {
            // Blocking execute the command buffer
            ret = cvkTransitionCmd.EndAndSubmit();                        
        }
        cvkTransitionCmd.Destroy();
        
        return ret;
    }
    
        
    /**
     * This method transitions an image from layout to another.  It uses a memory
     * barrier to do this.  It doesn't sound like the kind of construct that is
     * implicitly a command, but as the Vulkan-Tutorial (Images) says:
     * "One of the most common ways to perform layout transitions is using an image
     * memory barrier. A pipeline barrier like that is generally used to synchronize
     * access to resources, like ensuring that a write to a buffer completes before
     * reading from it..."
     * 
     * Some member variables aren't set until an image is transitioned for the first 
     * time, TODO_TT add asserts to catch access before transition?
     * 
     * @param cvkCmdBuf
     * @param newLayout
     * @return
     */
    public int Transition(CVKCommandBuffer cvkCmdBuf, int newLayout) {
        int ret = VK_SUCCESS;
    
        try(MemoryStack stack = stackPush()) {
            VkImageMemoryBarrier.Buffer vkBarrier = VkImageMemoryBarrier.callocStack(1, stack);
            vkBarrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            vkBarrier.oldLayout(layout);
            vkBarrier.newLayout(newLayout);
            vkBarrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            vkBarrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            vkBarrier.image(GetImageHandle());

            vkBarrier.subresourceRange().baseMipLevel(0);
            vkBarrier.subresourceRange().levelCount(1);
            vkBarrier.subresourceRange().baseArrayLayer(0);
            vkBarrier.subresourceRange().layerCount(1);

            // Depth/stencil or colour?
            if (newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                aspectMask = VK_IMAGE_ASPECT_DEPTH_BIT;                
                if (CVKFormatUtils.VkFormatHasStencilComponent(format)) {
                    aspectMask |= VK_IMAGE_ASPECT_STENCIL_BIT;
                }
            } else {
                aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            }
            vkBarrier.subresourceRange().aspectMask(aspectMask);

            int sourceStage;
            int destinationStage;
            if (layout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                vkBarrier.srcAccessMask(0);
                vkBarrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if(layout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                vkBarrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                vkBarrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            } else if (layout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                vkBarrier.srcAccessMask(0);
                vkBarrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
            } else if(layout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
                vkBarrier.srcAccessMask(0);
                vkBarrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
            } else {
                throw new IllegalArgumentException("Unsupported layout transition");
            }
            
            for (int iLayer = 0; iLayer < layers; ++iLayer) {
                vkBarrier.subresourceRange().baseArrayLayer(iLayer);
                vkCmdPipelineBarrier(cvkCmdBuf.GetVKCommandBuffer(),
                                     sourceStage,
                                     destinationStage,
                                     0,                 // dependency flags
                                     null,              // memory barriers
                                     null,              // buffer memory barriers
                                     vkBarrier);        // image memory barriers        
            }
        }
        layout = newLayout;
        
        return ret;
    }
    
    
    public int CopyFrom(CVKBuffer buffer) {       
        Vector3i dstPixelOffsets = new Vector3i(0,0,0);
        Vector3i dstPixelExtents = new Vector3i(0,0,0);        
        
        // Sanity check the buffer is probably holding pixels that match our format
        if ((buffer.GetBufferSize() % VkFormatByteDepth(format)) != 0) {
            return CVK_ERROR_INVALID_ARGS;
        }
        
        // Check for overflow from ridiculously large buffer
        if (buffer.GetBufferSize() > Integer.MAX_VALUE) {
            return CVK_ERROR_INVALID_ARGS;
        }                    
        
        // Calculate width      
        int pixelsInBuffer = (int)buffer.GetBufferSize() / VkFormatByteDepth(format);
        dstPixelExtents.setX(Math.min(pixelsInBuffer, width));
        
        // Calculate height
        int rowsInBuffer = pixelsInBuffer / width;
        int rowRemainder = pixelsInBuffer % width;
        if (rowRemainder > 0) {
            ++rowsInBuffer;
        }
        dstPixelExtents.setY(Math.min(rowsInBuffer, height));
        
        // Calculate depth
        int pixelsInImageLayer = width * height;
        int requiredLayers = pixelsInBuffer / pixelsInImageLayer;
        int leftOverPixels = pixelsInBuffer % pixelsInImageLayer;
        if (leftOverPixels > 0) {
            ++requiredLayers;

            // If this is a 1D array image we can treat it like a buffer, but if
            // it is 2D then we must either fill a region smaller than a full
            // layer or we fill full layers.
            if (layout == VK_IMAGE_TYPE_2D) {
                return CVK_ERROR_INVALID_ARGS;
            }
        }
        if (requiredLayers > layers) {
            return CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY;
        }
        dstPixelExtents.setZ(requiredLayers);
        
        return CopyFrom(buffer, 0, dstPixelOffsets, dstPixelExtents);
    }
    
    
    public int CopyFrom(CVKBuffer buffer, final int srcByteOffset, final Vector3i dstPixelOffset, final Vector3i dstPixelExtent) {
        int ret;
        
        // Sanity check our image is valid
        if (layout == VK_IMAGE_LAYOUT_UNDEFINED || width == 0 || height == 0 || layers == 0) {
            return CVK_ERROR_INVALID_IMAGE;
        }
        
        // Sanity check we have something to copy
        if (dstPixelExtent.isZero()) {
            return CVK_ERROR_INVALID_ARGS;
        }               
        
        // only allow incomplete writes into a single layer        
        if (!dstPixelOffset.isZero() && dstPixelExtent.getZ() != 1) {
            return CVK_ERROR_INVALID_ARGS;
        }        
        
        // Check the destination region is within our extents
        if (width < (dstPixelOffset.getX() + dstPixelExtent.getX())) {
            return CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY;
        }
        if (height < (dstPixelOffset.getY() + dstPixelExtent.getY())) {
            return CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY;
        }
        if (layers < (dstPixelOffset.getZ() + dstPixelExtent.getZ())) {
            return CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY;
        }
        
        // Validate the buffer contains enough bytes to fullful the destination extents
        int bytesRequired = dstPixelExtent.getX() * dstPixelExtent.getY() * dstPixelExtent.getZ() * VkFormatByteDepth(format);
        if (bytesRequired > (buffer.GetBufferSize() - srcByteOffset)) {
            return CVK_ERROR_BUFFER_TOO_SMALL_FOR_COPY;
        }
        
        // Command to copy pixels and potentially transition if necessary
        CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
        cvkCopyCmd.DEBUGNAME = "CVKImage.CopyFrom cvkCopyCmd";
        ret = cvkCopyCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
        if (VkFailed(ret)) { return ret; }
         
        // Handle image transition if needed
        int originalLayout = layout;
        if (layout != VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
            ret = Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            if (VkFailed(ret)) { return ret; }
        }
        
        // Copy each layer
        try (MemoryStack stack = stackPush()) {
            for (int iLayer = 0; iLayer < dstPixelExtent.getZ(); ++iLayer) {
                // Setup a buffer image copy structure for the current image layer
                VkBufferImageCopy.Buffer copyLayerBuffer = VkBufferImageCopy.callocStack(1, stack);
                VkBufferImageCopy copyLayer = copyLayerBuffer.get(0);
                copyLayer.bufferOffset(srcByteOffset);
                copyLayer.bufferRowLength(0);    // Tightly packed
                copyLayer.bufferImageHeight(0);  // Tightly packed
                copyLayer.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                copyLayer.imageSubresource().mipLevel(0);
                copyLayer.imageSubresource().baseArrayLayer(dstPixelOffset.getZ() + iLayer);
                copyLayer.imageSubresource().layerCount(1);
                copyLayer.imageOffset().set(dstPixelOffset.getX(), dstPixelOffset.getY(), 0);
                copyLayer.imageExtent(VkExtent3D.callocStack(stack).set(dstPixelExtent.getX(), dstPixelExtent.getY(), 1));     
                
                // Enqueue the copy command to the command buffer
                vkCmdCopyBufferToImage(cvkCopyCmd.GetVKCommandBuffer(),
                                       buffer.GetBufferHandle(),
                                       GetImageHandle(),
                                       layout,
                                       copyLayerBuffer);                    
            }
        }
        
        // Restore image layout if needed
        if (layout != originalLayout) {
            ret = Transition(cvkCopyCmd, originalLayout);
            if (VkFailed(ret)) { return ret; }
        }        
        
        // Blocking execute the command buffer
        ret = cvkCopyCmd.EndAndSubmit();  
        cvkCopyCmd.Destroy();
        
        return ret;
    }    
        
    
    public static CVKImage Create(  CVKDevice cvkDevice,
                                    int width,
                                    int height,
                                    int layers,
                                    int format,
                                    int tiling,
                                    int usage, 
                                    int properties,
                                    int aspectMask) {
        assert(cvkDevice != null);
        assert(cvkDevice.GetDevice() != null);
        assert(layers >= 1);
        
        int ret;
        CVKImage cvkImage   = new CVKImage();
        cvkImage.cvkDevice  = cvkDevice;
        cvkImage.width      = width;
        cvkImage.height     = height;
        cvkImage.layers     = layers;
        cvkImage.format     = format;
        cvkImage.tiling     = tiling;
        cvkImage.usage      = usage; 
        cvkImage.properties = properties;
        cvkImage.aspectMask = aspectMask;        
        cvkImage.imageType  = height > 1 ? VK_IMAGE_TYPE_2D : VK_IMAGE_TYPE_1D;
        cvkImage.viewType   = layers > 1 ? (height > 1 ? VK_IMAGE_VIEW_TYPE_2D_ARRAY : VK_IMAGE_VIEW_TYPE_1D_ARRAY) : (height > 1 ? VK_IMAGE_VIEW_TYPE_2D : VK_IMAGE_VIEW_TYPE_1D);
        cvkImage.layout     = VK_IMAGE_LAYOUT_UNDEFINED;
         
        try(MemoryStack stack = stackPush()) {
            // Create the image, this is an opaque type we can't read or write
            VkImageCreateInfo vkImageInfo = VkImageCreateInfo.callocStack(stack);
            vkImageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            vkImageInfo.imageType(cvkImage.imageType);
            vkImageInfo.extent().width(width);
            vkImageInfo.extent().height(height);
            vkImageInfo.extent().depth(1);
            vkImageInfo.mipLevels(1);
            vkImageInfo.arrayLayers(layers);
            vkImageInfo.format(format);
            vkImageInfo.tiling(tiling);
            vkImageInfo.initialLayout(cvkImage.layout);
            vkImageInfo.usage(usage);
            vkImageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
            vkImageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            ret = vkCreateImage(cvkDevice.GetDevice(), vkImageInfo, null, cvkImage.pImage);
            checkVKret(ret);
            assert(cvkImage.pImage.get(0) != VK_NULL_HANDLE);

            // The image isn't backed by memory, do that now
            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetImageMemoryRequirements(cvkDevice.GetDevice(), cvkImage.pImage.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(cvkDevice.GetMemoryType(memRequirements.memoryTypeBits(), properties));

            if(vkAllocateMemory(cvkDevice.GetDevice(), allocInfo, null, cvkImage.pImageMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate image memory");
            }

            vkBindImageMemory(cvkDevice.GetDevice(), cvkImage.pImage.get(0), cvkImage.pImageMemory.get(0), 0);
            
            // Create an image view that can be used to read and write this image
            VkImageViewCreateInfo vkViewInfo = VkImageViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            vkViewInfo.image(cvkImage.GetImageHandle());
            vkViewInfo.viewType(cvkImage.viewType);
            vkViewInfo.format(cvkImage.GetFormat());
            vkViewInfo.subresourceRange().aspectMask(cvkImage.GetAspectMask());
            vkViewInfo.subresourceRange().baseMipLevel(0);
            vkViewInfo.subresourceRange().levelCount(1);
            vkViewInfo.subresourceRange().baseArrayLayer(0);
            vkViewInfo.subresourceRange().layerCount(layers);

            ret = vkCreateImageView(cvkDevice.GetDevice(), vkViewInfo, null, cvkImage.pImageView);
            checkVKret(ret);           
            
            return cvkImage;
        } catch (Exception e) {
            //TODO_TT: move this to class destructor
            if (cvkImage.pImage.get(0) != VK_NULL_HANDLE) {
                vkDestroyImage(cvkDevice.GetDevice(), cvkImage.GetImageHandle(), null);
            }
            if (cvkImage.pImageMemory.get(0) != VK_NULL_HANDLE) {
                vkFreeMemory(cvkDevice.GetDevice(), cvkImage.GetMemoryImageHandle(), null);
            }       
        }
        
        return null;
    }
}
