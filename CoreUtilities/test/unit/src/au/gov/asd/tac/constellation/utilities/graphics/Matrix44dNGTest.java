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

import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Matrix44dNGTest {

    private static final double D[] = {
        101.1D, 102.2D, 103.3D, 104.4D,
        111.1D, 112.2D, 113.3D, 114.4D,
        121.1D, 122.2D, 123.3D, 124.4D,
        131.1D, 132.2D, 133.3D, 134.4D};
    private static final Matrix44d M1 = new Matrix44d();

    @BeforeClass
    public void before() {
        M1.setA(D);
    }

    /**
     * Can create a new Matrix44d.
     */
    @Test
    public void testConstructor() {
        final Matrix44d m = new Matrix44d();
        double a[] = m.getA();
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], 0D);
        }
    }

    /**
     * Can get the array of values inside the matrix.
     */
    @Test
    public void testGetA() {
        final double a[] = M1.getA();
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], D[i]);
        }
    }

    /**
     * Can set the array of values inside the matrix.
     */
    @Test
    public void testSetA() {
        final Matrix44d m = new Matrix44d();
        m.setA(Arrays.copyOf(D, D.length));
        for (int i = 0; i < D.length; i++) {
            assertEquals(m.getA()[i], D[i]);
        }
    }

    /**
     * Can set a matrix to the identity matrix values.
     */
    @Test
    public void testIdentity() {
        final double junk[] = new double[Matrix44d.LENGTH];
        for (int i = 0; i < Matrix44d.LENGTH; i++) {
            junk[i] = 1089.1451D;
        }
        final Matrix44d m = new Matrix44d();
        m.setA(junk);

        m.identity();
        final double a[] = m.getA();
        for (int i = 0; i < a.length; i++) {
            if (i == 0 || i == 5 || i == 10 || i == 15) {
                assertEquals(a[i], 1D);
            } else {
                assertEquals(a[i], 0D);
            }
        }
    }

    /**
     * Can get a 3x3 rotation matrix version of a matrix.
     */
    @Test
    public void testGetRotationMatrix33d() {
        final Matrix33d expected = new Matrix33d();
        expected.setA(new double[]{
            D[0], D[1], D[2], D[4], D[5], D[6], D[8], D[9], D[10]
        });

        final Matrix33d m33 = new Matrix33d();
        m33.setA(new double[]{1D, 2D, 3D, 4D, 5D, 6D, 7D, 8D, 9D});

        M1.getRotationMatrix(m33);
        for (int i = 0; i < expected.getA().length; i++) {
            assertEquals(m33.getA()[i], expected.getA()[i]);
        }
    }

    /**
     * Can inject a 3x3 rotation matrix into a 4x4 matrix.
     */
    @Test
    public void testSetRotationMatrix() {
        final Matrix44d expected = new Matrix44d();
        final double[] d33 = {
            311D, 312D, 313D,
            321D, 322D, 323D,
            331D, 332D, 323D};
        expected.setA(new double[]{
            d33[0], d33[1], d33[2], 104.4D,
            d33[3], d33[4], d33[5], 114.4D,
            d33[6], d33[7], d33[8], 124.4D,
            131.1D, 132.2D, 133.3D, 134.4D
        });

        final Matrix33d m33 = new Matrix33d();
        m33.setA(Arrays.copyOf(d33, d33.length));

        final Matrix44d m44 = new Matrix44d();
        m44.setA(Arrays.copyOf(D, D.length));
        m44.setRotationMatrix(m33);
        for (int i = 0; i < expected.getA().length; i++) {
            assertEquals(expected.getA()[i], m44.getA()[i]);
        }
    }
}
