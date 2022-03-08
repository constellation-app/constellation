/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

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
 * @author serpens24
 */
public class LayerNameNGTest {
    
    final String name = "layer-name";
    final int layer = 23;
    
    public LayerNameNGTest() {
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
     * Test of getLayer method, of class LayerName.
     */
    @Test
    public void testGetLayer() {
        LayerName layerName = new LayerName(layer, name);
        assertEquals(layerName.getLayer(), layer);
    }

    /**
     * Test of getName method, of class LayerName.
     */
    @Test
    public void testGetName() {
        LayerName layerName = new LayerName(layer, name);
        assertEquals(layerName.getName(), name);
    }

    /**
     * Test of toString method, of class LayerName.
     */
    @Test
    public void testToString() {
        LayerName layerName = new LayerName(layer, name);
        assertEquals(layerName.toString(), name);
    }

    /**
     * Test of compareTo method, of class LayerName.
     */
    @Test
    public void testCompareTo() {
        LayerName layerName = new LayerName(layer, name);
        assertEquals(layerName.compareTo(new LayerName(layer, null)), 0);
        assertEquals(layerName.compareTo(new LayerName(layer + 1, null)), -1);
        assertEquals(layerName.compareTo(new LayerName(layer - 1, null)), 1);
    }

    /**
     * Test of hashCode method, of class LayerName.
     */
    @Test
    public void testHashCode() {
        LayerName layerName = new LayerName(layer, name);
        assertEquals(layerName.hashCode(), -1259632285);
    }

    /**
     * Test of equals method, of class LayerName.
     */
    @Test
    public void testEquals() {
        LayerName layerName = new LayerName(layer, name);
        assertFalse(layerName.equals(null));
        assertFalse(layerName.equals(new String()));
        assertFalse(layerName.equals(new LayerName(layer, null)));
        assertTrue(layerName.equals(new LayerName(layer, name)));
        assertFalse(layerName.equals(new LayerName(layer + 1, name)));
    } 
}
