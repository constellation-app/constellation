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

import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Vector3fNGTest {

    private static final float F1 = 123F;
    private static final float F2 = 321F;
    private static final float F3 = 456F;
    private static final float F4 = 654F;
    private static final float F5 = 789F;
    private static final float F6 = 987F;
    private static final float F7 = 191F;
    private static final float F8 = 828F;
    private static final float F9 = 373F;
    private static final float F10 = 101.1F;
    private static final float F11 = 202.2F;
    private static final float F12 = 303.3F;

    // convenience method to get a new Matrix33f
    private Matrix33f getMatrix33f() {
        final Matrix33f m = new Matrix33f();
        m.setMatrixColumn(new Vector3f(F1, F2, F3), 0);
        m.setMatrixColumn(new Vector3f(F4, F5, F6), 1);
        m.setMatrixColumn(new Vector3f(F7, F8, F9), 2);
        return m;
    }

    // convenience method to get a new Matrix44f
    private Matrix44f getMatrix44f() {
        final Matrix44f m = new Matrix44f();
        m.setRow(101F, 102F, 103F, 104F, 0);
        m.setRow(201F, 202F, 203F, 204F, 1);
        m.setRow(301F, 302F, 303F, 304F, 2);
        m.setRow(401F, 402F, 403F, 404F, 3);
        return m;
    }

    /**
     * Can create a new Vector3f.
     */
    @Test
    public void testConstructor() {
        // empty constructor
        final Vector3f vEmpty = new Vector3f();
        for (Float f : vEmpty.a) {
            assertEquals(f, 0F);
        }

        // constuct with float values
        final Vector3f vFloat = new Vector3f(F1, F2, F3);
        assertEquals(vFloat.a, new float[]{F1, F2, F3});

        // construct by copying a Vector3f parameter
        final Vector3f vCopy1 = new Vector3f(F2, F3, F1);
        final Vector3f vCopy2 = new Vector3f(vCopy1);
        assertEquals(vCopy1.a, vCopy2.a);

        // construct with the first three values of a Vector4f parameter
        final Vector4f v4f = new Vector4f(F3, F1, F2, F4);
        final Vector3f v3f = new Vector3f(v4f);
        assertEquals(v3f.a, Arrays.copyOfRange(v4f.a, 0, 3));
    }

    /**
     * Can add two vectors.
     */
    @Test
    public void testAdd() {
        // add to an existing vector
        final Vector3f v = new Vector3f(F1, F2, F3);
        v.add(new Vector3f(F4, F5, F6));
        assertEquals(v.a, new float[]{777F, 1110F, 1443F});

        // add two vectors together and return a new vector
        final Vector3f v2 = Vector3f.add(
                new Vector3f(F7, F8, F9),
                new Vector3f(F5, F6, F4));
        assertEquals(v2.a, new float[]{980F, 1815F, 1027F});
    }

    /**
     * Can set the values of a vector.
     */
    @Test
    public void testSet() {
        final Vector3f v = new Vector3f();

        // set with float values
        v.set(F1, F2, F3);
        assertEquals(v.a, new float[]{F1, F2, F3});

        // set equal to another vector
        v.set(new Vector3f(F4, F5, F6));
        assertEquals(v.a, new float[]{F4, F5, F6});
    }

    /**
     * Can rotate a vector.
     */
    @Test
    public void testRotate() {
        // rotate a vector
        final Vector3f v1 = new Vector3f(F1, F2, F3);
        v1.rotate(getMatrix33f());
        assertEquals(v1.a, new float[]{312159F, 670320F, 543003F});

        // rotate a vector and store the result in a different vector
        final Vector3f v2 = new Vector3f();
        v2.rotate(new Vector3f(F10, F11, F12), getMatrix33f());
        assertEquals(v2.a, new float[]{202604.39F, 443121.28F, 358803.88F});
    }

    /**
     * Can scale a vector.
     */
    @Test
    public void testScale() {
        final Vector3f v = new Vector3f(F1, F2, F3);
        v.scale(0.123F);
        assertEquals(v.a, new float[]{15.129001F, 39.483F, 56.088F});
    }

    /**
     * Can transform a vector.
     */
    @Test
    public void testTransform() {
        final Vector3f v = new Vector3f();
        v.transform(new Vector3f(F1, F2, F3), getMatrix44f());
        assertEquals(v.a, new float[]{214601F, 215502F, 216403F});
    }

    /**
     * Can subtract a vector from another vector.
     */
    @Test
    public void testSubtract() {
        // subtract from an existing vector
        final Vector3f v = new Vector3f(F2, F1, F3);
        v.subtract(new Vector3f(F5, F6, F4));
        assertEquals(v.a, new float[]{-468F, -864F, -198F});

        // subtract one vector from another and return a new vector
        final Vector3f v2 = Vector3f.subtract(
                new Vector3f(F7, F8, F9),
                new Vector3f(F4, F5, F6));
        assertEquals(v2.a, new float[]{-463F, 39F, -614F});

        // subtract one vector from another and put the result in a third vector
        final Vector3f v3result = new Vector3f();
        Vector3f.subtract(
                v3result,
                new Vector3f(F3, F4, F1),
                new Vector3f(F9, F7, F8));
        assertEquals(v3result.a, new float[]{83F, 463F, -705F});
    }

    /**
     * Can get the cross-product of two vectors.
     */
    @Test
    public void testCrossProduct() {
        final Vector3f v = new Vector3f();
        v.crossProduct(
                new Vector3f(F1, F2, F3),
                new Vector3f(F4, F5, F6));
        assertEquals(v.a, new float[]{-42957F, 176823F, -112887F});
    }

    /**
     * Can calculate the dot product of two vectors.
     */
    @Test
    public void testDotProduct() {
        final float f = Vector3f.dotProduct(
                new Vector3f(F1, F2, F3),
                new Vector3f(F4, F5, F6));
        assertEquals(f, 783783F);
    }

    /**
     * Can change a vector by applying a convex combine with operation.
     */
    @Test
    public void testConvexCombineWith() {
        final Vector3f v = new Vector3f(F1, F2, F3);
        v.convexCombineWith(new Vector3f(F6, F5, F4), 0.345F);
        assertEquals(v.a, new float[]{421.08F, 482.45996F, 524.31F});
    }

    /**
     * Can get the angle between two vectors.
     */
    @Test
    public void testAngleBetweenVectors() {
        final float f = Vector3f.angleBetweenVectors(
                new Vector3f(0.12F, 0.34F, 0.56F),
                new Vector3f(0.78F, 0.99F, 1.01F));
        assertEquals(f, 0.091683425F);
    }

    /**
     * Can get the length squared of a vector.
     */
    @Test
    public void testLengthSquared() {
        final float f = new Vector3f(0.12F, 0.34F, 0.56F).getLengthSquared();
        assertEquals(f, 0.4436F);
    }

    /**
     * Can get the length of a vector.
     */
    @Test
    public void testLength() {
        final float f = new Vector3f(0.12F, 0.34F, 0.56F).getLength();
        assertEquals(f, 0.666033F);
    }

    /**
     * Can get the normalised length of the vector.
     */
    @Test
    public void testNormalise() {
        // normalising an empty vector gives zero, and the vector is not scaled
        final Vector3f v1 = new Vector3f();
        final float f1 = v1.normalize();
        assertEquals(f1, 0F);
        assertEquals(v1.a, new Vector3f().a);

        // normalise a full vector, which also scales the vector
        final Vector3f v2 = new Vector3f(0.12F, 0.34F, 0.56F);
        final float f2 = v2.normalize();
        assertEquals(f2, 0.666033F);
        assertEquals(v2.a, new float[]{0.18017124F, 0.51048523F, 0.84079915F});
    }

    /**
     * Can find the normal from three vectors.
     */
    @Test
    public void testFindNormal() {
        final Vector3f v1 = new Vector3f(F1, F2, F3);
        final Vector3f v2 = new Vector3f(F4, F5, F6);
        final Vector3f v3 = new Vector3f(F7, F8, F9);

        final Vector3f v = new Vector3f();
        v.findNormal(v1, v2, v3);
        assertEquals(v.a, new float[]{-308061F, 80181F, 237393F});
    }

    /**
     * Can create an array of vectors.
     */
    @Test
    public void testCreateArray() {
        final Vector3f empty = new Vector3f();
        final int length = 3;
        final Vector3f[] vectors = Vector3f.createArray(length);
        assertEquals(vectors.length, 3);
        for (Vector3f v : vectors) {
            assertEquals(v.a, empty.a);
        }
    }

    /**
     * Can test if the vector is zero.
     */
    @Test
    public void testIsZero() {
        final Vector3f v = new Vector3f();

        // all points zero
        assertTrue(v.isZero());

        // x is not zero
        v.a[0] = 1F;
        assertFalse(v.isZero());

        // y is not zero
        v.a[0] = 0F;
        v.a[1] = 1F;
        assertFalse(v.isZero());

        // z is not zero
        v.a[1] = 0F;
        v.a[2] = 1F;
        assertFalse(v.isZero());

        // set all back to zero again
        v.a[2] = 0F;
        assertTrue(v.isZero());
    }

    /**
     * Can get and set x, y and z values.
     */
    @Test
    public void testGettersSetters() {
        final Vector3f v = new Vector3f();

        // x
        v.setX(F1);
        assertEquals(v.getX(), F1);
        assertEquals(v.getR(), F1);

        // x
        v.setY(F2);
        assertEquals(v.getY(), F2);
        assertEquals(v.getG(), F2);

        // z
        v.setZ(F3);
        assertEquals(v.getZ(), F3);
        assertEquals(v.getB(), F3);
    }

    /**
     * Can test if the vector is valid.
     */
    @Test
    public void testIsValid() {
        final Vector3f v = new Vector3f();

        // all points are valid floats
        assertTrue(v.isValid());

        // x is not valid
        v.a[0] = Float.NaN;
        assertFalse(v.isValid());

        // y is not zero
        v.a[0] = 0F;
        v.a[1] = Float.NEGATIVE_INFINITY;
        assertFalse(v.isZero());

        // z is not zero
        v.a[1] = 0F;
        v.a[2] = Float.POSITIVE_INFINITY;
        assertFalse(v.isZero());

        // set all back to zero again
        v.a[2] = 0F;
        assertTrue(v.isZero());
    }
    
    @Test
    public void testAreSame() {
        assertTrue(new Vector3f(F1, F2, F3).areSame(new Vector3f(F1, F2, F3)));
        assertFalse(new Vector3f(F2, F2, F3).areSame(new Vector3f(F1, F2, F3)));
        assertFalse(new Vector3f(F1, F1, F3).areSame(new Vector3f(F1, F2, F3)));
        assertFalse(new Vector3f(F1, F2, F1).areSame(new Vector3f(F1, F2, F3)));
    }

    /**
     * Can get a string representation of a vector.
     */
    @Test
    public void testToString() {
        assertEquals(
                new Vector3f(F1, F2, F3).toString(),
                "3f[123.000000,321.000000,456.000000]");
    }
}
