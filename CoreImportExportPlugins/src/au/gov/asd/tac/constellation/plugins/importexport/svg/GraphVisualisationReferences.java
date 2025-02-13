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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import java.io.File;

/**
 * Provides a number of methods required to interpret visual aspects of the graph for SVG exports. 
 * Creates a local copy of components required to interpret the graph for thread safe usage.
 * 
 * @author capricornunicorn123
 */
public class GraphVisualisationReferences {

    private final GraphVisualAccess access;
    private final Frustum viewFrustum;
    private final Matrix44f modelViewProjectionMatrix = new Matrix44f();
    private final int[] viewPort; 
    private final Camera camera;
    private final DrawFlags drawFlags;
    public final boolean selectedElementsOnly;
    public final File directory;
    
    /**
     * Creates a local copy of all parameters
     * 
     * @param frustum
     * @param modelViewProjectionMatrix
     * @param viewPort
     * @param camera
     * @param drawFlags
     * @param selectedElementsOnly 
     */
    public GraphVisualisationReferences(final Frustum frustum, final Matrix44f modelViewProjectionMatrix, 
            final int[] viewPort, final Camera camera, final DrawFlags drawFlags, final boolean selectedElementsOnly) {
        this(frustum, modelViewProjectionMatrix, viewPort, camera, drawFlags, selectedElementsOnly, null);
    }
    
    /**
     * Creates a local copy of all parameters
     * 
     * @param frustum
     * @param modelViewProjectionMatrix
     * @param viewPort
     * @param camera
     * @param drawFlags
     * @param selectedElementsOnly
     * @param directory 
     */
    public GraphVisualisationReferences(final Frustum frustum, final Matrix44f modelViewProjectionMatrix, 
            final int[] viewPort, final Camera camera, final DrawFlags drawFlags, final boolean selectedElementsOnly, final File directory) {
        
        this.viewFrustum = frustum.getCopy();
        this.modelViewProjectionMatrix.set(modelViewProjectionMatrix);
        this.viewPort = new int[viewPort.length]; 
        System.arraycopy(viewPort, 0, this.viewPort, 0, viewPort.length);
        this.camera = new Camera(camera);
        this.drawFlags = new DrawFlags(drawFlags.drawNodes(), drawFlags.drawConnections(), drawFlags.drawNodeLabels(), drawFlags.drawConnectionLabels(), drawFlags.drawBlazes());
        this.selectedElementsOnly = selectedElementsOnly;
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        this.access = new GraphVisualAccess(currentGraph);
        this.directory = directory;
    }
    
    /**
     * To be called before use of any utility functions.
     */
    public void initialise() {
        access.beginUpdate();
        access.updateInternally();
    }
    
