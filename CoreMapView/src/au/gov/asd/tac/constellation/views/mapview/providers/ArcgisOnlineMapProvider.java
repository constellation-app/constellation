/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview.providers;

import org.openide.util.lookup.ServiceProvider;

/**
 * ArcGIS Online map.
 * 
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapProvider.class, position = Integer.MAX_VALUE - 1)
public class ArcgisOnlineMapProvider extends EsriMapProvider {

    @Override
    public String getName() {
        return "ArcGIS Online";
    }

    @Override
    public int zoomLevels() {
        return 19;
    }

    @Override
    public String getMapServer() {
        return "https://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
    }
}
