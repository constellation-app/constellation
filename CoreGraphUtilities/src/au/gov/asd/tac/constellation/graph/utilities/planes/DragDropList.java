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
package au.gov.asd.tac.constellation.graph.utilities.planes;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Plane;
import au.gov.asd.tac.constellation.graph.utilities.planes.DragDropList.MyElement;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * drop down list for selecting planes from a menu context button
 *
 * @author algol
 */
final class DragDropList extends JList<MyElement> {

    private MyListModel model;

    DragDropList() {
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setTransferHandler(new MyListDropHandler(this));
        new MyDragListener(this);

        model = new MyListModel(new String[0]);
    }

    @Override
    public MyListModel getModel() {
        return model;
    }

    @Override
    public void setModel(final ListModel<MyElement> model) {
        this.model = (MyListModel) model;
        super.setModel(model);
    }

    public void setPlanes(final List<Plane> planes, final BitSet visibleLayers) {
        if (planes != null) {
            final String[] labels = new String[planes.size()];
            int i = 0;
            for (Plane plane : planes) {
                labels[i++] = plane.getLabel();
            }

            model = new MyListModel(labels);
            setModel(model);
            setSelectedLayers(visibleLayers);
        } else {
            setModel(new MyListModel(new String[0]));
            setSelectedLayers(new BitSet());
        }
    }

    /**
     * Select the list elements corresponding to the visible layers.
     *
     * @param visibleLayers
     */
    public void setSelectedLayers(final BitSet visibleLayers) {
        final int[] selectedIndices = new int[visibleLayers.cardinality()];
        int ix = 0;
        for (int i = visibleLayers.nextSetBit(0); i >= 0; i = visibleLayers.nextSetBit(i + 1)) {
            selectedIndices[ix++] = model.getListIndex(i);
        }

        setSelectedIndices(selectedIndices);
    }

    public static class MyElement {

        final int index;
        final String label;

        public MyElement(final int index, final String label) {
            this.index = index;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    static class MyListModel implements ListModel<MyElement> {

        private final ArrayList<MyElement> elements;
        private final ArrayList<ListDataListener> listeners;

        public MyListModel(final String[] names) {
            elements = new ArrayList<>();
            for (int i = 0; i < names.length; i++) {
                elements.add(new MyElement(i, names[i]));
            }

            listeners = new ArrayList<>();
        }

        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public MyElement getElementAt(final int index) {
            return elements.get(index);
        }

        @Override
        public void addListDataListener(final ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(final ListDataListener l) {
            listeners.remove(l);
        }

        public MyElement getMyElementAt(final int index) {
            return elements.get(index);
        }

        public void addMyElement(final MyElement element) {
            elements.add(element);
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1));
            }
        }

        public void addMyElement(final int index, final MyElement element) {
            elements.add(index, element);
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1));
            }
        }

        public MyElement removeMyElement(final int index) {
            final MyElement element = elements.get(index);
            elements.remove(index);
            for (ListDataListener l : listeners) {
                l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize() - 1));
            }

            return element;
        }

        /**
         * Return the list index of the element with the specified element
         * index.
         *
         * @param elementIndex
         * @return
         */
        public int getListIndex(final int elementIndex) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).index == elementIndex) {
                    return i;
                }
            }

            return -1;
        }
    }

    private static class MyDragListener implements DragSourceListener, DragGestureListener {

        private final DragDropList list;
        private final DragSource ds;

        public MyDragListener(final DragDropList list) {
            this.list = list;
            ds = new DragSource();
            ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);
        }

        @Override
        public void dragEnter(final DragSourceDragEvent dsde) {
            // Required for implementation of DragGestureListener
        }

        @Override
        public void dragOver(final DragSourceDragEvent dsde) {
            // Required for implementation of DragGestureListener
        }

        @Override
        public void dropActionChanged(final DragSourceDragEvent dsde) {
            // Required for implementation of DragGestureListener
        }

        @Override
        public void dragExit(final DragSourceEvent dse) {
            // Required for implementation of DragGestureListener
        }

        @Override
        public void dragDropEnd(final DragSourceDropEvent dsde) {
            // Required for implementation of DragGestureListener
        }

        @Override
        public void dragGestureRecognized(final DragGestureEvent dge) {
            StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
            ds.startDrag(dge, DragSource.DefaultCopyDrop, transferable, this);
        }
    }

    private static class MyListDropHandler extends TransferHandler {

        private final DragDropList list;

        public MyListDropHandler(final DragDropList list) {
            this.list = list;
        }

        @Override
        public boolean canImport(final TransferHandler.TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();

            return dl.getIndex() != -1;
        }

        @Override
        public boolean importData(final TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            final Transferable transferable = support.getTransferable();
            String indexString;
            try {
                indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                return false;
            }

            final int index = Integer.parseInt(indexString);
            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
            final int dropTargetIndex = dl.getIndex();

            // Drop the dragged element into the correct place.
            if (index < dropTargetIndex) {
                // If the dragged element is being dropped at a higher target index,
                // add the element at the indicated index, remove the element from the source,
                // and select the dragged element (at a different position due to the removal).
                list.model.addMyElement(dropTargetIndex, list.model.getMyElementAt(index));
                list.model.removeMyElement(index);
                list.setSelectedIndex(dropTargetIndex - 1);
            } else {
                // If the dragged element is being dropped at a lower target index,
                // remove the element from the source, add it at the indicated index,
                // and select it.
                final MyElement element = list.model.removeMyElement(index);
                list.model.addMyElement(dropTargetIndex, element);
                list.setSelectedIndex(dropTargetIndex);
            }

            return true;
        }
    }
}
