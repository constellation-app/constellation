/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.vulkan;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.EventListener;
import org.lwjgl.vulkan.awt.AWTVKCanvas;

import javax.swing.SwingUtilities;
import org.lwjgl.vulkan.awt.VKData;



public class CVKCanvas extends AWTVKCanvas{
    private boolean parentAdded = false;
    static int frameNo = 0;
    protected long handle = 0;
    static float red = 0.0f;
    static float green = 0.5f;
    static float blue = 1.0f;
    
    protected final CVKRenderer vkRenderer;
    protected ArrayList<EventListener> eventListeners = new ArrayList<>();
    
    public CVKCanvas(VKData vkData, CVKRenderer vkRenderer) {
        super(vkData);
        this.vkRenderer = vkRenderer;
        this.addComponentListener(vkRenderer);
    }
   
    
    public void InitSurface() {
        //super.paint(null);
        parentAdded = true;
    }
    
    public void addEventListener(EventListener listener) {
        // TODO_TT: eventify this
        eventListeners.add(listener);
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
        boolean ret = super.requestFocusInWindow();
        parentAdded = true;
        return ret;
    }
    
    @Override
    public void initVK() {
        vkRenderer.Initialise(this.surface);
    }
    
    @Override
    public void paintVK() {
        // This will be called by AWTVKCanvas during initialisation but before
        // CVKRenderer is ready to use.  platformCanvas is private in our parent 
        // so this is the only way to complete the surface initialisation.  
        System.out.printf("Frame %d\n", ++frameNo);       
        vkRenderer.Display();
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
