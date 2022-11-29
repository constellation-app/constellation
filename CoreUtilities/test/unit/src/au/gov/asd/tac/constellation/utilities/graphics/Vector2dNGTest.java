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
public class Vector2dNGTest {

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
     * Can create a new Vector2d.
     */
    @Test
    public void testConstructor() {
        final Vector2d v = new Vector2d();
        for (Double d : v.getA()) {
            assertEquals(d, 0D);
        }
    }

    /**
     * Can set the values of a vector.
     */
    @Test
    public void testSet() {
        final Vector2d v = new Vector2d();

        // set with double values
        v.set(D1, D2);
        assertEquals(v.getA(), new double[]{D1, D2});

        // set equal to another vector
        final Vector2d v2 = new Vector2d();
        v2.set(D4, D5);
        v.set(v2);
        assertEquals(v.getA(), new double[]{D4, D5});
    }

    /**
     * Can scale a vector.
     */
    @Test
    public void testScale() {
        final Vector2d v = new Vector2d();
        v.set(D1, D2);
        v.scale(0.123D);
        assertEquals(v.getA(), new double[]{15.129D, 39.483D});
    }

    /**
     * Can add two vectors.
     */
    @Test
    public void testAdd() {
        final Vector2d v = new Vector2d();
        final Vector2d vAdd1 = new Vector2d();
        vAdd1.set(D7, D8);
        final Vector2d vAdd2 = new Vector2d();
        vAdd2.set(D5, D6);

        Vector2d.add(v, vAdd1, vAdd2);
        assertEquals(v.getA(), new double[]{980D, 1815D});
    }

    /**
     * Can subtract a vector from another vector.
     */
    @Test
    public void testSubtract() {
        final Vector2d v = new Vector2d();
        final Vector2d vSub1 = new Vector2d();
        vSub1.set(D3, D4);
        final Vector2d vSub2 = new Vector2d();
        vSub2.set(D9, D7);

        Vector2d.subtract(v, vSub1, vSub2);
        assertEquals(v.getA(), new double[]{83D, 463D});
    }
}
