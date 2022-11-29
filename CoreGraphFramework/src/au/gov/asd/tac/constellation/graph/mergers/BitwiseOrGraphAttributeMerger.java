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
package au.gov.asd.tac.constellation.graph.mergers;

import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author aldebaran30701
 */
@ServiceProvider(service = GraphAttributeMerger.class)
public class BitwiseOrGraphAttributeMerger extends GraphAttributeMerger {

    public static final String ID = "au.gov.asd.tac.constellation.graph.mergers.BitwiseOrGraphAttributeMerger";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean mergeAttribute(final GraphWriteMethods graph, final GraphElementType elementType, final int survivingElement, final int mergedElement, final int attribute) {
        final Long survivingValue = graph.getLongValue(attribute, survivingElement);
        final Long mergedValue = graph.getLongValue(attribute, mergedElement);

        final Long result = survivingValue | mergedValue;
        graph.setLongValue(attribute, survivingElement, result);

        return true;
    }
}
