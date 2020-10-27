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
 * Stores all notes currently created for this graph's state for the Notes View.
 *
 * @author sol695510
 */
public class NotesViewState {

    private final List<NotesViewEntry> notesViewEntries;

    public NotesViewState() {
        this.notesViewEntries = new ArrayList();
    }

    public NotesViewState(NotesViewState currentState) {
        this.notesViewEntries = currentState.getNotes();
    }

    public NotesViewState(final List<NotesViewEntry> notesViewEntries) {
        this.notesViewEntries = notesViewEntries;
    }

    public List<NotesViewEntry> getNotes() {
        return notesViewEntries;
    }

    public void setNotes(final List<NotesViewEntry> notes) {
        notesViewEntries.clear();
        notesViewEntries.addAll(notes);
    }
}
