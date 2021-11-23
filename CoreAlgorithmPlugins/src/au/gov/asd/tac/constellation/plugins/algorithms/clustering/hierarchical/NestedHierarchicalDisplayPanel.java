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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author twilight_sparkle
 */
public class NestedHierarchicalDisplayPanel extends JPanel implements ComponentListener, MouseInputListener {

    private HierarchicalState state;
    // Stores the data about relative heights and positions of the rectangles representing connected components.
    // Note that these are not actual pixel values - these are calculated dynamically when this panel is painted in order to respond to resizing etc.
    private List<LinePositioning> lines;
    private List<GroupTreeNode> sortedNodes;
    // The size of the border on each side of the step slider with which this panel is aligned.
    private static final int HORIZONTAL_BORDER_SIZE = 0;
    private static final int VERTICAL_GAP = 4;
    // The width in pixels of the rectangles representing connected components
    private static final int LINE_THICKNESS = 1;
    private static final int PROGRESS_BAR_THICKNESS = 3;
    private int neededHeight = 0;
    private int stepLimit = 0;
    private static final int MINIMUM_Y_SEPARATION = 3;
    private int progressX;
    private final JScrollPane scrollManager;
    private final HierarchicalControllerTopComponent controller;

    public NestedHierarchicalDisplayPanel(final HierarchicalControllerTopComponent controller, final JScrollPane scrollManager) {
        this.controller = controller;
        this.scrollManager = scrollManager;
        fitToScrollManager();
    }

    public void setState(final HierarchicalState state) {
        this.state = state;
        if (state != null) {
            stepLimit = state.getSteps() > 10 ? (int) Math.floor(state.getSteps() * 1.1) : state.getSteps() + 1;
            if (getMouseListeners().length == 0) {
                addMouseListener(this);
                addMouseMotionListener(this);
            }
            scrollManager.addComponentListener(this);
        } else {
            removeMouseListener(this);
            removeMouseMotionListener(this);
            scrollManager.removeComponentListener(this);
        }
        calculateLines();
    }

    public void updateColorsAndBar() {
        progressX = (state.getCurrentStep() * getWidth()) / (stepLimit == 0 ? 1 : stepLimit);

        for (final LinePositioning line : lines) {
            GroupTreeNode node = line.n;
            while (node != null && node.mergeStep <= state.getCurrentStep()) {
                node = node.parent;
            }
            if (node == null) {
                return;
            }
            line.color = node.color;
        }
    }

    @Override
    public void paintComponent(final Graphics g) {
        if (lines == null) {
            return;
        }

        final Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(new Color(44, 44, 44));
        g2.clearRect(0, 0, getWidth(), getHeight());

        // Draw each line.
        g2.setColor(Color.blue);
        for (int i = 0; i < lines.size(); i++) {
            g2.setColor(lines.get(i).color);
            // horizontal line
            g2.fillRect(HORIZONTAL_BORDER_SIZE, VERTICAL_GAP + lines.get(i).ystart, lines.get(i).xstop, LINE_THICKNESS);
            // vertical line
            g2.fillRect(HORIZONTAL_BORDER_SIZE + lines.get(i).xstop, VERTICAL_GAP + lines.get(i).ystop, LINE_THICKNESS, lines.get(i).ystart - lines.get(i).ystop + LINE_THICKNESS);
        }

        g2.setColor(new Color(0xB0, 0xB0, 0xB0));
        final int arrowTop = scrollManager.getViewport().getViewPosition().y;
        final int arrowBottom = arrowTop + Math.min(neededHeight, scrollManager.getViewport().getExtentSize().height);
        final int[] xpoints = {progressX - 6, progressX + 8, progressX + 1};
        final int[] ypoints_top = {arrowTop, arrowTop, arrowTop + 10};
        final int[] ypoints_bottom = {arrowBottom, arrowBottom, arrowBottom - 10};
        g2.fillPolygon(xpoints, ypoints_top, 3);
        g2.fillPolygon(xpoints, ypoints_bottom, 3);
        g2.setColor(new Color(0xB0, 0xB0, 0xB0, 130));
        g2.fillRect(progressX, 0, PROGRESS_BAR_THICKNESS, neededHeight);
    }

    private static class GroupTreeNode implements Comparable<GroupTreeNode> {

        private final int vertNum;
        private final int mergeStep; // will determine x end of horizontal line
        private final Color color;
        private SortedSet<GroupTreeNode> children;
        private GroupTreeNode parent;
        private int childSpan; // will determine height of vertical line

        public GroupTreeNode(final int vertNum, final int mergeStep, final Color color) {
            this.vertNum = vertNum;
            this.mergeStep = mergeStep;
            this.color = color;
        }

        @Override
        public int compareTo(final GroupTreeNode other) {
            return mergeStep == other.mergeStep ? compare(vertNum, other.vertNum) : compare(mergeStep, other.mergeStep);
        }

        // TODO: copied from JDK1.7 so compareTo() will work in JDK1.6 - fix when possible.
        public static int compare(final int x, final int y) {
            if (x < y) {
                return -1;
            }
            return (x == y) ? 0 : 1;
        }
    }

    private static class LinePositioning {

        private int xstop;
        private int ystart;
        private int ystop;
        private Color color;
        private GroupTreeNode n;
    }

