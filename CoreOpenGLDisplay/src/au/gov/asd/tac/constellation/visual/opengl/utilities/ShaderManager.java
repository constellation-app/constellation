/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import com.jogamp.opengl.GL3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages some stock shaders use by the OpenGL SuperBible.
 * <p>
 * For applications, these shaders are pretty much irrelevant. The only thing of
 * use are the shader attribute ids and fragment shader color numbers. (Maybe
 * these should be moved into their own class?)
 *
 * @author algol
 */
public class ShaderManager {

    static class ShaderLookupEntry {

        String vertexShaderName;
        String fragShaderName;
        int shaderId;
    }

//    // Maximum length of shader name.
//    //    public static final int MAX_SHADER_NAME_LENGTH = 64;
    // Stock shader ids.
    public static final int SHADER_IDENTITY = 0;
    public static final int SHADER_FLAT = 1;
    public static final int SHADER_SHADED = 2;
    public static final int SHADER_DEFAULT_LIGHT = 3;
    public static final int SHADER_POINT_LIGHT_DIFF = 4;
    public static final int SHADER_TEXTURE_REPLACE = 5;
    public static final int SHADER_TEXTURE_MODULATE = 6;
    public static final int SHADER_TEXTURE_POINT_LIGHT_DIFF = 7;
    public static final int SHADER_TEXTURE_RECT_REPLACE = 8;
    public static final int SHADER_LAST = 9;

    // Shader attribute ids.
    public static final int ATTRIBUTE_VERTEX = 0;
    public static final int ATTRIBUTE_COLOR = 1;
    public static final int ATTRIBUTE_NORMAL = 2;
    public static final int ATTRIBUTE_DATAA = 3;
    public static final int ATTRIBUTE_DATAB = 4;
    public static final int ATTRIBUTE_TEXTURE0 = 5;
    public static final int ATTRIBUTE_TEXTURE1 = 6;
    public static final int ATTRIBUTE_TEXTURE2 = 7;
    public static final int ATTRIBUTE_TEXTURE3 = 8;
    public static final int ATTRIBUTE_DATAA_INT4 = 9;
    public static final int ATTRIBUTE_DATAA_INT1 = 10;
    public static final int ATTRIBUTE_LAST = 11;

    // Fragment shader color numbers.
    public static final int FRAG_BASE = 100;

    private final int[] stockShaders;
    private final ArrayList<ShaderLookupEntry> shaderTable;
    private final Properties shaderMap;
    
    //other constants
    private static final String V_COLOR = "vColor";
    private static final String V_VERTEX = "vVertex";
    private static final String OUT_COLOR = "outColor";

    public ShaderManager() {
        stockShaders = new int[SHADER_LAST];
        shaderTable = new ArrayList<>();
        Properties tShaderMap = null;
        try {
            tShaderMap = loadShaders();
        } catch (IOException ex) {
            Logger.getLogger(ShaderManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        shaderMap = tShaderMap;
    }

    /**
     * Read shader sources from a Properties-like file.
     *
     * @return A Properties instance containing the shader sources keyed by
     * their names.
     * @throws IOException
     */
    private static Properties loadShaders() throws IOException {
        final Properties shaderMap = new Properties();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(ShaderManager.class.getResourceAsStream("shaders.txt"), StandardCharsets.UTF_8.name()));
        String key = null;
        String value = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().startsWith("//")) {
                if (line.trim().endsWith("=")) {
                    if (key != null) {
                        shaderMap.setProperty(key, value);
                    }
                    key = line.substring(0, line.length() - 1).trim();
                    value = "";
                } else if (line.length() != 0) {
                    value += line + "\n";
                }
            }
        }

