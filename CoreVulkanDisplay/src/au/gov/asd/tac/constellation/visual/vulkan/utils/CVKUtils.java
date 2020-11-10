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
package au.gov.asd.tac.constellation.visual.vulkan.utils;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTMetalSurface.VK_EXT_METAL_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
import static org.lwjgl.system.MemoryStack.stackPush;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;


public class CVKUtils {
    
    // Constants
    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
    public static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    public static final Matrix44f IDENTITY_44F = Matrix44f.identity();
    
    // Extra error codes that don't collide with VkResult
    public static final int CVK_ERROR_INVALID_ARGS                              = 0xFFFF0000;
    public static final int CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY                  = 0xFFFF0001;
    public static final int CVK_ERROR_BUFFER_TOO_SMALL_FOR_COPY                 = 0xFFFF0002;
    public static final int CVK_ERROR_INVALID_IMAGE                             = 0xFFFF0003;
    public static final int CVK_ERROR_SHADER_COMPILATION                        = 0xFFFF0004;
    public static final int CVK_ERROR_SHADER_MODULE                             = 0xFFFF0005;
    public static final int CVK_SURFACE_UNSUPPORTED                             = 0xFFFF0006;
    public static final int CVK_ERROR_OUT_OF_MEMORY                             = 0xFFFF0007;
    public static final int CVK_ERROR_IMAGE_VIEW_CREATION_FAILED                = 0xFFFF0008;
    public static final int CVK_ERROR_SAVE_TO_FILE_FAILED                       = 0xFFFF0009;
    public static final int CVK_ERROR_IMAGE_GET_ID_FAILED                       = 0xFFFF000A;
    public static final int CVK_ERROR_DEST_IMAGE_CREATE_FAILED                  = 0xFFFF000B;
    public static final int CVK_ERROR_HITTEST_SOURCE_IMAGE_CREATE_FAILED        = 0xFFFF000C;
    public static final int CVK_ERROR_HITTEST_DEPTH_IMAGE_CREATE_FAILED         = 0xFFFF000D;
    public static final int CVK_ERROR_MD5_ALGORITHM_LOAD_FAILED                 = 0xFFFF000E;
    public static final int CVK_ERROR_SHADER_SOURCE_LOAD_FAILED                 = 0xFFFF000F;
    public static final int CVK_ERROR_SHADER_COMPILER_LOAD_FAILED               = 0xFFFF0010;
    public static final int CVK_ERROR_SHADER_TYPE_UNKNOWN                       = 0xFFFF0011;
    public static final int CVK_ERROR_SHADER_SPIRV_WRITE_FAILED                 = 0xFFFF0012;
    public static final int CVK_ERROR_SHADER_MD5_WRITE_FAILED                   = 0xFFFF0013;
    public static final int CVK_ERROR_SHADER_SOURCE_FILE_NOT_FOUND              = 0xFFFF0014;
    public static final int CVK_RENDERABLE_INITIALISATION_FAILED                = 0xFFFF0015;
    
    
    // Enable this for additional logging, thread verification and other checks
    public static final boolean CVK_DEBUGGING = true;
    public static final Level CVK_DEFAULT_LOG_LEVEL = Level.FINE;  
    public static int CVK_VKALLOCATIONS = 0;
   

