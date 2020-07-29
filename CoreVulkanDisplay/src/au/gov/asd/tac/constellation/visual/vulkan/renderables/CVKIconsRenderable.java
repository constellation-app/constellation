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
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.LoadFileToDirectBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_ERROR_TOO_MANY_OBJECTS;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;
import static org.lwjgl.vulkan.VK10.vkCreateBufferView;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyBufferView;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;


public class CVKIconsRenderable extends CVKRenderable{
    // Static so we recreate descriptor layouts and shaders for each graph
    private static boolean staticInitialised = false;
    
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    
    private static long hVertexShader = VK_NULL_HANDLE;
    private static long hGeometryShader = VK_NULL_HANDLE;
    private static long hFragmentShader = VK_NULL_HANDLE;
    private long hDescriptorLayout = VK_NULL_HANDLE; 
    
    // TODO_TT: not really width....
    private static final int ICON_BUFFER_WIDTH = 4;
    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int XYZ_BUFFER_WIDTH = 8;
    

    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;
    private CVKBuffer cvkXYZWStagingBuffer = null;
    private CVKBuffer cvkXYZWTexelBuffer = null;
    private long hXYZWBufferView = VK_NULL_HANDLE;
    private List<Long> pipelines = null;
    private List<Long> pipelineLayouts = null;    
    private List<CVKBuffer> vertexBuffers = null;    
    private boolean recreateIcons = false;
    private ReentrantLock vertexLock = new ReentrantLock();
    
    
    // ========================> Classes <======================== \\
    
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
                      
    
    // ========================> Static init <======================== \\
    
    private static int LoadShaders(CVKDevice cvkDevice) {
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

    public static int StaticInitialise(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        if (!staticInitialised) {
            ret = LoadShaders(cvkDevice);
            if (VkFailed(ret)) { return ret; }            
            staticInitialised = true;
        }
        return ret;
    }    
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKIconsRenderable(CVKVisualProcessor inParent) {
        parent = inParent;
    }  
    
    @Override
    public int Initialise(CVKDevice cvkDevice) {
        this.cvkDevice = cvkDevice;
        return CreateDescriptorLayout(cvkDevice);
    }        
    
    @Override
    public void Destroy() {
        DestroyVertexBuffers();
        DestroyXYZWBuffer();
        DestroyUniformBuffers();
        DestroyDescriptorSets();
        DestroyCommandBuffers();
        DestroyPipelines();
        DestroyPipelineLayouts();
        DestroyCommandBuffers();  
        
        CVKAssert(pipelines == null);
        CVKAssert(pipelineLayouts == null);
        CVKAssert(cvkXYZWTexelBuffer == null);
        CVKAssert(hXYZWBufferView == VK_NULL_HANDLE);        
//        CVKAssert(pDescriptorSets == null);
//        CVKAssert(vertexUniformBuffers == null);
//        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(vertexBuffers == null);
//        CVKAssert(commandBuffers == null);     
    }
    
       
    // ========================> Swap chain <======================== \\
    
    @Override
    public int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines != null && swapChainImageCountChanged) {  
            DestroyVertexBuffers();
            DestroyUniformBuffers();
            DestroyDescriptorSets();
            DestroyCommandBuffers();            
            DestroyPipelineLayouts();
            DestroyCommandBuffers();   
            DestroyPipelines();
            
            swapChainImageCountChanged = true;
        } else {
            DestroyPipelines();
        }
        
