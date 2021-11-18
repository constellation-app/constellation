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
package au.gov.asd.tac.constellation.graph.schema.visual.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class LabelUtilities {
    
    private LabelUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return a list of attribute ids corresponding to the attribute names in
     * the given graphs labels object.
     * <p>
     * If an attribute name is not present, it will be skipped.
     *
     * @param labels The GraphLabels object to get the referenced attributes
     * from.
     * @param rg The graph.
     * @param elementType The element type.
     *
     * @return A list of attribute ids.
     */
    public static List<Integer> getAttributeIdsFromGraphLabels(final GraphLabels labels, final GraphReadMethods rg, final GraphElementType elementType) {
        final ArrayList<Integer> attributes = new ArrayList<>();
        if (labels != null) {
            labels.getLabels().forEach(label -> {
                attributes.add(rg.getAttribute(elementType, label.getAttributeName()));
            });
        }

        return attributes;
    }
}
