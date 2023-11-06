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
package au.gov.asd.tac.constellation.views.welcome;

import au.gov.asd.tac.constellation.graph.file.open.OpenFile;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles.HistoryItem;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot.RecentGraphScreenshotUtilities;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot.RecentGraphScreenshotUtilities.IMAGE_SIZE;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
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
import javafx.stage.Screen;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * WelcomeViewPane contains the content for WelcomeTopComponent
 *
 * @author Delphinus8821
 */
public class WelcomeViewPane extends BorderPane {

    private static final Preferences PREFERENCES = NbPreferences.forModule(ApplicationPreferenceKeys.class);

    private final BorderPane pane;

    public static final String ERROR_BUTTON_MESSAGE = String.format("%s Information", BrandingUtilities.APPLICATION_NAME);
    public static final String WELCOME_TEXT = String.format("Welcome to %s", BrandingUtilities.APPLICATION_NAME);
    public static final double SPLIT_POS = 0.2;

    // place holder image
    public static final String LOGO = "resources/constellation_logo.png";
    private static final Image PLACEHOLDER_IMAGE = new Image(WelcomeTopComponent.class.getResourceAsStream("resources/placeholder_icon.png"));

    private static final Button[] recentGraphButtons = new Button[10];

    public WelcomeViewPane() {
        pane = new BorderPane();
        initContent();
    }

    /**
     * Create the content for the welcome view pane
     */
    private void initContent() {
        ConstellationSecurityManager.startSecurityLaterFX(() -> {
            Platform.setImplicitExit(false);

            final SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(Orientation.HORIZONTAL);
            splitPane.setStyle("-fx-background-color: transparent;");
            final ScrollPane scrollPane = new ScrollPane(splitPane);
            final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

            splitPane.setPrefHeight(visualBounds.getHeight());
            splitPane.setPrefWidth(visualBounds.getWidth());
            pane.setCenter(scrollPane);
            pane.autosize();

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
            logoHBox.setPadding(new Insets(0, 0, 3, 0));
            logoHBox.setAlignment(Pos.CENTER);
            leftVBox.getChildren().add(logoHBox);

            //Create the labels for the left pane
            final Label welcome = new Label(WELCOME_TEXT);
            welcome.setId("title");
            welcome.setAlignment(Pos.CENTER);
            leftVBox.getChildren().add(welcome);
            leftVBox.setId("left-pane");
            
            //Create right VBox for graph controls
            final VBox rightVBox = new VBox();
            rightVBox.setPadding(new Insets(50, 50, 50, 50));
            splitPane.getItems().add(rightVBox);

            //Create HBoxes for the right_vbox
            final HBox topHBox = new HBox();
            final HBox bottomHBox = new HBox();

            //hbox formatting
            topHBox.setPadding(new Insets(50, 0, 50, 0));
            topHBox.setSpacing(10);
            bottomHBox.setPadding(new Insets(50, 0, 50, 0));
            bottomHBox.setSpacing(10);

            final WelcomePageLayoutProvider layout = Lookup.getDefault().lookup(WelcomePageLayoutProvider.class);

            //creating the button events along the top of the page
            final List<WelcomePluginInterface> topPlugins = layout.getTopPlugins();
            for (final WelcomePluginInterface plugin : topPlugins) {
                final Button currentButton = plugin.getButton();
                currentButton.setOnAction(e -> plugin.run());
                setButtonProps(currentButton);
                topHBox.getChildren().add(currentButton);
            }

            //creating the button events on the side of the page
            final List<WelcomePluginInterface> sidePlugins = layout.getSidePlugins();
            for (final WelcomePluginInterface plugin : sidePlugins) {
                final Button currentButton = plugin.getButton();
                currentButton.setOnAction(e -> plugin.run());
                setInfoButtons(currentButton);
                leftVBox.getChildren().add(currentButton);
            }

            leftVBox.setAlignment(Pos.TOP_CENTER);

            final HBox lowerLeftHBox = new HBox();
            lowerLeftHBox.setPadding(new Insets(30, 10, 10, 20));

            // Create a checkbox to change users preference regarding showing the Tutorial Page on startup
            final CheckBox showOnStartUpCheckBox = new CheckBox("Show on Startup");
            lowerLeftHBox.getChildren().add(showOnStartUpCheckBox);

            showOnStartUpCheckBox.selectedProperty().addListener((ov, oldVal, newVal)
                    -> PREFERENCES.putBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, newVal));
            showOnStartUpCheckBox.setSelected(PREFERENCES.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, ApplicationPreferenceKeys.WELCOME_ON_STARTUP_DEFAULT));

            // Create a preferenceListener in order to identify when user preference is changed
            // Keeps tutorial page and options tutorial selections in-sync when both are open
            PREFERENCES.addPreferenceChangeListener(evt
                    -> showOnStartUpCheckBox.setSelected(PREFERENCES.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, showOnStartUpCheckBox.isSelected()))
            );

