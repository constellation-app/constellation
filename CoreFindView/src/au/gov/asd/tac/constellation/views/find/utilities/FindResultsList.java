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
package au.gov.asd.tac.constellation.views.find.utilities;

import au.gov.asd.tac.constellation.views.find.components.advanced.utilities.AdvancedSearchParameters;
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Atlas139mkm
 */
public class FindResultsList extends ArrayList<FindResult> {

    private int currentIndex = -1;
    private final BasicFindReplaceParameters searchParameters;
    private final AdvancedSearchParameters advancedSearchParameters;

    /**
     * Constructor for a findResults list
     *
     * @param graphId
     */
    public FindResultsList() {
        this.searchParameters = new BasicFindReplaceParameters();
        this.advancedSearchParameters = new AdvancedSearchParameters();
    }

    /**
     * Constructor for a findResults list
     *
     * @param index
     * @param searchParameters
     * @param graphId
     */
    public FindResultsList(final int index, final BasicFindReplaceParameters searchParameters) {
        this.currentIndex = index;
        this.searchParameters = searchParameters;
        this.advancedSearchParameters = new AdvancedSearchParameters();
    }

    public FindResultsList(final int index, final AdvancedSearchParameters advancedSearchParamters) {
        this.currentIndex = index;
        this.advancedSearchParameters = advancedSearchParamters;
        this.searchParameters = new BasicFindReplaceParameters();
    }

    /**
     * Constructor that takes another FindResultList and replicates it
     *
     * @param resultsList
     */
    public FindResultsList(final FindResultsList resultsList) {
        this.currentIndex = resultsList.getCurrentIndex();
        this.searchParameters = resultsList.getSearchParameters();
        this.advancedSearchParameters = resultsList.getAdvancedSearchParameters();
    }

    /**
     * Gets the current index
     *
     * @return currentIndex
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Sets the current index
     *
     * @param index
     */
    public void setCurrentIndex(final int index) {
        currentIndex = index;
    }

    /**
     * Increments the current index to the right value. If the index is the size
     * of the list: index = 0, otherwise increment by 1
     */
    public void incrementCurrentIndex() {
        if (currentIndex == this.size() - 1) {
            currentIndex = 0;
        } else {
            currentIndex++;
        }
    }

    /**
     * Decrements the current index to the right value. If the index is 0 or the
     * starting index (-1): index = size -1, otherwise decrement by 1
     */
    public void decrementCurrentIndex() {
        if (currentIndex == 0 || currentIndex == -1) {
            currentIndex = this.size() - 1;
        } else {
            currentIndex--;
        }
    }

    public BasicFindReplaceParameters getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(final BasicFindReplaceParameters parameters) {
        this.searchParameters.copyParameters(parameters);
    }

    public AdvancedSearchParameters getAdvancedSearchParameters() {
        return advancedSearchParameters;
    }

    public void setAdvancedSearchParameters(final AdvancedSearchParameters parameters) {
        this.advancedSearchParameters.copyParameters(parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FindResultsList)) {
            return false;
        }

        final FindResultsList other = (FindResultsList) obj;

        return this.searchParameters.equals(other.searchParameters) && this.advancedSearchParameters.equals(other.advancedSearchParameters);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.currentIndex;
        hash = 53 * hash + Objects.hashCode(this.searchParameters);
        hash = 53 * hash + Objects.hashCode(this.advancedSearchParameters);
        return hash;
    }
}
