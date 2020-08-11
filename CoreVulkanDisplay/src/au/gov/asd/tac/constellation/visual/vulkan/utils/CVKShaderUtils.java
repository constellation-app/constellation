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

import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import org.lwjgl.system.NativeResource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import org.lwjgl.vulkan.VkDevice;


/**
 * TODO_TT: rationalise this
 * Copy of GLTools and Vulkan Tutorial
 */
public class CVKShaderUtils {
    /**
     * 
     * @param refClass Base class used as the root folder to search for shaderFile
     * @param shaderFile Name of the shader to compile
     * @param shaderKind Type of shader to compile
     * @return A SPIRV object with the compiled shader in bytes
     */
    public static SPIRV compileShaderFile( final Class<?> refClass, final String shaderFile, ShaderKind shaderKind){
        InputStream source = refClass.getResourceAsStream(shaderFile);
        try {
            String stringBytes = new String(source.readAllBytes());
            return compileShader(shaderFile, stringBytes, shaderKind);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Uses the lwjgl-shaderc library to compile a shader into SPIRV format
     * for Vulkan to use.
     * 
     * @param filename Filename of the shader
     * @param source Contents of the shader file in bytes
     * @param shaderKind Type of shader being compiled
     * @return A SPIRV object with the compiled shader in bytes
     */
    public static SPIRV compileShader(String filename, String source, ShaderKind shaderKind) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL);

        if(result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V:\n " + shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    /**
     * ShaderKind: Vertex, geometry, fragment
     */
    public enum ShaderKind {

        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

        private final int kind;

        ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    /**
     * SPIRV class - holder for the bytecode of a compiled SPIRV shader
     */
    public static final class SPIRV implements NativeResource {

        private final long handle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode) {
            this.handle = handle;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode() {
            return bytecode;
        }

        @Override
        public void free() {
            shaderc_result_release(handle);
            bytecode = null; // Help the GC
        }
    }
    
    public static long CreateShaderModule(ByteBuffer spirvCode, VkDevice device) {

        try(MemoryStack stack = stackPush()) {

            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.callocStack(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);

            int ret = vkCreateShaderModule(device, createInfo, null, pShaderModule);
            if (VkFailed(ret)) { return VK_NULL_HANDLE; }

            return pShaderModule.get(0);
        }
    }
}