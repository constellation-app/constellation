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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.text.Element;

/**
 * A numbered text area for use with Swing.
 *
 * @author algol
 */
public final class JNumberedTextArea extends JTextArea {

    private final JTextArea textArea;

    public JNumberedTextArea(final JTextArea textArea) {
        this.textArea = textArea;
        setBackground(Color.LIGHT_GRAY);
        setEditable(false);
    }

    public void updateLineNumbers() {
        setText(getLineNumbersText());
    }

    private String getLineNumbersText() {
        final int caretPosition = textArea.getDocument().getLength();
        final Element root = textArea.getDocument().getDefaultRootElement();
        final StringBuilder builder = new StringBuilder();
        builder.append("1").append(System.lineSeparator());

        for (int elementIndex = 2; elementIndex < root.getElementIndex(caretPosition) + 2; elementIndex++) {
            builder.append(elementIndex).append(System.lineSeparator());
        }

        return builder.toString();
    }
}
