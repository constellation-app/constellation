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
package au.gov.asd.tac.constellation.views.tableview2.service;

import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;

/**
 *
 * @author formalhaunt
 */
public class PreferenceService {
    private final TablePreferences tablePreferences;
    
    public PreferenceService() {
        tablePreferences = new TablePreferences();
    }
    
    public PreferenceService(final TablePreferences tablePreferences) {
        this.tablePreferences = tablePreferences;
    }

    public int getMaxRowsPerPage() {
        return tablePreferences.getMaxRowsPerPage();
    }
    
    public void setMaxRowsPerPage(final int maxRowsPerPage) {
        tablePreferences.setMaxRowsPerPage(maxRowsPerPage);
    }
}
