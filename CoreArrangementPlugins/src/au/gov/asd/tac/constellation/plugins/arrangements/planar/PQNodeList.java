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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PQ Node List
 *
 * @author twilight_sparkle
 */
class PQNodeList implements Iterable<PQNode> {

    private static class ListItem {

        PQNode node;
        ListItem next;
        ListItem prev;

        private ListItem(final PQNode node) {
            this(null, node, null);
        }

        private ListItem(final ListItem prev, final PQNode node, final ListItem next) {
            this.node = node;
            this.next = next;
            this.prev = prev;
        }
    }

    private ListItem first;
    private ListItem last;
    private int size;

    public PQNodeList() {
        first = last = null;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    private void link(final ListItem left, final ListItem right) {
        if (left != null) {
            left.next = right;
        }
        if (right != null) {
            right.prev = left;
        }
    }

    public void addFirst(final PQNode node) {
        final ListItem item = new ListItem(node);
        if (size == 0) {
            first = last = item;
        } else {
            item.next = first;
            first.prev = item;
            first = item;
        }
        size++;
    }

    public void addLast(final PQNode node) {
        final ListItem item = new ListItem(node);
        if (size == 0) {
            first = last = item;
        } else {
            item.prev = last;
            last.next = item;
            last = item;
        }
        size++;
    }

    public void remove(final PQNode node) {
        for (ListItem item = first; item != null; item = item.next) {
            if (item.node == node) {
                link(item.prev, item.next);
                size--;
                if (item == first) {
                    first = item.next;
                }
                if (item == last) {
                    last = item.prev;
                }
                break;
            }
        }
    }

    public void replace(final PQNode node, final PQNode newNode) {
        final ListItem newItem = new ListItem(newNode);
        for (ListItem item = first; item != null; item = item.next) {
            if (item.node == node) {
                link(item.prev, newItem);
                link(newItem, item.next);
                if (item == first) {
                    first = newItem;
                }
                if (item == last) {
                    last = newItem;
                }
                break;
            }
        }
    }

    // Reverses this list of children, useful when it belongs to a QNode.
    public void reverse() {
        for (ListItem childItem = first; childItem != null; childItem = childItem.prev) {
            final ListItem temp = childItem.next;
            childItem.next = childItem.prev;
            childItem.prev = temp;
        }
        final ListItem temp = first;
        first = last;
        last = temp;
    }

    // appends the apecified list to this list
    // Note that new list items are not created meaning that toConcat will no longer be a valid PQNodeList,
    // which is fine since this should only be called when the PQNode possessing toConcat is being removed.
    public void concatenate(final PQNodeList toConcat) {

        if (toConcat.size == 0) {
            return;
        } else if (size == 0) {
            first = toConcat.first;
            last = toConcat.last;
            size = toConcat.size;
            return;
        } else {
            // Do nothing
        }

        link(last, toConcat.first);

        last = toConcat.last;
        size += toConcat.size;
    }

    // Replaces the specified node in this list with its list of children.
    public void flatten(final PQNode node) {
        if (node.children.size == 0) {
            remove(node);
            return;
        }
        for (ListItem item = first; item != null; item = item.next) {
            if (item.node == node) {
                link(item.prev, node.children.first);
                link(node.children.last, item.next);
                size += (node.children.size - 1);
                if (item == first) {
                    first = node.children.first;
                }
                if (item == last) {
                    last = node.children.last;
                }
                break;
            }
        }
    }

    @Override
    public Iterator<PQNode> iterator() {
        return new Iterator<PQNode>() {

            ListItem current = null;
            ListItem nextItem = first;

            @Override
            public boolean hasNext() {
                return (nextItem != null);
            }

            @Override
            public PQNode next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                current = nextItem;
                nextItem = nextItem.next;
                return current.node;
            }

            @Override
            public void remove() {
                current.next.prev = current.prev;
                current.prev.next = current.next;
            }
        };
    }

    public static class PQNodeListTest {

        private PQNode makeNode() {
            return new PQNode(NodeType.LEAF_NODE);
        }

