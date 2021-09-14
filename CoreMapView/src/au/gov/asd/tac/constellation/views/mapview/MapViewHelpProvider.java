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
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("MapViewHelpProvider=Map View Help Provider")
public class MapViewHelpProvider extends HelpPageProvider {

    @Override
    public Map<String, String> getHelpMap() {
        Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String mapModulePath = ".." + sep + "constellation" + sep + "CoreMapView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + "constellation" + sep + "views" + sep + "mapview" + sep + "docs" + sep;

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

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String mapViewPath;
        mapViewPath = "constellation" + sep + "CoreMapView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac" + sep
                + "constellation" + sep + "views" + sep + "mapview" + sep + "docs" + sep + "mapview-toc.xml";
        return mapViewPath;
    }
}
