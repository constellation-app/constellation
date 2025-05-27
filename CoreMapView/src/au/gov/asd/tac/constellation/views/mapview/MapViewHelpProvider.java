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
package au.gov.asd.tac.constellation.views.mapview;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
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
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreMapView" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file
     * name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("au.gov.asd.tac.constellation.views.mapview.MapViewTopComponent", MODULE_PATH + "map-view.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.DayNightLayer", MODULE_PATH + "mapview-layers-day-night.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.AbstractHeatmapLayer", MODULE_PATH + "mapview-layers-heatmap.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.AbstractPathsLayer", MODULE_PATH + "mapview-layers-paths.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.layers.ThiessenPolygonsLayer", MODULE_PATH + "mapview-layers-thiessen-polygons.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.overlays.InfoOverlay", MODULE_PATH + "mapview-overlays-info.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.overlays.ToolsOverlay", MODULE_PATH + "mapview-overlays-tools.md");
        map.put("au.gov.asd.tac.constellation.views.mapview.overlays.OverviewOverlay", MODULE_PATH + "mapview-overlays-overview.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "mapview-toc.xml";
    }
}
