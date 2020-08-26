/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.vulkan;

import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKMissingEnums;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.UINT32_MAX;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import com.google.common.primitives.Ints;
import java.nio.IntBuffer;
import java.util.logging.Level;
import org.lwjgl.vulkan.awt.AWTVKCanvas;
import javax.swing.SwingUtilities;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.awt.VKData;


public class CVKCanvas extends AWTVKCanvas {
    private final CVKVisualProcessor cvkVisualProcessor;    
    private final CVKRenderer cvkRenderer;
    private VkSurfaceCapabilitiesKHR vkSurfaceCapabilities = null;
    private CVKMissingEnums.VkFormat selectedFormat = CVKMissingEnums.VkFormat.VK_FORMAT_NONE;
    private CVKMissingEnums.VkColorSpaceKHR selectedColourSpace = CVKMissingEnums.VkColorSpaceKHR.VK_COLOR_SPACE_NONE;
    private CVKMissingEnums.VkPresentModeKHR selectedPresentationMode = CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_NONE;     
    private final VkExtent2D vkCurrentSurfaceExtent = VkExtent2D.malloc().set(0, 0);
    private int frameNumber = 0;
    
        
    // ========================> Getters <======================== \\
    
    public int GetFrameNumber() { return frameNumber; }
    public CVKRenderer GetRenderer() { return cvkRenderer; }
    public CVKGraphLogger GetLogger() { return cvkVisualProcessor.GetLogger(); }
    public void VerifyInRenderThread() { cvkVisualProcessor.VerifyInRenderThread(); }
    public boolean IsRenderThreadCurrent() { return cvkVisualProcessor.IsRenderThreadCurrent(); }
    public boolean IsRenderThreadAlive() { return cvkVisualProcessor.IsRenderThreadAlive(); }
    public void AddRenderable(CVKRenderable renderable) { cvkRenderer.AddRenderable(renderable); }
    public long GetSurfaceHandle() { return surface; }
    public VkExtent2D GetCurrentSurfaceExtent() { return vkCurrentSurfaceExtent; }
    public VkSurfaceCapabilitiesKHR GetSurfaceCapabilities() { return vkSurfaceCapabilities; }
    public CVKMissingEnums.VkFormat GetSurfaceFormat() { return selectedFormat; }    
    public CVKMissingEnums.VkColorSpaceKHR GetSurfaceColourSpace() { return selectedColourSpace; }    
    public CVKMissingEnums.VkPresentModeKHR GetPresentationMode() { return selectedPresentationMode; }    
    
    

    // ========================> Lifetime <======================== \\
    
    private static VKData NewVKData() {
        VKData vkData = new VKData();
        vkData.instance = CVKInstance.GetVkInstance();        
        return vkData;
    }
    
    /**
     * CVKCanvas belongs to a JPanel which in turn belongs to a tabbed control.
     * When we are constructed as part of the VisualGraphOpener call chain that
     * panel hasn't yet been added to it's parent.  In that state we cannot lock
     * the cvkCanvas surface (JAWT_DrawingSurface_Lock returns an error).  Without
     * the surface we cannot initialise all the Vulkan resources we need.   
     * 
     * Our parent class AWTCanvas will call initVK when we have a valid surface to
     * which to render.  Until that time we cannot do any device initialisation or
     * rendering.  CVKRenderer is created and can receive renderables which in
     * turn can process user input events or graph load events, but we never pump
     * the render loop so no device dependent actions will occur until our surface
     * is ready.
     * 
     * @param visualProcessor: the visual processor that created this canvas.  It
     * holds a logger that is specific to graph this canvas displays.  It also tracks
     * the rendering thread (on Windows there is a single AWT event thread that
     * processes the rendering for all graphs but this may not be true for all 
     * platforms).
     */
    public CVKCanvas(CVKVisualProcessor visualProcessor) {
        super(NewVKData());
        CVKAssertNotNull(visualProcessor);
        vkSurfaceCapabilities = VkSurfaceCapabilitiesKHR.malloc();
        this.cvkVisualProcessor = visualProcessor;       
        cvkRenderer = new CVKRenderer(this, visualProcessor);         
    }
    
    public void Destroy() {
        try {
            GetLogger().StartLogSection("CVKCanvas Destroy");
            cvkRenderer.Destroy();
        } finally {
            GetLogger().EndLogSection("CVKCanvas Destroy");
        }
        
        if (vkSurfaceCapabilities != null)
        {
            vkSurfaceCapabilities.free();
            vkSurfaceCapabilities = null;
        }
    }
    
    
    // ========================> Surface <======================== \\    
    
