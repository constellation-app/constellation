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
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.HideVisualisation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = GraphVisualisationTranslator.class)
public class ScoreToHideTranslator extends AbstractHideTranslator<ScoreResult, ElementScore> {

    @Override
    public String getName() {
        return "Multi-Score -> Hide Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ScoreResult.class;
    }

    @Override
    public HideVisualisation<ElementScore> buildControl() {
        return new HideVisualisation<>(this);
    }

    @Override
    public void executePlugin(final boolean reset, final float threshold) {
        PluginExecution.withPlugin(new HideElementsPlugin())
                .withParameter(HideElementsPlugin.RESET_PARAMETER_ID, reset)
                .withParameter(HideElementsPlugin.THRESHOLD_PARAMETER_ID, threshold)
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }

    @PluginInfo(tags = {PluginTags.MODIFY})
    private class HideElementsPlugin extends SimpleEditPlugin {

        protected static final String RESET_PARAMETER_ID = "HideElementsPlugin.reset";
        protected static final String THRESHOLD_PARAMETER_ID = "HideElementsPlugin.threshold";

        @Override
        public PluginParameters createParameters() {
            final PluginParameters parameters = new PluginParameters();

            final PluginParameter<BooleanParameterValue> resetParameter = BooleanParameterType.build(RESET_PARAMETER_ID);
            resetParameter.setBooleanValue(false);
            parameters.addParameter(resetParameter);

            final PluginParameter<FloatParameterValue> thresholdParameter = FloatParameterType.build(THRESHOLD_PARAMETER_ID);
            thresholdParameter.setFloatValue(0.0f);
            parameters.addParameter(thresholdParameter);

            return parameters;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            // get parameter values
            final boolean reset = parameters.getBooleanValue(RESET_PARAMETER_ID);
            final float threshold = parameters.getFloatValue(THRESHOLD_PARAMETER_ID);

            // ensure attributes
            final int vertexVisibilityAttribute = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
            final int transactionVisibilityAttribute = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);

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
                            graph.setFloatValue(vertexVisibilityAttribute, elementId, 1.0f);
                            break;
                        case TRANSACTION:
                            graph.setFloatValue(transactionVisibilityAttribute, elementId, 1.0f);
                            break;
                        default:
                            throw new InvalidElementTypeException("'Hide Elements' is not supported "
                                    + "for the element type associated with this analytic question.");
                    }
                }
            } else {
                // find highest and lowest mean scores among available analytic events
                float highestMeanScore = 0.0f;
                float lowestMeanScore = 0.0f;
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

                // hide graph elements where their mean score is less than the normalised threshold score
                final float normalisedThreshold = (threshold * meanScoreRange) + lowestMeanScore;
                for (final ElementScore scoreResult : scoreResults.get()) {
                    final GraphElementType elementType = scoreResult.getElementType();
                    final int elementId = scoreResult.getElementId();
                    final float elementMeanScore = scoreResult.getNamedScores().values().stream()
                            .reduce((x, y) -> x + y).get() / scoreResult.getNamedScores().size();
                    switch (elementType) {
                        case VERTEX:
                            if (elementMeanScore >= normalisedThreshold) {
                                graph.setFloatValue(vertexVisibilityAttribute, elementId, 1.0f);
                            } else {
                                graph.setFloatValue(vertexVisibilityAttribute, elementId, -1.0f);
                            }
                            break;
                        case TRANSACTION:
                            if (elementMeanScore >= normalisedThreshold) {
                                graph.setFloatValue(transactionVisibilityAttribute, elementId, 1.0f);
                            } else {
                                graph.setFloatValue(transactionVisibilityAttribute, elementId, -1.0f);
                            }
                            break;
                        default:
                            throw new InvalidElementTypeException("'Hide Elements' is not supported "
                                    + "for the element type associated with this analytic question.");
                    }
                }
            }
        }

        @Override
        protected boolean isSignificant() {
            return false;
        }

        @Override
        public String getName() {
            return "Analytic View: Hide Elements";
        }
    }
}
