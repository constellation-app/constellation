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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.GraphContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Allow the user to open the Attribute Editor.
 * <p>
 * The element on which the menu was opened will be selected (and everything
 * unselected) so it displays in the editor.
 *
 * @author algol
 */
@ServiceProvider(service = GraphContextMenuProvider.class, position = 900)
public class ShowInAttributeEditorContextMenu implements GraphContextMenuProvider {

    private static final String TEXT = "Show in Attribute Editor";

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (elementType == GraphElementType.VERTEX || elementType == GraphElementType.TRANSACTION) {
            return Arrays.asList(TEXT);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected) {
        PluginExecution.withPlugin(new ShowInEditorPlugin(elementType, elementId)).executeLater(graph);
    }

    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    public static class ShowInEditorPlugin extends SimpleEditPlugin {

        final GraphElementType elementType;
        final int elementId;

        public ShowInEditorPlugin(final GraphElementType elementType, final int elementId) {
            this.elementType = elementType;
            this.elementId = elementId;
        }

        @Override
        public String getName() {
            return TEXT;
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            // Unselect vertices that need unselecting.
            final int vxSelectedId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            final int vxCount = wg.getVertexCount();
            for (int position = 0; position < vxCount; position++) {
                final int vxId = wg.getVertex(position);

                if (wg.getBooleanValue(vxSelectedId, vxId)) {
                    wg.setBooleanValue(vxSelectedId, vxId, false);
                }
            }

            // Unselect transactions that need unselecting.
            final int txSelectedId = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            final int txCount = wg.getTransactionCount();
            for (int position = 0; position < txCount; position++) {
                final int txId = wg.getTransaction(position);

                if (wg.getBooleanValue(txSelectedId, txId)) {
                    wg.setBooleanValue(txSelectedId, txId, false);
                }
            }

            // Select the element under the popup menu.
            final int selectedId = elementType == GraphElementType.VERTEX ? vxSelectedId : txSelectedId;
            wg.setBooleanValue(selectedId, elementId, true);

            // Open the value editor if it isn't open already.
            SwingUtilities.invokeLater(() -> {
                final TopComponent tc = WindowManager.getDefault().findTopComponent(AttributeEditorTopComponent.class.getSimpleName());
                if (tc != null) {
                    if (!tc.isOpened()) {
                        tc.open();
                    }
                    tc.requestActive();
                }
            });
        }
    }
}
