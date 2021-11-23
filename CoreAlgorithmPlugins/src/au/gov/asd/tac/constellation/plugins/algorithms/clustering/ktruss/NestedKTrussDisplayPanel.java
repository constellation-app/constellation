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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author twilight_sparkle
 */
public class NestedKTrussDisplayPanel extends JPanel implements MouseInputListener {

    private final KTrussState state;
    private final Graph graph;

    // Stores the data about relative heights and positions of the rectangles representing connected components.
    // Note that these are not actual pixel values - these are calculated dynamically when this panel is painted in order to respond to resizing etc.
    private int[][] rectangles;
    // The size of the vertical gap in pixels between rectangles representing connected components.
    private static final int COMPONENT_VISUAL_GRAP = 8;
    // The size of the border on each side of the step slider with which this panel is aligned.
    private static final int BORDER_SIZE = 8;
    // The width in pixels of the rectangles representing connected components
    private static final int rectangleWidth = 5;
    // the distance to the left (in pixels) of the step slider tick marks which rectangles will be drawn. Ensures that rectangles are centred on tick marks
    private final int rectangleOffset;
    private int totalNeededHeight;
    private boolean expandHeight = false;
    private static final int PREFERRED_HEIGHT = 500;
    private final Set<Integer> selectedRectangles;
    private Map<Integer, List<Integer>> childMapper;

