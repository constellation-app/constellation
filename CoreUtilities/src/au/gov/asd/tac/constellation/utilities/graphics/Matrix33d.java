/*
 * Copyright 2010-2025 Australian Signals Directorate
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
 * A 3x3 matrix of doubles.
 *
 * @author algol
 */
public final class Matrix33d {

    private double[] a;
    public static final int LENGTH = 9;

    private static final double[] IDENTITY33D
            = {
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
            };

    public Matrix33d() {
        a = new double[LENGTH];
    }

    public double[] getA() {
        return a;
    }

    public void setA(double[] a) {
        this.a = a;
    }

    public void identity() {
        System.arraycopy(IDENTITY33D, 0, a, 0, LENGTH);
    }

    public void getMatrixColumn(final Vector3d dst, final int column) {
        final int col = column * 3;
        dst.a[0] = a[col];
        dst.a[1] = a[col + 1];
        dst.a[2] = a[col + 2];
    }

    public void setMatrixColumn(final Vector3d src, final int column) {
        final int col = column * 3;
        a[col] = src.a[0];
        a[col + 1] = src.a[1];
        a[col + 2] = src.a[2];
    }
}
