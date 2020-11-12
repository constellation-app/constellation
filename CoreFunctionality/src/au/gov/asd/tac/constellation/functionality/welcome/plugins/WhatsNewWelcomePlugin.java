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

import au.gov.asd.tac.constellation.functionality.tutorial.TutorialTopComponent;
import au.gov.asd.tac.constellation.functionality.welcome.WelcomePageProvider;
import au.gov.asd.tac.constellation.functionality.welcome.WelcomeTopComponent;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * The plugin for the Welcome Page that leads to the Whats New in Constellation
 * resources
 *
 * @author Delphinus8821
 */

@ServiceProvider(service = WelcomePageProvider.class, position = 6)
@PluginInfo(tags = {"WELCOME"})
@NbBundle.Messages("WhatsNewWelcomePlugin=Whats New Welcome Plugin")
public class WhatsNewWelcomePlugin extends WelcomePageProvider {
    
    public static final String WHATS_NEW = "resources/welcome_new.png";
    ImageView newView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(WHATS_NEW)));
    Button whatsNewBtn = new Button("What's new?\nFeatures in the latest version", newView);
        
    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return WhatsNewWelcomePlugin.class.getName();
    }
    
    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            final TopComponent tutorial = WindowManager.getDefault().findTopComponent(TutorialTopComponent.class.getSimpleName());
            if (tutorial != null) {
                if (!tutorial.isOpened()) {
                    tutorial.open();
                }
                tutorial.setEnabled(true);
                tutorial.requestActive();
            }
        });
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
        newView.setFitHeight(25);
        newView.setFitWidth(25);
        return whatsNewBtn;
    }
}
