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
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.glyphs.GlyphManager;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4i;
import au.gov.asd.tac.constellation.utilities.text.LabelUtilities;
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
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKGlyphTextureAtlas;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
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
 * Requirements: 
 * - labels on connections (these can be multiline)
 * - labels above or below icons
 * - icon labels can be in any language
 * - icon label determined by attribute 'identifier', can be multiline
 * - icon label top determined by graph array attribute 'node_labels_top' (array of attributes, first 4 displayed)
 * - icon label bottom determined by graph array attribute 'node_labels_bottom'
 * - appears only 1 font at present, SansSerif 64 Plain.  This is likely configurable
 */

/**
 * What resources do node labels have
 *  - vs: vertex push constant (camera matrix)
 *  0 vs: vertex ubo (label meta data)
 *  1 vs: xyzw buffer sampler (icon positions owned by CVKIconsRenderable)
 *  2 gs: geometry ubo (proj mtx and render data)
 *  3 gs: glyphInfoTexture buffer sampler (glyph coordinates)
 *  4 fs: atlas texture sampler (rastered glyphs owned by CVKGlyphTextureAtlas)
 */
public class CVKIconLabelsRenderable extends CVKRenderable implements GlyphManager.GlyphStream {     
    
    // Resources recreated with the swap chain (dependent on the image count)    
    private LongBuffer pDescriptorSets = null; 
    private List<CVKCommandBuffer> displayCommandBuffers = null;
    private List<CVKBuffer> vertexBuffers = null;   
    private List<CVKBuffer> vertexUniformBuffers = null;
    private List<CVKBuffer> geometryUniformBuffers = null;    
    
    private ByteBuffer vertexPushConstants = null;
    private final VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private final GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject();     
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private final GlyphContext glyphContext = new GlyphContext();
    private List<Vertex> vertices = null;
    private CVKBuffer cvkVertexStagingBuffer = null;  

    private CVKBuffer cvkVertexUBStagingBuffer = null;
    private CVKBuffer cvkGeometryUBStagingBuffer = null;
    private final Vector4i topLabelRowSizes = new Vector4i();
    private final Vector4i bottomLabelRowSizes = new Vector4i();
    private final Vector3f[] topLabelColours = new Vector3f[LabelUtilities.MAX_LABELS_TO_DRAW];
    private final Vector3f[] bottomLabelColours = new Vector3f[LabelUtilities.MAX_LABELS_TO_DRAW]; 
    
    // Resources we don't own but use and must track so we know when to update
    // our descriptors
    private long hGlyphAtlasSampler = VK_NULL_HANDLE;
    private long hGlyphAtlasImageView = VK_NULL_HANDLE;     
    private long hGlyphCoordinateBuffer = VK_NULL_HANDLE;
    private long hGlyphCoordinateBufferView = VK_NULL_HANDLE;
    private long hPositionBuffer = VK_NULL_HANDLE;
    private long hPositionBufferView = VK_NULL_HANDLE;
   
    
    // ========================> Classes <======================== \\
    
    private static class GlyphContext {
        private float visibility;
        private int nodeId;
        private int totalScale;
        private int labelNumber;     
    }
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int BYTES = Vector3f.BYTES + Vector4i.BYTES;
        private static final int OFFSETOF_GLYPH_DATA = 0;
        private static final int OFFSETOF_GRAPH_DATA = 3 * Float.BYTES;        
        private static final int BINDING = 0;        
        
        // [0..1] x and y offsets of this glyph from the top centre of the line of text
        // [2] The visibility of this glyph (constant for a node, but easier to pass in the batch).
        private final Vector3f glyphLocationData = new Vector3f();
        
        // [0] the index of the glyph in the glyphInfoTexture
        // [1] The index of the node containg this glyph in the xyzTexture
        // [2] The total scale of the lines and their labels up to this point (< 0 if this is a glyph in a bottom label)
        // [3] The label number in which this glyph occurs     
        private final Vector4i graphLocationData = new Vector4i();
        
