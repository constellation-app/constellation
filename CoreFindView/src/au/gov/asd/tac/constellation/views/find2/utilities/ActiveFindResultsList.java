/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find2.utilities;

import java.util.HashSet;
import java.util.Set;

/**
 * The current list of all find view results
 *
 * @author Delphinus8821
 */
public class ActiveFindResultsList {

    private static FindResultsList basicResultsList;
    private static FindResultsList advancedResultsList;

    private ActiveFindResultsList() {
    }

    /**
     * Get the basic find results list
     * 
     * @return basicResultsList
     */
    public static synchronized FindResultsList getBasicResultsList() {
        return basicResultsList;
    }


    /**
     * Get the advanced find results list
     *
     * @return advancedResultsList
     */
    public static FindResultsList getAdvancedResultsList() {
        return advancedResultsList;
    }

    /**
     * Set the basic find results list
     *
     * @param basicResultsList
     */
    public static synchronized void setBasicResultsList(final FindResultsList basicResultsList) {
        ActiveFindResultsList.basicResultsList = basicResultsList;
    }

    /**
     * Set the advanced find results list
     *
     * @param advancedResultsList
     */
    public static void setAdvancedResultsList(final FindResultsList advancedResultsList) {
        ActiveFindResultsList.advancedResultsList = advancedResultsList;
    }

    /**
     * Add more results to the basic find results list if not already in the list
     *
     * @param additions
     */
    public static synchronized void addToBasicFindResultsList(final FindResultsList additions) {
        additions.forEach(result -> {
            boolean exists = false;
            for (final FindResult current : ActiveFindResultsList.basicResultsList) {
                if (current.equals(result)) {
                    exists = true;
                }
            }
            if (!exists) {
                ActiveFindResultsList.basicResultsList.add(result);
            }
        });
    }

    /**
     * Add more results to the advanced find results list if not already in the list
     *
     * @param additions
     */
    public static void addToAdvancedFindResultsList(final FindResultsList additions) {
        additions.forEach(result -> {
            boolean exists = false;
            for (final FindResult current : ActiveFindResultsList.advancedResultsList) {
                if (current.equals(result)) {
                    exists = true;
                }
            }
            if (!exists) {
                ActiveFindResultsList.advancedResultsList.add(result);
            }
        });
    }

    /**
     * Gets how many unique graphs are found in the list - will equal the number of graphs open when the search was done
     *
     * @param findResultsList
     * @return
     */
    public static int getUniqueGraphCount(final FindResultsList findResultsList) {
        final Set<String> uniqueGraphs = new HashSet<>();
        if (findResultsList != null) {
            findResultsList.stream().map(result -> result.getGraphId()).forEachOrdered(graphId -> 
                uniqueGraphs.add(graphId));
        }
        return uniqueGraphs.size();
    }
}
