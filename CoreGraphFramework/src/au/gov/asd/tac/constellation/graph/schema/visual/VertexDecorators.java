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
package au.gov.asd.tac.constellation.graph.schema.visual;

import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class VertexDecorators {

    public static final VertexDecorators NO_DECORATORS = new VertexDecorators(null, null, null, null);
    private static final char DECORATOR_DELIMITER = ';';

    private final String northWestDecoratorAttribute;
    private final String southWestDecoratorAttribute;
    private final String southEastDecoratorAttribute;
    private final String northEastDecoratorAttribute;

    public VertexDecorators(final String northWestDecoratorAttribute, final String northEastDecoratorAttribute,
            final String southEastDecoratorAttribute, final String southWestDecoratorAttribute) {
        this.northWestDecoratorAttribute = northWestDecoratorAttribute;
        this.northEastDecoratorAttribute = northEastDecoratorAttribute;
        this.southEastDecoratorAttribute = southEastDecoratorAttribute;
        this.southWestDecoratorAttribute = southWestDecoratorAttribute;
    }

    public String getNorthWestDecoratorAttribute() {
        return northWestDecoratorAttribute;
    }

    public String getNorthEastDecoratorAttribute() {
        return northEastDecoratorAttribute;
    }

    public String getSouthEastDecoratorAttribute() {
        return southEastDecoratorAttribute;
    }

    public String getSouthWestDecoratorAttribute() {
        return southWestDecoratorAttribute;
    }

    @Override
    public String toString() {
        return StringUtilities.quoteAndDelimitString(Arrays.asList(northWestDecoratorAttribute,
                northEastDecoratorAttribute, southEastDecoratorAttribute, southWestDecoratorAttribute), DECORATOR_DELIMITER);
    }

    public static VertexDecorators valueOf(final String decoratorsString) {
        if (decoratorsString == null) {
            return NO_DECORATORS;
        }
        final List<String> decorators;
        try {
            decorators = StringUtilities.unquoteAndSplitString(decoratorsString, DECORATOR_DELIMITER);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("String does not represent a decorator: " + decoratorsString);
        }
        if (decorators.size() == 4) {
            return new VertexDecorators(decorators.get(0), decorators.get(1), decorators.get(2), decorators.get(3));
        }
        throw new IllegalArgumentException("String for decorators has wrong number of fields: " + decoratorsString);
    }
}
