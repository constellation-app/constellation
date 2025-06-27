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
package au.gov.asd.tac.constellation.plugins.parameters;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane.ParameterLayout;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

/**
 * A PluginParameters represents a collection of parameters that belong to a
 * single plugin. Each {@link au.gov.asd.tac.constellation.plugins.Plugin} has
 * the opportunity to create a PluginParameters objects that holds all the
 * parameters that are needed to configure the plugin.
 * <p>
 * A PluginParameters object also contains a tree of
 * {@link PluginParametersNode} objects that describe the relationship between
 * parameters for visual layout, as well as any number of
 * {@link PluginParameterController} and {@link PluginParameterController}
 * objects that allow interactions between the values of parameters (e.g., when
 * one parameter's value changes, the range of valid values for another
 * parameter can be updated).
 *
 * @author sirius
 */
public class PluginParameters implements PluginParameterListener {

    private final Map<String, PluginParameter<?>> parameters = new LinkedHashMap<>();
    private final Map<String, PluginParameter<?>> uParameters = Collections.unmodifiableMap(parameters);
    private final Map<String, PluginParameterController> controllers = new HashMap<>();
    private final Map<String, PluginParameterController> uControllers = Collections.unmodifiableMap(controllers);
    private final Map<String, PluginParametersNode> nodes = new HashMap<>();
    private final PluginParametersNode rootNode = new PluginParametersNode((String) null);
    private boolean locked = false;

    /**
     * Create a new empty PluginParameters object.
     */
    public PluginParameters() {
        nodes.put(null, rootNode);
    }

    /**
     * Get the root node of the tree of {@link PluginParametersNode} objects
     *
     * @return A {@link PluginParametersNode} which is at the root of the tree.
     */
    public PluginParametersNode getRootNode() {
        return rootNode;
    }

    /**
     * A node in a tree like structure that describes the hierarchical
     * relationships between parameters, typically for visual layout purposes.
     * Internal nodes represent groups of parameters, whilst leaf nodes
     * represent individual parameter
     */
    public static class PluginParametersNode {

        public final String name;
        private final List<PluginParametersNode> children;
        private final PluginParameter<?> parameter;
        private ParameterLayout formatter;

        /**
         * Create a new PluginParametersNode corresponding to the given
         * parameter. This will be a leaf node.
         *
         * @param parameter The {@link PluginParameter} that this node
         * corresponds to.
         */
        public PluginParametersNode(final PluginParameter<?> parameter) {
            name = parameter.getId();
            this.parameter = parameter;
            children = null;
        }

        /**
         * Set the formatter that describes how this PluginParametersNode should
         * be visually laid out in a
         * {@link au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane}
         *
         * @param formatter A {@link ParameterLayout} object describing how this
         * node should be laid out.
         */
        public void setFormatter(final ParameterLayout formatter) {
            this.formatter = formatter;
        }

        public ParameterLayout getFormatter() {
            return formatter;
        }

        /**
         * Resets the formatting of the this plugin parameters node.
         * <p>
         * Copies and hence resets the formatters for this node and all
         * descendants, allowing this node to be displayed using the same
         * formatting but in a different context.
         */
        public void resetFormatting() {
            this.formatter = formatter == null ? null : formatter.copy();
            if (!isLeaf()) {
                children.forEach(PluginParametersNode::resetFormatting);
            }
        }

        /**
         * Create a new PluginParametersNode with the given name. This will be
         * an internal node, and will not correspond to a particular parameter
         *
         * @param nodeName A name to give to this node. This will not affect the
         * behaviour of the node.
         */
        public PluginParametersNode(final String nodeName) {
            this.name = nodeName;
            parameter = null;
            children = new ArrayList<>();
        }

        /**
         * Add a child PluginParametersNode to this node. The method will check
         * whether the node is an internal node before adding a child.
         *
         * @param node The child PluginParametersNode to add.
         */
        public void addNode(final PluginParametersNode node) {
            if (!isLeaf()) {
                children.add(node);
            }
        }

