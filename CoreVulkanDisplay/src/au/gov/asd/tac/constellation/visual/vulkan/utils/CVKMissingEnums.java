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



/**
 * This class contains enums that represent enums in the C Vulkan API that
 * are either missing in LWJGL or are represented by multiple static finals.
 * By mirroring these 'missing' enums we can utilise enums string reprentation
 * in Java.  In some instances they also make the code easier to follow.
 * 
 * - HERE BE DRAGONS -
 * The order of these enums and any explicit values assigned to them matches the
 * Vulkan API.  Do not reorder them, do not add entries unless at the tail, do not
 * remove entries and do not change explicit values.
 * 
 */
public class CVKMissingEnums {    
    /*
    * Some enums are are sequential, some are assigned explicit values, this
    * method ensures we always get a value that is compatible with the native
    * Vulkan API
    */
    private interface IntValue {
        public abstract int Value();
    }
    
    
    /**
     *
     */
    public enum VkColorSpaceKHR implements IntValue {
        VK_COLOR_SPACE_SRGB_NONLINEAR_KHR(0),
        VK_COLOR_SPACE_DISPLAY_P3_NONLINEAR_EXT(1000104001),
        VK_COLOR_SPACE_EXTENDED_SRGB_LINEAR_EXT(1000104002),
        VK_COLOR_SPACE_DISPLAY_P3_LINEAR_EXT(1000104003),
        VK_COLOR_SPACE_DCI_P3_NONLINEAR_EXT(1000104004),
        VK_COLOR_SPACE_BT709_LINEAR_EXT(1000104005),
        VK_COLOR_SPACE_BT709_NONLINEAR_EXT(1000104006),
        VK_COLOR_SPACE_BT2020_LINEAR_EXT(1000104007),
        VK_COLOR_SPACE_HDR10_ST2084_EXT(1000104008),
        VK_COLOR_SPACE_DOLBYVISION_EXT(1000104009),
        VK_COLOR_SPACE_HDR10_HLG_EXT(1000104010),
        VK_COLOR_SPACE_ADOBERGB_LINEAR_EXT(1000104011),
        VK_COLOR_SPACE_ADOBERGB_NONLINEAR_EXT(1000104012),
        VK_COLOR_SPACE_PASS_THROUGH_EXT(1000104013),
        VK_COLOR_SPACE_EXTENDED_SRGB_NONLINEAR_EXT(1000104014),
        VK_COLOR_SPACE_DISPLAY_NATIVE_AMD(1000213000),
        VK_COLOR_SPACE_DCI_P3_LINEAR_EXT(1000104003),
        
        // Added sentinal value
        VK_COLOR_SPACE_NONE(-1);

        private final int val;
        private VkColorSpaceKHR(int v) { val = v; }
        public static VkColorSpaceKHR GetByValue(int value) {
            for (VkColorSpaceKHR e : VkColorSpaceKHR.values()) {
                if (value == e.val) {
                    return e;
                }
            }
            return VK_COLOR_SPACE_NONE;
        }
        @Override
        public int Value() { return val; }
    }
    
