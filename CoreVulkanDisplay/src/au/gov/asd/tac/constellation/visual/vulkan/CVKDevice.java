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
package au.gov.asd.tac.constellation.visual.vulkan;

import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKMissingEnums;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_UNIFORM_TEXEL_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8_SINT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFeatures;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceLimits;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_SURFACE_UNSUPPORTED;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.GetRequiredVKLogicalDeviceExtensions;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.InitVKValidationLayers;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_BLIT_SRC_BIT;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;


public class CVKDevice {
    private static CVKDevice cvkDevice = null;
    private VkPhysicalDevice vkPhysicalDevice = null;
    private VkDevice vkDevice = null;
    private VkQueue vkQueue = null;
    private VkPhysicalDeviceProperties vkPhysicalDeviceProperties = null;
    private VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures = null;        
    private VkPhysicalDeviceMemoryProperties vkPhysicalDeviceMemoryProperties = null;   
    private long hCommandPool = VK_NULL_HANDLE; 
    private int queueFamilyIndex = -1;     
    private final VkExtent2D vkMaxFramebufferExtent = VkExtent2D.malloc().set(0, 0);
    private int max1DImageWidth = 0;
    private int max2DDimension = 0;
    private int maxImageLayers = 0;
    private int maxTexelBufferElements = 0;   
    private int minUniformBufferAlignment = 1;
    
    
    // ========================> Getters <======================== \\
    
    public static long GetCommandPoolHandle() { return GetInstance().hCommandPool; }
    public static VkDevice GetVkDevice() { return GetInstance().vkDevice; }   
    public static VkPhysicalDevice GetVkPhysicalDevice() { return GetInstance().vkPhysicalDevice; }
    public static VkQueue GetVkQueue() { return GetInstance().vkQueue; }
    public static VkExtent2D GetMaxFrameBufferExtent() { return GetInstance().vkMaxFramebufferExtent; }
    public static int GetMax1DImageWidth() { return GetInstance().max1DImageWidth; }
    public static int GetMax2DDimension()  { return GetInstance().max2DDimension; }
    public static int GetMaxImageLayers()  { return GetInstance().maxImageLayers; }
    public static int GetMaxTexelBufferElements() { return GetInstance().maxTexelBufferElements; }
    public static int GetMinUniformBufferAlignment() { return GetInstance().minUniformBufferAlignment; }
    private CVKGraphLogger GetLogger() { return CVKGraphLogger.GetStaticLogger(); }
    
    
    
    /**
     * Singleton method that will create the CVKDevice.
     * 
     * @return the one and only device
     */
    public static CVKDevice GetInstance() {
        if (CVKDevice.cvkDevice == null) {
            cvkDevice = new CVKDevice();
        } 
        return cvkDevice;      
    }        
    
    private CVKDevice() {
    }
    
    
    public int Initialise(long hSurface) {
        int ret;
        
        GetLogger().StartLogSection("Initialising CVKDevice");
        try (MemoryStack stack = stackPush()) {  
            ret = SelectVKPhysicalDevice(stack, hSurface);
            if (VkFailed(ret)) return ret;
            ret = CreateVKLogicalDevice(stack);
            if (VkFailed(ret)) return ret;
            StoreVKQueue(stack);    
            ret = CreateVKCommandPool(stack);
            if (VkFailed(ret)) return ret;
        } finally {
            GetLogger().EndLogSection("Initialising CVKDevice");
        }
        
        return ret;
    }
    
    /**
     * When a canvas notifies us that we have a surface available we need to 
     * either initialise this device if this is our first surface, or we need
     * to validate the new surface.  It is a Vulkan requirement that 
     * vkGetPhysicalDeviceSurfaceSupportKHR is called and must return VK_TRUE on
     * a surface before it can be rendered to.  Note in the initial case Initialise
     * will call ValidateSurface during the selection of the physical device.
     * 
     * @param canvas
     * @return
     */
    public int InitialiseSurface(CVKCanvas canvas) {
        if (vkPhysicalDevice == null) {
            return Initialise(canvas.GetSurfaceHandle());
        } else {
            return ValidateSurface(vkPhysicalDevice, queueFamilyIndex, canvas.GetSurfaceHandle());                        
        }
    }
    