    // Calculates the relative sizes and positions of the rectangles based on the connected component information stored in the KTrussState.
    // Note that this method does not calculate actual pixel values, but the relative information stored in the rectangles field. See the declaration for more information
    public void calculateLines() {
        if (state == null) {
            lines = null;
            sortedNodes = null;
            return;
        }

        final int numOfVerts = state.getGroups().length;

        final List<GroupTreeNode> treeRoots = new LinkedList<>();
        final Map<Integer, GroupTreeNode> treeNodes = new HashMap<>();

        // Build a tree of children, calculating nodes' x positions
        for (int i = 0; i < numOfVerts; i++) {
            final FastNewman.Group node = state.getGroups()[i];
            if (node == null) {
                continue;
            }
            final int nodeMergeStep = node.getMergeStep() == Integer.MAX_VALUE ? stepLimit : node.getMergeStep();

            GroupTreeNode treeNode = treeNodes.get(node.getVertex());
            if (treeNode == null) {
                treeNode = new GroupTreeNode(node.getVertex(), nodeMergeStep, node.getColor().getJavaColor());
                treeNodes.put(node.getVertex(), treeNode);
            }

            final FastNewman.Group parentGroup = node.getParent();
            if (parentGroup != null) {
                final int parentVertex = parentGroup.getVertex();
                final int parentMergeStep = parentGroup.getMergeStep() == Integer.MAX_VALUE ? stepLimit : parentGroup.getMergeStep();
                if (!treeNodes.containsKey(parentVertex)) {
                    final GroupTreeNode parentTreeNode = new GroupTreeNode(parentVertex, parentMergeStep, parentGroup.getColor().getJavaColor());
                    treeNodes.put(parentVertex, parentTreeNode);
                }
                if (treeNodes.get(parentVertex).children == null) {
                    treeNodes.get(parentVertex).children = new TreeSet<>();
                }
                treeNode.parent = treeNodes.get(parentVertex);
                treeNodes.get(parentVertex).children.add(treeNode);
            } else {
                treeRoots.add(treeNode);
            }
        }

        // Calculate the nodes' y positions and heights.
        sortedNodes = new LinkedList<>();
        for (final GroupTreeNode node : treeRoots) {
            if (!sortedNodes.contains(node)) {
                addNodeAndAllChildren(sortedNodes, 0, node);
            }
        }

        positionLines();

    }

    // Calculate the relative positioning of the lines in the diagram
    public void positionLines() {
        double xseparation;
        if (sortedNodes == null) {
            return;
        }

        lines = new LinkedList<>();
        final int prefHeight = scrollManager.getHeight();
        final boolean verticalScrolling = prefHeight < sortedNodes.size() * MINIMUM_Y_SEPARATION + (VERTICAL_GAP * 2);
        neededHeight = verticalScrolling ? sortedNodes.size() * MINIMUM_Y_SEPARATION + (VERTICAL_GAP * 2) : prefHeight;
        setSize(getWidth(), neededHeight);
        final int yseparation = verticalScrolling ? MINIMUM_Y_SEPARATION : (int) Math.floor(prefHeight / ((double) sortedNodes.size() + 1));
        setPreferredSize(new Dimension(getWidth(), neededHeight));

        // We do not want to draw on the border around the step slider
        final int sliderLabeledWidth = getWidth() - HORIZONTAL_BORDER_SIZE * 2;
        xseparation = sliderLabeledWidth / (double) stepLimit;

        for (int i = 0; i < sortedNodes.size(); i++) {
            final GroupTreeNode node = sortedNodes.get(i);
            final LinePositioning r = new LinePositioning();
            r.xstop = (int) Math.floor(node.mergeStep * xseparation);
            r.ystart = i * yseparation;
            r.ystop = (i - node.childSpan) * yseparation;
            r.n = node;
            lines.add(r);
        }
        fitToScrollManager();
        updateColorsAndBar();
    }

    private int addNodeAndAllChildren(final List<GroupTreeNode> sortedNodes, final int spanSoFar, final GroupTreeNode node) {
        sortedNodes.add(node);
        node.childSpan = spanSoFar;
        int numDescendants = 0;
        if (node.children != null) {
            for (final GroupTreeNode child : node.children) {
                if (child.mergeStep != 0) {
                    numDescendants++;
                    numDescendants += addNodeAndAllChildren(sortedNodes, numDescendants, child);
                }
            }
        }
        return numDescendants;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        positionLines();
        repaint();
    }

    private void fitToScrollManager() {
        final int dpWidth = scrollManager.getWidth() - scrollManager.getVerticalScrollBar().getWidth();
        setSize(dpWidth, neededHeight);
        setPreferredSize(new Dimension(dpWidth, neededHeight));
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // Override required for ComponentListener, intentionally left blank
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // Override required for ComponentListener, intentionally left blank
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // Override required for ComponentListener, intentionally left blank
    }
    private boolean leftClickDown = false;

    @Override
    public void mouseEntered(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClickDown = true;
            final int x = Math.max(0, e.getX());
            state.setCurrentStep(Math.min(state.getSteps(), (x * stepLimit) / getWidth()));
            controller.updateSlider();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (leftClickDown) {
            final int x = Math.max(0, e.getPoint().x);
            state.setCurrentStep(Math.min(state.getSteps(), (x * stepLimit) / getWidth()));
            controller.updateSlider();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClickDown = false;
        }
    }
}
