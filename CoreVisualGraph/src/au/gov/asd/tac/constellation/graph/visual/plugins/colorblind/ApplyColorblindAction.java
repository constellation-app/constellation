/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.plugins.colorblind;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimpleAction;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.prefs.Preferences;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author centauri0320001
 */

/*
 * toolbar options containing set of colourblind actions
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.graph.visual.plugins.colorblind.ApplyColorblindAction")
@ActionRegistration(displayName = "#CTL_ApplyColorblindAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/colorblind/resources/colorblind.png", surviveFocusChange = true)
@ActionReference(path = "Menu/Display", position = 950, separatorBefore = 901)
@NbBundle.Messages("CTL_ApplyColorblindAction=Apply Selected Colorblind Schema to Graph")
public final class ApplyColorblindAction extends SimpleAction {

    private static Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    //Retrieve colorblind mode selection preference 
    public static String colorMode = prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

    public ApplyColorblindAction(GraphNode context) {
        super(context);
    }

    @Override
    protected void edit(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = graphs.getGraph();
        final WritableGraph wg = graph.getWritableGraph("Changing colors", true);
        try {
            run(wg);
        } finally {
            wg.commit();
        }
    }

    public void run(final WritableGraph graph) {
        final int colorAttr = VisualConcept.VertexAttribute.COLOR.get(graph);
        prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        colorMode = prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

        // Color the taxonomies so we can see what's going on.
        final int typeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);

        if (typeAttributeId != Graph.NOT_FOUND) {

            final int vertexCount = graph.getVertexCount();
            for (int vertex = 0; vertex < vertexCount; vertex++) {

                final int vxId = graph.getVertex(vertex);
                final SchemaVertexType vertexType = graph.getObjectValue(typeAttributeId, vxId);

                if (typeAttributeId != Graph.NOT_FOUND) {
                    ConstellationColor caseColor;

                    switch (vertexType.getName()) {
                        case "Telephone Identifier":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.EMERALD : ConstellationColor.BUTTERMILK);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        case "Machine Identifier":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.CHOCOLATE : ConstellationColor.BLUSH);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        case "Document":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.BANANA : ConstellationColor.DARK_PURPLE);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        case "Event":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.PEACH : ConstellationColor.BROWN);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        case "Placeholder":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.TEAL : ConstellationColor.LIME);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        case "Email":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.MUSK : ConstellationColor.RED);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        //Below are types which inherit the same colors from the schema
                        case "Online Identifier":
                        case "User Name":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.AZURE : ConstellationColor.MIDNIGHT);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        case "Location":
                        case "Country":
                        case "Geohash":
                        case "MGRS":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.CARROT : ConstellationColor.BLUE);
                            graph.setObjectValue(colorAttr, vxId, caseColor);
                            break;
                        default:
                            //do nothing, color doesn't require updating
                            break;
                    }
                }
            }
        }

        // Color the taxonomies so we can see what's going on.
        final int transactionTypeId = AnalyticConcept.TransactionAttribute.TYPE.get(graph);
        final int transactionColorAttr = VisualConcept.TransactionAttribute.COLOR.get(graph);

        if (transactionTypeId != Graph.NOT_FOUND) {
            final int transactionCount = graph.getTransactionCount();
            for (int transaction = 0; transaction < transactionCount; transaction++) {

                final int transactionId = graph.getTransaction(transaction);
                final SchemaTransactionType transactionType = graph.getObjectValue(transactionTypeId, transactionId);

                if (transactionTypeId != Graph.NOT_FOUND) {
                    ConstellationColor caseColor;
                    switch (transactionType.getName()) {
                        case "Location":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.CARROT : ConstellationColor.BLUE);
                            graph.setObjectValue(transactionColorAttr, transactionId, caseColor);
                            break;
                        case "Created":
                        case "Referenced":
                            caseColor = (colorMode.equals(VisualSchemaFactory.NONE) ? ConstellationColor.CHOCOLATE : ConstellationColor.BROWN);
                            graph.setObjectValue(transactionColorAttr, transactionId, caseColor);
                            break;
                        default:
                            //do nothing, color doesn't require updating
                            break;
                    }
                }
            }
        }
    }
}
