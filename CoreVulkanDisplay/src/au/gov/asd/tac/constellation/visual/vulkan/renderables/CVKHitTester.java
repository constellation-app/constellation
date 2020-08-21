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

import au.gov.asd.tac.constellation.graph.hittest.HitState;
import au.gov.asd.tac.constellation.graph.hittest.HitState.HitType;
import au.gov.asd.tac.constellation.graph.hittest.HitTestRequest;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKImage;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;


/**
 * This lives in the Core Interactive Graph project so it can import HitTestRequest and
 * HitState.  If it lived in Core Display Vulkan importing those types would require
 * Core Display Vulkan to be dependent on Core Interactive Graph which would be a 
 * circular dependency.
 * 
 * 
 * Steps
 * 1. Create an image the same size as the viewport
 * 2. Create a framebuffer based off that image
 * 3. 
 */
public class CVKHitTester extends CVKRenderable {

    private HitTestRequest hitTestRequest;
    private final BlockingDeque<HitTestRequest> requestQueue = new LinkedBlockingDeque<>();
    private final Queue<Queue<HitState>> notificationQueues = new LinkedList<>();
    private boolean needsDisplayUpdate = true;
    private CVKImage cvkImage = null;
    private CVKImage cvkDepthImage = null;
    private Long hFrameBufferHandle = null;
    private CVKCommandBuffer commandBuffer = null;
    // TODO Use swapchain image format?
    //private int colorFormat = VK_FORMAT_R8G8B8A8_UINT;

    // ========================> Static init <======================== \\
    
    
    
    // ========================> Lifetime <======================== \\
    
    public CVKHitTester(CVKVisualProcessor parent) {
        this.cvkVisualProcessor = parent;
    }
    
    @Override
    public int Initialise(CVKDevice cvkDevice) { 
        this.cvkDevice = cvkDevice;  
        return VK_SUCCESS;
    }
    
    @Override
    public void Destroy() {
        DestroyFrameBuffer();
        DestroyImage();
        DestroyCommandBuffer();
        
        CVKAssertNull(hFrameBufferHandle);
        CVKAssertNull(cvkImage);
        CVKAssertNull(commandBuffer);
    }
    
    
    // ========================> Swap chain <======================== \\    
    
    @Override
    protected int DestroySwapChainResources() { return VK_SUCCESS; }
    
    private int CreateSwapChainResources() {
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(cvkSwapChain);
        int ret = VK_SUCCESS;

                
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (swapChainImageCountChanged) {
            ret = CreateImages();
            if (VkFailed(ret)) { return ret; }            

            ret = CreateFrameBuffer();
            if (VkFailed(ret)) { return ret; }

             ret = CreateCommandBuffer();
            if (VkFailed(ret)) { return ret; }
            
        } else {
            // This is the resize path, image count is unchanged.
        }
        
        swapChainResourcesDirty = false;
        swapChainImageCountChanged = false;
        
        return ret;
    } 
    
    
    // ========================> Image <======================== \\
    
    private int CreateImages() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(cvkSwapChain.GetDepthFormat() != VK_FORMAT_UNDEFINED);
        
        int ret = VK_SUCCESS;
   
        try (MemoryStack stack = stackPush()) {
            
            int textureWidth = cvkSwapChain.GetWidth();
            int textureHeight = cvkSwapChain.GetHeight();
            int requiredLayers = 1;
                        
            // Create destination color image            
            cvkImage = CVKImage.Create(cvkDevice, 
                                            textureWidth, 
                                            textureHeight, 
                                            requiredLayers, 
                                            //colorFormat, // Format TODO Not sure what the format should be - look at GL version
                                            cvkSwapChain.GetColorFormat(),
                                            VK_IMAGE_VIEW_TYPE_2D,
                                            VK_IMAGE_TILING_LINEAR, // Tiling
                                            VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT, // TODO Usage 
                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT , // TODO - properties?
                                            VK_IMAGE_ASPECT_COLOR_BIT,
                                            "CVKHitTester cvkImage");  // TODO - aspect mask
            
            if (cvkImage == null) {
                return 1;
            }
            
            // TODO HYDRA: Might be able to just use DepthImage from Swapchain!
            // Create depth image 
            cvkDepthImage = CVKImage.Create(cvkDevice, 
                                            textureWidth, 
                                            textureHeight, 
                                            requiredLayers, 
                                            cvkSwapChain.GetDepthFormat(),
                                            VK_IMAGE_VIEW_TYPE_2D,
                                            VK_IMAGE_TILING_OPTIMAL, // Tiling or VK_IMAGE_TILING_OPTIMAL
                                            VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, // TODO Usage 
                                            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, // TODO - properties?
                                            VK_IMAGE_ASPECT_DEPTH_BIT,
                                            "CVKHitTester cvkDepthImage");  // TODO - aspect mask
            if (cvkDepthImage == null) {
                // TODO HYDRA: If this DepthImage is required add a CVK error
                return 1;
            }           
        }
        
