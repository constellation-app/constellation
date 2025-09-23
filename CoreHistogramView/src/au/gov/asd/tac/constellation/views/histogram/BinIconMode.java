/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.utilities.javafx.JavaFxUtilities;
import au.gov.asd.tac.constellation.views.histogram.bins.ObjectBin;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/**
 * A BinIconMode represents the different ways that a bin can be annotated with an icon when it is rendered in the
 * histogram.
 *
 * @author sirius
 */
public enum BinIconMode {

    /**
     * No icon is added to the bin when it is rendered.
     */
    NONE(0.0F) {
        @Override
        public void draw(final Graphics2D graphics, final Bin bin, final int left, final int top, final int height) {
            // Left blank on purpose
        }

        @Override
        public Node createFXIcon(final Bin bin, final int height) {
            return null;
        }
    },
    /**
     * An standard icon is added to the bin when it is rendered. This typically comes from an attribute on the element.
     */
    ICON(1.5F) {
        @Override
        public void draw(final Graphics2D graphics, final Bin bin, final int left, final int top, final int height) {
            if (bin instanceof final ObjectBin objectBin) {
                final Object key = objectBin.getKeyAsObject();

                if (key != null) {
                    final String iconLabel = ((ConstellationIcon) key).getName();
                    BufferedImage icon = iconCache.get(iconLabel);
                    if (icon == null) {
                        icon = IconManager.getIcon(iconLabel).buildBufferedImage();
                        iconCache.put(iconLabel, icon);
                    }
                    if (icon != null) {
                        graphics.drawImage(icon, left, top, height, height, null);
                    }
                }
            }
        }

        @Override
        public Node createFXIcon(final Bin bin, final int height) {
            if (bin instanceof final ObjectBin objectBin) {
                final Object key = objectBin.getKeyAsObject();

                if (key == null) {
                    return null;
                }

                final String iconLabel = ((ConstellationIcon) key).getName();
                BufferedImage icon = iconCache.get(iconLabel);
                if (icon == null) {
                    icon = IconManager.getIcon(iconLabel).buildBufferedImage();
                    iconCache.put(iconLabel, icon);
                }
                if (icon != null) {
                    // convert icon into javafx image
                    final Image image = SwingFXUtils.toFXImage(icon, null);
                    final ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(height);
                    imageView.setFitWidth(height);

                    return imageView;
                }

            }
            return null;
        }
    },
    /**
     * An icon is created from a color by simply creating a plain square filled with that color. The color typically
     * comes from a color attribute on the element.
     */
    COLOR(1.5F) {
        @Override
        public void draw(final Graphics2D graphics, final Bin bin, final int left, final int top, final int height) {
            if (bin instanceof final ObjectBin objectBin) {
                final Object key = objectBin.getKeyAsObject();
                final ConstellationColor colorValue = (ConstellationColor) key;
                if (colorValue != null) {
                    graphics.setColor(colorValue.getJavaColor());
                }
                graphics.fillRoundRect(left, top, height, height, height / 2, height / 2);
            }
        }

        @Override
        public Node createFXIcon(final Bin bin, final int height) {
            if (bin instanceof final ObjectBin objectBin) {
                final Object key = objectBin.getKeyAsObject();
                final ConstellationColor colorValue = (ConstellationColor) key;

                // Make rectangle of that colour
                if (colorValue == null) {
                    return null;
                }

                final int arc = height / 3;
                final Rectangle rect = new Rectangle(Double.valueOf(height), Double.valueOf(height), colorValue.getJavaFXColor());
                rect.setArcHeight(arc);
                rect.setArcWidth(arc);
                return rect;
            }
            return null;
        }

    };

    private final float width;

    private BinIconMode(final float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    public abstract void draw(final Graphics2D graphics, final Bin bin, final int left, final int top, final int height);

    public abstract Node createFXIcon(final Bin bin, final int height);

    private static Map<String, BufferedImage> iconCache = new HashMap<>();

}
