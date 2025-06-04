/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.visual;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.List;

/**
 * An interface describing a means of accessing standard visual information
 * present in a data structure (typically a graph) than can be visualised by
 * CONSTELLATION.
 *
 * @author twilight_sparkle
 * @author antares
 */
public interface VisualAccess {

    /**
     * Updates internal state held by this visual access from the underlying
     * data structure.
     * <p>
     * This method is called by the visualisation framework every update cycle
     * (when required). The call is always proceeded by
     * {@link #beginUpdate() beginUpdate()} and followed by
     * {@link #endUpdate() endUpdate()}. This feature allows a VisualAccess to
     * keep cached state information around (such as attribute ids) that make
     * the retrieval of other visual information faster.
     */
    public void updateInternally();

    /**
     * Request this VisualAccess to update itself and return a list of visual
     * changes which have occurred within the underlying data structure since
     * the last call to this method.
     * <p>
     * Note that the returned list of changes is defined by the last call to
     * this method; subsequent calls to
     * {@link #updateInternally updateInternally()} should not alter it.
     *
     * @return
     */
    public List<VisualChange> getIndigenousChanges();

    /**
     * Signals that a visual update which will interrogate this access object is
     * beginning.
     * <p>
     * Implementations of this method should lock any underlying data
     * structures.
     */
    public void beginUpdate();

    /**
     * Signals that a visual update which was interrogating this access object
     * has ended.
     * <p>
     * Implementations of this method should unlock any underlying data
     * structures.
     */
    public void endUpdate();

    /**
     * Get the color to be used for the background of the graph display.
     *
     * @return
     */
    public ConstellationColor getBackgroundColor();

    /**
     * Get the highlight color for the graph (can be used for indicating
     * selection etc).
     *
     * @return
     */
    public ConstellationColor getHighlightColor();

    /**
     * Get the object flagging which elements of the graph are to be displayed.
     *
     * @return
     */
    public DrawFlags getDrawFlags();

    /**
     * Get the camera representing the current view of the graph.
     *
     * @return
     */
    public Camera getCamera();

    /**
     * Get the number of vertices to be displayed.
     *
     * @return
     */
    public int getVertexCount();

    /**
     * Get the number of connections to be displayed (these may correspond to
     * links, edges or transactions on the graph).
     *
     * @return
     */
    public int getConnectionCount();

    /**
     * Get the global size with which to display blazes.
     *
     * @return
     */
    public float getBlazeSize();

    /**
     * Get the global opacity with which to display blazes.
     *
     * @return
     */
    public float getBlazeOpacity();

    /**
     * Get the global opacity with which to display connections.
     *
     * @return
     */
    public float getConnectionOpacity();

    /**
     * Get the number of labels to display on top of each node.
     *
     * @return
     */
    public int getTopLabelCount();

    /**
     * Get the number of labels to display below each node.
     *
     * @return
     */
    public int getBottomLabelCount();

    /**
     * Get the current number of attribute labels that could be displayed on
     * each connection.
     *
     * @return
     */
    public int getConnectionAttributeLabelCount();

    /**
     * Get the number of labels to display on the given connection.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public int getConnectionLabelCount(final int connection);

    /**
     * Determine whether the labels for the given connection should be displayed
     * as 'summary' labels, as opposed to labels which represent actual
     * attribute values.
     * <p>
     * A common use case for summary labels is showing the number of
     * transactions beneath an edge. Given visual implementations may choose to
     * represent these types of labels differently from those showing attribute
     * values.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public boolean isLabelSummary(final int connection);

    /**
     * Get the color of the given label on top of nodes.
     *
     * @param labelNum The position of the label in the list of top labels to
     * display.
     * @return
     */
    public ConstellationColor getTopLabelColor(final int labelNum);

    /**
     * Get the color of the given label below nodes.
     *
     * @param labelNum The position of the label in the list of bottom labels to
     * display.
     * @return
     */
    public ConstellationColor getBottomLabelColor(final int labelNum);

    /**
     * Get the color of the given label on connections.
     *
     * @param labelNum The position of the label in the list of connection
     * labels to display.
     * @return
     */
    public ConstellationColor getConnectionLabelColor(final int labelNum);

