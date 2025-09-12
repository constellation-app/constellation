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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinCreator;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import au.gov.asd.tac.constellation.views.histogram.HistogramState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javafx.util.Pair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class HistogramPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(HistogramPaneNGTest.class.getName());

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

    /**
     * Test of updateDisplay method, of class HistogramPane.
     */
    @Test
    public void testUpdateDisplay() {
        System.out.println("updateDisplay");
        try (final MockedConstruction<HistogramDisplay2> mockConstructor = Mockito.mockConstruction(HistogramDisplay2.class)) {
            final HistogramPane instance = new HistogramPane(HistogramController.getDefault());
            instance.updateDisplay();

            // Assert HistogramDisplay2 was made and function was called
            assertTrue(mockConstructor.constructed().size() == 1);
            final HistogramDisplay2 menu = mockConstructor.constructed().getLast();
            verify(menu, atLeast(1)).updateDisplay();
        }
    }

    /**
     * Test of decreaseBarHeight method, of class HistogramPane.
     */
    @Test
    public void testDecreaseBarHeight() {
        System.out.println("decreaseBarHeight");
        try (final MockedConstruction<HistogramDisplay2> mockConstructor = Mockito.mockConstruction(HistogramDisplay2.class)) {
            final HistogramPane instance = new HistogramPane(HistogramController.getDefault());
            instance.decreaseBarHeight();

            // Assert HistogramDisplay2 was made and function was called
            assertTrue(mockConstructor.constructed().size() == 1);
            final HistogramDisplay2 menu = mockConstructor.constructed().getLast();
            verify(menu).decreaseBarHeight();
        }
    }

    /**
     * Test of increaseBarHeight method, of class HistogramPane.
     */
    @Test
    public void testIncreaseBarHeight() {
        System.out.println("increaseBarHeight");
        try (final MockedConstruction<HistogramDisplay2> mockConstructor = Mockito.mockConstruction(HistogramDisplay2.class)) {
            final HistogramPane instance = new HistogramPane(HistogramController.getDefault());
            instance.increaseBarHeight();

            // Assert HistogramDisplay2 was made and function was called
            assertTrue(mockConstructor.constructed().size() == 1);
            final HistogramDisplay2 menu = mockConstructor.constructed().getLast();
            verify(menu).increaseBarHeight();
        }
    }

    /**
     * Test of setBinCollection method, of class HistogramPane.
     */
    @Test
    public void testSetBinCollection() {
        System.out.println("setBinCollection");
        final BinCollection binCollection = mock(BinCollection.class);
        final BinIconMode binIconMode = BinIconMode.ICON;

        try (final MockedConstruction<HistogramDisplay2> mockConstructor = Mockito.mockConstruction(HistogramDisplay2.class)) {
            final HistogramPane instance = new HistogramPane(HistogramController.getDefault());
            instance.setBinCollection(binCollection, binIconMode);

            // Assert HistogramDisplay2 was made and function was called
            assertTrue(mockConstructor.constructed().size() == 1);
            final HistogramDisplay2 menu = mockConstructor.constructed().getLast();
            verify(menu).setBinCollection(binCollection, binIconMode);
        }
    }

    /**
     * Test of setBinSelectionMode method, of class HistogramPane.
     */
    @Test
    public void testSetBinSelectionMode() {
        System.out.println("setBinSelectionMode");
        final BinSelectionMode binSelectionMode = BinSelectionMode.FREE_SELECTION;

        try (final MockedConstruction<HistogramDisplay2> mockConstructor = Mockito.mockConstruction(HistogramDisplay2.class)) {
            final HistogramPane instance = new HistogramPane(HistogramController.getDefault());
            instance.setBinSelectionMode(binSelectionMode);

            // Assert HistogramDisplay2 was made and function was called
            assertTrue(mockConstructor.constructed().size() == 1);
            final HistogramDisplay2 menu = mockConstructor.constructed().getLast();
            verify(menu).setBinSelectionMode(binSelectionMode);
        }
    }

    /**
     * Test of updateBinCollection method, of class HistogramPane.
     */
    @Test
    public void testUpdateBinCollection() {
        System.out.println("updateBinCollection");
        try (final MockedConstruction<HistogramDisplay2> mockConstructor = Mockito.mockConstruction(HistogramDisplay2.class)) {
            final HistogramPane instance = new HistogramPane(HistogramController.getDefault());
            instance.updateBinCollection();

            // Assert HistogramDisplay2 was made and function was called
            assertTrue(mockConstructor.constructed().size() == 1);
            final HistogramDisplay2 menu = mockConstructor.constructed().getLast();
            verify(menu).updateBinCollection();
        }
    }

    /**
     * Test of setHistogramState method, of class HistogramPane.
     */
    @Test
    public void testSetHistogramState() {
        System.out.println("setHistogramState");
        final HistogramState histogramState = new HistogramState();
        final Map<String, BinCreator> attributes = new HashMap<>();
        final HistogramPane instance = new HistogramPane(HistogramController.getDefault());

        instance.setHistogramState(histogramState, attributes);
    }

    private Pair<HistogramTopComponent2, HistogramPane> createPanehelper() {
        final HistogramTopComponent2 mockTopComponent = mock(HistogramTopComponent2.class);
        final HistogramController controllerSpy = spy(HistogramController.getDefault());
        when(controllerSpy.getParent()).thenReturn(mockTopComponent);

        // Set up instance
        final HistogramPane spy = spy(new HistogramPane(controllerSpy));
        // Stub this method, so variable "isAdjusting" is not set to true
        Mockito.doNothing().when(spy).setHistogramState(any(), any());

        return new Pair<>(mockTopComponent, spy);
    }

    /**
     * Test of saveBinsToGraph method, of class HistogramPane.
     */
    @Test
    public void testSaveBinsToGraph() {
        System.out.println("saveBinsToGraph");

        // Set up instance
        final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
        final HistogramTopComponent2 mockTopComponent = p.getKey();
        final HistogramPane instance = p.getValue();

        instance.saveBinsToGraph();
        verify(mockTopComponent).saveBinsToGraph();
    }

    /**
     * Test of updateBinComparator method, of class HistogramPane.
     */
    @Test
    public void testUpdateBinComparator() {
        System.out.println("updateBinComparator");

        // Set up instance
        final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
        final HistogramTopComponent2 mockTopComponent = p.getKey();
        final HistogramPane instance = p.getValue();

        instance.updateBinComparator();
        verify(mockTopComponent).setBinComparator(any());
    }

