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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState.HitType;
import au.gov.asd.tac.constellation.graph.interaction.framework.InteractionEventHandler;
import au.gov.asd.tac.constellation.graph.interaction.framework.VisualAnnotator;
import au.gov.asd.tac.constellation.graph.interaction.framework.VisualInteraction;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateTransactionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.draw.CreateVertexPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.select.BoxSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.select.FreeformSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.plugins.select.PointSelectionPlugin;
import au.gov.asd.tac.constellation.graph.interaction.visual.EventState.CreationMode;
import au.gov.asd.tac.constellation.graph.interaction.visual.EventState.SceneAction;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.NewLineModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionBoxModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionFreeformModel;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualOperation;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import com.google.common.primitives.Ints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.openide.util.Lookup;

/**
 * The default implementation of {@link InteractionEventHandler} for
 * CONSTELLATION, corresponding to the standard mouse and keyboard scheme for
 * interacting with the graph.
 * <p>
 * Briefly, this InteractionEventHandler maps the following gestures to
 * functions:
 * <ul>
 * <li> Moving the mouse while no buttons are pressed will perform hit testing
 * and update the cursor if it moves from the background to an element (or vice
 * versa).
 * </li><li> Left clicking an element selects that element
 * </li><li> Left clicking and dragging performs box selection
 * </li><li> Middle clicking a node sets it as the centre of rotation
 * </li><li> Middle clicking and dragging rotates the graph about the X and Y
 * axes.
 * </li><li> Right clicking brings up a context menu
 * </li><li> Right clicking and dragging an element drags that element, along
 * with any other selected elements
 * </li><li> Right clicking and dragging the background pans the graph.
 * </li><li> Double left clicking on the background deselects everything.
 * </li><li> Scrolling the mouse wheel zooms in/out of the graph.
 * </li><li> Holding shift while selecting appends to selection (applies to
 * single element or box selections)
 * </li><li> Holding control while selecting toggle-appends to selection
 * (applies to single element or box selections)
 * </ul>
 * In addition, if in 'creation mode', the following functions will happen
 * instead of selection:
 * <ul>
 * <li> Moving the mouse while no buttons are pressed will update the new line
 * model if a transaction is being created.
 * </li><li> Left clicking a node starts creating a transaction from that node,
 * or finishes a transaction on that node if one is already being created
 * </li><li> Left clicking on the background creates a new node.
 * </ul>
 *
 * @author twilight_sparkle
 */
public class DefaultInteractionEventHandler implements InteractionEventHandler {

    private static final String MIX_ACTION_NAME = "Change Mix Ratio";
    private static final String ZOOM_ACTION_NAME = "Zoom";
    private static final String PAN_ACTION_NAME = "Pan";
    private static final String ROTATE_ACTION_NAME = "Rotate";
    private static final String DRAG_ACTION_NAME = "Drag";
    private static final String SET_CENTRE_ACTION_NAME = "Set Rotation Centre";
    private static final String CONTEXT_MENU_TEXT = "Context Menu: ";
    private static final int STANDARD_DELAY = 500;

    // The queue of gesture handlers for events that have been received from the EDT.
    private final BlockingQueue<GestureHandler> queue = new LinkedBlockingQueue<>();
    // State information
    private EventState eventState = new EventState();
    private final BlockingQueue<VisualOperation> operationQueue = new LinkedBlockingQueue<>();
    // The graph this is handling events for
    private Graph graph;
    // The manager this interaction handler delegates to
    private VisualManager manager;
    // The connection to the visual interaction that this handler uses
    private VisualInteraction visualInteraction;
    // The connection to the visual annotator that this handler uses
    private VisualAnnotator visualAnnotator;

    private final SelectionFreeformModel freeformModel = new SelectionFreeformModel();

    private boolean announceNextFlush = false;
    private boolean handleEvents;

    private static final Logger LOGGER = Logger.getLogger(DefaultInteractionEventHandler.class.getName());

    /**
     * A Functional interface that describes how a single mouse or keyboard
     * gesture should be processed once received this event handler from the
     * EDT.
     */
    @FunctionalInterface
    private static interface GestureHandler {

        /**
         * Process a gesture, manipulating the graph and scheduling
         * {@link VisualOperation VisualOperations} as appropriate. This method
         * is called by the event handler's main loop; it should never be
         * invoked elsewhere.
         * <p>
         * Note that these handlers should not do anything with
         * {@link DefaultInteractionEventHandler#graph graph} or
         * {@link DefaultInteractionEventHandler#interactionGraph interactionGraph},
         * as the supplied GarphWriteMethods should stay valid for the duration.
         * {@link VisualOperation VisualOperations} that depend on a flush
         * occurring should be queued to
         * {@link DefaultInteractionEventHandler#operationQueue operationQueue}.
         * After each event has been handled, if this queue is non-empty,
         * {@link DefaultInteractionEventHandler#interactionGraph interactionGraph}
         * will be flushed, all operations on this queue submitted to the
         * {@link VisualManager} and the queue cleared.
         *
         * @param graph Write access to the graph, corresponding to the lock on
         * which gestures are currently being processed.
         * @return The minimum amount of time that the event handler should hold
         * the lock on the graph waiting for more events.
         */
        long processEvent(final GraphWriteMethods graph);
    }

    private Thread eventHandlingThread;
    private long currentCameraChangeId;
    private final long[] currentXYZChangeIds = new long[3];

