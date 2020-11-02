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

import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.*;
import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import java.io.File;
import java.io.FileInputStream;
import org.lwjgl.system.NativeResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;


public class CVKShaderUtils {
    private static HashMap<String, Long> SHADER_MAP = new HashMap<>();
    private static MessageDigest md5 = null;
    private static boolean shaderCompilerVerified = false;
    
    /**
     * 
     * @param refClass Base class used as the root folder to search for shaderFile
     * @param shaderFile Name of the shader to compile
     * @param shaderKind Type of shader to compile
     * @return A SPIRV object with the compiled shader in bytes
     */
    public static SPIRV CompileShaderFile( final Class<?> refClass, final String shaderFile, ShaderType shaderKind){
        InputStream source = refClass.getResourceAsStream(shaderFile);
        try {
            String stringBytes = new String(source.readAllBytes());
            return CompileShader(shaderFile, stringBytes, shaderKind);
        } catch (Exception e) {
            CVKGraphLogger.GetStaticLogger().LogException(e, "CompileShaderFile threw exception for shader: %s", shaderFile);
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
    public static SPIRV CompileShader(String filename, String source, ShaderType shaderKind) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.type, filename, "main", NULL);

        if(result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V:\n " + shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    /**
     * ShaderType: Vertex, geometry, fragment
     */
    public enum ShaderType {
        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader),
        SHADER_TYPE_UNKNOWN(Integer.MAX_VALUE);

        private final int type;

        ShaderType(int type) {
            this.type = type;
        }
    }

    /**
     * SPIRV class - holder for the bytecode of a compiled SPIRV shader
     */
    public static final class SPIRV implements NativeResource {

        private final long handle;
        private final ByteBuffer bytecode;

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
    
    private static Date GetShaderResourceFromJarModifiedDate(final String resourceName) {
        try {
            // Slashes will be in UNC form
            final String uncResourceName = resourceName.replace("\\", "/");
            
            CodeSource refClassSource = CVKShaderPlaceHolder.class.getProtectionDomain().getCodeSource();
            if (refClassSource != null) {
                URL jarURL = refClassSource.getLocation();
                
                HashMap<String, String> env = new HashMap<>();
                try (FileSystem jarFS = FileSystems.newFileSystem(jarURL.toURI(), env)) {
                    for (var fileStore : jarFS.getFileStores()) {
                        String name = fileStore.name();
                        if (name.endsWith("/")) {
                            name = name.substring(0, name.length() - 1);
                        }
                        
                        ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(name)));
                        ZipEntry entry = zip.getNextEntry();
                        while (entry != null) {
                            final String entryName = entry.getName();
                            if (entryName.endsWith(uncResourceName)) {
                                return new Date(entry.getLastModifiedTime().toMillis());
                            }
                            entry = zip.getNextEntry();
                        }                        
                    }
                } catch (Exception e) {
                }
            } 
        } catch (Exception e) { }   
        
        return null;
    }
    
    private static Date GetShaderResourceFromFileModifiedDate(final String resourceName) {
        try {
            final Path path = GetUserDirShaderResourcePath(resourceName);
            if (Files.exists(path)) {
                return new Date(path.toFile().lastModified());
            }
        } catch (Exception e) {}  
        
        return null;
    }
    
    private static Path GetUserDirShaderResourcePath(final String resourceName) {
        return Paths.get(System.getProperty("user.dir"), "shaders", resourceName);
    }  
    
    public static enum CVKShaderResourceLocation {
        NOT_FOUND,
        JAR,
        FILE
    }

