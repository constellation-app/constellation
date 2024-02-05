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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.GraphContextMenuProvider;

/**
 * Merge Nodes Context Menu
 *
 * @author algol
 */
@ServiceProvider(service = GraphContextMenuProvider.class, position = 700)
public class PermanentMergeNodeContextMenu implements GraphContextMenuProvider {

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.VERTEX) {
            return Arrays.asList("Merge Nodes");
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int element, final Vector3f unprojected) {
        final GraphNode gNode = GraphNode.getGraphNode(graph);
        final PermanentMergeAction action = new PermanentMergeAction(gNode);
        new Thread(() -> {
            try {
                action.execute(element);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
