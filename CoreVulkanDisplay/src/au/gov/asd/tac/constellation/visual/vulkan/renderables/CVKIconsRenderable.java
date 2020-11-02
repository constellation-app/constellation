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

import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.*;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKIconTextureAtlas;
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
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
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


/**
 * What resources do icons have
 *  - vs: vertex buffer (icon indexes and bkg colour)
 *  - vs: xyzw buffer (icon positions)
 *  - vs: vertex ubo (camera vars)
 *  - gs: flag buffer (vertex flags)
 *  - gs: geometry ubo (proj mtx and render data)
 *  - fs: atlas texture sampler (
 *  - fs: fragment ubo (hit test flag)
 * 
 */
public class CVKIconsRenderable extends CVKRenderable {
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    public static final int SELECTED_BIT = 1;
    public static final int DIMMED_BIT = 2;

    // Resource states. The atlas sampler handle is cached so we know the atlas 
    // state, ie if it doesn't match the one returned by the atlas, we know we
    // need to recreate our descriptors to point to the new one.
    private CVKRenderableResourceState positionBufferState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexFlagsBufferState = CVK_RESOURCE_CLEAN;   
    private long hIconAtlasSampler = VK_NULL_HANDLE;
    private long hIconAtlasImageView = VK_NULL_HANDLE;    
        
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
    private final Matrix44f mtxHighlightColour = Matrix44f.identity();
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;
    private CVKBuffer cvkPositionStagingBuffer = null;
    private CVKBuffer cvkPositionBuffer = null;    
    private CVKBuffer cvkVertexFlagsStagingBuffer = null;
    private CVKBuffer cvkVertexFlagsBuffer = null;    
    private long hPositionBufferView = VK_NULL_HANDLE;
    private long hVertexFlagsBufferView = VK_NULL_HANDLE;
    
    // Push constants for shaders contains the MV matrix and drawHitTest int
    private ByteBuffer vertexPushConstants = null;
    private ByteBuffer hitTestPushConstants = null;
    
    
    // ========================> Debuggering <======================== \\
    
    private void SetPositionBufferState(final CVKRenderableResourceState state) {
        CVKAssert(!(positionBufferState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t positionBufferState %s -> %s\tSource: %s", 
                    cvkVisualProcessor.GetFrameNumber(), positionBufferState.name(), state.name(), GetParentMethodName());
        }
        positionBufferState = state;
    }
    
