/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.icon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * An IconData implementation allowing an icon to be built using a
 * {@link BufferedImage}.
 *
 * @author cygnus_x-1
 */
public class ImageIconData extends IconData {

    private final BufferedImage image;

    public ImageIconData(final BufferedImage image) {
        this.image = image;
    }

    @Override
    protected InputStream createRasterInputStream() throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, ConstellationIcon.DEFAULT_ICON_FORMAT, os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    protected InputStream createVectorInputStream() throws IOException {
        throw new UnsupportedOperationException("Image data can not be converted to vector input stream."); 
    }
}
