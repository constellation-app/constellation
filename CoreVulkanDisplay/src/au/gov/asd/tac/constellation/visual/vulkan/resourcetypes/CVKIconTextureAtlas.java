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
package au.gov.asd.tac.constellation.visual.vulkan.resourcetypes;

import au.gov.asd.tac.constellation.utilities.graphics.Vector3i;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKRenderUpdateTask;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_NEVER;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D_ARRAY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_A;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_B;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_G;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_R;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
import org.lwjgl.vulkan.VkComponentMapping;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_ICON_ATLAS_COPY_TIMEDOUT;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_ICON_ATLAS_SAMPLER_CREATION_FAILED;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_ICON_ATLAS_UNSUPPORTED_ICON_FORMAT;
import java.awt.image.ColorModel;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;


public class CVKIconTextureAtlas {
    private static CVKIconTextureAtlas cvkIconTextureAtlas = null;
    private static boolean RUNTIME_SWIZZLE = true;
    
    public static final int ICON_DIMENSION = 256;
    public static final int ICON_COMPONENTS = 4; //ARGB, 1 byte each
    public static final int ICON_SIZE_PIXELS = ICON_DIMENSION * ICON_DIMENSION;    
    public static final int ICON_SIZE_BYTES = ICON_SIZE_PIXELS * ICON_COMPONENTS;
    
    // Calculated once the device limits are known
    public int texture2DDimension = 0;  
    public int iconsPerLayer      = 0;
    public int iconsPerRowColumn  = 0;
    private int maxIcons          = Short.MAX_VALUE; //TODO: how do we handle the icons getting populated before texture limits are known?
    
    // These icons must be permanently present at these pre-defined indexes.
    // The shaders expect them to be there.
    public static final int HIGHLIGHTED_ICON_INDEX = 0;
    public static final String HIGHLIGHTED_ICON = DefaultIconProvider.HIGHLIGHTED.getExtendedName();
    public static final int UNKNOWN_ICON_INDEX = 1;
    public static final String UNKNOWN_ICON = DefaultIconProvider.UNKNOWN.getExtendedName();

    // Icons for drawing loops.
    public static final int LOOP_DIRECTED_ICON_INDEX = 2;
    public static final String LOOP_DIRECTED_ICON = DefaultIconProvider.LOOP_DIRECTED.getExtendedName();
    public static final int LOOP_UNDIRECTED_ICON_INDEX = 3;
    public static final String LOOP_UNDIRECTED_ICON = DefaultIconProvider.LOOP_UNDIRECTED.getExtendedName();

    // Noise indicator to be drawn when there are too many icons for the texture array.
    public static final int NOISE_ICON_INDEX = 4;
    public static final String NOISE_ICON = DefaultIconProvider.NOISE.getExtendedName();

    // Transparency.
    public static final int TRANSPARENT_ICON_INDEX = 5;
    public static final String TRANSPARENT_ICON = DefaultIconProvider.TRANSPARENT.getExtendedName();
    
    // Instance members
    private CVKImage cvkAtlasImage = null;
    private long hAtlasSampler = VK_NULL_HANDLE;
    private final LinkedHashMap<String, Integer> loadedIcons = new LinkedHashMap<>();
    private int lastTransferedIconCount = 0;
    private volatile boolean needsSaveToFile = false;
    
    
    // ========================> Classes <======================== \\ 
    
    // This could be replaced with a templated Pair type
    private class IndexedConstellationIcon {
        public final int index;
        public final ConstellationIcon icon;
        IndexedConstellationIcon(int index, ConstellationIcon icon) {
            this.index = index;
            this.icon = icon;               
        }
    }
    
    
    // ========================> Lifetime <======================== \\    
    
    public static boolean IsInstantiated() {
        return cvkIconTextureAtlas != null;
    }
    
    public static CVKIconTextureAtlas GetInstance() {  
        if (cvkIconTextureAtlas == null) {
            cvkIconTextureAtlas = new CVKIconTextureAtlas();
        }
        return cvkIconTextureAtlas;
    }
    
    private CVKIconTextureAtlas() {    
        // These icons are guaranteed to be in the iconMap in this order.
        // They must be at these pre-defined indices so other code (in particular the shaders) can use them.
        // See *_INDEX constants above.
        for (final String iconName : new String[]{HIGHLIGHTED_ICON, UNKNOWN_ICON, LOOP_DIRECTED_ICON, LOOP_UNDIRECTED_ICON, NOISE_ICON, TRANSPARENT_ICON}) {
            AddIcon(iconName);
        }            
    }
    
