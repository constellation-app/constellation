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
package au.gov.asd.tac.constellation.utilities.tooltip;

import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TooltipPaneNGTest {
    
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
     * Test of constructor, of class TooltipPane.
     */
    @Test
    public void testConstructor() {
        System.out.println("constructor");
        
        final TooltipPane instance = new TooltipPane();
        assertTrue(instance.getStyle().contains("-fx-background-color: transparent;"));
        assertTrue(instance.mouseTransparentProperty().getValue());
    }
  

    /**
     * Test of showTooltip method, of class TooltipPane.
     * Tests with a null current node
     */
    @Test
    public void testShowTooltip() {
        System.out.println("showTooltip");
        final Pane node = spy(new Pane());
        doNothing().when(node).setManaged(Mockito.anyBoolean());
        
        final double x = 1.1;
        final double y = 2.2;
        
        final double p2dL = 10.1;
        final double p2dT = 20.2;
        
        final TooltipPane instance = spy(new TooltipPane());
        
        final Point2D p2d = mock(Point2D.class);
        when(p2d.getX()).thenReturn(p2dL);
        when(p2d.getY()).thenReturn(p2dT);
        
        when(instance.sceneToLocal(Mockito.eq(x),Mockito.eq(y))).thenReturn(p2d);
        
        try(final MockedStatic<AnchorPane> apStatic = mockStatic(AnchorPane.class)){
            apStatic.when(() -> AnchorPane.setLeftAnchor(Mockito.eq(node), Mockito.eq(p2dL)))
                    .thenAnswer((Answer<Void>) invocation -> null);
            apStatic.when(() -> AnchorPane.setTopAnchor(Mockito.eq(node), Mockito.eq(p2dT)))
                    .thenAnswer((Answer<Void>) invocation -> null);
            
           
            // tooltipNode not same as node
            // tooltipNode is null
            instance.showTooltip(node, x, y);
            
            apStatic.verify(() -> AnchorPane.setLeftAnchor(Mockito.eq(node), Mockito.eq(p2dL)), times(1));
            apStatic.verify(() -> AnchorPane.setTopAnchor(Mockito.eq(node), Mockito.eq(p2dT)), times(1));

            // assert that instance is now using the new node as the tooltipNode
            assertEquals(instance.getTooltipNode(), node);
            verify(node,times(1)).setManaged(Mockito.eq(false));
            
            // assert and verify that the instance now has the child node.
            verify(instance, times(1)).getChildren();
            assertTrue(instance.getChildren().contains(node));
        }
    }
    
    /**
     * Test of showTooltip method, of class TooltipPane.
     * Tests with a non-null current node
     */
    @Test
    public void testShowTooltip2() {
        System.out.println("showTooltip2");
        final Pane toolTipNode = spy(new Pane());
        doNothing().when(toolTipNode).setManaged(Mockito.eq(true));
        
        final Pane node = spy(new Pane());
        doNothing().when(node).setManaged(Mockito.eq(false));
        
        final double x = 1.1;
        final double y = 2.2;
        
        final double p2dL = 10.1;
        final double p2dT = 20.2;
        
        final TooltipPane instance = spy(new TooltipPane());
        
        final Point2D p2d = mock(Point2D.class);
        when(p2d.getX()).thenReturn(p2dL);
        when(p2d.getY()).thenReturn(p2dT);
        
        when(instance.sceneToLocal(Mockito.eq(x),Mockito.eq(y))).thenReturn(p2d);
        
        try(final MockedStatic<AnchorPane> apStatic = mockStatic(AnchorPane.class)){
            apStatic.when(() -> AnchorPane.setLeftAnchor(Mockito.eq(node), Mockito.eq(p2dL)))
                    .thenAnswer((Answer<Void>) invocation -> null);
            apStatic.when(() -> AnchorPane.setTopAnchor(Mockito.eq(node), Mockito.eq(p2dT)))
                    .thenAnswer((Answer<Void>) invocation -> null);
            
            instance.setTooltipNode(toolTipNode);
            
            // tooltipNode not same as node
            // tooltipNode is not null
            instance.showTooltip(node, x, y);
            
            apStatic.verify(() -> AnchorPane.setLeftAnchor(Mockito.eq(node), Mockito.eq(p2dL)), times(1));
            apStatic.verify(() -> AnchorPane.setTopAnchor(Mockito.eq(node), Mockito.eq(p2dT)), times(1));

            // assert that instance is now using the new node as the tooltipNode
            assertEquals(instance.getTooltipNode(), node);
            verify(node,times(1)).setManaged(Mockito.eq(false));
            verify(toolTipNode,times(1)).setManaged(Mockito.eq(true));
            
            // assert and verify that the instance now has the child node.
            verify(instance, times(2)).getChildren();
            assertTrue(instance.getChildren().contains(node));
            assertFalse(instance.getChildren().contains(toolTipNode));
        }
    }
    
    
    /**
     * Test of showTooltip method, of class TooltipPane.
     * Tests with current node same as new node
     */
    @Test
    public void testShowTooltip3() {
        System.out.println("showTooltip2");
        
        final Pane node = mock(Pane.class);
        
        final double x = 1.1;
        final double y = 2.2;
        
        final double p2dL = 10.1;
        final double p2dT = 20.2;
        
        final TooltipPane instance = spy(new TooltipPane());
        
        final Point2D p2d = mock(Point2D.class);
        when(p2d.getX()).thenReturn(p2dL);
        when(p2d.getY()).thenReturn(p2dT);
        
        when(instance.sceneToLocal(Mockito.eq(x),Mockito.eq(y))).thenReturn(p2d);
        
        try(final MockedStatic<AnchorPane> apStatic = mockStatic(AnchorPane.class)){
            apStatic.when(() -> AnchorPane.setLeftAnchor(Mockito.eq(node), Mockito.eq(p2dL)))
                    .thenAnswer((Answer<Void>) invocation -> null);
            apStatic.when(() -> AnchorPane.setTopAnchor(Mockito.eq(node), Mockito.eq(p2dT)))
                    .thenAnswer((Answer<Void>) invocation -> null);
            
           
            instance.setTooltipNode(node);
            
            // tooltipNode not same as node
            // tooltipNode is not null
            instance.showTooltip(node, x, y);
            
            apStatic.verify(() -> AnchorPane.setLeftAnchor(Mockito.eq(node), Mockito.eq(p2dL)), times(1));
            apStatic.verify(() -> AnchorPane.setTopAnchor(Mockito.eq(node), Mockito.eq(p2dT)), times(1));

            // assert that instance is now using the new node as the tooltipNode
            assertEquals(instance.getTooltipNode(), node);
            
            // assert and verify that the instance now has the child node.
            verify(instance, Mockito.times(0)).getChildren();
        }
    }
    /**
     * Test of hideTooltip method, of class TooltipPane.
     */
    @Test
    public void testHideTooltip() {
        System.out.println("hideTooltip");
        
        final TooltipPane instance = spy(new TooltipPane());
        instance.setTooltipNode(null);
        instance.hideTooltip();
        
        // no children should be queried when the node is null
        verify(instance, times(0)).getChildren();
        
        // set up for a not null node
        final Pane node = mock(Pane.class);
        doNothing().when(node).setManaged(Mockito.eq(true));
        instance.setTooltipNode(node);
        
        instance.hideTooltip();
        // Verify that node is managed, current node is reset and does not exist in 
        // the current children.
        verify(instance, times(1)).getChildren();
        verify(node,times(1)).setManaged(Mockito.eq(true));
        assertNull(instance.getTooltipNode());
        assertFalse(instance.getChildren().contains(node));
    }


    /**
     * Test of layoutChildren method, of class TooltipPane.
     * null initial node
     */
    @Test
    public void testLayoutChildren() {
        System.out.println("layoutChildren");
        
        final TooltipPane instance = spy(new TooltipPane());
        instance.setTooltipNode(null);
        
        final double d1 = 1;
        final double d2 = 2;
        
        try(final MockedStatic<AnchorPane> apStatic = mockStatic(AnchorPane.class)){
            apStatic.when(() -> AnchorPane.getLeftAnchor(Mockito.any()))
                    .thenReturn(d1);
            apStatic.when(() -> AnchorPane.getTopAnchor(Mockito.any()))
                    .thenReturn(d2);
            
            instance.layoutChildren();
            
            apStatic.verifyNoInteractions();
        }
    }
    
    /**
     * Test of layoutChildren method, of class TooltipPane.
     */
    @Test
    public void testLayoutChildren2() {
        System.out.println("layoutChildren2");
        
        final double nodewidth = 10;
        final double nodeheight = 20;
        
        final double width = 15;
        final double height = 15;
        
        // setup current node mock
        final Pane node = mock(Pane.class);
        doNothing().when(node).resize(Mockito.anyDouble(),Mockito.anyDouble());
        doNothing().when(node).setLayoutX(Mockito.anyDouble());
        doNothing().when(node).setLayoutY(Mockito.anyDouble());
        doNothing().when(node).autosize();
        when(node.getWidth()).thenReturn(nodewidth);
        when(node.getHeight()).thenReturn(nodeheight);
        
        final TooltipPane instance = spy(new TooltipPane());
        when(instance.getWidth()).thenReturn(width);
        when(instance.getHeight()).thenReturn(height);
        
        instance.setTooltipNode(node);
        
        final double d1 = 10;
        final double d2 = 20;
        
        try(final MockedStatic<AnchorPane> apStatic = mockStatic(AnchorPane.class)){
            apStatic.when(() -> AnchorPane.getLeftAnchor(Mockito.any()))
                    .thenReturn(d1);
            apStatic.when(() -> AnchorPane.getTopAnchor(Mockito.any()))
                    .thenReturn(d2);
            
            instance.layoutChildren();
            
            verify(node, times(1)).resize(Mockito.eq(15.0d), Mockito.eq(35.0d));
            verify(node, times(1)).setLayoutX(Mockito.eq(5.0d));
            verify(node, times(1)).setLayoutY(Mockito.eq(0.0d));
            
            apStatic.verify(() -> AnchorPane.getLeftAnchor(Mockito.eq(node)), times(1));
            apStatic.verify(() -> AnchorPane.getTopAnchor(Mockito.eq(node)), times(1));
        } 
    }
    
    /**
     * Test of layoutChildren method, of class TooltipPane.
     */
    @Test
    public void testLayoutChildren3() {
        System.out.println("layoutChildren3");
        
        final double nodewidth = 10;
        final double nodeheight = 20;
        
        final double width = 100;
        final double height = 200;
        
        // setup current node mock
        final Pane node = mock(Pane.class);
        doNothing().when(node).resize(Mockito.anyDouble(),Mockito.anyDouble());
        doNothing().when(node).setLayoutX(Mockito.anyDouble());
        doNothing().when(node).setLayoutY(Mockito.anyDouble());
        doNothing().when(node).autosize();
        when(node.getWidth()).thenReturn(nodewidth);
        when(node.getHeight()).thenReturn(nodeheight);
        
        final TooltipPane instance = spy(new TooltipPane());
        when(instance.getWidth()).thenReturn(width);
        when(instance.getHeight()).thenReturn(height);
        
        instance.setTooltipNode(node);
        
        final double d1 = 10;
        final double d2 = 20;
        
        try(final MockedStatic<AnchorPane> apStatic = mockStatic(AnchorPane.class)){
            apStatic.when(() -> AnchorPane.getLeftAnchor(Mockito.any()))
                    .thenReturn(d1);
            apStatic.when(() -> AnchorPane.getTopAnchor(Mockito.any()))
                    .thenReturn(d2);
            
            instance.layoutChildren();
            
            verify(node, times(1)).resize(Mockito.eq(10.0d), Mockito.eq(20.0d));
            verify(node, times(1)).setLayoutX(Mockito.eq(10.0d));
            verify(node, times(1)).setLayoutY(Mockito.eq(20.0d));
            
            apStatic.verify(() -> AnchorPane.getLeftAnchor(Mockito.eq(node)), times(1));
            apStatic.verify(() -> AnchorPane.getTopAnchor(Mockito.eq(node)), times(1));
        }
    }
}
