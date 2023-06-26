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
package au.gov.asd.tac.constellation.graph.locking;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Auriga2
 */
public class UndoRedoReportManager {

    private static final List<UndoRedoReportListener> LISTENERS = new ArrayList<>();

    public static synchronized void addUndoRedoReportListener(UndoRedoReportListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static synchronized void removeUndoRedoReportListener(UndoRedoReportListener listener) {
        LISTENERS.remove(listener);
    }
    public static synchronized void fireNewUndoRedoReport(UndoRedoReport undoRedoReport) {
        LISTENERS.stream().forEach(listener -> listener.addNewUndoRedoReport(undoRedoReport));
    }
}
