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
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvider defining pie chart icons.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class PieChartIconProvider implements ConstellationIconProvider {

    public static final int PIE_CHART_LEVELS = 16;
    public static final List<ConstellationIcon> PIE_CHART_ICONS = new ArrayList<>();

    public PieChartIconProvider() {
        final BufferedImage[] pieChartIcons = createPieChartImages(256, 0.375F, 4);

        for (int i = 0; i < pieChartIcons.length; i++) {
            final String iconName = String.valueOf(i) + "/" + PIE_CHART_LEVELS + " Pie";
            final ConstellationIcon pieChartIcon = new ConstellationIcon.Builder(iconName, new ImageIconData(pieChartIcons[i]))
                    .addCategory("Pie Chart")
                    .build();

            PIE_CHART_ICONS.add(pieChartIcon);
        }
    }

    @Override
    public List<ConstellationIcon> getIcons() {
        return PIE_CHART_ICONS;
    }

    public static BufferedImage[] createPieChartImages(int size, float radius, int samples) {
        final int levels = PIE_CHART_LEVELS + 1;
        final BufferedImage[] images = new BufferedImage[levels];

        for (int level = 0; level < levels; level++) {
            final float fraction = (float) level / (levels - 1);
            final BufferedImage pieChartImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            final float sampleSize = 1.0F / (size * samples);
            final float sampleOffset = sampleSize * 0.5F;

            for (int x = 0; x < size; x++) {
                final float xMin = (float) x / size + sampleOffset;
                final float xMax = (float) (x + 1) / size + sampleOffset;

                for (int y = 0; y < size; y++) {
                    final float yMin = (float) y / size + sampleOffset;
                    final float yMax = (float) (y + 1) / size + sampleOffset;

                    float alpha = 0.0F;
                    float color = 0.0F;
                    for (float xx = xMin; xx < xMax; xx += sampleSize) {
                        for (float yy = yMin; yy < yMax; yy += sampleSize) {
                            final float dx = xx - 0.5F;
                            final float dy = yy - 0.5F;
                            if (dx * dx + dy * dy < radius * radius) {
                                alpha++;

                                float f;
                                if (dx == 0) {
                                    f = dy < 0 ? 0F : 0.5F;
                                } else if (dx < 0) {
                                    f = (float) (Math.atan(dy / dx) / 2 / Math.PI) + 0.75F;
                                } else {
                                    f = (float) (Math.atan(dy / dx) / 2 / Math.PI) + 0.25F;
                                }
                                if (f < fraction) {
                                    color++;
                                }
                            }
                        }
                    }

                    alpha /= samples * samples;
                    color /= samples * samples;

                    final int r = (int) (color * 255) << 16;
                    final int b = (int) ((1.0F - color) * 255);
                    final int a = (int) (alpha * 255) << 24;
                    pieChartImage.setRGB(x, y, r | b | a);
                }
            }

            images[level] = pieChartImage;
        }

        return images;
    }
}
