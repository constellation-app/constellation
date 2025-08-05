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
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * A DataObject representing the FileObject that a Graph was read from.
 *
 * @author algol
 */
@Messages({"LBL_Graph_LOADER=Files of Graph"})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Graph_LOADER",
        mimeType = "application/x-graph",
        extension = {"star"},
        position = 0)
@DataObject.Registration(
        mimeType = "application/x-graph",
        iconBase = "au/gov/asd/tac/constellation/graph/file/constellation.png",
        displayName = "#LBL_Graph_LOADER",
        position = 0)
@ActionReferences({
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/application/x-graph/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public final class GraphDataObject extends MultiDataObject implements OpenCookie {

    private static final Logger LOGGER = Logger.getLogger(GraphDataObject.class.getName());

    /**
     * Filename extension for graph files.
     */
    public static final String FILE_EXTENSION = ".star";

    private NebulaDataObject gdo;

    /**
     * If this graph is part of a graph, assign a color to it.
     */
    private Color graphColor;

    /* for file lock control */
    private FileLock fileLock;
    private FileChannel fileChannel;

    /**
     * Create a MultiFileObject.
     *
     * @param primaryFile The primary file object.
     * @param loader Loader of this data object.
     *
     * @throws DataObjectExistsException When data object exists.
     * @throws IOException When an I/O error occurs.
     */
    public GraphDataObject(final FileObject primaryFile, final MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(primaryFile, loader);
        gdo = null;
        graphColor = null;
    }

    @Override
    protected Node createNodeDelegate() {
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    /**
     * Enable this DataObject to be opened using the standard NetBeans File
     * &rarr; Open File menu.
     */
    @Override
    public void open() {
        GraphOpener.getDefault().openGraph(this);
    }

    /**
     * Is this DataObject backed by an in-memory file?
     *
     * @return True if backed by an in-memory file, false otherwise.
     */
    public boolean isInMemory() {
        try {
            return "MemoryFileSystem".equals(getPrimaryFile().getFileSystem().getDisplayName());
        } catch (final FileStateInvalidException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        return false;
    }

    /**
     * Return a tooltip for this GraphDataObject.
     *
     * @return A tooltip for this GraphDataObject.
     */
    public String getToolTipText() {
        if (isInMemory()) {
            return getPrimaryFile().getName() + " (unsaved)";
        }

        String s = FileUtil.getFileDisplayName(getPrimaryFile());
        final NebulaDataObject nebulaDataObject = getNebulaDataObject();
        if (nebulaDataObject != null) {
            s = String.format("%s - %s", nebulaDataObject.getName(), s);
        }

        return s;
    }

    public NebulaDataObject getNebulaDataObject() {
        return gdo;
    }

    public void setNebulaDataObject(final NebulaDataObject graphDataObject) {
        this.gdo = graphDataObject;
    }

    public Color getNebulaColor() {
        return graphColor;
    }

    public void setNebulaColor(final Color graphColor) {
        this.graphColor = graphColor;
    }

    /**
     * Lock the primary file stored in this GraphDataObject so that it cannot
     * be manipulated external to this app.
     */
    public void lockFile() {
        // Get a file lock
        final File graphFile = FileUtil.toFile(getPrimaryFile());
        if (graphFile != null) {
            try {
                final RandomAccessFile randomaccessfile = new RandomAccessFile(graphFile.getAbsoluteFile(), "rw");
                final FileChannel channel = randomaccessfile.getChannel();
                final FileLock lock = channel.tryLock(0, Long.MAX_VALUE, false);
                if (lock != null) {
                    setFileLock(lock);
                    setFileChannel(channel);
                } else {
                    LOGGER.log(Level.SEVERE, "Lock on file {0} cannot be obtained", graphFile.getAbsoluteFile());
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Release the lock on the primary file currently stored in this GraphDataObject.
     */
    public void unlockFile() {
        final FileLock lock = getFileLock();

        if (lock != null) {
            try {
                lock.release();
                if (fileChannel != null && fileChannel.isOpen()) {
                    fileChannel.close();
                }
            } catch (final IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * @return the channel
     */
    public FileChannel getFileChannel() {
        return fileChannel;
    }

    /**
     * @param fileChannel the channel to set
     */
    public void setFileChannel(final FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    /**
     * @return the fileLock
     */
    public FileLock getFileLock() {
        return fileLock;
    }

    /**
     * @param channelLock the fileLock to set
     */
    public void setFileLock(final FileLock channelLock) {
        this.fileLock = channelLock;
    }
}
