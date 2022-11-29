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
package au.gov.asd.tac.constellation.plugins.update;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a way of adding graph aware update components to an
 * {@link UpdateController}. When used in conjunction with a
 * {@link GraphUpdateManager} that handles graph listening, an instance of this
 * class will register changes with the graph aware components and call the
 * controller's {@link UpdateController#update} method when appropriate.
 * <p>
 * After a GraphUpdateController and GraphUpdateManager have been linked to an
 * UpdateController, update components can be made to depend on the
 * GraphUpdateController's graph aware update components so that they will
 * update whenever the relevant type of graph changes have occurred. The
 * following is an example of this workflow:
 * <pre><code>
 *
 *      // Set up the required components
 *      UpdateController controller = new UpdateController();
 *      GraphUpdateController graphController = new GraphUpdateController(controller);
 *      GraphUpdateManager graphManager = new GraphUpdateManager(graphController);
 *
 *      // Beginning listening to the graph
 *      graphManager.setManaged(true);
 *
 *      // Create an update component to do something
 *      UpdateComponent&lt;GraphReadMethods&gt; component = new UpdateComponent&lt;GraphReadMethods&gt;() {
 *          &#64;Override
 *          protected boolean update(GraphReadMethods graph) {
 *              doSomething();
 *          }
 *      };
 *
 *      // Now every time there is a structural change in the graph
 *      // (ie. nodes or transactions are added or removed) doSomething() will run.
 *      component.dependOn(graphController.getStructureUpdateComponent());
 * </code></pre>
 *
 * It is important to note that these graph aware update components can't be
 * made to depend on other update components; they are meant to be at the top of
 * the update hierarchy.
 *
 * @see GraphUpdateManager
 * @see UpdateController
 * @author sirius
 */
public class GraphUpdateController {

    private final UpdateController<GraphReadMethods> updateController;

    // The most recently seen modification counters for the currently active graph
    private long globalModificationCounter = -1;
    private long structureModificationCounter = -1;
    private long attributeModificationCounter = -1;

    private final SimpleUpdateComponent newGraphUpdateComponent;
    private final SimpleUpdateComponent globalUpdateComponent;
    private final SimpleUpdateComponent structureUpdateComponent;
    private final SimpleUpdateComponent attributeUpdateComponent;

    private final List<AttributeUpdateComponent> attributeUpdateComponents = new ArrayList<>();

    private String currentGraphId = null;

    /**
     * Create a new GrpahUpdateController that registers changes with the
     * specified UpdateController
     *
     * @param updateController The UpdateController to register graph changes
     * with.
     */
    public GraphUpdateController(UpdateController<GraphReadMethods> updateController) {
        this.updateController = updateController;

        newGraphUpdateComponent = new SimpleUpdateComponent("New Graph");
        globalUpdateComponent = new SimpleUpdateComponent("Global Change");
        structureUpdateComponent = new SimpleUpdateComponent("Structure Change");
        attributeUpdateComponent = new SimpleUpdateComponent("Attribute Change");
    }

    /**
     * Get the UpdateController that graph changes are registered with
     *
     * @return The UpdateController that changes are registered with
     */
    public UpdateController<GraphReadMethods> getUpdateController() {
        return updateController;
    }

    /**
     * Get the component that updates whenever there is a new active graph
     *
     * @return An UpdateComponent.
     */
    public UpdateComponent<GraphReadMethods> getNewGraphUpdateComponent() {
        return newGraphUpdateComponent;
    }

    /**
     * Get the component that updates whenever there is any type of change to
     * the current graph
     *
     * @return An UpdateComponent.
     */
    public UpdateComponent<GraphReadMethods> getGlobalUpdateComponent() {
        return globalUpdateComponent;
    }

    /**
     * Get the component that updates whenever there is a change to the current
     * graph's structure
     *
     * @return An UpdateComponent
     */
    public UpdateComponent<GraphReadMethods> getStructureUpdateComponent() {
        return structureUpdateComponent;
    }

    /**
     * Get the component that updates whenever there is a change to the current
     * graph's attributes
     *
     * @return An UpdateComponent
     */
    public UpdateComponent<GraphReadMethods> getAttributeUpdateComponent() {
        return attributeUpdateComponent;
    }

    /**
     * Create a component that can be set updates whenever there is a change to
     * the current graph's value for a specific attribute.
     * <p>
     * Note that initially this component is not listening to any graph
     * attributes. To get it to do this the
     * {@link AttributeUpdateComponent#setAttribute setAttribute} method must be
     * used.
     *
     * @return The created UpdateComponent
     */
    public AttributeUpdateComponent createAttributeUpdateComponent() {
        AttributeUpdateComponent component = new AttributeUpdateComponent();
        attributeUpdateComponents.add(component);
        return component;
    }

