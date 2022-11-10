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
package au.gov.asd.tac.constellation.views.find2.utilities;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import java.awt.EventQueue;
import java.util.Set;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Utility class for the Find View
 *
 * @author Delphinus8821
 */
public class FindViewUtilities {
    
    private FindViewUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Changes the active graph to the one where a graph element has been found and selected
     * 
     * @param graph
     */
    public static void searchAllGraphs(final GraphWriteMethods graph) {
        final Set<TopComponent> topComponents = WindowManager.getDefault().getRegistry().getOpened();
        if (topComponents != null) {
            for (final TopComponent component : topComponents) {
                if ((component instanceof VisualGraphTopComponent) && ((VisualGraphTopComponent) component).getGraphNode().getGraph().getId().equals(graph.getId())) {
                    EventQueue.invokeLater(() -> ((VisualGraphTopComponent) component).requestActive());
                    break;
                }
            }
        }
    }
}
