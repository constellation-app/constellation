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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4i;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.IDENTITY_44F;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkWriteDescriptorSet;


/*******************************************************************************
 * CVKBlazesRenderable
 * 
 * 
 *******************************************************************************/

public class CVKBlazesRenderable extends CVKRenderable {
    // Resources recreated with the swap chain (dependent on the image count)    
    private LongBuffer pDescriptorSets = null; 
    private CVKCommandBuffer cvkDisplayCommandBuffer;  
    private CVKBuffer cvkVertexBuffer;   
    private CVKBuffer cvkVertexUniformBuffer;
    private CVKBuffer cvkGeometryUniformBuffer;      
    private CVKBuffer cvkFragmentUniformBuffer; 
    
    // The UBO staging buffers are a known size so created outside user events
    private CVKBuffer cvkVertexUBStagingBuffer = null;
    private CVKBuffer cvkGeometryUBStagingBuffer = null;
    private CVKBuffer cvkFragmentUBStagingBuffer = null;
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private final GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject(); 
    private final FragmentUniformBufferObject fragmentUBO = new FragmentUniformBufferObject();
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;  

    // Resources we don't own but use and must track so we know when to update
    // our descriptors
    private long hPositionBuffer = VK_NULL_HANDLE;
    private long hPositionBufferView = VK_NULL_HANDLE;    
    
    // Push constants for shaders contains the MV matrix and drawHitTest int
    private ByteBuffer modelViewPushConstants = null;
    private ByteBuffer hitTestPushConstants = null;
    private static final int MODEL_VIEW_PUSH_CONSTANT_STAGES = VK_SHADER_STAGE_VERTEX_BIT;
    private static final int HIT_TEST_PUSH_CONSTANT_STAGES = VK_SHADER_STAGE_FRAGMENT_BIT;
    private static final int MODEL_VIEW_PUSH_CONSTANT_SIZE = Matrix44f.BYTES;
    private static final int HIT_TEST_PUSH_CONSTANT_SIZE = Integer.BYTES;    
    
    
    // ========================> Classes <======================== \\
    
    protected static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int BYTES = Vector4f.BYTES + Vector4i.BYTES;
        private static final int OFFSETOF_DATA = Vector4f.BYTES;
        private static final int OFFSET_CLR = 0;
        private static final int BINDING = 0;
        private final Vector4f colour = new Vector4f();
        private final Vector4i data = new Vector4i();
        
        private Vertex() {}
                    
       
        public Vertex(ConstellationColor inColour, 
                      float visibility,
                      int vertexID,
                      int angle) {
            colour.a[0] = inColour.getRed();
            colour.a[1] = inColour.getGreen();
            colour.a[2] = inColour.getBlue();
            colour.a[3] = visibility;
            
            data.a[0]   = vertexID;
            data.a[1]   = -1;
            data.a[2]   = angle;
            data.a[3]   = 0;
        }       
        
        public void CopyToSequentially(ByteBuffer buffer) {
            buffer.putFloat(colour.a[0]);
            buffer.putFloat(colour.a[1]);
            buffer.putFloat(colour.a[2]);
            buffer.putFloat(colour.a[3]);
            buffer.putInt(data.a[0]);
            buffer.putInt(data.a[1]);
            buffer.putInt(data.a[2]);
            buffer.putInt(data.a[3]);              
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
        protected static VkVertexInputBindingDescription.Buffer GetBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            // If we bind multiple vertex buffers with different descriptions
            // this is the index of this description occupies in the array of
            // bound descriptions.
            bindingDescription.binding(BINDING);
            bindingDescription.stride(Vertex.BYTES);
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
        protected static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // vColor
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            posDescription.offset(OFFSET_CLR);

            // data
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SINT);
            colorDescription.offset(OFFSETOF_DATA);