        return VK_SUCCESS; 
    }    
    
    private int CreateSwapChainResources() { 
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkDescriptorPool != null);
        int ret;
        
        try (MemoryStack stack = stackPush()) {  
            
            // We only need to recreate these resources if the number of images in 
            // the swapchain changes or if this is the first call after the initial
            // swapchain is created.
            if (swapChainImageCountChanged) {                               
                ret = CreateUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }

                ret = CreateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; } 

                ret = CreateVertexBuffers();
                if (VkFailed(ret)) { return ret; }   

                ret = CreateCommandBuffers();
                if (VkFailed(ret)) { return ret; }            

                ret = CreatePipelines();
                if (VkFailed(ret)) { return ret; }        
                
            } else {
                
                // This is the resize path, image count is unchanged.  We need to recreate
                // pipelines as Vulkan doesn't have a good mechanism to update them and as
                // they define the viewport and scissor rect they are now out of date.  We
                // also need to update the uniform buffer as a new image size will mean a
                // different position for our FPS.  After updating the uniform buffers we
                // need to update the descriptor sets that bind the uniform buffers as well.                  
                ret = CreatePipelines();
                if (VkFailed(ret)) { return ret; }                           

                ret = UpdateUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
                
                ret = UpdateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; }              
            }
        }
        
        swapChainImageCountChanged = false;
        swapChainResourcesDirty = false;
        
        return VK_SUCCESS;
    }    
    
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffers() {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
    
        // We can only create vertex buffers if we have something to put in them
        if (vertexCount > 0) {
            int imageCount = cvkSwapChain.GetImageCount();               
            vertexBuffers = new ArrayList<>();
            
            int xyzwBufferSizeBytes = XYZ_BUFFER_WIDTH * vertexCount * Float.BYTES;

            for (int i = 0; i < imageCount; ++i) {   
                CVKBuffer cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                             xyzwBufferSizeBytes,
                                                             VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
                cvkVertexBuffer.DEBUGNAME = String.format("CVKIconsRenderable cvkVertexBuffer %d", i);
                vertexBuffers.add(cvkVertexBuffer);        
            }

            // Populate them with some values
            UpdateVertexBuffers();
        }
        
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
        
        return ret;         
    }  
    
    @Override
    public int GetVertexCount() { return 0; }      
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }    
    
    
    // ========================> Texel buffer <======================== \\
    
    private int CreateXYZWBuffer() {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkXYZWTexelBuffer == null);
        VerifyInRenderThread();        
        int ret = VK_SUCCESS;
        
        // Create a layered 1D image to hold the position data.  While it would be
        // easier and faster to read this data from a uniform buffer they are generally
        // limited to 16KB, possibly smaller on some devices.  A 1D texture will likely
        // have a similar size restriction per layer but as we can have many layers we
        // should be able to fit all the points we need.        
        final long numberOfTexels = vertexCount * 2; //alternate positions
        final long maxNumberOfTexels = cvkDevice.GetMaxTexelBufferElements();//cvkDevice.GetMax1DImageWidth() * cvkDevice.GetMaxImageLayers();
        
        // Physical device limits can return a max texel buffer elements of -1 which 
        // we can only assume means there is no discrete limit (haven't been able to
        // find clarification in the spec).
        if ((maxNumberOfTexels > 0) && (numberOfTexels > maxNumberOfTexels)) {
            CVKLOGGER.severe(String.format("CVKIconsRenderable.CreateXYZWTexture cannot allocate %d vertex points, maxium this device supports is %d", numberOfTexels, maxNumberOfTexels));
            return VK_ERROR_TOO_MANY_OBJECTS;
        }
        
        cvkXYZWTexelBuffer = CVKBuffer.Create(cvkDevice, 
                                              cvkXYZWStagingBuffer.GetBufferSize(), 
                                              VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                              VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        cvkXYZWStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkXYZWStagingBuffer";       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R32G32B32A32_SFLOAT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkXYZWTexelBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(cvkDevice.GetDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hXYZWBufferView = pBufferView.get(0);
        }       
        
        ret = UpdateXYZWBuffer();
        if (VkFailed(ret)) { return ret; }
             
        return ret;
    }
    
    private int UpdateXYZWBuffer() {
        int ret;
        
        ret = cvkXYZWTexelBuffer.CopyFrom(cvkXYZWStagingBuffer);       
        cvkXYZWStagingBuffer.Destroy();
        cvkXYZWStagingBuffer = null;
        
        return ret;                               
    }   
    
    private void DestroyXYZWBuffer() {
        if (cvkXYZWTexelBuffer != null) {
            cvkXYZWTexelBuffer.Destroy();
            cvkXYZWTexelBuffer = null;
        }   
        if (hXYZWBufferView != VK_NULL_HANDLE) {
            vkDestroyBufferView(cvkDevice.GetDevice(), hXYZWBufferView, null);
            hXYZWBufferView = VK_NULL_HANDLE;
        }
    }
    
    
    // ========================> Uniform buffers <======================== \\
    
    private int CreateUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int imageCount = cvkSwapChain.GetImageCount();        

