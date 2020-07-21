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
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4i;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.LoadFileToDirectBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKImage;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_ERROR_TOO_MANY_OBJECTS;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;


public class CVKIconsRenderable extends CVKRenderable{
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    
    private static long hVertexShader = VK_NULL_HANDLE;
    private static long hGeometryShader = VK_NULL_HANDLE;
    private static long hFragmentShader = VK_NULL_HANDLE;
    private static long hDescriptorLayout = VK_NULL_HANDLE; 
    
    // TODO_TT: not really width....
    private static final int ICON_BUFFER_WIDTH = 4;
    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int XYZ_BUFFER_WIDTH = 8;
    

    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;
//    private CVKIconsRenderable.Vertex[] vertices = null;
    private float[] positions = null;
    
    private List<CVKBuffer> vertexBuffers = null;    
    private CVKImage xyzwTexture = null;
    private boolean recreateIcons = false;
    private ReentrantLock vertexLock = new ReentrantLock();
    
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int SIZEOF = 4 * Float.BYTES + 4 * Integer.BYTES;
        private static final int OFFSETOF_DATA = 4 * Float.BYTES;
        private static final int OFFSET_BKGCLR = 0;
        private static final int BINDING = 0;

        private Vector4f backgroundIconColour = new Vector4f();
        private Vector4i data = new Vector4i();
        
        public Vertex() {}

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
        
        public void CopyTo(ByteBuffer buffer) {
            buffer.putFloat(backgroundIconColour.a[0]);
            buffer.putFloat(backgroundIconColour.a[1]);
            buffer.putFloat(backgroundIconColour.a[2]);
            buffer.putFloat(backgroundIconColour.a[3]);
            buffer.putFloat(data.a[0]);
            buffer.putFloat(data.a[1]);
            buffer.putFloat(data.a[2]);
            buffer.putFloat(data.a[3]);              
        }
        
        private static void CopyTo(ByteBuffer buffer, Vertex[] vertices) {
            for (Vertex vertex : vertices) {  
                vertex.CopyTo(buffer);               
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

            // backgroundIconColor
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            posDescription.offset(OFFSETOF_DATA);

            // data
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SINT);
            colorDescription.offset(OFFSET_BKGCLR);

            return attributeDescriptions.rewind();
        }
    }    
    
    
    // TODO_TT: generalise this for all classes
    public static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try {
            ByteBuffer vsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.vs.spv");
            if (vsBytes.capacity() == 0) {
                throw new RuntimeException("Failed to load compiled/SimpleIcon.vs.spv");
            }
            ByteBuffer gsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.gs.spv");
            if (vsBytes.capacity() == 0) {
                throw new RuntimeException("Failed to load compiled/SimpleIcon.gs.spv");
            }            
            ByteBuffer fsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.fs.spv");
            if (vsBytes.capacity() == 0) {
                throw new RuntimeException("Failed to load compiled/SimpleIcon.fs.spv");
            }            
            
            hVertexShader = CVKShaderUtils.createShaderModule(vsBytes, cvkDevice.GetDevice());
            if (hVertexShader == VK_NULL_HANDLE) {
                throw new RuntimeException("Failed to create shader from VertexIcon.vs.spv bytes");
            }            
            hGeometryShader = CVKShaderUtils.createShaderModule(gsBytes, cvkDevice.GetDevice());
            if (hGeometryShader == VK_NULL_HANDLE) {
                throw new RuntimeException("Failed to create shader from VertexIcon.gs.spv bytes");
            }             
            hFragmentShader = CVKShaderUtils.createShaderModule(fsBytes, cvkDevice.GetDevice());            
            if (hFragmentShader == VK_NULL_HANDLE) {
                throw new RuntimeException("Failed to create shader from VertexIcon.fs.spv bytes");
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
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(6, stack);

            // 0: Vertex uniform buffer
            VkDescriptorSetLayoutBinding vertexUBDSLB = bindings.get(0);
            vertexUBDSLB.binding(0);
            vertexUBDSLB.descriptorCount(1);
            vertexUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            vertexUBDSLB.pImmutableSamplers(null);
            vertexUBDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            
            // 1: Vertex samplerBuffer
            VkDescriptorSetLayoutBinding vertexSamplerDSLB = bindings.get(1);
            vertexSamplerDSLB.binding(1);
            vertexSamplerDSLB.descriptorCount(1);
            vertexSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER);
            vertexSamplerDSLB.pImmutableSamplers(null);
            vertexSamplerDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);            
            
            // 2: Geometry uniform buffer
            VkDescriptorSetLayoutBinding geometryUBDSLB = bindings.get(2);
            geometryUBDSLB.binding(2);
            geometryUBDSLB.descriptorCount(1);
            geometryUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geometryUBDSLB.pImmutableSamplers(null);
            geometryUBDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);      
            
            // 3: Geometry isamplerBuffer
            VkDescriptorSetLayoutBinding geometrySamplerDSLB = bindings.get(3);
            geometrySamplerDSLB.binding(3);
            geometrySamplerDSLB.descriptorCount(1);
            geometrySamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER);
            geometrySamplerDSLB.pImmutableSamplers(null);
            geometrySamplerDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);               

            // 4: Fragment sampler2Darray
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

            ret = vkCreateDescriptorSetLayout(cvkDevice.GetDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
            }
        }        
        return ret;
    }    
            
    public CVKIconsRenderable(CVKVisualProcessor inParent) {
        parent = inParent;
    }
    
    public int Init() {
        int ret = VK_SUCCESS;
        //this.cvkDevice = cvkDevice;
        return ret;
    }
    
    @Override
    public int DeviceInitialised(CVKDevice cvkDevice) {
        this.cvkDevice = cvkDevice;
        return VK_SUCCESS;
    }      
    
