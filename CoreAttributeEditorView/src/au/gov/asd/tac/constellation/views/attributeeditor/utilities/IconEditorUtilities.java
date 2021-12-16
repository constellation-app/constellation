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
package au.gov.asd.tac.constellation.views.attributeeditor.utilities;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for IconEditor.
 *
 * @author sol695510
 */
public class IconEditorUtilities {

    private IconEditorUtilities() {
    }

    /**
     * Calls pngWalk(path, files) with given file path and returns a list of png
     * files if found in the directory.
     *
     * @param path
     * @return list of png files
     */
    public static List<File> pngWalk(final File path) {
        return pngWalk(path, new ArrayList<>());
    }

    /**
     * Checks if given file path is a directory and returns a list of png files
     * if found in the directory.
     *
     * @param path
     * @param files
     * @return list of png files
     */
    protected static List<File> pngWalk(final File path, final List<File> files) {
        final List<File> filesInPath = Arrays.stream(path.listFiles()).collect(Collectors.toList());
        final List<File> filesToAdd = new ArrayList<>();

        // Get directories to be searched or PNG files to be saved.
        filesInPath.forEach(file -> {
            if (file.isDirectory()) {
                filesToAdd.add(file);
            } else if (StringUtils.endsWithIgnoreCase(file.getAbsolutePath(), FileExtensionConstants.PNG)) {
                filesToAdd.add(file);
            }
        });

        // If file is a directory, recursively call this method to search for more PNG files.
        filesToAdd.forEach(file -> {
            if (file.isDirectory()) {
                pngWalk(file, files);
            } else {
                files.add(file);
            }
        });

        return files;
    }
}
