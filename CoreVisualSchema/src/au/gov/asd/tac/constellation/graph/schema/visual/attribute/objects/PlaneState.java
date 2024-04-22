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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Keep track of the use of planes in a graph.
 *
 * @author algol
 */
public final class PlaneState {

    /**
     * The name of the graph attribute used to hold the instance.
     */
    public static final String ATTRIBUTE_NAME = "planes";

    private ArrayList<Plane> planes;
    private boolean visibilityUpdate;

    public PlaneState() {
        visibilityUpdate = false;
        planes = new ArrayList<>();
    }

    public PlaneState(final PlaneState planeState) {
        this();
        for (final Plane plane : planeState.planes) {
            this.planes.add(plane);
        }
    }

    public void addPlane(final Plane plane) {
        planes.add(plane);
        visibilityUpdate = false;
    }

    public void setPlanes(final List<Plane> planes) {
        this.planes.clear();
        for (final Plane plane : planes) {
            this.planes.add(plane);
        }
    }

    public void removePlane(final int ix) {
        planes.remove(ix);
        visibilityUpdate = false;
    }

    public Plane getPlane(int ix) {
        return planes.get(ix);
    }

    public List<Plane> getPlanes() {
        return planes;
    }

    public BitSet getVisiblePlanes() {
        final BitSet visiblePlanes = new BitSet();
        int ix = 0;
        for (final Plane plane : planes) {
            if (plane.isVisible()) {
                visiblePlanes.set(ix);
            }

            ix++;
        }

        return visiblePlanes;
    }

    public void setVisiblePlanes(final BitSet bs) {
        int ix = 0;
        for (final Plane plane : planes) {
            plane.setVisible(bs.get(ix));

            ix++;
        }

        visibilityUpdate = true;
    }

    /**
     * Is the latest change visibility only (ie no change in planes)?
     * <p>
     * The internal flag will be reset before the method returns.
     * <p>
     * This is a hack to make plane visibility work efficiently. Only call this
     * from GraphChangeDetector.
     *
     * @return whether the latest change involves visibility only.
     */
    public boolean isVisibilityUpdate() {
        final boolean v = visibilityUpdate;
        visibilityUpdate = false;

        return v;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("%s[\n");
        for (final Plane plane : planes) {
            b.append(plane.toString());
            b.append(SeparatorConstants.NEWLINE);
        }

        b.append("]");

        return b.toString();
    }
}
