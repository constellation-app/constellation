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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapDirected;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapDirectedUnrecordedTeleportation;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapUndirdir;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapUndirected;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;

/**
 *
 * @author algol
 */
public class InfoMapContext {

    private final InfomapBase infoMap;

    public InfoMapContext(final Config config, final GraphReadMethods rg) {
        if (config.isUndirected()) {
            infoMap = new InfomapUndirected(config, rg);
        } else if (config.isUndirdir() || config.isOutdirdir() || config.isRawdir()) {
            infoMap = new InfomapUndirdir(config, rg);
        } else if (config.isRecordedTeleportation()) {
            infoMap = new InfomapDirected(config, rg);
        } else {
            infoMap = new InfomapDirectedUnrecordedTeleportation(config, rg);
        }
    }

    public InfomapBase getInfoMap() {
        return infoMap;
    }
}
