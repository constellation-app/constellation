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
package au.gov.asd.tac.constellation.graph.merger;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.mergers.ConcatenatedSetGraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.undo.UndoGraphEdit;
import org.testng.annotations.Test;

/**
 * Attribute Merger Test.
 *
 * @author sirius
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class AttributeMergerNGTest extends ConstellationTest {

    /**
     * Tests that getting an attribute merger returns the merger that was
     * specified when the attribute was created.
     */
    @Test
    public void attributeMergerSetGetTest() {
        final StoreGraph g = new StoreGraph();
        final int defaultMergerAttributeId = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "defaultMergerAttribute", null, null, GraphAttributeMerger.getDefault().getId());
        final int customMergerAttributeId = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "customMergerAttribute", null, null, ConcatenatedSetGraphAttributeMerger.ID);
        final int noMergerAttributeId = g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "noMergerAttribute", null, null, null);
        assert g.getAttributeMerger(defaultMergerAttributeId) == GraphAttributeMerger.getDefault();
        assert g.getAttributeMerger(customMergerAttributeId) == GraphAttributeMerger.getMergers().get(ConcatenatedSetGraphAttributeMerger.ID);
        assert g.getAttributeMerger(noMergerAttributeId) == null;
    }

    /**
     * Tests that specifying an unregistered attribute merger id results in an
     * IllegalArgumentException.
     */
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void unknownMergerSetTest() {
        final StoreGraph g = new StoreGraph();
        g.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "unknownMergerAttribute", null, null, "UnknownAttributeMergerId");
    }

    /**
     * Tests that that the undo/redo stack will record and then reinstate the
     * correct attribute merger, both when an attribute merger has been
     * specified and when the attribute merger is null.
     */
    @Test
    public void attributeMergerUndoTest() {
        // Create a new graph
        final StoreGraph graph = new StoreGraph();

        // Create a new edit to store the add attribute commands
        final UndoGraphEdit addEdit = new UndoGraphEdit();
        graph.setGraphEdit(addEdit);

        // Add attributes with no, the default, and a custom merger
        int noMergerAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "noMerger", null, null, null);
        int defaultMergerAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "defaultMerger", null, null, GraphAttributeMerger.getDefault().getId());
        int customMergerAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "customMerger", null, null, ConcatenatedSetGraphAttributeMerger.ID);

        // Assert that the attribute mergers are correct
        assert graph.getAttributeMerger(noMergerAttribute) == null;
        assert graph.getAttributeMerger(defaultMergerAttribute) == GraphAttributeMerger.getDefault();
        assert graph.getAttributeMerger(customMergerAttribute) == GraphAttributeMerger.getMergers().get(ConcatenatedSetGraphAttributeMerger.ID);

        // Finish the edit
        graph.setGraphEdit(null);
        addEdit.finish();

        // Undo the edit
        addEdit.undo(graph);

        // Assert that the attributes have been removed
        assert graph.getAttribute(GraphElementType.VERTEX, "noMerger") == Graph.NOT_FOUND;
        assert graph.getAttribute(GraphElementType.VERTEX, "defaultMerger") == Graph.NOT_FOUND;
        assert graph.getAttribute(GraphElementType.VERTEX, "customMerger") == Graph.NOT_FOUND;

        // Redo the edit
        addEdit.execute(graph);

        // Get the attribute ids for each attribute
        noMergerAttribute = graph.getAttribute(GraphElementType.VERTEX, "noMerger");
        defaultMergerAttribute = graph.getAttribute(GraphElementType.VERTEX, "defaultMerger");
        customMergerAttribute = graph.getAttribute(GraphElementType.VERTEX, "customMerger");

        // Assert that the attribute mergers have been restored correctly
        assert graph.getAttributeMerger(noMergerAttribute) == null;
        assert graph.getAttributeMerger(defaultMergerAttribute) == GraphAttributeMerger.getDefault();
        assert graph.getAttributeMerger(customMergerAttribute) == GraphAttributeMerger.getMergers().get(ConcatenatedSetGraphAttributeMerger.ID);

        // Create a new edit to store the remove attribute commands
        final UndoGraphEdit removeEdit = new UndoGraphEdit();
        graph.setGraphEdit(removeEdit);

        // Remove all three attributes
        graph.removeAttribute(noMergerAttribute);
        graph.removeAttribute(defaultMergerAttribute);
        graph.removeAttribute(customMergerAttribute);

        // Finish the edit
        graph.setGraphEdit(null);
        removeEdit.finish();

        // Assert that the attributes have been removed
        assert graph.getAttribute(GraphElementType.VERTEX, "noMerger") == Graph.NOT_FOUND;
        assert graph.getAttribute(GraphElementType.VERTEX, "defaultMerger") == Graph.NOT_FOUND;
        assert graph.getAttribute(GraphElementType.VERTEX, "customMerger") == Graph.NOT_FOUND;

        // Undo the remove edit
        removeEdit.undo(graph);

        // Assert that the attribute mergers have been restored correctly
        assert graph.getAttributeMerger(noMergerAttribute) == null;
        assert graph.getAttributeMerger(defaultMergerAttribute) == GraphAttributeMerger.getDefault();
        assert graph.getAttributeMerger(customMergerAttribute) == GraphAttributeMerger.getMergers().get(ConcatenatedSetGraphAttributeMerger.ID);
    }
}
