/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

/**
 * Some Utility functions to assist with testing Concept classes in Core Analytic Schema
 *
 * @author antares
 */
public class ConceptTestUtilities {
    
    private ConceptTestUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    protected static int getFieldCount(final Class<?> searchClass, final Class<?> fieldType) {
        int fieldCount = 0;
        for (final Field field : searchClass.getDeclaredFields()) {
            if (field.getType() == fieldType) {
                fieldCount++;
            }
        }
        return fieldCount;
    }
    
    protected static List<SchemaAttribute> getElementTypeSpecificAttributes(final Collection<SchemaAttribute> attributes, 
            final GraphElementType graphElementType) {
        return attributes.stream()
                .filter(attribute -> attribute.getElementType() == graphElementType)
                .toList();
    }
}
