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

import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;


public class CVKFrame {
    protected long hRenderFence = VK_NULL_HANDLE;
    protected long hImageAvailableSemaphore = VK_NULL_HANDLE;
    protected long hRenderFinishedSemaphore = VK_NULL_HANDLE;
    protected final VkDevice vkDevice;
    
    public long GetRenderFinishedSemaphoreHandle() { return hRenderFinishedSemaphore; }
    public long GetImageAcquireSemaphoreHandle() { return hImageAvailableSemaphore; }
    public long GetRenderFence() { return hRenderFence; }
    
    public CVKFrame(VkDevice device) {
        assert(device != null);
        
        vkDevice = device;
        try (MemoryStack stack = stackPush()) {
            
            // Render fence, CPU-GPU synch
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);
            LongBuffer pFence = stack.mallocLong(1);
            checkVKret(vkCreateFence(vkDevice, fenceInfo, null, pFence));
            hRenderFence = pFence.get(0);
            
            //TODO_TT: doco
            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);                
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
            checkVKret(vkCreateSemaphore(vkDevice, semaphoreInfo, null, pImageAvailableSemaphore));
            hRenderFinishedSemaphore = pImageAvailableSemaphore.get(0);
            checkVKret(vkCreateSemaphore(vkDevice, semaphoreInfo, null, pRenderFinishedSemaphore));                    
            hImageAvailableSemaphore = pRenderFinishedSemaphore.get(0);
        }
    }
    
    
    public int WaitResetRenderFence()
    {
        int ret;
        ret = vkWaitForFences(vkDevice, 
                              hRenderFence, 
                              true, //wait on first or all fences, only one fence in this case
                              Long.MAX_VALUE); //timeout
        if (VkFailed(ret)) return ret;
        ret = vkResetFences(vkDevice, hRenderFence);
        return ret;
    }
}
