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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the histogram view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 1200)
@NbBundle.Messages("HistogramViewHelpProvider=Histogram View Help Provider")
public class HistogramViewHelpProvider extends HelpPageProvider {

    private static final String CODEBASE_NAME = "constellation";

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String histogramModulePath = ".." + sep + "ext" + sep + "docs" + sep + "CoreHistogramView" + sep + "src" + sep + "au" + sep + "gov" + sep
                + "asd" + sep + "tac" + sep + CODEBASE_NAME + sep + "views" + sep + "histogram" + sep;

        map.put("au.gov.asd.tac.constellation.views.histogram.HistogramTopComponent", histogramModulePath + "histogram-view.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String histogramViewPath;
        histogramViewPath = "ext" + sep + "docs" + sep + "CoreHistogramView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac"
                + sep + CODEBASE_NAME + sep + "views" + sep + "histogram" + sep + "histogram-toc.xml";
        return histogramViewPath;
    }
}
