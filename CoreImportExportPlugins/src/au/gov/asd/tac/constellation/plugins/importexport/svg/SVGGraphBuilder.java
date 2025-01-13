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
package au.gov.asd.tac.constellation.plugins.importexport.svg;

import au.gov.asd.tac.constellation.plugins.importexport.svg.tasks.GenerateSVGNodesTask;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.interaction.animation.Animation;
import au.gov.asd.tac.constellation.graph.interaction.animation.AnimationUtilities;
import au.gov.asd.tac.constellation.graph.interaction.animation.PanAnimation;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.graph.visual.utilities.BoundingBoxUtilities;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGObjectConstants;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGTemplateConstants;
import au.gov.asd.tac.constellation.plugins.importexport.svg.tasks.GenerateSVGBlazesTask;
import au.gov.asd.tac.constellation.plugins.importexport.svg.tasks.GenerateSVGConnectionsTask;
import au.gov.asd.tac.constellation.plugins.MultiTaskInteraction;
import au.gov.asd.tac.constellation.utilities.camera.BoundingBox;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;        
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.svg.SVGTypeConstants;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.utilities.threadpool.ConstellationGlobalThreadPool;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Builder that generates the the output SVG file.
 * The builder abstracts the responsibility of building an SVG from the {@link ExportToSVGPlugin}.
 * Currently the builder requires a ReadableGraph, a title and a PluginInterraction.
 * <pre>
 * Example Usage: {@code new SVGGraphBuilder().withTitle("myTitle").withReadableGraph(graph).withInteraction(interaction).build();}
 * </pre>
 * 
 * @author capricornunicorn123
 */
public class SVGGraphBuilder {
    
    private final static String BUILDING_REPORT_MESSAGE= "Building Graph";
    
    // Variables specified when the Builder class is instantiated
    private final Matrix44f modelViewProjectionMatrix = new Matrix44f();
    private final VisualManager visualManager;
    private final GraphVisualAccess access;
    
    // Variables with default values, customisable by the builder pattern 
    private DrawFlags drawFlags = new DrawFlags(true, true, true, true, true);
    private boolean selectedElementsOnly = false;
    private ConstellationColor backgroundColor = VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR;
        
    // Variables without default values, customisable by the builder pattern 
    private GraphReadMethods readableGraph = null;
    private PluginInteraction interaction = null;
    private AxisConstants exportPerspective = null;
    private String graphTitle = null;
    
    // Variables created during preBuild
    private Camera camera = new Camera();
    private Frustum viewFrustum;
    private int[] viewPort;    
    private ExecutorService threadPool;
    private File assetDirectoty;
     
    /**
    * Builder that generates the the output SVG file.
    * @param svg 
    */
    public SVGGraphBuilder() {
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        visualManager = GraphNode.getGraphNode(currentGraph).getVisualManager();
        access = new GraphVisualAccess(currentGraph);
   }

    /**
     * Specifies the directory for SVG assets to be exported to. 
     * If a directory is provided, the directory must be in the same folder as the main SVG file.
     * Passing a directory of null will cause the plugin to embed SVG assets into the main svg file.
     * 
     * @param directory
     * @return 
     */
    public SVGGraphBuilder withAssetDirectory(final File directory){
        assetDirectoty = directory;
        return this;
    }
    
    /**
     * Specifies the number of cores that this plugin should utilize to complete its execution.
     * Due to the processor intensive workload running this plugin, a unique ExecutorService is 
     * requested to facilitate multi-threading in this export.
     * 
     * @param cores
     * @return 
     */
    public SVGGraphBuilder withCores(final int cores) {
        threadPool = ConstellationGlobalThreadPool.getThreadPool().getFixedThreadPool("SVG Export", cores);
        return this;
    }
    
