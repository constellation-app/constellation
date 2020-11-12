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
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.GraphFilePluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The Open Graph plugin for the Welcome Page.
 *
 * @author canis_majoris
 */

@ServiceProvider(service = WelcomePageProvider.class, position = 3)
@PluginInfo(tags = {"WELCOME"})
@NbBundle.Messages("OpenGraphWelcomePlugin=Open Graph Welcome Plugin")
public class OpenGraphWelcomePlugin extends WelcomePageProvider {
    
    public static final String OPEN = "resources/welcome_open_folder.png";
    ImageView openImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(OPEN)));
    Button openFile = new Button("Open File\nFile Explorer", openImage);
    
    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return OpenGraphWelcomePlugin.class.getName();
    }
    
    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        final StoreGraph sg = new StoreGraph();
        try {
            PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE).executeNow(sg);
        } catch (InterruptedException | PluginException ex) {
            Exceptions.printStackTrace(ex);
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
        openImage.setFitHeight(75);
        openImage.setFitWidth(75);
        return openFile;
    }
}
