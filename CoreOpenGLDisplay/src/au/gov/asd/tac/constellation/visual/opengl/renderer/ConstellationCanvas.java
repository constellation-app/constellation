/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import java.util.EventListener;
import org.lwjgl.vulkan.awt.AWTVKCanvas;
import org.lwjgl.vulkan.VkInstance;

import javax.swing.SwingUtilities;
import org.lwjgl.vulkan.awt.VKData;


/**
 *
 * @author TheTimurid
 */
public class ConstellationCanvas extends AWTVKCanvas{
    
    private boolean initialised = false;
    static int frameNo = 0;
    protected long handle = 0;
    static float red = 0.0f;
    static float green = 0.5f;
    static float blue = 1.0f;
    
    static VkInstance vkInstance = null;
    
    public ConstellationCanvas(VKData vkData) {
        super(vkData);
    }
    
    public void InitSurface() {
        super.paint(null);
        initialised = true;
    }
    
    public void addEventListener(EventListener listener) {
        // TODO_TT: eventify this
    }
    
    @Override
    public void initVK() {
        System.out.printf("initVK");

    }
    
    @Override
    public void paintVK() {
        // This will be called by AWTVKCanvas during initialisation but before
        // VKRenderer is ready to use.  platformCanvas is private in our parent 
        // so this is the only way to complete the surface initialisation.  
        if (initialised) {
            System.out.printf("Frame %d\n", ++frameNo);
        }
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
