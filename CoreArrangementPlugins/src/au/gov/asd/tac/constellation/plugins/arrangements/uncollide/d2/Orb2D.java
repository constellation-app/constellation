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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d2;

/**
 *
 * @author algol
 */
public final class Orb2D {

    private float x;
    private float y;
    public final float r;

    public Orb2D(final float x, final float y, final float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

//    public boolean contains(final float x2, final float y2)
//    {
//        final double dx = x - x2;
//        final double dy = y - y2;
//        return Math.hypot(dx, dy) <= r;
//    }
    @Override
    public String toString() {
        return String.format("[Orb2D {%d} %f,%f %f]", hashCode(), x, y, r);
    }
}
