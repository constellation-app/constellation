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

import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
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
public class IconAttributeInteractionNGTest {

    public IconAttributeInteractionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getDisplayText method, of class IconAttributeInteraction.
     */
    @Test
    public void testGetDisplayText() {
        System.out.println("getDisplayText");

        final IconAttributeInteraction instance = new IconAttributeInteraction();

        final String nullResult = instance.getDisplayText(null);
        assertNull(nullResult);

        final ConstellationIcon icon = IconManager.getIcon("Github");
        final String namedResult = instance.getDisplayText(icon);
        assertEquals(namedResult, "Internet.Github");
    }

    /**
     * Test of getDisplayNodes method, of class IconAttributeInteraction.
     */
    @Test
    public void testGetDisplayNodes() {
        System.out.println("getDisplayNodes");

        final IconAttributeInteraction instance = new IconAttributeInteraction();
        final ConstellationIcon icon = IconManager.getIcon("Github");

        final List<Node> result1 = instance.getDisplayNodes(icon, -1, 2);
        assertEquals(result1.size(), 1);
        final ImageView result1Node = (ImageView) result1.get(0);
        assertEquals(result1Node.getFitHeight(), 2.0);

        final List<Node> result2 = instance.getDisplayNodes(icon, -1, -1);
        assertEquals(result2.size(), 1);
        final ImageView result2Node = (ImageView) result2.get(0);
        // fit height should be the default node size
        assertEquals(result2Node.getFitHeight(), 50.0);

        final List<Node> result3 = instance.getDisplayNodes(icon, 3, 2);
        assertEquals(result3.size(), 1);
        final ImageView result3Node = (ImageView) result3.get(0);
        assertEquals(result3Node.getFitHeight(), 2.0);
    }
}