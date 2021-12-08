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
package au.gov.asd.tac.constellation.graph.schema.analytic.utilities;

import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import java.util.Comparator;
import java.util.List;
import org.openide.util.Lookup;

/**
 * A simple calculator for deciding which nodes are considered more important
 * based on a provided ordered list of types. This is intended to be used in the
 * creation of Comparator objects required by the processRecord method.
 *
 * @param <T> the object type to calculate the dominance of
 *
 * @author cygnus_x-1
 */
public abstract class VertexDominanceCalculator<T> {

    public static VertexDominanceCalculator getDefault() {
        return Lookup.getDefault().lookup(VertexDominanceCalculator.class);
    }

    public SchemaVertexType getDominant(final List<T> types) {
        types.sort(this.getComparator());
        return this.convertType(types.get(0));
    }

    public Comparator<T> getComparator() {
        return (final T type1, final T type2) -> {
            final SchemaVertexType convertedType1 = this.convertType(type1);
            final SchemaVertexType convertedType2 = this.convertType(type2);
            return Integer.compare(this.getDominance(convertedType1), this.getDominance(convertedType2));
        };
    }

    public int getDominance(final SchemaVertexType type) {
        // Note: overwritten types are not checked as there is currently
        // no way to link these back to the types overriding them. This
        // can be handled by including both the overridden type and the
        // overriding type in your provided dominance array.

        if (type == null || SchemaConceptUtilities.getDefaultVertexType().equals(type)) {
            return Integer.MAX_VALUE - 1;
        }

        SchemaVertexType testType = type.copy();
        while (true) {
            if (this.getTypePriority().contains(testType)) {
                return this.getTypePriority().indexOf(testType);
            }
            if (testType.isTopLevelType()) {
                break;
            }
            testType = (SchemaVertexType) testType.getSuperType();
        }

        return Integer.MAX_VALUE;
    }

    public abstract List<SchemaVertexType> getTypePriority();

    public abstract SchemaVertexType convertType(final T type);
}
