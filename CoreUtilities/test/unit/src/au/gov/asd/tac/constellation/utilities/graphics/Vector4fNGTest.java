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
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Vector4fNGTest {

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
     * Can create a new Vector4f.
     */
    @Test
    public void testConstructor() {
        // empty constructor
        final Vector4f vEmpty = new Vector4f();
        for (Float f : vEmpty.a) {
            assertEquals(f, 0F);
        }

        // constuct with float values
        final Vector4f vFloat = new Vector4f(F1, F2, F3, F4);
        assertEquals(vFloat.a, new float[]{F1, F2, F3, F4});
    }

    /**
     * Can set the values of a vector.
     */
    @Test
    public void testSet() {
        final Vector4f v = new Vector4f();

        // set with float values
        v.set(F1, F2, F3, F4);
        assertEquals(v.a, new float[]{F1, F2, F3, F4});

        // set equal to another vector
        v.set(new Vector4f(F5, F6, F7, F8));
        assertEquals(v.a, new float[]{F5, F6, F7, F8});
    }

    /**
     * Can scale a vector.
     */
    @Test
    public void testScale() {
        final Vector4f v = new Vector4f(F1, F2, F3, F4);
        v.scale(0.123F);
        assertEquals(v.a, new float[]{15.129001F, 39.483F, 56.088F, 80.442F});
    }

    /**
     * Can transform a vector.
     */
    @Test
    public void testTransform() {
        // method where the vector is mutated
        final Vector4f v1 = new Vector4f();
        v1.transform(new Vector4f(F1, F2, F3, F4), getMatrix44f());
        assertEquals(v1.a, new float[]{476454F, 478008F, 479562F, 481116F});

        // method where a new vector is returned
        final Vector4f v2 = new Vector4f(F5, F6, F7, F8).transform(getMatrix44f());
        assertEquals(v2.a, new float[]{667595F, 670390F, 673185F, 675980F});
    }

    /**
     * Can add two vectors together.
     */
    @Test
    public void testAdd() {
        final Vector4f v = new Vector4f();
        Vector4f.add(
                v,
                new Vector4f(F7, F8, F9, F10),
                new Vector4f(F5, F6, F4, F1));
        assertEquals(v.a, new float[]{980F, 1815F, 1027F, 224.1F});
    }

    /**
     * Can subtract one vector from another.
     */
    @Test
    public void testSubtract() {
        final Vector4f v = new Vector4f();
        Vector4f.subtract(
                v,
                new Vector4f(F3, F4, F1, F2),
                new Vector4f(F9, F7, F8, F10));
        assertEquals(v.a, new float[]{83F, 463F, -705F, 219.9F});
    }

    /**
     * Can copy a vector.
     */
    @Test
    public void testMatrixColumn() {
        final Vector4f v = new Vector4f(F1, F2, F3, F4);

        final Vector4f vResult = new Vector4f();
        v.getMatrixColumn(vResult, 0);
        assertEquals(vResult.a, v.a);

        vResult.set(new Vector4f());
        v.getMatrixColumn(vResult, 0);
        assertEquals(vResult.a, v.a);
    }

    /**
     * Can convert Vector4f to Vector3f.
     */
    @Test
    public void testToVector3f() {
        final Vector4f v4f = new Vector4f(F1, F2, F3, F4);
        final Vector3f v3f = v4f.toVector3f();
        assertEquals(v3f.a, Arrays.copyOfRange(v4f.a, 0, 3));
    }

    /**
     * Can get and set x, y, z and w values.
     */
    @Test
    public void testGetters() {
        final Vector4f v = new Vector4f(F1, F2, F3, F4);

        // x
        assertEquals(v.getX(), F1);
        assertEquals(v.getR(), F1);

        // y
        assertEquals(v.getY(), F2);
        assertEquals(v.getG(), F2);

        // z
        assertEquals(v.getZ(), F3);
        assertEquals(v.getB(), F3);

        // w
        assertEquals(v.getW(), F4);
        assertEquals(v.getA(), F4);
    }

    /**
     * Can get a String representation of a Vector4f.
     */
    @Test
    public void testToString() {
        assertEquals(
                new Vector4f(F1, F2, F3, F4).toString(),
                "4f[123.000000,321.000000,456.000000,654.000000]");
    }

}