//        vertexUniformBuffers = new ArrayList<>();
//        geometryUniformBuffers = new ArrayList<>();        
//        for (int i = 0; i < imageCount; ++i) {   
//            CVKBuffer vertUniformBuffer = CVKBuffer.Create(cvkDevice, 
//                                                          CVKFPSRenderable.SIZEOF,
//                                                          VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
//                                                          VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
//            vertUniformBuffer.DEBUGNAME = String.format("CVKFPSRenderable vertUniformBuffer %d", i);   
//            vertexUniformBuffers.add(vertUniformBuffer);            
//            
//            CVKBuffer geomUniformBuffer = CVKBuffer.Create(cvkDevice, 
//                                                          CVKFPSRenderable.SIZEOF,
//                                                          VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
//                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
//            geomUniformBuffer.DEBUGNAME = String.format("CVKFPSRenderable geomUniformBuffer %d", i);                                    
//            geometryUniformBuffers.add(geomUniformBuffer);            
//        }
        
        return UpdateUniformBuffers(stack);                
    }
        
    private int UpdateUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
     
//        // TODO_TT: investigate a frames in flight < imageCount approach
//        int imageCount = cvkSwapChain.GetImageCount();        
//        
//        // LIFTED FROM FPSRenderable.reshape(...)
//        //TT: the logic here seems to be the FPS text needs to be 50 pixels from the 
//        // edges, the calculation of dx and dy implies that the viewport is 
//        //-width/2, -height/2, width/2, height/2
//        
//        // whenever the drawable shape changes, recalculate the place where the fps is drawn
//        
//        // This is a GL viewport where the screen space origin is in the bottom left corner
//        //final int[] viewport = new int[]{0, 0, cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight()};
//        
//        // In Vulkan the screen space origin is in the top left hand corner.  Note we put the origin at 0, H and 
//        // the viewport dimensions are W and -H.  The -H means we we still have a 0->H range, just running in the
//        // opposite direction to GL.
//        final int[] viewport = new int[]{0, cvkSwapChain.GetHeight(), cvkSwapChain.GetWidth(), -cvkSwapChain.GetHeight()};
//        
//        final int dx = cvkSwapChain.GetWidth() / 2 - FPS_OFFSET;
//        final int dy = cvkSwapChain.GetHeight() / 2 - FPS_OFFSET;
//        pxScale = calculateXProjectionScale(viewport);
//        pyScale = calculateYProjectionScale(viewport);
//        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, bottomRightCorner);
//
//        
//        // set the number of pixels per world unit at distance 1
//        geometryUBO.pixelDensity = (float)(cvkSwapChain.GetHeight() * 0.5 / Math.tan(Math.toRadians(FIELD_OF_VIEW)));        
//        geometryUBO.pScale = pyScale;
//             
//        
//        // LIFTED FROM FPSRenerable.display(...)
//        // Initialise source data to sensible values   
//        final Matrix44f scalingMatrix = new Matrix44f();
//        scalingMatrix.makeScalingMatrix(pxScale, pyScale, 0);
//        final Matrix44f srMatrix = new Matrix44f();
//        srMatrix.multiply(scalingMatrix, IDENTITY_44F);
//
//        // build the fps matrix by translating the sr matrix
//        final Matrix44f translationMatrix = new Matrix44f();
//        translationMatrix.makeTranslationMatrix(bottomRightCorner.getX(),
//                                                bottomRightCorner.getY(), 
//                                                bottomRightCorner.getZ());
//        vertexUBO.mvMatrix.multiply(translationMatrix, srMatrix);        
//                      
//                
//        // In the JOGL version these were in a static var CAMERA that never changed
//        vertexUBO.visibilityLow = 0.0f;
//        vertexUBO.visibilityHigh = 1.0f;
//        
//        // Get the projection matrix from our parent
//        geometryUBO.pMatrix.set(parent.GetProjectionMatrix());
//        
//
//
//        // Staging buffer so our VB can be device local (most performant memory)
//        int size = CVKFPSRenderable.SIZEOF;
//        PointerBuffer pData = stack.mallocPointer(1);        
//        CVKBuffer cvkVertUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
//                                                            size,
//                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
//                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
//        cvkVertUBStagingBuffer.DEBUGNAME = "CVKFPSRenderable.UpdateUniformBuffers cvkVertUBStagingBuffer";                           
//        ret = vkMapMemory(cvkDevice.GetDevice(), cvkVertUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
//        if (VkFailed(ret)) { return ret; }
//        {
//            vertexUBO.CopyTo(pData.getByteBuffer(0, size));
//        }
//        vkUnmapMemory(cvkDevice.GetDevice(), cvkVertUBStagingBuffer.GetMemoryBufferHandle());             
//
//        // Fill of the geometry uniform buffer
//        size = CVKFPSRenderable.SIZEOF;
//        CVKBuffer cvkGeomUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
//                                                            size,
//                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
//                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
//        cvkGeomUBStagingBuffer.DEBUGNAME = "CVKFPSRenderable.UpdateUniformBuffers cvkGeomUBStagingBuffer";  
//        ret = vkMapMemory(cvkDevice.GetDevice(), cvkGeomUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
//        if (VkFailed(ret)) { return ret; }
//        {
//            geometryUBO.CopyTo(pData.getByteBuffer(0, size));
//        }
//        vkUnmapMemory(cvkDevice.GetDevice(), cvkGeomUBStagingBuffer.GetMemoryBufferHandle());          
//                
//        // Copy the UBOs in VK buffers we can bind to a descriptor set  
//        for (int i = 0; i < imageCount; ++i) {   
//            vertexUniformBuffers.get(i).CopyFrom(cvkVertUBStagingBuffer);                       
//            geometryUniformBuffers.get(i).CopyFrom(cvkGeomUBStagingBuffer);                                           
//        }
//	cvkVertUBStagingBuffer.Destroy();
//        cvkGeomUBStagingBuffer.Destroy();

        return ret;
    }    
    
    private void DestroyUniformBuffers() {
//        if (vertexUniformBuffers != null) {
//            vertexUniformBuffers.forEach(el -> {el.Destroy();});
//            vertexUniformBuffers = null;
//        }
//        
//        if (geometryUniformBuffers != null) {
//            geometryUniformBuffers.forEach(el -> {el.Destroy();});
//            geometryUniformBuffers = null;
//        }        
    }      
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
//        int imageCount = cvkSwapChain.GetImageCount();
//        
//        commandBuffers = new ArrayList<>(imageCount);
//
//        for (int i = 0; i < imageCount; ++i) {
//            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_SECONDARY);
//            buffer.DEBUGNAME = String.format("CVKFPSRenderable %d", i);
//            commandBuffers.add(buffer);
//        }
//        
//        CVKLOGGER.log(Level.INFO, "Init Command Buffer - FPSRenderable");
        
        return ret;
    }   
    
    @Override
    public int RecordCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int index){
        VerifyInRenderThread();
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain != null);
                
        int ret = VK_SUCCESS;
     