    /**
     * Specifies the {@link GraphReadMethods} representing the current active graph.
     * @param graph used to define the bounding box of the graph
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withReadableGraph(final GraphReadMethods graph) {
        readableGraph = graph;
        return this;
    }

    /**
     * Specifies the {@link PluginInteraction} instance to use for updating on plugin progress.
     * @param interactionReference 
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withInteraction(final PluginInteraction interactionReference) {
        this.interaction = interactionReference;
        return this;
    }

    /**
     * Specifies the title of the graph being exported.
     * @param title 
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withTitle(final String title) {
        graphTitle = title;
        return this;
    }

    /**
     * Specifies the {@link ConstellationColor} of the graph background being exported.
     * @param color the color of the graph background.
     * @return SVGGraphBuilder
     */
    public SVGGraphBuilder withBackground(final ConstellationColor color) {
        backgroundColor = color;
        return this;
    }

    /**
     * Specifies if only selected Nodes and related connections are to be included in the export.
     * @param selectedElementsOnlyFlag
     * @return 
     */
    public SVGGraphBuilder withSelectedElementsOnly(final boolean selectedElementsOnlyFlag) {
        selectedElementsOnly = selectedElementsOnlyFlag;
        return this;
    }

    /**
     * Specifies which visual elements should be included in the export.
     * @param drawFlagsReference
     * @return 
     */
    public SVGGraphBuilder withDrawFlags(final DrawFlags drawFlagsReference){
        drawFlags = drawFlagsReference;
        return this;
    }
    /**
     * Specifies the Perspective to export this graph from.
     * Setting the value as null will export the graph from the current perspective.
     * @param exportPerspectiveReference
     * @return 
     */
    public SVGGraphBuilder fromPerspective(final AxisConstants exportPerspectiveReference) {
        exportPerspective = exportPerspectiveReference;
        return this;
    }

    /**
     * Builds an SVGGraphObject representing the provided graph.
     * @return SVGData
     */
    public SVGData build() throws InterruptedException, IllegalArgumentException{
 
        // The layout template is the base for an exported SVG Graph and contains the required styling and child elements for the export
        final SVGObject svgGraph = SVGTemplateConstants.LAYOUT.getSVGObject();
        try {
            preBuild();
            // The content section of the layout is where the graphical visual elements sit.
            final SVGObject svgContent = SVGObjectConstants.CONTENT.findIn(svgGraph);
            buildNodes(svgContent, SVGObjectConstants.DEFINITIONS.findIn(svgGraph));
            svgContent.setChildren(buildConnections());
            svgContent.setChildren(buildBlazes());

            buildLayout(svgGraph);
        
        // This plugin may be interrupted by users. ensure the localy managed threads are closed off correctly.
        } catch (final InterruptedException ex){
            threadPool.shutdown();
            Thread.currentThread().interrupt();  
        } finally {
            // Clean up the builder
            postBuild();     
        }
        return svgGraph.toSVGData();
    }       