        return shaderMap;
    }

    public void dispose(final GL3 gl) {
        if (stockShaders[0] != 0) {
            for (int i = 0; i < stockShaders.length; i++) {
                gl.glDeleteProgram(stockShaders[i]);
            }

            for (int i = 0; i < shaderTable.size(); i++) {
                gl.glDeleteProgram(shaderTable.get(i).shaderId);
            }
        }
    }

    public void initialiseStockShaders(final GL3 gl) {
        stockShaders[SHADER_IDENTITY] = GLTools.loadShaderSourceWithAttributes(gl, "SHADER_IDENTITY",
                shaderMap.getProperty("SHADER_IDENTITY_VS"),
                null,
                shaderMap.getProperty("SHADER_IDENTITY_FS"),
                ATTRIBUTE_VERTEX, V_VERTEX,
                FRAG_BASE, OUT_COLOR);

        stockShaders[SHADER_FLAT] = GLTools.loadShaderSourceWithAttributes(gl, "SHADER_FLAT",
                shaderMap.getProperty("SHADER_FLAT_VS"),
                null,
                shaderMap.getProperty("SHADER_FLAT_FS"),
                ATTRIBUTE_VERTEX, V_VERTEX,
                FRAG_BASE, OUT_COLOR);

        stockShaders[SHADER_POINT_LIGHT_DIFF] = GLTools.loadShaderSourceWithAttributes(gl, "SHADER_POINT_LIGHT_DIFF",
                shaderMap.getProperty("SHADER_POINT_LIGHT_DIFF_VS"),
                null,
                shaderMap.getProperty("SHADER_POINT_LIGHT_DIFF_FS"),
                ATTRIBUTE_VERTEX, V_VERTEX,
                ATTRIBUTE_NORMAL, "vNormal",
                FRAG_BASE, OUT_COLOR);

        stockShaders[SHADER_TEXTURE_POINT_LIGHT_DIFF] = GLTools.loadShaderSourceWithAttributes(gl, "SHADER_TEXTURE_POINT_LIGHT_DIFF",
                shaderMap.getProperty("SHADER_TEXTURE_POINT_LIGHT_DIFF_VS"),
                null,
                shaderMap.getProperty("SHADER_TEXTURE_POINT_LIGHT_DIFF_FS"),
                ATTRIBUTE_VERTEX, V_VERTEX,
                ATTRIBUTE_NORMAL, "vNormal",
                ATTRIBUTE_TEXTURE0, "vTexCoord0",
                FRAG_BASE, OUT_COLOR);
    }

    /**
     * Use one of the provided shaders and set the shader-specific uniforms.
     *
     * @param gl the current OpenGL context.
     * @param shaderId The id of the shader.
     * @param args The shader-specific uniform arguments.
     *
     * @return the id of the shader.
     */
    public int useStockShader(final GL3 gl, final int shaderId, final Object... args) {
        if (shaderId >= SHADER_LAST) {
            throw new RenderException("Invalid shader id.");
        }

        // Bind to the correct shader.
        gl.glUseProgram(stockShaders[shaderId]);

        // Set up the uniforms.
        if (shaderId == SHADER_IDENTITY) {
            // Just the color.
            int colorLoc = gl.glGetUniformLocation(stockShaders[shaderId], V_COLOR);
            float[] color = (float[]) args[0];
            gl.glUniform4fv(colorLoc, 1, color, 0);
        } else if (shaderId == SHADER_FLAT) {
            // The modelview projection matrix and the color.
            int transformLoc = gl.glGetUniformLocation(stockShaders[shaderId], "mvpMatrix");
            Matrix44f mvpMatrix = (Matrix44f) args[0];
            gl.glUniformMatrix4fv(transformLoc, 1, false, mvpMatrix.a, 0);

            int colorLoc = gl.glGetUniformLocation(stockShaders[shaderId], V_COLOR);
            float[] color = (float[]) args[1];
            gl.glUniform4fv(colorLoc, 1, color, 0);
        } else if (shaderId == SHADER_POINT_LIGHT_DIFF) {
            int modelMatrix = gl.glGetUniformLocation(stockShaders[shaderId], "mvMatrix");
            Matrix44f mvMatrix = (Matrix44f) args[0];
            gl.glUniformMatrix4fv(modelMatrix, 1, false, mvMatrix.a, 0);

            int projMatrix = gl.glGetUniformLocation(stockShaders[shaderId], "pMatrix");
            Matrix44f pMatrix = (Matrix44f) args[1];
            gl.glUniformMatrix4fv(projMatrix, 1, false, pMatrix.a, 0);

            int light = gl.glGetUniformLocation(stockShaders[shaderId], "vLightPos");
            Vector3f vLightPos = (Vector3f) args[2];
            gl.glUniform3fv(light, 1, vLightPos.a, 0);

            int colorLoc = gl.glGetUniformLocation(stockShaders[shaderId], V_COLOR);
            float[] color = (float[]) args[3];
            gl.glUniform4fv(colorLoc, 1, color, 0);
        } else if (shaderId == SHADER_TEXTURE_POINT_LIGHT_DIFF) {
            int modelMatrix = gl.glGetUniformLocation(stockShaders[shaderId], "mvMatrix");
            Matrix44f mvMatrix = (Matrix44f) args[0];
            gl.glUniformMatrix4fv(modelMatrix, 1, false, mvMatrix.a, 0);

            int projMatrix = gl.glGetUniformLocation(stockShaders[shaderId], "pMatrix");
            Matrix44f pMatrix = (Matrix44f) args[1];
            gl.glUniformMatrix4fv(projMatrix, 1, false, pMatrix.a, 0);

            int light = gl.glGetUniformLocation(stockShaders[shaderId], "vLightPos");
            Vector3f lightPos = (Vector3f) args[2];
            gl.glUniform3fv(light, 1, lightPos.a, 0);

            int colorLoc = gl.glGetUniformLocation(stockShaders[shaderId], V_COLOR);
            Vector4f color = (Vector4f) args[3];
            gl.glUniform4fv(colorLoc, 1, color.a, 0);

            int textureUnit = gl.glGetUniformLocation(stockShaders[shaderId], "textureUnit0");
            int i = (Integer) args[4];
            gl.glUniform1i(textureUnit, i);
        } else {
            throw new RenderException("Unimplemented shader.");
        }

        return stockShaders[shaderId];
    }
}
