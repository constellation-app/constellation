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

import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.EndLogSection;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.GetRequiredVKPhysicalDeviceExtensions;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.InitVKValidationLayers;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.LOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.StartLogSection;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPresentInfoKHR;
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

public class CVKRenderer implements ComponentListener{
    // TODO_TT: explain why this may be less than imageCount
    protected static final int MAX_FRAMES_IN_FLIGHT = 2;
    
    protected CVKInstance cvkInstance = null;
    protected CVKDevice cvkDevice = null;
    protected CVKSwapChain cvkSwapChain = null;
    
    
    // TODO_TT: find out if this should be imageCount
    //protected IntBuffer pCurrentFrame = memAllocInt(1).put(0, 0);
    protected int currentFrame = 0;
    protected List<CVKFrame> cvkFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
       
    
    protected boolean swapChainNeedsRecreation = true;
    protected static boolean debugging = true;
    protected int frameNumber = 0;
    private final CVKVisualProcessor parent;  


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
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; ++i) {
            cvkFrames.add(new CVKFrame(cvkDevice.GetDevice()));
        }
        return ret;
    }
    
    
    public int RecreateSwapChain() {
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
            }
        } else {
            LOGGER.info("Unable to recreate swap chain, surface not ready.");
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
        assert(cvkDevice.GetDevice() != null);
        assert(cvkSwapChain.GetSwapChainHandle() != VK_NULL_HANDLE);
        assert(frame.GetImageAcquireSemaphoreHandle() != VK_NULL_HANDLE);
        assert(pImageIndex != null);
        
        int ret;
        
        ret = frame.WaitResetRenderFence();
        if (VkFailed(ret)) return ret;
        
        ret = vkAcquireNextImageKHR(cvkDevice.GetDevice(),
                                    cvkSwapChain.GetSwapChainHandle(),
                                    -1L,
                                    frame.GetImageAcquireSemaphoreHandle(),
                                    VK_NULL_HANDLE,
                                    pImageIndex);        
        
        return ret;        
    }
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param frame
     * @param commandBuffer
     * @param commandBufferHandle
     * @return
     */
    protected int ExecuteCommandBuffer(MemoryStack stack, CVKFrame frame, VkCommandBuffer commandBuffer) {
        int ret;
        
        assert(frame != null);
        assert(commandBuffer != null);
        
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
        
        assert(cvkDevice.GetQueue() != null);
        
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.pWaitSemaphores(stack.longs(frame.GetRenderFinishedSemaphoreHandle()));
        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(cvkSwapChain.GetSwapChainHandle()));
        presentInfo.pImageIndices(stack.ints(imageIndex));
        
        ret = vkQueuePresentKHR(cvkDevice.GetQueue(), presentInfo);
        
        return ret;
    }

    public void Display() {
        // If there are issues with missing resizes we could just test to see
        // if the size has changed between frames.
        if (swapChainNeedsRecreation) {
            RecreateSwapChain();
        }
        
        // Update everything that needs updating - drawables
        
        
        parent.signalUpdateComplete();
        
        // If the surface is not ready RecreateSwapChain won't have reset this flag
        int ret;
        if (!swapChainNeedsRecreation) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer pImageIndex = stack.mallocInt(1);
                CVKFrame frame = cvkFrames.get(currentFrame);
                ret = AcquireImageFromSwapchain(frame, pImageIndex);
                checkVKret(ret);
                int imageIndex = pImageIndex.get(0);
                LOGGER.log(Level.INFO, "Displaying image {0}", imageIndex);
                
                ret = ExecuteCommandBuffer(stack, 
                                           frame, 
                                           cvkSwapChain.GetCommandBuffer(imageIndex));
                checkVKret(ret);
                
                ret = ReturnImageToSwapchainAndPresent(stack,
                                                       frame,
                                                       imageIndex);
                checkVKret(ret);

                // Move the frame index to the next cab off the rank
                currentFrame = (++currentFrame) % MAX_FRAMES_IN_FLIGHT;
       
                // Display all the drawables/renderables
                LOGGER.log(Level.INFO, "Frame {0}", frameNumber++);
            }
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
        LOGGER.info("Canvas resized");
    }
    @Override
    public void componentHidden(ComponentEvent e) {
        LOGGER.info("Canvas hidden");
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        LOGGER.info("Canvas moved");     
    }
    @Override
    public void componentShown(ComponentEvent e) {
        LOGGER.info("Canvas shown");
    }    
}
