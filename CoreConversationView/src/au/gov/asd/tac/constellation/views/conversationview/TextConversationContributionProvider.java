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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectionMode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.BitSet;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
    private final Insets DEFAULT_INSETS = new Insets (5, 5, 5, 5);
    
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
            
            final TextFlow textFlow = new TextFlow();
            textFlow.setPadding(DEFAULT_INSETS);
            
            // Implementation for the 'Copy' context menu option.
            final MenuItem copyTextMenuItem = new MenuItem("Copy");
            copyTextMenuItem.setOnAction(event -> {
                final StringBuilder sb = new StringBuilder();
                
                for (final Node node : textFlow.getChildren()) {
                    if (node instanceof Text) {
                        sb.append(((Text) node).getText());
                    }
                }
                
                final StringSelection ss = new StringSelection(sb.toString());
                final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
            });
            
            // Implementation for the 'Select on Graph' context menu option.
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
            
            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().add(copyTextMenuItem);
            contextMenu.getItems().add(selectOnGraphMenuItem);
            
            textFlow.setOnContextMenuRequested(event -> {
                contextMenu.show(textFlow, event.getScreenX(), event.getScreenY());
            });
            
            // Return the created region with or without any search results if given any.
            return SearchText.createRegionWithSearchHits(textFlow, text);
        }
        
        @Override
        public String toString() {
            return "Text Contribution";
        }
    }
}