    /**
     * Sets up the Builder control attributes and reference utilities. 
     */
    private void preBuild() throws IllegalArgumentException {
        
        // Initialise the VisualGraphAccess
        access.beginUpdate();
        access.updateInternally();
        
        // Thorw errors if attributes without default values have not been set
        if (readableGraph == null ) {
            throw new IllegalArgumentException("SVGGraphBuilder requires GraphReadMethods to build");
        } else if (interaction == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires a PluginInteraction to build");
        } else if (graphTitle == null) {
            throw new IllegalArgumentException("SVGGraphBuilder requires a graph title to build");
        }
        
        // Set the camera position
        this.camera = access.getCamera();

        // Add repositioning animation if the export perspective is from a particular axis.
        if (exportPerspective != null) {        
            final Camera oldCamera = new Camera(this.camera);
            final BoundingBox box = new BoundingBox();
            BoundingBoxUtilities.recalculateFromGraph(box, readableGraph, selectedElementsOnly);
            CameraUtilities.refocus(camera, exportPerspective, box);
            AnimationUtilities.startAnimation(new PanAnimation(String.format("Reset to %s View", exportPerspective), oldCamera, camera, true), this.readableGraph.getId());
        }
        
        final Frame frame = new Frame(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp);
        frame.setOrigin(camera.lookAtEye); //Reposition the viewing position from the origin to the camera eye location
        camera.setObjectFrame(frame); //Store the frame in the camera for easy access in other methods of this class.
        
        // Determine the dimensions of the users InteractiveGraphView pane
        final int paneHeight = visualManager.getVisualComponent().getHeight();   
        final int paneWidth = visualManager.getVisualComponent().getWidth();

        // Define the view frustum in local units
        final float fieldOfView = Camera.FIELD_OF_VIEW; //Increase the field of view to ensure that objects near the edges are rendered corretly.
        final float aspect = paneWidth / (float) paneHeight;
        final float ymax = Camera.PERSPECTIVE_NEAR * (float) Math.tan(fieldOfView * Math.PI / 360.0);
        final float xmax = ymax * aspect;
        final float ymin = -ymax;
        final float xmin = -xmax;
        this.viewFrustum = new Frustum(fieldOfView, aspect, xmin, xmax, ymin, ymax, Camera.PERSPECTIVE_NEAR, Camera.PERSPECTIVE_FAR);
        
        // Generate the Model View Projection Matrix
        final Matrix44f projectionMatrix = new Matrix44f();
        projectionMatrix.set(viewFrustum.getProjectionMatrix());        
        final Matrix44f scaleMatrix = new Matrix44f();
        scaleMatrix.makeScalingMatrix(new Vector3f(1.0F, -1.0F, 1.0F)); //invert the Y-axis for translation from OpenGL cardinality to SVG cardinality
        projectionMatrix.multiply(projectionMatrix, scaleMatrix);
        final Matrix44f modelViewMatrix = Graphics3DUtilities.getModelViewMatrix(camera);
        modelViewProjectionMatrix.multiply(projectionMatrix, modelViewMatrix);   
        
        // Translate the frustum from local units to world units
        viewFrustum.transform(camera.getObjectFrame());
        
        // Translate and rotate the camera to get acurate x and y values for the view port 
        final Vector3f transformedCameraEye = new Vector3f();
        transformedCameraEye.transform(camera.lookAtEye, modelViewMatrix);
        viewPort = new int[] {Math.round(transformedCameraEye.getX()), Math.round(transformedCameraEye.getY()), paneWidth, paneHeight};
    }
    
    /**
     * Tears down the Builder design pattern reference utilities. 
     */
    private void postBuild(){
        access.endUpdate();
        if (threadPool != null){
            threadPool.close();
        }
    }
    
    /**
     * Generates SVG representations of Nodes from the graph and assigns them to the provided container.
     * This method creates runnable tasks equal to 4 times the number of processors available. 
     * This method will complete when all tasks have completed, appropriate nodes have been created and added to the correct container. 
     * The template file Node.svg is used to build the node.
     * @param nodesContainer The SVGObject holding all generated SVG node data 
     * @param defenitionsContainer The SVGObject holding definition references all generated SVG data 
     */
    private void buildNodes(final SVGObject nodesContainer, final SVGObject definitionsContainer) throws InterruptedException {


        // Initate plugin report information
        interaction.setExecutionStage(0, -1, BUILDING_REPORT_MESSAGE, "Building Nodes", true);
        final MultiTaskInteraction mti = new MultiTaskInteraction(interaction);
        
        if (!drawFlags.drawNodes()) {
            interaction.setProgress(0, -1, "Created 0 nodes", true);
            
        }  else {
            final int taskCount = Runtime.getRuntime().availableProcessors() * 4;

            // Get a unique sub list of vertex indicies for each available thread 
            final List<List<Integer>> threadInputLists = createSubListsOfRange(0, access.getVertexCount(), taskCount);
            final List<List<SVGObject>> threadOuputLists = new ArrayList<>();

            // Create a task for each set of inputLists with an unique list for their generated output
            for (final List<Integer> threadInput : threadInputLists) {
                final GraphVisualisationReferences graph = new GraphVisualisationReferences(viewFrustum, modelViewProjectionMatrix, viewPort, camera, drawFlags, selectedElementsOnly, assetDirectoty);
                final List<SVGObject> output = new ArrayList<>();
                final GenerateSVGNodesTask task = new GenerateSVGNodesTask(graph, threadInput, output);
                mti.addTask(task);
                threadOuputLists.add(output);
                CompletableFuture.runAsync(task, threadPool);
            }    

            // Wait until all tasks are complete
            mti.waitForTasksToComplete();

            // Combine and sort the generated elemtns into a two list.        
            final List<SVGObject> nodes = new ArrayList<>();
            final List<SVGObject> filters = new ArrayList<>();
            
            threadOuputLists.forEach(outputList -> outputList.forEach(svgObject -> {
                    if (SVGTypeConstants.FILTER.getTypeString().equals(svgObject.toSVGData().getType())){
                        filters.add(svgObject);
                    } else {
                        nodes.add(svgObject);
                    }
                })
            ); 

            definitionsContainer.setChildren(filters);
            nodesContainer.setChildren(nodes);

            // Update the PluginInteraction of the Nodes generated.
            interaction.setProgress(0, -1, String.format("Created %s nodes", nodes.size()), true);
        }
    }

