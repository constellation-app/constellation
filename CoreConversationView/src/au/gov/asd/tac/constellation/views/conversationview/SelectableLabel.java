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

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import org.apache.commons.lang3.StringUtils;

/**
 * A SelectableLabel is a TextArea that has been enhanced to provide similar
 * layout behaviour to a Label.
 *
 * @author sirius
 */
public class SelectableLabel extends Label {

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
        setText(StringUtils.defaultString(text));
        setWrapText(wrapText);
        final Insets insets = new Insets(3, 3, 3, 3);
        setPadding(insets);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
        setMinHeight(USE_PREF_SIZE);
        setMaxWidth(500);

        if (style != null) {
            setStyle(style);
        }
    }
}