    /**
     * This is a list of constant ints in VK10.java but having it as enum simplifies
     * translating values to string in Java.
     */
    public enum VkFormat implements IntValue {
        VK_FORMAT_UNDEFINED,
        VK_FORMAT_R4G4_UNORM_PACK8,
        VK_FORMAT_R4G4B4A4_UNORM_PACK16,
        VK_FORMAT_B4G4R4A4_UNORM_PACK16,
        VK_FORMAT_R5G6B5_UNORM_PACK16,
        VK_FORMAT_B5G6R5_UNORM_PACK16,
        VK_FORMAT_R5G5B5A1_UNORM_PACK16,
        VK_FORMAT_B5G5R5A1_UNORM_PACK16,
        VK_FORMAT_A1R5G5B5_UNORM_PACK16,
        VK_FORMAT_R8_UNORM,
        VK_FORMAT_R8_SNORM,
        VK_FORMAT_R8_USCALED,
        VK_FORMAT_R8_SSCALED,
        VK_FORMAT_R8_UINT,
        VK_FORMAT_R8_SINT,
        VK_FORMAT_R8_SRGB,
        VK_FORMAT_R8G8_UNORM,
        VK_FORMAT_R8G8_SNORM,
        VK_FORMAT_R8G8_USCALED,
        VK_FORMAT_R8G8_SSCALED,
        VK_FORMAT_R8G8_UINT,
        VK_FORMAT_R8G8_SINT,
        VK_FORMAT_R8G8_SRGB,
        VK_FORMAT_R8G8B8_UNORM,
        VK_FORMAT_R8G8B8_SNORM,
        VK_FORMAT_R8G8B8_USCALED,
        VK_FORMAT_R8G8B8_SSCALED,
        VK_FORMAT_R8G8B8_UINT,
        VK_FORMAT_R8G8B8_SINT,
        VK_FORMAT_R8G8B8_SRGB,
        VK_FORMAT_B8G8R8_UNORM,
        VK_FORMAT_B8G8R8_SNORM,
        VK_FORMAT_B8G8R8_USCALED,
        VK_FORMAT_B8G8R8_SSCALED,
        VK_FORMAT_B8G8R8_UINT,
        VK_FORMAT_B8G8R8_SINT,
        VK_FORMAT_B8G8R8_SRGB,
        VK_FORMAT_R8G8B8A8_UNORM,
        VK_FORMAT_R8G8B8A8_SNORM,
        VK_FORMAT_R8G8B8A8_USCALED,
        VK_FORMAT_R8G8B8A8_SSCALED,
        VK_FORMAT_R8G8B8A8_UINT,
        VK_FORMAT_R8G8B8A8_SINT,
        VK_FORMAT_R8G8B8A8_SRGB,
        VK_FORMAT_B8G8R8A8_UNORM,
        VK_FORMAT_B8G8R8A8_SNORM,
        VK_FORMAT_B8G8R8A8_USCALED,
        VK_FORMAT_B8G8R8A8_SSCALED,
        VK_FORMAT_B8G8R8A8_UINT,
        VK_FORMAT_B8G8R8A8_SINT,
        VK_FORMAT_B8G8R8A8_SRGB,
        VK_FORMAT_A8B8G8R8_UNORM_PACK32,
        VK_FORMAT_A8B8G8R8_SNORM_PACK32,
        VK_FORMAT_A8B8G8R8_USCALED_PACK32,
        VK_FORMAT_A8B8G8R8_SSCALED_PACK32,
        VK_FORMAT_A8B8G8R8_UINT_PACK32,
        VK_FORMAT_A8B8G8R8_SINT_PACK32,
        VK_FORMAT_A8B8G8R8_SRGB_PACK32,
        VK_FORMAT_A2R10G10B10_UNORM_PACK32,
        VK_FORMAT_A2R10G10B10_SNORM_PACK32,
        VK_FORMAT_A2R10G10B10_USCALED_PACK32,
        VK_FORMAT_A2R10G10B10_SSCALED_PACK32,
        VK_FORMAT_A2R10G10B10_UINT_PACK32,
        VK_FORMAT_A2R10G10B10_SINT_PACK32,
        VK_FORMAT_A2B10G10R10_UNORM_PACK32,
        VK_FORMAT_A2B10G10R10_SNORM_PACK32,
        VK_FORMAT_A2B10G10R10_USCALED_PACK32,
        VK_FORMAT_A2B10G10R10_SSCALED_PACK32,
        VK_FORMAT_A2B10G10R10_UINT_PACK32,
        VK_FORMAT_A2B10G10R10_SINT_PACK32,
        VK_FORMAT_R16_UNORM,
        VK_FORMAT_R16_SNORM,
        VK_FORMAT_R16_USCALED,
        VK_FORMAT_R16_SSCALED,
        VK_FORMAT_R16_UINT,
        VK_FORMAT_R16_SINT,
        VK_FORMAT_R16_SFLOAT,
        VK_FORMAT_R16G16_UNORM,
        VK_FORMAT_R16G16_SNORM,
        VK_FORMAT_R16G16_USCALED,
        VK_FORMAT_R16G16_SSCALED,
        VK_FORMAT_R16G16_UINT,
        VK_FORMAT_R16G16_SINT,
        VK_FORMAT_R16G16_SFLOAT,
        VK_FORMAT_R16G16B16_UNORM,
        VK_FORMAT_R16G16B16_SNORM,
        VK_FORMAT_R16G16B16_USCALED,
        VK_FORMAT_R16G16B16_SSCALED,
        VK_FORMAT_R16G16B16_UINT,
        VK_FORMAT_R16G16B16_SINT,
        VK_FORMAT_R16G16B16_SFLOAT,
        VK_FORMAT_R16G16B16A16_UNORM,
        VK_FORMAT_R16G16B16A16_SNORM,
        VK_FORMAT_R16G16B16A16_USCALED,
        VK_FORMAT_R16G16B16A16_SSCALED,
        VK_FORMAT_R16G16B16A16_UINT,
        VK_FORMAT_R16G16B16A16_SINT,
        VK_FORMAT_R16G16B16A16_SFLOAT,
        VK_FORMAT_R32_UINT,
        VK_FORMAT_R32_SINT,
        VK_FORMAT_R32_SFLOAT,
        VK_FORMAT_R32G32_UINT,
        VK_FORMAT_R32G32_SINT,
        VK_FORMAT_R32G32_SFLOAT,
        VK_FORMAT_R32G32B32_UINT,
        VK_FORMAT_R32G32B32_SINT,
        VK_FORMAT_R32G32B32_SFLOAT,
        VK_FORMAT_R32G32B32A32_UINT,
        VK_FORMAT_R32G32B32A32_SINT,
        VK_FORMAT_R32G32B32A32_SFLOAT,
        VK_FORMAT_R64_UINT,
        VK_FORMAT_R64_SINT,
        VK_FORMAT_R64_SFLOAT,
        VK_FORMAT_R64G64_UINT,
        VK_FORMAT_R64G64_SINT,
        VK_FORMAT_R64G64_SFLOAT,
        VK_FORMAT_R64G64B64_UINT,
        VK_FORMAT_R64G64B64_SINT,
        VK_FORMAT_R64G64B64_SFLOAT,
        VK_FORMAT_R64G64B64A64_UINT,
        VK_FORMAT_R64G64B64A64_SINT,
        VK_FORMAT_R64G64B64A64_SFLOAT,
        VK_FORMAT_B10G11R11_UFLOAT_PACK32,
        VK_FORMAT_E5B9G9R9_UFLOAT_PACK32,
        VK_FORMAT_D16_UNORM,
        VK_FORMAT_X8_D24_UNORM_PACK32,
        VK_FORMAT_D32_SFLOAT,
        VK_FORMAT_S8_UINT,
        VK_FORMAT_D16_UNORM_S8_UINT,
        VK_FORMAT_D24_UNORM_S8_UINT,
        VK_FORMAT_D32_SFLOAT_S8_UINT,
        VK_FORMAT_BC1_RGB_UNORM_BLOCK,
        VK_FORMAT_BC1_RGB_SRGB_BLOCK,
        VK_FORMAT_BC1_RGBA_UNORM_BLOCK,
        VK_FORMAT_BC1_RGBA_SRGB_BLOCK,
        VK_FORMAT_BC2_UNORM_BLOCK,
        VK_FORMAT_BC2_SRGB_BLOCK,
        VK_FORMAT_BC3_UNORM_BLOCK,
        VK_FORMAT_BC3_SRGB_BLOCK,
        VK_FORMAT_BC4_UNORM_BLOCK,
        VK_FORMAT_BC4_SNORM_BLOCK,
        VK_FORMAT_BC5_UNORM_BLOCK,
        VK_FORMAT_BC5_SNORM_BLOCK,
        VK_FORMAT_BC6H_UFLOAT_BLOCK,
        VK_FORMAT_BC6H_SFLOAT_BLOCK,
        VK_FORMAT_BC7_UNORM_BLOCK,
        VK_FORMAT_BC7_SRGB_BLOCK,
        VK_FORMAT_ETC2_R8G8B8_UNORM_BLOCK,
        VK_FORMAT_ETC2_R8G8B8_SRGB_BLOCK,
        VK_FORMAT_ETC2_R8G8B8A1_UNORM_BLOCK,
        VK_FORMAT_ETC2_R8G8B8A1_SRGB_BLOCK,
        VK_FORMAT_ETC2_R8G8B8A8_UNORM_BLOCK,
        VK_FORMAT_ETC2_R8G8B8A8_SRGB_BLOCK,
        VK_FORMAT_EAC_R11_UNORM_BLOCK,
        VK_FORMAT_EAC_R11_SNORM_BLOCK,
        VK_FORMAT_EAC_R11G11_UNORM_BLOCK,
        VK_FORMAT_EAC_R11G11_SNORM_BLOCK,
        VK_FORMAT_ASTC_4x4_UNORM_BLOCK,
        VK_FORMAT_ASTC_4x4_SRGB_BLOCK,
        VK_FORMAT_ASTC_5x4_UNORM_BLOCK,
        VK_FORMAT_ASTC_5x4_SRGB_BLOCK,
        VK_FORMAT_ASTC_5x5_UNORM_BLOCK,
        VK_FORMAT_ASTC_5x5_SRGB_BLOCK,
        VK_FORMAT_ASTC_6x5_UNORM_BLOCK,
        VK_FORMAT_ASTC_6x5_SRGB_BLOCK,
        VK_FORMAT_ASTC_6x6_UNORM_BLOCK,
        VK_FORMAT_ASTC_6x6_SRGB_BLOCK,
        VK_FORMAT_ASTC_8x5_UNORM_BLOCK,
        VK_FORMAT_ASTC_8x5_SRGB_BLOCK,
        VK_FORMAT_ASTC_8x6_UNORM_BLOCK,
        VK_FORMAT_ASTC_8x6_SRGB_BLOCK,
        VK_FORMAT_ASTC_8x8_UNORM_BLOCK,
        VK_FORMAT_ASTC_8x8_SRGB_BLOCK,
        VK_FORMAT_ASTC_10x5_UNORM_BLOCK,
        VK_FORMAT_ASTC_10x5_SRGB_BLOCK,
        VK_FORMAT_ASTC_10x6_UNORM_BLOCK,
        VK_FORMAT_ASTC_10x6_SRGB_BLOCK,
        VK_FORMAT_ASTC_10x8_UNORM_BLOCK,
        VK_FORMAT_ASTC_10x8_SRGB_BLOCK,
        VK_FORMAT_ASTC_10x10_UNORM_BLOCK,
        VK_FORMAT_ASTC_10x10_SRGB_BLOCK,
        VK_FORMAT_ASTC_12x10_UNORM_BLOCK,
        VK_FORMAT_ASTC_12x10_SRGB_BLOCK,
        VK_FORMAT_ASTC_12x12_UNORM_BLOCK,
        VK_FORMAT_ASTC_12x12_SRGB_BLOCK,
        
