/*
 * Copyright 2010-2019 Australian Signals Directorate
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
 * A 4x4 matrix of doubles.
 *
 * @author algol
 */
public final class Matrix44d {

    public double[] a;
    public static final int LENGTH = 16;

    private static final double[] IDENTITY44D
            = {
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
            };

    public Matrix44d() {
        a = new double[LENGTH];
    }

    public void identity() {
        System.arraycopy(IDENTITY44D, 0, a, 0, LENGTH);
    }

    public void getRotationMatrix(final Matrix33d dst) {
//        memcpy(dst, src, sizeof(float)*3); // X column
//        memcpy(dst+3, src+4, sizeof(float)*3); // Y column
//        memcpy(dst+6, src+8, sizeof(float)*3); // Z column
        System.arraycopy(a, 0, dst.a, 0, 3); // X column
        System.arraycopy(a, 4, dst.a, 3, 3); // Y column
        System.arraycopy(a, 8, dst.a, 6, 3); // Z column
    }

    public void setRotationMatrix(final Matrix33d src) {
//        memcpy(dst, src, sizeof(float)*4);
//        memcpy(dst+4, src+4, sizeof(float)*4);
//        memcpy(dst+8, src+8, sizeof(float)*4);
        System.arraycopy(src.a, 0, a, 0, 4);
        System.arraycopy(src.a, 4, a, 4, 4);
        System.arraycopy(src.a, 8, a, 8, 4);
    }
}
