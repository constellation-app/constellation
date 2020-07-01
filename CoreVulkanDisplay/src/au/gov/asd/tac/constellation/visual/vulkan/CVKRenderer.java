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

import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
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
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.UINT64_MAX;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VerifyInRenderThread;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;


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

CODING STANDARDS
helper classes should return error codes where possible
controller classes like CVKRenderer and aggregation classes like CVKScene will need some other mechanism
use MemoryStack to allocate nio buffers unless an allocation needs to last longer than function scope
comment where ever code is not trivially simple
make methods static whereever possible
TODO_TT: remember the reason for the Create factory patten in buffer and image

*/

public class CVKRenderer extends Renderer implements ComponentListener {
    public interface CVKRenderEventListener {
        //TODO_TT: how do we handle errors from this?
        public abstract void SwapChainRecreated(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain);
        public abstract void DeviceInitialised(CVKDevice cvkDevice);
        public abstract void DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex);
        
        //TEMP TEMP TEMP
        public abstract void Display(MemoryStack stack, CVKFrame frame, CVKRenderer cvkRenderer, CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex);
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
    private final CVKVisualProcessor parent;  
    
    // Descriptor pools are owned by the swapchain but because the lifetime of
    // the swapchain is transient we store these counts in the renderer so that
    // we can account for our scene being populated before we've created the swapchain.    
    protected CVKSynchronizedDescriptorTypeCounts desiredPoolDescriptorTypeCounts = new CVKSynchronizedDescriptorTypeCounts();   
    
    
//    // What is the minimum size the pool needs to accomodate?
//    protected int desiredPoolDescriptorTypeCounts[] = null;    
          
    
    protected List<CVKRenderEventListener> renderEventListeners = new ArrayList<>();     

    // hack - replace with a getRenderables function from the scene
    public List<CVKRenderable> renderables = new ArrayList<>();
    protected CVKScene scene = null;
    
    private static float clrChange = 0.01f;
    private static int curClrEl = 0;
    private static Vector3f clr = new Vector3f(0.0f, 0.0f, 0.0f);
    
    
    public void AddRenderable(CVKRenderable renderable) {
        renderables.add(renderable);
    }
    
    public void AddRenderEventListener(CVKRenderEventListener e) {
        renderEventListeners.add(e);
    }
    
    public CVKDevice GetDevice() {
        return cvkDevice;
    }
    
    /**
     * Render tasks can be created in response to use input or other events.  They 
     * are then actioned in the render thread to ensure that only the render thread
     * modifies rendering resources (so we don't try modifying vertex buffers or 
     * other data currently being accessed by the display driver).
     */
    @FunctionalInterface
    public static interface CVKRendererUpdateTask {
        public void Run();
    }
    protected BlockingQueue<CVKRendererUpdateTask> pendingUpdates = new LinkedBlockingQueue<>();

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
    
    
    protected int RecreateSwapChain() {
//        if (!Thread.currentThread().getName().contains("AWT")) {
//            CVKLOGGER.log(Level.INFO, "{0}: RecreateSwapChain (releasing frames)", 
//                    Thread.currentThread().getName());
//            LogStackTrace();            
//        }
        
        int ret = VK_NOT_READY;
        if (parent.surfaceReady()) {
            cvkDevice.WaitIdle();
            CVKSwapChain newSwapChain = new CVKSwapChain(cvkDevice, this);                                 
            ret = newSwapChain.Init(desiredPoolDescriptorTypeCounts);
            desiredPoolDescriptorTypeCounts.ResetDirty();
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
                    listener.SwapChainRecreated(cvkDevice, cvkSwapChain);
                });
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
     * @param primaryCommandBuffer
     * @param index - index to get the current frame/image/command buffer
     * @return 
     */
    protected int RecordCommandBuffer(MemoryStack stack, CVKFrame frame, VkCommandBuffer primaryCommandBuffer, int index){
        VerifyInRenderThread();
        assert(cvkSwapChain.GetFrameBufferHandle(index) != VK_NULL_HANDLE);
    
        int ret = VK_SUCCESS;
           
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
        clearValues.color().float32(stack.floats(clr.getR(), clr.getG(), clr.getB(), 1.0f));
        
        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassInfo.renderPass(cvkSwapChain.GetRenderPassHandle());

        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(cvkSwapChain.GetExtent());
        renderPassInfo.renderArea(renderArea);       
        renderPassInfo.pClearValues(clearValues);
        renderPassInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(index));

