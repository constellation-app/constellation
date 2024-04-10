/*
 * Copyright 2010-2021 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import au.gov.asd.tac.constellation.utilities.graphics.Vector2f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities; //pulled in by Windows-DPI-Scaling

/**
 * Tools for OpenGL and JOGL.
 *
 * @author algol
 */
public final class GLTools {

    private static final Logger LOGGER = Logger.getLogger(GLTools.class.getName());

    /**
     * The maximum width of an icon.
     */
    public static final int MAX_ICON_WIDTH = 256;

    /**
     * The maximum height of an icon.
     */
    public static final int MAX_ICON_HEIGHT = 256;

    /**
     * No constructor.
     */
    private GLTools() {
    }

    /**
     * Get the OpenGL version.
     *
     * @param gl the current OpenGL context.
     * @param version An int[2] to receive the version: version[0] contains
     * GL_MAJOR_VERSION, version[1] contains GL_MINOR_VERSION.
     */
    public static void getOpenGLVersion(final GL3 gl, int[] version) {
        gl.glGetIntegerv(GL2ES3.GL_MAJOR_VERSION, version, 0);
        gl.glGetIntegerv(GL2ES3.GL_MINOR_VERSION, version, 1);
    }

    /**
     * Convert a Vector3f[] to a float[].
     *
     * @param vector The vector to be converted to an array.
     *
     * @return A float[] containing the same values as the vector.
     */
    public static float[] toFloatArray(final Vector3f[] vector) {
        final float[] f = new float[vector.length * 3];
        int ix = 0;
        for (final Vector3f v : vector) {
            f[ix] = v.a[0];
            f[ix + 1] = v.a[1];
            f[ix + 2] = v.a[2];
            ix += 3;
        }

        return f;
    }

    /**
     * Convert a Vector2f[] to a float[].
     *
     * @param vector The vector to be converted to an array.
     *
     * @return A float[] containing the same values as the vector.
     */
    public static float[] toFloatArray(final Vector2f[] vector) {
        final float[] f = new float[vector.length * 2];
        int ix = 0;
        for (final Vector2f v : vector) {
            f[ix] = v.a[0];
            f[ix + 1] = v.a[1];
            ix += 2;
        }

        return f;
    }

    public static String loadFile(final Class<?> refClass, final String resourceName) throws IOException {
        final StringBuilder buf = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(refClass.getResourceAsStream(resourceName), StandardCharsets.UTF_8.name()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append(SeparatorConstants.NEWLINE);
            }
        }

