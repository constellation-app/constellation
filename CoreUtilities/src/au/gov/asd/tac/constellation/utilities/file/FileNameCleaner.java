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
package au.gov.asd.tac.constellation.utilities.file;

import java.util.Arrays;

/**
 * A utility created to help with preparing a procedurally generated file name.
 * This utility will simply remove all characters from a file name that are illegal.
 * 
 * @author capricornunicorn123
 */
public class FileNameCleaner {
    
    //Ensure this list is sorted, as required for binary search.
    private static final char[] ILLEGAL_CHARACTERS = {34, 42, 47, 58, 60, 62, 63, 92, 124};
    
    private FileNameCleaner(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Removes any illegal characters form the filename.
     * Note the file name should not include the path. as this method removes
     * path separators.
     * @param fileName
     * @return 
     */
    public static String cleanFileName(final String fileName) {
        Arrays.sort(ILLEGAL_CHARACTERS);
        final StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < fileName.length(); i++) {
            final char c = fileName.charAt(i);
            if (Arrays.binarySearch(ILLEGAL_CHARACTERS, c) < 0) {
                cleanName.append(c);
            }
        }
        return cleanName.toString();
    }
}
