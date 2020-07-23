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

import java.time.LocalDateTime;

/**
 * An entry note into the Notes View
 *
 * @author sol695510
 */
public class NotesViewEntry {

    private final Boolean isUserNote;
    private final LocalDateTime timestamp;
    private String noteTitle;
    private String noteContent;

    public NotesViewEntry(final Boolean isUserNote, final LocalDateTime timestamp,
            final String noteTitle, final String noteContent) {
        this.isUserNote = isUserNote;
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
    }

    public void setNoteTitle(final String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setNoteContent(final String noteContent) {
        this.noteContent = noteContent;
    }

    public Boolean isUserNote() {
        return isUserNote;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }
}
