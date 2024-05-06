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
package au.gov.asd.tac.constellation.views.notes.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all notes and filters for the selected graph's current state for the
 * Notes View.
 *
 * @author sol695510
 */
public class NotesViewState {

    private final List<NotesViewEntry> notesViewEntries;
    private final List<String> selectedFilters;
    private final List<String> tagsSelectedFilters;

    public NotesViewState() {
        notesViewEntries = new ArrayList<>();
        selectedFilters = new ArrayList<>();
        tagsSelectedFilters = new ArrayList<>();
    }

    public NotesViewState(final NotesViewState currentState) {
        notesViewEntries = currentState.getNotes();
        selectedFilters = currentState.getFilters();
        tagsSelectedFilters = currentState.getTagsFilters();
    }

    public NotesViewState(final List<NotesViewEntry> notesViewEntries, final List<String> selectedFilters, final List<String> tagsSelectedFilters) {
        this.notesViewEntries = notesViewEntries;
        this.selectedFilters = selectedFilters;
        this.tagsSelectedFilters = tagsSelectedFilters;
    }

    public List<NotesViewEntry> getNotes() {
        return notesViewEntries;
    }

    public List<String> getFilters() {
        return selectedFilters;
    }

    public List<String> getTagsFilters() {
        return tagsSelectedFilters;
    }

    public void setNotes(final List<NotesViewEntry> notesViewEntries) {
        this.notesViewEntries.clear();
        this.notesViewEntries.addAll(notesViewEntries);
    }

    public void setFilters(final List<String> selectedFilters) {
        this.selectedFilters.clear();
        this.selectedFilters.addAll(selectedFilters);
    }

    public void setTagsFilters(final List<String> selectedTagsFilters) {
        this.tagsSelectedFilters.clear();
        this.tagsSelectedFilters.addAll(selectedTagsFilters);
    }
}
