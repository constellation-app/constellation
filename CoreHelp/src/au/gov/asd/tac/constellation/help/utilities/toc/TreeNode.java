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
package au.gov.asd.tac.constellation.help.utilities.toc;

import au.gov.asd.tac.constellation.help.utilities.Generator;
import au.gov.asd.tac.constellation.help.utilities.HelpMapper;
import com.jogamp.common.os.Platform;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * TreeNode class which handles the storage of other nodes in a tree data
 * structure
 *
 * @author aldebaran30701
 * @param <T> Generic type of data to hold within the TreeNode
 */
public class TreeNode<T> {

    private static final Logger LOGGER = Logger.getLogger(TreeNode.class.getName());

    private static Map<String, String> cachedHelpMappings = null;

    private final T data;

    private final List<TreeNode<T>> children = new ArrayList<>();

    private TreeNode<T> parent = null;

    public TreeNode(final T data) {
        this.data = data;
    }

    public void addChild(final TreeNode<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChildren(final List<TreeNode<T>> children) {
        children.forEach(each -> each.setParent(this));
        this.children.addAll(children);
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    private void setParent(final TreeNode<T> parent) {
        this.parent = parent;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    /**
     * Print the tree to the stringbuilder and return the builder.
     *
     * @param <T>
     * @param node
     * @param appender
     * @param builder
     * @return
     */
    public static <T> StringBuilder printTree(final TreeNode<T> node, final String appender, final StringBuilder builder) {
        builder.append(appender);
        builder.append(node.getData());
        node.getChildren().forEach(each -> printTree(each, appender, builder));

        return builder;
    }

    /**
     * Write tree to the file writer provided. Caches the helpMappings to
     * cachedHelpMappings so the recursive calls to write(...) do not call
     * HelpMapper.getMappings() often
     *
     * @param <T> the type of node to store, usually @TOCItem
     * @param node the tree node which holds the TOCItem information
     * @param writer the writer to use to write the details of node
     * @param indent the level of indentation to use when writing
     */
    public static <T> void writeTree(final TreeNode<T> node, final FileWriter writer, final int indent) {
        if (cachedHelpMappings == null) {
            cachedHelpMappings = HelpMapper.getMappings();
        }

        write(node, writer, indent);
    }

    /**
     * Write tree to the file writer provided. Recurses through children.
     *
     * @param <T> the type of node to store, usually @TOCItem
     * @param node the tree node which holds the TOCItem information
     * @param writer the writer to use to write the details of node
     * @param indent the level of indentation to use when writing
     */
    private static <T> void write(final TreeNode<T> node, final FileWriter writer, final int indent) {
        final TOCItem item = (TOCItem) (node.getData());
        final TreeNode parentNode = node.getParent();
        final TOCItem parent = parentNode == null ? null : (TOCItem) (node.getParent().getData());

        if (StringUtils.isBlank(item.getTarget())) {
            // when no target, its a normal heading.
            TOCGenerator.writeAccordionItem(writer, item.getText(), item.getText());
        } else {
            final String helpLink = cachedHelpMappings.get(item.getTarget());
            if (StringUtils.isBlank(helpLink)) {
                TOCGenerator.writeItem(writer, item.getText(), indent);
            } else {
                TOCGenerator.writeItem(writer, TOCGenerator.generateHTMLLink(item.getText(), helpLink), indent);
            }
        }

        TOCGenerator.writeText(writer, Platform.NEWLINE);
        if (node.getChildren().isEmpty()) {
            // Base level nodes with no children get written with no indent
            node.getChildren().forEach(each -> write(each, writer, indent));
        } else {
            // Nodes with children get written with an extra level of indent
            // Write start of div which will hold children of current TOC Item
            if (!item.getText().equals(Generator.ROOT_NODE_NAME)) {
                final String id = item.getText().replace(StringUtils.SPACE, StringUtils.EMPTY).replace("/", "");
                final String div = "<div id=\"" + id + "\" class=\"collapse\" aria-labelledby=\"" + id + "\" data-parent=\"#" + id + "\"> <div class=\"card-body\">";
                TOCGenerator.writeText(writer, div);

                // Recurse and call same method to write children
                node.getChildren().forEach(each -> write(each, writer, indent + 1));

                // Close div which holds children of current TOC Item
                TOCGenerator.writeText(writer, "</a> </div> </div> </div>");
            } else {
                // Recurse and call same method to write children
                node.getChildren().forEach(each -> write(each, writer, indent + 1));
            }

        }
    }

    /**
     * Search for the TOCItem findItem which is matched based on the data.
     * Searches using the root of the tree as searchNode.
     *
     * @param findItem the TOCItem to look for
     * @param searchNode the node to look within
     * @return the node within searchNode that matches nodeToFind
     */
    public static TreeNode search(final TOCItem findItem, final TreeNode searchNode) {
        if (searchNode != null) {
            final TOCItem searchTOC = (TOCItem) (searchNode.getData());
            if (searchTOC != null && searchTOC.equals(findItem)) {
                return searchNode;
            } else {
                TreeNode foundNode = null;
                for (final Object child : searchNode.getChildren()) {
                    if (foundNode == null) {
                        foundNode = search(findItem, (TreeNode) child);
                    }
                }
                return foundNode;
            }
        } else {
            return null;
        }
    }

    /**
     * Equals method only checks to ensure the same data is present. This will
     * not compare parents or children.
     *
     * @param obj the comparison object
     * @return true if the same
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof TreeNode && ((TreeNode) (obj)).getData() != null && (((TreeNode) (obj)).getData().equals(data))) {
            return true;
        } else if (obj != null && obj instanceof TreeNode && ((TreeNode) (obj)).getData() == null && data == null) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.data);
        return hash;
    }
}
