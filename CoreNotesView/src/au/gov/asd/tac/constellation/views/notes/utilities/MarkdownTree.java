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

/**
 *
 * @author altair1673
 */
public class MarkdownTree {
    private final MarkdownNode root;

    private String rawString = "";


    public MarkdownTree() {
        root = new MarkdownNode();
    }

    public MarkdownTree(String rawString) {
        root = new MarkdownNode();
        this.rawString = rawString + "\n";
    }

    public void parseString(MarkdownNode currentNode, String text) {
        int currentIndex = 0;
        final char[] syntaxList = {'#', '\n', '*'};
        while (true) {

            int closestSyntax = Integer.MAX_VALUE;

            for (int i = 0; i < 3; ++i) {
                if (text.indexOf(syntaxList[i], currentIndex) != -1 && text.indexOf(syntaxList[i], currentIndex) < closestSyntax) {
                    closestSyntax = text.indexOf(syntaxList[i], currentIndex);
                }
            }

            if (text.charAt(closestSyntax) == '#') {
                int indexOfHeading = text.indexOf("#");
                currentIndex = indexOfHeading + 1;
                int level = -1;
                for (int i = 0; i < text.length(); ++i) {
                    if (text.charAt(i) == '#') {
                        ++level;
                    } else if (text.charAt(i) == ' ') {
                        int endIndex = text.indexOf("\n", i);
                        MarkdownNode heading = new MarkdownNode(MarkdownNode.Type.HEADING, i, endIndex, text.substring(i + 1, endIndex), level);
                        currentNode.getChildren().add(heading);
                        currentIndex = endIndex;
                        break;
                    }
                }

            } else if (text.charAt(closestSyntax) == '\n') {
                currentIndex = closestSyntax;
                int endIndex = text.indexOf("\n", currentIndex);
                if (endIndex != -1) {
                    MarkdownNode paragraph = new MarkdownNode(MarkdownNode.Type.PARAGRAPH, closestSyntax, endIndex, text.substring(currentIndex + 1, endIndex), -99);
                    currentNode.getChildren().add(paragraph);
                    parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(currentIndex + 1, endIndex));
                }
            } else if (text.charAt(closestSyntax) != '*') {

            }


            if (currentIndex >= text.length() - 1) {
                return;
            }
        }
    }
}
