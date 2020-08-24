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

import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKMissingEnums.VkFormat.VK_FORMAT_NONE;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkCreateRenderPass;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.UINT64_MAX;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKImage;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKSwapChainImage;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D24_UNORM_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyRenderPass;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

/**
 * This class owns the presentable Vulkan swapchain.  It is a collection of presentable
 * images as well as the procedure used for presentation (eg single, double or triple 
 * buffered, singular or stereoscopic).
 * 
 * Swapchains are regularly destroyed when our rendering surface is resized or
 * recreated (which could happen in response to users changing system display
 * properties).
 * 
 * For most resources controlled by the swapchain there will be an instance held
 * for each image in the swapchain.  This is so we can utilise Vulkan's buffered
 * approach to displaying, that is submitting a completed list of commands and
 * resources and updating the next set before the first has been presented.
 */
public class CVKSwapChain {
    private final CVKDevice cvkDevice;
    private CVKImage cvkDepthImage = null;
    private long hSwapChainHandle = VK_NULL_HANDLE;
    private long hRenderPassHandle = VK_NULL_HANDLE;
    private long hOffscreenRenderPassHandle = VK_NULL_HANDLE;
    private int imageCount = 0;
    private int depthFormat = VK_FORMAT_UNDEFINED;
    private List<CVKSwapChainImage> swapChainImages = null;
    private List<Long> framebufferHandles = null;
    private List<Long> imageAcquisitionHandles = null;
    private List<Long> commandExecutionHandles = null;
    private List<Long> renderFenceHandles = null;
    private int nextImageAcquisitionIndex = 0;
    private List<VkCommandBuffer> commandBuffers = null;
    private final VkExtent2D vkCurrentImageExtent = VkExtent2D.malloc().set(0,0);    
    //private int colorFormat = VK_FORMAT_R8G8B8A8_UINT;
    
    
    public int GetImageCount() { return imageCount; }
    public CVKImage GetImage(int imageIndex) {
        return swapChainImages.get(imageIndex);
    }
    public long GetSwapChainHandle() { return hSwapChainHandle; }
    public long GetRenderPassHandle() { return hRenderPassHandle; }
     public long GetOffscreenRenderPassHandle() { return hOffscreenRenderPassHandle; }
    public long GetFrameBufferHandle(int imageIndex) { return framebufferHandles.get(imageIndex); }
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return commandBuffers.get(imageIndex); }
    public long GetFence(int imageIndex) { return renderFenceHandles.get(imageIndex); }
    public int GetDepthFormat() { return depthFormat; }
    public int GetColorFormat() { return cvkDevice.GetSurfaceFormat().Value(); }
    
    public CVKSwapChain(CVKDevice device) {
        cvkDevice = device;
    }
    
    
    public int Initialise() {
        int ret;        
        
        try (MemoryStack stack = stackPush()) {                                
            cvkDevice.GetLogger().StartLogSection("Init SwapChain");
            ret = InitVKSwapChain(stack);
            if (VkFailed(ret)) return ret;
            ret = InitVKRenderPass(stack);
            if (VkFailed(ret)) return ret;
            ret = CreateOffscreenRenderPass(stack);
            if (VkFailed(ret)) return ret;
            ret = InitVKFrameBuffer(stack);
            if (VkFailed(ret)) return ret; 
            ret = InitVKCommandBuffers(stack);
            if (VkFailed(ret)) return ret;
        } finally {
            cvkDevice.GetLogger().EndLogSection("Init SwapChain");   
        }
        return ret;
    }

    
    public void Destroy() {
        cvkDevice.GetLogger().StartLogSection("Destroy SwapChain");        
        
        DestroyVKCommandBuffers();       
        DestroyVKFrameBuffer();      
        DestroyVKRenderPass();
        DestroyVKOffscreenRenderPass();
        DestroyVKSwapChain();
        
        CVKAssert(cvkDepthImage == null);
        CVKAssert(hSwapChainHandle == VK_NULL_HANDLE);
        CVKAssert(hRenderPassHandle == VK_NULL_HANDLE);
        CVKAssert(hOffscreenRenderPassHandle == VK_NULL_HANDLE);
        CVKAssert(framebufferHandles.isEmpty());
        CVKAssert(imageAcquisitionHandles.isEmpty());
        CVKAssert(commandExecutionHandles.isEmpty());
        CVKAssert(renderFenceHandles.isEmpty());
        CVKAssert(commandBuffers.isEmpty());
      
        cvkDevice.GetLogger().EndLogSection("Destroy SwapChain");   
    }
    
    /**
     * Initialises a swap chain which is the mechanism used to present images.
     * <p>
     * A swap chain is essentially an array of displayable buffers (vkImages).
     * They can be configured for direct rendering, double or triple buffered
     * rendering or even stereoscopic rendering.
     * <p>
     * When created vkImages contain a logical allocation but this has not been
     * backed by physical memory yet. The 3 steps to actually getting memory
     * are:
     * <ol>
     * <li>get allocation requirements, affected by mips, layers etc</li>
     * <li>allocate a chunk of suitable memory on the device</li>
     * <li>bind that allocation to the vkImage</li>
     * </ol>
     * vkCreateImageView takes care of all 3 steps through the VkImageViewCreateInfo
     * structure.
     * 
     * @param stack
     * @return 
     * @see
     * <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#_wsi_swapchain">Swapchain
     * reference</a>
     */
    private int InitVKSwapChain(MemoryStack stack) {
        int ret;
        
        // Update the ideal extent for our backbuffer as it may have changed
        ret = cvkDevice.UpdateSurfaceCapabilities();
        if (VkFailed(ret)) return ret;        
        
        // Double buffering is preferred
        VkSurfaceCapabilitiesKHR vkSurfaceCapablities = cvkDevice.GetSurfaceCapabilities();
        IntBuffer pImageCount = stack.ints(vkSurfaceCapablities.minImageCount() + 1);
        if (vkSurfaceCapablities.maxImageCount() > 0 && pImageCount.get(0) > vkSurfaceCapablities.maxImageCount()) {
            pImageCount.put(0, vkSurfaceCapablities.maxImageCount());
        }
        imageCount = pImageCount.get(0);
        cvkDevice.GetLogger().log(Level.INFO, "Swapchain will have %d images", imageCount);
        if (imageCount == 0) {
            throw new RuntimeException("Swapchain cannot have 0 images");
        }        
       
        
        //TODO_TT: this needs a lot of commenting
        vkCurrentImageExtent.set(cvkDevice.GetCurrentSurfaceExtent());
        VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
        createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
        createInfo.surface(cvkDevice.GetSurfaceHandle());
        createInfo.minImageCount(imageCount);
        createInfo.imageFormat(cvkDevice.GetSurfaceFormat().Value());
        createInfo.imageColorSpace(cvkDevice.GetSurfaceColourSpace().Value());
        createInfo.imageExtent(vkCurrentImageExtent);
        createInfo.imageArrayLayers(1);
        // VK_IMAGE_USAGE_TRANSFER_SRC_BIT flag is needed as we support screenshots of the swapchain image
        createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT );
        createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);        
        createInfo.preTransform(vkSurfaceCapablities.currentTransform());
        createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
        createInfo.presentMode(cvkDevice.GetPresentationMode().Value());
        createInfo.clipped(true);
        createInfo.oldSwapchain(VK_NULL_HANDLE);

        // Create the swapchain
        LongBuffer pSwapChainHandles = stack.longs(VK_NULL_HANDLE);
        ret = vkCreateSwapchainKHR(cvkDevice.GetDevice(), createInfo, null, pSwapChainHandles);
        if (VkFailed(ret)) return ret;
        hSwapChainHandle = pSwapChainHandles.get(0);

        // Check this swapchain supports the number of images we requested
        ret = vkGetSwapchainImagesKHR(cvkDevice.GetDevice(), hSwapChainHandle, pImageCount, null);
        if (VkFailed(ret)) return ret;
        //TODO_TT: exception?
        CVKAssert(imageCount == pImageCount.get(0));

        // Get the handles for each image
        LongBuffer pSwapchainImageHandles = stack.mallocLong(imageCount);
        ret = vkGetSwapchainImagesKHR(cvkDevice.GetDevice(), hSwapChainHandle, pImageCount, pSwapchainImageHandles);
        if (VkFailed(ret)) return ret;

        // Store the native handle for each image and create a view for it
        swapChainImages = new ArrayList<>(imageCount);
        imageAcquisitionHandles = new ArrayList<>();
        commandExecutionHandles = new ArrayList<>();
        renderFenceHandles = new ArrayList<>();
        for(int i = 0; i < imageCount; ++i) {
            long swapChainImageHandle = pSwapchainImageHandles.get(i);
            
            // Factory that returns a SwapChainImage instance initialised with device, image handle,
            // sets default parameters and creates the Image View
            swapChainImages.add(CVKSwapChainImage.Create(cvkDevice, swapChainImageHandle, String.format("CVKSwapChainIamge %d", i)));
            swapChainImages.get(i).SetExtent(cvkDevice.GetCurrentSurfaceExtent().width(),
                    cvkDevice.GetCurrentSurfaceExtent().height());
                  
            // Create synchronisation objects, we recreate these each time the swapchain is recreated
            // which is probably excessive, though it's theoretically possible the recreated swapchain
            // could have a different number of images to its precursor (though probably impossible in
            // reality).
            LongBuffer pSemaphore = stack.mallocLong(1);               
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
            ret = vkCreateSemaphore(cvkDevice.GetDevice(), semaphoreInfo, null, pSemaphore);
            if (VkFailed(ret)) { return ret; }   
            imageAcquisitionHandles.add(pSemaphore.get(0));
            pSemaphore.put(0, 0);
            ret = vkCreateSemaphore(cvkDevice.GetDevice(), semaphoreInfo, null, pSemaphore);
            if (VkFailed(ret)) { return ret; }   
            commandExecutionHandles.add(pSemaphore.get(0));
            
            // Render fence, CPU-GPU synch.  Created signalled so we can wait on it during the very
            // first frame before anything has been sent to the GPU and have it just return immediately.
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);
            LongBuffer pFence = stack.mallocLong(1);
            ret = vkCreateFence(cvkDevice.GetDevice(), fenceInfo, null, pFence);
            if (VkFailed(ret)) { return ret; }   
            renderFenceHandles.add(pFence.get(0));
        }
        
        // Figure out the best depth format our device supports        
        VkFormatProperties vkFormatProperties = VkFormatProperties.callocStack(stack);
        IntBuffer possibleDepthFormats = stack.ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT);
        for (int i = 0; i < possibleDepthFormats.capacity(); ++i) {
            vkGetPhysicalDeviceFormatProperties(cvkDevice.GetPhysicalDevice(),
                                                possibleDepthFormats.get(i),
                                                vkFormatProperties);
            if ((vkFormatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT) != 0) {
                depthFormat = possibleDepthFormats.get(i);
                break;
            }
        }
        if (depthFormat == VK_FORMAT_UNDEFINED) {
            cvkDevice.GetLogger().severe("Failed to find supported detpth format");
            throw new RuntimeException("Failed to find supported detpth format");
        }
        
        // Depth attachment.  It has:
        // * the same extents as the colour images
        // * a single layer 
        // * best format determined in the block above
        // * optimal tiling (square kernel vs linear row) for performance
        // * depth/stencil usage
        // * device local, the fastest memory type, not host readable/writable 
        //   but the gpu is the only thing that writes and then reads depth.
        cvkDepthImage = CVKImage.Create(cvkDevice, 
                                        vkCurrentImageExtent.width(),
                                        vkCurrentImageExtent.height(), 
                                        1, 
                                        depthFormat, 
                                        VK_IMAGE_VIEW_TYPE_2D,
                                        VK_IMAGE_TILING_OPTIMAL, 
                                        VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, 
                                        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                        VK_IMAGE_ASPECT_DEPTH_BIT,
                                        "CVKSwapChain cvkDepthImage");
        cvkDepthImage.Transition(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        
//        // Transition the depth image to the optimal depth state
//        CVKCommandBuffer cvkDepthTransitionCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
//        cvkDepthTransitionCmd.DEBUGNAME = "CVKSwapChain cvkDepthTransitionCmd";
//        ret = cvkDepthTransitionCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
//        if (VkFailed(ret)) { return ret; }               
//        ret = cvkDepthImage.Transition(cvkDepthTransitionCmd, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
//        if (VkFailed(ret)) { return ret; }   
//        ret = cvkDepthTransitionCmd.EndAndSubmit();
//        if (VkFailed(ret)) { return ret; }     
//        cvkDepthTransitionCmd.Destroy();        
        
        return ret;
    }   
    
    /**
     * In Vulkan the swap chain owns the presentable images.  A frame buffer is
     * a collection of attachments used by a render pass.  These attachments correspond
     * to the images owned by the swapchain.  There will be one frame buffer per
     * image in the swap chain.  The depth image is used by all frame buffers as
     * we never present it so once we get to the last fragment in the last fragment
     * shader it is free to be cleared and used by the next frame.
     * 
     * @param stack
     * @return
     * @see <a href=https://vulkan.lunarg.com/doc/view/1.2.141.2/windows/tutorial/html/12-init_frame_buffers.htm>LunarG Framebuffers</a>
     */
    private int InitVKFrameBuffer(MemoryStack stack) {
        CVKAssert(vkCurrentImageExtent.width() > 0);
        CVKAssert(vkCurrentImageExtent.height() > 0);
        
        int ret = VK_SUCCESS;
        LongBuffer attachments = stack.mallocLong(2);
        LongBuffer pFramebuffer = stack.mallocLong(1);

        VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
        framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
        framebufferInfo.renderPass(hRenderPassHandle);
        framebufferInfo.width(vkCurrentImageExtent.width());
        framebufferInfo.height(vkCurrentImageExtent.height());
        framebufferInfo.layers(1);
        
        framebufferHandles = new ArrayList<>(imageCount);

        for (var image : swapChainImages) {
            attachments.put(0, image.GetImageViewHandle());
            attachments.put(1, cvkDepthImage.GetImageViewHandle());
            framebufferInfo.pAttachments(attachments);
            ret = vkCreateFramebuffer(cvkDevice.GetDevice(), 
                                      framebufferInfo, 
                                      null, //allocation callbacks
                                      pFramebuffer);
            if (VkFailed(ret)) { return ret; }
            framebufferHandles.add(pFramebuffer.get(0)); 
        }
        
        return ret;
    } 


    /**
     * Vulkan has explicit objects that represent render passes. It describes the
     * frame buffer attachments such as the images in our swapchain, other depth,
     * colour or stencil buffers.  Subpasses read and write to these attachments.
     * A render pass will be instanced for use in a command buffer.
     * 
     * @param stack
     * @return 
     */
    private int InitVKRenderPass(MemoryStack stack) {
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetSurfaceFormat() != VK_FORMAT_NONE);
        
        int ret;      
        
        // 0: colour, 1: depth
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(2, stack);
        VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(2, stack);        
        
        // Colour attachment
        VkAttachmentDescription colorAttachment = attachments.get(0);
        colorAttachment.format(cvkDevice.GetSurfaceFormat().Value());
        colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        
        // These are the states of our display images at the start and end of this pass
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        VkAttachmentReference colorAttachmentRef = attachmentRefs.get(0);
        colorAttachmentRef.attachment(0);
        colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        
        // Depth attachment
        VkAttachmentDescription depthAttachment = attachments.get(1);
        depthAttachment.format(depthFormat);
        depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depthAttachment.initialLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);//VK_IMAGE_LAYOUT_UNDEFINED);
        depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        VkAttachmentReference depthAttachmentRef = attachmentRefs.get(1);
        depthAttachmentRef.attachment(1);
        depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);  
        
        VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
        subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount(1);
        // This bit of hackery is because pColorAttachments is a buffer of multiple references, whereas pDepthStencilAttachment is singular
        subpass.pColorAttachments(VkAttachmentReference.callocStack(1, stack).put(0, colorAttachmentRef)); 
        subpass.pDepthStencilAttachment(depthAttachmentRef);

        VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack);
        dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass(0);
        dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask(0);
        dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        renderPassInfo.pAttachments(attachments);
        renderPassInfo.pSubpasses(subpass);
        renderPassInfo.pDependencies(dependency);

        LongBuffer pRenderPass = stack.mallocLong(1);
        ret = vkCreateRenderPass(cvkDevice.GetDevice(),
                                 renderPassInfo, 
                                 null, //allocation callbacks
                                 pRenderPass);
        if (VkSucceeded(ret)) {
            hRenderPassHandle = pRenderPass.get(0);        
        }
        
        return ret;
    }
    
    private int CreateOffscreenRenderPass(MemoryStack stack) {
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetSurfaceFormat() != VK_FORMAT_NONE);
        
        int ret;      
               
        // 0: colour, 1: depth
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(2, stack);
        VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(2, stack);        

        // Colour attachment
        VkAttachmentDescription colorAttachment = attachments.get(0);
        //colorAttachment.format(colorFormat);
        colorAttachment.format(cvkDevice.GetSurfaceFormat().Value());
        colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);

        // These are the states of our display images at the start and end of this pass
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        // TODO - I think something is wrong with the colour attachment here
        // VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL CONFIRM THIS
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        VkAttachmentReference colorAttachmentRef = attachmentRefs.get(0);
        colorAttachmentRef.attachment(0);
        colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        // Depth attachment
        VkAttachmentDescription depthAttachment = attachments.get(1);
        depthAttachment.format(GetDepthFormat());
        depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);//VK_IMAGE_LAYOUT_UNDEFINED);
        depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        VkAttachmentReference depthAttachmentRef = attachmentRefs.get(1);
        depthAttachmentRef.attachment(1);
        depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);  

        VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
        subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount(1);
        // This bit of hackery is because pColorAttachments is a buffer of multiple references, whereas pDepthStencilAttachment is singular
        subpass.pColorAttachments(VkAttachmentReference.callocStack(1, stack).put(0, colorAttachmentRef)); 
        subpass.pDepthStencilAttachment(depthAttachmentRef);

        VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack);
        dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass(0);
        dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask(0);
        dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        renderPassInfo.pAttachments(attachments);
        renderPassInfo.pSubpasses(subpass);
        renderPassInfo.pDependencies(dependency);

        LongBuffer pRenderPass = stack.mallocLong(1);
        ret = vkCreateRenderPass(cvkDevice.GetDevice(),
                                 renderPassInfo, 
                                 null, //allocation callbacks
                                 pRenderPass);
        if (VkSucceeded(ret)) {
            hOffscreenRenderPassHandle = pRenderPass.get(0);        
        }

        return ret;
    } 
    
    
    private int InitVKCommandBuffers(MemoryStack stack) {
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);   
        CVKAssert(imageCount > 0);
        CVKAssert(vkCurrentImageExtent.width() > 0);
        CVKAssert(vkCurrentImageExtent.height() > 0);
        
        int ret;
                
        commandBuffers = new ArrayList<>(imageCount);

        VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
        allocInfo.commandPool(cvkDevice.GetCommandPoolHandle());
        allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
        allocInfo.commandBufferCount(imageCount);

        PointerBuffer pCommandBuffers = stack.mallocPointer(imageCount);
        ret = vkAllocateCommandBuffers(cvkDevice.GetDevice(), 
                                       allocInfo, 
                                       pCommandBuffers);
        if (VkFailed(ret)) return ret;

        for (int i = 0; i < imageCount; ++i) {
            commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), cvkDevice.GetDevice()));
        }
        
        return ret;
    }             
    
    
    public int WaitOnFence(int imageIndex) {
        int ret;
        ret = vkWaitForFences(cvkDevice.GetDevice(), 
                              renderFenceHandles.get(imageIndex), 
                              true, //wait on first or all fences, only one fence in this case
                              Long.MAX_VALUE); //timeout
        if (VkFailed(ret)) return ret;
        ret = vkResetFences(cvkDevice.GetDevice(), renderFenceHandles.get(imageIndex));
        return ret;        
    }
    
    
    /**
     *
     * Get the first available image acquire semaphore and bind it to the image
     * index returned.The image at imageIndex may still be in use by the 
     * presentation engine so the semaphore bound to this image must be used 
     * before executing commands into it.
     * 
     * @param stack
     * @param pImageIndex
     * @param pImageAcquiredSemaphore
     * @return
     */
    public int AcquireNextImage(MemoryStack stack, IntBuffer pImageIndex, LongBuffer pImageAcquiredSemaphore) {
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(GetSwapChainHandle() != VK_NULL_HANDLE);
        CVKAssert(pImageIndex != null);       
        CVKAssert(pImageAcquiredSemaphore != null);
        CVKAssert(imageAcquisitionHandles.size() > 0);
        
        // This semaphore is waited on by the command buffer queue submit.  That means on successive calls
        // to AcquireNextImage for the same image we return now, the semaphore is garaunteed to be in the
        // signalled state.
        long imageAcquiredSemaphore = imageAcquisitionHandles.get(nextImageAcquisitionIndex);        
        pImageAcquiredSemaphore.put(0, imageAcquiredSemaphore);
        nextImageAcquisitionIndex = (++nextImageAcquisitionIndex) % imageCount;
        
        int ret;        
        ret = vkAcquireNextImageKHR(cvkDevice.GetDevice(),
                                    hSwapChainHandle,
                                    UINT64_MAX, //timeout
                                    imageAcquiredSemaphore,
                                    VK_NULL_HANDLE,
                                    pImageIndex);        
                
        return ret;
    }
    
    
    public int GetCommandBufferExecutedSemaphore(final int imageIndex, LongBuffer pCommandExecutionSemaphore) {
        CVKAssert(imageIndex >= 0 && imageIndex < imageCount);
        long hCommandExecutionSemaphore = commandExecutionHandles.get(imageIndex);
        pCommandExecutionSemaphore.put(0, hCommandExecutionSemaphore);
        return VK_SUCCESS;
    }    
    
    
    private void DestroyVKCommandBuffers() {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffers.size());
            for (int i = 0; i < imageCount; ++i) {
                pCommandBuffers.put(commandBuffers.get(i).address());
            }
            vkFreeCommandBuffers(cvkDevice.GetDevice(), cvkDevice.GetCommandPoolHandle(), pCommandBuffers);
            commandBuffers.clear();
            cvkDevice.GetLogger().info("Destroyed command buffers for all images");
        }        
    }  
    
    private void DestroyVKFrameBuffer() {
        for (int i = 0; i < imageCount; ++i) {
            vkDestroyFramebuffer(cvkDevice.GetDevice(), framebufferHandles.get(i), null);
            cvkDevice.GetLogger().info("Destroyed frame buffer for image %d", i);
        }
        framebufferHandles.clear();
    }  
   
    private void DestroyVKRenderPass() {
        vkDestroyRenderPass(cvkDevice.GetDevice(), hRenderPassHandle, null);
        hRenderPassHandle = VK_NULL_HANDLE;
        cvkDevice.GetLogger().info("Destroyed render pass");
    }

    private void DestroyVKOffscreenRenderPass() {
        vkDestroyRenderPass(cvkDevice.GetDevice(), hOffscreenRenderPassHandle, null);
        hOffscreenRenderPassHandle = VK_NULL_HANDLE;
        cvkDevice.GetLogger().info("Destroyed offscreenrender pass");
    }
    
    private void DestroyVKSwapChain() {
        // Destroy synchronisation objects
        for (int i = 0; i < imageCount; ++i) {
            if (imageAcquisitionHandles.get(i) != VK_NULL_HANDLE) {
                vkDestroySemaphore(cvkDevice.GetDevice(), imageAcquisitionHandles.get(i), null);
                imageAcquisitionHandles.set(i, VK_NULL_HANDLE);
            }
            if (commandExecutionHandles.get(i) != VK_NULL_HANDLE) {
                vkDestroySemaphore(cvkDevice.GetDevice(), commandExecutionHandles.get(i), null);
                commandExecutionHandles.set(i, VK_NULL_HANDLE);
            }
            if (renderFenceHandles.get(i) != VK_NULL_HANDLE) {
                vkDestroyFence(cvkDevice.GetDevice(), renderFenceHandles.get(i), null);
                renderFenceHandles.set(i, VK_NULL_HANDLE);
            }
            cvkDevice.GetLogger().info("Destroyed synchronisation objects for image %d", i);
        }
        imageAcquisitionHandles.clear();
        commandExecutionHandles.clear();
        renderFenceHandles.clear();
                
        // The swapchain creates its own images but we create the image views for each one, destroy those now
        for (int i = 0; i < imageCount; ++i) {
             // Clear our list of images, we don't destroy these as the swapchain objects owns their memory
            swapChainImages.get(i).Destroy();
            cvkDevice.GetLogger().info("Destroyed image view for image %d", i);
        }
        
        // Destroy the depth attachment
        cvkDepthImage.Destroy();
        cvkDepthImage = null;
        
        // Finally, destroy the swapchain
        vkDestroySwapchainKHR(cvkDevice.GetDevice(), hSwapChainHandle, null);
        hSwapChainHandle = VK_NULL_HANDLE;
        cvkDevice.GetLogger().info("Destroyed swapchain");
    }
    
    
    public boolean NeedsResize() {
        checkVKret(cvkDevice.UpdateSurfaceCapabilities());
        return vkCurrentImageExtent.width() != cvkDevice.GetCurrentSurfaceExtent().width()
            || vkCurrentImageExtent.height() != cvkDevice.GetCurrentSurfaceExtent().height();
    }
    
    public VkExtent2D GetExtent() { return vkCurrentImageExtent; } 
    public int GetWidth() { return vkCurrentImageExtent.width(); }
    public int GetHeight() { return vkCurrentImageExtent.height(); }
    public int[] GetViewport(){
        return new int[]{0, GetHeight(), GetWidth(), -GetHeight()};
    }
}
