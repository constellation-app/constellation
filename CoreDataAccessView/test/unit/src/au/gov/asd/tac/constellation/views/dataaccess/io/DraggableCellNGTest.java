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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DraggableCellNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(DraggableCellNGTest.class.getName());

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    /**
     * Test of updateItem method, of class DraggableCell with null item.
     */
    @Test
    public void testUpdateItemNullItem() {
        System.out.println("updateItemNullItem");

        final DraggableCell<String> instance = new DraggableCell<>();
        instance.updateItem(null, false);

        assertEquals(instance.getText(), null);
        assertEquals(instance.getItem(), null);
        assertEquals(instance.isEmpty(), false);
    }

    /**
     * Test of updateItem method, of class DraggableCell.
     */
    @Test
    public void testUpdateItem() {
        System.out.println("updateItem");

        final DraggableCell<String> instance = new DraggableCell<>();
        final String item = "test";
        instance.updateItem(item, true);

        assertEquals(instance.getText(), "test");
        assertEquals(instance.getItem(), item);
        assertEquals(instance.isEmpty(), true);
    }

    /**
     * Test of onDragOver Listener
     */
    @Test
    public void testOnDragOver() {
        System.out.println("onDragOver");

        final DraggableCell<String> instance = new DraggableCell<>();

        // mocking the required dragboard
        final Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasString()).thenReturn(true);
        final Set<TransferMode> transferModes = new HashSet<>();
        transferModes.add(TransferMode.MOVE);
        transferModes.add(TransferMode.LINK);
        when(dragboard.getTransferModes()).thenReturn(transferModes);

        // NOTE: transfer mode set to something other than move to demonstrate test doesn't require event to to be move mode
        final DragEvent event = new DragEvent(null, dragboard, 0, 0, 0, 0, TransferMode.COPY, null, null, null);

        instance.getOnDragOver().handle(event);

        assertEquals(event.isConsumed(), true);
        assertEquals(event.getAcceptedTransferMode(), TransferMode.MOVE);
    }

    /**
     * Test of onDragEntered Listener
     */
    @Test
    public void testOnDragEntered() {
        System.out.println("onDragEntered");

        final DraggableCell<String> instance = new DraggableCell<>();

        // mocking the required dragboard
        final Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasString()).thenReturn(true);

        final DragEvent event = new DragEvent(null, dragboard, 0, 0, 0, 0, null, null, null, null);

        instance.getOnDragEntered().handle(event);

        assertEquals(instance.getOpacity(), 0.3);
    }

    /**
     * Test of onDragExited Listener
     */
    @Test
    public void testOnDragExited() {
        System.out.println("onDragExited");

        final DraggableCell<String> instance = new DraggableCell<>();
        // opacity is 1 by default so setting to a different value to ensure we know the value changed to 1 in the end
        instance.setOpacity(0.5);

        // mocking the required dragboard
        final Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasString()).thenReturn(true);

        final DragEvent event = new DragEvent(null, dragboard, 0, 0, 0, 0, null, null, null, null);

        instance.getOnDragExited().handle(event);

        assertEquals(instance.getOpacity(), 1.0);
    }

    /**
     * Test of onDragDropped Listener
     */
    @Test
    public void testOnDragDropped() {
        System.out.println("onDragDropped");

        final DraggableCell<String> instance = new DraggableCell<>();
        final ListView<String> list = new ListView<>();
        list.setItems(FXCollections.observableArrayList("t1", "t2", "t3"));
        instance.updateListView(list);
        instance.setItem("t1");

        // mocking the required dragboard
        final Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasString()).thenReturn(true);
        when(dragboard.getString()).thenReturn("1");

        final DragEvent event = new DragEvent(DragEvent.DRAG_DROPPED, dragboard, 0, 0, 0, 0, null, instance, null, null);

        final ObservableList<String> listViewItems = instance.getListView().getItems();

        assertEquals(listViewItems.get(0), "t1");
        assertEquals(listViewItems.get(1), "t2");
        assertEquals(listViewItems.get(2), "t3");

        instance.getOnDragDropped().handle(event);

        assertEquals(event.isConsumed(), true);
        assertEquals(event.isDropCompleted(), true);
        assertEquals(listViewItems.get(0), "t2");
        assertEquals(listViewItems.get(1), "t1");
        assertEquals(listViewItems.get(2), "t3");
    }
}
