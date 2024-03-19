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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.plugins.Plugin;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class PluginFinderNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(PluginFinderNGTest.class.getName());

    private final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);

    private PluginFinder pluginFinder;

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
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        reset(queryPhasePane);

        pluginFinder = spy(new PluginFinder());
    }

    @Test
    public void find() {
        final String pluginName1 = "plugin 1";
        final String pluginName2 = "plugin 2";

        final DataSourceTitledPane tp1 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane tp2 = mock(DataSourceTitledPane.class);
        
        final Plugin plugin = mock(Plugin.class);
        
        final Alert alert = mock(Alert.class);
        final DialogPane dialogPane = mock(DialogPane.class);

        when(alert.getDialogPane()).thenReturn(dialogPane);
        doReturn(alert).when(pluginFinder).createAlertDialog();

        when(queryPhasePane.getDataAccessPanes()).thenReturn(List.of(tp1, tp2));
        
        when(tp1.getPlugin()).thenReturn(plugin);
        when(tp2.getPlugin()).thenReturn(plugin);
        
        when(plugin.getName()).thenReturn(pluginName1).thenReturn(pluginName2);

        pluginFinder.find(queryPhasePane);

        final ArgumentCaptor<VBox> captor = ArgumentCaptor.forClass(VBox.class);
        verify(dialogPane).setContent(captor.capture());

        assertEquals(
                ((ListView) captor.getValue().getChildren().get(1)).getItems(),
                FXCollections.observableArrayList(pluginName1, pluginName2)
        );

        // TODO Figure out a way to simply trigger the event actions so that
        //      the global property "result" is set.
    }

    @Test
    public void keyEventHandler() {
        final Alert alert = mock(Alert.class);
        final ListView<String> listView = mock(ListView.class);

        final KeyEvent event = mock(KeyEvent.class);

        final MultipleSelectionModel<String> selectionModel = mock(MultipleSelectionModel.class);
        final ObservableList<String> items = mock(ObservableList.class);

        final PluginFinder.KeyEventHandler keyEventHandler
                = pluginFinder.new KeyEventHandler(alert, listView);

        when(event.getCode()).thenReturn(KeyCode.ENTER);

        when(listView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(items);
        when(items.size()).thenReturn(1);

        keyEventHandler.handle(event);

        // TODO Figure out a way to access the global property result in order
        //      to verify its value
        verify(alert).setResult(ButtonType.OK);
    }

    @Test
    public void mouseEventHandlerTwoClicks() {
        final Alert alert = mock(Alert.class);
        final ListView<String> listView = mock(ListView.class);

        final MouseEvent event = mock(MouseEvent.class);

        final MultipleSelectionModel<String> selectionModel = mock(MultipleSelectionModel.class);

        final PluginFinder.MouseEventHandler mouseEventHandler
                = pluginFinder.new MouseEventHandler(alert, listView);

        when(event.getClickCount()).thenReturn(2);

        when(listView.getSelectionModel()).thenReturn(selectionModel);

        mouseEventHandler.handle(event);

        // TODO Figure out a way to access the global property result in order
        //      to verify its value
        verify(alert).setResult(ButtonType.OK);
    }

    @Test
    public void mouseEventHandlerOneClick() {
        final Alert alert = mock(Alert.class);
        final ListView<String> listView = mock(ListView.class);

        final MouseEvent event = mock(MouseEvent.class);

        final MultipleSelectionModel<String> selectionModel = mock(MultipleSelectionModel.class);
        final ObservableList<String> items = mock(ObservableList.class);

        final PluginFinder.MouseEventHandler mouseEventHandler
                = pluginFinder.new MouseEventHandler(alert, listView);

        when(event.getClickCount()).thenReturn(1);

        when(listView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(items);
        when(items.size()).thenReturn(1);

        mouseEventHandler.handle(event);

        // TODO Figure out a way to access the global property result in order
        //      to verify its value
    }

    @Test
    public void testFieldChangedListenerSingleFiltered() {
        final ObservableList<String> texts = FXCollections.observableArrayList(
                "a substring of a string",
                "something else"
        );

        final ObservableList<String> observableList = mock(ObservableList.class);
        final MultipleSelectionModel<String> selectionModel = mock(MultipleSelectionModel.class);
        final ListView<String> listView = mock(ListView.class);

        when(listView.getSelectionModel()).thenReturn(selectionModel);

        when(listView.getItems()).thenReturn(observableList);
        when(observableList.size()).thenReturn(1);

        final PluginFinder.TextFieldChangeListener textFieldChangeListener
                = pluginFinder.new TextFieldChangeListener(texts, listView);

        try (MockedStatic<FXCollections> fxCollectionsMockStatic
                = Mockito.mockStatic(FXCollections.class)) {
            fxCollectionsMockStatic.when(() -> FXCollections.observableArrayList(List.of("a substring of a string")))
                    .thenReturn(observableList);

            textFieldChangeListener.changed(null, null, "substring");

            verify(listView).setItems(observableList);
            verify(selectionModel).select(0);

            // TODO Figure out a way to access the global property result in order
            //      to verify its value
        }
    }

    @Test
    public void testFieldChangedListenerMultipleFiltered() {
        final ObservableList<String> texts = FXCollections.observableArrayList(
                "a substring of a string",
                "something else",
                "a different substring"
        );

        final ObservableList<String> observableList = mock(ObservableList.class);
        final MultipleSelectionModel<String> selectionModel = mock(MultipleSelectionModel.class);
        final ListView<String> listView = mock(ListView.class);

        when(listView.getSelectionModel()).thenReturn(selectionModel);

        when(listView.getItems()).thenReturn(observableList);
        when(observableList.size()).thenReturn(5);

        final PluginFinder.TextFieldChangeListener textFieldChangeListener
                = pluginFinder.new TextFieldChangeListener(texts, listView);

        try (MockedStatic<FXCollections> fxCollectionsMockStatic
                = Mockito.mockStatic(FXCollections.class)) {
            fxCollectionsMockStatic.when(() -> FXCollections.observableArrayList(List.of("a substring of a string", "a different substring")))
                    .thenReturn(observableList);

            textFieldChangeListener.changed(null, null, "substring");

            verify(listView).setItems(observableList);
            verify(selectionModel).clearSelection();

            // TODO Figure out a way to access the global property result in order
            //      to verify its value
        }
    }

    @Test
    public void testFieldChangedListenerNewValueEmpty() {
        final ObservableList<String> texts = FXCollections.observableArrayList(
                "a substring of a string",
                "something else",
                "a different substring"
        );

        final ObservableList<String> observableList = mock(ObservableList.class);
        final MultipleSelectionModel<String> selectionModel = mock(MultipleSelectionModel.class);
        final ListView<String> listView = mock(ListView.class);

        when(listView.getSelectionModel()).thenReturn(selectionModel);

        when(listView.getItems()).thenReturn(observableList);
        when(observableList.size()).thenReturn(5);

        final PluginFinder.TextFieldChangeListener textFieldChangeListener
                = pluginFinder.new TextFieldChangeListener(texts, listView);

        textFieldChangeListener.changed(null, null, "");

        verify(listView).setItems(texts);
        verify(selectionModel).clearSelection();

        // TODO Figure out a way to access the global property result in order
        //      to verify its value
    }
}
