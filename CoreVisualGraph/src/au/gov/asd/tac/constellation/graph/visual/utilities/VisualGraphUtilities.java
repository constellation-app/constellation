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
package au.gov.asd.tac.constellation.graph.visual.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix33f;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides a number of utility methods to retrieve visual attributes from the
 * graph.
 * <p>
 * The main reason this class exists is to save a number of menu items and
 * sidebar buttons from having to get their own read-locks in order to update
 * their state based on the graph.
 *
 * @author twilight_sparkle
 */
public class VisualGraphUtilities {
    
    private VisualGraphUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static void setCamera(final GraphWriteMethods graph, final Camera camera) {
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
        if (cameraAttribute != Graph.NOT_FOUND) {
            graph.setObjectValue(cameraAttribute, 0, camera);
        }
    }

    public static Camera getCamera(final GraphReadMethods graph) {
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.get(graph);
        final Camera camera = cameraAttribute != Graph.NOT_FOUND ? graph.getObjectValue(cameraAttribute, 0) : VisualGraphDefaults.DEFAULT_CAMERA;
        return camera != null ? camera : VisualGraphDefaults.DEFAULT_CAMERA;
    }

    public static Camera getCamera(final GraphReadMethods graph, final int cameraAttribute) {
        final Camera camera = cameraAttribute != Graph.NOT_FOUND ? graph.getObjectValue(cameraAttribute, 0) : VisualGraphDefaults.DEFAULT_CAMERA;
        return camera != null ? camera : VisualGraphDefaults.DEFAULT_CAMERA;
    }

    public static void setVertexCoordinates(final GraphWriteMethods graph, final Vector3f coordinates, final int vertexId) {
        final int xAttribute = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttribute = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttribute = VisualConcept.VertexAttribute.Z.get(graph);
        if (xAttribute != Graph.NOT_FOUND && yAttribute != Graph.NOT_FOUND && zAttribute != Graph.NOT_FOUND) {
            graph.setFloatValue(xAttribute, vertexId, coordinates.getX());
            graph.setFloatValue(yAttribute, vertexId, coordinates.getY());
            graph.setFloatValue(zAttribute, vertexId, coordinates.getZ());
        }
    }

    public static void setVertexCoordinates(final GraphWriteMethods graph, final Vector3f coordinates, final int vertexId, final int xAttribute, final int yAttribute, final int zAttribute) {
        if (xAttribute != Graph.NOT_FOUND && yAttribute != Graph.NOT_FOUND && zAttribute != Graph.NOT_FOUND) {
            graph.setFloatValue(xAttribute, vertexId, coordinates.getX());
            graph.setFloatValue(yAttribute, vertexId, coordinates.getY());
            graph.setFloatValue(zAttribute, vertexId, coordinates.getZ());
        }
    }

    public static Vector3f getVertexCoordinates(final GraphReadMethods graph, final int vertexId) {
        final int xAttribute = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttribute = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttribute = VisualConcept.VertexAttribute.Z.get(graph);
        return new Vector3f(xAttribute != Graph.NOT_FOUND ? graph.getFloatValue(xAttribute, vertexId) : VisualGraphDefaults.getDefaultX(vertexId),
                yAttribute != Graph.NOT_FOUND ? graph.getFloatValue(yAttribute, vertexId) : VisualGraphDefaults.getDefaultY(vertexId),
                zAttribute != Graph.NOT_FOUND ? graph.getFloatValue(zAttribute, vertexId) : VisualGraphDefaults.getDefaultZ(vertexId));
    }

    public static Vector3f getVertexCoordinates(final GraphReadMethods graph, final int vertexId, final int xAttribute, final int yAttribute, final int zAttribute) {
        return new Vector3f(xAttribute != Graph.NOT_FOUND ? graph.getFloatValue(xAttribute, vertexId) : VisualGraphDefaults.getDefaultX(vertexId),
                yAttribute != Graph.NOT_FOUND ? graph.getFloatValue(yAttribute, vertexId) : VisualGraphDefaults.getDefaultY(vertexId),
                zAttribute != Graph.NOT_FOUND ? graph.getFloatValue(zAttribute, vertexId) : VisualGraphDefaults.getDefaultZ(vertexId));
    }

