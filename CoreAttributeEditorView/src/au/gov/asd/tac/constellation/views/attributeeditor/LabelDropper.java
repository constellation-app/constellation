/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper.DropInfo;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * Drop a label indicator onto a graph, change the top node label or transaction
 * label.
 *
 * @author algol
 */
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"IMPORT"})
@ServiceProvider(service = GraphDropper.class)
public class LabelDropper implements GraphDropper {
    
    private static final Logger LOGGER = Logger.getLogger(LabelDropper.class.getName());

    private static final String INDICATOR = "Attribute.Label=";
    private static final DataFlavor ATTRIBUTE_LABEL_FLAVOR;

    static {
        DataFlavor alf = null;
        try {
            alf = new DataFlavor(AttributeEditorPanel.MIMETYPE);
        } catch (final ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        ATTRIBUTE_LABEL_FLAVOR = alf;
    }

    @Override
    public BiConsumer<Graph, DropInfo> drop(final DropTargetDropEvent dtde) {
        final Transferable transferable = dtde.getTransferable();
        if (transferable.isDataFlavorSupported(ATTRIBUTE_LABEL_FLAVOR) || transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                final String data;
                if (transferable.isDataFlavorSupported(ATTRIBUTE_LABEL_FLAVOR)) {
                    final InputStream in = new ByteArrayInputStream(((ByteBuffer) transferable.getTransferData(ATTRIBUTE_LABEL_FLAVOR)).array());
                    final ObjectInputStream oin = new ObjectInputStream(in);
                    data = (String) oin.readObject();
                } else {
                    String t = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    // Do we have the correct indicator?
                    if (t != null && t.startsWith(INDICATOR)) {
                        // Skip the leading "indicator=".
                        data = t.substring(INDICATOR.length()).trim();
                    } else {
                        data = null;
                    }
                }
                if (data != null) {
                    return (graph, dropInfo) -> {
                        PluginExecution.withPlugin(new SetTopLabelPlugin(data)).executeLater(graph);
                    };
                }

            } catch (final UnsupportedFlavorException | IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            } catch (final ClassCastException ex) {
                // This exception occurs when dragging a label from Attribute Editor to graph area
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }

        return null;
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class SetTopLabelPlugin extends SimpleEditPlugin {

        final String data;

        public SetTopLabelPlugin(final String data) {
            this.data = data;
        }

        @Override
        public String getName() {
            return "Attribute Editor: Set Top Label";
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final GraphElementType et;
            final String labelAttribute;
            final int ix = data.indexOf(':');
            if (ix != -1) {
                et = GraphElementType.getValue(data.substring(0, ix));
                labelAttribute = data.substring(ix + 1);

                final int attrId = wg.getAttribute(et, labelAttribute);
                final int labelsId = et == GraphElementType.VERTEX ? VisualConcept.GraphAttribute.TOP_LABELS.get(wg) : VisualConcept.GraphAttribute.TRANSACTION_LABELS.get(wg);
                final ConstellationColor color = et == GraphElementType.VERTEX ? wg.getSchema().getFactory().getVertexLabelColor() : wg.getSchema().getFactory().getConnectionLabelColor();

                if (attrId != Graph.NOT_FOUND && labelsId != Graph.NOT_FOUND) {
                    final GraphLabels oldGraphLabels = wg.getObjectValue(labelsId, 0);
                    final List<GraphLabel> newLabels = new ArrayList<>();
                    if (oldGraphLabels != null) {
                        newLabels.addAll(oldGraphLabels.getLabels());
                    }
                    newLabels.add(new GraphLabel(labelAttribute, color));

                    wg.setObjectValue(labelsId, 0, new GraphLabels(newLabels));
                }

                ConstellationLoggerHelper.importPropertyBuilder(
                        this,
                        Arrays.asList(data),
                        null,
                        ConstellationLoggerHelper.SUCCESS
                );
            }
        }
    }

}
