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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 *
 * @author sirius
 */
public enum BinElementType {

    VERTEX(GraphElementType.VERTEX, "Vertex") {
        @Override
        public void getBinCreators(BinCreatorCollection collection) {

        }
    },
    LINK(GraphElementType.LINK, "Link (all transactions merged)") {
        @Override
        public void getBinCreators(BinCreatorCollection collection) {

        }
    },
    EDGE(GraphElementType.EDGE, "Edge (transactions merged by direction)") {
        @Override
        public void getBinCreators(BinCreatorCollection collection) {

        }
    },
    TRANSACTION(GraphElementType.TRANSACTION, "Transaction (no merging)") {
        @Override
        public void getBinCreators(BinCreatorCollection collection) {

        }
    };

    private final GraphElementType elementType;
    private final String label;

    private BinElementType(GraphElementType elementType, String label) {
        this.elementType = elementType;
        this.label = label;
    }

    public final GraphElementType getElementType() {
        return elementType;
    }

    public final String getLabel() {
        return label;
    }

    public abstract void getBinCreators(BinCreatorCollection collection);
}
