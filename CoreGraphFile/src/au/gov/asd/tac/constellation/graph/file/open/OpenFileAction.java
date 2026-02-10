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
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.GraphFilePluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

/**
 * Action which allows user to open a file.
 *
 * @author sol695510
 */
@ActionRegistration(
        displayName = "#LBL_openFile",
        iconBase = "au/gov/asd/tac/constellation/graph/file/open/resources/openFile.png",
        iconInMenu = false)
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.graph.file.open.OpenFileAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 300),
    @ActionReference(path = "Shortcuts", name = "C-O")})
public class OpenFileAction implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(OpenFileAction.class.getName());

    /**
     * {@inheritDoc} Displays a file chooser dialog and opens the selected
     * files.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final StoreGraph sg = new StoreGraph();

        try {
            PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE).executeNow(sg);
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            Thread.currentThread().interrupt();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
