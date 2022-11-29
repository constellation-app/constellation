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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.save.AutosaveUtilities;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.modules.OnStop;
import org.openide.util.NbBundle.Messages;

/**
 * Clean up autosaves for unclosed VisualTopComponents.
 * <p>
 * When the user closes the application window, the individual
 * VisualTopComponent componentClosing() method isn't called. If we get here, a
 * user-initiated close is happening. Therefore, we can clean up autosaves here.
 * <p>
 * We get rid of the autosaves relating to open graphs, and clean up any
 * dangling files (stars without autos, autos without stars).
 *
 * @author algol
 */
@OnStop
@Messages({
    "# {0} - autosave id",
    "MSG_CleanedUp=Cleaned up autosave {0}"
})
public final class AutosaveStop implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AutosaveStop.class.getName());

    @Override
    public void run() {
        final Map<String, Graph> graphs = GraphNode.getAllGraphs();
        for (final Map.Entry<String, Graph> entry : graphs.entrySet()) {
            final Graph graph = entry.getValue();
            AutosaveUtilities.deleteAutosave(graph.getId());
            LOGGER.info(Bundle.MSG_CleanedUp(graph.getId()));
        }

        AutosaveUtilities.cleanup();
    }
}
