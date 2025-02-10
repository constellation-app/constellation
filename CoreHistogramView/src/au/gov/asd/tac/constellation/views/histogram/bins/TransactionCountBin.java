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
package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;

/**
 * A bin holding integer values representing the number of transactions adjacent
 * to a binned vertex.
 *
 * @author sirius
 */
public class TransactionCountBin extends IntBin {

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        key = graph.getVertexTransactionCount(element);
    }

    @Override
    public Bin create() {
        return new TransactionCountBin();
    }
}