///////// ALL ABOVE WORKS
    // SEEMS TO WORK
    /**
     * Test of clearFilter method, of class HistogramPane.
     */
    @Test
    public void testClearFilter() {
        System.out.println("clearFilter");

        // Set up instance
        final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
        final HistogramTopComponent2 mockTopComponent = p.getKey();
        final HistogramPane instance = p.getValue();
        doNothing().when(instance).updateDisplay();

        instance.clearFilter();
        verify(mockTopComponent).clearFilter();
        verify(instance).updateDisplay();
    }

    // SEEMS TO WORK - Update: may not
    /**
     * Test of clearFilter method, of class HistogramPane.
     */
    @Test
    public void testFilterSelection() {
        System.out.println("filterSelection");

        // Set up instance
        final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
        final HistogramTopComponent2 mockTopComponent = p.getKey();
        final HistogramPane instance = p.getValue();
        doNothing().when(instance).updateDisplay();

        instance.filterSelection();
        verify(mockTopComponent).filterOnSelection();
        verify(instance).updateDisplay();
    }
//    // SEEMS TO WORK
//    /**
//     * Test of selectionModeChoiceHandler method, of class HistogramPane.
//     */
//    @Test
//    public void testSelectionModeChoiceHandler() {
//        System.out.println("selectionModeChoiceHandler");
//
//        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
//            // This is added so that the mocked static that we would otherwise be
//            // trying to run in the fx thread is actually invoked properly
//            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
//                ((Runnable) iom.getArgument(0)).run();
//                return null;
//            });
//
//            // Set up instance
//            final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
//            final HistogramTopComponent2 mockTopComponent = p.getKey();
//            final HistogramPane instance = p.getValue();
//
//            instance.selectionModeChoiceHandler();
//            verify(mockTopComponent).setBinSelectionMode(any());
//            verify(instance).updateDisplay();
//        }
//    }
//
//    // SEEMS TO WORK
//    /**
//     * Test of descendingButtonHandler method, of class HistogramPane.
//     */
//    @Test
//    public void testDescendingButtonHandler() {
//        System.out.println("descendingButtonHandler");
//        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
//
//            // This is added so that the mocked static that we would otherwise be
//            // trying to run in the fx thread is actually invoked properly
//            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
//                ((Runnable) iom.getArgument(0)).run();
//                return null;
//            });
//
//            // Set up instance
//            final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
//            final HistogramPane instance = p.getValue();
//
//            instance.descendingButtonHandler();
//            verify(instance).updateDisplay();
//        }
//    }
//
//    // SEEMS TO WORK
//    /**
//     * Test of sortChoiceHandler method, of class HistogramPane.
//     */
//    @Test
//    public void testSortChoiceHandler() {
//        System.out.println("sortChoiceHandler");
//
//        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
//            // This is added so that the mocked static that we would otherwise be
//            // trying to run in the fx thread is actually invoked properly
//            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
//                ((Runnable) iom.getArgument(0)).run();
//                return null;
//            });
//            // Set up instance
//            final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
//            final HistogramPane instance = p.getValue();
//
//            instance.sortChoiceHandler();
//            verify(instance).updateDisplay();
//        }
//    }
//
//    // SEEMS TO WORK
//    /**
//     * Test of selectionModeChoiceHandler method, of class HistogramPane.
//     */
//    @Test
//    public void testPropertyChoiceHandler() {
//        System.out.println("propertyChoiceHandler");
//
//        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
//            // This is added so that the mocked static that we would otherwise be
//            // trying to run in the fx thread is actually invoked properly
//            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
//                ((Runnable) iom.getArgument(0)).run();
//                return null;
//            });
//
//            // Set up instance
//            final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
//            final HistogramTopComponent2 mockTopComponent = p.getKey();
//            final HistogramPane instance = p.getValue();
//
//            instance.propertyChoiceHandler();
//            verify(mockTopComponent).setAttribute(any());
//            verify(instance).updateDisplay();
//        }
//    }
//
//    // FAILS, seemingly
//    /**
//     * Test of actionButtonMousePressed method, of class HistogramPane.
//     */
//    @Test
//    public void testActionButtonMousePressed() {
//        System.out.println("actionButtonMousePressed");
//
//        final ObservableList<MenuItem> mockItems = FXCollections.observableArrayList();
//        final ObjectProperty<Image> mockImageProperty = mock(ObjectProperty.class);// unchecked conversion
//        try (final MockedConstruction<ImageView> mockImageView = Mockito.mockConstruction(ImageView.class, (mock, context) -> {
//            when(mock.imageProperty()).thenReturn(mockImageProperty);
//        }); final MockedConstruction<ContextMenu> mockConstructor = Mockito.mockConstruction(ContextMenu.class, (mock, context) -> {
//            when(mock.getItems()).thenReturn(mockItems);
//        })) {
//            // Set up instance
//            final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
//            final HistogramPane instance = p.getValue();
//
//            instance.actionButtonMousePressed(mock(MouseEvent.class));
//            verify(instance).updateDisplay();
//        }
//    }
//    // SEEMS TO WORK, hang on sometimes it fails
//    // NEW INFO failing even with these two commmented out
//    /**
//     * Test of toggleStateChanged method, of class HistogramPane.
//     */
//    @Test
//    public void testToggleStateChanged() {
//        System.out.println("toggleStateChanged");
//
//        try (final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
//            // This is added so that the mocked static that we would otherwise be
//            // trying to run in the fx thread is actually invoked properly
//            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(iom -> {
//                ((Runnable) iom.getArgument(0)).run();
//                return null;
//            });
//
//            // Set up instance
//            final Pair<HistogramTopComponent2, HistogramPane> p = createPanehelper();
//            final HistogramTopComponent2 mockTopComponent = p.getKey();
//            final HistogramPane instance = p.getValue();
//
//            instance.toggleStateChanged(null);
//            verify(mockTopComponent).setGraphElementType(any());
//            verify(instance).updateDisplay();
//        }
//    }
}
