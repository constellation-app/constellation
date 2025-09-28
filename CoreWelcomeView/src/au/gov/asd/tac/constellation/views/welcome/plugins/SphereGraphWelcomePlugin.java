/*
 * Copyright 2010-2025 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.testing.CoreTestingPluginRegistry;
import au.gov.asd.tac.constellation.testing.construction.SphereGraphBuilderPlugin;
import au.gov.asd.tac.constellation.views.welcome.WelcomePluginInterface;
import au.gov.asd.tac.constellation.views.welcome.WelcomeTopComponent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
 * The New Graph with a Sphere plugin for the Welcome Page.
 *
 * @author canis_majoris
 */
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE, PluginTags.EXPERIMENTAL, PluginTags.WELCOME})
@NbBundle.Messages("SphereGraphWelcomePlugin=Sphere Graph Welcome Plugin")
public class SphereGraphWelcomePlugin implements WelcomePluginInterface {    

    private static final Logger LOGGER = Logger.getLogger(SphereGraphWelcomePlugin.class.getName());

    private static final String NEW_SPHERE = "resources/welcome_add_sphere.png";
    private final ImageView newSphere = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(NEW_SPHERE)));
    private final Button sphereGraphButton = new Button();

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return "Add Sphere Graph Welcome";
    }

    /**
     * This method describes what action should be taken when the link is
     * clicked on the Welcome Page
     */
    @Override
    public void run() {

        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph sg = new StoreGraph(schema);
        schema.newGraph(sg);
        final Graph dualGraph = new DualGraph(sg, false);

        final Future<?> f = PluginExecutor.startWith(CoreTestingPluginRegistry.SPHERE_GRAPH_BUILDER)
                .set(SphereGraphBuilderPlugin.ADD_CHARS_PARAMETER_ID, true)
                .set(SphereGraphBuilderPlugin.DRAW_MANY_DECORATORS_PARAMETER_ID, true)
                .set(SphereGraphBuilderPlugin.DRAW_MANY_TX_PARAMETER_ID, true)
                .set(SphereGraphBuilderPlugin.N_PARAMETER_ID, 100)
                .set(SphereGraphBuilderPlugin.OPTION_PARAMETER_ID, "Random vertices")
                .set(SphereGraphBuilderPlugin.T_PARAMETER_ID, 50)
                .set(SphereGraphBuilderPlugin.USE_ALL_DISPLAYABLE_CHARS_PARAMETER_ID, true)
                .set(SphereGraphBuilderPlugin.USE_LABELS_PARAMETER_ID, true)
                .set(SphereGraphBuilderPlugin.USE_RANDOM_ICONS_PARAMETER_ID, true)
                .executeWriteLater(dualGraph);

        try {
            // ensure sphere graph has finished before opening the graph
            f.get();
            final String graphName = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).getLabel().trim().toLowerCase();
            GraphOpener.getDefault().openGraph(dualGraph, graphName);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Sphere graph creation was interrupted", ex);
        } catch (final ExecutionException ex) {
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
        newSphere.setFitHeight(75);
        newSphere.setFitWidth(75);
        final Label sTitle = new Label("New Graph");
        final Label sSubtitle = new Label("Sphere network");
        sSubtitle.setId("smallInfoText");
        final VBox layoutVBox = new VBox(newSphere, sTitle, sSubtitle);
        layoutVBox.setAlignment(Pos.CENTER);
        sphereGraphButton.setGraphic(layoutVBox);
        return sphereGraphButton;
    }
}
