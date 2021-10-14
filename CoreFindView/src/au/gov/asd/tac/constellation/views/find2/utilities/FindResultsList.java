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
package au.gov.asd.tac.constellation.views.find2.utilities;

import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import java.util.ArrayList;

/**
 *
 * @author Atlas139mkm
 */
public class FindResultsList extends ArrayList<FindResult> {

    private int currentIndex = -1;
    private String graphId;
    private BasicFindReplaceParameters searchParameters;

    public FindResultsList() {
        graphId = "";
    }

    public FindResultsList(int index, BasicFindReplaceParameters searchParameters) {
        this.currentIndex = index;
        this.searchParameters = searchParameters;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        currentIndex = index;
    }

    /*
     * -1 - next
        0 - next
        1 - 
     *
     */
    public void incrementCurrentIndex() {
        if (currentIndex == this.size() - 1) {
            currentIndex = 0;
        } else {
            currentIndex++;
        }
    }

    public void decrementCurrentIndex() {
        if (currentIndex == 0 || currentIndex == -1) {
            currentIndex = this.size() - 1;
        } else {
            currentIndex--;
        }
    }

    public void setGraphId(String id) {
        graphId = id;
    }

    public String getGraphId() {
        return graphId;
    }

    public BasicFindReplaceParameters getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(BasicFindReplaceParameters parameters) {
        this.searchParameters = parameters;
    }

}
