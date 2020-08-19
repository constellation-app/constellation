/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.vulkan;

import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import java.awt.Graphics;

import org.lwjgl.vulkan.awt.AWTVKCanvas;
import javax.swing.SwingUtilities;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import org.lwjgl.vulkan.awt.VKData;


public class CVKCanvas extends AWTVKCanvas {
    private final CVKVisualProcessor cvkVisualProcessor;    
    private final CVKRenderer cvkRenderer;
    private int frameNumber = 0;
    
    public int GetFrameNumber() { return frameNumber; }
    public CVKRenderer GetRenderer() { return cvkRenderer; }
    public CVKGraphLogger GetLogger() { return cvkVisualProcessor.GetLogger(); }
    public void VerifyInRenderThread() { cvkVisualProcessor.VerifyInRenderThread(); }
    public boolean IsRenderThreadCurrent() { return cvkVisualProcessor.IsRenderThreadCurrent(); }
    public boolean IsRenderThreadAlive() { return cvkVisualProcessor.IsRenderThreadAlive(); }
    public void AddRenderable(CVKRenderable renderable) { cvkRenderer.AddRenderable(renderable); }
    
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }
    
    
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
        this.cvkVisualProcessor = visualProcessor;       
        cvkRenderer = new CVKRenderer(this, visualProcessor);         
    }
    
    public void Destroy() {
        try {
            cvkVisualProcessor.GetLogger().StartLogSection("CVKCanvas Destroy");
            cvkRenderer.Destroy();
        } finally {
            cvkVisualProcessor.GetLogger().EndLogSection("CVKCanvas Destroy");
        }
    }
    
    @Override
    public void removeNotify() {
        if (cvkRenderer != null) {
            cvkRenderer.GetLogger().info("CVKCanvas removed from parent container"); 
            cvkRenderer.SurfaceLost();
        }
        super.removeNotify();
    }    
    
    @Override
    public void initVK() {
        CVKAssertNotNull(cvkVisualProcessor);
        cvkVisualProcessor.GetLogger().info("CVKCanvas initVK %d (0x%016X)", surface, surface); 
        cvkRenderer.SurfaceCreated();
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