    public static Vector3f getAlternateVertexCoordinates(final GraphReadMethods graph, final int vertexId) {
        final int x2Attribute = VisualConcept.VertexAttribute.X2.get(graph);
        final int y2Attribute = VisualConcept.VertexAttribute.Y2.get(graph);
        final int z2Attribute = VisualConcept.VertexAttribute.Z2.get(graph);

        return new Vector3f(x2Attribute != Graph.NOT_FOUND ? graph.getFloatValue(x2Attribute, vertexId) : VisualGraphDefaults.DEFAULT_VERTEX_X2,
                y2Attribute != Graph.NOT_FOUND ? graph.getFloatValue(y2Attribute, vertexId) : VisualGraphDefaults.DEFAULT_VERTEX_Y2,
                z2Attribute != Graph.NOT_FOUND ? graph.getFloatValue(z2Attribute, vertexId) : VisualGraphDefaults.DEFAULT_VERTEX_Z2);
    }

    public static Vector3f getAlternateVertexCoordinates(final GraphReadMethods graph, final int vertexId, final int x2Attribute, final int y2Attribute, final int z2Attribute) {
        return new Vector3f(x2Attribute != Graph.NOT_FOUND ? graph.getFloatValue(x2Attribute, vertexId) : VisualGraphDefaults.DEFAULT_VERTEX_X2,
                y2Attribute != Graph.NOT_FOUND ? graph.getFloatValue(y2Attribute, vertexId) : VisualGraphDefaults.DEFAULT_VERTEX_Y2,
                z2Attribute != Graph.NOT_FOUND ? graph.getFloatValue(z2Attribute, vertexId) : VisualGraphDefaults.DEFAULT_VERTEX_Z2);
    }

    public static Vector3f getMixedVertexCoordinates(final GraphReadMethods graph, final int vertexId) {
        final Vector3f coordinates = getVertexCoordinates(graph, vertexId);
        final Vector3f altCoordinates = getAlternateVertexCoordinates(graph, vertexId);
        final float mixRatio = getCamera(graph).getMixRatio();
        coordinates.convexCombineWith(altCoordinates, mixRatio);
        return coordinates;
    }

    public static Vector3f getMixedVertexCoordinates(final GraphReadMethods graph, final int vertexId, final int xAttribute, final int x2Attribute, final int yAttribute, final int y2Attribute, final int zAttribute, final int z2Attribute, final int cameraAttribute) {
        final Vector3f coordinates = getVertexCoordinates(graph, vertexId, xAttribute, yAttribute, zAttribute);
        final Vector3f altCoordinates = getAlternateVertexCoordinates(graph, vertexId, x2Attribute, y2Attribute, z2Attribute);
        final float mixRatio = getCamera(graph, cameraAttribute).getMixRatio();
        coordinates.convexCombineWith(altCoordinates, mixRatio);
        return coordinates;
    }

    public static ConnectionMode getConnectionMode(final GraphReadMethods graph) {
        final int connectionModeAttribute = VisualConcept.GraphAttribute.CONNECTION_MODE.get(graph);
        return connectionModeAttribute != Graph.NOT_FOUND ? graph.getObjectValue(connectionModeAttribute, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_MODE;
    }

    public static ConnectionMode getConnectionMode(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            return getConnectionMode(rg);
        } finally {
            rg.release();
        }
    }

    public static boolean getDisplayModeIs3D(final GraphReadMethods graph) {
        final int displayModeAttribute = VisualConcept.GraphAttribute.DISPLAY_MODE_3D.get(graph);
        return displayModeAttribute != Graph.NOT_FOUND ? graph.getBooleanValue(displayModeAttribute, 0) : VisualGraphDefaults.DEFAULT_DISPLAY_MODE_3D;
    }

