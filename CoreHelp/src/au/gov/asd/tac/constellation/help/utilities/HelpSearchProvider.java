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

package au.gov.asd.tac.constellation.help.utilities;

import java.io.File;
import java.util.Collection;
import java.util.Map;
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

    public static final String LEFT_BRACKET = "\u276a";
    public static final String CIRCLED_H = "\u276a\uff28\u276b\u2005"; // u2b9c u2b9e

    /**
     * Collects search results for the quick search
     * Recognise and Process recent HELP searches
     *
     * @param request
     * @param response
     */
    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        // Check the request is valid
        final String text;
        if (request != null && StringUtils.isNotBlank(request.getText())) {
            text = request.getText().replace("\uff1c","<").replace("\uff1e",">").replace("\uff08","(").replace("\uff09",")");
        } else {
            return;
        }
        String prevFileName = "";
        // Locally defined Recent searches will start with a specific unicode left bracket in the search term
        if (text.startsWith(LEFT_BRACKET)) {
            final int termEnd = text.length();
            // A recent HELP search will begin with a (H) character and end with a diamond character
            if (termEnd > 0 && text.startsWith(CIRCLED_H)) {
                final int termPos = text.indexOf(" ") + 1;
                // Convert the search term into a Help file name.
                prevFileName = text.substring(termPos, termEnd).trim().toLowerCase().replace(" ", "-") + ".md";
            } else {
                // This is a recent search for a different category, so we can end the Help search here
                return;
            }
        }

        // Get the names of all of the help files
        final Map<String, String> mappings = HelpMapper.getMappings();
        final Collection<String> values = mappings.values();

        // Match the search to values in the map
        for (final String value : values) {
            final int index = value.lastIndexOf(File.separator);
            final String fileName = value.substring(index + 1);
            
            // Create a display name that is easier to search
            String displayName = fileName.replace("-", " ").replace("<","\uff1c").replace(">","\uff1e").replace("(","\uff08").replace(")","\uff09");
            final int indexMD = displayName.lastIndexOf(".");
            displayName = CIRCLED_H + "  " + displayName.substring(0, indexMD);

            if (fileName.contains(text.toLowerCase()) && "".equals(prevFileName)) {
                // Display the result and add a runnable for when it is clicked on 
                if (!response.addResult(new HelpSearchProviderTask(fileName), displayName)) {
                    return;
                }
            } else if (!StringUtils.isBlank(prevFileName) && fileName.contains(prevFileName)){
                // Found the recent Help search result. Set it and exit immediately
                response.addResult(new HelpSearchProviderTask(fileName), displayName);
                break;
            }
        }
    }

}
