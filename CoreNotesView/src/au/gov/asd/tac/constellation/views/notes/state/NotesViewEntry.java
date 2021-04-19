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

import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportListener;

/**
 * Holds the information for a note in the Notes View.
 *
 * @author sol695510
 */
public class NotesViewEntry implements PluginReportListener {

    private final String dateTime;
    private String noteTitle;
    private String noteContent;
    private final Boolean userCreated;

    public NotesViewEntry(final String dateTime, final String noteTitle, final String noteContent, final Boolean userCreated) {
        this.dateTime = dateTime;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.userCreated = userCreated;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public Boolean isUserCreated() {
        return userCreated;
    }

    public void setNoteTitle(final String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setNoteContent(final String noteContent) {
        this.noteContent = noteContent;
    }

    @Override
    public void pluginReportChanged(final PluginReport pluginReport) {
        this.noteContent = pluginReport.getMessage();
    }

    @Override
    public void addedChildReport(final PluginReport parentReport, final PluginReport childReport) {
        // Intentionally left blank. Ignoring child plugin reports.
    }
}
