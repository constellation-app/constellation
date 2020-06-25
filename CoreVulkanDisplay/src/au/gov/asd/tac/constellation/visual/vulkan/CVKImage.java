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
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class CVKImage {
    protected LongBuffer pImage = BufferUtils.createLongBuffer(1);
    protected LongBuffer pImageMemory = BufferUtils.createLongBuffer(1);
    protected final CVKDevice cvkDevice;
    
    protected CVKImage(CVKDevice cvkDevice) {
        this.cvkDevice = cvkDevice;
    }
    
    public long GetImageHandle() { return pImage.get(0); }
    public long GetMemoryImageHandle() { return pImageMemory.get(0); }
    
//    public void Set(CVKDevice cvkDevice, ByteBuffer pBytes, int size) {
//        try (MemoryStack stack = stackPush()) {
//            PointerBuffer data = stack.mallocPointer(1);
//            vkMapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle(), 0, size, 0, data);
//            {
//                ByteBuffer dest = data.getByteBuffer(0, (int)size);
//                pBytes.limit((int)size);
//                dest.put(pBytes);
//                pBytes.limit(pBytes.capacity()).rewind();
//            }
//            vkUnmapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle());
//        }
//    }
    
    @Override
    public void finalize() throws Throwable {
        vkDestroyImage(cvkDevice.GetDevice(), pImage.get(0), null);
        vkFreeMemory(cvkDevice.GetDevice(), pImageMemory.get(0), null);     
        
        super.finalize();
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
        CVKImage cvkImage = new CVKImage(cvkDevice);             
         
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
