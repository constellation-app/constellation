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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * data model of the parameters for the circular tree arrangement
 *
 * @author algol
 */
public class CircTreeChoiceParameters {

    final float scale;
    final boolean strictCircularLayout;

    final int rootAttrId;
    final String rootValue;

    int rootVxId;

    public CircTreeChoiceParameters(final float scale, final boolean strictCircularLayout, final int rootAttrId, final String rootValue) {
        this.scale = scale;
        this.strictCircularLayout = strictCircularLayout;
        this.rootAttrId = rootAttrId;
        this.rootValue = rootValue;
        this.rootVxId = Graph.NOT_FOUND;
    }

    public static CircTreeChoiceParameters getDefaultParameters() {
        return new CircTreeChoiceParameters(1, false, Graph.NOT_FOUND, "");
    }
}
