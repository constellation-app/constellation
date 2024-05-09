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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Vector3dNGTest {

    private static final double D1 = 123D;
    private static final double D2 = 321D;
    private static final double D3 = 456D;
    private static final double D4 = 654D;
    private static final double D5 = 789D;
    private static final double D6 = 987D;
    private static final double D7 = 191D;
    private static final double D8 = 828D;
    private static final double D9 = 373D;

    /**
     * Can create a new Vector3d.
     */
    @Test
    public void testConstructor() {
        // empty constructor
        final Vector3d vEmpty = new Vector3d();
        for (Double d : vEmpty.a) {
            assertEquals(d, 0D);
        }

        // constuct with double values
        final Vector3d vDouble = new Vector3d(D1, D2, D3);
        assertEquals(vDouble.a, new double[]{D1, D2, D3});

        // construct by copying a Vector3d parameter
        final Vector3d vCopy1 = new Vector3d(D2, D3, D1);
        final Vector3d vCopy2 = new Vector3d(vCopy1);
        assertEquals(vCopy1.a, vCopy2.a);
    }

    /**
     * Can add two vectors.
     */
    @Test
    public void testAdd() {
        // add to an existing vector
        final Vector3d v = new Vector3d(D1, D2, D3);
        v.add(new Vector3d(D4, D5, D6));
        assertEquals(v.a, new double[]{777D, 1110D, 1443D});

        // add two vectors together and return a new vector
        final Vector3d v2 = Vector3d.add(
                new Vector3d(D7, D8, D9),
                new Vector3d(D5, D6, D4));
        assertEquals(v2.a, new double[]{980D, 1815D, 1027D});
    }

    /**
     * Can set the values of a vector.
     */
    @Test
    public void testSet() {
        final Vector3d v = new Vector3d();

        // set with double values
        v.set(D1, D2, D3);
        assertEquals(v.a, new double[]{D1, D2, D3});

        // set equal to another vector
        v.set(new Vector3d(D4, D5, D6));
        assertEquals(v.a, new double[]{D4, D5, D6});
    }

    /**
     * Can scale a vector.
     */
    @Test
    public void testScale() {
        final Vector3d v = new Vector3d(D1, D2, D3);
        v.scale(0.123D);
        assertEquals(v.a, new double[]{15.129D, 39.483D, 56.088D});
    }

    /**
     * Can multiply a vector by a double.
     */
    @Test
    public void testMultiply() {
        final Vector3d v = Vector3d.multiply(new Vector3d(D1, D2, D3), D4);
        assertEquals(v.a, new double[]{80442D, 209934D, 298224D});
    }

    /**
     * Can get the length squared of a vector.
     */
    @Test
    public void testLengthSquared() {
        final double d = new Vector3d(0.12D, 0.34D, 0.56D).getLengthSquared();
        assertEquals(d, 0.4436000000000001D);
    }

    /**
     * Can get the length of a vector.
     */
    @Test
    public void testLength() {
        final double d = new Vector3d(0.12D, 0.34D, 0.56D).getLength();
        assertEquals(d, 0.6660330322138686D);
    }

    /**
     * Can get the cross-product of two vectors.
     */
    @Test
    public void testCrossProduct() {
        final Vector3d v = Vector3d.crossProduct(
                new Vector3d(D1, D2, D3),
                new Vector3d(D4, D5, D6));
        assertEquals(v.a, new double[]{-42957D, 176823D, -112887D});
    }

    /**
     * Can get the normalised length of the vector.
     */
    @Test
    public void testNormalise() {
        // normalising an empty vector gives zero, and the vector is not scaled
        final Vector3d v1 = new Vector3d();
        final double d1 = v1.normalize();
        assertEquals(d1, 0D);
        assertEquals(v1.a, new Vector3d().a);

        // normalise a full vector, which also scales the vector
        final Vector3d v2 = new Vector3d(0.12D, 0.34D, 0.56D);
        final double d2 = v2.normalize();
        assertEquals(d2, 0.6660330322138686D);
        assertEquals(v2.a,
                new double[]{
                    0.18017124406146126D,
                    0.5104851915074736D,
                    0.8407991389534859D});
    }

    /**
     * Can get the magnitude squared of a vector.
     */
    @Test
    public void testMagnitudeSquared() {
        final double d = new Vector3d(0.91D, 0.82D, 0.73D).getMagnitudeSquared();
        assertEquals(d, 2.0334D);
    }

    /**
     * Can get the magnitude of a vector.
     */
    @Test
    public void testMagnitude() {
        final double d = new Vector3d(0.91D, 0.82D, 0.73D).getMagnitude();
        assertEquals(d, 1.4259733517846678D);
    }

    /**
     * Can find the normal from three vectors.
     */
    @Test
    public void testFindNormal() {
        final Vector3d v1 = new Vector3d(D1, D2, D3);
        final Vector3d v2 = new Vector3d(D4, D5, D6);
        final Vector3d v3 = new Vector3d(D7, D8, D9);

        final Vector3d v = new Vector3d();
        v.findNormal(v1, v2, v3);
        assertEquals(v.a, new double[]{-308061D, 80181D, 237393D});
    }

    /**
     * Can subtract a vector from another vector.
     */
    @Test
    public void testSubtract() {
        // subtract one vector from another and return a new vector
        final Vector3d v = Vector3d.subtract(
                new Vector3d(D7, D8, D9),
                new Vector3d(D4, D5, D6));
        assertEquals(v.a, new double[]{-463D, 39D, -614D});
    }

    /**
     * Can calculate the dot product of two vectors.
     */
    @Test
    public void testDotProduct() {
        final double d = Vector3d.dotProduct(
                new Vector3d(D1, D2, D3),
                new Vector3d(D4, D5, D6));
        assertEquals(d, 783783D);
    }

    /**
     * Can get x, y and z values.
     */
    @Test
    public void testGetters() {
        final Vector3d v = new Vector3d(D1, D2, D3);
        assertEquals(v.getX(), D1);
        assertEquals(v.getY(), D2);
        assertEquals(v.getZ(), D3);
    }

    /**
     * Can get a string representation of a vector.
     */
    @Test
    public void testToString() {
        assertEquals(
                new Vector3d(D1, D2, D3).toString(),
                "3d[123.000000,321.000000,456.000000]");
    }

}
