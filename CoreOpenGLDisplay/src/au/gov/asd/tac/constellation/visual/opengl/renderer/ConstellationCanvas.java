/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.opengl.GL;


/**
 *
 * @author skitz
 */
public class ConstellationCanvas extends AWTGLCanvas{
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
        
    }
}
