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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import org.openide.util.NbPreferences;

/**
 * AttributeTitledPanes are used to display individual attributes and their set
 * of values for CONSTELLATION's attribute editor. The attribute editor UI
 * primarily consists of a number of AttributeTitledPanes for each graph element
 * type.
 * <br>
 * These titled panes also provide context menu actions to modify or delete
 * attributes, or copy text from their values.
 *
 * @author twinkle2_little
 */
public class AttributeTitledPane extends TitledPane {

    private String attributeValue;
    private AttributeData attributeData;

    private final CheckMenuItem hideAttributeMenuItem = new CheckMenuItem("Hide Attribute");
    private final MenuItem copyValueMenuItem = new MenuItem("Copy");
    private final MenuItem modifyAttributeMenuItem = new MenuItem("Modify Attribute");
    private final MenuItem removeAttributeMenuItem = new MenuItem("Delete Attribute");
    private final SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
    private final ContextMenu ctxMenu;
    private final Preferences prefs = NbPreferences.forModule(AttributePreferenceKey.class);
    private static final String HIDDEN = "hidden";

    public AttributeTitledPane() {
        this(null, null);
    }

    public AttributeTitledPane(final EventHandler<ActionEvent> removeEventHandler, final EventHandler<ActionEvent> modifyEventHandler) {

        if (removeEventHandler == null) {
            ctxMenu = new ContextMenu(copyValueMenuItem, separatorMenuItem, hideAttributeMenuItem);
        } else {
            ctxMenu = new ContextMenu(copyValueMenuItem, separatorMenuItem, hideAttributeMenuItem, modifyAttributeMenuItem, removeAttributeMenuItem);
            removeAttributeMenuItem.setOnAction(removeEventHandler);
            modifyAttributeMenuItem.setOnAction(modifyEventHandler);
        }

        copyValueMenuItem.setOnAction((final ActionEvent event) -> {
            final StringSelection ss = new StringSelection(attributeValue);
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
        });

        hideAttributeMenuItem.setOnAction((final ActionEvent event) -> {
            if (hideAttributeMenuItem.isSelected()) {
                hide();
                addToPreference();
            } else {
                unhide();
                removeFromPreference();
            }
        });
        setContextMenu(ctxMenu);

    }

    public void setAttributeValue(final String value) {
        attributeValue = value;
    }

    public void setAttribute(final AttributeData data) {
        attributeData = data;
    }

    public void addMenuItem(final String name, final EventHandler<ActionEvent> handler) {
        final MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(handler);
        ctxMenu.getItems().add(menuItem);
    }

    public void setHidden(final boolean hide) {
        if (hide) {
            hide();
        } else {
            unhide();
        }
    }

    private void hide() {
        if (!this.getStyleClass().contains(HIDDEN)) {
            this.getStyleClass().add(HIDDEN);
        }
        hideAttributeMenuItem.setSelected(true);

    }

    private void unhide() {
        this.getStyleClass().remove(HIDDEN);
        hideAttributeMenuItem.setSelected(false);
    }

    private void addToPreference() {
        String hiddenAttributes = prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTES, "");
        final String elementTypeAndAttributeName = attributeData.getElementType().toString() + attributeData.getAttributeName();
        hiddenAttributes = hiddenAttributes + StringUtilities.escapeString(elementTypeAndAttributeName, AttributePreferenceKey.META_CHARS) + AttributePreferenceKey.SPLIT_CHAR;
        prefs.put(AttributePreferenceKey.HIDDEN_ATTRIBUTES, hiddenAttributes);
    }

    private void removeFromPreference() {
        final String hiddenAttributes = prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTES, "");
        final StringBuilder newLabel = new StringBuilder();
        if (!hiddenAttributes.isEmpty()) {
            final String elementTypeAndAttributeName = attributeData.getElementType().toString() + attributeData.getAttributeName();
            final List<String> hiddenAttrList = StringUtilities.splitLabelsWithEscapeCharacters(hiddenAttributes, AttributePreferenceKey.SPLIT_CHAR_SET);
            for (final String attrName : hiddenAttrList) {
                if (attrName.isEmpty()) {
                    continue;
                }
                if (!attrName.equals(elementTypeAndAttributeName)) {
                    newLabel.append(StringUtilities.escapeString(attrName, AttributePreferenceKey.META_CHARS));
                    newLabel.append(AttributePreferenceKey.SPLIT_CHAR);
                }
            }
            prefs.put(AttributePreferenceKey.HIDDEN_ATTRIBUTES, newLabel.toString());
        }
    }
}
