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

import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.awt.BorderLayout;
import java.text.ParseException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
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
import javafx.scene.web.WebView;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

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
    private static final String WELCOME_WEBVIEW = "resources/welcome_webview.css";
    public static final String ERROR_BUTTON_MESSAGE = String.format("%s Information", BrandingUtilities.APPLICATION_NAME);
    public static final String WELCOME_TEXT = "Welcome to Constellation";
    public static final double SPLIT_POS = 0.2;
    
    //Place holder images
    public static final String NEW_GRAPH = "resources/welcome_add_graph.png";
    public static final String NEW_SPHERE = "resources/welcome_add_box.png";
    public static final String OPEN = "resources/welcome_open_folder.png";
    public static final String IMPORT = "resources/welcome_import.png";
    public static final String GETTING_STARTED = "resources/welcome_getting_started.png";
    public static final String WHATS_NEW = "resources/welcome_new.png";
    public static final String FEEDBACK = "resources/welcome_feedback.png";
    public static final String JOIN = "resources/welcome_join.png";
    public static final String LOGO = "resources/constellation-logo.png";

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
            VBox left_vbox = new VBox();
            splitPane.getItems().add(left_vbox);
            
            HBox logo_hbox = new HBox();
            logo_hbox.setBackground(new Background(new BackgroundFill(Color.valueOf("white"), CornerRadii.EMPTY, Insets.EMPTY)));
            ImageView logoView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(LOGO)));
            logoView.setFitHeight(100);
            logoView.setFitWidth(150);
            logo_hbox.getChildren().add(logoView);
            logo_hbox.setAlignment(Pos.CENTER);
            left_vbox.getChildren().add(logo_hbox);
            
            //Create right VBox for graph controls
            VBox right_vbox = new VBox();
            right_vbox.setPadding(new Insets(50, 50, 50, 50));
            right_vbox.setBackground(new Background(new BackgroundFill(Color.valueOf("#14161a"), CornerRadii.EMPTY, Insets.EMPTY)));
            splitPane.getItems().add(right_vbox);  
            
            //Create HBoxes for the right_vbox
            HBox top_hbox = new HBox();
            HBox bottom_hbox = new HBox();
            
            //hbox formatting
            top_hbox.setPadding(new Insets(50, 0, 50, 0));
            top_hbox.setSpacing(10);
            bottom_hbox.setPadding(new Insets(50, 0, 50, 0));
            bottom_hbox.setSpacing(10);
            
            //Buttons for the top h box
            ImageView addView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(NEW_GRAPH)));
            addView.setFitHeight(75);
            addView.setFitWidth(75);
            ImageView newSphere = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(NEW_SPHERE)));
            newSphere.setFitHeight(75);
            newSphere.setFitWidth(75);
            ImageView openImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(OPEN)));
            openImage.setFitHeight(75);
            openImage.setFitWidth(75);
            ImageView importImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(IMPORT)));
            importImage.setFitHeight(75);
            importImage.setFitWidth(75);

            Button new_graph = new Button("New Graph\nAdd mode", addView);
            setButtonProps(new_graph);
            top_hbox.getChildren().add(new_graph);            
            Button new_sphere_graph = new Button("New Sphere Graph\nSphere network", newSphere);
            setButtonProps(new_sphere_graph);
            top_hbox.getChildren().add(new_sphere_graph);
            Button open_file = new Button("Open File\nFile Explorer", openImage);
            setButtonProps(open_file);
            top_hbox.getChildren().add(open_file);
            Button importButton = new Button("Import File\nDelimited File Importer", importImage);
            setButtonProps(importButton);
            top_hbox.getChildren().add(importButton);
            
            //formatting for bottom hbox
            Label recent = new Label("Recent");
            recent.setFont(new Font("Arial Unicode MS", 24));
            
            right_vbox.getChildren().add(top_hbox);
            right_vbox.getChildren().add(recent);
            right_vbox.getChildren().add(bottom_hbox);
             
            FlowPane flow = new FlowPane();
            flow.setPrefWrapLength(1000);
            flow.setHgap(20);
            flow.setVgap(20);
            
            //Create the buttons for the recent page
            //These have just been created to be able to view them on a page
            //and get the layout right
            Button recentBtn1 = new Button();
            createRecentButtons(recentBtn1);
            flow.getChildren().add(recentBtn1);
            Button recentBtn2 = new Button();
            createRecentButtons(recentBtn2);
            flow.getChildren().add(recentBtn2);
            Button recentBtn3 = new Button();
            createRecentButtons(recentBtn3);
            flow.getChildren().add(recentBtn3);
            Button recentBtn4 = new Button();
            createRecentButtons(recentBtn4);
            flow.getChildren().add(recentBtn4);
            Button recentBtn5 = new Button();
            createRecentButtons(recentBtn5);
            flow.getChildren().add(recentBtn5);
            Button recentBtn6 = new Button();
            createRecentButtons(recentBtn6);
            flow.getChildren().add(recentBtn6);
            Button recentBtn7 = new Button();
            createRecentButtons(recentBtn7);
            flow.getChildren().add(recentBtn7);
            Button recentBtn8 = new Button();
            createRecentButtons(recentBtn8);
            flow.getChildren().add(recentBtn8);
            Button recentBtn9 = new Button();
            createRecentButtons(recentBtn9);
            flow.getChildren().add(recentBtn9);
            Button recentBtn10 = new Button();
            createRecentButtons(recentBtn10);
            flow.getChildren().add(recentBtn10);
             
            bottom_hbox.getChildren().add(flow);
            splitPane.getDividers().get(0).setPosition(SPLIT_POS);
            VBox.setVgrow(right_vbox, Priority.ALWAYS);

            final WebView welcomeView = new WebView();
            VBox.setVgrow(welcomeView, Priority.ALWAYS);
            welcomeView.getEngine().getLoadWorker().stateProperty().addListener((final ObservableValue<? extends State> observable, final State oldValue, final State newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    // Add a click listener for <a> tags.
                    // If there's a valid href attribute, open the default browser at that URL.
                    // If there's a valid helpId attribute, open NetBeans help using the helpId.
                    // An <a> without an href doesn't get underlined, so use href="" in addition to helpId="...".
                    final EventListener listener = (final Event event) -> {
                        final String eventType = event.getType();
                        if (eventType.equals("click")) {
                            event.preventDefault();

                            final String href = ((Element) event.getTarget()).getAttribute("href");
                            if (href != null && !href.isEmpty()) {
                                Lookup.getDefault().lookupAll(WelcomePageProvider.class).forEach(plugin -> {
                                    if (plugin.getName().equals(href)) {
                                        plugin.run();
                                    }
                                });
                            } else {
                                final String helpId = ((Element) event.getTarget()).getAttribute("helpId");
                                if (helpId != null && !helpId.isEmpty()) {
                                    new HelpCtx(helpId).display();
                                }
                            }
                        }
                    };

                    final Document doc = welcomeView.getEngine().getDocument();
                    final NodeList nodeList = doc.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, true);
                    }
                }
            });
            welcomeView.getEngine().setUserStyleSheetLocation(WelcomeTopComponent.class.getResource(WELCOME_WEBVIEW).toExternalForm());
            welcomeView.getStyleClass().add("web-view");
            try {
                welcomeView.getEngine().loadContent(getWelcomeContent());
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            left_vbox.getChildren().add(welcomeView);

            //Finally, insert the root object into a scene, and insert the
            //scene into the JavaFX panel.
            final Scene scene = new Scene(root, Color.web("1d1d1d"));
            scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
            scene.getStylesheets().add(WelcomeTopComponent.class.getResource(WELCOME_THEME).toExternalForm());
            panel.setScene(scene);
        });
    }

    public void setButtonProps(Button button){
        button.setPrefSize(125, 125);
        button.setMaxSize(150, 150);
        button.setStyle("-fx-background-color: #2e4973;");
        button.setContentDisplay(ContentDisplay.TOP);
    }
    
    public void createRecentButtons(Button button){
        button.setPrefSize(160, 160);
        button.setMaxSize(175, 175);
        button.setStyle("-fx-background-color: #333333; -fx-background-radius: 10px;");
        button.setContentDisplay(ContentDisplay.TOP);
    }
    
    private String getWelcomeContent() throws ParseException {

        final StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html><html><body>\n");
        buf.append(String.format("<style>body{font-size:%spx;}</style>", FontUtilities.getOutputFontSize()));

        buf.append("<h2>Welcome to Constellation</h2><br>");
        buf.append("Constellation is a first class domain agnostic visualisation and data analysis ");
        buf.append("application enabling the user to solve large and complex problems ");
        buf.append("in a simple and intuitive way.<br><br>");
        buf.append("Constellation is a graph focused visualisation and data analysis ");
        buf.append("application enabling data access, federation and manipulation ");
        buf.append("activities across large and complex datasets.<br><br>");
        buf.append("Below are some options for getting started:<br><br>");
        
        Lookup.getDefault().lookupAll(WelcomePageProvider.class).forEach(plugin -> {
            if (plugin.isVisible()) {
                buf.append(plugin.getLink());
                buf.append("<br><br>");
            }
        });
        buf.append("</body></html>");

        return buf.toString();
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
