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

import static au.gov.asd.tac.constellation.visual.vulkan.VKUtils.checkVKret;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import org.lwjgl.system.Platform;
import static org.lwjgl.vulkan.EXTMetalSurface.VK_EXT_METAL_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class VKRenderer {

    private static final Logger LOGGER = Logger.getLogger(VKRenderer.class.getName());
    protected VkInstance vkInstance = null;
    protected VkQueue vkQueue = null;
    protected VkDevice vkDevice = null;
    protected VkPhysicalDevice vkPhysicalDevice = null;
    protected VkPhysicalDeviceProperties vkPhysicalDeviceProperties = null;
    protected VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures = null;
    protected VkSurfaceCapabilitiesKHR vkSurfaceCapablities = null;
    protected VkSurfaceFormatKHR.Buffer vkSurfaceFormats = null;
    protected IntBuffer presentationModes = null;
    protected int queueFamilyIndex = -1;
    protected long surfaceHandle = 0;
    protected static boolean debugging = true;
    
    static {
        if (debugging) {
            final String logName = "VKRenderer.log";
            try {
                LOGGER.setUseParentHandlers(false);
                LOGGER.setLevel(Level.INFO);
                
                // Delete old log
                try {         
                    File oldLog = new File(logName);
                    oldLog.delete();                
                }  
                catch(Exception e)  
                {  
                    e.printStackTrace();  
                }  
                
                FileHandler fileHandler = new FileHandler(logName);
                SimpleFormatter simpleFormatter = new SimpleFormatter();
                fileHandler.setFormatter(simpleFormatter);
                LOGGER.addHandler(fileHandler);              
                
                StreamHandler streamHanlder = new StreamHandler(System.out, simpleFormatter);
                LOGGER.addHandler(streamHanlder);
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Exception initialising logger: {0}", exception.toString());
            }
        }
    }
        

    private boolean LayerPresent(VkLayerProperties.Buffer layers, String layer) {
        for (int i = 0; i < layers.limit(); ++i) {
            if (layers.get(i).layerNameString().equals(layer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a PointerBuffer of extensions required for our physical device to
     * be able to render on a surface provided by an AWT canvas.
     * <p>
     * To use an AWT canvas as a rendering surface we need a VkSurfaceKHR. When
     * creating the VkInstance we need the following extensions. Note that KHR
     * indicates a KHRonos extension.
     *
     * @TODO_TT: should be in a loop like layers that enumerates first. Also add
     * VK_EXT_DEBUG_REPORT_EXTENSION_NAME?
     *
     * @param stack
     * @return PointerBuffer of extensions allocated on the provided stack
     */
    private PointerBuffer GetRequiredVKPhysicalDeviceExtensions(MemoryStack stack) {
        ByteBuffer VK_KHR_SURFACE_EXTENSION = stack.UTF8(VK_KHR_SURFACE_EXTENSION_NAME);
        ByteBuffer VK_KHR_OS_SURFACE_EXTENSION;

        switch (Platform.get()) {
            case WINDOWS:
                VK_KHR_OS_SURFACE_EXTENSION = stack.UTF8(VK_KHR_WIN32_SURFACE_EXTENSION_NAME);
                break;
            case LINUX:
                VK_KHR_OS_SURFACE_EXTENSION = stack.UTF8(VK_KHR_XLIB_SURFACE_EXTENSION_NAME);
                break;
            case MACOSX:
                VK_KHR_OS_SURFACE_EXTENSION = stack.UTF8(VK_EXT_METAL_SURFACE_EXTENSION_NAME);
                break;
            default:
                //TODO_TT add exception
                throw new RuntimeException("Unknown platform trying it initialise Vulkan");
        }

        PointerBuffer pbEnabledExtensionNames = stack.mallocPointer(2);
        pbEnabledExtensionNames.put(VK_KHR_SURFACE_EXTENSION);
        pbEnabledExtensionNames.put(VK_KHR_OS_SURFACE_EXTENSION);

        // Flipping an org.lwjgl.system.CustomBuffer ends writes and prepares it for reads.  In practice
        // this resets the current index to 0
        pbEnabledExtensionNames.flip();
        return pbEnabledExtensionNames;
    }

    private PointerBuffer GetRequiredVKLogicalDeviceExtensions(MemoryStack stack) {
        PointerBuffer pbEnabledExtensionNames = stack.mallocPointer(1);
        pbEnabledExtensionNames.put(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
        pbEnabledExtensionNames.flip();
        return pbEnabledExtensionNames;
    }

    /**
     * Returns a list of validation layers to be added to the Vulkan stack for
     * debugging.
     * <p>
     * Vulkan has no error or state checking by default to make it more
     * performant. It does however have the concept of layers that can be added
     * to the API for validation or other purposes. Here we will first look for
     * validation layers made available by the Vulkan SDK and if that's not
     * installed we'll fall back to look for LunarG validation layers.
     *
     * @param stack
     * @return PointerBuffer of validation layers allocated on the provided
     * stack
     */
    private PointerBuffer InitVKValidationLayers(MemoryStack stack) {
        IntBuffer pInt = stack.mallocInt(1);
        pInt.put(0);
        pInt.flip();

        // Get the count of available layers
        checkVKret(vkEnumerateInstanceLayerProperties(pInt, null));
        int layerCount = pInt.get(0);
        LOGGER.log(Level.INFO, "Vulkan has {0} available layers.", layerCount);

        // Get available layers
        VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(layerCount, stack);
        checkVKret(vkEnumerateInstanceLayerProperties(pInt, availableLayers));
        for (int i = 0; i < layerCount; ++i) {
            availableLayers.position(i);
            String layerDesc = availableLayers.descriptionString();
            LOGGER.log(Level.INFO, "Vulkan layer {0}: {1}", new Object[]{i, layerDesc});
        }

        // Select the best set of validation layers.  If VK_LAYER_KHRONOS_validation
        // is not available then fall back to VK_LAYER_LUNARG_standard_validation, if
        // it's not available try a bunch of older validation layers.
        // TODO_TT: these are borrowed from https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/vulkan/HelloVulkan.java
        //          Find some logic behind layer selection and codify it here.
        ArrayList<String> validationLayers = new ArrayList<>();
        if (LayerPresent(availableLayers, "VK_LAYER_KHRONOS_validation")) {
            validationLayers.add("VK_LAYER_KHRONOS_validation");
        } else if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_standard_validation")) {
            validationLayers.add("VK_LAYER_LUNARG_standard_validation");
        } else {
            if (LayerPresent(availableLayers, "VK_LAYER_GOOGLE_threading")) {
                validationLayers.add("VK_LAYER_GOOGLE_threading");
            }
            if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_parameter_validation")) {
                validationLayers.add("VK_LAYER_LUNARG_parameter_validation");
            }
            if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_core_validation")) {
                validationLayers.add("VK_LAYER_LUNARG_core_validation");
            }
            if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_object_tracker")) {
                validationLayers.add("VK_LAYER_LUNARG_object_tracker");
            }
            if (LayerPresent(availableLayers, "VK_LAYER_GOOGLE_unique_objects")) {
                validationLayers.add("VK_LAYER_GOOGLE_unique_objects");
            }
        }

        PointerBuffer pbValidationLayers = stack.mallocPointer(validationLayers.size());
        for (String layer : validationLayers) {
            pbValidationLayers.put(stack.ASCII(layer));
        }
        pbValidationLayers.flip();
        return pbValidationLayers;
    }

    /**
     * Initialises the VkInstance, the object that represents a Vulkan API
     * instantiation.
     * <p>
     * VkInstance is the LWJGL object that wraps the native vkInstance handle.
     * In OpenGL state is global, in Vulkan state is stored in the VkInstance
     * object. The create info struct is used internally to create a
     * VKCapabilitiesInstance. We need this because reasons.
     *
     * @param stack
     * @param pbExtensions
     * @param pbValidationLayers
     */
    private void InitVKInstance(MemoryStack stack, PointerBuffer pbExtensions, PointerBuffer pbValidationLayers) {
        // Create the application info struct.  This is a sub struct of the createinfo struct below
        VkApplicationInfo appInfo = VkApplicationInfo.mallocStack(stack)
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8("Constellation"))
                .applicationVersion(VK_MAKE_VERSION(1, 0, 0))
                .pEngineName(stack.UTF8("NONE"))
                .apiVersion(VK_API_VERSION_1_0);  //Highest version of Vulkan supported by Constellation

        // Create the CreateInfo struct
        VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.mallocStack()
                .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pNext(0L)
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(pbExtensions)
                .ppEnabledLayerNames(pbValidationLayers);

        // Create the native VkInstance and return a pointer to it's handle in pInstance
        PointerBuffer pInstance = stack.pointers(1);
        checkVKret(vkCreateInstance(pCreateInfo, null, pInstance));
        long instance = pInstance.get(0);

        vkInstance = new VkInstance(instance, pCreateInfo);
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
    private void InitVKPhysicalDevice(MemoryStack stack) {
        // Get the number of physical devices
        IntBuffer pInt = stack.mallocInt(1);
        checkVKret(vkEnumeratePhysicalDevices(vkInstance, pInt, null));
        if (pInt.get(0) == 0) {
            throw new RuntimeException("Vulkan: no GPUs found");
        }

        // Get the physical devices
        int numDevices = pInt.get(0);
        PointerBuffer physicalDevices = stack.mallocPointer(numDevices);
        checkVKret(vkEnumeratePhysicalDevices(vkInstance, pInt, physicalDevices));

        // Enumerate physical devices.  Stop once requirements met and physical device set.
        vkPhysicalDevice = null;
        for (int iDevice = 0; (iDevice < numDevices) && vkPhysicalDevice == null; ++iDevice) {
            // Get the count of extensions supported by this device
            VkPhysicalDevice candidate = new VkPhysicalDevice(physicalDevices.get(iDevice), vkInstance);
            pInt.put(0, 0);
            checkVKret(vkEnumerateDeviceExtensionProperties(candidate, (String) null, pInt, null));
            int numExtensions = pInt.get(0);

            if (numExtensions > 0) {
                // Get the extensions supported by this device
                VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.mallocStack(numExtensions, stack);
                checkVKret(vkEnumerateDeviceExtensionProperties(candidate, (String) null, pInt, deviceExtensions));

                // Enumerate extensins looking for swap chain support.  Stop once requirements met and physical device set.
                for (int iExtension = 0; (iExtension < numExtensions) && vkPhysicalDevice == null; ++iExtension) {
                    String extensionName = deviceExtensions.position(iExtension).extensionNameString();
                    LOGGER.log(Level.INFO, "Vulkan: device {0} extension available: {1}", new Object[]{iExtension, extensionName});
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
                                checkVKret(vkGetPhysicalDeviceSurfaceSupportKHR(candidate, iQueueFamily, surfaceHandle, pInt));
                                if (pInt.get(0) == VK_TRUE) {
                                    queueFamilyIndex = iQueueFamily;
                                    vkPhysicalDevice = candidate;
                                }
                            }
                        } //end queue family loop  
                        break; // we don't care about other extensions here
                    }  // end if extension is swapchain                    
                } // end extension loop                
            } // end if numExtensions  > 0
        } // end physical device loop

        if (vkPhysicalDevice != null) {
            // Happy dance, we have a suitable physical device, get its properties
            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.malloc();
            vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);

            // And features
            vkPhysicalDeviceFeatures = VkPhysicalDeviceFeatures.malloc();
            vkGetPhysicalDeviceFeatures(vkPhysicalDevice, vkPhysicalDeviceFeatures);

            // Device caps for our surface
            vkSurfaceCapablities = VkSurfaceCapabilitiesKHR.malloc();
            checkVKret(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkPhysicalDevice, surfaceHandle, vkSurfaceCapablities));

            // Surface formats our device can use
            pInt.put(0, 0);
            checkVKret(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surfaceHandle, pInt, null));
            int numFormats = pInt.get(0);
            if (numFormats > 0) {
                vkSurfaceFormats = VkSurfaceFormatKHR.malloc(numFormats);
                checkVKret(vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, surfaceHandle, pInt, vkSurfaceFormats));
                
                if (debugging) {
                    LOGGER.info("Available surface formats:");
                    for (int i = 0; i < numFormats; ++i) {
                        VkSurfaceFormatKHR surfaceFormat = vkSurfaceFormats.get(i);
                        VKMISSINGENUMS.VkColorSpaceKHR colorSpace = VKMISSINGENUMS.VkColorSpaceKHR.GetByValue(surfaceFormat.colorSpace());
                        VKMISSINGENUMS.VkFormat format = VKMISSINGENUMS.VkFormat.values()[surfaceFormat.format()];
                        String strColorSpace = colorSpace.name();
                        LOGGER.log(Level.INFO, "    {0}:{1}", new Object[]{format.name(), colorSpace.name()});                        
                    }
                }
            }

            // Presentation modes our device can use for our surface
            pInt.put(0, 0);
            checkVKret(vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surfaceHandle, pInt, null));
            int numPresentationModes = pInt.get(0);
            if (numPresentationModes > 0) {
                presentationModes = stack.mallocInt(numPresentationModes);
                vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, surfaceHandle, pInt, presentationModes);
                
                if (debugging) {
                    LOGGER.info("Supported presentation modes:");
                    for (int i = 0; i < numPresentationModes; ++i) {                
                        VKMISSINGENUMS.VkPresentModeKHR presentationMode = VKMISSINGENUMS.VkPresentModeKHR.values()[presentationModes.get(i)];
                        LOGGER.log(Level.INFO, "   {0}", presentationMode.name());
                    }
                }
            }
        } else {
            // Sad face
            throw new RuntimeException("Vulkan: No suitable physical device found.");
        }
    }

    /**
     * Create a logical device that gives us control over the physical device
     *
     * @param stack
     */
    protected void InitVKLogicalDevice(MemoryStack stack) {
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
        checkVKret(vkCreateDevice(vkPhysicalDevice, deviceCreateInfo, null, pb));

        vkDevice = new VkDevice(pb.get(0), vkPhysicalDevice, deviceCreateInfo);
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
     * implicit for two types above.</li>
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
    protected void InitVKQueue(MemoryStack stack) {
        PointerBuffer pb = stack.mallocPointer(1);
        vkGetDeviceQueue(vkDevice, queueFamilyIndex, 0, pb);
        long queueHandle = pb.get(0);
        vkQueue = new VkQueue(queueHandle, vkDevice);
    }

    /**
     * Initialises a swap chain which is the mechanism used to present images.
     * <p>
     * A swap chain is essentially an array of displayable buffers (vkImages).
     * They can be configured for direct rendering, double or triple buffered
     * rendering or even stereoscopic rendering.
     *
     * @param stack
     * @see
     * <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#_wsi_swapchain">Swapchain
     * reference</a>
     */
    protected void InitVKSwapChain(MemoryStack stack) {

        /*
        
        
        
        
                SwapChainSupportDetails swapChainSupport = querySwapChainSupport(physicalDevice, stack);

                VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
                int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
                VkExtent2D extent = chooseSwapExtent(swapChainSupport.capabilities);

                IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

                if(swapChainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
                    imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
                }

                VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);

                createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
                createInfo.surface(surface);

                // Image settings
                createInfo.minImageCount(imageCount.get(0));
                createInfo.imageFormat(surfaceFormat.format());
                createInfo.imageColorSpace(surfaceFormat.colorSpace());
                createInfo.imageExtent(extent);
                createInfo.imageArrayLayers(1);
                createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

                QueueFamilyIndices indices = findQueueFamilies(physicalDevice);

                if(!indices.graphicsFamily.equals(indices.presentFamily)) {
                    createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                    createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
                } else {
                    createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
                }

                createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
                createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
                createInfo.presentMode(presentMode);
                createInfo.clipped(true);

                createInfo.oldSwapchain(VK_NULL_HANDLE);

                LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

                if(vkCreateSwapchainKHR(device, createInfo, null, pSwapChain) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create swap chain");
                }

                swapChain = pSwapChain.get(0);

                vkGetSwapchainImagesKHR(device, swapChain, imageCount, null);

                LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

                vkGetSwapchainImagesKHR(device, swapChain, imageCount, pSwapchainImages);

                swapChainImages = new ArrayList<>(imageCount.get(0));

                for(int i = 0;i < pSwapchainImages.capacity();i++) {
                    swapChainImages.add(pSwapchainImages.get(i));
                }

                swapChainImageFormat = surfaceFormat.format();
                swapChainExtent = VkExtent2D.create().set(extent);
         */
    }

    /**
     *
     * <p>
     * When created vkImages contain a logical allocation but this has not been
     * backed by physical memory yet. The 3 steps to actually getting memory
     * are:
     * <ol>
     * <li>get allocation requirements</li>
     * <li>allocate a chunk of suitable memory on the device</li>
     * <li>bind that allocation to the vkImage</li>
     * </ol>
     *
     * @param stack
     */
    protected void InitVKImages(MemoryStack stack) {

    }

    /**
     *
     *
     *
     * @param surfaceHandle
     * @see
     * <a href="https://renderdoc.org/vulkan-in-30-minutes.html">https://renderdoc.org/vulkan-in-30-minutes.html</a>      *
     */
    public void InitVKRenderer(long surfaceHandle) {
        this.surfaceHandle = surfaceHandle;
        try (MemoryStack stack = stackPush()) {
            InitVKPhysicalDevice(stack);
            InitVKLogicalDevice(stack);
            InitVKQueue(stack);
            InitVKSwapChain(stack);
            InitVKImages(stack);
//            InitVKImageViews(stack);
//            InitVKRenderPass(stack);
//            InitVKFrameBuffer(stack);
//            InitVKDescriptorSetLayout(stack);
//            InitVKPipelineLayout(stack);
//            InitVKShaderModules(stack);
//            InitVKGraphicsPipeline(stack);
//            InitVKDescriptorPool(stack);
//            InitVKDescriptorSet(stack);
//            InitVKUniformBuffers(stack);
//            InitVKCommandPool(stack);
//            InitVKCommandBuffers(stack);
            /*
 
            RenderPass
            FrameBuffer
            DescriptionSetLayout
            PipelineLayout
            ShaderModules
            GraphicsPipeline
            DescriptorPool
            DescriptorSet
            Buffer (for uniforms)
            CommandPool
            CommandBuffers
            
            RenderLoop
             */
        }
    }

    /**
     *
     * @throws Exception
     */
    public VKRenderer() throws Exception {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pbValidationLayers = null;
            PointerBuffer pbExtensions = GetRequiredVKPhysicalDeviceExtensions(stack);
            if (debugging) {
                pbValidationLayers = InitVKValidationLayers(stack);
            }
            InitVKInstance(stack, pbExtensions, pbValidationLayers);
        }
    }

    @Override
    public void finalize() throws Throwable {
        vkDestroyInstance(vkInstance, null);
    }

    public void CreateSwapChain(long surface) {

    }

    public VkInstance GetVkInstance() {
        return vkInstance;
    }
}
