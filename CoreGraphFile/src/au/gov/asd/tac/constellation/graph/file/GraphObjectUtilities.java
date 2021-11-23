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
package au.gov.asd.tac.constellation.graph.file;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.apache.commons.io.FilenameUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Memory Management Capability
 *
 * @author algol
 */
public class GraphObjectUtilities {
    
    private static final Logger LOGGER = Logger.getLogger(GraphObjectUtilities.class.getName());

    // This is the in-memory filesystem we use to store files for DataObjects.
    private static final FileSystem FILE_SYSTEM = FileUtil.createMemoryFileSystem();

    private static int fileCounter = 0;

    //Constants for dealing with copying of filenames
    private static final String COPY_STRING = " - Copy";
    private static final String COPY_STRING_PATTERN = "^(.{1,100} - Copy) \\((\\d{1,3})\\)$";
    private static final Pattern COPY_NAME_MATCHER = Pattern.compile(COPY_STRING_PATTERN);

    private static final int FILENAME_LENGTH_LIMIT = 100;

    private static final String CHOOSE_FILENAME = "Please enter a shorter filename:";
    private static final String FILENAME_TITLE = "Filename";

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
        final FileObject root = FILE_SYSTEM.getRoot();
        try {
            String fnam = getNewFileName(name, numbered, root);
            while (isFileNameDuplicateInMemory(fnam, root)) {
                //If after all of the above the filename already exists in memory, start again.
                fnam = getNewFileName(fnam, numbered, root);
            }
            while (fnam.length() >= FILENAME_LENGTH_LIMIT) {
                fnam = (String) JOptionPane.showInputDialog(null,
                        CHOOSE_FILENAME,
                        FILENAME_TITLE,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        null,
                        fnam);
            }
            fnam = String.format("%s%s", fnam, FileExtensionConstants.STAR);
            final FileObject fo = FileUtil.createData(root, fnam);
            gdo = (GraphDataObject) DataObject.find(fo);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
        return gdo;
    }

    private static String getNewFileName(final String name, final boolean numbered, final FileObject root) {
        final List<FileObject> files = Arrays.stream(root.getChildren()).filter(file -> file.getName().equals(name)).collect(Collectors.toList());
        if (files.size() > 0) {
            if (name.endsWith(COPY_STRING)) {
                return String.format("%s (%d)", name, 1);
            }

            final Matcher matcher = COPY_NAME_MATCHER.matcher(name);
            if (matcher.matches()) {
                final String fileNamePart = matcher.group(1);
                final int copyNum = Integer.parseInt(matcher.group(2));
                return String.format("%s (%d)", fileNamePart, copyNum + 1);
            }
            return String.format("%s - Copy", name);
        }
        return numbered ? String.format("%s%d", name, ++fileCounter) : String.format("%s", name);
    }

    private static boolean isFileNameDuplicateInMemory(final String name, final FileObject root) {
        final String tempName = FilenameUtils.getBaseName(name);
        final List<FileObject> files = Arrays.stream(root.getChildren()).filter(file -> file.getName().equals(tempName)).collect(Collectors.toList());
        return !files.isEmpty();
    }
}
