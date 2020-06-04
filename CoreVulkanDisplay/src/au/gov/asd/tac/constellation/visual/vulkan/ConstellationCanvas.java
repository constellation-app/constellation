/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.vulkan;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.logging.Logger;
import org.lwjgl.vulkan.awt.AWTVKCanvas;

import javax.swing.SwingUtilities;
import org.lwjgl.vulkan.awt.VKData;



public class ConstellationCanvas extends AWTVKCanvas{
    private static final Logger LOGGER = Logger.getLogger("VKRenderer");
    private boolean parentAdded = false;
    static int frameNo = 0;
    protected long handle = 0;
    static float red = 0.0f;
    static float green = 0.5f;
    static float blue = 1.0f;
    
    protected final VKRenderer vkRenderer;
    protected ArrayList<EventListener> eventListeners = new ArrayList<>();
    
    public ConstellationCanvas(VKData vkData, VKRenderer vkRenderer) {
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
    
    /**  <P>

      <B>Overrides:</B>
      <DL><DD><CODE>reshape</CODE> in class <CODE>java.awt.Component</CODE></DD></DL> */
//    @SuppressWarnings("deprecation")
//    @Override
//    public void reshape(final int x, final int y, final int width, final int height) {
//        Rectangle current = this.getBounds();
//        super.reshape(x, y, width, height);
//        if (current.width != width || current.height != height) {
//            
//        }
//        Rectangle newBounds = new Rectangle(x, y, width, height);
//        LOGGER.log(Level.INFO, "Canvas resized from {0} to {1}", new Object[]{currentBounds, newBounds});
//    }   
    
    @Override
    public void paint(final Graphics g) {
        if (parentAdded) {
            super.paint(g);
        }
    }
    
    @Override
    public void initVK() {
        vkRenderer.InitVKRenderer(this.surface);
    }
    
    @Override
    public void paintVK() {
        // This will be called by AWTVKCanvas during initialisation but before
        // VKRenderer is ready to use.  platformCanvas is private in our parent 
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
