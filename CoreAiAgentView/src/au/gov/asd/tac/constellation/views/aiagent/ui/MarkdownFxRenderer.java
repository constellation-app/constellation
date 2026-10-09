/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.ThematicBreak;
import org.commonmark.parser.Parser;

/**
 * Renders Markdown as native JavaFX nodes: TextFlows for paragraphs and
 * headings, monospace blocks for code and GridPanes for (GitHub style) tables.
 * Links are shown as text; nothing is fetched or opened.
 *
 * @author constellation
 */
public final class MarkdownFxRenderer {

    private static final Parser PARSER = Parser.builder().build();
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?\\s*$");

    private MarkdownFxRenderer() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Render Markdown.
     *
     * @param markdown the Markdown text.
     * @return a node containing the rendered content.
     */
    public static VBox render(final String markdown) {
        final VBox root = new VBox(6);
        root.getStyleClass().add("agent-md");
        if (markdown == null || markdown.isBlank()) {
            return root;
        }
        for (final Segment segment : split(markdown)) {
            if (segment.table()) {
                root.getChildren().add(renderTable(segment.lines()));
            } else {
                final org.commonmark.node.Node document = PARSER.parse(String.join("\n", segment.lines()));
                final BlockRenderer renderer = new BlockRenderer(root);
                document.accept(renderer);
            }
        }
        return root;
    }

    /**
     * A run of lines that is either a table or other Markdown.
     */
    record Segment(boolean table, List<String> lines) {
    }

    /**
     * Split Markdown into table and non-table segments. CommonMark itself has
     * no tables, so GitHub style tables are found here: a header row, a
     * separator row of dashes, then rows, all delimited by '|'.
     */
    static List<Segment> split(final String markdown) {
        final String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        final List<Segment> segments = new ArrayList<>();
        List<String> current = new ArrayList<>();
        boolean inFence = false;
        int i = 0;
        while (i < lines.length) {
            final String line = lines[i];
            if (line.trim().startsWith("```") || line.trim().startsWith("~~~")) {
                inFence = !inFence;
            }
            if (!inFence && line.contains("|") && i + 1 < lines.length && TABLE_SEPARATOR.matcher(lines[i + 1]).matches()
                    && lines[i + 1].contains("-")) {
                if (!current.isEmpty()) {
                    segments.add(new Segment(false, current));
                    current = new ArrayList<>();
                }
                final List<String> table = new ArrayList<>();
                table.add(line);
                table.add(lines[i + 1]);
                i += 2;
                while (i < lines.length && lines[i].contains("|") && !lines[i].isBlank()) {
                    table.add(lines[i]);
                    i++;
                }
                segments.add(new Segment(true, table));
                continue;
            }
            current.add(line);
            i++;
        }
        if (!current.isEmpty()) {
            segments.add(new Segment(false, current));
        }
        return segments;
    }

