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
package au.gov.asd.tac.constellation.functionality.welcome;

import au.gov.asd.tac.constellation.graph.file.open.RecentFilesWelcomePage;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot.RecentGraphScreenshotUtilities;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.awt.BorderLayout;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 *
 * WelcomeTopComponent is designed to inform users of news about Constellation.
 *
 * @author canis_majoris
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.visual.welcome//Welcome//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "WelcomeTopComponent",
        iconBase = "org/netbeans/modules/autoupdate/ui/info_icon.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(
        mode = "editor",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.functionality.welcome.WelcomeTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_WelcomeTopComponentAction",
        preferredID = "WelcomeTopComponent"
)
@Messages({
    "CTL_WelcomeTopComponentAction=Welcome Page",
    "CTL_WelcomeTopComponentTopComponent=Welcome Page",
    "HINT_WelcomeTopComponentTopComponent=Welcome Page"
})
public final class WelcomeTopComponent extends TopComponent {

    private JFXPanel panel = new JFXPanel();
    private static final String WELCOME_THEME = "resources/welcome_theme.css";
    public static final String ERROR_BUTTON_MESSAGE = String.format("%s Information", BrandingUtilities.APPLICATION_NAME);
    public static final String WELCOME_TEXT = "Welcome to Constellation";
    public static final double SPLIT_POS = 0.2;

    //Place holder images
    public static final String LOGO = "resources/constellation-logo.png";

    protected static final Button[] pluginButtons = new Button[10];
    protected static final Button[] recentGraphButtons = new Button[10];

