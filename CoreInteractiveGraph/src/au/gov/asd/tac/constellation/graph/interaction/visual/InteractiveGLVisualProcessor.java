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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState.HitType;
import au.gov.asd.tac.constellation.graph.interaction.framework.InteractionEventHandler;
import au.gov.asd.tac.constellation.graph.interaction.framework.VisualAnnotator;
import au.gov.asd.tac.constellation.graph.interaction.framework.VisualInteraction;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.HitTestRequest;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.HitTester;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.NewLineModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.NewLineRenderable;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.PlanesRenderable;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionBoxModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionBoxRenderable;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualOperation;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import java.awt.Component;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * An extension of the {@link GLVisualProcessor} that adds support for user
 * interaction through implementing {@link VisualInteraction} and
 * {@link VisualAnnotator}.
 * <p>
 * This provides the same basic visualisation of a graph as
 * {@link GLVisualProcessor} does, adding renderables for a new line, a
 * selection box, overlay planes, and hit testing.
 *
 * @author twilight_sparkle
 */
public class InteractiveGLVisualProcessor extends GLVisualProcessor implements VisualInteraction, VisualAnnotator {

    private final long selectionBoxUpdateId = VisualChangeBuilder.generateNewId();
    private final long newLineUpdateId = VisualChangeBuilder.generateNewId();
    private final long greyscaleUpdateId = VisualChangeBuilder.generateNewId();
    private final long hitTestId = VisualChangeBuilder.generateNewId();
    private final long hitTestPointId = VisualChangeBuilder.generateNewId();
    private final HitTester hitTester;
    private final SelectionBoxRenderable selectionBoxRenderable = new SelectionBoxRenderable();
    private final NewLineRenderable newLineRenderable = new NewLineRenderable(this);
    private final PlanesRenderable planesRenderable = new PlanesRenderable();
    private final TransformableGraphDisplayer graphDisplayer = new TransformableGraphDisplayer();

    private InteractionEventHandler handler;
    private DropTargetListener targetListener;
    private DropTarget target;

    /**
     * Create a new InteractiveGLVisualProcessor.
     *
     * @param debugGl Whether or not to utilise a GLContext that includes
     * debugging.
     * @param printGlCapabilities Whether or not to print out a list of GL
     * capabilities upon initialisation.
     */
    public InteractiveGLVisualProcessor(final boolean debugGl, final boolean printGlCapabilities) {
        super(debugGl, printGlCapabilities);
        setGraphDisplayer(graphDisplayer);
        addRenderable(newLineRenderable);
        addRenderable(selectionBoxRenderable);
        addRenderable(planesRenderable);
        hitTester = new HitTester(this);
        addRenderable(hitTester);
    }

    /**
     * Set the specified {@link InteractionEventHandler} to use this processor.
     * <p>
     * This method adds the event handler as a listener (of all the relevant
     * gesture types) to this processor's AWT component.
     *
     * @param handler The handler using this processor.
     */
    public void setEventHandler(final InteractionEventHandler handler) {
        this.handler = handler;
        getCanvas().addKeyListener(this.handler);
        getCanvas().addMouseListener(this.handler);
        getCanvas().addMouseWheelListener(this.handler);
        getCanvas().addMouseMotionListener(this.handler);
        this.handler.startHandlingEvents();
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        this.handler.stopHandlingEvents();
        if (target != null) {
            target.removeDropTargetListener(targetListener);
        }
    }

    /**
     * Add the specified {@link GraphRendererDropTarget} to this processor.
     *
     * @param targetListener The dropper using this processor.
     * @return A {@link DropTarget} wrapping the supplied target and this
     * processor's AWT component.
     */
    public void addDropTargetToCanvas(final DropTargetListener targetListener) {
        this.targetListener = targetListener;
        target = new DropTarget(canvas, this.targetListener);
    }

    private final class GLSetHitTestingOperation implements VisualOperation {

        private final boolean doHitTesting;

        @Override
        public int getPriority() {
            return ELEVATED_VISUAL_PRIORITY;
        }

