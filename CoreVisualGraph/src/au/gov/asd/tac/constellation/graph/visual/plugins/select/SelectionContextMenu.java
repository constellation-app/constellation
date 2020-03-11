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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Selection Context Menu
 *
 * @author sirius
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 800)
public class SelectionContextMenu implements ContextMenuProvider {

    private static final String INVERT_SELECTION = "Invert Selection";

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList("Selection");
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.GRAPH) {
            return Arrays.asList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(String item, Graph graph, GraphElementType elementType, int entity, Vector3f unprojected) {
        switch (item) {

            case INVERT_SELECTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.INVERT_SELECTION).executeLater(graph);
                break;
        }
    }
}
