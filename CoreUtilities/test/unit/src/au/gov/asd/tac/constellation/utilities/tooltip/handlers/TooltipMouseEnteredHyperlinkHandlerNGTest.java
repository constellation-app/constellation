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
package au.gov.asd.tac.constellation.utilities.tooltip.handlers;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipNode;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TooltipMouseEnteredHyperlinkHandlerNGTest {

    private static final Logger LOGGER = Logger.getLogger(TooltipMouseEnteredHyperlinkHandlerNGTest.class.getName());
    
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
        // Not currently required

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required

    }

    /**
     * Test of handle method, of class TooltipMouseEnteredHyperlinkHandler.
     */
    @Test
    public void testHandle() {
        System.out.println("handle");
        final Hyperlink hyperlink = mock(Hyperlink.class);
        final TooltipPane tooltipPane = spy(new TooltipPane());
        when(tooltipPane.isEnabled()).thenReturn(false);
        doNothing().when(hyperlink).requestFocus();
        final TooltipMouseEnteredHyperlinkHandler instance = new TooltipMouseEnteredHyperlinkHandler(hyperlink, tooltipPane);
        final Event event = mock(Event.class);

        instance.handle(event);

        verify(tooltipPane, times(1)).isEnabled();
        verify(hyperlink, times(0)).requestFocus();
    }

    /**
     * Test of handle method, of class TooltipMouseEnteredHyperlinkHandler.
     */
    @Test
    public void testHandle2() {
        System.out.println("handle2");
        final Hyperlink hyperlink = mock(Hyperlink.class);
        final TooltipPane tooltipPane = spy(new TooltipPane());

        final TooltipProvider.TooltipDefinition ttd1 = mock(TooltipProvider.TooltipDefinition.class);
        final TooltipProvider.TooltipDefinition ttd2 = mock(TooltipProvider.TooltipDefinition.class);

        // Create list of definitions to return.
        final List<TooltipProvider.TooltipDefinition> definitions = new ArrayList<>();
        definitions.add(ttd1);
        definitions.add(ttd2);
        final TooltipNode ttn = mock(TooltipNode.class);
        try (final MockedStatic<TooltipProvider> ttpStatic = mockStatic(TooltipProvider.class, CALLS_REAL_METHODS);
                final MockedStatic<TooltipMouseEnteredHyperlinkHandler> melStatic = mockStatic(TooltipMouseEnteredHyperlinkHandler.class, CALLS_REAL_METHODS)) {
            ttpStatic.when(() -> TooltipProvider.getAllTooltips(Mockito.eq("returnText"))).thenReturn(definitions);
            melStatic.when(() -> TooltipMouseEnteredHyperlinkHandler.createTooltipNode(Mockito.any(List.class))).thenReturn(ttn);

            final MouseEvent event = mock(MouseEvent.class);
            final Point2D p2d = mock(Point2D.class);
            when(p2d.getX()).thenReturn(200.0d);
            when(p2d.getY()).thenReturn(400.0d);
            when(tooltipPane.isEnabled()).thenReturn(true);
            doNothing().when(tooltipPane).showTooltip(Mockito.any(TooltipNode.class), Mockito.eq(200.0d), Mockito.eq(400.0d));
            doNothing().when(hyperlink).requestFocus();
            when(hyperlink.getText()).thenReturn("returnText");
            when(hyperlink.localToScene(Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(p2d);

            final TooltipMouseEnteredHyperlinkHandler instance = new TooltipMouseEnteredHyperlinkHandler(hyperlink, tooltipPane);

            instance.handle(event);

            // verify that the text area was not focused.
            verify(tooltipPane, times(1)).isEnabled();
            verify(hyperlink, times(1)).requestFocus();
            verify(hyperlink, times(1)).localToScene(Mockito.anyDouble(), Mockito.anyDouble());
            verify(p2d, times(1)).getX();
            verify(p2d, times(1)).getY();
            verify(tooltipPane, times(1)).showTooltip(Mockito.any(TooltipNode.class), Mockito.eq(200.0d), Mockito.eq(400.0d));
            ttpStatic.verify(() -> TooltipProvider.getAllTooltips(Mockito.eq("returnText")));
            melStatic.verify(() -> TooltipMouseEnteredHyperlinkHandler.createTooltipNode(Mockito.any(List.class)), times(1));
        }
    }

    /**
     * Test of createTooltipNode method, of class
     * TooltipMouseEnteredHyperlinkHandler.
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

        final TooltipNode result = TooltipMouseEnteredHyperlinkHandler.createTooltipNode(definitions);
        assertNotNull(result);
        assertNotNull(result.getChildren());
        assertEquals(result.getChildren().size(), definitions.size());
        assertEquals(result.getChildren().get(0), definitions.get(0).getNode());
        assertEquals(result.getChildren().get(1), definitions.get(1).getNode());
        assertNotEquals(result.getChildren().get(1), definitions.get(0).getNode());
    }
}
