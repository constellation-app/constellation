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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convenience class to hold and manage all state based information about
 * gesture processing for the {@link DefaultInteractionEventHandler}.
 * <p>
 * The state based information includes thing such as remembered mouse points
 * where an action originated, the current action that is being conducted by the
 * user, etc.
 * <p>
 * Note that this class extends {@link HitState} as results of hit tests need to
 * synchronised with gesture information.
 *
 * @author twilight_sparkle
 */
final class EventState extends HitState {

    /**
     * Constants defining the various actions that may be performed.
     */
    enum SceneAction {

        ROTATING("Rotate"),
        PANNING("Pan"),
        DRAG_NODES("Drag"),
        SELECTING("Selection"),
        FREEFORM_SELECTING("Freeform Selection"),
        CREATING("Add Nodes"),
        NONE(""),;

        public final String actionName;

        SceneAction(final String actionName) {
            this.actionName = actionName;
        }
    }

    /**
     * Constants defining the various modes of creating graph elements.
     */
    enum CreationMode {

        CREATING_VERTEX,
        CREATING_TRANSACTION,
        FINISHING_TRANSACTION,
        NONE,;
    }

    // Names for commonly used points
    public static final String DRAG_POINT = "drag_point";
    public static final String PRESSED_POINT = "pressed_point";
    public static final String REFERENCE_POINT = "reference_point";
    public static final String WHEEL_POINT = "wheel_point";

    private final Map<String, Point> pointStore = new HashMap<>();
    private final Set<String> validPoints = new HashSet<>();
    private int currentlyPressedButton;

    private SceneAction currentAction;
    private CreationMode currentCreationMode;

    //Add Vertex/Transaction Specific Variables
    private int addTransactionSourceVertex = Graph.NOT_FOUND;
    private int addTransactionDestinationVertex = Graph.NOT_FOUND;

    private final Set<String> eventNames = new LinkedHashSet<>();

    private Vector3f closestNode;

    /**
     * Create a new EventState.
     */
    EventState() {
        super();
        currentAction = SceneAction.NONE;
        currentCreationMode = CreationMode.NONE;
        setNoButton();
    }

    /**
     * Create an EventState with the same state information as the given
     * EventState.
     *
     * @param other The EventState to copy state information from.
     */
    EventState(EventState other) {
        super(other);
        pointStore.putAll(other.pointStore);
        validPoints.addAll(other.validPoints);
        currentlyPressedButton = other.currentlyPressedButton;
        currentAction = other.currentAction;
        currentCreationMode = other.currentCreationMode;
        addTransactionSourceVertex = other.addTransactionSourceVertex;
        addTransactionDestinationVertex = other.addTransactionDestinationVertex;
    }

    /**
     * Add the name of an event to the list of current events.
     * <p>
     * This method is used to name graph edits - sometimes multiple logical
     * events will occur within the one graph edit (e.g. zooming and panning)
     *
     * @param eventName The name to add.
     */
    void addEventName(final String eventName) {
        eventNames.add(eventName);
    }

    /**
     * Retrieve a comma separated string of all event names added with
     * {@link #addEventName} since this method was last called. Clear the list
     * of added event names.
     *
     * @return A string containing comma separated event names.
     */
    String retrieveAndClearEventNames() {
        final String combinedEventName = eventNames.stream().reduce((n1, n2) -> n1 + "/" + n2).orElse(null);
        eventNames.clear();
        return combinedEventName;
    }

    /**
     * Get the ID of the source vertex for the transaction currently being
     * created.
     * <p>
     * The result is undefined if this event state does not represent that a
     * transaction is currently being added.
     *
     * @return The source vertex ID for the transaction being created
     */
    int getAddTransactionSourceVertex() {
        return addTransactionSourceVertex;
    }

    /**
     * Set the ID of the source vertex for the transaction currently being
     * created.
     *
     * @param addTransactionSourceVertex The ID to set.
     */
    void setAddTransactionSourceVertex(final int addTransactionSourceVertex) {
        this.addTransactionSourceVertex = addTransactionSourceVertex;
    }

    /**
     * Get the ID of the destination vertex for the transaction currently being
     * created.
     * <p>
     * The result is undefined if this event state does not represent that a
     * transaction is being created whose destination vertex has been specified.
     *
     * @return The destionation vertex ID for the transaction being created
     */
    int getAddTransactionDestinationVertex() {
        return addTransactionDestinationVertex;
    }