    private static ByteBuffer LoadShaderResourceFromJar(final String resourceName) {  
        try {
            InputStream source = CVKShaderPlaceHolder.class.getResourceAsStream(resourceName);
            if (source != null) {            
                byte[] allBytes = source.readAllBytes();        
                ByteBuffer buffer = MemoryUtil.memAlloc(allBytes.length);
                buffer.put(allBytes);
                buffer.flip();        
                return buffer;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }              
    } 
    
    private static ByteBuffer LoadShaderResourceFromFile(final String resourceName) {        
        try {
            final Path path = GetUserDirShaderResourcePath(resourceName);
            byte[] allBytes = Files.readAllBytes(path);
            ByteBuffer buffer = MemoryUtil.memAlloc(allBytes.length);
            buffer.put(allBytes);
            buffer.flip();        
            return buffer;                
        } catch (Exception e) {
            return null;
        } 
    }     
    
    private static CVKShaderResourceLocation LoadCompiledShader(final String compiledFileName, final String md5FileName, CVKByteBufferPair buffers) {
        Date compiledDateJar  = GetShaderResourceFromJarModifiedDate(compiledFileName);
        Date compiledDateFile = GetShaderResourceFromFileModifiedDate(compiledFileName);
        Date md5DateJar  = GetShaderResourceFromJarModifiedDate(md5FileName);
        Date md5DateFile = GetShaderResourceFromFileModifiedDate(md5FileName);
        
        boolean fromJarValid  = compiledDateJar != null && md5DateJar != null;
        boolean fromFileValid = compiledDateFile != null && md5DateFile != null;
        
        CVKShaderResourceLocation newest = CVKShaderResourceLocation.NOT_FOUND;
        
        // There was a version in each, compare compile dates
        if (fromJarValid && fromFileValid) {
            if (compiledDateJar.after(compiledDateFile)) {
                newest = CVKShaderResourceLocation.JAR;
            } else {
                newest = CVKShaderResourceLocation.FILE;
            }
            
        } else if (fromJarValid) {
            newest = CVKShaderResourceLocation.JAR;
        } else if (fromFileValid) {
            newest = CVKShaderResourceLocation.FILE;
        } 
        
        switch (newest) {
            case JAR:
                buffers.buf1 = LoadShaderResourceFromJar(compiledFileName);
                buffers.buf2 = LoadShaderResourceFromJar(md5FileName);
                break;
            case FILE:
                buffers.buf1 = LoadShaderResourceFromFile(compiledFileName);
                buffers.buf2 = LoadShaderResourceFromFile(md5FileName);
                break;    
            case NOT_FOUND:
            default:
                return CVKShaderResourceLocation.NOT_FOUND;
        }
        
        if (buffers.buf1 == null || buffers.buf2 == null) {
            newest = CVKShaderResourceLocation.NOT_FOUND;
        }
        
        return newest;
    }
    
    private static class CVKByteBufferPair {
        private ByteBuffer buf1 = null;
        private ByteBuffer buf2 = null;
        
        public void Destroy() {
            if (buf1 != null) {
                MemoryUtil.memFree(buf1); 
                buf1 = null;
            }
            if (buf2 != null) {
                MemoryUtil.memFree(buf2); 
                buf2 = null;
            }            
        }
    }
        
    public static int LoadShader(String shaderName, MutableLong hShaderModule) {
        hShaderModule.setValue(VK_NULL_HANDLE);
        CVKGraphLogger.GetStaticLogger().fine("Loading shader '%s'", shaderName);
        
        if (SHADER_MAP.containsKey(shaderName)) {
            CVKGraphLogger.GetStaticLogger().fine("  shader '%s' found in shader map", shaderName);
            hShaderModule.setValue(SHADER_MAP.get(shaderName));
            return VK_SUCCESS;
        }
        CVKGraphLogger.GetStaticLogger().fine("  shader '%s' not found in shader map, looking on disk", shaderName);
        
        // First load the static MD5 digest instance
        if (md5 == null) {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                CVKGraphLogger.GetStaticLogger().LogException(e, "Cannot load MD5 digest algorithm instance");
                return CVK_ERROR_MD5_ALGORITHM_LOAD_FAILED;
            }
        }  

        // We have to compile, check we can load the shader compiler on this platform
        if (!shaderCompilerVerified) {
            long compiler = shaderc_compiler_initialize();
            if (compiler == NULL) {
                CVKGraphLogger.GetStaticLogger().severe("Failed to initialise shader compiler");
                return CVK_ERROR_SHADER_COMPILER_LOAD_FAILED;
            }
            shaderc_compiler_release(compiler);
            shaderCompilerVerified = true;
        }
        
        final String md5FileName = String.format("compiled/%s.md5", shaderName);
        final String compiledFileName = String.format("compiled/%s.spv", shaderName);

        // Load the shader source from the JAR as we will need it for compilation or checking MD5
        ByteBuffer sourceBytes = LoadShaderResourceFromJar(shaderName);
        if (sourceBytes == null) {
            CVKGraphLogger.GetStaticLogger().severe("Failed to load shader %s from resources", shaderName);
            return CVK_ERROR_SHADER_SOURCE_LOAD_FAILED;  
        }
        
        // Hash the source
        md5.reset();
        md5.update(sourceBytes);
        ByteBuffer sourceMD5 = ByteBuffer.wrap(md5.digest());          
        
        // Load the compiledBytes and its MD5 if it exists
        CVKByteBufferPair buffers = new CVKByteBufferPair();
        
        CVKShaderResourceLocation compiledLocation = LoadCompiledShader(compiledFileName, md5FileName, buffers);
        if (compiledLocation != CVKShaderResourceLocation.NOT_FOUND) {
            if (0 == buffers.buf2.compareTo(sourceMD5)) {
                // We have a match
                hShaderModule.setValue(CVKShaderUtils.CreateShaderModule(buffers.buf1, CVKDevice.GetVkDevice()));
                MemoryUtil.memFree(sourceBytes);              
                if (hShaderModule.longValue() == VK_NULL_HANDLE) {                        
                    CVKGraphLogger.GetStaticLogger().severe("Failed to create shader module for: %s", shaderName);
                    return CVK_ERROR_SHADER_MODULE;
                }    
                buffers.Destroy();
                SHADER_MAP.put(shaderName, hShaderModule.longValue());
                CVKGraphLogger.GetStaticLogger().fine("  shader '%s' was up to date, loaded '%s' from disk", shaderName, compiledFileName);
                return VK_SUCCESS;
            } else {
                CVKGraphLogger.GetStaticLogger().fine("  compiled shader '%s' is out of date, compiling", compiledFileName);
            }   
             
            buffers.Destroy();
        } else {
            CVKGraphLogger.GetStaticLogger().fine("  no compiled shader found for '%s', compiling", shaderName);
        }       
                   

        // Shader needs to be compiled
        ShaderType shaderType = ShaderType.SHADER_TYPE_UNKNOWN;
        String fileExtension = FilenameUtils.getExtension(shaderName).toLowerCase();
        switch (fileExtension) {
            case "vs":
            case "vert":
                shaderType = ShaderType.VERTEX_SHADER;
                break;
            case "gs":
            case "geom":
                shaderType = ShaderType.GEOMETRY_SHADER;
                break;
            case "fs":
            case "frag":
                shaderType = ShaderType.FRAGMENT_SHADER;
                break;                    
        }
        if (shaderType == ShaderType.SHADER_TYPE_UNKNOWN) {
            MemoryUtil.memFree(sourceBytes); 
            CVKGraphLogger.GetStaticLogger().severe("Could not determine shader type for: %s", shaderName);
            return CVK_ERROR_SHADER_TYPE_UNKNOWN;
        }
        SPIRV spirv = CompileShaderFile(CVKShaderPlaceHolder.class, shaderName, shaderType);
        if (spirv == null) {
            CVKGraphLogger.GetStaticLogger().severe("Failed to compile shader %s", shaderName);
            MemoryUtil.memFree(sourceBytes);             
            return CVK_ERROR_SHADER_COMPILATION;
        }

        hShaderModule.setValue(CVKShaderUtils.CreateShaderModule(spirv.bytecode(), CVKDevice.GetVkDevice()));
        if (hShaderModule.longValue() == VK_NULL_HANDLE) {       
            CVKGraphLogger.GetStaticLogger().severe("Failed to create shader module for %s", shaderName);
            MemoryUtil.memFree(sourceBytes);  
            return CVK_ERROR_SHADER_MODULE;
        }

        // Save the MD5 and SPIRV
        final Path md5Path = GetUserDirShaderResourcePath(md5FileName);
        final Path compiledPath = GetUserDirShaderResourcePath(compiledFileName);
        try {
            md5Path.toFile().delete();  
        } catch (Exception e) {
            // Not fatal, carry on
        }
        try {
            compiledPath.toFile().delete();  
        } catch (Exception e) {
            // Not fatal, carry on
        }            
        
        try {
            File compiledFile = compiledPath.toFile();
            File parentDir = new File(compiledFile.getParent());
            parentDir.mkdirs();
            compiledFile.createNewFile();
            byte[] bytes = new byte[spirv.bytecode().capacity()];
            spirv.bytecode().get(bytes);
            FileUtils.writeByteArrayToFile(compiledFile, bytes);
        } catch (Exception e) {
            CVKGraphLogger.GetStaticLogger().LogException(e, "Exception thrown writing %s to disk", compiledFileName);
            MemoryUtil.memFree(sourceBytes);             
            return CVK_ERROR_SHADER_SPIRV_WRITE_FAILED;
        }
        
        try {
            File md5File = md5Path.toFile();
            File parentDir = new File(md5File.getParent());
            parentDir.mkdirs();
            md5File.createNewFile();
            byte[] bytes = new byte[sourceMD5.capacity()];
            sourceMD5.get(bytes);
            FileUtils.writeByteArrayToFile(md5File, bytes);
        } catch (Exception e) {
            CVKGraphLogger.GetStaticLogger().LogException(e, "Exception thrown writing %s to disk", compiledFileName);
            MemoryUtil.memFree(sourceBytes);          
            return CVK_ERROR_SHADER_MD5_WRITE_FAILED;            
        }
        
        // Cleanup, note the md5 is a wrapped buffer not allocated so doesn't need explicit freeing
        try {
            MemoryUtil.memFree(sourceBytes);  
        } catch (Exception e) {
            CVKGraphLogger.GetStaticLogger().LogException(e, "Exception thrown releaseing ByteBuffers");
        }
            
        // SPIRV and MD5 written to disk, update map and return success
        SHADER_MAP.put(shaderName, hShaderModule.longValue());
        return VK_SUCCESS;        
    }
    
    private void Destroy() {
        if (SHADER_MAP != null) {
            for (Entry<String, Long> el : SHADER_MAP.entrySet()) {
                Long hShaderModule = el.getValue();
                if (hShaderModule != VK_NULL_HANDLE) {
                    vkDestroyShaderModule(CVKDevice.GetVkDevice(), hShaderModule, null);
                }                
            }
            SHADER_MAP.clear();
            SHADER_MAP = null;
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {
        Destroy();
        super.finalize();
    }      
}