    public WelcomeTopComponent() {
        setName(Bundle.CTL_WelcomeTopComponentTopComponent());
        setToolTipText(Bundle.HINT_WelcomeTopComponentTopComponent());
        setLayout(new BorderLayout());

        ProxyUtilities.setProxySelector(null);

        // Add the JavaFX container to this topcomponent (this enables JavaFX and Swing interoperability):
        add(panel, BorderLayout.CENTER);

        ConstellationSecurityManager.startSecurityLaterFX(() -> {
            Platform.setImplicitExit(false);

            //The root swing object to be inseted into a JFX Panel
            BorderPane root = new BorderPane();

            final SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(Orientation.HORIZONTAL);

            splitPane.setStyle("-fx-background-color: transparent;");
            root.setCenter(splitPane);

            //Create VBox to handle Browser and controls,
            //or error messages
            final VBox leftVBox = new VBox();
            splitPane.getItems().add(leftVBox);
            leftVBox.setSpacing(20);
            leftVBox.setMinWidth(350);
            leftVBox.setPrefWidth(400);
            leftVBox.setMaxWidth(450);

            final HBox logoHBox = new HBox();
            logoHBox.setBackground(new Background(new BackgroundFill(Color.valueOf("white"), CornerRadii.EMPTY, Insets.EMPTY)));
            ImageView logoView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(LOGO)));
            logoView.setFitHeight(100);
            logoView.setFitWidth(150);
            logoHBox.getChildren().add(logoView);
            logoHBox.setAlignment(Pos.CENTER);
            leftVBox.getChildren().add(logoHBox);

            //Create the labels for the left pane
            Label welcome = new Label(WELCOME_TEXT);
            welcome.setFont(new Font("Arial Unicode MS", 26));
            welcome.setAlignment(Pos.CENTER);
            leftVBox.getChildren().add(welcome);

            //Create right VBox for graph controls
            final VBox rightVBox = new VBox();
            rightVBox.setPadding(new Insets(50, 50, 50, 50));
            rightVBox.setBackground(new Background(new BackgroundFill(Color.valueOf("#14161a"), CornerRadii.EMPTY, Insets.EMPTY)));
            splitPane.getItems().add(rightVBox);

            //Create HBoxes for the right_vbox
            final HBox topHBox = new HBox();
            final HBox bottomHBox = new HBox();

            //hbox formatting
            topHBox.setPadding(new Insets(50, 0, 50, 0));
            topHBox.setSpacing(10);
            bottomHBox.setPadding(new Insets(50, 0, 50, 0));
            bottomHBox.setSpacing(10);

            getWelcomeContent();

            for (int i = 0; i < 10; i++) {
                if (pluginButtons[i] != null) {
                    Button currentButton = pluginButtons[i];
                    pluginButtons[i].setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            Lookup.getDefault().lookupAll(WelcomePageProvider.class).forEach(plugin -> {
                                if (currentButton == plugin.getButton()) {
                                    plugin.run();
                                }
                            });
                        }
                    });
                    if (i < 4) {
                        setButtonProps(pluginButtons[i]);
                        topHBox.getChildren().add(pluginButtons[i]);
                    } else {
                        setInfoButtons(pluginButtons[i]);
                        leftVBox.getChildren().add(pluginButtons[i]);
                    }
                }
            }

            leftVBox.setAlignment(Pos.TOP_CENTER);

            //formatting for bottom hbox
            Label recent = new Label("Recent");
            recent.setFont(new Font("Arial Unicode MS", 24));

            rightVBox.getChildren().add(topHBox);
            rightVBox.getChildren().add(recent);
            rightVBox.getChildren().add(bottomHBox);

            FlowPane flow = new FlowPane();
            flow.setPrefWrapLength(1000);
            flow.setHgap(20);
            flow.setVgap(20);

            //Create the buttons for the recent page
            final List<String> fileNames = RecentFilesWelcomePage.getFileNames();
            for (int i = 0; i < 10; i++) {
                recentGraphButtons[i] = new Button();
                //if the user has recent files get the names
                //and make them the text of the buttons
                createRecentButtons(recentGraphButtons[i]);
                if (i < fileNames.size()) {
                    recentGraphButtons[i].setText(fileNames.get(i));
                }
                final String text = recentGraphButtons[i].getText();

                final String screenshotFilename = RecentGraphScreenshotUtilities.getScreenshotsDir() + File.separator + text + ".png";
                if (new File(screenshotFilename).exists()) {
                    final ImageView imageView = new ImageView(new Image("File:/" + screenshotFilename));
                    imageView.setFitHeight(recentGraphButtons[i].getPrefHeight());
                    imageView.setFitWidth(recentGraphButtons[i].getPrefWidth());
                    recentGraphButtons[i].setGraphic(imageView);
                }

                //Calls the method for the recent graphs to open
                // on the button action
                recentGraphButtons[i].setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        RecentFilesWelcomePage.openGraph(text);
                    }
                });
                flow.getChildren().add(recentGraphButtons[i]);
            }

            bottomHBox.getChildren().add(flow);
            splitPane.getDividers().get(0).setPosition(SPLIT_POS);
            VBox.setVgrow(rightVBox, Priority.ALWAYS);

            //Finally, insert the root object into a scene, and insert the
            //scene into the JavaFX panel.
            final Scene scene = new Scene(root, Color.web("1d1d1d"));
            scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
            scene.getStylesheets().add(WelcomeTopComponent.class.getResource(WELCOME_THEME).toExternalForm());
            panel.setScene(scene);
        });
    }

    public void setButtonProps(Button button) {
        button.setPrefSize(125, 125);
        button.setMaxSize(150, 150);
        button.setStyle("-fx-background-color: #2e4973;");
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
    }

    public void createRecentButtons(Button button) {
        button.setPrefSize(160, 160);
        button.setMaxSize(175, 175);
        button.setStyle("-fx-background-color: #333333; -fx-background-radius: 10px; -fx-text-fill: white;");
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
    }

    public void setInfoButtons(Button button) {
        button.setPrefSize(325, 45);
        button.setMaxSize(350, 50);
        button.setStyle("-fx-background-color: transparent;");
        button.setCursor(Cursor.HAND);
        button.setAlignment(Pos.CENTER_LEFT);
    }

    private void getWelcomeContent() {
        Lookup.getDefault().lookupAll(WelcomePageProvider.class).forEach(plugin -> {
            if (plugin.isVisible()) {
                for (int i = 0; i < 10; i++) {
                    if (pluginButtons[i] == null) {
                        pluginButtons[i] = plugin.getButton();
                        break;
                    }
                }
            }
        });
    }

    void writeProperties(final java.util.Properties p) {
    }

    void readProperties(final java.util.Properties p) {
    }

////    /**
////     * This method is called from within the constructor to initialize the form.
////     * WARNING: Do NOT modify this code. The content of this method is always
////     * regenerated by the Form Editor.
////     */
////    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();

        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.columnWidths = new int[] {2};
        layout.rowHeights = new int[] {2};
        setLayout(layout);

        java.awt.GridBagLayout jPanel3Layout = new java.awt.GridBagLayout();
        jPanel3Layout.columnWidths = new int[] {1};
        jPanel3Layout.rowHeights = new int[] {1};
        jPanel3.setLayout(jPanel3Layout);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 1.0;
        add(jPanel3, gridBagConstraints);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(66, 66, 66));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WelcomeTopComponent.class, "WelcomeTopComponent.jLabel1.text_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 0, 0);
        add(jLabel1, gridBagConstraints);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        add(jPanel2, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
}
