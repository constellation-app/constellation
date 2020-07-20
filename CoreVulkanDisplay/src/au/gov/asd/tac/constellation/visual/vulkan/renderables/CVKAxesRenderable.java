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

import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.SPIRV;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.ShaderKind.FRAGMENT_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.ShaderKind.VERTEX_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.compileShaderFile;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils.ShaderKind.GEOMETRY_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class CVKAxesRenderable extends CVKRenderable {
    
    // Compiled Shader modules
    protected static long vertShaderModule = VK_NULL_HANDLE;
    protected static long fragShaderModule = VK_NULL_HANDLE;
    protected static long geomShaderModule = VK_NULL_HANDLE;
    
    // Compiled Shaders
    protected static SPIRV vertShaderSPIRV;
    protected static SPIRV fragShaderSPIRV;
    protected static SPIRV geomShaderSPIRV;
       
    // FROM AxesRenderable...
    private static final float LEN = 0.5f;
    private static final float HEAD = 0.05f;
    private static final int AXES_OFFSET = 50;
    private static final Vector4f XCOLOR = new Vector4f(1, 0.5f, 0.5f, 0.75f);
    private static final Vector4f YCOLOR = new Vector4f(0.5f, 1, 0.5f, 0.75f);
    private static final Vector4f ZCOLOR = new Vector4f(0, 0.5f, 1, 0.75f);
    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    private static final Matrix44f IDENTITY_44F = Matrix44f.identity();


    // All the verts are manually calculated for the Axes in CreateVertexBuffer():
    // - (3 x 2) = X, Y, Z lines for Axes
    // - (4, 4, 4) =  Arrows at the end of the Axes
    // - (4, 6, 6) = X, Y, Z labels
    private static final int NUMBER_OF_VERTICES = 3 * 2 + 4 + 4 + 4 + 4 + 6 + 6;
    
    private Vector3f topRightCorner = new Vector3f();
    private float pScale = 0;
    
    private Vertex[] vertices = new Vertex[NUMBER_OF_VERTICES];
    private VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    protected List<CVKBuffer> vertexUniformBuffers = null;
    protected List<CVKBuffer> vertexBuffers = null;
    private List<CVKCommandBuffer> commandBuffers = null;
    
    private List<Long> pipelines = null;
    private List<Long> pipelineLayouts = null;
    private LongBuffer pDescriptorSets = null;
    private static long hDescriptorLayout = VK_NULL_HANDLE;  
    
    
    private static class Vertex {

        private static final int SIZEOF = (3 + 4) * Float.BYTES;
        private static final int OFFSETOF_POS = 0;
        private static final int OFFSETOF_COLOR = 3 * Float.BYTES;

        private final Vector3f vertex;
        private final Vector4f color;

        public Vertex(final Vector3f vertex, final Vector4f color) {
            this.vertex = vertex;
            this.color = color;
        }
        
        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
            for(Vertex vertex : vertices) {
                buffer.putFloat(vertex.vertex.getX());
                buffer.putFloat(vertex.vertex.getY());
                buffer.putFloat(vertex.vertex.getZ());
                                
                buffer.putFloat(vertex.color.a[0]);
                buffer.putFloat(vertex.color.a[1]);
                buffer.putFloat(vertex.color.a[2]);
                buffer.putFloat(vertex.color.a[3]);
            }
        }      

        private static VkVertexInputBindingDescription.Buffer getBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            bindingDescription.binding(0);
            bindingDescription.stride(Vertex.SIZEOF);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            return bindingDescription;
        }

        private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // Vertex
            VkVertexInputAttributeDescription vertexDescription = attributeDescriptions.get(0);
            vertexDescription.binding(0);
            vertexDescription.location(0);
            vertexDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            vertexDescription.offset(OFFSETOF_POS);

            // Color
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(0);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            colorDescription.offset(OFFSETOF_COLOR);

            return attributeDescriptions.rewind();
        }
    }
    
    private static class VertexUniformBufferObject {
        private static final int SIZEOF = 16 * Float.BYTES;
        public Matrix44f mvpMatrix;
      
        public VertexUniformBufferObject() {
            mvpMatrix = new Matrix44f();
        }
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(mvpMatrix.get(iRow, iCol));
                }
            }
        }         
    }     
    
    
    public CVKAxesRenderable(CVKVisualProcessor visualProcessor) {
        parent = visualProcessor;
      
    }
    
    @Override
    public void Destroy() {
        DestroyCommandBuffers();
        DestroyVertexBuffers();
        DestroyUniformBuffers();
        DestroyDescriptorSets();
        DestroyPipeline();
        DestroyPipelineLayouts();
              
        CVKAssert(pipelines == null);
        CVKAssert(pipelineLayouts == null);
        CVKAssert(pDescriptorSets == null);
        CVKAssert(vertexUniformBuffers == null);
        CVKAssert(vertexBuffers == null);
        CVKAssert(commandBuffers == null);        
    }
    
    
    private void DestroyCommandBuffers() {
        if (null != commandBuffers && commandBuffers.size() > 0) {
            commandBuffers.forEach(el -> {el.Destroy();});
            commandBuffers.clear();
            commandBuffers = null;
        }       
    }
    
    private void DestroyVertexBuffers() {
        if (null != vertexBuffers) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }
    }
    
    private void DestroyUniformBuffers() {
        if (vertexUniformBuffers != null) {
            vertexUniformBuffers.forEach(el -> {el.Destroy();});
            vertexUniformBuffers = null;
        }
    }
    
    private void DestroyDescriptorSets(){
        // TODO HYDRA
        //pDescriptorSets empty list
                
        //vkDestroyDescriptorSetLayout(cvkDevice.GetDevice(), hDescriptorLayout, null);
    }
    
    private void DestroyPipeline() {     
        if (pipelines != null) {
            for (int i = 0; i < pipelines.size(); ++i) {
                vkDestroyPipeline(cvkDevice.GetDevice(), pipelines.get(i), null);
                pipelines.set(i, VK_NULL_HANDLE);
            }
            pipelines.clear();
            pipelines = null;
        }       
    }
    
    private void DestroyPipelineLayouts() {
        if (pipelineLayouts != null) {
            for (int i = 0; i < pipelineLayouts.size(); ++i) {
                vkDestroyPipelineLayout(cvkDevice.GetDevice(), pipelineLayouts.get(i), null);
                pipelineLayouts.set(i, VK_NULL_HANDLE);
            }
            pipelineLayouts.clear();
            pipelineLayouts = null;
        }
    }
    
    @Override
    public int GetVertexCount(){return NUMBER_OF_VERTICES; }
    
    
    public int Init(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
 
        // TODO HYDRA - Do we need an init?       
        return ret;
    }
    
    private float calculateProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.a[1] - proj1.a[1];

        return 25.0f / yScale;
    } 
    
    
    private int CreateUniformBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain){
        int ret = VK_SUCCESS;
        
        int imageCount = cvkSwapChain.GetImageCount(); 
        
        vertexUniformBuffers = new ArrayList<>();     
        for (int i = 0; i < imageCount; ++i) {   
            CVKBuffer vertUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                          VertexUniformBufferObject.SIZEOF,
                                                          VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            
            vertUniformBuffer.DEBUGNAME = String.format("CVKAxesRenderable vertexUniformBuffer %d", i);               
            vertexUniformBuffers.add(vertUniformBuffer);            
        }
        
        return UpdateUniformBuffers(stack, cvkSwapChain);
    }
  
    
    private int UpdateUniformBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
     
        int imageCount = cvkSwapChain.GetImageCount();        
        
        // LIFTED FROM AxesRenderable.reshape(...)
        // The logic here seems to be the FPS text needs to be 50 pixels from the 
        // edges, the calculation of dx and dy implies that the viewport is 
        //-width/2, -height/2, width/2, height/2
        
        // whenever the drawable shape changes, recalculate the place where the fps is drawn
        
        // This is a GL viewport where the screen space origin is in the bottom left corner
        //final int[] viewport = new int[]{0, 0, cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight()};
        
        // In Vulkan the screen space origin is in the top left hand corner.  Note we put the origin at 0, H and 
        // the viewport dimensions are W and -H.  The -H means we we still have a 0->H range, just running in the
        // opposite direction to GL.
        final int[] viewport = new int[]{0, cvkSwapChain.GetHeight(), cvkSwapChain.GetWidth(), -cvkSwapChain.GetHeight()};
               
        final int dx = cvkSwapChain.GetWidth() / 2 - AXES_OFFSET;
        final int dy = -cvkSwapChain.GetHeight() / 2 + AXES_OFFSET;
        pScale = calculateProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, topRightCorner);
        
        // LIFTED FROM AxesRenerable.display(...)
        // Extract the rotation matrix from the mvp matrix.
        final Matrix44f rotationMatrix = new Matrix44f();
        parent.getDisplayModelViewProjectionMatrix().getRotationMatrix(rotationMatrix);

        // Scale down to size.
        final Matrix44f scalingMatrix = new Matrix44f();
        scalingMatrix.makeScalingMatrix(pScale, pScale, 0);
        final Matrix44f srMatrix = new Matrix44f();
        srMatrix.multiply(scalingMatrix, rotationMatrix);

        // Translate to the top right corner.
        final Matrix44f translationMatrix = new Matrix44f();
        translationMatrix.makeTranslationMatrix(topRightCorner.getX(), 
                                                topRightCorner.getY(), 
                                                topRightCorner.getZ()); 
        vertexUBO.mvpMatrix.multiply(translationMatrix, srMatrix); 
          
        for (int i = 0; i < imageCount; ++i) {   
            // Initial fill of the vertex uniform buffer
            int size = VertexUniformBufferObject.SIZEOF;
            CVKBuffer vertUniformBuffer = vertexUniformBuffers.get(i);
            PointerBuffer vertData = stack.mallocPointer(1);
            vkMapMemory(cvkDevice.GetDevice(), vertUniformBuffer.GetMemoryBufferHandle(), 0, size, 0, vertData);
            {
                vertexUBO.CopyTo(vertData.getByteBuffer(0, size));
            }
            vkUnmapMemory(cvkDevice.GetDevice(), vertUniformBuffer.GetMemoryBufferHandle());
        }
        return ret;                
    }
    
    private int CreateVertexBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
              
        vertexBuffers = new ArrayList<>();
        
        // Size to upper limit, we don't have to draw each one.
        int size = vertices.length * Vertex.SIZEOF;
        
        // Converted from AxesRenderable.java. Keeping comments for reference.
        int i =  0;
        // x axis
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);      
        vertices[i++] = new Vertex(new Vector3f(LEN,0f,0f), XCOLOR);
        
        // arrow
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN - HEAD, HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN - HEAD, HEAD, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN, 0f, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN, 0f, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN - HEAD, -HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN - HEAD, -HEAD, 0f), XCOLOR);

        // X
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, HEAD, HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, -HEAD, -HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, HEAD, -HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, -HEAD, HEAD), XCOLOR);

        // y axis
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // arrow
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN - HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN - HEAD, HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN - HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN - HEAD, -HEAD), YCOLOR);
        // Y
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, LEN + HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, LEN + HEAD, -HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, LEN + HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, LEN + HEAD, -HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, HEAD), YCOLOR);

        // z axis
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // arrow
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, 0, LEN - HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, 0f, LEN - HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, 0, LEN - HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, 0f, LEN - HEAD), ZCOLOR);
        // Z
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        
         
        //TODO_TT: most if not all of Constellation's vertex buffers won't change after creation
        // so they should probably be allocated as VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT and staged
        // to once to fill them (staging buffer this is host visible then copied to the device local)
        //for (int j = 0; j < imageCount; ++j) {   
        CVKBuffer vertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                        size,
                                                        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        vertexBuffers.add(vertexBuffer);

        PointerBuffer data = stack.mallocPointer(1);
        vkMapMemory(cvkDevice.GetDevice(), vertexBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
        {
            Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
        }
        vkUnmapMemory(cvkDevice.GetDevice(), vertexBuffer.GetMemoryBufferHandle());

        return ret;  
    
    }
    
    private float calculateProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.a[1] - proj1.a[1];

        return 25.0f / yScale;
    } 
    
    private int CreateUniformBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
     
        // TODO_TT: investigate a frames in flight < imageCount approach
        int imageCount = cvkSwapChain.GetImageCount();        
        
        // LIFTED FROM AxeaRenerable.reshape(...)
        // The logic here seems to be the FPS text needs to be 50 pixels from the 
        // edges, the calculation of dx and dy implies that the viewport is 
        //-width/2, -height/2, width/2, height/2
        
        // whenever the drawable shape changes, recalculate the place where the fps is drawn
        
        // This is a GL viewport where the screen space origin is in the bottom left corner
        //final int[] viewport = new int[]{0, 0, cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight()};
        
        // In Vulkan the screen space origin is in the top left hand corner.  Note we put the origin at 0, H and 
        // the viewport dimensions are W and -H.  The -H means we we still have a 0->H range, just running in the
        // opposite direction to GL.
        final int[] viewport = new int[]{0, cvkSwapChain.GetHeight(), cvkSwapChain.GetWidth(), -cvkSwapChain.GetHeight()};
               
        final int dx = cvkSwapChain.GetWidth() / 2 - AXES_OFFSET;
        final int dy = -cvkSwapChain.GetHeight() / 2 + AXES_OFFSET;
        pScale = calculateProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, topRightCorner);
        
        // LIFTED FROM AxesRenerable.display(...)
        // Extract the rotation matrix from the mvp matrix.
        final Matrix44f rotationMatrix = new Matrix44f();
        parent.getDisplayModelViewProjectionMatrix().getRotationMatrix(rotationMatrix);

        // Scale down to size.
        final Matrix44f scalingMatrix = new Matrix44f();
        scalingMatrix.makeScalingMatrix(pScale, pScale, 0);
        final Matrix44f srMatrix = new Matrix44f();
        srMatrix.multiply(scalingMatrix, rotationMatrix);

        // Translate to the top right corner.
        final Matrix44f translationMatrix = new Matrix44f();
        translationMatrix.makeTranslationMatrix(topRightCorner.getX(), 
                                                topRightCorner.getY(), 
                                                topRightCorner.getZ()); 
        vertexUBO.mvpMatrix.multiply(translationMatrix, srMatrix); 
              
        vertexUniformBuffers = new ArrayList<>();     
        for (int i = 0; i < imageCount; ++i) {   
            // Initial fill of the vertex uniform buffer
            int size = VertexUniformBufferObject.SIZEOF;
            CVKBuffer vertUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                          VertexUniformBufferObject.SIZEOF,
                                                          VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                        
            PointerBuffer vertData = stack.mallocPointer(1);
            vkMapMemory(cvkDevice.GetDevice(), vertUniformBuffer.GetMemoryBufferHandle(), 0, size, 0, vertData);
            {
                vertexUBO.CopyTo(vertData.getByteBuffer(0, size));
            }
            vkUnmapMemory(cvkDevice.GetDevice(), vertUniformBuffer.GetMemoryBufferHandle());     
            vertexUniformBuffers.add(vertUniformBuffer);            
         
        }
        return ret;                
    }
    
    private int CreateVertexBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain) {
         int ret = VK_SUCCESS;
     
        // TODO_TT: investigate a frames in flight < imageCount approach
        //int imageCount = cvkSwapChain.GetImageCount();
                
        // TODO_TT: clean this up
        vertexBuffers = new ArrayList<>();
        
        // Size to upper limit, we don't have to draw each one.
        int size = vertices.length * Vertex.SIZEOF;
        
        // Converted from AxesRenderable.java. Keeping comments for reference.
        int i =  0;
        // x axis
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);      
        vertices[i++] = new Vertex(new Vector3f(LEN,0f,0f), XCOLOR);
        
        // arrow
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN - HEAD, HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN - HEAD, HEAD, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN, 0f, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN, 0, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN, 0f, 0f), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN - HEAD, -HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(LEN - HEAD, -HEAD, 0f), XCOLOR);

        // X
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, HEAD, HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, -HEAD, -HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, HEAD, -HEAD), XCOLOR);
        // axesBatch.stage(colorTarget, XCOLOR);
        // axesBatch.stage(vertexTarget, LEN + HEAD, -HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(LEN + HEAD, -HEAD, HEAD), XCOLOR);

        // y axis
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // arrow
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN - HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN - HEAD, HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN - HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN - HEAD, -HEAD), YCOLOR);
        // Y
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, LEN + HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, LEN + HEAD, -HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, LEN + HEAD, -HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, LEN + HEAD, -HEAD), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, 0);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, 0f), YCOLOR);
        // axesBatch.stage(colorTarget, YCOLOR);
        // axesBatch.stage(vertexTarget, 0, LEN + HEAD, HEAD);
        vertices[i++] = new Vertex(new Vector3f(0f, LEN + HEAD, HEAD), YCOLOR);

        // z axis
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, ZERO_3F);
        vertices[i++] = new Vertex(ZERO_3F, ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // arrow
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, 0, LEN - HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, 0f, LEN - HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, 0, 0, LEN);
        vertices[i++] = new Vertex(new Vector3f(0f, 0f, LEN), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, 0, LEN - HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, 0f, LEN - HEAD), ZCOLOR);
        // Z
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, -HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(-HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        // axesBatch.stage(colorTarget, ZCOLOR);
        // axesBatch.stage(vertexTarget, HEAD, -HEAD, LEN + HEAD);
        vertices[i++] = new Vertex(new Vector3f(HEAD, -HEAD, LEN + HEAD), ZCOLOR);
        
         
        //TODO_TT: most if not all of Constellation's vertex buffers won't change after creation
        // so they should probably be allocated as VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT and staged
        // to once to fill them (staging buffer this is host visible then copied to the device local)
        //for (int j = 0; j < imageCount; ++j) {   
        CVKBuffer vertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                        size,
                                                        VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        vertexBuffers.add(vertexBuffer);

        PointerBuffer data = stack.mallocPointer(1);
        vkMapMemory(cvkDevice.GetDevice(), vertexBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
        {
            Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
        }
        vkUnmapMemory(cvkDevice.GetDevice(), vertexBuffer.GetMemoryBufferHandle());

        return ret;  
    
    }
  
    
    public int CreateCommandBuffers(CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        commandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_SECONDARY);
            buffer.DEBUGNAME = String.format("CVKAxesRenderable %d", i);
            commandBuffers.add(buffer);
        }
        
        CVKLOGGER.log(Level.INFO, "Init Command Buffer - AxesRenderable");
        
        return ret;
    }
    
    public int CreatePipeline(CVKSwapChain cvkSwapChain) {
        assert(cvkDevice != null);
        assert(cvkDevice.GetDevice() != null);
        assert(cvkSwapChain.GetSwapChainHandle()        != VK_NULL_HANDLE);
        assert(cvkSwapChain.GetRenderPassHandle()       != VK_NULL_HANDLE);
        assert(cvkSwapChain.GetDescriptorPoolHandle()   != VK_NULL_HANDLE);
        assert(vertShaderModule != VK_NULL_HANDLE);
        assert(geomShaderModule != VK_NULL_HANDLE);
        assert(fragShaderModule != VK_NULL_HANDLE);        
        assert(cvkSwapChain.GetWidth() > 0);
        assert(cvkSwapChain.GetHeight() > 0);
        
        // TODO Make sure this object has been initialised
        //if (!isInitialised)
        //    logger.error("Cannot create pipeline when renderable is not initialised");
        
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
        
            ret = CreateUniformBuffers(stack, cvkSwapChain);
            checkVKret(ret);
            
            ret = CreateDescriptorSets(stack, cvkSwapChain);
            checkVKret(ret);     
            
            ret = CreateVertexBuffers(stack, cvkSwapChain);
            checkVKret(ret);     
            
            
            int imageCount = cvkSwapChain.GetImageCount();
             // A complete pipeline for each swapchain image.  Wasteful?
            pipelines = new ArrayList<>(imageCount);            
            pipelineLayouts = new ArrayList<>(imageCount);   
            for (int i = 0; i < imageCount; ++i) {       
                
                // ===> SHADER STAGE <===
                ByteBuffer entryPoint = stack.UTF8("main");
                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);
                
                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(vertShaderModule);
                vertShaderStageInfo.pName(entryPoint);
                
                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
                geomShaderStageInfo.module(geomShaderModule);
                geomShaderStageInfo.pName(entryPoint);   

                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(fragShaderModule);
                fragShaderStageInfo.pName(entryPoint);

                // ===> VERTEX STAGE <===
                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(Vertex.getBindingDescription());         // From Vertex struct
                vertexInputInfo.pVertexAttributeDescriptions(Vertex.getAttributeDescriptions());    // From Vertex struct

                // ===> ASSEMBLY STAGE <===
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_LINE_LIST);
                inputAssembly.primitiveRestartEnable(false);

                // ===> VIEWPORT & SCISSOR
                VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
                viewport.x(0.0f);
                viewport.y(0.0f);
                viewport.width(cvkSwapChain.GetWidth());
                viewport.height(cvkSwapChain.GetHeight());
                viewport.minDepth(0.0f);
                viewport.maxDepth(1.0f);

                VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
                scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
                scissor.extent(cvkDevice.GetCurrentSurfaceExtent());

                VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
                viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
                viewportState.pViewports(viewport);
                viewportState.pScissors(scissor);

                // ===> RASTERIZATION STAGE <===
                VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
                rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
                rasterizer.depthClampEnable(false);
                rasterizer.rasterizerDiscardEnable(false);
                rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
                rasterizer.lineWidth(1.0f);
                rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
                rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
                rasterizer.depthBiasEnable(false);

                // ===> MULTISAMPLING <===
                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
                multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
                multisampling.sampleShadingEnable(false);
                multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

                // ===> DEPTH <=== 
                // Even though we don't test depth, the renderpass created by CVKSwapChain is used by
                // each renderable and it was created to have a depth attachment
                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
                depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
                depthStencil.depthTestEnable(false);
                depthStencil.depthWriteEnable(false);
                depthStencil.depthCompareOp(VK_COMPARE_OP_ALWAYS);
                depthStencil.depthBoundsTestEnable(false);
                depthStencil.minDepthBounds(0.0f); // Optional
                depthStencil.maxDepthBounds(1.0f); // Optional
                depthStencil.stencilTestEnable(false);

                // ===> COLOR BLENDING <===
                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
                colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
                colorBlendAttachment.blendEnable(false);

                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
                colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
                colorBlending.logicOpEnable(false);
                colorBlending.logicOp(VK_LOGIC_OP_COPY);
                colorBlending.pAttachments(colorBlendAttachment);
                colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

                // ===> PIPELINE LAYOUT CREATION <===
                VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
                pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
                pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));

                LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

                ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
                checkVKret(ret);

                long hPipelineLayout = pPipelineLayout.get(0);
                assert(hPipelineLayout != VK_NULL_HANDLE);
                pipelineLayouts.add(hPipelineLayout);
                
                VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
                pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
                pipelineInfo.pStages(shaderStages);
                pipelineInfo.pVertexInputState(vertexInputInfo);
                pipelineInfo.pInputAssemblyState(inputAssembly);
                pipelineInfo.pViewportState(viewportState);
                pipelineInfo.pRasterizationState(rasterizer);
                pipelineInfo.pMultisampleState(multisampling);
                pipelineInfo.pDepthStencilState(depthStencil);
                pipelineInfo.pColorBlendState(colorBlending);
                pipelineInfo.layout(hPipelineLayout);
                pipelineInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
                pipelineInfo.subpass(0);
                pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
                pipelineInfo.basePipelineIndex(-1);

                LongBuffer pGraphicsPipeline = stack.mallocLong(1);


                ret = vkCreateGraphicsPipelines(cvkDevice.GetDevice(), 
                                                VK_NULL_HANDLE, 
                                                pipelineInfo, 
                                                null, 
                                                pGraphicsPipeline);
                checkVKret(ret);                
                pipelines.add(pGraphicsPipeline.get(0));
                assert(pipelines.get(i) != VK_NULL_HANDLE);
            }
        }
        CVKLOGGER.log(Level.INFO, "Graphics Pipeline created for AxesRenderable class.");
        
        return ret;
    }
    
   
    // Called from Record in the Renderer
    @Override
    public int RecordCommandBuffer(CVKSwapChain cvkSwapChain, VkCommandBufferInheritanceInfo inheritanceInfo, int index) {
        VerifyInRenderThread();
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {
              
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc();
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.pNext(0);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);  // hard coding this for now
            beginInfo.pInheritanceInfo(inheritanceInfo);     

            VkCommandBuffer commandBuffer = commandBuffers.get(index).GetVKCommandBuffer();
         
            ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
            checkVKret(ret);    

            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelines.get(index));

            // We only use 1 vertBuffer here as the verts are fixed the entire lifetime of the object
            LongBuffer pVertexBuffers = stack.longs(vertexBuffers.get(0).GetBufferHandle());
            LongBuffer offsets = stack.longs(0);
            
            // Bind verts
            vkCmdBindVertexBuffers(commandBuffer, 0, pVertexBuffers, offsets);
                        
            // Bind descriptors
            vkCmdBindDescriptorSets(commandBuffer, 
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    pipelineLayouts.get(index), 
                                    0, 
                                    stack.longs(pDescriptorSets.get(index)), 
                                    null);
            
            // Copy draw commands
            vkCmdDraw(commandBuffer, GetVertexCount(), 1, 0, 0);
            
            ret = vkEndCommandBuffer(commandBuffer);
            checkVKret(ret);

            beginInfo.free();
        }
        return ret;
    }
        
    @Override
    public int SwapChainRecreated(CVKSwapChain cvkSwapChain) {
        VerifyInRenderThread();
        int ret = VK_SUCCESS;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines == null || pipelines.size() != cvkSwapChain.GetImageCount()) {        
            DestroyVertexBuffers();
            DestroyUniformBuffers();
            DestroyDescriptorSets();
            DestroyCommandBuffers();
            DestroyPipeline();
            DestroyPipelineLayouts();
            DestroyCommandBuffers(); 

            CVKAssert(pipelines == null);
            CVKAssert(pipelineLayouts == null);
            CVKAssert(pDescriptorSets == null);
            CVKAssert(vertexUniformBuffers == null);
            CVKAssert(vertexBuffers == null);
            CVKAssert(commandBuffers == null);           

            try (MemoryStack stack = stackPush()) {                     
                ret = CreateUniformBuffers(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; }

                ret = CreateDescriptorSets(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; } 

                ret = CreateVertexBuffers(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; }   

                ret = CreateCommandBuffers(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }            

                ret = CreatePipeline(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }                                       
            }      
        } else {
            // This is the resize path, image count is unchanged.  We need to recreate
            // pipelines as Vulkan doesn't have a good mechanism to update them and as
            // they define the viewport and scissor rect they are now out of date.  We
            // also need to update the uniform buffer as a new image size will mean a
            // different position for our FPS.  After updating the uniform buffers we
            // need to update the descriptor sets that bind the uniform buffers as well.
            DestroyPipeline();
            DestroyDescriptorSets();
            CVKAssert(pipelines == null);
            
            try (MemoryStack stack = stackPush()) {                     
                ret = CreatePipeline(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }                           
                
                ret = UpdateUniformBuffers(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; }

                // TODO_TT: this is leaking
                ret = CreateDescriptorSets(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; } 
            }              
        }
        
        return ret;
    }
    
    public static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;

        try{
            vertShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThru.vs", VERTEX_SHADER);
            geomShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThruLine.gs", GEOMETRY_SHADER);
            fragShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThru.fs", FRAGMENT_SHADER);
            
            vertShaderModule = CVKShaderUtils.createShaderModule(vertShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            geomShaderModule = CVKShaderUtils.createShaderModule(geomShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            fragShaderModule = CVKShaderUtils.createShaderModule(fragShaderSPIRV.bytecode(), cvkDevice.GetDevice());
        } catch(Exception ex){
            CVKLOGGER.log(Level.WARNING, "Failed to compile AxesRenderable shaders: {0}", ex.toString());
        }
        
        CVKLOGGER.log(Level.INFO, "Static shaders loaded for AxesRenderable class");
        return ret;
    }
    
    @Override
    public int DisplayUpdate(CVKSwapChain cvkSwapChain, int frameIndex) {
        // UPDATE CODE
        
        return VK_SUCCESS;
    }    
    
    @Override
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[], int descriptorSetCount) {
        assert(descriptorTypeCounts.length == (VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT + 1));
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        
        // One descriptor set is required
        ++descriptorSetCount;
    }
    
    public static int CreateDescriptorLayout(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {
            /*
                Vertex shader needs a uniform buffer.
            */
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(1, stack);

            VkDescriptorSetLayoutBinding vertexUBOLayout = bindings.get(0);
            vertexUBOLayout.binding(0);
            vertexUBOLayout.descriptorCount(1);
            vertexUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            vertexUBOLayout.pImmutableSamplers(null);
            vertexUBOLayout.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            
            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            ret = vkCreateDescriptorSetLayout(cvkDevice.GetDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
            }
        }        
        return ret;
    }         
    
    
    private int CreateDescriptorSets(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret;
     
        int imageCount = cvkSwapChain.GetImageCount();

        // Create descriptor sets
        LongBuffer pLayouts = stack.mallocLong(imageCount);
        for (int i = 0; i < imageCount; ++i) {
            pLayouts.put(i, hDescriptorLayout);
        }

        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool(cvkSwapChain.GetDescriptorPoolHandle());
        allocInfo.pSetLayouts(pLayouts);            

        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
        ret = vkAllocateDescriptorSets(cvkDevice.GetDevice(), allocInfo, pDescriptorSets);
        checkVKret(ret);

        // Let's initialise them
        //descriptorSets = new ArrayList<>(pDescriptorSets.capacity());

        // Struct for the size of the uniform buffer used by PassThru.vs (we fill the actual buffer below)
        VkDescriptorBufferInfo.Buffer vertBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        vertBufferInfo.offset(0);
        vertBufferInfo.range(VertexUniformBufferObject.SIZEOF);      

        // We need 1 write descriptors, 1 for uniform buffers (vs) 
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(1, stack);

        VkWriteDescriptorSet vertUBDescriptorWrite = descriptorWrites.get(0);
        vertUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertUBDescriptorWrite.dstBinding(0);
        vertUBDescriptorWrite.dstArrayElement(0);
        vertUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        vertUBDescriptorWrite.descriptorCount(1);
        vertUBDescriptorWrite.pBufferInfo(vertBufferInfo);                       

        for (int i = 0; i < imageCount; ++i) {
            long descriptorSet = pDescriptorSets.get(i);

            vertBufferInfo.buffer(vertexUniformBuffers.get(i).GetBufferHandle());
            vertUBDescriptorWrite.dstSet(descriptorSet);

            // Update the descriptors with a write and no copy
            vkUpdateDescriptorSets(cvkDevice.GetDevice(), descriptorWrites, null);

            //descriptorSets.add(descriptorSet); 
        }   
        
        return ret;
    }
    
    @Override
    public boolean SharedResourcesNeedUpdating() {
        return false;
    }
    
    @Override
    public int DeviceInitialised(CVKDevice cvkDevice) {
        this.cvkDevice = cvkDevice;
        return VK_SUCCESS;
    }    

  
    @Override
    public int RecreateSharedResources(CVKSwapChain cvkSwapChain) { 
        VerifyInRenderThread();
        
        int ret;
        DestroyDescriptorSets();
        try (MemoryStack stack = stackPush()) {
            ret = CreateDescriptorSets(stack, cvkSwapChain);
        }
        return ret;
    }
    

    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex)
    {
        return commandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }    
}
