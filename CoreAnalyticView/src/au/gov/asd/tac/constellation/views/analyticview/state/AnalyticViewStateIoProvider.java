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
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewController;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestion;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.EmptyResult;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.GraphVisualisation;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.InternalVisualisation;
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
import javafx.scene.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * IO provider for the AnalyticViewState object.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class AnalyticViewStateIoProvider extends AbstractGraphIOProvider {

    private static final Logger LOGGER = Logger.getLogger(AnalyticViewStateIoProvider.class.getName());
    
    private static final String PLUGIN_LISTS = "pluginLists";
    private static final String PLUGIN_LIST = "pluginList";
    private static final String INDEX = "index";
    private static final String QUESTIONS = "questions";
    private static final String NAME = "name";
    private static final String PARAMETERS = "parameters";
    private static final String RESULT = "result";
    private static final String RESULT_VISIBLE = "resultVisible";
    private static final String CATEGORY = "category";
    private static final String CATEGORIES_VISIBLE = "categoriesVisible";
    private static final String CURRENT_QUESTION = "currentQuestion";
    private static final String QUESTION = "question";
    private static final String GRAPH_VISUALISATIONS = "graphVisualisations";
    private static final String INTERNAL_VISUALISATIONS = "internalVisualisations";

    @Override
    public String getName() {
        return AnalyticViewConcept.MetaAttribute.ANALYTIC_VIEW_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods graph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap,
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final int currentIndex = jnode.get(INDEX).asInt();

            final List<AnalyticQuestionDescription<?>> questions = new ArrayList<>();
            final ArrayNode questionArray = (ArrayNode) jnode.withArray(QUESTIONS);
            for (int i = 0; i < questionArray.size(); i++) {
                if (questionArray.get(i).isNull()) {
                    questions.add(null);
                } else {
                    questions.add(AnalyticUtilities.lookupAnalyticQuestionDescription(questionArray.get(i).asText()));
                }
            }

            final List<List<SelectableAnalyticPlugin>> plugins = new ArrayList<>();
            if (jnode.has(PLUGIN_LISTS)) {
                final Iterator<JsonNode> pluginListIterator = jnode.get(PLUGIN_LISTS).iterator();
                while (pluginListIterator.hasNext()) {
                    final JsonNode pluginList = pluginListIterator.next();
                    final List<SelectableAnalyticPlugin> selectablePluginList = new ArrayList<>();
                    if (jnode.has(PLUGIN_LIST)) {
                        final Iterator<JsonNode> pluginIterator = pluginList.get(PLUGIN_LIST).iterator();
                        while (pluginIterator.hasNext()) {
                            final JsonNode pluginEntry = pluginIterator.next();
                            final SelectableAnalyticPlugin selectablePlugin = AnalyticConfigurationPane.lookupSelectablePlugin(pluginEntry.get(NAME).asText());
                            if (selectablePlugin != null) {
                                final Iterator<Map.Entry<String, JsonNode>> parametersIterator = pluginEntry.get(PARAMETERS).fields();
                                while (parametersIterator.hasNext()) {
                                    final Map.Entry<String, JsonNode> parametersEntry = parametersIterator.next();
                                    final String parameterName = parametersEntry.getKey();
                                    final String parameterValue = parametersEntry.getValue().asText();
                                    selectablePlugin.setUpdatedParameter(parameterName, parameterValue);
                                }

                                selectablePluginList.add(selectablePlugin);
                            }
                        }
                    }

                    plugins.add(selectablePluginList);
                }
            }
            
            final ObjectMapper mapper = new ObjectMapper();
            AnalyticResult<?> result = new EmptyResult();
            if (jnode.has(RESULT)) {
                result = mapper.readValue(jnode.get(RESULT).asText(), AnalyticResult.class);
            }
            
            boolean resultsVisible = false;
            if (jnode.has(RESULT_VISIBLE)) {
                resultsVisible = jnode.get(RESULT_VISIBLE).asBoolean();
            }
            
            boolean categoriesVisible = false;
            if (jnode.has(CATEGORIES_VISIBLE)) {
                categoriesVisible = jnode.get(CATEGORIES_VISIBLE).asBoolean();
            }
            
            AnalyticQuestionDescription<?> currentQuestion = null;
            if (jnode.has(CURRENT_QUESTION)) {
                currentQuestion = mapper.readValue(jnode.get(CURRENT_QUESTION).asText(), AnalyticQuestionDescription.class);
            }
            
            AnalyticQuestion question = null;
            if (jnode.has(QUESTION)) {
                question = mapper.readValue(jnode.get(QUESTION).asText(), AnalyticQuestion.class);
            }
            
            String activeCategory = "";
            if (jnode.has(CATEGORY)) {
                activeCategory = jnode.get(CATEGORY).asText();
            }

            final HashMap<GraphVisualisation, Boolean> graphVisualisations = new HashMap<>();
            if (jnode.has(GRAPH_VISUALISATIONS)) {
                final Iterator<JsonNode> visualisationsList = jnode.get(GRAPH_VISUALISATIONS).iterator();
                while (visualisationsList.hasNext()) {
                    final JsonNode visualisationNode = visualisationsList.next();
                    final GraphVisualisation visualisation = mapper.readValue(visualisationNode.textValue(), GraphVisualisation.class);
                    graphVisualisations.put(visualisation, visualisation.isActive());
                }
            }
            
            final HashMap<InternalVisualisation, Node> internalVisualisations = new HashMap<>();
            if (jnode.has(INTERNAL_VISUALISATIONS)) {
                final Iterator<JsonNode> visualisationsList = jnode.get(INTERNAL_VISUALISATIONS).iterator();
                while (visualisationsList.hasNext()) {
                    final JsonNode visualisationNode = visualisationsList.next();
                    final InternalVisualisation visualisation = mapper.readValue(visualisationNode.textValue(), InternalVisualisation.class);
                    internalVisualisations.put(visualisation, visualisation.getVisualisation());
                }
            }

            final AnalyticViewState state = new AnalyticViewState(currentIndex, questions, plugins, result, currentQuestion, question, activeCategory, resultsVisible, categoriesVisible, graphVisualisations, null);
            graph.setObjectValue(attributeId, elementId, state);
            AnalyticViewController.getDefault().readState();
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
                jsonGenerator.writeNumberField(INDEX, state.getCurrentAnalyticQuestionIndex());
                jsonGenerator.writeArrayFieldStart(QUESTIONS);
                for (final AnalyticQuestionDescription<?> question : state.getActiveAnalyticQuestions()) {
                    if (question == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeString(question.getClass().getName());
                    }
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeArrayFieldStart(PLUGIN_LISTS);
                for (final List<SelectableAnalyticPlugin> plugins : state.getActiveSelectablePlugins()) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeArrayFieldStart(PLUGIN_LIST);
                    for (final SelectableAnalyticPlugin selectablePlugin : plugins) {
                        jsonGenerator.writeStartObject();
                        jsonGenerator.writeStringField(NAME, selectablePlugin.getPlugin().getName());
                        jsonGenerator.writeObjectFieldStart(PARAMETERS);
                        for (final String parameterName : selectablePlugin.getAllParameters().getParameters().keySet()) {
                            jsonGenerator.writeStringField(parameterName, selectablePlugin.getAllParameters().getParameters().get(parameterName).getStringValue());
                        }
                        jsonGenerator.writeEndObject();
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndArray();
                    jsonGenerator.writeEndObject();
                }
                
                    // write new attributes to the state
                final ObjectMapper mapper = new ObjectMapper();

                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(RESULT, mapper.writeValueAsString(state.getResult()));
                jsonGenerator.writeStringField(CURRENT_QUESTION, mapper.writeValueAsString(state.getCurrentQuestion()));
                jsonGenerator.writeStringField(QUESTION, mapper.writeValueAsString(state.getQuestion()));
                jsonGenerator.writeStringField(CATEGORY, state.getActiveCategory());
                jsonGenerator.writeBooleanField(RESULT_VISIBLE, state.isResultsPaneVisible());
                jsonGenerator.writeBooleanField(CATEGORIES_VISIBLE, state.isCategoriesPaneVisible());

                jsonGenerator.writeEndObject();
                jsonGenerator.writeStartObject();
                jsonGenerator.writeArrayFieldStart(GRAPH_VISUALISATIONS);

                if (!state.getGraphVisualisations().isEmpty()) {
                    state.getGraphVisualisations().entrySet().forEach(node -> {
                        try {
                            jsonGenerator.writeString(mapper.writeValueAsString(node.getKey()));
                        } catch (final JsonProcessingException ex) {
                            LOGGER.log(Level.SEVERE, ex.getMessage());
                        } catch (final IOException ex) {
                            LOGGER.log(Level.SEVERE, ex.getMessage());
                        }
                    });
                }
               
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
                
                jsonGenerator.writeStartObject();
                jsonGenerator.writeArrayFieldStart(INTERNAL_VISUALISATIONS);
                
                if (!state.getInternalVisualisations().isEmpty()) {
                    state.getInternalVisualisations().entrySet().forEach(node -> {
                        try {
                            jsonGenerator.writeString(mapper.writeValueAsString(node.getKey()));
                        } catch (final JsonProcessingException ex) {
                            LOGGER.log(Level.SEVERE, ex.getMessage());
                        } catch (final IOException ex) {
                            LOGGER.log(Level.SEVERE, ex.getMessage());
                        }
                    });
                }

                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        }
    }
}