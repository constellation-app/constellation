/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A TableModel that maps a Graph to a JTable.
 * <p>
 * The model stores Attribute+Segment instances. As well as keeping track of the
 * element type, we create dummy attributes to represent the id virtual
 * attributes.
 *
 * @author algol
 */
public class GraphTableModel implements TableModel {

    /**
     * Which part of the table a given attribute is in.
     */
    public static enum Segment {

        TX, VX_SRC, VX_DST;
    }

    // Dummy attribute instances so we can add a virtual id column.
    // One for vertices, one for transactions, one for source/destination vertices in the combined table.
    public static final int VX_ID_IX = -9;
    public static final int TX_ID_IX = -8;
    public static final int TX_SRC_ID_IX = -7;
    public static final int TX_DST_ID_IX = -6;
    public static final Attribute VX_ATTR = new GraphAttribute(VX_ID_IX, GraphElementType.VERTEX, "integer", "vx id_", "Vertex id", null, null);
    public static final Attribute TX_ATTR = new GraphAttribute(TX_ID_IX, GraphElementType.TRANSACTION, "integer", "tx id_", "Transaction id", null, null);
    public static final Attribute TX_SRC_ATTR = new GraphAttribute(TX_SRC_ID_IX, GraphElementType.TRANSACTION, "integer", "tx src_", "Transaction source", null, null);
    public static final Attribute TX_DST_ATTR = new GraphAttribute(TX_DST_ID_IX, GraphElementType.TRANSACTION, "integer", "tx dst_", "Transaction destination", null, null);

    private final Graph graph;
    private final GraphElementType elementType;
    private final ArrayList<AttributeSegment> attrs;
    private final ArrayList<TableModelListener> tmls;

    private static final Logger LOGGER = Logger.getLogger(GraphTableModel.class.getName());

    /**
     * Construct a new GraphTableModel.
     *
     * @param graph The graph backing the model.
     * @param elementType The element type that this table is displaying.
     */
    GraphTableModel(final Graph graph, final GraphElementType elementType) {
        this.graph = graph;
        this.elementType = elementType;

        attrs = new ArrayList<>();
        if (graph != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                // Always add dummy attributes for the id (and from_vx + to_vx if these are transactions).
                // This will be displayed in the table as an integer corresponding to the vertex/transaction id.
                // Even if you don't want to display them by default, they still have to be part of the model,
                // so don't remove them.
                if (elementType == GraphElementType.VERTEX) {
                    // The vertex table has the vertex id + the vertex attributes.
                    attrs.add(new AttributeSegment(VX_ATTR, Segment.VX_SRC));
                    addAttributesToModel(rg, elementType, Segment.VX_SRC);
                } else {
                    // The transaction table has the transaction id + the transaction attributes + the source vertex id+attributes + the destination vertex id+attributes.
                    attrs.add(new AttributeSegment(TX_ATTR, Segment.TX));
                    attrs.add(new AttributeSegment(TX_SRC_ATTR, Segment.TX));
                    attrs.add(new AttributeSegment(TX_DST_ATTR, Segment.TX));
                    addAttributesToModel(rg, elementType, Segment.TX);
                    addAttributesToModel(rg, GraphElementType.VERTEX, Segment.VX_SRC);
                    addAttributesToModel(rg, GraphElementType.VERTEX, Segment.VX_DST);
                }
            } finally {
                rg.release();
            }

            // Sort the attributes by segment then label, with the special attributes we added for the ids first in their segment.
            Collections.sort(attrs, (final AttributeSegment attrSeg1, final AttributeSegment attrSeg2) -> {
                final Segment segment1 = attrSeg1.segment;
                final Segment segment2 = attrSeg2.segment;
                if (segment1 != segment2) {
                    return segment1.ordinal() - segment2.ordinal();
                }

                final Attribute attr1 = attrSeg1.attr;
                final Attribute attr2 = attrSeg2.attr;

                if (attr1.getId() < 0 || attr2.getId() < 0) {
                    // At least one of these is a dummy attribute.
                    return attr1.getId() - attr2.getId();
                } else {
                    return attr1.getName().compareTo(attr2.getName());
                }
            });
        }

