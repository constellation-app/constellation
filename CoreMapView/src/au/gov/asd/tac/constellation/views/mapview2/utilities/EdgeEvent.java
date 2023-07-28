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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

/**
 *
 * @author altair1673
 */
public class EdgeEvent extends VoronoiEvent {

    private final Edge edge1;
    private final Edge edge2;
    private final Parabola involvedArc;
    private final Vec3 intersectionPoint;

    private boolean isValid = true;

    public EdgeEvent(double yOfEvent, final Vec3 intersection, final Edge a, final Edge b, final Parabola arc) {
        super(yOfEvent);

        intersectionPoint = intersection;
        involvedArc = arc;
        edge1 = a;
        edge2 = b;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public Parabola getInvolvedArc() {
        return involvedArc;
    }

    public Vec3 getIntersectionPoint() {
        return intersectionPoint;
    }



}
