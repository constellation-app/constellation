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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 *
 * @author altair1673
 */
public class MarkdownTree {

    private static final Logger LOGGER = Logger.getLogger(MarkdownTree.class.getName());

    private final MarkdownNode root;

    private String rawString = "";

    private final Pattern headingPattern = Pattern.compile("#{1,6}\\s([^\\n]+)");
    private final Pattern boldPattern = Pattern.compile("\\*\\*\\s?([^\\n]+)\\*\\*");
    private final Pattern boldPattern2 = Pattern.compile("__\\s?([^\\n]+)__");
    private final Pattern italicPattern = Pattern.compile("\\*\\s?([^\\n]+)\\*");
    private final Pattern italicPattern2 = Pattern.compile("_\\s?([^\\n`]+)_");
    private final Pattern strikeThroughPattern = Pattern.compile("~~\\s?([^\\n]+)~~");
    private final Pattern paragraphPattern = Pattern.compile("((?:[^\\n][\\n]?)+)");

    public MarkdownTree() {
        root = new MarkdownNode();
    }

    public MarkdownTree(String rawString) {
        root = new MarkdownNode();
        this.rawString += rawString + "\n";
        //LOGGER.log(Level.SEVERE, "The raw string: " + rawString);
    }

    public void parse() {
        parseString(root, rawString);
    }

