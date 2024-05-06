/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.svg.tasks;

import au.gov.asd.tac.constellation.plugins.importexport.svg.GraphVisualisationReferences;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGObjectConstants;
import au.gov.asd.tac.constellation.plugins.importexport.svg.resources.SVGTemplateConstants;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import java.util.List;
import au.gov.asd.tac.constellation.plugins.MultiTaskInteraction.SharedInteractionRunnable;


/**
 * A runnable task designed to build SVG assets representing graph nodes.
 * This task is designed to run concurrently and can represent anything from the process 
 * of building one node or the process o building all nodes on one thread.
 * 
 * @author capricornunicorn123
 */
public class GenerateSVGConnectionsTask implements Runnable, SharedInteractionRunnable {
    
    private final GraphVisualisationReferences graph;
    private final List<Integer> connectionIndicies;
    private final List<SVGObject> output;
    private final int totalSteps;
    private int currentStep;
    private boolean complete = false;
    
    public GenerateSVGConnectionsTask(final GraphVisualisationReferences graph, final List<Integer> linkIndicies, final List<SVGObject> output){
        this.graph = graph;
        this.connectionIndicies = List.copyOf(linkIndicies);
        this.output = output;
        this.totalSteps = linkIndicies.size();
    }

    @Override
    public void run() {
        try {
            graph.initialise();
            connectionIndicies.forEach(linkIndex -> {
                
                // Create a SVGObject to represent the current link
                final SVGObject svgLink = SVGTemplateConstants.LINK.getSVGObject();
                svgLink.setID(String.format("link-%s", linkIndex));

                // Get source and destination vertex references for the current link
                final int highIndex = graph.getLinkHighVertex(linkIndex);
                final int lowIndex = graph.getLinkLowVertex(linkIndex);

                // Determine the world references for the center of the vertices
                final Vector3f highCenterPosition = graph.getVertexWorldPosition(highIndex);
                final Vector3f lowCenterPosition = graph.getVertexWorldPosition(lowIndex);     

                // Build all of the connections in the current link 
                final SVGObject svgConnections = SVGObjectConstants.CONNECTIONS.findIn(svgLink);
                final SVGObject svgLabels = SVGObjectConstants.LABELS.findIn(svgLink);
                for (int connectionIndex = 0; connectionIndex < graph.getLinkConnectionCount(linkIndex); connectionIndex++) {

                    // Get the reference to the current connection
                    final int connection = graph.getLinkConnection(linkIndex, connectionIndex);

                    // Do not export the conection if only selected element are being exported and the connection is not selected
                    // Do not export the connection if it is invisable 
                    if((graph.selectedElementsOnly && !graph.isConnectionSelected(connection)) || graph.getConnectionVisibility(connection) == 0) {
                        continue;
                    }

                    // Build Looped Connection
                    if (highIndex == lowIndex) {

                        // Get the unit vectors for translation fom the node center to the nodes north west corner
                        final Vector3f upTranslation = graph.getUpVector();
                        final Vector3f rightTranslation = graph.getRightVector();

                        // Scale the translation to the radius length          
                        upTranslation.scale(graph.getRadius(highIndex));
                        rightTranslation.scale(graph.getRadius(highIndex));

                        // Apply the tranlsation
                        final Vector3f loopWorldCenterPosition = Vector3f.add(highCenterPosition, upTranslation, rightTranslation);

                        // Do not export the loop if it is not in view
                        if (!graph.inView(loopWorldCenterPosition, 0.25F)){
                            continue;
                        }

                        final Vector4f loopScreenCenterPosition = graph.getScreenPosition(loopWorldCenterPosition);
                        final float loopSize = graph.getDepthScaleFactor(loopWorldCenterPosition) * 128;

                        // Create the loopedConnection
                        final SVGObject svgLoop = SVGTemplateConstants.CONNECTION_LOOP.getSVGObject();
                        svgLoop.setID(graph.getConnectionId(connection));
                        svgLoop.setDimension(loopSize, loopSize);
                        svgLoop.setPosition(loopScreenCenterPosition.getX() - (loopSize/2) , loopScreenCenterPosition.getY() - (loopSize/2));
                        svgLoop.setParent(svgConnections);
                        svgLink.setSortOrderValue(loopScreenCenterPosition.getW());

                        // Generate the SVG Loop Image
                        final ConnectionDirection direction = graph.getConnectionDirection(connection);
                        final SVGData svgloopImage = switch (direction) {
                            case LOW_TO_HIGH, HIGH_TO_LOW -> DefaultIconProvider.LOOP_DIRECTED.buildSVG(graph.getConnectionColor(connection).getJavaColor());
                            default -> DefaultIconProvider.LOOP_UNDIRECTED.buildSVG(graph.getConnectionColor(connection).getJavaColor());
                        };
                        svgloopImage.setParent(svgLoop);

                        //Loop labels have not been implementd
                        SVGObjectConstants.LABELS.removeFrom(svgLink);

                    // Build Linear Connection
                    } else {
                        // Get references to SVG Objects being built
                        final SVGObject svgConnection = SVGTemplateConstants.CONNECTION_LINEAR.getSVGObject();
                        final SVGObject svgArrowShaft = SVGObjectConstants.ARROW_SHAFT.findIn(svgConnection);
                        final SVGObject svgArrowHeadHigh = SVGTemplateConstants.ARROW_HEAD.getSVGObject();
                        final SVGObject svgArrowHeadLow = SVGTemplateConstants.ARROW_HEAD.getSVGObject();         

                        // Determine the direction vectors of the link from the perspective of each vertex
                        final Vector3f lowDirectionVector = Vector3f.subtract(highCenterPosition, lowCenterPosition);
                        final Vector3f highDirectionVector = Vector3f.subtract(lowCenterPosition, highCenterPosition);

                        // Get the coordinates of the points where the connection intersects the node circumferences
                        final Vector3f highCircumferencePosition = graph.offsetPosition(highCenterPosition, graph.getRadius(highIndex), highDirectionVector);
                        final Vector3f lowCircumferencePosition = graph.offsetPosition(lowCenterPosition, graph.getRadius(lowIndex), lowDirectionVector);

                        // Get the direction vector of the line parallell to the viewing plane and perpendicular to the connection
                        final Vector3f vertexTagentDirection = new Vector3f();
                        vertexTagentDirection.crossProduct(graph.getForwardVector(), highDirectionVector);

                        // Determine the perpendicular offset distance of the current connection from the center line joing the source and destination node
                        final float perpendicularOffsetDistance = (connectionIndex / 2F + ((connectionIndex % 2 == 0) ? 0 : 1)) * 0.15F;

                        // Determine if this conection should be positioned above or below the center line joing the source and destination node
                        final float perpendicularOffsetDirection = ((Double) Math.pow(-1, connectionIndex)).floatValue();

                        // Determine the unique world corrdinates for end positions for the individual connection.
                        final Vector3f highEndPoint = graph.offsetPosition(highCircumferencePosition, perpendicularOffsetDistance * perpendicularOffsetDirection, vertexTagentDirection);
                        final Vector3f lowEndPoint = graph.offsetPosition(lowCircumferencePosition, perpendicularOffsetDistance * perpendicularOffsetDirection, vertexTagentDirection);

                        // Get the world corrdinates of the points where the conection passes through the frustum
                        final Vector3f lowFrustumEntryPoint = graph.getEntryPoint(lowEndPoint, highEndPoint);
                        final Vector3f highFrustumEntryPoint = graph.getEntryPoint(highEndPoint, lowEndPoint);

                        // The connection does not pass through the view frustum
                        if (lowFrustumEntryPoint == null || highFrustumEntryPoint == null){
                            continue;
                        }   

                        // Get the world coordinates of the point where the shaft will join the arrow head.
                        final Vector3f highArowHeadConnectionPoint = graph.offsetPosition(highEndPoint, 0.65F, highDirectionVector);
                        final Vector3f lowArowHeadConnectionPoint = graph.offsetPosition(lowEndPoint, 0.65F, lowDirectionVector);

                        // Assign the positional values of shaft and arrow head/s based on the direction of the Transaction/Edge/Link
                        final Vector3f highArrowShaftPosition = new Vector3f(highFrustumEntryPoint);
                        final Vector3f lowArrowShaftPosition = new Vector3f(lowFrustumEntryPoint);
                        final VisualAccess.ConnectionDirection direction = graph.getConnectionDirection(connection); 
                        switch (direction) {

                            //Bidirectional connections are Links with two link arrow heads
                            case BIDIRECTED:

                                // Generate new arrow base for diamond arrow heads
                                final Vector3f highArrowHeadBasePoint = graph.offsetPosition(highEndPoint, 1.0F, highDirectionVector);
                                final Vector3f lowArowHeadBasePoint = graph.offsetPosition(lowEndPoint, 1.0F, lowDirectionVector);

                                // Only build the high arrow head if the high arrow head has not been cropped
                                if (highFrustumEntryPoint.areSame(highEndPoint)) {
                                    buildArrowHead(svgArrowHeadHigh, highEndPoint, highArrowHeadBasePoint, highArowHeadConnectionPoint, vertexTagentDirection);
                                    svgArrowHeadHigh.setParent(svgConnection);
                                    highArrowShaftPosition.set(highArowHeadConnectionPoint);
                                }

                                // Only build the low arrow head if the low arrow head has not been cropped
                                if (lowFrustumEntryPoint.areSame(lowEndPoint)) {
                                    buildArrowHead(svgArrowHeadLow, lowEndPoint, lowArowHeadBasePoint, lowArowHeadConnectionPoint, vertexTagentDirection);
                                    svgArrowHeadLow.setParent(svgConnection);
                                    lowArrowShaftPosition.set(lowArowHeadConnectionPoint);
                                }
                                break;

                            // Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head    
                            case LOW_TO_HIGH:

                                // Only build the high arrow head if the high arrow head has not been cropped
                                if (highFrustumEntryPoint.areSame(highEndPoint)) {
                                    buildArrowHead(svgArrowHeadHigh, highEndPoint, highArowHeadConnectionPoint, highArowHeadConnectionPoint, vertexTagentDirection);
                                    svgArrowHeadHigh.setParent(svgConnection);
                                    highArrowShaftPosition.set(highArowHeadConnectionPoint);
                                }

                                // The high arrow head is not in view 
                                break;

                            //Unidirectional connectsions are Transactions, Edges and links with one transaction arrow head
                            case HIGH_TO_LOW:

                                // Only build the low arrow head if the high arrow head has not been cropped
                                if (lowFrustumEntryPoint.areSame(lowEndPoint)) {
                                    buildArrowHead(svgArrowHeadLow, lowEndPoint, lowArowHeadConnectionPoint, lowArowHeadConnectionPoint, vertexTagentDirection);
                                    svgArrowHeadLow.setParent(svgConnection);
                                    lowArrowShaftPosition.set(lowArowHeadConnectionPoint);
                                }

                                // The low arrow head is not in view 
                                break;

                            // Undirected connections are Transactions, Edges and Links with no arrow heads.
                            default:
                                break;
                        }

                        buildLinearArrowShaft(svgArrowShaft, highArrowShaftPosition, lowArrowShaftPosition, vertexTagentDirection);

                        // Set the attributes of the connection and add it to the connections conatainer  
                        final ConstellationColor color = graph.getConnectionColor(connection);
                        svgConnection.setID(String.format("Connection-%s", connection));
                        svgConnection.setFillColor(color);
                        svgConnection.setStrokeColor(color);
                        svgConnection.setStrokeStyle(graph.getConnectionLineStyle(connection));
                        svgConnection.setParent(svgConnections);

                        // Create the connection labels if required
                        if (graph.exportConnectionLabels()) {
                            addConnectionLabels(svgLabels, highEndPoint, lowEndPoint, connectionIndex, graph.getLinkConnectionCount(linkIndex));
                        } else {
                            SVGObjectConstants.LABELS.removeFrom(svgLink);
                        }
                        // Set the sort order as an average of the distance of the source and destination vertex distance from the camera.
                        svgLink.setSortOrderValue((graph.getScreenPosition(highArrowShaftPosition).getW() + graph.getScreenPosition(lowArrowShaftPosition).getW())/2F);
                    }
                } 
                // Add the link to the output if any connections were made.
                if (!SVGObjectConstants.CONNECTIONS.findIn(svgLink).toSVGData().getAllChildren().isEmpty()){
                    output.add(svgLink);
                }
                currentStep++;                
            });
        } finally {
            graph.terminate();
            complete = true;
        }
    }

