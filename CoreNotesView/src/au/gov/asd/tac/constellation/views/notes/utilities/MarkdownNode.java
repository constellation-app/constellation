/*
 * Copyright 2010-2025 Australian Signals Directorate
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

/**
 * This class is the MarkdownNode and represents the different types of text
 * that is processed
 *
 * @author altair1673
 */
public class MarkdownNode {

    // Can have many children
    private final List<MarkdownNode> children = new ArrayList<>();

    private int startIndex;
    private int endIndex;

    private String value;

    private int tabs;
    private int latestListItem;

    private Type type;

    private int headingLevel;

    public enum Type {
        ROOT,
        HEADING,
        PARAGRAPH,
        BOLD,
        ITALIC,
        LINE_BREAK,
        STRIKETHROUGH,
        ORDERED_LIST,
        LIST_END,
        LIST_ITEM,
        NORMAL
    }

    public MarkdownNode() {
        startIndex = -99;
        endIndex = -99;
        value = "";
        tabs = 0;
        latestListItem = 1;
        headingLevel = -99;
        type = Type.ROOT;
        value = "ROOT";
    }

    public MarkdownNode(final Type type, final int startIndex, final int endIndex, final String value, final int headingLevel) {
        this.type = type;

        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.value = value;
        this.headingLevel = headingLevel;
        latestListItem = 1;
        tabs = 0;
    }

    public List<MarkdownNode> getChildren() {
        return children;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public int getTabs() {
        return tabs;
    }

    public void setTabs(final int tabs) {
        this.tabs = tabs;
    }

    public int getLatestListItem() {
        return latestListItem;
    }

    public void setLatestListItem(final int latestListItem) {
        this.latestListItem = latestListItem;
    }


    public int getHeadingLevel() {
        return headingLevel;
    }


}
