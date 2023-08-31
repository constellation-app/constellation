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
import au.gov.asd.tac.constellation.views.notes.utilities.MarkdownTree;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.text.TextFlow;

/**
 * Holds the information for a note in the Notes View.
 *
 * @author sol695510
 */
public class NotesViewEntry implements PluginReportListener {

    private int id = -99;

    private final String dateTime;
    private String noteTitle;
    private String noteContent;

    private String nodeColour = "#942483";
    private final boolean userCreated;
    private Boolean graphAttribute;
    private List<Integer> nodesSelected;
    private List<Integer> transactionsSelected;
    private List<String> tags = new ArrayList<>();
    private boolean editMode;
    private boolean isShowing = true;

    private boolean inMarkdown = false;
    private TextFlow contentTextFlow;

    private boolean isUndone = false;

    private String tempContent;
    private String tempTitle;


    public NotesViewEntry(final String dateTime, final String noteTitle, final String noteContent, final boolean userCreated, final boolean graphAttribute, final String nodeColour, final boolean inMarkdown) {
        this.dateTime = dateTime;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        contentTextFlow = new TextFlow();
      
        tempContent = "";
        tempTitle = "";

        if (nodeColour != null) {
            this.nodeColour = nodeColour;
        }
        this.userCreated = userCreated;
        this.graphAttribute = graphAttribute;
        this.editMode = false;
        if (userCreated) {
            this.nodesSelected = new ArrayList<>();
            this.transactionsSelected = new ArrayList<>();
        }
        this.inMarkdown = inMarkdown;
    }

    public TextFlow getContentTextFlow() {
        return contentTextFlow;
    }

    public void setContentTextFlow(final TextFlow renderedTextFlow) {
        contentTextFlow.setPadding(new Insets(0, 0, 0, 0));
        contentTextFlow = renderedTextFlow;
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

    public boolean isUserCreated() {
        return userCreated;
    }

    public Boolean isGraphAttribute() {
        return graphAttribute;
    }

    public void setGraphAttribute(final boolean graphAttribute) {
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

    public String getNodeColour() {
        return nodeColour;
    }

    public void setNodeColour(final String nodeColour) {
        this.nodeColour = nodeColour;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    public boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(final boolean editMode) {
        this.editMode = editMode;
    }


    public void setShowing(final boolean showing) {
        isShowing = showing;
    }

    public boolean getShowing() {
        return isShowing;
    }

    public int getID() {
        return id;
    }

    public void setID(final int id) {
        this.id = id;

    }

    public void refreshTextFlow() {
        contentTextFlow.getChildren().clear();

        final MarkdownTree md = new MarkdownTree(noteTitle + "\n\n" + noteContent);
        md.parse();
        contentTextFlow = md.getRenderedText();
        contentTextFlow.autosize();
    }

    public String getTempContent() {
        return tempContent;

    }

    public void setTempContent(final String tempContent) {
        this.tempContent = tempContent;
    }

    public void setTempTitle(final String tempTitle) {
        this.tempTitle = tempTitle;
    }


    public String getTempTitle() {
        return tempTitle;
    }

    public void setInMarkdown(final boolean inMarkdown) {
        this.inMarkdown = inMarkdown;
    }

    public boolean isInMarkdown() {
        return inMarkdown;
    }
  
    public boolean getUndone() {
        return isUndone;
    }

    public void setUndone(final boolean isUndone) {
        this.isUndone = isUndone;
    }

    @Override
    public void pluginReportChanged(final PluginReport pluginReport) {
        this.noteContent = pluginReport.getLastMessage();
    }

    @Override
    public void addedChildReport(final PluginReport parentReport, final PluginReport childReport) {
        // Intentionally left blank. Ignoring child plugin reports.
    }
}
