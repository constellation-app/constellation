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
 * A 4x4 matrix of floats.
 * <p>
 * The matrix is stored in a float[] in column major format.
 * <pre>
 * 0       4       8       12
 * 1       5       9       13
 * 2       6       10      14
 * 3       7       11      15
 * </pre> The matrix array is called "a", and can be publicly accessed.
 *
 * @author algol
 */
public final class Matrix44f {

    public final float[] a;
    public static final int LENGTH = 16;

    private static final float[] IDENTITY44F
            = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
            };

    /**
     * Create a new matrix.
     */
    public Matrix44f() {
        a = new float[LENGTH];
    }

    /**
     * Set the matrix element at (row, column) to value.
     *
     * @param row the row of the cell to be set.
     * @param column the column of the cell to be set.
     * @param value the new value for the cell.
     */
    public void set(final int row, final int column, final float value) {
        a[row * 4 + column] = value;
    }

    /**
     * Set the matrix element at (row, column) to value.
     *
     * @param row the row of the cell to be set.
     * @param column the column of the cell to be set.
     * @param value the new value for the cell.
     */
    public void setTransposed(final int row, final int column, final float value) {
        a[column * 4 + row] = value;
    }

    /**
     * Get the value of the matrix element at (row, column).
     *
     * @param row the row of the cell to be returned.
     * @param column the column of the cell to be returned.
     * @return the current value of the specified cell.
     */
    public float get(final int row, final int column) {
        return a[row * 4 + column];
    }

    /**
     * Set this matrix to the identity matrix.
     */
    public void makeIdentity() {
        System.arraycopy(IDENTITY44F, 0, a, 0, LENGTH);
    }

    /**
     * Return a new identity matrix.
     *
     * @return the new identity matrix.
     */
    public static Matrix44f identity() {
        final Matrix44f m = new Matrix44f();
        m.makeIdentity();

        return m;
    }

    public void makeScalingMatrix(final float xScale, final float yScale, final float zScale) {
        makeIdentity();
        a[0] = xScale;
        a[5] = yScale;
        a[10] = zScale;
    }

    public void makeRotationMatrix(final float angle, float x, float y, float z) {
        final float s = (float) Math.sin(angle);
        final float c = (float) Math.cos(angle);

        final float mag = (float) Math.sqrt(x * x + y * y + z * z);

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

        a[index44(0, 0)] = (oneC * xx) + c;
        a[index44(0, 1)] = (oneC * xy) - zs;
        a[index44(0, 2)] = (oneC * zx) + ys;
        a[index44(0, 3)] = 0.0F;

        a[index44(1, 0)] = (oneC * xy) + zs;
        a[index44(1, 1)] = (oneC * yy) + c;
        a[index44(1, 2)] = (oneC * yz) - xs;
        a[index44(1, 3)] = 0.0F;

        a[index44(2, 0)] = (oneC * zx) - ys;
        a[index44(2, 1)] = (oneC * yz) + xs;
        a[index44(2, 2)] = (oneC * zz) + c;
        a[index44(2, 3)] = 0.0F;

        a[index44(3, 0)] = 0.0F;
        a[index44(3, 1)] = 0.0F;
        a[index44(3, 2)] = 0.0F;
        a[index44(3, 3)] = 1.0F;
    }

    public void makeScalingMatrix(final Vector3f vScale) {
        makeIdentity();
        a[0] = vScale.a[0];
        a[5] = vScale.a[1];
        a[10] = vScale.a[2];
    }

    /**
     * Make this a translation matrix.
     *
     * @param x the translation along the x axis
     * @param y the translation along the y axis
     * @param z the translation along the z axis
     */
    public void makeTranslationMatrix(final float x, final float y, final float z) {
        makeIdentity();
        a[12] = x;
        a[13] = y;
        a[14] = z;
    }

    /**
     * Make this a projection matrix.
     * <p>
     * This is similar to gluPerspective().
     *
     * @param fFov Field of view (radians).
     * @param fAspect Aspect ratio.
     * @param zMin Near z plane.
     * @param zMax Far z plane.
     */
    public void makePerspectiveMatrix(final float fFov, final float fAspect, final float zMin, final float zMax) {
        makeIdentity(); // Fastest way to get most valid values already in place

        float yMax = (float) (zMin * Math.tan(fFov * 0.5F));
        float yMin = -yMax;
        float xMin = yMin * fAspect;
        float xMax = -xMin;

        a[0] = (2.0F * zMin) / (xMax - xMin);
        a[5] = (2.0F * zMin) / (yMax - yMin);
        a[8] = (xMax + xMin) / (xMax - xMin);
        a[9] = (yMax + yMin) / (yMax - yMin);
        a[10] = -((zMax + zMin) / (zMax - zMin));
        a[11] = -1;
        a[14] = -((2.0F * (zMax * zMin)) / (zMax - zMin));
        a[15] = 0;
    }

    /**
     * Make this an orthographic projection matrix.
     *
     * @param xMin the minimum x value.
     * @param xMax the maximum x value.
     * @param yMin the minimum y value.
     * @param yMax the maximum y value.
     * @param zMin the minimum z value.
     * @param zMax the maximum z value.
     */
    public void makeOrthographicMatrix(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin, final float zMax) {
        makeIdentity();

        a[0] = 2.0F / (xMax - xMin);
        a[5] = 2.0F / (yMax - yMin);
        a[10] = -2.0F / (zMax - zMin);
        a[12] = -((xMax + xMin) / (xMax - xMin));
        a[13] = -((yMax + yMin) / (yMax - yMin));
        a[14] = -((zMax + zMin) / (zMax - zMin));
        a[15] = 1;
    }

    /**
     * Copy the specified matrix into this.
     *
     * @param m the matrix to copy into this matrix.
     */
    public void set(final Matrix44f m) {
        System.arraycopy(m.a, 0, a, 0, LENGTH);
    }

    private static int index44(final int col, final int row) {
        return (row * 4) + col;
    }

    /**
     * Multiply two matrices and put the answer in this.
     *
     * @param a the first matrix.
     * @param b the second matrix.
     */
    public void multiply(final Matrix44f a, final Matrix44f b) {
        final float[] pa = this.a;
        final float[] aa = a.a;
        final float[] ba = b.a;

        for (int i = 0; i < 4; i++) {
            final float ai0 = aa[index44(i, 0)];
            final float ai1 = aa[index44(i, 1)];
            final float ai2 = aa[index44(i, 2)];
            final float ai3 = aa[index44(i, 3)];
            pa[index44(i, 0)] = ai0 * ba[index44(0, 0)] + ai1 * ba[index44(1, 0)] + ai2 * ba[index44(2, 0)] + ai3 * ba[index44(3, 0)];
            pa[index44(i, 1)] = ai0 * ba[index44(0, 1)] + ai1 * ba[index44(1, 1)] + ai2 * ba[index44(2, 1)] + ai3 * ba[index44(3, 1)];
            pa[index44(i, 2)] = ai0 * ba[index44(0, 2)] + ai1 * ba[index44(1, 2)] + ai2 * ba[index44(2, 2)] + ai3 * ba[index44(3, 2)];
            pa[index44(i, 3)] = ai0 * ba[index44(0, 3)] + ai1 * ba[index44(1, 3)] + ai2 * ba[index44(2, 3)] + ai3 * ba[index44(3, 3)];
        }
    }

    /**
     * Multiply this by (effectively) a float[4].
     *
     * @param x the x component of the vector.
     * @param y the y component of the vector.
     * @param z the z component of the vector.
     * @param w the w component of the vector.
     *
     * @return A float[4] containing the result of the matrix44*vec4
     * multiplication.
     */
    public float[] multiply(final float x, final float y, final float z, final float w) {
        final float[] vec4 = new float[4];

        vec4[0] = a[0] * x + a[4] * y + a[8] * z + a[12] * w;
        vec4[1] = a[1] * x + a[5] * y + a[9] * z + a[13] * w;
        vec4[2] = a[2] * x + a[6] * y + a[10] * z + a[14] * w;
        vec4[3] = a[3] * x + a[7] * y + a[11] * z + a[15] * w;

        return vec4;
    }

    /**
     * Calculate the determinant of a 4x4 matrix. For private use only.
     *
     * @param m the matrix to use in the calculation.
     * @param i the row to use in the determinant calculation.
     * @param j the column to use in the determinant calculation.
     *
     * @return the determinant.
     */
    private static float detIJ(final Matrix44f m, final int i, final int j) {
        final float[][] mat = new float[3][3];

        int x = 0;
        for (int ii = 0; ii < 4; ii++) {
            if (ii == i) {
                continue;
            }
            int y = 0;
            for (int jj = 0; jj < 4; jj++) {
                if (jj == j) {
                    continue;
                }
                mat[x][y] = m.a[(ii * 4) + jj];
                y++;
            }
            x++;
        }

        float ret = mat[0][0] * (mat[1][1] * mat[2][2] - mat[2][1] * mat[1][2]);
        ret -= mat[0][1] * (mat[1][0] * mat[2][2] - mat[2][0] * mat[1][2]);
        ret += mat[0][2] * (mat[1][0] * mat[2][1] - mat[2][0] * mat[1][1]);

        return ret;
    }

    /**
     * Invert the matrix specified matrix, put the result in this.
     *
     * @param m The matrix to invert.
     */
    public void invert(final Matrix44f m) {
        // Calculate 4x4 determinant.
        float det = 0.0F;
        for (int i = 0; i < 4; i++) {
            det += (i & 0x1) == 1 ? (-m.a[i] * detIJ(m, 0, i)) : (m.a[i] * detIJ(m, 0, i));
        }
        det = (det != 0.0F ? (1.0F / det) : Float.POSITIVE_INFINITY);

        // Calculate inverse.
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final float detij = detIJ(m, j, i);
                a[(i * 4) + j] = ((i + j) & 0x1) == 1 ? (-detij * det) : (detij * det);
            }
        }
    }

    public void getRotationMatrix(final Matrix33f dst) {
        System.arraycopy(a, 0, dst.a, 0, 3); // X column
        System.arraycopy(a, 4, dst.a, 3, 3); // Y column
        System.arraycopy(a, 8, dst.a, 6, 3); // Z column
    }

    public void getRotationMatrix(final Matrix44f dst) {
        System.arraycopy(a, 0, dst.a, 0, 16);
        dst.a[3] = 0;
        dst.a[7] = 0;
        dst.a[11] = 0;
        dst.a[12] = 0;
        dst.a[13] = 0;
        dst.a[14] = 0;
        dst.a[15] = 1;
    }

    /**
     * Inject a 3x3 rotation matrix into a 4x4 matrix.
     *
     * @param src the matrix that supplies the rotation component of this
     * matrix.
     */
    public void setRotationMatrix(final Matrix33f src) {
        System.arraycopy(src.a, 0, a, 0, 3);
        System.arraycopy(src.a, 3, a, 4, 3);
        System.arraycopy(src.a, 6, a, 8, 3);
    }

    public void setRow(final Vector3f vector, final int row) {
        final int pos = row * 4;
        a[pos] = vector.a[0];
        a[pos + 1] = vector.a[1];
        a[pos + 2] = vector.a[2];
    }

    public void setRow(final float f0, final float f1, final float f2, final float f3, final int row) {
        final int pos = row * 4;
        a[pos] = f0;
        a[pos + 1] = f1;
        a[pos + 2] = f2;
        a[pos + 3] = f3;
    }

    public static Matrix44f[] createArray(final int length) {
        final Matrix44f[] array = new Matrix44f[length];
        for (int i = 0; i < length; i++) {
            array[i] = new Matrix44f();
        }

        return array;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                b.append(",");
                if (i % 4 == 0) {
                    b.append(" ");
                }
            }

            b.append(Float.toString(a[i]));
        }

        b.append("]");

        return b.toString();
    }
}
