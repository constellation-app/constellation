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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import java.util.ArrayList;
import java.util.List;

/**
 * This class has been designed to create a simple graph with known elements and attributes for testing purposes.
 * 
 * It is currently quite basic but has potential to enhance tests that require Graph objects.
 * Both for functional testing and procedural testing. 
 * 
 * The idea is that build methods can be called on this graph to build a {@link Graph} object that has known values. 
 * 
 * A relevant test can then run using this Graph object and the output of the test can be verified against static methods 
 * in this class that return expected values without actually referencing the Graph object.
 * 
 * 
 * @author capricornunicorn123
 */
public class TestableGraphBuilder {

    public static int[] getSelectedNodeIds() {
        return new int [] {9, 10, 11, 12, 13, 14, 15, 16};
    }

    public static int[] getNodeIds() {
        return new int [] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
    }
    
    final Graph graph;
    
    private final List<Integer> vertexIds = new ArrayList<>();
    private final List<Integer> transactionIds = new ArrayList<>();

    /**
     * Constructor used to create a Graph object with known elements and attributes.
     * 
     * This constructor is used to create a new Graph object with a builder pattern. 
     * Example usage:
     * <p>
     * Graph testGraph = TestableGraphBuilder().withNodes().withAllTransactions().withBottomLables().build();
     * </p>
     * @throws InterruptedException 
     */
    public TestableGraphBuilder() throws InterruptedException {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new DualGraph(schema);
    }
    
    /**
     * Constructor used to modify a Graph object with known elements and attributes.
     * 
     * This constructor is used to modify an existing Graph object so that it contains known elements and attributes. 
     * Example usage:
     * <p>
     * TestableGraphBuilder(graphWriteMethods).withNodes(graphWriteMethods).withAllTransactions(graphWriteMethods).withBottomLables(graphWriteMethods);
     * </p>
     * @param wg
     */
    public TestableGraphBuilder(final GraphWriteMethods wg) {
        graph = null;
    }
    
    public Graph buildGraphwithEverything() throws InterruptedException {
        return this.withNodes().withAllTransactions().withAllLabels().withDecorators().withBlazes().withConnectionMode(ConnectionMode.LINK).refocusCamera(AxisConstants.Z_NEGATIVE).build();
    }
    
    public void buildGraphwithEverything(final GraphWriteMethods gwm) {
        withNodes(gwm);
        withAllTransactions(gwm);
        withAllLabels(gwm);
        withDecorators(gwm);
        withBlazes(gwm);
        withConnectionMode(gwm, ConnectionMode.LINK);
        refocusCamera(gwm, AxisConstants.Z_NEGATIVE);
    }
    
    /**
     * Ensures that the graph being built has decorators.
     * <p>Decorators include:
     * <ul><li>NW: Tick for all even nodes, cross fro all odd nodes</li>
     * <li>NE: Nothing</li>
     * <li>SE: Pin fro all nodes except the origin.</li>
     * <li>SW: Nothing</li></ul></p>
     * @return
     * @throws InterruptedException 
     */
    public TestableGraphBuilder withDecorators() throws InterruptedException {
        final WritableGraph wg = graph.getWritableGraph("addDecroators", true);
        withDecorators(wg);
        wg.commit();
        
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it contains decorators.
     * <p>Decorators include:
     * <ul><li>NW: Tick for all even nodes, cross fro all odd nodes</li>
     * <li>NE: Nothing</li>
     * <li>SE: Pin for all nodes except the origin.</li>
     * <li>SW: Nothing</li></ul></p>
     * @param gwm 
     */
    public void withDecorators(final GraphWriteMethods gwm) {
        
        // Ensure the attributes exist on the graph
        final int vertexAttributeIdIsGood = gwm.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "isGood", null, false, null);
        final int vertexAttributeIdPinned = VisualConcept.VertexAttribute.PINNED.ensure(gwm);
        final int graphAttributeIdDecorators = VisualConcept.GraphAttribute.DECORATORS.ensure(gwm);
        
        // Create the Decrators
        final VertexDecorators decorators = new VertexDecorators(gwm.getAttributeName(vertexAttributeIdIsGood), null, gwm.getAttributeName(vertexAttributeIdPinned), null);
        
        // Write to the graph
        gwm.setObjectValue(graphAttributeIdDecorators, 0, decorators);
    }
    
