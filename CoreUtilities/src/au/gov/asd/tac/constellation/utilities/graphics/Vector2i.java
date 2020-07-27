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
package au.gov.asd.tac.constellation.utilities.graphics;

public class Vector2i {
    /**
     * The length of a vector.
     */
    public static final int LENGTH = 2;

    /**
     * The contents of this vector.
     */
    public final int[] a;

    public Vector2i() {
        a = new int[LENGTH];
    }
    
    public Vector2i(final int x, final int y) {
        a = new int[LENGTH];
        set(x, y);
    }    

    public void set(final int x, final int y) {
        a[0] = x;
        a[1] = y;
    }

    public void set(final Vector2i v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
    }
    
    public int getX() {
        return a[0];
    }

    public void setX(final int x) {
        a[0] = x;
    }

    public int getY() {
        return a[1];
    }

    public void setY(final int y) {
        a[1] = y;
    }    

    public static void add(final Vector2i result, final Vector2i a, final Vector2i b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
    }

    public static void subtract(final Vector2i result, final Vector2i a, final Vector2i b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
    }

    public static Vector2i[] createArray(final int length) {
        Vector2i[] array = new Vector2i[length];
        for (int i = 0; i < length; i++) {
            array[i] = new Vector2i();
        }

        return array;
    }
}
