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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A Javafx dialog that will present the {@link PluginParametersPane} GUI for a
 * given {@link PluginParameters} object. This is mainly used when parameters
 * need to be presented to the user for a plugin that doesn't belong to a given
 * CONSTELLATION view.
 *
 * @author sirius
 */
public class PluginParametersDialog extends Stage {

    public static final String OK = "OK";
    public static final String CANCEL = "Cancel";

    private volatile String result;

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * "OK" and "Cancel" buttons are displayed.
     *
     * @param owner The owner for this stage.
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     */
    public PluginParametersDialog(final Window owner, final String title, final PluginParameters parameters) {
        this(owner, title, parameters, (String[]) null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     *
     * @param owner The owner for this stage.
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param options The dialog box button labels, one for each button.
     */
    public PluginParametersDialog(final Window owner, final String title, final PluginParameters parameters, final String... options) throws IllegalArgumentException {

        if (parameters == null) {
            throw new IllegalArgumentException("Parameters must not be null");
        }
        
        initStyle(StageStyle.UTILITY);
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        setTitle(title);

        final BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        final Scene scene = new Scene(root);
        scene.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        setScene(scene);

        final PluginParametersPane parametersPane = PluginParametersPane.buildPane(parameters, null);
        root.setCenter(parametersPane);

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5));
        buttonPane.setHgap(5);
        root.setBottom(buttonPane);

        final String[] labels = options != null && options.length > 0 ? options : new String[]{OK, CANCEL};
        for (final String option : labels) {
            final Button okButton = new Button(option);
            okButton.setOnAction(event -> {
                result = option;
                parameters.storeRecentValues();
                PluginParametersDialog.this.hide();
            });
            buttonPane.getChildren().add(okButton);
        }

        // Without this, some parameter panes have a large blank area at the bottom. Huh?
        this.sizeToScene();

        result = null;
    }

    /**
     * The option that was selected by the user.
     *
     * @return The option that was selected by the user; may be null if the user
     * closed the dialog using the window close button.
     */
    public String getResult() {
        return result;
    }
}
