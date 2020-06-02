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

import static org.lwjgl.vulkan.VK10.*;

public class VKUtils {
    public static void checkVKret(int retCode) throws IllegalStateException {
        if (retCode != VK_SUCCESS) {
            String desc;
            switch(retCode) {
                case VK_NOT_READY: desc = new String("VK_NOT_READY"); break;                
                case VK_TIMEOUT: desc = new String("VK_TIMEOUT"); break;  
                case VK_EVENT_SET: desc = new String("VK_EVENT_SET"); break;  
                case VK_EVENT_RESET: desc = new String("VK_EVENT_RESET"); break;  
                case VK_INCOMPLETE: desc = new String("VK_INCOMPLETE"); break;  
                case VK_ERROR_OUT_OF_HOST_MEMORY: desc = new String("VK_ERROR_OUT_OF_HOST_MEMORY"); break;  
                case VK_ERROR_OUT_OF_DEVICE_MEMORY: desc = new String("VK_ERROR_OUT_OF_DEVICE_MEMORY"); break;  
                case VK_ERROR_INITIALIZATION_FAILED: desc = new String("VK_ERROR_INITIALIZATION_FAILED"); break;  
                case VK_ERROR_DEVICE_LOST: desc = new String("VK_ERROR_DEVICE_LOST"); break;  
                case VK_ERROR_MEMORY_MAP_FAILED: desc = new String("VK_ERROR_MEMORY_MAP_FAILED"); break;  
                case VK_ERROR_LAYER_NOT_PRESENT: desc = new String("VK_ERROR_LAYER_NOT_PRESENT"); break;  
                case VK_ERROR_EXTENSION_NOT_PRESENT: desc = new String("VK_ERROR_EXTENSION_NOT_PRESENT"); break;  
                case VK_ERROR_FEATURE_NOT_PRESENT: desc = new String("VK_ERROR_FEATURE_NOT_PRESENT"); break;  
                case VK_ERROR_INCOMPATIBLE_DRIVER: desc = new String("VK_ERROR_INCOMPATIBLE_DRIVER"); break;  
                case VK_ERROR_TOO_MANY_OBJECTS: desc = new String("VK_ERROR_TOO_MANY_OBJECTS"); break;  
                case VK_ERROR_FORMAT_NOT_SUPPORTED: desc = new String("VK_ERROR_FORMAT_NOT_SUPPORTED"); break;  
                case VK_ERROR_FRAGMENTED_POOL: desc = new String("VK_ERROR_FRAGMENTED_POOL"); break;  
                default: desc = String.format("Vulkan error [0x%X]", retCode);
            }
            throw new IllegalStateException(desc); 
        }
    }
}
