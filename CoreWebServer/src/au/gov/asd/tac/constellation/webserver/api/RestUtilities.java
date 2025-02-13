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
package au.gov.asd.tac.constellation.webserver.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.BooleanObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * A collection of utilities for the REST API.
 *
 * @author algol
 */
public class RestUtilities {
    
    private RestUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static Graph getActiveGraph() {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            throw new RestServiceException("No active graph!");
        }

        return graph;
    }

    /**
     * Add a value to a column.
     *
     * @param row The JSON array representing the column.
     * @param type The type of the value.
     * @param value The (possibly null) value.
     */
    public static void addData(final ArrayNode row, final String type, final String value) {
        switch (type) {
            case BooleanAttributeDescription.ATTRIBUTE_NAME, BooleanObjectAttributeDescription.ATTRIBUTE_NAME -> 
                // A DataFrame will parse [True, False, None] to [1.0, 0.0, Nan],
                // so implicitly convert null to False so the result is all booleans.
                row.add(Boolean.parseBoolean(value));
            case ColorAttributeDescription.ATTRIBUTE_NAME -> {
                if (value == null) {
                    row.addNull();
                } else {
                    final ArrayNode rgb = row.addArray();
                    final ConstellationColor cv = ConstellationColor.getColorValue(value);
                    rgb.add(cv.getRed());
                    rgb.add(cv.getGreen());
                    rgb.add(cv.getBlue());
                    rgb.add(cv.getAlpha());
                }
            }
            case ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME -> {
                // A DataFrame will parse null as NaT.
                if (value == null) {
                    row.addNull();
                } else {
                    // Remove the trailing tz name if present.
                    final int ix = value.lastIndexOf(" [");
                    row.add(ix == -1 ? value : value.substring(0, ix));
                }
            }
            case FloatAttributeDescription.ATTRIBUTE_NAME, FloatObjectAttributeDescription.ATTRIBUTE_NAME -> {
                // A DataFrame will parse null as NaN.
                if (value == null) {
                    row.addNull();
                } else {
                    row.add(Float.parseFloat(value));
                }
            }
            case IntegerAttributeDescription.ATTRIBUTE_NAME, IntegerObjectAttributeDescription.ATTRIBUTE_NAME -> {
                // A DataFrame will parse null as NaN, but the column will be
                // converted to a float column.
                if (value == null) {
                    row.addNull();
                } else {
                    row.add(Integer.parseInt(value));
                }
            }
            default -> 
                // Everything else we leave as a string; nulls are fine.
                row.add(value);
        }
    }
}
