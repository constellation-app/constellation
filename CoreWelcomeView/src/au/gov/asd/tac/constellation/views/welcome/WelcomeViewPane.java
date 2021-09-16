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

import au.gov.asd.tac.constellation.graph.file.open.RecentFilesWelcomePage;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot.RecentGraphScreenshotUtilities;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.security.ConstellationSecurityManager;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * WelcomeViewPane contains the content for WelcomeTopComponent
 *
 * @author Delphinus8821
 */
public class WelcomeViewPane extends BorderPane {

    private final BorderPane welcomeViewPane;

    public static final String ERROR_BUTTON_MESSAGE = String.format("%s Information", BrandingUtilities.APPLICATION_NAME);
    public static final String WELCOME_TEXT = "Welcome to Constellation";
    public static final double SPLIT_POS = 0.2;

    //Place holder images
    public static final String LOGO = "resources/constellation-logo.png";

    //Resized image height/width
    private static final int IMAGE_SIZE = 145;

    private final Logger LOGGER = Logger.getLogger(WelcomeViewPane.class.getName());

    private static final Button[] recentGraphButtons = new Button[10];

    public WelcomeViewPane() {
        welcomeViewPane = new BorderPane();
        ConstellationSecurityManager.startSecurityLaterFX(() -> {
            Platform.setImplicitExit(false);

            final SplitPane splitPane = new SplitPane();
            splitPane.setOrientation(Orientation.HORIZONTAL);
            splitPane.setStyle("-fx-background-color: transparent;");
            final ScrollPane scrollPane = new ScrollPane(splitPane);
            final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

            splitPane.setPrefHeight(visualBounds.getHeight());
            splitPane.setPrefWidth(visualBounds.getWidth());
            welcomeViewPane.setCenter(scrollPane);
            welcomeViewPane.autosize();

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
            final Label welcome = new Label(WELCOME_TEXT);
            welcome.setId("title");
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

            final WelcomePageLayoutProvider layout = Lookup.getDefault().lookup(WelcomePageLayoutProvider.class);

            //creating the button events along the top of the page
            final List<WelcomePluginInterface> topPlugins = layout.getTopPlugins();
            for (final WelcomePluginInterface plugin : topPlugins) {
                final Button currentButton = plugin.getButton();
                currentButton.setOnAction(e -> {
                    plugin.run();
                });
                setButtonProps(currentButton);
                topHBox.getChildren().add(currentButton);
            }

            //creating the button events on the side of the page
            final List<WelcomePluginInterface> sidePlugins = layout.getSidePlugins();
            for (final WelcomePluginInterface plugin : sidePlugins) {
                final Button currentButton = plugin.getButton();
                currentButton.setOnAction(e -> {
                    plugin.run();
                });
                setInfoButtons(currentButton);
                leftVBox.getChildren().add(currentButton);
            }

            leftVBox.setAlignment(Pos.TOP_CENTER);

            final HBox lowerLeftHBox = new HBox();
            lowerLeftHBox.setPadding(new Insets(30, 10, 10, 20));

            // Create a checkbox to change users preference regarding showing the Tutorial Page on startup
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final CheckBox showOnStartUpCheckBox = new CheckBox("Show on Startup");
            lowerLeftHBox.getChildren().add(showOnStartUpCheckBox);

            showOnStartUpCheckBox.selectedProperty().addListener((ov, oldVal, newVal) -> {
                prefs.putBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, newVal);
            });
            showOnStartUpCheckBox.setSelected(prefs.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, ApplicationPreferenceKeys.WELCOME_ON_STARTUP_DEFAULT));

            // Create a preferenceListener in order to identify when user preference is changed
            // Keeps tutorial page and options tutorial selections in-sync when both are open
            prefs.addPreferenceChangeListener(evt -> {
                showOnStartUpCheckBox.setSelected(prefs.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, showOnStartUpCheckBox.isSelected()));
            });

            leftVBox.getChildren().add(lowerLeftHBox);

            //formatting for bottom hbox
            final Label recent = new Label("Recent");
            recent.setId("title");
            rightVBox.getChildren().add(topHBox);
            rightVBox.getChildren().add(recent);
            rightVBox.getChildren().add(bottomHBox);

            final FlowPane flow = new FlowPane();
            flow.setPrefWrapLength(1000);
            flow.setHgap(20);
            flow.setVgap(20);

