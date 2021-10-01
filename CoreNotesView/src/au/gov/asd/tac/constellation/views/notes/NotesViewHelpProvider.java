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
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the notes view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 1600)
@NbBundle.Messages("NotesViewHelpProvider=Notes View Help Provider")
public class NotesViewHelpProvider extends HelpPageProvider {

    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String notesModulePath = ".." + sep + "constellation" + sep + "CoreNotesView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + "constellation" + sep + "views" + sep + "notes" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.views.notes.NotesViewTopComponent", notesModulePath + "notes-view.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String notesViewPath;
        notesViewPath = "constellation" + sep + "CoreNotesView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac" + sep
                + "constellation" + sep + "views" + sep + "notes" + sep + "docs" + sep + "notes-toc.xml";
        return notesViewPath;
    }
}