    /**
     * To be called once this class is finished being used.
     */
    public void terminate() {
        access.endUpdate();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Vertex Access Functions">         
    /**
     * Gets the normalized position of the vertex.
     * Position is normalized with respect to a predefined viewWindow 
     * with horizontal right and vertical down being positive directions.
     * Position is with respect to the center of the vertex.
     * @param vertexIndex
     * @return 
     */
    public Vector4f getVertexPosition(final int vertexIndex) {         
        return getScreenPosition(getVertexWorldPosition(vertexIndex));
    }

    /**
     * Retrieves the 3D coordinates of a vertex.
     * x, y and z values as specified in vertex attributes.
     * @param vertexIndex
     * @return 
     */
    public Vector3f getVertexWorldPosition(final int vertexIndex) {
        return new Vector3f(access.getX(vertexIndex), access.getY(vertexIndex), access.getZ(vertexIndex));
    }

    /**
     * Translates a 3D world coordinate to a 2D world projection onto a predefined plane.
     * The returned position is normalized with respect to a predefined viewWindow 
     * with horizontal right and vertical down being positive directions.
     * @param worldPosition
     * @return 
     */
    public Vector4f getScreenPosition(final Vector3f worldPosition) {
        final Vector4f screenPosition = new Vector4f();
        Graphics3DUtilities.project(worldPosition, modelViewProjectionMatrix, viewPort, screenPosition);
        return screenPosition;
    }
    
    /**
     * Determines the radius of a vertex.
     * @param vertexIndex
     * @return 
     */
    public float getRadius(final int vertexIndex) {
        return access.getRadius(vertexIndex);
    }

    /**
     * Translates the node radius from graph units to SVG units.
     * @param vertexIndex
     * @return 
     */
    public float getVertexScreenRadius(final int vertexIndex) {
        return access.getRadius(vertexIndex) * 128;
    }
    
    /**
     * Determines the radius of the node in screen units.
     * The scale is determined by projecting a position at the edge of the node
     * to its correlating screen position. 
     * @param vertexIndex
     * @return 
     */
    public float getVertexScaledRadius(final int vertexIndex) {  
        //Get the radius value of the node
        final float radius = getVertexScreenRadius(vertexIndex);

        //Get the scale foactor of the node determined by its distance from the camera.
        final float depthScaleFactor = getDepthScaleFactor(this.getVertexWorldPosition(vertexIndex));

        return radius * depthScaleFactor;         
    }

    /**
     * Gets the color of a vertex.
     * @param vertexIndex
     * @return 
     */
    public ConstellationColor getVertexColor(final int vertexIndex) {
        return access.getVertexColor(vertexIndex);
    }

    /**
     * Gets the name of the background icon of a vertex.
     * @param vertexIndex
     * @return 
     */
    public String getBackgroundIcon(final int vertexIndex) {
        return access.getBackgroundIcon(vertexIndex);
    }

    /**
     * Gets the name of the foreground icon of a vertex.
     * @param vertexIndex
     * @return 
     */
    public String getForegroundIcon(final int vertexIndex) {
        return access.getForegroundIcon(vertexIndex);
    }

    /**
     * Gets the Id value of a vertex.
     * @param vertexIndex
     * @return 
     */
    public int getVertexId(final int vertexIndex) {
        return access.getVertexId(vertexIndex);
    }

    /**
     * Determines if a vertex is dimmed.
     * @param vertexIndex
     * @return 
     */
    public boolean isVertexDimmed(final int vertexIndex) {
        return access.isVertexDimmed(vertexIndex);
    }

    /**
     * Determines if a vertex is in view with respect to the view frustum.
     * @param vertexIndex
     * @return 
     */
    public boolean inView(final int vertexIndex) {
        return viewFrustum.inView(getVertexWorldPosition(vertexIndex), access.getRadius(vertexIndex));
    }
    
    /**
     * Determines if a vertex is selected.
     * @param vertexIndex
     * @return 
     */
    public boolean isVertexSelected(final int vertexIndex) {
        return access.isVertexSelected(vertexIndex);
    }

    /**
     * Gets a vertexes opacity.
     * @param vertexIndex
     * @return 
     */
    public float getVertexVisibility(final int vertexIndex) {
        return access.getVertexVisibility(vertexIndex);
    }
    
    /**
     * Gets the name of the North West Decorator icon of a vertex.
     * @param vertexIndex
     * @return 
     */
    public String getNWDecorator(final int vertexIndex) {
        return access.getNWDecorator(vertexIndex);
    }

    /**
     * Gets the name of the North East Decorator icon of a vertex.
     * @param vertexIndex
     * @return 
     */
    public String getNEDecorator(final int vertexIndex) {
        return access.getNEDecorator(vertexIndex);
    }
    
    /**
     * Gets the name of the South West Decorator icon of a vertex.
     * @param vertexIndex
     * @return 
     */
    public String getSWDecorator(final int vertexIndex) {
        return access.getSWDecorator(vertexIndex);
    }
    /**
     * Gets the name of the South East Decorator icon of a vertex.
     * @param vertexIndex
     * @return 
     */
    public String getSEDecorator(final int vertexIndex) {
        return access.getSEDecorator(vertexIndex);
    }
    
    /**
     * Determines if a vertex has a blaze.
     * @param vertexIndex
     * @return 
     */
    public boolean isBlazed(final int vertexIndex) {
        return access.isBlazed(vertexIndex);
    }
    
    /**
     * Determines the angle of a blaze 
     * @param vertexIndex
     * @return 
     */
    public int getBlazeAngle(final int vertexIndex) {
        return access.getBlazeAngle(vertexIndex);
    }
    
    /**
     * Determines the length of the blaze.
     * @return 
     */
    public float getBlazeSize() {
        return access.getBlazeSize();
    }
    
    /**
     * Determines the color of the blaze.
     * @param vertexIndex
     * @return 
     */
    public ConstellationColor getBlazeColor(final int vertexIndex) {
        return access.getBlazeColor(vertexIndex);
    }
    
    /**
     * Determines the opacity of the blaze.
     * @return 
     */
    public float getBlazeOpacity() {
        return access.getBlazeOpacity();
    }
    
    /**
     * Determines if Node labels should be included in this export.
     * @return 
     */
    public boolean exportNodeLabels() {
        return drawFlags.drawNodeLabels();
    }

    /**
     * Determines how many bottom labels vertexes have.
     * @return 
     */
    public int getBottomLabelCount() {
        return access.getBottomLabelCount();
    }

    /**
     * Gets the text of a particular vertexes bottom label.
     * Vertexes can have multiple bottom labels so the label index is used to select a a specific one.
     * @param vertexIndex
     * @param labelIndex
     * @return 
     */
    public String getVertexBottomLabelText(final int vertexIndex, final int labelIndex) {
        return access.getVertexBottomLabelText(vertexIndex, labelIndex);
    }

    /**
     * Gets the size of a particular bottom label of vertexes.
     * Bottom label sizes are applied globally.
     * @param labelIndex
     * @return 
     */
    public float getBottomLabelSize(final int labelIndex) {
        return access.getBottomLabelSize(labelIndex);
    }

    /**
     * Gets the size of a particular bottom label of vertexes.
     * Bottom label colors are applies globally.
     * @param labelIndex
     * @return 
     */
    public ConstellationColor getBottomLabelColor(final int labelIndex) {
        return access.getBottomLabelColor(labelIndex);
    }
    
    /**
     * Determines how many top labels vertexes have.
     * @return 
     */
    public int getTopLabelCount() {
        return access.getTopLabelCount();
    }
    
    /**
     * Gets the text of a particular vertexes top label.
     * Vertexes can have multiple top labels so the label index is used to select a a specific one.
     * @param vertexIndex
     * @param labelIndex
     * @return 
     */
    public String getVertexTopLabelText(final int vertexIndex, final int labelIndex) {
        return access.getVertexTopLabelText(vertexIndex, labelIndex);
    }
    
    /**
     * Gets the size of a particular top label of vertexes.
     * Top label sizes are applied globally.
     * @param labelIndex
     * @return 
     */
    public float getTopLabelSize(final int labelIndex) {
        return access.getTopLabelSize(labelIndex);
    }

    /**
     * Gets the size of a particular top label of vertexes.
     * Top label colors are applies globally.
     * @param labelIndex
     * @return 
     */
    public ConstellationColor getTopLabelColor(final int labelIndex) {
        return access.getTopLabelColor(labelIndex);
    }
    
    // </editor-fold>      
    
    // <editor-fold defaultstate="collapsed" desc="Connection Access Functions">  
    /**
     * Gets the vertex index of a link which it its high vertex.
     * @param linkIndex
     * @return 
     */
    public int getLinkHighVertex(final int linkIndex) {
        return access.getLinkHighVertex(linkIndex);
    }
    
    /**
     * Gets the vertex index of a link which it its low vertex.
     * @param linkIndex
     * @return 
     */
    public int getLinkLowVertex(final int linkIndex) {
        return access.getLinkLowVertex(linkIndex);
    }
    
    /**
     * Gets the number of connections that are represented within this link.
     * this number is dependent on the total transaction sin the link, their direction 
     * and the connection mode of the graph.
     * @param linkIndex
     * @return 
     */
    public int getLinkConnectionCount(final int linkIndex) {
        return access.getLinkConnectionCount(linkIndex);
    }
    
    /**
     * Gets the global index of a relative link index and connection Index
     * @param linkIndex
     * @param connectionIndex
     * @return 
     */
    public int getLinkConnection(final int linkIndex, final int connectionIndex) {
        return access.getLinkConnection(linkIndex, connectionIndex);
    }
    
    /**
     * Determines if a connection is selected based on a connections global index.
     * @param connection
     * @return 
     */
    public boolean isConnectionSelected(final int connection) {
        return access.isConnectionSelected(connection);
    }
    
    /**
     * determines a connections opacity based on a connections global index.
     * @param connection
     * @return 
     */
    public float getConnectionVisibility(final int connection) {
        return access.getConnectionVisibility(connection);
    }
    
    /**
     * Determines the line style of a connection based on a connections global index.
     * @param connection
     * @return 
     */
    public LineStyle getConnectionLineStyle(final int connection){
        return access.getConnectionLineStyle(connection);
    }
    
    /**
     * Determines the color of a connection.
     * Handles connection dimming and multiple Transaction color values for Edges and Links
     * @param connectionIndex
     * @return 
     */
    public ConstellationColor getConnectionColor(final int connectionIndex) {  
        return access.isConnectionDimmed(connectionIndex) ? VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR : access.getConnectionColor(connectionIndex);
    }
    
    /**
     * Gets the Id of a connection.
     * @param connection
     * @return 
     */
    public int getConnectionId(final int connection){
        return access.getConnectionId(connection);
    }
    
    /**
     * Gets the direction of a connection. 
     * @param connection
     * @return 
     */
    public ConnectionDirection getConnectionDirection(final int connection){
        return access.getConnectionDirection(connection);
    }
    
    /**
     * Determines if connection labels should be exported.
     * @return 
     */
    public boolean exportConnectionLabels(){
        return drawFlags.drawConnectionLabels();
    }

    public int getConnectionLabelCount(final int connectionIndex){
        return access.getConnectionLabelCount(connectionIndex);
    }
    
    public float getConnectionLabelSize(final int labelIndex){
        return access.getConnectionLabelSize(labelIndex);
    }
    
    public ConstellationColor getConnectionLabelColor(final int labelIndex){
        return access.getConnectionLabelColor(labelIndex);
    }
    
    public String getConnectionLabelText(final int connectionIndex, final int labelIndex){ 
        return access.getConnectionLabelText(connectionIndex, labelIndex);
    }
    
    // </editor-fold>  
    
    // <editor-fold defaultstate="collapsed" desc="Perspective Access Functions">  
    /**
     * Determines a unit vector that represents the up direction based on the current camera orientation.
     * @return 
     */
    public Vector3f getUpVector(){
        return camera.getObjectFrame().getUpVector();
    }
    
    /**
     * Determines a unit vector that represents the forward direction based on the current camera orientation.
     * @return 
     */
    public Vector3f getForwardVector(){
        return camera.getObjectFrame().getForwardVector();
    }
    
    /**
     * Determines a unit vector that represents the right direction based on the current camera orientation.
     * @return 
     */
    public Vector3f getRightVector(){
        return camera.getObjectFrame().getXAxis();
    }
        
    public Vector3f getEntryPoint(final Vector3f startPoint, final Vector3f endPoint){
        return viewFrustum.getEntryPoint(startPoint, endPoint);
    }
    
    /**
     * Calculates the coordinates of a position located a fixed distance and angle from an origin.
     * Calculations are made in 2D screen units.
     * @param origin
     * @param distance
     * @param direction
     * @return 
     */
    public Vector4f offsetPosition(final Vector4f origin, final float distance, final double direction) {
        final float x = (float) (origin.getX() - (distance * Math.cos(direction)));
        final float y = (float) (origin.getY() - (distance * Math.sin(direction)));
        return new Vector4f(x, y, origin.getZ(), origin.getW());
    }
    
        /**
     * Calculates the coordinates of a position located a fixed distance and angle from an origin.
     * calculations are made in 3D world units.
     * @param origin
     * @param distance
     * @param direction
     * @return 
     */
    public Vector3f offsetPosition(final Vector3f origin, final float distance, final Vector3f direction) {
        final Vector3f offsetVector = new Vector3f(direction);

        // Ensure the direction vector is normalised
        offsetVector.normalize();
        
        // Scale the direction by the distance.      
        offsetVector.scale(distance);
        
        return Vector3f.add(origin, offsetVector);
    }
    
    /**
     * Determine the amount to scale an element based on its distance from the camera. 
     * 
     * @param worldPosition
     * @return 
     */
    public float getDepthScaleFactor(final Vector3f worldPosition) {
        // Get the screen position of the worldPosition
        final Vector4f screenPosition = getScreenPosition(worldPosition);

        // Move the point in the world equivelent of the screens upwards direction by one unit.
        final Vector3f up = new Vector3f(camera.lookAtUp);
        up.add(worldPosition);            

        // Convert this translated position to screen coordinates.
        final Vector4f edgePosition = getScreenPosition(up);

        // Get the distance between the two points
        final float screenDistance = Math.abs(edgePosition.getY() - screenPosition.getY());

        // One unit in world dimensions in 128 units in svg dimensions so return this as a screen to world ratio.
        return screenDistance / 128;
    }
    
        /**
     * Determines if a point and a given radius are in view with respect to the view frustum.
     * 
     * @param center
     * @param radius
     * @return 
     */
    public boolean inView(final Vector3f center, final float radius) {
        return viewFrustum.inView(center, radius);
    }
    
    // </editor-fold>  
}