        public Vertex(int glyphIndex, float x, float y, float visibility, int nodeId, int totalScale, int labelNumber) {
            glyphLocationData.setX(x);
            glyphLocationData.setY(y);
            glyphLocationData.setZ(visibility);
            graphLocationData.a[0] = glyphIndex;
            graphLocationData.a[1] = nodeId;
            graphLocationData.a[2] = totalScale;
            graphLocationData.a[3] = labelNumber;
        }           
        
        public void CopyToSequentially(ByteBuffer buffer) {
            buffer.putFloat(glyphLocationData.a[0]);
            buffer.putFloat(glyphLocationData.a[1]);
            buffer.putFloat(glyphLocationData.a[2]);            
            buffer.putInt(graphLocationData.a[0]);
            buffer.putInt(graphLocationData.a[1]);
            buffer.putInt(graphLocationData.a[2]);
            buffer.putInt(graphLocationData.a[3]);              
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

            // glyphLocationData
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
            posDescription.offset(OFFSETOF_GLYPH_DATA);

            // graphLocationData
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SINT);
            colorDescription.offset(OFFSETOF_GRAPH_DATA);

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

    private static class VertexUniformBufferObject {                
        // Each column is a node (bottom or top) label with the following structure:
        // [0..2] rgb colour (note label colours do not habve an alpha)
        // [3] label size
        private final Matrix44f labelBottomInfo = new Matrix44f();
        private final Matrix44f labelTopInfo = new Matrix44f();

        // Information from the graph's visual state
        private float morphMix = 0;
        private float visibilityLow = 0;
        private float visibilityHigh = 0;

        // The index of the background glyph in the glyphInfo texture
        private int backgroundGlyphIndex = 0;

        // Used to draw the label background.
        private final Vector4f backgroundColor = new Vector4f();        
        
        private static Integer padding1 = null;
        private static Integer padding2 = null;
        
        
        private static int SizeOf() {
            if (padding1 == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (16 * Float.BYTES));

                int sizeof = Matrix44f.BYTES +   // labelBottomInfo
                             Matrix44f.BYTES +   // labelTopInfo
                             1 * Float.BYTES +   // morphMix
                             1 * Float.BYTES +   // visibilityLow
                             1 * Float.BYTES +   // visibilityHigh 
                             1 * Integer.BYTES;  // backgroundGlyphIndex

                int overrun = sizeof % minAlignment;
                padding1 = overrun > 0 ? minAlignment - overrun : 0;
                
                sizeof += padding1 +
                          Vector4f.BYTES; //backgroundColor
                
                overrun = sizeof % minAlignment;
                padding2 = overrun > 0 ? minAlignment - overrun : 0;                
                
            }
            