            return attributeDescriptions.rewind();
        }
    }  
    
    @Override
    protected VkVertexInputBindingDescription.Buffer GetVertexBindingDescription() {
        return Vertex.GetBindingDescription();
    }
    
    @Override
    protected VkVertexInputAttributeDescription.Buffer GetVertexAttributeDescriptions() {
        return Vertex.GetAttributeDescriptions();
    }             
    
    protected static class VertexUniformBufferObject {
        public float morphMix = 0;  
        private static Integer padding = null;
        
        protected static int SizeOf() {
            if (padding == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (16 * Float.BYTES));

                int sizeof = 1 * Float.BYTES; // morphMix
                final int overrun = sizeof % minAlignment;
                padding = overrun > 0 ? minAlignment - overrun : 0;             
            }
            
            return 1 * Float.BYTES + // morphMix
                   padding;
        }        
        
        private void CopyTo(ByteBuffer buffer) {
            buffer.putFloat(morphMix);
            
            for (int i = 0; i < padding; ++i) {
                buffer.put((byte)0);
            }            
        }         
    }      
    
    protected static class GeometryUniformBufferObject {                                           
        private final Matrix44f pMatrix = new Matrix44f();  
        private float scale;
        private float visibilityLow;
        private float visibilityHigh;
        private static Integer padding = null;   
                    
        protected static int SizeOf() {
            if (padding == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (Matrix44f.BYTES));

                int sizeof = Matrix44f.BYTES +     // pMatrix
                             1 * Float.BYTES +     // scale
                             1 * Float.BYTES +     // visibilityLow
                             1 * Float.BYTES;      // visibilityHigh  

                final int overrun = sizeof % minAlignment;
                padding = overrun > 0 ? minAlignment - overrun : 0;             
            }
            
            return Matrix44f.BYTES  +     // pMatrix
                    1 * Float.BYTES +     // scale
                    1 * Float.BYTES +     // visibilityLow
                    1 * Float.BYTES +     // visibilityHigh  
                    padding;
        }
        
        private void CopyTo(ByteBuffer buffer) {  
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            } 
            buffer.putFloat(scale);
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);         
                        
            for (int i = 0; i < padding; ++i) {
                buffer.put((byte)0);
            }          
        }         
    }   
    
    protected static class FragmentUniformBufferObject {                                           
        private float opacity;
        private static Integer padding = null;   
                    
        protected static int SizeOf() {
            if (padding == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (Matrix44f.BYTES));

                int sizeof = 1 * Float.BYTES;      // opacity  

                final int overrun = sizeof % minAlignment;
                padding = overrun > 0 ? minAlignment - overrun : 0;             
            }
            
            return 1 * Float.BYTES +     // opacity  
                    padding;
        }
        
        private void CopyTo(ByteBuffer buffer) {  
            buffer.putFloat(opacity);     
                        
            for (int i = 0; i < padding; ++i) {
                buffer.put((byte)0);
            }          
        }         
    }    
                      
    
    // ========================> Shaders <======================== \\
    
    @Override
    protected String GetVertexShaderName() { return "Blaze.vs"; }
    
    @Override
    protected String GetGeometryShaderName() { return "Blaze.gs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "Blaze.fs"; }   
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKBlazesRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
        depthTest = false;
    }              
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                    GetLogger(),
                                                    "CVKBlazesRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer");   
        cvkGeometryUBStagingBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      GetLogger(),
                                                      "CVKBlazesRenderable.CreateUBOStagingBuffers cvkGeometryUBStagingBuffer"); 
        cvkFragmentUBStagingBuffer = CVKBuffer.Create(FragmentUniformBufferObject.SizeOf(),
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      GetLogger(),
                                                      "CVKBlazesRenderable.CreateUBOStagingBuffers cvkFragmentUBStagingBuffer");        
    }
    
    @Override
    public int Initialise() {
        int ret = super.Initialise();
        if (VkFailed(ret)) { return ret; }
        
        // Check for double initialisation
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);

        CreatePushConstants();         
        
        ret = CreateDescriptorLayout();
        if (VkFailed(ret)) { return ret; }   
        
        ret = CreatePipelineLayout();
        if (VkFailed(ret)) { return ret; }          
                
        CreateUBOStagingBuffers();
        
        return ret;
    }        
    
    private void DestroyStagingBuffers() {        
        if (cvkVertexStagingBuffer != null) {
            cvkVertexStagingBuffer.Destroy();
            cvkVertexStagingBuffer = null;
        }        
        if (cvkVertexUBStagingBuffer != null) {
            cvkVertexUBStagingBuffer.Destroy();
            cvkVertexUBStagingBuffer = null;
        }
        if (cvkGeometryUBStagingBuffer != null) {
            cvkGeometryUBStagingBuffer.Destroy();
            cvkGeometryUBStagingBuffer = null;
        }
        if (cvkFragmentUBStagingBuffer != null) {
            cvkFragmentUBStagingBuffer.Destroy();
            cvkFragmentUBStagingBuffer = null;
        }        
    }
    
    @Override
    public void Destroy() {
        DestroyVertexBuffer();
        DestroyVertexUniformBuffer();
        DestroyGeometryUniformBuffer();
        DestroyFragmentUniformBuffer();
        DestroyDescriptorSets();
        DestroyDescriptorLayout();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyCommandBuffer();
        DestroyStagingBuffers();
        DestroyPushConstants();
        
        CVKAssertNull(cvkVertexBuffer);
        CVKAssertNull(cvkVertexUniformBuffer);
        CVKAssertNull(cvkGeometryUniformBuffer);
        CVKAssertNull(cvkFragmentUniformBuffer);
        CVKAssertNull(pDescriptorSets);
        CVKAssertNull(hDescriptorLayout);  
        CVKAssertNull(cvkDisplayCommandBuffer);        
        CVKAssertNull(displayPipelines);
        CVKAssertNull(hPipelineLayout);    
        CVKAssertNull(modelViewPushConstants);
        CVKAssertNull(hitTestPushConstants);
    }
    
       
    // ========================> Swap chain <======================== \\
       
    @Override
    protected int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (displayPipelines != null && swapChainImageCountChanged) {  
            DestroyVertexBuffer();
            DestroyVertexUniformBuffer();
            DestroyGeometryUniformBuffer();
            DestroyFragmentUniformBuffer();
            DestroyDescriptorSets();
            DestroyCommandBuffer();     
            DestroyPipelines();
            DestroyCommandBuffer();                                  
        }
        
        return VK_SUCCESS; 
    }
    
    @Override
    public int SetNewSwapChain(CVKSwapChain newSwapChain) {
        int ret = super.SetNewSwapChain(newSwapChain);
        if (VkFailed(ret)) { return ret; }
        
        if (swapChainImageCountChanged) {
            // The number of images has changed, we need to rebuild all image
            // buffered resources
            SetVertexUBOState(CVK_RESOURCE_NEEDS_REBUILD);
            SetGeometryUBOState(CVK_RESOURCE_NEEDS_REBUILD);       
            SetFragmentUBOState(CVK_RESOURCE_NEEDS_REBUILD);  
            SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetCommandBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetDescriptorSetsState(CVK_RESOURCE_NEEDS_REBUILD);
            SetPipelinesState(CVK_RESOURCE_NEEDS_REBUILD);
        } else {
            // View frustum and projection matrix likely have changed.  We don't
            // need to rebuild our displayPipelines as the frustum is set by dynamic
            // state in RecordDisplayCommandBuffer
            if (geometryUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetGeometryUBOState(CVK_RESOURCE_NEEDS_UPDATE); 
            }
        }
        
        return ret;
    } 
    
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNull(cvkVertexBuffer);
        
        int ret = VK_SUCCESS;
    
        // We can only create vertex buffers if we have something to put in them
        if (cvkVertexStagingBuffer.GetBufferSize() > 0) { 
            cvkVertexBuffer = CVKBuffer.Create(cvkVertexStagingBuffer.GetBufferSize(),
                                               VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                               GetLogger(),
                                               "CVKBlazesRenderable cvkVertexBuffer");

            // Populate them with some values
            return UpdateVertexBuffer();
        }
        
        return ret;  
    }    
    
    private int UpdateVertexBuffer() {
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssert(cvkVertexStagingBuffer != null);
        CVKAssert(cvkVertexBuffer != null);
        CVKAssert(cvkVertexStagingBuffer.GetBufferSize() == cvkVertexBuffer.GetBufferSize());
        int ret = VK_SUCCESS;     
            
        ret = cvkVertexBuffer.CopyFrom(cvkVertexStagingBuffer);
        if (VkFailed(ret)) { return ret; }
        
        // Note the staging buffer is not freed as we can simplify the update tasks
        // by just updating it and then copying it over again during ProcessRenderTasks().
        SetVertexBuffersState(CVK_RESOURCE_CLEAN);

        return ret;         
    }  
    
    @Override
    public int GetVertexCount() { return cvkVisualProcessor.GetDrawFlags().drawConnections() ? vertexCount : 0; }     
    
    private void DestroyVertexBuffer() {
        if (cvkVertexBuffer != null) {
            cvkVertexBuffer.Destroy();
            cvkVertexBuffer = null;
        }           
    }               
    
    
    // ========================> Uniform buffers <======================== \\
    
    private int CreateVertexUniformBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNull(cvkVertexUniformBuffer);
 
        cvkVertexUniformBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                  VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                  VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                  GetLogger(),
                                                  "CVKBlazesRenderable cvkVertexUniformBuffer");   
        return UpdateVertexUniformBuffer();
    }
        
    private int UpdateVertexUniformBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkVertexUBStagingBuffer);
        CVKAssertNotNull(cvkVertexUniformBuffer);
        
        int ret = VK_SUCCESS;
        
        // Populate the UBO.  This is easy to deal with, but not super efficient
        // as we are effectively staging into the staging buffer below.
        vertexUBO.morphMix = cvkVisualProcessor.getDisplayCamera().getMix();
        
        // Staging buffer so our VBO can be device local (most performant memory)
        ByteBuffer pMemory = cvkVertexUBStagingBuffer.StartMemoryMap(0, VertexUniformBufferObject.SizeOf());
        {
            vertexUBO.CopyTo(pMemory);
        }
        cvkVertexUBStagingBuffer.EndMemoryMap();
        pMemory = null;
        
        // Copy the staging buffer into the uniform buffer on the device  
        ret = cvkVertexUniformBuffer.CopyFrom(cvkVertexUBStagingBuffer);   
        if (VkFailed(ret)) { return ret; }
        
        UpdateVertexPushConstants();
        
        // We are done, reset the resource state
        SetVertexUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyVertexUniformBuffer() {
        if (cvkVertexUniformBuffer != null) {
            cvkVertexUniformBuffer.Destroy();
            cvkVertexUniformBuffer = null;
        }    
    }      
    
    private int CreateGeometryUniformBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNull(cvkGeometryUniformBuffer);

        cvkGeometryUniformBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                               GetLogger(),
                                                               "CVKBlazesRenderable cvkGeometryUniformBuffer");
        return UpdateGeometryUniformBuffer();
    }
    
    private int UpdateGeometryUniformBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkGeometryUBStagingBuffer);
        CVKAssertNotNull(cvkGeometryUniformBuffer);    
        
        int ret = VK_SUCCESS;
        
        // Populate the UBO.  This is easy to deal with, but not super efficient
        // as we are effectively staging into the staging buffer below.
        geometryUBO.pMatrix.set(cvkVisualProcessor.GetProjectionMatrix());
        geometryUBO.visibilityLow = cvkVisualProcessor.getDisplayCamera().getVisibilityLow();
        geometryUBO.visibilityHigh = cvkVisualProcessor.getDisplayCamera().getVisibilityHigh();    
        
        // Staging buffer so our VBO can be device local (most performant memory)
        ByteBuffer pMemory = cvkGeometryUBStagingBuffer.StartMemoryMap(0, GeometryUniformBufferObject.SizeOf());
        {
            geometryUBO.CopyTo(pMemory);
        }
        cvkGeometryUBStagingBuffer.EndMemoryMap();
        pMemory = null;
     
        // Copy the staging buffer into the uniform buffer on the device
        cvkGeometryUniformBuffer.CopyFrom(cvkGeometryUBStagingBuffer);   
        if (VkFailed(ret)) { return ret; }     
                    
        // We are done, reset the resource state
        SetGeometryUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyGeometryUniformBuffer() {
        if (cvkGeometryUniformBuffer != null) {
            cvkGeometryUniformBuffer.Destroy();
            cvkGeometryUniformBuffer = null;
        }                
    }
    
    private int CreateFragmentUniformBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNull(cvkFragmentUniformBuffer);
 
        cvkFragmentUniformBuffer = CVKBuffer.Create(FragmentUniformBufferObject.SizeOf(),
                                                    VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                    GetLogger(),
                                                    "CVKBlazesRenderable cvkFragmentUniformBuffer");      
        return UpdateFragmentUniformBuffer();
    }
        
    private int UpdateFragmentUniformBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkFragmentUBStagingBuffer);
        CVKAssertNotNull(cvkFragmentUniformBuffer);
        
        int ret = VK_SUCCESS;
        
        // Staging buffer so our VBO can be device local (most performant memory)
        ByteBuffer pMemory = cvkFragmentUBStagingBuffer.StartMemoryMap(0, FragmentUniformBufferObject.SizeOf());
        {
            fragmentUBO.CopyTo(pMemory);
        }
        cvkFragmentUBStagingBuffer.EndMemoryMap();
        pMemory = null;
        
        // Copy the staging buffer into the uniform buffer on the device
        cvkFragmentUniformBuffer.CopyFrom(cvkFragmentUBStagingBuffer);   
        if (VkFailed(ret)) { return ret; }
        
        // We are done, reset the resource state
        SetFragmentUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyFragmentUniformBuffer() {
        if (cvkFragmentUniformBuffer != null) {
            cvkFragmentUniformBuffer.Destroy();
            cvkFragmentUniformBuffer = null;
        }    
    }       

    // ========================> Push constants <======================== \\
    
    private int CreatePushConstants() {
        // Initialise push constants to identity mtx
        modelViewPushConstants = MemoryUtil.memAlloc(MODEL_VIEW_PUSH_CONSTANT_SIZE);
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                modelViewPushConstants.putFloat(IDENTITY_44F.get(iRow, iCol));
            }
        }
        
        // Set DrawHitTest to false
        hitTestPushConstants = MemoryUtil.memAlloc(HIT_TEST_PUSH_CONSTANT_SIZE);
        hitTestPushConstants.putInt(0);
        
        modelViewPushConstants.flip();
        hitTestPushConstants.flip();
        
        return VK_SUCCESS;
    }
    
    private void UpdateVertexPushConstants(){
        CVKAssertNotNull(cvkSwapChain);
        
        modelViewPushConstants.clear();
        Matrix44f mvMatrix = cvkVisualProcessor.getDisplayModelViewMatrix();
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                modelViewPushConstants.putFloat(mvMatrix.get(iRow, iCol));
            }
        }
        
        modelViewPushConstants.flip();        
    }
    
    protected void UpdatePushConstantsHitTest(boolean drawHitTest){
        CVKAssertNotNull(cvkSwapChain);
        
        hitTestPushConstants.clear();
        
        if (drawHitTest) {
            hitTestPushConstants.putInt(1);
        } else {
            hitTestPushConstants.putInt(0);
        }

        hitTestPushConstants.flip();        
    }
    
    private void DestroyPushConstants() {
        if (modelViewPushConstants != null) {
            memFree(modelViewPushConstants);
            modelViewPushConstants = null;
        }
        
        if (hitTestPushConstants != null) {
            memFree(hitTestPushConstants);
            hitTestPushConstants = null;
        }
    }
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssertNotNull(cvkSwapChain);
        
        cvkDisplayCommandBuffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), "CVKLoopsRenderable cvkDisplayCommandBuffer");                    
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return VK_SUCCESS;
    }   
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {
        return cvkDisplayCommandBuffer.GetVKCommandBuffer(); 
    }       
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(CVKDevice.GetCommandPoolHandle());
        CVKAssertNotNull(cvkSwapChain);
                
        int ret;     
         
        CVKAssertNotNull(cvkDisplayCommandBuffer);
        CVKAssert(displayPipelines.get(imageIndex) != null);

        cvkDisplayCommandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, inheritanceInfo);

        cvkDisplayCommandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        cvkDisplayCommandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());

        cvkDisplayCommandBuffer.BindGraphicsPipeline(displayPipelines.get(imageIndex));
        cvkDisplayCommandBuffer.BindVertexInput(cvkVertexBuffer.GetBufferHandle());

        // Push MV matrix to the vertex shader
        cvkDisplayCommandBuffer.PushConstants(hPipelineLayout, MODEL_VIEW_PUSH_CONSTANT_STAGES, 0, modelViewPushConstants);

        // Push drawHitTest flag to the geometry shader
        cvkDisplayCommandBuffer.PushConstants(hPipelineLayout, HIT_TEST_PUSH_CONSTANT_STAGES, Matrix44f.BYTES, hitTestPushConstants);

        cvkDisplayCommandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));

        cvkDisplayCommandBuffer.Draw(GetVertexCount());

        ret = cvkDisplayCommandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }
        
        return ret;
    }       
    
    private void DestroyCommandBuffer() {         
        if (null != cvkDisplayCommandBuffer) {
            cvkDisplayCommandBuffer.Destroy();
            cvkDisplayCommandBuffer = null;
        } 
    }        
    
        
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout() {
        int ret;
        
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(4, stack);

            // 0: Vertex uniform buffer
            VkDescriptorSetLayoutBinding vertexUBDSLB = bindings.get(0);
            vertexUBDSLB.binding(0);
            vertexUBDSLB.descriptorCount(1);
            vertexUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            vertexUBDSLB.pImmutableSamplers(null);
            vertexUBDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            
            // 1: Vertex samplerBuffer (position buffer)
            VkDescriptorSetLayoutBinding vertexSamplerDSLB = bindings.get(1);
            vertexSamplerDSLB.binding(1);
            vertexSamplerDSLB.descriptorCount(1);
            vertexSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
            vertexSamplerDSLB.pImmutableSamplers(null);
            vertexSamplerDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);            
            
            // 2: Geometry uniform buffer
            VkDescriptorSetLayoutBinding geometryUBDSLB = bindings.get(2);
            geometryUBDSLB.binding(2);
            geometryUBDSLB.descriptorCount(1);
            geometryUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geometryUBDSLB.pImmutableSamplers(null);
            geometryUBDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);       
            
            // 3: Fragment uniform buffer
            VkDescriptorSetLayoutBinding fragmentUBDSLB = bindings.get(3);
            fragmentUBDSLB.binding(3);
            fragmentUBDSLB.descriptorCount(1);
            fragmentUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            fragmentUBDSLB.pImmutableSamplers(null);
            fragmentUBDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);            
                          
            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(CVKDevice.GetVkDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
                GetLogger().info("CVKBlazesRenderable created hDescriptorLayout: 0x%016X", hDescriptorLayout);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        GetLogger().info("CVKBlazesRenderable destroying hDescriptorLayout: 0x%016X", hDescriptorLayout);
        vkDestroyDescriptorSetLayout(CVKDevice.GetVkDevice(), hDescriptorLayout, null);
        hDescriptorLayout = VK_NULL_HANDLE;
    }
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        
        int ret;    

        // The same layout is used for each descriptor set. Each image has a 
        // an identical copy of the descriptor set so allow the GPU and CPU to
        // desynchronise (when there are 2 or more images in the swapchain).
        final int imageCount = cvkSwapChain.GetImageCount();
        LongBuffer layouts = stack.mallocLong(imageCount);
        for (int i = 0; i < imageCount; ++i) {
            layouts.put(i, hDescriptorLayout);
        }

        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool(cvkDescriptorPool.GetDescriptorPoolHandle());
        allocInfo.pSetLayouts(layouts);

        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
        ret = vkAllocateDescriptorSets(CVKDevice.GetVkDevice(), allocInfo, pDescriptorSets);
        if (VkFailed(ret)) { return ret; }
        
        for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
            GetLogger().info("CVKBlazesRenderable allocated hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
        }
        
        return UpdateDescriptorSets(stack);
    }
    
    // TODO: do we gain anything by having buffered UBOs?
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(pDescriptorSets);
        CVKAssert(pDescriptorSets.capacity() > 0);
        CVKAssertNotNull(cvkVertexUniformBuffer);
        CVKAssertNotNull(cvkGeometryUniformBuffer); 
        CVKAssertNotNull(cvkFragmentUniformBuffer); 
        CVKAssertNotNull(cvkVertexUniformBuffer.GetBufferHandle());
        CVKAssertNotNull(cvkGeometryUniformBuffer.GetBufferHandle()); 
        CVKAssertNotNull(cvkFragmentUniformBuffer.GetBufferHandle());         
        
        int ret = VK_SUCCESS;             
        
        final long positionBufferSize = cvkVisualProcessor.GetPositionBufferSize();
        hPositionBuffer = cvkVisualProcessor.GetPositionBufferHandle();
        hPositionBufferView = cvkVisualProcessor.GetPositionBufferViewHandle(); 
        CVKAssertNotNull(hPositionBuffer);
        CVKAssertNotNull(hPositionBufferView);        
        
        // - Descriptor info structs -
        // We create these to describe the different resources we want to address
        // in shaders.  We have one info struct per resource.  We then create a 
        // write descriptor set structure for each resource for each image.  For
        // buffered resources like the the uniform buffers we wait to set the 
        // buffer resource until the image loop below.
        
        // Struct for the uniform buffer used by Blaze.vs
        VkDescriptorBufferInfo.Buffer vertexUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        vertexUniformBufferInfo.buffer(cvkVertexUniformBuffer.GetBufferHandle());
        vertexUniformBufferInfo.offset(0);
        vertexUniformBufferInfo.range(VertexUniformBufferObject.SizeOf());        
        
        // Struct for texel buffer (positions) used by Blaze.vs
        VkDescriptorBufferInfo.Buffer positionsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        positionsTexelBufferInfo.buffer(hPositionBuffer);
        positionsTexelBufferInfo.offset(0);
        positionsTexelBufferInfo.range(positionBufferSize);               

        // Struct for the uniform buffer used by Blaze.gs
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        geometryUniformBufferInfo.buffer(cvkGeometryUniformBuffer.GetBufferHandle());
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(GeometryUniformBufferObject.SizeOf());  
        
        // Struct for the uniform buffer used by Blaze.fs
        VkDescriptorBufferInfo.Buffer fragmentUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        fragmentUniformBufferInfo.buffer(cvkFragmentUniformBuffer.GetBufferHandle());
        fragmentUniformBufferInfo.offset(0);
        fragmentUniformBufferInfo.range(FragmentUniformBufferObject.SizeOf());        

        // We need 4 write descriptors, 3 for uniform buffers and 1 for texel buffers           
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(4, stack);

        // Vertex uniform buffer
        VkWriteDescriptorSet vertexUBDescriptorWrite = descriptorWrites.get(0);
        vertexUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertexUBDescriptorWrite.dstBinding(0);
        vertexUBDescriptorWrite.dstArrayElement(0);
        vertexUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        vertexUBDescriptorWrite.descriptorCount(1);
        vertexUBDescriptorWrite.pBufferInfo(vertexUniformBufferInfo);      
        
        // Vertex texel buffer (positions)
        VkWriteDescriptorSet positionsTBDescriptorWrite = descriptorWrites.get(1);
        positionsTBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        positionsTBDescriptorWrite.dstBinding(1);
        positionsTBDescriptorWrite.dstArrayElement(0);
        positionsTBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
        positionsTBDescriptorWrite.descriptorCount(1);
        positionsTBDescriptorWrite.pBufferInfo(positionsTexelBufferInfo);               
        positionsTBDescriptorWrite.pTexelBufferView(stack.longs(hPositionBufferView));
        
        // Geometry uniform buffer
        VkWriteDescriptorSet geometryUBDescriptorWrite = descriptorWrites.get(2);
        geometryUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        geometryUBDescriptorWrite.dstBinding(2);
        geometryUBDescriptorWrite.dstArrayElement(0);
        geometryUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        geometryUBDescriptorWrite.descriptorCount(1);
        geometryUBDescriptorWrite.pBufferInfo(geometryUniformBufferInfo);     
        
        // Fragment uniform buffer
        VkWriteDescriptorSet fragmentUBDescriptorWrite = descriptorWrites.get(3);
        fragmentUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        fragmentUBDescriptorWrite.dstBinding(3);
        fragmentUBDescriptorWrite.dstArrayElement(0);
        fragmentUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        fragmentUBDescriptorWrite.descriptorCount(1);
        fragmentUBDescriptorWrite.pBufferInfo(fragmentUniformBufferInfo);           
        
        final int imageCount = cvkSwapChain.GetImageCount();
        for (int i = 0; i < imageCount; ++i) {                            
            // Set the descriptor set we're updating in each write struct
            long descriptorSet = pDescriptorSets.get(i);
            descriptorWrites.forEach(el -> {el.dstSet(descriptorSet);});

            // Update the descriptors with a write and no copy
            GetLogger().info("CVKBlazesRenderable updating descriptorSet: 0x%016X", descriptorSet);
            vkUpdateDescriptorSets(CVKDevice.GetVkDevice(), descriptorWrites, null);
        }           
        
        SetDescriptorSetsState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }
        
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
        if (pDescriptorSets != null) {
            CVKAssertNotNull(cvkDescriptorPool);
            CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());            
            GetLogger().fine("CVKBlazesRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity());
            
            for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
                GetLogger().info("CVKBlazesRenderable freeing hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
            }            
            
            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
            ret = vkFreeDescriptorSets(CVKDevice.GetVkDevice(), cvkDescriptorPool.GetDescriptorPoolHandle(), pDescriptorSets);
            pDescriptorSets = null;
            checkVKret(ret);
        }
        
        return ret;
    }    
    
    @Override
    public int DestroyDescriptorPoolResources() {
        int ret = VK_SUCCESS;
        
        if (cvkDescriptorPool != null) {
            return DestroyDescriptorSets();
        }
        
        return ret;        
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {
        // Blaze.vs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // Blaze.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        
        // Blaze.fs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        
        // One set per image
        ++perImageReqs.poolDesciptorSetCount;
    }      
    
    @Override
    public int SetNewDescriptorPool(CVKDescriptorPool newDescriptorPool) {
        int ret = super.SetNewDescriptorPool(newDescriptorPool);
        if (VkFailed(ret)) { return ret; }
        SetDescriptorSetsState(CVK_RESOURCE_NEEDS_REBUILD);
        return ret;
    }
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(hDescriptorLayout);
               
        int ret;       
        try (MemoryStack stack = stackPush()) {  
            VkPushConstantRange.Buffer pushConstantRange;
            pushConstantRange = VkPushConstantRange.calloc(2);
            pushConstantRange.get(0).stageFlags(MODEL_VIEW_PUSH_CONSTANT_STAGES);
            pushConstantRange.get(0).size(MODEL_VIEW_PUSH_CONSTANT_SIZE);
            pushConstantRange.get(0).offset(0);

            pushConstantRange.get(1).stageFlags(HIT_TEST_PUSH_CONSTANT_STAGES);
            pushConstantRange.get(1).size(HIT_TEST_PUSH_CONSTANT_SIZE);
            pushConstantRange.get(1).offset(MODEL_VIEW_PUSH_CONSTANT_SIZE);           

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
            pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(CVKDevice.GetVkDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssert(hPipelineLayout != VK_NULL_HANDLE);                
        }        
        return ret;        
    }    
    
    private void DestroyPipelineLayout() {
        if (hPipelineLayout != VK_NULL_HANDLE) {
            vkDestroyPipelineLayout(CVKDevice.GetVkDevice(), hPipelineLayout, null);
            hPipelineLayout = VK_NULL_HANDLE;
        }
    }      


    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() {   
        // Race condition: icons owns the position buffer and the update task (on
        // the visual processor thread) may not yet be complete.  If this happens
        // before the first update is finished icons will have a vertexCount of 
        // 0 so it won't be able to create the position buffer yet.  If we can't
        // get a handle to the current position buffer then we just skip updating
        // until we can.
        if (cvkVisualProcessor.GetPositionBufferHandle() == VK_NULL_HANDLE) {
            return false;
        }         
        
        if (hPositionBuffer != cvkVisualProcessor.GetPositionBufferHandle() ||
            hPositionBufferView != cvkVisualProcessor.GetPositionBufferViewHandle()) {
            if (descriptorSetsState != CVK_RESOURCE_NEEDS_REBUILD) {
                descriptorSetsState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        }        
        
        return vertexCount > 0 &&
               (vertexUBOState != CVK_RESOURCE_CLEAN ||
                geometryUBOState != CVK_RESOURCE_CLEAN ||            
                fragmentUBOState != CVK_RESOURCE_CLEAN || 
                vertexBuffersState != CVK_RESOURCE_CLEAN ||
                commandBuffersState != CVK_RESOURCE_CLEAN ||
                descriptorSetsState != CVK_RESOURCE_CLEAN ||
                pipelinesState != CVK_RESOURCE_CLEAN);                
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        cvkVisualProcessor.VerifyInRenderThread();
                         
        try (MemoryStack stack = stackPush()) {
            // Update vertex buffers
            if (vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
                DestroyVertexBuffer();
                ret = CreateVertexBuffer();
                if (VkFailed(ret)) { return ret; }         
            } else if (vertexBuffersState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexBuffer();
                if (VkFailed(ret)) { return ret; }           
            }                
        
            // Vertex uniform buffer
            if (vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateVertexUniformBuffers();
                if (VkFailed(ret)) { return ret; }
            } else if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexUniformBuffer();
                if (VkFailed(ret)) { return ret; }               
            }

            // Geometry uniform buffer
            if (geometryUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateGeometryUniformBuffer();
                if (VkFailed(ret)) { return ret; }
            } else if (geometryUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateGeometryUniformBuffer();
                if (VkFailed(ret)) { return ret; }               
            }    
            
            // Fragment uniform buffer
            if (fragmentUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateFragmentUniformBuffer();
                if (VkFailed(ret)) { return ret; }
            } else if (fragmentUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateFragmentUniformBuffer();
                if (VkFailed(ret)) { return ret; }               
            }               

            // Descriptors (binding values to shaders parameters)
            if (descriptorSetsState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; }  
            } else if (descriptorSetsState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; }               
            }  
        
            // Command buffers (rendering commands enqueued on the GPU)
            if (commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateCommandBuffers();
                if (VkFailed(ret)) { return ret; }  
            }           
        
            // Pipelines (all the render state and resources in one object)
            if (pipelinesState == CVK_RESOURCE_NEEDS_REBUILD) {
                displayPipelines = new ArrayList<>(cvkSwapChain.GetImageCount());
                ret = CreatePipelines(cvkSwapChain.GetRenderPassHandle(), displayPipelines);
                if (VkFailed(ret)) { return ret; }            
            }                                            
        }                                     
        
        return ret;
    }        
    
    
    // ========================> Tasks <======================== \\    
    // NOTE:  we effectively have two levels of staging.  The second level is pretty
    // straightforward, we need the resources we render with to be in the most
    // optimal memory possible: resident GPU memory.  This memory cannot be written
    // by the CPU so we can't update it directly, instead we update staging buffers
    // that are both GPU and CPU writable then copy from that into the final GPU
    // buffers.
    // The first level of staging is required as our staging buffers are VkDevice
    // resources and the device may not be initialised when these tasks are called.
    // This is the case when a graph is loaded into a new tab, we need to be able
    // to process the tasks that load the graph vertices etc before the device is
    // ready to create staging buffers.  For this reason we update local arrays
    // in the Build<blah>Array functions, then copy these to our staging buffers
    // during the renderer's display loop (when the rendering lambda of each task
    // is executed).  This also means we don't need to synchronise these arrays
    // created by the visual processor's thread with the staging buffers that have
    // a lifespan entirely within the rendering thread (AWT event thread).  This
    // is possible because the arrays are locals in the task functions and aren't
    // modified by the visual processor thread after they've been added to the 
    // queue of tasks for the renderer to process.
    
     private Vertex[] BuildVertexArray(final VisualAccess access, int first, int last) {
        final int newVertexCount = (last - first) + 1;
        if (newVertexCount > 0) {
            List<Vertex> vertices = new ArrayList<>();
            for (int pos = first; pos <= last; ++pos) {  
                if (access.getBlazed(pos)) {
                    vertices.add(new Vertex(access.getBlazeColor(pos),
                                            access.getVertexVisibility(pos),
                                            pos,
                                            access.getBlazeAngle(pos)));
                }
            }            
            
            Vertex[] verticesCopy = new Vertex[vertices.size()];
            return vertices.toArray(verticesCopy);
        } else {
            return null;
        } 
    }       
    
    private void RebuildVertexStagingBuffer(Vertex[] vertices) {      
        vertexCount = (vertices != null ? vertices.length : 0);
        final int newSizeBytes = vertexCount * Vertex.BYTES;
        final boolean recreate = cvkVertexStagingBuffer == null || newSizeBytes != cvkVertexStagingBuffer.GetBufferSize();
        
        if (recreate) {
            if (cvkVertexStagingBuffer != null) {
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            }
            
            if (newSizeBytes > 0) {
                cvkVertexStagingBuffer = CVKBuffer.Create(newSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                          GetLogger(),
                                                          "CVKLoopsRenderable.RebuildVertexStagingBuffer cvkVertexStagingBuffer");
            }
        }       

        if (newSizeBytes > 0) {   
            UpdateVertexStagingBuffer(vertices, 0, vertices.length - 1);                            
        }  
    }
    
    private void UpdateVertexStagingBuffer(Vertex[] vertices, int first, int last) {
        CVKAssertNotNull(cvkVertexStagingBuffer);
        CVKAssertNotNull(vertices != null);
        CVKAssert(vertices.length > 0 && vertices.length > last);
        CVKAssert(last >= 0 && last >= first && first >= 0);

        int offset = first * Vertex.BYTES;
        int size = ((last - first) + 1) * Vertex.BYTES;

        ByteBuffer pMemory = cvkVertexStagingBuffer.StartMemoryMap(offset, size);
        for (Vertex vertex : vertices) {
            vertex.CopyToSequentially(pMemory);
        }
        cvkVertexStagingBuffer.EndMemoryMap();
        pMemory = null; // now unmapped, do not use           
    }      
    
    public CVKRenderUpdateTask TaskUpdateBlazes(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//                                  
        final int changedVerticeRange[];
        final Vertex vertices[];
        if (cvkVertexStagingBuffer == null || change.isEmpty()) {            
            vertices = BuildVertexArray(access, 0, access.getVertexCount() - 1);
            changedVerticeRange = null;
        } else {
            changedVerticeRange = change.getRange();            
            vertices = BuildVertexArray(access, changedVerticeRange[0], changedVerticeRange[1]);         
        }
        
        GetLogger().fine("TaskUpdateBlazes frame %d: (%d) blazed verts", cvkVisualProcessor.GetFrameNumber(), vertices != null ? vertices.length : 0);
        
        // We have to enumerate the vertices to see which are blazed before we can compare the old and new buffer sizes
        final boolean rebuildRequired = cvkVertexStagingBuffer == null ||
                                        change.isEmpty() || 
                                        vertices.length * Vertex.BYTES != cvkVertexStagingBuffer.GetBufferSize();   
   
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {                
                RebuildVertexStagingBuffer(vertices);
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
                vertexCount = vertices != null ? vertices.length : 0;
            } else if (vertexBuffersState != CVK_RESOURCE_NEEDS_REBUILD) {
                UpdateVertexStagingBuffer(vertices, changedVerticeRange[0], changedVerticeRange[1]);
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        }; 
    }           
    
    public CVKRenderUpdateTask TaskUpdateSizeAndOpacity(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        final float updatedBlazeSize = access.getBlazeSize();
        final float updatedBlazeOpacity = access.getBlazeOpacity();
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            geometryUBO.scale = updatedBlazeSize;
            fragmentUBO.opacity = updatedBlazeOpacity;
            
            if (geometryUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetGeometryUBOState(CVK_RESOURCE_NEEDS_UPDATE);
            }  

            if (fragmentUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetFragmentUBOState(CVK_RESOURCE_NEEDS_UPDATE);
            }              
        }; 
    }    
    
    public CVKRenderUpdateTask TaskUpdateCamera() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
                                                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {  
            UpdateVertexPushConstants();
        };           
    }         
}
