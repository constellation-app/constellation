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
 * Provides a number of methods required to interpret visual aspects of the graph. 
 * Creates a local copy of components required to interpret the graph for thread save usage.
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
    
    public GraphVisualisationReferences(final Frustum frustum, final Matrix44f modelViewProjectionMatrix, 
            final int[] viewPort, final Camera camera, final DrawFlags drawFlags, final boolean selectedElementsOnly) {
        
        this.viewFrustum = frustum.getCopy();
        this.modelViewProjectionMatrix.set(modelViewProjectionMatrix);
        this.viewPort = new int[viewPort.length]; 
        System.arraycopy(viewPort, 0, this.viewPort, 0, viewPort.length);
        this.camera = new Camera(camera);
        this.drawFlags = new DrawFlags(drawFlags.drawNodes(), drawFlags.drawConnections(), drawFlags.drawNodeLabels(), drawFlags.drawConnectionLabels(), drawFlags.drawBlazes());
        this.selectedElementsOnly = selectedElementsOnly;
        
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        this.access = new GraphVisualAccess(currentGraph);
        directory = null;
    }
    
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
    
    public void initialise(){
        access.beginUpdate();
        access.updateInternally();
    }
    
    public void terminate(){
        access.endUpdate();
    }

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

    public ConstellationColor getVertexColor(int vertexIndex) {
        return access.getVertexColor(vertexIndex);
    }

    public String getBackgroundIcon(int vertexIndex) {
        return access.getBackgroundIcon(vertexIndex);
    }

    public String getForegroundIcon(int vertexIndex) {
        return access.getForegroundIcon(vertexIndex);
    }

    public int getVertexId(final int vertexIndex) {
        return access.getVertexId(vertexIndex);
    }

    public boolean isVertexDimmed(int vertexIndex) {
        return access.isVertexDimmed(vertexIndex);
    }

    public boolean inView(final int vertexIndex) {
        return viewFrustum.inView(getVertexWorldPosition(vertexIndex), access.getRadius(vertexIndex));
    }
    
    public boolean inView(final Vector3f center, final float radius) {
        return viewFrustum.inView(center, radius);
    }

    public boolean isVertexSelected(int vertexIndex) {
        return access.isVertexSelected(vertexIndex);
    }

    public float getVertexVisibility(int vertexIndex) {
        return access.getVertexVisibility(vertexIndex);
    }
    
    public String getNWDecorator(int vertexIndex) {
        return access.getNWDecorator(vertexIndex);
    }

    public String getNEDecorator(int vertexIndex) {
        return access.getNEDecorator(vertexIndex);
    }

    public String getSWDecorator(int vertexIndex) {
        return access.getSWDecorator(vertexIndex);
    }

    public String getSEDecorator(int vertexIndex) {
        return access.getSEDecorator(vertexIndex);
    }
    
    public boolean isBlazed(int vertexIndex) {
        return access.isBlazed(vertexIndex);
    }

    public int getBottomLabelCount() {
        return access.getBottomLabelCount();
    }

    public String getVertexBottomLabelText(int vertexIndex, int labelIndex) {
        return access.getVertexBottomLabelText(vertexIndex, labelIndex);
    }

    public float getBottomLabelSize(int labelIndex) {
        return access.getBottomLabelSize(labelIndex);
    }

    public ConstellationColor getBottomLabelColor(int labelIndex) {
        return access.getBottomLabelColor(labelIndex);
    }

    public int getTopLabelCount() {
        return access.getTopLabelCount();
    }

    public String getVertexTopLabelText(int vertexIndex, int labelIndex) {
        return access.getVertexTopLabelText(vertexIndex, labelIndex);
    }

    public float getTopLabelSize(int labelIndex) {
        return access.getTopLabelSize(labelIndex);
    }

    public ConstellationColor getTopLabelColor(int labelIndex) {
        return access.getTopLabelColor(labelIndex);
    }
    
    public int getLinkHighVertex(int linkIndex){
        return access.getLinkHighVertex(linkIndex);
    }
    
    public int getLinkLowVertex(int linkIndex){
        return access.getLinkLowVertex(linkIndex);
    }
    public int getLinkConnectionCount(final int linkIndex){
        return access.getLinkConnectionCount(linkIndex);
    }
    
    public int getLinkConnection(final int linkIndex, final int connectionIndex){
        return access.getLinkConnection(linkIndex, connectionIndex);
    }
    
    public boolean isConnectionSelected(final int connection){
        return access.isConnectionSelected(connection);
    }
    
    public float getConnectionVisibility(final int connection){
        return access.getConnectionVisibility(connection);
    }
    
    public LineStyle getConnectionLineStyle(final int connection){
        return access.getConnectionLineStyle(connection);
    }

    public Vector3f getUpVector(){
        return camera.getObjectFrame().getUpVector();
    }
    
    public Vector3f getForwardVector(){
        return camera.getObjectFrame().getForwardVector();
    }
    
    public Vector3f getRightVector(){
        return camera.getObjectFrame().getXAxis();
    }
    
    public float getRadius(final int highIndex){
        return access.getRadius(highIndex);
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
     * Determines the color of a connection.
     * Handles connection dimming and multiple Transaction color values for Edges and Links
     * @param connectionIndex
     * @return 
     */
    public ConstellationColor getConnectionColor(final int connectionIndex) {  
        return access.isConnectionDimmed(connectionIndex) ? VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR : access.getConnectionColor(connectionIndex);
    }
    public int getConnectionId(final int connection){
        return access.getConnectionId(connection);
    }
    
    public ConnectionDirection getConnectionDirection(final int connection){
        return access.getConnectionDirection(connection);
    }
    
    public Vector3f getEntryPoint(final Vector3f startPoint, final Vector3f endPoint){
        return viewFrustum.getEntryPoint(startPoint, endPoint);
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
    
    public int getBlazeAngle(final int vertexIndex){
        return access.getBlazeAngle(vertexIndex);
    }
    
    public float getBlazeSize(){
        return access.getBlazeSize();
    }
    
    public ConstellationColor getBlazeColor(int vertexIndex){
        return access.getBlazeColor(vertexIndex);
    }
    
    public float getBlazeOpacity(){
        return access.getBlazeOpacity();
    }
    
    public boolean exportNodeLabels(){
        return drawFlags.drawNodeLabels();
    }
    
    public boolean exportConnectionLabels(){
        return drawFlags.drawConnectionLabels();
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
}
