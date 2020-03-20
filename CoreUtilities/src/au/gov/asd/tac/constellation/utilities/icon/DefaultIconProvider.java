/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvder defining the set of icons required for a graph to work.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class DefaultIconProvider implements ConstellationIconProvider {

    public static final ConstellationIcon HIGHLIGHTED = new ConstellationIcon.Builder("Highlighted", new ImageIconData(getHighlightImage(256, 0.1f, 4)))
            .build();
    public static final ConstellationIcon NOISE = new ConstellationIcon.Builder("Noise", new FileIconData("modules/ext/icons/noise.png", "au.gov.asd.tac.constellation.utilities"))
            .build();
    public static final ConstellationIcon TRANSPARENT = new ConstellationIcon.Builder("Transparent", new FileIconData("modules/ext/icons/transparent.png", "au.gov.asd.tac.constellation.utilities"))
            .build();
    public static final ConstellationIcon UNKNOWN = new ConstellationIcon.Builder("Unknown", new FileIconData("modules/ext/icons/003f.png", "au.gov.asd.tac.constellation.utilities"))
            .build();
    public static final ConstellationIcon EMPTY = new ConstellationIcon.Builder("", TRANSPARENT.getIconData())
            .build();
    public static final ConstellationIcon LOOP_DIRECTED = new ConstellationIcon.Builder("Directed Loop", new FileIconData("modules/ext/icons/loop_directed.png", "au.gov.asd.tac.constellation.utilities"))
            .build();
    public static final ConstellationIcon LOOP_UNDIRECTED = new ConstellationIcon.Builder("Undirected Loop", new FileIconData("modules/ext/icons/loop_undirected.png", "au.gov.asd.tac.constellation.utilities"))
            .build();
    public static final ConstellationIcon FLAT_CIRCLE = new ConstellationIcon.Builder("Flat Circle", new FileIconData("modules/ext/icons/flat_circle.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Background")
            .build();
    public static final ConstellationIcon FLAT_SQUARE = new ConstellationIcon.Builder("Flat Square", new FileIconData("modules/ext/icons/flat_square.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Background")
            .build();
    public static final ConstellationIcon FLAT_TRIANGLE = new ConstellationIcon.Builder("Flat Triangle", new FileIconData("modules/ext/icons/flat_triangle.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Background")
            .build();
    public static final ConstellationIcon ROUND_CIRCLE = new ConstellationIcon.Builder("Round Circle", new FileIconData("modules/ext/icons/round_circle.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Background")
            .build();
    public static final ConstellationIcon ROUND_SQUARE = new ConstellationIcon.Builder("Round Square", new FileIconData("modules/ext/icons/round_square.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Background")
            .build();
    public static final ConstellationIcon EDGE_SQUARE = new ConstellationIcon.Builder("Edge Square", new FileIconData("modules/ext/icons/edge_square.png", "au.gov.asd.tac.constellation.utilities"))
            .addCategory("Background")
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        List<ConstellationIcon> defaultIcons = new ArrayList<>();
        defaultIcons.add(HIGHLIGHTED);
        defaultIcons.add(NOISE);
        defaultIcons.add(TRANSPARENT);
        defaultIcons.add(UNKNOWN);
        defaultIcons.add(EMPTY);
        defaultIcons.add(LOOP_DIRECTED);
        defaultIcons.add(LOOP_UNDIRECTED);
        defaultIcons.add(FLAT_CIRCLE);
        defaultIcons.add(FLAT_SQUARE);
        defaultIcons.add(FLAT_TRIANGLE);
        defaultIcons.add(ROUND_CIRCLE);
        defaultIcons.add(ROUND_SQUARE);
        defaultIcons.add(EDGE_SQUARE);
        return defaultIcons;
    }

    private static BufferedImage getHighlightImage(int size, float radius, int samples) {
        final BufferedImage highlightImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        final float sampleSize = 1.0f / (size * samples);
        final float sampleOffset = sampleSize * 0.5f;

        for (int x = 0; x < size; x++) {
            float xMin = (float) x / size + sampleOffset;
            float xMax = (float) (x + 1) / size + sampleOffset;

            for (int y = 0; y < size; y++) {
                float yMin = (float) y / size + sampleOffset;
                float yMax = (float) (y + 1) / size + sampleOffset;

                float alpha = 0.0f;
                for (float xx = xMin; xx < xMax; xx += sampleSize) {
                    for (float yy = yMin; yy < yMax; yy += sampleSize) {
                        alpha += calculateAlpha(xx, yy, radius);
                    }
                }

                alpha /= samples * samples;

                int colorInt = (((255 << 8) + 255) << 8) + 255;
                highlightImage.setRGB(x, y, ((int) (alpha * 255) << 24) + colorInt);
            }
        }

        return highlightImage;
    }

    private static float calculateAlpha(float x, float y, float radius) {
        float xDiameter = 0.0f, yDiameter = 0.0f;

        if (x < radius) {
            xDiameter = radius - x;
        } else if (x > 1.0f - radius) {
            xDiameter = x - (1.0f - radius);
        }

        if (y < radius) {
            yDiameter = radius - y;
        } else if (y > 1.0f - radius) {
            yDiameter = y - (1.0f - radius);
        }

        if (xDiameter * xDiameter + yDiameter * yDiameter > radius * radius) {
            return 0.0f;
        }

        return 2.0f * Math.max(Math.abs(0.5f - x), Math.abs(0.5f - y));
    }
}
