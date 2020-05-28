/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import java.util.EventListener;

/**
 *
 * @author TheTimurid
 */
public interface STUB_GLEventListener extends EventListener{
    public void init(final STUB_GLAutoDrawable drawable);
    public void dispose(final STUB_GLAutoDrawable drawable);
    public void display(final STUB_GLAutoDrawable drawable);
    public void reshape(final STUB_GLAutoDrawable drawable, final int x, final int y, final int width, final int height);
}
