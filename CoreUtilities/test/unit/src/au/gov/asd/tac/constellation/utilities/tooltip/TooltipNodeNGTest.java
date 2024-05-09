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
package au.gov.asd.tac.constellation.utilities.tooltip;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider.TooltipDefinition;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TooltipNodeNGTest {
    
    public TooltipNodeNGTest() {
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
     * Test of construction method, of class TooltipNode.
     */
    @Test
    public void testConstruction() {
        System.out.println("contructor");
        final TooltipNode instance = new TooltipNode();
        
        assertEquals(instance.getPadding(), new Insets(3));
        assertTrue(instance.isFillWidth());
        assertTrue(instance.getStyle().contains("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 5;"));
    }

    /**
     * Test of setTooltips method, of class TooltipNode.
     */
    @Test
    public void testSetTooltips() {
        System.out.println("setTooltips");
        
        final List<TooltipDefinition> tooltips = new ArrayList<>();
        final TooltipNode instance = spy(new TooltipNode());
        
        final Pane p1 = spy(new Pane());
        final Pane p2 = spy(new Pane());
        final Pane p3 = spy(new Pane());
        final Pane p4 = spy(new Pane());
        
        final TooltipProvider.TooltipDefinition ttd1 = spy(new TooltipProvider.TooltipDefinition(p1));
        final TooltipProvider.TooltipDefinition ttd2 = spy(new TooltipProvider.TooltipDefinition(p2));
        final TooltipProvider.TooltipDefinition ttd3 = spy(new TooltipProvider.TooltipDefinition(p3));
        final TooltipProvider.TooltipDefinition ttd4 = spy(new TooltipProvider.TooltipDefinition(p4));
        
        tooltips.add(ttd1);
        tooltips.add(ttd2);
        tooltips.add(ttd3);
        tooltips.add(ttd4);
        
        // Check that no children exist before adding tooltips
        assertEquals(instance.getChildren().size(), 0);
        
        instance.setTooltips(tooltips);
        
        // Check that tooltips were successfully added
        assertNotNull(instance.getChildren());
        assertEquals(instance.getChildren().size(), 4);
        
        // Verify that all the tooltips had their node fetched
        for(final TooltipDefinition ttd : tooltips) {
            verify(ttd, times(1)).getNode();
        }
        
        assertTrue(instance.getChildren().contains(p1));
        assertTrue(instance.getChildren().contains(p2));
        assertTrue(instance.getChildren().contains(p3));
        assertTrue(instance.getChildren().contains(p4));
        
        verify(instance, Mockito.atLeast(7)).getChildren();
    }
    
}
