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
package au.gov.asd.tac.constellation.utilities.camera;

import static au.gov.asd.tac.constellation.utilities.camera.BoundingBox.EMPTYBOX_CAMERA_DISTANCE;
import static au.gov.asd.tac.constellation.utilities.camera.BoundingBox.MINIMUM_CAMERA_DISTANCE;
import static au.gov.asd.tac.constellation.utilities.camera.BoundingBox.MINIMUM_SIZE;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class BoundingBoxNGTest {

    private static final Vector3f MIN = new Vector3f(1.1F, 1.2F, 1.3F);
    private static final Vector3f MAX = new Vector3f(9.1F, 9.2F, 9.3F);
    private static final Vector3f MIN2 = new Vector3f(2.1F, 2.2F, 2.3F);
    private static final Vector3f MAX2 = new Vector3f(8.1F, 8.2F, 8.3F);

    /**
     * Can explicitly set the verticies in a BoundingBox.
     */
    @Test
    public void testSet() {
        final BoundingBox bb = new BoundingBox();

        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.getMin().toString(), MIN.toString());
        assertEquals(bb.getMax().toString(), MAX.toString());
        assertEquals(bb.getMin2().toString(), MIN2.toString());
        assertEquals(bb.getMax2().toString(), MAX2.toString());

        // Set verticies again and check that the change was applied.
        bb.set(new Vector3f(MIN2), new Vector3f(MAX2), new Vector3f(MIN), new Vector3f(MAX));
        assertEquals(bb.getMin().toString(), MIN2.toString());
        assertEquals(bb.getMax().toString(), MAX2.toString());
        assertEquals(bb.getMin2().toString(), MIN.toString());
        assertEquals(bb.getMax2().toString(), MAX.toString());
    }

    /**
     * Can copy a BoundingBox.
     */
    @Test
    public void testCopy() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));

        final BoundingBox bb2 = bb.copy();

        assertEquals(bb2.getMin().toString(), MIN.toString());
        assertEquals(bb2.getMax().toString(), MAX.toString());
        assertEquals(bb2.getMin2().toString(), MIN2.toString());
        assertEquals(bb2.getMax2().toString(), MAX2.toString());
        assertFalse(bb2.isEmpty());
    }

    /**
     * Can create a BoundingBox from another BoundingBox.
     */
    @Test
    public void testConstructor() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));

        final BoundingBox bb2 = new BoundingBox(bb);

        assertEquals(bb2.getMin().toString(), MIN.toString());
        assertEquals(bb2.getMax().toString(), MAX.toString());
        assertEquals(bb2.getMin2().toString(), MIN2.toString());
        assertEquals(bb2.getMax2().toString(), MAX2.toString());
        assertFalse(bb2.isEmpty());
    }

    private static final Vector3f MIN_EXTREME = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    private static final Vector3f MAX_EXTREME = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

    /**
     * Can create a BoundingBox reset to the min and max of the float datatype.
     */
    @Test
    public void testResetConstructor() {
        final BoundingBox bb = new BoundingBox();
        assertEquals(bb.getMin().toString(), MIN_EXTREME.toString());
        assertEquals(bb.getMax().toString(), MAX_EXTREME.toString());
        assertEquals(bb.getMin2().toString(), MIN_EXTREME.toString());
        assertEquals(bb.getMax2().toString(), MAX_EXTREME.toString());
        assertTrue(bb.isEmpty());
    }

    /**
     * Can reset a BoundingBox to the min and max of the float datatype.
     */
    @Test
    public void testReset() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.resetMinMax();

        assertEquals(bb.getMin().toString(), MIN_EXTREME.toString());
        assertEquals(bb.getMax().toString(), MAX_EXTREME.toString());
        assertEquals(bb.getMin2().toString(), MIN_EXTREME.toString());
        assertEquals(bb.getMax2().toString(), MAX_EXTREME.toString());
        assertFalse(bb.isEmpty());
    }

    private static final float BIGGER_FLOAT1 = 10.1F;
    private static final float BIGGER_FLOAT2 = 10.2F;
    private static final float BIGGER_FLOAT3 = 10.3F;
    private static final float SMALLER_FLOAT1 = 0.1F;
    private static final float SMALLER_FLOAT2 = 0.2F;
    private static final float SMALLER_FLOAT3 = 0.3F;
    private static final float BIGISH_FLOAT = 9.21F;
    private static final float SMALLISH_FLOAT = 1.21F;

    /**
     * Can add a vertex to a BoundingBox.
     */
    @Test
    public void testAddVertex() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));

        // Change all elements in the max vector
        bb.addVertex(BIGGER_FLOAT1, BIGGER_FLOAT2, BIGGER_FLOAT3);
        assertEquals(bb.getMin().toString(), MIN.toString());
        assertEquals(bb.getMax().toString(),
                String.format("3f[%f,%f,%f]", BIGGER_FLOAT1, BIGGER_FLOAT2, BIGGER_FLOAT3));

        // Change all elements in the min vector
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.addVertex(SMALLER_FLOAT1, SMALLER_FLOAT2, SMALLER_FLOAT3);
        assertEquals(bb.getMin().toString(),
                String.format("3f[%f,%f,%f]", SMALLER_FLOAT1, SMALLER_FLOAT2, SMALLER_FLOAT3));
        assertEquals(bb.getMax().toString(), MAX.toString());

        // Change some elements in the max vector
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.addVertex(BIGISH_FLOAT, BIGISH_FLOAT, BIGISH_FLOAT);
        assertEquals(bb.getMin().toString(), MIN.toString());
        assertEquals(bb.getMax().toString(),
                String.format("3f[%f,%f,%f]", BIGISH_FLOAT, BIGISH_FLOAT, MAX.getZ()));

        // Change some elements in the min vector
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.addVertex(SMALLISH_FLOAT, SMALLISH_FLOAT, SMALLISH_FLOAT);
        assertEquals(bb.getMin().toString(),
                String.format("3f[%f,%f,%f]", MIN.getX(), MIN.getY(), SMALLISH_FLOAT));
        assertEquals(bb.getMax().toString(), MAX.toString());
    }

    private static final float BIGISH_FLOAT2 = 8.21F;
    private static final float SMALLISH_FLOAT2 = 2.21F;

    /**
     * Can add a secondary vertex to a BoundingBox.
     */
    @Test
    public void testAddVertex2() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));

        // Change all elements in the max2 vector
        bb.addVertex2(BIGGER_FLOAT1, BIGGER_FLOAT2, BIGGER_FLOAT3);
        assertEquals(bb.getMin2().toString(), MIN2.toString());
        assertEquals(bb.getMax2().toString(),
                String.format("3f[%f,%f,%f]", BIGGER_FLOAT1, BIGGER_FLOAT2, BIGGER_FLOAT3));

        // Change all elements in the min vector
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.addVertex2(SMALLER_FLOAT1, SMALLER_FLOAT2, SMALLER_FLOAT3);
        assertEquals(bb.getMin2().toString(),
                String.format("3f[%f,%f,%f]", SMALLER_FLOAT1, SMALLER_FLOAT2, SMALLER_FLOAT3));
        assertEquals(bb.getMax2().toString(), MAX2.toString());

        // Change some elements in the max vector
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.addVertex2(BIGISH_FLOAT2, BIGISH_FLOAT2, BIGISH_FLOAT2);
        assertEquals(bb.getMin2().toString(), MIN2.toString());
        assertEquals(bb.getMax2().toString(),
                String.format("3f[%f,%f,%f]", BIGISH_FLOAT2, BIGISH_FLOAT2, MAX2.getZ()));

        // Change some elements in the min vector
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.addVertex2(SMALLISH_FLOAT2, SMALLISH_FLOAT2, SMALLISH_FLOAT2);
        assertEquals(bb.getMin2().toString(),
                String.format("3f[%f,%f,%f]", MIN2.getX(), MIN2.getY(), SMALLISH_FLOAT2));
        assertEquals(bb.getMax2().toString(), MAX2.toString());
    }

    /**
     * Can zero the primary verticies in a BoundingBox.
     */
    @Test
    public void testZero() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.zero();
        assertEquals(bb.getMin().toString(), String.format("3f[%f,%f,%f]", -MINIMUM_SIZE, -MINIMUM_SIZE, -MINIMUM_SIZE));
        assertEquals(bb.getMax().toString(), String.format("3f[%f,%f,%f]", MINIMUM_SIZE, MINIMUM_SIZE, MINIMUM_SIZE));
    }

    /**
     * Can zero the secondary verticies in a BoundingBox.
     */
    @Test
    public void testZeroSecondary() {
        final BoundingBox bb = new BoundingBox();
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb.zero2();
        assertEquals(bb.getMin2().toString(), bb.getMin().toString());
        assertEquals(bb.getMax2().toString(), bb.getMax().toString());
    }

    /**
     * Can return the minimum vertex of the BoundingBox when the x value is less
     * than the maximum vertex x value.
     */
    @Test
    public void testGetMin() {
        final BoundingBox bb = new BoundingBox();

        // Can return
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.getBoundingBoxMinimum().toString(), MIN.toString());

        // Can't return
        bb.set(new Vector3f(MAX), new Vector3f(MIN), new Vector3f(MAX2), new Vector3f(MIN2));
        assertNull(bb.getBoundingBoxMinimum());
    }

    /**
     * Can return the maximum vertex of the BoundingBox when the x value is
     * greater than the minimum vertex x value.
     */
    @Test
    public void testGetMax() {
        final BoundingBox bb = new BoundingBox();

        // Can return
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.getBoundingBoxMaximum().toString(), MAX.toString());

        // Can't return
        bb.set(new Vector3f(MAX), new Vector3f(MIN), new Vector3f(MAX2), new Vector3f(MIN2));
        assertNull(bb.getBoundingBoxMaximum());
    }

    public static final Vector3f ALL_POINTS_SAME_VECTOR3F
            = new Vector3f(BIGISH_FLOAT, BIGISH_FLOAT, BIGISH_FLOAT);

    private static final float BIGGEST_FLOAT1 = 123.45F;
    private static final float BIGGEST_FLOAT2 = 234.56F;
    private static final float BIGGEST_FLOAT3 = 1345.67F;
    private static final float BIGGEST_FLOAT4 = 2456.78F;
    private static final float BIGGEST_FLOAT5 = 66567.89F;
    private static final float BIGGEST_FLOAT6 = 99678.91F;

    /**
     * Cube radius of the BoundingBox is successfully calculated when x, y or z
     * are the furthest points.
     */
    @Test
    public void testCubeRadius() {
        final BoundingBox bb = new BoundingBox();

        // x is the furthest point
        final Vector3f bigMinX = new Vector3f(ALL_POINTS_SAME_VECTOR3F);
        bigMinX.setX(BIGGEST_FLOAT1);
        Vector3f bigMaxX = new Vector3f(ALL_POINTS_SAME_VECTOR3F);
        bigMaxX.setX(BIGGEST_FLOAT2);
        bb.set(new Vector3f(bigMinX), new Vector3f(bigMaxX), new Vector3f(ALL_POINTS_SAME_VECTOR3F), new Vector3f(ALL_POINTS_SAME_VECTOR3F));
        float f = bb.getCubeRadius();
        assertEquals(f, 55.554993F);

        // y is the furthest point
        Vector3f bigMinY = new Vector3f(ALL_POINTS_SAME_VECTOR3F);
        bigMinY.setY(BIGGEST_FLOAT3);
        Vector3f bigMaxY = new Vector3f(ALL_POINTS_SAME_VECTOR3F);
        bigMaxY.setY(BIGGEST_FLOAT4);
        bb.set(new Vector3f(bigMinY), new Vector3f(bigMaxY), new Vector3f(ALL_POINTS_SAME_VECTOR3F), new Vector3f(ALL_POINTS_SAME_VECTOR3F));
        f = bb.getCubeRadius();
        assertEquals(f, 555.55493F);

        // z is the furthest point
        Vector3f bigMinZ = new Vector3f(ALL_POINTS_SAME_VECTOR3F);
        bigMinZ.setZ(BIGGEST_FLOAT5);
        Vector3f bigMaxZ = new Vector3f(ALL_POINTS_SAME_VECTOR3F);
        bigMaxZ.setZ(BIGGEST_FLOAT6);
        bb.set(new Vector3f(bigMinZ), new Vector3f(bigMaxZ), new Vector3f(ALL_POINTS_SAME_VECTOR3F), new Vector3f(ALL_POINTS_SAME_VECTOR3F));
        f = bb.getCubeRadius();
        assertEquals(f, 16555.508F);
    }

    /**
     * Can't get the cube radius of an empty BoundingBox.
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Bounding box is empty.")
    public void testCubeRadiusEmptyBox() {
        new BoundingBox().getCubeRadius();
    }

    /**
     * Sphere radius of a BoundingBox is successfully calculated unless the
     * vertices are co-positioned.
     */
    @Test
    public void testSphereRadius() {
        final BoundingBox bb = new BoundingBox();

        // different verticies
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.getSphereRadius(14F), 17.320509F);

        // all verticies are at the same point
        bb.set(new Vector3f(MIN), new Vector3f(MIN), new Vector3f(MIN), new Vector3f(MIN));
        assertEquals(bb.getSphereRadius(1F), 1F);
    }

    /**
     * Can't get the sphere radius of an empty BoundingBox.
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Bounding box is empty.")
    public void testSphereRadiusEmptyBox() {
        new BoundingBox().getSphereRadius(0F);
    }

    /**
     * Can get a vector representing the center of the BoundingBox if the
     * BoundingBox isn't empty.
     */
    @Test
    public void testCenter() {
        final BoundingBox bb = new BoundingBox();

        // empty box
        assertEquals(bb.getCentre(14F).toString(), new Vector3f(0F, 0F, 0F).toString());

        // full box
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.getCentre(41F).toString(),
                new Vector3f(5.100006F, 5.199997F, 5.300003F).toString());
    }

    Vector3f MIN_TINY = new Vector3f(0.000011F, 0.000012F, 0.000013F);
    Vector3f MAX_TINY = new Vector3f(0.000091F, 0.000092F, 0.000093F);
    Vector3f MIN2_TINY = new Vector3f(0.000021F, 0.000022F, 0.000023F);
    Vector3f MAX2_TINY = new Vector3f(0.000081F, 0.000082F, 0.000083F);

    /**
     * Can get an appropriate camera distance for viewing the contents of the
     * BoundingBox, unless the box is empty or the distance calculated is too
     * small.
     */
    @Test
    public void testCameraDistance() {
        final BoundingBox bb = new BoundingBox();

        // empty box
        assertEquals(bb.getCameraDistance(1F, 2F), EMPTYBOX_CAMERA_DISTANCE);

        // calculated distance is big enough
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.getCameraDistance(1F, 2F), 396.94675F);

        // calculated distance is too small
        bb.set(new Vector3f(MIN_TINY), new Vector3f(MAX_TINY),
                new Vector3f(MIN2_TINY), new Vector3f(MAX2_TINY));
        assertEquals(bb.getCameraDistance(1F, 2F), MINIMUM_CAMERA_DISTANCE);
    }
    @Test
    public void testAreSame() {
        final BoundingBox bb = new BoundingBox();
        final BoundingBox bb2 = new BoundingBox();
        assertTrue(bb.areSame(bb2));
        assertTrue(bb2.areSame(bb));
        
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        bb2.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertTrue(bb.areSame(bb2));
        assertTrue(bb2.areSame(bb));
        
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(4,4,4));
        assertFalse(bb.areSame(bb2));
        assertFalse(bb2.areSame(bb));
        
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(4,4,4), new Vector3f(MAX2));
        assertFalse(bb.areSame(bb2));
        assertFalse(bb2.areSame(bb));
        
        bb.set(new Vector3f(MIN), new Vector3f(4,4,4), new Vector3f(MIN2), new Vector3f(MAX2));
        assertFalse(bb.areSame(bb2));
        assertFalse(bb2.areSame(bb));
        
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertTrue(bb.areSame(bb2));
        assertTrue(bb2.areSame(bb));
    }
    

    /**
     * Can get a String representation of the BoundingBox.
     */
    @Test
    public void testGetString() {
        final BoundingBox bb = new BoundingBox();

        // empty box
        assertEquals(bb.toString(), "BoundingBox[isEmpty]");

        // full box
        bb.set(new Vector3f(MIN), new Vector3f(MAX), new Vector3f(MIN2), new Vector3f(MAX2));
        assertEquals(bb.toString(), "BoundingBox[min=3f[1.100000,1.200000,1.300000] "
                + "max=3f[9.100000,9.200000,9.300000] centre0=3f[5.100000,5.200000,5.300000] "
                + "centre1=3f[5.100000,5.200000,5.300000] cradius=4.000000 sradius0=6.928203 "
                + "sradius1=5.196152]");
    }
}