            leftVBox.getChildren().add(lowerLeftHBox);

            //formatting for bottom hbox
            final Label recent = new Label("Recent");
            recent.setId("title");
            rightVBox.getChildren().add(topHBox);
            rightVBox.getChildren().add(recent);
            rightVBox.getChildren().add(bottomHBox);

            // add the recent graphs section 
            final FlowPane flow = recentGraphsSetup();
            bottomHBox.getChildren().add(flow);
            splitPane.getDividers().get(0).setPosition(SPLIT_POS);
            VBox.setVgrow(rightVBox, Priority.ALWAYS);
            this.setCenter(pane);
            scrollPane.setVvalue(-1);
        });
    }

    /**
     * Setup the content for the recent graphs part of the welcome page
     * 
     * @return flowpane
     */
    private FlowPane recentGraphsSetup() {
        final FlowPane flow = new FlowPane();
        flow.setPrefWrapLength(1000);
        flow.setHgap(20);
        flow.setVgap(20);

        //Create the buttons for the recent page
        final List<HistoryItem> fileDetails = RecentFiles.getUniqueRecentFiles();
        for (int i = 0; i < recentGraphButtons.length; i++) {
            recentGraphButtons[i] = new Button();
            //if the user has recent files get the names
            //and make them the text of the buttons
            createRecentButtons(recentGraphButtons[i]);
            if (i < fileDetails.size()) {
                recentGraphButtons[i].setText(fileDetails.get(i).getFileName());
                final Tooltip toolTip = new Tooltip(fileDetails.get(i).getPath());
                recentGraphButtons[i].setTooltip(toolTip);

                final Optional<File> screenshotFile = RecentGraphScreenshotUtilities.findScreenshot(fileDetails.get(i).getPath(), fileDetails.get(i).getFileName());
                if (screenshotFile.isPresent()) {
                    recentGraphButtons[i].setGraphic(buildGraphic(
                            new Image("file:///" + screenshotFile.get().getAbsolutePath())
                    ));
                } else if (i < fileDetails.size()) {
                    recentGraphButtons[i].setGraphic(buildGraphic(PLACEHOLDER_IMAGE));
                }

                //Calls the method for the recent graphs to open
                //on the button action
                final String path = fileDetails.get(i).getPath();
                recentGraphButtons[i].setOnAction(e -> {
                    OpenFile.open(RecentFiles.convertPath2File(path), -1);
                    saveCurrentDirectory(path);
                });
            }
            flow.getChildren().add(recentGraphButtons[i]);
        }
        return flow;
    }

    /**
     * Build an {@code ImageView} from the {@code Image} provided. The
     * dimensions of the image are determined by {@code IMAGE_SIZE}
     *
     * @param image The image to present
     * @return An {@code ImageView} containing the {@code Image} provided
     */
    private static ImageView buildGraphic(final Image image) {
        final ImageView defaultImage = new ImageView(image);
        final Rectangle2D valueDefault = new Rectangle2D(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        defaultImage.setViewport(valueDefault);
        defaultImage.setFitHeight(IMAGE_SIZE);
        defaultImage.setFitWidth(IMAGE_SIZE);
        return defaultImage;
    }

    /**
     * Add a new top menu button and set its properties
     *
     * @param button
     */
    public void setButtonProps(final Button button) {
        button.setPrefSize(135, 135);
        button.setMaxSize(150, 150);
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
    }

    /**
     * Add a new recent graph button and set its properties
     *
     * @param button
     */
    public void createRecentButtons(final Button button) {
        button.setPrefSize(160, 160);
        button.setMaxSize(175, 175);
        button.setId("recent-button");
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
    }

    /**
     * Add a new left menu button and set its properties
     *
     * @param button
     */
    public void setInfoButtons(final Button button) {
        button.setPrefSize(310, 45);
        button.setMaxSize(310, 50);
        button.setStyle("-fx-background-color: transparent;");
        button.setCursor(Cursor.HAND);
        button.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * If the remember open and save location preference is enabled, this saves
     * the current directory as that location
     *
     * @param path the new location to open from when opening or saving a graph
     */
    private static void saveCurrentDirectory(final String path) {
        final String lastFileOpenAndSaveLocation = PREFERENCES.get(ApplicationPreferenceKeys.FILE_OPEN_AND_SAVE_LOCATION, "");
        final boolean rememberOpenAndSaveLocation = PREFERENCES.getBoolean(ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION, ApplicationPreferenceKeys.REMEMBER_OPEN_AND_SAVE_LOCATION_DEFAULT);

        if (!lastFileOpenAndSaveLocation.equals(path) && rememberOpenAndSaveLocation) {
            PREFERENCES.put(ApplicationPreferenceKeys.FILE_OPEN_AND_SAVE_LOCATION, path);
        }
    }
}
