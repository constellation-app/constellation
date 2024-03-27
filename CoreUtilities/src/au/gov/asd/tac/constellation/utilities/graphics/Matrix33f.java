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
 * A 3x3 matrix of floats.
 * <p>
 * The matrix is stored in a float[] in column major format.
 * <pre>
 * 0       3       6
 * 1       4       7
 * 2       5       8
 * </pre> The matrix array is called "a", and can be publicly accessed.
 *
 * @author algol
 */
public final class Matrix33f {

    public final float[] a;
    public static final int LENGTH = 9;
    private static final float[] IDENTITY33F
            = {
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
            };

    /**
     * Create a new matrix.
     */
    public Matrix33f() {
        a = new float[LENGTH];
    }

    /**
     * Set this matrix to the identity matrix.
     */
    public void makeIdentity() {
        System.arraycopy(IDENTITY33F, 0, a, 0, LENGTH);
    }

    public void makeRotationMatrix(final float angle, float x, float y, float z) {
        final float s = (float) Math.sin(angle);
        final float c = (float) Math.cos(angle);

        final float mag = (float) (Math.sqrt(x * x + y * y + z * z));

        // Identity matrix.
        if (mag == 0) {
            makeIdentity();
            return;
        }

        // Rotation matrix is normalized.
        x /= mag;
        y /= mag;
        z /= mag;

        final float xx = x * x;
        final float yy = y * y;
        final float zz = z * z;
        final float xy = x * y;
        final float yz = y * z;
        final float zx = z * x;
        final float xs = x * s;
        final float ys = y * s;
        final float zs = z * s;
        final float oneC = 1.0F - c;

        a[index33(0, 0)] = (oneC * xx) + c;
        a[index33(0, 1)] = (oneC * xy) - zs;
        a[index33(0, 2)] = (oneC * zx) + ys;

        a[index33(1, 0)] = (oneC * xy) + zs;
        a[index33(1, 1)] = (oneC * yy) + c;
        a[index33(1, 2)] = (oneC * yz) - xs;

        a[index33(2, 0)] = (oneC * zx) - ys;
        a[index33(2, 1)] = (oneC * yz) + xs;
        a[index33(2, 2)] = (oneC * zz) + c;
    }

    /**
     * Make this into a scaling matrix.
     *
     * @param xScale the x scaling factor.
     * @param yScale the y scaling factor.
     * @param zScale the z scaling factor.
     */
    public void makeScalingMatrix(final float xScale, final float yScale, final float zScale) {
        makeIdentity();
        a[0] = xScale;
        a[4] = yScale;
        a[8] = zScale;
    }

    /**
     * Make this into a scaling matrix.
     *
     * @param vScale a vector containing the scaling factors on each axis.
     */
    public void makeScalingMatrix(final Vector3f vScale) {
        makeIdentity();
        a[0] = vScale.a[0];
        a[4] = vScale.a[1];
        a[8] = vScale.a[2];
    }

    private static int index33(final int row, final int col) {
        return (col * 3) + row;
    }

    /**
     * Multiply two matrices and put the answer in this matrix.
     *
     * @param a the first matrix.
     * @param b the second matrix.
     */
    public void multiply(final Matrix33f a, final Matrix33f b) {
        final float[] pa = this.a;
        final float[] aa = a.a;
        final float[] ba = b.a;

        for (int i = 0; i < 3; i++) {
            final float ai0 = aa[index33(i, 0)];
            final float ai1 = aa[index33(i, 1)];
            final float ai2 = aa[index33(i, 2)];
            pa[index33(i, 0)] = ai0 * ba[index33(0, 0)] + ai1 * ba[index33(1, 0)] + ai2 * ba[index33(2, 0)];
            pa[index33(i, 1)] = ai0 * ba[index33(0, 1)] + ai1 * ba[index33(1, 1)] + ai2 * ba[index33(2, 1)];
            pa[index33(i, 2)] = ai0 * ba[index33(0, 2)] + ai1 * ba[index33(1, 2)] + ai2 * ba[index33(2, 2)];
        }
    }

    /**
     * Get a column from this matrix.
     *
     * @param dst the vector that will contain the column of this matrix.
     * @param column the position of the column to be copied.
     */
    public void getMatrixColumn(final Vector3f dst, final int column) {
        final int col = column * 3;
        dst.a[0] = a[col];
        dst.a[1] = a[col + 1];
        dst.a[2] = a[col + 2];
    }

    public void inverse() {
        float t = a[1];
        a[1] = a[3];
        a[3] = t;

        t = a[2];
        a[2] = a[6];
        a[6] = t;

        t = a[5];
        a[5] = a[7];
        a[7] = t;
    }

    /**
     * Set a column in this matrix.
     *
     * @param src a vector containing the new column values.
     * @param column the column to be set.
     */
    public void setMatrixColumn(final Vector3f src, final int column) {
        final int col = column * 3;
        a[col] = src.a[0];
        a[col + 1] = src.a[1];
        a[col + 2] = src.a[2];
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                b.append(",");
                if (i % 3 == 0) {
                    b.append(" ");
                }
            }

            b.append(Float.toString(a[i]));
        }

        b.append("]");

        return b.toString();
    }
}
