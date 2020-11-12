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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.functionality.welcome.WelcomePageProvider;
import au.gov.asd.tac.constellation.functionality.welcome.WelcomeTopComponent;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.DelimitedImportTopComponent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * The Open Delimited File plugin for the Welcome Page.
 *
 * @author canis_majoris
 */
@ServiceProvider(service = WelcomePageProvider.class, position = 4)
@PluginInfo(tags = {"WELCOME"})
@NbBundle.Messages("DelimitedFileWelcomePlugin=Delimited File Welcome Plugin")
public class DelimitedFileWelcomePlugin extends WelcomePageProvider {
    
    public static final String IMPORT = "resources/welcome_import.png";
    ImageView importImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(IMPORT)));
    Button importButton = new Button("Import File\nDelimited File Importer", importImage);

    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return DelimitedFileWelcomePlugin.class.getName();
    }
    
    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            final TopComponent stage = WindowManager.getDefault().findTopComponent(DelimitedImportTopComponent.class.getSimpleName());
            if (stage != null) {
                if (!stage.isOpened()) {
                    stage.open();
                }
                stage.setEnabled(true);
                stage.requestActive();
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
        importImage.setFitHeight(75);
        importImage.setFitWidth(75);
        return importButton;
    }
}
