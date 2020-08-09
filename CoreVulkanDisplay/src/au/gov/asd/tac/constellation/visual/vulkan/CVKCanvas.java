/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.vulkan;

import java.awt.Graphics;
import org.lwjgl.vulkan.awt.AWTVKCanvas;
import javax.swing.SwingUtilities;
import org.lwjgl.vulkan.awt.VKData;



public class CVKCanvas extends AWTVKCanvas{
    private boolean parentAdded = false;
    private int frameNumber = 0;
    private final CVKRenderer cvkRenderer;
    
    public int GetFrameNumber() { return frameNumber; }
    
    public CVKCanvas(VKData vkData, CVKRenderer cvkRenderer) {
        super(vkData);
        cvkRenderer.Logger().fine("Canvas constructed");        
        this.cvkRenderer = cvkRenderer;
        this.addComponentListener(cvkRenderer);
    }
  
    public void Destroy() {
        cvkRenderer.Logger().fine("Canvas destroyed"); 
        this.removeComponentListener(cvkRenderer);
    }
    
    public void InitSurface() {
        cvkRenderer.Logger().fine("Canvas InitSurface"); 
        parentAdded = true;
    }
    
    @Override
    public void paint(final Graphics g) {
        if (parentAdded) {
            super.paint(g);
        }
    }
    
    /*    
    * Our cvkCanvas belongs to a JPanel which in turn belongs to a tabbed control.
    * When we are constructed as part of the VisualGraphOpener call chain that
    * panel hasn't yet been added to it's parent.  In that state we cannot lock
    * the cvkCanvas surface (JAWT_DrawingSurface_Lock returns an error).  Without
    * the surface we cannot initialise all the Vulkan resources we need.   
    *
    * requestFocusInWindow is be called from the VisualTopComponent once the new tab has been created.  If
    * we try to create the surface before that (which happens on the first paint) it will fail.    
    */
    @Override
    public boolean requestFocusInWindow() {
        cvkRenderer.Logger().fine("Canvas requestFocusInWindow"); 
        boolean ret = super.requestFocusInWindow();
        parentAdded = true;
        return ret;
    }
    
    @Override
    public void initVK() {
        cvkRenderer.Logger().fine("Canvas initVK"); 
        cvkRenderer.Initialise(this.surface);
    }
    
    @Override
    public void paintVK() {
        // This will be called by AWTVKCanvas during initialisation but before
        // CVKRenderer is ready to use.  platformCanvas is private in our parent 
        // so this is the only way to complete the surface initialisation.  
        ++frameNumber;       
        cvkRenderer.Display();
    }
    
    @Override
    public void repaint() {
        if (SwingUtilities.isEventDispatchThread()) {
            paintVK();
        } else {
            SwingUtilities.invokeLater(() -> paintVK());
        }
    }
}
