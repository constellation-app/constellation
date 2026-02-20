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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author antares
 * @author cygnus_x-1
 */
public interface MergeTransactionType {

    public class MergeException extends Exception {

        public MergeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String getName();

    public void updateParameters(final Map<String, PluginParameter<?>> parameters);

    public Map<Integer, Set<Integer>> getTransactionsToMerge(final GraphWriteMethods graph, final Comparator<Long> leadTransactionChooser, final int threshold, final boolean selectedOnly) throws MergeException;

    /**
     * Checks the type attribute from two transactions and returns whether or
     * not the pair is eligible for merging. Transactions are eligible for
     * merging in the following three cases: 1. Same type 2. Same type
     * hierarchy. 3. No type for either.
     *
     * @param currentType
     * @param nextType
     * @return A boolean representing whether or not the two transactions with
     * the supplied attributes are eligible for merging.
     */
    public default boolean compareTypeHierarchy(final SchemaTransactionType currentType, final SchemaTransactionType nextType) {
        return (currentType != null && nextType != null & currentType.equals(nextType))
                || (currentType != null && nextType != null && currentType.isSubTypeOf(nextType))
                || (currentType != null && nextType != null && nextType.isSubTypeOf(currentType))
                || (currentType == null && nextType == null);
    }

    /**
     * Sort transactions by type and datetime in ascending order
     *
     * NOTE: This method is package protected so that unit testing can be done
     *
     * @param transactions
     * @param graph
     * @param typeAttributeId
     * @param dateTimeAttributeId
     * @param leadTransactionChooser
     */
    public default void sortTransactions(Integer[] transactions, final GraphReadMethods graph, final int typeAttributeId,
            final int dateTimeAttributeId, Comparator<Long> leadTransactionChooser) {
        Arrays.sort(transactions, (Integer o1, Integer o2) -> {
            final SchemaTransactionType type1 = graph.getObjectValue(typeAttributeId, o1);
            final SchemaTransactionType type2 = graph.getObjectValue(typeAttributeId, o2);

            if (type1 == null) {
                if (type2 != null) {
                    return 1;
                }
            } else if (type2 == null) {
                return -1;
            } else {
                final int typComparison = type1.compareTo(type2);
                if (typComparison != 0) {
                    return typComparison;
                }
            }

            final long datetime1 = graph.getLongValue(dateTimeAttributeId, o1);
            final long datetime2 = graph.getLongValue(dateTimeAttributeId, o2);

            return leadTransactionChooser.compare(datetime1, datetime2);
        });
    }
}
