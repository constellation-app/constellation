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
package au.gov.asd.tac.constellation.views.tableview2.state;

/**
 *
 * @author formalhaunt
 */
public class TablePreferences {
    public static final int DEFAULT_MAX_ROWS_PER_PAGE = 500;
    
    private Integer maxRowsPerPage = DEFAULT_MAX_ROWS_PER_PAGE;

    public Integer getMaxRowsPerPage() {
        return maxRowsPerPage;
    }

    public void setMaxRowsPerPage(Integer maxRowsPerPage) {
        this.maxRowsPerPage = maxRowsPerPage;
    }
}
