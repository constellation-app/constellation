/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.utilities.AttributeMonitor.AttributeTransition;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * unit tests for create/editing/deletion of attribute for elements
 *
 * @author algol
 */
public class AttributeMonitorNGTest {

    public AttributeMonitorNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of addAttribute method, of class StoreGraph.
     */
    @Test
    public void generalLifecycleTest() {

        StoreGraph g = new StoreGraph();

        AttributeMonitor monitor = new AttributeMonitor(GraphElementType.VERTEX, "Attribute");

        // Initial -> UNDEFINED
        assertEquals(monitor.getTransition(), AttributeTransition.UNDEFINED);

        // Initial update -> UNDEFINED_TO_MISSING
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.UNDEFINED_TO_MISSING);

        // Add the attribute -> ADDED
        g.addAttribute(monitor.getElementType(), "integer", monitor.getName(), "Attribute", 0, null);
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.ADDED);

        // Do nothing -> UNCHANGED
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.UNCHANGED);

        // Remove and Add the attribute -> REMOVED_AND_ADDED
        g.removeAttribute(monitor.getId());
        g.addAttribute(monitor.getElementType(), "integer", monitor.getName(), "Attribute", 0, null);
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.REMOVED_AND_ADDED);

        // Set a value -> CHANGED
        int v = g.addVertex();
        g.setIntValue(monitor.getId(), v, 100);
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.CHANGED);

        // Do nothing -> UNCHANGED
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.UNCHANGED);

        // Add a different attribute -> UNCHANGED
        int otherId = g.addAttribute(GraphElementType.VERTEX, "integer", "Other", "Other", 0, null);
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.UNCHANGED);

        // Set a value on another attribute -> UNCHANGED
        g.setIntValue(otherId, v, 100);
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.UNCHANGED);

        // Remove the other attribute -> UNCHANGED
        g.removeAttribute(otherId);
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.UNCHANGED);

        // Remove the attribute -> REMOVED
        g.removeAttribute(monitor.getId());
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.REMOVED);

        // Do nothing -> STILL_MISSING
        monitor.update(g);
        assertEquals(monitor.getTransition(), AttributeTransition.STILL_MISSING);
    }

}
