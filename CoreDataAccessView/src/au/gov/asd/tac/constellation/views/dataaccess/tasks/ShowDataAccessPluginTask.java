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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessSearchProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javax.swing.Icon;
import org.openide.awt.NotificationDisplayer;

/**
 * Task executed by the {@link DataAccessSearchProvider} for matching results.
 * As a user types in the quick search looking for data access plugins. Plugin
 * names that get a match will have this task created for them and executed.
 * <p/>
 * The task results in the plugin with the given name being expanded on the current
 * pane.
 * 
 * @author formalhaunt
 */
public class ShowDataAccessPluginTask implements Runnable {
    private static final String DAV_STEP_STRING = "Please open the Data Access view and create a step.";
    private static final String STEP_STRING = "Please create a step in the Data Access view.";

    private static final String NOTIFICATION_TITLE = "Data Access View";
    
    private static final Icon WARNING_ICON = UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor());
    
    private final String pluginName;

    /**
     * Create a new show data access plugin task
     * 
     * @param pluginName the name of the plugin to find and expand
     */
    public ShowDataAccessPluginTask(final String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Gets the data access pane view and gets the pane of the current tab. Searches
     * the pane for the provided plugin name and if found, it will expand that plugin.
     * <p/>
     * If the data access view is closed or there is not current tab then a error
     * notification is displayed.
     */
    @Override
    public void run() {
        final String message;
        final DataAccessPane dataAccessPane = DataAccessUtilities.getDataAccessPane();
        if (dataAccessPane != null) {
            final Tab tab = dataAccessPane.getDataAccessTabPane().getCurrentTab();
            if (tab != null) {
                final QueryPhasePane queryPhasePane = DataAccessTabPane.getQueryPhasePane(tab);
                
                Platform.runLater(() -> queryPhasePane.expandPlugin(pluginName));

                return;
            } else {
                message = STEP_STRING;
            }
        } else {
            message = DAV_STEP_STRING;
        }

        NotificationDisplayer.getDefault().notify(
                NOTIFICATION_TITLE,
                WARNING_ICON,
                message,
                null
        );
    }
}
