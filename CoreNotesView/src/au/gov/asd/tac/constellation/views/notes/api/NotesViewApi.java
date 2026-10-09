/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewConcept;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewEntry;
import au.gov.asd.tac.constellation.views.notes.state.NotesViewState;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Public API for adding user notes to a graph's Notes View from other modules.
 *
 * @author doubtsec
 */
public final class NotesViewApi {

    /**
     * The default colour used for notes added through this API.
     */
    public static final String DEFAULT_NOTE_COLOUR = "#4a90d9";

    private NotesViewApi() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Add a user note to the given graph. If the Notes View is open and showing
     * the graph, the note is added through the view so the view's in-memory
     * state stays consistent; otherwise the note is appended directly to the
     * graph's Notes View state attribute.
     * <p>
     * Must not be called on the Swing or JavaFX thread.
     *
     * @param graph The graph to add the note to.
     * @param title The note title.
     * @param content The note content.
     * @param markdown Whether the content is Markdown.
     * @param tags Tags for the note, may be empty.
     * @throws InterruptedException if interrupted while writing the note.
     * @throws PluginException if the note couldn't be written.
     */
    public static void addNote(final Graph graph, final String title, final String content, final boolean markdown, final List<String> tags)
            throws InterruptedException, PluginException {
        if (graph == null) {
            throw new IllegalArgumentException("A graph is required to add a note");
        }
        final NotesViewEntry entry = new NotesViewEntry(Long.toString(Instant.now().toEpochMilli()),
                title, content, true, true, DEFAULT_NOTE_COLOUR, markdown);
        entry.setTags(tags == null ? new ArrayList<>() : new ArrayList<>(tags));

        final AtomicBoolean added = new AtomicBoolean(false);
        try {
            EventQueue.invokeAndWait(() -> {
                final TopComponent tc = WindowManager.getDefault().findTopComponent(NotesViewTopComponent.class.getSimpleName());
                if (tc instanceof NotesViewTopComponent notesView) {
                    added.set(notesView.addNoteIfShowing(graph, entry));
                }
            });
        } catch (final InvocationTargetException ex) {
            throw new PluginException(au.gov.asd.tac.constellation.plugins.PluginNotificationLevel.ERROR, ex.getCause());
        }

        if (!added.get()) {
            PluginExecution.withPlugin(new AddNotePlugin(entry)).executeNow(graph);
        }
    }

    /**
     * Appends a note to the graph's Notes View state.
     */
    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
    private static final class AddNotePlugin extends SimpleEditPlugin {

        private final NotesViewEntry entry;

        AddNotePlugin(final NotesViewEntry entry) {
            this.entry = entry;
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int stateAttributeId = NotesViewConcept.MetaAttribute.NOTES_VIEW_STATE.ensure(graph);
            final NotesViewState currentState = graph.getObjectValue(stateAttributeId, 0);
            final NotesViewState newState = currentState == null ? new NotesViewState() : new NotesViewState(currentState);
            final List<NotesViewEntry> notes = new ArrayList<>(newState.getNotes());
            notes.add(entry);
            newState.setNotes(notes);
            graph.setObjectValue(stateAttributeId, 0, newState);
        }

        @Override
        public String getName() {
            return "Notes View: Add Note";
        }
    }
}
