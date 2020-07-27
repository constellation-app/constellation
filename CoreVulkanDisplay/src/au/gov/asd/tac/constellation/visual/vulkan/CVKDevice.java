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

import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKMissingEnums;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.debugging;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import com.google.common.primitives.Ints;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_UNIFORM_TEXEL_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
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
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;
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
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;


public class CVKDevice {
    private final CVKInstance cvkInstance;
    private final long hSurfaceHandle;
    private VkPhysicalDevice vkPhysicalDevice = null;
    private VkDevice vkDevice = null;
    private VkQueue vkQueue = null;
    private VkPhysicalDeviceProperties vkPhysicalDeviceProperties = null;
    private VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures = null;    
    private VkSurfaceCapabilitiesKHR vkSurfaceCapabilities = null;
    private VkSurfaceFormatKHR.Buffer vkSurfaceFormats = null;
    private VkPhysicalDeviceMemoryProperties vkPhysicalDeviceMemoryProperties = null;
    private CVKMissingEnums.VkFormat selectedFormat = CVKMissingEnums.VkFormat.VK_FORMAT_NONE;
    private CVKMissingEnums.VkColorSpaceKHR selectedColourSpace = CVKMissingEnums.VkColorSpaceKHR.VK_COLOR_SPACE_NONE;
    private CVKMissingEnums.VkPresentModeKHR selectedPresentationMode = CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_NONE;    
    private long hCommandPoolHandle = VK_NULL_HANDLE; 
    private int queueFamilyIndex = -1; 
    private VkExtent2D vkCurrentSurfaceExtent = VkExtent2D.malloc().set(0, 0);
    private int max1DImageWidth = 0;
    private int maxImageLayers = 0;
    
    
    // Getters
    public VkExtent2D GetCurrentSurfaceExtent() { return vkCurrentSurfaceExtent; }
    public long GetCommandPoolHandle() { return hCommandPoolHandle; }
    public VkDevice GetDevice() { return vkDevice; }   
    public VkPhysicalDevice GetPhysicalDevice() { return vkPhysicalDevice; }
    public VkQueue GetQueue() { return vkQueue; }
    public VkSurfaceCapabilitiesKHR GetSurfaceCapabilities() { return vkSurfaceCapabilities; }
    public long GetSurfaceHandle() { return hSurfaceHandle; }
    public CVKMissingEnums.VkFormat GetSurfaceFormat() { return selectedFormat; }    
    public CVKMissingEnums.VkColorSpaceKHR GetSurfaceColourSpace() { return selectedColourSpace; }    
    public CVKMissingEnums.VkPresentModeKHR GetPresentationMode() { return selectedPresentationMode; }
    public int GetMax1DImageWidth() { return max1DImageWidth; }
    public int GetMaxImageLayers() { return maxImageLayers; }
    
    
    public CVKDevice(CVKInstance instance, long surfaceHandle) {
        cvkInstance = instance;
        hSurfaceHandle = surfaceHandle;
    }
    
    
    public int Initialise() {
        int ret;
        
        StartLogSection("Initialising CVKDevice");
        try (MemoryStack stack = stackPush()) {  
            ret = InitVKPhysicalDevice(stack);
            if (VkFailed(ret)) return ret;
            ret = InitVKLogicalDevice(stack);
            if (VkFailed(ret)) return ret;
            InitVKQueue(stack);    
            if (VkFailed(ret)) return ret;
            ret = InitVKCommandPool(stack);
            if (VkFailed(ret)) return ret;
        }
        EndLogSection("Initialising CVKDevice");
        
        return ret;
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
    private int InitVKPhysicalDevice(MemoryStack stack) {
        int ret;
        
        // Get the number of physical devices
        IntBuffer pInt = stack.mallocInt(1);
        ret = vkEnumeratePhysicalDevices(cvkInstance.GetVkInstance(), pInt, null);
        if (VkFailed(ret)) return ret;
        if (pInt.get(0) == 0) {
            throw new RuntimeException("Vulkan: no GPUs found");
        }

        // Get the physical devices
        int numDevices = pInt.get(0);
        PointerBuffer physicalDevices = stack.mallocPointer(numDevices);
        ret = vkEnumeratePhysicalDevices(cvkInstance.GetVkInstance(), pInt, physicalDevices);
        if (VkFailed(ret)) return ret;

        // Enumerate physical devices.  Stop once requirements met and physical device set.
        vkPhysicalDevice = null;
        for (int iDevice = 0; (iDevice < numDevices) && vkPhysicalDevice == null; ++iDevice) {
            // Get the count of extensions supported by this device
            VkPhysicalDevice candidate = new VkPhysicalDevice(physicalDevices.get(iDevice), cvkInstance.GetVkInstance());
            
            // Check this device supports geometry shaders
            VkPhysicalDeviceFeatures candidatePhysicalDeviceFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
            vkGetPhysicalDeviceFeatures(candidate, candidatePhysicalDeviceFeatures);
            if (!candidatePhysicalDeviceFeatures.geometryShader()) {
                CVKLOGGER.info(String.format("Device %d discarded as it does not support geometry shaders", iDevice));
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
                    CVKLOGGER.log(Level.INFO, "Vulkan: device {0} extension available: {1}", new Object[]{iExtension, extensionName});                       
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
                                pInt.put(0, 0);
                                ret = vkGetPhysicalDeviceSurfaceSupportKHR(candidate, iQueueFamily, hSurfaceHandle, pInt);
                                if (VkFailed(ret)) return ret;
                                if (pInt.get(0) == VK_TRUE) {
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
                throw new RuntimeException("Selected surface does not support uniform texel buffers needed for the xyzw texture");
            }
            
            // Happy dance, we have a suitable physical device, get its properties
            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.malloc();
            vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);            

            // And features
            vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.malloc();
            vkGetPhysicalDeviceFeatures(vkPhysicalDevice, vkPhysicalDeviceFeatures);

            // Device caps for our surface
            vkSurfaceCapabilities = VkSurfaceCapabilitiesKHR.malloc();
            ret = UpdateSurfaceCapabilities();
            if (VkFailed(ret)) return ret;
            
            // What memory types are available
            vkPhysicalDeviceMemoryProperties = VkPhysicalDeviceMemoryProperties.malloc();
            vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, vkPhysicalDeviceMemoryProperties);
  
            // Figure out our ideal backbuffer size
            // The current size of the surface will either be explicit, which we use, or 
            // set to a value indicating it will use whatever is set in the swap chain.
            CVKLOGGER.log(Level.INFO, "Surface will be {0}x{1}", 
                    new Object[]{vkCurrentSurfaceExtent.width(),
                                 vkCurrentSurfaceExtent.height()});           
            
            VkPhysicalDeviceLimits l = vkPhysicalDeviceProperties.limits();
            max1DImageWidth = l.maxImageDimension1D();
            maxImageLayers  = l.maxImageArrayLayers();
            if (debugging) {                
                CVKLOGGER.info(String.format("Physcial device properties:\n"
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
                        + "\t\t minTexelBufferOffsetAlignment: %d\n"
                        + "\t\t minUniformBufferOffsetAlignment: %d\n"
                        + "\t\t minStorageBufferOffsetAlignment: %d\n"
                        + "\t\t pointSizeRange: %f - %f\n"
                        + "\t\t lineWidthRange: %f - %f\n",
                        vkPhysicalDeviceProperties.deviceNameString(),
                        CVKMissingEnums.VkPhysicalDeviceType.GetByValue(vkPhysicalDeviceProperties.deviceType()).name(),
                        vkPhysicalDeviceProperties.apiVersion(),
                        vkPhysicalDeviceProperties.driverVersion(),
                        vkPhysicalDeviceProperties.vendorID(),
                        vkPhysicalDeviceProperties.deviceID(),
                        l.maxImageDimension1D(),
                        l.maxImageDimension2D(),
                        l.maxImageDimension3D(),
                        l.maxImageDimensionCube(),
                        l.maxImageArrayLayers(),
                        l.maxPerStageDescriptorSamplers(),
                        l.maxPerStageDescriptorUniformBuffers(),
                        l.maxGeometryShaderInvocations(),
                        l.maxGeometryInputComponents(),
                        l.maxGeometryOutputComponents(),
                        l.maxGeometryOutputVertices(),
                        l.maxGeometryTotalOutputComponents(),
                        l.maxViewportDimensions().get(0), l.maxViewportDimensions().get(1),
                        l.viewportBoundsRange().get(0), l.viewportBoundsRange().get(1),
                        l.maxFramebufferWidth(), l.maxFramebufferHeight(),
                        l.maxFramebufferLayers(),
                        l.maxColorAttachments(),
                        l.minMemoryMapAlignment(),
                        l.minTexelBufferOffsetAlignment(),
                        l.minUniformBufferOffsetAlignment(),
                        l.minStorageBufferOffsetAlignment(),
                        l.pointSizeRange().get(0), l.pointSizeRange().get(1),
                        l.lineWidthRange().get(0), l.lineWidthRange().get(1)                                                
                        ));
            }            
                         

            // Surface formats our device can use
            pInt.put(0, 0);
            ret = vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, hSurfaceHandle, pInt, null);
            if (VkFailed(ret)) return ret;
            int numFormats = pInt.get(0);
            if (numFormats > 0) {
                vkSurfaceFormats = VkSurfaceFormatKHR.malloc(numFormats);
                ret = vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, hSurfaceHandle, pInt, vkSurfaceFormats);
                if (VkFailed(ret)) return ret;
                
                CVKLOGGER.info("Available surface formats:");
                for (int i = 0; i < numFormats; ++i) {
                    VkSurfaceFormatKHR surfaceFormat = vkSurfaceFormats.get(i);
                    CVKMissingEnums.VkColorSpaceKHR colorSpace = CVKMissingEnums.VkColorSpaceKHR.GetByValue(surfaceFormat.colorSpace());
                    CVKMissingEnums.VkFormat format = CVKMissingEnums.VkFormat.values()[surfaceFormat.format()];
                    
                    // We want to use VK_FORMAT_B8G8R8A8_SRGB for the surface format.  That's a byte for each
                    // of RGBA so it's easy to work with but where the value is nonlinearly mapped to 
                    // intensity.  Check out sRGB, but in short given the nature of human vision and the
                    // display characteristics of most displays we are better off concentrating granularity
                    // around intensities we can differentiate rather than just using a linear mapping.
                    if (format == CVKMissingEnums.VkFormat.VK_FORMAT_B8G8R8A8_SRGB) {
                        selectedFormat = format;
                    }
                    
                    // For the reason above we want the sRGB colour space
                    if (colorSpace == CVKMissingEnums.VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                        selectedColourSpace = colorSpace;
                    }
                    
                    CVKLOGGER.log(Level.INFO, "    {0}:{1}", new Object[]{format.name(), colorSpace.name()});                        
                }
            }
            
