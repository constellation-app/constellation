/*
 * Copyright 2010-2020 Australian Signals Directorate
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
    private final int lineThickness = 1;
    private final int progressBarThickness = 3;
    public int neededHeight = 0;
    private int stepLimit = 0;
    private double xseparation;
    private final int minimum_yseparation = 3;
    private int progress_x;
    private final JScrollPane scrollManager;
    private final HierarchicalControllerTopComponent controller;

    public NestedHierarchicalDisplayPanel(HierarchicalControllerTopComponent controller, JScrollPane scrollManager) {
        this.controller = controller;
        this.scrollManager = scrollManager;
        fitToScrollManager();
    }

    public void setState(HierarchicalState state) {
        this.state = state;
        if (state != null) {
            stepLimit = state.steps > 10 ? (int) Math.floor(state.steps * 1.1) : state.steps + 1;
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
        progress_x = (state.currentStep * getWidth()) / (stepLimit == 0 ? 1 : stepLimit);

        for (LinePositioning line : lines) {
            GroupTreeNode node = line.n;
            while (node.mergeStep <= state.currentStep) {
                node = node.parent;
            }
            line.color = node.color;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (lines == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(new Color(44, 44, 44));
        g2.clearRect(0, 0, getWidth(), getHeight());

        // Draw each line.
        g2.setColor(Color.blue);
        for (int i = 0; i < lines.size(); i++) {
            g2.setColor(lines.get(i).color);
            // horizontal line
            g2.fillRect(HORIZONTAL_BORDER_SIZE, VERTICAL_GAP + lines.get(i).ystart, lines.get(i).xstop, lineThickness);
            // vertical line
            g2.fillRect(HORIZONTAL_BORDER_SIZE + lines.get(i).xstop, VERTICAL_GAP + lines.get(i).ystop, lineThickness, lines.get(i).ystart - lines.get(i).ystop + lineThickness);
        }

        g2.setColor(new Color(0xB0, 0xB0, 0xB0));
        int arrowTop = scrollManager.getViewport().getViewPosition().y;
        int arrowBottom = arrowTop + Math.min(neededHeight, scrollManager.getViewport().getExtentSize().height);
        int[] xpoints = {progress_x - 6, progress_x + 8, progress_x + 1};
        int[] ypoints_top = {arrowTop, arrowTop, arrowTop + 10};
        int[] ypoints_bottom = {arrowBottom, arrowBottom, arrowBottom - 10};
        g2.fillPolygon(xpoints, ypoints_top, 3);
        g2.fillPolygon(xpoints, ypoints_bottom, 3);
        g2.setColor(new Color(0xB0, 0xB0, 0xB0, 130));
        g2.fillRect(progress_x, 0, progressBarThickness, neededHeight);
    }

    private static class GroupTreeNode implements Comparable<GroupTreeNode> {

        public final int vertNum;
        public final int mergeStep; // will determine x end of horizontal line
        public final Color color;
        public SortedSet<GroupTreeNode> children;
        public GroupTreeNode parent;
        public int childSpan; // will determine height of vertical line

        public GroupTreeNode(int vertNum, int mergeStep, Color color) {
            this.vertNum = vertNum;
            this.mergeStep = mergeStep;
            this.color = color;
        }

        @Override
        public int compareTo(GroupTreeNode other) {
            return mergeStep == other.mergeStep ? compare(vertNum, other.vertNum) : compare(mergeStep, other.mergeStep);
        }

        // TODO: copied from JDK1.7 so compareTo() will work in JDK1.6 - fix when possible.
        public static int compare(int x, int y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    }

    private static class LinePositioning {

        public int xstop, ystart, ystop;
        public Color color;
        public GroupTreeNode n;
    }

    // Calculates the relative sizes and positions of the rectangles based on the connected component information stored in the KTrussState.
    // Note that this method does not calculate actual pixel values, but the relative information stored in the rectangles field. See the declaration for more information
    public void calculateLines() {
        if (state == null) {
            lines = null;
            sortedNodes = null;
            return;
        }

        int numOfVerts = state.groups.length;

        List<GroupTreeNode> treeRoots = new LinkedList<>();
        Map<Integer, GroupTreeNode> treeNodes = new HashMap<>();

        // Build a tree of children, calculating nodes' x positions
        for (int i = 0; i < numOfVerts; i++) {
            FastNewman.Group node = state.groups[i];
            if (node == null) {
                continue;
            }
            int nodeMergeStep = node.mergeStep == Integer.MAX_VALUE ? stepLimit : node.mergeStep;

            GroupTreeNode treeNode = treeNodes.get(node.vertex);
            if (treeNode == null) {
                treeNode = new GroupTreeNode(node.vertex, nodeMergeStep, node.color.getJavaColor());
                treeNodes.put(node.vertex, treeNode);
            }

            FastNewman.Group parentGroup = node.parent;
            if (parentGroup != null) {
                int parentVertex = parentGroup.vertex;
                int parentMergeStep = parentGroup.mergeStep == Integer.MAX_VALUE ? stepLimit : parentGroup.mergeStep;
                if (!treeNodes.containsKey(parentVertex)) {
                    GroupTreeNode parentTreeNode = new GroupTreeNode(parentVertex, parentMergeStep, parentGroup.color.getJavaColor());
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
        for (GroupTreeNode node : treeRoots) {
            if (!sortedNodes.contains(node)) {
                addNodeAndAllChildren(sortedNodes, 0, node);
            }
        }

        positionLines();

    }

    // Calculate the relative positioning of the lines in the diagram
    public void positionLines() {
        if (sortedNodes == null) {
            return;
        }

        lines = new LinkedList<>();
        final int prefHeight = scrollManager.getHeight();
        final boolean verticalScrolling = prefHeight < sortedNodes.size() * minimum_yseparation + (VERTICAL_GAP * 2);
        neededHeight = verticalScrolling ? sortedNodes.size() * minimum_yseparation + (VERTICAL_GAP * 2) : prefHeight;
        setSize(getWidth(), neededHeight);
        int yseparation = verticalScrolling ? minimum_yseparation : (int) Math.floor(prefHeight / ((double) sortedNodes.size() + 1));
        setPreferredSize(new Dimension(getWidth(), neededHeight));

        // We do not want to draw on the border around the step slider
        final int sliderLabeledWidth = getWidth() - HORIZONTAL_BORDER_SIZE * 2;
        xseparation = sliderLabeledWidth / (double) stepLimit;

        for (int i = 0; i < sortedNodes.size(); i++) {
            GroupTreeNode node = sortedNodes.get(i);
            LinePositioning r = new LinePositioning();
            r.xstop = (int) Math.floor(node.mergeStep * xseparation);
            r.ystart = i * yseparation;
            r.ystop = (i - node.childSpan) * yseparation;
            r.n = node;
            lines.add(r);
        }
        fitToScrollManager();
        updateColorsAndBar();
    }

    private int addNodeAndAllChildren(List<GroupTreeNode> sortedNodes, int spanSoFar, GroupTreeNode node) {
        sortedNodes.add(node);
        node.childSpan = spanSoFar;
        int numDescendants = 0;
        if (node.children != null) {
            for (GroupTreeNode child : node.children) {
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
        int dpWidth = scrollManager.getWidth() - scrollManager.getVerticalScrollBar().getWidth();
        setSize(dpWidth, neededHeight);
        setPreferredSize(new Dimension(dpWidth, neededHeight));
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }
    private boolean leftClickDown = false;

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            leftClickDown = true;
            int x = Math.max(0, e.getX());
            state.currentStep = Math.min(state.steps, (x * stepLimit) / getWidth());
            controller.updateSlider();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (leftClickDown) {
            int x = Math.max(0, e.getPoint().x);
            state.currentStep = Math.min(state.steps, (x * stepLimit) / getWidth());
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
