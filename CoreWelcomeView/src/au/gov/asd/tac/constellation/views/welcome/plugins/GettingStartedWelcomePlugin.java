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
package au.gov.asd.tac.constellation.views.welcome.plugins;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
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

/**
 * The plugin for the Welcome Page that leads to the Getting Started guides and resources
 *
 * @author Delphinus8821
 */

@PluginInfo(tags = {"WELCOME"})
@NbBundle.Messages("GettingStartedWelcomePlugin=Getting Started Welcome Plugin")
public class GettingStartedWelcomePlugin implements WelcomePluginInterface {
    
    public static final String GETTING_STARTED = "resources/welcome_getting_started.png";
    final ImageView started = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(GETTING_STARTED)));
    final Button startedBtn = new Button();
    
        
    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "Getting Started Welcome";
    }
    
    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        String url = "https://constellation.readthedocs.io/en/latest/";

        PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
            .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Open " + getName())
            .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, url)
            .executeLater(null);
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
        started.setFitHeight(25);
        started.setFitWidth(25);
        final Text title = new Text("Getting Started");
        title.setFont(new Font("Arial", 18));
        title.setFill(Color.WHITE);
        final Text subtitle = new Text("Guides & Resources");
        subtitle.setFont(new Font("Arial", 10));
        subtitle.setFill(Color.WHITE);
        final VBox layoutVBox = new VBox(title, subtitle);
        layoutVBox.setAlignment(Pos.CENTER_LEFT);
        final HBox layoutHBox = new HBox(started, layoutVBox);
        layoutHBox.setSpacing(8);
        layoutHBox.setAlignment(Pos.CENTER_LEFT);
        startedBtn.setGraphic(layoutHBox);
        return startedBtn;
    }
}
