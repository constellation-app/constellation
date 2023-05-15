/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes.utilities;

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * This call represents the formatted text derived from the MarkdownTree
 *
 * @author altair1673
 */
public class TextHelper {

    private final Text text;

    private String family = "Helvetica";
    private FontWeight weight = FontWeight.NORMAL;
    private FontPosture posture = FontPosture.REGULAR;
    private double size = 12.28;

    private boolean isListStart = false;
    private boolean isListEnd = false;

    public TextHelper(final String rawText) {
        text = new Text(rawText);
        text.setFont(Font.font(family, weight, posture, size));
    }

    public Text getText() {
        return text;
    }

    public void setText(final String rawText) {
        text.setText(rawText);
        applyFont();
    }

    private void applyFont() {
        text.setFont(Font.font(family, weight, posture, size));
    }

    public void setFontFamily(final String family) {
        this.family = family;
        applyFont();
    }

    public void setWeight(final FontWeight weight) {
        this.weight = weight;
        applyFont();
    }

    public void setPosture(final FontPosture posture) {
        this.posture = posture;
        applyFont();
    }

    public void setSize(final double size) {
        this.size = size;
        applyFont();
    }

    public void setFill(final Color colour) {
        text.setFill(colour);
        applyFont();
    }

    public void setStrikeThrough(final boolean strikeThrough) {
        text.setStrikethrough(strikeThrough);
        applyFont();
    }

    public boolean isIsListStart() {
        return isListStart;
    }

    public void setIsListStart(boolean isListStart) {
        this.isListStart = isListStart;
    }

    public boolean isIsListEnd() {
        return isListEnd;
    }

    public void setIsListEnd(boolean isListEnd) {
        this.isListEnd = isListEnd;
    }


}
