/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import java.util.regex.Pattern;

/**
 *
 * @author aquila
 */
public class VersionUtilities {
    
    private static final Pattern CHARACTERS_AFTER_SPACE_REGEX = Pattern.compile(" .*");
    private static final Pattern SEP_REGEX = Pattern.compile("\\.");
    
    private VersionUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Compare two decimal separated version numbers and return if the stated
     * version meets the minimum required version. For example, 3.5.2 and 4.2
     *
     * @param current The current OpenGL version
     * @param minimumVersion The minimum OpenGL version
     *
     * @return True if current is at least minimum_version, false otherwise.
     */
    public static boolean doesVersionMeetMinimum(final String current, final String minimumVersion) {
        final String trimmedCurrent = CHARACTERS_AFTER_SPACE_REGEX.matcher(current).replaceAll("").trim();
        final String[] currentSplit = SEP_REGEX.split(trimmedCurrent);
        final String trimmedMinimumVersion = CHARACTERS_AFTER_SPACE_REGEX.matcher(minimumVersion).replaceAll("").trim();
        final String[] minimumVersionSplit = SEP_REGEX.split(trimmedMinimumVersion);

        final int length = Math.max(currentSplit.length, minimumVersionSplit.length);
        for (int i = 0; i < length; i++) {
            int thisPart;
            try {
                thisPart = i < currentSplit.length ? Integer.parseInt(currentSplit[i]) : 0;
            } catch (final NumberFormatException ex) {
                thisPart = 0;
            }

            int min;
            try {
                min = i < minimumVersionSplit.length ? Integer.parseInt(minimumVersionSplit[i]) : 0;
            } catch (final NumberFormatException ex) {
                min = 0;
            }
            
            if (thisPart > min) {
                return true;
            }

            if (thisPart < min) {
                return false;
            }
        }

        // The current is equal or greater, so that meets the minimum requirement
        return true;
    }
}