        return ret;
    }
    
    private void DestroyImage() {
        if (cvkImage != null) {
            cvkImage.Destroy();
            cvkImage = null;
        }
    }
    
    
    // ========================> Frame buffer <======================== \\
    
    private int CreateFrameBuffer() {
        int ret = VK_SUCCESS;
               
        try(MemoryStack stack = stackPush()) {
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(cvkSwapChain.GetOffscreenRenderPassHandle());
            framebufferInfo.width(cvkSwapChain.GetWidth());
            framebufferInfo.height(cvkSwapChain.GetHeight());
            framebufferInfo.layers(1);

            LongBuffer attachments = stack.mallocLong(2);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            attachments.put(0, cvkImage.GetImageViewHandle());
            attachments.put(1, cvkDepthImage.GetImageViewHandle());
            framebufferInfo.pAttachments(attachments);
            ret = vkCreateFramebuffer(cvkDevice.GetDevice(), 
                                      framebufferInfo, 
                                      null, //allocation callbacks
                                      pFramebuffer);
            if (VkFailed(ret)) { return ret; }
            
            hFrameBufferHandle = pFramebuffer.get(0);

        }
        return ret;

    }
    
    private void DestroyFrameBuffer() {
        if (hFrameBufferHandle != null) {
            vkDestroyFramebuffer(cvkDevice.GetDevice(), hFrameBufferHandle, null);
            hFrameBufferHandle = null;
            CVKLOGGER.info(String.format("Destroyed frame buffer for HitTester"));
        }
    }  
    
    
    // ========================> Vertex buffers <======================== \\    
    
    @Override
    public int GetVertexCount() { return 0; }
    
    
    // ========================> Command buffers <======================== \\
    
    private int CreateCommandBuffer() {       
        CVKAssertNotNull(cvkDevice);
        
        int ret = VK_SUCCESS;
             
        commandBuffer = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY, "CVKHitTester CommandBuffer");
        commandBuffer.DEBUGNAME = String.format("CVKHitTester");
        
        CVKLOGGER.log(Level.INFO, "Init Command Buffer - HitTester");
        
        return ret;
    }
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return commandBuffer.GetVKCommandBuffer(); }
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int index) { 
        return VK_SUCCESS;
    }
        
    private void DestroyCommandBuffer() {
        if (commandBuffer != null) {
            commandBuffer.Destroy();
            commandBuffer = null;
        }
    }   
    
    // ========================> Descriptors <======================== \\
    
    @Override
    public int DestroyDescriptorPoolResources() { return VK_SUCCESS; }     
      
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {}       
    
    
    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() { 
        return needsDisplayUpdate; 
    }
     
    @Override
    public int DisplayUpdate() {
        int ret = VK_SUCCESS;
        
        // TODO change to enum as in IconRenderable
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }
                
        // TODO Hydra: Need to reset the needsDisplayUpdate flag in here    
        if (requestQueue != null && !requestQueue.isEmpty()) {
            requestQueue.forEach(request -> notificationQueues.add(request.getNotificationQueue()));
            hitTestRequest = requestQueue.getLast();
            requestQueue.clear();
        }
        
        if (!notificationQueues.isEmpty()) {
            final int x = hitTestRequest.getX();
            final int y = hitTestRequest.getY();

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
        
        needsDisplayUpdate = false;
        return ret;
    }  

    @Override
    public int OffscreenRender(List<CVKRenderable> hitTestRenderables) {
        cvkVisualProcessor.VerifyInRenderThread();
        
        CVKAssertNotNull(cvkDevice.GetDevice());
        CVKAssertNotNull(cvkDevice.GetCommandPoolHandle());
        CVKAssertNotNull(cvkSwapChain);
                
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
            
            // TODO OR VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO ?
            ret = commandBuffer.Begin(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            if (VkFailed(ret)) { return ret; }
                       
            // Pre Draw Barrier
            commandBuffer.pipelineImageMemoryBarrierCmd(cvkImage.GetImageHandle(), 
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,    // Old/New Layout
                    0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,                                // Src/Dst Access mask
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,  // Src/Dst Stage mask
                    0, 1);                                                                  // baseMipLevel/mipLevelCount
                        
            // Clear colour to black
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.color().float32(stack.floats(0f, 1.0f, 0.5f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(cvkSwapChain.GetOffscreenRenderPassHandle());

            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(cvkSwapChain.GetExtent());
            renderPassInfo.renderArea(renderArea);       
            renderPassInfo.pClearValues(clearValues);
            renderPassInfo.framebuffer(hFrameBufferHandle);

            //  TODO - VK_SUBPASS_CONTENTS_INLINE  OR VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS
            vkCmdBeginRenderPass(commandBuffer.GetVKCommandBuffer(), renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            //commandBuffer.beginRenderPassCmd();

            // Inheritance info for the secondary command buffers (same for all!)
            VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack);
            inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
            inheritanceInfo.pNext(0);
            inheritanceInfo.framebuffer(hFrameBufferHandle);
            inheritanceInfo.renderPass(cvkSwapChain.GetOffscreenRenderPassHandle());
            //inheritanceInfo.subpass(0); // TODO Get the subpass or make it here?
            inheritanceInfo.occlusionQueryEnable(false);
            inheritanceInfo.queryFlags(0);
            inheritanceInfo.pipelineStatistics(0);

	    // TODO Is this needed or set in RecordHitTestCommandBuffer() ?
            // Set the dynamic viewport and scissor
            //commandBuffer.viewPortCmd(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight(), stack);
            //commandBuffer.scissorCmd(cvkDevice.GetCurrentSurfaceExtent(), stack);
            
            // Check flags and render the nodes and connections
            /// Loop through command buffers of hit test objects and record their buffers
            hitTestRenderables.forEach(renderable -> {
            // TODO HYDRA - WIP HIT TESTER
//                if (renderable.GetVertexCount() > 0) {
//                    renderable.RecordHitTestCommandBuffer(inheritanceInfo, 0);
//                    vkCmdExecuteCommands(commandBuffer.GetVKCommandBuffer(), renderable.GetCommandBuffer(0));
//                }
            });
            
            vkCmdEndRenderPass(commandBuffer.GetVKCommandBuffer());
        
            // Pre Draw Barrier
            commandBuffer.pipelineImageMemoryBarrierCmd(cvkImage.GetImageHandle(), 
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_GENERAL,//VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL,    // Old/New Layout
                    0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,                                // Src/Dst Access mask
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, //VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,  // Src/Dst Stage mask
                    0, 1);                                                                  // baseMipLevel/mipLevelCount
            
            // TODO Do we need this?
            vkDeviceWaitIdle(cvkDevice.GetDevice());
            commandBuffer.EndAndSubmit();
            
            // TODO - COMPLETE ME       
            //MapMemory();
        }
              
        return ret;
    }    
    
     
    private int MapMemory() {     
        int ret = VK_SUCCESS;
        
        // Test code to write out the image to file before we just map to memory and do the hit
        String fileName = "screenshot.png";       
        ret = cvkImage.SaveToFile(fileName);

        return ret;
    }
        

    // ========================> Tasks <======================== \\
    
    public void queueRequest(final HitTestRequest request) {
        requestQueue.add(request);
        needsDisplayUpdate = true;
    }
}
