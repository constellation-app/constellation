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

import java.awt.GraphicsEnvironment;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DraggableCellNGTest {
    
    public DraggableCellNGTest() {
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
     * Test of updateItem method, of class DraggableCell with null item.
     */
    @Test
    public void testUpdateItemNullItem() {
        System.out.println("updateItemNullItem");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            new JFXPanel();
            
            final DraggableCell<String> instance = new DraggableCell<>();
            instance.updateItem(null, false);

            assertEquals(instance.getText(), null);
            assertEquals(instance.getItem(), null);
            assertEquals(instance.isEmpty(), false);
        }
    }

    /**
     * Test of updateItem method, of class DraggableCell.
     */
    @Test
    public void testUpdateItem() {
        System.out.println("updateItem");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            new JFXPanel();
            
            final DraggableCell<String> instance = new DraggableCell<>();
            final String item = "test";
            instance.updateItem(item, true);

            assertEquals(instance.getText(), "test");
            assertEquals(instance.getItem(), item);
            assertEquals(instance.isEmpty(), true);
        }
    }
    
    /**
     * Test of onDragOver Listener
     */
    @Test
    public void testOnDragOver() {
        System.out.println("onDragOver");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            new JFXPanel();
            
            final DraggableCell<String> instance = new DraggableCell<>();
            
            // mocking the required dragboard
            final Dragboard dragboard = mock(Dragboard.class);
            when(dragboard.hasString()).thenReturn(true);
            final Set<TransferMode> transferModes = new HashSet<>();
            transferModes.add(TransferMode.MOVE);
            transferModes.add(TransferMode.LINK);
            when(dragboard.getTransferModes()).thenReturn(transferModes);
            
            // NOTE: transfer mode set to something other than move to demonstrate test doesn't require event to to be move mode
            final DragEvent event  = new DragEvent(null, dragboard, 0, 0, 0, 0, TransferMode.COPY, null, null, null);
            
            instance.getOnDragOver().handle(event);
            
            assertEquals(event.isConsumed(), true);
            assertEquals(event.getAcceptedTransferMode(), TransferMode.MOVE);
        }
    }
    
    /**
     * Test of onDragEntered Listener
     */
    @Test
    public void testOnDragEntered() {
        System.out.println("onDragEntered");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            new JFXPanel();
            
            final DraggableCell<String> instance = new DraggableCell<>();
            
            // mocking the required dragboard
            final Dragboard dragboard = mock(Dragboard.class);
            when(dragboard.hasString()).thenReturn(true);
            
            final DragEvent event  = new DragEvent(null, dragboard, 0, 0, 0, 0, null, null, null, null);
            
            instance.getOnDragEntered().handle(event);
            
            assertEquals(instance.getOpacity(), 0.3);
        }
    }
    
    /**
     * Test of onDragExited Listener
     */
    @Test
    public void testOnDragExited() {
        System.out.println("onDragExited");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            new JFXPanel();
            
            final DraggableCell<String> instance = new DraggableCell<>();
            // opacity is 1 by default so setting to a different value to ensure we know the value changed to 1 in the end
            instance.setOpacity(0.5);
            
            // mocking the required dragboard
            final Dragboard dragboard = mock(Dragboard.class);
            when(dragboard.hasString()).thenReturn(true);
            
            final DragEvent event  = new DragEvent(null, dragboard, 0, 0, 0, 0, null, null, null, null);
            
            instance.getOnDragExited().handle(event);
            
            assertEquals(instance.getOpacity(), 1.0);
        }
    }
    
    /**
     * Test of onDragDropped Listener
     */
    @Test
    public void testOnDragDropped() {
        System.out.println("onDragDropped");
        
        // TODO: Find a way to instantiate toolkit in a headless environment
        // This unit test should pass locally but will hold up in a headless environment (e.g. CI)
        // Putting it in this if loop is a temp fix for the hold up in CI but it does it by skipping the test 
        // (so CI won't actually tell you whether this is passing or not)
        if (!GraphicsEnvironment.isHeadless()) {
            new JFXPanel();
            
            final DraggableCell<String> instance = new DraggableCell<>();
            final ListView<String> list = new ListView<>();
            list.setItems(FXCollections.observableArrayList("t1", "t2", "t3"));
            instance.updateListView(list);
            instance.setItem("t1");
                       
            // mocking the required dragboard
            final Dragboard dragboard = mock(Dragboard.class);
            when(dragboard.hasString()).thenReturn(true);
            when(dragboard.getString()).thenReturn("1");
            
            final DragEvent event  = new DragEvent(DragEvent.DRAG_DROPPED, dragboard, 0, 0, 0, 0, null, instance, null, null);
            
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
}
