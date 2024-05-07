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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.NativeAttributeType.NativeValue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sirius
 */
public class GraphIndexUtilities {

    private static final Logger LOGGER = Logger.getLogger(GraphIndexUtilities.class.getName());

    private static final boolean VERBOSE = false;
    
    private GraphIndexUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static GraphIndexResult filterElements(final GraphReadMethods graph, final int attribute, final Object value) {
        if (graph.getAttributeIndexType(attribute) == GraphIndexType.NONE) {
            final Attribute attributeObject = new GraphAttribute(graph, attribute);
            final NativeAttributeType nativeType = graph.getNativeAttributeType(attribute);
            final NativeValue searchValue = nativeType.create(value);
            final NativeValue elementValue = new NativeValue();
            final GraphElementType elementType = attributeObject.getElementType();

            if (VERBOSE) {
                LOGGER.log(Level.INFO, "NOT USING INDEX: {0}: {1}", new Object[]{attributeObject.getName(), attribute});
            }

            return new GraphIndexResult() {

                private final int elementCount = elementType.getElementCount(graph);
                private int position = 0;
                private int count = -1;

                @Override
                public int getCount() {
                    if (count == -1) {
                        count = 0;
                        for (int pos = 0; pos < elementCount; pos++) {
                            final int element = elementType.getElement(graph, pos);
                            nativeType.get(graph, attribute, element, elementValue);
                            if (nativeType.equalValue(elementValue, searchValue)) {
                                count++;
                            }
                        }
                    }
                    return count;
                }

                @Override
                public int getNextElement() {
                    while (position < elementCount) {
                        final int element = elementType.getElement(graph, position++);
                        nativeType.get(graph, attribute, element, elementValue);
                        if (nativeType.equalValue(elementValue, searchValue)) {
                            return element;
                        }
                    }
                    return Graph.NOT_FOUND;
                }

            };
        } else {
            if (VERBOSE) {
                Attribute attributeObject = new GraphAttribute(graph, attribute);
                LOGGER.log(Level.INFO, "USING INDEX: {0}: {1}", new Object[]{attributeObject.getName(), attribute});
            }

            return graph.getElementsWithAttributeValue(attribute, value);
        }
    }
}
