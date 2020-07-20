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
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.debugging;
import java.nio.LongBuffer;
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
controller classes like CVKRenderer and aggregation classes like CVKVisualProcessor will need some other mechanism
use MemoryStack to allocate nio buffers unless an allocation needs to last longer than function scope
comment where ever code is not trivially simple
make methods static whereever possible
TODO_TT: remember the reason for the Create factory patten in buffer and image

*/

public class CVKRenderer implements ComponentListener {
    
    // TODO_TT: explain why this may be less than imageCount
    protected static final int MAX_FRAMES_IN_FLIGHT = 2;
    
    protected CVKInstance cvkInstance = null;
    protected CVKDevice cvkDevice = null;
    protected CVKSwapChain cvkSwapChain = null;
    public int currentFrame = 0;
           
    protected boolean swapChainNeedsRecreation = true;     
    private final CVKVisualProcessor parent;  
    
    // Descriptor pools are owned by the swapchain but because the lifetime of
    // the swapchain is transient we store these counts in the renderer so that
    // we can account for our scene being populated before we've created the swapchain.    
    protected CVKSynchronizedDescriptorTypeCounts desiredPoolDescriptorTypeCounts = new CVKSynchronizedDescriptorTypeCounts();   
       
    // Number of descriptor sets required
    protected int desiredPoolDescriptorSetCount = 0;
    
    public List<CVKRenderable> renderables = new ArrayList<>();
    
    private static float clrChange = 0.01f;
    private static int curClrEl = 0;
    private static Vector3f clr = new Vector3f(0.0f, 0.0f, 0.0f);
    
    
    public void AddRenderable(CVKRenderable renderable) {
        renderables.add(renderable);
        
        // Each renderable is responsible for telling the renderer what and how 
        // many Descriptor types it uses. This is calculated here each time a 
        // renderable is added.
        // The SwapChain will then create a Descriptor Pool based on these
        // descriptor counts to allocate the exact amount of memory required.
        //
        // If you get a Descriptor POOL_OUT_OF_MEMORY error, make sure you are 
        // returning the correct numbers for your Descriptor Types.
        // TODO_TT: this code sucks, make it not. Also change 11 to TOTAL_DESCRIPTOR_TYPES
        int[] descriptorTypeCounts = new int[11];
        int descriptorSetCount = 0;
        renderables.forEach(r -> {r.IncrementDescriptorTypeRequirements(descriptorTypeCounts, descriptorSetCount);});    
        desiredPoolDescriptorTypeCounts.Set(descriptorTypeCounts);
        desiredPoolDescriptorSetCount = descriptorSetCount;
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
     * This is called by the canvas once it has been created and has a valid surface handle.
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
        if (VkFailed(ret)) {
            return ret;
        }        
        
        ret = parent.DeviceInitialised(cvkDevice);
        if (VkFailed(ret)) {
            return ret;
        }
        
        for (int i = 0; VkSucceeded(ret) && (i < renderables.size()); ++i) {
            ret = renderables.get(i).DeviceInitialised(cvkDevice);
        if (VkFailed(ret)) {
            return ret;
        }            
        }
        
        return ret;
    }
       