        public void testAdd() {

            PQNodeList list = new PQNodeList();
            ListItem first;
            ListItem second;
            ListItem third;
            PQNode node1 = makeNode();
            PQNode node2 = makeNode();
            PQNode node3 = makeNode();

            // Test that the list is initially empty
            assert list.size == 0;
            assert list.first == null;
            assert list.last == null;

            // Test adding a first node
            list.addFirst(node1);

            assert list.size == 1;
            first = list.first;
            assert first.node == node1;
            assert first.next == null;

            assert first.prev == null;
            assert list.last == first;

            // Test adding a second node to the beginning
            list.addFirst(node2);

            assert list.size == 2;
            first = list.first;
            assert first.node == node2;
            assert first.prev == null;
            second = first.next;
            assert second.node == node1;
            assert second.prev == first;

            assert second.next == null;
            assert list.last == second;

            // Test adding a third node to the end
            list.addLast(node3);

            assert list.size == 3;
            first = list.first;
            assert first.node == node2;
            assert first.prev == null;
            second = first.next;
            assert second.node == node1;
            assert second.prev == first;
            third = second.next;
            assert third.node == node3;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;
        }

        public void testRemove() {

            PQNodeList list = new PQNodeList();
            ListItem first;
            ListItem second;
            ListItem third;
            PQNode node1 = makeNode();
            PQNode node2 = makeNode();
            PQNode node3 = makeNode();
            PQNode node4 = makeNode();

            list.addLast(node1);
            list.addLast(node2);
            list.addLast(node3);
            list.addLast(node4);

            // Test removing from the middle
            list.remove(node2);

            assert list.size == 3;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node3;
            assert second.prev == first;
            third = second.next;
            assert third.node == node4;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;

            // Test removing from the end
            list.remove(node4);

            assert list.size == 2;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node3;
            assert second.prev == first;

            assert second.next == null;
            assert list.last == second;

            // Test removing from the beginning
            list.remove(node1);

            assert list.size == 1;
            first = list.first;
            assert first.node == node3;
            assert first.prev == null;

            assert first.next == null;
            assert list.last == first;

            // Test removing non-existent item
            list.remove(node2);

            assert list.size == 1;
            first = list.first;
            assert first.node == node3;
            assert first.prev == null;

            assert first.next == null;
            assert list.last == first;

            // Test removing the last item
            list.remove(node3);

            assert list.size == 0;
            assert list.first == null;
            assert list.last == null;
        }

        public void testReplace() {

            PQNodeList list = new PQNodeList();
            ListItem first;
            ListItem second;
            ListItem third;
            PQNode node1 = makeNode();
            PQNode node2 = makeNode();
            PQNode node3 = makeNode();
            PQNode replacement1 = makeNode();
            PQNode replacement2 = makeNode();
            PQNode replacement3 = makeNode();

            list.addLast(node1);
            list.addLast(node2);
            list.addLast(node3);

            // Test replacing in the middle
            list.replace(node2, replacement2);

            assert list.size == 3;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == replacement2;
            assert second.prev == first;
            third = second.next;
            assert third.node == node3;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;

            // Test replacing at the end
            list.replace(node3, replacement3);

            assert list.size == 3;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == replacement2;
            assert second.prev == first;
            third = second.next;
            assert third.node == replacement3;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;

            // Test replacing at the beginning
            list.replace(node1, replacement1);

            assert list.size == 3;
            first = list.first;
            assert first.node == replacement1;
            assert first.prev == null;
            second = first.next;
            assert second.node == replacement2;
            assert second.prev == first;
            third = second.next;
            assert third.node == replacement3;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;

        }

        public void testReverse() {

            PQNodeList list = new PQNodeList();
            ListItem first;
            ListItem second;
            ListItem third;
            PQNode node1 = makeNode();
            PQNode node2 = makeNode();
            PQNode node3 = makeNode();

            // Test reversing an empty list
            list.reverse();

            assert list.size == 0;
            assert list.first == null;
            assert list.last == null;

            // Test reversing a single element list
            list.addFirst(node1);
            list.reverse();

            assert list.size == 1;
            first = list.first;
            assert first.node == node1;
            assert first.next == null;

            assert first.prev == null;
            assert list.last == first;

            // Test reversing a list with two elements
            list.addFirst(node2);
            list.reverse();

            assert list.size == 2;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node2;
            assert second.prev == first;

            assert second.next == null;
            assert list.last == second;

            // Test reversing a list with three elements
            list.addLast(node3);
            list.reverse();

            assert list.size == 3;
            first = list.first;
            assert first.node == node3;
            assert first.prev == null;
            second = first.next;
            assert second.node == node2;
            assert second.prev == first;
            third = second.next;
            assert third.node == node1;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;

        }

