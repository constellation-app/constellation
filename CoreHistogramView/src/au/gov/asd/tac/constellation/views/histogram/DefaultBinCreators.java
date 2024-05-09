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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.histogram.bins.AttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.BooleanBin;
import au.gov.asd.tac.constellation.views.histogram.bins.DoubleBin;
import au.gov.asd.tac.constellation.views.histogram.bins.EdgeTransactionCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.FloatBin;
import au.gov.asd.tac.constellation.views.histogram.bins.IncomingTransactionCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import au.gov.asd.tac.constellation.views.histogram.bins.LinkTransactionCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.LongBin;
import au.gov.asd.tac.constellation.views.histogram.bins.NeighbourCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.OutgoingTransactionCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import au.gov.asd.tac.constellation.views.histogram.bins.TransactionCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.TransactionDirectionBin;
import au.gov.asd.tac.constellation.views.histogram.bins.UndirectedTransactionCountBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeAverageTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeAverageTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMaxTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeMinTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeSumTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeSumTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeSumTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.edgeaggregates.EdgeUniqueValuesTransactionAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkAverageTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkAverageTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMaxTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkMinTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkSumTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates.LinkUniqueValuesTransactionAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexAverageNeighbourDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexAverageNeighbourFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMaxNeighbourDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMaxNeighbourDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMaxNeighbourFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMaxNeighbourLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMinNeighbourDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMinNeighbourDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMinNeighbourFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexMinNeighbourLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexSumNeighbourDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexSumNeighbourFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexSumNeighbourLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates.VertexUniqueValuesNeighbourAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexAverageTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexAverageTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMaxTransactionDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMaxTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMaxTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMaxTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMinTransactionDatetimeAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMinTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMinTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexMinTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexSumTransactionDoubleAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexSumTransactionFloatAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexSumTransactionLongAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates.VertexUniqueValuesTransactionAttributeBin;
import au.gov.asd.tac.constellation.views.histogram.representatives.DestinationVertexRepresentative;
import au.gov.asd.tac.constellation.views.histogram.representatives.IdentityRepresentative;
import au.gov.asd.tac.constellation.views.histogram.representatives.SourceVertexRepresentative;
import au.gov.asd.tac.constellation.views.histogram.representatives.TransactionAggregateRepresentative;

/**
 * DefaultBinCreators provides static definitions of the different BinCreators
 * used in the histogram.
 *
 * @author cygnus_x-1
 */
public class DefaultBinCreators {

    public static final BinCreator NULL_BIN_CREATOR = new BinCreator(false, null, null, null);

