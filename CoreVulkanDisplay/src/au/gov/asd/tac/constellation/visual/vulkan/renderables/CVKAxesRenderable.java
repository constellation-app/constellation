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
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.SPIRV;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.ShaderKind.FRAGMENT_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.ShaderKind.VERTEX_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.compileShaderFile;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils.ShaderKind.GEOMETRY_SHADER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_COMPILATION;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_MODULE;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkPushConstantRange;


public class CVKAxesRenderable extends CVKRenderable {
    // Static so we recreate descriptor layouts and shaders for each graph
    private static boolean staticInitialised = false;
    
    // Compiled Shader modules
    private long hVertexShaderModule = VK_NULL_HANDLE;
    private long hFragmentShaderModule = VK_NULL_HANDLE;
    private long hGeometryShaderModule = VK_NULL_HANDLE;
    
    private static SPIRV vertexShaderSPIRV = null;
    private static SPIRV geometryShaderSPIRV = null;
    private static SPIRV fragmentShaderSPIRV = null;
       
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
    
    private final Vector3f topRightCorner = new Vector3f();
    private float pScale = 0;
    
    private final Vertex[] vertices = new Vertex[NUMBER_OF_VERTICES];
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private CVKBuffer cvkVertexBuffer = null;
    private List<CVKCommandBuffer> commandBuffers = null;
    
    private List<Long> pipelines = null;
    private long hPipelineLayout = VK_NULL_HANDLE;

    private boolean needsDisplayUpdate = false;

    private ByteBuffer pushConstants = null;
 
    
    // ========================> Classes <======================== \\    
    
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
    
    
    // ========================> Static resources <======================== \\
    