        public void testConcatenate() {

            PQNodeList list = new PQNodeList();
            PQNodeList list2 = new PQNodeList();
            PQNodeList empty = new PQNodeList();
            ListItem first;
            ListItem second;
            ListItem third;
            ListItem fourth;
            PQNode node1 = makeNode();
            PQNode node2 = makeNode();
            PQNode node3 = makeNode();
            PQNode node4 = makeNode();

            list.addLast(node1);
            list.addLast(node2);
            list2.addLast(node3);
            list2.addLast(node4);

            // test concatenation with empty list
            list.concatenate(empty);

            assert list.size == 2;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node2;
            assert second.prev == first;

            assert second.next == null;
            assert list.last == second;

            // test concatenation into empty list
            empty.concatenate(list);

            assert empty.size == 2;
            first = empty.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node2;
            assert second.prev == first;

            assert second.next == null;
            assert empty.last == second;

            // test concatenation of two multiple element lists
            list.concatenate(list2);

            assert list.size == 4;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node2;
            assert second.prev == first;
            third = second.next;
            assert third.node == node3;
            assert third.prev == second;
            fourth = third.next;
            assert fourth.node == node4;
            assert fourth.prev == third;

            assert fourth.next == null;
            assert list.last == fourth;

        }

        public void testFlatten() {

            PQNodeList list = new PQNodeList();
            ListItem first;
            ListItem second;
            ListItem third;
            ListItem fourth;
            ListItem fifth;
            ListItem sixth;
            PQNode node1 = makeNode();
            PQNode node2 = makeNode();
            PQNode node3 = makeNode();
            list.addLast(node1);
            list.addLast(node2);
            list.addLast(node3);

            PQNodeList childList1 = node1.children;
            PQNodeList childList2 = node2.children;
            PQNodeList childList3 = node3.children;

            PQNode child1node1 = makeNode();
            PQNode child1node2 = makeNode();
            PQNode child2node1 = makeNode();
            PQNode child2node2 = makeNode();
            PQNode child3node1 = makeNode();
            PQNode child3node2 = makeNode();

            childList1.addLast(child1node1);
            childList1.addLast(child1node2);
            childList2.addLast(child2node1);
            childList2.addLast(child2node2);
            childList3.addLast(child3node1);
            childList3.addLast(child3node2);

            // test flattening in the middle
            list.flatten(node2);

            assert list.size == 4;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == child2node1;
            assert second.prev == first;
            third = second.next;
            assert third.node == child2node2;
            assert third.prev == second;
            fourth = third.next;
            assert fourth.node == node3;
            assert fourth.prev == third;

            assert fourth.next == null;
            assert list.last == fourth;

            // test flattening at the end
            list.flatten(node3);

            assert list.size == 5;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == child2node1;
            assert second.prev == first;
            third = second.next;
            assert third.node == child2node2;
            assert third.prev == second;
            fourth = third.next;
            assert fourth.node == child3node1;
            assert fourth.prev == third;
            fifth = fourth.next;
            assert fifth.node == child3node2;
            assert fifth.prev == fourth;

            assert fifth.next == null;
            assert list.last == fifth;

            // test flattening at the beginning
            list.flatten(node1);

            assert list.size == 6;
            first = list.first;
            assert first.node == child1node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == child1node2;
            assert second.prev == first;
            third = second.next;
            assert third.node == child2node1;
            assert third.prev == second;
            fourth = third.next;
            assert fourth.node == child2node2;
            assert fourth.prev == third;
            fifth = fourth.next;
            assert fifth.node == child3node1;
            assert fifth.prev == fourth;
            sixth = fifth.next;
            assert sixth.node == child3node2;
            assert sixth.prev == fifth;

            assert sixth.next == null;
            assert list.last == sixth;

            // Reset lists for last two tests
            node1 = makeNode();
            node2 = makeNode();
            node3 = makeNode();
            PQNode child = makeNode();

            PQNodeList singleChildList = node3.children;
            singleChildList.addLast(child);
            list = new PQNodeList();

            list.addLast(node1);
            list.addLast(node2);
            list.addLast(node3);

            // test flattening node with one child
            list.flatten(node3);

            assert list.size == 3;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == node2;
            assert second.prev == first;
            third = second.next;
            assert third.node == child;
            assert third.prev == second;

            assert third.next == null;
            assert list.last == third;

            // test flattening empty node in middle
            list.flatten(node2);

            assert list.size == 2;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == child;
            assert second.prev == first;

            assert second.next == null;
            assert list.last == second;

            // test flattening non-existent node
            list.flatten(node2);

            assert list.size == 2;
            first = list.first;
            assert first.node == node1;
            assert first.prev == null;
            second = first.next;
            assert second.node == child;
            assert second.prev == first;

            assert second.next == null;
            assert list.last == second;

            // test flattening empty node at the beginning
            list.flatten(node1);

            assert list.size == 1;
            first = list.first;
            assert first.node == child;
            assert first.prev == null;

            assert first.next == null;
            assert list.last == first;

        }

    }
}
