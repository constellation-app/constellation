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
package au.gov.asd.tac.constellation.views.analyticview.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticConfigurationPane.SelectableAnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * IO provider for the AnalyticViewState object.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class AnalyticViewStateIoProvider extends AbstractGraphIOProvider {

    private static final Logger LOGGER = Logger.getLogger(AnalyticViewStateIoProvider.class.getName());

    @Override
    public String getName() {
        return AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap,
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final int currentIndex = jnode.get("index").asInt();

            final List<AnalyticQuestionDescription<?>> questions = new ArrayList<>();
            final ArrayNode questionArray = (ArrayNode) jnode.withArray("questions");
            for (int i = 0; i < questionArray.size(); i++) {
                if (questionArray.get(i).isNull()) {
                    questions.add(null);
                } else {
                    questions.add(AnalyticUtilities.lookupAnalyticQuestionDescription(questionArray.get(i).asText()));
                }
            }

            final List<List<SelectableAnalyticPlugin>> plugins = new ArrayList<>();
            final Iterator<JsonNode> pluginListIterator = jnode.get("pluginLists").iterator();
            while (pluginListIterator.hasNext()) {
                final JsonNode pluginList = pluginListIterator.next();
                final List<SelectableAnalyticPlugin> selectablePluginList = new ArrayList<>();
                final Iterator<JsonNode> pluginIterator = pluginList.get("pluginList").iterator();
                while (pluginIterator.hasNext()) {
                    final JsonNode pluginEntry = pluginIterator.next();
                    final SelectableAnalyticPlugin selectablePlugin = AnalyticConfigurationPane.lookupSelectablePlugin(pluginEntry.get("name").asText());
                    if (selectablePlugin != null) {
                        final Iterator<Map.Entry<String, JsonNode>> parametersIterator = pluginEntry.get("parameters").fields();
                        while (parametersIterator.hasNext()) {
                            final Map.Entry<String, JsonNode> parametersEntry = parametersIterator.next();
                            final String parameterName = parametersEntry.getKey();
                            final String parameterValue = parametersEntry.getValue().asText();
                            selectablePlugin.setUpdatedParameter(parameterName, parameterValue);
                        }

                        selectablePluginList.add(selectablePlugin);
                    }
                }

                plugins.add(selectablePluginList);
            }

            final ObjectMapper mapper = new ObjectMapper();
            final AnalyticResult<?> result = mapper.readValue(jnode.get("result").asText(), AnalyticResult.class);
            final boolean resultsVisible = jnode.get("resultVisible").asBoolean();
            final boolean categoriesVisible = jnode.get("categoriesVisible").asBoolean();
            final AnalyticQuestionDescription<?> currentQuestion = mapper.readValue(jnode.get("currentQuestion").asText(), AnalyticQuestionDescription.class);
            final AnalyticQuestion question = mapper.readValue(jnode.get("question").asText(), AnalyticQuestion.class);
            final String activeCategory = jnode.get("category").asText();

            final HashMap<GraphVisualisation, Boolean> visualisations = new HashMap<>();
            final Iterator<JsonNode> visualisationsList = jnode.get("visualisations").iterator();
            while (visualisationsList.hasNext()) {
                final JsonNode visualisationNode = visualisationsList.next();
                final GraphVisualisation visualisation = mapper.readValue(visualisationNode.textValue(), GraphVisualisation.class);
                visualisations.put(visualisation, visualisation.isActive());
            }

            final AnalyticViewState state = new AnalyticViewState(currentIndex, questions, plugins, result, currentQuestion, question, activeCategory, resultsVisible, categoriesVisible, visualisations);
            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods graph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final AnalyticViewState originalState = graph.getObjectValue(attribute.getId(), elementId);
            if (originalState == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                // Make a copy in case the state on the graph is currently being modified.
                final AnalyticViewState state = new AnalyticViewState(originalState);
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeNumberField("index", state.getCurrentAnalyticQuestionIndex());
                jsonGenerator.writeArrayFieldStart("questions");
                for (final AnalyticQuestionDescription<?> question : state.getActiveAnalyticQuestions()) {
                    if (question == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeString(question.getClass().getName());
                    }
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeArrayFieldStart("pluginLists");
                for (final List<SelectableAnalyticPlugin> plugins : state.getActiveSelectablePlugins()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeArrayFieldStart("pluginList");
                    for (final SelectableAnalyticPlugin selectablePlugin : plugins) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField("name", selectablePlugin.getPlugin().getName());
                        jsonGenerator.writeObjectFieldStart("parameters");
                        for (final String parameterName : selectablePlugin.getAllParameters().getParameters().keySet()) {
                            jsonGenerator.writeStringField(parameterName, selectablePlugin.getAllParameters().getParameters().get(parameterName).getStringValue());
                        }
                        jsonGenerator.writeEndObject();
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndArray();
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();

                // write new attributes to the state
                final ObjectMapper mapper = new ObjectMapper();

                jsonGenerator.writeStringField("result", mapper.writeValueAsString(state.getResult()));
                jsonGenerator.writeStringField("currentQuestion", mapper.writeValueAsString(state.getCurrentQuestion()));
                jsonGenerator.writeStringField("question", mapper.writeValueAsString(state.getQuestion()));
                jsonGenerator.writeStringField("category", state.getActiveCategory());
                jsonGenerator.writeBooleanField("resultsVisible", state.isResultsPaneVisible());
                jsonGenerator.writeBooleanField("categoriesVisible", state.isCategoriesPaneVisible());

                jsonGenerator.writeArrayFieldStart("visualisations");
                jsonGenerator.writeStartObject();

                state.getVisualisations().entrySet().forEach(node -> {
                    try {
                        jsonGenerator.writeString(mapper.writeValueAsString(node.getKey()));
                    } catch (final JsonProcessingException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage());
                    } catch (final IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getMessage());
                    }
                });

                jsonGenerator.writeEndObject();
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }
}