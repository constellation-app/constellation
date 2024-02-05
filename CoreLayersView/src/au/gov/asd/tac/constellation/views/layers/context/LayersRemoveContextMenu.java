/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.context;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.GraphContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateElementBitmaskPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * Remove context menu for Layers View
 *
 * @author aldebaran30701
 */
@ServiceProvider(service = GraphContextMenuProvider.class, position = 220)
public class LayersRemoveContextMenu implements GraphContextMenuProvider {

    private static final String LAYER_MENU = "Layers";
    private static final String NO_LAYER_TEXT = "[NO DESCRIPTION]";
    private static final String REMOVE_FROM_LAYER = "Remove Selection from Layer...";

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList(LAYER_MENU, REMOVE_FROM_LAYER);
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (elementType == GraphElementType.VERTEX || elementType == GraphElementType.TRANSACTION) {
            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
            final List<String> currentLayers = new ArrayList<>();

            if (stateAttributeId != GraphConstants.NOT_FOUND) {
                final LayersViewState currentState = graph.getObjectValue(stateAttributeId, 0);

                if (currentState != null) {
                    for (int position = 0; position <= currentState.getLayerCount(); position++) {
                        final BitMaskQuery vxQuery = currentState.getVxQueriesCollection().getQuery(position);
                        final BitMaskQuery txQuery = currentState.getTxQueriesCollection().getQuery(position);

                        if (vxQuery != null && vxQuery.getIndex() > 0) {
                            final String description = StringUtils.isBlank(vxQuery.getDescription())
                                    ? NO_LAYER_TEXT : vxQuery.getDescription();
                            currentLayers.add(String.valueOf(vxQuery.getIndex()) + " - " + description);
                        } else if (txQuery != null && txQuery.getIndex() > 0) {
                            final String description = StringUtils.isBlank(txQuery.getDescription())
                                    ? NO_LAYER_TEXT : txQuery.getDescription();
                            currentLayers.add(String.valueOf(txQuery.getIndex()) + " - " + description);
                        } else {
                            // Do nothing
                        }
                    }
                } else {
                    currentLayers.add("1 - " + NO_LAYER_TEXT);
                }

            } else {
                currentLayers.add("1 - " + NO_LAYER_TEXT);
            }
            return currentLayers;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected) {
        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(
                Integer.parseInt(item.substring(0, 2).trim()), LayerAction.REMOVE, true))
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }
}
