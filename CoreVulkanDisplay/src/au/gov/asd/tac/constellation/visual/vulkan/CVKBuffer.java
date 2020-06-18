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
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class CVKBuffer {
    //TODO_TT: how do free these in Java, finalize() is deprecated
    protected LongBuffer pBuffer = BufferUtils.createLongBuffer(1);
    protected LongBuffer pBufferMemory = BufferUtils.createLongBuffer(1);
    
    protected CVKBuffer() { 
    }
    
    public long GetBufferHandle() { return pBuffer.get(0); }
    
    public static CVKBuffer CreateBuffer(CVKDevice cvkDevice,
                                         long size, 
                                         int usage, 
                                         int properties) {
        assert(cvkDevice != null);
        assert(cvkDevice.GetDevice() != null);
        
        int ret;
        CVKBuffer buffer = new CVKBuffer();      
        try(MemoryStack stack = stackPush()) {
            // Creating a buffer doesn't actually back it with memory.  Thanks Vulkan.
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(size);
            bufferInfo.usage(usage);
            // This refers to sharing across device queues and seeing as we only have one queue...
            bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
           
            // Create the buffer, it isn't memory backed yet so not terribly useful
            ret = vkCreateBuffer(cvkDevice.GetDevice(), 
                                 bufferInfo, 
                                 null, //alloc callbacks
                                 buffer.pBuffer);
            checkVKret(ret);
            
            // Calculate memory requirements based on the info we proved to the bufferInfo struct
            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetBufferMemoryRequirements(cvkDevice.GetDevice(), buffer.pBuffer.get(0), memRequirements);

            // Allocation info struct, type index needs a little logic as types can be mapped differently between GPUs
            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(cvkDevice.GetMemoryType(memRequirements.memoryTypeBits(), properties));

            // Allocate the memory needed for the buffer (still needs to be bound)
            ret = vkAllocateMemory(cvkDevice.GetDevice(), allocInfo, null, buffer.pBufferMemory);
            checkVKret(ret);

            // We have a pen, we have a apple, we have a pineapple...err bind the buffer to its memory
            ret = vkBindBufferMemory(cvkDevice.GetDevice(), 
                                     buffer.pBuffer.get(0), 
                                     buffer.pBufferMemory.get(0), 
                                     0); //this memory exists only for this buffer, so no offset
            checkVKret(ret);
            
            return buffer;
        }
    }
}
