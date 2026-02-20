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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Vector4dNGTest {

    private static final double D1 = 123D;
    private static final double D2 = 321D;
    private static final double D3 = 456D;
    private static final double D4 = 654D;
    private static final double D5 = 789D;
    private static final double D6 = 987D;
    private static final double D7 = 191D;
    private static final double D8 = 828D;
    private static final double D9 = 373D;
    private static final double D10 = 101.1D;
    private static final double D11 = 102.2D;
    private static final double D12 = 103.3D;

    /**
     * Can create a new Vector4d.
     */
    @Test
    public void testConstructor() {
        final Vector4d v = new Vector4d();
        for (Double d : v.getA()) {
            assertEquals(d, 0D);
        }
    }

    /**
     * Can set the values of a vector.
     */
    @Test
    public void testSet() {
        final Vector4d v = new Vector4d();

        // set with double values
        v.set(D1, D2, D3, D4);
        assertEquals(v.getA(), new double[]{D1, D2, D3, D4});

        // set equal to another vector
        final Vector4d v2 = new Vector4d();
        v2.set(D5, D6, D7, D8);
        v.set(v2);
        assertEquals(v.getA(), new double[]{D5, D6, D7, D8});

        // set with an array of doubles
        v.setA(new double[]{D9, D10, D11, D12});
        assertEquals(v.getA(), new double[]{D9, D10, D11, D12});
    }

    /**
     * Can scale a vector.
     */
    @Test
    public void testScale() {
        final Vector4d v = new Vector4d();
        v.set(D1, D2, D3, D4);
        v.scale(0.123F);
        assertEquals(v.getA(), new double[]{
            15.129000417888165D,
            39.483001090586185D,
            56.08800154924393D,
            80.44200222194195D});
    }

    /**
     * Can add two vectors together.
     */
    @Test
    public void testAdd() {
        final Vector4d v = new Vector4d();
        final Vector4d vAdd1 = new Vector4d();
        vAdd1.set(D7, D8, D9, D10);
        final Vector4d vAdd2 = new Vector4d();
        vAdd2.set(D5, D6, D4, D1);

        Vector4d.add(v, vAdd1, vAdd2);
        assertEquals(v.getA(), new double[]{980D, 1815D, 1027D, 224.1D});
    }

    /**
     * Can subtract one vector from another.
     */
    @Test
    public void testSubtract() {
        final Vector4d v = new Vector4d();
        final Vector4d vSub1 = new Vector4d();
        vSub1.set(D3, D4, D1, D2);
        final Vector4d vSub2 = new Vector4d();
        vSub2.set(D9, D7, D8, D10);

        Vector4d.subtract(v, vSub1, vSub2);
        assertEquals(v.getA(), new double[]{83D, 463D, -705D, 219.9D});
    }

    /**
     * Can copy a vector.
     */
    @Test
    public void testMatrixColumn() {
        final Vector4d v = new Vector4d();
        v.set(D1, D2, D3, D4);

        final Vector4d vResult = new Vector4d();
        v.getMatrixColumn(vResult, 0);
        assertEquals(vResult.getA(), v.getA());

        vResult.set(new Vector4d());
        v.getMatrixColumn(vResult, 0);
        assertEquals(vResult.getA(), v.getA());
    }
}
