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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimpleAction;
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
import org.openide.awt.ActionReferences;
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
@ActionRegistration(displayName = "#CTL_ApplyColorblindAction",
        iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/colorblind/resources/colorblind.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 950, separatorBefore = 901)
})
@NbBundle.Messages({
    "CTL_ApplyColorblindAction=Apply Selected Colorblind Schema to Graph"
})
public final class ApplyColorblindAction extends SimpleAction {

    Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
    String COLORMODE = prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

    public ApplyColorblindAction(GraphNode context) {
        super(context);
    }

    @Override
    protected void edit(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        final WritableGraph wg = graph.getWritableGraph("Changing colors", true);
        run(wg);
        wg.commit();
    }

    public void run(final WritableGraph graph) {
        final int vxColorAttr = VisualConcept.VertexAttribute.COLOR.get(graph);
        final int txColorAttr = VisualConcept.TransactionAttribute.COLOR.get(graph);

        //Populate the nodes with the colorblind_layer attribute
        final int vxColorblindAttr = VisualConcept.VertexAttribute.COLORBLIND_LAYER.ensure(graph);
        final int txColorblindAttr = VisualConcept.TransactionAttribute.COLORBLIND_LAYER.ensure(graph);

        final int vertexCount = graph.getVertexCount();
        final int transactionCount = graph.getTransactionCount();

        if (!"None".equals(COLORMODE)) {
            //Iterate through graph vertices. If vertexType is a defined schemaType color will be adjusted if applicable.
            for (int vertex = 0; vertex < vertexCount; vertex++) {
                final int vxId = graph.getVertex(vertex);
                final ConstellationColor vertexColor = graph.getObjectValue(vxColorAttr, vxId);
                final ConstellationColor vxColorblindAlpha = graph.getObjectValue(vxColorblindAttr, vxId);

                if (vxColorblindAlpha == null || vxColorblindAlpha.getAlpha() == 0.99F) {
                    ConstellationColor newColor = calcColorBrightness(vertexColor);
                    graph.setObjectValue(vxColorblindAttr, vxId, newColor);
                }
            }

            for (int transaction = 0; transaction < transactionCount; transaction++) {
                final int transactionId = graph.getTransaction(transaction);
                final ConstellationColor transactionColor = graph.getObjectValue(txColorAttr, transactionId);
                final ConstellationColor txColorblindAlpha = graph.getObjectValue(vxColorblindAttr, transactionId);

                if (txColorblindAlpha == null || txColorblindAlpha.getAlpha() == 0.99F) {
                    ConstellationColor newColor = calcColorBrightness(transactionColor);
                    graph.setObjectValue(txColorblindAttr, transactionId, newColor);
                }
            }
            setColorRef(graph, graph.getAttributeName(vxColorblindAttr), graph.getAttributeName(txColorblindAttr));
        } else {
            graph.removeAttribute(vxColorblindAttr);
            graph.removeAttribute(txColorblindAttr);
        }
    }

    public static void setColorRef(final GraphWriteMethods wg, final String vxColorAttrName, final String txColorAttrName) {
        final int vxColorRef = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(wg);
        final int txColorRef = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(wg);

        wg.setStringValue(vxColorRef, 0, vxColorAttrName);
        wg.setStringValue(txColorRef, 0, txColorAttrName);
    }

    /*Adjust RGB values using the to-be removed RGB value as a proportion of the calculation, acting as contrast booster for brightness adjustments.  
        Evaluate the selected colorblind mode and adjust contrast if RGB value is high enough; prevents new color from being too dark, then remove imperceivable colors. 
        Primary colors for the modes are then adjusted at different strengths to improve contrast. I.E. remove 50% red in deut, remove 18% blue for prot.*/
    public ConstellationColor calcColorBrightness(ConstellationColor vertexColor) {
        float adjustedRed = vertexColor.getRed();
        float adjustedGreen = vertexColor.getGreen();
        float adjustedBlue = vertexColor.getBlue();
        final float minPrimaryRGBVal = 0.15f;
        final float minimumRGBVal = 0.25f;
        final float minimumAdjustedVal = 0.35f;
        final float minimumCombinedRGB = 0.70f;
        final float brightenRGB = 0.1f;

        switch (COLORMODE) {
            case "None":
                //do nothing
                break;
            case "Deuteranopia":
                //If the constellation color is primarily composed of a single rgb shade (e.g. Blue) do not adjust the value
                if (vertexColor.getRed() + vertexColor.getBlue() <= minimumCombinedRGB || vertexColor.getBlue() <= minPrimaryRGBVal) {
                    break;
                }

                if (vertexColor.getRed() >= minimumRGBVal) {
                    adjustedRed = vertexColor.getRed() * vertexColor.getGreen();
                    adjustedRed = adjustedRed <= minimumAdjustedVal ? adjustedRed + brightenRGB : adjustedRed;
                    adjustedBlue = vertexColor.getBlue() / 1.2f;
                }

                break;
            case "Protanopia":
                if (vertexColor.getGreen() + vertexColor.getBlue() < minimumCombinedRGB || vertexColor.getRed() <= minPrimaryRGBVal) {
                    break;
                }

                if (vertexColor.getGreen() >= minimumRGBVal) {
                    adjustedGreen = vertexColor.getGreen() * vertexColor.getRed();
                    adjustedGreen = adjustedGreen <= minimumAdjustedVal ? adjustedGreen + brightenRGB : adjustedGreen;
                    adjustedRed = vertexColor.getRed() / 1.8f;
                }
                break;
            case "Tritanopia":
                if (vertexColor.getBlue() + vertexColor.getRed() <= minimumCombinedRGB || vertexColor.getGreen() <= minPrimaryRGBVal) {
                    break;
                }

                if (vertexColor.getBlue() >= minimumRGBVal) {
                    adjustedBlue = vertexColor.getBlue() * vertexColor.getRed();
                    adjustedBlue = adjustedBlue <= minimumAdjustedVal ? adjustedBlue + brightenRGB : adjustedBlue;
                    adjustedGreen = vertexColor.getGreen() / 1.05f;
                }
                break;
            default:
                //do nothing
                break;
        }

        ConstellationColor newColor = ConstellationColor.getColorValue(adjustedRed, adjustedGreen, adjustedBlue, 0.99F);
        return newColor;
    }
}