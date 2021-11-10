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
package au.gov.asd.tac.constellation.utilities.tooltip.handlers;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.TooltipMouseExitedHandler;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
public class TooltipMouseExitedHandlerNGTest {
    
    public TooltipMouseExitedHandlerNGTest() {
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
     * Test of handle method, of class TooltipMouseExitedHandler.
     */
    @Test
    public void testHandle() {
        System.out.println("handle");
        
        final TooltipPane tooltipPane = spy(new TooltipPane());
        when(tooltipPane.isEnabled()).thenReturn(true);
        doNothing().when(tooltipPane).hideTooltip();
        
        
        final TooltipMouseExitedHandler instance = new TooltipMouseExitedHandler(tooltipPane);
        final MouseEvent event = mock(MouseEvent.class);
            
        instance.handle(event);
        
        verify(tooltipPane, times(1)).isEnabled();
        verify(tooltipPane, times(1)).hideTooltip();
    }
    
        /**
     * Test of handle method, of class TooltipMouseExitedHandler.
     */
    @Test
    public void testHandle2() {
        System.out.println("handle2");
        
        final TooltipPane tooltipPane = spy(new TooltipPane());
        when(tooltipPane.isEnabled()).thenReturn(false);
        doNothing().when(tooltipPane).hideTooltip();
        
        
        final TooltipMouseExitedHandler instance = new TooltipMouseExitedHandler(tooltipPane);
        final MouseEvent event = mock(MouseEvent.class);
            
        instance.handle(event);
        
        verify(tooltipPane, times(1)).isEnabled();
        verify(tooltipPane, times(0)).hideTooltip();
    }
}
