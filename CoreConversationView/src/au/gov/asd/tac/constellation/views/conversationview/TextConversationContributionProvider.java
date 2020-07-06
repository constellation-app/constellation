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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectionMode;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import org.openide.util.lookup.ServiceProvider;

/**
 * A TextConversationContributionProvider adds the contents of the Content
 * attribute to a message.
 *
 * @author sirius
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
        protected Region createContent(final TooltipPane tips) {
            final List<MenuItem> menuItems = new ArrayList<>();
            final MenuItem selectOnGraphMenuItem = new MenuItem("Select on Graph");
            selectOnGraphMenuItem.setOnAction(event -> {
                final BitSet elementIds = new BitSet();
                elementIds.set(getMessage().getTransaction());

                PluginExecution.withPlugin(VisualGraphPluginRegistry.CHANGE_SELECTION)
                        .withParameter(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds)
                        .withParameter(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID, new ElementTypeParameterValue(GraphElementType.TRANSACTION))
                        .withParameter(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REPLACE)
                        .executeLater(GraphManager.getDefault().getActiveGraph());
            });
            menuItems.add(selectOnGraphMenuItem);

            return new SelectableLabel(text, true, null, tips, menuItems);
        }

        @Override
        public String toString() {
            return "Text Contribution";
        }
    }
}
