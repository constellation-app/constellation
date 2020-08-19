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
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkSubmitInfo;


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
	
    public void FinishRecord(){

        int err = vkEndCommandBuffer(vkCommandBuffer);

        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to finish record command buffer: ");
        }
    }

    public static CVKCommandBuffer Create(final CVKDevice cvkDevice, final int level, final String debugName) {
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
