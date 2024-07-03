/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipUtilities;
import javafx.scene.layout.Region;
import org.openide.util.lookup.ServiceProvider;

/**
 * A TextConversationContributionProvider adds the contents of the Content
 * attribute to a message.
 *
 * @author sirius
 * @author sol695510
 */
@ServiceProvider(service = ConversationContributionProvider.class, position = 1)
public class TextConversationContributionProvider extends ConversationContributionProvider {

    private static final String DISPLAY_NAME = "Text";
    private int contentAttribute = Graph.NOT_FOUND;

    public TextConversationContributionProvider() {
        super(DISPLAY_NAME, 0);
    }

    @Override
    public boolean isCompatibleWithGraph(final GraphReadMethods graph) {
        contentAttribute = ContentConcept.TransactionAttribute.CONTENT.get(graph);
        return contentAttribute != Graph.NOT_FOUND;
    }

    @Override
    public ConversationContribution createContribution(final GraphReadMethods graph, final ConversationMessage message) {
        final String text = graph.getStringValue(contentAttribute, message.getTransaction());

        if (text != null) {
            return new TextContribution(message, text);
        }

        return null;
    }

    protected class TextContribution extends ConversationContribution {

        private final String text;

        public TextContribution(final ConversationMessage message, final String text) {
            super(TextConversationContributionProvider.this, message);
            this.text = text;
        }

        @Override
        protected String getText() {
            return text;
        }

        @Override
        protected Region createContent(final TooltipPane tips) {
            final EnhancedTextArea textArea = new EnhancedTextArea(text, this);
            TooltipUtilities.activateTextInputControl(textArea, tips);
            return textArea;
        }

        @Override
        public String toString() {
            return "Text Contribution";
        }
    }
}
