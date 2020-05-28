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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.Platform;
import static org.lwjgl.vulkan.EXTMetalSurface.VK_EXT_METAL_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;



public class VKRenderer {
    
    private static final Logger LOGGER = Logger.getLogger(VKRenderer.class.getName());
    protected VkInstance vkInstance = null;
    protected VkPhysicalDevice vkPhysicalDevice = null;
    protected VkPhysicalDeviceProperties vkPhysicalDeviceProperties = null;
    protected long surfaceHandle;
    protected boolean debugging = true;
   
    //TODO_TT: move to utils
    public static void checkVKret(int retCode) throws IllegalStateException {
        if (retCode != VK_SUCCESS) {
            throw new IllegalStateException(String.format("Vulkan error [0x%X]", retCode)); 
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
     * Returns a PointerBuffer of required extensions
     * <p>
     * To use an AWT canvas as a rendering surface we need a VkSurfaceKHR. When
     * creating the VkInstance we need the following extensions. Note that 
     * KHR indicates a KHRonos extension.
     *
     * @TODO_TT: should be in a loop like layers that enumerates first.  Also add
     *           VK_EXT_DEBUG_REPORT_EXTENSION_NAME?
     * 
     * @param stack
     * @return PointerBuffer of extensions allocated on the provided stack
     */
    private PointerBuffer InitVKExtensions(MemoryStack stack) {
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
    
    /**
     * Returns a list of validation layers to be added to the Vulkan stack for debugging.
     * <p>
     * Vulkan has no error or state checking by default to make it more performant.  It does
     * however have the concept of layers that can be added to the API for validation
     * or other purposes.  Here we will first look for validation layers made available
     * by the Vulkan SDK and if that's not installed we'll fall back to look for LunarG
     * validation layers.
     * 
     * @param stack
     * @return PointerBuffer of validation layers allocated on the provided stack
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
     * Initialises the VkInstance, the object that represents a Vulkan API instantiation.
     * <p>
     * VkInstance is the LWJGL object that wraps the native vkInstance handle. 
     * In OpenGL state is global, in Vulkan state is stored in the VkInstance object. 
     * The create info struct is used internally to create a VKCapabilitiesInstance.  
     * We need this because reasons.
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
     * Enumerates physical devices and selects the first to support swap chains and with a queue family that
     * allows display (graphics operations and ability to present to our surface).
     * 
     * Vulkan can be used for a number of roles (eg display or compute).  Each device has
     * a number of queue families that may be suited to a particular role.  A queue
     * family contains a set of queues or a single queue.  These queues are what we
     * submit our command buffers to.  Unlike OpenGL where in immediate mode we
     * submit commands one by one to the display driver, Vulkan batches these up in command
     * buffers that we submit.  This is more performant than immediate mode commands and it
     * allows multiple threads to submit commands to the display driver.
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
            checkVKret(vkEnumerateDeviceExtensionProperties(candidate, (String)null, pInt, null));
            int numExtensions = pInt.get(0);
            
            if (numExtensions > 0) {
                // Get the extensions supported by this device
                VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.mallocStack(numExtensions, stack);
                checkVKret(vkEnumerateDeviceExtensionProperties(candidate, (String)null, pInt, deviceExtensions));
                
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
        } else {
            // Sad face
            throw new RuntimeException("Vulkan: No suitable physical device found.");
        }                             
    }
    
    
    public void InitVKRenderer(long surfaceHandle) {
        this.surfaceHandle = surfaceHandle;
        try (MemoryStack stack = stackPush()) {            
            InitVKPhysicalDevice(stack);
        }
    }
    
    /**
     * 
     * @throws Exception 
     */
    public VKRenderer() throws Exception {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pbValidationLayers = null;
            PointerBuffer pbExtensions = InitVKExtensions(stack);
            if (debugging) {
                pbValidationLayers = InitVKValidationLayers(stack);
            }
            InitVKInstance(stack, pbExtensions, pbValidationLayers);
        }

        
        //TODO_TT:
        // get Constellation version from somehwere
        // decide what API version to use.  Latest supported by this card?  or just 1.0.0?
        
        
        
        // Java's try with resource statement will ensure this stack allocator
        // is cleaned up when it falls out of scope.  Note try with resouce doesn't
        // require a catch statement
//        try (MemoryStack stack = stackPush()) {
//            // Just an int we can pass around to various functions that need it
//            IntBuffer pInt = stack.mallocInt(1);
//            
//            // Create the application info struct
//            VkApplicationInfo appInfo = VkApplicationInfo.mallocStack(stack)
//                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
//                    .pApplicationName(stack.UTF8("Constellation"))
//                    .applicationVersion(VK_MAKE_VERSION(1, 0, 0)) 
//                    .pEngineName(stack.UTF8("NONE"))
//                    .apiVersion(VK_API_VERSION_1_0);  //Highest version of Vulkan supported by Constellation

            //========================================================================================
            // EXTENSIONS
            //
            // To use an AWT canvas as a rendering surface we need a VkSurfaceKHR. When
            // creating the VkInstance we need the following extensions. Note that 
            // KHR indicates a KHRonos extension.
            // TODO_TT: should be in a loop like layers that enumerates first.  Also add
            //          VK_EXT_DEBUG_REPORT_EXTENSION_NAME?
            //========================================================================================
//            ByteBuffer VK_KHR_SURFACE_EXTENSION = stack.UTF8(VK_KHR_SURFACE_EXTENSION_NAME);
//            ByteBuffer VK_KHR_OS_SURFACE_EXTENSION;
//
//            switch (Platform.get()) {
//                case WINDOWS:
//                    VK_KHR_OS_SURFACE_EXTENSION = stack.UTF8(VK_KHR_WIN32_SURFACE_EXTENSION_NAME);
//                    break;
//                case LINUX:
//                    VK_KHR_OS_SURFACE_EXTENSION = stack.UTF8(VK_KHR_XLIB_SURFACE_EXTENSION_NAME);
//                    break;
//                case MACOSX:
//                    VK_KHR_OS_SURFACE_EXTENSION = stack.UTF8(VK_EXT_METAL_SURFACE_EXTENSION_NAME);
//                    break;
//                default:
//                    //TODO_TT add exception
//                    throw new RuntimeException("Unknown platform trying it initialise Vulkan");
//            }
//
//            PointerBuffer ppEnabledExtensionNames = stack.mallocPointer(2);
//            ppEnabledExtensionNames.put(VK_KHR_SURFACE_EXTENSION);
//            ppEnabledExtensionNames.put(VK_KHR_OS_SURFACE_EXTENSION);
//
//            // Flipping an org.lwjgl.system.CustomBuffer ends writes and prepares it for reads.  In practice
//            // this resets the current index to 0
//            ppEnabledExtensionNames.flip();

            
//            // Create the CreateInfo struct
//            VkInstanceCreateInfo pCreateInfo = VkInstanceCreateInfo.mallocStack()
//                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
//                    .pNext(0L)
//                    .pApplicationInfo(appInfo);
//            
//
//            // For an org.lwjgl.system.CustomBuffer remaining is length - current index, so
//            // as we haven't moved the current index this is a length() > 0 call
//            if (ppEnabledExtensionNames.hasRemaining()) {
//                pCreateInfo.ppEnabledExtensionNames(ppEnabledExtensionNames);
//            }
            
            // If we're debugging check what validation layers are available.  Vulkan has
            // no error or state checking by default to make it more performant.  It does
            // however have the concept of layers that can be added to the API for validation
            // or other purposes.  Here we will first look for validation layers made available
            // by the Vulkan SDK and if that's not installed we'll fall back to look for LunarG
            // validation layers.
//            if (debugging) {                                          
//                pInt.put(0);
//                pInt.flip();
//                checkVKret(vkEnumerateInstanceLayerProperties(pInt, null));
//                int layerCount = pInt.get(0);
//                LOGGER.log(Level.INFO, "Vulkan has {0} available layers.", layerCount);
//                VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(layerCount, stack);
//                checkVKret(vkEnumerateInstanceLayerProperties(pInt, availableLayers));
//                for (int i = 0; i < layerCount; ++i) {
//                    availableLayers.position(i);
//                    String layerDesc = availableLayers.descriptionString();
//                    LOGGER.log(Level.INFO, "Vulkan layer {0}: {1}", new Object[]{i, layerDesc});                    
//                }
//                
//                // TODO_TT: these are borrowed from https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/vulkan/HelloVulkan.java
//                //          Find some logic behind layer selection and codify it here.
//                ArrayList<String> validationLayers = new ArrayList<>();
//                if (LayerPresent(availableLayers, "VK_LAYER_KHRONOS_validation")) {
//                    validationLayers.add("VK_LAYER_KHRONOS_validation");
//                } else if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_standard_validation")) {
//                    validationLayers.add("VK_LAYER_LUNARG_standard_validation");
//                } else {
//                    if (LayerPresent(availableLayers, "VK_LAYER_GOOGLE_threading")) {
//                        validationLayers.add("VK_LAYER_GOOGLE_threading");
//                    }
//                    if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_parameter_validation")) {
//                        validationLayers.add("VK_LAYER_LUNARG_parameter_validation");
//                    }
//                    if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_core_validation")) {
//                        validationLayers.add("VK_LAYER_LUNARG_core_validation");
//                    }
//                    if (LayerPresent(availableLayers, "VK_LAYER_LUNARG_object_tracker")) {
//                        validationLayers.add("VK_LAYER_LUNARG_object_tracker");
//                    }
//                    if (LayerPresent(availableLayers, "VK_LAYER_GOOGLE_unique_objects")) {
//                        validationLayers.add("VK_LAYER_GOOGLE_unique_objects");
//                    }                    
//                }
//                                
//                PointerBuffer validationLayersBuffer = stack.mallocPointer(validationLayers.size());
//                for (String layer : validationLayers) {
//                    validationLayersBuffer.put(stack.ASCII(layer));
//                }
//                validationLayersBuffer.flip();
//                if (layerCount > 0) {
//                    pCreateInfo.ppEnabledLayerNames(validationLayersBuffer);
//                }
//            }            

//            // Create the native VkInstance and return a pointer to it's handle in pInstance
//            PointerBuffer pInstance = stack.pointers(1);
//            checkVKret(vkCreateInstance(pCreateInfo, null, pInstance));
//            long instance = pInstance.get(0);
//
//             // VkInstance is the LWJGL object that wraps the native VkInstance. The VkInstance
//             // is the handle the Vulkan API.  In OpenGL state is global, in Vulkan state is 
//             // stored in the VkInstance object.  The create info struct is used internallu to
//             // create a VKCapabilitiesInstance.  We need this because reasons.
//            vkInstance = new VkInstance(instance, pCreateInfo);
            
//            // Enumerate GPUs and select the first one
//            //TODO_TT: add heuristic to select the best GPU
//            pInt.put(0, 0);
//            checkVKret(vkEnumeratePhysicalDevices(vkInstance, pInt, null));
//            if (pInt.get(0) == 0) {
//                throw new RuntimeException("Vulkan: no GPUs found");
//            }            
//            PointerBuffer physicalDevices = stack.mallocPointer(pInt.get(0));
//            checkVKret(vkEnumeratePhysicalDevices(vkInstance, pInt, physicalDevices));            
//            vkPhysicalDevice = new VkPhysicalDevice(physicalDevices.get(0), vkInstance);
//            
//            //TODO_TT: why is this malloc and not new?  What do we need these for?
//            vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.malloc();
//            vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);
//            
//            // Check the physical device supports SwapChains.  It isn't possible to 
//            // display anything without using SwapChains so why are they provided by
//            // an extension?  The answer is that Vulkan can be run headless (eg compute).
//            pInt.put(0, 0);
//            checkVKret(vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String)null, pInt, null));
//            if (pInt.get(0) == 0) {
//                throw new RuntimeException("Vulkan: first GPU has no extension support (no swapchain)");
//            }
//            VkExtensionProperties.Buffer deviceExtensions = VkExtensionProperties.mallocStack(pInt.get(0), stack);
//            checkVKret(vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String)null, pInt, deviceExtensions));
//            boolean swapChainSupported = false;
//            for (int i = 0; i < pInt.get(0); ++i) {
//                String extensionName = deviceExtensions.position(i).extensionNameString();                
//                if (VK_KHR_SWAPCHAIN_EXTENSION_NAME.equals(extensionName)) {
//                    swapChainSupported = true;
//                }
//                if (debugging) {
//                    LOGGER.log(Level.INFO, "Vulkan: extension available:{0}", extensionName);
//                }                
//            }
//            if (!swapChainSupported) {
//                throw new RuntimeException("Vulkan: SwapChain extension not supported by first GPU");
//            }            
//        }
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
