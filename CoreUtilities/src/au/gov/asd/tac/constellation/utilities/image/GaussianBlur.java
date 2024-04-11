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
package au.gov.asd.tac.constellation.utilities.image;

import java.util.Arrays;

/**
 * Gaussian Blur.
 *
 * @author cygnus_x-1
 */
public class GaussianBlur {

    private static final String TARGET_SMALLER_THAN_SOURCE = "Target channel is smaller than source channel.";

    protected static final int[] RAINBOW = {
        0x0034f8, 0x0037f6, 0x003af3, 0x003df0, 0x003fed, 0x0041ea, 0x0044e7, 0x0046e4,
        0x0048e1, 0x004ade, 0x004cdb, 0x004fd8, 0x0051d5, 0x0053d2, 0x0054d0, 0x0056cd,
        0x0058ca, 0x005ac7, 0x005cc4, 0x005ec1, 0x0060be, 0x0061bb, 0x0063b8, 0x0065b6,
        0x0066b3, 0x0068b0, 0x006aad, 0x006baa, 0x006da7, 0x006ea5, 0x006fa2, 0x00719f,
        0x00729d, 0x00739a, 0x007598, 0x007695, 0x077793, 0x0d7890, 0x13798e, 0x187a8b,
        0x1c7b89, 0x1f7c87, 0x237d84, 0x267e82, 0x287f7f, 0x2b807d, 0x2d817b, 0x2f8278,
        0x318376, 0x328473, 0x348571, 0x35866f, 0x36876c, 0x37886a, 0x388967, 0x398a65,
        0x3a8b62, 0x3b8c60, 0x3c8e5d, 0x3c8f5b, 0x3d9058, 0x3d9155, 0x3e9253, 0x3e9350,
        0x3e944d, 0x3e954a, 0x3e9647, 0x3f9745, 0x3f9842, 0x3e993e, 0x3e9a3b, 0x3e9b38,
        0x3e9c35, 0x3e9d32, 0x3e9e2e, 0x3e9f2b, 0x3fa027, 0x3fa124, 0x40a221, 0x41a31d,
        0x42a41a, 0x44a517, 0x45a615, 0x47a713, 0x4aa711, 0x4ca80f, 0x4fa90e, 0x51a90d,
        0x54aa0d, 0x57ab0d, 0x5aab0d, 0x5dac0d, 0x5fad0d, 0x62ad0e, 0x65ae0e, 0x67ae0e,
        0x6aaf0f, 0x6db00f, 0x6fb00f, 0x72b110, 0x74b110, 0x77b211, 0x79b211, 0x7cb311,
        0x7eb412, 0x80b412, 0x83b512, 0x85b513, 0x88b613, 0x8ab613, 0x8cb714, 0x8fb814,
        0x91b815, 0x93b915, 0x95b915, 0x98ba16, 0x9aba16, 0x9cbb16, 0x9fbb17, 0xa1bc17,
        0xa3bc18, 0xa5bd18, 0xa7be18, 0xaabe19, 0xacbf19, 0xaebf19, 0xb0c01a, 0xb2c01a,
        0xb5c11b, 0xb7c11b, 0xb9c21b, 0xbbc21c, 0xbdc31c, 0xc0c31c, 0xc2c41d, 0xc4c41d,
        0xc6c51d, 0xc8c51e, 0xcac61e, 0xcdc61f, 0xcfc71f, 0xd1c71f, 0xd3c820, 0xd5c820,
        0xd7c920, 0xd9c921, 0xdcca21, 0xdeca22, 0xe0ca22, 0xe2cb22, 0xe4cb23, 0xe6cc23,
        0xe8cc23, 0xeacc24, 0xeccd24, 0xeecd24, 0xf0cd24, 0xf2cd24, 0xf3cd24, 0xf5cc24,
        0xf6cc24, 0xf8cb24, 0xf9ca24, 0xf9c923, 0xfac823, 0xfbc722, 0xfbc622, 0xfcc521,
        0xfcc421, 0xfcc220, 0xfdc120, 0xfdc01f, 0xfdbe1f, 0xfdbd1e, 0xfebb1d, 0xfeba1d,
        0xfeb91c, 0xfeb71b, 0xfeb61b, 0xfeb51a, 0xffb31a, 0xffb219, 0xffb018, 0xffaf18,
        0xffae17, 0xffac16, 0xffab16, 0xffa915, 0xffa815, 0xffa714, 0xffa513, 0xffa413,
        0xffa212, 0xffa111, 0xff9f10, 0xff9e10, 0xff9c0f, 0xff9b0e, 0xff9a0e, 0xff980d,
        0xff970c, 0xff950b, 0xff940b, 0xff920a, 0xff9109, 0xff8f08, 0xff8e08, 0xff8c07,
        0xff8b06, 0xff8905, 0xff8805, 0xff8604, 0xff8404, 0xff8303, 0xff8102, 0xff8002,
        0xff7e01, 0xff7c01, 0xff7b00, 0xff7900, 0xff7800, 0xff7600, 0xff7400, 0xff7200,
        0xff7100, 0xff6f00, 0xff6d00, 0xff6c00, 0xff6a00, 0xff6800, 0xff6600, 0xff6400,
        0xff6200, 0xff6100, 0xff5f00, 0xff5d00, 0xff5b00, 0xff5900, 0xff5700, 0xff5500,
        0xff5300, 0xff5000, 0xff4e00, 0xff4c00, 0xff4a00, 0xff4700, 0xff4500, 0xff4200,
        0xff4000, 0xff3d00, 0xff3a00, 0xff3700, 0xff3400, 0xff3100, 0xff2d00, 0xff2a00};

