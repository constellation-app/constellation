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

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import org.fxmisc.richtext.InlineCssTextArea;

/**
 * EnhancedTextArea is a InlineCssTextArea from the RichTextFX library with
 * added functionality, such as finding and highlighting text.
 *
 * @author sol695510
 */
public class EnhancedTextArea extends InlineCssTextArea {

    private final String text;
    private final Insets insets = new Insets(4, 8, 4, 8);

    /**
     * Constructor.
     *
     * @param text
     */
    public EnhancedTextArea(final String text) {

        this.text = text;
        this.appendText(text);
        this.setBackground(Background.EMPTY);
        this.setAutoHeight(true);
        this.setWrapText(true);
        this.setEditable(false);
        this.setPadding(insets);
    }

    /**
     * Finds and highlights passages of the given string found within the text
     * of the EnhancedTextArea. Also returns the count of passages found.
     *
     * @param searchText Text passage to be searched for.
     * @return Count of passages of searched text found.
     */
    public int findText(final String searchText) {

        List<Tuple<Integer, Integer>> found = new ArrayList<>();

        if (!searchText.isEmpty()) {

            found = StringUtilities.searchRange(text, searchText);
            final String highlight = "-rtfx-background-color: yellow;";

            if (!found.isEmpty()) {
                found.forEach(location -> this.setStyle(location.getFirst(), location.getSecond(), highlight));
            }
        }

        return found.size();
    }
}