        /**
         * Is this node a leaf node, that is a node corresponding to a single
         * parameter?
         *
         * @return True if this node is a leaf, false otherwise.
         */
        public boolean isLeaf() {
            return (children == null);
        }

        /**
         * Retrieve the list of leaf nodes that are descendants of this node.
         *
         * @return A List of PluginParametersNodes that are leaves
         */
        public List<PluginParametersNode> getLeaves() {
            List<PluginParametersNode> leaves = new ArrayList<>();
            if (isLeaf()) {
                leaves.add(this);
            } else {
                for (PluginParametersNode node : children) {
                    leaves.addAll(node.getLeaves());
                }
            }
            return leaves;
        }

        /**
         * Get the parameter that this node corresponds to.
         *
         * @return If this node is a leaf, the {@link PluginParameter} that this
         * node corresponds to. Otherwise, null.
         */
        public PluginParameter<?> getParameter() {
            return parameter;
        }

        /**
         * Get the list of children of this node.
         *
         * @return A List of PluginParametersNodes that are children.
         */
        public List<PluginParametersNode> getChildren() {
            if (children != null) {
                return Collections.unmodifiableList(children);
            }
            return Collections.emptyList();
        }

    }

    public final void lock() {
        locked = true;
    }

    /**
     * Add a new parameter.
     * <p>
     * For the purposes of layout, this parameter will be a direct child of this
     * PluginParameter's root PluginParameterNode.
     *
     * @param parameter The {@link PluginParameter} to add.
     * @throws IllegalStateException if a parameter with the same name already
     * exists.
     */
    public final void addParameter(final PluginParameter<?> parameter) {
        addParameter(parameter, null);
    }

    /**
     * Add a new parameter to the given group. This requires that a group with
     * the given name has already been added.
     * <p>
     * For the purposes of layout, this parameter will be a child of the
     * PluginParameterNode corresponding to the group.
     *
     * @param parameter The {@link PluginParameter} to add.
     * @param groupName The String name of the group to add the parameter to.
     * @throws IllegalStateException if a parameter with the same name already
     * exists, or if a group with the given name does not exist.
     */
    public final void addParameter(final PluginParameter<?> parameter, final String groupName) {
        if (parameter == null) {
            return;
        }

        if (locked) {
            throw new IllegalStateException("Adding a parameter after locking");
        }

        if (parameters.containsKey(parameter.getId())) {
            throw new IllegalStateException("Duplicate parameter id: " + parameter.getId());
        }

        parameter.addListener(this);

        final String paramName = parameter.getId();
        parameters.put(paramName, parameter);

        PluginParametersNode parameterNode = new PluginParametersNode(parameter);
        nodes.put(paramName, parameterNode);
        if (groupName != null) {
            if (!nodes.containsKey(groupName)) {
                throw new IllegalStateException("Attempting to add a parameter to a group which doesn't exist: " + groupName);
            }
            nodes.get(groupName).addNode(parameterNode);
        } else {
            rootNode.addNode(parameterNode);
        }
    }

    /**
     * Add all parameters, groups and controllers from the given
     * PluginParameters object to this one.
     *
     * @param parameters The PluginParameters object to append.
     */
    public final void appendParameters(final PluginParameters parameters) {
        if (parameters == null) {
            return;
        }

        parameters.getParameters().entrySet().forEach(parameter -> {
            if (!this.hasParameter(parameter.getKey())) {
                this.addParameter(parameter.getValue());
            }
        });

        parameters.getControllers().entrySet().forEach(controller -> {
            if (!this.getControllers().containsKey(controller.getKey())) {
                this.addController(controller.getKey(), controller.getValue());
            }
        });
    }

    /**
     * Update parameter values from the provided PluginParameters object. Only
     * parameters with IDs that match will be updated.
     *
     * @param parameters
     */
    public final void updateParameterValues(final PluginParameters parameters) {
        if (parameters == null) {
            return;
        }

        parameters.getParameters().entrySet().forEach(parameter -> {
            if (getParameters().containsKey(parameter.getKey())) {
                getParameter(parameter.getKey()).setObjectValue(parameter.getValue().getObjectValue());
            }
        });
    }

