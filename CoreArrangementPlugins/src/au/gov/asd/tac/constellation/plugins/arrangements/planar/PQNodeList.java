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
import java.util.NoSuchElementException;

/**
 * PQ Node List
 *
 * @author twilight_sparkle
 */
class PQNodeList implements Iterable<PQNode> {

    protected static class ListItem {

        private PQNode node;
        private ListItem next;
        private ListItem prev;

        private ListItem(final PQNode node) {
            this(null, node, null);
        }

        private ListItem(final ListItem prev, final PQNode node, final ListItem next) {
            this.node = node;
            this.next = next;
            this.prev = prev;
        }

        protected PQNode getNode() {
            return node;
        }

        protected ListItem getNext() {
            return next;
        }

        protected ListItem getPrev() {
            return prev;
        }
        
    }

    private ListItem first;
    private ListItem last;
    private int size;

    public PQNodeList() {
        first = last = null;
        size = 0;
    }

    protected ListItem getFirst() {
        return first;
    }

    protected ListItem getLast() {
        return last;
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
}