    /**
     * Set the ID of the destination vertex for the transaction currently being
     * created.
     *
     * @param addTransactionDestinationVertex The ID to set.
     */
    void setAddTransactionDestinationVertex(final int addTransactionDestinationVertex) {
        this.addTransactionDestinationVertex = addTransactionDestinationVertex;
    }

    /**
     * Get the {@link SceneAction} currently in progress by the user.
     *
     * @return The current {@link SceneAction}
     */
    SceneAction getCurrentAction() {
        return currentAction;
    }

    /**
     * Set the current {@link SceneAction}
     *
     * @param sceneAction The {@link SceneAction} to set
     */
    void setCurrentAction(final SceneAction sceneAction) {
        currentAction = sceneAction;
    }

    /**
     * Get the current {@link CreationMode}.
     *
     * @return The current {@link CreationMode}
     */
    CreationMode getCurrentCreationMode() {
        return currentCreationMode;
    }

    /**
     * Set the current {@link CreationMode}.
     *
     * @param creationMode The {@link CreationMode} to set
     */
    void setCurrentCreationMode(final CreationMode creationMode) {
        currentCreationMode = creationMode;
    }

    /**
     * Get whether or not a node closest to the mouse cursor has been
     * determined.
     *
     * @return Whether there is a closest node to the cursor.
     */
    boolean hasClosestNode() {
        return closestNode != null;
    }

    /**
     * Get the closest node to the mouse cursor
     *
     * @return The closest node, or null if no such node has been set.
     */
    Vector3f getClosestNode() {
        return closestNode;
    }

    /**
     * Set the closest node to the mouse cursor
     *
     * @param closestNode The node which is currently closest to the mouse
     * cursor.
     */
    void setClosestNode(final Vector3f closestNode) {
        this.closestNode = closestNode;
    }

    /**
     * Set the AWT ID of the current mouse button that is being held down.
     *
     * @param button The currently held down mouse button
     */
    void setCurrentButton(final int button) {
        currentlyPressedButton = button;
    }

    /**
     * Get the ID of the current mouse button that is being held down.
     *
     * @return The currently held down mouse button, or -1 if no button is
     * currently held down.
     */
    int getCurrentButton() {
        return currentlyPressedButton;
    }

    /**
     * Set that no mouse button is currently held down.
     */
    void setNoButton() {
        currentlyPressedButton = -1;
    }

    /**
     * Store a point against a given list of named keys.
     *
     * @param point The point to store.
     * @param keys The keys to store the point at.
     */
    void storePoint(final Point point, final String... keys) {
        for (final String key : keys) {
            pointStore.put(key, point);
            validPoints.add(key);
        }
    }

    /**
     * get the point corresponding to the specified named key.
     *
     * @param key The key to retrieve for.
     * @return The corresponding point, or <code>null</code> if no point exists
     * for the specified key.
     */
    Point getPoint(final String key) {
        return pointStore.get(key);
    }

    /**
     * Get the first point which corresponds to one of several keys.
     *
     * @param keys The list of keys to retrieve for.
     * @return The first point corresponding to a key in the list, or
     * <code>null</code> if no point exists for any of the specified key.
     */
    Point getFirstValidPoint(final String... keys) {
        for (final String key : keys) {
            if (isPoint(key)) {
                return getPoint(key);
            }
        }
        return null;
    }

    /**
     * Determine whether there is a point stored at the specified key
     *
     * @param key The key to check.
     * @return Wether or not there is a point corresponding to the specified
     * key.
     */
    boolean isPoint(final String key) {
        return validPoints.contains(key);
    }

    /**
     * Determine whether the mouse is currently pressed
     * <p>
     * More specifically checks whether a point is stored under the
     * <code>PRESSED_POINT</code> key.
     *
     * @return Whether or not the mouse is pressed.
     */
    boolean isMousePressed() {
        return isPoint(PRESSED_POINT);
    }

    /**
     * Determine whether the mouse is currently being dragged
     * <p>
     * More specifically checks whether a point is stored under the
     * <code>DRAGGED_POINT</code> key.
     *
     * @return Whether or not the mouse is dragged.
     */
    boolean isMouseDragged() {
        return isPoint(DRAG_POINT);
    }

    /**
     * Invalidate the point stored at the specified key, ie. unmap it.
     *
     * @param key The key to remove the point for.
     */
    void invalidatePoint(final String key) {
        validPoints.remove(key);
    }

    /**
     * Invalidate the points stored at the specified keys.
     *
     * @param keys The keys to remove points for.
     */
    void invalidatePoints(final String... keys) {
        for (String key : keys) {
            validPoints.remove(key);
        }
    }
}
