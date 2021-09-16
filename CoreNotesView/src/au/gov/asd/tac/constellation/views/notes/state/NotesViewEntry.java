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
import java.util.ArrayList;
import java.util.List;

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
    private Boolean graphAttribute;
    private List<Integer> nodesSelected;
    private List<Integer> transactionsSelected;
    private List<String> tags = new ArrayList<>();
    private boolean editMode;

    public NotesViewEntry(final String dateTime, final String noteTitle, final String noteContent, final boolean userCreated, final boolean graphAttribute) {
        this.dateTime = dateTime;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.userCreated = userCreated;
        this.graphAttribute = graphAttribute;
        this.editMode = false;
        if (userCreated) {
            this.nodesSelected = new ArrayList<>();
            this.transactionsSelected = new ArrayList<>();
        }
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

    public Boolean isGraphAttribute() {
        return graphAttribute;
    }

    public void setGraphAttribute(final Boolean graphAttribute) {
        this.graphAttribute = graphAttribute;
    }

    public List<Integer> getNodesSelected() {
        return nodesSelected;
    }

    public List<Integer> getTransactionsSelected() {
        return transactionsSelected;
    }

    public void setNodesSelected(final List<Integer> nodesSelected) {
        this.nodesSelected = nodesSelected;
    }

    public void setTransactionsSelected(final List<Integer> transactionsSelected) {
        this.transactionsSelected = transactionsSelected;
    }

    public void setNoteTitle(final String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public void setNoteContent(final String noteContent) {
        this.noteContent = noteContent;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
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
