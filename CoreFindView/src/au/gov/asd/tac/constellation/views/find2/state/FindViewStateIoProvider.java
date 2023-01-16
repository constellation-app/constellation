/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find2.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResult;
import au.gov.asd.tac.constellation.views.find2.utilities.FindResultsList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Atlas139mkm
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class FindViewStateIoProvider extends AbstractGraphIOProvider {

    @Override
    public String getName() {
        return FindViewConcept.MetaAttribute.FINDVIEW_STATE.getName();
    }

    @Override
    public void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods writableGraph, final Map<Integer, Integer> vertexMap,
            final Map<Integer, Integer> transactionMap, final GraphByteReader byteReader,
            final ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {

            // Get the findResultsList variables
            final int currentIndex = jnode.get("currentIndex").asInt();

            // Get the basic find replace parameters
            final String findString = jnode.get("findString").asText();
            final String replaceString = jnode.get("replaceString").asText();
            final String graphElementString = jnode.get("graphElement").asText();
            final GraphElementType graphElement = GraphElementType.getValue(graphElementString);
            final Boolean standardText = jnode.get("standardText").asBoolean();
            final Boolean regEx = jnode.get("regEx").asBoolean();
            final Boolean ignoreCase = jnode.get("ignoreCase").asBoolean();
            final Boolean exactMatch = jnode.get("exactMatch").asBoolean();
            final Boolean findInSelection = jnode.get("findInSelection").asBoolean();
            final Boolean addToSelection = jnode.get("addToSelection").asBoolean();
            final Boolean removeFromSelection = jnode.get("removeFromSelection").asBoolean();
            final Boolean replaceInSelected = jnode.get("replaceInSelected").asBoolean();
            final Boolean searchAllGraphs = jnode.get("searchAllGraphs").asBoolean();

            // Get the selected attributes
            final List<Attribute> selectedAttributes = new ArrayList<>();
            final ArrayNode selectedAttributesArray = (ArrayNode) jnode.withArray("selectedAttributes");

            for (int i = 0; i < selectedAttributesArray.size(); i++) {
                if (selectedAttributesArray.get(i).isNull()) {
                    selectedAttributes.add(null);
                } else {
                    int attributeInt = writableGraph.getAttribute(graphElement, selectedAttributesArray.get(i).asText());
                    // Only add the attribute to the selected attributes list if it exists in the graph
                    if (attributeInt >= 0) {
                        selectedAttributes.add(new GraphAttribute(writableGraph, attributeInt));
                    }
                }
            }

            // Create the basic find replace parameter object with the variables
            final BasicFindReplaceParameters parameters = new BasicFindReplaceParameters(findString, replaceString, graphElement,
                    selectedAttributes, standardText, regEx, ignoreCase, exactMatch, findInSelection, addToSelection, removeFromSelection, replaceInSelected, searchAllGraphs);

            // Get the find results
            final List<FindResult> findResults = new ArrayList<>();
            final ArrayNode findResultsArray = (ArrayNode) jnode.withArray("findResults");
            for (int i = 0; i < findResultsArray.size(); i = i + 3) {
                findResults.add(findResultsArray.get(i).isNull() ? null : new FindResult(findResultsArray.get(i).asInt(),
                        findResultsArray.get(i + 1).asInt(), GraphElementType.getValue(findResultsArray.get(i + 2).asText()), findResultsArray.get(i + 3).asText()));
            }

            // Create the findResultList object
            final FindResultsList findResultList = new FindResultsList(currentIndex, parameters);

            // Add the find results to the findResultsList
            findResultList.addAll(findResults);

            writableGraph.setObjectValue(attributeId, elementId, findResultList);
        }
    }

    @Override
    public void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods graph, final GraphByteWriter byteWriter,
            boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {

            final FindResultsList originalResultsList = graph.getObjectValue(attribute.getId(), elementId);
            if (originalResultsList == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                final FindResultsList resultsList = new FindResultsList(originalResultsList);

                // Store the current index and the graph ID
                jsonGenerator.writeObjectFieldStart(attribute.getName());
                jsonGenerator.writeNumberField("currentIndex", resultsList.getCurrentIndex());

                // Stores a list of the Find Results, ID, UID and Graph element type label
                jsonGenerator.writeArrayFieldStart("findResults");
                for (final FindResult fr : resultsList) {
                    jsonGenerator.writeNumber(fr.getID());
                    jsonGenerator.writeNumber(fr.getUID());
                    jsonGenerator.writeString(fr.getType().getShortLabel());
                }
                jsonGenerator.writeEndArray();

                // Store all the basic find replace parameters
                final BasicFindReplaceParameters parameters = resultsList.getSearchParameters();
                jsonGenerator.writeStringField("findString", parameters.getFindString());
                jsonGenerator.writeStringField("replaceString", parameters.getReplaceString());
                jsonGenerator.writeStringField("graphElement", parameters.getGraphElement().getShortLabel());
                jsonGenerator.writeBooleanField("standardText", parameters.isStandardText());
                jsonGenerator.writeBooleanField("regEx", parameters.isRegEx());
                jsonGenerator.writeBooleanField("ignoreCase", parameters.isIgnoreCase());
                jsonGenerator.writeBooleanField("exactMatch", parameters.isExactMatch());
                jsonGenerator.writeBooleanField("searchAllGraphs", parameters.isSearchAllGraphs());
                jsonGenerator.writeBooleanField("addToSelection", parameters.isAddTo());
                jsonGenerator.writeBooleanField("findInSelection", parameters.isFindIn());
                jsonGenerator.writeBooleanField("removeFromSelection", parameters.isRemoveFrom());
                jsonGenerator.writeBooleanField("replaceInSelected", parameters.isReplaceIn());

                // Store all the selected attributes of the search
                jsonGenerator.writeArrayFieldStart("selectedAttributes");
                for (final Attribute a : parameters.getAttributeList()) {
                    jsonGenerator.writeObject(a.getName());
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeEndObject();

            }
        }
    }

}
