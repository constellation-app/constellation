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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.views.mapview2.plugins.SelectOnGraphPlugin;


/**
 * Registry for the two plugins used by the map view.
 *
 * @author altair1673
 *
 */
public class MapViewPluginRegistry {

    private MapViewPluginRegistry() {

    }

    public static final String SELECT_ON_GRAPH = SelectOnGraphPlugin.class.getName();
}