    public static void LogStackTrace(Level level) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement el : stackTrace) {
            CVKGraphLogger.GetStaticLogger().log(level, el.toString());
        }
    }
    public static String GetMethodName(final int depth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > depth) {
            return String.format("%s (%s:%d)",
                    stackTrace[depth].getMethodName(),
                    stackTrace[depth].getFileName(),
                    stackTrace[depth].getLineNumber());
        } else {
            return "UNABLE TO GET METHOD NAME";
        }      
    }
    public static String GetCurrentMethodName() {
        return GetMethodName(3);   
    }    
    public static String GetParentMethodName() {
        return GetMethodName(4);   
    }

    public static boolean LayerPresent(VkLayerProperties.Buffer layers, String layer) {
        for (int i = 0; i < layers.limit(); ++i) {
            if (layers.get(i).layerNameString().equals(layer)) {
                return true;
            }
        }
        return false;
    }    
    
    public static void checkVKret(int retCode) throws IllegalStateException {
        if (retCode != VK_SUCCESS) {
            String desc;
            if (CVKMissingEnums.VkResult.KnownValue(retCode)) {
                CVKMissingEnums.VkResult result = CVKMissingEnums.VkResult.GetByValue(retCode);
                desc = result.name();
            }
            else {
                desc = String.format("Vulkan error [0x%X]", retCode);
            }          
            CVKGraphLogger.GetStaticLogger().severe(String.format("SEVERE: checkVKret failed, %s\nStack:", desc));
            LogStackTrace(Level.SEVERE);
            CVKGraphLogger.GetStaticLogger().severe("Exception incoming");
            
            throw new IllegalStateException(desc); 
        }
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
    public static PointerBuffer GetRequiredVKPhysicalDeviceExtensions(MemoryStack stack) {
        ByteBuffer VK_EXT_DEBUG_UTILS_EXTENSION = stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        ByteBuffer VK_KHR_SURFACE_EXTENSION = stack.UTF8(VK_KHR_SURFACE_EXTENSION_NAME);  
        ByteBuffer VK_EXT_DEBUG_REPORT_EXTENSION = stack.UTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);  
     
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

        PointerBuffer pbEnabledExtensionNames;
        if (CVK_DEBUGGING) {
            pbEnabledExtensionNames = stack.mallocPointer(4);
            pbEnabledExtensionNames.put(VK_EXT_DEBUG_UTILS_EXTENSION);
            pbEnabledExtensionNames.put(VK_EXT_DEBUG_REPORT_EXTENSION);
            pbEnabledExtensionNames.put(VK_KHR_SURFACE_EXTENSION);
            pbEnabledExtensionNames.put(VK_KHR_OS_SURFACE_EXTENSION);
        } else {
            pbEnabledExtensionNames = stack.mallocPointer(2);
            pbEnabledExtensionNames.put(VK_KHR_SURFACE_EXTENSION);
            pbEnabledExtensionNames.put(VK_KHR_OS_SURFACE_EXTENSION);            
        }

        // Flipping an org.lwjgl.system.CustomBuffer ends writes and prepares it for reads.  In practice
        // this resets the current index to 0
        pbEnabledExtensionNames.flip();
        return pbEnabledExtensionNames;
    }

    public static PointerBuffer GetRequiredVKLogicalDeviceExtensions(MemoryStack stack) {
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
    private final static Level VALIDATION_LAYER_LOG_LEVEL = Level.INFO;
    private static PointerBuffer pbVKValidationLayers = null;
    public static PointerBuffer InitVKValidationLayers() {
        if (pbVKValidationLayers == null) {
            try (MemoryStack stack = stackPush()) {  
                IntBuffer pInt = stack.mallocInt(1);
                pInt.put(0);
                pInt.flip();

                // Get the count of available layers
                int ret = vkEnumerateInstanceLayerProperties(pInt, null);
                checkVKret(ret);
                int layerCount = pInt.get(0);
                if (CVKGraphLogger.GetStaticLogger().isLoggable(VALIDATION_LAYER_LOG_LEVEL)) {
                    CVKGraphLogger.GetStaticLogger().log(VALIDATION_LAYER_LOG_LEVEL, "Vulkan has %d available layers.", layerCount);
                }

                // Get available layers
                VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(layerCount, stack);
                checkVKret(vkEnumerateInstanceLayerProperties(pInt, availableLayers));
                if (CVKGraphLogger.GetStaticLogger().isLoggable(VALIDATION_LAYER_LOG_LEVEL)) {
                    for (int i = 0; i < layerCount; ++i) {
                        availableLayers.position(i);
                        String layerDesc = availableLayers.descriptionString();
                        CVKGraphLogger.GetStaticLogger().log(VALIDATION_LAYER_LOG_LEVEL, "\tVulkan layer %d: %s", i, layerDesc);          
                    }
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

                pbVKValidationLayers = memAllocPointer(validationLayers.size());
                validationLayers.forEach(layer -> {
                    pbVKValidationLayers.put(memASCII(layer));
                });
                pbVKValidationLayers.flip();                
            }
        }
        
        return pbVKValidationLayers;
    }    
    
    
    //==========================================================================
    // DEBUGGING CODE - ASSERTS
    //
    // Note the code below was originally profiled in CVKCanvas but moved here as
    // they inform the explanation for the different assert methods.
    //
    // The blob below ran 5-6 times faster for CVKAssertNotNull when CVK_DEBUGGING
    // was false.  This may be due to Java knowing it doesn't need to dereference
    // the object when the body of CVKAssertNotNull is essentially a noop when
    // CVK_DEBUGGING is false.
    //            
    //        final int COUNT = 10000000; 
    //        long startCount = System.nanoTime();
    //        for (int i = 0; i < COUNT; ++i) {
    //            CVKAssert(cvkRenderer != null);
    //        }
    //        long endCount = System.nanoTime();
    //        float elapsedMilliSeconds = (endCount-startCount) / 1000.0f;
    //        cvkRenderer.GetLogger().severe("%d CVKAssert(cvkRenderer != null) took %f milliseconds with CVK_DEBUGGING = %s",
    //                COUNT, elapsedMilliSeconds, CVK_DEBUGGING ? "true" : "false");      
    //        
    //        startCount = System.nanoTime();
    //        for (int i = 0; i < COUNT; ++i) {
    //            CVKAssertNotNull(cvkRenderer);
    //        }
    //        endCount = System.nanoTime();
    //        elapsedMilliSeconds = (endCount-startCount) / 1000.0f;
    //        cvkRenderer.GetLogger().severe("%d CVKAssertNotNull(cvkRenderer != null) took %f milliseconds with CVK_DEBUGGING = %s",
    //                COUNT, elapsedMilliSeconds, CVK_DEBUGGING ? "true" : "false");  
    //        
    //
    // This hokey bit of code confirms that JAVA isn't evaluating the body of
    // VerifyInRenderThread() if CVK_DEBUGGING is false as the timings are more
    // than 1000 faster when CVK_DEBUGGING is false.
    //         
    //        final int COUNT = 10000000; 
    //        long startCount = System.nanoTime();
    //        for (int i = 0; i < COUNT; ++i) {
    //            cvkRenderer.VerifyInRenderThread();
    //        }
    //        long endCount = System.nanoTime();
    //        float elapsedMilliSeconds = (endCount-startCount) / 1000.0f;
    //        cvkRenderer.GetLogger().severe("%d VerifyInRenderThread took %f milliseconds with CVK_DEBUGGING = %s",
    //                COUNT, elapsedMilliSeconds, CVK_DEBUGGING ? "true" : "false");        
    //==========================================================================
    
    
    public static void CVKAssert(boolean exprResult) {
        if (CVK_DEBUGGING && !exprResult) {
            String msg = "CVKAssert fired";
            
            // If run from Netbeans the system console is null
            if (System.console() == null) {
                throw new RuntimeException(msg);
            } else {
                CVKGraphLogger.GetStaticLogger().severe(msg);
                LogStackTrace(Level.SEVERE);
            }
        }
    }
    
    public static void CVKAssertNotNull(Object object) {
        if (CVK_DEBUGGING && object == null) {            
            String msg = "CVKAssertNotNull(object) fired";
            
            // If run from Netbeans the system console is null
            if (System.console() == null) {
                throw new RuntimeException(msg);
            } else {
                CVKGraphLogger.GetStaticLogger().severe(msg);
                LogStackTrace(Level.SEVERE);
            }
        }
    }  
    
    public static void CVKAssertNotNull(long handle) {
        if (CVK_DEBUGGING && handle == VK_NULL_HANDLE) {            
            String msg = "CVKAssertNotNull(handle) fired";
            
            // If run from Netbeans the system console is null
            if (System.console() == null) {
                throw new RuntimeException(msg);
            } else {
                CVKGraphLogger.GetStaticLogger().severe(msg);
                LogStackTrace(Level.SEVERE);
            }
        }
    }    
    
    public static void CVKAssertNull(Object object) {
        if (CVK_DEBUGGING && object != null) {
            // If run from Netbeans the system console is null
            String msg = String.format("CVKAssertNull(object) (%s was supposed to be null) fired", object.toString());
            if (System.console() == null) {
                throw new RuntimeException(msg);
            } else {
                CVKGraphLogger.GetStaticLogger().severe(msg);
                LogStackTrace(Level.SEVERE);
            }
        }        
    }
    
    public static void CVKAssertNull(long handle) {
        if (CVK_DEBUGGING && handle != VK_NULL_HANDLE) {
            // If run from Netbeans the system console is null
            String msg = String.format("CVKAssertNull(handle) (%d(0x%016X) was supposed to be null) fired", handle, handle);
            if (System.console() == null) {
                throw new RuntimeException(msg);
            } else {
                CVKGraphLogger.GetStaticLogger().severe(msg);
                LogStackTrace(Level.SEVERE);
            }
        }        
    }    
    
    public static void CVKAssertNotNullOrEmpty(String str) {
        if (CVK_DEBUGGING) {
            if (str == null) {
                // If run from Netbeans the system console is null
                String msg = String.format("CVKAssertNotNullOrEmpty(string) fired (string was null)");
                if (System.console() == null) {
                    throw new RuntimeException(msg);
                } else {
                    CVKGraphLogger.GetStaticLogger().severe(msg);
                    LogStackTrace(Level.SEVERE);
                }
            } else if (str.isEmpty()) {
                // If run from Netbeans the system console is null
                String msg = String.format("CVKAssertNotNullOrEmpty(string) fired (string was empty)");
                if (System.console() == null) {
                    throw new RuntimeException(msg);
                } else {
                    CVKGraphLogger.GetStaticLogger().severe(msg);
                    LogStackTrace(Level.SEVERE);
                }                
            }
        }           
    }
    
    /**
     *
     * @param refClass
     * @param resourceName
     * @return
     * @throws IOException
     */
    public static ByteBuffer LoadFileToDirectBuffer(final Class<?> refClass, final String resourceName) throws IOException {
        InputStream source = refClass.getResourceAsStream(resourceName);        
        byte[] allBytes = source.readAllBytes();
        
        ByteBuffer buffer = MemoryUtil.memAlloc(allBytes.length);
        buffer.put(allBytes);
        buffer.flip();
        return buffer;          
    }
    

   
        
    public static boolean VkSucceeded(int ret) { return ret == VK_SUCCESS; }
    public static boolean VkFailed(int ret) { return !VkSucceeded(ret); }
    
    public static VkClearValue getClearValueColor(Vector3f clearColor){
    	
    	VkClearValue clearValues = VkClearValue.calloc();
        clearValues.color()
                .float32(0, clearColor.getX())
                .float32(1, clearColor.getY())
                .float32(2, clearColor.getZ())
                .float32(3, 1.0f);
        
        return clearValues;
    }
    
    public static VkClearColorValue getClearColorValue(){
    	
    	VkClearColorValue clearValues = VkClearColorValue.calloc();
        clearValues
                .float32(0, 0.0f)
                .float32(1, 0.0f)
                .float32(2, 0.0f)
                .float32(3, 1.0f);
        
        return clearValues;
    }
    
    public static VkClearValue getClearValueDepth(){
    	
    	VkClearValue clearValues = VkClearValue.calloc();
        clearValues.depthStencil()
        		.depth(1.0f);
        
        return clearValues;
    }  
}
