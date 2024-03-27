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
package au.gov.asd.tac.constellation.utilities.visual;

/**
 * Define elements of the graph to be drawn.
 * <p>
 * It is sometimes convenient to only draw some elements of the graph. The
 * constants in this class form a set of flags that can be ORed together to tell
 * the renderer which elements of the graph to draw.
 *
 * @author algol
 */
public final class DrawFlags {

    /**
     * Draw the nodes.
     */
    public static final int NODES = 1;

    /**
     * Draw the connections.
     */
    public static final int CONNECTIONS = 2;

    /**
     * Draw the node labels.
     */
    public static final int NODE_LABELS = 4;

    /**
     * Draw the connection labels.
     */
    public static final int CONNECTION_LABELS = 8;

    /**
     * Draw blazes.
     */
    public static final int BLAZES = 16;

    /**
     * Draw everything (the other flags ORed together).
     */
    public static final DrawFlags ALL = new DrawFlags(31);

    /**
     * Draw nothing.
     */
    public static final DrawFlags NONE = new DrawFlags(0);

    /**
     * Draw in 2D mode
     */
    public static final int MODE_2D = 2;

    /**
     * Draw in 2D mode
     */
    public static final int MODE_3D = 3;

    private final int drawFlgs;

    public DrawFlags(final int drawFlags) {
        this.drawFlgs = drawFlags;
    }

    public DrawFlags(final boolean drawNodes, final boolean drawConnections, final boolean drawNodeLabels, final boolean drawConnectionLabels, final boolean drawBlazes) {
        this((drawNodes ? NODES : 0) + (drawConnections ? CONNECTIONS : 0) + (drawNodeLabels ? NODE_LABELS : 0) + (drawConnectionLabels ? CONNECTION_LABELS : 0) + (drawBlazes ? BLAZES : 0));
    }

    public int getFlags() {
        return drawFlgs;
    }

    public boolean drawNodes() {
        return (drawFlgs & NODES) != 0;
    }

    public boolean drawConnections() {
        return (drawFlgs & CONNECTIONS) != 0;
    }

    public boolean drawNodeLabels() {
        return (drawFlgs & NODE_LABELS) != 0;
    }

    public boolean drawConnectionLabels() {
        return (drawFlgs & CONNECTION_LABELS) != 0;
    }

    public boolean drawBlazes() {
        return (drawFlgs & BLAZES) != 0;
    }

    public boolean drawAll() {
        return drawFlgs == 31;
    }

    public boolean drawAny() {
        return drawFlgs != 0;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof DrawFlags drawFlagsObj && drawFlgs == drawFlagsObj.drawFlgs;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.drawFlgs;
        return hash;
    }

    @Override
    public String toString() {
        return String.valueOf(drawFlgs);
    }

}
