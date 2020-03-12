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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3;

/**
 *
 * @author algol
 */
public final class Orb3D {

    public float x;
    public float y;
    public float z;
    public final float r;

    public Orb3D(final float x, final float y, final float z, final float r) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
    }

//    public boolean contains(final float x2, final float y2, final float z2)
//    {
//        final double dx = x - x2;
//        final double dy = y - y2;
//        final double dz = y - z2;
//        return Math.sqrt(dx*dx + dy*dy + dz*dz) <= r;
//    }
    @Override
    public String toString() {
        return String.format("[Orb3D {%d} %f,%f,%f %f]", hashCode(), x, y, z, r);
    }
}
