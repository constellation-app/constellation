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
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKFormatUtils.VkFormatByteDepth;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_IMAGE_VIEW_CREATION_FAILED;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SAVE_TO_FILE_FAILED;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_VKALLOCATIONS;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import java.awt.Point;
import java.awt.Transparency;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;       // TODO make sure this import is ok from javax
import org.lwjgl.PointerBuffer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_BLIT_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;
import static org.lwjgl.vulkan.VK10.vkCmdBlitImage;
import static org.lwjgl.vulkan.VK10.vkCmdCopyImage;
import static org.lwjgl.vulkan.VK10.vkGetImageSubresourceLayout;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageCopy;
import org.lwjgl.vulkan.VkImageSubresource;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkSubresourceLayout;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;


public class CVKImage {
    protected CVKDevice cvkDevice     = null;
    protected LongBuffer pImage       = MemoryUtil.memAllocLong(1);
    protected LongBuffer pImageView   = MemoryUtil.memAllocLong(1);
    protected LongBuffer pImageMemory = MemoryUtil.memAllocLong(1);    
    protected int width               = 0;
    protected int height              = 0;
    protected int layers              = 0;
    protected int format              = 0;
    protected int tiling              = 0;
    protected int usage               = 0;
    protected int properties          = 0;   
    protected int aspectMask          = 0;
    protected int imageType           = 0;
    protected int viewType            = 0;
    protected int layout              = 0;    
    protected String DEBUGNAME        = "";
    protected PointerBuffer pWriteMemory = null;
    CVKImage destImage                  = null;

    protected CVKImage() {}
    
    public long GetImageHandle() { return pImage.get(0); }
    public long GetImageViewHandle() { return pImageView.get(0); }
    public long GetMemoryImageHandle() { return pImageMemory.get(0); }
    public int GetFormat() { return format; }
    public int GetAspectMask() { return aspectMask; }
      
    public void Destroy() {
        DestroyImage();
        DestroyImageView();
        DestroyImageMemory();        
    }
    
