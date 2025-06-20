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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;

/**
 *
 * @author Quasar985
 */
public class HistogramController {

    // Analytic view controller instance
    private static HistogramController instance = null;
    private HistogramTopComponent2 parent;

    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized HistogramController getDefault() {
        if (instance == null) {
            instance = new HistogramController();
        }
        return instance;
    }

    /**
     *
     * @param parent the TopComponent which this controller controls.
     * @return the instance to allow chaining
     */
    public HistogramController init(final HistogramTopComponent2 parent) {
        this.parent = parent;
        return instance;
    }

    public HistogramTopComponent2 getParent() {
        return parent;
    }

    /**
     * Reads the graph's analytic_view_state attribute and populates the Analytic View pane.
     */
    public void readState() {
        if (getParent() == null) {
            return;
        }

        final HistogramPane pane = getParent().createContent();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (pane == null || graph == null) {
            return;
        }

        // Todo: implement
        //PluginExecution.withPlugin(new HistogramStateReaderPlugin(pane)).executeLater(graph);
    }

}
