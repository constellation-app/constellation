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
package au.gov.asd.tac.constellation.views.conversationview.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * The ConversationStateIOProvider supports saving and loading of the
 * ConversationState when the graph is saved and loaded.
 *
 * @author sirius
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class ConversationStateIOProvider extends AbstractGraphIOProvider {

    private static final String HIDDEN_CONTRIBUTION_PROVIDERS_TAG = "hiddenContributionProviders";
    private static final String SENDER_ATTRIBUTES_TAG = "senderAttributes";

    @Override
    public String getName() {
        return ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE.getAttributeType();
    }

    @Override
    public void readObject(int attributeId, int elementId, JsonNode jnode, GraphWriteMethods graph, Map<Integer, Integer> vertexMap, Map<Integer, Integer> transactionMap, GraphByteReader byteReader, ImmutableObjectCache cache) throws IOException {
        if (!jnode.isNull()) {
            final ConversationState state = new ConversationState();

            JsonNode hiddenContributionProvidersArray = jnode.get(HIDDEN_CONTRIBUTION_PROVIDERS_TAG);
            Iterator<JsonNode> hiddenContributionProviderIterator = hiddenContributionProvidersArray.iterator();
            while (hiddenContributionProviderIterator.hasNext()) {
                JsonNode hiddenContributionProvider = hiddenContributionProviderIterator.next();
                state.getHiddenContributionProviders().add(hiddenContributionProvider.asText());
            }

            JsonNode senderAttributesArray = jnode.get(SENDER_ATTRIBUTES_TAG);
            Iterator<JsonNode> senderAttributesIterator = senderAttributesArray.iterator();
            while (senderAttributesIterator.hasNext()) {
                JsonNode senderAttribute = senderAttributesIterator.next();
                state.getSenderAttributes().add(senderAttribute.asText());
            }

            graph.setObjectValue(attributeId, elementId, state);
        }
    }

    @Override
    public void writeObject(Attribute attribute, int elementId, JsonGenerator jsonGenerator, GraphReadMethods graph, GraphByteWriter byteWriter, boolean verbose) throws IOException {
        if (verbose || !graph.isDefaultValue(attribute.getId(), elementId)) {
            final ConversationState state = (ConversationState) graph.getObjectValue(attribute.getId(), elementId);
            if (state == null) {
                jsonGenerator.writeNullField(attribute.getName());
            } else {
                jsonGenerator.writeObjectFieldStart(attribute.getName());

                jsonGenerator.writeArrayFieldStart(HIDDEN_CONTRIBUTION_PROVIDERS_TAG);
                for (String hiddenContributionProvider : state.getHiddenContributionProviders()) {
                    jsonGenerator.writeString(hiddenContributionProvider);
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeArrayFieldStart(SENDER_ATTRIBUTES_TAG);
                for (String senderAttribute : state.getSenderAttributes()) {
                    jsonGenerator.writeString(senderAttribute);
                }
                jsonGenerator.writeEndArray();

                jsonGenerator.writeEndObject();
            }
        }
    }
}
