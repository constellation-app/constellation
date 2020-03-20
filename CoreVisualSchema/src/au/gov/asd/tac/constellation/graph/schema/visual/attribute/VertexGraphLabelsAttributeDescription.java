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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeDescription.class)
public final class VertexGraphLabelsAttributeDescription extends AbstractObjectAttributeDescription<GraphLabels> {

    private static final int DESCRIPTION_VERSION = 1;
    public static final String ATTRIBUTE_NAME = "graph_labels_nodes";
    public static final Class<GraphLabels> NATIVE_CLASS = GraphLabels.class;
    private static final GraphLabels DEFAULT_VALUE = GraphLabels.NO_LABELS;

    public VertexGraphLabelsAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    /**
     * Extract a GraphLabels from an Object.
     *
     * @param object An Object.
     *
     * @return A GraphLabels.
     */
    @Override
    @SuppressWarnings("unchecked") //Casts are checked manually
    protected GraphLabels convertFromObject(final Object object) {
        if (object == null) {
            return DEFAULT_VALUE;
        } else if (object instanceof GraphLabels) {
            return (GraphLabels) object;
        } else if (object instanceof String) {
            return convertFromString((String) object);
        } else {
            final String msg = String.format("Error converting Object '%s' to GraphLabels", object.getClass());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected GraphLabels convertFromString(String string) {
        return GraphLabels.valueOf(string);
    }

    @Override
    public int getVersion() {
        return DESCRIPTION_VERSION;
    }
}
