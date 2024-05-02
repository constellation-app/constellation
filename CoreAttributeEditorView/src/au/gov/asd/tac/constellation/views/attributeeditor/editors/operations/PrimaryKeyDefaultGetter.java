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
package au.gov.asd.tac.constellation.views.attributeeditor.editors.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Primary Key Default Getter
 *
 * @author twilight_sparkle
 */
public class PrimaryKeyDefaultGetter implements DefaultGetter {

    private final GraphElementType elementType;

    public PrimaryKeyDefaultGetter(final GraphElementType elementType) {
        this.elementType = elementType;
    }

    @Override
    public Object getDefaultValue() {
        final ReadableGraph rg = GraphManager.getDefault().getActiveGraph().getReadableGraph();
        List<SchemaAttribute> keys = new ArrayList<>();
        try {
            if (rg.getSchema() != null) {
                keys = rg.getSchema().getFactory().getKeyAttributes(elementType);
            }
        } finally {
            rg.release();
        }
        return keys.stream().map(s -> s.getName()).collect(Collectors.toList());
    }
}
