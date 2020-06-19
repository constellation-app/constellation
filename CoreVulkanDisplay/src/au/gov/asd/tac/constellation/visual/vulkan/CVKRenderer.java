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

import au.gov.asd.tac.constellation.visual.Renderer;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.EndLogSection;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.GetRequiredVKPhysicalDeviceExtensions;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.InitVKValidationLayers;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.StartLogSection;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import java.awt.event.ComponentListener;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkCommandBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.UINT64_MAX;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import java.awt.event.ComponentEvent;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;


/*
NAMING CONVENTION

BLAH: static final that can be accessed from either static or instanced code
CVKBlah: a Constellation class in the au.gov.asd.tac.constellation.visual.vulkan package
cvkBlah: instance of CVKBlah class
VkBlah: Blah is LWJGL-Vulkan class, a Java object wrapping a native handle to Vulkan object
vkBlah: instance of a VkBlah class
hBlah: a handle to some object.  Treat as constant after first initialised.
pBlah: this is an object that has been explicitly malloced and needs to be explicitly released (unless
       a stack allocator was used to allocate it).
blah: anything else

*/

public class CVKRenderer extends Renderer implements ComponentListener {
    public interface CVKRenderEventListener {
        //TODO_TT: how do we handle errors from this?
        public void SwapChainRezied(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain);
        public void DeviceInitialised(CVKDevice cvkDevice);
    }
    
    
    // TODO_TT: explain why this may be less than imageCount
    protected static final int MAX_FRAMES_IN_FLIGHT = 2;
    
    protected CVKInstance cvkInstance = null;
    protected CVKDevice cvkDevice = null;
    protected CVKSwapChain cvkSwapChain = null;
    protected int currentFrame = 0;
    protected List<CVKFrame> cvkFrames = null;
           
    protected boolean swapChainNeedsRecreation = true;
    protected static boolean debugging = true;
    protected int frameNumber = 0;
    private final CVKVisualProcessor parent;  
    
    protected List<CVKRenderEventListener> renderEventListeners = new ArrayList<>();

    // hack - replace with a getRenderables function from the scene
    public List<CVKRenderable> renderables = new ArrayList<>();
    
    private static float red = 0.0f;
    private static float green = 0.0f;
    private static float blue = 0.0f;
    
    
    public void AddRenderable(CVKRenderable renderable) {
        renderables.add(renderable);
    }
    
    public void AddRenderEventListener(CVKRenderEventListener e) {
        renderEventListeners.add(e);
    }

    /**
     *
     *
     *
     * @param surfaceHandle
     * @return 
     * @see
     * <a href="https://renderdoc.org/vulkan-in-30-minutes.html">https://renderdoc.org/vulkan-in-30-minutes.html</a>      *
     */
    public int Init(long surfaceHandle) {
        cvkDevice = new CVKDevice(cvkInstance, surfaceHandle);
        int ret = cvkDevice.Init();
        
        if (VkSucceeded(ret)) {
            renderEventListeners.forEach(listener -> {
                listener.DeviceInitialised(cvkDevice);
            });
        }
        
        return ret;
    }
    
    
    public int RecreateSwapChain() {
//        if (!Thread.currentThread().getName().contains("AWT")) {
//            CVKLOGGER.log(Level.INFO, "{0}: RecreateSwapChain (releasing frames)", 
//                    Thread.currentThread().getName());
//            LogStackTrace();            
//        }
        
        int ret = VK_NOT_READY;
        if (parent.surfaceReady()) {
            cvkDevice.WaitIdle();
            CVKSwapChain newSwapChain = new CVKSwapChain(cvkDevice);                                 
            ret = newSwapChain.Init();
            if (VkSucceeded(ret)) {
                if (cvkSwapChain != null) {
                    cvkSwapChain.Deinit();
                }
                cvkSwapChain = newSwapChain;
                swapChainNeedsRecreation = false;
                
                if (cvkFrames != null) {
                    cvkFrames.forEach(frame -> {
                        frame.Deinit();
                    });
                }
                cvkFrames = new ArrayList<>(cvkSwapChain.GetImageCount());
                for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {
                    cvkFrames.add(new CVKFrame(cvkDevice.GetDevice()));
                }
                
                renderEventListeners.forEach(listener -> {
                    listener.SwapChainRezied(cvkDevice, cvkSwapChain);
                });
                
                // Now rebuild the command buffer with all the objects
                ret = cvkSwapChain.BuildCommandBuffers(renderables);
                checkVKret(ret);
            }
        } else {
            CVKLOGGER.info("Unable to recreate swap chain, surface not ready.");
        }
        return ret;
    }