            return Matrix44f.BYTES +   // labelBottomInfo
                   Matrix44f.BYTES +   // labelTopInfo
                   1 * Float.BYTES +   // morphMix
                   1 * Float.BYTES +   // visibilityLow
                   1 * Float.BYTES +   // visibilityHigh 
                   1 * Integer.BYTES + // backgroundGlyphIndex
                   padding1 + 
                   Vector4f.BYTES +    //backgroundColor 
                   padding2;
        }        
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(labelBottomInfo.get(iRow, iCol));
                }
            }    
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(labelTopInfo.get(iRow, iCol));
                }
            }              
            buffer.putFloat(morphMix);
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);
            buffer.putInt(backgroundGlyphIndex);
            
            for (int i = 0; i < padding1; ++i) {
                buffer.put((byte)0);
            }  
            
            for (int i = 0; i < Vector4f.LENGTH; ++i) {
                buffer.putFloat(backgroundColor.a[i]);
            }      
            
            for (int i = 0; i < padding2; ++i) {
                buffer.put((byte)0);
            }             
        }         
    } 
    
    private static class GeometryUniformBufferObject {                
        // Matrix to convert from camera coordinates to scene coordinates.
        private final Matrix44f pMatrix = new Matrix44f();

        // The scaling factor to convert from texture coordinates to world unit coordinates
        private float widthScalingFactor;
        private float heightScalingFactor;

        // Used to draw the connection indicator on the label background.
        private final Vector4f highlightColor = new Vector4f();
        
        private static Integer padding1 = null;
        private static Integer padding2 = null;
        
        
        private static int SizeOf() {
            if (padding1 == null) {
                CVKAssertNotNull(CVKDevice.GetVkDevice()); 
                final int minAlignment = CVKDevice.GetMinUniformBufferAlignment();
                
                // The matrices are 64 bytes each so should line up on a boundary (unless the minimum alignment is huge)
                CVKAssert(minAlignment <= (16 * Float.BYTES));

                int sizeof = Matrix44f.BYTES +   // pMatrix
                             1 * Float.BYTES +   // widthScalingFactor
                             1 * Float.BYTES;    // heightScalingFactor

                int overrun = sizeof % minAlignment;
                padding1 = overrun > 0 ? minAlignment - overrun : 0;
                
                sizeof += padding1 +
                          Vector4f.LENGTH * Float.BYTES;        //highlightColor
                
                overrun = sizeof % minAlignment;
                padding2 = overrun > 0 ? minAlignment - overrun : 0;                                
            }
            
            return Matrix44f.BYTES +   // pMatrix
                   1 * Float.BYTES +   // widthScalingFactor
                   1 * Float.BYTES +   // heightScalingFactor
                   padding1 + 
                   Vector4f.BYTES +    //highlightColor 
                   padding2;
        }        
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            }                 
            buffer.putFloat(widthScalingFactor);
            buffer.putFloat(heightScalingFactor);
            
            for (int i = 0; i < padding1; ++i) {
                buffer.put((byte)0);
            }  
            
            for (int i = 0; i < Vector4f.LENGTH; ++i) {
                buffer.putFloat(highlightColor.a[i]);
            }      
            
            for (int i = 0; i < padding2; ++i) {
                buffer.put((byte)0);
            }             
        }         
    }   
    
    
    // ========================> Shaders <======================== \\
    
    @Override
    protected String GetVertexShaderName() { return "NodeLabel.vs"; }
    
    @Override
    protected String GetGeometryShaderName() { return "Label.gs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "Label.fs"; }      
                
    
    // ========================> Lifetime <======================== \\
    
    public CVKIconLabelsRenderable(CVKVisualProcessor visualProcessor) {
        super(visualProcessor);
        for (int i = 0; i < LabelUtilities.MAX_LABELS_TO_DRAW; ++i) {
            topLabelColours[i] = new Vector3f();
            bottomLabelColours[i] = new Vector3f();
        }      
    }       
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                    GetLogger(),
                                                    "CVKIconLabelsRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer");   
        cvkGeometryUBStagingBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                      GetLogger(),
                                                      "CVKIconLabelsRenderable.CreateUBOStagingBuffers cvkGeometryUBStagingBuffer");         
    }
    
    @Override
    public int Initialise() {
        int ret = super.Initialise();  
        if (VkFailed(ret)) { return ret; }   
        
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
        
        // Reset our cached handles
        hGlyphAtlasSampler = VK_NULL_HANDLE;
        hGlyphAtlasImageView = VK_NULL_HANDLE;     
        hGlyphCoordinateBufferView = VK_NULL_HANDLE;
        hPositionBufferView = VK_NULL_HANDLE;

        CVKAssert(vertexBuffers == null);
        CVKAssert(vertexUniformBuffers == null);
        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(pDescriptorSets == null);
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);  
        CVKAssert(displayCommandBuffers == null);        
        CVKAssert(displayPipelines == null);
        CVKAssert(hPipelineLayout == VK_NULL_HANDLE);    
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
                                                             String.format("CVKIconLabelsRenderable cvkVertexBuffer %d", i));
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
    public int GetVertexCount() {
        return cvkVisualProcessor.GetDrawFlags().drawNodes() && 
               cvkVisualProcessor.GetDrawFlags().drawNodeLabels() ? 
                   vertexCount : 0; 
    }   
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }    
    

    // ========================> Uniform buffers <======================== \\
    
    private int CreateVertexUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(vertexUniformBuffers == null);
 
        vertexUniformBuffers = new ArrayList<>();
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer vertexUniformBuffer = CVKBuffer.Create(VertexUniformBufferObject.SizeOf(),
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                             GetLogger(),
                                                             String.format("CVKIconLabelsRenderable vertexUniformBuffer %d", i));   
            vertexUniformBuffers.add(vertexUniformBuffer);                     
        }        
        return UpdateVertexUniformBuffers(stack);
    }
        
    private int UpdateVertexUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkVertexUBStagingBuffer != null);
        CVKAssert(vertexUniformBuffers != null);
        CVKAssert(vertexUniformBuffers.size() > 0);
        
        int ret = VK_SUCCESS;
        
        // While this never changes we won't know it when the UBO is created as the
        // background glyph won't be added until the first render as we need the
        // device to be initialised.  
        // TODO: check this is correct, it comes from the glyphmanager not the atlas
        vertexUBO.backgroundGlyphIndex = CVKGlyphTextureAtlas.BACKGROUND_GLYPH_INDEX;
        
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
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = vertexUniformBuffers.get(i).CopyFrom(cvkVertexUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }
        
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
    
    private int CreateGeometryUniformBuffers(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssert(geometryUniformBuffers == null);

        geometryUniformBuffers = new ArrayList<>(); 
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer geometryUniformBuffer = CVKBuffer.Create(GeometryUniformBufferObject.SizeOf(),
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                                               GetLogger(),
                                                               String.format("CVKIconLabelsRenderable geometryUniformBuffer %d", i));
            geometryUniformBuffers.add(geometryUniformBuffer);              
        }
        return UpdateGeometryUniformBuffers(stack);
    }
    
    private int UpdateGeometryUniformBuffers(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkGeometryUBStagingBuffer);
        CVKAssertNotNull(geometryUniformBuffers);
        CVKAssert(geometryUniformBuffers.size() > 0);        
        
        int ret = VK_SUCCESS;
        
        geometryUBO.pMatrix.set(cvkVisualProcessor.GetProjectionMatrix());
        geometryUBO.widthScalingFactor  = CVKGlyphTextureAtlas.GetInstance().GetWidthScalingFactor();
        geometryUBO.heightScalingFactor = CVKGlyphTextureAtlas.GetInstance().GetWidthScalingFactor();
        
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
        vertexPushConstants.flip();
        
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
    
    private void DestroyPushConstants() {
        if (vertexPushConstants != null) {
            memFree(vertexPushConstants);
            vertexPushConstants = null;
        }
    }
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        displayCommandBuffers = new ArrayList<>(imageCount);
        
        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKIconLabelsRenderable %d", i));
            displayCommandBuffers.add(buffer);
        }
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }   
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {
        return displayCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
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

        commandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }
        
        return ret;
    }       
    
    private void DestroyCommandBuffers() {         
        if (displayCommandBuffers != null) {
            displayCommandBuffers.forEach(el -> {el.Destroy();});
            displayCommandBuffers.clear();
            displayCommandBuffers = null;
        }      
    }        
    
        
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout() {
        int ret;

        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(5, stack);

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
            
            // 3: Geometry samplerBuffer (glyph coordinate buffer)
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

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(CVKDevice.GetVkDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
                GetLogger().info("CVKIconLabelsRenderable created hDescriptorLayout: 0x%016X", hDescriptorLayout);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        GetLogger().info("CVKIconLabelsRenderable destroying hDescriptorLayout: 0x%016X", hDescriptorLayout);
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
            GetLogger().info("CVKIconLabelsRenderable allocated hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
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
        
        // Cache atlas handles so we know when to recreate descriptors
        hGlyphAtlasSampler = CVKGlyphTextureAtlas.GetInstance().GetAtlasSamplerHandle();
        hGlyphAtlasImageView = CVKGlyphTextureAtlas.GetInstance().GetAtlasImageViewHandle(); 
        hGlyphCoordinateBuffer = CVKGlyphTextureAtlas.GetInstance().GetCoordinateBufferHandle();
        hGlyphCoordinateBufferView = CVKGlyphTextureAtlas.GetInstance().GetCoordinateBufferViewHandle();
        hPositionBuffer = cvkVisualProcessor.GetPositionBufferHandle();
        hPositionBufferView = cvkVisualProcessor.GetPositionBufferViewHandle();  
        CVKAssertNotNull(hGlyphAtlasSampler);
        CVKAssertNotNull(hGlyphAtlasImageView);
        CVKAssertNotNull(hGlyphCoordinateBuffer);
        CVKAssertNotNull(hGlyphCoordinateBufferView);
        CVKAssertNotNull(hPositionBuffer);
        CVKAssertNotNull(hPositionBufferView);
        
        // - Descriptor info structs -
        // We create these to describe the different resources we want to address
        // in shaders.  We have one info struct per resource.  We then create a 
        // write descriptor set structure for each resource for each image.  For
        // buffered resources like the the uniform buffers we wait to set the 
        // buffer resource until the image loop below.       
                      
        // Struct for the uniform buffer used by NodeLabel.vs
        VkDescriptorBufferInfo.Buffer vertexUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // vertexUniformBufferInfo.buffer is set per imageIndex
        vertexUniformBufferInfo.offset(0);
        vertexUniformBufferInfo.range(VertexUniformBufferObject.SizeOf());  
        
        // Struct for texel buffer (positions) used by NodeLabel.vs
        VkDescriptorBufferInfo.Buffer positionsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        positionsTexelBufferInfo.buffer(hPositionBuffer);
        positionsTexelBufferInfo.offset(0);
        positionsTexelBufferInfo.range(cvkVisualProcessor.GetPositionBufferSize());             

        // Struct for the uniform buffer used by Label.gs
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // geometryBufferInfo.buffer is set per imageIndex
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(GeometryUniformBufferObject.SizeOf());      
        
        // Struct for texel buffer (glyph coordinates) used by Label.gs
        VkDescriptorBufferInfo.Buffer glyphCoordinateTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        glyphCoordinateTexelBufferInfo.buffer(hGlyphCoordinateBuffer);
        glyphCoordinateTexelBufferInfo.offset(0);
        glyphCoordinateTexelBufferInfo.range(CVKGlyphTextureAtlas.GetInstance().GetCoordinateBufferSize());            

        // Struct for the size of the image sampler (atlas) used by Label.fs
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(hGlyphAtlasImageView);
        imageInfo.sampler(hGlyphAtlasSampler);

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
        
        // Geometry texel buffer (glyph coordinates)
        VkWriteDescriptorSet vertexFlagsTBDescriptorWrite = descriptorWrites.get(3);
        vertexFlagsTBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertexFlagsTBDescriptorWrite.dstBinding(3);
        vertexFlagsTBDescriptorWrite.dstArrayElement(0);
        vertexFlagsTBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
        vertexFlagsTBDescriptorWrite.descriptorCount(1);
        vertexFlagsTBDescriptorWrite.pBufferInfo(glyphCoordinateTexelBufferInfo);       
        vertexFlagsTBDescriptorWrite.pTexelBufferView(stack.longs(hGlyphCoordinateBufferView));

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
            GetLogger().info("CVKIconLabelsRenderable updating descriptorSet: 0x%016X", descriptorSet);
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
            GetLogger().fine("CVKIconLabelsRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity());
            
            for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
                GetLogger().info("CVKIconLabelsRenderable freeing hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
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
        // NodeLabel.vs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // Label.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // Label.fs
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
            pushConstantRange = VkPushConstantRange.calloc(1);
            pushConstantRange.get(0).stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            pushConstantRange.get(0).size(64);
            pushConstantRange.get(0).offset(0);

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
            hGlyphAtlasSampler != CVKGlyphTextureAtlas.GetInstance().GetAtlasSamplerHandle() ||
            hGlyphAtlasImageView != CVKGlyphTextureAtlas.GetInstance().GetAtlasImageViewHandle() ||
            hGlyphCoordinateBuffer != CVKGlyphTextureAtlas.GetInstance().GetCoordinateBufferHandle() ||
            hGlyphCoordinateBufferView != CVKGlyphTextureAtlas.GetInstance().GetCoordinateBufferViewHandle()) {
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
        
            // Vertex uniform buffer (camera guff)
            if (vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateVertexUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
            } else if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }

            // Geometry uniform buffer (projection, highlight colour)
            if (geometryUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateGeometryUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
            } else if (geometryUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateGeometryUniformBuffers(stack);
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
    
    @Override
    public void addGlyph(int glyphPosition, float x, float y) {
        vertices.add(new Vertex(glyphPosition, x, y, glyphContext.visibility,
                                glyphContext.nodeId, glyphContext.totalScale, glyphContext.labelNumber));
    }    
    
    @Override
    public void newLine(float width) {
        vertices.add(new Vertex(CVKGlyphTextureAtlas.BACKGROUND_GLYPH_INDEX, -width / 2.0f - 0.2f, 0.0f, glyphContext.visibility,
                                glyphContext.nodeId, glyphContext.totalScale, glyphContext.labelNumber));        
    }    
    
    private Vertex[] BuildVertexArray(final VisualAccess access, int first, int last) {
        vertices = new ArrayList<>();
        final int newVertexCount = (last - first) + 1;
        if (newVertexCount > 0) {         
            for (int pos = first; pos <= last; ++pos) {
                glyphContext.visibility = access.getVertexVisibility(pos);     
                
                // Top labels
                int totalScale = LabelUtilities.NRADIUS_TO_LABEL_UNITS;
                for (int label = 0; label < access.getTopLabelCount(); label++) {
                    glyphContext.nodeId      = pos;
                    glyphContext.totalScale  = totalScale;
                    glyphContext.labelNumber = label;
                    
                    final String text = access.getVertexTopLabelText(pos, label);
                    ArrayList<String> lines = LabelUtilities.splitTextIntoLines(text);
                    Collections.reverse(lines);
                    for (final String line : lines) {
                        CVKGlyphTextureAtlas.GetInstance().RenderTextAsLigatures(line, this);
                        totalScale += topLabelRowSizes.a[label];
                    }                    
                }
                
                // Bottom labels
                totalScale = LabelUtilities.NRADIUS_TO_LABEL_UNITS;
                for (int label = 0; label < access.getBottomLabelCount(); label++) {
                    glyphContext.nodeId      = pos;
                    glyphContext.totalScale  = -totalScale;
                    glyphContext.labelNumber = label;
                    
                    final String text = access.getVertexBottomLabelText(pos, label);
                    ArrayList<String> lines = LabelUtilities.splitTextIntoLines(text);
                    for (final String line : lines) {
                        CVKGlyphTextureAtlas.GetInstance().RenderTextAsLigatures(line, this);
                        totalScale += bottomLabelRowSizes.a[label];
                    }                    
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
                                                          "CVKIconLabelsRenderable.RebuildVertexStagingBuffer cvkVertexStagingBuffer");
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
    
    public CVKRenderUpdateTask TaskUpdateLabels(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        cvkVisualProcessor.GetLogger().fine("TaskUpdateLabels frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
        
        final boolean rebuildRequired = cvkVertexStagingBuffer == null || 
                                        access.getVertexCount() * Vertex.BYTES != cvkVertexStagingBuffer.GetBufferSize() || 
                                        change.isEmpty();
        final int changedVerticeRange[];
        final Vertex vertexArray[];
        if (rebuildRequired) {
            vertexArray = BuildVertexArray(access, 0, access.getVertexCount() - 1);
            changedVerticeRange = null;
        } else {
            changedVerticeRange = change.getRange();
            vertexArray = BuildVertexArray(access, changedVerticeRange[0], changedVerticeRange[1]);         
        }
        
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
    
    public CVKRenderUpdateTask TaskUpdateColours(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        cvkVisualProcessor.GetLogger().fine("TaskUpdateColours frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
                
        final int numTopLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getTopLabelCount());
        for (int i = 0; i < numTopLabels; i++) {
            topLabelColours[i].setR(access.getBottomLabelColor(i).getRed());
            topLabelColours[i].setG(access.getBottomLabelColor(i).getGreen());
            topLabelColours[i].setB(access.getBottomLabelColor(i).getBlue());
        }      
        
        final int numBottomLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getBottomLabelCount());
        for (int i = 0; i < numBottomLabels; i++) {
            bottomLabelColours[i].setR(access.getBottomLabelColor(i).getRed());
            bottomLabelColours[i].setG(access.getBottomLabelColor(i).getGreen());
            bottomLabelColours[i].setB(access.getBottomLabelColor(i).getBlue());            
        }           
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            for (int i = 0; i < numTopLabels; i++) {
                vertexUBO.labelTopInfo.setRow(topLabelColours[i], i);
            }
            for (int i = 0; i < numBottomLabels; i++) {
                vertexUBO.labelBottomInfo.setRow(bottomLabelColours[i], i);
            }            
            if (vertexUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexUBOState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };        
    }
    
    public CVKRenderUpdateTask TaskUpdateSizes(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        cvkVisualProcessor.GetLogger().fine("TaskUpdateSizes frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
                
        final int numTopLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getTopLabelCount());
        for (int i = 0; i < numTopLabels; i++) {
            topLabelRowSizes.a[i] = (int)(LabelUtilities.NRADIUS_TO_LABEL_UNITS * Math.min(access.getTopLabelSize(i), LabelUtilities.MAX_LABEL_SIZE));
        }      
        
        final int numBottomLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getBottomLabelCount());
        for (int i = 0; i < numBottomLabels; i++) {
            bottomLabelRowSizes.a[i] = (int)(LabelUtilities.NRADIUS_TO_LABEL_UNITS * Math.min(access.getBottomLabelSize(i), LabelUtilities.MAX_LABEL_SIZE));
        }           
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            for (int i = 0; i < numTopLabels; i++) {
                vertexUBO.labelTopInfo.set(i, 3, topLabelRowSizes.a[i]);
            }
            for (int i = 0; i < numBottomLabels; i++) {
                vertexUBO.labelBottomInfo.set(i, 3, bottomLabelRowSizes.a[i]);
            }            
            if (vertexUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexUBOState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };         
    }    
    
    public CVKRenderUpdateTask TaskSetBackgroundColor(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        cvkVisualProcessor.GetLogger().fine("TaskSetBackgroundColor frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
        final ConstellationColor colour = access.getBackgroundColor();
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            vertexUBO.backgroundColor.set(colour.getRed(), colour.getGreen(), colour.getBlue(), 1.0f);            
            if (vertexUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexUBOState(CVK_RESOURCE_NEEDS_UPDATE);
            }            
        };        
    }   
    
    public CVKRenderUpdateTask TaskSetHighlightColor(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        cvkVisualProcessor.GetLogger().fine("TaskSetHighlightColor frame %d: %d verts", cvkVisualProcessor.GetFrameNumber(), access.getVertexCount());
        final ConstellationColor colour = access.getHighlightColor();
            
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            geometryUBO.highlightColor.set(colour.getRed(), colour.getGreen(), colour.getBlue(), 1.0f);            
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
}