    static List<String> cells(final String row) {
        String trimmed = row.trim();
        if (trimmed.startsWith("|")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("|") && !trimmed.endsWith("\\|")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        final List<String> cells = new ArrayList<>();
        for (final String cell : trimmed.split("(?<!\\\\)\\|", -1)) {
            cells.add(cell.trim().replace("\\|", "|"));
        }
        return cells;
    }

    private static Node renderTable(final List<String> lines) {
        final GridPane grid = new GridPane();
        grid.getStyleClass().add("md-table");
        final List<String> header = cells(lines.get(0));
        for (int c = 0; c < header.size(); c++) {
            grid.add(cell(header.get(c), true), c, 0);
        }
        for (int r = 2; r < lines.size(); r++) {
            final List<String> row = cells(lines.get(r));
            for (int c = 0; c < header.size(); c++) {
                grid.add(cell(c < row.size() ? row.get(c) : "", false), c, r - 1);
            }
        }
        grid.setMaxWidth(Double.MAX_VALUE);
        return grid;
    }

    private static Node cell(final String markdown, final boolean header) {
        final TextFlow flow = new TextFlow();
        final org.commonmark.node.Node document = PARSER.parse(markdown);
        document.accept(new InlineRenderer(flow, new ArrayList<>()));
        final StackPane pane = new StackPane(flow);
        pane.getStyleClass().add("md-cell");
        if (header) {
            pane.getStyleClass().add("md-header-cell");
        }
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.setMaxHeight(Double.MAX_VALUE);
        return pane;
    }

    private static Text styled(final String text, final List<String> styles) {
        final Text node = new Text(text);
        node.getStyleClass().addAll(styles);
        return node;
    }

    /**
     * Renders block level nodes into a VBox.
     */
    private static final class BlockRenderer extends AbstractVisitor {

        private final VBox container;

        BlockRenderer(final VBox container) {
            this.container = container;
        }

        @Override
        public void visit(final Paragraph paragraph) {
            container.getChildren().add(inline(paragraph, List.of()));
        }

        @Override
        public void visit(final Heading heading) {
            container.getChildren().add(inline(heading, List.of("md-h" + Math.min(3, heading.getLevel()))));
        }

        @Override
        public void visit(final BulletList list) {
            container.getChildren().add(listBox(list, false, 1));
        }

        @Override
        public void visit(final OrderedList list) {
            container.getChildren().add(listBox(list, true, list.getMarkerStartNumber() == null ? 1 : list.getMarkerStartNumber()));
        }

        @Override
        public void visit(final FencedCodeBlock code) {
            container.getChildren().add(codeBlock(code.getLiteral()));
        }

        @Override
        public void visit(final IndentedCodeBlock code) {
            container.getChildren().add(codeBlock(code.getLiteral()));
        }

        @Override
        public void visit(final HtmlBlock html) {
            // shown as code; HTML is never interpreted
            container.getChildren().add(codeBlock(html.getLiteral()));
        }

        @Override
        public void visit(final BlockQuote quote) {
            final VBox box = new VBox(4);
            box.getStyleClass().add("md-quote");
            visitChildrenInto(quote, box);
            container.getChildren().add(box);
        }

        @Override
        public void visit(final ThematicBreak thematicBreak) {
            container.getChildren().add(new Separator());
        }

        private Node listBox(final org.commonmark.node.Node list, final boolean ordered, final int start) {
            final VBox box = new VBox(3);
            int number = start;
            for (org.commonmark.node.Node item = list.getFirstChild(); item != null; item = item.getNext()) {
                if (item instanceof ListItem) {
                    final Label bullet = new Label(ordered ? number++ + "." : "•");
                    bullet.getStyleClass().add("md-bullet");
                    final VBox content = new VBox(3);
                    visitChildrenInto(item, content);
                    HBox.setHgrow(content, Priority.ALWAYS);
                    final HBox row = new HBox(4, bullet, content);
                    box.getChildren().add(row);
                }
            }
            return box;
        }

        private static void visitChildrenInto(final org.commonmark.node.Node parent, final VBox target) {
            final BlockRenderer renderer = new BlockRenderer(target);
            for (org.commonmark.node.Node child = parent.getFirstChild(); child != null; child = child.getNext()) {
                child.accept(renderer);
            }
        }

        private static Node codeBlock(final String literal) {
            final String text = literal.endsWith("\n") ? literal.substring(0, literal.length() - 1) : literal;
            final TextFlow flow = new TextFlow(new Text(text));
            final StackPane pane = new StackPane(flow);
            pane.getStyleClass().add("md-code-block");
            return pane;
        }

        private static TextFlow inline(final org.commonmark.node.Node block, final List<String> styles) {
            final TextFlow flow = new TextFlow();
            final InlineRenderer renderer = new InlineRenderer(flow, new ArrayList<>(styles));
            for (org.commonmark.node.Node child = block.getFirstChild(); child != null; child = child.getNext()) {
                child.accept(renderer);
            }
            return flow;
        }
    }

    /**
     * Renders inline nodes into a TextFlow.
     */
    private static final class InlineRenderer extends AbstractVisitor {

        private final TextFlow flow;
        private final List<String> styles;

        InlineRenderer(final TextFlow flow, final List<String> styles) {
            this.flow = flow;
            this.styles = styles;
        }

        @Override
        public void visit(final org.commonmark.node.Text text) {
            flow.getChildren().add(styled(text.getLiteral(), styles));
        }

        @Override
        public void visit(final Code code) {
            final List<String> codeStyles = new ArrayList<>(styles);
            codeStyles.add("md-code");
            flow.getChildren().add(styled(code.getLiteral(), codeStyles));
        }

        @Override
        public void visit(final Emphasis emphasis) {
            withStyle("md-italic", emphasis);
        }

        @Override
        public void visit(final StrongEmphasis strong) {
            withStyle("md-bold", strong);
        }

        @Override
        public void visit(final Link link) {
            // links are shown, never followed
            withStyle("md-link", link);
        }

        @Override
        public void visit(final SoftLineBreak softLineBreak) {
            flow.getChildren().add(styled(" ", styles));
        }

        @Override
        public void visit(final HardLineBreak hardLineBreak) {
            flow.getChildren().add(styled("\n", styles));
        }

        @Override
        public void visit(final HtmlInline html) {
            flow.getChildren().add(styled(html.getLiteral(), styles));
        }

        @Override
        public void visit(final Paragraph paragraph) {
            // inside table cells
            visitChildren(paragraph);
        }

        private void withStyle(final String style, final org.commonmark.node.Node node) {
            styles.add(style);
            visitChildren(node);
            styles.remove(styles.size() - 1);
        }
    }
}
