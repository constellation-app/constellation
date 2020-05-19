/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL.*;
import javax.swing.SwingUtilities;

/**
 *
 * @author TheTimurid
 */
public class ConstellationCanvas extends AWTGLCanvas{
    
    public GL30 gl;
    static int frameNo = 0;
    protected long handle = 0;
    static float red = 0.0f;
    static float green = 0.5f;
    static float blue = 1.0f;
    
    public ConstellationCanvas(GLData glData) {
        super(glData);
    }
    
    @Override
    public void initGL() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }       
        
        // COMMENT SOURCE: https://www.lwjgl.org/guide
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCaptabilities instance and makes the OpenGL
        // bindings available for use.    
        createCapabilities();      
    }
    
    @Override
    public void paintGL() {
        System.out.printf("Frame %d\n", ++frameNo);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
                return;
        }
        float aspect = (float) w / h;
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glViewport(0, 0, w, h);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor3f(red, green, blue);
        GL11.glVertex2f(-0.75f / aspect, 0.0f);
        GL11.glVertex2f(0, -0.75f);
        GL11.glVertex2f(+0.75f / aspect, 0);
        GL11.glVertex2f(0, +0.75f);
        GL11.glEnd();
        swapBuffers();
        
        red += 0.1f;
        blue += 0.1f;
        green += 0.1f;
        
        if (red > 1.0f)
            red = 0.0f;
        
        if (blue > 1.0f)
            blue = 0.0f;
        
        if (green > 1.0f)
            green = 0.0f;
    }
    
    @Override
    public void repaint() {
        if (SwingUtilities.isEventDispatchThread()) {
                render();
        } else {
                SwingUtilities.invokeLater(() -> render());
        }
    }

}
