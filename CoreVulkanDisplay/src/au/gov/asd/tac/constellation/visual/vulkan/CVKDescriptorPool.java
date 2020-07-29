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

import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

public class CVKDescriptorPool {
    private CVKDevice cvkDevice = null;
    private long hDescriptorPool = VK_NULL_HANDLE;
    
    public long GetDescriptorPoolHandle() { return hDescriptorPool; }
        
    
    /**
     * Simple class for counting the number of descriptors and desciptor sets
     * needed by all of the renderables.
     */
    public static class CVKDescriptorPoolRequirements {
        public final static int VK_DESCRIPTOR_TYPE_COUNT = 11;
        public final int poolDescriptorTypeCounts[] = new int[VK_DESCRIPTOR_TYPE_COUNT];
        public int poolDesciptorSetCount = 0;
        
        public int GetTotalDescriptorCount() {
            int count = 0;
            for (int i = 0; i < VK_DESCRIPTOR_TYPE_COUNT; ++i) {
                count += poolDescriptorTypeCounts[i];
            }
            return count;
        }
        public int GetNumberOfNonEmptyTypes() {
            int count = 0;
            for (int i = 0; i < VK_DESCRIPTOR_TYPE_COUNT; ++i) {
                if (poolDescriptorTypeCounts[i] > 0) { ++count; }
            }
            return count;          
        }
    }
    private final CVKDescriptorPoolRequirements cvkDescriptorPoolRequirements = new CVKDescriptorPoolRequirements();    
    
    
    public boolean CanAccomodate(final int imageCount, CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {
        CVKDescriptorPoolRequirements coalesced = new CVKDescriptorPoolRequirements();
                
        coalesced.poolDesciptorSetCount = reqs.poolDesciptorSetCount +
                                          perImageReqs.poolDesciptorSetCount * imageCount;
        
        for (int i = 0; i < CVKDescriptorPoolRequirements.VK_DESCRIPTOR_TYPE_COUNT; ++i) {
            coalesced.poolDescriptorTypeCounts[i] = reqs.poolDescriptorTypeCounts[i] +
                                                    perImageReqs.poolDescriptorTypeCounts[i] * imageCount;
        }
        
        if (coalesced.poolDesciptorSetCount > cvkDescriptorPoolRequirements.poolDesciptorSetCount) {
            return false;
        }
        
        for (int i = 0; i < CVKDescriptorPoolRequirements.VK_DESCRIPTOR_TYPE_COUNT; ++i) {
            if (coalesced.poolDescriptorTypeCounts[i] > cvkDescriptorPoolRequirements.poolDescriptorTypeCounts[i]) {
                return false;
            }
        }
        
        return true;
    }
    

    /**
     * Descriptor pools are per thread (we have one Render thread in Constellation).
     * Here we create one Descriptor pool for all our Renderable objects. 
     * Each Renderable object is in charge of telling the Renderer how many 
     * Descriptors Types and Descriptor Sets it uses (IncrementDescriptorTypeRequirements)
     * so here we can allocate the correct amount of memory. 
     * 
     * @param cvkDevice
     * @param imageCount
     * @param poolReqs
     * @param perImagePoolReqs
     */
    public CVKDescriptorPool(CVKDevice cvkDevice,
                             final int imageCount,
                             final CVKDescriptorPoolRequirements poolReqs,
                             final CVKDescriptorPoolRequirements perImagePoolReqs) {
        VerifyInRenderThread();
        CVKAssert(cvkDevice != null);
        CVKAssert(imageCount > 0);
        CVKAssert(poolReqs != null);
        CVKAssert(perImagePoolReqs != null);
                        
        int ret = VK_SUCCESS;
        this.cvkDevice = cvkDevice;
        
        cvkDescriptorPoolRequirements.poolDesciptorSetCount = poolReqs.poolDesciptorSetCount +
                                                              perImagePoolReqs.poolDesciptorSetCount * imageCount;
        
        for (int i = 0; i < CVKDescriptorPoolRequirements.VK_DESCRIPTOR_TYPE_COUNT; ++i) {
            cvkDescriptorPoolRequirements.poolDescriptorTypeCounts[i] = poolReqs.poolDescriptorTypeCounts[i] +
                                                                        perImagePoolReqs.poolDescriptorTypeCounts[i] * imageCount;
        }
        
        
        // Every renderable object will likely want it's own descriptor set.  For some it will
        // consist of a uniform buffer, a sampler and image.
        
        // To size the descriptor pool we need to know how many objects will have a descriptor set
        // and what types are in those descriptor sets.
        
        // This will need to be resized periodically when new renderable objects are added to our
        // scene.  The descriptor pool will also need to be recreated to the appropriate size when
        // the swapchain is rebuilt.
        
        try (MemoryStack stack = stackPush()) {  
            // Do we have anything to render?
            int allTypesCount = cvkDescriptorPoolRequirements.GetNumberOfNonEmptyTypes();       
            if (allTypesCount > 0) {
                VkDescriptorPoolSize.Buffer pPoolSizes = VkDescriptorPoolSize.callocStack(allTypesCount, stack);

                int iPoolSize = 0;
                for (int iType = 0; iType < 11; ++iType) {
                    int count = cvkDescriptorPoolRequirements.poolDescriptorTypeCounts[iType];
                    if (count > 0) {
                        VkDescriptorPoolSize vkPoolSize = pPoolSizes.get(iPoolSize++);
                        vkPoolSize.type(iType);
                        vkPoolSize.descriptorCount(count);
                        CVKLOGGER.info(String.format("Descriptor pool type %d = count %d", iType, count));                                        
                    } 
                }           

                // Create the complete Descriptor pool using the poolSizes we calculated for each
                // Descriptor Type
                VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
                poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
                poolInfo.flags(VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT);
                poolInfo.pPoolSizes(pPoolSizes);
                poolInfo.maxSets(cvkDescriptorPoolRequirements.poolDesciptorSetCount);
                CVKLOGGER.info(String.format("Descriptor pool maxSets = %d", cvkDescriptorPoolRequirements.poolDesciptorSetCount));  

                LongBuffer pDescriptorPool = stack.mallocLong(1);
                ret = vkCreateDescriptorPool(cvkDevice.GetDevice(), poolInfo, null, pDescriptorPool);
                checkVKret(ret);
                hDescriptorPool = pDescriptorPool.get(0);

                CVKAssert(hDescriptorPool != VK_NULL_HANDLE);
            } 
        }
    }
    
    public void Destroy() {
        vkDestroyDescriptorPool(cvkDevice.GetDevice(), hDescriptorPool, null);
        hDescriptorPool = VK_NULL_HANDLE;
        cvkDevice = null;
        CVKLOGGER.info("Destroyed descriptor pool");
    }    
}
