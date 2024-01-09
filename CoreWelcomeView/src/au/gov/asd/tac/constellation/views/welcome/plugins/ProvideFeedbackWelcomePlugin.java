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
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.openide.util.NbBundle;

/**
 * The plugin for the Welcome Page that leads to where the user can provide
 * feedback for the Constellation app
 *
 * @author Delphinus8821
 */
@PluginInfo(tags = {PluginTags.WELCOME})
@NbBundle.Messages("ProvideFeedbackWelcomePlugin=Provide Feedback Welcome Plugin")
public class ProvideFeedbackWelcomePlugin implements WelcomePluginInterface {

    private static final String FEEDBACK = "resources/welcome_feedback.png";
    private final ImageView feedView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(FEEDBACK)));
    private final Button feedbackButton = new Button();

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "Provide Feedback Welcome";
    }

    /**
     * This method describes what action should be taken when the link is
     * clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        final String url = "https://github.com/constellation-app/constellation/issues/new";

        PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
                .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Open " + getName())
                .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, url)
                .executeLater(null);
    }


    /**
     * Creates the button object to represent this plugin
     *
     * @return the button object
     */
    @Override
    public Button getButton() {
        feedView.setFitHeight(25);
        feedView.setFitWidth(25);
        final Label title = new Label("Provide Feedback");
        title.setId("label");
        final Label subtitle = new Label("Let us know your thoughts");
        subtitle.setId("infoText");
        final VBox layoutVBox = new VBox(title, subtitle);
        layoutVBox.setAlignment(Pos.CENTER_LEFT);
        final HBox layoutHBox = new HBox(feedView, layoutVBox);
        layoutHBox.setSpacing(8);
        layoutHBox.setAlignment(Pos.CENTER_LEFT);
        feedbackButton.setGraphic(layoutHBox);
        feedbackButton.setId("left-button");
        return feedbackButton;
    }
}
