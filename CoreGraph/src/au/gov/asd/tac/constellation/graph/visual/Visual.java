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
package au.gov.asd.tac.constellation.graph.visual;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.utilities.ConnectionMode;
import java.io.File;

/**
 * An interface to any module that renders the graph for display.
 * <p>
 * This should be made available in a lookup so other modules can affect the way
 * that the graph is displayed.
 *
 * @author algol
 */
public interface Visual {

    /**
     * Reset the view of the graph to a "sensible" state.
     */
    void reset();

    /**
     * Get the graph display's draw flags as an <tt>int</tt> with bits defined
     * by {@link au.gov.asd.tac.constellation.renderer.GraphDrawVisuals}.
     *
     * @return The graph display's draw flags.
     */
    int getDrawFlags();

    /**
     * Set the graph display's draw flags as an <tt>int</tt> with bits defined
     * by {@link au.gov.asd.tac.constellation.renderer.GraphDrawVisuals}.
     *
     * @param flags See
     * {@link au.gov.asd.tac.constellation.renderer.GraphDrawVisuals}.
     */
    void setDrawFlags(int flags);

    /**
     * Get the type of connection that the graph display draws.
     *
     * @return The type of connection that the graph display draws.
     */
    ConnectionMode getConnectionMode();

    /**
     * Set the type of connection that the graph display draws.
     *
     * @param mode The connection mode.
     */
    void setConnectionMode(ConnectionMode mode);

    /**
     * Get the display mode.
     *
     * @return 2=2D, 3=3D.
     */
    int getDisplayMode();

    /**
     * Set the display mode.
     *
     * @param displayMode 2=2D, 3=3D.
     */
    void setDisplayMode(int displayMode);

    /**
     * Zoom the display to the currently selected vertices.
     */
    void zoomToSelection();

    /**
     * Zoom to given vertices.
     *
     * @param vertices an array of vertex ids that should be zoomed to.
     */
    void zoomToVertices(int[] vertices);

    /**
     * Unzoom the display to where it was before it was zoomed.
     */
    void zoomFromSelection();

    /**
     * Rotate the display to the specified axis.
     * <p>
     * An axis is specified by 'z', 'y', 'x', 'Z', 'Y', 'X'.
     *
     * @param rg The graph on which the display will be changed. Required to
     * determine the bounding box.
     * @param axis The axis to rotate the display to.
     */
    void resetAxis(GraphReadMethods rg, char axis);

    /**
     * Export the display to an image file.
     *
     * @param imageFile The File to write the image to.
     */
    void exportImage(File imageFile);

    /**
     * Set the graph to add mode
     */
    public void setAddMode();

    /**
     * Set the graph to standard selection mode
     */
    public void setSelectionMode();

    /**
     * Set the 'Add' mode to do directed connections
     */
    void setDirectedMode();

    /**
     * Set the 'Add' mode to do undirected connections
     */
    void setUndirectedMode();

    /**
     * return whether the graph is in selection mode or not
     *
     * @return boolean
     */
    boolean isSelectionMode();

    /**
     * return whether the graph is to use directed connections or not
     *
     * @return boolean
     */
    boolean isDirectedMode();
}