        tmls = new ArrayList<>();
    }

    /**
     * Add the attributes for a given segment to the model.
     *
     * @param rg
     * @param elementType
     * @param segment
     */
    private void addAttributesToModel(final ReadableGraph rg, final GraphElementType elementType, final Segment segment) {
        for (int position = 0; position < rg.getAttributeCount(elementType); position++) {
            final int attrId = rg.getAttribute(elementType, position);
            final Attribute attr = new GraphAttribute(rg, attrId);
            if (!attr.getAttributeType().equals(ObjectAttributeDescription.ATTRIBUTE_NAME)) {
                attrs.add(new AttributeSegment(attr, segment));
            }
        }
    }

    /**
     * Return the element type that this graph is displaying.
     *
     * @return The element type that this graph is displaying.
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    public AttributeSegment getAttributeSegment(final int modelIndex) {
        return attrs.get(modelIndex);
    }

    public Attribute getAttribute(final int modelIndex) {
        return attrs.get(modelIndex).attr;
    }

    public Segment getSegment(final int modelIndex) {
        return attrs.get(modelIndex).segment;
    }

    /**
     * Return the index in the model where this attribute is.
     * <p>
     * If the attribute id is less than zero, this is a dummy attribute with a
     * unique id, so we don't care what the segment is.
     *
     * @param attrId the id of the attribute.
     * @param segment the segment.
     * @return the index in the model where this attribute is.
     */
    public int getModelIndex(final int attrId, final Segment segment) {
        for (int ix = 0; ix < attrs.size(); ix++) {
            final AttributeSegment as = attrs.get(ix);
            if (as.attr.getId() == attrId && (attrId < 0 || as.segment == segment)) {
                return ix;
            }
        }

        throw new IllegalArgumentException(String.format("No attribute id %d in table model for segment %s", attrId, segment));
    }

    @Override
    public int getRowCount() {
        if (graph == null) {
            return 0;
        }

        final int count;
        ReadableGraph rg = graph.getReadableGraph();
        try {
            count = elementType == GraphElementType.VERTEX ? rg.getVertexCount() : rg.getTransactionCount();
        } finally {
            rg.release();
        }

        return count;
    }

    @Override
    public int getColumnCount() {
        return graph != null ? attrs.size() : 0;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return attrs.get(columnIndex).attr.getName();
    }

    private static Class<?> primitiveClassToWrapperClass(final Class<?> primitive) {
        if (primitive.equals(int.class)) {
            return Integer.class;
        } else if (primitive.equals(float.class)) {
            return Float.class;
        } else if (primitive.equals(long.class)) {
            return Long.class;
        } else if (primitive.equals(boolean.class)) {
            return Boolean.class;
        } else if (primitive.equals(double.class)) {
            return Double.class;
        } else if (primitive.equals(byte.class)) {
            return Byte.class;
        } else if (primitive.equals(char.class)) {
            return Character.class;
        } else if (primitive.equals(short.class)) {
            return Short.class;
        } else if (primitive.equals(void.class)) {
            return Void.class;
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        final Attribute attr = attrs.get(columnIndex).attr;
        if (attr.getId() < 0) {
            // Dummy attributes come first.
            // And they're (currently) always integers.
            return Integer.class;
        }

        final AttributeRegistry ar = AttributeRegistry.getDefault();
        final Class<? extends AttributeDescription> attrDescr = attr.getDataType();
        final Class<?> nativeType = ar.getNativeType(attrDescr);
        if (nativeType.isPrimitive()) {
            return primitiveClassToWrapperClass(nativeType);
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final Object s;
        if (graph != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                // The convenience of table row index === graph element position comes in handy.
                final AttributeSegment ai = attrs.get(columnIndex);
                final Attribute attr = ai.attr;
                final int id;
                if (elementType == GraphElementType.VERTEX) {
                    id = rg.getVertex(rowIndex);
                } else if (ai.segment == Segment.TX) {
                    if (attr.getId() == TX_SRC_ID_IX) {
                        id = rg.getTransactionSourceVertex(rg.getTransaction(rowIndex));
                    } else if (attr.getId() == TX_DST_ID_IX) {
                        id = rg.getTransactionDestinationVertex(rg.getTransaction(rowIndex));
                    } else {
                        id = rg.getTransaction(rowIndex);
                    }
                } else if (ai.segment == Segment.VX_SRC) {
                    id = rg.getTransactionSourceVertex(rg.getTransaction(rowIndex));
                } else {
                    id = rg.getTransactionDestinationVertex(rg.getTransaction(rowIndex));
                }

                if (attr.getId() < 0) {
                    s = id;
                } else {
                    final int attrId = attr.getId();
                    final Class<? extends AttributeDescription> dataType = attr.getDataType();
                    final Object attrVal = rg.getObjectValue(attrId, id);

                    // If the attribute is a primitive type, the table will handle it natively for displaying, sorting, editing etc.
                    // Otherwise, we use the attribute's itneraction to get a display string for the
                    if (AttributeRegistry.getDefault().getNativeType(dataType).isPrimitive()) {
                        s = attrVal;
                    } else {
                        s = attrVal == null ? null : AbstractAttributeInteraction.getInteraction(attr.getAttributeType()).getDisplayText(attrVal);
                    }
                }
            } finally {
                rg.release();
            }
        } else {
            s = null;
        }

        return s;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addTableModelListener(final TableModelListener l) {
        tmls.add(l);
    }

    @Override
    public void removeTableModelListener(final TableModelListener l) {
        tmls.remove(l);
    }

    /**
     * Tell the JTable that the model has changed.
     *
     */
    public void graphDataChange() {
        try {
            // Note: if there are any rows in the table, the [firstRow, lastRow] constructor variant must be used.
            // Otherwise, the selection is reset.
            final int nrows = getRowCount();
            final TableModelEvent tme = nrows == 0 ? new TableModelEvent(this) : new TableModelEvent(this, 0, nrows - 1);
            for (final TableModelListener tml : tmls) {
                tml.tableChanged(tme);
            }
        } catch (IndexOutOfBoundsException ex) {
            // This is a known issue as a side effect of not writing on the EDT.
            // I will ware this as a side effect of the quick fix until the Table View is re-written.
            // Logging the fact this is happening so we don't forget.
            LOGGER.log(Level.SEVERE, "Known thread race condition bug. Exception is {0}", ex.getLocalizedMessage());
        }
    }

    /**
     * A convenience class for keeping track of Attributes and their segments
     * into the table columns.
     */
    public static class AttributeSegment {

        public final Attribute attr;
        public final Segment segment;

        /**
         * The segment is for ordering attributes in (tx, vxsrc, vxdst) order.
         *
         * @param attr Attribute.
         * @param segment The attribute's segment.
         */
        AttributeSegment(final Attribute attr, final Segment segment) {
            this.attr = attr;
            this.segment = segment;
        }

        @Override
        public String toString() {
            return String.format("[%s %s;%s]", AttributeSegment.class.getSimpleName(), segment, attr);
        }
    }
}
