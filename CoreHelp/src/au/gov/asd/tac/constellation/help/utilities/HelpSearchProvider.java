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
package au.gov.asd.tac.constellation.help.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 * Class responsible for managing searching for help pages from the QuickSearch box.
 * <p>
 * Note: This is a NetBeans platform specific feature.
 *
 * @author Delphinus8821
 */
public class HelpSearchProvider implements SearchProvider {

    /**
     * Collects search results for the quick search Recognise and Process recent HELP searches
     *
     * @param request
     * @param response
     */
    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final QuickSearchUtils qs = new QuickSearchUtils();
        // Check the request is valid
        final String text;
        if (request != null && StringUtils.isNotBlank(request.getText())) {
            text = qs.restoreBrackets(request.getText());
        } else {
            return;
        }
        String prevFileName = "";
        // Locally defined Recent searches will start with a specific unicode left bracket in the search term
        if (text.startsWith(QuickSearchUtils.LEFT_BRACKET)) {
            final int termEnd = text.length();
            // A recent HELP search will begin with a (H) character and end with a diamond character
            if (termEnd > 0 && text.startsWith(QuickSearchUtils.CIRCLED_H)) {
                final int termPos = text.indexOf(" ") + 1;
                // Convert the search term into a Help file name.
                prevFileName = text.substring(termPos, termEnd).trim().toLowerCase().replace(" ", "-") + ".md";
            } else {
                // This is a recent search for a different category, so we can end the Help search here
                return;
            }
        }

        // Get the names of all of the help files
        List<String> distinctValues = new ArrayList(new HashSet<>(HelpMapper.getMappings().values()));

        // Match the search to values in the map
        for (final String value : distinctValues) {
            final int index = value.lastIndexOf(File.separator);
            final String fileName = value.substring(index + 1);

            // Create a display name that is easier to search
            String displayName = qs.replaceBrackets(fileName.replace("-", " "));
            final int indexMD = displayName.lastIndexOf(".");
            displayName = QuickSearchUtils.CIRCLED_H + "  " + displayName.substring(0, indexMD);

            if (fileName.contains(text.toLowerCase()) && "".equals(prevFileName)) {
                // Display the result and add a runnable for when it is clicked on 
                if (!response.addResult(new HelpSearchProviderTask(fileName), displayName)) {
                    return;
                }
            } else if (StringUtils.isNotBlank(prevFileName) && fileName.contains(prevFileName)) {
                // Found the recent Help search result. Set it and exit immediately
                response.addResult(new HelpSearchProviderTask(fileName), displayName);
                break;
            }
        }
    }

    public class QuickSearchUtils {
        // Cannot import the QuickSearchUtilities class due to cyclic dependency issues,
        // so the required functions have been put into this stripped down local version of the class.

        public static final String LEFT_BRACKET = "\u276a"; // bold left parenthesis
        public static final String RIGHT_BRACKET = "\u276b"; // bold right parenthesis
        public static final String SMALL_SPACE = "\u2005";
        public static final String CIRCLED_H = LEFT_BRACKET + "\uff28" + RIGHT_BRACKET + SMALL_SPACE; // (H) - prefix for HELP results

        // Substitution characters for angled brackets and round brackets, used to address a Netbeans issue
        private static final String LT_FULL = "\uff1c"; // <
        private static final String GT_FULL = "\uff1e"; // >
        private static final String OB_FULL = "\uff08"; // (
        private static final String CB_FULL = "\uff09"; // )

        public String replaceBrackets(final String source) {
            return source.replace("<", LT_FULL).replace(">", GT_FULL).replace("(", OB_FULL).replace(")", CB_FULL);
        }

        public String restoreBrackets(final String source) {
            return source.replace(LT_FULL, "<").replace(GT_FULL, ">").replace(OB_FULL, "(").replace(CB_FULL, ")");
        }
    }

}