            //Create the buttons for the recent page
            final List<String> fileNames = RecentFilesWelcomePage.getFileNames();
            for (int i = 0; i < recentGraphButtons.length; i++) {
                recentGraphButtons[i] = new Button();
                //if the user has recent files get the names
                //and make them the text of the buttons
                createRecentButtons(recentGraphButtons[i]);
                if (i < fileNames.size()) {
                    recentGraphButtons[i].setText(fileNames.get(i));
                    final Tooltip toolTip = new Tooltip(recentGraphButtons[i].getText());
                    recentGraphButtons[i].setTooltip(toolTip);
                }
                final String text = recentGraphButtons[i].getText();

                final Rectangle2D value = new Rectangle2D(0, 0, IMAGE_SIZE, IMAGE_SIZE);
                final String screenshotFilename = RecentGraphScreenshotUtilities.getScreenshotsDir() + File.separator + text + ".png";
                final String screenshotFilenameResize = RecentGraphScreenshotUtilities.getScreenshotsDir() + File.separator + text + "Resize" + ".png";

                if (new File(screenshotFilename).exists()) {

                    if (!new File(screenshotFilenameResize).exists()) {
                        final Path source = Paths.get(screenshotFilename);
                        final Path target = Paths.get(screenshotFilenameResize);

                        try (final InputStream is = new FileInputStream(source.toFile())) {
                            resize(is, target, IMAGE_SIZE, IMAGE_SIZE);

                        } catch (final IOException ex) {
                            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        }
                    }
                    final ImageView imageView = new ImageView(new Image("File:/" + screenshotFilenameResize));
                    imageView.setViewport(value);
                    imageView.setFitHeight(IMAGE_SIZE);
                    imageView.setFitWidth(IMAGE_SIZE);
                    recentGraphButtons[i].setGraphic(imageView);

                } else if (i < fileNames.size()) {
                    final ImageView defaultImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream("resources/Constellation_Application_Icon_Small.png")));
                    final Rectangle2D valueDefault = new Rectangle2D(0, 0, IMAGE_SIZE, IMAGE_SIZE);
                    defaultImage.setViewport(valueDefault);
                    defaultImage.setFitHeight(IMAGE_SIZE);
                    defaultImage.setFitWidth(IMAGE_SIZE);
                    recentGraphButtons[i].setGraphic(defaultImage);
                }

                //Calls the method for the recent graphs to open
                // on the button action
                recentGraphButtons[i].setOnAction(e -> {
                    RecentFilesWelcomePage.openGraph(text);
                });
                flow.getChildren().add(recentGraphButtons[i]);
            }
            bottomHBox.getChildren().add(flow);
            splitPane.getDividers().get(0).setPosition(SPLIT_POS);
            VBox.setVgrow(rightVBox, Priority.ALWAYS);
            this.setCenter(welcomeViewPane);
            scrollPane.setVvalue(-1);
        });
    }

    /**
     * Referenced from https://mkyong.com/java/how-to-resize-an-image-in-java/
     *
     * @param is the input stream
     * @param target the file path were we want to store the resized image
     * @param height the new height of the resized image
     * @param width the new width of the resized image
     * @throws IOException
     */
    public void resize(final InputStream is, final Path target, final int height, final int width) throws IOException {
        final BufferedImage originalImage = ImageIO.read(is);

        // create a new BufferedImage for drawing
        final BufferedImage newResizedImage
                = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = newResizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.fillRect(0, 0, width, height);

        final Map<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.addRenderingHints(hints);

        // puts the original image into the newResizedImage
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        // get file extension
        final String s = target.getFileName().toString();
        final String fileExtension = s.substring(s.lastIndexOf(".") + 1);

        // we want image in png format
        ImageIO.write(newResizedImage, fileExtension, target.toFile());
    }

    public void setButtonProps(final Button button) {
        button.setPrefSize(135, 135);
        button.setMaxSize(150, 150);
        button.setStyle("-fx-background-color: #2e4973;");
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
    }

    public void createRecentButtons(final Button button) {
        button.setPrefSize(160, 160);
        button.setMaxSize(175, 175);
        button.setStyle("-fx-background-color: #333333; -fx-background-radius: 10px; -fx-text-fill: white;");
        button.setCursor(Cursor.HAND);
        button.setContentDisplay(ContentDisplay.TOP);
    }

    public void setInfoButtons(final Button button) {
        button.setPrefSize(310, 45);
        button.setMaxSize(310, 50);
        button.setStyle("-fx-background-color: transparent;");
        button.setCursor(Cursor.HAND);
        button.setAlignment(Pos.CENTER_LEFT);
    }
}
