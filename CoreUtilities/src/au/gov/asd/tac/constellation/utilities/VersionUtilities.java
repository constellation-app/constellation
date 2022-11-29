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
package au.gov.asd.tac.constellation.utilities;

/**
 *
 * @author aquila
 */
public class VersionUtilities {
    
    private VersionUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Compare two decimal separated version numbers and return if the stated
     * version meets the minimum required version. For example, 3.5.2 and 4.2
     *
     * @param current The current OpenGL version
     * @param minimum_version The minimum OpenGL version
     *
     * @return True if current is at least minimum_version, false otherwise.
     */
    public static boolean doesVersionMeetMinimum(String current, String minimum_version) {
        final String[] current_split = current.replaceAll(" .*", "").trim().split("\\.");
        final String[] minimum_version_split = minimum_version.replaceAll(" .*", "").trim().split("\\.");

        int length = Math.max(current_split.length, minimum_version_split.length);
        for (int i = 0; i < length; i++) {
            int thisPart;
            try {
                thisPart = i < current_split.length ? Integer.parseInt(current_split[i]) : 0;
            } catch (NumberFormatException ex) {
                thisPart = 0;
            }

            int min;
            try {
                min = i < minimum_version_split.length ? Integer.parseInt(minimum_version_split[i]) : 0;
            } catch (NumberFormatException ex) {
                min = 0;
            }

            if (thisPart > min) {
                return true;
            }

            if (thisPart < min) {
                return false;
            }
        }

        //The strings are identical, so that meets the minimum requirement
        return true;
    }
}
