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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.views.histogram.bins.ObjectBin;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A BinIconMode represents the different ways that a bin can be annotated with
 * an icon when it is rendered in the histogram.
 *
 * @author sirius
 */
public enum BinIconMode {

    /**
     * No icon is added to the bin when it is rendered.
     */
    NONE(0.0f) {
        @Override
        public void draw(Graphics2D graphics, Bin bin, int left, int top, int height) {
        }
    },
    /**
     * An standard icon is added to the bin when it is rendered. This typically
     * comes from an attribute on the element.
     */
    ICON(1.5f) {
        @Override
        public void draw(Graphics2D graphics, Bin bin, int left, int top, int height) {
            if (bin instanceof ObjectBin) {
                Object key = ((ObjectBin) bin).getKeyAsObject();

                if (key != null) {
                    String iconLabel = ((ConstellationIcon) key).getName();
                    BufferedImage icon = ICON_CACHE.get(iconLabel);
                    if (icon == null) {
                        try {
                            icon = IconManager.getIcon(iconLabel).buildBufferedImage();
                            ICON_CACHE.put(iconLabel, icon);
                        } catch (Exception e) {
                        }
                    }
                    if (icon != null) {
                        graphics.drawImage(icon, left, top, height, height, null);
                    }
                }
            }
        }
    },
    /**
     * An icon is created from a color by simply creating a plain square filled
     * with that color. The color typically comes from a color attribute on the
     * element.
     */
    COLOR(1.5f) {
        @Override
        public void draw(Graphics2D graphics, Bin bin, int left, int top, int height) {
            if (bin instanceof ObjectBin) {
                Object key = ((ObjectBin) bin).getKeyAsObject();
                ConstellationColor colorValue = (ConstellationColor) key;
                if (colorValue != null) {
                    graphics.setColor(colorValue.getJavaColor());
                }
                graphics.fillRoundRect(left, top, height, height, height / 2, height / 2);
            }
        }
    };

    private final float width;

    private BinIconMode(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public abstract void draw(Graphics2D graphics, Bin bin, int left, int top, int height);

    private static Map<String, BufferedImage> ICON_CACHE = new HashMap<>();

}
