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
package au.gov.asd.tac.constellation.welcome;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.functionality.intro.Intro;
import au.gov.asd.tac.constellation.functionality.intro.IntroProvider;
import au.gov.asd.tac.constellation.functionality.whatsnew.WhatsNewProvider;
import au.gov.asd.tac.constellation.functionality.whatsnew.WhatsNewProvider.WhatsNewEntry;
import au.gov.asd.tac.constellation.importexport.delimited.DelimitedFileImporterStage;
import au.gov.asd.tac.constellation.importexport.delimited.ImportDelimitedFileAction;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.security.proxy.ProxyUtilities;
import au.gov.asd.tac.constellation.visual.fonts.FontUtilities;
import java.awt.BorderLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
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
import au.gov.asd.tac.constellation.importexport.delimited.ImportDelimitedFileAction;
/**
 *
 * WelcomeTopComponent is designed to inform users of news about Constellation.
 *
 * @author aquila
 */
@ConvertAsProperties(
        dtd = "-//au.gov.asd.tac.constellation.visual.tutorial//Tutorial//EN",
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
        id = "au.gov.asd.tac.constellation.functionality.tutorial.WelcomeTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 1)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_WelcomeTopComponentAction",
        preferredID = "WelcomeTopComponent"
)
@Messages({
    "CTL_WelcomeTopComponentAction=Welcome",
    "CTL_WelcomeTopComponentTopComponent=Welcome",
    "HINT_WelcomeTopComponentTopComponent=Welcome"
})
public final class WelcomelTopComponent extends TopComponent {

    private JFXPanel panel = new JFXPanel();
    private static final String TUTORIAL_THEME = "resources/tutorialStylesheet.css";
    private static final String WEBENGINE_CSS_INJECTION = "resources/webengine.css";
    public static final String MOUSE_IMAGE = "/au/gov/asd/tac/constellation/functionality/tutorial/mouse3.png";
    public static final String MENU_IMAGE = "/au/gov/asd/tac/constellation/functionality/tutorial/sidebar.png";
    public static final String ERROR_BUTTON_MESSAGE = "CONSTELLATION Information";
    public static final String WELCOME_TEXT = "Welcome to Constellation";
    public static final double SPLIT_POS = 0.2;

    /**
     * Required date format
     */
    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * The number of days an entry is considered recent
     */
    public static final int RECENT_DAYS = -30; // 1 month

    /**
     * The number of days before an entry is archived and no longer shown
     */
    public static final int ARCHIVE_DAYS = -180; // 6 months

