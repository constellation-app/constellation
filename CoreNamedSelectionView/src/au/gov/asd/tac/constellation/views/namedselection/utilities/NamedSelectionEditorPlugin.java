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
package au.gov.asd.tac.constellation.views.namedselection.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;

/**
 * This class provides access to the Named Selection Services provided on the
 * given graph.
 *
 * @author betelgeuse
 * @see SimpleEditPlugin
 */
@NbBundle.Messages({"CTL_API=Named Selection Operation API"})
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class NamedSelectionEditorPlugin extends SimpleEditPlugin {

    /**
     * The range of operations that are provided by this
     * NamedSelectionEditorPlugin.
     * <p>
     * These operations include:
     * <ul>
     * <li><code>INTERSECTION</code></li>
     * <li><code>UNION</code></li>
     * <li><code>RECALL</code></li>
     * <li><code>SAVE</code></li>
     * </ul>
     */
    public enum Operation {

        /**
         * Indicates that an intersection operation should be executed by this
         * NamedSelectionEditorPlugin upon plugin execution.
         */
        INTERSECTION,
        /**
         * Indicates that a union operation should be executed by this
         * NamedSelectionEditorPlugin upon plugin execution.
         */
        UNION,
        /**
         * Indicates that a recall operation should be executed by this
         * NamedSelectionEditorPlugin upon plugin execution.
         */
        RECALL,
        /**
         * Indicates that a save operation should be executed by this
         * NamedSelectionEditorPlugin upon plugin execution.
         */
        SAVE,
        /**
         * Indicates that a custom save (specified rather than currently
         * selected graph elements) operation should be executed by this
         * NamedSelectionEditorPlugin upon plugin execution.
         */
        CUSTOM_SAVE;
    }
    // Graph attributes:
    public static final String NAMED_SELECTION_ATTR = "named_selection";
    public static final String NAMED_SELECTION_ATTR_DESC = "Named Selections";
    public static final long NAMED_SELECTION_DEFAULT_VALUE = 0L;
    private static final long ALL_SET = 0xffffffffffffffffL;
    private static final int NULL_INTEGER = -1;
    // Named Selection service parameters:
    private final Operation operation;
    private final boolean useCurrentlySelected;
    private final boolean isSelectResults;
    private final boolean isDimOthers;
    private final int inputSelection;
    private final int[] inputSelections;
    // For custom saves that aren't based on the current selection
    private final int[] nodesToSave;
    private final int[] transactionsToSave;

    /**
     * Constructs an NamedSelectionEditorPlugin instance that can be used to
     * SAVE named selections to the graph.
     *
     * @param inputSelection The ID that the currently selected nodes will be
     * saved in under the graph.
     */
    public NamedSelectionEditorPlugin(final int inputSelection) {
        this.operation = Operation.SAVE;
        this.inputSelection = inputSelection;

        // No other parameters are needed for a save operation, so set them all to false:
        this.useCurrentlySelected = false;
        this.isSelectResults = false;
        this.isDimOthers = false;
        this.inputSelections = null;
        this.nodesToSave = null;
        this.transactionsToSave = null;
    }

    public NamedSelectionEditorPlugin(final int[] nodesToSave, final int[] transactionsToSave, final int inputSelection) {
        this.operation = Operation.CUSTOM_SAVE;
        this.inputSelection = inputSelection;
        this.nodesToSave = nodesToSave;
        this.transactionsToSave = transactionsToSave;

        // No other parameters are needed for a save operation, so set them all to false:
        this.useCurrentlySelected = false;
        this.isSelectResults = false;
        this.isDimOthers = false;
        this.inputSelections = null;
    }

    /**
     * Constructs an NamedSelectionEditorPlugin instance that can be used to
     * RECALL named selections on the graph.
     *
     * @param isSelectResults <code>true</code> if results should be set to
     * 'selected'.
     * @param isDimOthers <code>true</code> if nodes and transactions not
     * present in the selection are to be dimmed.
     * @param inputSelection The ID number of the selection to be recalled.
     */
    public NamedSelectionEditorPlugin(final boolean isSelectResults, final boolean isDimOthers, final int inputSelection) {
        this.operation = Operation.RECALL;
        this.isSelectResults = isSelectResults;
        this.isDimOthers = isDimOthers;
        this.inputSelection = inputSelection;

        // No other parameters are needed so set them all to false:
        this.useCurrentlySelected = false;
        this.inputSelections = null;
        this.nodesToSave = null;
        this.transactionsToSave = null;
    }

    /**
     * Constructs an NamedSelectionEditorPlugin instance that can be used for
     * Union and Intersection operation.
     * <p>
     * The supported operations are: INTERSECTION, and UNION.
     *
     * @param operation The requested operation type.
     * @param useCurrentlySelected <code>true</code> if the current graph
     * selection should be used when computing unions and intersections.
     * @param isSelectResults <code>true</code> if results should be set to
     * 'selected'.
     * @param isDimOthers <code>true</code> if nodes and transactions that
     * weren't part of the result set are to be dimmed.
     * @param inputSelections Array of ID numbers that the given operation is to
     * be performed on.
     *
     * @see Operation
     */
    public NamedSelectionEditorPlugin(final Operation operation, final boolean useCurrentlySelected,
            final boolean isSelectResults, final boolean isDimOthers, final int... inputSelections) {
        this.operation = operation;
        this.useCurrentlySelected = useCurrentlySelected;
        this.isSelectResults = isSelectResults;
        this.isDimOthers = isDimOthers;
        this.inputSelections = inputSelections;

        // Nothing else needed so null out:
        this.inputSelection = NULL_INTEGER;
        this.nodesToSave = null;
        this.transactionsToSave = null;
    }

    @Override
    public String getName() {
        return "Named Selection: " + operation.name();
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        switch (operation) {
            case INTERSECTION -> performIntersection(graph);
            case RECALL -> recallSelection(graph);
            case SAVE -> saveSelection(graph);
            case UNION -> performUnion(graph);
            case CUSTOM_SAVE -> saveCustomSelection(graph);
            default -> {
                // do nothing
            }
        }
    }

    /**
     * Performs a union operation on the graph using one or more existing Named
     * Selections (and optionally the current selected graph elements).
     *
     * @param graph The graph that will have the unions performed on.
     */
    private void performUnion(GraphWriteMethods graph) throws InterruptedException {
        long inputMask = 0L;

        for (final int inputSelection : inputSelections) {
            if (inputSelection >= 0) {
                inputMask |= 1L << inputSelection;
            }
        }

        // Get attribute ids:
        final int vertexAttr = graph.getAttribute(GraphElementType.VERTEX, NAMED_SELECTION_ATTR);
        final int transAttr = graph.getAttribute(GraphElementType.TRANSACTION, NAMED_SELECTION_ATTR);

        // Add the "selected" attribute if necessary:
        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int transSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add the "dim" attribute if necessary:
        final int vertexDimAttr = VisualConcept.VertexAttribute.DIMMED.ensure(graph);
        final int transDimAttr = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);

        // Perform union operation on vertices:
        final int vertexCount = graph.getVertexCount();
        if (vertexAttr == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME,
                    NAMED_SELECTION_ATTR, NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);

            for (int i = 0; i < vertexCount; i++) {
                graph.setBooleanValue(vertexSelectedAttr, graph.getVertex(i), (Boolean) VisualConcept.VertexAttribute.SELECTED.getDefault());
            }
        } else {
            for (int i = 0; i < vertexCount; i++) {
                final int v = graph.getVertex(i);
                long current = graph.getLongValue(vertexAttr, v);
                boolean result = (current & inputMask) != 0;

                // Intersect on current selection if required:
                if (useCurrentlySelected) {
                    result |= graph.getBooleanValue(vertexSelectedAttr, v);
                }

                // Check whether we need to select positive hits:
                if (isSelectResults) {
                    // Set the selection result:
                    graph.setBooleanValue(vertexSelectedAttr, v, result);
                } else {
                    // Unselect if selected:
                    graph.setBooleanValue(vertexSelectedAttr, v, (Boolean) VisualConcept.VertexAttribute.SELECTED.getDefault());
                }

                // Check whether we need to dim:
                if (isDimOthers) {
                    graph.setBooleanValue(vertexDimAttr, v, !result);
                } else {
                    // Undim by default:
                    graph.setBooleanValue(vertexDimAttr, v, (Boolean) VisualConcept.VertexAttribute.DIMMED.getDefault());
                }
            }
        }

        // Perform union on transactions:
        final int transCount = graph.getTransactionCount();
        if (transAttr == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.TRANSACTION, LongAttributeDescription.ATTRIBUTE_NAME,
                    NAMED_SELECTION_ATTR, NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);

            for (int i = 0; i < transCount; i++) {
                graph.setBooleanValue(transSelectedAttr, graph.getTransaction(i), (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
            }
        } else {
            for (int i = 0; i < transCount; i++) {
                final int t = graph.getTransaction(i);
                long current = graph.getLongValue(transAttr, t);
                boolean result = (current & inputMask) != 0;

                // Intersect on current selection if required:
                if (useCurrentlySelected) {
                    result |= graph.getBooleanValue(transSelectedAttr, t);
                }

                // Check whether we need to select positive hits:
                if (isSelectResults) {
                    graph.setBooleanValue(transSelectedAttr, t, result);
                } else {
                    // Unselect if selected:
                    graph.setBooleanValue(transSelectedAttr, t, (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
                }

                // Check whether we need to dim:
                if (isDimOthers) {
                    graph.setBooleanValue(transDimAttr, t, !result);
                } else {
                    // Undim by default
                    graph.setBooleanValue(transDimAttr, t, (Boolean) VisualConcept.TransactionAttribute.DIMMED.getDefault());
                }
            }
        }
    }

    /**
     * Performs an intersection operation on the graph using one or more
     * existing Named Selections (and optionally the current selected graph
     * elements).
     *
     * @param graph The graph that will have the intersection performed on.
     */
    private void performIntersection(GraphWriteMethods graph) throws InterruptedException {
        // Perform the intersection:
        long inputMask = 0L;

        for (final int inputSelection : inputSelections) {
            if (inputSelection >= 0) {
                inputMask |= 1L << inputSelection;
            }
        }

        // Get attribute ids:
        final int vertexAttr = graph.getAttribute(GraphElementType.VERTEX, NAMED_SELECTION_ATTR);
        final int transAttr = graph.getAttribute(GraphElementType.TRANSACTION, NAMED_SELECTION_ATTR);

        int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        int transSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        int vertexDimAttr = VisualConcept.VertexAttribute.DIMMED.get(graph);
        int transDimAttr = VisualConcept.TransactionAttribute.DIMMED.get(graph);

        // If both vertex and transaction attributes don't exist for selected, don't bother searching current selection.
        boolean intersectCurrentlySelected = (vertexSelectedAttr != Graph.NOT_FOUND || transSelectedAttr != Graph.NOT_FOUND) && useCurrentlySelected;

        // Add the "selected" attribute if necessary:
        VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add the "dim" attribute if necessary:
        VisualConcept.VertexAttribute.DIMMED.ensure(graph);
        VisualConcept.TransactionAttribute.DIMMED.ensure(graph);

        // Perform intersection on vertices:
        final int vertexCount = graph.getVertexCount();
        if (vertexAttr == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME, NAMED_SELECTION_ATTR,
                    NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);

            for (int i = 0; i < vertexCount; i++) {
                graph.setBooleanValue(vertexSelectedAttr, graph.getVertex(i), (Boolean) VisualConcept.VertexAttribute.SELECTED.getDefault());
            }
        } else {
            for (int i = 0; i < vertexCount; i++) {
                final int v = graph.getVertex(i);
                long current = graph.getLongValue(vertexAttr, v);
                boolean result = (current & inputMask) == inputMask;

                // Intersect on current selection if required:
                if (result && intersectCurrentlySelected) {
                    result &= graph.getBooleanValue(vertexSelectedAttr, v);
                }

                // Check whether we need to select positive hits:
                if (isSelectResults) {
                    // Set the selection result:
                    graph.setBooleanValue(vertexSelectedAttr, v, result);
                } else {
                    // Unselect if selected:
                    graph.setBooleanValue(vertexSelectedAttr, v, (Boolean) VisualConcept.VertexAttribute.SELECTED.getDefault());
                }

                // Check whether we need to dim:
                if (isDimOthers) {
                    graph.setBooleanValue(vertexDimAttr, v, !result);
                } else {
                    // Undim by default:
                    graph.setBooleanValue(vertexDimAttr, v, (Boolean) VisualConcept.VertexAttribute.DIMMED.getDefault());
                }
            }
        }

        // Perform intersection on transactions:
        final int transCount = graph.getTransactionCount();
        if (transAttr == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.TRANSACTION, LongAttributeDescription.ATTRIBUTE_NAME, NAMED_SELECTION_ATTR,
                    NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);

            for (int i = 0; i < transCount; i++) {
                graph.setBooleanValue(transSelectedAttr, graph.getTransaction(i), (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
            }
        } else {
            for (int i = 0; i < transCount; i++) {
                final int t = graph.getTransaction(i);
                long current = graph.getLongValue(transAttr, t);
                boolean result = (current & inputMask) == inputMask;

                // Intersect on current selection if required:
                if (intersectCurrentlySelected) {
                    result &= graph.getBooleanValue(transSelectedAttr, t);
                }

                // Check whether we need to select positive hits:
                if (isSelectResults) {
                    graph.setBooleanValue(transSelectedAttr, t, result);
                } else {
                    // Unselect if selected:
                    graph.setBooleanValue(transSelectedAttr, t, (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
                }

                // Check whether we need to dim:
                if (isDimOthers) {
                    graph.setBooleanValue(transDimAttr, t, !result);
                } else {
                    // Undim by default:
                    graph.setBooleanValue(transDimAttr, t, (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
                }
            }
        }
    }

    /**
     * Recalls a previously saved Named Selection.
     *
     * @param graph The graph that will have the intersection performed on.
     */
    private void recallSelection(GraphWriteMethods graph) throws InterruptedException {
        // Get attribute ids:
        final int vertexAttr = graph.getAttribute(GraphElementType.VERTEX, NAMED_SELECTION_ATTR);
        final int transAttr = graph.getAttribute(GraphElementType.TRANSACTION, NAMED_SELECTION_ATTR);

        int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        int transSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        int vertexDimAttr = VisualConcept.VertexAttribute.DIMMED.get(graph);
        int transDimAttr = VisualConcept.TransactionAttribute.DIMMED.get(graph);

        final long mask = 1L << inputSelection;

        // Add the "selected" attribute if necessary:
        VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // Add the "dim" attribute if necessary:
        VisualConcept.VertexAttribute.DIMMED.ensure(graph);
        VisualConcept.TransactionAttribute.DIMMED.ensure(graph);

        // Recall vertices for the given named selection's id:
        final int vertexCount = graph.getVertexCount();
        if (vertexAttr == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME, NAMED_SELECTION_ATTR,
                    NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);

            for (int i = 0; i < vertexCount; i++) {
                graph.setBooleanValue(vertexSelectedAttr, graph.getVertex(i), (Boolean) VisualConcept.VertexAttribute.SELECTED.getDefault());
            }
        } else {
            for (int i = 0; i < vertexCount; i++) {
                final int v = graph.getVertex(i);
                long current = graph.getLongValue(vertexAttr, v);
                boolean result = (current & mask) != 0;

                // Check whether we need to select positive hits:
                if (isSelectResults) {
                    graph.setBooleanValue(vertexSelectedAttr, v, result);
                } else {
                    // Unselect if selected:
                    graph.setBooleanValue(vertexSelectedAttr, v, (Boolean) VisualConcept.VertexAttribute.SELECTED.getDefault());
                }

                // Check whether we need to dim:
                if (isDimOthers) {
                    graph.setBooleanValue(vertexDimAttr, v, !result);
                } else {
                    // Undim by default:
                    graph.setBooleanValue(vertexDimAttr, v, (Boolean) VisualConcept.VertexAttribute.DIMMED.getDefault());
                }
            }
        }

        // Recall transactions for the given named selection's id:
        final int transCount = graph.getTransactionCount();
        if (transAttr == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.TRANSACTION, LongAttributeDescription.ATTRIBUTE_NAME, NAMED_SELECTION_ATTR,
                    NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);

            for (int i = 0; i < transCount; i++) {
                graph.setBooleanValue(transSelectedAttr, graph.getTransaction(i), (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
            }
        } else {
            for (int i = 0; i < transCount; i++) {
                final int t = graph.getTransaction(i);
                long current = graph.getLongValue(transAttr, t);
                boolean result = (current & mask) != 0;

                // Check whether we need to select positive hits:
                if (isSelectResults) {
                    graph.setBooleanValue(transSelectedAttr, t, result);
                } else {
                    // Unselect if selected:
                    graph.setBooleanValue(transSelectedAttr, t, (Boolean) VisualConcept.TransactionAttribute.SELECTED.getDefault());
                }

                // Check whether we need to dim:
                if (isDimOthers) {
                    graph.setBooleanValue(transDimAttr, t, !result);
                } else {
                    // Undim by default:
                    graph.setBooleanValue(transDimAttr, t, (Boolean) VisualConcept.TransactionAttribute.DIMMED.getDefault());
                }
            }
        }
    }

    /**
     * Saves the currently selected graph elements to a NamedSelection.
     *
     * @param graph The graph that will have the intersection performed on.
     * @param selection ID of selection to save to.
     */
    private void saveSelection(GraphWriteMethods graph) throws InterruptedException {
        // Attribute ids:
        int vertexAttr = graph.getAttribute(GraphElementType.VERTEX, NAMED_SELECTION_ATTR);
        int transAttr = graph.getAttribute(GraphElementType.TRANSACTION, NAMED_SELECTION_ATTR);

        final int vertexSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        final long mask = 1L << inputSelection;

        // Add the "selected" attribute if necessary:
        if (vertexAttr == Graph.NOT_FOUND) {
            vertexAttr = graph.addAttribute(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME,
                    NAMED_SELECTION_ATTR, NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);
        }

        if (transAttr == Graph.NOT_FOUND) {
            transAttr = graph.addAttribute(GraphElementType.TRANSACTION, LongAttributeDescription.ATTRIBUTE_NAME,
                    NAMED_SELECTION_ATTR, NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);
        }

        // Save the currently selected vertices:
        final int vertexCount = graph.getVertexCount();
        if (vertexSelectedAttr == Graph.NOT_FOUND) {
            for (int i = 0; i < vertexCount; i++) {
                final int v = graph.getVertex(i);
                final long result = graph.getLongValue(vertexAttr, v) & (mask ^ ALL_SET);

                graph.setLongValue(vertexAttr, v, result);
            }
        } else {
            for (int i = 0; i < vertexCount; i++) {
                final int v = graph.getVertex(i);
                long current = graph.getLongValue(vertexAttr, v);

                if (graph.getBooleanValue(vertexSelectedAttr, v)) {
                    current |= mask;
                } else {
                    current &= (mask ^ ALL_SET);
                }

                graph.setLongValue(vertexAttr, v, current);
            }
        }

        // Save the currently selected transactions:
        final int transCount = graph.getTransactionCount();
        if (transSelectedAttr == Graph.NOT_FOUND) {
            for (int i = 0; i < transCount; i++) {
                final int t = graph.getTransaction(i);
                final long result = graph.getLongValue(transAttr, t) & (mask ^ ALL_SET);

                graph.setLongValue(transAttr, t, result);
            }
        } else {
            for (int i = 0; i < transCount; i++) {
                final int t = graph.getTransaction(i);
                long current = graph.getLongValue(transAttr, t);

                if (graph.getBooleanValue(transSelectedAttr, t)) {
                    current |= mask;
                } else {
                    current &= (mask ^ ALL_SET);
                }

                graph.setLongValue(transAttr, t, current);
            }
        }
    }

    /**
     * Saves the specified graph elements to a NamedSelection.
     *
     * @param graph The graph that will have the intersection performed on.
     * @param nodesToSave The nodes to save to this named selection
     * @param transactionsToSave The transactions to save to this named
     * selection
     * @param selection ID of selection to save to.
     */
    private void saveCustomSelection(GraphWriteMethods graph) throws InterruptedException {
        // Attribute ids:
        int vertexAttr = graph.getAttribute(GraphElementType.VERTEX, NAMED_SELECTION_ATTR);
        int transAttr = graph.getAttribute(GraphElementType.TRANSACTION, NAMED_SELECTION_ATTR);

        // Add the "selected" attribute if necessary:
        if (vertexAttr == Graph.NOT_FOUND) {
            vertexAttr = graph.addAttribute(GraphElementType.VERTEX, LongAttributeDescription.ATTRIBUTE_NAME,
                    NAMED_SELECTION_ATTR, NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);
        }

        if (transAttr == Graph.NOT_FOUND) {
            transAttr = graph.addAttribute(GraphElementType.TRANSACTION, LongAttributeDescription.ATTRIBUTE_NAME,
                    NAMED_SELECTION_ATTR, NAMED_SELECTION_ATTR_DESC, NAMED_SELECTION_DEFAULT_VALUE, null);
        }

        final long mask = 1L << inputSelection;

        // Exclude all nodes from the named selection
        int vertexCount = graph.getVertexCount();
        for (int i = 0; i < vertexCount; i++) {
            final int vxID = graph.getVertex(i);
            final long result = graph.getLongValue(vertexAttr, vxID) & (mask ^ ALL_SET);
            graph.setLongValue(vertexAttr, vxID, result);
        }
        // Save the specified nodes to the named selection
        if (nodesToSave != null) {
            for (int i = 0; i < nodesToSave.length; i++) {
                int vxID = nodesToSave[i];
                long current = graph.getLongValue(vertexAttr, vxID);
                current |= mask;
                graph.setLongValue(vertexAttr, vxID, current);
            }
        }
        // Exclude all transactions from the named selection
        int transactionCount = graph.getTransactionCount();
        for (int i = 0; i < transactionCount; i++) {
            final int txID = graph.getTransaction(i);
            final long result = graph.getLongValue(transAttr, txID) & (mask ^ ALL_SET);
            graph.setLongValue(transAttr, txID, result);
        }
        // Save the specified transactions to the named selection
        if (transactionsToSave != null) {
            for (int i = 0; i < transactionsToSave.length; i++) {
                int txID = transactionsToSave[i];
                long current = graph.getLongValue(transAttr, txID);
                current |= mask;
                graph.setLongValue(transAttr, txID, current);
            }
        }
    }
}
