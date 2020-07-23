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
 * A state for the notes view.
 *
 * @author sol695510
 */
public class NotesViewState {

    // list of entries
    private final List<NotesViewEntry> NotesViewEntries;

    public NotesViewState() {
        this.NotesViewEntries = new ArrayList();
    }

    /**
     * gets an unmodifiable list of all notes entries
     *
     * @return Unmodifiable list of all entries.
     */
    public List<NotesViewEntry> getNotesViewEntries() {
        return List.copyOf(NotesViewEntries);
    }
}