//    private void SetIconInfo(final int pos, final VisualAccess access) {
//        CVKAssert(access != null);
//        CVKAssert(vertices != null);
//        CVKAssert(pos < vertices.length);
//        CVKAssert(vertices[pos] != null);
//        
//        final String foregroundIconName = access.getForegroundIcon(pos);
//        final String backgroundIconName = access.getBackgroundIcon(pos);
//        final int foregroundIconIndex = parent.GetTextureAtlas().AddIcon(foregroundIconName);
//        final int backgroundIconIndex = parent.GetTextureAtlas().AddIcon(backgroundIconName);
//
//        final String nWDecoratorName = access.getNWDecorator(pos);
//        final String sWDecoratorName = access.getSWDecorator(pos);
//        final String sEDecoratorName = access.getSEDecorator(pos);
//        final String nEDecoratorName = access.getNEDecorator(pos);
//        final int nWDecoratorIndex = nWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
//        final int sWDecoratorIndex = sWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
//        final int sEDecoratorIndex = sEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
//        final int nEDecoratorIndex = nEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
//
////        if (nWDecoratorIndex > MAX_ICON_INDEX || sWDecoratorIndex > MAX_ICON_INDEX || sEDecoratorIndex > MAX_ICON_INDEX || nEDecoratorIndex > MAX_ICON_INDEX) {
////            final String msg = "Decorator icon index is too large";
////            throw new IllegalStateException(msg);
////        }
////        if (foregroundIconIndex > MAX_ICON_INDEX) {
////            final String msg = String.format("Too many foreground icons: %d > %d", foregroundIconIndex, MAX_ICON_INDEX);
////            throw new IllegalStateException(msg);
////        }
////        if (backgroundIconIndex > MAX_ICON_INDEX) {
////            final String msg = String.format("Too many background icons: %d > %d", backgroundIconIndex, MAX_ICON_INDEX);
////            throw new IllegalStateException(msg);
////        }
//
//        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
//        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
//        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);
//
//        vertices[pos].SetIconData(icons, decoratorsWest, decoratorsEast, access.getVertexId(pos));
//    }
    
    private void SetIconInfo(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(vertex != null);
        CVKAssert(pos < access.getVertexCount());
        
        final String foregroundIconName = access.getForegroundIcon(pos);
        final String backgroundIconName = access.getBackgroundIcon(pos);
        final int foregroundIconIndex = parent.GetTextureAtlas().AddIcon(foregroundIconName);
        final int backgroundIconIndex = parent.GetTextureAtlas().AddIcon(backgroundIconName);

        final String nWDecoratorName = access.getNWDecorator(pos);
        final String sWDecoratorName = access.getSWDecorator(pos);
        final String sEDecoratorName = access.getSEDecorator(pos);
        final String nEDecoratorName = access.getNEDecorator(pos);
        final int nWDecoratorIndex = nWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sWDecoratorIndex = sWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sEDecoratorIndex = sEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int nEDecoratorIndex = nEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);

        vertex.SetIconData(icons, decoratorsWest, decoratorsEast, access.getVertexId(pos));
    }    

