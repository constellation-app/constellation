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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticTranslatorUtilities;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.ColorVisualisation;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * Multi Score To Color Translator
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = GraphVisualisationTranslator.class)
public class ScoreToColorTranslator extends AbstractColorTranslator<ScoreResult, ElementScore> {

    // Maps of the colors of the vertices and transactions before the plugin is run
    private Map<Integer, ConstellationColor> vertexColors = new HashMap<>();
    private Map<Integer, ConstellationColor> transactionColors = new HashMap<>();

    @Override
    public String getName() {
        return "Multi-Score -> Color Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ScoreResult.class;
    }

    @Override
    public ColorVisualisation<ElementScore> buildControl() {
        return new ColorVisualisation<>(this);
    }

    @Override
    public void executePlugin(final boolean reset) {
        PluginExecution.withPlugin(new ColorElementsPlugin())
                .withParameter(ColorElementsPlugin.RESET_PARAMETER_ID, reset)
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }
    
    @Override
    public Map<Integer, ConstellationColor> getVertexColors() {
        return vertexColors;
    }

    @Override
    public void setVertexColors(final Map<Integer, ConstellationColor> colors) {
        vertexColors = colors;
    }

    @Override
    public Map<Integer, ConstellationColor> getTransactionColors() {
        return transactionColors;
    }

    @Override
    public void setTransactionColors(final Map<Integer, ConstellationColor> colors) {
        transactionColors = colors;
    }
    
    @PluginInfo(tags = {PluginTags.MODIFY})
    private class ColorElementsPlugin extends SimpleEditPlugin {

        protected static final String RESET_PARAMETER_ID = "ColorElementsPlugin.reset";

        @Override
        public PluginParameters createParameters() {
            final PluginParameters parameters = new PluginParameters();

            final PluginParameter<BooleanParameterValue> resetParameter = BooleanParameterType.build(RESET_PARAMETER_ID);
            resetParameter.setBooleanValue(false);
            parameters.addParameter(resetParameter);

            return parameters;
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            // get parameter values
            final boolean reset = parameters.getBooleanValue(RESET_PARAMETER_ID);
            
            final String currentGraphKey = GraphManager.getDefault().getActiveGraph().getId();

            // When a new instance of this class is created, it will not know if the current colors are at their original values
            // This means it won't have valid data to use for the reset function ... in a new instance (ie. a new "Run") it will be empty
            // Using a static cache gets around the issue. We can retrieve and initialise color data from the cache if available.
            
            if (AnalyticTranslatorUtilities.getVertexColorCache().containsKey(currentGraphKey)) {
                vertexColors = AnalyticTranslatorUtilities.getVertexColorCache().get(currentGraphKey);
            } else {
                vertexColors = new HashMap<>();
            }
            if (AnalyticTranslatorUtilities.getTransactionColorCache().containsKey(currentGraphKey)) {
                transactionColors = AnalyticTranslatorUtilities.getTransactionColorCache().get(currentGraphKey);
            } else {
                transactionColors = new HashMap<>();
            }

            // ensure attributes
            final int vertexOverlayColorAttribute = VisualConcept.VertexAttribute.OVERLAY_COLOR.ensure(graph);
            final int vertexColorReferenceAttribute = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(graph);
            final int transactionOverlayColorAttribute = VisualConcept.TransactionAttribute.OVERLAY_COLOR.ensure(graph);
            final int transactionColorReferenceAttribute = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(graph);

            if (result == null) {
                return;
            }

            final ScoreResult scoreResults = result;
            
            vertexColors.keySet().forEach(vertexKey -> 
                graph.setObjectValue(vertexOverlayColorAttribute, vertexKey, vertexColors.get(vertexKey)));
            
            transactionColors.keySet().forEach(transactionKey -> 
                graph.setObjectValue(transactionOverlayColorAttribute, transactionKey, transactionColors.get(transactionKey)));
            
            vertexColors.clear();
            transactionColors.clear();
            graph.setObjectValue(vertexColorReferenceAttribute, 0, null);
            graph.setObjectValue(transactionColorReferenceAttribute, 0, null);

            if (!reset) {
                // find highest and lowest mean scores among available analytic events
                float highestMeanScore = 0.0F;
                float lowestMeanScore = 0.0F;
                for (final ElementScore scoreResult : scoreResults.get()) {
                    final float elementMeanScore = scoreResult.getNamedScores().values().stream()
                            .reduce((x, y) -> x + y).get() / scoreResult.getNamedScores().size();
                    if (elementMeanScore > highestMeanScore) {
                        highestMeanScore = elementMeanScore;
                    }
                    if (elementMeanScore < lowestMeanScore) {
                        lowestMeanScore = elementMeanScore;
                    }
                }
                final float meanScoreRange = highestMeanScore - lowestMeanScore;

                // color graph elements based on their mean score normalised by the range of mean scores
                for (final ElementScore scoreResult : scoreResults.get()) {
                    final GraphElementType elementType = scoreResult.getElementType();
                    final int elementId = scoreResult.getElementId();
                    final float elementMeanScore = scoreResult.getNamedScores().values().stream()
                            .reduce((x, y) -> x + y).get() / scoreResult.getNamedScores().size();
                    final float colorIntensity = meanScoreRange != 0 ? (elementMeanScore + lowestMeanScore) / meanScoreRange : elementMeanScore + lowestMeanScore;
                    switch (elementType) {
                        case VERTEX:
                            final ConstellationColor vertexColor = graph.getObjectValue(vertexOverlayColorAttribute, elementId);
                            vertexColors.put(elementId, vertexColor);
                            graph.setObjectValue(vertexOverlayColorAttribute, elementId, ConstellationColor.getColorValue((float) 1.0 - colorIntensity, (float) 1.0 - colorIntensity, 1F, 1F));
                            break;
                        case TRANSACTION:
                            final ConstellationColor transactionColor = graph.getObjectValue(transactionOverlayColorAttribute, elementId);
                            transactionColors.put(elementId, transactionColor);
                            graph.setObjectValue(transactionOverlayColorAttribute, elementId, ConstellationColor.getColorValue((float) 1.0 - colorIntensity, (float) 1.0 - colorIntensity, 1F, 1F));
                            break;
                        default:
                            throw new InvalidElementTypeException("'Color Elements' is not supported "
                                    + "for the element type associated with this analytic question.");
                    }
                }
                graph.setObjectValue(vertexColorReferenceAttribute, 0, VisualConcept.VertexAttribute.OVERLAY_COLOR.getName());
                graph.setObjectValue(transactionColorReferenceAttribute, 0, VisualConcept.TransactionAttribute.OVERLAY_COLOR.getName());
            }
            
            AnalyticTranslatorUtilities.addToVertexColorCache(currentGraphKey, vertexColors);
            AnalyticTranslatorUtilities.addToTransactionColorCache(currentGraphKey, transactionColors);
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Analytic View: Color Elements";
        }
    }
}
