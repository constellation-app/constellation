/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Memory Management Capability
 *
 * @author algol
 */
public class GraphObjectUtilities {

    // This is the in-memory filesystem we use to store files for DataObjects.
    private static final FileSystem FILE_SYSTEM = FileUtil.createMemoryFileSystem();

    private static int fileCounter = 0;

    /**
     * Create a new DataObject backed by an in-memory file.
     * <p>
     * When a new graph is created, we don't want to first ask the user where it
     * should be stored. (The user might not want to store it anywhere.)
     * However, NetBeans pretty much requires that a DataObject be backed by a
     * file.
     * <p>
     * To meet both of these requirements, we'll create new graphs in an
     * in-memory filesystem, backed by an empty file. Obviously the graph should
     * not then be saved in-memory: callers must be aware of this and only allow
     * "Save As..." until the graph is safely backed by a real file.
     * <p>
     * If numbered is true, the name of the graph will be name%d.star, where
     * name is passed as a parameter, and %d is an internal static counter, to
     * guarantee uniqueness. If numbered is false, the name will be simply
     * name.star.
     *
     * @param name The name to give the graph file. The extension will be added
     * to the name.
     * @param numbered If true, a monotonically increasing number will be
     * appended to the name.
     *
     * @return A GraphDataObject backed by an in-memory file.
     */
    public static GraphDataObject createMemoryDataObject(final String name, final boolean numbered) {
        GraphDataObject gdo = null;
        try {
            final String fnam = numbered ? String.format("%s%d%s", name, ++fileCounter, GraphDataObject.FILE_EXTENSION) : String.format("%s%s", name, GraphDataObject.FILE_EXTENSION);
            final FileObject root = FILE_SYSTEM.getRoot();
            final FileObject fo = FileUtil.createData(root, fnam);
            gdo = (GraphDataObject) DataObject.find(fo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return gdo;
    }
}