        public GLSetHitTestingOperation(final boolean doHitTesting) {
            this.doHitTesting = doHitTesting;
        }

        @Override
        public void apply() {
            setDrawHitTest(doHitTesting);
        }

        @Override
        public List<VisualChange> getVisualChanges() {
            return Collections.emptyList();
        }
    }

    @Override
    public VisualOperation setHitTestingEnabled(boolean enabled) {
        return new GLSetHitTestingOperation(enabled);
    }

    @Override
    public VisualOperation hitTestCursor(final int x, final int y, final HitState hitState, final Queue<HitState> notificationQueue) {
        hitTester.queueRequest(new HitTestRequest(x, y, hitState, notificationQueue, resultState -> {
            if (resultState.getCurrentHitType().equals(HitType.NO_ELEMENT)) {
                getCanvas().setCursor(DEFAULT_CURSOR);
            } else {
                getCanvas().setCursor(CROSSHAIR_CURSOR);
            }
        }));
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(hitTestId).build());
    }

    @Override
    public VisualOperation hitTestPoint(int x, int y, Queue<HitState> notificationQueue) {
        hitTester.queueRequest(new HitTestRequest(x, y, new HitState(), notificationQueue, null));
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(hitTestPointId).build());

    }

    @Override
    public VisualOperation setNewLineModel(NewLineModel model) {
        newLineRenderable.queueModel(model);
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(newLineUpdateId).build());
    }

    @Override
    public VisualOperation setSelectionBoxModel(SelectionBoxModel model) {
        selectionBoxRenderable.queueModel(model);
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(selectionBoxUpdateId).build());
    }

    @Override
    public VisualOperation flagBusy(boolean isBusy) {
        graphDisplayer.setGreyscale(isBusy);
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(greyscaleUpdateId).build());
    }

    @Override
    public Vector3f convertZoomPointToDirection(final Point zoomPoint) {
        final Component canvas = getCanvas();
        return new Vector3f(
                (float) (canvas.getWidth() / 2.0 - zoomPoint.x),
                (float) (zoomPoint.y - canvas.getHeight() / 2.0),
                (float) (canvas.getHeight() / (2 * Math.tan(Camera.FIELD_OF_VIEW * Math.PI / 180 / 2))));
    }

    @Override
    public Vector3f convertTranslationToDrag(final Camera camera, final Vector3f nodeLocation, final Point from, final Point to) {

        // Calculate the vector representing the drag (in window coordinates)
        final int dx = to.x - from.x;
        final int dy = to.y - from.y;
        final Vector3f movement = new Vector3f(dx, dy, 0);

        // Calculate and return the vector representing the drag in graph coordinates.
        final Vector3f newposition = new Vector3f();
        Graphics3DUtilities.moveByProjection(nodeLocation, getCameraModelViewProjectionMatrix(camera), getViewport(), movement, newposition);
        return newposition;
    }

    @Override
    public float convertTranslationToSpin(final Point from, final Point to) {
        final int canvasWidth = getCanvas().getWidth();
        final int canvasHeight = getCanvas().getHeight();
        final float xDist = (canvasWidth / 2.0f - to.x) / (canvasWidth / 2.0f);
        final float yDist = (canvasHeight / 2.0f - to.y) / (canvasHeight / 2.0f);
        final float xDelta = (from.x - to.x) / 2.0f;
        final float yDelta = (from.y - to.y) / 2.0f;
        return yDist * xDelta - xDist * yDelta;
    }

    @Override
    public Vector3f convertTranslationToPan(final Point from, final Point to, final Vector3f panReferencePoint) {
        final float dx = (to.x - from.x);
        final float dy = (to.y - from.y);

        // Get the current screen height
        final float screenHeight = getCanvas().getHeight();

        // Calculate how many world units the camera needs to move per pixel
        // This value is based on the current screen height and, for
        // the nearest node from the cursor (when the mouse was pressed),
        // the distance to the camera in the direction of the screen centre.
        float worldUnitsPerPixel = (float) (panReferencePoint.getLength() * Math.tan(Math.toRadians(Camera.FIELD_OF_VIEW / 2.0)) * 2) / screenHeight;

        // Convert the pixel distances into world unit distances
        return new Vector3f(-dx * worldUnitsPerPixel, dy * worldUnitsPerPixel, 0);
    }

    @Override
    public Vector3f windowToGraphCoordinates(final Camera camera, final Point point) {
        final Camera originCamera = new Camera(camera);
        CameraUtilities.moveEyeToOrigin(originCamera);
        Matrix44f modelViewProjectionMatrix = getCameraModelViewProjectionMatrix(originCamera);
        Vector3f worldPosition = new Vector3f();
        final Vector3f direction = CameraUtilities.getFocusVector(originCamera);
        direction.scale(10);
        Graphics3DUtilities.screenToWorldCoordinates(new Vector3f(point.x, point.y, 0), direction, modelViewProjectionMatrix, getViewport(), worldPosition);
        worldPosition.add(camera.lookAtEye);
        return worldPosition;
    }

    @Override
    public Vector3f closestNodeCameraCoordinates(GraphReadMethods graph, Camera camera, Point p) {

        // Calculate the height and width of the viewing frustrum as a function of distance from the camera
        final float verticalScale = (float) (Math.tan(Math.toRadians(Camera.FIELD_OF_VIEW / 2.0)));
        final float horizontalScale = verticalScale * getCanvas().getWidth() / getCanvas().getHeight();
        float closestDistance = Float.MAX_VALUE;
        Vector3f closestNode = null;
        boolean foundScreenNode = false;

        // Iterate through the camera locations of each node in the graph
        Iterator<Vector3f> nodeLocations = VisualGraphUtilities.streamVertexSceneLocations(graph, camera).iterator();
        while (nodeLocations.hasNext()) {

            final Vector3f nodeLoaction = nodeLocations.next();
            final float zDistanceFromCamera = nodeLoaction.getZ();
            final float distanceFromCamera = nodeLoaction.getLength();

            // Is the vertex in front of the camera?
            if (zDistanceFromCamera < 0) {
                final float horizontalOffset = nodeLoaction.getX() / zDistanceFromCamera;
                final float verticalOffset = nodeLoaction.getY() / zDistanceFromCamera;

                // Is this vertex visible on the screen?
                if (horizontalOffset > -horizontalScale && horizontalOffset < horizontalScale && verticalOffset > -verticalScale && verticalOffset < verticalScale) {
                    // Is the first or closest node visible on the screen, record it as the closest node
                    if (!foundScreenNode || distanceFromCamera < closestDistance) {
                        closestNode = nodeLoaction;
                        closestDistance = closestNode.getLength();
                        foundScreenNode = true;
                    }
                } else if (!foundScreenNode && distanceFromCamera < closestDistance) {
                    // If no vertices on the screen have been found, this vertex is in front of the camera, and is the closest (or first) such vertex, record it as the closest node.
                    closestNode = nodeLoaction;
                    closestDistance = closestNode.getLength();
                }
            }
        }
        return closestNode;
    }

    @Override
    public float[] windowBoxToCameraBox(int left, int right, int top, int bottom) {
        final float verticalScale = (float) (Math.tan(Math.toRadians(Camera.FIELD_OF_VIEW / 2.0)));
        final float horizontalScale = verticalScale * getCanvas().getWidth() / getCanvas().getHeight();
        final int[] viewport = getViewport();

        final float leftScale = (((float) left / (float) viewport[2]) - 0.5f) * horizontalScale * 2;
        final float rightScale = (((float) right / (float) viewport[2]) - 0.5f) * horizontalScale * 2;
        final float topScale = (((float) (viewport[3] - top) / (float) viewport[3]) - 0.5f) * verticalScale * 2;
        final float bottomScale = (((float) (viewport[3] - bottom) / (float) viewport[3]) - 0.5f) * verticalScale * 2;
        return new float[]{leftScale, rightScale, topScale, bottomScale};
    }
}
