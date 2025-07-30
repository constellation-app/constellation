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
package au.gov.asd.tac.constellation.graph.file;

import au.gov.asd.tac.constellation.graph.file.nebula.NebulaDataObject;
import java.awt.Color;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.nodes.Node;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author andromeda
 */
public class GraphDataObjectNGTest {

    public GraphDataObjectNGTest() {
    }

    @Test
    public void createNodeDelegateTest() {
        final GraphDataObject gdoMock = mock(GraphDataObject.class);
        final Node nodeMock = mock(DataNode.class);
        when(gdoMock.createNodeDelegate()).thenReturn(nodeMock);
        final Node node = gdoMock.createNodeDelegate();
        assertTrue(node == nodeMock);
        verify(gdoMock, times(1)).createNodeDelegate();
    }

    @Test
    public void fileChannelTest() throws IOException {
        final GraphDataObject gdoMock = mock(GraphDataObject.class);
        final FileChannel fileChannelMock = mock(FileChannel.class);
        doCallRealMethod().when(gdoMock).setFileChannel(Mockito.any());
        doCallRealMethod().when(gdoMock).getFileChannel();

        gdoMock.setFileChannel(fileChannelMock);
        assertEquals(gdoMock.getFileChannel(), fileChannelMock);
    }

    @Test
    public void nebulaObjectTest() throws IOException {
        final GraphDataObject gdoMock = mock(GraphDataObject.class);
        final NebulaDataObject nebulaDataObjectMock = mock(NebulaDataObject.class);
        gdoMock.setNebulaDataObject(nebulaDataObjectMock);
        verify(gdoMock, times(1)).setNebulaDataObject(nebulaDataObjectMock);

        NebulaDataObject nebulaDataObject = gdoMock.getNebulaDataObject();
        assertEquals(null, nebulaDataObject);
        when(gdoMock.getNebulaDataObject()).thenReturn(nebulaDataObjectMock);
        nebulaDataObject = gdoMock.getNebulaDataObject();
        assertEquals(nebulaDataObject, nebulaDataObjectMock);
        verify(gdoMock, times(2)).getNebulaDataObject();
    }

    @Test
    public void nebulaColortTest() throws IOException {
        final GraphDataObject gdoMock = mock(GraphDataObject.class);
        final Color nebulaColorMock = mock(Color.class);
        gdoMock.setNebulaColor(nebulaColorMock);
        verify(gdoMock, times(1)).setNebulaColor(nebulaColorMock);

        Color nebulaColor = gdoMock.getNebulaColor();
        assertEquals(null, nebulaColor);
        when(gdoMock.getNebulaColor()).thenReturn(nebulaColorMock);
        nebulaColor = gdoMock.getNebulaColor();
        assertEquals(nebulaColor, nebulaColorMock);
        verify(gdoMock, times(2)).getNebulaColor();
    }

    @Test
    public void fileLockTest() {
        final GraphDataObject gdoMock = mock(GraphDataObject.class);
        final FileLock fileLockMock = mock(FileLock.class);
        doCallRealMethod().when(gdoMock).setFileLock(Mockito.any());
        doCallRealMethod().when(gdoMock).getFileLock();

        gdoMock.setFileLock(fileLockMock);
        assertTrue(gdoMock.getFileLock() == fileLockMock);
    }

    @Test
    public void getToolTipTextTest() throws IOException {

        try (final MockedStatic<FileUtil> fileUtilMockStatic = mockStatic(FileUtil.class)) {
            final GraphDataObject gdoMock = mock(GraphDataObject.class);
            final NebulaDataObject nebDataOjMock = mock(NebulaDataObject.class);
            doCallRealMethod().when(gdoMock).setNebulaDataObject(nebDataOjMock);

            final FileObject fileObjectMock = mock(FileObject.class);
            // test when in memory
            when(gdoMock.isInMemory()).thenReturn(true);
            when(gdoMock.getPrimaryFile()).thenReturn(fileObjectMock);
            when(fileObjectMock.getName()).thenReturn("TestFileName");
            doCallRealMethod().when(gdoMock).getToolTipText();

            String toolTipText = gdoMock.getToolTipText();
            assertEquals(toolTipText, "TestFileName (unsaved)");

            // test when is not in memory
            when(gdoMock.getNebulaDataObject()).thenReturn(nebDataOjMock);
            fileUtilMockStatic.when(() -> FileUtil.getFileDisplayName(fileObjectMock)).thenReturn("TestFileName2");

            when(gdoMock.isInMemory()).thenReturn(false);
            when(gdoMock.getNebulaDataObject()).thenReturn(nebDataOjMock);
            when(nebDataOjMock.getName()).thenReturn("gdoName");

            toolTipText = gdoMock.getToolTipText();
            assertEquals(toolTipText, "gdoName - TestFileName2");
        }
    }
}
