/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all notes and filters for the selected graph's current state for the Notes View.
 *
 * @author sol695510
 */
public class NotesViewState {
    
    private final List<NotesViewEntry> notesViewEntries;
    private final List<String> selectedFilters;
    
    public NotesViewState() {
        notesViewEntries = new ArrayList();
        selectedFilters = new ArrayList();
    }
    
    public NotesViewState(NotesViewState currentState) {
        notesViewEntries = currentState.getNotes();
        selectedFilters = currentState.getFilters();
    }
    
    public NotesViewState(final List<NotesViewEntry> notesViewEntries, final List<String> selectedFilters) {
        this.notesViewEntries = notesViewEntries;
        this.selectedFilters = selectedFilters;
    }
    
    public List<NotesViewEntry> getNotes() {
        return notesViewEntries;
    }
    
    public List<String> getFilters() {
        return selectedFilters;
    }
    
    public void setNotes(final List<NotesViewEntry> notesViewEntries) {
        this.notesViewEntries.clear();
        this.notesViewEntries.addAll(notesViewEntries);
    }
    
    public void setFilters(final List<String> selectedFilters) {
        this.selectedFilters.clear();
        this.selectedFilters.addAll(selectedFilters);
    }
}
