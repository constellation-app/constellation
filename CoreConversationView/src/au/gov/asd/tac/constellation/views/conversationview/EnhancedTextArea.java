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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.ChangeSelectionPlugin;
import au.gov.asd.tac.constellation.graph.visual.plugins.select.SelectionMode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.InlineCssTextArea;

/**
 * EnhancedTextArea is a InlineCssTextArea from the RichTextFX library with
 * added methods for functionality, such as highlighting text.
 *
 * @author sol695510
 */
public final class EnhancedTextArea extends InlineCssTextArea {

    private final Insets insets = new Insets(6, 10, -8, 10);

    /**
     * Default constructor.
     */
    public EnhancedTextArea() {

        this.setBackground(Background.EMPTY);
        this.setAutoHeight(true);
        this.setWrapText(true);
        this.setEditable(false);
        this.setPadding(insets);
    }

    /**
     * Constructor with context menu for Conversation View.
     *
     * @param text
     * @param contribution Provides information required for context menu.
     */
    public EnhancedTextArea(final String text, final ConversationContribution contribution) {

        this.setBackground(Background.EMPTY);
        this.setAutoHeight(true);
        this.setWrapText(true);
        this.setEditable(false);
        this.setPadding(insets);
        this.appendText(text + "\n");
        //make sure to request focus everytime text area is loaded so ensure cached text been wrapped correctly.
        this.requestFocus();
        
        // Implementation for the 'Copy' context menu option.
        final MenuItem copyTextMenuItem = new MenuItem("Copy");
        copyTextMenuItem.setOnAction(event -> {
            final StringSelection ss = new StringSelection(this.getSelectedText());
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
        });

        // Implementation for the 'Select All' context menu option.
        final MenuItem selectAllTextMenuItem = new MenuItem("Select All");
        selectAllTextMenuItem.setOnAction(event -> this.selectAll());

        // Implementation for the 'Select on Graph' context menu option.
        final MenuItem selectOnGraphMenuItem = new MenuItem("Select on Graph");
        selectOnGraphMenuItem.setOnAction(event -> {
            final BitSet elementIds = new BitSet();
            elementIds.set(contribution.getMessage().getTransaction());

            PluginExecution.withPlugin(VisualGraphPluginRegistry.CHANGE_SELECTION)
                    .withParameter(ChangeSelectionPlugin.ELEMENT_BIT_SET_PARAMETER_ID, elementIds)
                    .withParameter(ChangeSelectionPlugin.ELEMENT_TYPE_PARAMETER_ID, new ElementTypeParameterValue(GraphElementType.TRANSACTION))
                    .withParameter(ChangeSelectionPlugin.SELECTION_MODE_PARAMETER_ID, SelectionMode.REPLACE)
                    .executeLater(GraphManager.getDefault().getActiveGraph());
        });

        final ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(copyTextMenuItem);
        contextMenu.getItems().add(selectAllTextMenuItem);
        contextMenu.getItems().add(selectOnGraphMenuItem);

        this.setOnContextMenuRequested(event -> {
            contextMenu.show(this, event.getScreenX(), event.getScreenY());
            copyTextMenuItem.setDisable(this.getSelectedText().isEmpty());
        });
    }

    /**
     * Finds and highlights occurrences of the given text string found within
     * the EnhancedTextArea's text and returns the count of occurrences found.
     *
     * @param searchText Text string to be searched.
     * @return Count of the occurrences of the searched text string found.
     */
    public int highlightText(final String searchText) {

        // Clear any previous highlighting.
        this.setStyle(0, this.getText().length(), "-rtfx-background-color: transparent;");

        // If searchText isn't whitespace, empty or null, find its occurrences in the EnhancedTextArea's text.
        List<Tuple<Integer, Integer>> found = StringUtils.isBlank(searchText) ? new ArrayList<>() : StringUtilities.searchRange(this.getText(), searchText);

        // Highlight each occurrence at its associated location.
        found.forEach(location -> this.setStyle(location.getFirst(), location.getSecond(), "-rtfx-background-color: yellow;"));

        return found.size();
    }
}
