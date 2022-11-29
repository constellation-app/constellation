/*
 * Copyright 2010-2021 Australian Signals Directorate
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
 * A vector of two floating point values.
 *
 * @author algol
 */
public final class Vector2f {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 2;

    /**
     * The contents of this vector.
     */
    public final float[] a;

    public Vector2f() {
        a = new float[LENGTH];
    }

    public void set(final float x, final float y) {
        a[0] = x;
        a[1] = y;
    }

    public void set(final Vector2f v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
    }

    public void scale(final float s) {
        a[0] *= s;
        a[1] *= s;
    }

    public static void add(final Vector2f result, final Vector2f a, final Vector2f b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
    }

    public static void subtract(final Vector2f result, final Vector2f a, final Vector2f b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
    }

    public static Vector2f[] createArray(final int length) {
        Vector2f[] array = new Vector2f[length];
        for (int i = 0; i < length; i++) {
            array[i] = new Vector2f();
        }

        return array;
    }
}
