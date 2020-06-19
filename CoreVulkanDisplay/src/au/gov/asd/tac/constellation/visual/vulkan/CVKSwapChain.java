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

import static au.gov.asd.tac.constellation.visual.vulkan.CVKMissingEnums.VkFormat.VK_FORMAT_NONE;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.EndLogSection;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.StartLogSection;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
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
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateRenderPass;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import org.lwjgl.vulkan.VkExtent2D;

public class CVKSwapChain {
    protected final CVKDevice cvkDevice;
    protected long hSwapChainHandle = VK_NULL_HANDLE;
    protected long hRenderPassHandle = VK_NULL_HANDLE;
    protected int imageCount = 0;
    protected List<Long> swapChainImageHandles = null;
    protected List<Long> swapChainImageViewHandles = null;
    protected List<Long> swapChainFramebufferHandles = null;
    protected List<VkCommandBuffer> commandBuffers = null; 
    protected VkExtent2D vkCurrentImageExtent = VkExtent2D.malloc().set(0,0);
    
    
    public int GetImageCount() { return imageCount; }
    public long GetSwapChainHandle() { return hSwapChainHandle; }
    public long GetRenderPassHandle() { return hRenderPassHandle; }
    public VkCommandBuffer GetCommandBuffer(int index) { return commandBuffers.get(index); }
    public Long GetFrameBufferHandle(int index) { return swapChainFramebufferHandles.get(index); }
    
    
    public CVKSwapChain(CVKDevice device) {
        cvkDevice = device;
    }
    
    
    public int Init() {
        int ret;
        StartLogSection("Init SwapChain");
        try (MemoryStack stack = stackPush()) {                                
            
            ret = InitVKSwapChain(stack);
            if (VkFailed(ret)) return ret;
            ret = InitVKRenderPass(stack);
            if (VkFailed(ret)) return ret;
            ret = InitVKFrameBuffer(stack);
            if (VkFailed(ret)) return ret; 
            ret = InitVKCommandBuffers(stack);
            if (VkFailed(ret)) return ret;
            
            // pipeline?
        }
        EndLogSection("Init SwapChain");   
        return ret;
    }

    
    public int Deinit() {
        StartLogSection("Deinit SwapChain");
        int ret = VK_SUCCESS;
        EndLogSection("Deinit SwapChain");   
        return ret;
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
    protected int InitVKSwapChain(MemoryStack stack) {
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
        CVKLOGGER.log(Level.INFO, "Swapchain will have {0} images", imageCount);
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
        createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
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
        assert(imageCount == pImageCount.get(0));

        // Get the handles for each image
        LongBuffer pSwapchainImageHandles = stack.mallocLong(imageCount);
        ret = vkGetSwapchainImagesKHR(cvkDevice.GetDevice(), hSwapChainHandle, pImageCount, pSwapchainImageHandles);
        if (VkFailed(ret)) return ret;

        // Store the native handle for each image and create a view for it
        swapChainImageHandles = new ArrayList<>(imageCount);
        swapChainImageViewHandles = new ArrayList<>();
        for(int i = 0;i < pSwapchainImageHandles.capacity();i++) {
            long swapChainImageHandle = pSwapchainImageHandles.get(i);
            swapChainImageHandles.add(swapChainImageHandle);
            
            // Create an image view for this image
            VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.callocStack(stack);

            imageViewCreateInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            imageViewCreateInfo.image(swapChainImageHandle);
            imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            imageViewCreateInfo.format(cvkDevice.GetSurfaceFormat().Value());

            imageViewCreateInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY);
            imageViewCreateInfo.components().g(VK_COMPONENT_SWIZZLE_IDENTITY);
            imageViewCreateInfo.components().b(VK_COMPONENT_SWIZZLE_IDENTITY);
            imageViewCreateInfo.components().a(VK_COMPONENT_SWIZZLE_IDENTITY);

            imageViewCreateInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageViewCreateInfo.subresourceRange().baseMipLevel(0);
            imageViewCreateInfo.subresourceRange().levelCount(1);
            imageViewCreateInfo.subresourceRange().baseArrayLayer(0);
            imageViewCreateInfo.subresourceRange().layerCount(1);

            LongBuffer pImageView = stack.mallocLong(1);
            ret = vkCreateImageView(cvkDevice.GetDevice(), 
                                    imageViewCreateInfo, 
                                    null, //allocation callbacks
                                    pImageView);
            if (VkFailed(ret)) { return ret; }            
            swapChainImageViewHandles.add(pImageView.get(0));            
        }
        return ret;
    }   
    