    /**
     * Create a component that updates whenever there is a change to the current
     * graph's value for the specified attribute.
     *
     * @param elementType The element type of the attribute
     * @param label The label of the attribute
     * @param lock Whether or not to lock the update controller so that the
     * update cycle can't begin while the component's attribute of interest is
     * being set
     * @return The created UpdateComponent
     */
    public AttributeUpdateComponent createAttributeUpdateComponent(GraphElementType elementType, String label, boolean lock) {
        AttributeUpdateComponent component = createAttributeUpdateComponent();
        component.setAttribute(elementType, label, lock);
        return component;
    }

    /**
     * Create a component that updates whenever there is a change to the current
     * graph's value for the specified attribute. The update controller is
     * locked while the attribute is set.
     *
     * @param elementType The element type of the attribute
     * @param label The label of the attribute
     * @return The created UpdateComponent
     */
    public AttributeUpdateComponent createAttributeUpdateComponent(GraphElementType elementType, String label) {
        return createAttributeUpdateComponent(elementType, label, true);
    }

    /**
     * Create a component that updates whenever there is a change to the current
     * graph's value for the specified schema attribute.
     *
     * @param attribute The SchemaAttribute of interest
     * @param lock Whether or not to lock the update controller so that the
     * update cycle can't begin while the component's attribute of interest is
     * being set
     * @return The created UpdateComponent
     */
    public AttributeUpdateComponent createAttributeUpdateComponent(SchemaAttribute attribute, boolean lock) {
        AttributeUpdateComponent component = createAttributeUpdateComponent();
        component.setAttribute(attribute, lock);
        return component;
    }

    /**
     * Create a component that updates whenever there is a change to the current
     * graph's value for the specified schema attribute. The update controller
     * is locked while the attribute is set.
     *
     * @param attribute The SchemaAttribute of interest update cycle can't begin
     * while the component's attribute of interest is being set
     * @return The created UpdateComponent
     */
    public AttributeUpdateComponent createAttributeUpdateComponent(SchemaAttribute attribute) {
        return createAttributeUpdateComponent(attribute, true);
    }

    /**
     * Checks for various types of graph changes, registers these changes with
     * the relevant update components, then kicks off the UpdateController's
     * update cycle.
     *
     * @param graph the update will occur using this graph read lock.
     */
    public void update(GraphReadMethods graph) {

        if (currentGraphId == null) {
            if (graph != null) {
                currentGraphId = graph.getId();
                handleNewGraph(graph);
            }
        } else {
            if (graph == null) {
                currentGraphId = null;
                handleNullGraph();
            } else {
                if (currentGraphId.equals(graph.getId())) {
                    handleChangedGraph(graph);
                } else {
                    currentGraphId = graph.getId();
                    handleNewGraph(graph);
                }
            }
        }

        updateController.update(graph);
    }

    /**
     * Handles the case where the graph has changed from not null to null.
     */
    private void handleNullGraph() {
        updateController.registerChange(newGraphUpdateComponent);
    }

    /**
     * Handles the case where a new graph is now active.
     */
    private void handleNewGraph(GraphReadMethods graph) {

        globalModificationCounter = graph.getGlobalModificationCounter();
        structureModificationCounter = graph.getStructureModificationCounter();
        attributeModificationCounter = graph.getAttributeModificationCounter();

        updateController.registerChange(newGraphUpdateComponent);
        updateController.registerChange(globalUpdateComponent);
        updateController.registerChange(structureUpdateComponent);
        updateController.registerChange(attributeUpdateComponent);

        // Initialise each attribute from scratch.
        for (AttributeUpdateComponent component : attributeUpdateComponents) {
            component.reset(graph);
        }
    }

    /**
     * Handles the case where the graph has changed.
     */
    private void handleChangedGraph(GraphReadMethods graph) {

        // Has the graph changed in any way?
        final long oldGlobalModificationCounter = globalModificationCounter;
        globalModificationCounter = graph.getGlobalModificationCounter();
        if (globalModificationCounter != oldGlobalModificationCounter) {

            // Register that the graph has changed.
            updateController.registerChange(globalUpdateComponent);

            // Has the structure of the graph changed in any way?
            final long oldStructureModificationCounter = structureModificationCounter;
            structureModificationCounter = graph.getStructureModificationCounter();
            if (structureModificationCounter != oldStructureModificationCounter) {

                // Register that the graph structure has changed.
                updateController.registerChange(structureUpdateComponent);
            }

            // Has an attribute been addeded or removed?
            final long oldAttributeModificationCounter = attributeModificationCounter;
            attributeModificationCounter = graph.getAttributeModificationCounter();
            if (attributeModificationCounter != oldAttributeModificationCounter) {
                // Register that an attribute has been added or removed
                updateController.registerChange(attributeUpdateComponent);

                // Initialise each attribute from scratch.
                for (AttributeUpdateComponent component : attributeUpdateComponents) {
                    component.reset(graph);
                }

            } else {

                // Update each attribute
                for (AttributeUpdateComponent component : attributeUpdateComponents) {
                    component.detectChange(graph);
                }
            }
        }
    }

