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
package au.gov.asd.tac.constellation.utilities.text;

import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.fxmisc.richtext.InlineCssTextArea;

/**
 * @author Auriga2
 */
public class SpellCheckingTextArea extends InlineCssTextArea {

    private final SpellChecker spellChecker = new SpellChecker(this);
    private final Insets insets = new Insets(4, 8, 4, 8);

    /**
     * Default constructor.
     */
    public SpellCheckingTextArea() {
        this.setAutoHeight(false);
        this.setWrapText(true);
        this.setPadding(insets);

        this.setOnMouseClicked((final MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.isStillSincePress()) {
                spellChecker.popUpSuggestionsListAction(event);
            }
        });

        this.textProperty().addListener((observable, oldText, newText) -> {
            spellChecker.checkSpelling();
        });
    }

    /**
     * Constructor with input text.
     */
    public SpellCheckingTextArea(final String text) {
        this();
        setText(text);
    }

    public void setText(final String text) {
        this.replaceText(text);
    }

    /**
     * underline and highlight the text from start to end.
     */
    public void highlightText(final int start, final int end) {
        String underlineAndHighlight = "-rtfx-background-color:yellow;" + "-rtfx-underline-color: red; "
                + "-rtfx-underline-dash-array: 2 2;" + "-rtfx-underline-width: 2.0;";

        this.setStyle(start, end, underlineAndHighlight);
    }

    /**
     * Clear any previous highlighting.
     */
    public void clearStyles() {
        this.setStyle(0, this.getText().length(), "-rtfx-background-color: transparent;-rtfx-underline-color: transparent;");
    }
}