        // Added sentinal value
        VK_FORMAT_NONE;
        
        @Override
        public int Value() { return ordinal(); }
    }  
    
    public enum VkPresentModeKHR implements IntValue {
        VK_PRESENT_MODE_IMMEDIATE_KHR,
        VK_PRESENT_MODE_MAILBOX_KHR,
        VK_PRESENT_MODE_FIFO_KHR,
        VK_PRESENT_MODE_FIFO_RELAXED_KHR,
        
        // Added sentinal value
        VK_PRESENT_MODE_NONE;
        
        @Override
        public int Value() { return ordinal(); }        
    }
    
    public enum VkResult implements IntValue {
        VK_SUCCESS(0),
        VK_NOT_READY(1),
        VK_TIMEOUT(2),
        VK_EVENT_SET(3),
        VK_EVENT_RESET(4),
        VK_INCOMPLETE(5),
        VK_ERROR_OUT_OF_HOST_MEMORY(-1),
        VK_ERROR_OUT_OF_DEVICE_MEMORY(-2),
        VK_ERROR_INITIALIZATION_FAILED(-3),
        VK_ERROR_DEVICE_LOST(-4),
        VK_ERROR_MEMORY_MAP_FAILED(-5),
        VK_ERROR_LAYER_NOT_PRESENT(-6),
        VK_ERROR_EXTENSION_NOT_PRESENT(-7),
        VK_ERROR_FEATURE_NOT_PRESENT(-8),
        VK_ERROR_INCOMPATIBLE_DRIVER(-9),
        VK_ERROR_TOO_MANY_OBJECTS(-10),
        VK_ERROR_FORMAT_NOT_SUPPORTED(-11),
        VK_ERROR_FRAGMENTED_POOL(-12),
        VK_ERROR_UNKNOWN(-13),
        VK_ERROR_OUT_OF_POOL_MEMORY(-1000069000),
        VK_ERROR_INVALID_EXTERNAL_HANDLE(-1000072003),
        VK_ERROR_FRAGMENTATION(-1000161000),
        VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS(-1000257000),
        VK_ERROR_SURFACE_LOST_KHR(-1000000000),
        VK_ERROR_NATIVE_WINDOW_IN_USE_KHR(-1000000001),
        VK_SUBOPTIMAL_KHR(1000001003),
        VK_ERROR_OUT_OF_DATE_KHR(-1000001004),
        VK_ERROR_INCOMPATIBLE_DISPLAY_KHR(-1000003001),
        VK_ERROR_VALIDATION_FAILED_EXT(-1000011001),
        VK_ERROR_INVALID_SHADER_NV(-1000012000),
        VK_ERROR_INCOMPATIBLE_VERSION_KHR(-1000150000),
        VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT(-1000158000),
        VK_ERROR_NOT_PERMITTED_EXT(-1000174001),
        VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT(-1000255000),
        VK_THREAD_IDLE_KHR(1000268000),
        VK_THREAD_DONE_KHR(1000268001),
        VK_OPERATION_DEFERRED_KHR(1000268002),
        VK_OPERATION_NOT_DEFERRED_KHR(1000268003),
        VK_PIPELINE_COMPILE_REQUIRED_EXT(1000297000),
        VK_ERROR_OUT_OF_POOL_MEMORY_KHR(-1000069000),
        VK_ERROR_INVALID_EXTERNAL_HANDLE_KHR(-1000072003),
        VK_ERROR_FRAGMENTATION_EXT(-1000161000),
        VK_ERROR_INVALID_DEVICE_ADDRESS_EXT(-1000257000),
        VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS_KHR(-1000257000),
        VK_ERROR_PIPELINE_COMPILE_REQUIRED_EXT(1000297000),
                
