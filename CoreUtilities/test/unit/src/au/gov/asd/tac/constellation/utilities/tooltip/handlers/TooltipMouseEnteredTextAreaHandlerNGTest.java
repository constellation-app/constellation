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
package au.gov.asd.tac.constellation.utilities.tooltip.handlers;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipNode;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxToolkit;
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
public class TooltipMouseEnteredTextAreaHandlerNGTest {

    private static final Logger LOGGER = Logger.getLogger(TooltipMouseEnteredTextAreaHandlerNGTest.class.getName());

    public TooltipMouseEnteredTextAreaHandlerNGTest() {
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
        } catch (final TimeoutException ex) {
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
     * Test of handle method, of class TooltipMouseEnteredTextAreaHandler.
     */
    @Test
    public void testHandle() {
        System.out.println("handle");
        final InlineCssTextArea inlineCssTextArea = mock(InlineCssTextArea.class);
        final TooltipPane tooltipPane = spy(new TooltipPane());

        try (final MockedStatic<TooltipUtilities> ttuStatic = mockStatic(TooltipUtilities.class, CALLS_REAL_METHODS)) {
            ttuStatic.when(() -> TooltipUtilities.selectActiveArea(Mockito.any(TextInputControl.class), Mockito.any(List.class))).thenAnswer((Answer<Void>) invocation -> null);
            when(tooltipPane.isEnabled()).thenReturn(false);
            doNothing().when(inlineCssTextArea).requestFocus();
            final TooltipMouseEnteredTextAreaHandler instance = new TooltipMouseEnteredTextAreaHandler(inlineCssTextArea, tooltipPane);
            final Event event = mock(Event.class);

            instance.handle(event);

            // verify that the text area was not focused.
            verify(tooltipPane, times(0)).isEnabled();
            verify(inlineCssTextArea, times(0)).requestFocus();
        }
    }

    /**
     * Test of handle method, of class TooltipMouseEnteredTextAreaHandler.
     */
    @Test
    public void testHandle2() {
        System.out.println("handle2");
        final InlineCssTextArea inlineCssTextArea = mock(InlineCssTextArea.class);
        final TooltipPane tooltipPane = spy(new TooltipPane());

        final Pane p1 = spy(new Pane());
        final Pane p2 = spy(new Pane());
        final TooltipProvider.TooltipDefinition ttd1 = spy(new TooltipProvider.TooltipDefinition(p1));
        final TooltipProvider.TooltipDefinition ttd2 = spy(new TooltipProvider.TooltipDefinition(p2));

        // Create list of definitions to return.
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        definitions.add(ttd1);
        definitions.add(ttd2);
        final TooltipNode ttn = mock(TooltipNode.class);

        try (final MockedStatic<TooltipUtilities> ttuStatic = mockStatic(TooltipUtilities.class, CALLS_REAL_METHODS);
                final MockedStatic<TooltipProvider> ttpStatic = mockStatic(TooltipProvider.class, CALLS_REAL_METHODS);
                final MockedStatic<TooltipMouseEnteredTextAreaHandler> melStatic = mockStatic(TooltipMouseEnteredTextAreaHandler.class, CALLS_REAL_METHODS)) {
            ttuStatic.when(() -> TooltipUtilities.selectActiveArea(Mockito.any(TextInputControl.class), Mockito.any(List.class))).thenAnswer((Answer<Void>) invocation -> null);
            ttpStatic.when(() -> TooltipProvider.getTooltips(Mockito.eq("returnText"), Mockito.eq(10))).thenReturn(definitions);
            melStatic.when(() -> TooltipMouseEnteredTextAreaHandler.createTooltipNode(Mockito.any(List.class))).thenReturn(ttn);

            final MouseOverTextEvent event = mock(MouseOverTextEvent.class);
            final Point2D p2d = mock(Point2D.class);
            when(p2d.getX()).thenReturn(200.0d);
            when(p2d.getY()).thenReturn(400.0d);
            when(tooltipPane.isEnabled()).thenReturn(true);
            doNothing().when(tooltipPane).showTooltip(Mockito.any(TooltipNode.class), Mockito.eq(200.0d), Mockito.eq(400.0d));
            doNothing().when(inlineCssTextArea).requestFocus();
            when(inlineCssTextArea.getText()).thenReturn("returnText");
            when(event.getCharacterIndex()).thenReturn(10);
            when(event.getScreenPosition()).thenReturn(p2d);

            final TooltipMouseEnteredTextAreaHandler instance = new TooltipMouseEnteredTextAreaHandler(inlineCssTextArea, tooltipPane);

            instance.handle(event);

            // verify that the text area was not focused.
            verify(tooltipPane, times(1)).isEnabled();
            verify(event, times(1)).getCharacterIndex();
            verify(inlineCssTextArea, times(1)).requestFocus();
            verify(event, times(1)).getScreenPosition();
            verify(p2d, times(1)).getX();
            verify(p2d, times(1)).getY();
            verify(tooltipPane, times(1)).showTooltip(Mockito.any(TooltipNode.class), Mockito.eq(200.0d), Mockito.eq(400.0d));
            ttpStatic.verify(() -> TooltipProvider.getTooltips(Mockito.eq("returnText"), Mockito.eq(10)), times(1));
            ttuStatic.verify(() -> TooltipUtilities.selectActiveArea(Mockito.any(InlineCssTextArea.class), Mockito.any(List.class)), times(1));
            melStatic.verify(() -> TooltipMouseEnteredTextAreaHandler.createTooltipNode(Mockito.any(List.class)), times(1));
        }
    }

    /**
     * Test of handle method, of class TooltipMouseEnteredTextAreaHandler.
     */
    @Test
    public void testHandle3() {
        System.out.println("handle3");
        final InlineCssTextArea inlineCssTextArea = mock(InlineCssTextArea.class);
        final TooltipPane tooltipPane = spy(new TooltipPane());

        // Create list of definitions to return.
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        final TooltipNode ttn = mock(TooltipNode.class);

        try (final MockedStatic<TooltipUtilities> ttuStatic = mockStatic(TooltipUtilities.class, CALLS_REAL_METHODS);
                final MockedStatic<TooltipProvider> ttpStatic = mockStatic(TooltipProvider.class, CALLS_REAL_METHODS);
                final MockedStatic<TooltipMouseEnteredTextAreaHandler> melStatic = mockStatic(TooltipMouseEnteredTextAreaHandler.class, CALLS_REAL_METHODS)) {
            ttuStatic.when(() -> TooltipUtilities.selectActiveArea(Mockito.any(TextInputControl.class), Mockito.any(List.class))).thenAnswer((Answer<Void>) invocation -> null);
            ttpStatic.when(() -> TooltipProvider.getTooltips(Mockito.eq("returnText"), Mockito.eq(10))).thenReturn(definitions);
            melStatic.when(() -> TooltipMouseEnteredTextAreaHandler.createTooltipNode(Mockito.any(List.class))).thenReturn(ttn);

            final MouseOverTextEvent event = mock(MouseOverTextEvent.class);
            final Point2D p2d = mock(Point2D.class);
            when(p2d.getX()).thenReturn(200.0d);
            when(p2d.getY()).thenReturn(400.0d);
            when(tooltipPane.isEnabled()).thenReturn(true);
            doNothing().when(tooltipPane).showTooltip(Mockito.any(TooltipNode.class), Mockito.eq(200.0d), Mockito.eq(400.0d));
            doNothing().when(inlineCssTextArea).requestFocus();
            when(inlineCssTextArea.getText()).thenReturn("returnText");
            when(event.getCharacterIndex()).thenReturn(10);
            when(event.getScreenPosition()).thenReturn(p2d);

            final TooltipMouseEnteredTextAreaHandler instance = new TooltipMouseEnteredTextAreaHandler(inlineCssTextArea, tooltipPane);

            instance.handle(event);

            // verify that the text area was not focused.
            verify(tooltipPane, times(1)).isEnabled();
            verify(event, times(1)).getCharacterIndex();
            verify(inlineCssTextArea, times(1)).requestFocus();
            verify(event, times(0)).getScreenPosition();
            verify(p2d, times(0)).getX();
            verify(p2d, times(0)).getY();
            verify(tooltipPane, times(0)).showTooltip(Mockito.any(TooltipNode.class), Mockito.eq(200.0d), Mockito.eq(400.0d));
            ttpStatic.verify(() -> TooltipProvider.getTooltips(Mockito.eq("returnText"), Mockito.eq(10)), times(1));
            ttuStatic.verify(() -> TooltipUtilities.selectActiveArea(Mockito.any(InlineCssTextArea.class), Mockito.any(List.class)), times(1));
            melStatic.verify(() -> TooltipMouseEnteredTextAreaHandler.createTooltipNode(Mockito.any(List.class)), times(0));
        }
    }

    /**
     * Test of createTooltipNode method, of class
     * TooltipMouseEnteredTextAreaHandler.
     */
    @Test
    public void testCreateTooltipNode() {
        System.out.println("createTooltipNode");
        // Create list of definitions to return.
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        final Pane p1 = spy(new Pane());
        final Pane p2 = spy(new Pane());
        final TooltipProvider.TooltipDefinition ttd1 = spy(new TooltipProvider.TooltipDefinition(p1));
        final TooltipProvider.TooltipDefinition ttd2 = spy(new TooltipProvider.TooltipDefinition(p2));

        definitions.add(ttd1);
        definitions.add(ttd2);

        final TooltipNode result = TooltipMouseEnteredTextAreaHandler.createTooltipNode(definitions);
        assertNotNull(result);
        assertNotNull(result.getChildren());
        assertEquals(result.getChildren().size(), definitions.size());
        assertEquals(result.getChildren().get(0), definitions.get(0).getNode());
        assertEquals(result.getChildren().get(1), definitions.get(1).getNode());
        assertNotEquals(result.getChildren().get(1), definitions.get(0).getNode());
    }
}
