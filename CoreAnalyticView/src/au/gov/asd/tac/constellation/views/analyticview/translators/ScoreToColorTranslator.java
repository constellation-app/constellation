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
import au.gov.asd.tac.constellation.views.analyticview.visualisation.ColorVisualisation;
import org.openide.util.lookup.ServiceProvider;

/**
 * Multi Score To Color Translator
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = GraphVisualisationTranslator.class)
public class ScoreToColorTranslator extends AbstractColorTranslator<ScoreResult, ElementScore> {

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

            // ensure attributes
            final int vertexIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);
            final int vertexOverlayColorAttribute = VisualConcept.VertexAttribute.OVERLAY_COLOR.ensure(graph);
            final int vertexColorReferenceAttribute = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(graph);
            final int transactionOverlayColorAttribute = VisualConcept.TransactionAttribute.OVERLAY_COLOR.ensure(graph);
            final int transactionColorReferenceAttribute = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(graph);

            if (result == null) {
                return;
            }

            final ScoreResult scoreResults = result;

            if (reset) {
                for (final ElementScore scoreResult : scoreResults.get()) {
                    final GraphElementType elementType = scoreResult.getElementType();
                    final int elementId = scoreResult.getElementId();
                    switch (elementType) {
                        case VERTEX:
                            if (graph.getSchema() != null) {
                                graph.getSchema().completeVertex(graph, elementId);
                            }
                            break;
                        case TRANSACTION:
                            if (graph.getSchema() != null) {
                                graph.getSchema().completeTransaction(graph, elementId);
                            }
                            break;
                        default:
                            throw new InvalidElementTypeException("'Color Elements' is not supported "
                                    + "for the element type associated with this analytic question.");
                    }
                }
                graph.setObjectValue(vertexColorReferenceAttribute, 0, null);
                graph.setObjectValue(transactionColorReferenceAttribute, 0, null);
            } else {
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
                            graph.setObjectValue(vertexOverlayColorAttribute, elementId, ConstellationColor.getColorValue((float) 1.0 - colorIntensity, (float) 1.0 - colorIntensity, 1F, 1F));
                            graph.setObjectValue(vertexIconAttribute, elementId, "transparent");
                            break;
                        case TRANSACTION:
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
