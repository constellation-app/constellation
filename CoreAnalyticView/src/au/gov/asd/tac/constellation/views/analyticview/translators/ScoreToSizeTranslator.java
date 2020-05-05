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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.graphics.BBoxf;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.SizeVisualisation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = GraphVisualisationTranslator.class)
public class ScoreToSizeTranslator extends AbstractSizeTranslator<ScoreResult, ElementScore> {

    @Override
    public String getName() {
        return "Multi-Score -> Size Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ScoreResult.class;
    }

    @Override
    public SizeVisualisation<ElementScore> buildControl() {
        return new SizeVisualisation<>(this);
    }

    @Override
    public void executePlugin(final boolean reset) {
        PluginExecution.withPlugin(new SizeElementsPlugin())
                .withParameter(SizeElementsPlugin.RESET_PARAMETER_ID, reset)
                .executeLater(GraphManager.getDefault().getActiveGraph());
    }

    private class SizeElementsPlugin extends SimpleEditPlugin {

        protected static final String RESET_PARAMETER_ID = "SizeElementsPlugin.reset";

        @Override
        public PluginParameters createParameters() {
            final PluginParameters parameters = new PluginParameters();

            final PluginParameter<BooleanParameterValue> resetParameter = BooleanParameterType.build(RESET_PARAMETER_ID);
            resetParameter.setBooleanValue(false);
            parameters.addParameter(resetParameter);

            return parameters;
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

            // get parameter values
            final boolean reset = parameters.getBooleanValue(RESET_PARAMETER_ID);

            // ensure attributes
            final int vertexSizeAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
            final int transactionSizeAttribute = VisualConcept.TransactionAttribute.WIDTH.ensure(graph);

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
                            graph.setFloatValue(vertexSizeAttribute, elementId, 1.0f);
                            break;
                        case TRANSACTION:
                            graph.setFloatValue(transactionSizeAttribute, elementId, 1.0f);
                            break;
                        default:
                            throw new InvalidElementTypeException("'Size Elements' is not supported "
                                    + "for the element type associated with this analytic question.");
                    }
                }
            } else {
                // estimate size of graph
                final BBoxf graphBoundingBox = BBoxf.getGraphBoundingBox(graph);
                float graphEstimatedDiameter = 0.0f;
                graphEstimatedDiameter = Math.max(graphBoundingBox.getMax()[BBoxf.X] - graphBoundingBox.getMin()[BBoxf.X],
                        graphBoundingBox.getMax()[BBoxf.Y] - graphBoundingBox.getMin()[BBoxf.Y]);
                graphEstimatedDiameter = Math.max(graphEstimatedDiameter,
                        graphBoundingBox.getMax()[BBoxf.Z] - graphBoundingBox.getMin()[BBoxf.Z]);

                // find highest and lowest mean scores among available analytic results
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

                // size graph elements based on their mean score normalised by the estimated diameter of the graph
                for (final ElementScore scoreResult : scoreResults.get()) {
                    final GraphElementType elementType = scoreResult.getElementType();
                    final int elementId = scoreResult.getElementId();
                    final float elementMeanScore = scoreResult.getNamedScores().values().stream()
                            .reduce((x, y) -> x + y).get() / scoreResult.getNamedScores().size();
                    final float sizeIntensity = (float) Math.log((double) (elementMeanScore * (graphEstimatedDiameter / meanScoreRange)));
                    switch (elementType) {
                        case VERTEX:
                            graph.setFloatValue(vertexSizeAttribute, elementId, sizeIntensity > 1.0f ? sizeIntensity : 1.0f);
                            break;
                        case TRANSACTION:
                            graph.setFloatValue(transactionSizeAttribute, elementId, sizeIntensity > 1.0f ? sizeIntensity : 1.0f);
                            break;
                        default:
                            throw new InvalidElementTypeException("'Size Elements' is not supported "
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
            return "Analytic View: Size Elements";
        }
    }
}
