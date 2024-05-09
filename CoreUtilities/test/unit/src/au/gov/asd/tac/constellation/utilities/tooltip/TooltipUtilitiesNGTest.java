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

import au.gov.asd.tac.constellation.utilities.tooltip.handlers.TooltipMouseEnteredHyperlinkHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.TooltipMouseMovedHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.TooltipMouseEnteredHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.TooltipMouseExitedHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.TooltipMouseEnteredTextAreaHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.util.Pair;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TooltipUtilitiesNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(TooltipUtilitiesNGTest.class.getName());
    
    public TooltipUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
         try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of activateTextInputControl method, of class TooltipUtilities.
     */
    @Test
    public void testActivateTextInputControl_TextInputControl_TooltipPane() {
        System.out.println("activateTextInputControl");
        final TextArea textInputControl = mock(TextArea.class);
        final TooltipPane tooltipPane = new TooltipPane();
        
        doNothing().when(textInputControl).setOnMouseEntered(Mockito.any(TooltipMouseEnteredHandler.class));
        doNothing().when(textInputControl).setOnMouseMoved(Mockito.any(TooltipMouseMovedHandler.class));
        doNothing().when(textInputControl).setOnMouseExited(Mockito.any(TooltipMouseExitedHandler.class));
        
        TooltipUtilities.activateTextInputControl(textInputControl, tooltipPane);
        
        verify(textInputControl, times(1)).setOnMouseEntered(Mockito.any(TooltipMouseEnteredHandler.class));
        verify(textInputControl, times(1)).setOnMouseMoved(Mockito.any(TooltipMouseMovedHandler.class));
        verify(textInputControl, times(1)).setOnMouseExited(Mockito.any(TooltipMouseExitedHandler.class));
    }

    /**
     * Test of activateTextInputControl method, of class TooltipUtilities.
     */
    @Test
    public void testActivateTextInputControl_Hyperlink_TooltipPane() {
        System.out.println("activateTextInputControl");
        
        final Hyperlink hyperlink = mock(Hyperlink.class);
        final TooltipPane tooltipPane = new TooltipPane();
        
        doNothing().when(hyperlink).setOnMouseEntered(Mockito.any(TooltipMouseEnteredHyperlinkHandler.class));
        doNothing().when(hyperlink).setOnMouseExited(Mockito.any(TooltipMouseExitedHandler.class));
        
        TooltipUtilities.activateTextInputControl(hyperlink, tooltipPane);
        
        verify(hyperlink, times(1)).setOnMouseEntered(Mockito.any(TooltipMouseEnteredHyperlinkHandler.class));
        verify(hyperlink, times(1)).setOnMouseExited(Mockito.any(TooltipMouseExitedHandler.class));
        
    }

    /**
     * Test of activateTextInputControl method, of class TooltipUtilities.
     */
    @Test
    public void testActivateTextInputControl_InlineCssTextArea_TooltipPane() {
        System.out.println("activateTextInputControl");
        final InlineCssTextArea textArea = mock(InlineCssTextArea.class);
        final TooltipPane tooltipPane = new TooltipPane();
        
        doNothing().when(textArea).setMouseOverTextDelay(Mockito.any(Duration.class));
        doNothing().when(textArea).addEventHandler(Mockito.eq(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN),Mockito.any(TooltipMouseEnteredTextAreaHandler.class));
        doNothing().when(textArea).addEventHandler(Mockito.eq(MouseOverTextEvent.MOUSE_OVER_TEXT_END),Mockito.any(TooltipMouseExitedHandler.class));
        
        TooltipUtilities.activateTextInputControl(textArea, tooltipPane);
        
        verify(textArea, times(1)).setMouseOverTextDelay(Mockito.any(Duration.class));
        verify(textArea, times(1)).addEventHandler(Mockito.eq(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN),Mockito.any(TooltipMouseEnteredTextAreaHandler.class));
        verify(textArea, times(1)).addEventHandler(Mockito.eq(MouseOverTextEvent.MOUSE_OVER_TEXT_END),Mockito.any(TooltipMouseExitedHandler.class));
    }
    
    /**
     * Test of findActiveArea method, of class TooltipUtilities.
     */
    @Test
    public void testfindActiveArea() {
        System.out.println("findActiveArea");
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        final TooltipProvider.TooltipDefinition ttd1 = mock(TooltipProvider.TooltipDefinition.class);
        final TooltipProvider.TooltipDefinition ttd2 = mock(TooltipProvider.TooltipDefinition.class);

        when(ttd1.getStart()).thenReturn(10);
        when(ttd2.getStart()).thenReturn(20);
        when(ttd1.getFinish()).thenReturn(100);
        when(ttd2.getFinish()).thenReturn(200);
        
        // Create list of definitions
        definitions.add(ttd1);
        definitions.add(ttd2);
        
        TooltipUtilities.findActiveArea(definitions);
        
        // verify calls
        verify(ttd1, times(1)).getStart();
        verify(ttd2, times(1)).getStart();
        verify(ttd1, times(1)).getFinish();
        verify(ttd2, times(1)).getFinish();
    }
    
    /**
     * Test of testselectActiveAreaControl method, of class TooltipUtilities.
     */
    @Test
    public void testselectActiveAreaControl() {
        System.out.println("testselectActiveAreaControl");
        final TextInputControl textControl = mock(TextInputControl.class);
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        
        try(final MockedStatic<TooltipUtilities> ttuStatic = mockStatic(TooltipUtilities.class, CALLS_REAL_METHODS)){
            final Pair<Integer, Integer> pairMock = mock(Pair.class);
            when(pairMock.getKey()).thenReturn(10);
            when(pairMock.getValue()).thenReturn(200);
            ttuStatic.when(() -> TooltipUtilities.findActiveArea(Mockito.any(List.class))).thenReturn(pairMock);
            doNothing().when(textControl).selectRange(Mockito.eq(10),Mockito.eq(200));
            
            TooltipUtilities.selectActiveArea(textControl, definitions);

            // verify calls
            verify(textControl, times(1)).selectRange(Mockito.eq(10),Mockito.eq(200));
        }
    }
    
        /**
     * Test of testselectActiveAreaInlineCssTextArea method, of class TooltipUtilities.
     */
    @Test
    public void testselectActiveAreaInlineCssTextArea() {
        System.out.println("testselectActiveAreaInlineCssTextArea");
        final InlineCssTextArea textArea = mock(InlineCssTextArea.class);
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        
        try(final MockedStatic<TooltipUtilities> ttuStatic = mockStatic(TooltipUtilities.class, CALLS_REAL_METHODS)){
            final Pair<Integer, Integer> pairMock = mock(Pair.class);
            when(pairMock.getKey()).thenReturn(10);
            when(pairMock.getValue()).thenReturn(200);
            ttuStatic.when(() -> TooltipUtilities.findActiveArea(Mockito.any(List.class))).thenReturn(pairMock);
            doNothing().when(textArea).selectRange(Mockito.eq(10),Mockito.eq(200));
            
            TooltipUtilities.selectActiveArea(textArea, definitions);

            // verify calls
            verify(textArea, times(1)).selectRange(Mockito.eq(10),Mockito.eq(200));
        }
    }
}