//        try (MemoryStack stack = stackPush()) {
// 
//            // Fill
//            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
//            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
//            beginInfo.pNext(0);
//            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);  // hard coding this for now
//            beginInfo.pInheritanceInfo(inheritanceInfo);             
//
//            VkCommandBuffer commandBuffer = commandBuffers.get(index).GetVKCommandBuffer();
//            ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
//            checkVKret(ret);
//
//            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelines.get(index));
//
//            LongBuffer pVertexBuffers = stack.longs(vertexBuffers.get(index).GetBufferHandle());
//            LongBuffer offsets = stack.longs(0);
//
//            // Bind verts
//            vkCmdBindVertexBuffers(commandBuffer, 0, pVertexBuffers, offsets);
//
//            // Bind descriptors
//            vkCmdBindDescriptorSets(commandBuffer, 
//                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
//                                    pipelineLayouts.get(index), 
//                                    0, 
//                                    stack.longs(pDescriptorSets.get(index)), 
//                                    null);
//            vkCmdDraw(commandBuffer,
//                      GetVertexCount(),  //number of verts == number of digits
//                      1,  //no instancing, but we must draw at least 1 point
//                      0,  //first vert index
//                      0); //first instance index (N/A)                         
//            ret = vkEndCommandBuffer(commandBuffer);
//            checkVKret(ret);
//        }
        
        return ret;
    }       
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) { return null; }   
    
    private void DestroyCommandBuffers() {         
//        if (null != commandBuffers) {
//            commandBuffers.forEach(el -> {el.Destroy();});
//            commandBuffers.clear();
//            commandBuffers = null;
//        }      
    }        
    
        
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout(CVKDevice cvkDevice) {
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
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain != null);
//        
//        int ret;    
//        int imageCount = cvkSwapChain.GetImageCount();
//
//        // The same layout is used for each descriptor set (each descriptor set is
//        // identical but allow the GPU and CPU to desynchronise_.
//        LongBuffer layouts = stack.mallocLong(imageCount);
//        for (int i = 0; i < imageCount; ++i) {
//            layouts.put(i, hDescriptorLayout);
//        }
//
//        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
//        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
//        allocInfo.descriptorPool(cvkSwapChain.GetDescriptorPoolHandle());
//        allocInfo.pSetLayouts(layouts);            
//
//        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
//        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
//        ret = vkAllocateDescriptorSets(cvkDevice.GetDevice(), allocInfo, pDescriptorSets);
//        if (VkFailed(ret)) { return ret; }   

        descriptorPoolResourcesDirty = false;
        
        return UpdateDescriptorSets(stack);
    }
    
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
//     
//        int imageCount = cvkSwapChain.GetImageCount();
//
//        // Struct for the size of the uniform buffer used by SimpleIcon.vs (we fill the actual buffer below)
//        VkDescriptorBufferInfo.Buffer vertBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
//        vertBufferInfo.offset(0);
//        vertBufferInfo.range(CVKFPSRenderable.SIZEOF);
//
//        // Struct for the size of the uniform buffer used by SimpleIcon.gs (we fill the actual buffer below)
//        VkDescriptorBufferInfo.Buffer geomBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
//        geomBufferInfo.offset(0);
//        geomBufferInfo.range(CVKFPSRenderable.SIZEOF);      
//
//        // Struct for the size of the image sampler used by SimpleIcon.fs
//        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
//        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
//        imageInfo.imageView(parent.GetTextureAtlas().GetAtlasImageViewHandle());
//        imageInfo.sampler(parent.GetTextureAtlas().GetAtlasSamplerHandle());            
//
//        // We need 3 write descriptors, 2 for uniform buffers (vs + gs) and one for texture (fs)
//        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(3, stack);
//
//        VkWriteDescriptorSet vertUBDescriptorWrite = descriptorWrites.get(0);
//        vertUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
//        vertUBDescriptorWrite.dstBinding(0);
//        vertUBDescriptorWrite.dstArrayElement(0);
//        vertUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//        vertUBDescriptorWrite.descriptorCount(1);
//        vertUBDescriptorWrite.pBufferInfo(vertBufferInfo);
//
//        VkWriteDescriptorSet geomUBDescriptorWrite = descriptorWrites.get(1);
//        geomUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
//        geomUBDescriptorWrite.dstBinding(1);
//        geomUBDescriptorWrite.dstArrayElement(0);
//        geomUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
//        geomUBDescriptorWrite.descriptorCount(1);
//        geomUBDescriptorWrite.pBufferInfo(geomBufferInfo);            
//
//        VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(2);
//        samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
//        samplerDescriptorWrite.dstBinding(2);
//        samplerDescriptorWrite.dstArrayElement(0);
//        samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
//        samplerDescriptorWrite.descriptorCount(1);
//        samplerDescriptorWrite.pImageInfo(imageInfo);                                
//
//
//        for (int i = 0; i < imageCount; ++i) {
//            long descriptorSet = pDescriptorSets.get(i);
//
//            vertBufferInfo.buffer(vertexUniformBuffers.get(i).GetBufferHandle());
//            geomBufferInfo.buffer(geometryUniformBuffers.get(i).GetBufferHandle());
// 
//            vertUBDescriptorWrite.dstSet(descriptorSet);
//            geomUBDescriptorWrite.dstSet(descriptorSet);
//            samplerDescriptorWrite.dstSet(descriptorSet);
//
//            // Update the descriptors with a write and no copy
//            vkUpdateDescriptorSets(cvkDevice.GetDevice(), descriptorWrites, null);
//        }
//        
//        // Cache atlas handles so we know when to recreate descriptors
//        hAtlasSampler = parent.GetTextureAtlas().GetAtlasSamplerHandle();
//        hAtlasImageView = parent.GetTextureAtlas().GetAtlasImageViewHandle();            
        
        return ret;
    }
        
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
//        if (pDescriptorSets != null) {
//            CVKLOGGER.info(String.format("CVKFPSRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity()));
//            
//            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
//            ret = vkFreeDescriptorSets(cvkDevice.GetDevice(), cvkSwapChain.GetDescriptorPoolHandle(), pDescriptorSets);
//            pDescriptorSets = null;
//            checkVKret(ret);
//        }
//        
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
    
    private int CreateDescriptorPoolResources() {
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain != null);
        try (MemoryStack stack = stackPush()) {
            return CreateDescriptorSets(stack);
        }
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {}      
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelines() {
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain.GetSwapChainHandle()        != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain.GetRenderPassHandle()       != VK_NULL_HANDLE);
        CVKAssert(cvkDescriptorPool.GetDescriptorPoolHandle()   != VK_NULL_HANDLE);
        CVKAssert(hVertexShader   != VK_NULL_HANDLE);
        CVKAssert(hGeometryShader != VK_NULL_HANDLE);
        CVKAssert(hFragmentShader != VK_NULL_HANDLE);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        final int imageCount = cvkSwapChain.GetImageCount();                
        int ret = VK_SUCCESS;