    public void Destroy() {
        try {
		GetLogger().StartLogSection("Destroying CVKDevice");
	        DestroyVKCommandPool();
	        DestroyVKLogicalDevice();
        } finally {
            GetLogger().EndLogSection("Destroying CVKDevice");
        }
    }
    
    private int ValidateSurface(VkPhysicalDevice device, int queueFamilyIndex, long hSurface) {
       try (MemoryStack stack = stackPush()) {
           IntBuffer pInt = stack.mallocInt(1);
           int ret = vkGetPhysicalDeviceSurfaceSupportKHR(device, queueFamilyIndex, hSurface, pInt);
           if (VkFailed(ret)) return ret;
           return (pInt.get(0) == VK_TRUE) ? VK_SUCCESS : CVK_SURFACE_UNSUPPORTED;
       }                          
    }
            
    
    
    /**
     * Enumerates physical devices and selects the first to support swap chains
     * and with a queue family that allows display (graphics operations and
     * ability to present to our surface).
     * <p>
     * Vulkan can be used for a number of roles (eg display or compute). Each
     * device has a number of queue families that may be suited to a particular
     * role. A queue family contains a set of queues or a single queue. These
     * queues are what we submit our command buffers to. Unlike OpenGL where in
     * immediate mode we submit commands one by one to the display driver,
     * Vulkan batches these up in command buffers that we submit. This is more
     * performant than immediate mode commands and it allows multiple threads to
     * submit commands to the display driver.
     * <p>
     * After we have a vkPhysicalDevice we then query it for a bunch of details.
     * <p>
     * <h2>Surface Presentation Modes</h2>
     * There are 4 modes:
     * <table style="width:100%">
     * <tr>
     * <th style="text-align:left">Num</th>
     * <th style="text-align:left">Name</th>
     * <th style="text-align:left">Description</th>
     * </tr><tr>
     * <td style="text-align:left">0</td>
     * <td style="text-align:left">VK_PRESENT_MODE_IMMEDIATE_KHR</td>
     * <td style="text-align:left">I can haz screen tear</td>
     * </tr><tr>
     * <td style="text-align:left">1</td>
     * <td style="text-align:left">VK_PRESENT_MODE_MAILBOX_KHR</td>
     * <td style="text-align:left">Images are queued, latest is displayed next</td>
     * </tr><tr>
     * <td style="text-align:left">2</td>
     * <td style="text-align:left">VK_PRESENT_MODE_FIFO_KHR</td>
     * <td style="text-align:left">FIFO, no tearing, possible slight input lag</td>
     * </tr><tr>
     * <td style="text-align:left">3</td>
     * <td style="text-align:left">VK_PRESENT_MODE_FIFO_RELAXED_KHR</td>
     * <td style="text-align:left">FIFO queue, can result in image tearing</td>
     * </tr></table>
     * 
     * @param stack
     */
    private int SelectVKPhysicalDevice(MemoryStack stack, long hSurface) {
        int ret;
        
        // Get the number of physical devices
        IntBuffer pInt = stack.mallocInt(1);
        ret = vkEnumeratePhysicalDevices(CVKInstance.GetVkInstance(), pInt, null);
        if (VkFailed(ret)) return ret;
        if (pInt.get(0) == 0) {
            throw new RuntimeException("Vulkan: no GPUs found");
        }

        // Get the physical devices
        int numDevices = pInt.get(0);
        PointerBuffer physicalDevices = stack.mallocPointer(numDevices);
        ret = vkEnumeratePhysicalDevices(CVKInstance.GetVkInstance(), pInt, physicalDevices);
        if (VkFailed(ret)) return ret;

        // Enumerate physical devices.  Stop once requirements met and physical device set.
        vkPhysicalDevice = null;
        for (int iDevice = 0; (iDevice < numDevices) && vkPhysicalDevice == null; ++iDevice) {
            // Get the count of extensions supported by this device
            VkPhysicalDevice candidate = new VkPhysicalDevice(physicalDevices.get(iDevice), CVKInstance.GetVkInstance());
            
            // Check this device supports geometry shaders
            VkPhysicalDeviceFeatures candidatePhysicalDeviceFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
            vkGetPhysicalDeviceFeatures(candidate, candidatePhysicalDeviceFeatures);
            if (!candidatePhysicalDeviceFeatures.geometryShader()) {
                GetLogger().info("Device %d discarded as it does not support geometry shaders", iDevice);
                continue;
            }
            
            // Check extensions for Swapchain support
            pInt.put(0, 0);
            ret = vkEnumerateDeviceExtensionProperties(candidate, (String) null, pInt, null);
            if (VkFailed(ret)) return ret;
            int numExtensions = pInt.get(0);
            if (numExtensions > 0) {
                // Get the extensions supported by this device
                VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.mallocStack(numExtensions, stack);
                ret = vkEnumerateDeviceExtensionProperties(candidate, (String) null, pInt, deviceExtensions);
                if (VkFailed(ret)) return ret;
                
                for (int iExtension = 0; iExtension < numExtensions; ++iExtension) {
                    String extensionName = deviceExtensions.position(iExtension).extensionNameString();
                    GetLogger().info("Vulkan: device %d extension available: %s", iExtension, extensionName);                       
                }             

                // Enumerate extensions looking for swap chain support.  Stop once requirements met and physical device set.
                for (int iExtension = 0; (iExtension < numExtensions) && vkPhysicalDevice == null; ++iExtension) {
                    String extensionName = deviceExtensions.position(iExtension).extensionNameString();
                    if (VK_KHR_SWAPCHAIN_EXTENSION_NAME.equals(extensionName)) {
                        // Spapchain tick, now check the queue families for one that can peform graphics operations
                        pInt.put(0, 0);

                        // Calling this with no queue family properties will return the number of queue families into pInt
                        vkGetPhysicalDeviceQueueFamilyProperties(candidate, pInt, null);
                        int numQueueFamilies = pInt.get(0);

                        // This populates all the properties for each queue family ie candidateQueueFamilyProperties is an array
                        VkQueueFamilyProperties.Buffer candidateQueueFamilyProperties = VkQueueFamilyProperties.mallocStack(numQueueFamilies, stack);
                        vkGetPhysicalDeviceQueueFamilyProperties(candidate, pInt, candidateQueueFamilyProperties);

                        // Find a queue family that supports display.  Stop once requirements met and physical device set.
                        for (int iQueueFamily = 0; (iQueueFamily < numQueueFamilies) && vkPhysicalDevice == null; ++iQueueFamily) {
                            VkQueueFamilyProperties queueFamilyProperties = candidateQueueFamilyProperties.get(iQueueFamily);
                            // This this queue family support graphics operations>
                            if ((queueFamilyProperties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                                // Can this queue family present to our surface
                                ret = ValidateSurface(candidate, iQueueFamily, hSurface);
                                if (VkSucceeded(ret)) {
                                    queueFamilyIndex = iQueueFamily;
                                    vkPhysicalDevice = candidate;
                                }
                            }
                        } //end queue family loop  
                    }  // end if extension is swapchain                    
                } // end extension loop                
            } // end if numExtensions  > 0
        } // end physical device loop

        if (vkPhysicalDevice != null) {
            // Check we can use uniform texel buffers
            VkFormatProperties vkFormatProperties = VkFormatProperties.callocStack(stack);
            vkGetPhysicalDeviceFormatProperties(vkPhysicalDevice, VK_FORMAT_R32G32B32A32_SFLOAT, vkFormatProperties);
            if ((vkFormatProperties.bufferFeatures() & VK_FORMAT_FEATURE_UNIFORM_TEXEL_BUFFER_BIT) == 0) {
                throw new RuntimeException("Selected surface does not support uniform texel buffers needed for the vertex position buffer");
            }
            vkGetPhysicalDeviceFormatProperties(vkPhysicalDevice, VK_FORMAT_R8_SINT, vkFormatProperties);
            if ((vkFormatProperties.bufferFeatures() & VK_FORMAT_FEATURE_UNIFORM_TEXEL_BUFFER_BIT) == 0) {
                throw new RuntimeException("Selected surface does not support uniform texel buffers needed for the vertex flags buffer");
            }            
                        
            // Happy dance, we have a suitable physical device, get its properties
            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.malloc();
            vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);            

            // And features
            vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.malloc();
            vkGetPhysicalDeviceFeatures(vkPhysicalDevice, vkPhysicalDeviceFeatures);
            
            // TODO: in order to use the smooth lines line rasterization extension we first have to
            // query it is enabled.  The code below didn't work and isn't a high priority atm, but 
            // it would be good to use some newer features on cards that support them.
            
//            VkPhysicalDeviceLineRasterizationFeaturesEXT physicalDeviceLineRasterFeatures = VkPhysicalDeviceLineRasterizationFeaturesEXT.malloc();
//            physicalDeviceLineRasterFeatures.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_LINE_RASTERIZATION_FEATURES_EXT);
//            physicalDeviceLineRasterFeatures.pNext(VK_NULL_HANDLE);
//            
//            
//            VkPhysicalDeviceFeatures2 physicalDeviceFeatures2 = VkPhysicalDeviceFeatures2.malloc();
//            try {
//                
//                long functionHandle = vkGetInstanceProcAddr(CVKInstance.GetVkInstance(), memUTF8("vkGetPhysicalDeviceFeatures2"));
//                if (functionHandle != VK_NULL_HANDLE) {
//                    physicalDeviceFeatures2.sType(VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2);
//                    physicalDeviceFeatures2.pNext(physicalDeviceLineRasterFeatures.address());
//                    vkGetPhysicalDeviceFeatures2(vkPhysicalDevice, physicalDeviceFeatures2);
//                }
//            } catch (Exception e) {
//                GetLogger().LogException(e, "Oh dear");
//            }
//
//            physicalDeviceLineRasterFeatures.free();
//            physicalDeviceFeatures2.free();
            
            // What memory types are available
            vkPhysicalDeviceMemoryProperties = VkPhysicalDeviceMemoryProperties.malloc();
            vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, vkPhysicalDeviceMemoryProperties);
  

            VkPhysicalDeviceLimits limits = vkPhysicalDeviceProperties.limits();
            max1DImageWidth = limits.maxImageDimension1D();
            max2DDimension  = limits.maxImageDimension2D();
            maxImageLayers  = limits.maxImageArrayLayers();
            maxTexelBufferElements = limits.maxTexelBufferElements();
            minUniformBufferAlignment = (int)limits.minUniformBufferOffsetAlignment();
            vkMaxFramebufferExtent.set(limits.maxFramebufferWidth(), limits.maxFramebufferHeight());             
            
            if (CVK_DEBUGGING) {                
                GetLogger().info("Physcial device properties:\n"
                        + "\tdeviceName: %s\n"
                        + "\tdeviceType: %s\n"
                        + "\tapiVersion: %d\n"
                        + "\tdriverVersion: %d\n"
                        + "\tvendorID: %d\n"
                        + "\tdeviceID: %d\n"
                        + "\tLimits\n"
                        + "\t\t maxImageDimension1D: %d\n"
                        + "\t\t maxImageDimension2D: %d\n"
                        + "\t\t maxImageDimension3D: %d\n"
                        + "\t\t maxImageDimensionCube: %d\n"
                        + "\t\t maxImageArrayLayers: %d\n"
                        + "\t\t maxPerStageDescriptorSamplers: %d\n"
                        + "\t\t maxPerStageDescriptorUniformBuffers: %d\n"
                        + "\t\t maxGeometryShaderInvocations: %d\n"
                        + "\t\t maxGeometryInputComponents: %d\n"
                        + "\t\t maxGeometryOutputComponents: %d\n"
                        + "\t\t maxGeometryOutputVertices: %d\n"
                        + "\t\t maxGeometryTotalOutputComponents: %d\n"
                        + "\t\t maxViewportDimensions: %d x %d\n"
                        + "\t\t viewportBoundsRange: %f x %f\n"
                        + "\t\t maxFramebuffer dims: %d x %d\n"
                        + "\t\t maxFramebufferLayers: %d\n"
                        + "\t\t maxColorAttachments: %d\n"
                        + "\t\t minMemoryMapAlignment: %d\n"
                        + "\t\t maxTexelBufferElements: %d\n"
                        + "\t\t minTexelBufferOffsetAlignment: %d\n"
                        + "\t\t minUniformBufferOffsetAlignment: %d\n"
                        + "\t\t minStorageBufferOffsetAlignment: %d\n"
                        + "\t\t maxPushConstantsSize: %d\n"                        
                        + "\t\t pointSizeGranularity: %f\n" 
                        + "\t\t pointSizeRange: %f - %f\n"
                        + "\t\t lineWidthGranularity: %f\n" 
                        + "\t\t lineWidthRange: %f - %f\n",
                        vkPhysicalDeviceProperties.deviceNameString(),
                        CVKMissingEnums.VkPhysicalDeviceType.GetByValue(vkPhysicalDeviceProperties.deviceType()).name(),
                        vkPhysicalDeviceProperties.apiVersion(),
                        vkPhysicalDeviceProperties.driverVersion(),
                        vkPhysicalDeviceProperties.vendorID(),
                        vkPhysicalDeviceProperties.deviceID(),
                        limits.maxImageDimension1D(),
                        limits.maxImageDimension2D(),
                        limits.maxImageDimension3D(),
                        limits.maxImageDimensionCube(),
                        limits.maxImageArrayLayers(),
                        limits.maxPerStageDescriptorSamplers(),
                        limits.maxPerStageDescriptorUniformBuffers(),
                        limits.maxGeometryShaderInvocations(),
                        limits.maxGeometryInputComponents(),
                        limits.maxGeometryOutputComponents(),
                        limits.maxGeometryOutputVertices(),
                        limits.maxGeometryTotalOutputComponents(),
                        limits.maxViewportDimensions().get(0), limits.maxViewportDimensions().get(1),
                        limits.viewportBoundsRange().get(0), limits.viewportBoundsRange().get(1),
                        limits.maxFramebufferWidth(), limits.maxFramebufferHeight(),
                        limits.maxFramebufferLayers(),
                        limits.maxColorAttachments(),
                        limits.minMemoryMapAlignment(),
                        limits.maxTexelBufferElements(),
                        limits.minTexelBufferOffsetAlignment(),
                        limits.minUniformBufferOffsetAlignment(),
                        limits.minStorageBufferOffsetAlignment(),
                        limits.maxPushConstantsSize(),
                        limits.pointSizeGranularity(),
                        limits.pointSizeRange().get(0), limits.pointSizeRange().get(1),
                        limits.lineWidthGranularity(),
                        limits.lineWidthRange().get(0), limits.lineWidthRange().get(1)                                                
                        );
            }                                       
        } else {
            // Sad face
            GetLogger().severe("Vulkan: No suitable physical device found.");
            throw new RuntimeException("Vulkan: No suitable physical device found.");
        }
        