    public static void gaussianBlurReal(final float[] sourceChannel, final float[] targetChannel,
            final int width, final int height, final int radius) {
        if (sourceChannel.length == width * height) {
            if (sourceChannel.length <= targetChannel.length) {
                final int rs = (int) Math.ceil(radius * 2.57);
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        double val = 0;
                        double wsum = Double.MIN_VALUE;
                        for (int iy = (i - rs); iy < (i + rs + 1); iy++) {
                            for (int ix = (j - rs); ix < (j + rs + 1); ix++) {
                                final int x = Math.min(width - 1, Math.max(0, ix));
                                final int y = Math.min(height - 1, Math.max(0, iy));
                                final int dsq = (ix - j) * (ix - j) + (iy - i) * (iy - i);
                                final double wght = Math.exp(-dsq / (2.0 * radius * radius)) / (Math.PI * 2.0 * radius * radius);
                                val += sourceChannel[y * width + x] * wght;
                                wsum += wght;
                            }
                        }
                        targetChannel[i * width + j] = (float) (val / wsum);
                    }
                }
            } else {
                throw new IllegalArgumentException(TARGET_SMALLER_THAN_SOURCE);
            }
        } else {
            throw new IllegalArgumentException("Source channel does not have the dimensions provided.");
        }

    }

    public enum BoxBlurType {
        STANDARD,
        FAST,
        FASTEST;
    }

    /**
     * Edits the targetChannel to be a blurred copy of the soruceChannel
     *
     * @param sourceChannel Float array containing image data
     * @param targetChannel Empty float array with size greater than or equal to the size of sourceChannel
     * @param width Width of image stored in float sourceChannel
     * @param height Height of image stored in sourceChannel
     * @param radius Radius of pixel blur
     * @param passes Number of blur passes
     * @param type Which blur algorithm to use: STANDARD, FAST or FASTEST
     */
    public static void gaussianBlurBox(final float[] sourceChannel, float[] targetChannel,
            final int width, final int height, final int radius, final int passes, final BoxBlurType type) {

        // Error Handling
        if (sourceChannel.length != width * height) {
            throw new IllegalArgumentException("Source channel does not have the dimensions provided.");
        }

        if (sourceChannel.length > targetChannel.length) {
            throw new IllegalArgumentException(TARGET_SMALLER_THAN_SOURCE);
        }

        float[] tempChannel = Arrays.copyOf(sourceChannel, sourceChannel.length);
        final int[] boxes = boxesForGauss(radius, passes);
        for (int i = 0; i < passes; i++) {
            switch (type) {
                case STANDARD -> boxBlur(tempChannel, targetChannel, width, height, ((boxes[i] - 1) / 2));
                case FAST -> {
                    boxBlurFH(tempChannel, targetChannel, width, height, ((boxes[i] - 1) / 2));
                    boxBlurFT(tempChannel, targetChannel, width, height, ((boxes[i] - 1) / 2));
                }
                case FASTEST -> {
                    boxBlurFFH(tempChannel, targetChannel, width, height, ((boxes[i] - 1) / 2));
                    boxBlurFFT(tempChannel, targetChannel, width, height, ((boxes[i] - 1) / 2));
                }
            }
            tempChannel = targetChannel;
        }
    }

    private static int[] boxesForGauss(final float sigma, final int n) {
        final double wIdeal = Math.sqrt((12 * sigma * sigma / n) + 1);
        int wl = (int) Math.floor(wIdeal);
        if (wl % 2 == 0) {
            wl--;
        }
        final int wu = wl + 2;

        final double mIdeal = (12 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4);
        final int m = (int) Math.round(mIdeal);

        final int[] sizes = new int[n];
        for (int i = 0; i < n; i++) {
            sizes[i] = i < m ? wl : wu;
        }

        return sizes;
    }

    private static void boxBlur(final float[] sourceChannel, final float[] targetChannel,
            final int width, final int height, final int radius) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double val = 0;
                for (int iy = (i - radius); iy < (i + radius + 1); iy++) {
                    for (int ix = (j - radius); ix < (j + radius + 1); ix++) {
                        final int x = Math.min(width - 1, Math.max(0, ix));
                        final int y = Math.min(height - 1, Math.max(0, iy));
                        val += sourceChannel[y * width + x];
                    }
                }
                targetChannel[i * width + j] = (float) (val / ((radius + radius + 1) * (radius + radius + 1)));
            }
        }
    }

    private static void boxBlurFH(final float[] sourceChannel, final float[] targetChannel,
            final int width, final int height, final int radius) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double val = 0;
                for (int ix = (j - radius); ix < (j + radius + 1); ix++) {
                    final int x = Math.min(width - 1, Math.max(0, ix));
                    val += sourceChannel[i * width + x];
                }
                targetChannel[i * width + j] = (float) (val / (radius + radius + 1));
            }
        }
    }

    private static void boxBlurFT(final float[] sourceChannel, final float[] targetChannel,
            final int width, final int height, final int radius) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double val = 0;
                for (int iy = (i - radius); iy < (i + radius + 1); iy++) {
                    final int y = Math.min(height - 1, Math.max(0, iy));
                    val += sourceChannel[y * width + j];
                }
                targetChannel[i * width + j] = (float) (val / (radius + radius + 1));
            }
        }
    }

    private static void boxBlurFFH(final float[] sourceChannel, final float[] targetChannel,
            final int width, final int height, final int radius) {
        final float iarr = 1F / (radius + radius + 1);
        for (int i = 0; i < height; i++) {
            int ti = i * width;
            int li = ti;
            int ri = ti + radius;
            final float fv = sourceChannel[ti];
            final float lv = sourceChannel[ti + width - 1];
            float val = (radius + 1) * fv;
            for (int j = 0; j < radius; j++) {
                if (ti + j < sourceChannel.length) {
                    val += sourceChannel[ti + j];
                }
            }
            for (int j = 0; j <= radius; j++) {
                if (ri < sourceChannel.length) {
                    val += sourceChannel[ri++] - fv;
                }
                if (ti < targetChannel.length) {
                    targetChannel[ti++] = val * iarr;
                }
            }
            for (int j = radius + 1; j < width - radius; j++) {
                if (ri < sourceChannel.length && li < sourceChannel.length) {
                    val += sourceChannel[ri++] - sourceChannel[li++];
                }
                if (ti < targetChannel.length) {
                    targetChannel[ti++] = val * iarr;
                }
            }
            for (int j = width - radius; j < width; j++) {
                if (li < sourceChannel.length) {
                    val += lv - sourceChannel[li++];
                }
                if (ti < targetChannel.length) {
                    targetChannel[ti++] = val * iarr;
                }
            }
        }
    }

    private static void boxBlurFFT(final float[] sourceChannel, final float[] targetChannel,
            final int width, final int height, final int radius) {
        final float iarr = 1F / (radius + radius + 1);
        for (int i = 0; i < width; i++) {
            int ti = i;
            int li = ti;
            int ri = ti + radius * width;
            final float fv = sourceChannel[ti];
            final float lv = sourceChannel[ti + width * (height - 1)];
            float val = (radius + 1) * fv;
            for (int j = 0; j < radius; j++) {
                if ((ti + j * width) < sourceChannel.length) {
                    val += sourceChannel[ti + j * width];
                }
            }
            for (int j = 0; j <= radius; j++) {
                if (ri < sourceChannel.length) {
                    val += sourceChannel[ri] - fv;
                }
                if (ti < targetChannel.length) {
                    targetChannel[ti] = val * iarr;
                }
                ri += width;
                ti += width;
            }
            for (int j = radius + 1; j < height - radius; j++) {
                if (ri < sourceChannel.length) {
                    val += sourceChannel[ri] - sourceChannel[li];
                }
                if (ti < targetChannel.length) {
                    targetChannel[ti] = val * iarr;
                }
                li += width;
                ri += width;
                ti += width;
            }
            for (int j = height - radius; j < height; j++) {
                if (li < sourceChannel.length) {
                    val += lv - sourceChannel[li];
                }
                if (ti < targetChannel.length) {
                    targetChannel[ti] = val * iarr;
                }
                li += width;
                ri += width;
            }
        }
    }

    public static void normalise(final float[] sourceChannel, final int scaleFactor) {
        float min = sourceChannel[0];
        float max = sourceChannel[0];
        for (int i = 0; i < sourceChannel.length; i++) {
            min = Math.min(sourceChannel[i], min);
            max = Math.max(sourceChannel[i], max);
        }
        for (int i = 0; i < sourceChannel.length; i++) {
            sourceChannel[i] = ((sourceChannel[i] - min) / (max - min)) * scaleFactor;
        }
    }

    public static void colorise(final float[] sourceChannel, final int[] targetChannel,
            final int threshold, final float severity) {
        if (threshold >= 0 && threshold < 255) {
            if (sourceChannel.length <= targetChannel.length) {
                GaussianBlur.normalise(sourceChannel, 255);
                for (int i = 0; i < sourceChannel.length; i++) {
                    final int paletteIndex = (int) Math.floor(sourceChannel[i]);
                    final int alpha = paletteIndex < threshold ? 0
                            : Math.min((int) Math.floor((paletteIndex * severity) - threshold), 192);
                    targetChannel[i] = GaussianBlur.RAINBOW[paletteIndex] + (alpha << 24);
                }
            } else {
                throw new IllegalArgumentException(TARGET_SMALLER_THAN_SOURCE);
            }
        } else {
            throw new IllegalArgumentException("Threshold must be a value between 0 and 255");
        }
    }
}