    /**
     * Adds labels for connections in a link. 
     * Labels are not added for looped connections.
     * @param svgLabels
     * @param highPosition
     * @param lowPosition
     * @param connectionIndex
     * @param connectionCount 
     * @param highIndex
     * @param lowIndex
     */
    private void addConnectionLabels(final SVGObject svgLabels, final Vector3f highPosition, final Vector3f lowPosition, final int connectionIndex, final int connectionCount) {
        
        // Determine how many segments along the connection length are needed.
        final int totalSegments;
        if (connectionCount > 7) {
            totalSegments = 8;
        } else{
            totalSegments = connectionCount + 1;
        }

        // Determine which segment this connection label will occupy
        final int labelSegment = (connectionIndex % 7) + 1;
        final float segmentRatio = (float) labelSegment / totalSegments;
        
        // Calculate the position of the label
        final float offsetDistance = Mathf.distance(highPosition, lowPosition) * segmentRatio;
        final Vector3f angle = Vector3f.subtract(highPosition, lowPosition);
        final Vector3f worldPosition = graph.offsetPosition(lowPosition, offsetDistance, angle);
          
        // Only procede if the label is in view
        if (graph.inView(worldPosition, 0)){

            // Determine the scale factor of the label
            final float scaleFactor = graph.getDepthScaleFactor(worldPosition);
            final Vector4f screenPosition = graph.getScreenPosition(worldPosition); 
            
            // Track the distance bewteen the bottom of the svgLabels element and the top of the most recently created svgLabel
            float offset = 0;
            for (int labelIndex = 0; labelIndex < graph.getConnectionLabelCount(connectionIndex); labelIndex++) {
                final String labelString = graph.getConnectionLabelText(connectionIndex, labelIndex);

                // Only add the label if the label value exists.
                if (labelString != null) {
                    final SVGObject svgLabel = SVGTemplateConstants.LABEL.getSVGObject();
                    final float size = graph.getConnectionLabelSize(labelIndex) * 64 * scaleFactor;
                    svgLabel.setPosition(screenPosition.getX(), screenPosition.getY() + offset);
                    svgLabel.setFontSize(size);
                    svgLabel.setFillColor(graph.getConnectionLabelColor(labelIndex));
                    svgLabel.setBaseline("middle");
                    svgLabel.setID(String.format("label-%s-%s", connectionIndex, labelIndex));
                    svgLabel.setContent(labelString);
                    svgLabel.setParent(svgLabels);
                    offset = offset + size;
                }
            }
        }
    }

