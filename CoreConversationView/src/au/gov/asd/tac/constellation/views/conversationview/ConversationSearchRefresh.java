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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.conversationview.state.ConversationState;
import au.gov.asd.tac.constellation.views.conversationview.state.ConversationViewConcept;
import java.util.concurrent.Future;

/**
 * The ConversationSearchRefresh enable the text search within bubble in the
 * conversation view.
 * <p>
 * It contains a Conversation, the model for the dynamically generated content
 * based on the current graph selection, as well as a list view of Bubbles to
 * display this dynamic content, and some static controls used to interact with
 * and alter the content that is displayed.
 *
 * @see Conversation
 * @see ConversationBubble
 *
 * @author mimosa
 */
public final class ConversationSearchRefresh {

    private final Conversation conversation;

    public ConversationSearchRefresh(final Conversation conversation) {
        this.conversation = conversation;
    }

    public void updateContributionProviderRefresh(final String contributionProviderName) {
        final Graph graph = conversation.getGraphUpdateManager().getActiveGraph();
        if (graph != null) {
            final Future<?> turningOff = PluginExecutor.startWith(new SimpleEditPlugin("Conversation View: Update Hidden Contribution Providers OFF") {
                @Override
                protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                    final int stateAttribute = ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE.ensure(graph);
                    final ConversationState originalState = (ConversationState) graph.getObjectValue(stateAttribute, 0);
                    final ConversationState newState = new ConversationState(originalState);
                    if (originalState == null) {
                        newState.setSenderAttributesToKeys(graph);
                    }
                    if (contributionProviderName != null && newState.getHiddenContributionProviders().add(contributionProviderName)) {
                        graph.setObjectValue(stateAttribute, 0, newState);
                    }
                }
            }).executeWriteLater(graph);

            PluginExecutor.startWith(new SimpleEditPlugin("Conversation View: Update Hidden Contribution Providers ON") {
                @Override
                protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                    final int stateAttribute = ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE.ensure(graph);
                    final ConversationState originalState = (ConversationState) graph.getObjectValue(stateAttribute, 0);
                    final ConversationState newState = new ConversationState(originalState);
                    if (originalState == null) {
                        newState.setSenderAttributesToKeys(graph);
                    }
                    if (contributionProviderName != null && newState.getHiddenContributionProviders().remove(contributionProviderName)) {
                        graph.setObjectValue(stateAttribute, 0, newState);
                    }
                }
            }).executeWriteLater(graph, turningOff);
        }
    }
}