    private static int NextPowerOfTwo(int num) {
        int highestOneBit = Integer.highestOneBit(num);
        if (num == highestOneBit) {
            return num;
        }
        return highestOneBit << 1;
    }
    
    private static int CalcMinTextureDimension(final int numberOfIcons, final int maxTextureDimension) {
        final int maxIconsPerDimension = maxTextureDimension / ICON_DIMENSION;
        final int iconsPerDimension   = (int)(Math.ceil(Math.sqrt(numberOfIcons)));
        if (iconsPerDimension < maxIconsPerDimension) {
            return NextPowerOfTwo(iconsPerDimension * ICON_DIMENSION);            
        } else {
            return maxIconsPerDimension * ICON_DIMENSION;
        }        
    }
    
    private int CreateAtlas() {
        CVKAssertNull(lastTransferedIconCount);
        CVKAssertNull(cvkAtlasImage);
        CVKAssertNull(hAtlasSampler);
        

        texture2DDimension = CalcMinTextureDimension(loadedIcons.size(), CVKDevice.GetMax2DDimension());
        iconsPerRowColumn  = texture2DDimension / ICON_DIMENSION;
        iconsPerLayer      = iconsPerRowColumn * iconsPerRowColumn; 
        maxIcons           = iconsPerLayer * CVKDevice.GetMaxImageLayers();
        GetLogger().info("Icon atlas will be %dx%d to accomodate %d icons:\n\t%d icons per dimension\n\t%d icons per layer\n\t%d maximum icons",
                texture2DDimension, texture2DDimension, loadedIcons.size(), iconsPerRowColumn, iconsPerLayer, maxIcons);   

                     
//        final Set<String> iconNames = IconManager.getIconNames(false);
//        CVKLOGGER.info("\n====ALL ICONS====");
//        iconNames.forEach(el -> {CVKLOGGER.info(el);});
//        CVKLOGGER.info("");
        
        int ret = VK_SUCCESS;
        if (loadedIcons.size() > 0) {
            List<IndexedConstellationIcon> allIcons = new ArrayList<>();
            loadedIcons.entrySet().forEach(entry -> {                
                allIcons.add(new IndexedConstellationIcon(entry.getValue(), IconManager.getIcon(entry.getKey()))); 
            });
                        
            ret = AddIconsToAtlas(allIcons);
            checkVKret(ret);
            lastTransferedIconCount = loadedIcons.size();  
        }
        return ret;    
    }     
        
    private void Destroy() {
        if (cvkAtlasImage != null) {
            cvkAtlasImage.Destroy();
            cvkAtlasImage = null;            
        }

        if (hAtlasSampler != VK_NULL_HANDLE) {
            vkDestroySampler(CVKDevice.GetVkDevice(), hAtlasSampler, null);    
            hAtlasSampler = VK_NULL_HANDLE;
        }
        
        lastTransferedIconCount = 0;
    }      
       
    
    // ========================> Display <======================== \\
    
