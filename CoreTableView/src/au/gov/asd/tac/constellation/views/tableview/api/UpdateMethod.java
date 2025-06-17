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
package au.gov.asd.tac.constellation.views.tableview.api;

/**
 * Update methods specifying how the table state's list of visible columns will
 * be updated given a new list of columns.
 *
 * @author formalhaunt
 * @see TableService#updateVisibleColumns(Graph, TableViewState, List,
 * UpdateMethod)
 */
public enum UpdateMethod {
    /**
     * The new columns are added on top of the existing visible columns in the
     * current state.
     */
    ADD,
    /**
     * The passed columns are removed from the existing visible columns in the
     * current state.
     */
    REMOVE,
    /**
     * The passed columns become the new visible columns.
     */
    REPLACE;
}