    private static int LoadShaders() {       
        int ret = VK_SUCCESS;

        try{
            if (vertexShaderSPIRV == null) {
                vertexShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThru.vs", VERTEX_SHADER);
                if (vertexShaderSPIRV == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to compile AxesRenderable shaders: PassThru.vs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (geometryShaderSPIRV == null) {
                geometryShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThruLine.gs", GEOMETRY_SHADER);
                if (geometryShaderSPIRV == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to compile AxesRenderable shader: PassThruLine.gs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (fragmentShaderSPIRV == null) {
                fragmentShaderSPIRV = compileShaderFile(CVKShaderPlaceHolder.class, "PassThru.fs", FRAGMENT_SHADER);
                if (fragmentShaderSPIRV == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to compile AxesRenderable shaders: PassThru.fs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
        } catch(Exception ex){
            CVKLOGGER.log(Level.SEVERE, "Failed to compile AxesRenderable shaders: {0}", ex.toString());
            ret = CVK_ERROR_SHADER_COMPILATION;
            return ret;
        }
        
        CVKLOGGER.log(Level.INFO, "Static shaders loaded for AxesRenderable class");
        return ret;
    }
                      
    public static int StaticInitialise() {
        int ret = VK_SUCCESS;
        if (!staticInitialised) {
            ret = LoadShaders();
            if (VkFailed(ret)) { return ret; }
            staticInitialised = true;
        }
        return ret;
    }
    
    public static void DestroyStaticResources() {
        if (vertexShaderSPIRV != null) {
            vertexShaderSPIRV.free();
            vertexShaderSPIRV = null;
        }
        
        if (geometryShaderSPIRV != null) {
            geometryShaderSPIRV.free();
            geometryShaderSPIRV = null;
        }
        
        if (fragmentShaderSPIRV != null) {
            fragmentShaderSPIRV.free();
            fragmentShaderSPIRV = null;
        }
        
        staticInitialised = false;
    }
    
    
    // ========================> Lifetime <======================== \\
    
    public CVKAxesRenderable(CVKVisualProcessor visualProcessor) {
        cvkVisualProcessor = visualProcessor;
    }
    
    private int CreateShaderModules() {
        int ret = VK_SUCCESS;
        
        try{           
            hVertexShaderModule = CVKShaderUtils.CreateShaderModule(vertexShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            if (hVertexShaderModule == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: PassThru.vs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hGeometryShaderModule = CVKShaderUtils.CreateShaderModule(geometryShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            if (hGeometryShaderModule == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: PassThruLine.gs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hFragmentShaderModule = CVKShaderUtils.CreateShaderModule(fragmentShaderSPIRV.bytecode(), cvkDevice.GetDevice());
            if (hFragmentShaderModule == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: PassThru.fs");
                return CVK_ERROR_SHADER_MODULE;
            }
        } catch(Exception ex){
            CVKLOGGER.log(Level.SEVERE, "Failed to create shader module AxesRenderable: {0}", ex.toString());
            ret = CVK_ERROR_SHADER_MODULE;
            return ret;
        }
        
        cvkVisualProcessor.GetLogger().info("Shader modules created for CVKAxesRenderable class:\n\tVertex:   0x%016x\n\tGeometry: 0x%016x\n\tFragment: 0x%016x",
                hVertexShaderModule, hGeometryShaderModule, hFragmentShaderModule);
        return ret;
    } 
    
    private void DestroyShaderModules() {
        if (hVertexShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(cvkDevice.GetDevice(), hVertexShaderModule, null);
            hVertexShaderModule = VK_NULL_HANDLE;
        }
        if (hGeometryShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(cvkDevice.GetDevice(), hGeometryShaderModule, null);
            hGeometryShaderModule = VK_NULL_HANDLE;
        }
        if (hFragmentShaderModule != VK_NULL_HANDLE) {
            vkDestroyShaderModule(cvkDevice.GetDevice(), hFragmentShaderModule, null);
            hFragmentShaderModule = VK_NULL_HANDLE;
        }
    }
    
    @Override
    public int Initialise(CVKDevice cvkDevice) {
        // Check for double initialisation
        CVKAssertNull(hVertexShaderModule);
        
        int ret;
        
        this.cvkDevice = cvkDevice;

        // Initialise push constants to identity mtx
        CreatePushConstants();
        
        ret = CreateShaderModules();
        if (VkFailed(ret)) { return ret; }               
        
        ret = CreatePipelineLayout();
        if (VkFailed(ret)) { return ret; }                
               
        return VK_SUCCESS;
    }
    
    @Override
    public void Destroy() {
        DestroyCommandBuffers();
        DestroyVertexBuffer();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyPushConstants();
        DestroyShaderModules();
              
        CVKAssertNull(pipelines);
        CVKAssertNull(hPipelineLayout);
        CVKAssertNull(cvkVertexBuffer);
        CVKAssertNull(commandBuffers);
        CVKAssertNull(pushConstants);
        CVKAssertNull(hVertexShaderModule);
        CVKAssertNull(hGeometryShaderModule);
        CVKAssertNull(hFragmentShaderModule);
    }
    
    
    // ========================> Swap chain <======================== \\
    
    private int CreateSwapChainResources() {
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(cvkSwapChain);
        int ret = VK_SUCCESS;

                
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (swapChainImageCountChanged) {
            try (MemoryStack stack = stackPush()) {

                ret = CreateVertexBuffer(stack);
                if (VkFailed(ret)) { return ret; }   

                ret = CreateCommandBuffers();
                if (VkFailed(ret)) { return ret; }            

                ret = CreatePipelines();
                if (VkFailed(ret)) { return ret; }                                       
            }      
        } else {
            // This is the resize path, image count is unchanged.       
            UpdatePushConstants();
        }
        
        swapChainResourcesDirty = false;
        swapChainImageCountChanged = false;
        
        return ret;
    }  
    
    @Override
    protected int DestroySwapChainResources(){
        cvkVisualProcessor.VerifyInRenderThread();
        int ret = VK_SUCCESS;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines != null && swapChainImageCountChanged) {        
            DestroyVertexBuffer();
            DestroyCommandBuffers();
            DestroyPipelines();
            DestroyCommandBuffers(); 

            CVKAssertNull(pipelines);
            CVKAssertNull(cvkVertexBuffer);
            CVKAssertNull(commandBuffers);
         } else {
            // This is the resize path, image count is unchanged.  We

        }
        
        return ret;
    }
      
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffer(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        int ret = VK_SUCCESS;
        
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
        
         
        // Staging buffer so our VB can be device local (most performant memory)
        CVKBuffer cvkStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                      size,
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      "CVKAxesRenderable.CreateVertexBuffer cvkStagingBuffer");

        PointerBuffer data = stack.mallocPointer(1);
        vkMapMemory(cvkDevice.GetDevice(), cvkStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
        if (VkFailed(ret)) { return ret; }
        {
            Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkStagingBuffer.GetMemoryBufferHandle());
        
        // Create and stage into the actual VB which will be device local
        cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                           size,
                                           VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                           VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                           "CVKAxesRenderable.CreateVertexBuffers cvkStagingBuffer");
        cvkVertexBuffer.CopyFrom(cvkStagingBuffer);
        
        // Cleanup
        cvkStagingBuffer.Destroy();
        
        return ret;  
    }
    
    @Override
    public int GetVertexCount() { return NUMBER_OF_VERTICES; }    
    
    private void DestroyVertexBuffer() {
        if (null != cvkVertexBuffer) {
            cvkVertexBuffer.Destroy();
            cvkVertexBuffer = null;
        }
    }    
   
    
    // ========================> Push constants <======================== \\
    
    private int CreatePushConstants() {
        // Initialise push constants to identity mtx
        pushConstants = memAlloc(VertexUniformBufferObject.SIZEOF);
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                pushConstants.putFloat(IDENTITY_44F.get(iRow, iCol));
            }
        }
        pushConstants.flip();
         
        return VK_SUCCESS;
    }
    
    private void UpdatePushConstants(){
        CVKAssertNotNull(cvkSwapChain);
            
        final int[] viewport = cvkSwapChain.GetViewport();             
        final int dx = cvkSwapChain.GetWidth() / 2 - AXES_OFFSET;
        final int dy = -cvkSwapChain.GetHeight() / 2 + AXES_OFFSET;
        pScale = CalculateProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, topRightCorner);
        
        // LIFTED FROM AxesRenerable.display(...)
        // Extract the rotation matrix from the mvp matrix.
        final Matrix44f rotationMatrix = new Matrix44f();
        cvkVisualProcessor.getDisplayModelViewProjectionMatrix().getRotationMatrix(rotationMatrix);

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
        
        // Calculate the model-view-projection matrix
        vertexUBO.mvpMatrix.multiply(translationMatrix, srMatrix);
        
        // Update the push constants data
        vertexUBO.CopyTo(pushConstants);
        pushConstants.flip();        
    }
    
    private void DestroyPushConstants() {
        if (pushConstants != null) {
            memFree(pushConstants);
            pushConstants = null;
        }
    }
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers() {       
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        commandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, 
                    VK_COMMAND_BUFFER_LEVEL_SECONDARY, 
                    String.format("CVKAxesRenderable %d", i));
            commandBuffers.add(buffer);
        }
        
        cvkVisualProcessor.GetLogger().info("Init Command Buffer - CVKAxesRenderable");
        
        return ret;
    }
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) {
        return commandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }     
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex) {
        CVKAssertNotNull(cvkSwapChain);
        cvkVisualProcessor.VerifyInRenderThread();
        int ret;
        
        try (MemoryStack stack = stackPush()) {
            
            CVKCommandBuffer commandBuffer = commandBuffers.get(imageIndex);
            CVKAssertNotNull(commandBuffer);
            CVKAssertNotNull(pipelines.get(imageIndex));
            
            commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
                                                           inheritanceInfo);
            
            // Set the dynamic viewport and scissor
            commandBuffer.viewPortCmd(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight(), stack);
            commandBuffer.scissorCmd(cvkDevice.GetCurrentSurfaceExtent(), stack);
            
            // Bind the graphics pipeline
            commandBuffer.BindGraphicsPipelineCmd(pipelines.get(imageIndex));
            

            // We only use 1 vertBuffer here as the verts are fixed the entire lifetime of the object
            LongBuffer pVertexBuffers = stack.longs(cvkVertexBuffer.GetBufferHandle());
            LongBuffer offsets = stack.longs(0);
            
            // Bind verts
            vkCmdBindVertexBuffers(commandBuffer.GetVKCommandBuffer(), 0, pVertexBuffers, offsets);
                        
            // Push MVP matrix to the shader
            vkCmdPushConstants(commandBuffer.GetVKCommandBuffer(),   // The buffer to push the matrix to
				hPipelineLayout,            // The pipeline layout
				VK_SHADER_STAGE_VERTEX_BIT, // Flags
				0,                          // Offset
				pushConstants);             // Matrix buffer
            
            // Copy draw commands
            vkCmdDraw(commandBuffer.GetVKCommandBuffer(), GetVertexCount(), 1, 0, 0);
            
            ret = vkEndCommandBuffer(commandBuffer.GetVKCommandBuffer());
            checkVKret(ret);
        }
        return ret;
    }
    