    /**
     * Builds SVG representations of Connections between Nodes.
     * Generates representations of transactions, links and edges depending on connectionMode.
     * Labels are supported for all connection types excluding looped connections.
     * Other graph attributes including maxTransactions are considered.
     * This method creates runnable tasks equal to 4 times the number of processors available. 
     */
    private List<SVGObject> buildConnections() throws InterruptedException {

        // Initate plugin report information
        interaction.setExecutionStage(0, -1 , BUILDING_REPORT_MESSAGE, "Building Connections", false);
        final MultiTaskInteraction mti = new MultiTaskInteraction(interaction);
        
        // Do not export any connections if the show connections parameter is disabled
        if (!drawFlags.drawConnections()) {
            interaction.setProgress(0, -1, "Created 0 connections", true);
            return new ArrayList<>();
        }      
                
        // Get a unique sub list of vertex indicies for each available thread 
        final List<List<Integer>> threadInputLists = createSubListsOfRange(0, access.getLinkCount(), Runtime.getRuntime().availableProcessors() * 4);
        final List<List<SVGObject>> threadOuputLists = new ArrayList<>(); 
        
        // Create a task for each set of inputLists with an unique list for their generated output
        for (final List<Integer> threadInput : threadInputLists) {
            final GraphVisualisationReferences giu = new GraphVisualisationReferences(viewFrustum, modelViewProjectionMatrix, viewPort, camera, drawFlags, selectedElementsOnly);
            final List<SVGObject> output = new ArrayList<>();
            final GenerateSVGConnectionsTask task = new GenerateSVGConnectionsTask(giu, threadInput, output);
            mti.addTask(task);
            threadOuputLists.add(output);
            CompletableFuture.runAsync(task, threadPool);
        }      
        
        // Wait until all tasks are complete
        mti.waitForTasksToComplete();
                
        // Combine the generated nodes into a single list.
        final List<SVGObject> connections = new ArrayList<>();
        threadOuputLists.forEach(outputList -> connections.addAll(outputList)); 
        
        interaction.setProgress(0, -1, String.format("Created %s connections", connections.size()), true);
        return connections;
    }    
    