    /**
     *
     * @param parent
     * @throws Exception
     */
    public CVKRenderer(CVKVisualProcessor parent) throws Exception {
        StartLogSection("VKRenderer ctor");
        this.parent = parent;
        try (MemoryStack stack = stackPush()) {            
            PointerBuffer pbValidationLayers = null;
            PointerBuffer pbExtensions = GetRequiredVKPhysicalDeviceExtensions(stack);
            if (debugging) {
                pbValidationLayers = InitVKValidationLayers(stack);
            }
            cvkInstance = new CVKInstance();
            checkVKret(cvkInstance.Init(stack, pbExtensions, pbValidationLayers, debugging));         
        }
        EndLogSection("VKRenderer ctor");
    }

    @Override
    public void finalize() throws Throwable {
        cvkInstance.Deinit();
        cvkInstance = null;
        
    }
    
    /**
     * 
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param frame
     * @param pImageIndex
     * @return
     */
    protected int AcquireImageFromSwapchain(CVKFrame frame, IntBuffer pImageIndex) {
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkSwapChain.GetSwapChainHandle() != VK_NULL_HANDLE);
        CVKAssert(frame.GetImageAcquireSemaphoreHandle() != VK_NULL_HANDLE);
        CVKAssert(pImageIndex != null);
        
        int ret;
        
        ret = frame.WaitResetRenderFence();
        if (VkFailed(ret)) return ret;
        
        ret = vkAcquireNextImageKHR(cvkDevice.GetDevice(),
                                    cvkSwapChain.GetSwapChainHandle(),
                                    UINT64_MAX,
                                    frame.GetImageAcquireSemaphoreHandle(),
                                    VK_NULL_HANDLE,
                                    pImageIndex);        
        
        return ret;        
    }
    
    /**
     * Records/updates the Primary Command Buffer and all its Secondary Command Buffers
     * 
     * @param stack
     * @param frame
     * @param commandBuffer
     * @return 
     */
    //protected int RecordCommandBuffer(MemoryStack stack, CVKFrame frame, VkCommandBuffer commandBuffer){
    protected int RecordCommandBuffer(MemoryStack stack, VkCommandBuffer commandBuffer){
        int ret = VK_SUCCESS;
        
        //cvkSwapChain.RecordCommandBuffer()
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassInfo.renderPass(cvkSwapChain.GetRenderPassHandle());

        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(cvkSwapChain.GetExtent());
        renderPassInfo.renderArea(renderArea);

        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.color().float32(stack.floats(red, green, blue, 1.0f));
        renderPassInfo.pClearValues(clearValues);

   //     for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {
    //        assert(cvkSwapChain.GetFrameBufferHandle(i) != VK_NULL_HANDLE);
            
   //         VkCommandBuffer commandBuffer = cvkSwapChain.GetCommandBuffer(i);

            checkVKret(vkBeginCommandBuffer(commandBuffer, beginInfo));
            renderPassInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(0));
            //vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
       
                    // Do we need to do a vkBeginCommandBuffer again here?
                    //checkVKret(vkBeginCommandBuffer(commandBuffer, beginInfo));
                    // Loop through renderables and record their buffers
                    for (int r = 0; r < renderables.size(); ++r) {
                        VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
				.pNext(0)
				.framebuffer(cvkSwapChain.GetFrameBufferHandle(0))
				.renderPass(cvkSwapChain.GetRenderPassHandle())
				.subpass(1) // Get the subpass of make it here?
				.occlusionQueryEnable(false)
				.queryFlags(0)
				.pipelineStatistics(0);
		
                        VkCommandBufferBeginInfo beginInfoSecondary = VkCommandBufferBeginInfo.calloc()
                                        .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                                        .pNext(0)
                                        .flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT)
                                        .pInheritanceInfo(inheritanceInfo);
                
                        VkCommandBuffer secondaryBuffer = renderables.get(r).GetCommandBuffer();
                        checkVKret(vkBeginCommandBuffer(secondaryBuffer, beginInfoSecondary));
                    
                            vkCmdBindPipeline(secondaryBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, renderables.get(r).GetGraphicsPipeline());
                            vkCmdDraw(secondaryBuffer, renderables.get(r).GetVertex(), 1, 0, 0);
                        
                        checkVKret(vkEndCommandBuffer(commandBuffer));
                    }
               
