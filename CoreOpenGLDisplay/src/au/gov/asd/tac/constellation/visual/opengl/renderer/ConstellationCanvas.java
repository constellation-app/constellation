/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import java.util.EventListener;
import org.lwjgl.vulkan.awt.AWTVKCanvas;


import javax.swing.SwingUtilities;
import org.lwjgl.vulkan.awt.VKData;
import org.lwjgl.vulkan.VkPhysicalDevice;


/**
 *
 * @author TheTimurid
 */
public class ConstellationCanvas extends AWTVKCanvas{
    
    static int frameNo = 0;
    protected long handle = 0;
    static float red = 0.0f;
    static float green = 0.5f;
    static float blue = 1.0f;
    
    public ConstellationCanvas(VKData vkData) {
        super(vkData);
    }
    
    public void addEventListener(EventListener listener) {
        // TODO_TT: eventify this
    }
    
    @Override
    public void initVK() {
        @SuppressWarnings("unused")
        long surface = this.surface;
        // Do something with surface...      
        
        ;
    }
    
    @Override
    public void paintVK() {
        System.out.printf("Frame %d\n", ++frameNo);
        
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
