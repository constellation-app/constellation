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
import au.gov.asd.tac.constellation.utilities.text.LabelUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
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
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKIconTextureAtlas;
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
import java.util.SortedMap;
import java.util.TreeMap;
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
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
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
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkWriteDescriptorSet;


/*******************************************************************************
 * CVKLoopsRenderable
 * 
 * 
 *******************************************************************************/

public class CVKLoopsRenderable extends CVKRenderable {
    // Resources recreated with the swap chain (dependent on the image count)    
    private LongBuffer pDescriptorSets = null; 
    private List<Long> hitTestPipelines = null;
    private List<CVKCommandBuffer> displayCommandBuffers = null;
    private List<CVKCommandBuffer> hittestCommandBuffers = null;    
    private List<CVKBuffer> vertexBuffers = null;   
    private List<CVKBuffer> vertexUniformBuffers = null;
    private List<CVKBuffer> geometryUniformBuffers = null;      
    
    // The UBO staging buffers are a known size so created outside user events
    private CVKBuffer cvkVertexUBStagingBuffer = null;
    private CVKBuffer cvkGeometryUBStagingBuffer = null;
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private final GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject(); 
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;  

    // Resources we don't own but use and must track so we know when to update
    // our descriptors
    private long hPositionBuffer = VK_NULL_HANDLE;
    private long hPositionBufferView = VK_NULL_HANDLE;    
    private long hIconAtlasSampler = VK_NULL_HANDLE;
    private long hIconAtlasImageView = VK_NULL_HANDLE;    
    
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
        private Vector4f colour = new Vector4f();
        private Vector4i data = new Vector4i();
        
        private Vertex() {}
                    
       
        public Vertex(ConstellationColor inColour, 
                      float visibility,
                      int connectionID,
                      int vertexID,
                      int flags,
                      int iconID) {
            colour.a[0] = inColour.getRed();
            colour.a[1] = inColour.getGreen();
            colour.a[2] = inColour.getBlue();
            colour.a[3] = visibility;
            data.a[0]   = connectionID;
            data.a[1]   = vertexID;
            data.a[2]   = flags;
            data.a[3]   = iconID;
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
                             1 * Float.BYTES +     // visibilityLow
                             1 * Float.BYTES;      // visibilityHigh  

                final int overrun = sizeof % minAlignment;
                padding = overrun > 0 ? minAlignment - overrun : 0;             
            }
            
