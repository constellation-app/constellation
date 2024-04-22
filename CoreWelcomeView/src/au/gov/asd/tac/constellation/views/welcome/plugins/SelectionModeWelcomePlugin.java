/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.openide.util.NbBundle;

/**
 * The New Graph in Selection Mode plugin for the Welcome Page.
 *
 * @author Delphinus8821
 */
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.WELCOME})
@NbBundle.Messages("SelectionModeWelcomePlugin=Selection Mode Welcome Plugin")
public class SelectionModeWelcomePlugin implements WelcomePluginInterface {

    private static final String NEW_GRAPH = "resources/welcome_add_selection.png";
    private final ImageView addView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(NEW_GRAPH)));
    private final Button newButton = new Button();

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "Selection Mode Graph Welcome";
    }

    /**
     * This method describes what action should be taken when the link is
     * clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph sg = new StoreGraph(schema);
        schema.newGraph(sg);
        final Graph dualGraph = new DualGraph(sg, false);

        final String graphName = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).getLabel().trim().toLowerCase();
        GraphOpener.getDefault().openGraph(dualGraph, graphName);

    }

    /**
     * Creates the button object to represent this plugin
     *
     * @return the button object
     */
    @Override
    public Button getButton() {
        addView.setFitHeight(75);
        addView.setFitWidth(75);
        final Label title = new Label("New Graph");
        final Label subtitle = new Label("Selection mode");
        subtitle.setId("smallInfoText");
        final VBox layoutVBox = new VBox(addView, title, subtitle);
        layoutVBox.setAlignment(Pos.CENTER);
        newButton.setGraphic(layoutVBox);
        return newButton;
    }
}
