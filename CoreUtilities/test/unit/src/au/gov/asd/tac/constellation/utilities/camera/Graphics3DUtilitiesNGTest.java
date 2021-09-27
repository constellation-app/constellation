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

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Graphics3DUtilitiesNGTest {

    private static final Vector3f ZERO_V = new Vector3f(0F, 0F, 0F);
    private static final Vector3f V3_1 = new Vector3f(1.1F, 1.2F, 1.3F);
    private static final Vector3f V3_2 = new Vector3f(2.1F, 2.2F, 2.3F);
    private static final Vector3f V3_3 = new Vector3f(3.1F, 3.2F, 3.3F);

    private static final Vector4f V4_1 = new Vector4f(50.1F, 50.2F, 50.3F, 50.4F);

    private static final Matrix44f M1 = new Matrix44f();
    private static final Matrix44f M2 = new Matrix44f();
    private static final float[] M1_R1 = {101F, 102F, 103F, 104F};
    private static final float[] M1_R2 = {201F, 202F, 203F, 204F};
    private static final float[] M1_R3 = {301F, 302F, 303F, 304F};
    private static final float[] M1_R4 = {401F, 402F, 403F, 404F};
    private static final float[] M2_R1 = {656.79047F, 566.4892F, 138.49348F, 134.44435F};
    private static final float[] M2_R2 = {967.43665F, 267.54318F, 789.1595F, 498.02588F};
    private static final float[] M2_R3 = {460.65683F, 741.18677F, 503.92276F, 77.53038F};
    private static final float[] M2_R4 = {340.8639F, 112.199844F, 861.1616F, 153.07498F};

    @BeforeClass
    public void before() {
        M1.setRow(M1_R1[0], M1_R1[1], M1_R1[2], M1_R1[3], 0);
        M1.setRow(M1_R2[0], M1_R2[1], M1_R2[2], M1_R2[3], 1);
        M1.setRow(M1_R3[0], M1_R3[1], M1_R3[2], M1_R3[3], 2);
        M1.setRow(M1_R4[0], M1_R4[1], M1_R4[2], M1_R4[3], 3);
        M2.setRow(M2_R1[0], M2_R1[1], M2_R1[2], M2_R1[3], 0);
        M2.setRow(M2_R2[0], M2_R2[1], M2_R2[2], M2_R2[3], 1);
        M2.setRow(M2_R3[0], M2_R3[1], M2_R3[2], M2_R3[3], 2);
        M2.setRow(M2_R4[0], M2_R4[1], M2_R4[2], M2_R4[3], 3);
    }

    // convenience method to get a copy of a Matrix, as it's clunky
    private Matrix44f copyMatrix(Matrix44f m) {
        Matrix44f ret = new Matrix44f();
        ret.set(m);
        return ret;
    }

    // convenience method to get a copy of a Vector4f, as it's clunky
    private Vector4f copyVector4f(Vector4f v) {
        Vector4f ret = new Vector4f();
        ret.set(v);
        return ret;
    }

    /**
     * Can convert the supplied point from screen coordinates to world
     * coordinates.
     */
    @Test
    public void testScreenToWorldCoordinates() {
        final int[] viewport = {7, 3, 4, 14};
        final Vector3f worldPosition = new Vector3f(V3_3);
        Graphics3DUtilities.screenToWorldCoordinates(
                new Vector3f(V3_1),
                new Vector3f(V3_2),
                copyMatrix(M2),
                viewport,
                worldPosition);
        assertEquals(worldPosition.toString(),
                new Vector3f(-51.814575F, 19.934322F, 37.437538F).toString());
    }

    /**
     * Can get a model view matrix.
     */
    @Test
    public void testGetModelViewMatrix() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(0.010413259F, -0.01803626F, -0.5773502F, 0.0F, 0);
        expected.setRow(-0.020826489F, -1.7695129E-8F, -0.57735026F, 0.0F, 1);
        expected.setRow(0.010413229F, 0.018036276F, -0.57735026F, 0.0F, 2);
        expected.setRow(5.5879354E-9F, -0.0036072508F, 2.078461F, 1.0F, 3);

        // using the main method
        final Matrix44f m = copyMatrix(M1);
        Graphics3DUtilities.getModelViewMatrix(
                new Vector3f(V3_1), new Vector3f(V3_2), new Vector3f(V3_3), m);
        assertEquals(m.toString(), expected.toString());

        // using the convenience method passing in a Camera object
        final Camera c = new Camera();
        c.lookAtEye.set(V3_1);
        c.lookAtCentre.set(V3_2);
        c.lookAtUp.set(V3_3);
        final Matrix44f m2 = Graphics3DUtilities.getModelViewMatrix(c);
        assertEquals(m2.toString(), expected.toString());
    }

    /**
     * Can project a position to the viewing plane.
     */
    @Test
    public void testProject() {
        final Vector4f v = new Vector4f();
        assertTrue(Graphics3DUtilities.project(
                new Vector3f(V3_1), copyMatrix(M1), new int[]{7, 3, 4, 14}, v));
        assertEquals(v.toString(),
                new Vector4f(10.976173F, 16.944405F, 0.998015F, 1158.400024F).toString());
    }

    /**
     * Unsuccessful projection.
     */
    @Test
    public void testUnsuccessfulProject() {
        final Matrix44f zeroMatrix = new Matrix44f();
        for (int i = 0; i < 4; i++) {
            zeroMatrix.setRow(0, 0, 0, 0, i);
        }
        final Vector4f v = copyVector4f(V4_1);
        assertFalse(Graphics3DUtilities.project(
                new Vector3f(ZERO_V),
                zeroMatrix,
                new int[]{0, 0, 0, 0},
                new Vector4f()));
        assertEquals(v.toString(), V4_1.toString());
    }

    /**
     * Can un-project a position.
     */
    @Test
    public void testUnproject() {
        final Vector3f v = new Vector3f();
        assertTrue(Graphics3DUtilities.unproject(
                copyVector4f(V4_1), copyMatrix(M2), new int[]{7, 3, 4, 14}, v));
        assertEquals(v.toString(),
                new Vector3f(2.996596F, -3.345851F, -2.156693F).toString());
    }

    /**
     * Can move a position in window coordinates.
     */
    @Test
    public void testMoveByProjection() {
        final Vector3f newPosition = new Vector3f(V3_3);
        Graphics3DUtilities.moveByProjection(
                new Vector3f(V3_1),
                copyMatrix(M2),
                new int[]{7, 3, 4, 14},
                new Vector3f(V3_2),
                newPosition);
        assertEquals(newPosition.toString(),
                new Vector3f(4.897831F, -3.934850F, -4.368528F).toString());
    }

    /**
     * Can move a position in window coordinates.
     */
    @Test
    public void testMoveByProjectionUnprojection() {
        final Vector3f newPosition = new Vector3f(V3_3);
        assertTrue(Graphics3DUtilities.moveByProjection(
                new Vector3f(V3_1),
                copyMatrix(M2),
                new int[]{7, 3, 4, 14},
                15,
                102,
                newPosition));
        assertEquals(newPosition.toString(),
                new Vector3f(11.809882F, -2.461684F, -9.804049F).toString());
    }

    /**
     * Can move a position in window coordinates.
     */
    @Test
    public void testUnsuccessfulMoveByProjectionUnprojection() {
        final Matrix44f zeroMatrix = new Matrix44f();
        for (int i = 0; i < 4; i++) {
            zeroMatrix.setRow(0, 0, 0, 0, i);
        }
        final Vector3f newPosition = new Vector3f(V3_3);
        assertFalse(Graphics3DUtilities.moveByProjection(
                new Vector3f(ZERO_V),
                zeroMatrix,
                new int[]{0, 0, 0, 0},
                0,
                0,
                newPosition));
        assertEquals(newPosition.toString(), V3_3.toString());
    }

    /**
     * Can get a vector containing the mix of the two input vectors
     */
    @Test
    public void testMixVector() {
        assertEquals(
                Graphics3DUtilities.mix(new Vector3f(V3_1), new Vector3f(V3_2), 155.597F).toString(),
                new Vector3f(156.696991F, 156.797012F, 156.897018F).toString());
    }

    /**
     * Can get the mix of two floats.
     */
    @Test
    public void testMixFloat() {
        assertEquals(
                Graphics3DUtilities.mix(-53.213F, 13.4F, 155.597F),
                10311.57F);
    }

    /**
     * Can clamp an integer between a min and a max value.
     */
    @Test
    public void testClampInt() {
        // value between min and max
        assertEquals(Graphics3DUtilities.clamp(7, 1, 10), 7);
        // value less than min
        assertEquals(Graphics3DUtilities.clamp(2, 8, 11), 8);
        // value more than max
        assertEquals(Graphics3DUtilities.clamp(3, 12, 9), 9);
    }

    /**
     * Can clamp a float between a min and a max value.
     */
    @Test
    public void testClampFloat() {
        // value between min and max
        assertEquals(Graphics3DUtilities.clamp(7F, 1F, 10F), 7F);
        // value less than min
        assertEquals(Graphics3DUtilities.clamp(2F, 8F, 11F), 8F);
        // value more than max
        assertEquals(Graphics3DUtilities.clamp(3F, 12F, 9F), 9F);
    }

    /**
     * Can calculate the distance between vectors.
     */
    @Test
    public void testDistance() {
        assertEquals(
                Graphics3DUtilities.distance(new Vector3f(V3_1), new Vector3f(V3_3)),
                3.4641016F);
    }
}
