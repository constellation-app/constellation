/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;

/**
 *
 * @author aldebaran30701
 */
public class LayersUtilities {

    public static int calculateCurrentLayerSelectionBitMask(final BitMaskQueryCollection vxQueriesCollection, final BitMaskQueryCollection txQueriesCollection) {
        int newBitmask = 0b0;
        final int iteratorEnd = Math.max(vxQueriesCollection.getHighestQueryIndex(), txQueriesCollection.getHighestQueryIndex());
        for (int position = 0; position <= iteratorEnd; position++) {
            final BitMaskQuery vxQuery = vxQueriesCollection.getQuery(position);
            final BitMaskQuery txQuery = txQueriesCollection.getQuery(position);

            if (vxQuery != null) {// can use vx
                newBitmask |= vxQuery.getVisibility() ? (1 << vxQuery.getIndex()) : 0;
            } else if (txQuery != null) {// have to use tx
                newBitmask |= txQuery.getVisibility() ? (1 << txQuery.getIndex()) : 0;
            } else {
                // cannot use any.
            }
        }
        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        newBitmask = (newBitmask == 0) ? 0b1 : (newBitmask > 1) ? newBitmask & ~0b1 : newBitmask;

        return newBitmask;
    }
}
