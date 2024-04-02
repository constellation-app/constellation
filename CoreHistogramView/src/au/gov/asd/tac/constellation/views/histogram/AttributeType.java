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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The AttributeType enum describes the high-level types of things that can be
 * displayed in the histogram.
 *
 * @author sirius
 */
public enum AttributeType {

    /**
     * Graph elements are binned based on an attribute value.
     */
    ATTRIBUTE("Attribute", GraphElementType.VERTEX, GraphElementType.LINK, GraphElementType.EDGE, GraphElementType.TRANSACTION) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(elementType);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(elementType, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator == null) {
                    attributeBinCreator = DefaultBinCreators.DEFAULT_ATTRIBUTE_BIN_CREATOR;
                }
                binCreators.put(attribute.getName(), attributeBinCreator);
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return null;
        }
    },
    /**
     * Graph elements are binned based on graph-based attributes such as degree
     * etc.
     */
    GRAPH_PROPERTY("Graph Property", GraphElementType.VERTEX, GraphElementType.LINK, GraphElementType.EDGE, GraphElementType.TRANSACTION) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            switch (elementType) {
                case VERTEX -> {
                    binCreators.put("Neighbour Count", DefaultBinCreators.NEIGHBOUR_COUNT_BIN_CREATOR);
                    binCreators.put(AttributeTypeConstants.TRANSACTION_COUNT, DefaultBinCreators.TRANSACTION_COUNT_BIN_CREATOR);
                    binCreators.put("Outgoing Transaction Count", DefaultBinCreators.OUTGOING_TRANSACTION_COUNT_BIN_CREATOR);
                    binCreators.put("Incoming Transaction Count", DefaultBinCreators.INCOMING_TRANSACTION_COUNT_BIN_CREATOR);
                    binCreators.put("Undirected Transaction Count", DefaultBinCreators.UNDIRECTED_TRANSACTION_COUNT_BIN_CREATOR);
                }

                case LINK -> {
                    binCreators.put(AttributeTypeConstants.TRANSACTION_COUNT, DefaultBinCreators.LINK_TRANSACTION_COUNT_BIN_CREATOR);
                }

                case EDGE -> {
                    binCreators.put(AttributeTypeConstants.TRANSACTION_COUNT, DefaultBinCreators.EDGE_TRANSACTION_COUNT_BIN_CREATOR);
                }

                case TRANSACTION -> {
                    binCreators.put("Transaction Direction", DefaultBinCreators.TRANSACTION_DIRECTION_BIN_CREATOR);
                }
                
                default -> {
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return null;
        }
    },
    /**
     * Connection graph elements are binned on the the attribute values of their
     * source vertices.
     */
    SOURCE_VERTEX_ATTRIBUTE("Source Node Attribute", GraphElementType.LINK, GraphElementType.EDGE, GraphElementType.TRANSACTION) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {

            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (o1, o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator == null) {
                    attributeBinCreator = DefaultBinCreators.SOURCE_DEFAULT_ATTRIBUTE_BIN_CREATOR;
                }
                binCreators.put(attribute.getName(), attributeBinCreator);
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    },
    /**
     * Connection graph elements are binned on the attribute values of their
     * destination vertices.
     */
    DESTINATION_VERTEX_ATTRIBUTE("Destination Node Attribute", GraphElementType.LINK, GraphElementType.EDGE, GraphElementType.TRANSACTION) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator == null) {
                    attributeBinCreator = DefaultBinCreators.DESTINATION_DEFAULT_ATTRIBUTE_BIN_CREATOR;
                }
                binCreators.put(attribute.getName(), attributeBinCreator);
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    },
    /**
     * Link elements are binned by the sum of the attribute values of all
     * transactions they represent.
     */
    LINK_SUM_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.SUM_TRANSACTION_ATTRIBUTE, GraphElementType.LINK) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Link elements are binned by the average of the attribute values of all
     * transactions they represent.
     */
    LINK_AVERAGE_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.AVERAGE_TRANSACTION_ATTRIBUTE, GraphElementType.LINK) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Link elements are binned by the maximum of the attribute values of all
     * transactions they represent.
     */
    LINK_MAX_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.MAXIMUM_TRANSACTION_ATTRIBUTE, GraphElementType.LINK) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Link elements are binned by the minimum of the attribute values of all
     * transactions they represent.
     */
    LINK_MIN_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.MINIMUM_TRANSACTION_ATTRIBUTE, GraphElementType.LINK) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Link elements are binned by the number of unique attribute values of all
     * transactions they represent.
     */
    LINK_UNIQUE_VALUES_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.UNIQUE_VALUES_TRANSACTION_ATTRIBUTE, GraphElementType.LINK) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(DEFAULT_ATTRIBUTE_TYPE);
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Edge elements are binned by the sum of the attribute values of all
     * transactions they represent.
     */
    EDGE_SUM_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.SUM_TRANSACTION_ATTRIBUTE, GraphElementType.EDGE) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Edge elements are binned by the average of the attribute values of all
     * transactions they represent.
     */
    EDGE_AVERAGE_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.AVERAGE_TRANSACTION_ATTRIBUTE, GraphElementType.EDGE) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Edge elements are binned by the maximum of the attribute values of all
     * transactions they represent.
     */
    EDGE_MAX_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.MAXIMUM_TRANSACTION_ATTRIBUTE, GraphElementType.EDGE) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Edge elements are binned by the minimum of the attribute values of all
     * transactions they represent.
     */
    EDGE_MIN_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.MINIMUM_TRANSACTION_ATTRIBUTE, GraphElementType.EDGE) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Edge elements are binned by the number of unique attribute values of all
     * transactions they represent.
     */
    EDGE_UNIQUE_VALUES_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.UNIQUE_VALUES_TRANSACTION_ATTRIBUTE, GraphElementType.EDGE) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(DEFAULT_ATTRIBUTE_TYPE);
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Vertex elements are binned by the sum of the attribute values of all
     * adjacent transactions.
     */
    VERTEX_SUM_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.SUM_TRANSACTION_ATTRIBUTE, GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Vertex elements are binned by the average of the attribute values of all
     * adjacent transactions.
     */
    VERTEX_AVERAGE_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.AVERAGE_TRANSACTION_ATTRIBUTE, GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Vertex elements are binned by the maximum of the attribute values of all
     * adjacent transactions.
     */
    VERTEX_MAX_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.MAXIMUM_TRANSACTION_ATTRIBUTE, GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Vertex elements are binned by the minimum of the attribute values of all
     * adjacent transactions.
     */
    VERTEX_MIN_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.MINIMUM_TRANSACTION_ATTRIBUTE, GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Vertex elements are binned by the number of unique attribute values of
     * all adjacent transactions.
     */
    VERTEX_UNIQUE_VALUES_TRANSACTION_ATTRIBUTE(AttributeTypeConstants.UNIQUE_VALUES_TRANSACTION_ATTRIBUTE, GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.TRANSACTION, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(DEFAULT_ATTRIBUTE_TYPE);
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.TRANSACTION;
        }
    },
    /**
     * Vertex elements are binned by the sum of the attribute values of all
     * neighbouring vertices.
     */
    VERTEX_SUM_NEIGHBOUR_ATTRIBUTE("Sum Neighbour Attribute", GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    },
    /**
     * Vertex elements are binned by the average of the attribute values of all
     * neighbouring vertices.
     */
    VERTEX_AVERAGE_NEIGHBOUR_ATTRIBUTE("Average Neighbour Attribute", GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    },
    /**
     * Vertex elements are binned by the maximum of the attribute values of all
     * neighbouring vertices.
     */
    VERTEX_MAX_NEIGHBOUR_ATTRIBUTE("Maximum Neighbour Attribute", GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    },
    /**
     * Vertex elements are binned by the minimum of the attribute values of all
     * neighbouring vertices.
     */
    VERTEX_MIN_NEIGHBOUR_ATTRIBUTE("Minimum Neighbour Attribute", GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(attribute.getAttributeType());
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    },
    /**
     * Vertex elements are binned by the number of unique attribute values of
     * all neighbouring vertices.
     */
    VERTEX_UNIQUE_VALUES_NEIGHBOUR_ATTRIBUTE("Unique Values Neighbour Attribute", GraphElementType.VERTEX) {
        @Override
        public void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators) {
            List<Attribute> attributes = new ArrayList<>();

            int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
            for (int position = 0; position < attributeCount; position++) {
                int attribute = graph.getAttribute(GraphElementType.VERTEX, position);
                Attribute attributeRecord = new GraphAttribute(graph, attribute);
                attributes.add(attributeRecord);
            }

            Collections.sort(attributes, (Attribute o1, Attribute o2) -> o1.getName().compareTo(o2.getName()));

            AttributeBinCreatorProvider.init();
            binCreators.put("", DefaultBinCreators.NULL_BIN_CREATOR);
            for (Attribute attribute : attributes) {
                BinCreator attributeBinCreator = getBinCreators().get(DEFAULT_ATTRIBUTE_TYPE);
                if (attributeBinCreator != null) {
                    binCreators.put(attribute.getName(), attributeBinCreator);
                }
            }
        }

        @Override
        public GraphElementType getBinCreatorsGraphElementType() {
            return GraphElementType.VERTEX;
        }
    };

    public static final String DEFAULT_ATTRIBUTE_TYPE = "default";

    private final String label;
    private final Set<GraphElementType> applicableElementTypes = EnumSet.noneOf(GraphElementType.class);
    private final Map<String, BinCreator> attributeBinCreatorMap = new HashMap<>();

    private AttributeType(String label, GraphElementType... elementTypes) {
        this.label = label;
        applicableElementTypes.addAll(Arrays.asList(elementTypes));
    }

    /**
     * Returns the user-visible label for this AttributeType.
     *
     * @return the user-visible label for this AttributeType.
     */
    public String getLabel() {
        return label;
    }

    public Map<String, BinCreator> getBinCreators() {
        return attributeBinCreatorMap;
    }

    public boolean appliesToElementType(GraphElementType elementType) {
        return applicableElementTypes.contains(elementType);
    }

    public void registerAttributeBinCreator(String attributeType, BinCreator attributeBinCreator) {
        attributeBinCreatorMap.put(attributeType, attributeBinCreator);
    }

    @Override
    public String toString() {
        return label;
    }

    public abstract void addBinCreators(GraphReadMethods graph, GraphElementType elementType, Map<String, BinCreator> binCreators);

    public abstract GraphElementType getBinCreatorsGraphElementType();

    private class AttributeTypeConstants {

        private static final String TRANSACTION_COUNT = "Transaction Count";

        private static final String AVERAGE_TRANSACTION_ATTRIBUTE = "Average Transaction Attribute";
        private static final String MAXIMUM_TRANSACTION_ATTRIBUTE = "Maximum Transaction Attribute";
        private static final String MINIMUM_TRANSACTION_ATTRIBUTE = "Minimum Transaction Attribute";
        private static final String SUM_TRANSACTION_ATTRIBUTE = "Sum Transaction Attribute";
        private static final String UNIQUE_VALUES_TRANSACTION_ATTRIBUTE = "Unique Values Transaction Attribute";
    }
}
