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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3;

/**
 *
 * @author algol
 */
public final class Orb3D {

    private float x;
    private float y;
    private float z;
    public final float r;

    public Orb3D(final float x, final float y, final float z, final float r) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
    }

    public float getX() {
        return x;
    }

    public void setX(final float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(final float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return String.format("[Orb3D {%d} %f,%f,%f %f]", hashCode(), x, y, z, r);
    }
}
