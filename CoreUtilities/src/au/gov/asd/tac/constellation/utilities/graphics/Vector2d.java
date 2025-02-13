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
package au.gov.asd.tac.constellation.utilities.graphics;

/**
 *
 * @author algol
 */
public final class Vector2d {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 2;

    /**
     * The contents of this vector.
     */
    private final double[] a;

    public Vector2d() {
        a = new double[LENGTH];
    }

    public double[] getA() {
        return a;
    }

    public void set(final double x, final double y) {
        a[0] = x;
        a[1] = y;
    }

    public void set(final Vector2d v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
    }

    public void scale(final double s) {
        a[0] *= s;
        a[1] *= s;
    }

    public static void add(final Vector2d result, final Vector2d a, final Vector2d b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
    }

    public static void subtract(final Vector2d result, final Vector2d a, final Vector2d b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
    }
}
