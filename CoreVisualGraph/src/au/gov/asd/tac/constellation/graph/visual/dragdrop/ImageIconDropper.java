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
package au.gov.asd.tac.constellation.graph.visual.dragdrop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.GraphIndexUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.icon.ImageIconData;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.openide.util.lookup.ServiceProvider;

/**
 * Allows an image object or an image file to be dropped onto the graph. The
 * currently selected vertices will have their icon changed to show the image.
 *
 * @author sirius
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
@ServiceProvider(service = GraphDropper.class, position = 10000)
public class ImageIconDropper implements GraphDropper {

    private static final Logger LOGGER = Logger.getLogger(ImageIconDropper.class.getName());

    private static final DataFlavor IMAGE_FLAVOR;
    private static final DataFlavor IMAGE_FILE_FLAVOR;

    static {
        DataFlavor imageFlavor = null;
        DataFlavor imageFileFlavor = null;
        try {
            imageFlavor = new DataFlavor("image/x-java-image;class=java.awt.Image");
            imageFileFlavor = new DataFlavor("application/x-java-file-list;class=java.util.List");
        } catch (final ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        IMAGE_FLAVOR = imageFlavor;
        IMAGE_FILE_FLAVOR = imageFileFlavor;
    }

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {
        try {
            final Transferable transferable = dtde.getTransferable();
            BufferedImage image = null;
            if (transferable.isDataFlavorSupported(IMAGE_FLAVOR)) {
                final Object data = transferable.getTransferData(IMAGE_FLAVOR);
                if (data instanceof BufferedImage bufferedImage) {
                    image = bufferedImage;
                }
            } else if (transferable.isDataFlavorSupported(IMAGE_FILE_FLAVOR)) {
                final Object data = transferable.getTransferData(IMAGE_FILE_FLAVOR);
                if (data instanceof List list) {
                    @SuppressWarnings("unchecked") //data is be list of files which extends from object type
                    final List<File> fileList = list;
                    if (fileList.size() == 1) {
                        final File file = fileList.get(0);
                        image = ImageIO.read(file);
                    }
                }
            }
            
            if (image != null) {
                final BufferedImage resultImage = image;
                return (graph, dropInfo) -> {
                    final String iconName = loadDraggedImage(resultImage);
                    if (iconName != null) {
                        PluginExecution.withPlugin(new SetVertexIconsPlugin(iconName)).interactively(true).executeLater(graph);
                    }
                };
            }
        } catch (final UnsupportedFlavorException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        return null;
    }

    private static String loadDraggedImage(final BufferedImage image) {
        final String iconName = "DragAndDropIcon." + UUID.randomUUID();
        final ConstellationIcon icon = IconManager.getIcon(iconName);

        if (icon != null) {
            return iconName;
        }

        try {
            final ConstellationIcon customIcon = new ConstellationIcon.Builder(iconName, new ImageIconData(image)).build();
            final boolean added = IconManager.addIcon(customIcon);
            if (!added) {
                return null;
            }

            return iconName;

        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            return null;
        }
    }

    /**
     * Plugin to set the foreground icon of the selected vertices to the given icon.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class SetVertexIconsPlugin extends SimpleEditPlugin {

        final String iconName;

        public SetVertexIconsPlugin(final String iconName) {
            this.iconName = iconName;

        }

        @Override
        public String getName() {
            return "Set Vertex Icons";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
            final int iconAttr = VisualConcept.VertexAttribute.FOREGROUND_ICON.get(graph);
            if (selectedAttr != Graph.NOT_FOUND && iconAttr != Graph.NOT_FOUND) {
                final GraphIndexResult selectionResult = GraphIndexUtilities.filterElements(graph, selectedAttr, true);
                int vertex = selectionResult.getNextElement();
                while (vertex != Graph.NOT_FOUND) {
                    graph.setStringValue(iconAttr, vertex, iconName);
                    vertex = selectionResult.getNextElement();
                }
            }
            ConstellationLoggerHelper.importPropertyBuilder(this, Arrays.asList(iconName), null, ConstellationLoggerHelper.SUCCESS);
        }
    }
}
