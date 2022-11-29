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
 *
 * @author algol
 */
public final class Vector4d {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 4;

    /**
     * The contents of this vector.
     */
    private double[] a;

    public Vector4d() {
        a = new double[LENGTH];
    }

    public double[] getA() {
        return a;
    }

    public void setA(double[] a) {
        this.a = a;
    }

    public void set(final double x, final double y, final double z, final double w) {
        a[0] = x;
        a[1] = y;
        a[2] = z;
        a[3] = w;
    }

    public void set(final Vector4d v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
        a[3] = v.a[3];
    }

    public void scale(final double s) {
        a[0] *= s;
        a[1] *= s;
        a[2] *= s;
        a[3] *= s;
    }

    public static void add(final Vector4d result, final Vector4d a, final Vector4d b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
        result.a[2] = a.a[2] + b.a[2];
        result.a[3] = a.a[3] + b.a[3];
    }

    public static void subtract(final Vector4d result, final Vector4d a, final Vector4d b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
        result.a[2] = a.a[2] - b.a[2];
        result.a[3] = a.a[3] - b.a[3];
    }

    public void getMatrixColumn(final Vector4d dst, final int column) {
        System.arraycopy(a, 4 * column, dst.a, 0, 4);
    }

    public void setMatrixColumn(final Vector4d dst, final int column) {
        System.arraycopy(a, 0, dst.a, 4 * column, 4);
    }
}
