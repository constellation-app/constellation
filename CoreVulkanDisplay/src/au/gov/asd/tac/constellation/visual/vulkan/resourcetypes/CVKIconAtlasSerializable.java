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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.imageio.ImageIO;


/**
 * This class stores the minimal set of data needed to cache the icon atlas
 * textures and dimensions to and from disk.  This improves load times 
 * significantly.
 */
public class CVKIconAtlasSerializable implements Serializable {
    public transient List<BufferedImage> layers = new ArrayList<>();
    public final LinkedHashMap<String, Integer> loadedIcons = new LinkedHashMap<>();
    public int texture2DDimension = 0;  
    public int iconsPerLayer      = 0;
    public int iconsPerRowColumn  = 0;
    public int maxIcons           = Short.MAX_VALUE; 
    
    
    public void Reset() {
        texture2DDimension = 0;
        iconsPerLayer = 0;
        iconsPerRowColumn = 0;
        maxIcons = Short.MAX_VALUE;
        layers.clear();
        loadedIcons.clear();
    }
    

    /**
     * BufferedImages don't implement Serializable and therefore must be manually
     * serialised.  To do this we mark it transient so defaultWriteObject doesn't
     * attempt to serialise it.  We then use the ImageIO to serialize to or from
     * the iostream.
     * 
     * @param out: stream this object is serialized to
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(layers.size());
        for (BufferedImage eachImage : layers) {
            ImageIO.write(eachImage, "png", out); // png is lossless
        }        
    }
    
    /**
     * BufferedImages don't implement Serializable and therefore must be manually
     * serialised.  To do this we mark it transient so defaultWriteObject doesn't
     * attempt to serialise it.  We then use the ImageIO to serialize to or from
     * the iostream.
     * 
     * @param in: stream this object is serialized from
     * @throws IOException 
     */    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final int imageCount = in.readInt();
        layers = new ArrayList<>(imageCount);
        for (int i = 0; i < imageCount; ++i) {
            BufferedImage image = ImageIO.read(in);
            layers.add(image);
        }
    }
}
