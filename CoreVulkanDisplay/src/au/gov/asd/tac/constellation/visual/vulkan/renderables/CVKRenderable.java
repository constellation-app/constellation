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
package au.gov.asd.tac.constellation.visual.vulkan.renderables;


import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;

public abstract class CVKRenderable{
    public int SwapChainRecreated(CVKSwapChain cvkSwapChain) { return VK_SUCCESS; }
    public int DisplayUpdate(CVKSwapChain cvkSwapChain, int frameIndex) { return VK_SUCCESS; }
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[]) {}        
    public int RecordCommandBuffer(CVKSwapChain cvkSwapChain, VkCommandBufferInheritanceInfo inheritanceInfo, int index) { return VK_SUCCESS; }
    public VkCommandBuffer GetCommandBuffer(int index) { return null; }
    public int InitCommandBuffer(CVKSwapChain cvkSwapChain) { return VK_SUCCESS; }
    public int GetVertexCount() { return 0; }
    public boolean IsDirty() { return false; }
    public int DeviceInitialised(CVKDevice cvkDevice) { return VK_SUCCESS; }    
    public boolean SharedResourcesNeedUpdating() { return false; }
    public int RecreateSharedResources(CVKSwapChain cvkSwapChain) { return VK_SUCCESS; }


    /**
     * Tasks that implement CVKRenderableUpdateTask are created in the VisualProcessor
     * thread in response to user input.  If those tasks have constructors that 
     * code will be executed in the VisualProcessor thread.  Code in the run method
     * is called from the render thread (AWT Event thread).
     */
    @FunctionalInterface
    public static interface CVKRenderableUpdateTask {
        public void run();
    }                
}
