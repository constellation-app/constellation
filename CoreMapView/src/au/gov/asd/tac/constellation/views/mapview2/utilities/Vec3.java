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
 * Utility class to hold 3 variables and do vector maths on them
 *
 * @author altair1673
 */
public class Vec3 {
    private double x;
    private double y;
    private double z;

    public Vec3() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vec3(final Vec3 vec3) {
        x = vec3.x;
        y = vec3.y;
        z = vec3.z;
    }

    public Vec3(final double x, final double y) {
        z = 0;
        this.x = x;
        this.y = y;
    }

    public Vec3(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void multiplyFloat(final float value) {
        x *= value;
        y *= value;
        z *= value;
    }

    public void multiplyDouble(final double value) {
        x *= value;
        y *= value;
        z *= value;
    }

    public void addVector(final Vec3 value) {
        x += value.x;
        y += value.y;
        z += value.z;
    }

    public void divVector(final double d) {
        if (d != 0) {
            x /= d;
            y /= d;
            z /= d;
        }
    }

    public static double getDistance(final Vec3 v1, final Vec3 v2) {
        return Math.sqrt(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2));
    }

    public Vec3 cross(final Vec3 v) {
        final Vec3 ans = new Vec3();
        ans.x = y * v.z - z * v.y;
        ans.y = z * v.x - x * v.z;
        ans.z = x * v.y - y * v.x;

        return ans;
    }

    public double getX() {
        return x;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(final double z) {
        this.z = z;
    }

    public void setVec3(final Vec3 v3) {
        this.x = v3.getX();
        this.y = v3.getY();
        this.z = v3.getZ();
    }

    public void normalizeVec2() {
        final double temp = x;
        x = x / Math.sqrt(x * x + y * y);
        y = y / Math.sqrt(temp * temp + y * y);
    }
}
