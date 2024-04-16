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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * An object that holds the descriptions of node and connections labels and node
 * decorators.
 * <p>
 * Note that this should no longer be used and only remains to support legacy
 * graph files.
 *
 * @author algol
 *
 * <p>
 * Replaced by {@link au.gov.asd.tac.constellation.visual.labels.GraphLabels}
 * and {@link au.gov.asd.tac.constellation.visual.decorators.Decorators}.
 */
@Deprecated
public final class GraphLabelsAndDecoratorsV0 implements Serializable {

    /**
     * How many decorators are there?
     */
    public static final int N_DECORATORS = Decorator.values().length;
    public static final String FIELD_BOTTOM = "bottom";
    public static final String FIELD_TOP = "top";
    public static final String FIELD_CONNECTIONS = "connections";
    public static final String FIELD_ATTR = "attr";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_RADIUS = "radius";
    private static final char[] META_CHAR = {' ', ';', '[', ']'};

    /**
     * What kind of label?
     */
    public enum LabelType {

        /**
         * Bottom of node.
         */
        BOTTOM,
        /**
         * Top of node.
         */
        TOP,
        /**
         * Connection.
         */
        CONNECTION
    }

    /**
     * Decorator corner.
     * <p>
     * Don't change the order: SceneBatchStore and the shader rely on a specific
     * order.
     */
    public enum Decorator {

        /**
         * Northwest.
         */
        NW,
        /**
         * Southwest.
         */
        SW,
        /**
         * Southeast.
         */
        SE,
        /**
         * Northeast.
         */
        NE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    private final List<GraphLabelV0> bottomLabels;
    private final List<GraphLabelV0> topLabels;
    private final List<GraphLabelV0> connLabels;
    private final String[] decoratorLabels;

    /**
     * Construct a new Labels instance.
     */
    public GraphLabelsAndDecoratorsV0() {
        bottomLabels = new ArrayList<>();
        topLabels = new ArrayList<>();
        connLabels = new ArrayList<>();

        decoratorLabels = new String[N_DECORATORS];
    }

    /**
     * Construct a new GraphLabels instance from an existing GraphLabels
     * instance.
     *
     * @param glad An existing GraphLabels instance.
     */
    public GraphLabelsAndDecoratorsV0(final GraphLabelsAndDecoratorsV0 glad) {
        bottomLabels = new ArrayList<>();
        topLabels = new ArrayList<>();
        connLabels = new ArrayList<>();

        if (glad != null) {
            bottomLabels.addAll(Arrays.asList(glad.getBottomLabels()));
            topLabels.addAll(Arrays.asList(glad.getTopLabels()));
            connLabels.addAll(Arrays.asList(glad.getConnectionLabels()));
            decoratorLabels = Arrays.copyOf(glad.decoratorLabels, glad.decoratorLabels.length);
        } else {
            decoratorLabels = new String[Decorator.values().length];
        }
    }

    /**
     * Get the node bottom labels.
     *
     * @return The node bottom labels.
     */
    public GraphLabelV0[] getBottomLabels() {
        return bottomLabels.toArray(new GraphLabelV0[bottomLabels.size()]);
    }

    /**
     * Set the node bottom labels.
     *
     * @param labels The node bottom labels.
     */
    public void setBottomLabels(final Collection<GraphLabelV0> labels) {
        bottomLabels.clear();
        bottomLabels.addAll(labels);
    }

    /**
     * Clear the bottom labels.
     */
    public void clearBottomLabels() {
        bottomLabels.clear();
    }

    /**
     * Add a label to the bottom of the nodes.
     *
     * @param label A label to be added to the bottom of the nodes.
     */
    public void addBottomLabel(final GraphLabelV0 label) {
        bottomLabels.add(label);
    }

    /**
     * Add a label to the bottom of the nodes as long as the label does not
     * already exists
     *
     * @param label A label to be added to the bottom of the nodes.
     */
    public void addUniquelyBottomLabel(final GraphLabelV0 label) {
        boolean add = false;
        for (GraphLabelV0 labelEntry : bottomLabels) {
            if (!labelEntry.getLabel().equals(label.getLabel()) || labelEntry.getRadius() != label.getRadius() || labelEntry.getColor().equals(label.getColor())) {
                add = true;
            }
        }
        if (add) {
            bottomLabels.add(label);
        }
    }

    /**
     * Get the node top labels.
     *
     * @return The node top labels.
     */
    public GraphLabelV0[] getTopLabels() {
        return topLabels.toArray(new GraphLabelV0[topLabels.size()]);
    }