    /**
     * Get the size of the given label on top of nodes.
     *
     * @param labelNum The position of the label in the list of top labels to
     * display.
     * @return
     */
    public float getTopLabelSize(final int labelNum);

    /**
     * Get the size of the given label below nodes.
     *
     * @param labelNum The position of the label in the list of bottom labels to
     * display.
     * @return
     */
    public float getBottomLabelSize(final int labelNum);

    /**
     * Get the size of the given label on connections.
     *
     * @param labelNum The position of the label in the list of connection
     * labels to display.
     * @return
     */
    public float getConnectionLabelSize(final int labelNum);

    /**
     * Get the graph ID for a transaction which is representative of the given
     * connection.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public int getConnectionId(final int connection);

    /**
     * Get the graph ID which corresponds to the given vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public int getVertexId(final int vertex);

    /**
     * Get the x component of the coordinates at which to display the given
     * vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getX(final int vertex);

    /**
     * Get the y component of the coordinates at which to display the given
     * vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getY(final int vertex);

    /**
     * Get the z component of the coordinates at which to display the given
     * vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getZ(final int vertex);

    /**
     * Get the x component of the alternative coordinates at which to display
     * the given vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getX2(final int vertex);

    /**
     * Get the y component of the alternative coordinates at which to display
     * the given vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getY2(final int vertex);

    /**
     * Get the z component of the alternative coordinates at which to display
     * the given vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getZ2(final int vertex);

    /**
     * Get the color of the given vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public ConstellationColor getVertexColor(final int vertex);

    /**
     * Get the name of the icon to use as the background icon for the given
     * vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public String getBackgroundIcon(final int vertex);

    /**
     * Get the name of the icon to use as the foreground icon for the given
     * vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public String getForegroundIcon(final int vertex);

    /**
     * Get whether or not the given vertex should be displayed as selected.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public boolean isVertexSelected(final int vertex);

    /**
     * Get the visibility value for the given vertex, informing whether or not
     * it should be displayed at all.
     *
     * Usually a vertex should be visible when its visibility is between the
     * camera's low and high visibility values.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getVertexVisibility(final int vertex);

    /**
     * Get whether or not the given vertex should be displayed as dimmed.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public boolean isVertexDimmed(final int vertex);

    /**
     * Get the radius of the given vertex
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public float getRadius(final int vertex);

    /**
     * Get whether or not the given vertex should have a blaze displayed on it.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public boolean isBlazed(final int vertex);

    /**
     * Get the angle at which the blaze should be displayed for the given
     * vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display. This should be a vertex v for which
     * {@link #getBlazed getBlaze(v)} will return true.
     * @return
     */
    public int getBlazeAngle(final int vertex);

    /**
     * Get the color to use for the blaze for the given vertex.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display. This should be a vertex v for which
     * {@link #getBlazed getBlaze(v)} will return true.
     * @return
     */
    public ConstellationColor getBlazeColor(final int vertex);

    /**
     * Get the name of the icon to use for the decorator on the given vertex's
     * NW corner, or null if no decorator should be displayed.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public String getNWDecorator(final int vertex);

    /**
     * Get the name of the icon to use for the decorator on the given vertex's
     * NE corner, or null if no decorator should be displayed.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public String getNEDecorator(final int vertex);

    /**
     * Get the name of the icon to use for the decorator on the given vertex's
     * SE corner, or null if no decorator should be displayed.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public String getSEDecorator(final int vertex);

    /**
     * Get the name of the icon to use for the decorator on the given vertex's
     * SW corner, or null if no decorator should be displayed.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @return
     */
    public String getSWDecorator(final int vertex);

    /**
     * Get the color of the given connection.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public ConstellationColor getConnectionColor(final int connection);

    /**
     * Get whether or not to display the given connection as selected.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public boolean isConnectionSelected(final int connection);

    /**
     * Get the visibility value for the given connection, informing whether or
     * not it should be displayed at all.
     *
     * Usually a connection should be visible when its visibility is between the
     * camera's low and high visibility values.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public float getConnectionVisibility(final int connection);

    /**
     * Get whether or not to display the given connection as dimmed.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public boolean isConnectionDimmed(final int connection);

    /**
     * Get the linestyle to use to display the given connection.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public LineStyle getConnectionLineStyle(final int connection);

    /**
     * Get the width of the given connection.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return
     */
    public float getConnectionWidth(final int connection);

