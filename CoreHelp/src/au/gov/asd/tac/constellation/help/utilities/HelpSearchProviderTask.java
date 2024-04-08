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

import java.util.Collection;
import java.util.Map;
import org.openide.util.HelpCtx;

/**
 * Runnable for HelpSearchProvider
 *
 * @author Delphinus8821
 */
public class HelpSearchProviderTask implements Runnable {

    private final String helpPageName;

    public String getHelpPageName() {
        return helpPageName;
    }

    /**
     * Create a new help search provider task
     *
     * @param name of the help page
     */
    public HelpSearchProviderTask(final String name) {
        this.helpPageName = name;
    }

    /**
     * Matches the quick search item to a value in the mappings and displays the help page if there is a valid key in the mapping
     */
    @Override
    public void run() {
        // Get the names of all of the help files
        final Map<String, String> mappings = HelpMapper.getMappings();
        final Collection<String> values = mappings.values();

        // Find the value that matches the name of the quick search item
        for (final String value : values) {
            if (value.contains(helpPageName)) {
                for (final Map.Entry<String, String> entry : mappings.entrySet()) {

                    // Display the help page for that value 
                    if (value.equals(entry.getValue())) {
                        new HelpCtx(entry.getKey()).display();
                    }
                }
            }
        }
    }
}