    public void DestroyImage() {
        if (CVK_DEBUGGING && pImage.get(0) != VK_NULL_HANDLE) {
            if (pImageMemory != null && pImageMemory.get(0) != VK_NULL_HANDLE) {
                --CVK_VKALLOCATIONS;
                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS (%d-) Destroy called on CVKimage %s (image:0x%016X memory:0x%016X), vkFreeMemory will be called", 
                        CVK_VKALLOCATIONS, DEBUGNAME, pImage.get(0), pImageMemory.get(0));                      
            } else {
                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS (%d!) Destroy called on CVKimage %s (image:0x%016X memory:0x%016X), vkFreeMemory will NOT be called", 
                        CVK_VKALLOCATIONS, DEBUGNAME, pImage.get(0), pImageMemory.get(0));  
            }            
        }                
        if (pImage.get(0) != VK_NULL_HANDLE) {
            vkDestroyImage(cvkDevice.GetDevice(), pImage.get(0), null);
            pImage.put(0, VK_NULL_HANDLE);
            pImage = null;
        }
    }
    
    public void DestroyImageView() {
        if (pImageView.get(0) != VK_NULL_HANDLE) {
            vkDestroyImageView(cvkDevice.GetDevice(), pImageView.get(0), null);
            pImageView.put(0, VK_NULL_HANDLE);
            pImageView = null;
        }
    }
    
    public void DestroyImageMemory() {
        if (pImageMemory.get(0) != VK_NULL_HANDLE) {
            vkFreeMemory(cvkDevice.GetDevice(), pImageMemory.get(0), null);
            pImageMemory.put(0, VK_NULL_HANDLE);
            pImageMemory = null;
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
        
        CVKCommandBuffer cvkTransitionCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY, "CVKImage.Transition cvkTransitionCmd");
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
    
    // TODO HYDRA - Check if this is still needed?
    public int Transition(CVKCommandBuffer cvkCmdBuf, int oldLayout, int newLayout) {
         layout = oldLayout;
         return Transition(cvkCmdBuf, newLayout);
    }
    
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
            } else if(/*layout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL &&*/ newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
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
            // TODO - this should work for any initial layout
            // Tansition image into layout for copying (e.g. copy to memory or screenshot)
            } else if(/*layout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL &&*/ newLayout == VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL) {
                vkBarrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                vkBarrier.dstAccessMask(VK_ACCESS_MEMORY_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            // Transition copy image back to general state
            } else if(layout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_GENERAL) {
                vkBarrier.srcAccessMask(VK_ACCESS_MEMORY_READ_BIT);
                vkBarrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            // Transition swapchain image back to original state after a screenshot
            } else if(layout == VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_PRESENT_SRC_KHR) {
                vkBarrier.srcAccessMask(VK_ACCESS_MEMORY_READ_BIT);
                vkBarrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
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
        CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY, "CVKImage.CopyFrom cvkCopyCmd");
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
        if (VkFailed(ret)) { return ret; }
        
        cvkCopyCmd.Destroy();
        
        return ret;
    }    
    
    
    /**
     * Factory returning an Image that has been allocated and an ImageView
     * created with it.
     * 
     * @param cvkDevice
     * @param width
     * @param height
     * @param layers
     * @param format
     * @param viewType
     * @param tiling
     * @param usage
     * @param properties
     * @param aspectMask
     * @param debugName
     * @return The image or null if errors occur
     */
    public static CVKImage Create(  CVKDevice cvkDevice,
                                    int width,
                                    int height,
                                    int layers,
                                    int format,
                                    int viewType,
                                    int tiling,
                                    int usage, 
                                    int properties,
                                    int aspectMask,
                                    String debugName) {
        CVKImage cvkImage = CreateImage(cvkDevice,
                width,
                height,
                layers,
                format,
                viewType,
                tiling,
                usage,
                properties,
                aspectMask,
                debugName);
        
        cvkImage.CreateImageView();
        
        return cvkImage;
        
    }
    
   
    /**
     * Factory function that creates/allocates memory for an image
     * 
     * @param cvkDevice
     * @param width
     * @param height
     * @param layers
     * @param format
     * @param viewType
     * @param tiling
     * @param usage
     * @param properties
     * @param aspectMask
     * @param debugName
     * @return The image or null if errors occur
     */
    public static CVKImage CreateImage(  CVKDevice cvkDevice,
                                    int width,
                                    int height,
                                    int layers,
                                    int format,
                                    int viewType,
                                    int tiling,
                                    int usage, 
                                    int properties,
                                    int aspectMask,
                                    String debugName) {
        CVKAssertNotNull(cvkDevice);
        CVKAssertNotNull(cvkDevice.GetDevice());
        CVKAssert(layers >= 1);
        
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
        cvkImage.viewType   = viewType;
        cvkImage.layout     = VK_IMAGE_LAYOUT_UNDEFINED;
        cvkImage.DEBUGNAME  = debugName;
         
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
            if (VkFailed(ret)) { return null; }
            
            CVKAssertNotNull(cvkImage.pImage.get(0));

            // The image isn't backed by memory, do that now
            VkMemoryRequirements vkMemoryRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetImageMemoryRequirements(cvkDevice.GetDevice(), cvkImage.pImage.get(0), vkMemoryRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(vkMemoryRequirements.size());
            allocInfo.memoryTypeIndex(cvkDevice.GetMemoryType(vkMemoryRequirements.memoryTypeBits(), properties));

            if (vkAllocateMemory(cvkDevice.GetDevice(), allocInfo, null, cvkImage.pImageMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate image memory");
            }
            if (CVK_DEBUGGING) {
                ++CVK_VKALLOCATIONS;
                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS(%d+) vkAllocateMemory(%d) for CVKimage %s (image:0x%016X memory:0x%016X)", 
                        CVK_VKALLOCATIONS, vkMemoryRequirements.size(), cvkImage.DEBUGNAME, cvkImage.pImage.get(0), cvkImage.pImageMemory.get(0));
            }        

            vkBindImageMemory(cvkDevice.GetDevice(), cvkImage.pImage.get(0), cvkImage.pImageMemory.get(0), 0);
        }
        return cvkImage;
    }


    /**
     * Creates an ImageView associate with the image handle
     * @return error code
     */
    public int CreateImageView() {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {
                // Create an image view that can be used to read and write this image
                VkImageViewCreateInfo vkViewInfo = VkImageViewCreateInfo.callocStack(stack);
                vkViewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
                vkViewInfo.image(GetImageHandle());
                vkViewInfo.viewType(viewType);
                vkViewInfo.format(GetFormat());
                vkViewInfo.subresourceRange().aspectMask(GetAspectMask());
                vkViewInfo.subresourceRange().baseMipLevel(0);
                vkViewInfo.subresourceRange().levelCount(1);
                vkViewInfo.subresourceRange().baseArrayLayer(0);
                vkViewInfo.subresourceRange().layerCount(layers);

                ret = vkCreateImageView(cvkDevice.GetDevice(), vkViewInfo, null, pImageView);
                if (VkFailed(ret)) { return ret; }

        } catch (Exception e) {
            // TODO HYDRA TEST THIS CODE PATH       
            cvkDevice.GetLogger().LogException(e, "Image View creation failed for Image Handle %d. Image will be destroyed!", GetImageHandle());
            ret = CVK_ERROR_IMAGE_VIEW_CREATION_FAILED;
            
            DestroyImage();
            DestroyImageMemory();
        }
        
        return ret;
    }
    
    
    /**
     * Copies this image into destImage in CPU memory to the VK_FORMAT_R8G8B8A8_UNORM
     * format
     * 
     * @param stack
     * @return error code
     */
    public int CopyToCPUMemoryImage(MemoryStack stack) {
        CVKAssertNull(destImage);
        
        int ret = VK_SUCCESS;
 	boolean supportsBlit = true;
        int originalLayout = VK_IMAGE_LAYOUT_UNDEFINED;
                
        // Check blit support for source and destination
        // TODO HYDRA do this once and store result in cvkdevice
        VkFormatProperties formatProps = VkFormatProperties.callocStack(stack);

        // Check if the device supports blitting from optimal images (the swapchain images are in optimal format)
        vkGetPhysicalDeviceFormatProperties(cvkDevice.GetPhysicalDevice(), format, formatProps);

        if ((formatProps.optimalTilingFeatures() & VK_FORMAT_FEATURE_BLIT_SRC_BIT) == 0) {
                cvkDevice.GetLogger().info("Device does not support blitting from optimal tiled images, using copy instead of blit!");
                supportsBlit = false;
        }

        // Check if the device supports blitting to linear images
        vkGetPhysicalDeviceFormatProperties(cvkDevice.GetPhysicalDevice(), VK_FORMAT_R8G8B8A8_UNORM, formatProps);
        if ((formatProps.linearTilingFeatures() & VK_FORMAT_FEATURE_BLIT_DST_BIT) == 0) {
                cvkDevice.GetLogger().info("Device does not support blitting to linear tiled images, using copy instead of blit!");
                supportsBlit = false;
        }

        // Source for the copy is the last rendered swapchain image
        originalLayout = layout;
        CVKImage srcImage = this;

        // Create the linear tiled destination image to copy to and to read the memory from
        destImage = CVKImage.Create(cvkDevice, width, height, 
                layers, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_TYPE_2D, VK_IMAGE_TILING_LINEAR, 
                VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                aspectMask,
                "CVKImage destImage");
        if (destImage == null) { return 1; }
        
        // Do the actual blit from the swapchain image to our host visible destination image
        CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY, "CVKImage-CopyCmdBuffer");

        ret = cvkCopyCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
        if (VkFailed(ret)) { return ret; }

        // Transition destination image to transfer destination layout
        ret = destImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        if (VkFailed(ret)) { return ret; }

        // Transition source image from present to transfer source layout
        ret = srcImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
        if (VkFailed(ret)) { return ret; }

        // If source and destination support blit we'll blit as this also does automatic format conversion (e.g. from BGR to RGB)
        boolean needsSwizzle = false;
        if (supportsBlit) {
            // Define the region to blit (we will blit the whole swapchain image)
            VkOffset3D blitSize = VkOffset3D.callocStack(stack);
            blitSize.set(width, height, 1);
            VkImageBlit.Buffer imageBlitRegion = VkImageBlit.callocStack(1, stack);
            imageBlitRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageBlitRegion.srcSubresource().layerCount(1);
            imageBlitRegion.srcOffsets(1, blitSize);
            imageBlitRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageBlitRegion.dstSubresource().layerCount(1);
            imageBlitRegion.dstOffsets(1, blitSize);

            // Issue the blit command
            // The blit will automatically transfer from the original format
            // to the destination. For example the swap image uses RGB 
            // while the destination format is VK_FORMAT_R8G8B8A8_UNORM 
            // TODO HYDRA we could probably just use a 3 byte format without alpha
            // If that works it will save a lot of messing around later. Test after
            // hittester is working.
            vkCmdBlitImage(
                    cvkCopyCmd.GetVKCommandBuffer(),
                    srcImage.GetImageHandle(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    destImage.GetImageHandle(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    imageBlitRegion,
                    VK_FILTER_NEAREST);
        }
        else
        {
            // TODO Hydra test this code path
            // Otherwise use image copy (requires us to manually flip components)
            VkImageCopy.Buffer imageCopyRegion = VkImageCopy.callocStack(1, stack);
            imageCopyRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageCopyRegion.srcSubresource().layerCount(1);
            imageCopyRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageCopyRegion.dstSubresource().layerCount(1);

            VkExtent3D extent3D = VkExtent3D.callocStack(stack);
            extent3D.set(width, height, 1);
            imageCopyRegion.extent(extent3D);

            // Issue the copy command
            vkCmdCopyImage(
                    cvkCopyCmd.GetVKCommandBuffer(),
                    srcImage.GetImageHandle(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    destImage.GetImageHandle(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    imageCopyRegion);

            needsSwizzle = true;
        }

        // Transition destination image to general layout, which is the required layout for mapping the image memory later on
        ret = destImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_GENERAL);
        if (VkFailed(ret)) { return ret; }

        // Transition back the image to original layout after the blit is done
        ret = srcImage.Transition(cvkCopyCmd, originalLayout);
        if (VkFailed(ret)) { return ret; }

        // Submit the commands and wait
        cvkCopyCmd.EndAndSubmit();
                
        return ret;
    }
    
    
    public PointerBuffer StartMemoryMap(MemoryStack stack) {
        CVKAssert(pWriteMemory == null);    
        int ret = VK_SUCCESS;
        
        // Get layout of the image (including row pitch)
        VkImageSubresource subResource = VkImageSubresource.callocStack(stack);
        subResource.set(VK_IMAGE_ASPECT_COLOR_BIT, 0, 0);
        VkSubresourceLayout subResourceLayout = VkSubresourceLayout.callocStack(stack);
        vkGetImageSubresourceLayout(cvkDevice.GetDevice(), destImage.GetImageHandle(), subResource, subResourceLayout);

        // Map Memory
        pWriteMemory = MemoryUtil.memAllocPointer(1);
        long offset = subResourceLayout.offset();

        ret = vkMapMemory(cvkDevice.GetDevice(), destImage.GetMemoryImageHandle(), offset, VK_WHOLE_SIZE, 0, pWriteMemory);
        if (VkFailed(ret)) 
        { 
            cvkDevice.GetLogger().severe(String.format("Mapping image memory failed"));
            return null; 
        }
        
        return pWriteMemory;     
    }       
    
    
    /**
     * Cleanup the mapped memory and from StartMemoryMap and destroy the destImage
     * 
     */
    public void EndMemoryMap() {
        CVKAssertNotNull(pWriteMemory);
        vkUnmapMemory(cvkDevice.GetDevice(), destImage.GetMemoryImageHandle());
        MemoryUtil.memFree(pWriteMemory);
        pWriteMemory = null;
        destImage.Destroy();
        destImage = null;
    }
    
    
    /**
     * SaveMemoryToFile
     * 
     * Takes the imageSource, that has already been transitioned to CPU memory,
     * and saves it to file in the PNG format.
     * 
     * @param stack
     * @param file
     * @param imageMemory
     * @param imageSource
     * @param needsSwizzle
     * @return 
     */
    public int SaveMemoryToFile(MemoryStack stack, File file, boolean needsSwizzle) {
        // Check paramters
        CVKAssertNotNull(file);
        CVKAssertNotNull(pWriteMemory);
        CVKAssertNotNull(destImage);
        
        int ret = VK_SUCCESS;
        BufferedImage out = null;

        // Get layout of the image (including row pitch)
        VkImageSubresource subResource = VkImageSubresource.callocStack(stack);
        subResource.set(VK_IMAGE_ASPECT_COLOR_BIT, 0, 0);
        VkSubresourceLayout subResourceLayout = VkSubresourceLayout.callocStack(stack);
        vkGetImageSubresourceLayout(cvkDevice.GetDevice(), destImage.GetImageHandle(), subResource, subResourceLayout);

        // The destination image is in the format VK_FORMAT_B8G8R8A8_UNORM:
        // CmdBlitImage() automatically converts between formats for us.
        // VK_FORMAT_B8G8R8A8_UNORM
        //    specifies a four-component, 32-bit unsigned normalized format 
        //    that has an 8-bit B component in byte 0, an 8-bit G component 
        //    in byte 1, an 8-bit R component in byte 2, and an 8-bit A component in byte 3.

        // The PNG image format is RGB (24-bit)

        // Size (in bytes) of the destination image including any padding
        final long sourceImageSize = subResourceLayout.size(); 
        // Offset (in bytes) that the image starts (usually 0)
        long offset = subResourceLayout.offset();
        // Calculate the file width including the padding (in bytes)
        int fileWidth = (int)(subResourceLayout.rowPitch() / 4);
        // Calculate the file height (in bytes)
        int fileHeight = (int)(sourceImageSize / fileWidth) / 4;
        // Calculate the file size (in bytes)
        int fileSize = (int)(sourceImageSize / 4 ) * 3;

        // TODO Hydra need to handle images with layers like the atlas texture
        long fileLayers = subResourceLayout.depthPitch();

        try {
            // Create the output buffered image with the original images
            // width and height, and in the PNG format (3 bytes BGR)
            out = new BufferedImage(width, height, TYPE_3BYTE_BGR);
            byte[] rgb = new byte[fileSize];
            int i = 0;
            int r = 0;

            ByteBuffer row = pWriteMemory.getByteBuffer((int)sourceImageSize);

            for (int y = 0; y < fileHeight; ++y)
            {                   
                for (int x = 0; x < fileWidth; ++x)
                {
                    // Skip the padded bytes
                    if (x >= width) {
                        r += 4;
                        continue;
                    }
                    // TODO: tests to see if we need to swizzle the rgb channels
                    Byte red = row.get(r++);        // red
                    Byte green = row.get(r++);      // green
                    Byte blue = row.get(r++);       // blue
                    Byte alpha = row.get(r++);      // alpha

                    // If we need to swizzle colours manually (e.g. Blit is not supported which
                    // does automatic conversion for us
                    if (needsSwizzle) {
                        rgb[i++] = blue;
                        rgb[i++] = green;
                        rgb[i++] = red;
                    }
                    else {
                        rgb[i++] = red;
                        rgb[i++] = green;
                        rgb[i++] = blue;
                    }
                }
            }

            DataBuffer buffer = new DataBufferByte(rgb, rgb.length);

            // 3 bytes per pixel: red, green, blue
            WritableRaster raster = Raster.createInterleavedRaster(buffer, 
                    width, height,          // width, height of image
                    3 * width,              // number of bytes per row (stride)
                    3,                      // number of channels (rgb)
                    new int[] {0, 1, 2},    // colour format order (i.e. byte 0 is first, byte 1 is second ...
                                            // can change order if you want a different format rgb -> bgr
                    (Point)null);           // TODO Look up this
            ColorModel colorModel = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE); 
            BufferedImage image = new BufferedImage(colorModel, raster, true, null);

            // Save to file
            ImageIO.write(image, "png", file);
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            cvkDevice.GetLogger().LogException(e, String.format("Save file failed: %s", file.getName()));
            return CVK_ERROR_SAVE_TO_FILE_FAILED;
        }

        return ret;
    }
       

    /**
     * Creates a file from filename and saves this image to it
     * 
     * @param filename
     * @return errorCode
     */
    public int SaveToFile(String filename) {
        File file = new File(filename); 
        return SaveToFile(file);
    }
    
    /**
     * Saves this image to file (in PNG format)
     * @param file
     * @return 
     */
    public int SaveToFile(File file) {
        CVKAssertNotNull(file);
        int ret;

        try( MemoryStack stack = stackPush()) {
            // Copy this image into CPU memory
            ret = CopyToCPUMemoryImage(stack);
            if (VkFailed(ret)) { return ret; }
                        
            // Start mapping the destination images memory
            StartMemoryMap(stack);
            
            // Save to the given file
            ret = SaveMemoryToFile(stack, file, false);
            if (VkFailed(ret)) { return ret; }

            // Cleanup
            EndMemoryMap();
     
        }
        return ret;
    }
}