    /**
     * Generates SVG representations of blazes.
     * 
     * This method creates runnable tasks equal to the number of processors available. 
     * This method will complete when all tasks have been run and relevant blazes have been created. 
     */
    private List<SVGObject> buildBlazes() throws InterruptedException {
        
        // Initate plugin report information
        interaction.setExecutionStage(0, -1, BUILDING_REPORT_MESSAGE, "Building Blazes", true);
        final MultiTaskInteraction mti = new MultiTaskInteraction(interaction);
        
        if (!drawFlags.drawBlazes()) {
            interaction.setProgress(0, -1, "Created 0 blazes", true);
            return new ArrayList<>();
        }  
              
        // Get a unique sub list of vertex indicies for each available thread 
        final List<List<Integer>> threadInputLists = createSubListsOfRange(0, access.getVertexCount(), Runtime.getRuntime().availableProcessors());
        final List<List<SVGObject>> threadOuputLists = new ArrayList<>();

        // Create a task for each set of inputLists with an array list for their generated output
        for (final List<Integer> threadInput : threadInputLists) {
            final GraphVisualisationReferences graph = new GraphVisualisationReferences(viewFrustum, modelViewProjectionMatrix, viewPort, camera, drawFlags, selectedElementsOnly);
            final List<SVGObject> output = new ArrayList<>();
            final GenerateSVGBlazesTask task = new GenerateSVGBlazesTask(graph, threadInput, output);
            mti.addTask(task);
            threadOuputLists.add(output);
            CompletableFuture.runAsync(task, threadPool);
        }    

        // Wait until all tasks are complete
        mti.waitForTasksToComplete();
        
        // Combine the generated nodes into a single list.
        final List<SVGObject> blazes = new ArrayList<>();
        threadOuputLists.forEach(outputList -> blazes.addAll(outputList));
        
        // Update the PluginInteraction of the Nodes generated 
        interaction.setProgress(0, -1, String.format("Created %s blazes", blazes.size()), true);
        return blazes;
    }
    

    /**
     * Builds the header area of the output SVG.
     * This method will add a title and a date stamp of the export.
     * @param svgGraph The SVGObject holding all generated SVG data 
     */
    private void buildHeader(final SVGObject svgGraph) {
        
        final ZonedDateTime date = ZonedDateTime.now();
        final String dateInfo = String.format("Exported: %s %s, %s",
                        StringUtilities.camelCase(date.getMonth().toString()), 
                        date.getDayOfMonth(), 
                        date.getYear()
                );

        SVGObjectConstants.TITLE.findIn(svgGraph).setContent(graphTitle);
        SVGObjectConstants.SUBTITLE.findIn(svgGraph).setContent(dateInfo);
    }

    /**
     * Sets the layout of the exported SVG file.
     * Specifically setts the background color and ensures that the graph is scaled within the output svg width and height.
     * @param svgGraph The SVGObject holding all generated SVG data 
     */
    private void buildLayout(final SVGObject svgGraph) {
        buildHeader(svgGraph);
        
        // Set the background color of the output file
        SVGObjectConstants.BACKGROUND.findIn(svgGraph).setFillColor(backgroundColor);

        // Get the dimensions of the users window
        final float viewPortWidth = viewPort[2];
        final float viewPortHeight = viewPort[3];           

        //Set the content dimensions
        SVGObjectConstants.CONTENT.findIn(svgGraph).setViewBox(0F, 0F, viewPortWidth, viewPortHeight);
        
        //Set the overall dimensions
        svgGraph.setViewBox(0F, 0F, viewPortWidth, viewPortHeight + (viewPortHeight * .05F));
    }
    
    /**
     * Creates a 2d array from List objects based on the provided parameters so that each list is approximately equal in size and no element is duplicated across any list. 
     * Used to separate element indexes into multiple lists.
     * 
     * @param lowIndexInclusive 
     * @param highIndexExclusive
     * @param subListQuantity
     * @return 
     */
    private List<List<Integer>> createSubListsOfRange(final int lowIndexInclusive, final int highIndexExclusive, final int subListQuantity) {
        
        //Create the sublists
        final List<List<Integer>> returnValue = new ArrayList<>();
        for (int i = 0; i < subListQuantity; i++){
            returnValue.add(new ArrayList<>());
        }
        
        //Add the content
        for (int i = lowIndexInclusive; i < highIndexExclusive; i++) {
            returnValue.get(i%subListQuantity).add(i);
        }

        return returnValue;
    }
}

