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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.GraphFilePluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.openide.util.NbBundle;

/**
 * The Open Graph plugin for the Welcome Page.
 *
 * @author canis_majoris
 */
@PluginInfo(tags = {PluginTags.WELCOME})
@NbBundle.Messages("OpenGraphWelcomePlugin=Open Graph Welcome Plugin")
public class OpenGraphWelcomePlugin implements WelcomePluginInterface {
    
    private static final Logger LOGGER = Logger.getLogger(OpenGraphWelcomePlugin.class.getName());

    private static final String OPEN = "resources/welcome_open_folder.png";
    private final ImageView openImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(OPEN)));
    private final Button openFile = new Button();

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "Open Graph Welcome";
    }

    /**
     * This method describes what action should be taken when the link is
     * clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        final StoreGraph sg = new StoreGraph();
        try {
            PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE).executeNow(sg);
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            Thread.currentThread().interrupt();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

    }

    /**
     * Creates the button object to represent this plugin
     *
     * @return the button object
     */
    @Override
    public Button getButton() {
        openImage.setFitHeight(75);
        openImage.setFitWidth(75);
        final Label title = new Label("Open");
        final Label subtitle = new Label("File Explorer");
        subtitle.setId("smallInfoText");
        final VBox layoutVBox = new VBox(openImage, title, subtitle);
        layoutVBox.setAlignment(Pos.CENTER);
        openFile.setGraphic(layoutVBox);
        return openFile;
    }
}