    /**
     * Manipulates an arrow head container to adjust it's position and rotation.
     * @param svgArrowHead
     * @param arrowPointPosition
     * @param arrowBasePosition 
     * @param shaftEndPosition
     * @param perpendicularDirection
     */
    private void buildArrowHead(final SVGObject svgArrowHead, final Vector3f arrowPointPosition, final Vector3f arrowBasePosition, final Vector3f shaftEndPosition, final Vector3f perpendicularDirection) {
        
        // Calculate the four points of the arrow head.
        final Vector4f point = graph.getScreenPosition(arrowPointPosition);
        final Vector4f base = graph.getScreenPosition(arrowBasePosition);
        final Vector4f upperEdge = graph.getScreenPosition(graph.offsetPosition(shaftEndPosition, -0.15F, perpendicularDirection));
        final Vector4f lowerEdge = graph.getScreenPosition(graph.offsetPosition(shaftEndPosition, 0.15F, perpendicularDirection));
               
        svgArrowHead.setID(String.format("arrow-head-%s-%s", point.getX(), point.getY()));

        SVGObjectConstants.ARROW_HEAD.findIn(svgArrowHead).setPoints(point, upperEdge, base, lowerEdge);
    }

    /**
     * Manipulates an arrow shaft container to adjust it's position.
     * @param svgArrowShaft
     * @param sourcePosition
     * @param destinationPosition 
     * @param sourceIndex
     * @param destinationIndex 
     */
    private void buildLinearArrowShaft(final SVGObject svgArrowShaft, final Vector3f sourcePosition, final Vector3f destinationPosition, final Vector3f perpendicularDirection) {

        // Calculate the four points of the arrow shaft.
        final Vector4f p1 = graph.getScreenPosition(graph.offsetPosition(sourcePosition, 0.03F, perpendicularDirection));
        final Vector4f p2 = graph.getScreenPosition(graph.offsetPosition(sourcePosition, -0.03F, perpendicularDirection));
        final Vector4f p3 = graph.getScreenPosition(graph.offsetPosition(destinationPosition, -0.03F, perpendicularDirection));
        final Vector4f p4 = graph.getScreenPosition(graph.offsetPosition(destinationPosition, 0.03F, perpendicularDirection));
        
        svgArrowShaft.setPoints(p1, p2, p3, p4);
    }

    @Override
    public int getTotalSteps() {
        return totalSteps;
    }

    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
    
}