    private void SetVertexFlagsBufferState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        if (LOGSTATECHANGE) {
            GetLogger().info("%d\t vertexFlagsBufferState %s -> %s\tSource: %s", 
               cvkVisualProcessor.GetFrameNumber(), vertexFlagsBufferState.name(), state.name(), GetParentMethodName());    
        }
        vertexFlagsBufferState = state;
    }
    
    
    // ========================> Classes <======================== \\
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int BYTES = Vector4f.BYTES + Vector4i.BYTES;
        private static final int OFFSETOF_DATA = Vector4f.BYTES;
        private static final int OFFSET_BKGCLR = 0;
        private static final int BINDING = 0;
        private Vector4f backgroundIconColour = new Vector4f();
        private Vector4i data = new Vector4i();
        
        private Vertex() {}

        public Vertex(Vector4i inData, Vector4f inColour) {
            data = inData;
            backgroundIconColour = inColour;
        }
        
        public void SetBackgroundIconColour(ConstellationColor colour) {
            backgroundIconColour.a[0] = colour.getRed();
            backgroundIconColour.a[1] = colour.getGreen();
            backgroundIconColour.a[2] = colour.getBlue();
        }
        
        public void SetVertexVisibility(float visibility) {
            backgroundIconColour.a[3] = visibility;
        }
        
        public void SetIconData(int mainIconIndices, int decoratorWestIconIndices, int decoratorEastIconIndices, int vertexIndex) {
            data.set(mainIconIndices, decoratorWestIconIndices, decoratorEastIconIndices, vertexIndex);
        }   
        
        public void CopyToSequentially(ByteBuffer buffer) {
            buffer.putFloat(backgroundIconColour.a[0]);
            buffer.putFloat(backgroundIconColour.a[1]);
            buffer.putFloat(backgroundIconColour.a[2]);
            buffer.putFloat(backgroundIconColour.a[3]);
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
        private static VkVertexInputBindingDescription.Buffer GetBindingDescription() {

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
        private static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // backgroundIconColor
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            posDescription.offset(OFFSET_BKGCLR);

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
    
    private static class Position {
        private static final int BYTES = 2 * Vector4f.BYTES;
        public final Vector4f position1;
        public final Vector4f position2;
        
        public Position(float x1, float y1, float z1, float radius1,
                        float x2, float y2, float z2, float radius2) {
            position1 = new Vector4f(x1, y1, z1, radius1);
            position2 = new Vector4f(x2, y2, z2, radius2);
        }
        
        public void CopyToSequentially(ByteBuffer buffer) {
            buffer.putFloat(position1.getX());
            buffer.putFloat(position1.getY());
            buffer.putFloat(position1.getZ());
            buffer.putFloat(position1.getW());
            buffer.putFloat(position2.getX());
            buffer.putFloat(position2.getY());
            buffer.putFloat(position2.getZ());
            buffer.putFloat(position2.getW());             
        }            
    }
    
    private static class VertexUniformBufferObject {
        public float morphMix = 0;
        public float visibilityLow = 0;
        public float visibilityHigh = 0;    
        private static Integer padding = null;
        
        private static int SizeOf() {
            if (padding == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (16 * Float.BYTES));

                int sizeof = 1 * Float.BYTES + // morphMix
                             1 * Float.BYTES + // visibilityLow
                             1 * Float.BYTES;  // visibilityHigh 

                final int overrun = sizeof % minAlignment;
                padding = Integer.valueOf(overrun > 0 ? minAlignment - overrun : 0);             
            }
            
            return 1 * Float.BYTES + // morphMix
                   1 * Float.BYTES + // visibilityLow
                   1 * Float.BYTES + // visibilityHigh
                   padding;
        }        
        
        private void CopyTo(ByteBuffer buffer) {
            buffer.putFloat(morphMix);
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);
            
            for (int i = 0; i < padding; ++i) {
                buffer.put((byte)0);
            }            
        }         
    }      
    
    private static class GeometryUniformBufferObject {                                           
        private int iconsPerRowColumn;
        private int iconsPerLayer;
        private int atlas2DDimension;
        private float pixelDensity = 0;
        private final Matrix44f highlightColor = Matrix44f.identity();
        private final Matrix44f pMatrix = new Matrix44f();    
        private static Integer padding = null;        
        
        private static int SizeOf() {
            if (padding == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (16 * Float.BYTES));

                int sizeof = 1 * Integer.BYTES + // iconsPerRowColumn
                             1 * Integer.BYTES + // iconsPerLayer
                             1 * Integer.BYTES + // atlas2DDimension
                             1 * Float.BYTES;    // pixelDensity    

                final int overrun = sizeof % minAlignment;
                padding = Integer.valueOf(overrun > 0 ? minAlignment - overrun : 0);             
            }
            
            return       1 * Integer.BYTES + // iconsPerRowColumn
                         1 * Integer.BYTES + // iconsPerLayer
                         1 * Integer.BYTES + // atlas2DDimension
                         1 * Float.BYTES   + // pixelDensity    
                         padding +
                         16 * Float.BYTES + // highlightColor
                         16 * Float.BYTES;  // pMatrix
        }
        
        private void CopyTo(ByteBuffer buffer) {                        
            buffer.putInt(iconsPerRowColumn);
            buffer.putInt(iconsPerLayer);
            buffer.putInt(atlas2DDimension);
            buffer.putFloat(pixelDensity);
            
            for (int i = 0; i < padding; ++i) {
                buffer.put((byte)0);
            }
                        
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(highlightColor.get(iRow, iCol));
                }
            }   
            
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            }            
        }         
    }   
                      
    
    // ========================> Shaders <======================== \\
    
    @Override
    protected String GetVertexShaderName() { return "VertexIcon.vs"; }
    
    @Override
    protected String GetGeometryShaderName() { return "VertexIcon.gs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "VertexIcon.fs"; }   
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKIconsRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
    }              
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                    GetLogger(),
                                                    "CVKIconsRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer");   
        cvkGeometryUBStagingBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      GetLogger(),
                                                      "CVKIconsRenderable.CreateUBOStagingBuffers cvkGeometryUBStagingBuffer"); 
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
        if (cvkPositionStagingBuffer != null) {
            cvkPositionStagingBuffer.Destroy();
            cvkPositionStagingBuffer = null;
        }
        if (cvkVertexFlagsStagingBuffer != null) {
            cvkVertexFlagsStagingBuffer.Destroy();
            cvkVertexFlagsStagingBuffer = null;
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
        DestroyPositionBuffer();
        DestroyVertexFlagsBuffer();
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
        CVKAssertNull(cvkPositionBuffer);
        CVKAssertNull(hPositionBufferView); 
        CVKAssertNull(cvkVertexFlagsBuffer);
        CVKAssertNull(hVertexFlagsBufferView); 
        CVKAssertNull(vertexUniformBuffers);
        CVKAssertNull(geometryUniformBuffers);
        CVKAssertNull(pDescriptorSets);
        CVKAssertNull(hDescriptorLayout);  
        CVKAssertNull(displayCommandBuffers);        
        CVKAssertNull(displayPipelines);
        CVKAssertNull(hPipelineLayout);    
        CVKAssertNull(vertexPushConstants);
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
                                                             String.format("CVKIconsRenderable cvkVertexBuffer %d", i));
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