    /**
     *
     * @param stack
     * @return
     */
    protected int InitVKFrameBuffer(MemoryStack stack) {
        assert(vkCurrentImageExtent.width() > 0);
        assert(vkCurrentImageExtent.height() > 0);
        
        int ret = VK_SUCCESS;
        LongBuffer attachments = stack.mallocLong(1);
        LongBuffer pFramebuffer = stack.mallocLong(1);

        VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
        framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
        framebufferInfo.renderPass(hRenderPassHandle);
        framebufferInfo.width(vkCurrentImageExtent.width());
        framebufferInfo.height(vkCurrentImageExtent.height());
        framebufferInfo.layers(1);
        
        swapChainFramebufferHandles = new ArrayList<>(imageCount);

        for (long imageView : swapChainImageViewHandles) {
            attachments.put(0, imageView);
            framebufferInfo.pAttachments(attachments);
            ret = vkCreateFramebuffer(cvkDevice.GetDevice(), 
                                      framebufferInfo, 
                                      null, //allocation callbacks
                                      pFramebuffer);
            swapChainFramebufferHandles.add(pFramebuffer.get(0));
        }
        
        return ret;
    } 


    /**
     * Vulkan has explicit objects that represent render passes.It describes the
     * frame buffer attachments such as the images in our swapchain, other depth,
     * colour or stencil buffers.  Subpasses read and write to these attachments.
     * A render pass will be instanced for use in a command buffer.
     * 
     * @param stack
     * @return 
     */
    protected int InitVKRenderPass(MemoryStack stack) {
        assert(cvkDevice.GetDevice() != null);
        assert(cvkDevice.GetSurfaceFormat() != VK_FORMAT_NONE);
        
        int ret;      
        
        VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.callocStack(1, stack);
        colorAttachment.format(cvkDevice.GetSurfaceFormat().Value());
        colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        
        // These are the states of our display images at the start and end of this pass
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.callocStack(1, stack);
        colorAttachmentRef.attachment(0);
        colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
        subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        subpass.colorAttachmentCount(1);
        subpass.pColorAttachments(colorAttachmentRef);

        VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack);
        dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
        dependency.dstSubpass(0);
        dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.srcAccessMask(0);
        dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        renderPassInfo.pAttachments(colorAttachment);
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
    
    
    protected int InitVKCommandBuffers(MemoryStack stack) {
        assert(cvkDevice.GetDevice() != null);
        assert(cvkDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);   
        assert(imageCount > 0);
        assert(vkCurrentImageExtent.width() > 0);
        assert(vkCurrentImageExtent.height() > 0);
        
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
    
  
    public int BuildCommandBuffers(List<CVKRenderable> renderables){
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
        
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(hRenderPassHandle);

            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(vkCurrentImageExtent);
            renderPassInfo.renderArea(renderArea);

            VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
            clearValues.color().float32(stack.floats(1.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);

            for (int i = 0; i < imageCount; ++i) {
                assert(swapChainFramebufferHandles.get(i) != VK_NULL_HANDLE);

                VkCommandBuffer commandBuffer = commandBuffers.get(i);

                checkVKret(vkBeginCommandBuffer(commandBuffer, beginInfo));
                renderPassInfo.framebuffer(swapChainFramebufferHandles.get(i));
                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    // Loop renderables and call draw()
                    for(int r = 0; r < renderables.size(); ++r){
                        renderables.get(r).draw(commandBuffer);
                    }
    //                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
    //                vkCmdDraw(commandBuffer, 3, 1, 0, 0);
                }
                vkCmdEndRenderPass(commandBuffer);
                checkVKret(vkEndCommandBuffer(commandBuffer));
            }
        }
        
        return ret;
    }    
    
    public boolean NeedsResize() {
        checkVKret(cvkDevice.UpdateSurfaceCapabilities());
        return vkCurrentImageExtent.width() !=cvkDevice.GetCurrentSurfaceExtent().width()
            || vkCurrentImageExtent.height() != cvkDevice.GetCurrentSurfaceExtent().height();
    }
    
    public VkExtent2D GetExtent() { return vkCurrentImageExtent; } 
    public int GetWidth() { return vkCurrentImageExtent.width(); }
    public int GetHeight() { return vkCurrentImageExtent.height(); }
}
