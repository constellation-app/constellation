/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.geospatial;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.MGRSCoord;

/**
 *
 * @author cygnus_x-1
 */
public class Mgrs {

    public static final String encode(final double latitude, final double longitude) {
        final Angle latAngle = Angle.fromDegrees(latitude);
        final Angle lonAngle = Angle.fromDegrees(longitude);
        return MGRSCoord.fromLatLon(latAngle, lonAngle).toString();
    }

    public static final double[] decode(final String mgrs) {
        final MGRSCoord coordinate = MGRSCoord.fromString(mgrs, null);
        return new double[]{coordinate.getLatitude().degrees, coordinate.getLongitude().degrees};
    }
}
