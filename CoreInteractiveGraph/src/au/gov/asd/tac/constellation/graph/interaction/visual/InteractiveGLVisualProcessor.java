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
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionFreeformModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionFreeformRenderable;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualOperation;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * An extension of the {@link GLVisualProcessor} that adds support for user interaction through implementing
 * {@link VisualInteraction} and {@link VisualAnnotator}.
 * <p>
 * This provides the same basic visualisation of a graph as {@link GLVisualProcessor} does, adding renderables for a new
 * line, a selection box, overlay planes, and hit testing.
 *
 * @author twilight_sparkle
 */
public class InteractiveGLVisualProcessor extends GLVisualProcessor implements VisualInteraction, VisualAnnotator {

    private final long selectionBoxUpdateId = VisualChangeBuilder.generateNewId();
    private final long selectionFreeformUpdateId = VisualChangeBuilder.generateNewId();
    private final long newLineUpdateId = VisualChangeBuilder.generateNewId();
    private final long greyscaleUpdateId = VisualChangeBuilder.generateNewId();
    private final long hitTestId = VisualChangeBuilder.generateNewId();
    private final long hitTestPointId = VisualChangeBuilder.generateNewId();
    private final HitTester hitTester;
    private final SelectionBoxRenderable selectionBoxRenderable = new SelectionBoxRenderable();
    private final SelectionFreeformRenderable selectionFreeformRenderable = new SelectionFreeformRenderable();
    private final NewLineRenderable newLineRenderable = new NewLineRenderable(this);
    private final PlanesRenderable planesRenderable = new PlanesRenderable();
    private final TransformableGraphDisplayer graphDisplayer = new TransformableGraphDisplayer();

    private InteractionEventHandler handler;
    private DropTargetListener targetListener;
    private DropTarget target;

    private static final Logger LOGGER = Logger.getLogger(InteractiveGLVisualProcessor.class.getName());

    /**
     * Create a new InteractiveGLVisualProcessor.
     *
     * @param debugGl Whether or not to utilise a GLContext that includes debugging.
     * @param printGlCapabilities Whether or not to print out a list of GL capabilities upon initialisation.
     */
    public InteractiveGLVisualProcessor(final boolean debugGl, final boolean printGlCapabilities) {
        super(debugGl, printGlCapabilities);
        setGraphDisplayer(graphDisplayer);
        addRenderable(newLineRenderable);
        addRenderable(selectionBoxRenderable);
        addRenderable(selectionFreeformRenderable);
        addRenderable(planesRenderable);
        hitTester = new HitTester(this);
        addRenderable(hitTester);
    }

    /**
     * Set the specified {@link InteractionEventHandler} to use this processor.
     * <p>
     * This method adds the event handler as a listener (of all the relevant gesture types) to this processor's AWT
     * component.
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
     * @return A {@link DropTarget} wrapping the supplied target and this processor's AWT component.
     */
    public void addDropTargetToCanvas(final DropTargetListener targetListener) {
        this.targetListener = targetListener;
        target = new DropTarget(canvas, this.targetListener);
    }

    private final class GLSetHitTestingOperation implements VisualOperation {

        private final boolean doHitTesting;

        @Override
        public int getPriority() {
            return VisualPriority.ELEVATED_VISUAL_PRIORITY.getValue();
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
            if (resultState.getCurrentHitType() == HitType.NO_ELEMENT && !VisualGraphUtilities.isDrawingMode(GraphManager.getDefault().getActiveGraph())) {
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
    public VisualOperation setSelectionBoxModel(final SelectionBoxModel model) {
        selectionBoxRenderable.queueModel(model);
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(selectionBoxUpdateId).build());
    }

    @Override
    public VisualOperation setSelectionFreeformModel(final SelectionFreeformModel model) {
        selectionFreeformRenderable.queueModel(model);
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(selectionFreeformUpdateId).build());
    }