    /**
     * An extension of {@link UpdateComponent} that precludes depending on other
     * components.
     */
    private class SimpleUpdateComponent extends UpdateComponent<GraphReadMethods> {

        public SimpleUpdateComponent(String name) {
            super(name, -1);
        }

        @Override
        public void dependOn(UpdateComponent<GraphReadMethods> parent) {
            throw new UnsupportedOperationException("unable to depend on other components");
        }

        @Override
        protected boolean update(GraphReadMethods updateState) {
            return true;
        }
    }

    /**
     * An extension of {@link UpdateComponent} specifically designed to update
     * based on changes to the value of a graph attribute.
     */
    public class AttributeUpdateComponent extends UpdateComponent<GraphReadMethods> {

        private GraphElementType elementType = null;
        private String label = null;
        private boolean attributeUpdated = false;

        private int id = Graph.NOT_FOUND;

        private long modificationCounter = -1;

        /**
         * Creates an AttributeUpdateComponent. Initially there is no attribute
         * of interest, hence this component won't update.
         */
        public AttributeUpdateComponent() {
            super(-1);
        }

        /**
         * Unset the attribute of interest, thereby returning this component to
         * a state where it is not updating.
         *
         * @param lock Whether or not to lock the update controller so that the
         * update cycle can't begin while the attribute is being unset.
         */
        public void disable(boolean lock) {
            setAttribute(null, null, lock);
        }

        /**
         * Unset the attribute of interest, thereby returning this component to
         * a state where it is not updating. The update controller is locked
         * while the attribute is unset.
         */
        public void disable() {
            disable(true);
        }

        /**
         * Set the attribute of interest, thereby ensuring this component will
         * update in response to changes in the values of the attribute on the
         * current graph.
         *
         * @param elementType The element type of the attribute
         * @param label The label of the attribute
         * @param lock Whether or not to lock the update controller so that the
         * update cycle can't begin while the attribute is being set.
         */
        public void setAttribute(GraphElementType elementType, String label, boolean lock) {
            if (this.label == null) {
                if (this.elementType == elementType && label == null) {
                    return;
                }
            } else {
                if (this.elementType == elementType && this.label.equals(label)) {
                    return;
                }
            }
            if (lock) {
                updateController.lock();
            }
            this.elementType = elementType;
            this.label = label;
            attributeUpdated = true;
            if (lock) {
                updateController.release();
            }
        }

        /**
         * Set the attribute of interest, thereby ensuring this component will
         * update in response to changes in the values of the attribute on the
         * current graph. The update controller is locked while the attribute is
         * being set.
         *
         * @param elementType The element type of the attribute
         * @param label The label of the attribute
         */
        public void setAttribute(GraphElementType elementType, String label) {
            setAttribute(elementType, label, true);
        }

        /**
         * Set the attribute of interest, thereby ensuring this component will
         * update in response to changes in the values of the attribute on the
         * current graph.
         *
         * @param attribute The SchemaAttribute of interest
         * @param lock Whether or not to lock the update controller so that the
         * update cycle can't begin while the attribute is being set.
         */
        public void setAttribute(SchemaAttribute attribute, boolean lock) {
            setAttribute(attribute.getElementType(), attribute.getName(), lock);
        }

        /**
         * Set the attribute of interest, thereby ensuring this component will
         * update in response to changes in the values of the attribute on the
         * current graph. The update controller is locked while the attribute is
         * being set.
         *
         * @param attribute The SchemaAttribute of interest
         */
        public void setAttribute(SchemaAttribute attribute) {
            setAttribute(attribute, true);
        }

        private void reset(GraphReadMethods graph) {
            id = label == null ? Graph.NOT_FOUND : graph.getAttribute(elementType, label);
            if (id == Graph.NOT_FOUND) {
                modificationCounter = -1;
            } else {
                modificationCounter = graph.getValueModificationCounter(id);
            }
            updateController.registerChange(this);
        }

        private void detectChange(GraphReadMethods graph) {
            if (attributeUpdated) {
                attributeUpdated = false;
                reset(graph);
            } else if (id != Graph.NOT_FOUND) {
                long oldModificationCounter = modificationCounter;
                modificationCounter = graph.getValueModificationCounter(id);
                if (modificationCounter != oldModificationCounter) {
                    updateController.registerChange(this);
                }
            } else {
                modificationCounter = -1;
            }
        }

        @Override
        protected boolean update(GraphReadMethods updateState) {
            return true;
        }

        @Override
        public String toString() {
            return "Attribute Change: " + label + "(" + elementType + ")";
        }
    }

}