            return Matrix44f.BYTES  +     // pMatrix
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
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);         
                        
            for (int i = 0; i < padding; ++i) {
                buffer.put((byte)0);
            }          
        }         
    }   
                      
    
    // ========================> Shaders <======================== \\
    
    @Override
    protected String GetVertexShaderName() { return "Loop.vs"; }
    
    @Override
    protected String GetGeometryShaderName() { return "Loop.gs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "Loop.fs"; }   
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKLoopsRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
        
        cullMode = VK_CULL_MODE_NONE;
    }              
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                    GetLogger(),
                                                    "CVKLoopsRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer");   
        cvkGeometryUBStagingBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      GetLogger(),
                                                      "CVKLoopsRenderable.CreateUBOStagingBuffers cvkGeometryUBStagingBuffer"); 
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
    }
    
    @Override
    public void Destroy() {
        DestroyVertexBuffers();
        DestroyVertexUniformBuffers();
        DestroyGeometryUniformBuffers();
        DestroyDescriptorSets();
        DestroyDescriptorLayout();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyCommandBuffers();
        DestroyStagingBuffers();
        DestroyPushConstants();
        
        CVKAssertNull(vertexBuffers);
        CVKAssertNull(vertexUniformBuffers);
        CVKAssertNull(geometryUniformBuffers);
        CVKAssertNull(pDescriptorSets);
        CVKAssertNull(hDescriptorLayout);  
        CVKAssertNull(displayCommandBuffers);        
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
            DestroyVertexBuffers();
            DestroyVertexUniformBuffers();
            DestroyGeometryUniformBuffers();
            DestroyDescriptorSets();
            DestroyCommandBuffers();     
            DestroyPipelines();
            DestroyCommandBuffers();                                  
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
    
    private int CreateVertexBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
    
        // We can only create vertex buffers if we have something to put in them
        if (cvkVertexStagingBuffer.GetBufferSize() > 0) {
            int imageCount = cvkSwapChain.GetImageCount();               
            vertexBuffers = new ArrayList<>();
            
            for (int i = 0; i < imageCount; ++i) {   
                CVKBuffer cvkVertexBuffer = CVKBuffer.Create(cvkVertexStagingBuffer.GetBufferSize(),
                                                             VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                             GetLogger(),
                                                             String.format("CVKLoopsRenderable cvkVertexBuffer %d", i));
                vertexBuffers.add(cvkVertexBuffer);        
            }

            // Populate them with some values
            return UpdateVertexBuffers();
        }
        
        return ret;  
    }    
    
    private int UpdateVertexBuffers() {
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssert(cvkVertexStagingBuffer != null);
        CVKAssert(vertexBuffers != null);
        CVKAssert(vertexBuffers.size() > 0);
        CVKAssert(cvkVertexStagingBuffer.GetBufferSize() == vertexBuffers.get(0).GetBufferSize());
        int ret = VK_SUCCESS;     
            
        for (int i = 0; i < vertexBuffers.size(); ++i) {   
            CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
            ret = cvkVertexBuffer.CopyFrom(cvkVertexStagingBuffer);
            if (VkFailed(ret)) { return ret; }
        }   
        
        // Note the staging buffer is not freed as we can simplify the update tasks
        // by just updating it and then copying it over again during ProcessRenderTasks().
        SetVertexBuffersState(CVK_RESOURCE_CLEAN);

        return ret;         
    }  
    
    @Override
    public int GetVertexCount() { return cvkVisualProcessor.GetDrawFlags().drawConnections() ? vertexCount : 0; }     
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }               
    
    
    // ========================> Uniform buffers <======================== \\
    
    private int CreateVertexUniformBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(vertexUniformBuffers == null);
 
        vertexUniformBuffers = new ArrayList<>();
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer vertexUniformBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                             GetLogger(),
                                                             String.format("CVKLoopsRenderable vertexUniformBuffer %d", i));   
            vertexUniformBuffers.add(vertexUniformBuffer);                     
        }        
        return UpdateVertexUniformBuffers();
    }
        
    private int UpdateVertexUniformBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkVertexUBStagingBuffer);
        CVKAssertNotNull(vertexUniformBuffers);
        CVKAssert(vertexUniformBuffers.size() > 0);
        
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
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = vertexUniformBuffers.get(i).CopyFrom(cvkVertexUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }
        
        UpdateVertexPushConstants();
        
        // We are done, reset the resource state
        SetVertexUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyVertexUniformBuffers() {
        if (vertexUniformBuffers != null) {
            vertexUniformBuffers.forEach(el -> {el.Destroy();});
            vertexUniformBuffers = null;
        }    
    }      
    
    private int CreateGeometryUniformBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(geometryUniformBuffers == null);

        geometryUniformBuffers = new ArrayList<>(); 
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer geometryUniformBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                               GetLogger(),
                                                               String.format("CVKLoopsRenderable geometryUniformBuffer %d", i));
            geometryUniformBuffers.add(geometryUniformBuffer);              
        }
        return UpdateGeometryUniformBuffers();
    }
    
    private int UpdateGeometryUniformBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkGeometryUBStagingBuffer);
        CVKAssertNotNull(geometryUniformBuffers);
        CVKAssert(geometryUniformBuffers.size() > 0);        
        
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
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = geometryUniformBuffers.get(i).CopyFrom(cvkGeometryUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }       
                    
        // We are done, reset the resource state
        SetGeometryUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyGeometryUniformBuffers() {
        if (geometryUniformBuffers != null) {
            geometryUniformBuffers.forEach(el -> {el.Destroy();});
            geometryUniformBuffers = null;
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
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        displayCommandBuffers = new ArrayList<>(imageCount);
        hittestCommandBuffers = new ArrayList<>(imageCount);
        
        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKLoopsRenderable %d", i));
            displayCommandBuffers.add(buffer);
            
            CVKCommandBuffer offscreenBuffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKLoopsRenderable Offscreen Buffer %d", i));
            hittestCommandBuffers.add(offscreenBuffer);
        }
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }   
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {
        return displayCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }       
    
    @Override
    public VkCommandBuffer GetHitTestCommandBuffer(int imageIndex) {
        return hittestCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(CVKDevice.GetCommandPoolHandle());
        CVKAssertNotNull(cvkSwapChain);
                
        int ret;     
         
        CVKCommandBuffer commandBuffer = displayCommandBuffers.get(imageIndex);
        CVKAssert(commandBuffer != null);
        CVKAssert(displayPipelines.get(imageIndex) != null);

        commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, inheritanceInfo);

        commandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        commandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());

        commandBuffer.BindGraphicsPipeline(displayPipelines.get(imageIndex));
        commandBuffer.BindVertexInput(vertexBuffers.get(imageIndex).GetBufferHandle());

        // Push MV matrix to the vertex shader
        commandBuffer.PushConstants(hPipelineLayout, MODEL_VIEW_PUSH_CONSTANT_STAGES, 0, modelViewPushConstants);

        // Push drawHitTest flag to the geometry shader
        commandBuffer.PushConstants(hPipelineLayout, HIT_TEST_PUSH_CONSTANT_STAGES, Matrix44f.BYTES, hitTestPushConstants);

        commandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }
        
        return ret;
    }
    
    @Override
    public int RecordHitTestCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssert(CVKDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);
        CVKAssertNotNull(cvkSwapChain);

        int ret;     
        
        // Set the hit test flag in the shaders to true
        UpdatePushConstantsHitTest(true);            

        CVKCommandBuffer commandBuffer = hittestCommandBuffers.get(imageIndex);          
        CVKAssertNotNull(commandBuffer);
        CVKAssertNotNull(hitTestPipelines.get(imageIndex));

        ret = commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
                                                       inheritanceInfo);
        if (VkFailed(ret)) { return ret; }

        commandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        commandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());

        commandBuffer.BindGraphicsPipeline(hitTestPipelines.get(imageIndex));
        commandBuffer.BindVertexInput(vertexBuffers.get(imageIndex).GetBufferHandle());

        // Push MV matrix to the vertex shader
        commandBuffer.PushConstants(hPipelineLayout, 
                                    MODEL_VIEW_PUSH_CONSTANT_STAGES, 
                                    0, 
                                    modelViewPushConstants);

        // Push drawHitTest flag to the geometry shader
        commandBuffer.PushConstants(hPipelineLayout, 
                                    HIT_TEST_PUSH_CONSTANT_STAGES,
                                    MODEL_VIEW_PUSH_CONSTANT_SIZE,
                                    hitTestPushConstants);

        commandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }

        // Reset hit test flag to false
        UpdatePushConstantsHitTest(false);
        
        return ret;
    }
    
    private void DestroyCommandBuffers() {         
        if (null != displayCommandBuffers) {
            displayCommandBuffers.forEach(el -> {el.Destroy();});
            displayCommandBuffers.clear();
            displayCommandBuffers = null;
        }
        
        if (null != hittestCommandBuffers) {
            hittestCommandBuffers.forEach(el -> {el.Destroy();});
            hittestCommandBuffers.clear();
            hittestCommandBuffers = null;
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
            
            // 3: Fragment sampler2Darray (atlas)
            VkDescriptorSetLayoutBinding fragmentSamplerDSLB = bindings.get(3);
            fragmentSamplerDSLB.binding(3);
            fragmentSamplerDSLB.descriptorCount(1);
            fragmentSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            fragmentSamplerDSLB.pImmutableSamplers(null);
            fragmentSamplerDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);            
                          

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(CVKDevice.GetVkDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
                GetLogger().info("CVKLoopsRenderable created hDescriptorLayout: 0x%016X", hDescriptorLayout);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        GetLogger().info("CVKLoopsRenderable destroying hDescriptorLayout: 0x%016X", hDescriptorLayout);
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
            GetLogger().info("CVKLoopsRenderable allocated hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
        }
        
        return UpdateDescriptorSets(stack);
    }
    
    // TODO_TT: do we gain anything by having buffered UBOs?
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(pDescriptorSets);
        CVKAssert(pDescriptorSets.capacity() > 0);
        CVKAssertNotNull(vertexUniformBuffers);
        CVKAssert(vertexUniformBuffers.size() > 0);
        CVKAssertNotNull(geometryUniformBuffers);
        CVKAssert(geometryUniformBuffers.size() > 0);   
        
        int ret = VK_SUCCESS;
     
        final int imageCount = cvkSwapChain.GetImageCount();
        
        final long positionBufferSize = cvkVisualProcessor.GetPositionBufferSize();
        hPositionBuffer = cvkVisualProcessor.GetPositionBufferHandle();
        hPositionBufferView = cvkVisualProcessor.GetPositionBufferViewHandle(); 
        hIconAtlasSampler = CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle();
        hIconAtlasImageView = CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle();
        CVKAssertNotNull(hPositionBuffer);
        CVKAssertNotNull(hPositionBufferView);        
        CVKAssertNotNull(hIconAtlasSampler);
        CVKAssertNotNull(hIconAtlasImageView);
        
        // - Descriptor info structs -
        // We create these to describe the different resources we want to address
        // in shaders.  We have one info struct per resource.  We then create a 
        // write descriptor set structure for each resource for each image.  For
        // buffered resources like the the uniform buffers we wait to set the 
        // buffer resource until the image loop below.
        
        // Struct for the uniform buffer used by Loop.vs
        VkDescriptorBufferInfo.Buffer vertexUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // vertexUniformBufferInfo.buffer is set per imageIndex
        vertexUniformBufferInfo.offset(0);
        vertexUniformBufferInfo.range(VertexUniformBufferObject.SizeOf());        
        
        // Struct for texel buffer (positions) used by Loop.vs
        VkDescriptorBufferInfo.Buffer positionsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        positionsTexelBufferInfo.buffer(hPositionBuffer);
        positionsTexelBufferInfo.offset(0);
        positionsTexelBufferInfo.range(positionBufferSize);               

        // Struct for the uniform buffer used by Loop.gs
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // geometryBufferInfo.buffer is set per imageIndex
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(GeometryUniformBufferObject.SizeOf());  
        
        // Struct for the size of the image sampler (atlas) used by Loop.fs
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(hIconAtlasImageView);
        imageInfo.sampler(hIconAtlasSampler);        

        // We need 4 write descriptors, 2 for uniform buffers, 1 for texel buffers and 1 for the texture sampler               
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
        
        // Fragment image (atlas) sampler
        VkWriteDescriptorSet atlasSamplerDescriptorWrite = descriptorWrites.get(3);
        atlasSamplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        atlasSamplerDescriptorWrite.dstBinding(3);
        atlasSamplerDescriptorWrite.dstArrayElement(0);
        atlasSamplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        atlasSamplerDescriptorWrite.descriptorCount(1);
        atlasSamplerDescriptorWrite.pImageInfo(imageInfo);         
        
        for (int i = 0; i < imageCount; ++i) {                        
            // Update the buffered resource buffers
            vertexUniformBufferInfo.buffer(vertexUniformBuffers.get(i).GetBufferHandle());
            geometryUniformBufferInfo.buffer(geometryUniformBuffers.get(i).GetBufferHandle());
                    
            // Set the descriptor set we're updating in each write struct
            long descriptorSet = pDescriptorSets.get(i);
            descriptorWrites.forEach(el -> {el.dstSet(descriptorSet);});

            // Update the descriptors with a write and no copy
            GetLogger().info("CVKLoopsRenderable updating descriptorSet: 0x%016X", descriptorSet);
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
            GetLogger().fine("CVKLoopsRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity());
            
            for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
                GetLogger().info("CVKLoopsRenderable freeing hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
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
        // Loop.vs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // Loop.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        
        // Loop.fs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
        
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
        if (hPositionBuffer != cvkVisualProcessor.GetPositionBufferHandle() ||
            hPositionBufferView != cvkVisualProcessor.GetPositionBufferViewHandle() ||
            hIconAtlasSampler != CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle() ||
            hIconAtlasImageView != CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle()) {
            if (descriptorSetsState != CVK_RESOURCE_NEEDS_REBUILD) {
                descriptorSetsState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        }        
        
        return vertexCount > 0 &&
               (vertexUBOState != CVK_RESOURCE_CLEAN ||
                geometryUBOState != CVK_RESOURCE_CLEAN ||            
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
                DestroyVertexBuffers();
                ret = CreateVertexBuffers();
                if (VkFailed(ret)) { return ret; }         
            } else if (vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = UpdateVertexBuffers();
                if (VkFailed(ret)) { return ret; }           
            }                
        
            // Vertex uniform buffer
            if (vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateVertexUniformBuffers();
                if (VkFailed(ret)) { return ret; }
            } else if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexUniformBuffers();
                if (VkFailed(ret)) { return ret; }               
            }

            // Geometry uniform buffer
            if (geometryUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateGeometryUniformBuffers();
                if (VkFailed(ret)) { return ret; }
            } else if (geometryUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateGeometryUniformBuffers();
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
 
                hitTestPipelines = new ArrayList<>(cvkSwapChain.GetImageCount());
                ret = CreatePipelines(cvkSwapChain.GetOffscreenRenderPassHandle(), hitTestPipelines);
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

    
    /**
     *
     * @param access: the view of this graph
     * @param loopedConnections: list of node vertices that have looped connections
     * @return: an array of vertex objects ready to copy into the VB
     */     
    private Vertex[] BuildVertexArray(final VisualAccess access, List<Integer> loopedConnections) {
        Vertex[] vertices = null;
        if (!loopedConnections.isEmpty()) {
            vertices = new Vertex[loopedConnections.size()];
            for (int i = 0; i < loopedConnections.size(); ++i) {
                int pos = loopedConnections.get(i);
                final ConstellationColor colour = access.getConnectionColor(pos);
                final float visibility = access.getConnectionVisibility(pos);
                final int connectionID = access.getConnectionId(pos);
                final int vertexID = access.getConnectionLowVertex(pos);
                final int flags = (access.getConnectionDimmed(pos) ? 2 : 0) | (access.getConnectionSelected(pos) ? 1 : 0);                
                final int iconID = access.getConnectionDirected(pos) ? CVKIconTextureAtlas.LOOP_DIRECTED_ICON_INDEX : CVKIconTextureAtlas.LOOP_UNDIRECTED_ICON_INDEX;                                       
                vertices[i] = new Vertex(colour,
                                         visibility,
                                         connectionID,
                                         vertexID,
                                         flags,
                                         iconID);
            }
        }

        return vertices;
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
    
    public CVKRenderUpdateTask TaskUpdateLoops(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        
        // Loops aren't separate data, they're just connections where both vertices are the same
        final int changedVerticeRange[] = change.getRange();
        List<Integer> loopedConnections = new ArrayList<>();
        for (int i = 0; i < access.getConnectionCount(); i++) {
            if (access.getConnectionLowVertex(i) == access.getConnectionHighVertex(i)) {
                if (changedVerticeRange == null || (i >= changedVerticeRange[0] && i <= changedVerticeRange[1])) {
                    loopedConnections.add(i);
                }
            }
        }
        
        GetLogger().fine("TaskUpdateLoops frame %d: %d loops", cvkVisualProcessor.GetFrameNumber(), loopedConnections.size());                      
        final boolean rebuildRequired = cvkVertexStagingBuffer == null || 
                                        loopedConnections.size() * Vertex.BYTES != cvkVertexStagingBuffer.GetBufferSize() || 
                                        change.isEmpty();
        
        final Vertex vertexArray[];
        vertexArray = BuildVertexArray(access, loopedConnections);
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {                
                RebuildVertexStagingBuffer(vertexArray);
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
                vertexCount = vertexArray != null ? vertexArray.length : 0;
            } else if (vertexBuffersState != CVK_RESOURCE_NEEDS_REBUILD) {
                UpdateVertexStagingBuffer(vertexArray, changedVerticeRange[0], changedVerticeRange[1]);
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_UPDATE);
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
