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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.interaction;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class BlazeAttributeInteractionNGTest {
    
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
     * Test of getDisplayText method, of class BlazeAttributeInteraction.
     */
    @Test
    public void testGetDisplayText() {
        System.out.println("getDisplayText");

        final BlazeAttributeInteraction instance = new BlazeAttributeInteraction();

        final String nullResult = instance.getDisplayText(null);
        assertNull(nullResult);

        final Blaze blaze = new Blaze(87, ConstellationColor.BANANA);
        final String validResult = instance.getDisplayText(blaze);
        assertEquals(validResult, "Color: Banana; Angle: 87");
    }

    /**
     * Test of getDisplayNodes method, of class BlazeAttributeInteraction.
     */
    @Test
    public void testGetDisplayNodes() {
        System.out.println("getDisplayNodes");

        final BlazeAttributeInteraction instance = new BlazeAttributeInteraction();
        final Blaze blaze = new Blaze(87, ConstellationColor.CYAN);

        final List<Node> result1 = instance.getDisplayNodes(blaze, -1, 2);
        assertEquals(result1.size(), 1);
        final Rectangle result1Node = (Rectangle) result1.get(0);
        assertEquals(result1Node.getFill(), Color.CYAN);
        assertEquals(result1Node.getHeight(), 2.0);
        assertEquals(result1Node.getWidth(), 2.0);

        final List<Node> result2 = instance.getDisplayNodes(blaze, -1, -1);
        assertEquals(result2.size(), 1);
        final Rectangle result2Node = (Rectangle) result2.get(0);
        assertEquals(result2Node.getFill(), Color.CYAN);
        // height and width should be the default node size
        assertEquals(result2Node.getHeight(), 50.0);
        assertEquals(result2Node.getWidth(), 50.0);

        final List<Node> result3 = instance.getDisplayNodes(blaze, 3, 2);
        assertEquals(result3.size(), 1);
        final Rectangle result3Node = (Rectangle) result3.get(0);
        assertEquals(result3Node.getFill(), Color.CYAN);
        assertEquals(result3Node.getHeight(), 2.0);
        assertEquals(result3Node.getWidth(), 3.0);
    }
}