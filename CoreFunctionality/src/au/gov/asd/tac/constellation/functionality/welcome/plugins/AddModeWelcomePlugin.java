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
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * The New Graph in Add Mode plugin for the Welcome Page.
 *
 * @author canis_majoris
 */

@ServiceProvider(service = WelcomePageProvider.class, position = 1)
@PluginInfo(tags = {"WELCOME"})
@NbBundle.Messages("AddModeWelcomePlugin=Add Mode Welcome Plugin")
public class AddModeWelcomePlugin extends WelcomePageProvider {
    
    public static final String NEW_GRAPH = "resources/welcome_add_graph.png";
    ImageView addView = new ImageView(new Image(WelcomeTopComponent.class.getResourceAsStream(NEW_GRAPH)));
    Button new_graph = new Button("New Graph\nAdd mode", addView);
        
    /**
     * Get a unique reference that is used to identify the plugin 
     *
     * @return a unique reference
     */
    @Override
    public String getName() {
        return AddModeWelcomePlugin.class.getName();
    }
    
    /**
     * Get a description for the link that will appear on the Welcome Page 
     *
     * @return a unique reference
     */
    @Override
    public String getLinkDescription() {
        return "Open up a new graph in Add Mode";
    }
    
    /**
     * Get an optional textual description that appears on the Welcome Page.
     *
     * @return a unique reference
     */
    @Override
    public String getDescription() {
        StringBuilder buf = new StringBuilder();
        buf.append("<br>");
        buf.append("Add mode allows you to draw your own graph by clicking on ");
        buf.append("the background.<br>");
        buf.append("By connecting on nodes, you can connect them with links.<br>");
        buf.append("The side menu contains options for toggling whether the links ");
        buf.append("are directed or undirected.<br>");
        buf.append("When finished drawing, you can toggle the graph back into Selection ");
        buf.append("Mode by clicking the button on the side menu, changing the attribute ");
        buf.append("in the Attribute Editor, or by toggling Draw Mode in the Edit Menu.");
        return buf.toString();
    }
    
    /**
     * Returns a link to a resource that can be used instead of text.
     *
     * @return a unique reference
     */
    @Override
    public String getImage() {
        return null;
    }
    /**
     * This method describes what action should be taken when the 
     * link is clicked on the Welcome Page
     *
     */
    @Override
    public void run() {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph sg = new StoreGraph(schema);
        schema.newGraph(sg);
        int drawModeAttribute = VisualConcept.GraphAttribute.DRAWING_MODE.ensure(sg);
        sg.setBooleanValue(drawModeAttribute, 0, true);
        final Graph dualGraph = new DualGraph(sg, false);

        final String graphName = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).getLabel().replace(" ", "").toLowerCase();
        GraphOpener.getDefault().openGraph(dualGraph, graphName);

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
        addView.setFitHeight(75);
        addView.setFitWidth(75);
        return new_graph;
    }
}