    public WelcomelTopComponent() {
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

            //Create left VBox to handle "help" images of menu and mouse
            VBox left_vbox = new VBox(10);
            splitPane.getItems().add(left_vbox);
            left_vbox.setPadding(new Insets(10, 10, 10, 10));
            left_vbox.setAlignment(Pos.TOP_CENTER);

            //Create images for Left VBox
            ImageView menu_image = new ImageView(new Image(WelcomelTopComponent.class.getResourceAsStream(MENU_IMAGE)));
            menu_image.setFitWidth(300);
            menu_image.setPreserveRatio(true);
            left_vbox.getChildren().add(menu_image);
            ImageView mouse_image = new ImageView(new Image(WelcomelTopComponent.class.getResourceAsStream(MOUSE_IMAGE)));
            mouse_image.setFitWidth(300);
            mouse_image.setPreserveRatio(true);
            left_vbox.getChildren().add(mouse_image);

            //Create Right VBox to handle Browser and controls,
            //or error messages
            VBox right_vbox = new VBox();
            splitPane.getItems().add(right_vbox);

            splitPane.getDividers().get(0).setPosition(SPLIT_POS);
//            HBox.setHgrow(right_vbox, Priority.ALWAYS);
//            VBox.setVgrow(right_vbox, Priority.ALWAYS);

//            final WebView whatsNewView = new WebView();
//            VBox.setVgrow(whatsNewView, Priority.ALWAYS);
//            whatsNewView.getEngine().getLoadWorker().stateProperty().addListener((final ObservableValue<? extends State> observable, final State oldValue, final State newValue) -> {
//                if (newValue == Worker.State.SUCCEEDED) {
//                    // Add a click listener for <a> tags.
//                    // If there's a valid href attribute, open the default browser at that URL.
//                    // If there's a valid helpId attribute, open NetBeans help using the helpId.
//                    // An <a> without an href doesn't get underlined, so use href="" in addition to helpId="...".
//                    final EventListener listener = (final Event event) -> {
//                        final String eventType = event.getType();
//                        if (eventType.equals("click")) {
//                            event.preventDefault();
//
//                            final String href = ((Element) event.getTarget()).getAttribute("href");
//                            if (href != null && !href.isEmpty()) {
//                                PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
//                                        .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Open Tutorial Link")
//                                        .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, href)
//                                        .executeLater(null);
//                            } else {
//                                final String helpId = ((Element) event.getTarget()).getAttribute("helpId");
//                                if (helpId != null && !helpId.isEmpty()) {
//                                    new HelpCtx(helpId).display();
//                                } else {
//                                    final String actionId = ((Element) event.getTarget()).getAttribute("actionId");
//                                    if (actionId != null && !actionId.isEmpty()) {
//                                        System.out.println(actionId);
//                                        //TODO: Invoke Action here
//                                        //Action action=org.openide.awt.Actions.forID("File", "au.gov.asd.tac.constellation.importexport.delimited.ImportDelimitedFileAction");
//                                        //action.actionPerformed(new ActionEvent(panel,ActionEvent.ACTION_PERFORMED,"link_clicked"));
//                                        switch (actionId) {
//                                            case "actionid1":
////                                                Platform.runLater(() -> {
////                                                    final DelimitedFileImporterStage stage = new DelimitedFileImporterStage();
////                                                    stage.show();
////                                                });
//                                                ImportDelimitedFileAction.openDelimitedFileImporterStage();
//                                                break;
//                                            default:
//                                                throw new AssertionError();
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    };
//
//                    final Document doc = whatsNewView.getEngine().getDocument();
//                    final NodeList nodeList = doc.getElementsByTagName("a");
//                    for (int i = 0; i < nodeList.getLength(); i++) {
//                        ((EventTarget) nodeList.item(i)).addEventListener("click", listener, true);
//                    }
//                }
//            });
//            whatsNewView.getEngine().setUserStyleSheetLocation(WelcomelTopComponent.class.getResource("resources/whatsnew.css").toExternalForm());
//            whatsNewView.getStyleClass().add("web-view");
//            try {
//                //whatsNewView.getEngine().loadContent(getWhatsNew());
//                whatsNewView.getEngine().loadContent(getIntro());
//            } catch (Exception ex) {
//                Exceptions.printStackTrace(ex);
//            }
//            right_vbox.getChildren().add(whatsNewView);
            //Creating text objects  
            Text text1 = new Text("Welcome to Constellation ");

            //Setting font to the text 
            text1.setFont(new Font(15));

            //Setting color to the text  
            text1.setFill(Color.WHITE);

            Text text2 = new Text("How to start building graphs in CONSTELLATION.  ");

            //Setting font to the text 
            text2.setFont(new Font(15));

            //Setting color to the text 
            text2.setFill(Color.WHITE);
            Text text3 = new Text("\n Select File &rarr; New Graph &rarr; Analytic Graph. This will open a new window containing an empty graph. ");

            //Setting font to the text 
            text3.setFont(new Font(15));

            //Setting color to the text 
            text3.setFill(Color.WHITE);

            Text text4 = new Text("Select Experimental &rarr; Build graph &rarr; Sphere Graph Builder to build a random spherical graph in the window.");

            //Setting font to the text 
            text4.setFont(new Font(15));
            text4.setFill(Color.WHITE);

            //Creating the text flow plane 
            TextFlow textFlowPane = new TextFlow();

            //Setting the line spacing between the text objects 
            textFlowPane.setTextAlignment(TextAlignment.JUSTIFY);

