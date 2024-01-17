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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javax.swing.JButton;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    
    private final String[] acceptanceButtonLabels = {"OK", "Import", "Export", "Save", "Open", "Build", "Create", "Load", "Rename", "Add", "Remove"};
    public static final String CANCEL = "Cancel";
    public static final String OK = "OK";

    private volatile String result;
    private final String title;
    private final String acceptanceText;
    private final JFXPanel xp;

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * Acceptance Button displayed as "OK" or "Import" or "Export" or "Save" or "Open" or "Build" or "Create" or "Load" or "Rename"
     * Rejection Button displayed as "Cancel".
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters) {
        this(title, parameters, null, null, null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * Acceptance Button displayed as "OK" or "Import" or "Export" or "Save" or "Open" or "Build" or "Create" or "Load" or "Rename"
     * Rejection Button displayed as "Cancel".
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param excludedParameters Plugin parameters to exclude from the dialog
     * box.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters, final Set<String> excludedParameters) {
        this(title, parameters, excludedParameters, null, null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * Acceptance Button displayed as "OK" or "Import" or "Export" or "Save" or "Open" or "Build" or "Create" or "Load" or "Rename"
     * Rejection Button displayed as "Cancel".
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param helpID The JavaHelp ID of the help.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters, final String helpID) {
        this(title, parameters, null, null, null);
    }

    /**
     * Display a dialog box containing the parameters that allows the user to
     * enter values.
     * <p>
     * Acceptance Button displayed as "OK" or "Import" or "Export" or "Save" or "Open" or "Build" or "Create" or "Load" or "Rename"
     * Rejection Button displayed as "Cancel".
     *
     * @param title The dialog box title.
     * @param parameters The plugin parameters.
     * @param excludedParameters Plugin parameters to exclude from the dialog
     * box.
     * @param acceptanceText acceptance Button text
     * @param helpID The JavaHelp ID of the help.
     */
    public PluginParametersSwingDialog(final String title, final PluginParameters parameters, final Set<String> excludedParameters, final String acceptanceText, final String helpID) {
        this.title = title;
        this.acceptanceText = acceptanceText;
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

    public void setSize(final Dimension dimension) {
        xp.setPreferredSize(dimension);
    }

    /**
     * Generates a DialogDescripter window and waits until the user
     * to select an option (the acceptance option will be highlighted). 
     * The acceptance option is "OK" by default.
     * The rejectionoOption is "Cancel" by default. 
     * If a keyword is present in the title of the DialogDisplayer
     * the acceptance option will adjust dynamically to express that keyword 
     * (i.e. "Build", "Save", "Export")
     */
    public void showAndWait() {
        final DialogDescriptor dd = createDialogDescriptor(true);  
        final Object r = DialogDisplayer.getDefault().notify(dd);
        if (r == DialogDescriptor.CANCEL_OPTION){
            result = CANCEL;
        } else if (r == DialogDescriptor.OK_OPTION){
            result = OK;
        } else {
            result = null;
        }
    }

    /**
     * Generates a DialogDescripter window and waits until the user
     * to select an option (No option will be highlighted). 
     * The acceptance option is "OK" by default.
     * The rejectionoOption is "Cancel" by default. 
     * If a keyword is present in the title of the DialogDisplayer
     * the acceptance option will adjust dynamically to express that keyword 
     * (i.e. "Build", "Save", "Export")
     */
    public void showAndWaitNoFocus() {
        final DialogDescriptor dd = createDialogDescriptor(false);  
        final Object r = DialogDisplayer.getDefault().notify(dd);
        if (r == DialogDescriptor.CANCEL_OPTION){
            result = CANCEL;
        } else if (r == DialogDescriptor.OK_OPTION){
            result = OK;
        } else {
            result = null;
        }
    }
    
    /**
     * Generates a DialogDescripter window and dynamically sets the 
     * acceptance option based on the title. 
     * The acceptance option is "OK" by default.
     * The rejectionoOption is "Cancel" by default. 
     * If a keyword is present in the title of the DialogDisplayer
     * the acceptance option will adjust dynamically to express that keyword 
     * (i.e. "Build", "Save", "Export")
     * @param focused a Boolean representing if the 
     * acceptance option should be highlighted by default
     * @Return
     */
    private DialogDescriptor createDialogDescriptor(final boolean focused) {
        // Generate options
        final Object[] options = new Object[2];
        final JButton acceptanceOption = new JButton(getAcceptanceButton());
        
        options[0] = acceptanceOption; 
        options[1] = DialogDescriptor.CANCEL_OPTION; 
        
        //Having 'No' button as initial value means focus is off of 'OK' and 'Cancel' buttons
        final Object focus = focused ? acceptanceOption : DialogDescriptor.NO_OPTION;
        
        final DialogDescriptor dd = new DialogDescriptor(xp, title, true, options, focus, DialogDescriptor.DEFAULT_ALIGN, null, null);
        
        // Create an action listener for the custom button
        final ActionListener al = (ActionEvent e) -> dd.setValue(NotifyDescriptor.OK_OPTION);
        acceptanceOption.addActionListener(al);

        return dd;
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

    /**
     * Generates the text of the PluginParameterPane acceptance button based on keywords in the Pane title.
     * If developers have specified an acceptance button text, this specified text is returned.
     * If no text was specified a key word is extracted from the title and returned. 
     * If no word key word is extracted, "OK" is returned
     * 
     * @return 
     */
    private String getAcceptanceButton() {
        if (!StringUtils.isBlank(acceptanceText)) {
            return acceptanceText;
        }
        for (final String keyWord : acceptanceButtonLabels){
            if (StringUtils.containsIgnoreCase(title, keyWord)){
                return keyWord;
            }
        }
        return PluginParametersSwingDialog.OK;
    }
    
    /**
     * Checks to see if the acceptance button was selected.
     * @return 
     */
    public boolean isAccepted() {
        return OK.equals(this.result);
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