        // Added sentinal value
        VK_RESULT_UNKNOWN(0xF2345678);

        private final int val;
        private VkResult(int v) { val = v; }
        public static VkResult GetByValue(int value) {
            for (VkResult e : VkResult.values()) {
                if (value == e.val) {
                    return e;
                }
            }
            return VK_RESULT_UNKNOWN;
        }
        public static boolean KnownValue(int value) {
            for (VkResult e : VkResult.values()) {
                if (value == e.val) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public int Value() { return val; }                
    }
    
    public enum VkPhysicalDeviceType implements IntValue {
        VK_PHYSICAL_DEVICE_TYPE_OTHER,
        VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU,
        VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU,
        VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU,
        VK_PHYSICAL_DEVICE_TYPE_CPU,
        
        // Added sentinel value
        VK_PHYSICAL_DEVICE_TYPE_NONE;
        
        @Override
        public int Value() { return ordinal(); }     
        
        public static VkPhysicalDeviceType GetByValue(int value) {
            if (value >= 0 && value < VK_PHYSICAL_DEVICE_TYPE_NONE.Value()) {
                return VkPhysicalDeviceType.values()[value];
            } else {
                return VK_PHYSICAL_DEVICE_TYPE_NONE;
            }
        }        
    }
    
    public enum VkObjectType implements IntValue {
        VK_OBJECT_TYPE_UNKNOWN(0),
        VK_OBJECT_TYPE_INSTANCE(1),
        VK_OBJECT_TYPE_PHYSICAL_DEVICE(2),
        VK_OBJECT_TYPE_DEVICE(3),
        VK_OBJECT_TYPE_QUEUE(4),
        VK_OBJECT_TYPE_SEMAPHORE(5),
        VK_OBJECT_TYPE_COMMAND_BUFFER(6),
        VK_OBJECT_TYPE_FENCE(7),
        VK_OBJECT_TYPE_DEVICE_MEMORY(8),
        VK_OBJECT_TYPE_BUFFER(9),
        VK_OBJECT_TYPE_IMAGE(10),
        VK_OBJECT_TYPE_EVENT(11),
        VK_OBJECT_TYPE_QUERY_POOL(12),
        VK_OBJECT_TYPE_BUFFER_VIEW(13),
        VK_OBJECT_TYPE_IMAGE_VIEW(14),
        VK_OBJECT_TYPE_SHADER_MODULE(15),
        VK_OBJECT_TYPE_PIPELINE_CACHE(16),
        VK_OBJECT_TYPE_PIPELINE_LAYOUT(17),
        VK_OBJECT_TYPE_RENDER_PASS(18),
        VK_OBJECT_TYPE_PIPELINE(19),
        VK_OBJECT_TYPE_DESCRIPTOR_SET_LAYOUT(20),
        VK_OBJECT_TYPE_SAMPLER(21),
        VK_OBJECT_TYPE_DESCRIPTOR_POOL(22),
        VK_OBJECT_TYPE_DESCRIPTOR_SET(23),
        VK_OBJECT_TYPE_FRAMEBUFFER(24),
        VK_OBJECT_TYPE_COMMAND_POOL(25),
        // Provided by VK_VERSION_1_1(),
        VK_OBJECT_TYPE_SAMPLER_YCBCR_CONVERSION(1000156000),
        // Provided by VK_VERSION_1_1(),
        VK_OBJECT_TYPE_DESCRIPTOR_UPDATE_TEMPLATE(1000085000),
        // Provided by VK_KHR_surface(),
        VK_OBJECT_TYPE_SURFACE_KHR(1000000000),
        // Provided by VK_KHR_swapchain(),
        VK_OBJECT_TYPE_SWAPCHAIN_KHR(1000001000),
        // Provided by VK_KHR_display(),
        VK_OBJECT_TYPE_DISPLAY_KHR(1000002000),
        // Provided by VK_KHR_display(),
        VK_OBJECT_TYPE_DISPLAY_MODE_KHR(1000002001),
        // Provided by VK_EXT_debug_report(),
        VK_OBJECT_TYPE_DEBUG_REPORT_CALLBACK_EXT(1000011000),
        // Provided by VK_EXT_debug_utils(),
        VK_OBJECT_TYPE_DEBUG_UTILS_MESSENGER_EXT(1000128000),
        // Provided by VK_KHR_ray_tracing(),
        VK_OBJECT_TYPE_ACCELERATION_STRUCTURE_KHR(1000165000),
        // Provided by VK_EXT_validation_cache(),
        VK_OBJECT_TYPE_VALIDATION_CACHE_EXT(1000160000),
        // Provided by VK_INTEL_performance_query(),
        VK_OBJECT_TYPE_PERFORMANCE_CONFIGURATION_INTEL(1000210000),
        // Provided by VK_KHR_deferred_host_operations(),
        VK_OBJECT_TYPE_DEFERRED_OPERATION_KHR(1000268000),
        // Provided by VK_NV_device_generated_commands(),
        VK_OBJECT_TYPE_INDIRECT_COMMANDS_LAYOUT_NV(1000277000),
        // Provided by VK_EXT_private_data(),
        VK_OBJECT_TYPE_PRIVATE_DATA_SLOT_EXT(1000295000),
        // Provided by VK_KHR_descriptor_update_template(),
        VK_OBJECT_TYPE_DESCRIPTOR_UPDATE_TEMPLATE_KHR(VK_OBJECT_TYPE_DESCRIPTOR_UPDATE_TEMPLATE.Value()),
        // Provided by VK_KHR_sampler_ycbcr_conversion(),
        VK_OBJECT_TYPE_SAMPLER_YCBCR_CONVERSION_KHR(VK_OBJECT_TYPE_SAMPLER_YCBCR_CONVERSION.Value()),
        // Provided by VK_NV_ray_tracing(),
        VK_OBJECT_TYPE_ACCELERATION_STRUCTURE_NV(VK_OBJECT_TYPE_ACCELERATION_STRUCTURE_KHR.Value());      
        
        private final int val;
        private VkObjectType(int v) { val = v; }        
        @Override
        public int Value() { return val; }    
    }
}