    public static final BinCreator NEIGHBOUR_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new NeighbourCountBin());
    public static final BinCreator TRANSACTION_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new TransactionCountBin());
    public static final BinCreator LINK_TRANSACTION_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new LinkTransactionCountBin());
    public static final BinCreator EDGE_TRANSACTION_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new EdgeTransactionCountBin());
    public static final BinCreator TRANSACTION_DIRECTION_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new TransactionDirectionBin());
    public static final BinCreator OUTGOING_TRANSACTION_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new OutgoingTransactionCountBin());
    public static final BinCreator INCOMING_TRANSACTION_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new IncomingTransactionCountBin());
    public static final BinCreator UNDIRECTED_TRANSACTION_COUNT_BIN_CREATOR = new BinCreator(false, null, new IdentityRepresentative(), new UndirectedTransactionCountBin());

    public static final BinCreator DEFAULT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new AttributeBin());
    public static final BinCreator STRING_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new StringBin());
    public static final BinCreator INT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new IntBin());
    public static final BinCreator LONG_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new LongBin());
    public static final BinCreator FLOAT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new FloatBin());
    public static final BinCreator DOUBLE_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new DoubleBin());
    public static final BinCreator BOOLEAN_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, null, new IdentityRepresentative(), new BooleanBin());

    public static final BinCreator SOURCE_DEFAULT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new AttributeBin());
    public static final BinCreator SOURCE_STRING_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new StringBin());
    public static final BinCreator SOURCE_INT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new IntBin());
    public static final BinCreator SOURCE_LONG_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new LongBin());
    public static final BinCreator SOURCE_FLOAT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new FloatBin());
    public static final BinCreator SOURCE_DOUBLE_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new DoubleBin());
    public static final BinCreator SOURCE_BOOLEAN_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new SourceVertexRepresentative(), new BooleanBin());

    public static final BinCreator DESTINATION_DEFAULT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new AttributeBin());
    public static final BinCreator DESTINATION_STRING_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new StringBin());
    public static final BinCreator DESTINATION_INT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new IntBin());
    public static final BinCreator DESTINATION_LONG_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new LongBin());
    public static final BinCreator DESTINATION_FLOAT_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new FloatBin());
    public static final BinCreator DESTINATION_DOUBLE_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new DoubleBin());
    public static final BinCreator DESTINATION_BOOLEAN_ATTRIBUTE_BIN_CREATOR = new BinCreator(true, GraphElementType.VERTEX, new DestinationVertexRepresentative(), new BooleanBin());

    // LINK AGGREGATE BIN PROVIDERS
    public static final BinCreator LINK_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkSumTransactionLongAttributeBin());
    public static final BinCreator LINK_TRANSACTION_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkSumTransactionFloatAttributeBin());
    public static final BinCreator LINK_TRANSACTION_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkSumTransactionDoubleAttributeBin());

    public static final BinCreator LINK_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkAverageTransactionFloatAttributeBin());
    public static final BinCreator LINK_TRANSACTION_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkAverageTransactionDoubleAttributeBin());

    public static final BinCreator LINK_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMaxTransactionLongAttributeBin());
    public static final BinCreator LINK_TRANSACTION_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMaxTransactionFloatAttributeBin());
    public static final BinCreator LINK_TRANSACTION_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMaxTransactionDoubleAttributeBin());
    public static final BinCreator LINK_TRANSACTION_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMaxTransactionDatetimeAttributeBin());

    public static final BinCreator LINK_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMinTransactionLongAttributeBin());
    public static final BinCreator LINK_TRANSACTION_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMinTransactionFloatAttributeBin());
    public static final BinCreator LINK_TRANSACTION_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMinTransactionDoubleAttributeBin());
    public static final BinCreator LINK_TRANSACTION_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkMinTransactionDatetimeAttributeBin());

    public static final BinCreator LINK_TRANSACTION_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new LinkUniqueValuesTransactionAttributeBin());

    // EDGE AGGREGATE BIN PROVIDERS
    public static final BinCreator EDGE_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeSumTransactionLongAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeSumTransactionFloatAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeSumTransactionDoubleAttributeBin());

    public static final BinCreator EDGE_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeAverageTransactionFloatAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeAverageTransactionDoubleAttributeBin());

    public static final BinCreator EDGE_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMaxTransactionLongAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMaxTransactionFloatAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMaxTransactionDoubleAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMaxTransactionDatetimeAttributeBin());

    public static final BinCreator EDGE_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMinTransactionLongAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMinTransactionFloatAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMinTransactionDoubleAttributeBin());
    public static final BinCreator EDGE_TRANSACTION_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeMinTransactionDatetimeAttributeBin());

    public static final BinCreator EDGE_TRANSACTION_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new EdgeUniqueValuesTransactionAttributeBin());

    // VERTEX AGGREGATE BIN PROVIDERS FOR TRANSACTION ATTRIBUTES
    public static final BinCreator VERTEX_TRANSACTION_SUM_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexSumTransactionLongAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexSumTransactionFloatAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexSumTransactionDoubleAttributeBin());

    public static final BinCreator VERTEX_TRANSACTION_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexAverageTransactionFloatAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexAverageTransactionDoubleAttributeBin());

    public static final BinCreator VERTEX_TRANSACTION_MAX_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMaxTransactionLongAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMaxTransactionFloatAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMaxTransactionDoubleAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMaxTransactionDatetimeAttributeBin());

    public static final BinCreator VERTEX_TRANSACTION_MIN_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMinTransactionLongAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMinTransactionFloatAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMinTransactionDoubleAttributeBin());
    public static final BinCreator VERTEX_TRANSACTION_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexMinTransactionDatetimeAttributeBin());

    public static final BinCreator VERTEX_TRANSACTION_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.TRANSACTION, new TransactionAggregateRepresentative(), new VertexUniqueValuesTransactionAttributeBin());

    // VERTEX AGGREGATE BIN PROVIDERS FOR NEIGHBOUR ATTRIBUTES
    public static final BinCreator VERTEX_NEIGHBOUR_SUM_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexSumNeighbourLongAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_SUM_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexSumNeighbourFloatAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_SUM_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexSumNeighbourDoubleAttributeBin());

    public static final BinCreator VERTEX_NEIGHBOUR_AVERAGE_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexAverageNeighbourFloatAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_AVERAGE_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexAverageNeighbourDoubleAttributeBin());

    public static final BinCreator VERTEX_NEIGHBOUR_MAX_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMaxNeighbourLongAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_MAX_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMaxNeighbourFloatAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_MAX_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMaxNeighbourDoubleAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_MAX_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMaxNeighbourDatetimeAttributeBin());

    public static final BinCreator VERTEX_NEIGHBOUR_MIN_LONG_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMinNeighbourLongAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_MIN_FLOAT_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMinNeighbourFloatAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_MIN_DOUBLE_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMinNeighbourDoubleAttributeBin());
    public static final BinCreator VERTEX_NEIGHBOUR_MIN_DATETIME_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexMinNeighbourDatetimeAttributeBin());

    public static final BinCreator VERTEX_NEIGHBOUR_UNIQUE_VALUES_ATTRIBUTE_BIN_PROVIDER = new BinCreator(true, GraphElementType.VERTEX, new IdentityRepresentative(), new VertexUniqueValuesNeighbourAttributeBin());
}