        return buf.toString();
    }

    public static void loadShaderSource(final GL3 gl, final String shaderSrc, final int shader) {
        final String[] shaderParam = {shaderSrc};

        gl.glShaderSource(shader, 1, shaderParam, null, 0);
    }

    public static String getShaderLog(final GL3 gl, final int shader) {
        final int[] maxLength = new int[1];
        gl.glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, maxLength, 0);
        if (maxLength[0] == 0) {
            return "";
        }

        final byte[] buf = new byte[maxLength[0]];
        final int[] length = new int[1];
        gl.glGetShaderInfoLog(shader, maxLength[0], length, 0, buf, 0);
        final String log = new String(buf);

        return log.trim();
    }

    public static String getProgramLog(final GL3 gl, final int shader) {
        final int[] maxLength = new int[1];
        gl.glGetProgramiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, maxLength, 0);
        if (maxLength[0] == 0) {
            return "";
        }

        final byte[] buf = new byte[maxLength[0]];
        final int[] length = new int[1];
        gl.glGetProgramInfoLog(shader, maxLength[0], length, 0, buf, 0);
        final String log = new String(buf);

        return log.trim();
    }

    public static int loadShaderSourceWithAttributes(final GL3 gl, final String label, final String vertexSrc, final String geometrySrc, final String fragmentSrc, final Object... args) {
        // Temporary shader objects.
        final int vertexShader = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        final int fragmentShader = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
        final int geometryShader = geometrySrc != null ? gl.glCreateShader(GL3ES3.GL_GEOMETRY_SHADER) : -1;

        final int[] testVal = new int[1];

        // Load the shaders.
        loadShaderSource(gl, vertexSrc, vertexShader);
        LOGGER.log(Level.FINE, "VERTEXSHADERLOG::{0}::{1}", new Object[]{label, getShaderLog(gl, vertexShader)});
        loadShaderSource(gl, fragmentSrc, fragmentShader);
        LOGGER.log(Level.FINE, "FRAGMENTSHADERLOG::{0}::{1}", new Object[]{label, getShaderLog(gl, fragmentShader)});
        if (geometryShader != -1) {
            loadShaderSource(gl, geometrySrc, geometryShader);
            LOGGER.log(Level.FINE, "GEOMETRYSHADERLOG::{0}::{1}", new Object[]{label, getShaderLog(gl, geometryShader)});
        }

        // Compile the shaders.
        gl.glCompileShader(vertexShader);
        gl.glCompileShader(fragmentShader);
        if (geometryShader != -1) {
            gl.glCompileShader(geometryShader);
        }

        // Check for compile errors.
        gl.glGetShaderiv(vertexShader, GL2ES2.GL_COMPILE_STATUS, testVal, 0);
        if (testVal[0] == GL.GL_FALSE) {
            final String log = getShaderLog(gl, vertexShader);
            gl.glDeleteShader(vertexShader);
            gl.glDeleteShader(fragmentShader);
            if (geometryShader != -1) {
                gl.glDeleteShader(geometryShader);
            }
            throw new RenderException(String.format("Invalid vertex shader '%s':%n%n%s", label, log));
        }

        gl.glGetShaderiv(fragmentShader, GL2ES2.GL_COMPILE_STATUS, testVal, 0);
        if (testVal[0] == GL.GL_FALSE) {
            final String log = getShaderLog(gl, fragmentShader);
            gl.glDeleteShader(vertexShader);
            gl.glDeleteShader(fragmentShader);
            if (geometryShader != -1) {
                gl.glDeleteShader(geometryShader);
            }
            throw new RenderException(String.format("Invalid fragment shader '%s':%n%n%s", label, log));
        }

        if (geometryShader != -1) {
            gl.glGetShaderiv(geometryShader, GL2ES2.GL_COMPILE_STATUS, testVal, 0);
            if (testVal[0] == GL.GL_FALSE) {
                final String log = getShaderLog(gl, geometryShader);
                gl.glDeleteShader(vertexShader);
                gl.glDeleteShader(fragmentShader);
                gl.glDeleteShader(geometryShader);
                throw new RenderException(String.format("Invalid geometry shader '%s':%n%n%s", label, log));
            }
        }

        // Link.
        final int progid = gl.glCreateProgram();
        gl.glAttachShader(progid, vertexShader);
        gl.glAttachShader(progid, fragmentShader);
        if (geometryShader != -1) {
            gl.glAttachShader(progid, geometryShader);
        }

        // Bind attributes from the args.
        for (int i = 0; i < args.length;) {
            final int index = (Integer) args[i++];
            final String arg = (String) args[i++];
            if (index >= ShaderManager.FRAG_BASE) {
                final int fragDataLocation = index - ShaderManager.FRAG_BASE;
                gl.glBindFragDataLocation(progid, fragDataLocation, arg);
            } else {
                gl.glBindAttribLocation(progid, index, arg);
            }
        }

        gl.glLinkProgram(progid);

        // These are no longer needed.
        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);
        if (geometryShader != -1) {
            gl.glDeleteShader(geometryShader);
        }

        // Check for link errors.
        gl.glGetProgramiv(progid, GL2ES2.GL_LINK_STATUS, testVal, 0);
        if (testVal[0] == GL.GL_FALSE) {
            final String log = getProgramLog(gl, progid);
            gl.glDeleteProgram(progid);
            throw new RenderException(String.format("Invalid program link '%s':%n%n%s", label, log));
        }

        LOGGER.log(Level.FINE, "PROGRAMLOG::{0}::{1}", new Object[]{label, getProgramLog(gl, progid)});

        return progid;
    }

    /**
     * Make a sphere.
     *
     * @param gl the current OpenGL context.
     * @param sphereBatch the batch to store the vertices to.
     * @param fRadius the radius of the sphere.
     * @param iSlices the number of slices in the sphere.
     * @param iStacks the number of stacks in the sphere.
     */
    public static void makeSphere(final GL3 gl, final TriangleBatch sphereBatch, final float fRadius, final int iSlices, final int iStacks) {
        float drho = (float) Math.PI / (float) iStacks;
        float dtheta = 2.0F * (float) Math.PI / (float) iSlices;
        float ds = 1.0F / (float) iSlices;
        float dt = 1.0F / (float) iStacks;
        float t = 1.0F;
        float s;

        sphereBatch.beginMesh(iSlices * iStacks * 6);
        for (int i = 0; i < iStacks; i++) {
            float rho = (float) i * drho;
            float srho = (float) (Math.sin(rho));
            float crho = (float) (Math.cos(rho));
            float srhodrho = (float) (Math.sin(rho + drho));
            float crhodrho = (float) (Math.cos(rho + drho));

            // Many sources of OpenGL sphere drawing code uses a triangle fan
            // for the caps of the sphere. This however introduces texturing
            // artifacts at the poles on some OpenGL implementations.
            s = 0.0F;
            Vector3f[] vVertex = Vector3f.createArray(4);
            Vector3f[] vNormal = Vector3f.createArray(4);
            Vector2f[] vTexture = Vector2f.createArray(4);

            for (int j = 0; j < iSlices; j++) {
                float theta = j * dtheta;
                float stheta = (float) (-Math.sin(theta));
                float ctheta = (float) (Math.cos(theta));

                float x = stheta * srho;
                float y = ctheta * srho;
                float z = crho;

                vTexture[0].a[0] = s;
                vTexture[0].a[1] = t;
                vNormal[0].a[0] = x;
                vNormal[0].a[1] = y;
                vNormal[0].a[2] = z;
                vVertex[0].a[0] = x * fRadius;
                vVertex[0].a[1] = y * fRadius;
                vVertex[0].a[2] = z * fRadius;

                x = stheta * srhodrho;
                y = ctheta * srhodrho;
                z = crhodrho;

                vTexture[1].a[0] = s;
                vTexture[1].a[1] = t - dt;
                vNormal[1].a[0] = x;
                vNormal[1].a[1] = y;
                vNormal[1].a[2] = z;
                vVertex[1].a[0] = x * fRadius;
                vVertex[1].a[1] = y * fRadius;
                vVertex[1].a[2] = z * fRadius;

                theta = ((j + 1) == iSlices) ? 0.0F : (j + 1) * dtheta;
                stheta = (float) (-Math.sin(theta));
                ctheta = (float) (Math.cos(theta));

                x = stheta * srho;
                y = ctheta * srho;
                z = crho;

                s += ds;
                vTexture[2].a[0] = s;
                vTexture[2].a[1] = t;
                vNormal[2].a[0] = x;
                vNormal[2].a[1] = y;
                vNormal[2].a[2] = z;
                vVertex[2].a[0] = x * fRadius;
                vVertex[2].a[1] = y * fRadius;
                vVertex[2].a[2] = z * fRadius;

                x = stheta * srhodrho;
                y = ctheta * srhodrho;
                z = crhodrho;

                vTexture[3].a[0] = s;
                vTexture[3].a[1] = t - dt;
                vNormal[3].a[0] = x;
                vNormal[3].a[1] = y;
                vNormal[3].a[2] = z;
                vVertex[3].a[0] = x * fRadius;
                vVertex[3].a[1] = y * fRadius;
                vVertex[3].a[2] = z * fRadius;

                sphereBatch.addTriangle(vVertex, vNormal, vTexture);

                // Rearrange for next triangle.
                vVertex[0].set(vVertex[1]);
                vNormal[0].set(vNormal[1]);
                vTexture[0].set(vTexture[1]);

                vVertex[1].set(vVertex[3]);
                vNormal[1].set(vNormal[3]);
                vTexture[1].set(vTexture[3]);

                sphereBatch.addTriangle(vVertex, vNormal, vTexture);
            }

            t -= dt;
        }
        sphereBatch.end(gl);
    }

    /**
     * Draw a torus (doughnut) at z = fZVal... torus is in xy plane.
     *
     * @param gl the current OpenGL context.
     * @param torusBatch the batch to store the vertices to.
     * @param majorRadius the major radius of the torus.
     * @param minorRadius the minor radius of the torus.
     * @param numMajor the number of slices around the major radius.
     * @param numMinor the number of slices around the minor radius.
     */
    public static void makeTorus(final GL3 gl, final TriangleBatch torusBatch, final float majorRadius, final float minorRadius, final int numMajor, final int numMinor) {
        final double majorStep = 2.0F * Math.PI / numMajor;
        final double minorStep = 2.0F * Math.PI / numMinor;

        torusBatch.beginMesh(numMajor * (numMinor + 1) * 6);
        for (int i = 0; i < numMajor; i++) {
            final double a0 = i * majorStep;
            final double a1 = a0 + majorStep;
            final float x0 = (float) Math.cos(a0);
            final float y0 = (float) Math.sin(a0);
            final float x1 = (float) Math.cos(a1);
            final float y1 = (float) Math.sin(a1);

            Vector3f[] vVertex = Vector3f.createArray(4);
            Vector3f[] vNormal = Vector3f.createArray(4);
            Vector2f[] vTexture = Vector2f.createArray(4);

            for (int j = 0; j <= numMinor; j++) {
                double b = j * minorStep;
                float c = (float) Math.cos(b);
                float r = minorRadius * c + majorRadius;
                float z = minorRadius * (float) Math.sin(b);

                // First point
                vTexture[0].a[0] = (float) (i) / (float) (numMajor);
                vTexture[0].a[1] = (float) (j) / (float) (numMinor);
                vNormal[0].a[0] = x0 * c;
                vNormal[0].a[1] = y0 * c;
                vNormal[0].a[2] = z / minorRadius;
                vNormal[0].normalize();
                vVertex[0].a[0] = x0 * r;
                vVertex[0].a[1] = y0 * r;
                vVertex[0].a[2] = z;

                // Second point
                vTexture[1].a[0] = (float) (i + 1) / (float) (numMajor);
                vTexture[1].a[1] = (float) (j) / (float) (numMinor);
                vNormal[1].a[0] = x1 * c;
                vNormal[1].a[1] = y1 * c;
                vNormal[1].a[2] = z / minorRadius;
                vNormal[1].normalize();
                vVertex[1].a[0] = x1 * r;
                vVertex[1].a[1] = y1 * r;
                vVertex[1].a[2] = z;

                // Next one over
                b = (j + 1) * minorStep;
                c = (float) Math.cos(b);
                r = minorRadius * c + majorRadius;
                z = minorRadius * (float) Math.sin(b);

                // Third (based on first)
                vTexture[2].a[0] = (float) (i) / (float) (numMajor);
                vTexture[2].a[1] = (float) (j + 1) / (float) (numMinor);
                vNormal[2].a[0] = x0 * c;
                vNormal[2].a[1] = y0 * c;
                vNormal[2].a[2] = z / minorRadius;
                vNormal[2].normalize();
                vVertex[2].a[0] = x0 * r;
                vVertex[2].a[1] = y0 * r;
                vVertex[2].a[2] = z;

                // Fourth (based on second)
                vTexture[3].a[0] = (float) (i + 1) / (float) (numMajor);
                vTexture[3].a[1] = (float) (j + 1) / (float) (numMinor);
                vNormal[3].a[0] = x1 * c;
                vNormal[3].a[1] = y1 * c;
                vNormal[3].a[2] = z / minorRadius;
                vNormal[3].normalize();
                vVertex[3].a[0] = x1 * r;
                vVertex[3].a[1] = y1 * r;
                vVertex[3].a[2] = z;

                torusBatch.addTriangle(vVertex, vNormal, vTexture);

                // Rearrange for next triangle.
                vVertex[0].set(vVertex[1]);
                vNormal[0].set(vNormal[1]);
                vTexture[0].set(vTexture[1]);

                vVertex[1].set(vVertex[3]);
                vNormal[1].set(vNormal[3]);
                vTexture[1].set(vTexture[3]);

                torusBatch.addTriangle(vVertex, vNormal, vTexture);
            }
        }
        torusBatch.end(gl);
    }

    // Load a TGA as a 2D Texture. Completely initialize the state
    public static Texture loadTexture(final GL3 gl, final InputStream in, final String ext, final int minFilter, final int magFilter, final int wrapMode) throws IOException {
        // NVS-415: Appears to be a bug in JOGL where texture provider for PNG files does not flip the texture.
        final TextureData data = TextureIO.newTextureData(gl.getGLProfile(), in, false, null);
        final Texture tex = TextureIO.newTexture(data);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, wrapMode);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, wrapMode);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, minFilter);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, magFilter);

        return tex;
    }

    /**
     * Load a BufferedImage array into a texture array.
     * <p>
     * Each BufferedImage must be of type BufferedImage.TYPE_4BYTE_ABGR. The
     * images will be loaded at 0,0 at each level of the texture array.
     * <p>
     * It appears that the images must have a row length that is a multiple of
     * four. This is probably due to the particular format we're using, and
     * could probably be worked around, but the simple fix is to check your row
     * length.
     *
     * @param gl the current OpenGL context.
     * @param textureName The name of the texture to bind to with BindTexture().
     * @param images A List of BufferedImages of type
     * BufferedImage.TYPE_4BYTE_ABGR.
     * @param maxWidth The maximum width of the images.
     * @param maxHeight The maximum height of the images.
     * @param minFilter Texture selection with TEXTURE_MIN_FILTER.
     * @param magFilter Texture selection with TEXTURE_MAG_FILTER.
     * @param wrapMode texture wrap mode with TEXTURE_WRAP_S and TEXTURE_WRAP_T.
     */
    public static void loadTextures(final GL3 gl, final int textureName, final List<BufferedImage> images, final int maxWidth, final int maxHeight, final int minFilter, final int magFilter, final int wrapMode) {
        gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, textureName);

        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_S, wrapMode);
        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_T, wrapMode);
        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MIN_FILTER, minFilter);
        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MAG_FILTER, magFilter);

        // Call glTexImage3D() to create the buffer here: we've assumed the internalformat and format.
        gl.glTexImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, GL.GL_RGBA, maxWidth, maxHeight, images.size(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);

        int i = 0;
        for (BufferedImage image : images) {
            try {
                final TextureData data = AWTTextureIO.newTextureData(gl.getGLProfile(), image, false);

                if (data.getWidth() > maxWidth || data.getHeight() > maxHeight) {
                    throw new RenderException(String.format("Image %d is too large", i));
                }

                // Images are always placed at 0,0.
                final int xoffset = 0;
                final int yoffset = 0;
                final int zoffset = i;
                gl.glTexSubImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, xoffset, yoffset, zoffset, data.getWidth(), data.getHeight(), 1, data.getPixelFormat(), GL.GL_UNSIGNED_BYTE, data.getBuffer());
                data.destroy();
            } catch (final RuntimeException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            i++;
        }
    }

    /**
     * Load an array of icon textures.
     * <p>
     * We assume that the textures being loaded are icons, and therefore are
     * roughly the same size (with a maximum of (width,height).
     * <p>
     * The array is limited to GL_MAX_ARRAY_TEXTURE_LAYERS layers. This can be
     * fairly low (512 on low-end systems), so icons are loaded into an 8x8 icon
     * matrix in each layer, thus giving a maximum of 512x8x8=32768 icons. (This
     * assumes that GL_MAX_3D_TEXTURE_SIZE is big enough to take that many
     * pixels. With the current icon size of 256x256, then
     * GL_MAX_3D_TEXTURE_SIZE must be at least 2048.)
     * <p>
     * Icons that are smaller than (width,height) are offset so they are
     * centred, so the shader can just draw the icons without worrying about
     * where in the texture they are.
     * <p>
     * It appears that the images must have a row length that is a multiple of
     * four. This is probably due to the particular format we're using, and
     * could probably be worked around, but the simple fix is to check your row
     * length.
     *
     * @param glCurrent the current OpenGL context.
     * @param icons a list of icons that need to added to the buffer.
     * @param width the width of each icon.
     * @param height the height of each icon.
     *
     * @return the id of the texture buffer.
     */
    public static int loadSharedIconTextures(final GL3 glCurrent, final List<ConstellationIcon> icons, final int width, final int height) {
        final int[] v = new int[1];
        glCurrent.glGetIntegerv(GL2ES3.GL_MAX_ARRAY_TEXTURE_LAYERS, v, 0);
        final int maxIcons = v[0] * 64;
        if (icons.size() > maxIcons) {
            final String log = """
                               ****
                               **** Warning: nIcons %d > GL_MAX_ARRAY_TEXTURE_LAYERS %d
                               ****
                               """.formatted(icons.size(), maxIcons);
            LOGGER.log(Level.INFO, log);
        }

        final int nIcons = Math.min(icons.size(), maxIcons);

        glCurrent.getContext().release();
        final GL3 gl = (GL3) SharedDrawable.getSharedAutoDrawable().getGL();
        final int result = gl.getContext().makeCurrent();
        if (result == GLContext.CONTEXT_NOT_CURRENT) {
            glCurrent.getContext().makeCurrent();
            throw new RenderException("Could not make texture context current.");
        }

        final int[] textureName = new int[1];
        try {
            textureName[0] = SharedDrawable.getIconTextureName();
            gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, textureName[0]);
            gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, GL.GL_RGBA, width * 8, height * 8, (nIcons + 63) / 64, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);

            final Iterator<ConstellationIcon> iconIterator = icons.iterator();
            for (int i = 0; i < nIcons; i++) {
                final ConstellationIcon icon = iconIterator.next();
                try {
                    BufferedImage iconImage = icon.buildBufferedImage();

                    if (iconImage != null) {
                        // Appears to be a bug in JOGL where texture provider for PNG files does not flip the texture.
                        final TextureData data = AWTTextureIO.newTextureData(gl.getGLProfile(), iconImage, false);

                        if (data.getWidth() > width || data.getHeight() > height) {
                            throw new RenderException(String.format("Image %d is too large (width %d>%d, height %d>%d)", i, data.getWidth(), width, data.getHeight(), height));
                        }

                        // Offset each icon into an 8x8 matrix.
                        // There are multiple icons in each
                        // Allow for icons that are smaller than width,height.
                        final int xoffset = (width - data.getWidth()) / 2 + (width * (i & 7));
                        final int yoffset = (height - data.getHeight()) / 2 + (height * ((i >>> 3) & 7));
                        final int zoffset = i >>> 6;
                        gl.glTexSubImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, xoffset, yoffset, zoffset, data.getWidth(), data.getHeight(), 1, data.getPixelFormat(), GL.GL_UNSIGNED_BYTE, data.getBuffer());
                        data.destroy();
                    }
                } catch (final RuntimeException ex) {
                    final String log = String.format("##%n## GLTools.loadTextures() icon %d throwable: %s%n##%n", i, ex);
                    LOGGER.log(Level.SEVERE, log);
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        } finally {
            gl.getContext().release();
            glCurrent.getContext().makeCurrent();
        }

        return textureName[0];
    }

    // These icons must be permanently present at these pre-defined indexes.
    // The shaders expect them to be there.
    public static final int HIGHLIGHTED_ICON_INDEX = 0;
    public static final String HIGHLIGHTED_ICON = DefaultIconProvider.HIGHLIGHTED.getExtendedName();
    public static final int UNKNOWN_ICON_INDEX = 1;
    public static final String UNKNOWN_ICON = DefaultIconProvider.UNKNOWN.getExtendedName();

    // Icons for drawing loops.
    public static final int LOOP_DIRECTED_ICON_INDEX = 2;
    public static final String LOOP_DIRECTED_ICON = DefaultIconProvider.LOOP_DIRECTED.getExtendedName();
    public static final int LOOP_UNDIRECTED_ICON_INDEX = 3;
    public static final String LOOP_UNDIRECTED_ICON = DefaultIconProvider.LOOP_UNDIRECTED.getExtendedName();

    // Noise indicator to be drawn when there are too many icons for the texture array.
    public static final int NOISE_ICON_INDEX = 4;
    public static final String NOISE_ICON = DefaultIconProvider.NOISE.getExtendedName();

    // Transparency.
    public static final int TRANSPARENT_ICON_INDEX = 5;
    public static final String TRANSPARENT_ICON = DefaultIconProvider.TRANSPARENT.getExtendedName();

    /**
     * Singleton holder of list of used icons.
     * <p>
     * As new icon names are seen in the graph, they are added to this data
     * structure, which maintains a mapping between an icon name and its index.
     * <p>
     * Note: a LinkedHashMap is used here to maintain the insertion order.
     * Because the texture array that uses this is shared between multiple
     * drawables, and some icons must be at predefined indexes (for instance
     * "highlighted" must be at index 0, "unknown" at index 1), the order of the
     * icons must not change: a drawable that uses an icon at index 17 (for
     * example) can't have that icon changing due to a different drawable being
     * created. Therefore, whenever new icons are added, they are always
     * appended.
     */
    public static final class LoadedIconHelper {

        private final LinkedHashMap<String, Integer> loadedIcons;
        private boolean requiresReload;

        // We use a texture array to store icons.
        // If we have too many icons (more than GL_MAX_ARRAY_TEXTURE_LAYERS), then OpenGL won't like it.
        // What happens next is probably driver/hardware dependent, but one possibility is just displaying
        // whatever icon it feels like. Yuck.
        // We don't want this to happen, so we have a maximum number of icons.
        // If an attempt is made to add any more icons, you'll get the noise icon as an indicator if icon overflow.
        private int maxNIcons;

        private LoadedIconHelper() {
            maxNIcons = Integer.MAX_VALUE;
            loadedIcons = new LinkedHashMap<>();

            // These icons are guaranteed to be in the iconMap in this order.
            // They must be at these pre-defined indices so other code (in particular the shaders) can use them.
            // See *_INDEX constants above.
            for (final String iconName : new String[]{HIGHLIGHTED_ICON, UNKNOWN_ICON, LOOP_DIRECTED_ICON, LOOP_UNDIRECTED_ICON, NOISE_ICON, TRANSPARENT_ICON}) {
                addIcon(iconName);
            }
        }

        /**
         * Add an icon label to the index map and return the index of that icon.
         * <p>
         * If the label already exists, return the existing index. Null labels
         * and empty labels (ie "") return the index of the transparent icon.
         * Therefore, a valid icon index (&lt;=0 &amp;&amp; &gt;=MAX_ICON_INDEX)
         * will always be returned.
         *
         * @param label The index of an icon.
         *
         * @return the index of the icon.
         */
        public int addIcon(final String label) {
            final Integer iconIndex = loadedIcons.get(label);
            if (iconIndex == null) {
                final int index = loadedIcons.size();
                if (index >= maxNIcons) {
                    // Too many icons: return NOISE icon.
                    return NOISE_ICON_INDEX;
                }

                loadedIcons.put(label, index);
                requiresReload = true;
                return index;
            }

            return iconIndex;
        }

        public boolean isEmpty() {
            return loadedIcons.isEmpty();
        }

        public int size() {
            return loadedIcons.size();
        }

        public void reset() {
            loadedIcons.clear();
            requiresReload = false;
        }

        public int getMaximumIcons() {
            return maxNIcons;
        }

        public void setMaximumTextureLayers(final int maxTextureLayers) {
            this.maxNIcons = maxTextureLayers * 64;
        }
    }

    public static final LoadedIconHelper LOADED_ICON_HELPER = new LoadedIconHelper();

    /**
     * Load the icon textures into a texture array.
     * <p>
     * This texture array is shared amongst all of the OpenGL drawables, so once
     * an icon has been added to the list of icons, its index must not change.
     *
     * @param glCurrent the current OpenGL context.
     * @param width the width of each icon.
     * @param height the height of each icon.
     *
     * @return the id of the texture buffer.
     */
    public static int loadSharedIconTextures(final GL3 glCurrent, final int width, final int height) {
        // Do we have new icons to be loaded?
        // If so, reload the lot.
        if (LOADED_ICON_HELPER.requiresReload) {
            final int nIcons = LOADED_ICON_HELPER.loadedIcons.size();
            final List<ConstellationIcon> iconList = new ArrayList<>(nIcons);
            for (int i = 0; i < nIcons; i++) {
                iconList.add(null);
            }

            for (final Map.Entry<String, Integer> entry : LOADED_ICON_HELPER.loadedIcons.entrySet()) {
                final String iconLabel = entry.getKey();
                final int iconIndex = entry.getValue();

                ConstellationIcon icon = IconManager.getIcon(iconLabel);
                if (icon == null) {
                    icon = DefaultIconProvider.UNKNOWN;
                }

                iconList.set(iconIndex, icon);
            }

            LOADED_ICON_HELPER.requiresReload = false;

            final long t0 = System.currentTimeMillis();
            final int iconTextureArray = loadSharedIconTextures(glCurrent, iconList, width, height);
            final long t1 = System.currentTimeMillis();
            LOGGER.log(Level.FINE, "Time to load icon textures: {0} msec\n", (t1 - t0));

            return iconTextureArray;
        }

        return SharedDrawable.getIconTextureName();
    }

    /**
     * Obtain error information.
     * <p>
     * This is a low-level alternative to using a debug GL context. See the
     * OpenGL specification 3.3 Core section 2.5.
     *
     * @param gl the current OpenGL context.
     * @param msg the message that will be printed out if an error has occurred.
     */
    public static void checkError(final GL3 gl, final String msg) {
        while (true) {
            final int err = gl.glGetError();
            if (err == GL.GL_NO_ERROR || msg == null) {
                return;
            }
            String errtext;
            errtext = switch (err) {
                case GL.GL_INVALID_ENUM -> "invalid enum";
                case GL.GL_INVALID_VALUE -> "invalid value";
                case GL.GL_INVALID_OPERATION -> "invalid operation";
                case GL.GL_OUT_OF_MEMORY -> "out of memory";
                case GL.GL_INVALID_FRAMEBUFFER_OPERATION -> "invalid framebuffer operation";
                default -> Integer.toString(err);
            };
            LOGGER.log(Level.SEVERE, "OpenGL error {0}: {1} ({2})", new Object[]{msg, errtext, err});
        }
    }

    /**
     * Check that a framebuffer is complete.
     * <p>
     * See the OpenGL specification 3.3 Core section 4.4.4.
     *
     * @param gl the current OpenGL context.
     * @param msg msg the message that will be printed out if an error has
     * occurred.
     */
    public static void checkFramebufferStatus(final GL3 gl, final String msg) {
        int fboStatus = gl.glCheckFramebufferStatus(GL.GL_DRAW_FRAMEBUFFER);
        if (fboStatus == GL.GL_FRAMEBUFFER_COMPLETE) {
            return;
        }

        String errtext = "";
        if (fboStatus == GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
            errtext = "framebuffer incomplete missing attachment";
        } else if (fboStatus == GL.GL_FRAMEBUFFER_UNSUPPORTED) {
            errtext = "framebuffer unsupported";
        } else {
            // Do nothing
        }
        LOGGER.log(Level.SEVERE, "**** Framebuffer error %{0}: %{1} ({2})", new Object[]{msg, errtext, fboStatus});
    }

    /**
     * Windows-DPI-Scaling
     *
     * JOGL version 2.3.2 on Windows doesn't correctly support DPI
     * scaling.setSurfaceScale() is not overridden in WindowsJAWTWindow so it is
     * not possible to scale the the canvas and mouse events at this level. It
     * should be noted that it is overridden in MacOSXJAWTWindow. Where manual
     * scaling is required the caller will need to scale each GL viewport and
     * ensure hit tests take that size into account.
     *
     * If JOGL is ever fixed or another solution is found, either change this
     * function to return false or look for any code that calls it and remove
     * it.
     *
     * @return
     */
    public static boolean needsManualDPIScaling() {
        return Utilities.isWindows();
    }
}
