/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.file;

import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;

/**
 * A bit of a hack so when we save a file, we can update the Recent Files list.
 * <p>
 * We can't use property changes on DataObject modification because we
 * setModified(false) when discarding. Therefore we need a separate mechanism
 * just for saving.
 *
 * @author algol
 */
public class SaveNotification {

    /**
     * Add the path to the recent file list.
     *
     * @param path The path to be added to the recent file list.
     */
    public static void saved(final String path) {
        RecentFiles.saved(path);
    }
}