        // The primary command buffer does not contain any rendering commands
	// These are stored (and retrieved) from the secondary command buffers
        checkVKret(vkBeginCommandBuffer(primaryCommandBuffer, beginInfo));
        
        //vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
        vkCmdBeginRenderPass(primaryCommandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

        // Inheritance info for the secondary command buffers (same for all!)
        VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc();
        inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
        inheritanceInfo.pNext(0);
        inheritanceInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(index));
        inheritanceInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
        inheritanceInfo.subpass(0); // Get the subpass of make it here?
        inheritanceInfo.occlusionQueryEnable(false);
        inheritanceInfo.queryFlags(0);
        inheritanceInfo.pipelineStatistics(0);
            
        // Loop through renderables and record their buffers
        for (int r = 0; r < renderables.size(); ++r) {

            //vkWaitForFences(cvkDevice.GetDevice(), frame.GetRenderFence(), true, UINT64_MAX);
            if (renderables.get(r).IsDirty()){
                renderables.get(r).RecordCommandBuffer(cvkDevice, cvkSwapChain, inheritanceInfo, index);

                // TODO Hydra: may be more efficient to add all the visible command buffers to a master list then 
                // call the following line once with the whole list
                vkCmdExecuteCommands(primaryCommandBuffer, renderables.get(r).GetCommandBuffer(index));
            }
            //vkResetFences(cvkDevice.GetDevice(), frame.GetRenderFence());
        }
        
        vkCmdEndRenderPass(primaryCommandBuffer);
        checkVKret(vkEndCommandBuffer(primaryCommandBuffer)); 
    
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
    public int ExecuteCommandBuffer(MemoryStack stack, CVKFrame frame, VkCommandBuffer commandBuffer) {
        int ret;
        
        CVKAssert(frame != null);
        CVKAssert(commandBuffer != null);
        
        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
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
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(stack.longs(frame.GetRenderFinishedSemaphoreHandle()));
        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(cvkSwapChain.GetSwapChainHandle()));
        presentInfo.pImageIndices(stack.ints(imageIndex));
        
        ret = vkQueuePresentKHR(cvkDevice.GetQueue(), presentInfo);
        