//        try (MemoryStack stack = stackPush()) {                 
//            // A complete pipeline for each swapchain image.  Wasteful?
//            pipelines = new ArrayList<>(imageCount);            
//            pipelineLayouts = new ArrayList<>(imageCount);   
//            for (int i = 0; i < imageCount; ++i) {                              
//                // prepare vertex attributes
//
//                //From the GL FPSBatcher and FPSRenderable and shaders:
//                // 1 vertex per digit.
//                // Vert inputs:
//                // int[2] data {icon indexes (encoded to int), digit index * 4)
//                // float[4] backgroundIconColor
//                // Vert outputs:
//                // flat out ivec2 gData; this is data passed through
//                // out mat4 gBackgroundIconColor; backgroundIconColor in a 4x4 matrix
//                // flat out float gRadius; 1 if visible, -1 otherwise
//                // gl_Position = mvMatrix * vec4(digitPosition, 1); where digitPosition is (digit index * 4, 0, 0)
//
//                // A bunch of uniforms:
//                // SimpleIcon.vs:
//                // uniform mat4 mvMatrix;
//                // uniform float visibilityLow;
//                // uniform float visibilityHigh;
//                // uniform float offset;
//
//                // SimpleIcon.gs:
//                // Input:
//                // uniform mat4 pMatrix;
//                // uniform float pixelDensity;
//                // uniform float pScale;     
//                // Ouput:
//                // flat out mat4 iconColor;
//                // noperspective centroid out vec3 textureCoords;
//                // layout(triangle_strip, max_vertices=28) out;     
//
//
//            
//                // TODO: Generalize map of shaders and type per renderable
//                // Then can dynamically set here? Think I saw something like
//                // this in the Oreon engine.
//                ByteBuffer entryPoint = stack.UTF8("main");
//
//                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);
//
//                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
//
//                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
//                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
//                vertShaderStageInfo.module(hVertexShader);
//                vertShaderStageInfo.pName(entryPoint);
//
//                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
//                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
//                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
//                geomShaderStageInfo.module(hGeometryShader);
//                geomShaderStageInfo.pName(entryPoint);            
//
//                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
//                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
//                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
//                fragShaderStageInfo.module(hFragmentShader);
//                fragShaderStageInfo.pName(entryPoint);
//
//                // ===> VERTEX STAGE <===
//                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
//                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
//                vertexInputInfo.pVertexBindingDescriptions(CVKFPSRenderable.Vertex.GetBindingDescription());
//                vertexInputInfo.pVertexAttributeDescriptions(CVKFPSRenderable.Vertex.GetAttributeDescriptions());
//
//                // ===> ASSEMBLY STAGE <===
//                // Triangle list is stipulated by the layout of the out attribute of
//                // SimpleIcon.gs
//                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
//                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
////                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
//                
//                // Generalize me! Parameter?
//                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_POINT_LIST);
//                inputAssembly.primitiveRestartEnable(false);
//
//                // ===> VIEWPORT & SCISSOR
//                VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
//                viewport.x(0.0f);
//                viewport.y(0.0f);
//                viewport.width(cvkSwapChain.GetWidth());
//                viewport.height(cvkSwapChain.GetHeight());
//                viewport.minDepth(0.0f);
//                viewport.maxDepth(1.0f);
//
//                VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
//                scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
//                scissor.extent(cvkSwapChain.GetExtent());
//
//                VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
//                viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
//                viewportState.pViewports(viewport);
//                viewportState.pScissors(scissor);
//
//                // ===> RASTERIZATION STAGE <===
//                VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
//                rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
//                rasterizer.depthClampEnable(false);
//                rasterizer.rasterizerDiscardEnable(false);
//                rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
//                rasterizer.lineWidth(1.0f);
//                rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
//                rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
//                rasterizer.depthBiasEnable(false);
//
//                // ===> MULTISAMPLING <===
//                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
//                multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
//                multisampling.sampleShadingEnable(false);
//                multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
//                
//                // ===> DEPTH <===
//
//                // Even though we don't test depth, the renderpass created by CVKSwapChain is used by
//                // each renderable and it was created to have a depth attachment
//                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
//                depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
//                depthStencil.depthTestEnable(false);
//                depthStencil.depthWriteEnable(false);
//                depthStencil.depthCompareOp(VK_COMPARE_OP_ALWAYS);
//                depthStencil.depthBoundsTestEnable(false);
//                depthStencil.minDepthBounds(0.0f); // Optional
//                depthStencil.maxDepthBounds(1.0f); // Optional
//                depthStencil.stencilTestEnable(false);                       
//
//                // ===> COLOR BLENDING <===
//                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
//                colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
//                colorBlendAttachment.blendEnable(false);
//
//                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
//                colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
//                colorBlending.logicOpEnable(false);
//                colorBlending.logicOp(VK_LOGIC_OP_COPY);
//                colorBlending.pAttachments(colorBlendAttachment);
//                colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
//
//                // ===> PIPELINE LAYOUT CREATION <===
//                VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
//                pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
//                pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
//
//                LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
//
//                ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
//                checkVKret(ret);
//
//                long hPipelineLayout = pPipelineLayout.get(0);
//                CVKAssert(hPipelineLayout != VK_NULL_HANDLE);
//                pipelineLayouts.add(hPipelineLayout);
//                
//                VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
//                pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
//                pipelineInfo.pStages(shaderStages);
//                pipelineInfo.pVertexInputState(vertexInputInfo);
//                pipelineInfo.pInputAssemblyState(inputAssembly);
//                pipelineInfo.pViewportState(viewportState);
//                pipelineInfo.pRasterizationState(rasterizer);
//                pipelineInfo.pMultisampleState(multisampling);
//                pipelineInfo.pDepthStencilState(depthStencil);
//                pipelineInfo.pColorBlendState(colorBlending);
//                pipelineInfo.layout(hPipelineLayout);
//                pipelineInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
//                pipelineInfo.subpass(0);
//                pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
//                pipelineInfo.basePipelineIndex(-1);
//
//                LongBuffer pGraphicsPipeline = stack.mallocLong(1);
//
//
//                ret = vkCreateGraphicsPipelines(cvkDevice.GetDevice(), 
//                                                VK_NULL_HANDLE, 
//                                                pipelineInfo, 
//                                                null, 
//                                                pGraphicsPipeline);
//                if (VkFailed(ret)) { return ret; }
//                CVKAssert(pGraphicsPipeline.get(0) != VK_NULL_HANDLE);  
//                pipelines.add(pGraphicsPipeline.get(0));                      
//            }
//        }
        
        CVKLOGGER.log(Level.INFO, "Graphics Pipeline created for FPSRenderable class.");
        return ret;
    }
    
    private void DestroyPipelines() {
//        if (pipelines != null) {
//            for (int i = 0; i < pipelines.size(); ++i) {
//                vkDestroyPipeline(cvkDevice.GetDevice(), pipelines.get(i), null);
//                pipelines.set(i, VK_NULL_HANDLE);
//            }
//            pipelines.clear();
//            pipelines = null;
//        }        
    }
    
    private void DestroyPipelineLayouts() {
//        if (pipelineLayouts != null) {
//            for (int i = 0; i < pipelineLayouts.size(); ++i) {
//                vkDestroyPipelineLayout(cvkDevice.GetDevice(), pipelineLayouts.get(i), null);
//                pipelineLayouts.set(i, VK_NULL_HANDLE);
//            }
//            pipelineLayouts.clear();
//            pipelineLayouts = null;
//        }
    }      


    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() { return recreateIcons || descriptorPoolResourcesDirty || swapChainResourcesDirty; }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        VerifyInRenderThread();
        
        
        if (swapChainResourcesDirty) {
            ret = CreateSwapChainResources();
            if (VkFailed(ret)) { return ret; }
        }
        
        if (descriptorPoolResourcesDirty) {
            ret = CreateDescriptorPoolResources();
            if (VkFailed(ret)) { return ret; }
        }
        
        // TODO: we need to figure out if we recreate all icons on every event, if not
        // then we need to track what events we need to process here.
        
        if (recreateIcons) {
            DestroyVertexBuffers();
            DestroyXYZWBuffer();

            ret = CreateVertexBuffers();
            if (VkFailed(ret)) { return ret; }
            ret = CreateXYZWBuffer();
            if (VkFailed(ret)) { return ret; }

            try (MemoryStack stack = stackPush()) {          
                ret = UpdateUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
            }

            recreateIcons = false;
        }
        
        return ret;
    }        
    
    
    // ========================> Tasks <======================== \\    
    
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
            
            if (vertexCount > 0) {
                int vertexBufferSizeBytes = CVKIconsRenderable.Vertex.SIZEOF * vertexCount;
                cvkVertexStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                          vertexBufferSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkVertexStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkVertexStagingBuffer";
                
                int xyzwBufferSizeBytes = XYZ_BUFFER_WIDTH * vertexCount * Float.BYTES;
                cvkXYZWStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                        xyzwBufferSizeBytes, 
                                                        VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkXYZWStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkXYZWStagingBuffer";
                
                ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartWrite(0, vertexBufferSizeBytes);
                ByteBuffer pXYZWMemory = cvkXYZWStagingBuffer.StartWrite(0, xyzwBufferSizeBytes);
                CVKIconsRenderable.Vertex vertex = new CVKIconsRenderable.Vertex();
                for (int pos = 0; pos < vertexCount; pos++) {
                    SetColorInfo(pos, vertex, access);
                    SetIconInfo(pos, vertex, access);
                    vertex.CopyTo(pVertexMemory);
                    SetXYZWInfo(pos, pXYZWMemory, access);
                }
                int vertMemPos = pVertexMemory.position();
                CVKAssert(vertMemPos == vertexBufferSizeBytes);
                cvkVertexStagingBuffer.EndWrite();
                pVertexMemory = null; // now unmapped, do not use
                int xyzwMemPos = pXYZWMemory.position();
                CVKAssert(xyzwMemPos == xyzwBufferSizeBytes);
                cvkXYZWStagingBuffer.EndWrite();
                pXYZWMemory = null; // now unmapped, do not use                
                
                
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
        return (imageIndex) -> {
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
        return (imageIndex) -> {
            VerifyInRenderThread();
        };        
    }    
    
    
    // ========================> Helpers <======================== \\      
    
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
    
    private void SetColorInfo(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(vertex != null);
        CVKAssert(pos < access.getVertexCount());
        
        vertex.SetBackgroundIconColour(access.getVertexColor(pos));
        vertex.SetVertexVisibility(access.getVertexVisibility(pos));
    }    
    
    // TODO_TT: find out more about the second coord
    // TODO_TT: see if anything ever uses the radius   - yes the blaze batcher 
    private void SetXYZWInfo(final int pos, ByteBuffer buffer, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(buffer.remaining() >= (XYZ_BUFFER_WIDTH * Float.BYTES));    
        
        buffer.putFloat(access.getX(pos));
        buffer.putFloat(access.getY(pos));
        buffer.putFloat(access.getZ(pos));
        buffer.putFloat(access.getRadius(pos));
        buffer.putFloat(access.getX2(pos));
        buffer.putFloat(access.getY2(pos));
        buffer.putFloat(access.getZ2(pos));
        buffer.putFloat(access.getRadius(pos));  
    }    
}
