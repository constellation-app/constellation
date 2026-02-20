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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix33f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class InteractiveSelectionUtilitiesNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of lineSegmentIntersectsRectangle method, of class InteractiveSelectionUtilities. An axis of the line's coordinates is completely outside the rectangle's min or max
     */
    @Test
    public void testLineSegmentIntersectsRectangleCoordinatesCompletelyOutside() {
        System.out.println("lineSegmentIntersectsRectangleCoordinatesCompletelyOutside");
        
        // Rectangle has bottom-left (0,0) and top-right (2,2)
        // left of the rectangle
        assertFalse(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(-1F, 0F, -1F, 1F, 0F, 0F, 2F, 2F));
        // right of the rectangle
        assertFalse(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(3F, 0F, 3F, 1F, 0F, 0F, 2F, 2F));
        // below the rectangle
        assertFalse(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(0F, -1F, 1F, -1F, 0F, 0F, 2F, 2F));
        // above of the rectangle
        assertFalse(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(0F, 3F, 1F, 3F, 0F, 0F, 2F, 2F));
    }
    
    /**
     * Test of lineSegmentIntersectsRectangle method, of class InteractiveSelectionUtilities.
     */
    @Test
    public void testLineSegmentIntersectsRectangleCompletelyOutside() {
        System.out.println("lineSegmentIntersectsRectangle");
        
        // Rectangle has bottom-left (0,0) and top-right (2,2)
        assertTrue(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(1F, 0F, 1F, 1F, 0F, 0F, 2F, 2F));
        assertTrue(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(-1F, 0F, 3F, 1F, 0F, 0F, 2F, 2F));
        assertTrue(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(-2F, -3F, 3F, 2F, 0F, 0F, 2F, 2F));
        assertTrue(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(-2F, -3F, 1F, 1F, 0F, 0F, 2F, 2F));
        assertFalse(InteractiveSelectionUtilities.lineSegmentIntersectsRectangle(-4F, -4F, 1F, 1F, 0F, 0F, 2F, 2F));
    }

    /**
     * Test of convertWorldToScene method, of class InteractiveSelectionUtilities.
     */
    @Test
    public void testConvertWorldToScene() {
        System.out.println("convertWorldToScene");
        
        final Matrix33f rotMatrix = new Matrix33f();
        rotMatrix.makeRotationMatrix(0F, 1F, 1F, 1F);
        
        // converting world coords of 0,0,0
        final Vector3f sceneCoords = InteractiveSelectionUtilities.convertWorldToScene(1F, 1F, 1F, new Vector3f(0F, 0F, 0F), rotMatrix, 2);
        
        assertEquals(sceneCoords, new Vector3f(1F, 1F, -1F));
    } 
}