            if (selectedFormat == CVKMissingEnums.VkFormat.VK_FORMAT_NONE) {
                throw new RuntimeException("Required surface format unsupported (VK_FORMAT_B8G8R8A8_SRGB)");
            }
            if (selectedColourSpace == CVKMissingEnums.VkColorSpaceKHR.VK_COLOR_SPACE_NONE) {
                throw new RuntimeException("Required color space unsupported (VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)");
            }
            

            // Presentation modes our device can use for our surface
            pInt.put(0, 0);
            ret = vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, hSurfaceHandle, pInt, null);
            if (VkFailed(ret)) return ret;
            int numPresentationModes = pInt.get(0);
            if (numPresentationModes > 0) {
                IntBuffer presentationModes = stack.mallocInt(numPresentationModes);
                vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, hSurfaceHandle, pInt, presentationModes);
                
                CVKLOGGER.info("Supported presentation modes:");
                for (int i = 0; i < numPresentationModes; ++i) {                
                    CVKMissingEnums.VkPresentModeKHR presentationMode = CVKMissingEnums.VkPresentModeKHR.values()[presentationModes.get(i)];
                    // Mailbox is our first choice
                    if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR) {
                        selectedPresentationMode = presentationMode;
                    }
                    // Second preference is VK_PRESENT_MODE_FIFO_KHR, selected unless we already have mailbox
                    else if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR 
                     && selectedPresentationMode != CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR) {
                        selectedPresentationMode = presentationMode;
                    }
                    // Third preference is VK_PRESENT_MODE_FIFO_RELAXED_KHR
                    else if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_FIFO_RELAXED_KHR 
                     && selectedPresentationMode != CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR
                     && selectedPresentationMode != CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR) {
                        selectedPresentationMode = presentationMode;
                    }           
                    // Last choice
                    else if (presentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_IMMEDIATE_KHR
                     && selectedPresentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_NONE) {
                        selectedPresentationMode = presentationMode;
                    }
                    CVKLOGGER.log(Level.INFO, "   {0}", presentationMode.name());
                }                              
            }
            
            if (selectedPresentationMode == CVKMissingEnums.VkPresentModeKHR.VK_PRESENT_MODE_NONE) {
                throw new RuntimeException("No presentation mode supported");
            }            
        } else {
            // Sad face
            throw new RuntimeException("Vulkan: No suitable physical device found.");
        }
        
        return ret;
    }

    /**
     * Create a logical device that gives us control over the physical device
     *
     * @param stack
     * @return 
     */
    private int InitVKLogicalDevice(MemoryStack stack) {
        int ret = VK_SUCCESS;
        
        // We need the extensions and layers again.  
        PointerBuffer pbValidationLayers = null;
        PointerBuffer pbExtensions = GetRequiredVKLogicalDeviceExtensions(stack);
        if (debugging) {
            pbValidationLayers = InitVKValidationLayers(stack);
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
    private void InitVKQueue(MemoryStack stack) {
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
    private int InitVKCommandPool(MemoryStack stack) {
        int ret;
        
        VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
        poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
        poolInfo.queueFamilyIndex(queueFamilyIndex);
        poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);   // Hydra - added this for secondary buffers which we may not need
        LongBuffer pCommandPool = stack.mallocLong(1);
        ret = vkCreateCommandPool(vkDevice, poolInfo, null, pCommandPool);
        if (VkSucceeded(ret)) {
            hCommandPoolHandle = pCommandPool.get(0);
        }
        
        return ret;
    }    
    
    
    
    
    
    /**
     * Updates surface capabilities which may have changed due to our canvas
     * being resized.  It also updates the ideal extent which is either the
     * capabilities currentExtent capped to minImageExtent and maxImageExtent 
     * 
     * @return
     */
    public int UpdateSurfaceCapabilities() {
        StartLogSection("Device updating surface capalities");
        
        int ret = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkPhysicalDevice, hSurfaceHandle, vkSurfaceCapabilities);
        if (VkSucceeded(ret)) {
            vkCurrentSurfaceExtent.set(vkSurfaceCapabilities.currentExtent());
            if (vkCurrentSurfaceExtent.width() == UINT32_MAX) {
                //TODO_TT: find out how big our surface is somehow
                vkCurrentSurfaceExtent.set(800, 600);
                CVKLOGGER.log(Level.WARNING, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR returned extent with the magic don't care size");
            }
            vkCurrentSurfaceExtent.set(Ints.constrainToRange(vkCurrentSurfaceExtent.width(), 
                                                  vkSurfaceCapabilities.minImageExtent().width(), 
                                                  vkSurfaceCapabilities.maxImageExtent().width()),
                            Ints.constrainToRange(vkCurrentSurfaceExtent.height(), 
                                                  vkSurfaceCapabilities.minImageExtent().height(), 
                                                  vkSurfaceCapabilities.maxImageExtent().height()));             
            CVKLOGGER.log(Level.INFO, "Ideal extent will be {0}x{1}", new Object[]{vkCurrentSurfaceExtent.width(), vkCurrentSurfaceExtent.height()});
        }
        else {
            CVKLOGGER.log(Level.SEVERE, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR failed with error: {0}", ret);
        }
        
        
        
        EndLogSection("Device updating surface capalities");
        return ret;
    }   
    
    
    public void WaitIdle() {
        assert(vkDevice != null);
        vkDeviceWaitIdle(vkDevice);
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
    public int GetMemoryType(int typeFilter, int properties) {
        assert(vkPhysicalDeviceMemoryProperties != null);
        
        for (int i = 0; i < vkPhysicalDeviceMemoryProperties.memoryTypeCount(); ++i) {
            if ((typeFilter & (1 << i)) != 0 && (vkPhysicalDeviceMemoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }
        
        throw new RuntimeException(String.format("GetMemoryType failed to translate type %d with properties %d", typeFilter, properties));
    }
}