    public static boolean getDisplayModeIs3D(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            return getDisplayModeIs3D(rg);
        } finally {
            rg.release();
        }
    }

    public static int getDrawFlags(final GraphReadMethods graph) {
        final int drawFlagsAttribute = VisualConcept.GraphAttribute.DRAW_FLAGS.get(graph);
        return drawFlagsAttribute != Graph.NOT_FOUND ? graph.getIntValue(drawFlagsAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAW_FLAGS.getFlags();
    }

    public static int getDrawFlags(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            return getDrawFlags(rg);
        } finally {
            rg.release();
        }
    }

    public static boolean getIsDrawingMode(final GraphReadMethods graph) {
        final int drawModeAttribute = VisualConcept.GraphAttribute.DRAWING_MODE.get(graph);
        return drawModeAttribute != Graph.NOT_FOUND ? graph.getBooleanValue(drawModeAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAWING_MODE;
    }

    public static boolean getIsDrawingMode(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            return getIsDrawingMode(rg);
        } finally {
            rg.release();
        }
    }

    public static boolean getIsDrawingDirectedTransactions(final GraphReadMethods graph) {
        final int drawDirectedTransactionsAttribute = VisualConcept.GraphAttribute.DRAW_DIRECTED_TRANSACTIONS.get(graph);
        return drawDirectedTransactionsAttribute != Graph.NOT_FOUND ? graph.getBooleanValue(drawDirectedTransactionsAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAWING_DIRECTED_TRANSACTIONS;
    }

    public static boolean getIsDrawingDirectedTransactions(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            return getIsDrawingDirectedTransactions(rg);
        } finally {
            rg.release();
        }
    }

    public static Stream<Vector3f> streamVertexWorldLocations(GraphReadMethods rg, final Camera state) {

        final int vertexCount = rg.getVertexCount();

        final float mix = state.getMix();

        // Look up all the required attributes.
        final int xAttr = rg.getAttribute(GraphElementType.VERTEX, "x");
        final int yAttr = rg.getAttribute(GraphElementType.VERTEX, "y");
        final int zAttr = rg.getAttribute(GraphElementType.VERTEX, "z");
        final int x2Attr = rg.getAttribute(GraphElementType.VERTEX, "x2");
        final int y2Attr = rg.getAttribute(GraphElementType.VERTEX, "y2");
        final int z2Attr = rg.getAttribute(GraphElementType.VERTEX, "z2");

        // Do the vertex positions need mixing?
        final boolean requiresMix = x2Attr != Graph.NOT_FOUND && y2Attr != Graph.NOT_FOUND && z2Attr != Graph.NOT_FOUND;

        final Iterable<Vector3f> worldLocations;
        if (xAttr != Graph.NOT_FOUND && yAttr != Graph.NOT_FOUND && zAttr != Graph.NOT_FOUND) {

            worldLocations = () -> new Iterator<Vector3f>() {

                int position = 0;

                @Override
                public boolean hasNext() {
                    return position < vertexCount;
                }

                @Override
                public Vector3f next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    final Vector3f worldLocation = new Vector3f();
                    final int vxId = rg.getVertex(position);

                    // Get the main location of the vertex.
                    float x = rg.getFloatValue(xAttr, vxId);
                    float y = rg.getFloatValue(yAttr, vxId);
                    float z = rg.getFloatValue(zAttr, vxId);

                    // If mixing is required then mix the main location with the alternative location.
                    if (requiresMix) {
                        x = (1 - mix) * x + mix * rg.getFloatValue(x2Attr, vxId);
                        y = (1 - mix) * y + mix * rg.getFloatValue(y2Attr, vxId);
                        z = (1 - mix) * z + mix * rg.getFloatValue(z2Attr, vxId);
                    }

                    worldLocation.set(x, y, z);
                    position++;
                    return worldLocation;
                }
            };
        } else {
            worldLocations = Collections::emptyIterator;
        }

        return StreamSupport.stream(worldLocations.spliterator(), false);
    }

    public static Stream<Vector3f> streamVertexSceneLocations(GraphReadMethods rg, final Camera camera) {

        // Get a copy of the current rotation matrix.
        final Matrix44f modelViewMatrix = Graphics3DUtilities.getModelViewMatrix(camera);
        final Matrix33f rotationMatrix = new Matrix33f();
        modelViewMatrix.getRotationMatrix(rotationMatrix);

        // Convert from world to scene coordinates
        Stream<Vector3f> worldLocations = streamVertexWorldLocations(rg, camera);
        return worldLocations.map(worldLocation -> {
            final float[] screenLocation = modelViewMatrix.multiply(worldLocation.getX(), worldLocation.getY(), worldLocation.getZ(), 1);
            return new Vector3f(screenLocation[0], screenLocation[1], screenLocation[2]);
        });
    }

    public static List<Integer> getSelectedElements(final GraphReadMethods graph) {
        final List<Integer> selectedIds = new ArrayList<>();
        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (vertexSelectedAttribute != Graph.NOT_FOUND) {
            graph.vertexStream().forEach(vertexId -> {
                if (graph.getBooleanValue(vertexSelectedAttribute, vertexId)) {
                    selectedIds.add(vertexId);
                }
            });

//            This code is left in as a suggested improvement pending the outcomes of an investigation into the use of indexing with Constellation
//            final GraphIndexResult result = grm.getElementsWithAttributeValue(vertexSelectedAttribute, Boolean.TRUE);
//            final int resultCount = result.getCount();
//            for (int i = 0; i < resultCount; i++) {
//                selectedIds.add(result.getNextElement());
//            }
        }
        return selectedIds;
    }
}
