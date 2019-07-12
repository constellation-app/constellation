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
package au.gov.asd.tac.constellation.graph.visual.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.visual.decorators.Decorators;
import au.gov.asd.tac.constellation.visual.labels.GraphLabels;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class LabelAndDecoratorUtilities {

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

    /**
     * Return a list of attribute ids corresponding to the attribute names in
     * the given decorators object.
     * <p>
     * A list of length 4 is always returned, with decorator attributes in the
     * NW, SW, SE, NE order. When some (or all) attributes are not present,
     * their spots in the list will be populated with Graph.NOT_FOUND
     * <p>
     * If an attribute name is not present, it will be skipped.
     *
     * @param decorators The Decorators object to get the referenced attributes
     * from.
     * @param rg The graph.
     *
     * @return A list of attribute ids.
     */
    public static List<Integer> getAttributeIdsFromDecorators(final Decorators decorators, final GraphReadMethods rg) {
        final ArrayList<Integer> attributes = new ArrayList<>();
        if (decorators != null) {
            if (decorators.getNorthWestDecoratorAttribute() != null) {
                attributes.add(rg.getAttribute(GraphElementType.VERTEX, decorators.getNorthWestDecoratorAttribute()));
            } else {
                attributes.add(Graph.NOT_FOUND);
            }
            if (decorators.getNorthEastDecoratorAttribute() != null) {
                attributes.add(rg.getAttribute(GraphElementType.VERTEX, decorators.getNorthEastDecoratorAttribute()));
            } else {
                attributes.add(Graph.NOT_FOUND);
            }
            if (decorators.getSouthEastDecoratorAttribute() != null) {
                attributes.add(rg.getAttribute(GraphElementType.VERTEX, decorators.getSouthEastDecoratorAttribute()));
            } else {
                attributes.add(Graph.NOT_FOUND);
            }
            if (decorators.getSouthWestDecoratorAttribute() != null) {
                attributes.add(rg.getAttribute(GraphElementType.VERTEX, decorators.getSouthWestDecoratorAttribute()));
            } else {
                attributes.add(Graph.NOT_FOUND);
            }
        }
        return attributes;
    }
}
