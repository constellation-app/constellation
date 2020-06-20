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

import au.gov.asd.tac.constellation.visual.Renderable;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKFrame;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderer;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public interface CVKRenderable extends Renderable{

    public abstract int SwapChainRezied(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain);
    public abstract int DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex);
    public abstract void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[]);  
    
    //TEMP TEMP TEMP
    public abstract void Display(MemoryStack stack, CVKFrame frame, CVKRenderer cvkRenderer, CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex);
    //public abstract int LoadShaders(CVKDevice cvkDevice);
    public abstract int RecordCommandBuffer(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain);
    public abstract VkCommandBuffer GetCommandBuffer();
    public abstract long GetGraphicsPipeline();
    public abstract int GetVertex();
    public abstract void draw(VkCommandBuffer commandBuffer);
    // TEMP TEMP TEMP

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
