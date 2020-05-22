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

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipUtilities;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;

/**
 * A SelectableLabel is a TextArea that has been enhanced to provide similar
 * layout behaviour to a Label.
 *
 * TODO: {@link TextArea#populateContextMenu) no longer exists, fix it.
 *
 * @author sirius
 */
public class SelectableLabel extends TextArea {

    private Node content = null;
    private TextAreaSkin skin = null;
    private List<MenuItem> contextMenuItems = null;

    private double cachedWidth = Double.MIN_VALUE;
    private double cachedPrefHeight = Double.MIN_VALUE;

    private double cachedHeight = Double.MIN_VALUE;
    private double cachedPrefWidth = Double.MIN_VALUE;

    private class SelectableLabelSkin extends TextAreaSkin {

        public SelectableLabelSkin(final TextArea textArea) {
            super(textArea);
        }

//        @Override
//        public void populateContextMenu(final ContextMenu contextMenu) {
//            super.populateContextMenu(contextMenu);
//            if (contextMenuItems != null) {
//                contextMenu.getItems().addAll(contextMenuItems);
//            }
//        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SelectableLabelSkin(this);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        if (cachedHeight != height) {
            if (skin == null) {
                initSkin();
            }
            cachedPrefWidth = content.prefWidth(height);
            cachedHeight = height;
        }

        return cachedPrefWidth;
    }

    @Override
    protected double computePrefHeight(double width) {
        if (cachedWidth != width) {
            if (skin == null) {
                initSkin();
            }
            cachedPrefHeight = content.prefHeight(width);
            cachedWidth = width;
        }

        return cachedPrefHeight;
    }

    public void setSelectableText(String text) {
        cachedHeight = cachedWidth = Double.MIN_VALUE;
        super.setText(text);
    }

    /**
     * Constructor.
     *
     * @param text the label to use for the text. Styles will be copied from
     * this label.
     * @param wrapText specifies whether text wrapping is allowed in this label.
     * @param style the CSS style of the label. This can be null.
     * @param tipsPane the tooltip for the label. This can be null.
     * @param contextMenuItems a list of menu items to add to the context menu
     * of the label. This can be null.
     */
    public SelectableLabel(final String text, boolean wrapText, String style, final TooltipPane tipsPane, final List<MenuItem> contextMenuItems) {
        getStyleClass().add("selectable-label");
        setText(text == null ? "" : text);
        setWrapText(wrapText);
        setEditable(false);
        setPadding(Insets.EMPTY);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setMinHeight(USE_PREF_SIZE);

        if (style != null) {
            setStyle(style);
        }

        if (tipsPane != null) {
            TooltipUtilities.activateTextInputControl(this, tipsPane);
        }

        this.contextMenuItems = contextMenuItems;
    }

    private void initSkin() {
        content = lookup(".content");
        skin = (TextAreaSkin) getSkin();
    }

}