        return ret;
    }

    public void Display() {
        //CVKLOGGER.log(Level.INFO, Thread.currentThread().getName());
               
        //==================================
        // TODO_TT: currentFrame looks wrong, it should be set to the imageIndex
        // that is acquired and not incremented I think, the acquire should do 
        // the cycling.
        // Except...we wait on a renderfence to acquire the next image.  Needs
        // some thought.
        //==================================
        
        if (CVKUtils.renderThreadID != 0) {
            VerifyInRenderThread();
        } else {
            CVKUtils.renderThreadID = Thread.currentThread().getId();
        }
        
        
        // If the surface is not ready RecreateSwapChain won't have reset this flag
        int ret;
        if (cvkSwapChain != null && !swapChainNeedsRecreation) {
                    
            try (MemoryStack stack = stackPush()) {
                IntBuffer pImageIndex = stack.mallocInt(1);
                CVKAssert(currentFrame < cvkFrames.size());
                CVKFrame frame = cvkFrames.get(currentFrame);
  
                // Wait for fence to signal that all command buffers are ready
                //vkWaitForFences(cvkDevice.GetDevice(), frame.GetRenderFence(), true, UINT64_MAX);
                
                ret = AcquireImageFromSwapchain(frame, pImageIndex);
                if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                 || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                    swapChainNeedsRecreation = true;
                } else {
                    checkVKret(ret);
                    
                    int imageIndex = pImageIndex.get(0);
     //                CVKLOGGER.log(Level.INFO, "Displaying image {0}", imageIndex);
     
                    // TODO_TT: does this mean rebuilding the swapchain?
                    if (desiredPoolDescriptorTypeCounts.IsDirty()) {
                        cvkSwapChain.DescriptorTypeRequirementsUpdated(desiredPoolDescriptorTypeCounts);
                        desiredPoolDescriptorTypeCounts.ResetDirty();
                    }
                         

                    // Update everything that needs updating - drawables 
                    renderEventListeners.forEach(listener->{
                            listener.DisplayUpdate(cvkDevice, cvkSwapChain, imageIndex);
                        });
                    
                    // TODO ERROR needing to wait for fence here
                    RecordCommandBuffer(stack, frame, cvkSwapChain.GetCommandBuffer(imageIndex), imageIndex);
                                      
                    // TODO_TT: simplify queues, renderEventListeners and this could be merged
                    // Process updates queue by other threads
                    if (!pendingUpdates.isEmpty()) {
                        final List<CVKRendererUpdateTask> tasks = new ArrayList<>();
                        pendingUpdates.drainTo(tasks);
                        tasks.forEach(task -> {
                            task.Run();
                        });                        
                    }
                    
                    parent.signalUpdateComplete();     
                    
                    //renderEventListeners.forEach(listener->{
                    //       listener.Display(stack, frame, this, cvkDevice, cvkSwapChain, imageIndex);
                    //    });
                    
                    // Reset the fences
                    //vkResetFences(cvkDevice.GetDevice(), frame.GetRenderFence());
                    
                    ret = ExecuteCommandBuffer(stack, 
                               frame, 
                               cvkSwapChain.GetCommandBuffer(imageIndex));
                    checkVKret(ret);             
                    

                     ret = ReturnImageToSwapchainAndPresent(stack,
                                                            frame,
                                                            imageIndex);
                     if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                      || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                         swapChainNeedsRecreation = true;
                     } else {
                         checkVKret(ret);
                     }                    
        

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
    
    //TODO_TT: ask a more knowledgable Java dev what this should look like
//    protected class CVKUpdateDescriptorTypeRequirements implements CVKRendererUpdateTask {
//        protected final CVKScene cvkScene;
//        protected final boolean renderableAdded;
//        public CVKUpdateDescriptorTypeRequirements(CVKScene cvkScene, boolean renderableAdded) {
//            this.cvkScene = cvkScene;
//            this.renderableAdded = renderableAdded;
//        }
//        
//        @Override
//        public void Run() {
//            VerifyInRenderThread();
//            
//            // The addition of a new renderable may exceed our current descriptor pool's
//            // size.  Calculate the pool requirements and resize if necessary.
//            if (renderableAdded) {
//
//                // LWJGL doesn't reflect a lot of Vulkan enums instead exposing them as
//                // multiple static final ints.  VkDescriptorType ranges from 
//                // VK_DESCRIPTOR_TYPE_SAMPLER(0) to VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT(10)    
//
//                // Scene will ask each renderable to tell it about the descriptors it needs. 
//                descriptorTypeCounts = new int[11];
//                cvkScene.GetDescriptorTypeRequirements(descriptorTypeCounts);   
//
//                // SwapChain owns the descriptor pool so let it recreate it if needed
//                cvkSwapChain.DescriptorTypeRequirementsUpdated(descriptorTypeCounts);
//            }            
//        }
//    }
    
    public void RenderableAdded(CVKScene cvkScene, CVKRenderable renderable) {
        assert(cvkScene != null);
        
        int[] descriptorTypeCounts = new int[11];
        cvkScene.GetDescriptorTypeRequirements(descriptorTypeCounts);         
        desiredPoolDescriptorTypeCounts.Set(descriptorTypeCounts);

        scene = cvkScene;
        
        // TEMP TEMP TEMP
        renderables.add(renderable);
        
        
//        pendingUpdates.add(new CVKUpdateDescriptorTypeRequirements(cvkScene, renderableAdded));
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
        // When the current component reaches it's limit do something
        if (clr.a[curClrEl] >= 1.0f || clr.a[curClrEl] < 0.0f) {
            // Clamp it to 0-1
            clr.a[curClrEl] = Math.max(0.0f, Math.min(1.0f, clr.a[curClrEl]));
            // If we have hit the ceiling or floor on all components, change direction
            if (curClrEl == 2) {
                clrChange = -clrChange;
            }
            // Start changing the next component
            curClrEl = (curClrEl + 1) % 3;
        }
        // Walk the current component a little
        clr.a[curClrEl] += clrChange;            
    }
}
