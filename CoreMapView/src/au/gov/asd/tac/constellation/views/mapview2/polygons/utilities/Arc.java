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

import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;

/**
 *
 * @author altair1673
 */
public class Arc extends BlineElement {
    private final Vec3 focus;


    public Arc(final Vec3 focus) {
        this.focus = focus;
    }

    public Vec3 getFocus() {
        return focus;
    }

    public double getY(final double x, final double directtrix) {
        return (Math.pow((x - focus.getX()), 2) / (2 * (focus.getY() - directtrix))) + ((focus.getX() + directtrix) / 2);
    }

}
