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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.List;
import javax.swing.SwingUtilities;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.windows.WindowManager;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ShowInTableViewContextMenuProviderNGTest {

    @Test
    public void getMenuPath() {
        assertTrue(new ShowInTableViewContextMenuProvider()
                .getMenuPath(GraphElementType.META).isEmpty());
    }

    @Test
    public void getItems() {
        final ShowInTableViewContextMenuProvider provider = new ShowInTableViewContextMenuProvider();
        assertEquals(provider.getItems(null, GraphElementType.VERTEX, -1), List.of("Show in Table View"));
        assertEquals(provider.getItems(null, GraphElementType.TRANSACTION, -1), List.of("Show in Table View"));
        assertTrue(provider.getItems(null, GraphElementType.META, -1).isEmpty());
    }

    @Test
    public void selectItemTopComponentNotOpen() {
        try (
                final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class);
                final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            final TableViewTopComponent tc = mock(TableViewTopComponent.class);

            swingUtilsMockedStatic.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        final Runnable runnable = invocation.getArgument(0);

                        runnable.run();

                        verify(tc).open();
                        verify(tc).requestActive();
                        verify(tc).showSelected(GraphElementType.META, 42);

                        return null;
                    });

            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);

            when(windowManager.findTopComponent("TableViewTopComponent")).thenReturn(tc);
            when(tc.isOpened()).thenReturn(false);

            new ShowInTableViewContextMenuProvider().selectItem(null, null, GraphElementType.META, 42, null);
        }
    }

    @Test
    public void selectItemTopComponentOpen() {
        try (
                final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class);
                final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            final TableViewTopComponent tc = mock(TableViewTopComponent.class);

            swingUtilsMockedStatic.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        final Runnable runnable = invocation.getArgument(0);

                        runnable.run();

                        verify(tc, times(0)).open();
                        verify(tc).requestActive();
                        verify(tc).showSelected(GraphElementType.META, 42);

                        return null;
                    });

            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);

            when(windowManager.findTopComponent("TableViewTopComponent")).thenReturn(tc);
            when(tc.isOpened()).thenReturn(true);

            new ShowInTableViewContextMenuProvider().selectItem(null, null, GraphElementType.META, 42, null);
        }
    }

    @Test
    public void selectItemTopComponentNull() {
        try (
                final MockedStatic<SwingUtilities> swingUtilsMockedStatic = Mockito.mockStatic(SwingUtilities.class);
                final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class)) {
            final WindowManager windowManager = mock(WindowManager.class);

            swingUtilsMockedStatic.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                    .thenAnswer(invocation -> {
                        final Runnable runnable = invocation.getArgument(0);

                        runnable.run();

                        return null;
                    });

            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);

            when(windowManager.findTopComponent("TableViewTopComponent")).thenReturn(null);

            new ShowInTableViewContextMenuProvider().selectItem(null, null, GraphElementType.META, 42, null);
        }
    }
}
