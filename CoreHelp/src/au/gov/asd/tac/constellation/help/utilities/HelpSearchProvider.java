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

    /**
     * Collects search results for the quick search
     *
     * @param request
     * @param response
     */
    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        // Check the request is valid
        final String text;
        if (request != null && StringUtils.isNotBlank(request.getText())) {
            text = request.getText().toLowerCase();
        } else {
            return;
        }

        // Get the names of all of the help files
        final Map<String, String> mappings = HelpMapper.getMappings();
        final Collection<String> values = mappings.values();

        // Match the search to values in the map
        for (final String value : values) {
            final int index = value.lastIndexOf(File.separator);
            final String fileName = value.substring(index + 1);
            if (fileName.contains(text)) {

                // Create a display name that is easier to search
                String displayName = fileName.replace("-", " ");
                final int indexMD = displayName.lastIndexOf(".");
                displayName = displayName.substring(0, indexMD);

                // Display the result and add a runnable for when it is clicked on 
                if (!response.addResult(new HelpSearchProviderTask(fileName), displayName)) {
                    return;
                }
            }
        }
    }

}
