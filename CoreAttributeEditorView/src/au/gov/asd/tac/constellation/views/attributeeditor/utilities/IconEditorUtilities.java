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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for IconEditor.
 *
 * @author sol695510
 */
public class IconEditorUtilities {

    private IconEditorUtilities() {
    }

    /**
     * Calls pngWalk() with given file path and returns a list of png files.
     *
     * @param path
     * @return list of png files
     */
    public static List<File> pngWalk(final File path) {
        final List<File> files = new ArrayList<>();
        pngWalk(path, files);
        return files;
    }

    /**
     * Checks if given file path is a directory and returns a list of png files
     * if found in the directory.
     *
     * @param path
     * @param files
     * @return list of png files
     */
    private static List<File> pngWalk(final File path, final List<File> files) {
        final File[] filesInPath = path.listFiles((File pathname) -> {
            if (pathname.isDirectory()) {
                return true;
            } else {
                final String filename = pathname.getAbsolutePath();
                return filename.endsWith(".png") || filename.endsWith(".PNG");
            }
        });

        for (final File file : filesInPath) {
            if (file.isDirectory()) {
                pngWalk(file, files);
            } else {
                files.add(file);
            }
        }

        return files;
    }
}
