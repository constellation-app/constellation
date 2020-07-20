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
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.LoadFileToDirectBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;


public class CVKIconsRenderable extends CVKRenderable{
    CVKDevice cvkDevice = null;
    
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    
    private static long hVertexShader = VK_NULL_HANDLE;
    private static long hGeometryShader = VK_NULL_HANDLE;
    private static long hFragmentShader = VK_NULL_HANDLE;
    private static long hDescriptorLayout = VK_NULL_HANDLE; 
    
    // TODO_TT: not really width....
    private static final int ICON_BUFFER_WIDTH = 4;
    private static final int COLOR_BUFFER_WIDTH = 4;
    
    
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
    
    private int bufferIconInfo(final int pos, final IntBuffer iconBuffer, final VisualAccess access) {
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

//        if (nWDecoratorIndex > MAX_ICON_INDEX || sWDecoratorIndex > MAX_ICON_INDEX || sEDecoratorIndex > MAX_ICON_INDEX || nEDecoratorIndex > MAX_ICON_INDEX) {
//            final String msg = "Decorator icon index is too large";
//            throw new IllegalStateException(msg);
//        }
//        if (foregroundIconIndex > MAX_ICON_INDEX) {
//            final String msg = String.format("Too many foreground icons: %d > %d", foregroundIconIndex, MAX_ICON_INDEX);
//            throw new IllegalStateException(msg);
//        }
//        if (backgroundIconIndex > MAX_ICON_INDEX) {
//            final String msg = String.format("Too many background icons: %d > %d", backgroundIconIndex, MAX_ICON_INDEX);
//            throw new IllegalStateException(msg);
//        }

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);

        iconBuffer.put(icons);
        iconBuffer.put(decoratorsWest);
        iconBuffer.put(decoratorsEast);
        iconBuffer.put(access.getVertexId(pos));
        return pos;
    }

    private int bufferColorInfo(final int pos, final FloatBuffer colorBuffer, final VisualAccess access) {
        ConstellationColor color = access.getVertexColor(pos);
        colorBuffer.put(color.getRed());
        colorBuffer.put(color.getGreen());
        colorBuffer.put(color.getBlue());
        colorBuffer.put(access.getVertexVisibility(pos));
        return pos;
    }
    
    public CVKRenderableUpdateTask TaskCreateIcons(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        final int numVertices = access.getVertexCount();
        if (numVertices > 0) {
            final FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(COLOR_BUFFER_WIDTH * numVertices);
            final IntBuffer iconBuffer = MemoryUtil.memAllocInt(ICON_BUFFER_WIDTH * numVertices);
            for (int pos = 0; pos < numVertices; pos++) {
                bufferColorInfo(pos, colorBuffer, access);
                bufferIconInfo(pos, iconBuffer, access);
            }
            colorBuffer.flip();
            iconBuffer.flip();
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (cvkSwapChain, imageIndex) -> {
            VerifyInRenderThread();
            if (numVertices > 0) {

            }
        };
    }    
    
    public CVKRenderableUpdateTask TaskDestroyIcons() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//

        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.DisplayUpdate) ===//
        return (cvkSwapChain, imageIndex) -> {
            VerifyInRenderThread();

        };        
    }
    
    @Override
    public void Destroy() {
//        DestroyVertexBuffers();
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
//        CVKAssert(vertexBuffers == null);
//        CVKAssert(commandBuffers == null);     
    }
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return null; }   
    @Override
    public int SwapChainRecreated(CVKSwapChain cvkSwapChain) { return VK_SUCCESS;}
    @Override
    public int DisplayUpdate(CVKSwapChain cvkSwapChain, int frameIndex) { return VK_SUCCESS;}
    @Override
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[]) {}     
    @Override
    public int RecordCommandBuffer(CVKSwapChain cvkSwapChain, VkCommandBufferInheritanceInfo inheritanceInfo, int index) { return VK_SUCCESS;}
    @Override
    public int GetVertexCount() { return 0; }
    @Override
    public int DeviceInitialised(CVKDevice cvkDevice) { return VK_SUCCESS;}
}