    protected int RecreateSwapChain() {
        VerifyInRenderThread();
        
        int ret = VK_NOT_READY;
        if (parent.surfaceReady()) {
            cvkDevice.WaitIdle();
            CVKSwapChain newSwapChain = new CVKSwapChain(cvkDevice);                                 
            ret = newSwapChain.Init(desiredPoolDescriptorTypeCounts, desiredPoolDescriptorSetCount);
            desiredPoolDescriptorTypeCounts.ResetDirty();
            if (VkSucceeded(ret)) {
                if (cvkSwapChain != null) {
                    cvkSwapChain.Destroy();
                }
                cvkSwapChain = newSwapChain;
                swapChainNeedsRecreation = false;                
                
                // Update the parent (CVKVisualProcessor) so it can update the shared viewport and frustum
                parent.SwapChainRecreated(cvkDevice, cvkSwapChain);
                
                // Give each renderable a chance to recreate swapchain depedent resources
                for (int i = 0; VkSucceeded(ret) && (i < renderables.size()); ++i) {
                    ret = renderables.get(i).SwapChainRecreated(cvkSwapChain);
                }                
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

    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {
        cvkInstance.Deinit();
        cvkInstance = null;
        super.finalize();
    }
       
    /**
     * Records/updates the Primary Command Buffer and all its Secondary Command Buffers
     * 
     * @param stack
     * @param imageIndex - index to get the current frame/image/command buffer
     * @return 
     */
    protected int RecordCommandBuffer(MemoryStack stack, int imageIndex){
        VerifyInRenderThread();
        CVKAssert(cvkSwapChain.GetFrameBufferHandle(imageIndex) != VK_NULL_HANDLE);
    
        int ret = VK_SUCCESS;
        
        VkCommandBuffer primaryCommandBuffer = cvkSwapChain.GetCommandBuffer(imageIndex);
           
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
        clearValues.color().float32(stack.floats(clr.getR(), clr.getG(), clr.getB(), 1.0f));
        clearValues.get(1).depthStencil().set(1.0f, 0);
        
        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassInfo.renderPass(cvkSwapChain.GetRenderPassHandle());

        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(cvkSwapChain.GetExtent());
        renderPassInfo.renderArea(renderArea);       
        renderPassInfo.pClearValues(clearValues);
        renderPassInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(imageIndex));

        // The primary command buffer does not contain any rendering commands
	// These are stored (and retrieved) from the secondary command buffers
        checkVKret(vkBeginCommandBuffer(primaryCommandBuffer, beginInfo));
        
        //vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
        vkCmdBeginRenderPass(primaryCommandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

        // Inheritance info for the secondary command buffers (same for all!)
        VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc();
        inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
        inheritanceInfo.pNext(0);
        inheritanceInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(imageIndex));
        inheritanceInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
        inheritanceInfo.subpass(0); // Get the subpass of make it here?
        inheritanceInfo.occlusionQueryEnable(false);
        inheritanceInfo.queryFlags(0);
        inheritanceInfo.pipelineStatistics(0);
            
        // Loop through renderables and record their buffers
        for (int r = 0; r < renderables.size(); ++r) {
            if (renderables.get(r).IsDirty() && renderables.get(r).GetVertexCount() > 0){
                renderables.get(r).RecordCommandBuffer(cvkSwapChain, inheritanceInfo, imageIndex);

                // TODO Hydra: may be more efficient to add all the visible command buffers to a master list then 
                // call the following line once with the whole list
                vkCmdExecuteCommands(primaryCommandBuffer, renderables.get(r).GetCommandBuffer(imageIndex));
            }
        }
        
        vkCmdEndRenderPass(primaryCommandBuffer);
        checkVKret(vkEndCommandBuffer(primaryCommandBuffer)); 
    
        if (debugging) {
            Debug_UpdateRGB();
        }
        
        return ret; 
    }
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param pImageAcquiredSemaphore
     * @param pCommandBufferExecutedSemaphore
     * @param commandBuffer
     * @return
     */
    public int ExecuteCommandBuffer(MemoryStack stack, 
                                    LongBuffer pImageAcquiredSemaphore, 
                                    LongBuffer pCommandBufferExecutedSemaphore, 
                                    VkCommandBuffer commandBuffer,
                                    long hRenderFence) {
        CVKAssert(pImageAcquiredSemaphore != null && pImageAcquiredSemaphore.get(0) != VK_NULL_HANDLE);
        CVKAssert(pCommandBufferExecutedSemaphore != null && pCommandBufferExecutedSemaphore.get(0) != VK_NULL_HANDLE);
        CVKAssert(commandBuffer != null);
        
        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
        submitInfo.pCommandBuffers(stack.pointers(commandBuffer));
        submitInfo.pWaitSemaphores(pImageAcquiredSemaphore);
        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
        submitInfo.pSignalSemaphores(pCommandBufferExecutedSemaphore);   
        
        return vkQueueSubmit(cvkDevice.GetQueue(), 
                             submitInfo, 
                             hRenderFence);    
    }
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param pCommandExecutionSemaphore
     * @param imageIndex
     * @return
     */
    protected int ReturnImageToSwapchainAndPresent(MemoryStack stack, LongBuffer pCommandExecutionSemaphore, int imageIndex) {
        CVKAssert(cvkDevice.GetQueue() != null);
        
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(pCommandExecutionSemaphore);
        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(cvkSwapChain.GetSwapChainHandle()));
        presentInfo.pImageIndices(stack.ints(imageIndex));        
        return vkQueuePresentKHR(cvkDevice.GetQueue(), presentInfo);
    }


    /**
     * No parrellisation of CPU/GPU, eg only one swap chain image and set of command buffers
     * <table cellspacing="2" cellpadding="1" border="1" align="center">
     * <tr><td>CPU</td><td>Prepare image 0</td><td></td><td>Prepare image 0</td><td></td></tr>
     * <tr><td>GPU</td><td></td><td>Present image 0</td><td></td><td>Present image 0</td></tr>
     * </table>
     * <p>
     * 2 swapchain images with synchronisation of CPU preparation and GPU execution/presentation
     * <table cellspacing="2" cellpadding="1" border="1" align="center">
     * <tr><td>CPU</td><td>Prepare image 0</td><td>Prepare image 1</td><td>Prepare image 0</td><td>Prepare image 1</td></tr>
     * <tr><td>GPU</td><td>Present image 1</td><td>Present image 0</td><td>Present image 1</td><td>Present image 0</td></tr>
     * </table>
     * <p>
     * 
     * <h1>SYNCHRONISATION</h1>
     * There are several points that need to be synchronised:
     * <p>
     * 1. Acquiring the next available image from the swap chain.  The image the swap chain
     *    returns to us depends on the state of the images in flight and the policy used to
     *    create the swap chain.  See the initialisation of CVKDevice.selectedPresentationMode
     *    for more details.
     *    vkAcquireNextImageKHR will return immediately with the index of the image that will be
     *    acquired for rendering and presenting, but the image itself may not be ready yet to be
     *    rendered to or presented.  For that we need to provide either a fence or semaphore to
     *    vkAcquireNextImageKHR that will be signaled when the image is ready.  We can pass that
     *    semaphore into vkQueueSubmit when we submit our command buffers so the execution of
     *    commands waits until the image is ready.
     * <p>
     * 2. Presenting an image.  vkQueuePresentKHR adds an acquired image to the presentation queue
     *    which once drained unacquires the image and returns it to the swapchain.  Before we 
     *    enqueue an image into the presentation queue we need to ensure our rendering (execution
     *    of command buffers) has completed.
     * 
     * Frame 0:
     * 1. acquire image index 0  (get imageAcquisitionSemaphore 0)
     * 2. wait on that image 0 being ready
     * 3. execute command buffer 0 (return imageAcquisitionSemaphore 0 and get commandBufferExecutedSemaphore 0)
     * 4. wait for command buffer 0 to execute
     * 5. present image 0
     * 
     * !Problem: commandBufferExecutedSemaphore is never returned.  Maybe it never needs to be returned as we
     * have to wait for imageAcquisitionSemaphore 0 which implies commandBufferExecutedSemaphore 0 will have 
     * already been signaled.  If that is the case then we only need logic for the image acquisition semaphore.
     * We could bind them together, like the CVKFrame concept.
     * 
     * Frame 1:
     * 1. acquire image index 1
     * 2. wait on that image 1 being ready
     * 3. execute command buffer 1
     * 4. wait for command buffer 1 to execute
     * 5. present image 1
     * 
     * Frame 2
     * 
     * 0. wait on imageAcquisitionSemaphore 0, is this necessary?
     * 1. acquire image index 0 
     * 2. wait on that image 0 being ready
     * 3. execute command buffer 0
     * 4. wait for command buffer 0 to execute
     * 5. present image 0
     * 
     * Commands may be enqueued in order but without synchronisation they can be processed and
     * complete in any order.  We often require a particular state before an operation occurs,
     * for example we require an image is transitioned to the destination transfer optimal 
     * state before we copy data into it.  In order to enforce required states we use pipeline
     * barriers.  VkImageMemoryBarrier is the type of barrier we use in the previous example,
     * not only does this barrier block latter commands until our destination stage is reached,
     * it is also responsible for the transition from source to destination state.
     * 
     * Pipeline barriers (image transitions).  
     * 
     * 
     */
    public void Display() {
        int ret;
        
        // Sychronisation
        
        //
        
        // 1.  Acquire next available image from the swap chain.
        
        
        //CVKLOGGER.log(Level.INFO, Thread.currentThread().getName());
               
        // Our render thread should be the AWT thread that owns the canvas, whose
        // surface is our render target.  Being called by any other thread will 
        // lead to resource contention and deadlock (seen during development when
        // user events were handled immediately rather than enqueueing them for
        // the render thread to handle).
        if (debugging) {
            if (CVKUtils.renderThreadID != 0) {
                VerifyInRenderThread();
            } else {
                CVKUtils.renderThreadID = Thread.currentThread().getId();
            }
        }
        
        if (cvkSwapChain != null) {
            // Process updates enqueued by other threads
            if (!pendingUpdates.isEmpty()) {
                final List<CVKRendererUpdateTask> tasks = new ArrayList<>();
                pendingUpdates.drainTo(tasks);
                tasks.forEach(task -> {
                    task.Run();
                });                        
            }
            
            if (desiredPoolDescriptorTypeCounts.IsDirty()) {
                cvkSwapChain.UpdateDescriptorTypeRequirements(desiredPoolDescriptorTypeCounts,
                                                              desiredPoolDescriptorSetCount);
                desiredPoolDescriptorTypeCounts.ResetDirty();
            }            
            
            // If any renderable holds a resource shared across frames then to
            // recreate it we need to a complete halt, that is we need all in
            // flight images to have been presented and all fences available.
            // Note the one liner was created by Netbeans, I am not convinced it's
            // very clear.
            boolean updateSharedResources = false;
            for (int i = 0; (i < renderables.size()) && (updateSharedResources == false); ++ i) {
                updateSharedResources = renderables.get(i).SharedResourcesNeedUpdating();
            }
                      
            if (updateSharedResources) {
                cvkDevice.WaitIdle();
                for (int i = 0; i < renderables.size(); ++ i) {
                    CVKRenderable r = renderables.get(i);
                    if (r.SharedResourcesNeedUpdating()) {
                        ret = r.RecreateSharedResources(cvkSwapChain);
                        checkVKret(ret); 
                    }
                }
            }           
        }
   
        // If the surface is not ready RecreateSwapChain won't have reset this flag        
        if (cvkSwapChain != null && !swapChainNeedsRecreation) {
                    
            try (MemoryStack stack = stackPush()) {                
                // The swapchain decides which image we should render to based on the
                // mode we created with.
                IntBuffer pImageIndex = stack.mallocInt(1);
                LongBuffer pImageAcquisitionSemaphore = stack.mallocLong(1);
                ret = cvkSwapChain.AcquireNextImage(stack, pImageIndex, pImageAcquisitionSemaphore);
                if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                 || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                    swapChainNeedsRecreation = true;
                } else {
                    checkVKret(ret);                    
                    int imageIndex = pImageIndex.get(0);
                    
                    // The two semaphores tell us when an image is ready and when we've finished writing 
                    // to our command buffers, they don't tell us when the GPU is finished with the command
                    // buffers.  For that we need a fence, this is a synchronisation structure that is 
                    // device writable and host readable.
                    ret = cvkSwapChain.WaitOnFence(imageIndex);
                    checkVKret(ret); 
                              
                    // Update everything that needs updating - drawables 
                    for (int i = 0; VkSucceeded(ret) && (i < renderables.size()); ++i) {
                        ret = renderables.get(i).DisplayUpdate(cvkSwapChain, imageIndex);
                        checkVKret(ret); 
                    }                    
                    
                    // Record each renderables commands into secondary buffers and add them to the
                    // primary command buffer.
                    ret = RecordCommandBuffer(stack, imageIndex);
                    checkVKret(ret); 
                                                         
//                    parent.signalUpdateComplete();    
                    // This will wait for the image at imageIndex to be acquired then submit
                    // our primary command buffer to the execution queue.  Once executed 
                    // hCommandBufferExecutedSemaphore will be signaled and we can present.
                    LongBuffer pCommandExecutionSemaphore = stack.mallocLong(1);
                    ret = cvkSwapChain.GetCommandBufferExecutedSemaphore(imageIndex, pCommandExecutionSemaphore);
                    checkVKret(ret);
                    ret = ExecuteCommandBuffer(stack, 
                                               pImageAcquisitionSemaphore, 
                                               pCommandExecutionSemaphore,
                                               cvkSwapChain.GetCommandBuffer(imageIndex),
                                               cvkSwapChain.GetFence(imageIndex));
                    checkVKret(ret);             
                    
                    // Render fences synchronise CPU-GPU access to shared memory.  The
                    // fence we want to use will be in the signalled state as either it
                    // was just created (we create them in the signalled state) or the 
                    // GPU will have signalled it's finished with.  We now reset it to
                    // the unsignalled state prior to ReturnImageToSwapchainAndPresent 
                    // so that we can wait on the GPU to signal it again.
                    //frame.ResetRenderFence();
                    ret = ReturnImageToSwapchainAndPresent(stack,
                                                           pCommandExecutionSemaphore,
                                                           imageIndex);
                    if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                     || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                        swapChainNeedsRecreation = true;
                    } else {
                        checkVKret(ret);
                    }                    
        
                    // Move the frame index to the next cab off the rank
                    currentFrame = (++currentFrame) % MAX_FRAMES_IN_FLIGHT;                  
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
    
    

    
    
    @Override
    public void componentResized(ComponentEvent e) {
//        swapChainNeedsRecreation = true;
//        CVKLOGGER.info("Canvas sent componentResized");
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
