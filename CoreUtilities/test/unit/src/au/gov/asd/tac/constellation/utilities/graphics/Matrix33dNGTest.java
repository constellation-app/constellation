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
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Matrix33dNGTest {

    private static final double D1 = 123D;
    private static final double D2 = 321D;
    private static final double D3 = 456D;
    private static final double D4 = 654D;
    private static final double D5 = 789D;
    private static final double D6 = 987D;
    private static final double D7 = 195D;
    private static final double D8 = 951D;
    private static final double D9 = 286D;
    private static final double D10 = 101.1D;
    private static final double D11 = 202.2D;
    private static final double D12 = 303.3D;
    private static final Vector3d V1 = new Vector3d(D1, D2, D3);
    private static final Vector3d V2 = new Vector3d(D4, D5, D6);
    private static final Vector3d V3 = new Vector3d(D7, D8, D9);

    // convenience method to get a new matrix
    private Matrix33d getMatrix() {
        final Matrix33d m = new Matrix33d();
        m.setMatrixColumn(new Vector3d(V1), 0);
        m.setMatrixColumn(new Vector3d(V2), 1);
        m.setMatrixColumn(new Vector3d(V3), 2);
        return m;
    }

    /**
     * Can create a new Matrix33d.
     */
    @Test
    public void testConstructor() {
        final Matrix33d m = new Matrix33d();
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
        final double expected[] = {D1, D2, D3, D4, D5, D6, D7, D8, D9};
        final double a[] = getMatrix().getA();
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], expected[i]);
        }
    }

    /**
     * Can set the array of values inside the matrix.
     */
    @Test
    public void testSetA() {
        final double expected[] = getMatrix().getA();
        final Matrix33d m = new Matrix33d();
        m.setA(Arrays.copyOf(expected, expected.length));
        for (int i = 0; i < expected.length; i++) {
            assertEquals(m.getA()[i], expected[i]);
        }
    }

    /**
     * Can set a matrix to the identity matrix values.
     */
    @Test
    public void testIdentity() {
        final double junk[] = new double[Matrix33d.LENGTH];
        for (int i = 0; i < Matrix33d.LENGTH; i++) {
            junk[i] = 1089.1451D;
        }
        final Matrix33d m = new Matrix33d();
        m.setA(junk);

        m.identity();
        final double a[] = m.getA();
        for (int i = 0; i < a.length; i++) {
            if (i == 0 || i == 4 || i == 8) {
                assertEquals(a[i], 1D);
            } else {
                assertEquals(a[i], 0D);
            }
        }
    }

    /**
     * Can get a matrix column.
     */
    @Test
    public void testGetMatrixColumn() {
        final Matrix33d m = getMatrix();
        final Vector3d expected = V2;
        final Vector3d v = new Vector3d();
        m.getMatrixColumn(v, 1);
        assertEquals(v.toString(), expected.toString());
    }

    /**
     * Can set the values in a matrix column from a 3 value vector.
     */
    @Test
    public void testSetMatrixColumn() {
        final Matrix33d mBase = getMatrix();
        final Matrix33d m = getMatrix();
        final Vector3d v = new Vector3d(D10, D11, D12);

        m.setMatrixColumn(v, 1);
        for (int i = 0; i < m.getA().length; i++) {
            if (i > 2 && i < 6) {
                // compare the row set to the vector values
                int vectorIndex = i - 3;
                assertEquals(m.getA()[i], v.a[vectorIndex]);
            } else {
                assertEquals(m.getA()[i], mBase.getA()[i]);
            }
        }
    }
}
