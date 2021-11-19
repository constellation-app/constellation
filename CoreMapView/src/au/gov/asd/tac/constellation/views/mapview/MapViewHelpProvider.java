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
package au.gov.asd.tac.constellation.views.mapview;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the map view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 1400)
@NbBundle.Messages("MapViewHelpProvider=Map View Help Provider")
public class MapViewHelpProvider extends HelpPageProvider {

    private static final String CODEBASE_NAME = "constellation";

    /**
     * Provides a map of all the help files Maps the file name to the md file
     * name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String mapModulePath = ".." + sep + CODEBASE_NAME + sep + "CoreMapView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + CODEBASE_NAME + sep + "views" + sep + "mapview" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.views.mapview.MapViewTopComponent", mapModulePath + "map-view.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.DayNightLayer", mapModulePath + "mapview-layers-day-night.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.AbstractHeatmapLayer", mapModulePath + "mapview-layers-heatmap.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.AbstractPathsLayer", mapModulePath + "mapview-layers-paths.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.ThiessenPolygonsLayer", mapModulePath + "mapview-layers-thiessen-polygons.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.overlays.InfoOverlay", mapModulePath + "mapview-overlays-info.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.overlays.ToolsOverlay", mapModulePath + "mapview-overlays-tools.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.overlays.OverviewOverlay", mapModulePath + "mapview-overlays-overview.md");
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
        return CODEBASE_NAME + sep + "CoreMapView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac" + sep
                + CODEBASE_NAME + sep + "views" + sep + "mapview" + sep + "docs" + sep + "mapview-toc.xml";
    }
}