    public NestedKTrussDisplayPanel(final KTrussState state, final Graph graph) {
        this.state = state;
        this.graph = graph;
        rectangleOffset = rectangleWidth / 2;
        selectedRectangles = new HashSet<>();
        addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {

        if (!state.isNestedTrussesVisible()) {
            return;
        }

        final Graphics2D g2 = (Graphics2D) g;

        // Draw each rectangle.
        for (int i = 0; i < rectangles.length; i++) {
            if (selectedRectangles.contains(i)) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.blue);
            }
            g2.fillRect(rectX(i), rectY(i), rectangleWidth, rectHeight(i));
        }
    }

    public void selectRectangles(final int k) {
        selectedRectangles.clear();
        for (int i = 0; i < rectangles.length; i++) {
            if (state.getKTrussFromIndex(rectangles[i][0]) == k) {
                addToSelection(i);
            }
        }
        repaint();
    }

    // Returns the x position on this panel of the given rectangle.
    private int rectX(final int rectangleNum) {
        if (rectangles[rectangleNum][0] == 0) {
            return BORDER_SIZE - rectangleOffset;
        }
        // We do not want to draw on the border around the step slider
        final int sliderLabeledWidth = getWidth() - BORDER_SIZE * 2;
        // The column on which to draw. We subtract 2 because the first column is actually the 3-truss column etc.
        final int columnNumber = state.getKTrussFromIndex(rectangles[rectangleNum][0]) - 2;
        // thte total number of columns in the slider. We subtract 2 as above.
        final int totalColumns = state.getHighestK() - 2;
        // To find the X position, we simply divide the width on which we want to draw by the total columns and multiply
        // by the column number we want to draw on. We then shift across to account for the border
        return ((sliderLabeledWidth * columnNumber) / totalColumns) + (BORDER_SIZE - rectangleOffset);
    }

    // Returns the y position on this panel of the given rectangle.
    private int rectY(final int rectangleNum) {
        final int y = rectangles[rectangleNum][2];
        return expandHeight ? (y * PREFERRED_HEIGHT) / totalNeededHeight : y;
    }

    // Returns the height in this panel of the given rectangle
    private int rectHeight(final int rectangleNum) {
        final int h = rectangles[rectangleNum][1];
        return expandHeight ? (h * PREFERRED_HEIGHT) / totalNeededHeight : h;
    }

    // Calculates the relative sizes and positions of the rectangles based on the connected component information stored in the
    // KTrussState. Note that this method does not calculate actual pixel values, but the relative information stored in the
    // rectangles field. See the declaration for more information
    public void calculateRectangles() {
        Map<Integer, Integer> numDescendants;
        childMapper = new HashMap<>();
        numDescendants = new HashMap<>();

        // Calculate the children of each component.
        for (int i = state.getNumComponents() - 1; i >= 0; i--) {
            // Get the parent (and its list of children) of the current component
            final int parent = state.getComponentParent(i);
            List<Integer> childList = childMapper.get(parent);

            // These components are not K-trusses.
            if (parent == i) {
                childMapper.put(i, null);
                // These components are 3-Trusses with parents that are not K-Trusses.
            } else if (state.getComponentParent(parent) == parent) {
                List<Integer> selfChildList = childMapper.get(i);
                if (selfChildList == null) {
                    selfChildList = new ArrayList<>();
                }
                selfChildList.add(i);
                childMapper.put(i, selfChildList);

                if (numDescendants.get(i) == null) {
                    numDescendants.put(i, 0);
                }
                // These components are K-Trusses for K > 3.
            } else {
                if (childList == null) {
                    childList = new ArrayList<>();
                }
                childList.add(i);
                childMapper.put(parent, childList);
                if (numDescendants.get(i) == null) {
                    numDescendants.put(i, 0);
                }
                final int currentDescendants = numDescendants.get(i) + (numDescendants.get(parent) == null ? 0 : numDescendants.get(parent));
                numDescendants.put(parent, currentDescendants + 1);
            }
        }

        int totalGaps = 0;
        for (final int n : numDescendants.values()) {
            totalGaps += (n == 0) ? 0 : (n - 1);
        }
        totalNeededHeight = state.getTotalVertsInTrusses() + (totalGaps * COMPONENT_VISUAL_GRAP);
        if (totalNeededHeight < PREFERRED_HEIGHT) {
            expandHeight = true;
        }
//        System.out.println(totalNeededHeight);
        final Dimension size = new Dimension(getWidth(), expandHeight ? PREFERRED_HEIGHT : totalNeededHeight);
        setSize(size);

        // Each rectangle will be of the form: {rectangle column, rectangle relative height, rectangle relative y-position}
        // All 'relative' values are integers between 0 and 1000 where 1000 represents the full vertical size of this display panel.
        rectangles = new int[state.getNumComponents()][];

        // The cumulative height of the first column - used to determine the relative y-position of components in the frist column
        int firstColumnHeight = 0;

        // calculate the rectangle details for each component
        for (int i = 0; i < state.getNumComponents(); i++) {

            //If we see a top level graph component which is not a k-truss, give it dummy values of 0 height and x/y positions.
            final List<Integer> childList = childMapper.get(i);
            if (childList == null) {
                if (state.getComponentParent(i) == i) {
                    final int[] rect = {0, 0, 0};
                    rectangles[i] = rect;
                }
                continue;
            }

            // If the component has itself as a child, determine its height as the ratio of its size to the total number of vertices in trusses
            if (childList.contains(i)) {
                final int gaps = Math.max(0, numDescendants.get(i) - 1);
                final int height = state.getComponentSize(i) + (gaps * COMPONENT_VISUAL_GRAP);
                // column number, height, ypos
                final int[] rect = {1, height, firstColumnHeight};
                // Increase the cumulative height of the first column
                firstColumnHeight += (height + COMPONENT_VISUAL_GRAP);
                rectangles[i] = rect;
            }

            // Initialise the variables for position and size calculation of the children of the current component
            final Iterator<Integer> childIter = childList.iterator();
            // The cumulative height of the child column - used to determine the y-position of child components relative to the current component.
            int childColumnHeight = 0;

            // For each child of the current component, calculate its rectangle based on the the current component's rectangle.
            while (childIter.hasNext()) {
                final int child = childIter.next();
                if (child == i) {
                    continue;
                }
                // Calculate the height of the child based on the number of vertices it contains and the number of required gaps
                final int childGaps = Math.max(0, (numDescendants.get(child) - 1));
                final int childHeight = state.getComponentSize(child) + (childGaps * COMPONENT_VISUAL_GRAP);
                // column number, height, ypos
                final int[] rect = {rectangles[i][0] + 1, childHeight, rectangles[i][2] + childColumnHeight};
//                System.out.println("x" + (rectangles[i][0] + 3) + "-truss : " + childHeight + " : " + (rectangles[i][2] + childColumnHeight));
                rectangles[child] = rect;
                // Increase the cumulative height of this child column
                childColumnHeight += (childHeight + COMPONENT_VISUAL_GRAP);
            }
        }
    }

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
        final int x = e.getX();
        final int y = e.getY();
        if (!e.isControlDown()) {
            selectedRectangles.clear();
        }
        // For each rectangle, check whether the mouse hit it, and if so, select the component corresponding to that rectangle
        for (int i = 0; i < rectangles.length; i++) {
            final int rectX = rectX(i);
            final int rectY = rectY(i);
            final int rectHeight = rectHeight(i);
            if (x >= rectX && x <= rectX + rectangleWidth && y >= rectY && y <= rectY + rectHeight) {
                final int parent = state.getComponentParent(i);
                if (e.isControlDown() && selectedRectangles.contains(parent) && parent != i) {
                    return;
                }
                if (e.isControlDown() && selectedRectangles.contains(i)) {
                    removeFromSelection(i);
                    selectComponent(i, -1);
                } else {
                    addToSelection(i);
                    selectComponent(i, e.isControlDown() ? 1 : 0);
                }
                repaint();
                return;
            }
        }
        repaint();
    }

    private void removeFromSelection(final int i) {
        selectedRectangles.remove(i);
        final List<Integer> childList = childMapper.get(i);
        if (childList == null) {
            return;
        }
        for (final int child : childList) {
            if (child != i) {
                removeFromSelection(child);
            }
        }
    }

    private void addToSelection(final int i) {
        selectedRectangles.add(i);
        final List<Integer> childList = childMapper.get(i);
        if (childList == null) {
            return;
        }
        for (final int child : childList) {
            if (child != i) {
                addToSelection(child);
            }
        }
    }

    private void selectComponent(final int componentNum, final int selectionMode) {
        final NestedKTrussDisplayPanel.Select select = new NestedKTrussDisplayPanel.Select(state, componentNum, selectionMode);
        PluginExecution.withPlugin(select).interactively(true).executeLater(graph);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Override required for MouseInputListener, intentionally left blank
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.SELECT})
    // Used to select the vertices contained in a component represented by a rectangle when the user clicks on that rectangle
    private static final class Select extends SimpleEditPlugin {

        private final KTrussState state;
        private final int componentNum;
        private final int selectionMode;

        public Select(final KTrussState state, final int componentNum, final int selectionMode) {
            this.state = state;
            this.componentNum = componentNum;
            this.selectionMode = selectionMode;
        }

        @Override
        public String getName() {
            return "K-Truss: Select Component";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final int txSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);
            final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);

            // Update the selection of the graph's nodes
            for (int i = 0; i < graph.getVertexCount(); i++) {
                // Determine whether we should select or deselect the current node based on whether it lies in the component of interest.
                final int vxID = graph.getVertex(i);

                if (state.isNodeInComponent(vxID, componentNum)) {
                    graph.setBooleanValue(vxSelectedAttr, vxID, selectionMode >= 0);
                } else if (selectionMode == 0) {
                    graph.setBooleanValue(vxSelectedAttr, vxID, false);
                } else {
                    // Do nothing
                }
            }

            // Update the selection of the graph's links
            for (int i = 0; i < graph.getLinkCount(); i++) {
                // Determine whether we should select or deselect all of the current link's transactions based on whether it lies in the component of interest.
                final int lnID = graph.getLink(i);
                if (state.isLinkInComponent(lnID, componentNum)) {
                    for (int txPos = 0; txPos < graph.getLinkTransactionCount(lnID); txPos++) {
                        final int txID = graph.getLinkTransaction(lnID, txPos);
                        graph.setBooleanValue(txSelectedAttr, txID, selectionMode >= 0);
                    }
                } else if (selectionMode == 0) {
                    for (int txPos = 0; txPos < graph.getLinkTransactionCount(lnID); txPos++) {
                        final int txID = graph.getLinkTransaction(lnID, txPos);
                        graph.setBooleanValue(txSelectedAttr, txID, false);
                    }
                } else {
                    // Do nothing
                }
            }
        }
    }
}
