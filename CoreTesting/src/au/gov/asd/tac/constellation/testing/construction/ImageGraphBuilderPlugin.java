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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A plugin that builds a graph from the pixels of an image.
 *
 * @author algol
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@NbBundle.Messages("ImageGraphBuilderPlugin=Image Graph Builder")
public class ImageGraphBuilderPlugin extends SimpleEditPlugin {

    public static final String IMAGE_FILE_PARAMETER_ID = PluginParameter.buildId(ImageGraphBuilderPlugin.class, "image_file");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FileParameterValue> imageFileParameter = FileParameterType.build(IMAGE_FILE_PARAMETER_ID);
        imageFileParameter.setName("Image File");
        imageFileParameter.setDescription("The image file from which to build a graph");
        FileParameterType.setFileFilters(imageFileParameter, new ExtensionFilter("Images", "png", "jpg", "gif"));
        parameters.addParameter(imageFileParameter);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        @SuppressWarnings("unchecked") //imageFiles will be a list of files which extends from object type
        final List<File> imageFiles = (List<File>) parameters.getObjectValue(IMAGE_FILE_PARAMETER_ID);

        for (final File imageFile : imageFiles) {
            final ArrayList<BufferedImage> images = new ArrayList<>();
            if (imageFile.getName().endsWith(".gif")) {
                final ThreeTuple<List<BufferedImage>, List<Integer>, List<Integer>> loadedImageData;
                try {
                    loadedImageData = loadImagesFromStream(imageFile);
                } catch (IOException ex) {
                    throw new PluginException(PluginNotificationLevel.ERROR, ex);
                }

                final BufferedImage firstImage = loadedImageData.getFirst().get(0);
                images.add(firstImage);

                final AffineTransform identity = new AffineTransform();
                identity.setToIdentity();
                for (int i = 1; i < loadedImageData.getFirst().size(); i++) {
                    final BufferedImage currentImage = loadedImageData.getFirst().get(i);
                    final BufferedImage image = new BufferedImage(firstImage.getWidth(), firstImage.getHeight(), firstImage.getType());
                    final Graphics2D g2d = image.createGraphics();
//                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
//                    g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//                    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//                    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                    g2d.drawImage(images.get(i - 1), identity, null);
                    g2d.drawImage(currentImage, new AffineTransform(1, 0, 0, 1, loadedImageData.getSecond().get(i), loadedImageData.getThird().get(i)), null);
                    g2d.dispose();
                    images.add(image);
                }
            } else {
                final BufferedImage loadedImageData;
                try {
                    loadedImageData = loadImage(imageFile);
                    images.add(loadedImageData);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            final int vertexVisibilityAttributeId;
            final boolean multipleFrames = images.size() > 1;
            if (multipleFrames) {
                final int attrLayers = graph.addAttribute(GraphElementType.GRAPH, IntegerAttributeDescription.ATTRIBUTE_NAME, "layers", "layers", 0, null);
                graph.setIntValue(attrLayers, 0, images.size());
                vertexVisibilityAttributeId = VisualConcept.VertexAttribute.VISIBILITY.get(graph);
            } else {
                vertexVisibilityAttributeId = Graph.NOT_FOUND;
            }

            final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
            final int vertexColorAttributeId = VisualConcept.VertexAttribute.COLOR.get(graph);
            final int vertexBackgroundIconAttributeId = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);
            final int vertexXAttributeId = VisualConcept.VertexAttribute.X.get(graph);
            final int vertexYAttributeId = VisualConcept.VertexAttribute.Y.get(graph);
            final int vertexZAttributeId = VisualConcept.VertexAttribute.Z.get(graph);
            final int vertexX2AttributeId = VisualConcept.VertexAttribute.X2.get(graph);
            final int vertexY2AttributeId = VisualConcept.VertexAttribute.Y2.get(graph);
            final int vertexZ2AttributeId = VisualConcept.VertexAttribute.Z2.get(graph);

            final int transactionWeightAttributeId = AnalyticConcept.TransactionAttribute.WEIGHT.get(graph);

            int frame = 0;
            for (BufferedImage image : images) {
                final int w = image.getWidth();
                final int h = image.getHeight();

                int[][] vertexIds = new int[w][h];

                final float zlen = multipleFrames ? 0 : Math.min(w, h) / 4f;
                final float vis = frame / (float) (images.size() - 1);

                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        final int rgb = image.getRGB(x, y);
                        final int a = (rgb >> 24) & 0xff;
                        final int r = (rgb >> 16) & 0xff;
                        final int g = (rgb >> 8) & 0xff;
                        final int b = rgb & 0xff;

                        // Generate Z using grayscale.
                        final float gray = 0;//0.21f * r + 0.71f * g + 0.08f * b;

                        final int vxId = vertexIds[x][y] = graph.addVertex();
                        graph.setStringValue(vertexIdentifierAttributeId, vxId, String.format("%d,%d", x, y));

                        final int yinv = h - y;
                        ConstructionUtilities.setxyz(graph, vxId, vertexXAttributeId, vertexYAttributeId, vertexZAttributeId, x * 2, yinv * 2, -gray * zlen / 255f);
                        ConstructionUtilities.setxyz(graph, vxId, vertexX2AttributeId, vertexY2AttributeId, vertexZ2AttributeId, x * 2, yinv * 2, 0);
                        graph.setStringValue(vertexBackgroundIconAttributeId, vxId, "Background.Flat Square");
                        ConstellationColor color = ConstellationColor.getColorValue(r / 255f, g / 255f, b / 255f, a / 255f);
                        graph.setObjectValue(vertexColorAttributeId, vxId, color);
                        if (multipleFrames) {
                            graph.setFloatValue(vertexVisibilityAttributeId, vxId, vis);
                        }

                        if (x > 0) {
                            final int transactionId = graph.addTransaction(vxId, vertexIds[x - 1][y], false);
                            ConstellationColor otherColor = (ConstellationColor) graph.getObjectValue(vertexColorAttributeId, vertexIds[x - 1][y]);
                            graph.setFloatValue(transactionWeightAttributeId, transactionId, calculateWeight(color, otherColor));
                        }

                        if (y > 0) {
                            final int transactionId = graph.addTransaction(vxId, vertexIds[x][y - 1], false);
                            ConstellationColor otherColor = (ConstellationColor) graph.getObjectValue(vertexColorAttributeId, vertexIds[x][y - 1]);
                            graph.setFloatValue(transactionWeightAttributeId, transactionId, calculateWeight(color, otherColor));
                        }
                    }
                }

                frame++;
            }
        }

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        interaction.setProgress(1, 0, "Completed successfully", true);
    }

    private static float calculateWeight(final ConstellationColor a, final ConstellationColor b) {
        float aGray = a.getRed() * 0.21f + a.getGreen() * 0.71f + a.getBlue() * 0.08f;
        float bGray = b.getRed() * 0.21f + b.getGreen() * 0.71f + b.getBlue() * 0.08f;
        float weight = Math.abs(aGray - bGray);
        weight = (float) Math.exp(-weight);

        return weight;
    }

    /**
     * Load an image from a file.
     *
     * @param name The file to load the image from.
     *
     * @return A BufferedImage containing the image.
     *
     * @throws IOException
     */
    private static BufferedImage loadImage(final File file) throws IOException {
        final ByteArrayOutputStream out;
        try (InputStream in = new FileInputStream(file)) {
            out = new ByteArrayOutputStream();
            final byte[] buf = new byte[1024];
            while (true) {
                final int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                out.write(buf, 0, len);
            }
        }
        out.close();

        return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
    }

    /**
     * Load a series of images from a streaming source (such as a GIF file).
     *
     * @param file The file to load the images from.
     *
     * @return A {@link ThreeTuple} containing the images, as well as their left
     * and top offsets.
     *
     * @throws IOException
     */
    private static ThreeTuple<List<BufferedImage>, List<Integer>, List<Integer>> loadImagesFromStream(final File file) throws IOException {
        final ArrayList<BufferedImage> frames = new ArrayList<>();
        final ArrayList<Integer> delays = new ArrayList<>();
        final ArrayList<Integer> loffsets = new ArrayList<>();
        final ArrayList<Integer> toffsets = new ArrayList<>();

        try (ImageInputStream imageStream = new FileImageInputStream(file)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            ImageReader reader = null;
            while (readers.hasNext()) {
                reader = readers.next();

                final String metaFormat = reader.getOriginatingProvider().getNativeImageMetadataFormatName();
                if ("gif".equalsIgnoreCase(reader.getFormatName()) && !"javax_imageio_gif_image_1.0".equals(metaFormat)) {
                    continue;
                } else {
                    break;
                }
            }

            if (reader == null) {
                throw new IOException("Can't read image format!");
            }

            final boolean isGif = reader.getFormatName().equalsIgnoreCase("gif");
            reader.setInput(imageStream, false, !isGif);

            boolean unknownMetaformat = false;
            for (int index = 0;; index++) {
                try {
                    // Read a frame and its metadata.
                    final IIOImage frame = reader.readAll(index, null);

                    // Add the frame to the list.
                    frames.add((BufferedImage) frame.getRenderedImage());

                    if (unknownMetaformat) {
                        continue;
                    }

                    // Obtain metadata.
                    final IIOMetadata meta = frame.getMetadata();
                    IIOMetadataNode imgRootNode = null;
                    try {
                        imgRootNode = (IIOMetadataNode) meta.getAsTree("javax_imageio_gif_image_1.0");
                    } catch (IllegalArgumentException ex) {
                        unknownMetaformat = true;
                        continue;
                    }

                    final IIOMetadataNode gce = (IIOMetadataNode) imgRootNode.getElementsByTagName("GraphicControlExtension").item(0);
                    delays.add(Integer.parseInt(gce.getAttribute("delayTime")));
                    final IIOMetadataNode imgDescr = (IIOMetadataNode) imgRootNode.getElementsByTagName("ImageDescriptor").item(0);
                    loffsets.add(Integer.parseInt(imgDescr.getAttribute("imageLeftPosition")));
                    toffsets.add(Integer.parseInt(imgDescr.getAttribute("imageTopPosition")));
                } catch (IndexOutOfBoundsException ex) {
                    break;
                }
            }

            reader.dispose();
        }

        return ThreeTuple.create(frames, loffsets, toffsets);
    }
}
