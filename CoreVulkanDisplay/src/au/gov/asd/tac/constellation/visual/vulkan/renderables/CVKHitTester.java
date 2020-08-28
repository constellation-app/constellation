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
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKImage;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_HITTEST_DEPTH_IMAGE_CREATE_FAILED;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_HITTEST_SOURCE_IMAGE_CREATE_FAILED;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.GetParentMethodName;
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
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32_SFLOAT;
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
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCmdExecuteCommands;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;


/**
 * This lives in the Core Interactive Graph project so it can import HitTestRequest and
 * HitState.  If it lived in Core Display Vulkan importing those types would require
 * Core Display Vulkan to be dependent on Core Interactive Graph which would be a 
 * circular dependency.
 * 
 * 
 * The Hit Tester performs a second offscreen render pass with objects that need
 * to be tested against mouse clicks/mouse overs (e.g. Icons, Connections).
 * However the objects are each drawn with a unique color determined in the shader
 * by the node id.
 * <p>
 * Whenever the mouse is moved, read the current pixel from the alternate
 * image and convert the unique color back to the node id.
 * <p>
 * It is assumed that node ids are &gt;=0. Since a black background would return
 * 0, we add 1 to the node id in the shader, and subtract 1 here. Change this
 * for a non-black background.
 * <p>
 * The alternate framebuffer is currently R32F format. This gives 22 bits of
 * mantissa, or 4,194,304 ids. Using the sign bit gives another 22 bits. We use
 * positive numbers for node ids, negative numbers for line ids.
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
    private final int colorFormat = VK_FORMAT_R32_SFLOAT;   // Only use the red channel for hit testing
    
    private CVKRenderableResourceState commandBuffersState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState frameBuffersState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState imagesState = CVK_RESOURCE_CLEAN;
    
    
    // ========================> Debuggering <======================== \\
    
    static int counter = 0;
    private void SaveToFile() {
        counter++;
        
        if (counter % 100 == 0 ) {
            // Debug code to write out the offscreen hittester image to file
            String fileName = String.format("C:\\OffscreenRender_%d.png", counter);       
            cvkImage.SaveToFile(fileName);
        }
    }
    
       
    private static boolean LOGSTATECHANGE = false;
    private void SetCommandBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t commandBuffersState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), commandBuffersState.name(), state.name(), GetParentMethodName());
        }
        commandBuffersState = state;
    }
    private void SetFrameBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(frameBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t frameBuffersState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), frameBuffersState.name(), state.name(), GetParentMethodName());
        }
        frameBuffersState = state;
    }
    private void SetImagesState(final CVKRenderableResourceState state) {
        CVKAssert(!(imagesState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t imagesState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), imagesState.name(), state.name(), GetParentMethodName());
        }
        imagesState = state;
    }
    
    // ========================> Lifetime <======================== \\
    
    public CVKHitTester(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
    }
    
    @Override
    public int Initialise() { 
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
    protected int DestroySwapChainResources() { 
         this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (swapChainImageCountChanged) {  
            DestroyCommandBuffer();     
            DestroyImage();
            DestroyFrameBuffer();
        }
        
        return VK_SUCCESS; 
    }
    
    @Override
    public int SetNewSwapChain(CVKSwapChain newSwapChain) {
        int ret = super.SetNewSwapChain(newSwapChain);
        if (VkFailed(ret)) { return ret; }
        
        if (swapChainImageCountChanged) {
            // The number of images has changed, we need to rebuild all image
            // buffered resources
            SetCommandBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetImagesState(CVK_RESOURCE_NEEDS_REBUILD);
            SetFrameBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
        }
        
        return ret;
    } 
    
    
    // ========================> Image <======================== \\
    
    private int CreateImages() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(cvkSwapChain.GetDepthFormat() != VK_FORMAT_UNDEFINED);
        CVKAssertNull(cvkImage);
        CVKAssertNull(cvkDepthImage);
        
        int ret = VK_SUCCESS;   
        int textureWidth = cvkSwapChain.GetWidth();
        int textureHeight = cvkSwapChain.GetHeight();
        int requiredLayers = 1;

        // Create destination color image to render to
        cvkImage = CVKImage.Create( textureWidth, 
                                    textureHeight, 
                                    requiredLayers, 
                                    colorFormat,            // R32 Float - we only use the Red channel for hit testing
                                    VK_IMAGE_VIEW_TYPE_2D,
                                    VK_IMAGE_TILING_LINEAR, // Linear Tiling so we can read it later
                                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT , // Host Visible so we can read the memory without transitioning
                                    VK_IMAGE_ASPECT_COLOR_BIT,
                                    GetLogger(),
                                    "CVKHitTester cvkImage");
        if (cvkImage == null) {
            return CVK_ERROR_HITTEST_SOURCE_IMAGE_CREATE_FAILED;
        }

        // Create depth image required for hittesting
        cvkDepthImage = CVKImage.Create(textureWidth, 
                                        textureHeight, 
                                        requiredLayers, 
                                        cvkSwapChain.GetDepthFormat(),
                                        VK_IMAGE_VIEW_TYPE_2D,
                                        VK_IMAGE_TILING_OPTIMAL,
                                        VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                                        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                        VK_IMAGE_ASPECT_DEPTH_BIT,
                                        GetLogger(),
                                        "CVKHitTester cvkDepthImage");  // TODO - aspect mask
        if (cvkDepthImage == null) {
            return CVK_ERROR_HITTEST_DEPTH_IMAGE_CREATE_FAILED;
        }
        
        SetImagesState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }
    
    private void DestroyImage() {
        if (cvkImage != null) {
            cvkImage.Destroy();
            cvkImage = null;
        }
        
        if (cvkDepthImage != null) {
            cvkDepthImage.Destroy();
            cvkDepthImage = null;
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
            
            ret = vkCreateFramebuffer(CVKDevice.GetVkDevice(), 
                                      framebufferInfo, 
                                      null, //allocation callbacks
                                      pFramebuffer);
            if (VkFailed(ret)) { return ret; }
            
            hFrameBufferHandle = pFramebuffer.get(0);
            
            SetFrameBuffersState(CVK_RESOURCE_CLEAN);

        }
        return ret;

    }
    
    private void DestroyFrameBuffer() {
        if (hFrameBufferHandle != null) {
            vkDestroyFramebuffer(CVKDevice.GetVkDevice(), hFrameBufferHandle, null);
            hFrameBufferHandle = null;
            GetLogger().info("Destroyed frame buffer for HitTester");
        }
    }  
    
    
    // ========================> Vertex buffers <======================== \\    
    
    @Override
    public int GetVertexCount() { return 0; }
    
    
    // ========================> Command buffers <======================== \\
    
    private int CreateCommandBuffer() {       
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNull(commandBuffer);
        
        int ret = VK_SUCCESS;
             
        commandBuffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_PRIMARY, GetLogger(), "CVKHitTester CommandBuffer");
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        GetLogger().info("Init Command Buffer - HitTester");
        
        return ret;
    }
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) { return commandBuffer.GetVKCommandBuffer(); }
    
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
        return needsDisplayUpdate ||
                imagesState != CVK_RESOURCE_CLEAN ||
                frameBuffersState != CVK_RESOURCE_CLEAN ||
                commandBuffersState != CVK_RESOURCE_CLEAN; 
    }
     
    @Override
    public int DisplayUpdate() {
        int ret = VK_SUCCESS;
                
        if (imagesState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = CreateImages();
            if (VkFailed(ret)) { return ret; }  
        }
        if (commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = CreateCommandBuffer();
            if (VkFailed(ret)) { return ret; }  
        }
        if (frameBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
            ret = CreateFrameBuffer();
            if (VkFailed(ret)) { return ret; }  
        }
                      
        if (requestQueue != null && !requestQueue.isEmpty()) {
            requestQueue.forEach(request -> notificationQueues.add(request.getNotificationQueue()));
            hitTestRequest = requestQueue.getLast();
            requestQueue.clear();
        }
        
        if (!notificationQueues.isEmpty()) {
            final int x = hitTestRequest.getX();
            final int y = hitTestRequest.getY();
            int redPixel = 0;

            if (cvkImage.GetLayout() != VK_IMAGE_LAYOUT_UNDEFINED) {
                redPixel = cvkImage.ReadPixel(x, y);
            }

            final int id;
            final HitType currentHitType;
            if (redPixel == 0) {
                currentHitType = HitType.NO_ELEMENT;
                id = -1;
            } else {
                currentHitType = redPixel > 0 ? HitType.VERTEX : HitType.TRANSACTION;
                id = redPixel > 0 ? redPixel - 1 : -redPixel - 1;
            }
             
            final HitState hitState = hitTestRequest.getHitState();
            hitState.setCurrentHitId(id);
            hitState.setCurrentHitType(currentHitType);
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
        
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(CVKDevice.GetCommandPoolHandle());
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        if (hitTestRenderables.isEmpty()) {
            return ret;
        }

        try (MemoryStack stack = stackPush()) {
            
            ret = commandBuffer.Begin(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            if (VkFailed(ret)) { return ret; }
                       
            // Pre Draw Barrier
            commandBuffer.PipelineImageMemoryBarrier(cvkImage.GetImageHandle(), 
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,    // Old/New Layout
                    0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,                                // Src/Dst Access mask
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,  // Src/Dst Stage mask
                    0, 1);                                                                  // baseMipLevel/mipLevelCount
            
            commandBuffer.BeginRenderPass(cvkSwapChain.GetOffscreenRenderPassHandle(),
                    hFrameBufferHandle, cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight(), 
                    1, 1, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

            // Inheritance info for the secondary command buffers (same for all!)
            VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack);
            inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
            inheritanceInfo.pNext(0);
            inheritanceInfo.framebuffer(hFrameBufferHandle);
            inheritanceInfo.renderPass(cvkSwapChain.GetOffscreenRenderPassHandle());
            inheritanceInfo.occlusionQueryEnable(false);
            inheritanceInfo.queryFlags(0);
            inheritanceInfo.pipelineStatistics(0);
            
            // Loop through command buffers of hit test objects and record their buffers
            hitTestRenderables.forEach(renderable -> {
                if (renderable.GetVertexCount() > 0) {
                    renderable.RecordHitTestCommandBuffer(inheritanceInfo, 0);
                    vkCmdExecuteCommands(commandBuffer.GetVKCommandBuffer(), renderable.GetHitTestCommandBuffer(0));
                }
            });
            
            commandBuffer.EndRenderPass();
        
            // Pre Draw Barrier
            commandBuffer.PipelineImageMemoryBarrier(cvkImage.GetImageHandle(), 
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_GENERAL,                                 // Old/New Layout
                    0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,                                            // Src/Dst Access mask
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,  // Src/Dst Stage mask
                    0, 1);                                                                              // baseMipLevel/mipLevelCount
            
            cvkImage.SetLayout(VK_IMAGE_LAYOUT_GENERAL);
            commandBuffer.EndAndSubmit();
        }
              
        return ret;
    }
        

    // ========================> Tasks <======================== \\
    
    public void queueRequest(final HitTestRequest request) {
        requestQueue.add(request);
        needsDisplayUpdate = true;
    }
    
    
    // ========================> Helpers <======================== \\

}
