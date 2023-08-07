/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.polygons.utilities;

import au.gov.asd.tac.constellation.views.mapview2.utilities.Edge;
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;

/**
 *
 * @author altair1673
 */
public class EdgeEvent extends VoronoiEvent {

    private final HalfEdge edge1;
    private final HalfEdge edge2;

    private final Vec3 intersectionPoint;
    private final BlineElement squeezed;

    private boolean isValid = true;

    public EdgeEvent(final double yOfEvent, final HalfEdge e1, final HalfEdge e2, final BlineElement squeezed, final Vec3 intersection) {
        super(yOfEvent);

        edge1 = e1;
        edge2 = e2;

        this.squeezed = squeezed;

        this.intersectionPoint = intersection;
    }

    public HalfEdge getEdge1() {
        return edge1;
    }

    public HalfEdge getEdge2() {
        return edge2;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(final boolean isValid) {
        this.isValid = isValid;
    }

    public Vec3 getIntersectionPoint() {
        return intersectionPoint;
    }

    public BlineElement getSqueezed() {
        return squeezed;
    }

}
