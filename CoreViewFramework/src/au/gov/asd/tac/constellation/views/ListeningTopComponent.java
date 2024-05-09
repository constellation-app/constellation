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
package au.gov.asd.tac.constellation.views;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.AttributeCountMonitor;
import au.gov.asd.tac.constellation.graph.monitor.AttributeValueMonitor;
import au.gov.asd.tac.constellation.graph.monitor.GlobalMonitor;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.monitor.Monitor;
import au.gov.asd.tac.constellation.graph.monitor.MonitorTransition;
import au.gov.asd.tac.constellation.graph.monitor.MonitorTransitionFilter;
import au.gov.asd.tac.constellation.graph.monitor.StructureMonitor;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.openide.windows.TopComponent;

/**
 * A generic top component which listens for changes on the graph.
 *
 * @param <P> The class used by this {@link TopComponent} to display content.
 *
 * @author cygnus_x-1
 */
public abstract class ListeningTopComponent<P> extends AbstractTopComponent<P> implements GraphManagerListener, GraphChangeListener, PreferenceChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ListeningTopComponent.class.getName());

    private static final String ADDED_MONITOR_COUNT_FORMAT = "Added AttributeValueMonitor, count is {0}";

    protected final Map<GlobalMonitor, Consumer<Graph>> globalMonitors;
    protected final Map<StructureMonitor, Consumer<Graph>> structureMonitors;
    protected final Map<AttributeCountMonitor, Consumer<Graph>> attributeCountMonitors;
    protected final Map<AttributeValueMonitor, Tuple<Consumer<Graph>, MonitorTransitionFilter>> attributeValueMonitors;
    protected final Map<String, Consumer<PreferenceChangeEvent>> preferenceMonitors;
    protected final Set<Object> ignoredEvents;
    protected Graph currentGraph;

    protected ListeningTopComponent() {
        super();
        this.globalMonitors = Collections.synchronizedMap(new HashMap<>());
        this.structureMonitors = Collections.synchronizedMap(new HashMap<>());
        this.attributeCountMonitors = Collections.synchronizedMap(new HashMap<>());
        this.attributeValueMonitors = Collections.synchronizedMap(new HashMap<>());
        this.preferenceMonitors = Collections.synchronizedMap(new HashMap<>());
        this.ignoredEvents = new HashSet<>();

        preferenceMonitors.put(ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT, event -> updateFont());
        preferenceMonitors.put(ApplicationPreferenceKeys.FONT_SIZE_DEFAULT, event -> updateFont());
    }

    /**
     * Get the graph this top component is currently listening to.
     *
     * @return the active {@link Graph}.
     */
    public final Graph getCurrentGraph() {
        return currentGraph;
    }

    @Override
    public final void componentOpened() {
        super.componentOpened();
        LOGGER.finer("ComponentOpened");
        preferenceMonitors.keySet().forEach(preference -> PreferenceUtilities.addPreferenceChangeListener(preference, this));
        GraphManager.getDefault().addGraphManagerListener(ListeningTopComponent.this);
        newActiveGraph(GraphManager.getDefault().getActiveGraph());
        handleComponentOpened();
    }

    @Override
    public final void componentClosed() {
        super.componentClosed();
        LOGGER.finer("ComponentClosed");
        preferenceMonitors.keySet().forEach(preference -> PreferenceUtilities.removePreferenceChangeListener(preference, this));
        GraphManager.getDefault().removeGraphManagerListener(ListeningTopComponent.this);
        newActiveGraph(null);
        handleComponentClosed();
    }

    @Override
    public final void graphOpened(final Graph graph) {
        LOGGER.finer("GraphOpened");

        handleGraphOpened(graph);
    }

    @Override
    public final void graphClosed(final Graph graph) {
        LOGGER.finer("GraphClosed");

        handleGraphClosed(graph);
    }

    @Override
    public final void newActiveGraph(final Graph graph) {
        LOGGER.finer("NewActiveGraph");

        if (currentGraph != graph) {
            if (currentGraph != null) {
                currentGraph.removeGraphChangeListener(this);
                currentGraph = null;
            }
            if (graph != null) {
                currentGraph = graph;
                currentGraph.addGraphChangeListener(this);

                final ReadableGraph readableGraph = currentGraph.getReadableGraph();
                try {
                    final Map<GlobalMonitor, Consumer<Graph>> globalMonitorsCopy;
                    synchronized (globalMonitors) {
                        globalMonitorsCopy = new HashMap<>(globalMonitors);
                    }
                    globalMonitorsCopy.forEach((monitor, handler) -> monitor.update(readableGraph));
                    final Map<StructureMonitor, Consumer<Graph>> structureMonitorsCopy;
                    synchronized (globalMonitors) {
                        structureMonitorsCopy = new HashMap<>(structureMonitors);
                    }
                    structureMonitorsCopy.forEach((monitor, handler) -> monitor.update(readableGraph));
                    final Map<AttributeCountMonitor, Consumer<Graph>> attributeCountMonitorsCopy;
                    synchronized (globalMonitors) {
                        attributeCountMonitorsCopy = new HashMap<>(attributeCountMonitors);
                    }
                    attributeCountMonitorsCopy.forEach((monitor, handler) -> monitor.update(readableGraph));
                    final Map<AttributeValueMonitor, Tuple<Consumer<Graph>, MonitorTransitionFilter>> attributeMonitorsCopy;
                    synchronized (globalMonitors) {
                        attributeMonitorsCopy = new HashMap<>(attributeValueMonitors);
                    }
                    attributeMonitorsCopy.forEach((monitor, handler) -> monitor.update(readableGraph));
                } finally {
                    readableGraph.release();
                }

                graphChanged(null);
            }
        }

        handleNewGraph(graph);
    }

    @Override
    public final void graphChanged(final GraphChangeEvent event) {
        LOGGER.finer("GraphChange");

        if (event != null && ignoredEvents.contains(event.getDescription())) {
            LOGGER.log(Level.FINER, "IgnoringEvent::{0}", event.getDescription());
            return;
        }

        final ReadableGraph readableGraph = currentGraph.getReadableGraph();
        try {
            final Map<GlobalMonitor, Consumer<Graph>> globalMonitorsCopy;
            synchronized (globalMonitors) {
                globalMonitorsCopy = new HashMap<>(globalMonitors);
            }
            globalMonitorsCopy.forEach((monitor, handler) -> {
                LOGGER.finer("GraphChanged::CheckGlobal");

                if (monitor.update(readableGraph) == MonitorTransition.CHANGED) {
                    LOGGER.finer("GraphChanged::UpdateGlobal");

                    if (handler != null) {
                        handler.accept(currentGraph);
                    }
                }
            });
            final Map<StructureMonitor, Consumer<Graph>> structureMonitorsCopy;
            synchronized (globalMonitors) {
                structureMonitorsCopy = new HashMap<>(structureMonitors);
            }
            structureMonitorsCopy.forEach((monitor, handler) -> {
                LOGGER.finer("GraphChanged::CheckStructure");

                if (monitor.update(readableGraph) == MonitorTransition.CHANGED) {
                    LOGGER.finer("GraphChanged::UpdateStructure");

                    if (handler != null) {
                        handler.accept(currentGraph);
                    }
                }
            });
            final Map<AttributeCountMonitor, Consumer<Graph>> attributeCountMonitorsCopy;
            synchronized (globalMonitors) {
                attributeCountMonitorsCopy = new HashMap<>(attributeCountMonitors);
            }
            attributeCountMonitorsCopy.forEach((monitor, handler) -> {
                LOGGER.finer("GraphChanged::CheckAttributeCount");

                if (monitor.update(readableGraph) == MonitorTransition.CHANGED) {
                    LOGGER.finer("GraphChanged::UpdateAttributeCount");

                    if (handler != null) {
                        handler.accept(currentGraph);
                    }
                }
            });
            final Map<AttributeValueMonitor, Tuple<Consumer<Graph>, MonitorTransitionFilter>> attributeMonitorsCopy;
            synchronized (globalMonitors) {
                attributeMonitorsCopy = new HashMap<>(attributeValueMonitors);
            }
            attributeMonitorsCopy.forEach((monitor, handlerPair) -> {
                LOGGER.finer("GraphChanged::CheckAttribute");

                final Consumer<Graph> handler = handlerPair.getFirst();
                final MonitorTransitionFilter transitionFilter = handlerPair.getSecond();
                monitor.update(readableGraph);
                if (transitionFilter.matchesTransitions(monitor)) {
                    LOGGER.log(Level.FINER, "GraphChanged::UpdateAttribute::{0}", monitor.getName());

                    if (handler != null) {
                        handler.accept(currentGraph);
                    }
                }
            });
        } finally {
            readableGraph.release();
        }

        handleGraphChange(event);
    }

    @Override
    public final void preferenceChange(final PreferenceChangeEvent event) {
        LOGGER.finer("PreferenceChange");

        final Map<String, Consumer<PreferenceChangeEvent>> preferenceMonitorsCopy;
        synchronized (preferenceMonitors) {
            preferenceMonitorsCopy = new HashMap<>(preferenceMonitors);
        }
        preferenceMonitorsCopy.forEach((preference, handler) -> {
            LOGGER.log(Level.FINER, "ManualUpdate::UpdatePreferences::{0}", preference);

            if (handler != null) {
                handler.accept(event);
            }
        });

        handlePreferenceChange(event);
    }

    /**
     * Updates this top component in response to a font preference change.
     */
    protected abstract void updateFont();

    /**
     * Manually trigger the handlers of all registered monitors to fire.
     */
    protected final void manualUpdate() {
        final Map<GlobalMonitor, Consumer<Graph>> globalMonitorsCopy;
        synchronized (globalMonitors) {
            globalMonitorsCopy = new HashMap<>(globalMonitors);
        }
        globalMonitorsCopy.forEach((monitor, handler) -> {
            LOGGER.finer("ManualUpdate::UpdateGlobal");

            if (handler != null) {
                handler.accept(currentGraph);
            }
        });
        final Map<StructureMonitor, Consumer<Graph>> structureMonitorsCopy;
        synchronized (globalMonitors) {
            structureMonitorsCopy = new HashMap<>(structureMonitors);
        }
        structureMonitorsCopy.forEach((monitor, handler) -> {
            LOGGER.finer("ManualUpdate::UpdateStructure");

            if (handler != null) {
                handler.accept(currentGraph);
            }
        });
        final Map<AttributeCountMonitor, Consumer<Graph>> attributeCountMonitorsCopy;
        synchronized (globalMonitors) {
            attributeCountMonitorsCopy = new HashMap<>(attributeCountMonitors);
        }
        attributeCountMonitorsCopy.forEach((monitor, handler) -> {
            LOGGER.finer("ManualUpdate::UpdateAttributeCount");

            if (handler != null) {
                handler.accept(currentGraph);
            }
        });
        final Map<AttributeValueMonitor, Tuple<Consumer<Graph>, MonitorTransitionFilter>> attributeMonitorsCopy;
        synchronized (globalMonitors) {
            attributeMonitorsCopy = new HashMap<>(attributeValueMonitors);
        }
        attributeMonitorsCopy.forEach((monitor, handlerPair) -> {
            final Consumer<Graph> handler = handlerPair.getFirst();
            LOGGER.log(Level.FINER, "ManualUpdate::UpdateAttribute::{0}", monitor.getName());

            if (handler != null) {
                handler.accept(currentGraph);
            }
        });
    }

    /**
     * The default transition filter which will be used by all registered
     * monitors unless otherwise specified.
     *
     * @return
     */
    protected final MonitorTransitionFilter getDefaultTransitionFilter() {
        return new MonitorTransitionFilter(
                MonitorTransition.UNDEFINED_TO_PRESENT,
                MonitorTransition.CHANGED,
                MonitorTransition.ADDED,
                MonitorTransition.REMOVED_AND_ADDED
        );
    }

    /**
     * Initialise the given monitor using the current graph, or reset it if the
     * current graph is null.
     *
     * @param monitor the {@link Monitor} to initialise
     */
    private void initialiseMonitor(final Monitor monitor) {
        if (currentGraph != null) {
            final ReadableGraph readableGraph = currentGraph.getReadableGraph();
            try {
                monitor.update(readableGraph);
            } finally {
                readableGraph.release();
            }
        } else {
            monitor.reset();
        }
    }

    /**
     * Define how your TopComponent handles any change to the graph.
     *
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to any change to the
     * graph.
     * @return the {@link GlobalMonitor} attached to your handler.
     */
    protected GlobalMonitor addGlobalChangeHandler(final Consumer<Graph> handler) {
        final GlobalMonitor globalMonitor = new GlobalMonitor();
        initialiseMonitor(globalMonitor);
        globalMonitors.put(globalMonitor, handler);
        LOGGER.log(Level.FINE, "Added GlobalMonitor, count is {0}", globalMonitors.size());
        return globalMonitor;
    }

    /**
     * Update the attribute change handler attached to the specified
     * {@link GlobalMonitor}.
     *
     * @param monitor the {@link GlobalMonitor} attached to the handler you wish
     * to update.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines the updated method in which this top component should respond
     * to structural changes on the graph.
     */
    protected void updateGlobalChangeHandler(final GlobalMonitor monitor, final Consumer<Graph> handler) {
        globalMonitors.put(monitor, handler);
        LOGGER.log(Level.FINE, "Updated GlobalMonitor, count is {0}", globalMonitors.size());
    }

    /**
     * Remove the attribute change handler attached to the specified
     * {@link GlobalMonitor}.
     *
     * @param monitor the {@link GlobalMonitor} attached to the handler you wish
     * to remove.
     */
    protected void removeGlobalChangeHandler(final GlobalMonitor monitor) {
        globalMonitors.remove(monitor);
        LOGGER.log(Level.FINE, "Removed GlobalMonitor, count is {0}\nMonitors are: {1}", new Object[]{globalMonitors.size(), globalMonitors.keySet()});
    }

    /**
     * Define how your TopComponent handles changes to the structure of the
     * graph.
     *
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to structural changes on
     * the graph.
     * @return the {@link StructureMonitor} attached to your handler.
     */
    protected StructureMonitor addStructureChangeHandler(final Consumer<Graph> handler) {
        final StructureMonitor structureMonitor = new StructureMonitor();
        initialiseMonitor(structureMonitor);
        structureMonitors.put(structureMonitor, handler);
        LOGGER.log(Level.FINE, "Added StructureMonitor, count is {0}", structureMonitors.size());
        return structureMonitor;
    }

    /**
     * Update the attribute change handler attached to the specified
     * {@link StructureMonitor}.
     *
     * @param monitor the {@link StructureMonitor} attached to the handler you
     * wish to update.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines the updated method in which this top component should respond
     * to structural changes on the graph.
     */
    protected void updateStructureChangeHandler(final StructureMonitor monitor, final Consumer<Graph> handler) {
        structureMonitors.put(monitor, handler);
        LOGGER.log(Level.FINE, "Updated StructureMonitor, count is {0}", structureMonitors.size());
    }

    /**
     * Remove the attribute change handler attached to the specified
     * {@link StructureMonitor}.
     *
     * @param monitor the {@link StructureMonitor} attached to the handler you
     * wish to remove.
     */
    protected void removeStructureChangeHandler(final StructureMonitor monitor) {
        structureMonitors.remove(monitor);
        LOGGER.log(Level.FINE, "Removed StructureMonitor, count is {0}\nMonitors are: {1}", new Object[]{structureMonitors.size(), structureMonitors.keySet()});
    }

    /**
     * Define how your TopComponent handles changes to the number of attributes
     * on the graph.
     *
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to changes to the number
     * of attributes on the graph.
     * @return the {@link AttributeCountMonitor} attached to your handler.
     */
    protected AttributeCountMonitor addAttributeCountChangeHandler(final Consumer<Graph> handler) {
        final AttributeCountMonitor attributeCountMonitor = new AttributeCountMonitor();
        initialiseMonitor(attributeCountMonitor);
        attributeCountMonitors.put(attributeCountMonitor, handler);
        LOGGER.log(Level.FINE, "Added AttributeCountMonitor, count is {0}", attributeCountMonitors.size());
        return attributeCountMonitor;
    }

    /**
     * Update the attribute change handler attached to the specified
     * {@link AttributeCountMonitor}.
     *
     * @param monitor the {@link AttributeCountMonitor} attached to the handler
     * you wish to update.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines the updated method in which this top component should respond
     * to changes to the number of attributes on the graph.
     */
    protected void updateAttributeCountChangeHandler(final AttributeCountMonitor monitor, final Consumer<Graph> handler) {
        attributeCountMonitors.put(monitor, handler);
        LOGGER.log(Level.FINE, "Updated AttributeCountMonitor, count is {0}", attributeCountMonitors.size());
    }

    /**
     * Remove the attribute change handler attached to the specified
     * {@link AttributeCountMonitor}.
     *
     * @param monitor the {@link AttributeCountMonitor} attached to the handler
     * you wish to remove.
     */
    protected void removeAttributeCountChangeHandler(final AttributeCountMonitor monitor) {
        attributeCountMonitors.remove(monitor);
        LOGGER.log(Level.FINE, "Removed AttributeCountMonitor, count is {0}\nMonitors are: {1}", new Object[]{attributeCountMonitors.size(), attributeCountMonitors.keySet()});
    }

    /**
     * Defines how your TopComponent handles changes to special attributes which
     * affect it.
     *
     * @param attribute a {@link SchemaAttribute} representing the state of *
     * this top component.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to changes to the value
     * of the specified attribute.
     * @return the {@link AttributeValueMonitor} attached to your handler.
     */
    protected AttributeValueMonitor addAttributeValueChangeHandler(final SchemaAttribute attribute, final Consumer<Graph> handler) {
        final AttributeValueMonitor attributeValueMonitor = new AttributeValueMonitor(attribute);
        initialiseMonitor(attributeValueMonitor);
        attributeValueMonitors.put(attributeValueMonitor, Tuple.create(handler, getDefaultTransitionFilter()));
        LOGGER.log(Level.FINE, ADDED_MONITOR_COUNT_FORMAT, attributeValueMonitors.size());
        return attributeValueMonitor;
    }

    /**
     * Defines how your TopComponent handles changes to special attributes which
     * affect it.
     *
     * @param elementType a {@link GraphElementType} representing the type of
     * attribute this top component should listen to.
     * @param attributeName a {@link String} representing the name of the
     * attribute this top component should listen to.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to changes to the value
     * of the specified attribute.
     * @return the {@link AttributeValueMonitor} attached to your handler.
     */
    protected AttributeValueMonitor addAttributeValueChangeHandler(final GraphElementType elementType, final String attributeName, final Consumer<Graph> handler) {
        final AttributeValueMonitor attributeValueMonitor = new AttributeValueMonitor(elementType, attributeName);
        initialiseMonitor(attributeValueMonitor);
        attributeValueMonitors.put(attributeValueMonitor, Tuple.create(handler, getDefaultTransitionFilter()));
        LOGGER.log(Level.FINE, ADDED_MONITOR_COUNT_FORMAT, attributeValueMonitors.size());
        return attributeValueMonitor;
    }

    /**
     * Defines how your TopComponent handles changes to special attributes which
     * affect it.
     *
     * @param attribute a {@link SchemaAttribute} this top component should
     * listen to.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to changes to the value
     * of the specified attribute.
     * @param filter a {@link MonitorTransitionFilter} describing which
     * {@link MonitorTransition} objects this top component should react to.
     * @return the {@link AttributeValueMonitor} attached to your handler.
     */
    protected AttributeValueMonitor addAttributeValueChangeHandler(final SchemaAttribute attribute, final Consumer<Graph> handler, final MonitorTransitionFilter filter) {
        final AttributeValueMonitor attributeValueMonitor = new AttributeValueMonitor(attribute);
        initialiseMonitor(attributeValueMonitor);
        attributeValueMonitors.put(attributeValueMonitor, Tuple.create(handler, filter));
        LOGGER.log(Level.FINE, ADDED_MONITOR_COUNT_FORMAT, attributeValueMonitors.size());
        return attributeValueMonitor;
    }

    /**
     * Defines how your TopComponent handles changes to special attributes which
     * affect it. This method will return the {@link AttributeValueMonitor}
     * attached to your handler, which you can later use to change or remove
     * that handler.
     *
     * @param elementType a {@link GraphElementType} representing the type of
     * attribute this top component should listen to.
     * @param attributeName a {@link String} representing the name of the
     * attribute this top component should listen to.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines how this top component should respond to changes to the value
     * of the specified attribute.
     * @param filter a {@link MonitorTransitionFilter} describing which
     * {@link MonitorTransition} objects this top component should react to.
     * @return the {@link AttributeValueMonitor} attached to your handler.
     */
    protected AttributeValueMonitor addAttributeValueChangeHandler(final GraphElementType elementType, final String attributeName, final Consumer<Graph> handler, final MonitorTransitionFilter filter) {
        final AttributeValueMonitor attributeValueMonitor = new AttributeValueMonitor(elementType, attributeName);
        initialiseMonitor(attributeValueMonitor);
        attributeValueMonitors.put(attributeValueMonitor, Tuple.create(handler, filter));
        LOGGER.log(Level.FINE, ADDED_MONITOR_COUNT_FORMAT, attributeValueMonitors.size());
        return attributeValueMonitor;
    }

    /**
     * Update the attribute change handler attached to the specified
     * {@link AttributeValueMonitor}.
     *
     * @param monitor the {@link AttributeValueMonitor} attached to the handler
     * you wish to update.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines the updated method in which this top component should respond
     * to changes to the value of the specified attribute.
     */
    protected void updateAttributeValueChangeHandler(final AttributeValueMonitor monitor, final Consumer<Graph> handler) {
        attributeValueMonitors.put(monitor, Tuple.create(handler, getDefaultTransitionFilter()));
        LOGGER.log(Level.FINE, "Updated AttributeValueMonitor, count is {0}", attributeValueMonitors.size());
    }

    /**
     * Update the attribute change handler attached to the specified
     * {@link AttributeValueMonitor}.
     *
     * @param monitor the {@link AttributeValueMonitor} attached to the handler
     * you wish to update.
     * @param handler a {@link Consumer} of {@link Graph} objects which
     * determines the updated method in which this top component should respond
     * to changes to the value of the specified attribute.
     * @param filter a {@link MonitorTransitionFilter} describing which
     * {@link MonitorTransition} objects this top component should react to.
     */
    protected void updateAttributeValueChangeHandler(final AttributeValueMonitor monitor, final Consumer<Graph> handler, final MonitorTransitionFilter filter) {
        attributeValueMonitors.put(monitor, Tuple.create(handler, filter));
        LOGGER.log(Level.FINE, "Updated AttributeValueMonitor, count is {0}", attributeValueMonitors.size());
    }

    /**
     * Remove the attribute change handler attached to the specified
     * {@link AttributeValueMonitor}.
     *
     * @param monitor the {@link AttributeValueMonitor} attached to the handler
     * you wish to remove.
     */
    protected void removeAttributeValueChangeHandler(final AttributeValueMonitor monitor) {
        attributeValueMonitors.remove(monitor);
        LOGGER.log(Level.FINE, "Removed AttributeCountMonitor, count is {0}\nMonitors are: {1}", new Object[]{attributeValueMonitors.size(), attributeValueMonitors.keySet()});
    }

    /**
     * Defines how your TopComponent handles changes to the specified
     * preference.
     *
     * @param preference the key of the preference you wish to listen to.
     * @param handler a {@link Consumer} of {@link PreferenceChangeEvent}
     * objects which determines how this top component should respond to changes
     * to the specified preference.
     */
    protected void addPreferenceChangeHandler(final String preference, final Consumer<PreferenceChangeEvent> handler) {
        preferenceMonitors.put(preference, handler);
        LOGGER.log(Level.FINE, "Added preference monitor, count is {0}", preferenceMonitors.size());
    }

    /**
     * Remove the preference change handler attached to the specified
     * preference.
     *
     * @param preference the key of the preference you wish to stop listening
     * to.
     */
    protected void removePreferenceChangeHandler(final String preference) {
        preferenceMonitors.remove(preference);
        LOGGER.log(Level.FINE, "Removed preference monitor, count is {0}\nMonitors are: {1}", new Object[]{preferenceMonitors.size(), preferenceMonitors.keySet()});
    }

    /**
     * Registers the id of an event which should be ignored by this top
     * component. If you wish to ignore the execution of a particular plugin,
     * this event id should be the name of that plugin.
     *
     * @param eventId a {@link String} representing the id of an event.
     */
    protected void addIgnoredEvent(final String eventId) {
        ignoredEvents.add(eventId);
    }

    /**
     * Registers the id of an event which should no longer be ignored by this
     * top component.
     *
     * @param eventId a {@link String} representing the id of an event.
     */
    protected void removeIgnoredEvent(final String eventId) {
        ignoredEvents.remove(eventId);
    }

    /**
     * Allows you to react when the top component is opened.
     */
    protected void handleComponentOpened() {
        // DO NOTHING
    }

    /**
     * Allows you to react when the top component is closed.
     */
    protected void handleComponentClosed() {
        // DO NOTHING
    }

    /**
     * Allows you to react when a graph is opened.
     *
     * @param graph the new {@link Graph} being listened to.
     */
    protected void handleGraphOpened(final Graph graph) {
        // DO NOTHING
    }

    /**
     * Allows you to react when a new graph is closed.
     *
     * @param graph the new {@link Graph} being listened to.
     */
    protected void handleGraphClosed(final Graph graph) {
        // DO NOTHING
    }

    /**
     * Allows you to react when a new graph becomes active.
     *
     * @param graph the new {@link Graph} being listened to.
     */
    protected void handleNewGraph(final Graph graph) {
        // DO NOTHING
    }

    /**
     * Allows you to react when a graph changes.
     *
     * @param event the {@link GraphChangeEvent} describing the change.
     */
    protected void handleGraphChange(final GraphChangeEvent event) {
        // DO NOTHING
    }

    /**
     * Allows you to react when a preference changes.
     *
     * @param event the {
     * @PreferenceChangeEvent} describing the change.
     */
    protected void handlePreferenceChange(final PreferenceChangeEvent event) {
        // DO NOTHING
    }
}
