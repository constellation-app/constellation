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

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.AutoDrawable;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKScene;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.VkSucceeded;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKFPSRenderable.hDescriptorLayout;
import java.nio.LongBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;


public class CVKAxesRenderable implements CVKRenderable {
    protected final CVKScene scene;
    
    public CVKAxesRenderable(CVKScene inScene) {
        scene = inScene;
    }
    
    @Override
    public int getPriority() { if (true) throw new UnsupportedOperationException(""); else return 0; }
    @Override
    public void dispose(final AutoDrawable drawable) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void init(final AutoDrawable drawable) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void reshape(final int x, final int y, final int width, final int height) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void update(final AutoDrawable drawable) { throw new UnsupportedOperationException("Not yet implemented"); }
    @Override
    public void display(final AutoDrawable drawable, final Matrix44f pMatrix) { throw new UnsupportedOperationException("Not yet implemented"); }
    
    
    public int CreatePipeline(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {
            
        }
        return ret;
    }
    
    
    public int DestroyPipeline(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {
            
        }
        return ret;
    }
    
    
    @Override
    public int SwapChainRezied(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        int ret = DestroyPipeline(cvkDevice, cvkSwapChain);
        if (VkSucceeded(ret)) {
            ret = CreatePipeline(cvkDevice, cvkSwapChain);
        }
        return ret;
    }
    
    
    public static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        // load shader (can probably be done earlier)
        return ret;
    }
    
    @Override
    public int DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
        return VK_SUCCESS;
    }    
    
    @Override
    public void IncrementDescriptorTypeRequirements(int descriptorTypeCounts[]) {
        assert(descriptorTypeCounts.length == (VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT + 1));
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++descriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
    }    
    
    
    public static int CreateDescriptorLayout(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try(MemoryStack stack = stackPush()) {
            /*
            Vertex shader needs a uniform buffer.
            Geometry shader needs a different uniform buffer.
            Fragment shader needs a sampler2Darray
            */

//            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(3, stack);
//
//            VkDescriptorSetLayoutBinding vertexUBOLayout = bindings.get(0);
//            vertexUBOLayout.binding(0);
//            vertexUBOLayout.descriptorCount(1);
//            vertexUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//            vertexUBOLayout.pImmutableSamplers(null);
//            vertexUBOLayout.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
//            
//            VkDescriptorSetLayoutBinding geomUBOLayout = bindings.get(1);
//            geomUBOLayout.binding(1);
//            geomUBOLayout.descriptorCount(1);
//            geomUBOLayout.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//            geomUBOLayout.pImmutableSamplers(null);
//            geomUBOLayout.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);            
//
//            VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(2);
//            samplerLayoutBinding.binding(2);
//            samplerLayoutBinding.descriptorCount(1);
//            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
//            samplerLayoutBinding.pImmutableSamplers(null);
//            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
//
//            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
//            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
//            layoutInfo.pBindings(bindings);
//
//            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
//
//            ret = vkCreateDescriptorSetLayout(cvkDevice.GetDevice(), layoutInfo, null, pDescriptorSetLayout);
//            if (VkSucceeded(ret)) {
//                hDescriptorLayout = pDescriptorSetLayout.get(0);
//            }
        }        
        return ret;
    }    
}