    private void parseString(MarkdownNode currentNode, String text) {
        //LOGGER.log(Level.SEVERE, "parseString: " + currentNode.getTypeString());
        if (text.isBlank() || text.isEmpty()) {
            return;
        }

        /*if (currentNode.getType() == MarkdownNode.Type.HEADING && text.charAt(text.length() - 1) != '\n') {
            text += "\n";
        }*/


        char boldSyntax = 'f';
        int currentIndex = 0;
        final char[] syntaxList = {'#', '*', '_', '~', '.'};
        while (currentIndex < text.length()) {
            //LOGGER.log(Level.SEVERE, "working on: " + text);
            int closestSyntax = Integer.MAX_VALUE;
            //LOGGER.log(Level.SEVERE, "Current Index is: " + currentIndex);
            for (int i = 0; i < 5; ++i) {
                if (text.indexOf(syntaxList[i], currentIndex) != -1 && text.indexOf(syntaxList[i], currentIndex) < closestSyntax) {
                    closestSyntax = text.indexOf(syntaxList[i], currentIndex);
                }
            }

            if (closestSyntax != Integer.MAX_VALUE && (text.charAt(closestSyntax) == '*' || text.charAt(closestSyntax) == '_')) {
                boldSyntax = text.charAt(closestSyntax);
            } else {
                boldSyntax = 'f';
            }

            /*if (text.charAt(closestSyntax) == '\n' && closestSyntax + 1 < text.length() && text.charAt(closestSyntax + 1) != '\n') {
                currentIndex = closestSyntax + 1;
                continue;
            }*/


            if (closestSyntax == Integer.MAX_VALUE) {
                LOGGER.log(Level.SEVERE, "No syntax found");
                closestSyntax = currentIndex;
            } else if (closestSyntax != currentIndex) {
                //LOGGER.log(Level.SEVERE, "Making early text for: " + text.substring(currentIndex, closestSyntax));

                MarkdownNode normal;

                if (closestSyntax != text.length() - 1 && text.charAt(closestSyntax) == '\n') {
                    normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex, closestSyntax + 1), -99);
                } else if (text.charAt(closestSyntax) == '.'
                        && closestSyntax - 1 >= 0
                        && Character.isDigit(text.charAt(closestSyntax - 1))
                        && ((currentNode.getType() == MarkdownNode.Type.ORDERED_LIST
                        || currentNode.getType() == MarkdownNode.Type.UNORDERED_LIST
                        || currentNode.getType() == MarkdownNode.Type.LIST_ITEM)
                        || text.charAt(closestSyntax - 1) == '1')) {
                    normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex, closestSyntax - 1), -99);
                } else {
                    //LOGGER.log(Level.SEVERE, "First character of the early text: " + text.charAt(currentIndex));
                    normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex, closestSyntax), -99);
                }

                //if (!(normal.getValue().isBlank() || normal.getValue().isEmpty())) {
                    //LOGGER.log(Level.SEVERE, "Making early text for: " + text.substring(currentIndex, closestSyntax));
                    currentNode.getChildren().add(normal);
                //}
            }

            if (text.charAt(closestSyntax) == '#') {
                currentIndex = closestSyntax;
                final Matcher headingMatcher = headingPattern.matcher(text.substring(closestSyntax));
                final Pattern hashPattern = Pattern.compile("#{1,}");
                final Matcher hashMatcher = hashPattern.matcher(text.substring(closestSyntax));

                if (headingMatcher.find() && hashMatcher.find()) {
                    final MarkdownNode heading = new MarkdownNode(MarkdownNode.Type.HEADING, closestSyntax, headingMatcher.end(1), headingMatcher.group(1), hashMatcher.group().length());
                    currentNode.getChildren().add(heading);
                    parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), headingMatcher.group(1));
                    currentIndex += headingMatcher.end();
                }
                else
                    currentIndex++;

            } else if (text.charAt(closestSyntax) == boldSyntax) {
                currentIndex = closestSyntax;
                // Bold
                if (currentIndex + 1 < text.length() && text.charAt(currentIndex + 1) == boldSyntax) {
                    final Matcher boldMatcher;

                    if (text.charAt(currentIndex) == '*') {
                        boldMatcher = boldPattern.matcher(text.substring(currentIndex));
                    } else {
                        boldMatcher = boldPattern2.matcher(text.substring(currentIndex));
                    }

                    if (boldMatcher.find()) {
                        final MarkdownNode bold = new MarkdownNode(MarkdownNode.Type.BOLD, currentIndex, boldMatcher.end(1), boldMatcher.group(1), -99);
                        currentNode.getChildren().add(bold);
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), boldMatcher.group(1));
                        currentIndex += boldMatcher.end(1) + 2;
                    } else {
                        currentIndex++;
                    }
                    // Italic
                } else if (currentIndex + 1 < text.length() && text.charAt(currentIndex + 1) != boldSyntax) {
                    final Matcher italicMatcher;

                    if (text.charAt(currentIndex) == '*') {
                        italicMatcher = italicPattern.matcher(text.substring(currentIndex));
                    } else {
                        italicMatcher = italicPattern2.matcher(text.substring(currentIndex));
                    }

                    if (italicMatcher.find()) {
                        final MarkdownNode italic = new MarkdownNode(MarkdownNode.Type.ITALIC, currentIndex + 1, italicMatcher.end(1), italicMatcher.group(1), -99);
                        currentNode.getChildren().add(italic);
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), italicMatcher.group(1));
                        currentIndex += italicMatcher.end(1) + 1;
                    } else {
                        LOGGER.log(Level.SEVERE, "Italic syntax not found");
                        currentIndex++;
                    }
                }
            } else if (text.charAt(closestSyntax) == '~') {
                currentIndex = closestSyntax;

                final Matcher strikeThroughMatcher = strikeThroughPattern.matcher(text.substring(currentIndex));

                if (strikeThroughMatcher.find()) {
                    LOGGER.log(Level.SEVERE, "Strike through text: " + strikeThroughMatcher.group(1));
                    final MarkdownNode strikeThrough = new MarkdownNode(MarkdownNode.Type.STRIKETHROUGH, currentIndex + 1, strikeThroughMatcher.end(), strikeThroughMatcher.group(1), -99);
                    currentNode.getChildren().add(strikeThrough);
                    parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), strikeThroughMatcher.group(1));

                    // TODO: Test to see what character currentIndex += strikeThroughMatcher.end() + 1 actually is
                    currentIndex += strikeThroughMatcher.end() + 1;
                } else {
                    LOGGER.log(Level.SEVERE, "Strike through not found");
                    currentIndex++;
                }

            } else if (text.charAt(closestSyntax) == '.') {
                currentIndex = closestSyntax;
                int numIndex = closestSyntax - 1;
                int enterIndex = closestSyntax - 2;

                if (numIndex >= 0) {
                    if (text.charAt(numIndex) == '1' && (enterIndex < 0
                            || text.charAt(enterIndex) == '\n'
                            || text.charAt(enterIndex) == '\t') && (currentNode.getType() != MarkdownNode.Type.UNORDERED_LIST
                            && currentNode.getType() != MarkdownNode.Type.ORDERED_LIST
                            || currentNode.getType() == MarkdownNode.Type.LIST_ITEM)) {

                        MarkdownNode orderedList = new MarkdownNode(MarkdownNode.Type.ORDERED_LIST, numIndex, text.length() - 1, "ORDERED LIST", 99);
                        //orderedList.setTabs(1);

                        orderedList.setTabs(currentNode.getTabs());
                        //LOGGER.log(Level.SEVERE, "Ordered list: " + text.substring(numIndex, text.length() - 1));

                        currentNode.getChildren().add(orderedList);
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(numIndex));
                        MarkdownNode listParent = currentNode.getChildren().get(currentNode.getChildren().size() - 1);

                        currentIndex = numIndex + listParent.getChildren().get(listParent.getChildren().size() - 1).getEndIndex() + 1;
                        //LOGGER.log(Level.SEVERE, "End index of list: " + currentIndex);
                    } else if (Character.isDigit(text.charAt(numIndex))
                            && (currentNode.getType() == MarkdownNode.Type.UNORDERED_LIST
                            || currentNode.getType() == MarkdownNode.Type.ORDERED_LIST)) {

                        String tabString = "";

                        for (int i = 0; i < currentNode.getTabs(); ++i) {
                            tabString += "\t";
                        }

                        int endIndex = text.indexOf('\n', currentIndex);

                        while (endIndex != -1 && ((!tabString.isEmpty()
                                && (text.indexOf(tabString, endIndex + 1) != endIndex + 1
                                || (text.indexOf(tabString, endIndex + 1) == endIndex + 1
                                && text.indexOf("\t", endIndex + currentNode.getTabs() + 1) == endIndex + currentNode.getTabs() + 1)))
                                || (tabString.isEmpty()
                                && text.indexOf("\t", endIndex + 1) == endIndex + 1))) {
                            endIndex = text.indexOf("\n", endIndex + 1);
                        }

                        if (endIndex == -1) {
                            text += "\n";
                            endIndex = text.length() - 1;
                        }


                        MarkdownNode listItem = new MarkdownNode(MarkdownNode.Type.LIST_ITEM, currentIndex + 1, endIndex, "LIST ITEM", 99);
                        listItem.setLatestListItem(currentNode.getLatestListItem());
                        listItem.setTabs(currentNode.getTabs() + 1);
                        currentNode.setLatestListItem(currentNode.getLatestListItem() + 1);

                        currentNode.getChildren().add(listItem);
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(currentIndex + 1, endIndex).strip());
                        if (endIndex + 1 < text.length() && text.charAt(endIndex + 1) == '\n') {
                            return;
                        }

                        currentIndex = endIndex + 1;
                    } else
                        ++currentIndex;
                } else
                    ++currentIndex;

            } else {
                LOGGER.log(Level.SEVERE, "Making text node for: " + text.substring(currentIndex));
                MarkdownNode normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, text.length() - 1, text.substring(currentIndex), -99);
                currentNode.getChildren().add(normal);
                return;
            }

            if (currentIndex > text.length() - 1 || currentIndex < 0) {
                return;
            }
        }
    }

    public void print() {
        printContents(root);
    }

    private void printContents(MarkdownNode currentNode) {
        if (currentNode.getType() == MarkdownNode.Type.NORMAL) {
            LOGGER.log(Level.SEVERE, currentNode.getValue());
            return;
        }

        LOGGER.log(Level.SEVERE, currentNode.getTypeString());

        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            printContents(currentNode.getChildren().get(i));
        }

        LOGGER.log(Level.SEVERE, currentNode.getTypeString());
    }

    public List<TextHelper> getTextNodes() {
        return getText(root);
    }

    private List<TextHelper> getText(MarkdownNode currentNode) {
        List<TextHelper> textNodes = new ArrayList<TextHelper>();

        if (currentNode.getType() == MarkdownNode.Type.NORMAL) {
            TextHelper text = new TextHelper(currentNode.getValue());
            text.setFill(Color.WHITE);
            textNodes.add(text);
            return textNodes;
        } else if (currentNode.getType() == MarkdownNode.Type.PARAGRAPH) {
            textNodes.add(new TextHelper("\n\n"));
            //return textNodes;
        }
        else if (currentNode.getType() == MarkdownNode.Type.LIST_ITEM) {
            //LOGGER.log(Level.SEVERE, "Processing list item that has: " + currentNode.getChildren().size() + " children");
            String tabString = "";
            for (int tabs = 0; tabs < currentNode.getTabs(); ++tabs) {
                //LOGGER.log(Level.SEVERE, "Adding tabs");
                tabString += "\t";
            }
            final TextHelper listItemNumber = new TextHelper("\n" + tabString + currentNode.getLatestListItem() + ". ");
            listItemNumber.setFill(Color.WHITE);
            textNodes.add(listItemNumber);

        }

        for (int i = 0; i < currentNode.getChildren().size(); ++i) {
            List<TextHelper> childTextNodes = getText(currentNode.getChildren().get(i));

            for (int j = 0; j < childTextNodes.size(); ++j) {
                TextHelper currentText = childTextNodes.get(j);
                if (currentNode.getType() == MarkdownNode.Type.HEADING) {
                    int level = currentNode.getHeadingLevel();
                    if (level == 1) {
                        currentText.setSize(32.0);
                    } else if (level == 2) {
                        currentText.setSize(24.0);
                    } else if (level == 3) {
                        currentText.setSize(18.72);
                    } else if (level == 4) {
                        currentText.setSize(16.0);
                    } else if (level == 5) {
                        currentText.setSize(12.28);
                    } else {
                        currentText.setSize(10.72);
                    }
                } else if (currentNode.getType() == MarkdownNode.Type.ITALIC) {
                    currentText.setPosture(FontPosture.ITALIC);
                } else if (currentNode.getType() == MarkdownNode.Type.BOLD) {
                    currentText.setWeight(FontWeight.BOLD);
                } else if (currentNode.getType() == MarkdownNode.Type.STRIKETHROUGH) {
                    currentText.setStrikeThrough(true);
                }/* else if (currentNode.getType() == MarkdownNode.Type.LIST_ITEM) {

                    if (!(currentText.getText().getText().isBlank() || currentText.getText().getText().isEmpty())) {
                        //LOGGER.log(Level.SEVERE, "List item text: " + currentText.getText().getText());
                        currentText.setText(" " + currentText.getText().getText());
                    }
                }*/

                //LOGGER.log(Level.SEVERE, "List item text: " + currentText.getText().getText());
                //if (!(currentText.getText().getText().isBlank() || currentText.getText().getText().isEmpty())) {
                textNodes.add(currentText);
                //}

            }
        }

        return textNodes;
    }

}
