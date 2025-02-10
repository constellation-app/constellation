/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import java.util.Iterator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * PQ Node Test.
 *
 * @author twilight_sparkle
 */
public class PQNodeNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    @Test
    public void testAdd() {
        PQNode node = new PQNode(NodeType.PNODE);
        PQNode child = new PQNode(NodeType.PNODE);
        PQNode fullChild1 = new PQNode(NodeType.PNODE);
        PQNode fullChild2 = new PQNode(NodeType.PNODE);
        PQNode grandChild = new PQNode(NodeType.PNODE);
        PQNode grandChild2 = new PQNode(NodeType.PNODE);

        child.setLabel(NodeLabel.EMPTY);
        child.setNumLeafDescendants(1);
        fullChild1.setLabel(NodeLabel.FULL);
        fullChild1.setNumLeafDescendants(1);
        fullChild2.setLabel(NodeLabel.FULL);
        fullChild2.setNumLeafDescendants(1);
        grandChild.setLabel(NodeLabel.EMPTY);
        grandChild.setNumLeafDescendants(2);
        grandChild2.setLabel(NodeLabel.EMPTY);
        grandChild2.setNumLeafDescendants(1);

        // Test that a new node has no children
        assertEquals(node.children.getSize(), 0);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 0);

        Iterator<PQNode> iter = node.children.iterator();
        assertFalse(iter.hasNext());

        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test adding a child
        node.addChild(child);

        // Check parent metrics
        assertEquals(node.children.getSize(), 1);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 1);

        // Check list of children
        iter = node.children.iterator();
        assertEquals(iter.next(), child);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child));
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(child.children.getSize(), 0);
        assertEquals(child.getParent(), node);
        assertEquals(child.getNumLeafDescendants(), 1);
        assertFalse(child.children.iterator().hasNext());

        // Test adding a second child with a different label
        node.addChild(fullChild1);

        // Check parent metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 2);

        // Check lists of children
        iter = node.children.iterator();
        assertEquals(iter.next(), child);
        assertEquals(iter.next(), fullChild1);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(fullChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(fullChild1.children.getSize(), 0);
        assertEquals(fullChild1.getParent(), node);
        assertEquals(fullChild1.getNumLeafDescendants(), 1);
        assertFalse(fullChild1.children.iterator().hasNext());

        // Test adding a child with the same label to the beginning of the list
        node.addFirstChild(fullChild2);

        // Check parent metrics
        assertEquals(node.children.getSize(), 3);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 3);

        // Check lists of children
        iter = node.children.iterator();
        assertEquals(iter.next(), fullChild2);
        assertEquals(iter.next(), child);
        assertEquals(iter.next(), fullChild1);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 2);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(fullChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(fullChild2));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(fullChild2.children.getSize(), 0);
        assertEquals(fullChild2.getParent(), node);
        assertEquals(fullChild2.getNumLeafDescendants(), 1);
        assertFalse(fullChild2.children.iterator().hasNext());

        // Test adding a grandchild
        child.addFirstChild(grandChild);

        // Check grandparent metrics
        assertEquals(node.children.getSize(), 3);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 5);

        // Check parent metrics
        assertEquals(child.children.getSize(), 1);
        assertEquals(child.getParent(), node);
        assertEquals(child.getNumLeafDescendants(), 3);

        // Check list of children
        iter = child.children.iterator();
        assertEquals(iter.next(), grandChild);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(child.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild));
        assertTrue(child.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(child.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(grandChild.children.getSize(), 0);
        assertEquals(grandChild.getParent(), child);
        assertEquals(grandChild.getNumLeafDescendants(), 2);
        assertFalse(grandChild.children.iterator().hasNext());

        // Test adding a second grandchild
        child.addChild(grandChild2);

        // Check grandparent metrics
        assertEquals(node.children.getSize(), 3);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 6);

        // Check parent metrics
        assertEquals(child.children.getSize(), 2);
        assertEquals(child.getParent(), node);
        assertEquals(child.getNumLeafDescendants(), 4);

        // Check list of children
        iter = child.children.iterator();
        assertEquals(iter.next(), grandChild);
        assertEquals(iter.next(), grandChild2);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(child.labeledChildren.get(NodeLabel.EMPTY).size(), 2);
        assertTrue(child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild));
        assertTrue(child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild2));
        assertTrue(child.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(child.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(grandChild2.children.getSize(), 0);
        assertEquals(grandChild2.getParent(), child);
        assertEquals(grandChild2.getNumLeafDescendants(), 1);
        assertFalse(grandChild2.children.iterator().hasNext());
    }

    @Test
    public void testRemove() {
        PQNode node = new PQNode(NodeType.PNODE);
        PQNode child = new PQNode(NodeType.PNODE);
        PQNode fullChild1 = new PQNode(NodeType.PNODE);
        PQNode fullChild2 = new PQNode(NodeType.PNODE);
        PQNode grandChild = new PQNode(NodeType.PNODE);
        PQNode grandChild2 = new PQNode(NodeType.PNODE);

        child.setLabel(NodeLabel.EMPTY);
        child.setNumLeafDescendants(1);
        fullChild1.setLabel(NodeLabel.FULL);
        fullChild1.setNumLeafDescendants(1);
        fullChild2.setLabel(NodeLabel.FULL);
        fullChild2.setNumLeafDescendants(1);
        grandChild.setLabel(NodeLabel.EMPTY);
        grandChild.setNumLeafDescendants(2);
        grandChild2.setLabel(NodeLabel.EMPTY);
        grandChild2.setNumLeafDescendants(1);

        // Build same structure as in testAdd()
        node.addChild(child);
        node.addChild(fullChild1);
        node.addFirstChild(fullChild2);
        child.addChild(grandChild);
        child.addChild(grandChild2);

        // Test removing a child
        node.removeChild(fullChild1);

        // Check parent metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 5);

        // Check list of children
        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), fullChild2);
        assertEquals(iter.next(), child);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(fullChild2));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test removing a grandchild
        child.removeChild(grandChild2);

        // Check grandparent metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 4);

        // Check parent metrics
        assertEquals(child.children.getSize(), 1);
        assertEquals(child.getParent(), node);
        assertEquals(child.getNumLeafDescendants(), 3);

        // Check list of children
        iter = child.children.iterator();
        assertEquals(iter.next(), grandChild);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(child.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(child.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild));
        assertTrue(child.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(child.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test removing a child with a grandchild
        node.removeChild(child);

        // Check parent metrics
        assertEquals(node.children.getSize(), 1);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 1);

        // Check lists of children
        iter = node.children.iterator();
        assertEquals(iter.next(), fullChild2);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(fullChild2));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test removing the last child
        node.removeChild(fullChild2);

        // Check parent metrics
        assertEquals(node.children.getSize(), 0);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 0);

        // Check lists of children
        iter = node.children.iterator();
        assertFalse(iter.hasNext());

        // Check sets of children
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());
    }

    @Test
    public void testReplace() {
        final PQNode node = new PQNode(NodeType.PNODE);
        final PQNode child1 = new PQNode(NodeType.PNODE);
        final PQNode child2 = new PQNode(NodeType.PNODE);
        final PQNode replacementChild1 = new PQNode(NodeType.PNODE);
        final PQNode replacementChild2 = new PQNode(NodeType.PNODE);
        final PQNode grandChild = new PQNode(NodeType.PNODE);
        final PQNode grandChild2 = new PQNode(NodeType.PNODE);
        final PQNode replacementGrandChild = new PQNode(NodeType.PNODE);

        child1.setLabel(NodeLabel.EMPTY);
        replacementChild1.setLabel(NodeLabel.FULL);
        child2.setLabel(NodeLabel.FULL);
        replacementChild2.setLabel(NodeLabel.EMPTY);
        grandChild.setLabel(NodeLabel.EMPTY);
        grandChild2.setLabel(NodeLabel.FULL);
        replacementGrandChild.setLabel(NodeLabel.PARTIAL);

        child1.setNumLeafDescendants(1);
        replacementChild1.setNumLeafDescendants(2);
        child2.setNumLeafDescendants(1);
        replacementChild2.setNumLeafDescendants(3);
        grandChild.setNumLeafDescendants(2);
        grandChild2.setNumLeafDescendants(1);
        replacementGrandChild.setNumLeafDescendants(0);

        // Build the beginning structure
        node.addChild(child1);
        node.addChild(child2);
        child2.addChild(grandChild);
        replacementChild2.addChild(grandChild2);

        // Test replacing a leaf node a child
        node.replaceChild(child1, replacementChild1);

        // Check parent metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 5);

        // Check list of children
        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), replacementChild1);
        assertEquals(iter.next(), child2);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 2);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(replacementChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child2));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(replacementChild1.children.getSize(), 0);
        assertEquals(replacementChild1.getParent(), node);
        assertEquals(replacementChild1.getNumLeafDescendants(), 2);
        assertFalse(replacementChild1.children.iterator().hasNext());

        // Test replacing a leaf node grandchild
        child2.replaceChild(grandChild, replacementGrandChild);

        // Check grandparent metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 3);

        // Check parent metrics
        assertEquals(child2.children.getSize(), 1);
        assertEquals(child2.getParent(), node);
        assertEquals(child2.getNumLeafDescendants(), 1);

        // Check list of children
        iter = child2.children.iterator();
        assertEquals(iter.next(), replacementGrandChild);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertTrue(child2.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertTrue(child2.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertEquals(child2.labeledChildren.get(NodeLabel.PARTIAL).size(), 1);
        assertTrue(child2.labeledChildren.get(NodeLabel.PARTIAL).contains(replacementGrandChild));

        // Check child metrics
        assertEquals(replacementGrandChild.children.getSize(), 0);
        assertEquals(replacementGrandChild.getParent(), child2);
        assertEquals(replacementGrandChild.getNumLeafDescendants(), 0);
        assertFalse(replacementGrandChild.children.iterator().hasNext());

        // Test replacing an internal child
        node.replaceChild(child2, replacementChild2);

        // Check parent metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 6);

        // Check lists of children
        iter = node.children.iterator();
        assertEquals(iter.next(), replacementChild1);
        assertEquals(iter.next(), replacementChild2);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(replacementChild2));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(replacementChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check child metrics
        assertEquals(replacementChild2.children.getSize(), 1);
        assertEquals(replacementChild2.getParent(), node);
        assertEquals(replacementChild2.getNumLeafDescendants(), 4);
    }

    @Test
    public void testReverse() {
        final PQNode node = new PQNode(NodeType.PNODE);
        final PQNode child1 = new PQNode(NodeType.PNODE);
        final PQNode child2 = new PQNode(NodeType.PNODE);
        final PQNode grandChild1 = new PQNode(NodeType.PNODE);
        final PQNode grandChild2 = new PQNode(NodeType.PNODE);

        child1.setLabel(NodeLabel.EMPTY);
        child1.setNumLeafDescendants(1);
        child2.setLabel(NodeLabel.FULL);
        child2.setNumLeafDescendants(1);
        grandChild1.setLabel(NodeLabel.EMPTY);
        grandChild1.setNumLeafDescendants(2);
        grandChild2.setLabel(NodeLabel.EMPTY);
        grandChild2.setNumLeafDescendants(0);

        node.addChild(child1);
        node.addChild(child2);
        child2.addChild(grandChild1);
        child2.addChild(grandChild2);

        //Test reversing the parent
        node.reverseChildren();

        // Check node metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 4);

        // Check list of children
        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), child2);
        assertEquals(iter.next(), child1);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child1));
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child2));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test reversing a child with no children
        child1.reverseChildren();

        // Check node metrics
        assertEquals(child1.children.getSize(), 0);
        assertEquals(child1.getParent(), node);
        assertEquals(child1.getNumLeafDescendants(), 1);

        // Check list of children
        iter = child1.children.iterator();
        assertFalse(iter.hasNext());

        // Check sets of children
        assertTrue(child1.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertTrue(child1.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(child1.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test reversing a child with children
        child2.reverseChildren();

        // Check node metrics
        assertEquals(child2.children.getSize(), 2);
        assertEquals(child2.getParent(), node);
        assertEquals(child2.getNumLeafDescendants(), 3);

        // Check list of grandparentchildren
        iter = child2.children.iterator();
        assertEquals(iter.next(), grandChild2);
        assertEquals(iter.next(), grandChild1);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(child2.labeledChildren.get(NodeLabel.EMPTY).size(), 2);
        assertTrue(child2.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild1));
        assertTrue(child2.labeledChildren.get(NodeLabel.EMPTY).contains(grandChild2));
        assertTrue(child2.labeledChildren.get(NodeLabel.FULL).isEmpty());
        assertTrue(child2.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());
    }

    @Test
    public void testConcatenate() {
        final PQNode node = new PQNode(NodeType.PNODE);
        final PQNode child1 = new PQNode(NodeType.PNODE);
        final PQNode child2 = new PQNode(NodeType.PNODE);
        final PQNode child3 = new PQNode(NodeType.PNODE);
        final PQNode child2grandChild1 = new PQNode(NodeType.PNODE);
        final PQNode child2grandChild2 = new PQNode(NodeType.PNODE);
        final PQNode child3grandChild1 = new PQNode(NodeType.PNODE);
        final PQNode child3grandChild2 = new PQNode(NodeType.PNODE);

        child1.setLabel(NodeLabel.EMPTY);
        child1.setNumLeafDescendants(0);
        child2.setLabel(NodeLabel.FULL);
        child2.setNumLeafDescendants(0);
        child3.setLabel(NodeLabel.FULL);
        child3.setNumLeafDescendants(0);
        child2grandChild1.setLabel(NodeLabel.EMPTY);
        child2grandChild1.setNumLeafDescendants(2);
        child2grandChild2.setLabel(NodeLabel.EMPTY);
        child2grandChild2.setNumLeafDescendants(0);
        child3grandChild1.setLabel(NodeLabel.FULL);
        child3grandChild1.setNumLeafDescendants(1);
        child3grandChild2.setLabel(NodeLabel.FULL);
        child3grandChild2.setNumLeafDescendants(0);

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);
        child2.addChild(child2grandChild1);
        child2.addChild(child2grandChild2);
        child3.addChild(child3grandChild1);
        child3.addChild(child3grandChild2);

        // Test concatenating a child in the middle
        child3.concatenateSibling(child2);

        // Check parent node metrics
        assertEquals(node.children.getSize(), 2);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 3);

        // Check list of children
        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child3);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child1));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child3));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check concatenated child metrics
        assertEquals(child3.children.getSize(), 4);
        assertEquals(child3.getParent(), node);
        assertEquals(child3.getNumLeafDescendants(), 3);

        // check concatenated child's list of children
        iter = child3.children.iterator();
        assertEquals(iter.next(), child3grandChild1);
        assertEquals(iter.next(), child3grandChild2);
        assertEquals(iter.next(), child2grandChild1);
        assertEquals(iter.next(), child2grandChild2);
        assertFalse(iter.hasNext());

        // Check concatenated child's sets of children
        assertEquals(child3.labeledChildren.get(NodeLabel.EMPTY).size(), 2);
        assertTrue(child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1));
        assertTrue(child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2));
        assertEquals(child3.labeledChildren.get(NodeLabel.FULL).size(), 2);
        assertTrue(child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild1));
        assertTrue(child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild2));
        assertTrue(child3.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // check grandchildren parent pointers
        assertEquals(child3grandChild1.getParent(), child3);
        assertEquals(child3grandChild2.getParent(), child3);
        assertEquals(child2grandChild1.getParent(), child3);
        assertEquals(child2grandChild2.getParent(), child3);

        // Test concatenating a child at the beginning with no children
        child3.concatenateSibling(child1);

        // Check parent node metrics
        assertEquals(node.children.getSize(), 1);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 3);

        // Check list of children
        iter = node.children.iterator();
        assertEquals(iter.next(), child3);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).isEmpty());
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child3));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Check concatenated child metrics
        assertEquals(child3.children.getSize(), 4);
        assertEquals(child3.getParent(), node);
        assertEquals(child3.getNumLeafDescendants(), 3);

        // check concatenated child's list of children
        iter = child3.children.iterator();
        assertEquals(iter.next(), child3grandChild1);
        assertEquals(iter.next(), child3grandChild2);
        assertEquals(iter.next(), child2grandChild1);
        assertEquals(iter.next(), child2grandChild2);
        assertFalse(iter.hasNext());

        // Check concatenated child's sets of children
        assertEquals(child3.labeledChildren.get(NodeLabel.EMPTY).size(), 2);
        assertTrue(child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1));
        assertTrue(child3.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2));
        assertEquals(child3.labeledChildren.get(NodeLabel.FULL).size(), 2);
        assertTrue(child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild1));
        assertTrue(child3.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild2));
        assertTrue(child3.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());
    }

    @Test
    public void testFlatten() {
        final PQNode node = new PQNode(NodeType.PNODE);
        final PQNode child1 = new PQNode(NodeType.PNODE);
        final PQNode child2 = new PQNode(NodeType.PNODE);
        final PQNode child3 = new PQNode(NodeType.PNODE);
        final PQNode child2grandChild1 = new PQNode(NodeType.PNODE);
        final PQNode child2grandChild2 = new PQNode(NodeType.PNODE);
        final PQNode child3grandChild1 = new PQNode(NodeType.PNODE);
        final PQNode child3grandChild2 = new PQNode(NodeType.PNODE);

        child1.setLabel(NodeLabel.EMPTY);
        child1.setNumLeafDescendants(1);
        child2.setLabel(NodeLabel.FULL);
        child2.setNumLeafDescendants(0);
        child3.setLabel(NodeLabel.FULL);
        child3.setNumLeafDescendants(0);
        child2grandChild1.setLabel(NodeLabel.EMPTY);
        child2grandChild1.setNumLeafDescendants(2);
        child2grandChild2.setLabel(NodeLabel.EMPTY);
        child2grandChild2.setNumLeafDescendants(0);
        child3grandChild1.setLabel(NodeLabel.FULL);
        child3grandChild1.setNumLeafDescendants(1);
        child3grandChild2.setLabel(NodeLabel.FULL);
        child3grandChild2.setNumLeafDescendants(0);

        node.addChild(child1);
        node.addChild(child2);
        node.addChild(child3);
        child2.addChild(child2grandChild1);
        child2.addChild(child2grandChild2);
        child3.addChild(child3grandChild1);
        child3.addChild(child3grandChild2);

        // Test flattening a child in the middle
        node.flatten(child2);

        // Check node metrics
        assertEquals(node.children.getSize(), 4);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 4);

        // Check list of children
        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child2grandChild1);
        assertEquals(iter.next(), child2grandChild2);
        assertEquals(iter.next(), child3);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 3);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child1));
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 1);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child3));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());

        // Test flattening a child at the end
        node.flatten(child3);

        // Check node metrics
        assertEquals(node.children.getSize(), 5);
        assertNull(node.getParent());
        assertEquals(node.getNumLeafDescendants(), 4);

        // Check list of children
        iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child2grandChild1);
        assertEquals(iter.next(), child2grandChild2);
        assertEquals(iter.next(), child3grandChild1);
        assertEquals(iter.next(), child3grandChild2);
        assertFalse(iter.hasNext());

        // Check sets of children
        assertEquals(node.labeledChildren.get(NodeLabel.EMPTY).size(), 3);
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child1));
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.EMPTY).contains(child2grandChild2));
        assertEquals(node.labeledChildren.get(NodeLabel.FULL).size(), 2);
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild1));
        assertTrue(node.labeledChildren.get(NodeLabel.FULL).contains(child3grandChild2));
        assertTrue(node.labeledChildren.get(NodeLabel.PARTIAL).isEmpty());
    }

    @Test
    public void testTrimSinglyPartial() {
        final PQNode child1 = new PQNode(NodeType.PNODE);
        final PQNode child2 = new PQNode(NodeType.PNODE);
        final PQNode child3 = new PQNode(NodeType.PNODE);
        final PQNode child4 = new PQNode(NodeType.PNODE);
        final PQNode[] children = {child1, child2, child3, child4};

        // Test 1 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.EMPTY);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.EMPTY);
        PQNode node = makeQNodeWithChildren(children);

        // Test 1 - test trimming pointer at first node, with deletion immediately after
        node.trimAndFlattenQNodeChildren(child1, false);

        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child3);
        assertFalse(iter.hasNext());

        // Test 2 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.FULL);
        node = makeQNodeWithChildren(children);

        // Test 2 - test trimming pointer at a middle node, with deletion immediately before and after
        node.trimAndFlattenQNodeChildren(child3, false);

        iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child4);
        assertFalse(iter.hasNext());

        // Test 3 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.EMPTY);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.FULL);
        node = makeQNodeWithChildren(children);

        // Test 3 - test trimming pointer at end with deletion immediately before and at pointer
        node.trimAndFlattenQNodeChildren(child4, false);

        iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child2);
        assertFalse(iter.hasNext());

        // Test 4 setup
        child1.setLabel(NodeLabel.FULL);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.EMPTY);
        child4.setLabel(NodeLabel.FULL);
        node = makeQNodeWithChildren(children);

        // Test 4 - test trimming pointer is null, with deletion in the middle somewhere
        node.trimAndFlattenQNodeChildren(null, false);

        iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child2);
        assertEquals(iter.next(), child4);
        assertFalse(iter.hasNext());

        // Test 5 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.EMPTY);
        child4.setLabel(NodeLabel.EMPTY);
        node = makeQNodeWithChildren(children);

        // Test 5 - reverse of test 1
        node.trimAndFlattenQNodeChildren(child3, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), child4);
        assertEquals(iter.next(), child2);
        assertFalse(iter.hasNext());

        // Test 6 setup
        child1.setLabel(NodeLabel.FULL);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.EMPTY);
        node = makeQNodeWithChildren(children);

        // Test 6 - reverse of test 2
        node.trimAndFlattenQNodeChildren(child1, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), child4);
        assertEquals(iter.next(), child1);
        assertFalse(iter.hasNext());

        // Test 7 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.EMPTY);
        child4.setLabel(NodeLabel.EMPTY);
        node = makeQNodeWithChildren(children);

        // Test 7 - reverse of test 3
        node.trimAndFlattenQNodeChildren(null, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), child4);
        assertEquals(iter.next(), child3);
        assertEquals(iter.next(), child1);
        assertFalse(iter.hasNext());

        // Test 8 setup
        child1.setLabel(NodeLabel.FULL);
        child2.setLabel(NodeLabel.EMPTY);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.FULL);
        node = makeQNodeWithChildren(children);

        // Test 8 - reverse of test 4
        node.trimAndFlattenQNodeChildren(child4, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), child4);
        assertEquals(iter.next(), child3);
        assertEquals(iter.next(), child1);
        assertFalse(iter.hasNext());

        // Setup for tests with partial nodes
        final PQNode emptyChild = new PQNode(NodeType.PNODE);
        emptyChild.setLabel(NodeLabel.EMPTY);
        final PQNode fullChild = new PQNode(NodeType.PNODE);
        fullChild.setLabel(NodeLabel.FULL);
        PQNode partialChild = null;
        final PQNode gChild1 = new PQNode(NodeType.PNODE);
        final PQNode gChild2 = new PQNode(NodeType.PNODE);
        final PQNode gChild3 = new PQNode(NodeType.PNODE);
        final PQNode gChild4 = new PQNode(NodeType.PNODE);
        gChild1.setLabel(NodeLabel.EMPTY);
        gChild2.setLabel(NodeLabel.EMPTY);
        gChild3.setLabel(NodeLabel.FULL);
        gChild4.setLabel(NodeLabel.FULL);
        final PQNode[] topLevelChildren = {emptyChild, partialChild, fullChild};
        final PQNode[] topLevelChildrenReverse = {fullChild, partialChild, emptyChild};
        final PQNode[] secondLevelChildren = {gChild1, gChild2, gChild3, gChild4};

        // Test 9 setup
        partialChild = makeQNodeWithChildren(secondLevelChildren);
        partialChild.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[1] = partialChild;
        node = makeQNodeWithChildren(topLevelChildren);

        // Test 9 - test trimming pointer at partial node.
        node.trimAndFlattenQNodeChildren(partialChild, false);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild);
        assertEquals(iter.next(), gChild1);
        assertEquals(iter.next(), gChild2);
        assertEquals(iter.next(), gChild3);
        assertEquals(iter.next(), gChild4);
        assertEquals(iter.next(), fullChild);
        assertFalse(iter.hasNext());

        // Test 10 setup
        partialChild = makeQNodeWithChildren(secondLevelChildren);
        partialChild.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[1] = partialChild;
        node = makeQNodeWithChildren(topLevelChildren);

        // Test 10 - test trimming pointer immediately before partial node.
        node.trimAndFlattenQNodeChildren(emptyChild, false);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild);
        final PQNode next = iter.next();
        assertEquals(next, gChild3);
        assertEquals(iter.next(), gChild4);
        assertEquals(iter.next(), fullChild);
        assertFalse(iter.hasNext());

        // Test 11 setup
        partialChild = makeQNodeWithChildren(secondLevelChildren);
        partialChild.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[1] = partialChild;
        node = makeQNodeWithChildren(topLevelChildren);

        // Test 11 - test trimming pointer immediately after partial node.
        node.trimAndFlattenQNodeChildren(fullChild, false);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild);
        assertEquals(iter.next(), gChild1);
        assertEquals(iter.next(), gChild2);
        assertFalse(iter.hasNext());

        // Test 12 setup
        partialChild = makeQNodeWithChildren(secondLevelChildren);
        partialChild.setLabel(NodeLabel.PARTIAL);
        topLevelChildrenReverse[1] = partialChild;
        node = makeQNodeWithChildren(topLevelChildrenReverse);

        // Test 12 - reverse of test 9.
        node.trimAndFlattenQNodeChildren(partialChild, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild);
        assertEquals(iter.next(), gChild1);
        assertEquals(iter.next(), gChild2);
        assertEquals(iter.next(), gChild3);
        assertEquals(iter.next(), gChild4);
        assertEquals(iter.next(), fullChild);
        assertFalse(iter.hasNext());

        // Test 13 setup
        partialChild = makeQNodeWithChildren(secondLevelChildren);
        partialChild.setLabel(NodeLabel.PARTIAL);
        topLevelChildrenReverse[1] = partialChild;
        node = makeQNodeWithChildren(topLevelChildrenReverse);

        // Test 13 - reverse of test 10.
        node.trimAndFlattenQNodeChildren(emptyChild, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), gChild4);
        assertEquals(iter.next(), gChild3);
        assertEquals(iter.next(), fullChild);
        assertFalse(iter.hasNext());

        // Test 14 setup
        partialChild = makeQNodeWithChildren(secondLevelChildren);
        partialChild.setLabel(NodeLabel.PARTIAL);
        topLevelChildrenReverse[1] = partialChild;
        node = makeQNodeWithChildren(topLevelChildrenReverse);

        // Test 14 - reverse of test 11.
        node.trimAndFlattenQNodeChildren(fullChild, true);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild);
        assertEquals(iter.next(), gChild2);
        assertEquals(iter.next(), gChild1);
        assertEquals(iter.next(), fullChild);
        assertFalse(iter.hasNext());
    }

    @Test
    public void testTrimDoublyPartial() {
        final PQNode child1 = new PQNode(NodeType.PNODE);
        final PQNode child2 = new PQNode(NodeType.PNODE);
        final PQNode child3 = new PQNode(NodeType.PNODE);
        final PQNode child4 = new PQNode(NodeType.PNODE);
        final PQNode child5 = new PQNode(NodeType.PNODE);
        final PQNode[] children = {child1, child2, child3, child4, child5};

        // Test 1 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.EMPTY);
        child4.setLabel(NodeLabel.FULL);
        child5.setLabel(NodeLabel.EMPTY);
        PQNode node = makeQNodeWithChildren(children);

        // Test 1 - test trimming pointers so nodes inside are removed
        node.trimAndFlattenQNodeChildren(child2, child5);

        Iterator<PQNode> iter = node.children.iterator();
        assertEquals(iter.next(), child1);
        assertEquals(iter.next(), child2);
        assertEquals(iter.next(), child4);
        assertEquals(iter.next(), child5);
        assertFalse(iter.hasNext());

        // Test 2 setup
        child1.setLabel(NodeLabel.FULL);
        child2.setLabel(NodeLabel.EMPTY);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.FULL);
        child5.setLabel(NodeLabel.FULL);
        node = makeQNodeWithChildren(children);

        // Test 2 - test trimming pointers so nodes at and outside pointers are removed
        node.trimAndFlattenQNodeChildren(child2, child5);

        iter = node.children.iterator();
        assertEquals(iter.next(), child3);
        assertEquals(iter.next(), child4);
        assertFalse(iter.hasNext());

        // Test 3 setup
        child1.setLabel(NodeLabel.EMPTY);
        child2.setLabel(NodeLabel.FULL);
        child3.setLabel(NodeLabel.FULL);
        child4.setLabel(NodeLabel.EMPTY);
        child5.setLabel(NodeLabel.FULL);
        node = makeQNodeWithChildren(children);

        // Test 3 - test trimming pointers spanning whole node
        node.trimAndFlattenQNodeChildren(child1, null);

        iter = node.children.iterator();
        assertEquals(iter.next(), child2);
        assertEquals(iter.next(), child3);
        assertEquals(iter.next(), child5);
        assertFalse(iter.hasNext());

        // Setup for tests with partial nodes
        final PQNode emptyChild1 = new PQNode(NodeType.PNODE);
        emptyChild1.setLabel(NodeLabel.EMPTY);
        final PQNode emptyChild2 = new PQNode(NodeType.PNODE);
        emptyChild2.setLabel(NodeLabel.EMPTY);
        final PQNode fullChild = new PQNode(NodeType.PNODE);
        fullChild.setLabel(NodeLabel.FULL);
        PQNode partialChild1 = null;
        PQNode partialChild2 = null;
        final PQNode g1Child1 = new PQNode(NodeType.PNODE);
        final PQNode g1Child2 = new PQNode(NodeType.PNODE);
        final PQNode g1Child3 = new PQNode(NodeType.PNODE);
        final PQNode g1Child4 = new PQNode(NodeType.PNODE);
        final PQNode g2Child1 = new PQNode(NodeType.PNODE);
        final PQNode g2Child2 = new PQNode(NodeType.PNODE);
        final PQNode g2Child3 = new PQNode(NodeType.PNODE);
        final PQNode g2Child4 = new PQNode(NodeType.PNODE);
        g1Child1.setLabel(NodeLabel.EMPTY);
        g1Child2.setLabel(NodeLabel.EMPTY);
        g1Child3.setLabel(NodeLabel.FULL);
        g1Child4.setLabel(NodeLabel.FULL);
        g2Child1.setLabel(NodeLabel.EMPTY);
        g2Child2.setLabel(NodeLabel.EMPTY);
        g2Child3.setLabel(NodeLabel.FULL);
        g2Child4.setLabel(NodeLabel.FULL);
        final PQNode[] topLevelChildren = {emptyChild1, partialChild1, fullChild, partialChild2, emptyChild2};
        final PQNode[] secondLevelChildren1 = {g1Child1, g1Child2, g1Child3, g1Child4};
        final PQNode[] secondLevelChildren2 = {g2Child1, g2Child2, g2Child3, g2Child4};

        // Test 4 setup
        partialChild1 = makeQNodeWithChildren(secondLevelChildren1);
        partialChild1.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[1] = partialChild1;
        partialChild2 = makeQNodeWithChildren(secondLevelChildren2);
        partialChild2.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[3] = partialChild2;
        node = makeQNodeWithChildren(topLevelChildren);

        // Test 4 - test trimming pointers at partial nodes.
        node.trimAndFlattenQNodeChildren(partialChild1, partialChild2);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild1);
        assertEquals(iter.next(), g1Child1);
        assertEquals(iter.next(), g1Child2);
        assertEquals(iter.next(), g1Child3);
        assertEquals(iter.next(), g1Child4);
        assertEquals(iter.next(), fullChild);
        assertEquals(iter.next(), g2Child4);
        assertEquals(iter.next(), g2Child3);
        assertEquals(iter.next(), g2Child2);
        assertEquals(iter.next(), g2Child1);
        assertEquals(iter.next(), emptyChild2);
        assertFalse(iter.hasNext());

        // Test 5 setup
        partialChild1 = makeQNodeWithChildren(secondLevelChildren1);
        partialChild1.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[1] = partialChild1;
        partialChild2 = makeQNodeWithChildren(secondLevelChildren2);
        partialChild2.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[3] = partialChild2;
        node = makeQNodeWithChildren(topLevelChildren);

        // Test 5 - test trimming pointers around first partial node.
        node.trimAndFlattenQNodeChildren(emptyChild1, fullChild);

        iter = node.children.iterator();
        assertEquals(iter.next(), g1Child3);
        assertEquals(iter.next(), g1Child4);
        assertEquals(iter.next(), g2Child1);
        assertEquals(iter.next(), g2Child2);
        assertEquals(iter.next(), emptyChild2);
        assertFalse(iter.hasNext());

        // Test 6 setup
        partialChild1 = makeQNodeWithChildren(secondLevelChildren1);
        partialChild1.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[1] = partialChild1;
        partialChild2 = makeQNodeWithChildren(secondLevelChildren2);
        partialChild2.setLabel(NodeLabel.PARTIAL);
        topLevelChildren[3] = partialChild2;
        node = makeQNodeWithChildren(topLevelChildren);

        // Test 6 - test trimming pointers around second partial node.
        node.trimAndFlattenQNodeChildren(fullChild, emptyChild2);

        iter = node.children.iterator();
        assertEquals(iter.next(), emptyChild1);
        assertEquals(iter.next(), g1Child1);
        assertEquals(iter.next(), g1Child2);
        assertEquals(iter.next(), fullChild);
        assertEquals(iter.next(), g2Child3);
        assertEquals(iter.next(), g2Child4);
        assertEquals(iter.next(), emptyChild2);
        assertFalse(iter.hasNext());
    }
    
    private PQNode makeQNodeWithChildren(final PQNode[] children) {
        final PQNode node = new PQNode(NodeType.QNODE);
        for (final PQNode child : children) {
            node.addChild(child);
        }
        return node;
    }
}
