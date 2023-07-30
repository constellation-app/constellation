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
package au.gov.asd.tac.constellation.graph.reporting;

import au.gov.asd.tac.constellation.graph.reporting.UndoRedoReport;

/**
 * A listener that gets alerted whenever a new undo/redo report is created.
 *
 * @author Auriga2
 */
public interface UndoRedoReportListener {

    /**
     * Called by the LockingManager when an undo/redo event occurs to advertise
     * that a new {@link UndoRedoReport} has been created.
     *
     * @param undoRedoReport the newly created {@link UndoRedoReport}.
     */
    public void addNewUndoRedoReport(UndoRedoReport undoRedoReport);

}
