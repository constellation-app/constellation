/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.visual.vulkan.renderables;

import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKScene;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SINT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.LoadFileToDirectBuffer;

public class CVKFPSRenderable extends CVKTextForegroundRenderable{
    protected final CVKScene scene;
    protected long pVertexShader = VK_NULL_HANDLE;
    protected long pGeometryShader = VK_NULL_HANDLE;
    protected long pFragmentShader = VK_NULL_HANDLE;
    
    
    private static class Vertex {
        private static final int SIZEOF = 2 * Integer.BYTES + 4 * Float.BYTES;
        private static final int OFFSETOF_DATA = 0;
        private static final int OFFSET_BKGCLR = 2 * Integer.BYTES;
        private static final int BINDING = 0;

        private int[] data = new int[2];
        private Vector4f backgroundIconColor = new Vector4f();

        public Vertex(int[] inData, Vector4f inColour) {
            data = inData;
            backgroundIconColor = inColour;
        }

        /**
         * A VkVertexInputBindingDescription defines the rate at which data is
         * consumed by vertex shader (per vertex or per instance).  
         * The input rate determines whether to move to the next data entry after
         * each vertex or after each instance.
         * The binding description also defines the vertex stride, the number of
         * bytes that must be stepped from vertex n-1 to vertex n.
         * 
         * @return Binding description for the FPS vertex type
         */
        private static VkVertexInputBindingDescription.Buffer GetBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            // If we bind multiple vertex buffers with different descriptions
            // this is the index of this description occupies in the array of
            // bound descriptions.
            bindingDescription.binding(BINDING);
            bindingDescription.stride(Vertex.SIZEOF);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            return bindingDescription;
        }

        
        /**
         * A VkVertexInputAttributeDescription describes each element int the
         * vertex buffer.
         * binding:  matches the binding member of VkVertexInputBindingDescription
         * location: corresponds to the layout(location = #) in the vertex shader
         *           for this element (0 for data, 1 for bkgClr).
         * format:   format the shader will interpret this as.
         * offset:   bytes from the start of the vertex this attribute starts at
         * 
         * @return 
         */
        private static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // data
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32_SINT);
            posDescription.offset(OFFSETOF_DATA);

            // backgroundIconColor
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            colorDescription.offset(OFFSET_BKGCLR);

            return attributeDescriptions.rewind();
        }

    }    
    
    
    public CVKFPSRenderable(CVKScene inScene) {
        scene = inScene;
    }
    
    
    @Override
    public int CreatePipeline() {
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {
            // prepare vertex attributes

            //From the GL FPSBatcher and FPSRenderable and shaders:
            // 1 vertex per digit.
            // Vert inputs:
            // int[2] data {icon indexes (encoded to int), digit index * 4)
            // float[4] backgroundIconColor
            // Vert outputs:
            // flat out ivec2 gData; this is data passed through
            // out mat4 gBackgroundIconColor; backgroundIconColor in a 4x4 matrix
            // flat out float gRadius; 1 if visible, -1 otherwise
            // gl_Position = mvMatrix * vec4(digitPosition, 1); where digitPosition is (digit index * 4, 0, 0)

            // A bunch of uniforms:
            // SimpleIcon.vs:
            // uniform mat4 mvMatrix;
            // uniform float visibilityLow;
            // uniform float visibilityHigh;
            // uniform float offset;

            // SimpleIcon.gs:
            // Input:
            // uniform mat4 pMatrix;
            // uniform float pixelDensity;
            // uniform float pScale;     
            // Ouput:
            // flat out mat4 iconColor;
            // noperspective centroid out vec3 textureCoords;
            // layout(triangle_strip, max_vertices=28) out;            
        }
        return ret;
    }
    
    
    @Override
    public int DestroyPipeline() {
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {
            
        }
        return ret;
    }
    
    
    @Override
    public int SwapChainRezied() {
        int ret = DestroyPipeline();
        if (VkSucceeded(ret)) {
            ret = CreatePipeline();
        }
        return ret;
    }
        
    @Override
    public int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        // load shader (can probably be done earlier)
        try {
            ByteBuffer vsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.vs.spv");
            ByteBuffer strGSBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.gs.spv");
            ByteBuffer strFSBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.fs.spv");
            
            long hVS = CVKShaderUtils.createShaderModule(vsBytes, cvkDevice.GetDevice());
            long hGS = CVKShaderUtils.createShaderModule(strGSBytes, cvkDevice.GetDevice());
            long hFS = CVKShaderUtils.createShaderModule(strFSBytes, cvkDevice.GetDevice());
            
        } catch (IOException e) {
            //TODO_TT
        }
        
        return ret;
    }    
}
