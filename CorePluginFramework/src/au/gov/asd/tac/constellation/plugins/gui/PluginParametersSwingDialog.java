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
import java.awt.Dimension;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 * Embed a JavaFX PluginParametersPane in a DialogDescriptor. This is akin to
 * {@link PluginParametersDialog} except for Swing based GUIs rather than JavaFX
 * based GUIs.
 *
 * @see PluginParametersDialog
 * @author algol
 */
public class PluginParametersSwingDialog {
    
    private static final Logger LOGGER = Logger.getLogger(PluginParametersSwingDialog.class.getName());

    public static final String OK = "OK";
    public static final String CANCEL = "Cancel";

    private volatile String result;

    private final String title;
    private final JFXPanel xp;

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * "OK" and "Cancel" buttons are displayed.
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters) {
        this(title, parameters, null, null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * "OK" and "Cancel" buttons are displayed.
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param excludedParameters Plugin parameters to exclude from the dialog
     * box.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters, final Set<String> excludedParameters) {
        this(title, parameters, excludedParameters, null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * "OK" and "Cancel" buttons are displayed.
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param helpID The JavaHelp ID of the help.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters, final String helpID) {
        this(title, parameters, null, null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * "OK" and "Cancel" (and "Help" if helpID is non-null) buttons are
     * displayed.
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param excludedParameters Plugin parameters to exclude from the dialog
     * box.
     * @param helpID The JavaHelp ID of the help.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters, final Set<String> excludedParameters, final String helpID) {
//        if(!SwingUtilities.isEventDispatchThread())
//        {
//            throw new IllegalStateException("Not event dispatch thread");
//        }

        this.title = title;

        final CountDownLatch latch = new CountDownLatch(1);
        xp = helpID != null ? new JFXPanelWithHelp(helpID) : new JFXPanel();
        Platform.runLater(() -> {
            final BorderPane root = new BorderPane();
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: #F0F0F0;");

            // Attempt to give the window a sensible width and/or height.
            root.setMinWidth(500);

            final PluginParametersPane parametersPane = PluginParametersPane.buildPane(parameters, null, excludedParameters);
            root.setCenter(parametersPane);
            final Scene scene = new Scene(root);

            // TODO: the main stylesheet isn't loaded here and should be
            // something like the following
//            scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
//            scene.getStylesheets().add(JavafxStyleManager.getDynamicStyleSheet());
            xp.setScene(scene);
            xp.setPreferredSize(new Dimension((int) scene.getWidth(), (int) scene.getHeight()));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            Thread.currentThread().interrupt();
        }
    }

    public void showAndWait() {
        final DialogDescriptor dd = new DialogDescriptor(xp, title);
        final Object r = DialogDisplayer.getDefault().notify(dd);
        if (r == DialogDescriptor.OK_OPTION) {
            result = OK;
        } else {
            result = r == DialogDescriptor.CANCEL_OPTION ? CANCEL : null;
        }
    }

    public void showAndWaitNoFocus() {
        //Having 'No' button as initial value means focus is off of 'OK' and 'Cancel' buttons
        final DialogDescriptor dd = new DialogDescriptor(xp, title, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.NO_OPTION, null);
        final Object r = DialogDisplayer.getDefault().notify(dd);
        if (r == DialogDescriptor.OK_OPTION) {
            result = OK;
        } else {
            result = r == DialogDescriptor.CANCEL_OPTION ? CANCEL : null;
        }
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

    private static class JFXPanelWithHelp extends JFXPanel implements HelpCtx.Provider {

        private final String helpID;

        public JFXPanelWithHelp(final String helpID) {
            this.helpID = helpID;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(helpID);
        }
    }
}
