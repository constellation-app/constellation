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
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SINT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.LoadFileToDirectBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
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
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;


public class CVKFPSRenderable extends CVKRenderable {
    private static final int MAX_DIGITS = 4;
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;    
    private static final int DIGIT_ICON_OFFSET = 4;
    private static final int FPS_OFFSET = 50;
    private static final float FIELD_OF_VIEW = 35; //move to renderer or cvkScene
    private static final Matrix44f IDENTITY_44F = Matrix44f.identity();
    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    
    private static long hVertexShader = VK_NULL_HANDLE;
    private static long hGeometryShader = VK_NULL_HANDLE;
    private static long hFragmentShader = VK_NULL_HANDLE;
    private static long hDescriptorLayout = VK_NULL_HANDLE;    
    
    private final Vector3f bottomRightCorner = new Vector3f();
    private float pyScale = 0;
    private float pxScale = 0;         
    private List<Long> pipelines = null;
    private List<Long> pipelineLayouts = null;
    private Vertex[] vertices = new Vertex[MAX_DIGITS];
    private VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject();
    private LongBuffer pDescriptorSets = null;
    //private List<Long> descriptorSets = null;
    private List<Integer> currentFPS = null;  
    
    // TODO: Candidates to be moved to CVKRenderable
    private List<CVKBuffer> vertexUniformBuffers = null;
    private List<CVKBuffer> geometryUniformBuffers = null;
    private List<CVKBuffer> vertexBuffers = null;
    private List<CVKCommandBuffer> commandBuffers = null;
    
    private CVKSwapChain cvkSwapChain = null; //cached for cleaning up descriptor sets
   
    // Cache image view and sampler handles so we know when they've been recreated
    // so we can recreate our descriptors
    private long hAtlasSampler = VK_NULL_HANDLE;
    private long hAtlasImageView = VK_NULL_HANDLE;
 
    @Override
    public boolean IsDirty(){return isDirty; }
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
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
        
        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
            for(Vertex vertex : vertices) {
                buffer.putInt(vertex.data[0]);
                buffer.putInt(vertex.data[1]);
                                
                buffer.putFloat(vertex.backgroundIconColor.a[0]);
                buffer.putFloat(vertex.backgroundIconColor.a[1]);
                buffer.putFloat(vertex.backgroundIconColor.a[2]);
                buffer.putFloat(vertex.backgroundIconColor.a[3]);
            }
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
    
    
    private static class VertexUniformBufferObject {
        private static final int SIZEOF = (16 + 1 + 1) * Float.BYTES;

        public Matrix44f mvMatrix;
        public float visibilityLow = 0;
        public float visibilityHigh = 0;
        

