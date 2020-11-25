/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.welcome.plugins;

import au.gov.asd.tac.constellation.functionality.welcome.WelcomePageProvider;
import au.gov.asd.tac.constellation.functionality.welcome.WelcomeTopComponent;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The plugin for the Welcome Page that leads to where the user can 
 * provide feedback for the Constellation app
 *
 * @author Delphinus8821
 */

@ServiceProvider(service = WelcomePageProvider.class, position = 7)
@PluginInfo(tags = {"WELCOME"})
@NbBundle.Messages("ProvideFeedbackWelcomePlugin=Provide Feedback Welcome Plugin")
public class ProvideFeedbackWelcomePlugin extends WelcomePageProvider {
    
    public static final String FEEDBACK = "resources/welcome_feedback.png";
    ImageView feedView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(FEEDBACK)));
    Button feedbackButton = new Button();
    
    private static final Logger LOGGER = Logger.getLogger(ProvideFeedbackWelcomePlugin.class.getName());
        
    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return ProvideFeedbackWelcomePlugin.class.getName();
    }
    
    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        String url = "https://github.com/constellation-app/constellation/issues/new";

        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        } else {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }
        }
    }

    /**
     * Determines whether this analytic appear on the Welcome Page 
     *
     * @return true is this analytic should be visible, false otherwise.
     */
    @Override
    public boolean isVisible() {
        return true;
    }
    
     /**
     * Creates the button object to represent this plugin
     * 
     * @return the button object
     */
    @Override
    public Button getButton(){
        feedView.setFitHeight(25);
        feedView.setFitWidth(25);
        Text title = new Text("Provide Feedback");
        title.setFill(Color.WHITE);
        title.setFont(new Font("Arial", 18));
        Text subtitle = new Text("Let us know your thoughts");
        subtitle.setFill(Color.WHITE);
        subtitle.setFont(new Font("Arial", 10));
        VBox layoutVBox = new VBox(title, subtitle);
        layoutVBox.setAlignment(Pos.CENTER_LEFT);
        HBox layoutHBox = new HBox(feedView, layoutVBox);
        layoutHBox.setSpacing(8);
        layoutHBox.setAlignment(Pos.CENTER_LEFT);
        feedbackButton.setGraphic(layoutHBox);
        return feedbackButton;
    }
}
