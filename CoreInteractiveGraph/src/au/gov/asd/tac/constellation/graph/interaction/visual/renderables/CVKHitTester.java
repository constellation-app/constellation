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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState.HitType;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;


/**
 * This lives in the Core Interactive Graph project so it can import HitTestRequest and
 * HitState.  If it lived in Core Display Vulkan importing those types would require
 * Core Display Vulkan to be dependent on Core Interactive Graph which would be a 
 * circular dependency.
 * 
 */
public class CVKHitTester extends CVKRenderable {

    private HitTestRequest hitTestRequest;
    private final BlockingDeque<HitTestRequest> requestQueue = new LinkedBlockingDeque<>();
    private final Queue<Queue<HitState>> notificationQueues = new LinkedList<>();
    private boolean needsDisplayUpdate = false;
    
    public void queueRequest(final HitTestRequest request) {
        requestQueue.add(request);
        needsDisplayUpdate = true;
    }
    
    
    @Override
    public boolean NeedsDisplayUpdate() { 
        return needsDisplayUpdate; 
    }
     

    @Override
    public int DisplayUpdate() {
        // TODO Hydra: Need to reset the needsDisplayUpdate flag in here
        int ret = VK_SUCCESS;
        if (requestQueue != null && !requestQueue.isEmpty()) {
            requestQueue.forEach(request -> notificationQueues.add(request.getNotificationQueue()));
            hitTestRequest = requestQueue.getLast();
            requestQueue.clear();
        }
        
        if (!notificationQueues.isEmpty()) {
            final int x = hitTestRequest.getX();
            final int y = hitTestRequest.getY();

            //  Windows-DPI-Scaling
            //
            // If JOGL is ever fixed or another solution is found, either change
            // needsManualDPIScaling to return false (so there is effectively no
            // DPI scaling here) or to remove dpiScaleY below.            
            

            final HitState hitState = hitTestRequest.getHitState();
            hitState.setCurrentHitId(-1);
            hitState.setCurrentHitType(HitType.NO_ELEMENT);
            if (hitTestRequest.getFollowUpOperation() != null) {
                hitTestRequest.getFollowUpOperation().accept(hitState);
            }
            synchronized (this.notificationQueues) {
                while (!notificationQueues.isEmpty()) {
                    final Queue<HitState> queue = notificationQueues.remove();
                    if (queue != null) {
                        queue.add(hitState);
                    }
                }
            }
        }
        return ret;
    }  
    
    @Override
    public void Destroy() {}
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return null; }
    @Override
    public int DestroySwapChainResources() { return VK_SUCCESS; }
    
    @Override
    public int CreateSwapChainResources(CVKSwapChain cvkSwapChain) { 
        return VK_SUCCESS; 
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKSwapChain.CVKDescriptorPoolRequirements reqs, CVKSwapChain.CVKDescriptorPoolRequirements perImageReqs) {}     
    @Override
    public int RecordCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int index) { return VK_SUCCESS;}
    @Override
    public int GetVertexCount() { return 0; }
    @Override
    public int Initialise(CVKDevice cvkDevice) { return VK_SUCCESS;}    
}
