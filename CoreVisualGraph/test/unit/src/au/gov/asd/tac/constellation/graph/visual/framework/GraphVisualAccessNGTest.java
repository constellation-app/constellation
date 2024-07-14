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
package au.gov.asd.tac.constellation.graph.visual.framework;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class GraphVisualAccessNGTest {

    DualGraph graph;
    StoreGraph sGraph;

    int vxId1;
    int vxId2;

    int tId1;

    public GraphVisualAccessNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();

        sGraph = new StoreGraph();

        vxId1 = sGraph.addVertex();
        vxId2 = sGraph.addVertex();

        tId1 = sGraph.addTransaction(vxId1, vxId2, true);

        graph = new DualGraph(schema, sGraph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getIndigenousChanges method, of class GraphVisualAccess.
     */
    @Test
    public void testGetIndigenousChanges() {
        System.out.println("getIndigenousChanges");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final List<VisualChange> changes = instance.getIndigenousChanges();
        instance.endUpdate();

        assertEquals(changes.size(), 38);
    }

    /**
     * Test of the following methods when the attributes are not found, of class
     * GraphVisualAccess: getBackgroundColor, getHighlightColor, getBlazeSize,
     * getBlazeOpacity, getConnectionOpacity
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetGraphAttributesOneLineFunctionsAttributesNotFound() throws InterruptedException {
        System.out.println("getGraphAttributesOneLineFunctionsAttributesNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final ConstellationColor backgroundColor = instance.getBackgroundColor();
        final ConstellationColor highlightColor = instance.getHighlightColor();
        final float blazeSize = instance.getBlazeSize();
        final float blazeOpacity = instance.getBlazeOpacity();
        final float connectionOpacity = instance.getConnectionOpacity();
        instance.endUpdate();

        assertEquals(backgroundColor, VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR);
        assertEquals(highlightColor, VisualGraphDefaults.DEFAULT_HIGHLIGHT_COLOR);
        assertEquals(blazeSize, VisualGraphDefaults.DEFAULT_BLAZE_SIZE);
        assertEquals(blazeOpacity, VisualGraphDefaults.DEFAULT_BLAZE_OPACITY);
        assertEquals(connectionOpacity, VisualGraphDefaults.DEFAULT_CONNECTION_OPACITY);
    }

    /**
     * Test of the following methods when attributes are added, of class
     * GraphVisualAccess: getBackgroundColor, getHighlightColor, getBlazeSize,
     * getBlazeOpacity, getConnectionOpacity
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetGraphAttributesOneLineFunctionsAttributesAdded() throws InterruptedException {
        System.out.println("getGraphAttributesOneLineFunctionsAttributesAdded");

        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int graphBackgroundColorAttribute = VisualConcept.GraphAttribute.BACKGROUND_COLOR.ensure(wg);
            final int graphHighlightColorAttribute = VisualConcept.GraphAttribute.HIGHLIGHT_COLOR.ensure(wg);
            final int graphBlazeSizeAttribute = VisualConcept.GraphAttribute.BLAZE_SIZE.ensure(wg);
            final int graphBlazeOpacityAttribute = VisualConcept.GraphAttribute.BLAZE_OPACITY.ensure(wg);
            final int graphConnectionOpacityAttribute = VisualConcept.GraphAttribute.CONNECTION_OPACITY.ensure(wg);

            wg.setObjectValue(graphBackgroundColorAttribute, 0, ConstellationColor.BANANA);
            wg.setObjectValue(graphHighlightColorAttribute, 0, ConstellationColor.CARROT);
            wg.setFloatValue(graphBlazeSizeAttribute, 0, 0.5f);
            wg.setFloatValue(graphBlazeOpacityAttribute, 0, 0.6f);
            wg.setFloatValue(graphConnectionOpacityAttribute, 0, 0.7f);
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        instance.updateInternally();
        final ConstellationColor backgroundColor = instance.getBackgroundColor();
        final ConstellationColor highlightColor = instance.getHighlightColor();
        final float blazeSize = instance.getBlazeSize();
        final float blazeOpacity = instance.getBlazeOpacity();
        final float connectionOpacity = instance.getConnectionOpacity();
        instance.endUpdate();

        assertEquals(backgroundColor, ConstellationColor.BANANA);
        assertEquals(highlightColor, ConstellationColor.CARROT);
        assertEquals(blazeSize, 0.5f);
        assertEquals(blazeOpacity, 0.6f);
        assertEquals(connectionOpacity, 0.7f);
    }

    /**
     * Test of getDrawFlags method when attribute is not found, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetDrawFlagsAttributeNotFound() throws InterruptedException {
        System.out.println("getDrawFlagsAttributeNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final DrawFlags flags = instance.getDrawFlags();
        instance.endUpdate();

        assertEquals(flags, VisualGraphDefaults.DEFAULT_DRAW_FLAGS);
    }

    /**
     * Test of getDrawFlags method when attribute is added, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetDrawFlagsAttributeAdded() throws InterruptedException {
        System.out.println("getDrawFlagsAttributeAdded");

        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int graphDrawFlagsAttribute = VisualConcept.GraphAttribute.DRAW_FLAGS.ensure(wg);
            wg.setObjectValue(graphDrawFlagsAttribute, 0, new DrawFlags(DrawFlags.NODE_LABELS));
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);
        instance.beginUpdate();
        instance.updateInternally();
        final DrawFlags flags = instance.getDrawFlags();
        instance.endUpdate();

        assertEquals(flags.getFlags(), 4);
    }

    /**
     * Test of getCamera method when attribute is not found, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetCameraAttributeNotFound() throws InterruptedException {
        System.out.println("getCameraAttributeNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final Camera camera = instance.getCamera();
        instance.endUpdate();

        assertEquals(camera, VisualGraphDefaults.DEFAULT_CAMERA);
        assertEquals(camera.getVisibilityHigh(), 1.0f);
    }

    /**
     * Test of getCamera method when attribute is added, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetCameraAttributeAdded() throws InterruptedException {
        System.out.println("getCameraAttributeAdded");

        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int graphCameraAttribute = VisualConcept.GraphAttribute.CAMERA.ensure(wg);
            final Camera newCamera = new Camera();
            newCamera.setVisibilityHigh(0.6f);
            wg.setObjectValue(graphCameraAttribute, 0, newCamera);
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        instance.updateInternally();
        final Camera camera = instance.getCamera();
        instance.endUpdate();

        assertEquals(camera.getVisibilityHigh(), 0.6f);
    }

    private void changeConnectionMode(final Graph graph, final ConnectionMode mode) throws InterruptedException {
        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int graphConnectionModeAttribute = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(wg);
            wg.setObjectValue(graphConnectionModeAttribute, 0, mode);
        } finally {
            wg.commit();
        }
    }

    private void assertConnectionId(final GraphVisualAccess access) {
        access.beginUpdate();
        access.updateInternally();
        final int id = access.getConnectionId(0);
        access.endUpdate();

        //expected result could be transaction id, edge id, or link id
        assertEquals(id, 0);
    }

    /**
     * Test of getConnectionId method, of class GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetConnectionId() throws InterruptedException {
        System.out.println("getConnectionId");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        // Default connection mode is Edge
        assertConnectionId(instance);

        changeConnectionMode(graph, ConnectionMode.TRANSACTION);
        assertConnectionId(instance);

        changeConnectionMode(graph, ConnectionMode.LINK);
        assertConnectionId(instance);
    }

    private void assertConnectionDirection(final GraphVisualAccess access, final int pos, final ConnectionDirection expResult) {
        access.beginUpdate();
        access.updateInternally();
        final ConnectionDirection direction = access.getConnectionDirection(pos);
        access.endUpdate();

        assertEquals(direction, expResult);
    }

    /**
     * Test of getConnectionDirection method, of class GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetConnectionDirection() throws InterruptedException {
        System.out.println("getConnectionDirection");

        // TODO: add more test cases to cover function
        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        assertConnectionDirection(instance, 0, ConnectionDirection.LOW_TO_HIGH);

        changeConnectionMode(graph, ConnectionMode.TRANSACTION);
        assertConnectionDirection(instance, 0, ConnectionDirection.LOW_TO_HIGH);

        changeConnectionMode(graph, ConnectionMode.LINK);
        assertConnectionDirection(instance, 0, ConnectionDirection.LOW_TO_HIGH);
    }

    /**
     * Test of getConnectionDirected method when attribute is not found, of
     * class GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetConnectionDirectedAttributeNotFound() throws InterruptedException {
        System.out.println("getConnectionDirectedAttributeNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        final boolean directed = instance.isConnectionDirected(0);
        assertEquals(directed, false);
    }

    private void assertConnectionDirected(final GraphVisualAccess access) {
        access.beginUpdate();
        access.updateInternally();
        final boolean directed = access.isConnectionDirected(0);
        access.endUpdate();

        assertEquals(directed, true);
    }

    /**
     * Test of getConnectionDirected method when attribute is added, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetConnectionDirectedAttributeAdded() throws InterruptedException {
        System.out.println("getConnectionDirectedAttributeAdded");

        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(wg);
            // this is the value the attribute would be set to if it existed before the transaction was created
            wg.setBooleanValue(transactionDirectedAttribute, tId1, true);
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        assertConnectionDirected(instance);

        changeConnectionMode(graph, ConnectionMode.TRANSACTION);
        assertConnectionDirected(instance);

        changeConnectionMode(graph, ConnectionMode.LINK);
        assertConnectionDirected(instance);
    }

    /**
     * Test of the following methods when attributes are not found, of class
     * GraphVisualAccess: getX, getY, getZ, getX2, getY2, getZ2,
     * getBackgroundIcon, getForegroundIcon, getVertexSelected, getVertexDimmed,
     * getRadius, getNWDecorator, getNEDecorator, getSEDecorator, getSWDecorator
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetVertexAttributesOneLineFunctionsAttributesNotFound() throws InterruptedException {
        System.out.println("getVertexAttributesOneLineFunctionsAttributesNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final float x = instance.getX(0);
        final float y = instance.getY(0);
        final float z = instance.getZ(0);
        final float x2 = instance.getX2(0);
        final float y2 = instance.getY2(0);
        final float z2 = instance.getZ2(0);

        final String backgroundIcon = instance.getBackgroundIcon(0);
        final String foregroundIcon = instance.getForegroundIcon(0);
        final boolean vertexSelected = instance.isVertexSelected(0);
        final boolean vertexDimmed = instance.isVertexDimmed(0);
        final float radius = instance.getRadius(0);

        final String nwDecorator = instance.getNWDecorator(0);
        final String neDecorator = instance.getNEDecorator(0);
        final String seDecorator = instance.getSEDecorator(0);
        final String swDecorator = instance.getSWDecorator(0);
        instance.endUpdate();

        assertEquals(x, VisualGraphDefaults.getDefaultX(vxId1));
        assertEquals(y, VisualGraphDefaults.getDefaultY(vxId1));
        assertEquals(z, VisualGraphDefaults.getDefaultZ(vxId1));
        assertEquals(x2, VisualGraphDefaults.DEFAULT_VERTEX_X2);
        assertEquals(y2, VisualGraphDefaults.DEFAULT_VERTEX_Y2);
        assertEquals(z2, VisualGraphDefaults.DEFAULT_VERTEX_Z2);

        assertEquals(backgroundIcon, VisualGraphDefaults.DEFAULT_VERTEX_BACKGROUND_ICON);
        assertEquals(foregroundIcon, VisualGraphDefaults.DEFAULT_VERTEX_FOREGROUND_ICON);
        assertEquals(vertexSelected, VisualGraphDefaults.DEFAULT_VERTEX_SELECTED);
        assertEquals(vertexDimmed, VisualGraphDefaults.DEFAULT_VERTEX_DIMMED);
        assertEquals(radius, VisualGraphDefaults.DEFAULT_VERTEX_RADIUS);

        assertEquals(nwDecorator, null);
        assertEquals(neDecorator, null);
        assertEquals(seDecorator, null);
        assertEquals(swDecorator, null);
    }

    /**
     * Test of the following methods when attributes are added, of class
     * GraphVisualAccess: getX, getY, getZ, getX2, getY2, getZ2,
     * getBackgroundIcon, getForegroundIcon, getVertexSelected, getVertexDimmed,
     * getRadius, getNWDecorator, getNEDecorator, getSEDecorator, getSWDecorator
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetVertexAttributesOneLineFunctionsAttributesAdded() throws InterruptedException {
        System.out.println("getVertexAttributesOneLineFunctionsAttributesAdded");

        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int vertexXAttribute = VisualConcept.VertexAttribute.X.ensure(wg);
            final int vertexYAttribute = VisualConcept.VertexAttribute.Y.ensure(wg);
            final int vertexZAttribute = VisualConcept.VertexAttribute.Z.ensure(wg);
            final int vertexX2Attribute = VisualConcept.VertexAttribute.X2.ensure(wg);
            final int vertexY2Attribute = VisualConcept.VertexAttribute.Y2.ensure(wg);
            final int vertexZ2Attribute = VisualConcept.VertexAttribute.Z2.ensure(wg);

            final int vertexBackgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(wg);
            final int vertexForegroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(wg);
            final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            final int vertexDimmedAttribute = VisualConcept.VertexAttribute.DIMMED.ensure(wg);
            final int vertexRadiusAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(wg);

            final int graphDecoratorsAttribute = VisualConcept.GraphAttribute.DECORATORS.ensure(wg);

            wg.setFloatValue(vertexXAttribute, vxId1, 2.0f);
            wg.setFloatValue(vertexYAttribute, vxId1, 3.0f);
            wg.setFloatValue(vertexZAttribute, vxId1, 4.0f);
            wg.setFloatValue(vertexX2Attribute, vxId1, 5.0f);
            wg.setFloatValue(vertexY2Attribute, vxId1, 6.0f);
            wg.setFloatValue(vertexZ2Attribute, vxId1, 7.0f);

            wg.setStringValue(vertexBackgroundIconAttribute, vxId1, "Background.Square");
            wg.setStringValue(vertexForegroundIconAttribute, vxId1, "Noise");
            wg.setBooleanValue(vertexSelectedAttribute, vxId1, true);
            wg.setBooleanValue(vertexDimmedAttribute, vxId1, true);
            wg.setFloatValue(vertexRadiusAttribute, vxId1, 1.5f);

            final VertexDecorators decorators = new VertexDecorators(
                    VisualConcept.VertexAttribute.BACKGROUND_ICON.getName(),
                    VisualConcept.VertexAttribute.FOREGROUND_ICON.getName(),
                    VisualConcept.VertexAttribute.SELECTED.getName(),
                    VisualConcept.VertexAttribute.DIMMED.getName());
            wg.setObjectValue(graphDecoratorsAttribute, 0, decorators);
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        instance.updateInternally();
        final float x = instance.getX(0);
        final float y = instance.getY(0);
        final float z = instance.getZ(0);
        final float x2 = instance.getX2(0);
        final float y2 = instance.getY2(0);
        final float z2 = instance.getZ2(0);

        final String backgroundIcon = instance.getBackgroundIcon(0);
        final String foregroundIcon = instance.getForegroundIcon(0);
        final boolean vertexSelected = instance.isVertexSelected(0);
        final boolean vertexDimmed = instance.isVertexDimmed(0);
        final float radius = instance.getRadius(0);

        final String nwDecorator = instance.getNWDecorator(0);
        final String neDecorator = instance.getNEDecorator(0);
        final String seDecorator = instance.getSEDecorator(0);
        final String swDecorator = instance.getSWDecorator(0);
        instance.endUpdate();

        assertEquals(x, 2.0f);
        assertEquals(y, 3.0f);
        assertEquals(z, 4.0f);
        assertEquals(x2, 5.0f);
        assertEquals(y2, 6.0f);
        assertEquals(z2, 7.0f);

        assertEquals(backgroundIcon, "Background.Square");
        assertEquals(foregroundIcon, "Noise");
        assertEquals(vertexSelected, true);
        assertEquals(vertexDimmed, true);
        assertEquals(radius, 1.5f);

        assertEquals(nwDecorator, "Background.Square");
        assertEquals(neDecorator, "Noise");
        assertEquals(seDecorator, "true");
        assertEquals(swDecorator, "true");
    }

    /**
     * Test of getVertexColor method when attributes are not found, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetVertexColorAttributesNotFound() throws InterruptedException {
        System.out.println("getVertexColorAttributeNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final ConstellationColor color1 = instance.getVertexColor(0);
        final ConstellationColor color2 = instance.getVertexColor(1);
        instance.endUpdate();

        assertEquals(color1, VisualGraphDefaults.DEFAULT_VERTEX_COLOR);
        assertEquals(color2, VisualGraphDefaults.DEFAULT_VERTEX_COLOR);
    }

    /**
     * Test of getVertexColor method when attributes are added, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetVertexColorAttributesAdded() throws InterruptedException {
        System.out.println("getVertexColorAttributesAdded");

        final WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int graphNodeColorReferenceAttribute = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(wg);
            final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(wg);
            wg.setStringValue(graphNodeColorReferenceAttribute, 0, VisualConcept.VertexAttribute.COLOR.getName());
            wg.setObjectValue(vertexColorAttribute, vxId1, ConstellationColor.BANANA);
            wg.setObjectValue(vertexColorAttribute, vxId2, ConstellationColor.CARROT);
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        instance.updateInternally();
        final ConstellationColor color1 = instance.getVertexColor(0);
        final ConstellationColor color2 = instance.getVertexColor(1);
        instance.endUpdate();

        assertEquals(color1, ConstellationColor.BANANA);
        assertEquals(color2, ConstellationColor.CARROT);
    }

    /**
     * Test of getVertexVisibility method when attributes are not found, of
     * class GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetVertexVisibilityAttributesNotFound() throws InterruptedException {
        System.out.println("getVertexVisibilityAttributesNotFound");

        final GraphVisualAccess instance = new GraphVisualAccess(graph);

        instance.beginUpdate();
        final float visibility1 = instance.getVertexVisibility(0);
        final float visibility2 = instance.getVertexVisibility(1);
        instance.endUpdate();

        assertEquals(visibility1, VisualGraphDefaults.DEFAULT_VERTEX_VISIBILITY);
        assertEquals(visibility2, VisualGraphDefaults.DEFAULT_VERTEX_VISIBILITY);
    }

    /**
     * Test of getVertexVisibility method when attributes are added, of class
     * GraphVisualAccess.
     *
     * @throws InterruptedException
     */
    @Test
    public void testGetVertexVisibilityAttributesAdded() throws InterruptedException {
        System.out.println("getVertexVisibilityAttributesAdded");

        WritableGraph wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int vertexLayerVisibilityAttribute = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(wg);
            wg.setObjectValue(vertexLayerVisibilityAttribute, vxId1, 0.4f);
            wg.setObjectValue(vertexLayerVisibilityAttribute, vxId2, 0.75f);
        } finally {
            wg.commit();
        }

        final GraphVisualAccess instance = new GraphVisualAccess(graph);
        instance.beginUpdate();
        instance.updateInternally();
        float visibility1 = instance.getVertexVisibility(0);
        float visibility2 = instance.getVertexVisibility(1);
        instance.endUpdate();

        assertEquals(visibility1, 0.4f);
        assertEquals(visibility2, 0.75f);

        wg = graph.getWritableGraph("Graph Visual Access", true);
        try {
            final int vertexVisibilityAttribute = VisualConcept.VertexAttribute.VISIBILITY.ensure(wg);
            wg.setObjectValue(vertexVisibilityAttribute, vxId1, 0.5f);
            wg.setObjectValue(vertexVisibilityAttribute, vxId2, 0.2f);
        } finally {
            wg.commit();
        }

        instance.beginUpdate();
        instance.updateInternally();
        visibility1 = instance.getVertexVisibility(0);
        visibility2 = instance.getVertexVisibility(1);
        instance.endUpdate();

        assertEquals(visibility1, 0.2f);
        assertEquals(visibility2, 0.15f);
    }

    /**
     * Test of getBlazed method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetBlazed() {
//        System.out.println("getBlazed");
//    }
    /**
     * Test of getBlazeAngle method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetBlazeAngle() {
//        System.out.println("getBlazeAngle");
//    }
    /**
     * Test of getBlazeColor method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetBlazeColor() {
//        System.out.println("getBlazeColor");
//    }
    /**
     * Test of getConnectionColor method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionColor() {
//        System.out.println("getConnectionColor");
//    }
    /**
     * Test of getConnectionSelected method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionSelected() {
//        System.out.println("getConnectionSelected");
//    }
    /**
     * Test of getConnectionVisibility method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionVisibility() {
//        System.out.println("getConnectionVisibility");
//    }
    /**
     * Test of getConnectionDimmed method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionDimmed() {
//        System.out.println("getConnectionDimmed");
//    }
    /**
     * Test of getConnectionLineStyle method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionLineStyle() {
//        System.out.println("getConnectionLineStyle");
//    }
    /**
     * Test of getConnectionWidth method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionWidth() {
//        System.out.println("getConnectionWidth");
//    }
    /**
     * Test of getConnectionLowVertex method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionLowVertex() {
//        System.out.println("getConnectionLowVertex");
//    }
    /**
     * Test of getConnectionHighVertex method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionHighVertex() {
//        System.out.println("getConnectionHighVertex");
//    }
    /**
     * Test of getLinkLowVertex method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetLinkLowVertex() {
//        System.out.println("getLinkLowVertex");
//    }
    /**
     * Test of getLinkHighVertex method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetLinkHighVertex() {
//        System.out.println("getLinkHighVertex");
//    }
    /**
     * Test of getLinkSource method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetLinkSource() {
//        System.out.println("getLinkSource");
//    }
    /**
     * Test of getLinkDestination method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetLinkDestination() {
//        System.out.println("getLinkDestination");
//    }
    /**
     * Test of getLinkConnectionCount method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetLinkConnectionCount() {
//        System.out.println("getLinkConnectionCount");
//    }
    /**
     * Test of getVertexTopLabelText method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetVertexTopLabelText() {
//        System.out.println("getVertexTopLabelText");
//    }
    /**
     * Test of getVertexBottomLabelText method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetVertexBottomLabelText() {
//        System.out.println("getVertexBottomLabelText");
//    }
    /**
     * Test of getConnectionLabelText method, of class GraphVisualAccess.
     */
//    @Test
//    public void testGetConnectionLabelText() {
//        System.out.println("getConnectionLabelText");
//    }
    /**
     * Test of updateModCounts method, of class GraphVisualAccess.
     */
//    @Test
//    public void testUpdateModCounts() {
//        System.out.println("updateModCounts");
//    }
}
