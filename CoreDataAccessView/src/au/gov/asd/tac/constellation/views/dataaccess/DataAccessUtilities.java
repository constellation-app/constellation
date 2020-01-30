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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewTopComponent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author algol
 */
public class DataAccessUtilities {

    /**
     * A convenience method for getting the Pane used by the Data Access view.
     * <p>
     * This allows dialog boxes to be given parents, for example.
     * <p>
     * If the Data Access view is not opened, it will be.
     *
     * @return The Pane used by the Data Access view.
     */
    public static DataAccessPane getDataAccessPane() {
        if (SwingUtilities.isEventDispatchThread()) {
            return getInternalDataAccessPane();
        }

        final DataAccessPane[] panes = new DataAccessPane[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                panes[0] = getInternalDataAccessPane();
            });
        } catch (final InterruptedException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        return panes[0];
    }

    private static DataAccessPane getInternalDataAccessPane() {
        final TopComponent tc = WindowManager.getDefault().findTopComponent(DataAccessViewTopComponent.class.getSimpleName());
        if (tc != null) {
            if (!tc.isOpened()) {
                tc.open();
            }
            tc.requestVisible();
            return ((DataAccessViewTopComponent) tc).getDataAccessPane();
        } else {
            return null;
        }
    }
}
