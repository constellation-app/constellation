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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class Vector2fNGTest extends ConstellationTest {

    private static final float F1 = 123F;
    private static final float F2 = 321F;
    private static final float F3 = 456F;
    private static final float F4 = 654F;
    private static final float F5 = 789F;
    private static final float F6 = 987F;
    private static final float F7 = 191F;
    private static final float F8 = 828F;
    private static final float F9 = 373F;

    /**
     * Can create a new Vector2f.
     */
    @Test
    public void testConstructor() {
        final Vector2f v = new Vector2f();
        for (Float f : v.a) {
            assertEquals(f, 0F);
        }
    }

    /**
     * Can set the values of a vector.
     */
    @Test
    public void testSet() {
        final Vector2f v = new Vector2f();

        // set with float values
        v.set(F1, F2);
        assertEquals(v.a, new float[]{F1, F2});

        // set equal to another vector
        final Vector2f v2 = new Vector2f();
        v2.set(F4, F5);
        v.set(v2);
        assertEquals(v.a, new float[]{F4, F5});
    }

    /**
     * Can scale a vector.
     */
    @Test
    public void testScale() {
        final Vector2f v = new Vector2f();
        v.set(F1, F2);
        v.scale(0.123F);
        assertEquals(v.a, new float[]{15.129001F, 39.483F});
    }

    /**
     * Can add two vectors.
     */
    @Test
    public void testAdd() {
        final Vector2f v = new Vector2f();
        final Vector2f vAdd1 = new Vector2f();
        vAdd1.set(F7, F8);
        final Vector2f vAdd2 = new Vector2f();
        vAdd2.set(F5, F6);

        Vector2f.add(v, vAdd1, vAdd2);
        assertEquals(v.a, new float[]{980F, 1815F});
    }

    /**
     * Can subtract a vector from another vector.
     */
    @Test
    public void testSubtract() {
        final Vector2f v = new Vector2f();
        final Vector2f vSub1 = new Vector2f();
        vSub1.set(F3, F4);
        final Vector2f vSub2 = new Vector2f();
        vSub2.set(F9, F7);

        Vector2f.subtract(v, vSub1, vSub2);
        assertEquals(v.a, new float[]{83F, 463F});
    }

    /**
     * Can create an array of vectors.
     */
    @Test
    public void testCreateArray() {
        final Vector2f empty = new Vector2f();
        final int length = 2;
        final Vector2f[] vectors = Vector2f.createArray(length);
        assertEquals(vectors.length, length);
        for (Vector2f v : vectors) {
            assertEquals(v.a, empty.a);
        }
    }
}