    @Override
    public VisualOperation flagBusy(boolean isBusy) {
        graphDisplayer.setGreyscale(isBusy);
        return () -> Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE)
                .withId(greyscaleUpdateId).build());
    }

    @Override
    public Vector3f convertZoomPointToDirection(final Point zoomPoint) {
        return new Vector3f(
                (float) (getCanvas().getWidth() / 2.0 - zoomPoint.x),
                (float) (zoomPoint.y - getCanvas().getHeight() / 2.0),
                (float) (getCanvas().getHeight() / (2 * Math.tan(Camera.FIELD_OF_VIEW * Math.PI / 180 / 2))));
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
        final float xDist = (getCanvas().getWidth() / 2.0F - to.x) / (getCanvas().getWidth() / 2.0F);
        final float yDist = (getCanvas().getHeight() / 2.0F - to.y) / (getCanvas().getHeight() / 2.0F);
        final float xDelta = (from.x - to.x) / 2.0F;
        final float yDelta = (from.y - to.y) / 2.0F;
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

        // Iterate through the camera locations of each node in the graph
        final Stream<NodeCameraDistance> nodeCameraDistances = VisualGraphUtilities.streamVertexSceneLocations(graph, camera)
                .parallel()
                .map(vector -> new NodeCameraDistance(vector, horizontalScale, verticalScale));

        final NodeCameraDistance closest = nodeCameraDistances.parallel().reduce(new NodeCameraDistance(), (ncd1, ncd2) -> NodeCameraDistance.getClosestNode(ncd1, ncd2));

        return closest.nodeLocation;
    }

    @Override
    public float[] windowBoxToCameraBox(int left, int right, int top, int bottom) {
        final float verticalScale = (float) (Math.tan(Math.toRadians(Camera.FIELD_OF_VIEW / 2.0)));
        final float horizontalScale = verticalScale * getCanvas().getWidth() / getCanvas().getHeight();
        final int[] viewport = getViewport();
        final float leftScale = (((float) left / (float) viewport[2]) - 0.5F) * horizontalScale * 2;
        final float rightScale = (((float) right / (float) viewport[2]) - 0.5F) * horizontalScale * 2;
        final float topScale = (((float) (viewport[3] - top) / (float) viewport[3]) - 0.5F) * verticalScale * 2;
        final float bottomScale = (((float) (viewport[3] - bottom) / (float) viewport[3]) - 0.5F) * verticalScale * 2;
        return new float[]{leftScale, rightScale, topScale, bottomScale};
    }

    @Override
    public float getDPIScalingFactor() {
        // HACK_DPI - Get the X Scale value from the GLCanva's transform matrix
        // This method was derived from the JOGL post found here:
        // http://forum.jogamp.org/canvas-not-filling-frame-td4040092.html#a4040210
        try {
            return (float) ((Graphics2D) getCanvas().getGraphics()).getTransform().getScaleX();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Null exception accessing interactionGraph", ex);
            return 1.0F;
        }
    }

    public static class NodeCameraDistance {

        final Vector3f nodeLocation;
        final Float distanceFromCamera;

        public NodeCameraDistance() {
            this.nodeLocation = null;
            this.distanceFromCamera = null;
        }

        public NodeCameraDistance(Vector3f nodeLocation, final float horizontalScale, final float verticalScale) {
            this.nodeLocation = nodeLocation;
            this.distanceFromCamera = getDistanceFromCamera(nodeLocation, horizontalScale, verticalScale);
        }

        public Vector3f getNodeLocation() {
            return nodeLocation;
        }

        public static NodeCameraDistance getClosestNode(final NodeCameraDistance ncd1, final NodeCameraDistance ncd2) {
            NodeCameraDistance closest = null;
            if (ncd1.distanceFromCamera == null) {
                closest = ncd2;
            } else if (ncd2.distanceFromCamera == null) {
                closest = ncd1;
            } else if (ncd1.distanceFromCamera < 0) {
                if (ncd2.distanceFromCamera > ncd1.distanceFromCamera) {
                    closest = ncd2;
                } else {
                    closest = ncd1;
                }
            } else if (ncd2.distanceFromCamera < 0) {
                if (ncd1.distanceFromCamera > ncd2.distanceFromCamera) {
                    closest = ncd1;
                } else {
                    closest = ncd2;
                }
            } else {
                if (ncd1.distanceFromCamera < ncd2.distanceFromCamera) {
                    closest = ncd1;
                } else {
                    closest = ncd2;
                }
            }
            return closest;
        }

        private static Float getDistanceFromCamera(Vector3f nodeLocation, final float horizontalScale, final float verticalScale) {
            final float zDistanceFromCamera = nodeLocation.getZ();
            final float distanceFromCamera = nodeLocation.getLength();

            // Is the vertex in front of the camera?
            if (zDistanceFromCamera < 0) {
                final float horizontalOffset = nodeLocation.getX() / zDistanceFromCamera;
                final float verticalOffset = nodeLocation.getY() / zDistanceFromCamera;

                // Is this vertex visible on the screen?
                if (horizontalOffset > -horizontalScale && horizontalOffset < horizontalScale && verticalOffset > -verticalScale && verticalOffset < verticalScale) {
                    // Is the first or closest node visible on the screen, record it as the closest node
                    return distanceFromCamera;
                } else {
                    // If no vertices on the screen have been found, this vertex is in front of the camera, and is the closest (or first) such vertex, record it as the closest node.
                    return -distanceFromCamera;
                }
            }
            return null;
        }

    }
}
