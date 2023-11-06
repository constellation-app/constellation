/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;

/**
 * This class makes a tree containing nodes that represent text and their respective formatting
 *
 * @author altair1673
 */
public class MarkdownTree {

    // Root node that doesn't contain any text and is of no type, it is just an entry point for the tree
    private final MarkdownNode root;

    private boolean markdownEnabled = true;

    private String rawString;

    // The different markdown syntax patterns supported
    private static final Pattern HEADING_PATTERN = Pattern.compile("#{1,6}\\s([^\\n]+)", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern BOLD_AND_ITALIC_PATTERN = Pattern.compile("\\*\\*\\*([^\\n]+)\\*\\*\\*");
    private static final Pattern BOLD_AND_ITALIC_PATTERN_2 = Pattern.compile("___([^\\n]+)___");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*\\s?([^\\n\\*]+.{0,1000000}?)\\*\\*", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern BOLD_PATTERN_2 = Pattern.compile("__\\s?([^\\n]+)__", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?<!\\*)\\*\\s?([^\\n]+)(?<!\\*)\\*(?!\\*)", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern ITALIC_PATTERN_2 = Pattern.compile("(?<!_)_\\s?([^\\n`]+)(?<!_)_(?!_)", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern STRIKE_THROUGH_PATTERN = Pattern.compile("~~\\s?([^\\n]+)~~", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern HASH_PATTERN = Pattern.compile("#{1,6}");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d{1,5}.");

    public MarkdownTree() {
        rawString = "";
        root = new MarkdownNode();
    }

    /**
     * Pass in the raw markdown string into the class when creating
     *
     * @param rawString
     */
    public MarkdownTree(final String rawString) {
        root = new MarkdownNode();
        this.rawString = rawString + "\n";
    }

    public void setMarkdownEnabled(final boolean markdownEnabled) {
        this.markdownEnabled = markdownEnabled;
    }

    /**
     * Processes the markdown text
     */
    public void parse() {
        if (markdownEnabled) {
            parseString(root, rawString);
        } else {
            final MarkdownNode normal = new MarkdownNode(MarkdownNode.Type.NORMAL, 0, 0, rawString, -99);
            root.getChildren().add(normal);
        }
    }

    /**
     * Process raw string and record formatting and text inside MarkdownNodes and add them to the tree
     *
     * @param currentNode - The current node the text is under or contained in
     * @param text - The piece of text that the function is currently processing
     */
    private void parseString(final MarkdownNode currentNode, String text) {

        if (StringUtils.isBlank(text)) {
            return;
        }

        // Default letter for bold syntax
        char boldSyntax;

        // Track what part of the string is being parsed
        int currentIndex = 0;

        // The syntax this implementation processes
        final char[] syntaxList = {'#', '*', '_', '~', '.'};

        // Loop through the text
        while (currentIndex < text.length()) {
            // Find the index of the closest syntax
            int closestSyntax = Integer.MAX_VALUE;
            for (int i = 0; i < 5; ++i) {
                // If a symbol is found and has an index less than closestSyntax then set closestSyntax to that index
                if (text.indexOf(syntaxList[i], currentIndex) != -1 && text.indexOf(syntaxList[i], currentIndex) < closestSyntax) {
                    closestSyntax = text.indexOf(syntaxList[i], currentIndex);
                }
            }

            // Since there are two type of symbols for bold and italic, set the boldSyntax variable to the bold/italic symbol used IF that is the closes syntax
            if (closestSyntax != Integer.MAX_VALUE && (text.charAt(closestSyntax) == '*' || text.charAt(closestSyntax) == '_')) {
                boldSyntax = text.charAt(closestSyntax);
            } else {
                // This 'f' means nothing and is just a default value set to the variable
                boldSyntax = 'f';
            }

            // If no syntax is found then reset closest syntax to currentIndex which should be the start of the string.
            if (closestSyntax == Integer.MAX_VALUE) {
                closestSyntax = currentIndex;

                // Else if the index of the closest syntax is not the current index it means that the text between currentIneex and closestSyntax is unformatted raw text
                // so create a MarkdownNode of type normal
            } else if (closestSyntax != currentIndex) {

                // Markdown node to contain raw text
                final MarkdownNode normal;

                if (text.charAt(closestSyntax) == '.'
                        && closestSyntax - 1 >= 0
                        && Character.isDigit(text.charAt(closestSyntax - 1))
                        && ((currentNode.getType() == MarkdownNode.Type.ORDERED_LIST
                        || currentNode.getType() == MarkdownNode.Type.LIST_ITEM)
                        || text.charAt(closestSyntax - 1) == '1')) {
                    // Check that digits exist before the dot
                    final Matcher digitMatcher = DIGIT_PATTERN.matcher(text.substring(currentIndex));

                    // If it does create a MarkdownNode of type normal contaning text from the currentIndex to before the list item number
                    if (digitMatcher.find()) {
                        normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex, closestSyntax - (digitMatcher.group().length() - 1)), -99);

                        // Else create a normal node with type with text up to the dot
                    } else if (closestSyntax + 1 < text.length()) {
                        normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax + 1, text.substring(currentIndex, closestSyntax + 1), -99);
                    } else {
                        normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex), -99);
                    }
                } else if (text.charAt(closestSyntax) == '.') {
                    if (closestSyntax + 1 < text.length()) {
                        normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax + 1, text.substring(currentIndex, closestSyntax + 1), -99);
                    } else {
                        normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex), -99);
                    }
                } else {
                    // Create normal node with text from currentIndex to closestSyntax
                    normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, closestSyntax, text.substring(currentIndex, closestSyntax), -99);
                }

                // Add normal node to markdown tree
                currentNode.getChildren().add(normal);

            }

            // If the symbol for a heading is found
            if (text.charAt(closestSyntax) == '#') {
                currentIndex = closestSyntax;

                if (closestSyntax != 0 && text.charAt(closestSyntax - 1) != '\n') {
                    addSyntaxNormalNode(Character.toString('#'), currentNode);
                    currentIndex++;
                    continue;
                }

                // Match the line of text containing the heading
                final Matcher headingMatcher = HEADING_PATTERN.matcher(text.substring(closestSyntax));

                // Match the hashes at the start of the heading
                final Matcher hashMatcher = HASH_PATTERN.matcher(text.substring(closestSyntax));

                // If a heading is found AND a set of hashes is found
                if (headingMatcher.find() && hashMatcher.find()) {
                    // Make a MarkdownNode of type HEADING with the heading level set to the length of the string found from the hashMatcher variable
                    final MarkdownNode heading = new MarkdownNode(MarkdownNode.Type.HEADING, closestSyntax, headingMatcher.end(1), headingMatcher.group(1), hashMatcher.group().length());
                    // Add node as the child of th ecurrentNode
                    currentNode.getChildren().add(heading);
                    // Call this function on the piece of text inside the heading (NOT THE HASHES) and pass in the Heading node created just now
                    parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), headingMatcher.group(1));

                    // Set currentIndex to be after the heading string
                    currentIndex += headingMatcher.end();
                } else {
                    addSyntaxNormalNode(Character.toString('#'), currentNode);
                    currentIndex++;
                }

                // Else if the clostst syntax is the bold/italic syntax
            } else if (text.charAt(closestSyntax) == boldSyntax) {
                currentIndex = closestSyntax;

                // Check to see if syntax is bold
                if (currentIndex + 2 < text.length() && text.charAt(currentIndex + 1) == boldSyntax && text.charAt(currentIndex + 2) == boldSyntax) {
                    final Matcher boldAndItalicMatcher;

                    // Get a matcher object based on which symbol the user has used
                    if (text.charAt(currentIndex) == '*') {
                        boldAndItalicMatcher = BOLD_AND_ITALIC_PATTERN.matcher(text.substring(currentIndex));
                    } else {
                        boldAndItalicMatcher = BOLD_AND_ITALIC_PATTERN_2.matcher(text.substring(currentIndex));
                    }

                    if (boldAndItalicMatcher.find()) {
                        final MarkdownNode italic = new MarkdownNode(MarkdownNode.Type.ITALIC, currentIndex + 1, boldAndItalicMatcher.end(1), "**" + boldAndItalicMatcher.group(1) + "**", -99);
                        currentNode.getChildren().add(italic);

                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), "**" + boldAndItalicMatcher.group(1) + "**");
                        currentIndex += boldAndItalicMatcher.end(1) + 3;
                    } else {
                        addSyntaxNormalNode(Character.toString(boldSyntax), currentNode);
                        currentIndex++;
                    }

                } else if (currentIndex + 1 < text.length() && text.charAt(currentIndex + 1) == boldSyntax) {
                    final Matcher boldMatcher;

                    // Get a matcher object based on which symbol the user has used
                    if (text.charAt(currentIndex) == '*') {
                        boldMatcher = BOLD_PATTERN.matcher(text.substring(currentIndex));
                    } else {
                        boldMatcher = BOLD_PATTERN_2.matcher(text.substring(currentIndex));
                    }

                    // Find the bolded text
                    if (boldMatcher.find()) {
                        // Create a MarkdownNode of type bold and add it as a child of current node
                        final MarkdownNode bold = new MarkdownNode(MarkdownNode.Type.BOLD, currentIndex, boldMatcher.end(1), boldMatcher.group(1), -99);
                        currentNode.getChildren().add(bold);

                        // Call this function on the text between the bold syntax
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), boldMatcher.group(1));
                        currentIndex += boldMatcher.end(1) + 2;
                    } else {
                        addSyntaxNormalNode(Character.toString(boldSyntax), currentNode);
                        currentIndex++;
                    }
                    // Else if the user wanted italic text
                } else if (currentIndex + 1 < text.length() && text.charAt(currentIndex + 1) != boldSyntax) {
                    final Matcher italicMatcher;

                    // Check for specific italic syntax and get a Matcher out of it
                    if (text.charAt(currentIndex) == '*') {
                        italicMatcher = ITALIC_PATTERN.matcher(text.substring(currentIndex));
                    } else {
                        italicMatcher = ITALIC_PATTERN_2.matcher(text.substring(currentIndex));
                    }

                    // If italic text is found
                    if (italicMatcher.find()) {
                        // Create a MarkdownNode of type ITALIC and add it as a child of the current node
                        final MarkdownNode italic = new MarkdownNode(MarkdownNode.Type.ITALIC, currentIndex + 1, italicMatcher.end(1), italicMatcher.group(1), -99);
                        currentNode.getChildren().add(italic);

                        // Call this funciton on this italic text
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), italicMatcher.group(1));
                        currentIndex += italicMatcher.end(1) + 1;
                    } else {
                        addSyntaxNormalNode(Character.toString(boldSyntax), currentNode);
                        currentIndex++;
                    }
                } else {
                    addSyntaxNormalNode(Character.toString(boldSyntax), currentNode);
                    currentIndex++;
                }
                // Else if strikethrough syxtax is found
            } else if (text.charAt(closestSyntax) == '~') {
                currentIndex = closestSyntax;

                // Get a matcher for the striketrhough pattern
                final Matcher strikeThroughMatcher = STRIKE_THROUGH_PATTERN.matcher(text.substring(currentIndex));

                // If correct strike through syntax is found
                if (strikeThroughMatcher.find()) {
                    // Create a MarkdownNode of type STRIKETHROUGH and add it to the tree
                    final MarkdownNode strikeThrough = new MarkdownNode(MarkdownNode.Type.STRIKETHROUGH, currentIndex + 1, strikeThroughMatcher.end(), strikeThroughMatcher.group(1), -99);
                    currentNode.getChildren().add(strikeThrough);

                    // Call this funciton on the Strikethrough text
                    parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), strikeThroughMatcher.group(1));

                    currentIndex += strikeThroughMatcher.end();
                } else {
                    addSyntaxNormalNode("~", currentNode);
                    currentIndex++;
                }

                // Else if list syntax is found
            } else if (text.charAt(closestSyntax) == '.') {
                currentIndex = closestSyntax;

                // The index of where a number should be
                final int numIndex = closestSyntax - 1;

                // The index of where an enter should be
                final int enterIndex = closestSyntax - 2;

                if (numIndex >= 0) {
                    // If the character at the numIndex is 1 and there is an enter before it and the current node IS NOT a ordered list OR it IS of type LIST_ITEM
                    if (text.charAt(numIndex) == '1' && (enterIndex < 0
                            || text.charAt(enterIndex) == '\n'
                            || text.charAt(enterIndex) == '\t') && (currentNode.getType() != MarkdownNode.Type.ORDERED_LIST
                            || currentNode.getType() == MarkdownNode.Type.LIST_ITEM)) {

                        // The list ends where there is a tripple enter
                        final int endIndex = text.indexOf("\n\n\n", numIndex);

                        // Make a MarkdownNode of type ORDERED_LIST that starts at the number 1 and ends at the tripple enter
                        final MarkdownNode orderedList = new MarkdownNode(MarkdownNode.Type.ORDERED_LIST, numIndex, endIndex, "ORDERED LIST", 99);

                        // Set how many tabs the list should have
                        orderedList.setTabs(currentNode.getTabs());

                        currentNode.getChildren().add(orderedList);

                        // If the tripple enter is found then call this function on the text contained within the ordered list
                        if (endIndex != -1) {
                            parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(numIndex, endIndex));

                            // Else call this function on the entire piece of text from the first list number
                        } else {
                            parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(numIndex));
                        }

                        // Get the list parent
                        final MarkdownNode listParent = currentNode.getChildren().get(currentNode.getChildren().size() - 1);

                        // Add a MarkdoenNode of type LIST_END
                        final MarkdownNode listEnd = new MarkdownNode(MarkdownNode.Type.LIST_END, endIndex, endIndex, "LIST END", 99);
                        currentNode.getChildren().add(listEnd);

                        // Get the endIndex of the last list item and set curentIndex to that.
                        currentIndex = numIndex + listParent.getChildren().get(listParent.getChildren().size() - 1).getEndIndex() + 1;

                        // Else if the character at num index is a digit and the current node is an Ordered List
                    } else if (Character.isDigit(text.charAt(numIndex))
                            && (currentNode.getType() == MarkdownNode.Type.ORDERED_LIST)) {

                        final StringBuilder tabString = new StringBuilder();

                        // Find the ammount of tabs for the currentNode
                        for (int i = 0; i < currentNode.getTabs(); ++i) {
                            tabString.append("\t");
                        }

                        // Find the next new line character from the begining of the list item
                        int endIndex = text.indexOf('\n', currentIndex);

                        // While a \n character is found check if the tabs on this list item
                        // and the next line does not start with the same amount of tabs or it does start with the same amount of tabs
                        // and there is a new tab right after that OR there are no tabs on the list item and the next line contains a tab
                        // then change end index of the \n for this next line
                        final String tabs = tabString.toString();
                        while (endIndex != -1 && ((StringUtils.isEmpty(tabs)
                                && (text.indexOf(tabs, endIndex + 1) != endIndex + 1
                                || (text.indexOf(tabs, endIndex + 1) == endIndex + 1
                                && text.indexOf("\t", endIndex + currentNode.getTabs() + 1) == endIndex + currentNode.getTabs() + 1)) || text.indexOf("\n", endIndex + 1) == endIndex + 1)
                                || (StringUtils.isEmpty(tabs)
                                && text.indexOf("\t", endIndex + 1) == endIndex + 1 || text.indexOf("\n", endIndex + 1) == endIndex + 1))) {
                            endIndex = text.indexOf("\n", endIndex + 1);
                        }

                        if (endIndex == -1) {
                            text += "\n";
                            endIndex = text.length() - 1;
                        }

                        // Create a list item from the part after the dot to the end of the list item
                        final MarkdownNode listItem = new MarkdownNode(MarkdownNode.Type.LIST_ITEM, currentIndex + 1, endIndex, "LIST ITEM", 99);

                        // Set the number that should appear before the list item
                        listItem.setLatestListItem(currentNode.getLatestListItem());

                        // Set the ammount of tabs before the list item
                        listItem.setTabs(currentNode.getTabs() + 1);
                        currentNode.setLatestListItem(currentNode.getLatestListItem() + 1);

                        currentNode.getChildren().add(listItem);

                        // Call this function on the text in the actual list item
                        parseString(currentNode.getChildren().get(currentNode.getChildren().size() - 1), text.substring(currentIndex + 1, endIndex).strip());

                        // If the list item ends up being the final piece of text for the entire text than end the function
                        if (endIndex + 1 < text.length() && text.charAt(endIndex + 1) == '\n') {
                            return;
                        }

                        currentIndex = endIndex;
                    } else {
                        ++currentIndex;
                    }
                } else {
                    ++currentIndex;
                }

                // If no syntax is found it means that it is just raw text so a normal node can be made
            } else {
                final MarkdownNode normal = new MarkdownNode(MarkdownNode.Type.NORMAL, currentIndex, text.length() - 1, text.substring(currentIndex), -99);
                currentNode.getChildren().add(normal);
                return;
            }

            if (currentIndex > text.length() - 1 || currentIndex < 0) {
                return;
            }
        }
    }

    public List<TextHelper> getTextNodes() {
        return getText(root);
    }

    /**
     * Itterates over MarkdownTreee and returns a list of TextHelper oblects
     *
     * @param currentNode
     * @return List of formatted text
     */
    private List<TextHelper> getText(final MarkdownNode currentNode) {
        List<TextHelper> textNodes = new ArrayList<>();

        // Base case, make a TextHelper object with the raw text from the NORMAL markdown node and add no formatting and return that in a list
        if (currentNode.getType() == MarkdownNode.Type.NORMAL) {
            final TextHelper text = new TextHelper(currentNode.getValue());        
            text.setFill(Color.WHITE);         
            textNodes.add(text);
            return textNodes;
            // If the currentNode is an ordered list then add in a TextHelper signifying the begining of a list
        } else if (currentNode.getType() == MarkdownNode.Type.ORDERED_LIST) {
            final TextHelper startList = new TextHelper("");
            startList.setIsListStart(true);
            textNodes.add(startList);

            // Same for the end of a list add in a TextHelper for list end
        } else if (currentNode.getType() == MarkdownNode.Type.LIST_END) {
            final TextHelper endList = new TextHelper("");
            endList.setIsListEnd(true);
            textNodes.add(endList);

            // If the current node is a list item add in a text helper containing a new line character and the number of that list
        } else if (currentNode.getType() == MarkdownNode.Type.LIST_ITEM) {
            final TextHelper listItemNumber = new TextHelper("\n" + currentNode.getLatestListItem() + ". ");
            listItemNumber.setFill(Color.WHITE);
            textNodes.add(listItemNumber);
        }

        // For each children of the currentNode
        for (int i = 0; i < currentNode.getChildren().size(); i++) {
            // Call this funciton on all its children
            final List<TextHelper> childTextNodes = getText(currentNode.getChildren().get(i));

            // For each children apply formatting to the TextHelper based on the child's type
            for (int j = 0; j < childTextNodes.size(); ++j) {
                final TextHelper currentText = childTextNodes.get(j);
                if (currentNode.getType() == MarkdownNode.Type.HEADING) {
                    int level = currentNode.getHeadingLevel();
                    switch (level) {
                        case 1:
                            currentText.setSize(32.0);
                            break;
                        case 2:
                            currentText.setSize(24.0);
                            break;
                        case 3:
                            currentText.setSize(18.72);
                            break;
                        case 4:
                            currentText.setSize(16.0);
                            break;
                        case 5:
                            currentText.setSize(12.28);
                            break;
                        default:
                            currentText.setSize(10.72);
                            break;
                    }
                } else if (currentNode.getType() == MarkdownNode.Type.ITALIC) {
                    currentText.setPosture(FontPosture.ITALIC);
                } else if (currentNode.getType() == MarkdownNode.Type.BOLD) {
                    currentText.setWeight(FontWeight.BOLD);
                } else if (currentNode.getType() == MarkdownNode.Type.STRIKETHROUGH) {
                    currentText.setStrikeThrough(true);
                }

                // Add the TextHelper to the list of formatted text to be returned
                textNodes.add(currentText);
            }
        }

        return textNodes;
    }

    private void addSyntaxNormalNode(final String syntax, final MarkdownNode parent) {
        final MarkdownNode normal = new MarkdownNode(MarkdownNode.Type.NORMAL, 0, 1, syntax, -99);
        parent.getChildren().add(normal);
    }

    /**
     * Adds formatted texts to a TextFlow
     *
     * @return a TextFlow containing all the formatted text
     */
    public TextFlow getRenderedText() {
        // TextFlow to be returned
        final TextFlow renderedText = new TextFlow();
        renderedText.setTextAlignment(TextAlignment.LEFT);
        renderedText.setPadding(new Insets(0, 0, 0, 0));
        renderedText.setLineSpacing(-3.5);

        // List of text flows
        final List<TextFlow> textFlowList = new ArrayList<>();
        textFlowList.add(renderedText);

        // Get the formatted text
        final List<TextHelper> textNodes = getText(root);

        int tabCount = 0;

        // For each piece of formatted text
        for (int i = 0; i < textNodes.size(); i++) {
            // If the textNode signifies a list starting
            if (textNodes.get(i).isIsListStart()) {
                // Increase the tabCount
                tabCount++;

                // Create a new textflow
                final TextFlow listFlow = new TextFlow();
                listFlow.setTextAlignment(TextAlignment.LEFT);
                listFlow.setPadding(new Insets(0, 0, 0, 0));
                listFlow.setBorder(Border.EMPTY);
                listFlow.setLineSpacing(-3.5);

                // Add the newly created text flow to the previous text flow and to the list
                textFlowList.get(textFlowList.size() - 1).getChildren().add(listFlow);
                textFlowList.add(listFlow);

                // Indent the text flow based on the tab count variable
                listFlow.setTranslateX(tabCount * 10);

                listFlow.prefWidthProperty().bind(textFlowList.get(textFlowList.size() - 2).widthProperty().subtract(15 * tabCount));

                // Else if the TextNode signifies a list end then remove the latest TextFlow from the list ONLY and reduce the tab count
            } else if (textNodes.get(i).isIsListEnd()) {
                tabCount--;
                textFlowList.remove(textFlowList.size() - 1);
            }

            // If the current TextNode is within a list
            if (tabCount > 0) {
                // Make a StringBuilder containing the raw text
                final StringBuilder builder = new StringBuilder(textNodes.get(i).getText().getText());
                int indexOfNewLine = builder.indexOf("\n");

                // Remove all tabs after each new line charcter from witin that list
                while (indexOfNewLine != -1) {
                    if (indexOfNewLine + 1 < builder.length() && builder.charAt(indexOfNewLine + 1) == '\t') {
                        builder.deleteCharAt(indexOfNewLine + 1);

                        int tabIndex = indexOfNewLine + 1;
                        while (tabIndex < builder.length()) {
                            if (builder.charAt(tabIndex) == '\t') {
                                builder.deleteCharAt(tabIndex);
                            } else {
                                break;
                            }
                        }
                    }
                    indexOfNewLine = builder.indexOf("\n", indexOfNewLine + 1);
                }

                // Set the textNodes raw string to be this new one with the appropriate tabs removed
                textNodes.get(i).setText(builder.toString());
            }

            // Add textNode to the last TextFlow in the list
            textFlowList.get(textFlowList.size() - 1).getChildren().add(textNodes.get(i).getText());
        }

        return renderedText;
    }

    public MarkdownNode getRoot() {
        return root;
    }

}
