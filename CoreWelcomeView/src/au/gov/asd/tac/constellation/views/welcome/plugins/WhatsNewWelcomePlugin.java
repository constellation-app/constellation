/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import au.gov.asd.tac.constellation.views.whatsnew.WhatsNewTopComponent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * The plugin for the Welcome Page that leads to the Whats New in Constellation resources
 *
 * @author Delphinus8821
 */
@PluginInfo(tags = {PluginTags.WELCOME})
@NbBundle.Messages("WhatsNewWelcomePlugin=Whats New Welcome Plugin")
public class WhatsNewWelcomePlugin implements WelcomePluginInterface {

    private static final String WHATS_NEW = "resources/welcome_new.png";
    private final ImageView newView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(WHATS_NEW)));
    private final Button whatsNewBtn = new Button();

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "What's New Welcome";
    }

    /**
     * This method describes what action should be taken when the link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            final TopComponent whatsNew = WindowManager.getDefault().findTopComponent(WhatsNewTopComponent.class.getSimpleName());
            if (whatsNew != null) {
                if (!whatsNew.isOpened()) {
                    whatsNew.open();
                }
                whatsNew.setEnabled(true);
                whatsNew.requestActive();
            }
        });
    }

    /**
     * Creates the button object to represent this plugin
     *
     * @return the button object
     */
    @Override
    public Button getButton() {
        newView.setFitHeight(25);
        newView.setFitWidth(25);
        final Label title = new Label("What's New?");
        title.setId("label");
        final Label subtitle = new Label("Features in the latest version");
        subtitle.setId("infoText");
        final VBox layoutVBox = new VBox(title, subtitle);
        layoutVBox.setAlignment(Pos.CENTER_LEFT);
        final HBox layoutHBox = new HBox(newView, layoutVBox);
        layoutHBox.setSpacing(8);
        layoutHBox.setAlignment(Pos.CENTER_LEFT);
        whatsNewBtn.setGraphic(layoutHBox);
        whatsNewBtn.setId("left-button");
        return whatsNewBtn;
    }
}
