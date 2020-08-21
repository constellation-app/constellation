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
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_VKALLOCATIONS;

public class CVKBuffer {
    final static int COPY_SIZE = 4096; //candidate for profiling
    
    //TODO_TT: how do free these in Java, finalize() is deprecated
    protected LongBuffer pBuffer = MemoryUtil.memAllocLong(1);
    protected LongBuffer pBufferMemory = MemoryUtil.memAllocLong(1);
    protected CVKDevice cvkDevice = null;
    protected long bufferSize = 0;
    private PointerBuffer pWriteMemory = null;
    private int properties = 0;
    
    private String DEBUGNAME = "";
    
    // Use the static Create() method instead of direct construction
    private CVKBuffer() { }
    
    public long GetBufferHandle() { return pBuffer.get(0); }
    public long GetBufferSize() { return bufferSize; }
    public long GetMemoryBufferHandle() { return pBufferMemory.get(0); }
    
    public int CopyFrom(CVKBuffer other) {
        CVKAssert(GetBufferSize() >= other.GetBufferSize());
        int ret;
        
        try (MemoryStack stack = stackPush()) {
            CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY, "CVKBuffer cvkCopyCmd");
            ret = cvkCopyCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            if (VkFailed(ret)) { return ret; }  

            VkBufferCopy.Buffer vkCopyRegions = VkBufferCopy.callocStack(1, stack);
            VkBufferCopy vkBufferCopy = vkCopyRegions.get(0);
            vkBufferCopy.dstOffset(0);
            vkBufferCopy.srcOffset(0);
            vkBufferCopy.size(other.GetBufferSize());

            vkCmdCopyBuffer(cvkCopyCmd.GetVKCommandBuffer(),
                            other.GetBufferHandle(),
                            GetBufferHandle(),
                            vkCopyRegions);            
            ret = cvkCopyCmd.EndAndSubmit();               
            cvkCopyCmd.Destroy();            
        }
        
