/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The ConversationState represents the controlled state of a conversation. It
 * is stored on the graph and is therefore saved when the graph is saved.
 *
 * @author sirius
 */
public class ConversationState {

    private final Set<String> hiddenContributionProviders;
    private final List<String> senderAttributes;

    /**
     * Creates a new ConversationState
     */
    public ConversationState() {
        this.hiddenContributionProviders = new HashSet<>();
        this.senderAttributes = new ArrayList<>();
    }

    /**
     * Creates a new ConversationState with the given contribution provider and
     * sender attributes
     *
     * @param hiddenContributionProviders a list of contribution providers who's
     * content will be displayed in the conversation.
     * @param senderAttributes the attributes that will be displayed as the
     * sender of the messages in this conversation.
     */
    public ConversationState(final Set<String> hiddenContributionProviders, final List<String> senderAttributes) {
        this.hiddenContributionProviders = new HashSet<>(hiddenContributionProviders);
        this.senderAttributes = new ArrayList<>(senderAttributes);
    }

    public ConversationState(final ConversationState original) {

        if (original == null) {
            this.hiddenContributionProviders = new HashSet<>();
            this.senderAttributes = new ArrayList<>();
        } else {
            this.hiddenContributionProviders = new HashSet<>(original.hiddenContributionProviders);
            this.senderAttributes = new ArrayList<>(original.senderAttributes);
        }
    }

    public Set<String> getHiddenContributionProviders() {
        return hiddenContributionProviders;
    }

    public List<String> getSenderAttributes() {
        return senderAttributes;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() == o.getClass()) {
            final ConversationState cs = (ConversationState) o;
            return hiddenContributionProviders.equals(cs.hiddenContributionProviders)
                    && senderAttributes.equals(cs.senderAttributes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.hiddenContributionProviders);
        hash = 79 * hash + Objects.hashCode(this.senderAttributes);
        return hash;
    }

    public void setSenderAttributesToKeys(final GraphReadMethods graph) {
        senderAttributes.clear();

        for (final int keyAttributeId : graph.getPrimaryKey(GraphElementType.VERTEX)) {
            final Attribute keyAttribute = new GraphAttribute(graph, keyAttributeId);
            senderAttributes.add(keyAttribute.getName());
        }
    }
}
