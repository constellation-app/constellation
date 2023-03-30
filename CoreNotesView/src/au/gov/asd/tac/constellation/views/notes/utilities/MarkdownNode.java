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

/**
 *
 * @author altair1673
 */
public class MarkdownNode {
    private final List<MarkdownNode> children = new ArrayList<MarkdownNode>();

    private int startIndex = -99;
    private int endIndex = -99;

    private String value = "";

    private Type type;

    private int headingLevel = -99;

    public enum Type {
        ROOT,
        HEADING,
        PARAGRAPH,
        BOLD,
        ITALIC,
        NORMAL
    }

    public MarkdownNode() {
        type = Type.ROOT;
    }

    public MarkdownNode(Type type, int startIndex, int endIndex, String value, int headingLevel) {
        this.type = type;

        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.value = value;
        this.headingLevel = headingLevel;
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

    public int getHeadingLevel() {
        return headingLevel;
    }


}