        return ret;
    }

    public boolean CheckDeviceSupportsBlit(int sourceFormat, int destinationFormat) {
        // Check and store if the device can perform blit image operations
        boolean supportsBlit = true;

        try (MemoryStack stack = stackPush()) {
            // Check blit support for source and destination
            // TODO HYDRA do this once and store result in cvkdevice
            VkFormatProperties formatProps = VkFormatProperties.callocStack(stack);

            // Check if the device supports blitting from optimal images (the swapchain images are in optimal format)
            vkGetPhysicalDeviceFormatProperties(GetVkPhysicalDevice(), sourceFormat, formatProps);

            if ((formatProps.optimalTilingFeatures() & VK_FORMAT_FEATURE_BLIT_SRC_BIT) == 0) {
                    GetLogger().info("Device does not support blitting from optimal tiled images, using copy instead of blit!");
                    supportsBlit = false;
            }

            // Check if the device supports blitting to linear images
            vkGetPhysicalDeviceFormatProperties(GetVkPhysicalDevice(), destinationFormat, formatProps);
            if ((formatProps.linearTilingFeatures() & VK_FORMAT_FEATURE_BLIT_DST_BIT) == 0) {
                    GetLogger().info("Device does not support blitting to linear tiled images, using copy instead of blit!");
                    supportsBlit = false;
            }
        }
        
        return supportsBlit;
    }
    
    /**
     * Create a logical device that gives us control over the physical device
     *
     * @param stack
     * @return 
     */
    private int CreateVKLogicalDevice(MemoryStack stack) {
        int ret = VK_SUCCESS;
        
        // We need the extensions and layers again.  
        PointerBuffer pbValidationLayers = null;
        PointerBuffer pbExtensions = GetRequiredVKLogicalDeviceExtensions(stack);
        if (CVK_DEBUGGING) {
            pbValidationLayers = InitVKValidationLayers();
        }

        // We only need one queue
        VkDeviceQueueCreateInfo.Buffer queue = VkDeviceQueueCreateInfo.mallocStack(1, stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                .pNext(NULL)
                .flags(0)
                .queueFamilyIndex(queueFamilyIndex)
                .pQueuePriorities(stack.floats(1.0f));

        //TODO_TT: do we need this for Consty?
        VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.callocStack(stack);
        features.geometryShader(true);
        if (vkPhysicalDeviceFeatures.shaderClipDistance()) {
            features.shaderClipDistance(true);
        }

        VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.mallocStack(stack)
                .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pNext(NULL)
                .flags(0)
                .pQueueCreateInfos(queue)
                .ppEnabledLayerNames(pbValidationLayers)
                .ppEnabledExtensionNames(pbExtensions)
                .pEnabledFeatures(features);

        PointerBuffer pb = stack.mallocPointer(1);
        ret = vkCreateDevice(vkPhysicalDevice, deviceCreateInfo, null, pb);
        if (VkSucceeded(ret)) {
            vkDevice = new VkDevice(pb.get(0), vkPhysicalDevice, deviceCreateInfo);
        }
        return ret;
    }
    
    private void DestroyVKLogicalDevice() {
        if (vkDevice != null) {
            vkDestroyDevice(vkDevice, null);
            vkDevice = null;
        }
    }

    /**
     * Initialises the queue that enables our code to communicate with the
     * display device driver. We really only care about a single graphics queue.
     * <p>
     * There are 4 queue types:
     * <ol>
     * <li>Graphics: drawing things</li>
     * <li>Compute: crunching equations</li>
     * <li>Transfer: allows buffer copying (DMAing to/from video memory). Is
     * implicit for the two types above.</li>
     * <li>Sparse: supports images backed by sparse memory, think ID's
     * megatexture where only part of a gigantic texture is backed by physical
     * memory at any point in time.</li>
     * </ol>
     * A queue family may contain more than one type of queue and where a type
     * is present there may be one or more queues of that type.
     *
     *
     *
     * @param stack
     * @see
     * <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#devsandqueues-queues">Queue
     * reference</a>
     */
    private void StoreVKQueue(MemoryStack stack) {
        PointerBuffer pb = stack.mallocPointer(1);
        vkGetDeviceQueue(vkDevice, queueFamilyIndex, 0, pb);
        long queueHandle = pb.get(0);
        vkQueue = new VkQueue(queueHandle, vkDevice);
    }   
    
    /**
     * Create the command pool from which all the command buffers are created.
     * Each image in the swap chain has its own command buffer.
     * 
     * @param stack
     * @return
     */
    private int CreateVKCommandPool(MemoryStack stack) {
        int ret;
        
        VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
        poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
        poolInfo.queueFamilyIndex(queueFamilyIndex);
        poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);   // Hydra - added this for secondary buffers which we may not need
        LongBuffer pCommandPool = stack.mallocLong(1);
        ret = vkCreateCommandPool(vkDevice, poolInfo, null, pCommandPool);
        if (VkSucceeded(ret)) {
            hCommandPool = pCommandPool.get(0);
        }
        
        return ret;
    }    
    
    private void DestroyVKCommandPool() {
        if (hCommandPool != VK_NULL_HANDLE) {
            vkDestroyCommandPool(vkDevice, hCommandPool, null);
            hCommandPool = VK_NULL_HANDLE;
        }
    }       
    
    public static void WaitIdle() {
        assert(GetVkDevice() != null);
        vkDeviceWaitIdle(GetVkDevice());
    }    
    
    /**
     * Use the mask provided by VkMemoryRequirements to return the index of the memory
     * type we want that match the provided properties bit mask.
     * 
     * The reason we need this function is that physical devices may support only some
     * of the types of display memory types eg device local, host visible.  In addition
     * to this memory types are referred to by their type index but the same type if 
     * available can be at different indexes on different gpus.  So this function exists
     * to map the type we want to the correct index.
     * 
     * @param typeFilter 1 << the type we want, which is the representation VkMemoryRequirements uses
     * @param properties 
     * @return
     */
    public static int GetMemoryType(int typeFilter, int properties) {
        assert(cvkDevice.vkPhysicalDeviceMemoryProperties != null);
        
        for (int i = 0; i < cvkDevice.vkPhysicalDeviceMemoryProperties.memoryTypeCount(); ++i) {
            if ((typeFilter & (1 << i)) != 0 && (cvkDevice.vkPhysicalDeviceMemoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }
        
        throw new RuntimeException(String.format("GetMemoryType failed to translate type %d with properties %d", typeFilter, properties));
    }
}