    @Override
    public void startHandlingEvents() {
        final Runnable timedInteractionHandler = () -> {
            long time = System.currentTimeMillis();

            GestureHandler handler;
            while (handleEvents) {
                currentCameraChangeId = VisualChangeBuilder.generateNewId();
                for (int i = 0; i < 2; i++) {
                    currentXYZChangeIds[0] = VisualChangeBuilder.generateNewId();
                }
                // Wait for something from the queue to be acquired
                while (true) {
                    try {
                        handler = queue.take();
                        break;
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        if (!handleEvents) {
                            return;
                        }
                    }
                }

                // Wait for the graph lock to be acquired
                WritableGraph interactionGraph = graph.getWritableGraphNow("Process Gestures", true);

                if (interactionGraph == null) {
                    manager.addOperation(visualAnnotator.flagBusy(true));
                }

                long waitTime = 0;
                try {
                    // Continue to loop whilst we see events before the alloted time is up
                    while (handler != null) {
                        long beforeProcessing = System.currentTimeMillis();
                        //HACK_DPI
                        // TODO: there's a race condition here which is causing access to interactionGraph to raise
                        // a NullPointerException on the handling of some events.  It appears to happen far more often
                        // on an old MacBook than in Windows on a moderately powerful PC.  There is no attempt at a fix
                        // here and this issue needs to be resolved in a future change.
                        //final long nextWaitTime = Math.max(0, beforeProcessing + handler.processEvent(interactionGraph) - System.currentTimeMillis());
                        long nextWaitTime = 0;
                        try {
                            nextWaitTime = Math.max(0, beforeProcessing + handler.processEvent(interactionGraph) - System.currentTimeMillis());
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Null exception accessing interactionGraph", ex);
                        }
                        // Add any visual operations that need to occur after a graph flush.
                        final List<VisualOperation> operations = new LinkedList<>();
                        operationQueue.drainTo(operations);
                        if (announceNextFlush || !operations.isEmpty()) {
                            if (interactionGraph != null) {
                                interactionGraph = interactionGraph.flush(announceNextFlush);
                            }
                            operations.forEach(op -> manager.addOperation(op));
                            announceNextFlush = false;
                        }
                        final boolean waitForever = eventState.isMousePressed() || (eventState.getCurrentAction().equals(SceneAction.CREATING) && eventState.getCurrentCreationMode().equals(CreationMode.CREATING_TRANSACTION));
                        waitTime = Math.max(nextWaitTime, time + waitTime - System.currentTimeMillis());
                        time = System.currentTimeMillis();

                        // Wait for another event to come through until the time has elapsed
                        while (true) {
                            try {
                                handler = queue.poll(waitForever ? Long.MAX_VALUE : waitTime, TimeUnit.MILLISECONDS);
                                break;
                            } catch (InterruptedException ex) {
                                waitTime = Math.max(0, time + waitTime - System.currentTimeMillis());
                                time = System.currentTimeMillis();
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                } finally {
                    if (interactionGraph != null) {
                        interactionGraph.commit(null, eventState.retrieveAndClearEventNames());
                    } else {
                        manager.addOperation(visualAnnotator.flagBusy(false));
                    }
                }
            }
        };
        eventHandlingThread = new Thread(timedInteractionHandler);
        handleEvents = true;
        eventHandlingThread.setName("Default Interaction Event Handler");
        eventHandlingThread.start();
    }

    @Override
    public void stopHandlingEvents() {
        handleEvents = false;
        eventHandlingThread.interrupt();
        graph = null;
        manager = null;
        visualAnnotator = null;
        visualInteraction = null;
    }

    /**
     * Create a new DefaultInteractionEventHandler for the specified
     * {@link Graph}, utilising the specified
     * {@link VisualManager}, {@link VisualInteraction} and
     * {@link VisualAnnotator}.
     *
     * @param graph The {@link Graph} to interact with.
     * @param manager The {@link VisualManager} to use for queueing visual
     * operations and updates
     * @param visualInteraction The {@link VisualInteraction} to facilitate
     * operations that convert between screen coordinates and graph coordinates.
     * @param visualAnnotator The {@link VisualAnnotator} to use for changing
     * the properties of annotations and other visualisations ancillary to the
     * graph such as drawing new lines, the selection box, and hit testing.
     */
    public DefaultInteractionEventHandler(final Graph graph, final VisualManager manager, final VisualInteraction visualInteraction, final VisualAnnotator visualAnnotator) {
        this.manager = manager;
        this.visualInteraction = visualInteraction;
        this.visualAnnotator = visualAnnotator;
        this.graph = graph;
    }

    private void scheduleCameraChangeOperation() {
        scheduleCameraChangeOperation(null);
    }

    private void scheduleCameraChangeOperation(final VisualOperation withOperation) {
        final VisualOperation cameraChange = manager.constructSingleChangeOperation(new VisualChangeBuilder(VisualProperty.CAMERA).forItems(1).withId(currentCameraChangeId).build());
        operationQueue.add(withOperation == null ? cameraChange : cameraChange.join(withOperation));
    }

    private void scheduleXYZChangeOperation(final int[] verticiesMoved) {
        // VerticiesMoved should be a list of vertexPositions for the verticies moved.
        operationQueue.add(manager.constructMultiChangeOperation(Arrays.asList(
                new VisualChangeBuilder(VisualProperty.VERTEX_X).forItems(verticiesMoved).withId(currentXYZChangeIds[0]).build(),
                new VisualChangeBuilder(VisualProperty.VERTEX_Y).forItems(verticiesMoved).withId(currentXYZChangeIds[1]).build(),
                new VisualChangeBuilder(VisualProperty.VERTEX_Z).forItems(verticiesMoved).withId(currentXYZChangeIds[2]).build()
        )));
    }

    /**
     * Respond to a key press event on the graph. This will respond to keys that
     * interact directly with the graph's visuals, such as W,A,S,D to pan. Most
     * key presses in CONSTELLATION, for example Ctrl+A, will be picked up by
     * the netbeans framework and cause plugins to be executed.
     * <p>
     * This is called continually whenever a key is held down (at the key repeat
     * rate of the operating system).
     *
     * @param event The KeyEvent related to the key press.
     */
    @Override
    public void keyPressed(final KeyEvent event) {
        final int keyCode = event.getKeyCode();
        // Avoid the control key so we don't interfere with ^S for save, for example.
        final boolean isCtrl = event.isControlDown();
        final boolean isShift = event.isShiftDown();
        if (keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_PAGE_DOWN || (!isCtrl
                && (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_W))) {
            queue.add(wg -> {
                if (wg != null) {
                    final Camera camera = new Camera(VisualGraphUtilities.getCamera(wg));
                    if (keyCode == KeyEvent.VK_PAGE_UP) {
                        CameraUtilities.changeMixRatio(camera, true, isCtrl);
                        eventState.addEventName(MIX_ACTION_NAME);
                    } else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
                        CameraUtilities.changeMixRatio(camera, false, isCtrl);
                        eventState.addEventName(MIX_ACTION_NAME);
                    } else if (keyCode == KeyEvent.VK_A) {
                        CameraUtilities.pan(camera, -0.5F * (isShift ? 10 : 1), 0);
                        eventState.addEventName(PAN_ACTION_NAME);
                    } else if (keyCode == KeyEvent.VK_D) {
                        CameraUtilities.pan(camera, 0.5F * (isShift ? 10 : 1), 0);
                        eventState.addEventName(PAN_ACTION_NAME);
                    } else if (keyCode == KeyEvent.VK_S) {
                        CameraUtilities.pan(camera, 0, -0.5F * (isShift ? 10 : 1));
                        eventState.addEventName(PAN_ACTION_NAME);
                    } else if (keyCode == KeyEvent.VK_W) {
                        CameraUtilities.pan(camera, 0, 0.5F * (isShift ? 10 : 1));
                        eventState.addEventName(PAN_ACTION_NAME);
                    } else {
                        // Do nothing
                    }
                    VisualGraphUtilities.setCamera(wg, camera);
                    scheduleCameraChangeOperation();
                }
                return STANDARD_DELAY;
            });
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        // Method override required, intentionally left blank
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        // Method override required, intentionally left blank
    }

    /**
     * Mouse pressed.
     * <p>
     * Note: if you right-click on an element, move the mouse to another part of
     * the element (ignoring the pop-up menu), and right-press, you don't get a
     * mousePressed event.
     *
     * @param event Mouse event.
     */
    @Override
    public void mouseDragged(final MouseEvent event) {
        queue.add(wg -> {
            // If a mouse pressed event was never registered (can happen when left clicking off a context menu) we ignore this event.
            // HACK_DPI - Multiply point by DPI scale factor
            Point point = event.getPoint();
            scaleMousePointByDPIFactor(point);

            if (eventState.isMousePressed()) {
                if (wg != null) {
                    final Camera camera = new Camera(VisualGraphUtilities.getCamera(wg));
                    final Point from;
                    final Point to;
                    boolean cameraChange = false;
                    switch (eventState.getCurrentAction()) {
                        case ROTATING:
                            from = eventState.getFirstValidPoint(EventState.DRAG_POINT, EventState.REFERENCE_POINT);
                            to = point;
                            final boolean zAxisRotation = !VisualGraphUtilities.isDisplayModeIn3D(wg) || (event.isControlDown() && event.isShiftDown());
                            if (zAxisRotation) {
                                CameraUtilities.spin(camera, visualInteraction.convertTranslationToSpin(from, to));
                            } else {
                                CameraUtilities.rotate(camera, event.isShiftDown() ? 0 : (from.y - to.y) / 2.0F, event.isControlDown() ? 0 : (from.x - to.x) / 2.0F);
                            }
                            cameraChange = true;
                            break;
                        case PANNING:
                            from = eventState.getFirstValidPoint(EventState.DRAG_POINT, EventState.REFERENCE_POINT);
                            to = point;
                            final Vector3f panReferencePoint = eventState.hasClosestNode() ? eventState.getClosestNode() : CameraUtilities.getFocusVector(camera);
                            final Vector3f translation = visualInteraction.convertTranslationToPan(from, to, panReferencePoint);
                            CameraUtilities.pan(camera, translation.getX(), translation.getY());
                            cameraChange = true;
                            break;
                        case DRAG_NODES:
                            from = eventState.getFirstValidPoint(EventState.DRAG_POINT, EventState.REFERENCE_POINT);
                            to = point;
                            performDrag(wg, camera, from, to);
                            break;
                        case SELECTING:
                            updateSelectionBoxModel(new SelectionBoxModel(eventState.getPoint(EventState.PRESSED_POINT), point));
                            break;
                        case FREEFORM_SELECTING:
                            freeformModel.addPoint(point);
                            updateSelectionFreeformModel(freeformModel);
                            break;
                        default:
                            break;
                    }
                    updateCameraAndNewLine(wg, point, cameraChange ? camera : VisualGraphUtilities.getCamera(wg), cameraChange);

                }

                eventState.storePoint(point, EventState.DRAG_POINT);
            } else if (wg != null) {
                // In this case, a button is held down but its pressed event was not registered for whatever reason.
                updateHitTestAndNewLine(wg, point);
            } else {
                // Do nothing
            }
            return 0;
        });
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        queue.add(wg -> {
            // If the mouse is currently pressed (meaning this event represents pressing multiple buttons) we ignore this event.
            if (!eventState.isMousePressed()) {
                // HACK_DPI - Multiply point by DPI scale factor
                Point point = event.getPoint();
                scaleMousePointByDPIFactor(point);

                // In case we are panning, we must take the distance of the scene from the camera into account,
                // otherwise scenes that are further away will appear to move very slowly.
                eventState.storePoint(point, EventState.PRESSED_POINT, EventState.REFERENCE_POINT);
                eventState.setCurrentButton(event.getButton());

                final Camera camera;
                if (wg != null) {
                    camera = VisualGraphUtilities.getCamera(wg);

                    // Order a hit test and wait for it to complete.
                    orderHitTest(point, HitTestMode.HANDLE_SYNCHRONOUSLY);

                    // Now that we have got the results of hit testing we can turn it off until the mouse is released.
                    setHitTestingEnabled(false);

                    eventState.setClosestNode(visualInteraction.closestNodeCameraCoordinates(wg, camera, point));
                } else {
                    camera = null;
                }

                if (SwingUtilities.isMiddleMouseButton(event)) {
                    eventState.setCurrentAction(SceneAction.ROTATING);
                } else if ((eventState.getCurrentHitType().equals(HitType.NO_ELEMENT) && SwingUtilities.isRightMouseButton(event))
                        || (SwingUtilities.isRightMouseButton(event) && event.isControlDown())) {
                    eventState.setCurrentAction(SceneAction.PANNING);
                } else if (SwingUtilities.isRightMouseButton(event) && !eventState.getCurrentHitType().equals(HitType.NO_ELEMENT)) {
                    eventState.setCurrentAction(SceneAction.DRAG_NODES);
                } else if (SwingUtilities.isLeftMouseButton(event)) {
                    if ((wg == null || !VisualGraphUtilities.isDrawingMode(wg)) && event.isAltDown()) {
                        eventState.setCurrentAction(SceneAction.FREEFORM_SELECTING);
                    } else if (wg == null || !VisualGraphUtilities.isDrawingMode(wg)) {
                        eventState.setCurrentAction(SceneAction.SELECTING);
                    } else {
                        eventState.setCurrentAction(SceneAction.CREATING);
                        //Check if we are on a vertex
                        if (eventState.getCurrentHitType().equals(HitType.VERTEX)) {
                            beginCreateTransaction();
                            scheduleNewLineChangeOperation(wg, null, camera, false);
                        } else {
                            beginCreateVertex();
                            clearNewLineModel(camera);
                        }
                    }
                } else {
                    // Do nothing
                }
            }
            return 0;
        });
    }

    @SuppressWarnings("fallthrough")
    @Override
    public void mouseReleased(final MouseEvent event) {
        queue.add(wg -> {
            // If a button other than the original button is involved, or a mouse pressed event was never registered (can happen when left clicking off a context menu) we ignore this event.
            if (eventState.isMousePressed() && eventState.getCurrentButton() == event.getButton()) {
                if (wg != null) {
                    // Once all buttons have been released, we immediately turn hit testing back on
                    setHitTestingEnabled(true);

                    final Camera camera = VisualGraphUtilities.getCamera(wg);
                    // HACK_DPI - Multiply point by DPI scale factor
                    Point point = event.getPoint();
                    scaleMousePointByDPIFactor(point);
                    final Point from;
                    final Point to;
                    switch (eventState.getCurrentAction()) {
                        case SELECTING:
                            if (eventState.isMouseDragged()) {
                                performBoxSelection(wg, point, eventState.getPoint(EventState.PRESSED_POINT), event.isShiftDown(), event.isControlDown());
                                clearSelectionBoxModel();
                            } else {
                                // If the mouse has clicked on an element (and neither pan nor control are pressed),
                                // or has double-clicked on the background, clear the selection.
                                final boolean clearSelection = !event.isControlDown() && !event.isShiftDown() && (!eventState.getCurrentHitType().equals(HitType.NO_ELEMENT) || event.getClickCount() == 2);
                                performPointSelection(event.isControlDown(), clearSelection, eventState.getCurrentHitType().elementType, eventState.getCurrentHitId());
                            }
                            break;
                        case FREEFORM_SELECTING:
                            if (eventState.isMouseDragged()) {
                                performFreeformSelection(wg, event.isShiftDown(), event.isControlDown());
                                clearSelectionFreeformModel();
                            } else {
                                // If the mouse has clicked on an element (and neither pan nor control are pressed),
                                // or has double-clicked on the background, clear the selection.
                                final boolean clearSelection = !event.isControlDown() && !event.isShiftDown() && (!eventState.getCurrentHitType().equals(HitType.NO_ELEMENT) || event.getClickCount() == 2);
                                performPointSelection(event.isControlDown(), clearSelection, eventState.getCurrentHitType().elementType, eventState.getCurrentHitId());
                            }
                            break;
                        case CREATING:                            
                            setCurrentCreationMode(camera, point, wg, event);
                            break;
                        case ROTATING:
                            if (!eventState.isMouseDragged() && eventState.getCurrentHitType().equals(HitType.VERTEX)) {
                                final Camera centredCamera = new Camera(camera);
                                CameraUtilities.setRotationCentre(centredCamera, VisualGraphUtilities.getMixedVertexCoordinates(wg, eventState.getCurrentHitId()));
                                VisualGraphUtilities.setCamera(wg, centredCamera);
                                scheduleCameraChangeOperation();
                                eventState.addEventName(SET_CENTRE_ACTION_NAME);
                            } else {
                                eventState.addEventName(ROTATE_ACTION_NAME);
                            }
                            break;
                        // If the mouse was never dragged, then all actions involving a right
                        // mouse button require the context menu to be displayed.
                        case DRAG_NODES:
                            if (eventState.isMouseDragged()) {
                                from = eventState.getPoint(EventState.DRAG_POINT);
                                to = point;
                                performDrag(wg, camera, from, to);
                                eventState.addEventName(DRAG_ACTION_NAME);
                            }
                        // falls through
                        case PANNING:
                            if (!eventState.isMouseDragged()) {
                                showContextMenu(wg, camera, event.getPoint(), eventState.getCurrentHitType().elementType, eventState.getCurrentHitId());
                            } else {
                                eventState.addEventName(PAN_ACTION_NAME);
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (!(eventState.getCurrentAction().equals(SceneAction.CREATING) && eventState.getCurrentCreationMode().equals(CreationMode.CREATING_TRANSACTION))) {
                    eventState.setCurrentAction(SceneAction.NONE);
                }

                eventState.invalidatePoints(EventState.PRESSED_POINT, EventState.DRAG_POINT, EventState.WHEEL_POINT, EventState.REFERENCE_POINT);
                eventState.setNoButton();
            }
            return 0;
        });
    }

    private void setCurrentCreationMode(final Camera camera, final Point point, final GraphWriteMethods wg, final MouseEvent event) {
        switch (eventState.getCurrentCreationMode()) {
            case CREATING_VERTEX:
                createVertex(camera, point);
                eventState.setCurrentCreationMode(CreationMode.NONE);
                break;
            case FINISHING_TRANSACTION:
                createTransaction(wg, eventState.getAddTransactionSourceVertex(), eventState.getAddTransactionDestinationVertex(), VisualGraphUtilities.isDrawingDirectedTransactions(wg));
                if (event.isShiftDown()) {
                    eventState.setCurrentCreationMode(CreationMode.CREATING_TRANSACTION);
                } else if (event.isControlDown()) {
                    eventState.setCurrentCreationMode(CreationMode.CREATING_TRANSACTION);
                    eventState.setAddTransactionSourceVertex(eventState.getAddTransactionDestinationVertex());
                } else {
                    eventState.setCurrentCreationMode(CreationMode.NONE);
                    clearNewLineModel(camera);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        queue.add(wg -> {
            if (wg != null) {
                // HACK_DPI - Multiply point by DPI scale factor
                Point point = event.getPoint();
                scaleMousePointByDPIFactor(point);
                updateHitTestAndNewLine(wg, point);
            }
            return 0;
        });
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent event) {
        queue.add(wg -> {
            if (wg != null) {
                final Camera camera = new Camera(VisualGraphUtilities.getCamera(wg));
                final Point wheelPoint = event.getPoint();
                // HACK_DPI: Don't need to scale the wheelPoint.
                eventState.setClosestNode(visualInteraction.closestNodeCameraCoordinates(wg, camera, wheelPoint));
                if (!eventState.isPoint(EventState.WHEEL_POINT) || !wheelPoint.equals(eventState.getPoint(EventState.WHEEL_POINT))) {
                    eventState.storePoint(wheelPoint, EventState.WHEEL_POINT);
                }
                final Vector3f zoomReferencePoint = eventState.hasClosestNode() ? eventState.getClosestNode() : CameraUtilities.getFocusVector(camera);

                // If the mouse is currently pressed and the mouse wheel was moved somewhere
                // other than the mouse pressed point, then we need to recalculate our
                // reference point for further action, and in some cases simulate a drag.
                if (eventState.isMousePressed() && !wheelPoint.equals(eventState.getPoint(EventState.REFERENCE_POINT))) {
                    final Point from;
                    switch (eventState.getCurrentAction()) {
                        case PANNING:
                            from = eventState.getFirstValidPoint(EventState.DRAG_POINT, EventState.REFERENCE_POINT);
                            final Vector3f translation = visualInteraction.convertTranslationToPan(from, wheelPoint, zoomReferencePoint);
                            CameraUtilities.pan(camera, translation.getX(), translation.getY());
                            break;
                        case DRAG_NODES:
                            from = eventState.getPoint(EventState.DRAG_POINT);
                            performDrag(wg, camera, from, wheelPoint);
                            break;
                        default:
                            break;
                    }
                }

                CameraUtilities.zoom(camera, -event.getWheelRotation(), visualInteraction.convertZoomPointToDirection(wheelPoint), zoomReferencePoint.getLength());
                eventState.addEventName(ZOOM_ACTION_NAME);
                if (eventState.isMousePressed()) {
                    eventState.setClosestNode(visualInteraction.closestNodeCameraCoordinates(wg, camera, wheelPoint));
                    eventState.storePoint(wheelPoint, EventState.REFERENCE_POINT, EventState.DRAG_POINT);
                }

                updateCameraAndNewLine(wg, wheelPoint, camera, true);
            }
            return STANDARD_DELAY;
        });
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        // Method override required, intentionally left blank
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        // Method override required, intentionally left blank
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        // Method override required, intentionally left blank
    }

    /**
     * For mouse movements when eventState has no current button - we do a hit
     * test, and, in the case of creating a transaction, update the new line
     * model.
     * <p>
     * When we are creating a transaction, we wait on the result of the hit test
     * before updating the new line model.
     *
     * @param rg Read access to the graph, corresponding to the lock on which
     * gestures are currently being processed.
     * @param point
     */
    private void updateHitTestAndNewLine(final GraphReadMethods rg, final Point point) {
        final boolean newLine = eventState.getCurrentCreationMode().equals(CreationMode.CREATING_TRANSACTION);
        // We need to wait for the results of the hit test if we are creating a transaction
        if (newLine) {
            orderHitTest(point, HitTestMode.HANDLE_ASYNCHRONOUSLY, eventState -> scheduleNewLineChangeOperation(rg, point, VisualGraphUtilities.getCamera(rg), false, eventState));
        } else {
            orderHitTest(point, HitTestMode.REQUEST_ONLY);
        }
    }

    /**
     * For mouse movements when event state has a current button registered - if
     * the camera has changed we update it, and, in the case of creating a
     * transaction, update the new line model.
     * <p>
     * When we are creating a transaction, we update the camera and the new line
     * model together as one {@link VisualOperation}.
     *
     * @param wg Write access to the graph, corresponding to the lock on which
     * gestures are currently being processed.
     * @param point
     * @param camera
     * @param cameraChange
     */
    private void updateCameraAndNewLine(final GraphWriteMethods wg, final Point point, final Camera camera, final boolean cameraChange) {
        if (cameraChange) {
            VisualGraphUtilities.setCamera(wg, camera);
        }

        if (eventState.getCurrentCreationMode().equals(CreationMode.CREATING_TRANSACTION)) {
            scheduleNewLineChangeOperation(wg, point, camera, cameraChange);
        } else if (cameraChange) {
            scheduleCameraChangeOperation();
        } else {
            // Do nothing
        }
    }

    /**
     * This describes the different manners in which a hit test request can be
     * made.
     *
     */
    private enum HitTestMode {

        /**
         * Make the request but ignore the resulting EventState.
         */
        REQUEST_ONLY,
        /**
         * Make the request and wait for the result on this event handler's main
         * thread and then set this handler's EventState to the result.
         */
        HANDLE_SYNCHRONOUSLY,
        /**
         * Make the request and wait for the result on a new thread, to be used
         * in conjunction with a Consumer that will handle the resulting
         * EventState.
         */
        HANDLE_ASYNCHRONOUSLY;
    }

    /**
     * Orders a hit test via a {@link VisualOperation}, storing the result
     * {@link EventState} when the mode handles results.
     *
     * @param point The {@link Point} to hit test in screen coordinates.
     * @param mode The {@link HitTestMode} indicating whether or not to handle
     * results and the synchronicity.
     */
    private void orderHitTest(final Point point, final HitTestMode mode) {
        orderHitTest(point, mode, e -> eventState = e);
    }

    /**
     * Orders a hit test via a {@link VisualOperation}, using the supplied
     * consumer to handler the result event state and optionally waits on the
     * result.
     *
     * @param point The {@link Point} to hit test in screen coordinates.
     * @param mode The {@link HitTestMode} indicating whether or not to handle
     * results and the synchronicity.
     * @param resultConsumer The consumer to handle the {@link EventState}
     * resulting from the hit test.
     */
    private void orderHitTest(final Point point, final HitTestMode mode, final Consumer<EventState> resultConsumer) {
        final BlockingQueue<HitState> hitTestQueue = new ArrayBlockingQueue<>(1);
        manager.addOperation(visualAnnotator.hitTestCursor(point.x, point.y, new EventState(eventState), mode != HitTestMode.REQUEST_ONLY ? hitTestQueue : null));

        final Runnable handleResult = () -> {
            while (true) {
                try {
                    resultConsumer.accept((EventState) hitTestQueue.take());
                    break;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        switch (mode) {
            case HANDLE_SYNCHRONOUSLY:
                handleResult.run();
                break;
            case HANDLE_ASYNCHRONOUSLY:
                new Thread(handleResult).start();
                break;
            default:
                break;
        }
    }

    /**
     * Set whether hit testing is enabled on the {@link VisualProcessor} via a
     * {@link VisualOperation}
     *
     * @param enabled Whether to enable hit testing on the
     * {@link VisualProcessor}.
     */
    private void setHitTestingEnabled(final boolean enabled) {
        manager.addOperation(visualAnnotator.setHitTestingEnabled(enabled));
    }

    /**
     * Schedule a {@link VisualOperation} to clear the selection box model
     */
    private void clearSelectionBoxModel() {
        manager.addOperation(visualAnnotator.setSelectionBoxModel(SelectionBoxModel.getClearModel()));
    }

    /**
     * Schedule a {@link VisualOperation} to update the selection box model
     *
     * @param model The model to update to.
     */
    private void updateSelectionBoxModel(final SelectionBoxModel model) {
        manager.addOperation(visualAnnotator.setSelectionBoxModel(model));
    }

    /**
     * Schedule a {@link VisualOperation} to clear the selection freeform model
     */
    private void clearSelectionFreeformModel() {
        freeformModel.resetModel();
        manager.addOperation(visualAnnotator.setSelectionFreeformModel(freeformModel));
    }

    /**
     * Schedule a {@link VisualOperation} to update the selection freeform model
     *
     * @param model The model to update to.
     */
    private void updateSelectionFreeformModel(final SelectionFreeformModel model) {
        manager.addOperation(visualAnnotator.setSelectionFreeformModel(model));
    }

    /**
     * Schedule a {@link VisualOperation} to clear the new line model
     */
    private void clearNewLineModel(final Camera camera) {
        manager.addOperation(visualAnnotator.setNewLineModel(NewLineModel.getClearModel(camera)));
    }

    /**
     * Schedule a {@link VisualOperation} to change the new line model, along
     * with the camera if necessary.
     *
     * @param rg Read access to the graph, corresponding to the lock on which
     * gestures are currently being processed.
     * @param endPoint The updated end point for the new line
     * @param camera The camera (which may or may not be updated) that this
     * newline corresponds to
     * @param cameraChange Whether or not to also schedule a camera change
     * operation.
     */
    private void scheduleNewLineChangeOperation(final GraphReadMethods rg, final Point endPoint, final Camera camera, final boolean cameraChange) {
        scheduleNewLineChangeOperation(rg, endPoint, camera, cameraChange, eventState);
    }

    /**
     * Schedule a {@link VisualOperation} to change the new line model, along
     * with the camera if necessary, using an alternate EventState for current
     * hit testing information.
     *
     * @param rg Read access to the graph, corresponding to the lock on which
     * gestures are currently being processed.
     * @param endPoint The updated end point for the new line
     * @param camera The camera (which may or may not be updated) that this
     * newline corresponds to
     * @param cameraChange Whether or not to also schedule a camera change
     * operation.
     * @param newLineHitTestState The EventState giving the most recent
     * information about hit testing with relation to the new line.
     */
    private void scheduleNewLineChangeOperation(final GraphReadMethods rg, final Point endPoint, final Camera camera, final boolean cameraChange, final EventState newLineHitTestState) {
        final NewLineModel updatedModel;
        final Vector3f startPoint = VisualGraphUtilities.getVertexCoordinates(rg, eventState.getAddTransactionSourceVertex());
        if (newLineHitTestState.getCurrentHitType().equals(HitType.VERTEX)) {
            final int vertexId = newLineHitTestState.getCurrentHitId();
            updatedModel = new NewLineModel(startPoint, VisualGraphUtilities.getVertexCoordinates(rg, vertexId), camera);
        } else {
            updatedModel = new NewLineModel(startPoint, endPoint == null ? startPoint : visualInteraction.windowToGraphCoordinates(VisualGraphUtilities.getCamera(rg), endPoint), camera);
        }
        final VisualOperation updateNewLineOperation = visualAnnotator.setNewLineModel(updatedModel);
        if (cameraChange) {
            scheduleCameraChangeOperation(updateNewLineOperation);
        } else {
            manager.addOperation(updateNewLineOperation);
        }
    }

    /**
     * Flag in the event state that a new vertex is in the process of being
     * created.
     */
    private void beginCreateVertex() {
        if (!eventState.getCurrentCreationMode().equals(CreationMode.NONE)) {
            eventState.setCurrentCreationMode(CreationMode.NONE);
        } else {
            eventState.setCurrentCreationMode(CreationMode.CREATING_VERTEX);
        }
    }

    /**
     * Flag in the event state that a new transaction is in the process of being
     * created.
     */
    private void beginCreateTransaction() {
        switch (eventState.getCurrentCreationMode()) {
            case NONE:
                eventState.setAddTransactionSourceVertex(eventState.getCurrentHitId());
                eventState.setCurrentCreationMode(CreationMode.CREATING_TRANSACTION);
                break;
            case CREATING_TRANSACTION:
                eventState.setAddTransactionDestinationVertex(eventState.getCurrentHitId());
                eventState.setCurrentCreationMode(CreationMode.FINISHING_TRANSACTION);
                break;
            default:
                break;
        }
    }

    /**
     * Creates a vertex on the graph at a specified point.
     * <p>
     * The vertex will be created via a plugin which will run later (after the
     * event handler gives up its current lock).
     *
     * @param camera The current camera
     * @param createAt The point at which the vertex should be created.
     */
    private void createVertex(final Camera camera, final Point createAt) {
        Vector3f position = visualInteraction.windowToGraphCoordinates(camera, createAt);
        Plugin plugin = PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_VERTEX);
        PluginParameters parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        parameters.getParameters().get(CreateVertexPlugin.X_PARAMETER_ID).setObjectValue(position.getX());
        parameters.getParameters().get(CreateVertexPlugin.Y_PARAMETER_ID).setObjectValue(position.getY());
        parameters.getParameters().get(CreateVertexPlugin.Z_PARAMETER_ID).setObjectValue(position.getZ());

        PluginExecution.withPlugin(plugin).withParameters(parameters).interactively(false).executeLater(graph);
    }

    /**
     * Creates a transaction on the graph between the specified vertices.
     * <p>
     * The vertex will be created via a plugin which will synchronously, holding
     * up the event handler until it is finished. The reason for this is that
     * otherwise the new transaction will not be visible until the event handler
     * gives up its current lock (which if we are creating multiple transactions
     * with the shift/control key down, could be indefinitely).
     *
     * @param fromVertex The graph id of the source vertex
     * @param toVertex The graph id of the destination vertex
     * @param directed Whether or not the transaction should be directed.
     * @param wg Write access to the graph, corresponding to the lock on which
     * gestures are currently being processed.
     */
    private void createTransaction(final GraphWriteMethods wg, final int fromVertex, final int toVertex, final boolean directed) {
        Plugin plugin = PluginRegistry.get(InteractiveGraphPluginRegistry.CREATE_TRANSACTION);
        PluginParameters parameters = DefaultPluginParameters.getDefaultParameters(plugin);
        parameters.getParameters().get(CreateTransactionPlugin.SOURCE_PARAMETER_ID).setObjectValue(fromVertex);
        parameters.getParameters().get(CreateTransactionPlugin.DESTINATION_PARAMETER_ID).setObjectValue(toVertex);
        parameters.getParameters().get(CreateTransactionPlugin.DIRECTED_PARAMETER_ID).setObjectValue(directed);
        try {
            PluginExecution.withPlugin(plugin).withParameters(parameters).interactively(false).executeNow(wg);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (PluginException ex) {
        }
        announceNextFlush = true;
    }

    /**
     * Gather all the selected nodes identifiers. Add the selected node if
     * appropriate If a transaction was selected, then also add its two nodes.
     *
     * @return array of node IDs
     */
    public List<Integer> gatherSelectedNodes(final GraphReadMethods rg) {
        List<Integer> selectedIds = VisualGraphUtilities.getSelectedElements(rg);

        final int hitId = eventState.getCurrentHitId();
        if (eventState.getCurrentHitType().equals(HitType.VERTEX) && !selectedIds.contains(hitId)) {
            selectedIds.add(hitId);
        } else if (eventState.getCurrentHitType().equals(HitType.TRANSACTION)) {
            final int srcId = rg.getTransactionSourceVertex(hitId);
            if (!selectedIds.contains(srcId)) {
                selectedIds.add(srcId);
            }
            final int dstId = rg.getTransactionDestinationVertex(hitId);
            if (!selectedIds.contains(dstId)) {
                selectedIds.add(dstId);
            }
        } else {
            // Do nothing
        }
        return selectedIds;
    }

    private void performDrag(final GraphWriteMethods wg, final Camera camera, final Point from, final Point to) {
        // Get the ids of the selected nodes (and those of the associated transaction as well)
        final List<Integer> draggedNodes = gatherSelectedNodes(wg);
        int nodeBeingDraggedId = eventState.getCurrentHitType().equals(HitType.TRANSACTION) ? wg.getTransactionSourceVertex(eventState.getCurrentHitId()) : eventState.getCurrentHitId();

        // Get the position of the node being dragged.
        final Vector3f position = VisualGraphUtilities.getMixedVertexCoordinates(wg, nodeBeingDraggedId);

        final Vector3f delta = visualInteraction.convertTranslationToDrag(camera, position, from, to);

        final int xAttribute = VisualConcept.VertexAttribute.X.get(wg);
        final int yAttribute = VisualConcept.VertexAttribute.Y.get(wg);
        final int zAttribute = VisualConcept.VertexAttribute.Z.get(wg);
        final int x2Attribute = VisualConcept.VertexAttribute.X2.get(wg);
        final int y2Attribute = VisualConcept.VertexAttribute.Y2.get(wg);
        final int z2Attribute = VisualConcept.VertexAttribute.Z2.get(wg);
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(wg);

        draggedNodes.forEach(vertexId -> {
            final Vector3f currentPos = VisualGraphUtilities.getMixedVertexCoordinates(wg, vertexId, xAttribute, x2Attribute, yAttribute, y2Attribute, zAttribute, z2Attribute, cameraAttribute);
            currentPos.add(delta);
            VisualGraphUtilities.setVertexCoordinates(wg, currentPos, vertexId, xAttribute, yAttribute, zAttribute);
        });

        draggedNodes.replaceAll(id -> wg.getVertexPosition(id)); // Replade the Id's with positions, as required by scheduleXYZCHangeOperation.
        scheduleXYZChangeOperation(Ints.toArray(draggedNodes));
    }

    private void performPointSelection(final boolean toggleSelection, final boolean clearSelection, final GraphElementType elementType, final int elementId) {

        final IntArray vxIds = new IntArray();
        final IntArray txIds = new IntArray();

        switch (elementType) {
            case VERTEX:
                vxIds.add(elementId);
                break;
            case TRANSACTION:
                txIds.add(elementId);
                break;
            default:
                break;
        }

        if (!(vxIds.isEmpty() && txIds.isEmpty() && !clearSelection)) {
            Plugin selectPoint = new PointSelectionPlugin(vxIds, txIds, toggleSelection, clearSelection);
            PluginExecution.withPlugin(selectPoint).executeLater(graph);
        }
    }

    /**
     * Performs a selection based on a given start and end point.
     *
     * If the start and end point are equal, a point selection is performed
     * based on the hit tester.
     *
     * If the start and end point differ, a box selection is performed with the
     * two points representing diagonally opposite corners of the box.
     *
     * In the latter case, the 2d box is actually converted to a 3d frustrum in
     * order to make the correct selection on the 3 dimensional graph.
     *
     * @param selectTo the point where selection ends
     * @param selectFrom the point where selection begins.
     * @param appendSelection whether or not the selection will be appended to
     * the current selection
     * @param toggleSelection whether or not the selection will toggle the
     * current selection. Note that if appendSelection is true, this parameter
     * has no effect.
     */
    private void performBoxSelection(final GraphReadMethods rg, final Point selectTo, final Point selectFrom, final boolean appendSelection, final boolean toggleSelection) {

        if (selectTo.equals(selectFrom)) {
            return;
        }

        // Sort the press/release coordinates to look like a drag from top-left to bottom-right: pressed<=released.
        Point bottomRight = new Point();
        if (selectTo.x < selectFrom.x) {
            bottomRight.x = selectFrom.x;
            selectFrom.x = selectTo.x;
        } else {
            bottomRight.x = selectTo.x;
        }

        if (selectTo.y < selectFrom.y) {
            bottomRight.y = selectFrom.y;
            selectFrom.y = selectTo.y;
        } else {
            bottomRight.y = selectTo.y;
        }

        final float[] boxCameraCoordinates = visualInteraction.windowBoxToCameraBox(selectFrom.x, bottomRight.x, selectFrom.y, bottomRight.y);

        Plugin plugin = new BoxSelectionPlugin(appendSelection, toggleSelection, VisualGraphUtilities.getCamera(rg), boxCameraCoordinates);
        PluginExecution.withPlugin(plugin).executeLater(graph);
    }

    /**
     * Performs a selection based on a given polygon in the freeformModel.
     *
     * @param appendSelection whether or not the selection will be appended to
     * the current selection
     * @param toggleSelection whether or not the selection will toggle the
     * current selection. Note that if appendSelection is true, this parameter
     * has no effect.
     */
    private void performFreeformSelection(final GraphReadMethods rg, final boolean appendSelection, final boolean toggleSelection) {

        final float[] boxCameraCoordinates = visualInteraction.windowBoxToCameraBox(freeformModel.getLeftMost(), freeformModel.getRightMost(), freeformModel.getTopMost(), freeformModel.getBottomMost());

        freeformModel.setWindowBoxToCameraBox(boxCameraCoordinates);
        final Float[] transformedVertices = freeformModel.getTransformedVertices();

        final Plugin plugin = new FreeformSelectionPlugin(appendSelection, toggleSelection, VisualGraphUtilities.getCamera(rg), boxCameraCoordinates, transformedVertices, freeformModel.getNumPoints());
        PluginExecution.withPlugin(plugin).executeLater(graph);
    }

    private void showContextMenu(final GraphReadMethods rg, final Camera camera, final Point screenLocation, final GraphElementType elementType, final int clickedId) {
        final JPopupMenu popup = new JPopupMenu();
        final Collection<? extends ContextMenuProvider> popups = Lookup.getDefault().lookupAll(ContextMenuProvider.class);
        final Vector3f graphLocation = visualInteraction.windowToGraphCoordinates(camera, screenLocation);

        for (final ContextMenuProvider pmp : popups) {
            // Retrive list of item names to populate and optional list of icons to assign
            // Icons will be added corresponding to an image if the icons list is not ull
            // and a non null icon is provided with corresponding index.
            final List<String> items = pmp.getItems(rg, elementType, clickedId);
            final List<ImageIcon> icons = pmp.getIcons(rg, elementType, clickedId);
            if (!items.isEmpty()) {
                final List<String> menuPath = pmp.getMenuPath(elementType);
                if (CollectionUtils.isEmpty(menuPath)) {
                    for (int idx = 0; idx < items.size(); idx++) {
                        final Icon icon = (icons != null && icons.size() > idx + 1) ? (Icon) icons.get(idx) : null;
                        final String item = items.get(idx);
                        if (icon == null) {
                            // No icon was found, add menu item without an icon
                            popup.add(new AbstractAction(item) {
                                @Override
                                public void actionPerformed(final ActionEvent event) {
                                    PluginExecution.withPlugin(new SelectGraphItem(pmp, item, elementType, clickedId, graphLocation)).executeLater(null);
                                }
                            });
                        } else {
                            // An icon was found, add menu item containing the icon
                            popup.add(new AbstractAction(item, icon) {
                                @Override
                                public void actionPerformed(final ActionEvent event) {
                                    PluginExecution.withPlugin(new SelectGraphItem(pmp, item, elementType, clickedId, graphLocation)).executeLater(null);
                                }
                            });
                        }
                    }
                } else {
                    JComponent currentMenu = popup;
                    for (final String level : menuPath) {
                        int childCount = currentMenu.getComponentCount();
                        for (int i = 0; i < childCount; i++) {
                            final JComponent childComponent = (JComponent) currentMenu.getComponent(i);
                            if (childComponent instanceof JMenu) {
                                JMenu childMenu = (JMenu) childComponent;
                                if (childMenu.getText().equals(level)) {
                                    currentMenu = childComponent;
                                    break;
                                }
                            }
                        }
                        final JMenu childMenu = new JMenu(level);
                        currentMenu.add(childMenu);
                        currentMenu = childMenu;
                    }

                    for (int idx = 0; idx < items.size(); idx++) {
                        final Icon icon = (icons != null && icons.size() > idx) ? (Icon) icons.get(idx) : null;
                        final String item = items.get(idx);
                        if (icon == null) {
                            // No icon was found, add menu item without an icon
                            ((JMenu) currentMenu).add(new AbstractAction(item) {
                                @Override
                                public void actionPerformed(final ActionEvent event) {
                                    PluginExecution.withPlugin(new SelectGraphItem(pmp, item, elementType, clickedId, graphLocation)).executeLater(null);
                                }
                            });
                        } else {
                            // An icon was found, add menu item containing the icon
                            ((JMenu) currentMenu).add(new AbstractAction(item, icon) {
                                @Override
                                public void actionPerformed(final ActionEvent event) {
                                    PluginExecution.withPlugin(new SelectGraphItem(pmp, item, elementType, clickedId, graphLocation)).executeLater(null);
                                }
                            });
                        }
                    }
                }
            }
        }

        popup.show(manager.getVisualComponent(), screenLocation.x, screenLocation.y);
    }

    private void scaleMousePointByDPIFactor(final Point pointToScale) {
        // HACK_DPI - Get the DPI scale factor and multiply the point by it
        final float dpiScalingFactor = this.visualInteraction.getDPIScalingFactor();
        pointToScale.x *= dpiScalingFactor;
        pointToScale.y *= dpiScalingFactor;
    }

    /**
     * Plugin to select graph item.
     */
    @PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
    private class SelectGraphItem extends SimplePlugin {

        private final ContextMenuProvider pmp;
        private final String item;
        private final GraphElementType elementType;
        private final int clickedId;
        private final Vector3f graphLocation;

        public SelectGraphItem(final ContextMenuProvider pmp, final String item, final GraphElementType elementType, final int clickedId, final Vector3f graphLocation) {
            this.pmp = pmp;
            this.item = item;
            this.elementType = elementType;
            this.clickedId = clickedId;
            this.graphLocation = graphLocation;
        }

        @Override
        public String getName() {
            return CONTEXT_MENU_TEXT + item;
        }

        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            pmp.selectItem(item, graph, elementType, clickedId, graphLocation);
        }
    }
}
