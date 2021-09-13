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

import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * The Open Data Access View for the Welcome Page.
 *
 * @author Atlas139mkm
 */
@PluginInfo(pluginType = PluginType.CREATE, tags = {"WELCOME"})
@NbBundle.Messages("DataAcessViewWelcomePluging=Data Acess View Welcome Plugin")
public class DataAccessViewWelcomePlugin implements WelcomePluginInterface {

    public static final String IMPORT = "resources/welcome_data_access.png";
    final ImageView importImage = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(IMPORT)));
    final Button importButton = new Button();

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "Open Data Access";
    }

    /**
     * This method describes what action should be taken when the link is
     * clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            final TopComponent stage = WindowManager.getDefault().findTopComponent(DataAccessViewTopComponent.class.getSimpleName());
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
     * @return true if this analytic should be visible, false otherwise.
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
    public Button getButton() {
        importImage.setFitHeight(75);
        importImage.setFitWidth(75);
        final Label imTitle = new Label("Open");
        final Label imSubtitle = new Label("Data Access View");
        imSubtitle.setId("smallInfoText");
        final VBox layoutVBox = new VBox(importImage, imTitle, imSubtitle);
        layoutVBox.setAlignment(Pos.CENTER);
        importButton.setGraphic(layoutVBox);
        return importButton;
    }

}