        return ret;
    }
    
    /**
     *
     * @param pBytes, source ByteBuffer
     * @param destOffset, where in our buffer to start the write
     * @param srcOffset, where in pBytes to start the read
     * @param size, how much to read/write
     * @return VkResult code
     */
    public int Put(ByteBuffer pBytes, int destOffset, int srcOffset, int size) {
        int ret;
        CVKAssert((properties & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) != 0);
        
        // Check memory is not already mapped with an unfinished write
        CVKAssert(pWriteMemory == null); 
        try (MemoryStack stack = stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            
            // Remember source position and limit so we can restore them post copy
            int origPos = pBytes.position();
            int origLim = pBytes.limit();
            
            // Map destOffset into our buffer into host writable memory
            ret = vkMapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle(), destOffset, size, 0, data); //arg 5 is flags
            if (VkFailed(ret)) { return ret; }
            {
                // Get a ByteBuffer representing the mapped memory, note offset is 0 as we offset in vkMapMemory
                ByteBuffer dest = data.getByteBuffer(0, size);
                
                // Move to the source start position
                pBytes.position(srcOffset);
                
                // Set the limit from there so we only copy size bytes even if both buffers would allow a bigger read/write
                pBytes.limit(size + srcOffset);
                
                // Do the copy
                dest.put(pBytes);
                
                // Reset pBytes to it's starting position and limit         
                pBytes.limit(origLim).position(origPos);
            }
            vkUnmapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle());
        }
        
        return ret;
    }
    
    public ByteBuffer StartMemoryMap(int offset, int size) {
        CVKAssert((properties & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) != 0);
        CVKAssert(pWriteMemory == null);
        CVKAssert(size > offset);
        pWriteMemory = MemoryUtil.memAllocPointer(1);
        vkMapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle(), offset, size, 0, pWriteMemory);
        return pWriteMemory.getByteBuffer(size);
    }
    
    public void EndMemoryMap() {
        CVKAssert(pWriteMemory != null);
        vkUnmapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle());
        MemoryUtil.memFree(pWriteMemory);
        pWriteMemory = null;
    }
    
    public static class DEBUG_CVKBufferElementDescriptor {
        public final String label;
        public final Class<?> type;
        public DEBUG_CVKBufferElementDescriptor(String inLabel, Class<?> inType) {
            label = inLabel;
            type = inType;
        }
    }
    public void DEBUGPRINT(List<DEBUG_CVKBufferElementDescriptor> typeDescriptors) {
        ByteBuffer pData = StartMemoryMap(0, (int)bufferSize);
        
        cvkDevice.GetLogger().info("\n");
        cvkDevice.GetLogger().info(String.format("Contents of %s:", DEBUGNAME));
        int idx = 0;
        while (pData.hasRemaining()) {
            for (DEBUG_CVKBufferElementDescriptor desc : typeDescriptors) {
                if (desc.type == Float.TYPE) {  
                    final float f = pData.getFloat();
                    cvkDevice.GetLogger().info(String.format("\tidx %d\t%s:\t%f", idx, desc.label, f));
                } else if (desc.type == Integer.TYPE) {
                    final int d = pData.getInt();
                    cvkDevice.GetLogger().info(String.format("\tidx %d\t%s:\t%d", idx, desc.label, d));
                }else {
                    cvkDevice.GetLogger().info(String.format("CVKBuffer.DEBUGPRINT cannot handle type <%s>", desc.type.getName()));
                    break;
                }
            }
            ++idx;
        }
        
        cvkDevice.GetLogger().info("\n");
        
        EndMemoryMap();
    }
    
    /**
     * java.nio.ByteBuffer and their like are unpooled heap buffers unlike DirectByteBuffer
     * and HeapByteBuffer.  The pooled buffers are zeroed by the JVM, the nio buffers on the
     * other hand must be explicitly zeroed.
     */
    public void ZeroMemory() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle(), 0, bufferSize, 0, data);
            {                
                ByteBuffer dest = data.getByteBuffer(0, (int)bufferSize);
                MemoryUtil.memSet(dest, 0);
            }
            vkUnmapMemory(cvkDevice.GetDevice(), GetMemoryBufferHandle());
        }
    }
    
    public void Destroy() {
        if (CVK_DEBUGGING && pBuffer != null) {
            if (pBufferMemory != null && pBufferMemory.get(0) != VK_NULL_HANDLE) {
                --CVK_VKALLOCATIONS;
                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS (%d-) Destroy called on CVKBuffer %s (Buffer:0x%016X Memory:0x%016X), vkFreeMemory will be called",
                        CVK_VKALLOCATIONS, DEBUGNAME, pBuffer.get(0), pBufferMemory.get(0));
            } else {                
                cvkDevice.GetLogger().info("CVK_VKALLOCATIONS (%d!) Destroy called on CVKBuffer %s (Buffer:0x%016X Memory:0x%016X), vkFreeMemory will NOT be called", 
                        CVK_VKALLOCATIONS, CVK_VKALLOCATIONS, DEBUGNAME, pBuffer.get(0), pBufferMemory.get(0));                              
            }            
        }
        if (pBuffer != null && pBuffer.get(0) != VK_NULL_HANDLE) {
            vkDestroyBuffer(cvkDevice.GetDevice(), pBuffer.get(0), null);
            pBuffer.put(0, VK_NULL_HANDLE);
            MemoryUtil.memFree(pBuffer);
            pBuffer = null;
        }
        if (pBufferMemory != null && pBufferMemory.get(0) != VK_NULL_HANDLE) {
            vkFreeMemory(cvkDevice.GetDevice(), pBufferMemory.get(0), null);
            pBufferMemory.put(0, VK_NULL_HANDLE);
            MemoryUtil.memFree(pBufferMemory);
            pBufferMemory = null;            
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {
        Destroy();
        super.finalize();
    }
    
 
    /**
     * Factory creation method for CVKBuffers
     * 
     * @param cvkDevice
     * @param size
     * @param usage
     * @param properties
     * @param debugName
     * @return
     */
    public static CVKBuffer Create( CVKDevice cvkDevice,
                                    long size, 
                                    int usage, 
                                    int properties,
                                    String debugName) {
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        
        int ret;
        CVKBuffer cvkBuffer = new CVKBuffer();      
        try(MemoryStack stack = stackPush()) {
            cvkBuffer.bufferSize = size;
            cvkBuffer.cvkDevice  = cvkDevice;
            cvkBuffer.properties = properties;
            
            // Creating a buffer doesn't actually back it with memory.  Thanks Vulkan.
            VkBufferCreateInfo vkBufferInfo = VkBufferCreateInfo.callocStack(stack);
            vkBufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            vkBufferInfo.size(size);
            vkBufferInfo.usage(usage);
            
            // This refers to sharing across device queues and seeing as we only have one queue...
            vkBufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
           
            // Create the buffer, it isn't memory backed yet so not terribly useful
            ret = vkCreateBuffer(cvkDevice.GetDevice(), 
                                 vkBufferInfo, 
                                 null, //alloc callbacks
                                 cvkBuffer.pBuffer);
            checkVKret(ret);
            
            // Calculate memory requirements based on the info we proved to the bufferInfo struct
            VkMemoryRequirements vkMemoryRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetBufferMemoryRequirements(cvkDevice.GetDevice(), cvkBuffer.pBuffer.get(0), vkMemoryRequirements);

            // Allocation info struct, type index needs a little logic as types can be mapped differently between GPUs
            VkMemoryAllocateInfo vkAllocationInfo = VkMemoryAllocateInfo.callocStack(stack);
            vkAllocationInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            vkAllocationInfo.allocationSize(vkMemoryRequirements.size());
            vkAllocationInfo.memoryTypeIndex(cvkDevice.GetMemoryType(vkMemoryRequirements.memoryTypeBits(), properties));

            // Allocate the memory needed for the buffer (still needs to be bound)
            ret = vkAllocateMemory(cvkDevice.GetDevice(), vkAllocationInfo, null, cvkBuffer.pBufferMemory);
            checkVKret(ret);

            // We have a pen, we have a apple, we have a pineapple...err bind the buffer to its memory
            ret = vkBindBufferMemory(cvkDevice.GetDevice(), 
                                     cvkBuffer.pBuffer.get(0), 
                                     cvkBuffer.pBufferMemory.get(0), 
                                     0); //this memory exists only for this buffer, so no offset
            checkVKret(ret);
            
            if (CVK_DEBUGGING) {
                cvkBuffer.DEBUGNAME = debugName;
                ++CVK_VKALLOCATIONS;
                cvkDevice.GetLogger().info(String.format("CVK_VKALLOCATIONS(%d+) vkAllocateMemory(%d) for CVKBuffer %s (Buffer:0x%016X Memory:0x%016X)", 
                        CVK_VKALLOCATIONS, vkMemoryRequirements.size(), cvkBuffer.DEBUGNAME, cvkBuffer.pBuffer.get(0), cvkBuffer.pBufferMemory.get(0)));                
            }
            
            return cvkBuffer;
        }
    }
}
