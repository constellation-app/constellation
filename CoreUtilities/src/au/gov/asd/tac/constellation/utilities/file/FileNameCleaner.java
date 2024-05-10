/*
* Copyright 2010-2023 Australian Signals Directorate
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
 *
 * @author capricornunicorn123
 */
public class FileNameCleaner {
    
    private static final char[] ILLEGAL_CHARACTERS = {'/','\\',':','*','?','"','<','>','|'};
    
    private FileNameCleaner(){
        throw new IllegalStateException("Utility class");
    }

    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            char c = badFileName.charAt(i);
            if (Arrays.binarySearch(ILLEGAL_CHARACTERS, c) < 0) {
                cleanName.append(c);
            }
        }
        return cleanName.toString();
    }
}