    /**
     * Get a map of parameter IDs to parameters.
     *
     * @return An unmodifiable map of all {@link PluginParameters} indexed by
     * their IDs.
     */
    public final Map<String, PluginParameter<?>> getParameters() {
        return uParameters;
    }

    /**
     * Check if the specified parameter exists
     *
     * @param id The parameter name.
     * @return True if the parameter exists, otherwise false.
     */
    public final boolean hasParameter(final String id) {
        return uParameters.containsKey(id);
    }

    /**
     *
     * Check if any of the parameters is a multi-line string parameter
     *
     * @return true if such a parameter exists, false otherwise
     */
    public boolean hasMultiLineStringParameter() {
        for (PluginParameter<?> parameter : uParameters.values()) {
            if (parameter.getParameterValue() instanceof StringParameterValue
                    && parameter.getProperty(StringParameterType.LINES) != null
                    && (int) parameter.getProperty(StringParameterType.LINES) > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the specified parameter has a master controller assigned in this
     * PluginParameters.
     *
     * @param masterId A {@link String} representing the id of a
     * {@link PluginParameter}.
     * @return True if a master controller is assigned, otherwise false.
     */
    public final boolean hasController(final String masterId) {
        return controllers.containsKey(masterId);
    }

    /**
     * Add a controller to be called when the value of the parameter identified
     * by masterId is changed.
     * <p>
     * The controller has access to all of the plugin's parameters.
     *
     * @param masterId The id of the parameter that causes the controller to be
     * called when its value is changed.
     * @param controller The controller to be called when the parameter's value
     * changes.
     */
    public final void addController(final String masterId, final PluginParameterController controller) {
        if (locked) {
            throw new IllegalStateException("Adding a controller after locking");
        }

        // Ensure that the master exists.
        if (!parameters.containsKey(masterId)) {
            throw new IllegalStateException("Unknown master: " + masterId);
        }

        controllers.put(masterId, controller);
    }

    /**
     * Get a map of parameter IDs to controllers.
     *
     * @return An unmodifiable map of all {@link PluginParameterController}
     * indexed by the ID of their respective master parameters.
     */
    public final Map<String, PluginParameterController> getControllers() {
        return uControllers;
    }

    @Override
    public void parameterChanged(final PluginParameter<?> parameter, final ParameterChange change) {
        final PluginParameterController controller = controllers.get(parameter.getId());
        if (controller != null) {
            controller.parameterChanged(parameter, parameters, change);
        }
    }

    /**
     * Check if the specified group exists in this PluginParameters.
     *
     * @param groupName A {@link String} representing the name of a group.
     * @return True if the group exists, otherwise false.
     */
    public final boolean hasGroup(final String groupName) {
        return nodes.containsKey(groupName);
    }

    /**
     * Add a new group to which multiple parameters can be subsequently added.
     * <p>
     * This is for layout purposes, and will add a new PluginParametersNode for
     * the group as a child of the root PluginParametersNode.
     *
     * @param groupName The String name of the group.
     * @param layout The ParameterLayout describing how this group should be
     * visually laid out in a
     * {@link au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane}
     * @throws IllegalStateException if a group with the given name already
     * exists, or the group name is null.
     */
    public final void addGroup(final String groupName, final ParameterLayout layout) {
        addGroup(groupName, layout, null);
    }

    /**
     * Add a new subgroup to the given parent group. This requires that the
     * parent group has already been added.
     * <p>
     * This is for layout purposes, and will add a new PluginParametersNode for
     * the subgroup as a child of the PluginParametersNode corresponding to the
     * parent group.
     *
     * @param groupName The String name of the subgroup to be added.
     * @param layout The ParameterLayout describing how this group should be
     * visually laid out in a
     * {@link au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane}
     * @param parentGroup The String name of the group to be added to.
     * @throws IllegalStateException if a subgroup with the given name already
     * exists, if the parent group doesn't exist, or if the subgroup name is
     * null.
     */
    public final void addGroup(final String groupName, final ParameterLayout layout, final String parentGroup) {
        if (locked) {
            throw new IllegalStateException("Adding a group after locking");
        }

        if (nodes.containsKey(groupName)) {
            throw new IllegalStateException("Duplicate group name: " + parentGroup);
        }

        if (groupName == null) {
            throw new IllegalStateException("Can't add group without a name");
        }

        PluginParametersNode groupNode = new PluginParametersNode(groupName);
        groupNode.setFormatter(layout);
        nodes.put(groupName, groupNode);
        if (parentGroup != null) {
            if (!nodes.containsKey(parentGroup)) {
                throw new IllegalStateException("Attempting to add a new group to a group which doesn't exist: " + parentGroup);
            }
            final PluginParametersNode parentGroupNode = nodes.get(parentGroup);
            if (!parentGroupNode.formatter.supportsChildrenWithMultipleElements() && groupNode.formatter.hasMultipleElements()) {
                throw new IllegalStateException("Attempting to add a new group which lays out multiple elements for each child to a parent group which doesn't support such layouts: " + parentGroup);
            }
            nodes.get(parentGroup).addNode(groupNode);
        } else {
            rootNode.addNode(groupNode);
        }
    }

    /**
     * Request that all parameters store recent values
     */
    public void storeRecentValues() {
        for (PluginParameter<?> parameter : parameters.values()) {
            parameter.storeRecentValue();
        }
    }

    /**
     * Request that all parameters load recent values
     */
    public void loadRecentValues() {
        for (PluginParameter<?> parameter : parameters.values()) {
            parameter.loadToRecentValue();
        }
    }

    /**
     * Called immediately prior to the loading of individual parameters inside
     * this PluginParameters object. Along with endParameterLoading, this allows
     * a plugin to exercise control over the manner in which its parameters are
     * loaded.
     */
    public void startParameterLoading() {
    }

    /**
     * Called immediately after the loading of individual parameters inside this
     * PluginParameters object. Along with startParameterLoading, this allows a
     * plugin to exercise control over the manner in which its parameters are
     * loaded.
     */
    public void endParameterLoading() {
    }

    /**
     * Internal helper to get a PluginParameter with a check that it exists, and
     * a helpful exception if it doesn't.
     *
     * @param id The parameter name.
     *
     * @return A PluginParameter.
     */
    private PluginParameter<?> getParameter(final String id) {
        final PluginParameter<?> pp = getParameters().get(id);
        if (pp == null) {
            throw new IllegalArgumentException(String.format("Parameter '%s' does not exist", id));
        }

        return pp;
    }

    /**
     * Helper method for convenient retrieval of boolean parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's boolean value.
     */
    public boolean getBooleanValue(final String id) {
        return getParameter(id).getBooleanValue();
    }

    /**
     * Helper method for convenient setting of boolean parameter values.
     *
     * @param id The parameter name.
     * @param b A boolean value.
     */
    public void setBooleanValue(final String id, final boolean b) {
        getParameter(id).setBooleanValue(b);
    }

    /**
     * Helper method for convenient retrieval of integer parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's integer value.
     */
    public int getIntegerValue(final String id) {
        return getParameter(id).getIntegerValue();
    }

    /**
     * Helper method for convenient setting of int parameter values.
     *
     * @param id The parameter name.
     * @param i An int value.
     */
    public void setIntegerValue(final String id, final int i) {
        getParameter(id).setIntegerValue(i);
    }

    /**
     * Helper method for convenient retrieval of float parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's float value.
     */
    public float getFloatValue(final String id) {
        return getParameter(id).getFloatValue();
    }

    /**
     * Helper method for convenient setting of float parameter values.
     *
     * @param id The parameter name.
     * @param f A float value.
     */
    public void setFloatValue(final String id, final float f) {
        getParameter(id).setFloatValue(f);
    }

    /**
     * Helper method for convenient retrieval of String parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's String value.
     */
    public String getStringValue(final String id) {
        return getParameter(id).getStringValue();
    }

    /**
     * Helper method for convenient setting of String parameter values.
     *
     * @param id The parameter name.
     * @param s A String value.
     */
    public void setStringValue(final String id, final String s) {
        getParameter(id).setStringValue(s);
    }

    /**
     * Helper method for convenient retrieval of Color parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's Color value.
     */
    public ConstellationColor getColorValue(final String id) {
        return getParameter(id).getColorValue();
    }

    /**
     * Helper method for convenient setting of Color parameter values.
     *
     * @param id The parameter name.
     * @param c A Color value.
     */
    public void setColorValue(final String id, final ConstellationColor c) {
        getParameter(id).setColorValue(c);
    }

    /**
     * Helper method for convenient retrieval of LocalDate parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's LocalDate value.
     */
    public LocalDate getLocalDateValue(final String id) {
        return getParameter(id).getLocalDateValue();
    }

    /**
     * Helper method for convenient setting of LocalDate parameter values.
     *
     * @param id The parameter name.
     * @param ld A LocalDate value.
     */
    public void setLocalDateValue(final String id, final LocalDate ld) {
        getParameter(id).setLocalDateValue(ld);
    }

    /**
     * Helper method for convenient retrieval of DateTimeRange parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's DateTimeRange value;
     */
    public DateTimeRange getDateTimeRangeValue(final String id) {
        return getParameter(id).getDateTimeRangeValue();
    }

    /**
     * Helper method for convenient setting of boolean parameter values.
     *
     * @param id The parameter name.
     * @param dtr A DateTimeRange value.
     */
    public void setDateTimeRangeValue(final String id, final DateTimeRange dtr) {
        getParameter(id).setDateTimeRangeValue(dtr);
    }

    /**
     * Helper method for convenient retrieval of SingleChoice parameter values.
     *
     * @param id The parameter name.
     *
     * @return The ParameterValue chosen by the user, or null if there was no
     * default and the user didn't choose anything.
     */
    public ParameterValue getSingleChoice(final String id) {
        return getParameter(id).getSingleChoice();
    }

    /**
     * Helper method for convenient retrieval of MultiChoice parameter values.
     *
     * @param id The parameter name.
     *
     * @return A MultiChoiceParameterValue representing the choices made.
     */
    public MultiChoiceParameterValue getMultiChoiceValue(final String id) {
        return getParameter(id).getMultiChoiceValue();
    }

    /**
     * Helper method for convenient retrieval of Object parameter values.
     *
     * @param id The parameter name.
     *
     * @return The parameter's Object value.
     */
    public Object getObjectValue(final String id) {
        return getParameter(id).getObjectValue();
    }

    /**
     * Helper method for convenient setting of Object parameter values.
     *
     * @param id The parameter name.
     * @param o An Object value.
     */
    public void setObjectValue(final String id, final Object o) {
        getParameter(id).setObjectValue(o);
    }

    /**
     * Get a copy of this PluginParameters object. This will perform a deep copy
     * of all parameters, the PluginParametersNode tree and any
     * {@link PluginParameterController} or {@link PluginParameterController}
     * objects.
     *
     * @return a copy of this parameter.
     */
    public PluginParameters copy() {
        PluginParameters copy = new PluginParameters();
        copyTo(copy);
        return copy;
    }

    protected final void copyTo(PluginParameters copy) {

        // Copy the parameters and their group structure
        Queue<String> nodeQueue = new LinkedList<>();
        nodeQueue.add(null);
        while (!nodeQueue.isEmpty()) {
            final String currentNodeName = nodeQueue.remove();
            final PluginParametersNode currentNode = nodes.get(currentNodeName);
            for (PluginParametersNode child : currentNode.getChildren()) {
                if (child.isLeaf()) {
                    copy.addParameter(child.parameter.copy(), currentNodeName);
                } else {
                    copy.addGroup(child.name, child.formatter.copy(), currentNodeName);
                    nodeQueue.add(child.name);
                }
            }
        }

        // Copy the controllers
        copy.controllers.putAll(controllers);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        for (Entry<String, PluginParameter<?>> parameterEntry : parameters.entrySet()) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(parameterEntry.getKey());
            buffer.append('=');
            buffer.append(parameterEntry.getValue().getStringValue());
        }
        return buffer.toString();
    }
}
