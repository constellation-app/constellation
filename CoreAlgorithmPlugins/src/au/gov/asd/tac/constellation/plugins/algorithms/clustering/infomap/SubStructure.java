/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapBase;

/**
 *
 * @author algol
 */
public class SubStructure {

    private InfomapBase subInfomap;
    private boolean exploredWithoutImprovement;

    public SubStructure() {
        subInfomap = null;
        exploredWithoutImprovement = false;
    }

    public InfomapBase getSubInfomap() {
        return subInfomap;
    }

    public void setSubInfomap(final InfomapBase subInfomap) {
        this.subInfomap = subInfomap;
    }

    public boolean isExploredWithoutImprovement() {
        return exploredWithoutImprovement;
    }

    public void setExploredWithoutImprovement(final boolean exploredWithoutImprovement) {
        this.exploredWithoutImprovement = exploredWithoutImprovement;
    }
}