//                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
//                vkCmdDraw(commandBuffer, 3, 1, 0, 0);
         
            vkCmdEndRenderPass(commandBuffer);
            checkVKret(vkEndCommandBuffer(commandBuffer));
    //    }    
    
        Debug_UpdateRGB();
        
        return ret; 
    }
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param frame
     * @param commandBuffer
     * @return
     */
    protected int ExecuteCommandBuffer(MemoryStack stack, CVKFrame frame, VkCommandBuffer commandBuffer) {
        int ret;
        
        CVKAssert(frame != null);
        CVKAssert(commandBuffer != null);
        
        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.pCommandBuffers(stack.pointers(commandBuffer));
        submitInfo.pWaitSemaphores(stack.longs(frame.GetImageAcquireSemaphoreHandle()));
        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
        submitInfo.pSignalSemaphores(stack.longs(frame.GetRenderFinishedSemaphoreHandle()));   
        
        ret = vkQueueSubmit(cvkDevice.GetQueue(), 
                            submitInfo, 
                            frame.GetRenderFence());
        
        return ret;        
    }
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param frame
     * @param imageIndex
     * @return
     */
    protected int ReturnImageToSwapchainAndPresent(MemoryStack stack, CVKFrame frame, int imageIndex) {
        int ret;
        
        CVKAssert(cvkDevice.GetQueue() != null);
        
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.pWaitSemaphores(stack.longs(frame.GetRenderFinishedSemaphoreHandle()));
        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(cvkSwapChain.GetSwapChainHandle()));
        presentInfo.pImageIndices(stack.ints(imageIndex));
        
        ret = vkQueuePresentKHR(cvkDevice.GetQueue(), presentInfo);
        
        return ret;
    }

    public void Display() {
        //CVKLOGGER.log(Level.INFO, Thread.currentThread().getName());
        
        // Update everything that needs updating - drawables   
        // Check if needs updating flag?
        //renderables.get(0).RecordCommandBuffer(cvkDevice, cvkSwapChain);
        
//                });
        
        
        parent.signalUpdateComplete();
        
        // If the surface is not ready RecreateSwapChain won't have reset this flag
        int ret;
        if (cvkSwapChain != null && !swapChainNeedsRecreation) {
                    
            try (MemoryStack stack = stackPush()) {
                
                
                IntBuffer pImageIndex = stack.mallocInt(1);
                CVKAssert(currentFrame < cvkFrames.size());
                CVKFrame frame = cvkFrames.get(currentFrame);
                ret = AcquireImageFromSwapchain(frame, pImageIndex);
                if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                 || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                    swapChainNeedsRecreation = true;
                } else {
                    checkVKret(ret);
                    
                    int imageIndex = pImageIndex.get(0);
     //                CVKLOGGER.log(Level.INFO, "Displaying image {0}", imageIndex);
                    
                    //RecordCommandBuffer(stack, cvkSwapChain.GetCommandBuffer(imageIndex));
     
                    ret = ExecuteCommandBuffer(stack, 
                                               frame, 
                                               cvkSwapChain.GetCommandBuffer(imageIndex));
                    checkVKret(ret);
                    
//                    ret = ExecuteCommandBuffer(stack, 
//                                              frame, 
//                                              renderables.get(0).GetCommandBuffer());
//                        
//                    checkVKret(ret);
                      
                     ret = ReturnImageToSwapchainAndPresent(stack,
                                                            frame,
                                                            imageIndex);
                     if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                      || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                         swapChainNeedsRecreation = true;
                     } else {
                         checkVKret(ret);
                     }

                     
//                     if (renderables.get(0).GetCommandBuffer() != null){
//                        int imageIndex2 = pImageIndex.get(0);
//                        ret = ExecuteCommandBuffer(stack, 
//                                              frame, 
//                                              renderables.get(0).GetCommandBuffer());
//                        
//                        checkVKret(ret);
//                        
//                        ret = ReturnImageToSwapchainAndPresent(stack,
//                                                            frame,
//                                                            imageIndex2);
//                    } 
//                     
                     
                     // Move the frame index to the next cab off the rank
                     currentFrame = (++currentFrame) % MAX_FRAMES_IN_FLIGHT;

                     // Display all the drawables/renderables
     //                CVKLOGGER.log(Level.INFO, "Frame {0}", frameNumber++);                    
                }
            }
        }
        
        // Explicit waiting on semaphores was introduced in Vulkan 1.2.  It's
        // simpler to recreate the swap chain at the end of the display as the
        // render fence has been the reset and the semaphores will be in the 
        // signalled state, so we just destroy them without waiting.
        if (cvkSwapChain == null || swapChainNeedsRecreation) {
            RecreateSwapChain();
        }        
        
        // hack for constant render loop
        if (debugging) {
            parent.rebuild();
        } 
        parent.signalProcessorIdle();
    }

    
    int[] getViewport() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public VkInstance GetVkInstance() {
        return cvkInstance.GetInstance();
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        swapChainNeedsRecreation = true;
        CVKLOGGER.info("Canvas sent componentResized");
    }
    @Override
    public void componentHidden(ComponentEvent e) {
        CVKLOGGER.info("Canvas sent ");
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        CVKLOGGER.info("Canvas moved");     
    }
    @Override
    public void componentShown(ComponentEvent e) {
        CVKLOGGER.info("Canvas shown");
    }    
    
    private void Debug_UpdateRGB(){
        if(red < 1.f)
            red +=0.01;
        else if(green < 1.f)
            green +=0.01;
        else if(blue < 1.f)
            blue +=0.01;
        else
        {
            red = 0.f;
            green = 0.f;
            blue = 0.f;
        }
        
    }
}