            //Setting the width  
            textFlowPane.setPrefSize(600, 300);

            //Setting the line spacing  
            textFlowPane.setLineSpacing(5.0);

            //Retrieving the observable list of the TextFlow Pane 
            ObservableList list = textFlowPane.getChildren();

            //Adding cylinder to the pane  
            list.addAll(text1, text2, text3, text4);
            Button button1 = new Button("Button Number 1");
            button1.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent t) {
                    System.out.println(" button pressed");
                    ImportDelimitedFileAction.openDelimitedFileImporterStage();
                }
                
            });
            right_vbox.getChildren().add(textFlowPane);
            right_vbox.getChildren().add(button1);
            //Finally, insert the root object into a scene, and insert the
            //scene into the JavaFX panel.
            final Scene scene = new Scene(root, Color.web("1d1d1d"));
            scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
            scene.getStylesheets().add(WelcomelTopComponent.class.getResource(TUTORIAL_THEME).toExternalForm());
            panel.setScene(scene);
        }
        );
    }

    private String getIntro() {
        final StringBuilder buf = new StringBuilder();
        Intro intro = new Intro();
        buf
                .append(IntroProvider.getHtmlContent(IntroProvider.class,
                        intro.getResource()));
        return buf.toString();

    }

    private String getWhatsNew() throws ParseException {
        Collection<? extends WhatsNewProvider> whatsNew = Lookup.getDefault().lookupAll(WhatsNewProvider.class
        );
        final ArrayList<WhatsNewEntry> wnList = new ArrayList<>();
        whatsNew.stream().forEach(wnp -> {
            wnList.addAll(WhatsNewProvider.getWhatsNew(wnp.getClass(), wnp.getResource(), wnp.getSection()));
        });

        Collections.sort(wnList);

        final Date now = new Date();
        final Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DATE, RECENT_DAYS);
        final Date twoWeeksOld = cal.getTime();

        cal.setTime(now);
        cal.add(Calendar.DATE, ARCHIVE_DAYS);
        final Date archiveLimit = cal.getTime();

        boolean headerDone = false;
        final StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html><html><body>\n");
        buf.append(String.format("<style>body{font-size:%spx;}</style>", FontUtilities.getOutputFontSize()));

        for (final WhatsNewEntry wne : wnList) {
            // Use a far-future date to indicate an undated fixed position at the top.
            final String dt;
            if (wne.date.compareTo("3000") == -1) {
                if (!headerDone) {
                    buf.append("<hr>\n<h2>What's New</h2>\n");
                    headerDone = true;
                }

                final Date whatsNewDate = DATE_FORMATTER.parse(wne.date);
                if (whatsNewDate.after(twoWeeksOld) && whatsNewDate.before(now)) {
                    // display date, header, recent badge and section.
                    dt = String.format("%s <strong>%s</strong> <span class=\"badge badge-recent\">Recent</span> <span class=\"section badge badge-section\">%s</span>", wne.date, wne.header, wne.section);
                } else if (whatsNewDate.before(archiveLimit)) {
                    dt = null;
                } else {
                    // The "normal" case: display date, header and section.
                    dt = String.format("%s <strong>%s</strong> <span class=\"section badge badge-section\">%s</span>", wne.date, wne.header, wne.section);
                }

            } else {
                // Display a pegged entry without the date.
                dt = String.format("<strong>%s</strong> <span class=\"section badge badge-section\">%s</span>", wne.header, wne.section);
            }

            if (dt != null) {
                buf.append(String.format("<dl><dt>%s</dt>\n<dd>", dt));
                buf.append(wne.text);
                buf.append("</dd></dl>\n");
            }
        }

        buf.append("</body></html>");

        // validate the html
//        try {
//            File createTempFile = File.createTempFile("whatsnew", ".html");
//            FileWriter fileWriter = new FileWriter(createTempFile);
//            fileWriter.write(buf.toString());
//            fileWriter.close();
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
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
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(WelcomelTopComponent.class, "WelcomelTopComponent.jLabel1.text")); // NOI18N
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
