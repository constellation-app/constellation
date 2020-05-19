/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFW;


/**
 *
 * @author skitz
 */
public class ConstellationCanvas extends AWTGLCanvas{
    
    public GL30 gl;
    static int frameNo = 0;
    
    public ConstellationCanvas(GLData glData) {
        super(glData);
    }
    
    @Override
    public void initGL() {
        // COMMENT SOURCE: https://www.lwjgl.org/guide
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        
        GL.createCapabilities();      
    }

    @Override
    public void paintGL() {
        
        System.out.printf("Frame %d\n", ++frameNo);
        GL30.glClearColor(0.3f, 0.4f, 0.5f, 1);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
    }
}