//    private int SetColorInfo(final int pos, final VisualAccess access) {
//        CVKAssert(access != null);
//        CVKAssert(vertices != null);
//        CVKAssert(pos < vertices.length);
//        CVKAssert(vertices[pos] != null);
//        
//        vertices[pos].SetBackgroundIconColour(access.getVertexColor(pos));
//        vertices[pos].SetVertexVisibility(access.getVertexVisibility(pos));
//        return pos;
//    }
    
    private void SetColorInfo(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(vertex != null);
        CVKAssert(pos < access.getVertexCount());
        
        vertex.SetBackgroundIconColour(access.getVertexColor(pos));
        vertex.SetVertexVisibility(access.getVertexVisibility(pos));
    }    
    
    // TODO_TT: find out more about the second coord
    // TODO_TT: see if anything ever uses the radius   - yes the blaze batcher 
    private void SetXYZWInfo(final int pos, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(positions != null);
        CVKAssert(((pos + 1) * XYZ_BUFFER_WIDTH) <= positions.length);        
        
        int offset = XYZ_BUFFER_WIDTH * pos;
        positions[offset]     = access.getX(pos);
        positions[offset + 1] = access.getY(pos);
        positions[offset + 2] = access.getZ(pos);
        positions[offset + 3] = access.getRadius(pos);
        positions[offset + 4] = access.getX2(pos);
        positions[offset + 5] = access.getY2(pos);
        positions[offset + 6] = access.getZ2(pos);
        positions[offset + 7] = access.getRadius(pos);  
    }
    
    public CVKRenderableUpdateTask TaskCreateIcons(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        vertexCount = access.getVertexCount();
        try {
            // Vertices are modified by the event thread
            vertexLock.lock(); 
            
            // Destroy old staging buffer if it exists
            if (cvkVertexStagingBuffer != null) {
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            }                       
            
//            vertices = null;
            positions = null;
            if (vertexCount > 0) {
                positions = new float[XYZ_BUFFER_WIDTH * vertexCount];
                int bufferSizeBytes = CVKIconsRenderable.Vertex.SIZEOF * vertexCount;
                cvkVertexStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                          bufferSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                ByteBuffer pMemory = cvkVertexStagingBuffer.StartWrite(0, bufferSizeBytes);
                CVKIconsRenderable.Vertex vertex = new CVKIconsRenderable.Vertex();
                for (int pos = 0; pos < vertexCount; pos++) {
                    SetColorInfo(pos, vertex, access);
                    SetIconInfo(pos, vertex, access);
                    vertex.CopyTo(pMemory);
                    SetXYZWInfo(pos, access);
                }
                int memPos = pMemory.position();
                CVKAssert(memPos == bufferSizeBytes);
                cvkVertexStagingBuffer.EndWrite();
                pMemory = null; // now unmapped, do not use
                
                
//                vertices = new CVKIconsRenderable.Vertex[vertexCount];
//                positions = new float[XYZ_BUFFER_WIDTH * vertexCount];
//                for (int pos = 0; pos < vertexCount; pos++) {
//                    vertices[pos] = new CVKIconsRenderable.Vertex();
//                    SetColorInfo(pos, access);
//                    SetIconInfo(pos, access);
//                    SetXYZWInfo(pos, access);
//                }
            }
        } finally {
            vertexLock.unlock();
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (cvkSwapChain, imageIndex) -> {
            // We can't update the xyzw texture here as it is needed to render each image
            // in the swap chain.  If we recreate it for image 1 it will be likely be in
            // flight for presenting image 0.  The shared resource recreation path is
            // synchronised for all images so we need to do it there.
            recreateIcons = true;
        };
    }    
    
    // TODO_TT: do we need this if we are destroying in create?
    public CVKRenderableUpdateTask TaskDestroyIcons() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//

        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (cvkSwapChain, imageIndex) -> {
            VerifyInRenderThread();

//            if (colorBuffer != null) { MemoryUtil.memFree(colorBuffer); colorBuffer = null; }
//            if (iconBuffer != null) { MemoryUtil.memFree(iconBuffer); iconBuffer = null; }
//            if (xyzBuffer != null) { MemoryUtil.memFree(xyzBuffer); xyzBuffer = null; }
        };        
    }
    
    private int CreateVertexBuffers(CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
    
        int imageCount = cvkSwapChain.GetImageCount();               
        vertexBuffers = new ArrayList<>();
        
        //TODO_TT: most if not all of Constellation's vertex buffers won't change after creation
        // so they should probably be allocated as VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT and staged
        // to once to fill them (staging buffer this is host visible then copied to the device local)
        for (int i = 0; i < imageCount; ++i) {   
            CVKBuffer cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                         cvkVertexStagingBuffer.GetBufferSize(),
                                                         VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                                                         VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            cvkVertexBuffer.DEBUGNAME = String.format("CVKIconsRenderable cvkVertexBuffer %d", i);
            vertexBuffers.add(cvkVertexBuffer);        
        }
        
        // Populate them with some values
        UpdateVertexBuffers();
        
        return ret;  
    }    
    
    private int UpdateVertexBuffers() {
        VerifyInRenderThread();
        int ret = VK_SUCCESS;
        
        try {
            vertexLock.lock();
            for (int i = 0; i < vertexBuffers.size(); ++i) {   
                CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
                cvkVertexBuffer.CopyFrom(cvkVertexStagingBuffer);
            }
        } finally {
            vertexLock.unlock();
        }        
        
//        try (MemoryStack stack = stackPush()) {                
//            int size = vertices.length * CVKIconsRenderable.Vertex.SIZEOF;
//            for (int i = 0; i < vertexBuffers.size(); ++i) {   
//                CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
//                PointerBuffer data = stack.mallocPointer(1);
//                vkMapMemory(cvkDevice.GetDevice(), cvkVertexBuffer.GetMemoryBufferHandle(), 0, size, 0, data);
//                {
//                    CVKIconsRenderable.Vertex.CopyTo(data.getByteBuffer(0, size), vertices);
//                }
//                vkUnmapMemory(cvkDevice.GetDevice(), cvkVertexBuffer.GetMemoryBufferHandle());
//            }
//        } finally {
//            vertexLock.unlock();
//        }
        
        return ret;         
    }    
    
    private int CreateXYZWTexture(CVKSwapChain cvkSwapChain) {
        CVKAssert(xyzwTexture == null);
        VerifyInRenderThread();        
        int ret = VK_SUCCESS;
        
        // Create a layered 1D image to hold the position data.  While it would be
        // easier and faster to read this data from a uniform buffer they are generally
        // limited to 16KB, possibly smaller on some devices.  A 1D texture will likely
        // have a similar size restriction per layer but as we can have many layers we
        // should be able to fit all the points we need.        
        final int numberOfTexels = vertexCount * 2; //alternate positions
        final int maxNumberOfTexels = cvkDevice.GetMax1DImageWidth() * cvkDevice.GetMaxImageLayers();
        
        if (numberOfTexels > maxNumberOfTexels) {
            CVKLOGGER.severe(String.format("CVKIconsRenderable.CreateXYZWTexture cannot allocate %d vertex points, maxium this device supports is %d", numberOfTexels, maxNumberOfTexels));
            return VK_ERROR_TOO_MANY_OBJECTS;
        }
        
        final int width = Math.min(numberOfTexels, cvkDevice.GetMax1DImageWidth());
        final int numberOfLayers = (numberOfTexels/cvkDevice.GetMax1DImageWidth()) + 1;
        CVKAssert(width <= cvkDevice.GetMax1DImageWidth());
        CVKAssert(numberOfLayers <= cvkDevice.GetMaxImageLayers());
        
        
        xyzwTexture = CVKImage.Create(cvkDevice,
                                      width,
                                      1,
                                      numberOfLayers,
                                      VK_FORMAT_R32G32B32A32_SFLOAT,
                                      VK_IMAGE_TILING_LINEAR,
                                      VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                                      VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                      VK_IMAGE_ASPECT_COLOR_BIT);
        
        ret = UpdateXYZWTexture();
        
        return ret;
    }
    
    private int UpdateXYZWTexture() {
        int ret;
        
        try (MemoryStack stack = stackPush()) {
           // Command to copy pixels and transition formats
            CVKCommandBuffer cvkCopyCmd = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            cvkCopyCmd.DEBUGNAME = "CVKIconsRenderable cvkCopyCmd";
            ret = cvkCopyCmd.Begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            if (VkFailed(ret)) { return ret; }
            
            // Transition image from undefined to transfer destination optimal
            ret = xyzwTexture.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            if (VkFailed(ret)) { return ret; }         

            /*int numIcons = icons.size();
            List<CVKBuffer> iconStagingBuffers = new ArrayList<>(numIcons);
            
            for (int iIcon = 0; iIcon < numIcons; ++iIcon) {
                IndexedConstellationIcon el = icons.get(iIcon);
                BufferedImage iconImage = el.icon.buildBufferedImage();     
               
                CVKBuffer cvkStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                              ICON_SIZE_BYTES, 
                                                              VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                              VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);  
                cvkStagingBuffer.DEBUGNAME = String.format("CVKIconTextureAtlas cvkStagingBuffer %d", iIcon);
                
               // Convert the buffered image if its not in our desired state.
                if (TYPE_4BYTE_ABGR != iconImage.getType()) {
                    BufferedImage convertedImg = new BufferedImage(iconImage.getWidth(), iconImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    convertedImg.getGraphics().drawImage(iconImage, 0, 0, null);
                    iconImage = convertedImg;
                }     
                int width = iconImage.getWidth();
                int height = iconImage.getHeight();
                assert(width <= ICON_WIDTH);
                assert(height <= ICON_HEIGHT);
                
                // Get pixel data into a direct buffer
                ByteBuffer pixels = ByteBuffer.wrap(((DataBufferByte) iconImage.getRaster().getDataBuffer()).getData());
                if (debugging) {
                    if (IsEmpty(pixels) && el.index != TRANSPARENT_ICON_INDEX) {
                        CVKLOGGER.warning(String.format("Icon %d is empty", el.index));
                    }
                }
                
                // To save us having to swizzle with ever render, do it now (ABGR->RGBA, AWT what were you thinking?)
                for (int i = 0; i < pixels.capacity(); i+=4) {
                    byte a = pixels.get(i);
                    byte b = pixels.get(i+1);
                    
                    // swap alpha and red
                    pixels.put(i, pixels.get(i+3));
                    pixels.put(i+3, a);
                    
                    // swap blue and green
                    pixels.put(i+1, pixels.get(i+2));
                    pixels.put(i+2, b);             
                }
                             
                // Copy pixels, note for undersized icons we need extra offsets to pad the top and sides                    
                if (width == ICON_WIDTH && height == ICON_HEIGHT) {
                    assert(pixels.capacity() == ICON_SIZE_BYTES);
                    cvkStagingBuffer.CopyFrom(pixels, 0, 0, ICON_SIZE_BYTES);
                } else {
                    // Zero this buffer so undersized icons are padded with transparent pixels
                    cvkStagingBuffer.ZeroMemory();     
                    
                    // Offsets to centre the icon are in pixels
                    int colOffset = (ICON_WIDTH - width) / 2;
                    int rowOffset = (ICON_HEIGHT - height) / 2;
                    
                    // Adjust the start position to the right row
                    for (int iRow = 0; iRow < height; ++iRow) {       
                        // offset to the start of this row
                        int writePos = (iRow + rowOffset) * ICON_WIDTH * ICON_COMPONENTS;
                        assert(((iRow + rowOffset + 1) * ICON_WIDTH * ICON_COMPONENTS) <= ICON_SIZE_BYTES);
                        
                        // offset from the start of the row to the start of the icon
                        writePos += colOffset * ICON_COMPONENTS;
                        int readPos = iRow * width * ICON_COMPONENTS;
                        cvkStagingBuffer.CopyFrom(pixels, writePos, readPos, width * ICON_COMPONENTS);
                    }
                }
                iconStagingBuffers.add(cvkStagingBuffer);
                                              
                // Calculate offset into staging buffer for the current image layer
                Vector3i texIndices = IndexToTextureIndices(el.index);

                // Setup a buffer image copy structure for the current image layer
                VkBufferImageCopy.Buffer copyLayerBuffer = VkBufferImageCopy.callocStack(1, stack);
                VkBufferImageCopy copyLayer = copyLayerBuffer.get(0);
                copyLayer.bufferOffset(0);
                copyLayer.bufferRowLength(ICON_WIDTH);//0);    // Tightly packed
                copyLayer.bufferImageHeight(ICON_HEIGHT);//0);  // Tightly packed
                copyLayer.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                copyLayer.imageSubresource().mipLevel(0);
                copyLayer.imageSubresource().baseArrayLayer(texIndices.getW());
                copyLayer.imageSubresource().layerCount(1);
                copyLayer.imageOffset().set(texIndices.getU() * ICON_WIDTH,
                                            texIndices.getV() * ICON_HEIGHT, 
                                            0);
                copyLayer.imageExtent(VkExtent3D.callocStack(stack).set(ICON_WIDTH, ICON_HEIGHT, 1));
//                CVKLOGGER.info(String.format("Icon %d: (%d, %d)",
//                        el.index, texIndices.getU() * ICON_WIDTH, texIndices.getV() * ICON_HEIGHT));
                
                // Copy staging buffer to atlas texture
                vkCmdCopyBufferToImage(cvkCopyCmd.GetVKCommandBuffer(),
                                       cvkStagingBuffer.GetBufferHandle(),
                                       cvkAtlasImage.GetImageHandle(),
                                       VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                                       copyLayerBuffer);                               
            }
            
            // Now the image is populated, transition it for reading
            ret = cvkAtlasImage.Transition(cvkCopyCmd, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            checkVKret(ret);      
            
            // Ok nothing has actually happened yet, time to execute the transitions and copy
            ret = cvkCopyCmd.EndAndSubmit();
            checkVKret(ret);    
            cvkCopyCmd.Destroy();
            
            // We've finished with the staging buffer
            iconStagingBuffers.forEach(el -> {el.Destroy();});
            
            // Create a sampler to match the image.  Note the sampler allows us to sample
            // an image but isn't tied to a specific image, note the lack of image or 
            // imageview parameters below.
            VkSamplerCreateInfo vkSamplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);                        
            vkSamplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            vkSamplerCreateInfo.maxAnisotropy(1.0f);
            vkSamplerCreateInfo.magFilter(VK_FILTER_LINEAR);
            vkSamplerCreateInfo.minFilter(VK_FILTER_LINEAR);
            vkSamplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            vkSamplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            vkSamplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            vkSamplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            vkSamplerCreateInfo.mipLodBias(0.0f);
            vkSamplerCreateInfo.maxAnisotropy(8);
            vkSamplerCreateInfo.compareOp(VK_COMPARE_OP_NEVER);
            vkSamplerCreateInfo.minLod(0.0f);
            vkSamplerCreateInfo.maxLod(0.0f);
            vkSamplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
            
            LongBuffer pTextureSampler = stack.mallocLong(1);
            ret = vkCreateSampler(cvkDevice.GetDevice(), vkSamplerCreateInfo, null, pTextureSampler);
            checkVKret(ret);
            hAtlasSampler = pTextureSampler.get(0);
            assert(hAtlasSampler != VK_NULL_HANDLE);   */            
        }
        
        return ret;
    }
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }
    
    private void DestroyXYZWTexture() {
        if (xyzwTexture != null) {
            xyzwTexture.Destroy();
            xyzwTexture = null;
        }
    }
    
    @Override
    public void Destroy() {
        DestroyVertexBuffers();
        DestroyXYZWTexture();
//        DestroyUniformBuffers();
//        DestroyDescriptorSets();
//        DestroyCommandBuffers();
//        DestroyPipelines();
//        DestroyPipelineLayouts();
//        DestroyCommandBuffers();  
//        
//        CVKAssert(pipelines == null);
//        CVKAssert(pipelineLayouts == null);
//        CVKAssert(pDescriptorSets == null);
//        CVKAssert(vertexUniformBuffers == null);
//        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(vertexBuffers == null);
//        CVKAssert(commandBuffers == null);     
    }
    
    @Override
    public boolean SharedResourcesNeedUpdating() { return recreateIcons; }
    
    @Override
    public int RecreateSharedResources(CVKSwapChain cvkSwapChain) { 
        int ret;
        VerifyInRenderThread();
        
        DestroyVertexBuffers();
        DestroyXYZWTexture();
        
        ret = CreateVertexBuffers(cvkSwapChain);
        if (VkFailed(ret)) { return ret; }
        ret = CreateXYZWTexture(cvkSwapChain);
        if (VkFailed(ret)) { return ret; }
        if (VkFailed(ret)) { return ret; }

        // We don't need these now they've been copied into the relavant buffers
//        vertices = null;
        positions = null;
        
        recreateIcons = false;
        
        return ret;
    }
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return null; }   
    @Override
    public int SwapChainRecreated(CVKSwapChain cvkSwapChain) { return VK_SUCCESS;}
    @Override
    public int DisplayUpdate(CVKSwapChain cvkSwapChain, int frameIndex) { return VK_SUCCESS;}
    @Override
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[], int descriptorSetCount) {}     
    @Override
    public int RecordCommandBuffer(CVKSwapChain cvkSwapChain, VkCommandBufferInheritanceInfo inheritanceInfo, int index) { return VK_SUCCESS;}
    @Override
    public int GetVertexCount() { return 0; }
}