    /**
     * Set the node top labels.
     *
     * @param labels The node top labels.
     */
    public void setTopLabels(final Collection<GraphLabelV0> labels) {
        topLabels.clear();
        topLabels.addAll(labels);
    }

    /**
     * Clear the top labels.
     */
    public void clearTopLabels() {
        topLabels.clear();
    }

    /**
     * Add a label to the top of the nodes.
     *
     * @param label A label to be added to the top of the nodes.
     */
    public void addTopLabel(final GraphLabelV0 label) {
        topLabels.add(label);
    }

    /**
     * Get the connection labels.
     *
     * @return The connection labels.
     */
    public GraphLabelV0[] getConnectionLabels() {
        return connLabels.toArray(new GraphLabelV0[connLabels.size()]);
    }

    /**
     * Set the connection labels.
     *
     * @param labels The connection labels.
     */
    public void setConnectionLabels(final Collection<GraphLabelV0> labels) {
        connLabels.clear();
        connLabels.addAll(labels);
    }

    /**
     * Clear the bottom labels.
     */
    public void clearConnectionLabels() {
        connLabels.clear();
    }

    /**
     * Add a label to the connections.
     *
     * @param label A label to be added to the connections.
     */
    public void addConnectionLabel(final GraphLabelV0 label) {
        connLabels.add(label);
    }

    /**
     * Return a list of attribute ids corresponding to the attribute names in
     * the graph's labels structure.
     * <p>
     * If an attribute name is not present, it will be skipped.
     *
     * @param rg The graph.
     * @param ltype The label type.
     *
     * @return A list of attribute ids.
     */
    public List<Integer> getReferencedAttributes(final GraphReadMethods rg, final GraphLabelsAndDecoratorsV0.LabelType ltype) {
        final List<GraphLabelV0> labels;
        final GraphElementType etype;
        switch (ltype) {
            case null -> {
                labels = connLabels;
                etype = GraphElementType.TRANSACTION;
            }
            case BOTTOM -> {
                labels = bottomLabels;
                etype = GraphElementType.VERTEX;
            }
            case TOP -> {
                labels = topLabels;
                etype = GraphElementType.VERTEX;
            }
            default -> {
                labels = connLabels;
                etype = GraphElementType.TRANSACTION;
            }
        }

        final ArrayList<Integer> attributes = new ArrayList<>();

        labels.forEach(label -> attributes.add(rg.getAttribute(etype, label.getLabel())));

        return attributes;
    }

    /**
     * Return a hashmap of the attribute keyed on their ids
     * <p>
     * If an attribute name is not present, it will be skipped.
     *
     * @param rg The graph.
     * @param ltype The label type.
     *
     * @return A list of attribute ids.
     */
    public Map<Integer, GraphLabelV0> getReferencedAttributesAndGraphLabels(final GraphReadMethods rg, final GraphLabelsAndDecoratorsV0.LabelType ltype) {
        final List<GraphLabelV0> labels;
        final GraphElementType etype;
        switch (ltype) {
            case null -> {
                labels = connLabels;
                etype = GraphElementType.TRANSACTION;
            }
            case BOTTOM -> {
                labels = bottomLabels;
                etype = GraphElementType.VERTEX;
            }
            case TOP -> {
                labels = topLabels;
                etype = GraphElementType.VERTEX;
            }
            default -> {
                labels = connLabels;
                etype = GraphElementType.TRANSACTION;
            }
        }

        final HashMap<Integer, GraphLabelV0> attributes = new HashMap<>();

        for (final GraphLabelV0 label : labels) {
            final int attr = rg.getAttribute(etype, label.getLabel());
            if (attr != Graph.NOT_FOUND) {
                attributes.put(attr, label);
            }
        }

        return attributes;
    }

    /**
     * Set the label to be used for the specified decorator.
     *
     * @param decorator The decorator being set.
     * @param label The label to use, or null to not use a decorator in this
     * position.
     */
    public void setDecoratorLabel(final Decorator decorator, final String label) {
        decoratorLabels[decorator.ordinal()] = label;
    }

    /**
     * Return the attribute label at the specified decorator position.
     *
     * @param dec A Decorator position.
     *
     * @return The attribute label at the specified decorator position.
     */
    public String getDecoratorLabel(final Decorator dec) {
        return decoratorLabels[dec.ordinal()];
    }

    public String[] getDecoratorLabels() {
        return Arrays.copyOf(decoratorLabels, N_DECORATORS);
    }