    /**
     * Ensures that the graph being built has blazes.
     * <p>Blazes include:
     * <ul><li>Fill in the details</li></ul></p>
     * @return
     * @throws InterruptedException 
     */
    public TestableGraphBuilder withBlazes() throws InterruptedException {
        final WritableGraph wg = graph.getWritableGraph("addDecroators", true);
        withBlazes(wg);
        wg.commit();
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it contains blazes.
     * <p>Blazes include:
     * <ul><li>Fill in the details</li></ul></p>
     * @param gwm 
     */
    private void withBlazes(final GraphWriteMethods gwm) {
        final int blazeAttributeID = VisualConcept.VertexAttribute.BLAZE.ensure(gwm);
        final Blaze blaze = new Blaze(90, ConstellationColor.BROWN);
        gwm.setObjectValue(blazeAttributeID, 0, blaze);
    }
    
    /**
     * Ensures that the graph being built has bottom labels on vertices.
     * <p>Vertex Bottom Labels include:
     * <ul><li>Attribute:Identifier, Color:White, Size: 1</li>
     * <li>Attribute:ForegroundIcon, Color:DarkGreen, Size: 0.5</li></ul></p>
     * @return
     * @throws InterruptedException 
     */
    public TestableGraphBuilder withBottomLabels() throws InterruptedException{
        final WritableGraph wg = graph.getWritableGraph("addBottomLabels", true);
        withBottomLabels(wg);
        wg.commit();
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it has bottom labels on vertices.
     * <p>Vertex Bottom Labels include:
     * <ul><li>Attribute:Identifier, Color:White, Size: 1</li>
     * <li>Attribute:ForegroundIcon, Color:DarkGreen, Size: 0.5</li></ul></p>
     * @param gwm 
     */
    private void withBottomLabels(final GraphWriteMethods gwm) {
        
        // Ensure the attributes exist on the graph
        final int graphAttributeIdVertexBottomLabels = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(gwm);
        VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(gwm);
        VisualConcept.VertexAttribute.IDENTIFIER.ensure(gwm);
        
        // Create the Labels
        final List<GraphLabel> bottomLabels = new ArrayList<>();
        bottomLabels.add(new GraphLabel(VisualConcept.VertexAttribute.IDENTIFIER.getName(), ConstellationColor.WHITE, 1F));
        bottomLabels.add(new GraphLabel(VisualConcept.VertexAttribute.FOREGROUND_ICON.getName(), ConstellationColor.DARK_GREEN, 0.5F));
        
        // Write to the graph
        gwm.setObjectValue(graphAttributeIdVertexBottomLabels, 0, new GraphLabels(bottomLabels));
    }
    
    /**
     * Ensures that the graph being built has top labels on vertices.
     * <p>Vertex Top Labels include:
     * <ul><li>Attribute:Color, Color:Magenta, Size: 3</li></ul></p>
     * @return
     * @throws InterruptedException 
     */
    public TestableGraphBuilder withTopLabels() throws InterruptedException{
        final WritableGraph gwm = graph.getWritableGraph("addTopLabels", true);
        withTopLabels(gwm);
        gwm.commit();
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it has top labels on vertices.
     * <p>Vertex Top Labels include:
     * <ul><li>Attribute:Color, Color:Magenta, Size: 3</li></ul></p>
     * @param gwm 
     */
    private void withTopLabels(GraphWriteMethods gwm) {
        
        // Ensure the attributes exist on the graph
        final int graphAttributeIdVertexTopLabels = VisualConcept.GraphAttribute.TOP_LABELS.ensure(gwm);
        VisualConcept.VertexAttribute.COLOR.ensure(gwm);
        
        // Create the Labels
        final List<GraphLabel> topLabels = new ArrayList<>();
        topLabels.add(new GraphLabel(VisualConcept.VertexAttribute.COLOR.getName(), ConstellationColor.MAGENTA, 0.5F));
        
        // Write to the graph
        gwm.setObjectValue(graphAttributeIdVertexTopLabels, 0, new GraphLabels(topLabels));
    }
    
    /**
     * Ensures that the graph being built has labels on transactions.
     * <p>Transaction Labels include:
     * <ul><li>Attribute:Visibility, Color:Light Green, Size: 1</li></ul></p>
     * @return
     * @throws InterruptedException 
     */
    public TestableGraphBuilder withTransactionLabels() throws InterruptedException{
        final WritableGraph wg = graph.getWritableGraph("addTransactionLabels", true);
        withTransactionLabels(wg);
        wg.commit();
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it has labels on transactions.
     * <p>Transaction Labels include:
     * <ul><li>Attribute:Visibility, Color:Light Green, Size: 1</li></ul></p>
     * @param gwm 
     */
    private void withTransactionLabels(final GraphWriteMethods gwm) {
        
        // Ensure the attributes exist on the graph
        final int graphAttributeIdTransactionLabels = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(gwm);
        VisualConcept.TransactionAttribute.VISIBILITY.ensure(gwm);
        
        // Create the Labels
        final List<GraphLabel> transactionLabels = new ArrayList<>();
        transactionLabels.add(new GraphLabel(VisualConcept.TransactionAttribute.VISIBILITY.getName(), ConstellationColor.LIGHT_GREEN, 1F));
        
        // Write to the graph
        gwm.setObjectValue(graphAttributeIdTransactionLabels, 0, new GraphLabels(transactionLabels));
    }
       
     /**
     * Ensures that the graph being built has bottom and top labels on vertices and also labels on transactions.
     * <p>Vertex Top Labels include:
     * <ul><li>Attribute:Color, Color:Magenta, Size: 3</li></ul></p>
     * <p>Bottom Labels include:
     * <ul><li>Attribute:Identifier, Color:White, Size: 1</li>
     * <li>Attribute:ForegroundIcon, Color:DarkGreen, Size: 0.5</li></ul></p>
     * <p>Transaction Labels include:
     * <ul><li>Attribute:Visibility, Color:Light Green, Size: 1</li></ul></p>
     * @return
     * @throws InterruptedException 
     */
    public TestableGraphBuilder withAllLabels() throws InterruptedException{
        withTopLabels();  
        withBottomLabels();     
        withTransactionLabels();     
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it has bottom and top labels on vertices and also labels on transactions.
     * <p>Vertex Top Labels include:
     * <ul><li>Attribute:Color, Color:Magenta, Size: 3</li></ul></p>
     * <p>Bottom Labels include:
     * <ul><li>Attribute:Identifier, Color:White, Size: 1</li>
     * <li>Attribute:ForegroundIcon, Color:DarkGreen, Size: 0.5</li></ul></p>
     * <p>Transaction Labels include:
     * <ul><li>Attribute:Visibility, Color:Light Green, Size: 1</li></ul></p>
     * @param gwm 
     */
    public void withAllLabels(final GraphWriteMethods gwm){
        withTopLabels(gwm);
        withBottomLabels(gwm);
        withTransactionLabels(gwm);
    }
    
    
    public TestableGraphBuilder withNodes() throws InterruptedException{
        final WritableGraph wg = graph.getWritableGraph("addVerticies", true);
        withNodes(wg);       
        wg.commit();
        return this;
    }
    
    /**
     * Uses {@link GraphWriteMethods} to modify a graph so that it has vertices with known attributes.
     * 
     * <p>There are 17 vertices created in 3 distinct zones:
     * <ol>
     * <li>1 Vertex at the Origin(0,0,0)</li>
     * <li>1 Vertex at all 8 combinations of ([-3, 3],[-3, 3],[-3, 3])</li>
     * <li>1 Vertex at all 8 combinations of ([-5, 5],[-5, 5],[-5, 5])</li>
     * </ol>
     * <p>
     * 
     * <p>Vertex Attributes include:
     * <ul>
     * <li>{@link Float}: X, Y, Z</li>
     * <li>{@link ConstellationIcon}: ForegrooundIcon, BackgroundIcon</li>
     * <li>{@link ConstellationColor}: color</li>
     * <li>{@link Boolean}: Pinned, Selected, IsGood</li>
     * </ul></p>
     * @param gwm 
     */
    public void withNodes(final GraphWriteMethods gwm) {
        
        // Ensure the attributes exist on the graph
        final int vertexAttributeIdX = VisualConcept.VertexAttribute.X.ensure(gwm);
        final int vertexAttributeIdY = VisualConcept.VertexAttribute.Y.ensure(gwm);
        final int vertexAttributeIdZ = VisualConcept.VertexAttribute.Z.ensure(gwm);
        final int vertexAttributeIdBackgroundImage = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(gwm);
        final int vertexAttributeIdForegroundImage = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(gwm);
        final int vertexAttributeIdRadius = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(gwm);
        final int vertexAttributeIdPinned = VisualConcept.VertexAttribute.PINNED.ensure(gwm);
        final int vertexAttributeIdSelected = VisualConcept.VertexAttribute.SELECTED.ensure(gwm);
        final int VertexAttributeIdIdentifier = VisualConcept.VertexAttribute.IDENTIFIER.ensure(gwm);
        final int vertexAttributeIdIsGood = gwm.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "isGood", null, false, null);
        final int vertexAttributeIdColor = VisualConcept.VertexAttribute.COLOR.ensure(gwm);
        
        final float[][] offsets = {{0F},{-4F, 4F},{-6F, 6F}};
        final ConstellationIcon[] icons = {
            CharacterIconProvider.CHAR_0041,
            CharacterIconProvider.CHAR_0042,
            CharacterIconProvider.CHAR_0043,
            CharacterIconProvider.CHAR_0044,
            CharacterIconProvider.CHAR_0045,
            CharacterIconProvider.CHAR_0046,
            CharacterIconProvider.CHAR_0047,
            CharacterIconProvider.CHAR_0048,
            CharacterIconProvider.CHAR_0049,
            CharacterIconProvider.CHAR_004A,
            CharacterIconProvider.CHAR_004B,
            CharacterIconProvider.CHAR_004C,
            CharacterIconProvider.CHAR_004D,
            CharacterIconProvider.CHAR_004E,
            CharacterIconProvider.CHAR_004F,
            CharacterIconProvider.CHAR_0050,
            CharacterIconProvider.CHAR_0051
        };
        
        // Build the Verticies
        for (int id = 0; id < 17 ; id++){
            
            vertexIds.add(gwm.addVertex(id));    
                      
            final int shell = (int) Math.ceil(id / 8F);
            final float[] offset = offsets[shell];

            final int indicator = id % 8;

            gwm.setFloatValue(vertexAttributeIdX, id, offset[indicator % 2]); // follows pattern 0,1
            gwm.setFloatValue(vertexAttributeIdY, id, offset[(indicator % 4) / 2]); // follows pattern 0,0,1,1
            gwm.setFloatValue(vertexAttributeIdZ, id, offset[indicator / 4]); // follows pattern 0,0,0,0,1,1,1,1
            
            switch (shell){
                case 0 -> {
                    gwm.setObjectValue(vertexAttributeIdBackgroundImage, id, DefaultIconProvider.FLAT_TRIANGLE);
                    gwm.setFloatValue(vertexAttributeIdRadius, id, 3F);
                    gwm.setObjectValue(vertexAttributeIdColor, id, ConstellationColor.RED);
                }
                case 1 -> {
                    gwm.setObjectValue(vertexAttributeIdBackgroundImage, id, DefaultIconProvider.ROUND_CIRCLE);
                    gwm.setFloatValue(vertexAttributeIdRadius, id, 1F);
                    gwm.setObjectValue(vertexAttributeIdColor, id, ConstellationColor.BLUE);
                }
                default -> {
                    gwm.setObjectValue(vertexAttributeIdBackgroundImage, id, DefaultIconProvider.EDGE_SQUARE);
                    gwm.setFloatValue(vertexAttributeIdRadius, id, 0.5F);
                    gwm.setObjectValue(vertexAttributeIdColor, id, ConstellationColor.YELLOW);gwm.setBooleanValue(vertexAttributeIdSelected,id, id == 0);
                }
            }
            gwm.setIntValue(VertexAttributeIdIdentifier, id, id);
            gwm.setBooleanValue(vertexAttributeIdPinned, id, id != 0);
            gwm.setBooleanValue(vertexAttributeIdSelected,id, shell == 2);
            gwm.setBooleanValue(vertexAttributeIdIsGood,id, (id % 2) == 0);
            gwm.setObjectValue(vertexAttributeIdForegroundImage, id, icons[id]);
            
            
            final Camera oldCamera = VisualGraphUtilities.getCamera(gwm);
            final BoundingBox box = new BoundingBox();
            final Camera camera = new Camera(oldCamera);
            BoundingBoxUtilities.recalculateFromGraph(box, gwm, true);
            CameraUtilities.zoomToBoundingBox(camera, box);
            VisualGraphUtilities.setCamera(gwm, camera);
           
        }
    }
    
    public TestableGraphBuilder withAllTransactions() throws InterruptedException{
        withLinearTransactions();    
        withLoopedTransactions(); 
        return this;
    }
    
    public void withAllTransactions(final GraphWriteMethods gwm){
        withLinearTransactions(gwm);
        withLoopedTransactions(gwm);
    }
    
    public TestableGraphBuilder withLoopedTransactions() {
        try {
            WritableGraph wg = graph.getWritableGraph("addLoopedTransactions", true);
            withLoopedTransactions(wg);    
            wg.commit();
        } catch (final InterruptedException ex) {
            //Log that something went wrong
        } finally {
            return this;
        }
    }
    
    
    public void withLoopedTransactions(final GraphWriteMethods gwm){
        for (final int vertex : vertexIds){
            if (vertex % 3 == 0){
                transactionIds.add(gwm.addTransaction(vertex, vertex, true));
            }
            
            if (vertex % 6 == 0){
                transactionIds.add(gwm.addTransaction(vertex, vertex, false));
            }
            
            if (vertex % 12 == 0){
                transactionIds.add(gwm.addTransaction(vertex, vertex, true));
                transactionIds.add(gwm.addTransaction(vertex, vertex, false));
            }
        }
    }
    
    public TestableGraphBuilder withLinearTransactions() throws InterruptedException{
        final WritableGraph wg = graph.getWritableGraph("addLinearTransactions", true);
        withLinearTransactions(wg);    
        wg.commit();
        return this;
    }
    
    public void withLinearTransactions(final GraphWriteMethods gwm){
        for (final int vertex : vertexIds){
            if (vertex % 2 == 0){
                transactionIds.add(gwm.addTransaction(vertex, 0, true));
            } else {
                transactionIds.add(gwm.addTransaction(vertex, 0, false));
            }
            
            if (vertex == 10){
                transactionIds.add(gwm.addTransaction(0, vertex, true));
                transactionIds.add(gwm.addTransaction(0, vertex, false));
                transactionIds.add(gwm.addTransaction(vertex, 0, true));
                transactionIds.add(gwm.addTransaction(vertex, 0, false));
            } 
        }
    }
    
    public TestableGraphBuilder withConnectionMode(final ConnectionMode mode) throws InterruptedException{
        final WritableGraph wg = graph.getWritableGraph("setConnectionMode", true);
        withConnectionMode(wg, mode);    
        wg.commit();
        return this;
    }
    
    public void withConnectionMode(final GraphWriteMethods gwm, final ConnectionMode mode){
        final int connectionModeattributeId = VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(gwm);
        gwm.setObjectValue(connectionModeattributeId, 0, mode);
    }
    
    public TestableGraphBuilder refocusCamera(final AxisConstants axis) throws InterruptedException{
        final WritableGraph wg = graph.getWritableGraph("setCamera", true);
        refocusCamera(wg, axis);    
        wg.commit();
        return this;
    }
    
    public void refocusCamera(final GraphWriteMethods gwm, final AxisConstants axis){
        final int cameraAttributeId = VisualConcept.GraphAttribute.CAMERA.ensure(gwm);
        
        final Camera camera = new Camera(gwm.getObjectValue(cameraAttributeId, 0));
        final BoundingBox boundingBox = new BoundingBox();
        BoundingBoxUtilities.recalculateFromGraph(boundingBox, gwm, false);
        CameraUtilities.refocus(camera, axis, boundingBox);

        gwm.setObjectValue(cameraAttributeId, 0, camera);
    }
    

    public Graph build(){
        return graph;
    }
}