    /**
     * Updates surface capabilities which may have changed due to our canvas
     * being resized.  It also updates the ideal extent which is either the
     * capabilities currentExtent capped to minImageExtent and maxImageExtent 
     * 
     * @return error code
     */
    public int UpdateSurfaceCapabilities() {
        int ret;
        try {
            GetLogger().StartLogSection("Canvas updating surface capalities");

            ret = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(CVKDevice.GetVkPhysicalDevice(), GetSurfaceHandle(), vkSurfaceCapabilities);
            if (VkSucceeded(ret)) {
                vkCurrentSurfaceExtent.set(vkSurfaceCapabilities.currentExtent());
                if (vkCurrentSurfaceExtent.width() == UINT32_MAX) {
                    //TODO_TT: find out how big our surface is somehow
                    vkCurrentSurfaceExtent.set(800, 600);
                    GetLogger().log(Level.WARNING, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR returned extent with the magic don't care size");
                }

                // TODO: clean this up once the attribute calculator bug is fixed
                int width = vkCurrentSurfaceExtent.width();
                int height = vkCurrentSurfaceExtent.height();

                final int minSurfaceWidth = vkSurfaceCapabilities.minImageExtent().width();
                final int minSurfaceHeight = vkSurfaceCapabilities.minImageExtent().height();

                final VkExtent2D maxFrameBufferExtent = CVKDevice.GetMaxFrameBufferExtent();
                GetLogger().info("Calculating surface height:\n\tvkCurrentSurfaceExtent:%d\n\tvkSurfaceCapabilities.min:%d\n\tvkSurfaceCapabilities.max:%d\n\tvkMaxFramebufferExtent:%d",
                        height, minSurfaceHeight, vkSurfaceCapabilities.maxImageExtent().height(), maxFrameBufferExtent.height());

                CVKAssert(width >= minSurfaceWidth);
                CVKAssert(height >= minSurfaceHeight);            

                // Constrain to dimensions supported by the current surface capabilities
                width = Ints.constrainToRange(width, minSurfaceWidth, vkSurfaceCapabilities.maxImageExtent().width());
                height = Ints.constrainToRange(height, minSurfaceHeight, vkSurfaceCapabilities.maxImageExtent().height());

                // Constrain to framebuffer maximums            
                width = Ints.constrainToRange(width, 0, maxFrameBufferExtent.width());
                height = Ints.constrainToRange(height, 0, maxFrameBufferExtent.height());

                vkCurrentSurfaceExtent.set(width, height);
                GetLogger().info("Ideal extent will be %d x %d", vkCurrentSurfaceExtent.width(), vkCurrentSurfaceExtent.height());
            }
            else {
                GetLogger().severe("vkGetPhysicalDeviceSurfaceCapabilitiesKHR failed with error: %d (0x%08X)", ret, ret);
            }

            // Figure out our ideal backbuffer size
            // The current size of the surface will either be explicit, which we use, or 
            // set to a value indicating it will use whatever is set in the swap chain.
            GetLogger().info(String.format("Surface will be %dx%d", vkCurrentSurfaceExtent.width(), vkCurrentSurfaceExtent.height()));
        } finally {               
            GetLogger().EndLogSection("Canvas updating surface capalities");
        }
        
        return ret;
    }   
    
    private int SelectPresentationMode() {
        int ret;
        try (MemoryStack stack = stackPush()) {
            // Surface formats our device can use
            IntBuffer pInt = stack.mallocInt(1);
            pInt.put(0, 0);
            ret = vkGetPhysicalDeviceSurfaceFormatsKHR(CVKDevice.GetVkPhysicalDevice(), GetSurfaceHandle(), pInt, null);
            if (VkFailed(ret)) return ret;
            int numFormats = pInt.get(0);
            if (numFormats > 0) {
                VkSurfaceFormatKHR.Buffer vkSurfaceFormats = VkSurfaceFormatKHR.callocStack(numFormats, stack);
                ret = vkGetPhysicalDeviceSurfaceFormatsKHR(CVKDevice.GetVkPhysicalDevice(), surface, pInt, vkSurfaceFormats);
                if (VkFailed(ret)) return ret;
                
                GetLogger().info(String.format("Available surface formats:"));
                for (int i = 0; i < numFormats; ++i) {
                    VkSurfaceFormatKHR surfaceFormat = vkSurfaceFormats.get(i);
                    CVKMissingEnums.VkColorSpaceKHR colorSpace = CVKMissingEnums.VkColorSpaceKHR.GetByValue(surfaceFormat.colorSpace());
                    CVKMissingEnums.VkFormat format = CVKMissingEnums.VkFormat.values()[surfaceFormat.format()];
                    
                    // We want to use VK_FORMAT_B8G8R8A8_SRGB for the surface format.  That's a byte for each
                    // of RGBA so it's easy to work with but where the value is nonlinearly mapped to 
                    // intensity.  Check out sRGB, but in short given the nature of human vision and the
                    // display characteristics of most displays we are better off concentrating granularity
                    // around intensities we can differentiate rather than just using a linear mapping.
                    if (format == CVKMissingEnums.VkFormat.VK_FORMAT_B8G8R8A8_SRGB) {
                        selectedFormat = format;
                    }
                    
                    // For the reason above we want the sRGB colour space
                    if (colorSpace == CVKMissingEnums.VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                        selectedColourSpace = colorSpace;
                    }
                    
                    GetLogger().info(String.format("    %s:%s", format.name(), colorSpace.name()));                        
                }
            }
            
            if (selectedFormat == CVKMissingEnums.VkFormat.VK_FORMAT_NONE) {
                throw new RuntimeException("Required surface format unsupported (VK_FORMAT_B8G8R8A8_SRGB)");
            }
            if (selectedColourSpace == CVKMissingEnums.VkColorSpaceKHR.VK_COLOR_SPACE_NONE) {
                throw new RuntimeException("Required color space unsupported (VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)");
            }
            

            // Presentation modes our device can use for our surface
            pInt.put(0, 0);
            ret = vkGetPhysicalDeviceSurfacePresentModesKHR(CVKDevice.GetVkPhysicalDevice(), GetSurfaceHandle(), pInt, null);
            if (VkFailed(ret)) return ret;
            int numPresentationModes = pInt.get(0);
            if (numPresentationModes > 0) {
                IntBuffer presentationModes = stack.mallocInt(numPresentationModes);
                vkGetPhysicalDeviceSurfacePresentModesKHR(CVKDevice.GetVkPhysicalDevice(), GetSurfaceHandle(), pInt, presentationModes);
                
                GetLogger().info(String.format("Supported presentation modes:"));
                for (int i = 0; i < numPresentationModes; ++i) {                
                    CVKMissingEnums.VkPresentModeKHR presentationMode = CVKMissingEnums.VkPresentModeKHR.values()[presentationModes.get(i)];
                    // Mailbox is our first choice
                    if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR) {
                        selectedPresentationMode = presentationMode;
                    }
                    // Second preference is VK_PRESENT_MODE_FIFO_KHR, selected unless we already have mailbox
                    else if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR 
                     && selectedPresentationMode != CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR) {
                        selectedPresentationMode = presentationMode;
                    }
                    // Third preference is VK_PRESENT_MODE_FIFO_RELAXED_KHR
                    else if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_FIFO_RELAXED_KHR 
                     && selectedPresentationMode != CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR
                     && selectedPresentationMode != CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR) {
                        selectedPresentationMode = presentationMode;
                    }           
                    // Last choice
                    else if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_IMMEDIATE_KHR
                     && selectedPresentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_NONE) {
                        selectedPresentationMode = presentationMode;
                    }
                    GetLogger().info(String.format("   %s", presentationMode.name()));
                }                              
            }
            
            if (selectedPresentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_NONE) {
                throw new RuntimeException("No presentation mode supported");
            }  
        }
        
        return ret;
    }
    
    
    
    @Override
    public void removeNotify() {
        if (cvkRenderer != null) {
            cvkRenderer.GetLogger().info("CVKCanvas removed from parent container"); 
            cvkRenderer.SurfaceLost();
        }
        
        if (surface != VK_NULL_HANDLE) {
            vkDestroySurfaceKHR(CVKInstance.GetVkInstance(), surface, null);
            surface = VK_NULL_HANDLE;
        }        
        
        super.removeNotify();
    }    
    
    @Override
    public void initVK() {
        CVKAssertNotNull(cvkVisualProcessor);
        cvkVisualProcessor.GetLogger().info("CVKCanvas initVK %d (0x%016X)", surface, surface); 

        int ret = CVKDevice.GetInstance().InitialiseSurface(this);
        if (VkFailed(ret)) {
            throw new RuntimeException("CVKCanvas's new surface is unsupported");
        }
        
        ret = UpdateSurfaceCapabilities();
        if (VkFailed(ret)) {
            throw new RuntimeException("CVKCanvas UpdateSurfaceCapabilities failed");
        }    
        
        ret = SelectPresentationMode();
        if (VkFailed(ret)) {
            throw new RuntimeException("CVKCanvas SelectPresentationMode failed");
        }          
    }        
    
    @Override
    public void paintVK() {        
        // This will be called by AWTVKCanvas during initialisation but before
        // CVKRenderer is ready to use.
        if (surface != VK_NULL_HANDLE) {
            ++frameNumber;       
            cvkRenderer.Display();
        }
    }
    
    @Override
    public void repaint() {
        if (surface != VK_NULL_HANDLE) {
            if (SwingUtilities.isEventDispatchThread()) {
                paintVK();
            } else {
                SwingUtilities.invokeLater(() -> paintVK());
            }
        }
    }  
}