    /**
     * Return an int[] containing the ids of the attributes to be used as node
     * decorators.
     * <p>
     * The decorators are in the order NW, SW, NE, SE.
     *
     * @param rg The graph.
     *
     * @return An int[] containing the ids of the attributes to be used as node
     * decorators.
     */
    public Map<Decorator, Integer> getDecoratorAttributes(final GraphReadMethods rg) {
        final EnumMap<Decorator, Integer> dec = new EnumMap<>(Decorator.class);

        dec.put(Decorator.NW, rg.getAttribute(GraphElementType.VERTEX, decoratorLabels[Decorator.NW.ordinal()]));
        dec.put(Decorator.SW, rg.getAttribute(GraphElementType.VERTEX, decoratorLabels[Decorator.SW.ordinal()]));
        dec.put(Decorator.SE, rg.getAttribute(GraphElementType.VERTEX, decoratorLabels[Decorator.SE.ordinal()]));
        dec.put(Decorator.NE, rg.getAttribute(GraphElementType.VERTEX, decoratorLabels[Decorator.NE.ordinal()]));

        return dec;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append(" ");
        buf.append(String.format("nw=%s sw=%s se=%s ne=%s",
                StringUtilities.escapeString(decoratorLabels[Decorator.NW.ordinal()], META_CHAR),
                StringUtilities.escapeString(decoratorLabels[Decorator.SW.ordinal()], META_CHAR),
                StringUtilities.escapeString(decoratorLabels[Decorator.SE.ordinal()], META_CHAR),
                StringUtilities.escapeString(decoratorLabels[Decorator.NE.ordinal()], META_CHAR)));
        buf.append(" ");

        for (final GraphLabelV0 gl : bottomLabels) {
            buf.append("b");
            buf.append(gl);
            buf.append(" ");
        }
        for (final GraphLabelV0 gl : topLabels) {
            buf.append("t");
            buf.append(gl);
            buf.append(" ");
        }
        for (final GraphLabelV0 gl : connLabels) {
            buf.append("c");
            buf.append(gl);
            buf.append(" ");
        }
        buf.append("]");

        return buf.toString();
    }

    public static GraphLabelsAndDecoratorsV0 fromString(final String decoratorsAndLabelsString) {
        GraphLabelsAndDecoratorsV0 thisGraphLabelsAndDecorators = new GraphLabelsAndDecoratorsV0();

        if (StringUtils.isNotBlank(decoratorsAndLabelsString)) {
            Set<Character> splitChar = new HashSet<>();
            splitChar.add(' ');

            List<String> decoratorsAndLabelsComponents = StringUtilities.splitLabelsWithEscapeCharacters(decoratorsAndLabelsString, splitChar);

            // The decorator components are of the form 'dd=values'.
            // Knock off the first three characters.
            thisGraphLabelsAndDecorators.setDecoratorLabel(Decorator.NW, StringUtilities.unescapeString(decoratorsAndLabelsComponents.get(1).substring(3), META_CHAR));
            thisGraphLabelsAndDecorators.setDecoratorLabel(Decorator.SW, StringUtilities.unescapeString(decoratorsAndLabelsComponents.get(2).substring(3), META_CHAR));
            thisGraphLabelsAndDecorators.setDecoratorLabel(Decorator.SE, StringUtilities.unescapeString(decoratorsAndLabelsComponents.get(3).substring(3), META_CHAR));
            thisGraphLabelsAndDecorators.setDecoratorLabel(Decorator.NE, StringUtilities.unescapeString(decoratorsAndLabelsComponents.get(4).substring(3), META_CHAR));

            for (int i = 5; i < decoratorsAndLabelsComponents.size(); i++) {
                String currentComponent = decoratorsAndLabelsComponents.get(i);
                if (currentComponent.startsWith("b")) {
                    GraphLabelV0 thisGraphLabel = GraphLabelV0.fromString(currentComponent.substring(currentComponent.indexOf('['), currentComponent.lastIndexOf(']') + 1));
                    thisGraphLabelsAndDecorators.addBottomLabel(thisGraphLabel);
                } else if (currentComponent.startsWith("t")) {
                    GraphLabelV0 thisGraphLabel = GraphLabelV0.fromString(currentComponent.substring(currentComponent.indexOf('['), currentComponent.lastIndexOf(']') + 1));
                    thisGraphLabelsAndDecorators.addTopLabel(thisGraphLabel);
                } else if (currentComponent.startsWith("c")) {
                    GraphLabelV0 thisGraphLabel = GraphLabelV0.fromString(currentComponent.substring(currentComponent.indexOf('['), currentComponent.lastIndexOf(']') + 1));
                    thisGraphLabelsAndDecorators.addConnectionLabel(thisGraphLabel);
                } else {
                    // Do nothing
                }
            }
        }

        return thisGraphLabelsAndDecorators;
    }
}