        public VertexUniformBufferObject() {
            mvMatrix = new Matrix44f();
        }
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(mvMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);
        }         
    }
    
    
    private static class GeometryUniformBufferObject {
        private static final int SIZEOF = (16 + 1 + 1) * Float.BYTES;

        public Matrix44f pMatrix;
        public float pixelDensity = 0;
        public float pScale = 0;        

        public GeometryUniformBufferObject() {
            pMatrix = new Matrix44f();
        }
        
        private void CopyTo(ByteBuffer buffer) {
            // TODO_TT: convert to a blat
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(pixelDensity);
            buffer.putFloat(pScale);
        }             
    }       
    
    
    public CVKFPSRenderable(CVKVisualProcessor visualProcessor) {
        parent = visualProcessor;
        
        currentFPS = new ArrayList<>();
        currentFPS.add(7);
        currentFPS.add(3);
        currentFPS.add(0);  // unused
        currentFPS.add(0);  // unused
    }  
        
    @Override
    public int GetVertexCount(){ return 4; }
  
    public int Init() {
        int ret = VK_SUCCESS;
        for (int digit = 0; digit < 10; ++digit) {
            // Returns the index of the icon, not a success code
            parent.GetTextureAtlas().AddIcon(Integer.toString(digit));
        }
        return ret;
    }
    
    // LIFTED FROM FPSRenderable.java
    private float calculateXProjectionScale(final int[] viewport) {
        // calculate the number of pixels a cvkScene object of y-length 1 projects to.
        final Vector4f proj1 = new Vector4f();
        //TT: Projects 0,0,0 into an identity matrix scaled to the width and 
        // height of the viewport (in pixels) and a z of 0->1.  This will lead to
        // proj1 being width/2, height/2, 0.5
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        final Vector3f unitPosition = new Vector3f(1, 0, 0);
        //TT: Projecting 1,0,0 into the same space yields width, height/2, 0.5
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        //TT: the above seems like a lot of messing around to arrive at 
        // xScale = width/2
        final float xScale = proj2.getX() - proj1.getX();
        //TT: 4/(width/2), what are the 256 and 64?  Magic numbers rock.  Maybe
        // dimensions of the generated icon texture?  8/width.
        // 256 is the icon width and height in the atlas texture, 64 is the number
        // of icons you can fit in a 2048x2048 texture.
        return (256.0f / 64) / xScale;
    }
    private float calculateYProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.getY() - proj1.getY();
        return (256.0f / 64) / yScale;
    }    
    
    
    private int CreateUniformBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int imageCount = cvkSwapChain.GetImageCount();        

        vertexUniformBuffers = new ArrayList<>();
        geometryUniformBuffers = new ArrayList<>();        
        for (int i = 0; i < imageCount; ++i) {   
            CVKBuffer vertUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                          VertexUniformBufferObject.SIZEOF,
                                                          VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            vertUniformBuffer.DEBUGNAME = String.format("CVKFPSRenderable vertUniformBuffer %d", i);   
            vertexUniformBuffers.add(vertUniformBuffer);            
            
            CVKBuffer geomUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                          GeometryUniformBufferObject.SIZEOF,
                                                          VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            geomUniformBuffer.DEBUGNAME = String.format("CVKFPSRenderable geomUniformBuffer %d", i);                                    
            geometryUniformBuffers.add(geomUniformBuffer);            
        }
        
        return UpdateUniformBuffers(stack, cvkSwapChain);                
    }
    
    
    private int UpdateUniformBuffers(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
     
        // TODO_TT: investigate a frames in flight < imageCount approach
        int imageCount = cvkSwapChain.GetImageCount();        
        
        // LIFTED FROM FPSRenderable.reshape(...)
        //TT: the logic here seems to be the FPS text needs to be 50 pixels from the 
        // edges, the calculation of dx and dy implies that the viewport is 
        //-width/2, -height/2, width/2, height/2
        
        // whenever the drawable shape changes, recalculate the place where the fps is drawn
        
        // This is a GL viewport where the screen space origin is in the bottom left corner
        //final int[] viewport = new int[]{0, 0, cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight()};
        
        // In Vulkan the screen space origin is in the top left hand corner.  Note we put the origin at 0, H and 
        // the viewport dimensions are W and -H.  The -H means we we still have a 0->H range, just running in the
        // opposite direction to GL.
        final int[] viewport = new int[]{0, cvkSwapChain.GetHeight(), cvkSwapChain.GetWidth(), -cvkSwapChain.GetHeight()};
        
        final int dx = cvkSwapChain.GetWidth() / 2 - FPS_OFFSET;
        final int dy = cvkSwapChain.GetHeight() / 2 - FPS_OFFSET;
        pxScale = calculateXProjectionScale(viewport);
        pyScale = calculateYProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, bottomRightCorner);

        
        // set the number of pixels per world unit at distance 1
        geometryUBO.pixelDensity = (float)(cvkSwapChain.GetHeight() * 0.5 / Math.tan(Math.toRadians(FIELD_OF_VIEW)));        
        geometryUBO.pScale = pyScale;
             
        
        // LIFTED FROM FPSRenerable.display(...)
        // Initialise source data to sensible values   
        final Matrix44f scalingMatrix = new Matrix44f();
        scalingMatrix.makeScalingMatrix(pxScale, pyScale, 0);
        final Matrix44f srMatrix = new Matrix44f();
        srMatrix.multiply(scalingMatrix, IDENTITY_44F);

        // build the fps matrix by translating the sr matrix
        final Matrix44f translationMatrix = new Matrix44f();
        translationMatrix.makeTranslationMatrix(bottomRightCorner.getX(),
                                                bottomRightCorner.getY(), 
                                                bottomRightCorner.getZ());
        vertexUBO.mvMatrix.multiply(translationMatrix, srMatrix);        
                      
                
        // In the JOGL version these were in a static var CAMERA that never changed
        vertexUBO.visibilityLow = 0.0f;
        vertexUBO.visibilityHigh = 1.0f;
        
        // Get the projection matrix from our parent
        geometryUBO.pMatrix.set(parent.GetProjectionMatrix());
                
        // Copy the UBOs in VK buffers we can bind to a descriptor set  
        PointerBuffer pData = stack.mallocPointer(1);
        for (int i = 0; i < imageCount; ++i) {   
            // Fill of the vertex uniform buffer
            int size = VertexUniformBufferObject.SIZEOF;
            CVKBuffer vertUniformBuffer = vertexUniformBuffers.get(i);                       
            ret = vkMapMemory(cvkDevice.GetDevice(), vertUniformBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
            if (VkFailed(ret)) { return ret; }
            {
                vertexUBO.CopyTo(pData.getByteBuffer(0, size));
            }
            vkUnmapMemory(cvkDevice.GetDevice(), vertUniformBuffer.GetMemoryBufferHandle());             
            
            // Fill of the geometry uniform buffer
            size = GeometryUniformBufferObject.SIZEOF;
            CVKBuffer geomUniformBuffer = geometryUniformBuffers.get(i);
            ret = vkMapMemory(cvkDevice.GetDevice(), geomUniformBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
            if (VkFailed(ret)) { return ret; }
            {
                geometryUBO.CopyTo(pData.getByteBuffer(0, size));
            }
            vkUnmapMemory(cvkDevice.GetDevice(), geomUniformBuffer.GetMemoryBufferHandle());                                              
        }
        
        return ret;
    }
    
    
    private int CreateVertexBuffers(CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
    
        int imageCount = cvkSwapChain.GetImageCount();               
        vertexBuffers = new ArrayList<>();
        
        // Size to upper limit, we don't have to draw each one.
        int size = vertices.length * Vertex.SIZEOF;
        
        //TODO_TT: most if not all of Constellation's vertex buffers won't change after creation
        // so they should probably be allocated as VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT and staged
        // to once to fill them (staging buffer this is host visible then copied to the device local)
        for (int i = 0; i < imageCount; ++i) {   
            CVKBuffer cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                         size,
                                                         VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                                         VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            cvkVertexBuffer.DEBUGNAME = String.format("CVKFPSRenderable cvkVertexBuffer %d", i);
            vertexBuffers.add(cvkVertexBuffer);        
        }
        
        // Populate them with some values
        UpdateVertexBuffers();
        
        return ret;  
    }
    
    
    private int UpdateVertexBuffers() {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {     
            // Size to upper limit, we don't have to draw each one.
            int size = vertices.length * Vertex.SIZEOF;
            for (int i = 0; i < vertices.length; ++i) {
                int data[] = new int[2];

                int digit = currentFPS.get(i);
                
                final int foregroundIconIndex;
                if (digit >= 0 && digit < 10) {
                    foregroundIconIndex = parent.GetTextureAtlas().AddIcon(Integer.toString(digit));
                } else {
                    foregroundIconIndex = digit;
                }
               
                final int backgroundIconIndex = CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;

                // packed icon indices
                data[0] = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);

                // offset which is used for this digit's position in SimpleIcon.vs
                data[1] = i * DIGIT_ICON_OFFSET;

                // colour which is inexplicably converted to a 4x4 matrix in the vert shader
                Vector4f colour = new Vector4f(1.0f,1.0f,1.0f,1.0f);

                vertices[i] = new Vertex(data, colour);
            }

            // Populate
            for (int i = 0; i < vertexBuffers.size(); ++i) {   
                CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
                PointerBuffer data = stack.mallocPointer(1);
                vkMapMemory(cvkDevice.GetDevice(), cvkVertexBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
                {
                    Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
                }
                vkUnmapMemory(cvkDevice.GetDevice(), cvkVertexBuffer.GetMemoryBufferHandle());
            }
        }
        
        return ret;         
    }
    
    
    public int CreateCommandBuffers(CVKSwapChain cvkSwapChain){
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        commandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_SECONDARY);
            buffer.DEBUGNAME = String.format("CVKFPSRenderable %d", i);
            commandBuffers.add(buffer);
        }
        
        CVKLOGGER.log(Level.INFO, "Init Command Buffer - FPSRenderable");
        
        return ret;
    }
    
    @Override
    public int RecordCommandBuffer(CVKSwapChain cvkSwapChain, VkCommandBufferInheritanceInfo inheritanceInfo, int index){
        VerifyInRenderThread();
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);           
                
        int ret = VK_SUCCESS;
     
        // TODO_TT: investigate a frames in flight < imageCount approach
        try (MemoryStack stack = stackPush()) {
 
            // Fill
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.pNext(0);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);  // hard coding this for now
            beginInfo.pInheritanceInfo(inheritanceInfo);             

            VkCommandBuffer commandBuffer = commandBuffers.get(index).GetVKCommandBuffer();
            ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
            checkVKret(ret);

            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelines.get(index));

            LongBuffer pVertexBuffers = stack.longs(vertexBuffers.get(index).GetBufferHandle());
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
            vkCmdDraw(commandBuffer,
                      GetVertexCount(),  //number of verts == number of digits
                      1,  //no instancing, but we must draw at least 1 point
                      0,  //first vert index
                      0); //first instance index (N/A)                         
            ret = vkEndCommandBuffer(commandBuffer);
            checkVKret(ret);
        }
        
        return ret;
    }    


    private int CreateDescriptorSets(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret;
     
        int imageCount = cvkSwapChain.GetImageCount();

        // The same layout is used for each descriptor set (each descriptor set is
        // identical but allow the GPU and CPU to desynchronise_.
        LongBuffer layouts = stack.mallocLong(imageCount);
        for (int i = 0; i < imageCount; ++i) {
            layouts.put(i, hDescriptorLayout);
        }

        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool(cvkSwapChain.GetDescriptorPoolHandle());
        allocInfo.pSetLayouts(layouts);            

        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
        ret = vkAllocateDescriptorSets(cvkDevice.GetDevice(), allocInfo, pDescriptorSets);
        if (VkFailed(ret)) { return ret; }      
        
        return UpdateDescriptorSets(stack, cvkSwapChain);
    }
    
    private int UpdateDescriptorSets(MemoryStack stack, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
     
        int imageCount = cvkSwapChain.GetImageCount();

        // Struct for the size of the uniform buffer used by SimpleIcon.vs (we fill the actual buffer below)
        VkDescriptorBufferInfo.Buffer vertBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        vertBufferInfo.offset(0);
        vertBufferInfo.range(VertexUniformBufferObject.SIZEOF);

        // Struct for the size of the uniform buffer used by SimpleIcon.gs (we fill the actual buffer below)
        VkDescriptorBufferInfo.Buffer geomBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        geomBufferInfo.offset(0);
        geomBufferInfo.range(GeometryUniformBufferObject.SIZEOF);      

        // Struct for the size of the image sampler used by SimpleIcon.fs
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(parent.GetTextureAtlas().GetAtlasImageViewHandle());
        imageInfo.sampler(parent.GetTextureAtlas().GetAtlasSamplerHandle());            

        // We need 3 write descriptors, 2 for uniform buffers (vs + gs) and one for texture (fs)
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(3, stack);

        VkWriteDescriptorSet vertUBDescriptorWrite = descriptorWrites.get(0);
        vertUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertUBDescriptorWrite.dstBinding(0);
        vertUBDescriptorWrite.dstArrayElement(0);
        vertUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        vertUBDescriptorWrite.descriptorCount(1);
        vertUBDescriptorWrite.pBufferInfo(vertBufferInfo);

        VkWriteDescriptorSet geomUBDescriptorWrite = descriptorWrites.get(1);
        geomUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        geomUBDescriptorWrite.dstBinding(1);
        geomUBDescriptorWrite.dstArrayElement(0);
        geomUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        geomUBDescriptorWrite.descriptorCount(1);
        geomUBDescriptorWrite.pBufferInfo(geomBufferInfo);            

        VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(2);
        samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        samplerDescriptorWrite.dstBinding(2);
        samplerDescriptorWrite.dstArrayElement(0);
        samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        samplerDescriptorWrite.descriptorCount(1);
        samplerDescriptorWrite.pImageInfo(imageInfo);                                


        for (int i = 0; i < imageCount; ++i) {
            long descriptorSet = pDescriptorSets.get(i);

            vertBufferInfo.buffer(vertexUniformBuffers.get(i).GetBufferHandle());
            geomBufferInfo.buffer(geometryUniformBuffers.get(i).GetBufferHandle());
 
            vertUBDescriptorWrite.dstSet(descriptorSet);
            geomUBDescriptorWrite.dstSet(descriptorSet);
            samplerDescriptorWrite.dstSet(descriptorSet);

            // Update the descriptors with a write and no copy
            vkUpdateDescriptorSets(cvkDevice.GetDevice(), descriptorWrites, null);
        }
        
        // Cache atlas handles so we know when to recreate descriptors
        hAtlasSampler = parent.GetTextureAtlas().GetAtlasSamplerHandle();
        hAtlasImageView = parent.GetTextureAtlas().GetAtlasImageViewHandle();            
        
        return ret;
    }
    
    
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
        if (pDescriptorSets != null) {
            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
            ret = vkFreeDescriptorSets(cvkDevice.GetDevice(), cvkSwapChain.GetDescriptorPoolHandle(), pDescriptorSets);
            pDescriptorSets = null;
            checkVKret(ret);
        }
        
        return ret;
    }

    
    private int CreatePipelines(CVKSwapChain cvkSwapChain) {
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkSwapChain.GetSwapChainHandle()        != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain.GetRenderPassHandle()       != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain.GetDescriptorPoolHandle()   != VK_NULL_HANDLE);
        CVKAssert(hVertexShader   != VK_NULL_HANDLE);
        CVKAssert(hGeometryShader != VK_NULL_HANDLE);
        CVKAssert(hFragmentShader != VK_NULL_HANDLE);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        final int imageCount = cvkSwapChain.GetImageCount();                
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {                 
            // A complete pipeline for each swapchain image.  Wasteful?
            pipelines = new ArrayList<>(imageCount);            
            pipelineLayouts = new ArrayList<>(imageCount);   
            for (int i = 0; i < imageCount; ++i) {                              
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


            
                // TODO: Generalize map of shaders and type per renderable
                // Then can dynamically set here? Think I saw something like
                // this in the Oreon engine.
                ByteBuffer entryPoint = stack.UTF8("main");

                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);

                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(hVertexShader);
                vertShaderStageInfo.pName(entryPoint);

                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
                geomShaderStageInfo.module(hGeometryShader);
                geomShaderStageInfo.pName(entryPoint);            

                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(hFragmentShader);
                fragShaderStageInfo.pName(entryPoint);

                // ===> VERTEX STAGE <===
                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(Vertex.GetBindingDescription());
                vertexInputInfo.pVertexAttributeDescriptions(Vertex.GetAttributeDescriptions());

                // ===> ASSEMBLY STAGE <===
                // Triangle list is stipulated by the layout of the out attribute of
                // SimpleIcon.gs
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
//                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
                
                // Generalize me! Parameter?
                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_POINT_LIST);
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
                scissor.extent(cvkSwapChain.GetExtent());

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
                CVKAssert(hPipelineLayout != VK_NULL_HANDLE);
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
                if (VkFailed(ret)) { return ret; }
                CVKAssert(pGraphicsPipeline.get(0) != VK_NULL_HANDLE);  
                pipelines.add(pGraphicsPipeline.get(0));                      
            }
        }
        
        CVKLOGGER.log(Level.INFO, "Graphics Pipeline created for FPSRenderable class.");
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
        
    private void DestroyCommandBuffers() {         
        if (null != commandBuffers) {
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
        
        if (geometryUniformBuffers != null) {
            geometryUniformBuffers.forEach(el -> {el.Destroy();});
            geometryUniformBuffers = null;
        }        
    }
    
    @Override
    public void Destroy() {
        DestroyVertexBuffers();
        DestroyUniformBuffers();
        DestroyDescriptorSets();
        DestroyCommandBuffers();
        DestroyPipelines();
        DestroyPipelineLayouts();
        DestroyCommandBuffers();  
        
        CVKAssert(pipelines == null);
        CVKAssert(pipelineLayouts == null);
        CVKAssert(pDescriptorSets == null);
        CVKAssert(vertexUniformBuffers == null);
        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(vertexBuffers == null);
        CVKAssert(commandBuffers == null);      
    }    
    
    @Override
    public int SwapChainRecreated(CVKSwapChain cvkSwapChain) { 
        // Cache the last swap chain as it is needed to release descriptor sets and
        // if we are destroyed by the finalizer we'll need it
        this.cvkSwapChain = cvkSwapChain;
        
        int ret;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines == null || pipelines.size() != cvkSwapChain.GetImageCount()) {        
            DestroyVertexBuffers();
            DestroyUniformBuffers();
            DestroyDescriptorSets();
            DestroyCommandBuffers();
            DestroyPipelines();
            DestroyPipelineLayouts();
            DestroyCommandBuffers(); 

            CVKAssert(pipelines == null);
            CVKAssert(pipelineLayouts == null);
            CVKAssert(pDescriptorSets == null);
            CVKAssert(vertexUniformBuffers == null);
            CVKAssert(geometryUniformBuffers == null);
            CVKAssert(vertexBuffers == null);
            CVKAssert(commandBuffers == null);           

            try (MemoryStack stack = stackPush()) {                     
                ret = CreateUniformBuffers(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; }

                ret = CreateDescriptorSets(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; } 

                ret = CreateVertexBuffers(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }   

                ret = CreateCommandBuffers(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }            

                ret = CreatePipelines(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }                                       
            }      
        } else {
            // This is the resize path, image count is unchanged.  We need to recreate
            // pipelines as Vulkan doesn't have a good mechanism to update them and as
            // they define the viewport and scissor rect they are now out of date.  We
            // also need to update the uniform buffer as a new image size will mean a
            // different position for our FPS.  After updating the uniform buffers we
            // need to update the descriptor sets that bind the uniform buffers as well.
            DestroyPipelines();
            DestroyDescriptorSets();
            CVKAssert(pipelines == null);
            
            try (MemoryStack stack = stackPush()) {                     
                ret = CreatePipelines(cvkSwapChain);
                if (VkFailed(ret)) { return ret; }                           
                
                ret = UpdateUniformBuffers(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; }
  
                // Updating rather than recreating raises:
                // VkDebugUtilsMessengerCallbackEXTI.java:47>Validation layer: Validation Error: [ VUID-VkWriteDescriptorSet-dstSet-00320 ] Object 0: handle = 0x2206b694060, type = VK_OBJECT_TYPE_INSTANCE; | MessageID = 0x8e0ca77 | Invalid VkDescriptorSet Object 0xb2a1ba00000001d0. The Vulkan spec states: dstSet must be a valid VkDescriptorSet handle (https://www.khronos.org/registry/vulkan/specs/1.1-extensions/html/vkspec.html#VUID-VkWriteDescriptorSet-dstSet-00320)
//                ret = UpdateDescriptorSets(stack, cvkSwapChain);
//                if (VkFailed(ret)) { return ret; }    

                ret = CreateDescriptorSets(stack, cvkSwapChain);
                if (VkFailed(ret)) { return ret; } 
            }              
        }
        
        return ret; 
    }
         
    public static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try {
            ByteBuffer vsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.vs.spv");
            if (vsBytes.capacity() == 0) {
                throw new RuntimeException("Failed to load compiled/SimpleIcon.vs.spv");
            }
            ByteBuffer gsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.gs.spv");
            if (vsBytes.capacity() == 0) {
                throw new RuntimeException("Failed to load compiled/SimpleIcon.gs.spv");
            }            
            ByteBuffer fsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/SimpleIcon.fs.spv");
            if (vsBytes.capacity() == 0) {
                throw new RuntimeException("Failed to load compiled/SimpleIcon.fs.spv");
            }            
            
            hVertexShader = CVKShaderUtils.createShaderModule(vsBytes, cvkDevice.GetDevice());
            if (hVertexShader == VK_NULL_HANDLE) {
                throw new RuntimeException("Failed to create shader from SimpleIcon.vs.spv bytes");
            }            
            hGeometryShader = CVKShaderUtils.createShaderModule(gsBytes, cvkDevice.GetDevice());
            if (hGeometryShader == VK_NULL_HANDLE) {
                throw new RuntimeException("Failed to create shader from SimpleIcon.gs.spv bytes");
            }             
            hFragmentShader = CVKShaderUtils.createShaderModule(fsBytes, cvkDevice.GetDevice());            
            if (hFragmentShader == VK_NULL_HANDLE) {
                throw new RuntimeException("Failed to create shader from SimpleIcon.fs.spv bytes");
            }      
            
            MemoryUtil.memFree(vsBytes);
            MemoryUtil.memFree(gsBytes);
            MemoryUtil.memFree(fsBytes);
        } catch (IOException e) {
            //TODO_TT
        }
        
        return ret;
    }
    
    
    public static int CreateDescriptorLayout(CVKDevice cvkDevice) {
        int ret;
        
        try(MemoryStack stack = stackPush()) {
            /*
            Vertex shader needs a uniform buffer.
            Geometry shader needs a different uniform buffer.
            Fragment shader needs a sampler2Darray
            */

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(3, stack);

            VkDescriptorSetLayoutBinding vertexUBOLayout = bindings.get(0);
            vertexUBOLayout.binding(0);
            vertexUBOLayout.descriptorCount(1);
            vertexUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            vertexUBOLayout.pImmutableSamplers(null);
            vertexUBOLayout.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            
            VkDescriptorSetLayoutBinding geomUBOLayout = bindings.get(1);
            geomUBOLayout.binding(1);
            geomUBOLayout.descriptorCount(1);
            geomUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geomUBOLayout.pImmutableSamplers(null);
            geomUBOLayout.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);            

            VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(2);
            samplerLayoutBinding.binding(2);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

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
    
    @Override
    public int DisplayUpdate(CVKSwapChain cvkSwapChain, int imageIndex) {
        return DebugUpdateFPS();
    }
    
    static int counter = 0;
    private int DebugUpdateFPS(){
        counter++;
        
        // Debug code to update every 100 frames
        if (counter % 100 != 0)
            return VK_SUCCESS;

        currentFPS.set(0, GetRandom(0,9));
        currentFPS.set(1, GetRandom(0,9));
        currentFPS.set(2, GetRandom(10, parent.GetTextureAtlas().GetAtlasIconCount()));
        currentFPS.set(3, GetRandom(10, parent.GetTextureAtlas().GetAtlasIconCount()));
        
        return UpdateVertexBuffers();   
    }
    
    private int GetRandom(int min, int max){
        return Math.min(max, (int)(Math.random() * ((max - min) + 1)) + min);
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[], int descriptorSetCount) {
        CVKAssert(descriptorTypeCounts.length == (VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT + 1));
        // SimpleIcon.vs
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        // SimpleIcon.gs
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        // SimpleIcon.fs
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
        
        // One descriptor set is required
        ++descriptorSetCount;
    } 
    
    @Override
    public int DeviceInitialised(CVKDevice cvkDevice) {
        this.cvkDevice = cvkDevice;
        return VK_SUCCESS;
    }   
    
    @Override
    public boolean SharedResourcesNeedUpdating() { 
        VerifyInRenderThread();
        
        return hAtlasSampler != parent.GetTextureAtlas().GetAtlasSamplerHandle() ||
               hAtlasImageView != parent.GetTextureAtlas().GetAtlasImageViewHandle(); }
    
    @Override
    public int RecreateSharedResources(CVKSwapChain cvkSwapChain) { 
        VerifyInRenderThread();
        
        int ret;
        this.cvkSwapChain = cvkSwapChain;
        ret = DestroyDescriptorSets();
        if (VkFailed(ret)) {
            return ret;
        }
        try (MemoryStack stack = stackPush()) {
            //TODO_TT: updating the descriptor sets is probably a better approach to recreation
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
