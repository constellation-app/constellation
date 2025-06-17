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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import au.gov.asd.tac.constellation.plugins.arrangements.planar.PQNodeList.ListItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * PQ Node List Test.
 *
 * @author twilight_sparkle
 */
public class PQNodeListNGTest {

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
        final PQNodeList list = new PQNodeList();
        final PQNode node1 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node2 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node3 = new PQNode(NodeType.LEAF_NODE);

        // Test that the list is initially empty
        assertEquals(list.getSize(), 0);
        assertNull(list.getFirst());
        assertNull(list.getLast());

        // Test adding a first node
        list.addFirst(node1);

        assertEquals(list.getSize(), 1);
        ListItem first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getNext());

        assertNull(first.getPrev());
        assertEquals(list.getLast(), first);

        // Test adding a second node to the beginning
        list.addFirst(node2);

        assertEquals(list.getSize(), 2);
        first = list.getFirst();
        assertEquals(first.getNode(), node2);
        assertNull(first.getPrev());
        ListItem second = first.getNext();
        assertEquals(second.getNode(), node1);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(list.getLast(), second);

        // Test adding a third node to the end
        list.addLast(node3);

        assertEquals(list.getSize(), 3);
        first = list.getFirst();
        assertEquals(first.getNode(), node2);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), node1);
        assertEquals(second.getPrev(), first);
        ListItem third = second.getNext();
        assertEquals(third.getNode(), node3);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);
    }

    @Test
    public void testRemove() {
        final PQNodeList list = new PQNodeList();
        final PQNode node1 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node2 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node3 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node4 = new PQNode(NodeType.LEAF_NODE);

        list.addLast(node1);
        list.addLast(node2);
        list.addLast(node3);
        list.addLast(node4);

        // Test removing from the middle
        list.remove(node2);

        assertEquals(list.getSize(), 3);
        ListItem first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        ListItem second = first.getNext();
        assertEquals(second.getNode(), node3);
        assertEquals(second.getPrev(), first);
        ListItem third = second.getNext();
        assertEquals(third.getNode(), node4);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);

        // Test removing from the end
        list.remove(node4);

        assertEquals(list.getSize(), 2);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), node3);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(list.getLast(), second);

        // Test removing from the beginning
        list.remove(node1);

        assertEquals(list.getSize(), 1);
        first = list.getFirst();
        assertEquals(first.getNode(), node3);
        assertNull(first.getPrev());

        assertNull(first.getNext());
        assertEquals(list.getLast(), first);

        // Test removing non-existent item
        list.remove(node2);

        assertEquals(list.getSize(), 1);
        first = list.getFirst();
        assertEquals(first.getNode(), node3);
        assertNull(first.getPrev());

        assertNull(first.getNext());
        assertEquals(list.getLast(), first);

        // Test removing the last item
        list.remove(node3);

        assertEquals(list.getSize(), 0);
        assertNull(list.getFirst());
        assertNull(list.getLast());
    }

    @Test
    public void testReplace() {
        final PQNodeList list = new PQNodeList();
        final PQNode node1 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node2 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node3 = new PQNode(NodeType.LEAF_NODE);
        final PQNode replacement1 = new PQNode(NodeType.LEAF_NODE);
        final PQNode replacement2 = new PQNode(NodeType.LEAF_NODE);
        final PQNode replacement3 = new PQNode(NodeType.LEAF_NODE);

        list.addLast(node1);
        list.addLast(node2);
        list.addLast(node3);

        // Test replacing in the middle
        list.replace(node2, replacement2);

        assertEquals(list.getSize(), 3);
        ListItem first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        ListItem second = first.getNext();
        assertEquals(second.getNode(), replacement2);
        assertEquals(second.getPrev(), first);
        ListItem third = second.getNext();
        assertEquals(third.getNode(), node3);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);

        // Test replacing at the end
        list.replace(node3, replacement3);

        assertEquals(list.getSize(), 3);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), replacement2);
        assertEquals(second.getPrev(), first);
        third = second.getNext();
        assertEquals(third.getNode(), replacement3);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);

        // Test replacing at the beginning
        list.replace(node1, replacement1);

        assertEquals(list.getSize(), 3);
        first = list.getFirst();
        assertEquals(first.getNode(), replacement1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), replacement2);
        assertEquals(second.getPrev(), first);
        third = second.getNext();
        assertEquals(third.getNode(), replacement3);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);
    }

    @Test
    public void testReverse() {
        final PQNodeList list = new PQNodeList();
        final PQNode node1 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node2 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node3 = new PQNode(NodeType.LEAF_NODE);

        // Test reversing an empty list
        list.reverse();

        assertEquals(list.getSize(), 0);
        assertNull(list.getFirst());
        assertNull(list.getLast());

        // Test reversing a single element list
        list.addFirst(node1);
        list.reverse();

        assertEquals(list.getSize(), 1);
        ListItem first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getNext());

        assertNull(first.getPrev());
        assertEquals(list.getLast(), first);

        // Test reversing a list with two elements
        list.addFirst(node2);
        list.reverse();

        assertEquals(list.getSize(), 2);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        ListItem second = first.getNext();
        assertEquals(second.getNode(), node2);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(list.getLast(), second);

        // Test reversing a list with three elements
        list.addLast(node3);
        list.reverse();

        assertEquals(list.getSize(), 3);
        first = list.getFirst();
        assertEquals(first.getNode(), node3);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), node2);
        assertEquals(second.getPrev(), first);
        ListItem third = second.getNext();
        assertEquals(third.getNode(), node1);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);
    }

    @Test
    public void testConcatenate() {
        final PQNodeList list = new PQNodeList();
        final PQNodeList list2 = new PQNodeList();
        final PQNodeList empty = new PQNodeList();
        final PQNode node1 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node2 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node3 = new PQNode(NodeType.LEAF_NODE);
        final PQNode node4 = new PQNode(NodeType.LEAF_NODE);

        list.addLast(node1);
        list.addLast(node2);
        list2.addLast(node3);
        list2.addLast(node4);

        // test concatenation with empty list
        list.concatenate(empty);

        assertEquals(list.getSize(), 2);
        ListItem first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        ListItem second = first.getNext();
        assertEquals(second.getNode(), node2);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(list.getLast(), second);

        // test concatenation into empty list
        empty.concatenate(list);

        assertEquals(empty.getSize(), 2);
        first = empty.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), node2);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(empty.getLast(), second);

        // test concatenation of two multiple element lists
        list.concatenate(list2);

        assertEquals(list.getSize(), 4);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), node2);
        assertEquals(second.getPrev(), first);
        ListItem third = second.getNext();
        assertEquals(third.getNode(), node3);
        assertEquals(third.getPrev(), second);
        ListItem fourth = third.getNext();
        assertEquals(fourth.getNode(), node4);
        assertEquals(fourth.getPrev(), third);

        assertNull(fourth.getNext());
        assertEquals(list.getLast(), fourth);
    }

    @Test
    public void testFlatten() {
        PQNodeList list = new PQNodeList();
        PQNode node1 = new PQNode(NodeType.LEAF_NODE);
        PQNode node2 = new PQNode(NodeType.LEAF_NODE);
        PQNode node3 = new PQNode(NodeType.LEAF_NODE);
        list.addLast(node1);
        list.addLast(node2);
        list.addLast(node3);

        PQNodeList childList1 = node1.children;
        PQNodeList childList2 = node2.children;
        PQNodeList childList3 = node3.children;

        PQNode child1node1 = new PQNode(NodeType.LEAF_NODE);
        PQNode child1node2 = new PQNode(NodeType.LEAF_NODE);
        PQNode child2node1 = new PQNode(NodeType.LEAF_NODE);
        PQNode child2node2 = new PQNode(NodeType.LEAF_NODE);
        PQNode child3node1 = new PQNode(NodeType.LEAF_NODE);
        PQNode child3node2 = new PQNode(NodeType.LEAF_NODE);

        childList1.addLast(child1node1);
        childList1.addLast(child1node2);
        childList2.addLast(child2node1);
        childList2.addLast(child2node2);
        childList3.addLast(child3node1);
        childList3.addLast(child3node2);

        // test flattening in the middle
        list.flatten(node2);

        assertEquals(list.getSize(), 4);
        ListItem first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        ListItem second = first.getNext();
        assertEquals(second.getNode(), child2node1);
        assertEquals(second.getPrev(), first);
        ListItem third = second.getNext();
        assertEquals(third.getNode(), child2node2);
        assertEquals(third.getPrev(), second);
        ListItem fourth = third.getNext();
        assertEquals(fourth.getNode(), node3);
        assertEquals(fourth.getPrev(), third);

        assertNull(fourth.getNext());
        assertEquals(list.getLast(), fourth);

        // test flattening at the end
        list.flatten(node3);

        assertEquals(list.getSize(), 5);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), child2node1);
        assertEquals(second.getPrev(), first);
        third = second.getNext();
        assertEquals(third.getNode(), child2node2);
        assertEquals(third.getPrev(), second);
        fourth = third.getNext();
        assertEquals(fourth.getNode(), child3node1);
        assertEquals(fourth.getPrev(), third);
        ListItem fifth = fourth.getNext();
        assertEquals(fifth.getNode(), child3node2);
        assertEquals(fifth.getPrev(), fourth);

        assertNull(fifth.getNext());
        assertEquals(list.getLast(), fifth);

        // test flattening at the beginning
        list.flatten(node1);

        assertEquals(list.getSize(), 6);
        first = list.getFirst();
        assertEquals(first.getNode(), child1node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), child1node2);
        assertEquals(second.getPrev(), first);
        third = second.getNext();
        assertEquals(third.getNode(), child2node1);
        assertEquals(third.getPrev(), second);
        fourth = third.getNext();
        assertEquals(fourth.getNode(), child2node2);
        assertEquals(fourth.getPrev(), third);
        fifth = fourth.getNext();
        assertEquals(fifth.getNode(), child3node1);
        assertEquals(fifth.getPrev(), fourth);
        ListItem sixth = fifth.getNext();
        assertEquals(sixth.getNode(), child3node2);
        assertEquals(sixth.getPrev(), fifth);

        assertNull(sixth.getNext());
        assertEquals(list.getLast(), sixth);

        // Reset lists for last two tests
        node1 = new PQNode(NodeType.LEAF_NODE);
        node2 = new PQNode(NodeType.LEAF_NODE);
        node3 = new PQNode(NodeType.LEAF_NODE);
        PQNode child = new PQNode(NodeType.LEAF_NODE);

        PQNodeList singleChildList = node3.children;
        singleChildList.addLast(child);
        list = new PQNodeList();

        list.addLast(node1);
        list.addLast(node2);
        list.addLast(node3);

        // test flattening node with one child
        list.flatten(node3);

        assertEquals(list.getSize(), 3);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), node2);
        assertEquals(second.getPrev(), first);
        third = second.getNext();
        assertEquals(third.getNode(), child);
        assertEquals(third.getPrev(), second);

        assertNull(third.getNext());
        assertEquals(list.getLast(), third);

        // test flattening empty node in middle
        list.flatten(node2);

        assertEquals(list.getSize(), 2);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), child);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(list.getLast(), second);

        // test flattening non-existent node
        list.flatten(node2);

        assertEquals(list.getSize(), 2);
        first = list.getFirst();
        assertEquals(first.getNode(), node1);
        assertNull(first.getPrev());
        second = first.getNext();
        assertEquals(second.getNode(), child);
        assertEquals(second.getPrev(), first);

        assertNull(second.getNext());
        assertEquals(list.getLast(), second);

        // test flattening empty node at the beginning
        list.flatten(node1);

        assertEquals(list.getSize(), 1);
        first = list.getFirst();
        assertEquals(first.getNode(), child);
        assertNull(first.getPrev());

        assertNull(first.getNext());
        assertEquals(list.getLast(), first);
    }
}
