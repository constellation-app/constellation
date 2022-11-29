/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.value.utilities;

import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;

/**
 *
 * A collection of utility methods to assist in using the Expressions framework
 *
 * @author aldebaran30701
 */
public final class ExpressionUtilities {

    private ExpressionUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Tests if the Expression String parses correctly. Does not check if there
     * are returnable results. Suppresses the error dialogs within the parse
     * method.
     *
     * @param queryText the expression string to test
     * @return true if the query is valid, false otherwise
     */
    public static boolean testQueryValidity(final String queryText) {
        ExpressionParser.hideErrorPrompts(true);
        final boolean validity = ExpressionParser.parse(queryText) != null;
        ExpressionParser.hideErrorPrompts(false);

        return validity;
    }

}