    public int DisplayUpdate() {
        int ret = VK_SUCCESS;
        
        while (needsSaveToFile) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                GetLogger().LogException(e, "Exception while waiting for Atlas to save to file");
            }
        }
        
        if (loadedIcons.size() > lastTransferedIconCount) {
            Destroy();
            ret = CreateAtlas();
            if (VkFailed(ret)) { return ret; }
        }
        
        return ret;
    }
    
    
    // ========================> Tasks <======================== \\
    
    public CVKRenderUpdateTask TaskSaveToFile(File file) {
        return () -> {
            needsSaveToFile = true;
            SaveToFile(file);
            needsSaveToFile = false;
        };
    }
    
    
    // ========================> Helpers <======================== \\
    
    public int GetAtlasIconCount() { return lastTransferedIconCount; }
    
    public long GetAtlasImageViewHandle() { return cvkAtlasImage.GetImageViewHandle(); }
    
    public long GetAtlasSamplerHandle() { return hAtlasSampler; }
  
    public int AddIcon(final String label) {
        CVKAssertNotNull(label);
        if (label.isEmpty()) {
            return TRANSPARENT_ICON_INDEX;
        }
        
        // Use extended name to prevent duplication in the texture atlas
        String extendedName = label;
        final ConstellationIcon icon = IconManager.getIcon(extendedName);
        if (icon != null) {
            extendedName = icon.getExtendedName();
        }
        
        final Integer iconIndex = loadedIcons.get(extendedName);
        if (iconIndex == null) {
            final int index = loadedIcons.size();
            if (index >= maxIcons) {
                // Too many icons: return NOISE icon.
                return NOISE_ICON_INDEX;
            }

            loadedIcons.put(extendedName, index);
            return index;
        }

        return iconIndex;
    }
    
    public int SaveToFile(File file) {
        int ret = cvkAtlasImage.SaveToFile(file);
        needsSaveToFile = false;
        return ret;
    }
    
    /**
     * Converts an icon index into column, row and layer indices
     * 
     * Example where 
     *   iconsPerRow   = 4
     *   rowsPerLayer  = 5
     *   iconsPerLayer = 20
     * 
     *  0  1  2  3
     *  4  5  6  7
      * 8  9 10 11
     * 12 13 14 15
     * 16 17 18 19
     * 
     * 20 21 22 23
     * 24 25 26 27
     * 28 29 30 31
     * 32 33 34 35
     * 36 37 38 39
     * 
     * 40 41 42 43
     * 44 45 46 47
     * 48 49 50 51
     * 52 53 54 55
     * 56 57 58 59
     * 
     * index = 27
     * x = 27 % 4	 = 3       
     * y = (27 % 20) / 4 = 1
     * z = 27 / 20       = 1
     * 
     * index = 38
     * x = 38 % 4        = 2       
     * y = (38 % 20) / 4 = 4
     * z = 38 / 20       = 1
     * 
     * index = 48
     * x = 48 % 4	 = 0       
     * y = (48 % 20) / 4 = 2
     * z = 48 / 20       = 2
     * 
     * @param index
     * @return vector of lookup indices
     */
    public Vector3i IndexToTextureIndices(int index) {
        return new Vector3i(index % iconsPerRowColumn,
                            (index % iconsPerLayer) / iconsPerRowColumn,
                            index / iconsPerLayer);     
    }
    
    public long IndexToBufferOffset(int index) {
        long offset = 0;
        Vector3i texIndices = IndexToTextureIndices(index);  
        
        // add size of previous icons on this row
        offset += texIndices.getX() * ICON_SIZE_BYTES;
        
        // add size of previous rows on this layer
        offset += texIndices.getY() * iconsPerRowColumn * ICON_SIZE_BYTES;
                
        // add size of previous layers
        offset += texIndices.getZ() * iconsPerLayer * ICON_SIZE_BYTES;
        
        int povCalc = index * ICON_SIZE_BYTES;
        CVKAssert(offset == povCalc);
        
        return offset;
    }
    
    private static boolean IsEmpty(ByteBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); ++i) {
            if (buffer.get(i) != 0) {
                return false;
            }
        }
        return true;
    }
    
    class IconCopyWorker extends Thread {
        private final IndexedConstellationIcon el;
        private int ret = VK_SUCCESS;

        IconCopyWorker(final IndexedConstellationIcon el) {
            this.el = el;
        }

        @Override
        public void run() {
            ret = CopyIcon(el);
        }
    }     
    
    private int CopyIcon(final IndexedConstellationIcon el) {
        int ret;
        
        BufferedImage iconImage = el.icon.buildBufferedImage();  

        // Convert the buffered image if its not in our desired state.
        if (TYPE_4BYTE_ABGR != iconImage.getType()) {
            BufferedImage convertedImg = new BufferedImage(iconImage.getWidth(), iconImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            convertedImg.getGraphics().drawImage(iconImage, 0, 0, null);
            iconImage = convertedImg;
        }     
        int width = iconImage.getWidth();
        int height = iconImage.getHeight();
        CVKAssert(width  <= ICON_DIMENSION);
        CVKAssert(height <= ICON_DIMENSION);

        ColorModel clrmdl = iconImage.getColorModel();
        
        ByteBuffer pixels = null;
        if (RUNTIME_SWIZZLE) {
            final DataBuffer data = iconImage.getRaster().getDataBuffer();
            if (data instanceof DataBufferByte) {
                pixels = ByteBuffer.wrap(((DataBufferByte) data).getData());
            } else {
                GetLogger().severe("data is not in byte form");
                return CVK_ERROR_ICON_ATLAS_UNSUPPORTED_ICON_FORMAT;
            }            
        } else {
            final int iconSizeBytes = ICON_COMPONENTS * width * height;
            pixels = memAlloc(iconSizeBytes);   
            
            // To save us having to swizzle with every render, do it now (ABGR->RGBA, AWT what were you thinking?)
            for (int v = 0; v < height; ++v) {
                for (int u = 0; u < width; ++u) {



                    Object o = iconImage.getRaster().getDataElements(u, v, null);
                    final byte r = (byte) iconImage.getColorModel().getRed(o);
                    final byte g = (byte) iconImage.getColorModel().getGreen(o);
                    final byte b = (byte) iconImage.getColorModel().getBlue(o);
                    final byte a = (byte) iconImage.getColorModel().getAlpha(o);
                    final int offset = ICON_COMPONENTS * (u + width * v);
                    pixels.put(offset,   r);//(byte) (agbr&0x000000FF));
                    pixels.put(offset+1, g);//(byte)((agbr&0x00FF0000)>>16));
                    pixels.put(offset+2, b);//(byte)((agbr&0x0000FF00)>>8));
                    pixels.put(offset+3, a);//(byte) (agbr>>24));     


                }                                      
            }             
        }            

        CVKBuffer stagingBuffer = CVKBuffer.Create(ICON_SIZE_BYTES, 
                                                   VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                   VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                                                   null,
                                                   "CVKIconTextureAtlas cvkStagingBuffer");      

        // Copy pixels, note for undersized icons we need extra offsets to pad the top and sides                    
        if (width == ICON_DIMENSION && height == ICON_DIMENSION) {
            CVKAssert(pixels.capacity() == ICON_SIZE_BYTES);
            stagingBuffer.Put(pixels, 0, 0, ICON_SIZE_BYTES);
        } else {
            // Zero this buffer so undersized icons are padded with transparent pixels
            stagingBuffer.ZeroMemory();     

            // Offsets to centre the icon are in pixels
            int colOffset = (ICON_DIMENSION - width) / 2;
            int rowOffset = (ICON_DIMENSION - height) / 2;

            // Adjust the start position to the right row
            for (int iRow = 0; iRow < height; ++iRow) {       
                // offset to the start of this row
                int writePos = (iRow + rowOffset) * ICON_DIMENSION * ICON_COMPONENTS;
                CVKAssert(((iRow + rowOffset + 1) * ICON_DIMENSION * ICON_COMPONENTS) <= ICON_SIZE_BYTES);

                // offset from the start of the row to the start of the icon
                writePos += colOffset * ICON_COMPONENTS;
                int readPos = iRow * width * ICON_COMPONENTS;
                ret = stagingBuffer.Put(pixels, writePos, readPos, width * ICON_COMPONENTS);
                if (VkFailed(ret)) { return ret; }
            }
        }

        // Calculate offset into staging buffer for the current image layer
        Vector3i texIndices = IndexToTextureIndices(el.index);

        // Copy it in.  Note this is blocking.  If this is highlighted as a performance issue use the
        // add a version of CVKImage.CopyFrom that takes a command buffer but doesn't submit it and
        // doesn't do any image layout transitions.                
        Vector3i dstOffset = new Vector3i(texIndices.getU() * ICON_DIMENSION, texIndices.getV() * ICON_DIMENSION, texIndices.getW());
        Vector3i dstExtent = new Vector3i(ICON_DIMENSION, ICON_DIMENSION, 1);

        //GetLogger().finer("Icon %d copying into %s", iIcon, dstOffset);

        synchronized(cvkAtlasImage) {
            ret = cvkAtlasImage.CopyFrom(stagingBuffer, 0, dstOffset, dstExtent);
            if (VkFailed(ret)) { return ret; }    
        }
        
        // If we swizzled before copying then we need to release the pixel data
        // we allocated.
        if (!RUNTIME_SWIZZLE && pixels != null) {
            memFree(pixels);
        }    
        
        return ret;
    }
    
    private int CreateAtlasSampler(MemoryStack stack) {
        int ret;
        
        // Create a sampler to match the image.  Note the sampler allows us to sample
        // an image but isn't tied to a specific image, note the lack of image or 
        // imageview parameters below.
        VkSamplerCreateInfo vkSamplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);                        
        vkSamplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
        vkSamplerCreateInfo.maxAnisotropy(1.0f);
        vkSamplerCreateInfo.magFilter(VK_FILTER_LINEAR);
        vkSamplerCreateInfo.minFilter(VK_FILTER_LINEAR);
        vkSamplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
        vkSamplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
        vkSamplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
        vkSamplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
        vkSamplerCreateInfo.mipLodBias(0.0f);
        vkSamplerCreateInfo.maxAnisotropy(8);
        vkSamplerCreateInfo.compareOp(VK_COMPARE_OP_NEVER);
        vkSamplerCreateInfo.minLod(0.0f);
        vkSamplerCreateInfo.maxLod(0.0f);
        vkSamplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);

        LongBuffer pTextureSampler = stack.mallocLong(1);
        ret = vkCreateSampler(CVKDevice.GetVkDevice(), vkSamplerCreateInfo, null, pTextureSampler);
        if (VkFailed(ret)) { return ret; }
        hAtlasSampler = pTextureSampler.get(0);
        if (hAtlasSampler == VK_NULL_HANDLE) { return CVK_ERROR_ICON_ATLAS_SAMPLER_CREATION_FAILED; }
        
        return ret;
    }
    
    // Only it's own function for measuring timing
    private void ProcessIconCopying(final List<IndexedConstellationIcon> icons, final ExecutorService pool) {
        icons.stream().map(el -> new IconCopyWorker(el)).forEachOrdered(thread -> {
            pool.execute(thread);
        });        
    }
    
    private int AddIconsToAtlas(final List<IndexedConstellationIcon> icons) {
        int ret;
                
        try (MemoryStack stack = stackPush()) {
            // Sanity check duplicates        
            if (CVK_DEBUGGING) {
                int dupcount = 0;
                for (int i = 0; i < icons.size(); ++i) {
                    for (int j = i + 1; j < icons.size(); ++j) {
                        if (icons.get(i).icon.getExtendedName().equals(icons.get(j).icon.getExtendedName())) {
                            GetLogger().warning("%d Duplicate icon %s, %d (%s) and %d (%s)",
                                    ++dupcount, icons.get(i).icon.getName(),
                                    i, icons.get(i).icon.getExtendedName(),
                                    j, icons.get(j).icon.getExtendedName());
                        }                        
                    }
                }
            }

            int requiredLayers = (icons.size() / iconsPerLayer) + 1;

            if (cvkAtlasImage != null) {
                cvkAtlasImage.Destroy();
                cvkAtlasImage = null;
            }

            // Control AGBR->RGBA swizzling in the image view
            VkComponentMapping componentMapping = null;
            if (RUNTIME_SWIZZLE) {
                componentMapping = VkComponentMapping.callocStack(stack);
                componentMapping.set(VK_COMPONENT_SWIZZLE_A, 
                                     VK_COMPONENT_SWIZZLE_B, 
                                     VK_COMPONENT_SWIZZLE_G, 
                                     VK_COMPONENT_SWIZZLE_R);
            }


            // Create destination image            
            cvkAtlasImage = CVKImage.Create(texture2DDimension, 
                                            texture2DDimension, 
                                            requiredLayers,
                                            VK_FORMAT_R8G8B8A8_UNORM, //non-linear format to give more fidelity to the hues we are most able to perceive
                                            VK_IMAGE_VIEW_TYPE_2D_ARRAY, //regardless how how many layers are in the image, the shaders that use that atlas use a sampler2DArray
                                            VK_IMAGE_TILING_OPTIMAL, //we usually sample rectangles rather than long straight lines
                                            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                                            VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                                            VK_IMAGE_ASPECT_COLOR_BIT,
                                            componentMapping,
                                            null,
                                            "CVKIconTextureAtlas cvkAtlasImage");                         

            // Transition image from undefined to transfer destination optimal
            ret = cvkAtlasImage.Transition(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            if (VkFailed(ret)) { return ret; }

            // Loop that copies icons from the icon manager into the atlas texture(s)
            final ExecutorService pool = Executors.newFixedThreadPool(CVKUtils.NUM_CORES);
            ProcessIconCopying(icons, pool);

            // Create Sampler while we wait for the workers
            CreateAtlasSampler(stack);

            pool.shutdown();
            try {
                pool.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                GetLogger().LogException(e, "Timed out copying icons into CVKIconTextureAtlas");
                return CVK_ERROR_ICON_ATLAS_COPY_TIMEDOUT;
            }

            // Now the image is populated, transition it for reading
            ret = cvkAtlasImage.Transition(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            if (VkFailed(ret)) { return ret; }             
        }                                    
        
        return ret;
    }
    
    private CVKGraphLogger GetLogger() { return CVKGraphLogger.GetStaticLogger(); }
}