    private void DestroyCommandBuffers() {
        if (null != commandBuffers && commandBuffers.size() > 0) {
            commandBuffers.forEach(el -> {el.Destroy();});
            commandBuffers.clear();
            commandBuffers = null;
        }       
    }
    
    
    // ========================> Descriptors <======================== \\  
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {
        // No descriptor sets required because axes use push constants instead of descriptor bound uniform buffers.
    }
  
    @Override
    public int DestroyDescriptorPoolResources() {         
        return VK_SUCCESS; 
    }       
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssertNotNull(cvkDevice);
        CVKAssertNotNull(cvkDevice.GetDevice());
               
        int ret;       
        try (MemoryStack stack = stackPush()) {      
            VkPushConstantRange.Buffer pushConstantRange;
            pushConstantRange = VkPushConstantRange.callocStack(1, stack);
            pushConstantRange.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            pushConstantRange.size(VertexUniformBufferObject.SIZEOF);
            pushConstantRange.offset(0);                     
            
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);  
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssertNotNull(hPipelineLayout);                
        }        
        return ret;        
    }
    
    private void DestroyPipelineLayout() {
        if (hPipelineLayout != VK_NULL_HANDLE) {
            vkDestroyPipelineLayout(cvkDevice.GetDevice(), hPipelineLayout, null);
            hPipelineLayout = VK_NULL_HANDLE;
        }
    }       
    
    private int CreatePipelines() {
        CVKAssertNotNull(cvkDevice);
        CVKAssertNotNull(cvkDevice.GetDevice());
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkSwapChain.GetSwapChainHandle());
        CVKAssertNotNull(cvkSwapChain.GetRenderPassHandle());
        CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());
        CVKAssertNotNull(hVertexShaderModule);
        CVKAssertNotNull(hGeometryShaderModule);
        CVKAssertNotNull(hFragmentShaderModule);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        int ret = VK_SUCCESS;
        
        try (MemoryStack stack = stackPush()) {           
            int imageCount = cvkSwapChain.GetImageCount();
             // A complete pipeline for each swapchain image.  Wasteful?
            pipelines = new ArrayList<>(imageCount);            
            for (int i = 0; i < imageCount; ++i) {       
                
                // ===> SHADER STAGE <===
                ByteBuffer entryPoint = stack.UTF8("main");
                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);
                
                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(hVertexShaderModule);
                vertShaderStageInfo.pName(entryPoint);
                
                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
                geomShaderStageInfo.module(hGeometryShaderModule);
                geomShaderStageInfo.pName(entryPoint);   

                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(hFragmentShaderModule);
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

                // ===> VIEWPORT & SCISSOR <===
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
                
                // ===> DYNAMIC PROPERTIES CREATION <===
                IntBuffer pDynamicStates = memAllocInt(2);
                pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT);
                pDynamicStates.put(VK_DYNAMIC_STATE_SCISSOR);
                pDynamicStates.flip();
                VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.callocStack(stack);
                dynamicState.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
                dynamicState.pDynamicStates(pDynamicStates);
                
                // ===> PIPELINE CREATION <===
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
                pipelineInfo.pDynamicState(dynamicState);
                
                LongBuffer pGraphicsPipeline = stack.mallocLong(1);
                ret = vkCreateGraphicsPipelines(cvkDevice.GetDevice(), 
                                                VK_NULL_HANDLE, 
                                                pipelineInfo, 
                                                null, 
                                                pGraphicsPipeline);
                if (VkFailed(ret)) { return ret; }
                
                pipelines.add(pGraphicsPipeline.get(0));
                CVKAssertNotNull(pipelines.get(i));
            }
        }
        cvkVisualProcessor.GetLogger().info("Graphics Pipeline created for AxesRenderable class.");
        
        return ret;
    }
    
    private void DestroyPipelines() {     
        if (pipelines != null) {
            for (int i = 0; i < pipelines.size(); ++i) {
                vkDestroyPipeline(cvkDevice.GetDevice(), pipelines.get(i), null);
                pipelines.set(i, VK_NULL_HANDLE);
            }
            pipelines.clear();
            pipelines = null;
        }       
    }   
    
    
    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() {
        return needsDisplayUpdate | descriptorPoolResourcesDirty | swapChainResourcesDirty;
    }

    @Override
    public int DisplayUpdate() { 
        cvkVisualProcessor.VerifyInRenderThread();
        
        int ret = VK_SUCCESS;    
        
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }           

        UpdatePushConstants();
 
        needsDisplayUpdate = false;
        return ret;
    }
    
    
    // ========================> Tasks <======================== \\
    
    public CVKRenderUpdateTask TaskUpdateCamera() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//

        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            cvkVisualProcessor.VerifyInRenderThread();                      
            needsDisplayUpdate = true;
        };
    }  


    // ========================> Helpers <======================== \\ 
         
    private float CalculateProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.a[1] - proj1.a[1];

        return 25.0f / yScale;
    } 
}
