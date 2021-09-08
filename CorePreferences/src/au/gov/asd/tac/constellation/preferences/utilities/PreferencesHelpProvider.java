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
package au.gov.asd.tac.constellation.preferences.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the preferences module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("PreferencesHelpProvider=Preference Help Provider")
public class PreferencesHelpProvider extends HelpPageProvider {

    @Override
    public List<String> getHelpPages() {
        List<String> filePaths = new ArrayList<>();
        return filePaths;
    }

    @Override
    public List<String> getHelpResources() {
        List<String> filePaths = new ArrayList<>();
        return filePaths;
    }

    @Override
    public Map<String, String> getHelpMap() {
        Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String preferencesModulePath = ".." + sep + "constellation" + sep + "CorePreferences" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + "constellation" + sep + "preferences" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.preferences.application", preferencesModulePath + "application-preferences.md");
        map.put("au.gov.asd.tac.constellation.preferences.applicationfont", preferencesModulePath + "application-font-preferences.md");
        map.put("au.gov.asd.tac.constellation.preferences.developer", preferencesModulePath + "developer-prefernces.md");
        map.put("au.gov.asd.tac.constellation.preferences.graph", preferencesModulePath + "graph-preferences.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final int count = userDir.length() - 13;
        final String substr = userDir.substring(count);
        final String preferencesPath;
        if ("constellation".equals(substr)) {
            preferencesPath = userDir + sep + "CorePreferences" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "preferences" + sep + "docs" + sep + "preferences-toc.xml";

        } else {
            preferencesPath = userDir + sep + ".." + sep + "CorePreferences" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "preferences" + sep + "docs" + sep + "preferences-toc.xml";
        }

        return preferencesPath;
    }
}
