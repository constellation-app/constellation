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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Definition of context menu button for table view.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 1000)
public class ShowInTableViewContextMenuProvider implements ContextMenuProvider {

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        return elementType == GraphElementType.VERTEX || elementType == GraphElementType.TRANSACTION 
                ? Arrays.asList("Show in Table View") : Collections.emptyList();
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId,
            final Vector3f unprojected) {
        SwingUtilities.invokeLater(() -> {
            final TopComponent tc = WindowManager.getDefault().findTopComponent("TableViewTopComponent");
            if (tc != null) {
                if (!tc.isOpened()) {
                    tc.open();
                }
                tc.requestActive();
                ((TableViewTopComponent) tc).showSelected(elementType, elementId);
            }
        });
    }
}
