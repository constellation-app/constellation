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

import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.beans.Beans;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTMetalSurface.VK_EXT_METAL_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;


public class CVKUtils {
    
    // Constants
    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
    
    // Extra error codes that don't collide with VkResult
    public static final int CVK_ERROR_INVALID_ARGS                              = 0xFFFF0000;
    public static final int CVK_ERROR_IMAGE_TOO_SMALL_FOR_COPY                  = 0xFFFF0001;
    public static final int CVK_ERROR_BUFFER_TOO_SMALL_FOR_COPY                 = 0xFFFF0002;
    public static final int CVK_ERROR_INVALID_IMAGE                             = 0xFFFF0003;
    
    // Remove this once we are sure everything is working, but for now ensure all render ops happen in the render thread
    // TODO_TT: !!!THIS WILL ONLY WORK FOR A SINGLE GRAPH, MULTIPLE GRAPHS WILL TRIP THIS !!!
    public static long renderThreadID = 0;    
    
    // Logger shared by all of Constellation's Vulkan classes with a minimal formatter
    // as a proxy for the IDE's console window (as prints to stdout aren't appearing).
    public final static Logger CVKLOGGER = CreateNamedFileLogger("CVK");
    public final static Level DEFAULT_LOG_LEVEL = Level.INFO;    
    
    // Enable this for additional logging, thread verification and other checks
    public static boolean debugging = true;
    
    
    public static class MinimalLogFormatter extends Formatter {
        public static int indent = 0;
        public final static int PADLEN = 30;
        
        @Override
        public String format(LogRecord record) {
            // This does all the VA_ARGS formatting
            String msg = formatMessage(record);
            
            // Don't fuss about with line and file for empty lines
            if (msg.isBlank()) {
                return msg;
            }
            
            // StartLogSection increments indent 
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < indent; ++i) {
                lineBuilder.append("    ");
            }
            
            // With a grand total of 1 observation, the call we are interested
            // is at element 8
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length >= 8) {
                StackTraceElement ste = stackTrace[8];
                String fileAndLine = String.format("%s:%d>", ste.getFileName(), ste.getLineNumber());
                lineBuilder.append(fileAndLine);
                
                // Right pad (additional to indent padding) up to PADLEN so we have 
                // table like output for records at the same indent level                
                int padding = PADLEN - fileAndLine.length();
                for (int i = 0; i < padding; ++i) {
                    lineBuilder.append(" ");
                }                
            }              
            
            StringBuilder sb = new StringBuilder();
            String msgLines[] = msg.split("\\r?\\n");
            for (String msgLine : msgLines) {
                if (!msgLine.isBlank()) {
                    sb.append(lineBuilder.toString());
                    sb.append(msgLine);
                }
                sb.append(System.getProperty("line.separator"));
            }                               
            return sb.toString();
        }         
    }       
    
    public static Logger CreateNamedFileLogger(String name) {
        return CreateNamedFileLogger(name, DEFAULT_LOG_LEVEL);
    }
    public static Logger CreateNamedFileLogger(String name, Level level) {        
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);

        // Delete old log
        final String logName = String.format("%s.log", name);
        try {         
            File oldLog = new File(logName);
            oldLog.delete();                
        }  
        catch(Exception e) {   
            // old log doesn't exist or is locked, oh well keep going
        }  

        try {
            FileHandler fileHandler = new FileHandler(logName);
            fileHandler.setFormatter(new MinimalLogFormatter());
            logger.addHandler(fileHandler);              
        } catch (IOException e) {
            logger.log(Level.WARNING, "Logger failed to create {0}, exception: {1}",
                    new Object[]{logName, e.toString()});
        }

        StreamHandler streamHanlder = new StreamHandler(System.out, new MinimalLogFormatter());
        logger.addHandler(streamHanlder);   
        
        return logger;
    }
    
    
    public static void StartLogSection(String msg) {
        CVKLOGGER.log(Level.INFO, "{0}---- START {1} ----", new Object[]{System.getProperty("line.separator"), msg});        
        ++CVKUtils.MinimalLogFormatter.indent;
    }
    public static void EndLogSection(String msg) {
        --CVKUtils.MinimalLogFormatter.indent;
        CVKLOGGER.log(Level.INFO, "---- END {1} ----{0}{0}", new Object[]{System.getProperty("line.separator"), msg});        
    }
    public static void LogStackTrace() {
        LogStackTrace(Level.INFO);
    }
    public static void LogStackTrace(Level level) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement el : stackTrace) {
            CVKLOGGER.log(level, el.toString());
        }
    }

    public static boolean LayerPresent(VkLayerProperties.Buffer layers, String layer) {
        for (int i = 0; i < layers.limit(); ++i) {
            if (layers.get(i).layerNameString().equals(layer)) {
                return true;
            }
        }
        return false;
    }
        
    public static void VerifyInRenderThread() {
        if (renderThreadID != 0 && (renderThreadID != Thread.currentThread().getId())) {
            throw new RuntimeException(String.format("Error: render operation performed from thread %d, render thread %d",
                    Thread.currentThread().getId(), renderThreadID));
        }
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
            CVKLOGGER.severe(String.format("SEVERE: checkVKret failed, %s\nStack:", desc));
            LogStackTrace(Level.SEVERE);
            CVKLOGGER.severe("Exception incoming");
            
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

        PointerBuffer pbEnabledExtensionNames = stack.mallocPointer(3);
        pbEnabledExtensionNames.put(VK_EXT_DEBUG_UTILS_EXTENSION);
        pbEnabledExtensionNames.put(VK_KHR_SURFACE_EXTENSION);
        pbEnabledExtensionNames.put(VK_KHR_OS_SURFACE_EXTENSION);

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
    public static PointerBuffer InitVKValidationLayers(MemoryStack stack) {
        IntBuffer pInt = stack.mallocInt(1);
        pInt.put(0);
        pInt.flip();

        // Get the count of available layers
        checkVKret(vkEnumerateInstanceLayerProperties(pInt, null));
        int layerCount = pInt.get(0);
        CVKLOGGER.log(Level.INFO, "Vulkan has {0} available layers.", layerCount);

        // Get available layers
        VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(layerCount, stack);
        checkVKret(vkEnumerateInstanceLayerProperties(pInt, availableLayers));
        for (int i = 0; i < layerCount; ++i) {
            availableLayers.position(i);
            String layerDesc = availableLayers.descriptionString();
            CVKLOGGER.log(Level.INFO, "Vulkan layer {0}: {1}", new Object[]{i, layerDesc});
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
        validationLayers.forEach(layer -> {
            pbValidationLayers.put(stack.ASCII(layer));
        });
        pbValidationLayers.flip();
        return pbValidationLayers;
    }    
    
    public static void CVKAssert(boolean exprResult) {
        if (!exprResult) {
            // If run from Netbeans the system console is null
            if (System.console() == null) {
                throw new RuntimeException("CVKAssert fired");
            } else {
                CVKLOGGER.warning("!!!!!!!!!!!!!!!Assertion failure!!!!!!!!!!!!!!!!!");
                LogStackTrace(Level.WARNING);
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