//            List<DEBUG_CVKBufferElementDescriptor> DEBUG_vertexDescriptors = new ArrayList<>();
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("r", Float.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("g", Float.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("b", Float.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("a", Float.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("mainIconIndices", Integer.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("decoratorWestIconIndices", Integer.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("decoratorEastIconIndices", Integer.TYPE));
//            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("vertexIndex", Integer.TYPE)); 
//            cvkVertexStagingBuffer.DEBUGPRINT(DEBUG_vertexDescriptors);            
            
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
    public int GetVertexCount() { return vertexCount; }      
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }    
    
    
    // ========================> Texel buffers <======================== \\
    
    private int CreatePositionBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(cvkPositionBuffer == null);
        cvkVisualProcessor.VerifyInRenderThread();        
        int ret = VK_SUCCESS;
                
        cvkPositionBuffer = CVKBuffer.Create(cvkPositionStagingBuffer.GetBufferSize(), 
                                             VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                             GetLogger(),
                                             "CVKIconsRenderable.CreatePositionBuffer cvkPositionBuffer");       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R32G32B32A32_SFLOAT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkPositionBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(CVKDevice.GetVkDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hPositionBufferView = pBufferView.get(0);            
            GetLogger().info("Created CVKIconsRenderable.hPositionBufferView: 0x%016X", hPositionBufferView);
            
            // Descriptor sets will reference the old buffer view until updated
            SetDescriptorSetsState(pDescriptorSets != null ? CVK_RESOURCE_NEEDS_UPDATE : CVK_RESOURCE_NEEDS_REBUILD);
        }       
        
        return UpdatePositionBuffer();
    }
    
    private int UpdatePositionBuffer() {
//            List<DEBUG_CVKBufferElementDescriptor> DEBUG_positionDescriptors = new ArrayList<>();
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("X1", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Y1", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Z1", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Rad1", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("X2", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Y2", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Z2", Float.TYPE));
//            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Rad2", Float.TYPE)); 
//            cvkPositionStagingBuffer.DEBUGPRINT(DEBUG_positionDescriptors);            
        
        int ret = cvkPositionBuffer.CopyFrom(cvkPositionStagingBuffer);
        if (VkFailed(ret)) { return ret; }
        
        SetPositionBufferState(CVK_RESOURCE_CLEAN);                        
        
        return ret;                               
    }   
    
    private void DestroyPositionBuffer() {
        if (cvkPositionBuffer != null) {
            cvkPositionBuffer.Destroy();
            cvkPositionBuffer = null;
        }   
        if (hPositionBufferView != VK_NULL_HANDLE) {
            GetLogger().info("Destroying CVKIconsRenderable.hPositionBufferView: 0x%016X", hPositionBufferView);
            vkDestroyBufferView(CVKDevice.GetVkDevice(), hPositionBufferView, null);
            hPositionBufferView = VK_NULL_HANDLE;
        }
    }
    
    private int CreateVertexFlagsBuffer() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(cvkVertexFlagsBuffer == null);
        cvkVisualProcessor.VerifyInRenderThread();        
        int ret = VK_SUCCESS;
        
        cvkVertexFlagsBuffer = CVKBuffer.Create(cvkVertexFlagsStagingBuffer.GetBufferSize(), 
                                                VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                GetLogger(),
                                                "CVKIconsRenderable.CreateVertexFlagsBuffer cvkVertexFlagsBuffer");       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R8_SINT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkVertexFlagsBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R8_SINT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(CVKDevice.GetVkDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hVertexFlagsBufferView = pBufferView.get(0);
            GetLogger().info("Created CVKIconsRenderable.hVertexFlagsBufferView: 0x%016X", hVertexFlagsBufferView);
            
            // Descriptor sets will reference the old buffer view until updated
            SetDescriptorSetsState(pDescriptorSets != null ? CVK_RESOURCE_NEEDS_UPDATE : CVK_RESOURCE_NEEDS_REBUILD);   
        }       
        
        return UpdateVertexFlagsBuffer();
    }
    
    private int UpdateVertexFlagsBuffer() {
        int ret = cvkVertexFlagsBuffer.CopyFrom(cvkVertexFlagsStagingBuffer);  
        if (VkFailed(ret)) { return ret; }
        SetVertexFlagsBufferState(CVK_RESOURCE_CLEAN);
        
        return ret; 
    }
    
    private void DestroyVertexFlagsBuffer() {
        if (cvkVertexFlagsBuffer != null) {
            cvkVertexFlagsBuffer.Destroy();
            cvkVertexFlagsBuffer = null;
        }   
        if (hVertexFlagsBufferView != VK_NULL_HANDLE) {
            GetLogger().info("Destroying CVKIconsRenderable.hVertexFlagsBufferView: 0x%016X", hVertexFlagsBufferView);
            vkDestroyBufferView(CVKDevice.GetVkDevice(), hVertexFlagsBufferView, null);
            hVertexFlagsBufferView = VK_NULL_HANDLE;
        }        
    }
    
    
    // ========================> Uniform buffers <======================== \\
    
    // TODO: make sure these are called when the camera changes etc
    
    private int CreateVertexUniformBuffers() {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(vertexUniformBuffers == null);
 
        vertexUniformBuffers = new ArrayList<>();
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer vertexUniformBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                             GetLogger(),
                                                             String.format("CVKIconsRenderable vertexUniformBuffer %d", i));   
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
        
        // TODO: replace with constants.  In the JOGL version these were in a static var CAMERA that never changed
        vertexUBO.visibilityLow = cvkVisualProcessor.getDisplayCamera().getVisibilityLow();
        vertexUBO.visibilityHigh = cvkVisualProcessor.getDisplayCamera().getVisibilityHigh();            

        // Staging buffer so our VBO can be device local (most performant memory)
        ByteBuffer pMemory = cvkVertexUBStagingBuffer.StartMemoryMap(0, VertexUniformBufferObject.SizeOf());
        {
            vertexUBO.CopyTo(pMemory);
        }
        cvkVertexUBStagingBuffer.EndMemoryMap();
        pMemory = null;
        
        // Copy the staging buffer into the uniform buff-er on the device
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
                                                               String.format("CVKIconsRenderable geometryUniformBuffer %d", i));
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
        geometryUBO.pixelDensity = cvkVisualProcessor.GetPixelDensity();
        geometryUBO.highlightColor.set(mtxHighlightColour);

        geometryUBO.iconsPerRowColumn = CVKIconTextureAtlas.GetInstance().iconsPerRowColumn;
        geometryUBO.iconsPerLayer     = CVKIconTextureAtlas.GetInstance().iconsPerLayer;
        geometryUBO.atlas2DDimension  = CVKIconTextureAtlas.GetInstance().texture2DDimension;        
        
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
        vertexPushConstants = memAlloc(64);
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                vertexPushConstants.putFloat(IDENTITY_44F.get(iRow, iCol));
            }
        }
        
        // Set DrawHitTest to false
        hitTestPushConstants = memAlloc(4);
        hitTestPushConstants.putInt(0);
        
        vertexPushConstants.flip();
        hitTestPushConstants.flip();
        
        return VK_SUCCESS;
    }
    
    private void UpdateVertexPushConstants(){
        CVKAssertNotNull(cvkSwapChain);
        
        vertexPushConstants.clear();
        
        // Update MV Matrix
        Matrix44f mvMatrix = cvkVisualProcessor.getDisplayModelViewMatrix();
        for (int iRow = 0; iRow < 4; ++iRow) {
            for (int iCol = 0; iCol < 4; ++iCol) {
                vertexPushConstants.putFloat(mvMatrix.get(iRow, iCol));
            }
        }
        
        vertexPushConstants.flip();        
    }
    
    private void UpdatePushConstantsHitTest(boolean drawHitTest){
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
        if (vertexPushConstants != null) {
            memFree(vertexPushConstants);
            vertexPushConstants = null;
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
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKIconsRenderable %d", i));
            displayCommandBuffers.add(buffer);
            
            CVKCommandBuffer offscreenBuffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKIconsRenderable Offscreen Buffer %d", i));
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

        // Push MV matrix to the shader
        commandBuffer.PushConstants(hPipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, vertexPushConstants);

        // Push drawHitTest flag to the shaders
        commandBuffer.PushConstants(hPipelineLayout, VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, Matrix44f.BYTES, hitTestPushConstants);

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

        // Push MV matrix to the shader
        commandBuffer.PushConstants(hPipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, vertexPushConstants);

        // Push drawHitTest flag to the shaders
        commandBuffer.PushConstants(hPipelineLayout, VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, Matrix44f.BYTES, hitTestPushConstants);

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
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(6, stack);

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
            
            // 3: Geometry isamplerBuffer (vertex flags buffer)
            VkDescriptorSetLayoutBinding geometrySamplerDSLB = bindings.get(3);
            geometrySamplerDSLB.binding(3);
            geometrySamplerDSLB.descriptorCount(1);
            geometrySamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
            geometrySamplerDSLB.pImmutableSamplers(null);
            geometrySamplerDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);               

            // 4: Fragment sampler2Darray (atlas)
            VkDescriptorSetLayoutBinding fragmentSamplerDSLB = bindings.get(4);
            fragmentSamplerDSLB.binding(4);
            fragmentSamplerDSLB.descriptorCount(1);
            fragmentSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            fragmentSamplerDSLB.pImmutableSamplers(null);
            fragmentSamplerDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            
            // 5: Fragment uniform buffer
            VkDescriptorSetLayoutBinding fragmentUBDSLB = bindings.get(5);
            fragmentUBDSLB.binding(5);
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
                GetLogger().info("CVKIconsRenderable created hDescriptorLayout: 0x%016X", hDescriptorLayout);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        GetLogger().info("CVKIconsRenderable destroying hDescriptorLayout: 0x%016X", hDescriptorLayout);
        vkDestroyDescriptorSetLayout(CVKDevice.GetVkDevice(), hDescriptorLayout, null);
        hDescriptorLayout = VK_NULL_HANDLE;
    }
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        
        int ret;    

        // The same layout is used for each descriptor set (each descriptor set is
        // identical but allow the GPU and CPU to desynchronise.
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
            GetLogger().info("CVKIconsRenderable allocated hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
        }
        
        return UpdateDescriptorSets(stack);
    }
    
    // TODO_TT: do we gain anything by having buffered UBOs?
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(pDescriptorSets);
        CVKAssert(pDescriptorSets.capacity() > 0);
        CVKAssert(hPositionBufferView != VK_NULL_HANDLE);
        CVKAssert(hVertexFlagsBufferView != VK_NULL_HANDLE);
        CVKAssertNotNull(vertexUniformBuffers);
        CVKAssert(vertexUniformBuffers.size() > 0);
        CVKAssertNotNull(geometryUniformBuffers);
        CVKAssert(geometryUniformBuffers.size() > 0);   
        
        int ret = VK_SUCCESS;
     
        final int imageCount = cvkSwapChain.GetImageCount();
        
        // - Descriptor info structs -
        // We create these to describe the different resources we want to address
        // in shaders.  We have one info struct per resource.  We then create a 
        // write descriptor set structure for each resource for each image.  For
        // buffered resources like the the uniform buffers we wait to set the 
        // buffer resource until the image loop below.
        
        // Struct for the uniform buffer used by VertexIcon.vs
        VkDescriptorBufferInfo.Buffer vertexUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // vertexUniformBufferInfo.buffer is set per imageIndex
        vertexUniformBufferInfo.offset(0);
        vertexUniformBufferInfo.range(VertexUniformBufferObject.SizeOf());        
        
        // Struct for texel buffer (positions) used by VertexIcon.vs
        VkDescriptorBufferInfo.Buffer positionsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        positionsTexelBufferInfo.buffer(cvkPositionBuffer.GetBufferHandle());
        positionsTexelBufferInfo.offset(0);
        positionsTexelBufferInfo.range(cvkPositionBuffer.GetBufferSize());               

        // Struct for the uniform buffer used by VertexIcon.gs
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // geometryBufferInfo.buffer is set per imageIndex
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(GeometryUniformBufferObject.SizeOf());      
        
        // Struct for texel buffer (vertex flags) used by VertexIcon.gs
        VkDescriptorBufferInfo.Buffer vertexFlagsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        vertexFlagsTexelBufferInfo.buffer(cvkVertexFlagsBuffer.GetBufferHandle());
        vertexFlagsTexelBufferInfo.offset(0);
        vertexFlagsTexelBufferInfo.range(cvkVertexFlagsBuffer.GetBufferSize());            

        // Struct for the size of the image sampler (atlas) used by VertexIcon.fs
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle());
        imageInfo.sampler(CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle());

        // We need 5 write descriptors, 2 for uniform buffers, 2 for texel buffers and 1 for texture sampler                       
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(5, stack);

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
        
        // Geometry texel buffer (vertex flags)
        VkWriteDescriptorSet vertexFlagsTBDescriptorWrite = descriptorWrites.get(3);
        vertexFlagsTBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertexFlagsTBDescriptorWrite.dstBinding(3);
        vertexFlagsTBDescriptorWrite.dstArrayElement(0);
        vertexFlagsTBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
        vertexFlagsTBDescriptorWrite.descriptorCount(1);
        vertexFlagsTBDescriptorWrite.pBufferInfo(vertexFlagsTexelBufferInfo);       
        vertexFlagsTBDescriptorWrite.pTexelBufferView(stack.longs(hVertexFlagsBufferView));

        // Fragment image (atlas) sampler
        VkWriteDescriptorSet atlasSamplerDescriptorWrite = descriptorWrites.get(4);
        atlasSamplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        atlasSamplerDescriptorWrite.dstBinding(4);
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
            GetLogger().info("CVKIconsRenderable updating descriptorSet: 0x%016X", descriptorSet);
            vkUpdateDescriptorSets(CVKDevice.GetVkDevice(), descriptorWrites, null);
        }
        
        // Cache atlas handles so we know when to recreate descriptors
        hIconAtlasSampler = CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle();
        hIconAtlasImageView = CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle();            
        
        SetDescriptorSetsState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }
        
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
        if (pDescriptorSets != null) {
            CVKAssertNotNull(cvkDescriptorPool);
            CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());            
            GetLogger().fine("CVKIconsRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity());
            
            for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
                GetLogger().info("CVKIconsRenderable freeing hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
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
        // VertexIcon.vs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // VertexIcon.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // VertexIcon.fs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
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
            pushConstantRange.get(0).stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            pushConstantRange.get(0).size(64);
            pushConstantRange.get(0).offset(0);

            pushConstantRange.get(1).stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT);
            pushConstantRange.get(1).size(4);
            pushConstantRange.get(1).offset(64);

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
        return vertexCount > 0 &&
               (positionBufferState != CVK_RESOURCE_CLEAN ||
                vertexFlagsBufferState != CVK_RESOURCE_CLEAN ||
                vertexUBOState != CVK_RESOURCE_CLEAN ||
                geometryUBOState != CVK_RESOURCE_CLEAN ||              
                vertexBuffersState != CVK_RESOURCE_CLEAN ||
                commandBuffersState != CVK_RESOURCE_CLEAN ||
                descriptorSetsState != CVK_RESOURCE_CLEAN ||
                pipelinesState != CVK_RESOURCE_CLEAN ||                
                hIconAtlasSampler != CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle() ||
                hIconAtlasImageView != CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle() ); 
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        cvkVisualProcessor.VerifyInRenderThread();
        
        if (hIconAtlasSampler != CVKIconTextureAtlas.GetInstance().GetAtlasSamplerHandle() ||
            hIconAtlasImageView != CVKIconTextureAtlas.GetInstance().GetAtlasImageViewHandle()) {
            if (descriptorSetsState != CVK_RESOURCE_NEEDS_REBUILD) {
                descriptorSetsState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        }
                  
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

            // Update position buffer
            if (positionBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                DestroyPositionBuffer();
                ret = CreatePositionBuffer();
                if (VkFailed(ret)) { return ret; }            
            } else if (positionBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = UpdatePositionBuffer();
                if (VkFailed(ret)) { return ret; }            
            }

            // Update vertex flags buffer
            if (vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                DestroyVertexFlagsBuffer();
                ret = CreateVertexFlagsBuffer();
                if (VkFailed(ret)) { return ret; }            
            } else if (vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = UpdateVertexFlagsBuffer();
                if (VkFailed(ret)) { return ret; }            
            }                  
        
            // Vertex uniform buffer (camera guff)
            if (vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateVertexUniformBuffers();
                if (VkFailed(ret)) { return ret; }
            } else if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexUniformBuffers();
                if (VkFailed(ret)) { return ret; }               
            }

            // Geometry uniform buffer (projection, highlight colour and hit test)
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
    
    private Vertex[] BuildVertexArray(final VisualAccess access, int first, int last) {
        final int newVertexCount = (last - first) + 1;
        if (newVertexCount > 0) {
            Vertex vertices[] = new Vertex[newVertexCount];            
            for (int pos = first; pos <= last; ++pos) {
                vertices[pos] = new Vertex();
                Vertex vertex = vertices[pos];
                SetColorInfo(pos, vertex, access);
                SetIconIndexes(pos, vertex, access);
            }            
            
            return vertices;
        } else {
            return null;
        } 
    }  
    
    private Position[] BuildPositionArray(final VisualAccess access, int first, int last) {
        final int newVertexCount = (last - first) + 1;
        if (newVertexCount > 0) {
            Position positions[] = new Position[newVertexCount];            
            for (int pos = first; pos <= last; ++pos) {
                positions[pos] = new Position(access.getX(pos),
                                              access.getY(pos),
                                              access.getZ(pos),
                                              access.getRadius(pos),
                                              access.getX2(pos),
                                              access.getY2(pos),
                                              access.getZ2(pos),
                                              access.getRadius(pos));                 
            }            
            
            return positions;
        } else {
            return null;
        } 
    }           
    
    private byte[] BuildVertexFlagArray(final VisualAccess access, int first, int last) {
        final int newVertexCount = (last - first) + 1;
        if (newVertexCount > 0) {
            byte vertexFlags[] = new byte[newVertexCount];            
            for (int pos = first; pos <= last; ++pos) {
                final boolean isSelected = access.getVertexSelected(pos);
                final boolean isDimmed = access.getVertexDimmed(pos);                
                vertexFlags[pos] = (byte)((isDimmed ? DIMMED_BIT : 0) | (isSelected ? SELECTED_BIT : 0));  
            }            
            
            return vertexFlags;
        } else {
            return null;
        }        
    }    
    
    private void RebuildVertexStagingBuffer(Vertex[] vertices) {        
        final int newSizeBytes = (vertices != null ? vertices.length : 0) * Vertex.BYTES;
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
                                                          "CVKIconsRenderable.RebuildVertexStagingBuffer cvkVertexStagingBuffer");
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
    
    private void RebuildPositionStagingBuffer(Position[] positions) {
        final int newSizeBytes = (positions != null ? positions.length : 0) * Position.BYTES;
        final boolean recreate = cvkPositionStagingBuffer == null || newSizeBytes != cvkPositionStagingBuffer.GetBufferSize();
        
        if (recreate) {
            if (cvkPositionStagingBuffer != null) {
                cvkPositionStagingBuffer.Destroy();
                cvkPositionStagingBuffer = null;
            }
            
            if (newSizeBytes > 0) {
                cvkPositionStagingBuffer = CVKBuffer.Create(newSizeBytes, 
                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                            GetLogger(),
                                                            "CVKIconsRenderable.RebuildPositionStagingBuffer cvkPositionBuffer");
            }
        }       

        if (newSizeBytes > 0) {   
            UpdatePositionStagingBuffer(positions, 0, positions.length - 1);                            
        }  
    }
    
    private void UpdatePositionStagingBuffer(Position[] positions, int first, int last) {
        CVKAssertNotNull(cvkPositionStagingBuffer);
        CVKAssertNotNull(positions != null);
        CVKAssert(positions.length > 0 && positions.length > last);
        CVKAssert(last >= 0 && last >= first && first >= 0);

        int offset = first * Position.BYTES;
        int size = ((last - first) + 1) * Position.BYTES;

        ByteBuffer pMemory = cvkPositionStagingBuffer.StartMemoryMap(offset, size);
        for (Position position : positions) {
            position.CopyToSequentially(pMemory);
        }
        cvkPositionStagingBuffer.EndMemoryMap();
        pMemory = null; // now unmapped, do not use           
    }          
    
    private void RebuildVertexFlagsStagingBuffer(byte[] vertexFlags) {
        final int newSizeBytes = (vertexFlags != null ? vertexFlags.length : 0) * Byte.BYTES;
        final boolean recreate = cvkVertexFlagsStagingBuffer == null || newSizeBytes != cvkVertexFlagsStagingBuffer.GetBufferSize();
        
        if (recreate) {
            if (cvkVertexFlagsStagingBuffer != null) {
                cvkVertexFlagsStagingBuffer.Destroy();
                cvkVertexFlagsStagingBuffer = null;
            }
            
            if (newSizeBytes > 0) {
                cvkVertexFlagsStagingBuffer = CVKBuffer.Create(newSizeBytes, 
                                                               VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                               VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                               GetLogger(),
                                                               "CVKIconsRenderable.RebuildVertexFlagsStagingBuffers cvkVertexFlagsStagingBuffer");
            }
        }       

        if (newSizeBytes > 0) {   
            UpdateVertexFlagsStagingBuffer(vertexFlags, 0, vertexFlags.length - 1);                            
        }           
    }   
    
    private void UpdateVertexFlagsStagingBuffer(byte[] vertexFlags, int first, int last) {
        CVKAssert(vertexFlags != null && vertexFlags.length > 0);
        CVKAssert(last >= 0 && last >= first && first >= 0);

        int offset = first * Byte.BYTES;
        int size = ((last - first) + 1) * Byte.BYTES;

        ByteBuffer pVertexFlagsMemory = cvkVertexFlagsStagingBuffer.StartMemoryMap(offset, size);
        pVertexFlagsMemory.put(vertexFlags);
        cvkVertexFlagsStagingBuffer.EndMemoryMap();
        pVertexFlagsMemory = null; // now unmapped, do not use           
    }              
    
    public CVKRenderUpdateTask TaskUpdateIcons(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if we our vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexStagingBuffer == null || 
                                        access.getVertexCount() * Vertex.BYTES != cvkVertexStagingBuffer.GetBufferSize() || 
                                        change.isEmpty();
        final int changedVerticeRange[];
        final Vertex vertices[];
        if (rebuildRequired) {
            GetLogger().fine("TaskUpdateIcons frame %d: all (%d) verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
            vertices = BuildVertexArray(access, 0, access.getVertexCount() - 1);
            changedVerticeRange = null;
        } else {
            changedVerticeRange = change.getRange();
            GetLogger().fine("TaskUpdateIcons frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), (changedVerticeRange[1] - changedVerticeRange[0]) + 1);
            vertices = BuildVertexArray(access, changedVerticeRange[0], changedVerticeRange[1]);         
        }
        
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
    
    public CVKRenderUpdateTask TaskUpdatePositions(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//                
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if we our vertex count has somehow changed.
        final boolean rebuildRequired = cvkPositionStagingBuffer == null || 
                                        access.getVertexCount() * Position.BYTES != cvkPositionStagingBuffer.GetBufferSize() || 
                                        change.isEmpty();
        final int changedVerticeRange[];
        final Position positions[];
        if (rebuildRequired) {
            GetLogger().fine("TaskUpdatePositions frame %d: all (%d) verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
            positions = BuildPositionArray(access, 0, access.getVertexCount() - 1);
            changedVerticeRange = null;
        } else {
            changedVerticeRange = change.getRange();
            GetLogger().fine("TaskUpdatePositions frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), (changedVerticeRange[1] - changedVerticeRange[0]) + 1);
            positions = BuildPositionArray(access, changedVerticeRange[0], changedVerticeRange[1]);         
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {
                RebuildPositionStagingBuffer(positions);
                SetPositionBufferState(CVK_RESOURCE_NEEDS_REBUILD);
            } else if (positionBufferState != CVK_RESOURCE_NEEDS_REBUILD) {
                UpdatePositionStagingBuffer(positions, changedVerticeRange[0], changedVerticeRange[1]);
                SetPositionBufferState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };         
    }    
    
    public CVKRenderUpdateTask TaskUpdateVertexFlags(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        GetLogger().fine("TaskUpdateVertexFlags frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
        
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if we our vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexFlagsStagingBuffer == null || 
                                        access.getVertexCount() * Position.BYTES != cvkVertexFlagsStagingBuffer.GetBufferSize() ||
                                        change.isEmpty();
        final int changedVerticeRange[];
        final byte vertexFlags[];
        if (rebuildRequired) {
            vertexFlags = BuildVertexFlagArray(access, 0, access.getVertexCount() - 1);
            changedVerticeRange = null;
        } else {
            changedVerticeRange = change.getRange();
            vertexFlags = BuildVertexFlagArray(access, changedVerticeRange[0], changedVerticeRange[1]);         
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {
                RebuildVertexFlagsStagingBuffer(vertexFlags);
                SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_REBUILD);
            } else if (vertexFlagsBufferState != CVK_RESOURCE_NEEDS_REBUILD) {
                UpdateVertexFlagsStagingBuffer(vertexFlags, changedVerticeRange[0], changedVerticeRange[1]);
                SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };         
    }
    
    public CVKRenderUpdateTask TaskUpdateColours(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        GetLogger().fine("TaskUpdateColours frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (vertexBuffersState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };             
    }   
    
    public CVKRenderUpdateTask TaskSetHighlightColour(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        GetLogger().fine("TaskSetHighLightColour frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
        final ConstellationColor highlightColour = access.getHighlightColor();
                                                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            mtxHighlightColour.set(0, 0, highlightColour.getRed());
            mtxHighlightColour.set(1, 1, highlightColour.getGreen());
            mtxHighlightColour.set(2, 2, highlightColour.getBlue());
          
            if (geometryUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetGeometryUBOState(CVK_RESOURCE_NEEDS_UPDATE);
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
    
    
    // ========================> Helpers <======================== \\
    
    private Vector4i MakeIconIndexes(final int pos, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(pos < access.getVertexCount());
        
        final String foregroundIconName = access.getForegroundIcon(pos);
        final String backgroundIconName = access.getBackgroundIcon(pos);
        final int foregroundIconIndex = CVKIconTextureAtlas.GetInstance().AddIcon(foregroundIconName);
        final int backgroundIconIndex = CVKIconTextureAtlas.GetInstance().AddIcon(backgroundIconName);

        final String nWDecoratorName = access.getNWDecorator(pos);
        final String sWDecoratorName = access.getSWDecorator(pos);
        final String sEDecoratorName = access.getSEDecorator(pos);
        final String nEDecoratorName = access.getNEDecorator(pos);
        final int nWDecoratorIndex = nWDecoratorName != null ? CVKIconTextureAtlas.GetInstance().AddIcon(nWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sWDecoratorIndex = sWDecoratorName != null ? CVKIconTextureAtlas.GetInstance().AddIcon(sWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sEDecoratorIndex = sEDecoratorName != null ? CVKIconTextureAtlas.GetInstance().AddIcon(sEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int nEDecoratorIndex = nEDecoratorName != null ? CVKIconTextureAtlas.GetInstance().AddIcon(nEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);

        return new Vector4i(icons, decoratorsWest, decoratorsEast, access.getVertexId(pos));
    }      
    
    private void SetIconIndexes(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(vertex != null);        
        vertex.data.set(MakeIconIndexes(pos, access));
    }    
    
    private void SetColorInfo(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(vertex != null);
        CVKAssert(pos < access.getVertexCount());
        
        vertex.SetBackgroundIconColour(access.getVertexColor(pos));
        vertex.SetVertexVisibility(access.getVertexVisibility(pos));
    }      
    
    public long GetPositionBufferViewHandle() { return hPositionBufferView; }
    
    public long GetPositionBufferHandle() { return cvkPositionBuffer != null ? cvkPositionBuffer.GetBufferHandle() : VK_NULL_HANDLE; }
    
    public long GetPositionBufferSize() { return cvkPositionBuffer != null ? cvkPositionBuffer.GetBufferSize() : 0; }
    
    public long GetVertexFlagsBufferViewHandle() { return hVertexFlagsBufferView; }
}
