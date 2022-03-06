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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.plugins.importexport.ConfigurationPane;
import au.gov.asd.tac.constellation.plugins.importexport.ImportPane;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import javafx.scene.input.KeyCode;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

public class JDBCImportPane extends ImportPane {

    private static final String HELP_CTX = JDBCImportPane.class.getName();

    public JDBCImportPane(final JDBCImportTopComponent jdbcImportTopComponent,
            final JDBCImportController importController, final ConfigurationPane configurationPane,
            final JDBCSourcePane sourcePane) {
        super(jdbcImportTopComponent, importController, configurationPane, sourcePane);

        loadButton.setOnAction(event -> {
            if (configurationPane.hasDataQueried()) {
                ImportJDBCIO.loadParameters(this.getParentWindow(), importController);
            } else {
                NotifyDisplayer.display("Run a query first.", NotifyDescriptor.WARNING_MESSAGE);
            }
        });

        // save menu item
        saveButton.setOnAction(event -> ImportJDBCIO.saveParameters(this.getParentWindow(), importController));

        helpButton.setOnAction(event -> new HelpCtx(HELP_CTX).display());
        // Setting help keyevent to f1
        setOnKeyPressed(event -> {
            final KeyCode c = event.getCode();
            if (c == KeyCode.F1) {
                new HelpCtx(HELP_CTX).display();
            }
        });
    }
}
