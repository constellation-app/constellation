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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author altair1673
 */
public class MarkdownTree {

    private static final Logger LOGGER = Logger.getLogger(MarkdownTree.class.getName());

    private final MarkdownNode root;

    private String rawString = "";


    public MarkdownTree() {
        root = new MarkdownNode();
    }

    public MarkdownTree(String rawString) {
        root = new MarkdownNode();
        this.rawString += rawString + "\n";
        LOGGER.log(Level.SEVERE, "The raw string: " + rawString);
    }

    public void parse() {
        parseString(root, rawString);
    }

    private void parseString(MarkdownNode currentNode, String text) {
        if (text.isBlank() || text.isEmpty()) {
            return;
        }

        int currentIndex = 0;
        final char[] syntaxList = {'#', '\n', '*'};
        while (true) {

            int closestSyntax = Integer.MAX_VALUE;

            for (int i = 0; i < 3; ++i) {
                if (text.indexOf(syntaxList[i], currentIndex) != -1 && text.indexOf(syntaxList[i], currentIndex) < closestSyntax) {
                    closestSyntax = text.indexOf(syntaxList[i], currentIndex);
                }
            }
            if (closestSyntax == Integer.MAX_VALUE) {
                LOGGER.log(Level.SEVERE, "No syntax found");
                closestSyntax = currentIndex;
            } else if (closestSyntax != currentIndex) {
                MarkdownNode normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex, closestSyntax), -99);
                currentNode.getChildren().add(normal);
            }

            if (text.charAt(closestSyntax) == '#') {
                int indexOfHeading = text.indexOf("#");
                currentIndex = indexOfHeading + 1;
                int level = 0;
                for (int i = 0; i < text.length(); ++i) {
                    if (text.charAt(i) == '#') {
                        ++level;
                    } else if (text.charAt(i) == ' ') {
                        int endIndex = text.indexOf("\n", i);
                        MarkdownNode heading = new MarkdownNode(MarkdownNode.Type.HEADING, i, endIndex, text.substring(i + 1, endIndex), level);
                        currentNode.getChildren().add(heading);
                        currentIndex = endIndex + 1;
                        break;
                    }
                }

            } else if (text.charAt(closestSyntax) == '\n') {
                currentIndex = closestSyntax;
                int endIndex = text.indexOf("\n", currentIndex);
                if (endIndex != -1) {
                    MarkdownNode paragraph = new MarkdownNode(MarkdownNode.Type.PARAGRAPH, closestSyntax, endIndex, "Paragraph", -99);
                    currentNode.getChildren().add(paragraph);
                    parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(currentIndex, endIndex));
                }
            } else if (text.charAt(closestSyntax) == '*') {
                currentIndex = closestSyntax;

                if (currentIndex + 1 < text.length() && text.charAt(currentIndex + 1) == '*') {
                    ++currentIndex;
                    if (text.indexOf("**", currentIndex + 1) != -1) {
                        int endIndex = text.indexOf("**", currentIndex + 1);
                        MarkdownNode bold = new MarkdownNode(MarkdownNode.Type.BOLD, currentIndex, endIndex, "Bold", -99);
                        currentNode.getChildren().add(bold);
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(currentIndex + 1, endIndex));
                        currentIndex = endIndex + 2;
                    }

                } else if (currentIndex + 1 < text.length() && text.charAt(currentIndex + 1) != '*') {
                    if (text.indexOf("*", currentIndex + 1) != -1) {
                        int endIndex = text.indexOf("*", currentIndex + 1);
                        MarkdownNode italic = new MarkdownNode(MarkdownNode.Type.ITALIC, currentIndex, endIndex, "Italic", -99);
                        currentNode.getChildren().add(italic);
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(currentIndex + 1, endIndex));
                        currentIndex = endIndex + 1;
                    }
                }
            } else {
                MarkdownNode normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, text.length() - 1, text.substring(currentIndex + 1, text.length() - 1), -99);
                currentNode.getChildren().add(normal);
                return;
            }


            if (currentIndex >= text.length() - 1) {
                return;
            }
        }
    }

    public void print() {
        printContents(root);
    }

    private void printContents(MarkdownNode currentNode) {
        if (currentNode.getType() == MarkdownNode.Type.NORMAL) {
            LOGGER.log(Level.SEVERE, "Actual text: " + currentNode.getValue());
            return;
        }

        LOGGER.log(Level.SEVERE, "Parent: " + currentNode.getValue());

        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            printContents(currentNode.getChildren().get(i));
        }

        LOGGER.log(Level.SEVERE, "Parent: " + currentNode.getValue());
    }
}
