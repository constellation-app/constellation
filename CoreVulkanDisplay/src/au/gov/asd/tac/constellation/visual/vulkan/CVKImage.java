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

import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
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
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D16_UNORM_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D24_UNORM_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_X8_D24_UNORM_PACK32;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_STENCIL_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
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
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class CVKImage {
    private LongBuffer pImage       = MemoryUtil.memAllocLong(1);
    private LongBuffer pImageMemory = MemoryUtil.memAllocLong(1);
    private CVKDevice cvkDevice     = null;
    private int width               = 0;
    private int height              = 0;
    private int layers              = 0;
    private int format              = 0;
    private int tiling              = 0;
    private int usage               = 0;
    private int properties          = 0;   
    private int aspectMask          = 0;
    
    private CVKImage() {}
    
    public long GetImageHandle() { return pImage.get(0); }
    public long GetMemoryImageHandle() { return pImageMemory.get(0); }
    public int GetFormat() { return format; }
    public int GetAspectMask() { return aspectMask; }
      
    public void Destroy() {
        if (pImage.get(0) != VK_NULL_HANDLE) {
            vkDestroyImage(cvkDevice.GetDevice(), pImage.get(0), null);
            pImage.put(0, VK_NULL_HANDLE);
        }
        if (pImageMemory.get(0) != VK_NULL_HANDLE) {
            vkFreeMemory(cvkDevice.GetDevice(), pImageMemory.get(0), null);
            pImageMemory.put(0, VK_NULL_HANDLE);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {
        Destroy();        
        super.finalize();
    }
    
    public static boolean HasDepthComponent(int format) {
        return  format == VK_FORMAT_D16_UNORM ||
                format == VK_FORMAT_X8_D24_UNORM_PACK32 ||
                format == VK_FORMAT_D32_SFLOAT  ||
                format == VK_FORMAT_D16_UNORM_S8_UINT ||
                format == VK_FORMAT_D24_UNORM_S8_UINT ||
                format == VK_FORMAT_D32_SFLOAT_S8_UINT;
    }
    
    public static boolean HasStencilComponent(int format) {
        return format == VK_FORMAT_S8_UINT ||
               format == VK_FORMAT_D16_UNORM_S8_UINT ||
               format == VK_FORMAT_D24_UNORM_S8_UINT ||
               format == VK_FORMAT_D32_SFLOAT_S8_UINT;
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
     * @param oldLayout
     * @param newLayout
     * @return
     */
    public int Transition(CVKCommandBuffer cvkCmdBuf, int oldLayout, int newLayout) {
        int ret = VK_SUCCESS;
    
        try(MemoryStack stack = stackPush()) {
            VkImageMemoryBarrier.Buffer vkBarrier = VkImageMemoryBarrier.callocStack(1, stack);
            vkBarrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            vkBarrier.oldLayout(oldLayout);
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
                if (HasStencilComponent(format)) {
                    aspectMask |= VK_IMAGE_ASPECT_STENCIL_BIT;
                }
            } else {
                aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
            }
            vkBarrier.subresourceRange().aspectMask(aspectMask);

            int sourceStage;
            int destinationStage;
            if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                vkBarrier.srcAccessMask(0);
                vkBarrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if(oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                vkBarrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                vkBarrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
                vkBarrier.srcAccessMask(0);
                vkBarrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
            } else if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
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
        return ret;
    }
        
    
    public static CVKImage Create(  CVKDevice cvkDevice,
                                    int width,
                                    int height,
                                    int layers,
                                    int format,
                                    int tiling,
                                    int usage, 
                                    int properties) {
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
         
        try(MemoryStack stack = stackPush()) {
            VkImageCreateInfo vkImageInfo = VkImageCreateInfo.callocStack(stack);
            vkImageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            vkImageInfo.imageType(VK_IMAGE_TYPE_2D);
            vkImageInfo.extent().width(width);
            vkImageInfo.extent().height(height);
            vkImageInfo.extent().depth(1);
            vkImageInfo.mipLevels(1);
            vkImageInfo.arrayLayers(layers);
            vkImageInfo.format(format);
            vkImageInfo.tiling(tiling);
            vkImageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            vkImageInfo.usage(usage);
            vkImageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
            vkImageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            ret = vkCreateImage(cvkDevice.GetDevice(), vkImageInfo, null, cvkImage.pImage);
            checkVKret(ret);
            assert(cvkImage.pImage.get(0) != VK_NULL_HANDLE);

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