    /**
     * Get the motion of the connections
     *
     * @return
     */
    public float getConnectionMotion();

    /**
     * The options for the 'directedness' of connections.
     */
    public enum ConnectionDirection {

        LOW_TO_HIGH,
        HIGH_TO_LOW,
        UNDIRECTED,
        BIDIRECTED;
    }

    /**
     * Get the direction associated with the given connection.
     *
     * This gives an indication of the visual information that should be shown
     * about the given connection, ie. whether it is undirected, bidirected, or
     * if it has a single direction, which of the low and high vertices is its
     * source.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return A ConnectionDirection constant indicating the visual information
     * pertaining to the given connection.
     */
    public ConnectionDirection getConnectionDirection(final int connection);

    /**
     * Get whether the connection is directed or not
     *
     * @param connection The position of the connection in the list of
     * connections to display
     * @return A boolean value representing whether the connection is directed
     */
    public boolean isConnectionDirected(final int connection);

    /**
     * Get the endpoint vertex for the given connection with the lower ID in the
     * graph.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return The position of the low vertex in the list of vertices to display
     */
    public int getConnectionLowVertex(final int connection);

    /**
     * Get the endpoint vertex for the given connection with the lower ID in the
     * graph.
     *
     * This should return the vertex with the higher ID in the graph if the
     * connection is undirected
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @return The position of the high vertex in the list of vertices to
     * display
     */
    public int getConnectionHighVertex(final int connection);

    /**
     * Get the total number of links on the graph.
     *
     * This is independent from the set of connections that are being displayed
     * based on the graph's current connection mode.
     *
     * @return
     */
    public int getLinkCount();

    /**
     * Get the vertex with the lower id for the given link.
     *
     * @param link The position of the link in the list of links on the graph.
     * @return The position of the source vertex in the list of vertices to
     * display
     */
    public int getLinkLowVertex(int link);

    /**
     * Get the vertex with the higher id for the given link.
     *
     * @param link The position of the link in the list of links on the graph.
     * @return The position of the source vertex in the list of vertices to
     * display
     */
    public int getLinkHighVertex(int link);

    /**
     * Get the source vertex for the given link.
     *
     * This should return the vertex with the lower ID in the graph if the link
     * is undirected
     *
     * @param link The position of the link in the list of links on the graph.
     * @return The position of the source vertex in the list of vertices to
     * display
     */
    public int getLinkSource(final int link);

    /**
     * Get the destination vertex for the given link.
     *
     * This should return the vertex with the higher ID in the graph if the link
     * is undirected
     *
     * @param link The position of the link in the list of links on the graph.
     * @return The position of the destination vertex in the list of vertices to
     * display
     */
    public int getLinkDestination(final int link);

    /**
     * Get the number of connections that should be displayed for the given
     * link.
     *
     * This will depend on the graph's current connection mode, and the maximum
     * number of transactions to display per link on the graph.
     *
     * @param link The position of the link in the list of links on the graph.
     * @return
     */
    public int getLinkConnectionCount(final int link);

    /**
     * Get the connection to display at the given position within the given
     * link.
     *
     * @param link The position of the link in the list of links on the graph.
     * @param pos The position of the connection within the link.
     * @return The position of the connection in the list of connections to
     * display.
     */
    public int getLinkConnection(final int link, final int pos);

    /**
     * Get the text to display above the given node for the label in the given
     * position.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @param labelNum The position of the label in the list of top labels to
     * display.
     * @return
     */
    public String getVertexTopLabelText(final int vertex, final int labelNum);

    /**
     * Get the text to display below the given node for the label in the given
     * position.
     *
     * @param vertex The position of the vertex in the list of vertices to
     * display.
     * @param labelNum The position of the label in the list of bottom labels to
     * display.
     * @return
     */
    public String getVertexBottomLabelText(final int vertex, final int labelNum);

    /**
     * Get the text to display on the given connection for the label in the
     * given position.
     *
     * @param connection The position of the connection in the list of
     * connections to display.
     * @param labelNum The position of the label in the list of connection
     * labels to display.
     * @return
     */
    public String getConnectionLabelText(final int connection, final int labelNum);
}
