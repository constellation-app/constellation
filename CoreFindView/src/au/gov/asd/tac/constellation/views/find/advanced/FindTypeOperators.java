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
package au.gov.asd.tac.constellation.views.find.advanced;

import java.util.EnumSet;
import java.util.Set;
import org.openide.util.NbBundle.Messages;

/**
 * Class that defines the different types and operations that Find utilises.
 *
 * @author betelgeuse
 */
public class FindTypeOperators {

    /**
     * Supported Find types.
     */
    public enum Type {

        /**
         * BOOLEAN types.
         */
        /**
         * BOOLEAN types.
         */
        BOOLEAN("BOOLEAN", Operator.BOOLEAN_SET),
        /**
         * COLOR types.
         */
        COLOR("COLOR", Operator.COLOR_SET),
        /**
         * DATE types.
         */
        DATE("DATE", Operator.DATE_SET),
        /**
         * DATETIME types.
         */
        DATETIME("DATETIME", Operator.DATETIME_SET),
        /**
         * FLOAT types.
         */
        FLOAT("FLOAT", Operator.FLOAT_SET),
        /**
         * INTEGER types.
         */
        INTEGER("INTEGER", Operator.INTEGER_SET),
        /**
         * ICON types.
         */
        ICON("ICON", Operator.ICON_SET),
        /**
         * STRING types.
         */
        STRING("STRING", Operator.STRING_SET),
        /**
         * TIME types.
         */
        TIME("TIME", Operator.TIME_SET);
        private final String label;
        private final Set<Operator> operators;

        /**
         * Constructs a Type.
         *
         * @param label The label of the given type.
         * @param operators The set of operators applicable to the given type.
         */
        private Type(final String label, final Set<Operator> operators) {
            this.label = label;
            this.operators = operators;
        }

        @Override
        public String toString() {
            return label;
        }

        /**
         * Determines the enum for the given String 'type'.
         *
         * @param type The string to be matched against the relevant enum.
         * @return The enum for the given type.
         */
        public static Type getTypeEnum(final String type) {
            final String item = type.toUpperCase();

            try {
                return Type.valueOf(item);
            } catch (IllegalArgumentException ex) {
                // Treat unknown types as strings:
                return STRING;
            }
        }

        /**
         * Returns the set of operators used by this type.
         *
         * @return EnumSet operators.
         */
        public Set<Operator> getOperatorSet() {
            return operators;
        }
    }

    /**
     * Find operators.
     */
    @Messages({
        "Oper_IS=is",
        "Oper_IS_NOT=is not",
        "Oper_OCCURRED_ON=occurred on",
        "Oper_NOT_OCCURRED_ON=didn't occur on",
        "Oper_OCCURRED_BEFORE=occurred before",
        "Oper_OCCURRED_AFTER=occurred after",
        "Oper_OCCURRED_BETWEEN=occurred between",
        "Oper_LESS_THAN=is less than",
        "Oper_GREATER_THAN=is greater than",
        "Oper_BETWEEN=is between",
        "Oper_CONTAINS=contains",
        "Oper_NOT_CONTAINS=doesn't contain",
        "Oper_BEGINS_WITH=begins with",
        "Oper_ENDS_WITH=ends with",
        "Oper_REGEX=matches (regex)"
    })
    public enum Operator {

        /**
         * Temporal: '=='.
         */
        OCCURRED_ON(Bundle.Oper_OCCURRED_ON()),
        /**
         * Temporal: '!='.
         */
        NOT_OCCURRED_ON(Bundle.Oper_NOT_OCCURRED_ON()),
        /**
         * Temporal; '&lt;'.
         */
        OCCURRED_BEFORE(Bundle.Oper_OCCURRED_BEFORE()),
        /**
         * Temporal: '&gt;'.
         */
        OCCURRED_AFTER(Bundle.Oper_OCCURRED_AFTER()),
        /**
         * Temporal: '&gt; a and &lt; b'.
         */
        OCCURRED_BETWEEN(Bundle.Oper_OCCURRED_BETWEEN()),
        /**
         * Synonymous with: '&lt;'.
         */
        LESS_THAN(Bundle.Oper_LESS_THAN()),
        /**
         * Synonymous with: '&gt;'.
         */
        GREATER_THAN(Bundle.Oper_GREATER_THAN()),
        /**
         * Synonymous with: '&gt; a and &lt; b'.
         */
        BETWEEN(Bundle.Oper_BETWEEN()),
        /**
         * Is a substring of: 'Aa<b>rdv</b>ark'.
         */
        CONTAINS(Bundle.Oper_CONTAINS()),
        /**
         * Is not a substring of: 'Aardvark'.
         */
        NOT_CONTAINS(Bundle.Oper_NOT_CONTAINS()),
        /**
         * Synonymous with '=='.
         */
        IS(Bundle.Oper_IS()),
        /**
         * Synonymous with '!='.
         */
        IS_NOT(Bundle.Oper_IS_NOT()),
        /**
         * String begins with: '<b>Aar</b>dvark'.
         */
        BEGINS_WITH(Bundle.Oper_BEGINS_WITH()),
        /**
         * String ends with: 'Aardv<b>ark</b>'.
         */
        ENDS_WITH(Bundle.Oper_ENDS_WITH()),
        /**
         * String matches the given regular expression.
         */
        REGEX(Bundle.Oper_REGEX());
        private final String label;

        /**
         * Construct an Operator.
         *
         * @param label The pretty string for this operator.
         */
        Operator(final String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        /**
         * Determines the enum for the given String 'operator'.
         *
         * @param operator The string to be matched against the relevant enum.
         * @return The enum for the given type.
         */
        public static Operator getTypeEnum(final String operator) {
            final String item = operator.toUpperCase();

            return Operator.valueOf(item);
        }
        /**
         * The collection of operators used for BOOLEAN types.
         */
        private static final EnumSet<Operator> BOOLEAN_SET = EnumSet.of(
                IS);
        /**
         * The collection of operators used for COLOR types.
         */
        private static final EnumSet<Operator> COLOR_SET = EnumSet.of(
                IS,
                IS_NOT);
        /**
         * The collection of operators used for DATE types.
         */
        private static final EnumSet<Operator> DATE_SET = EnumSet.of(
                OCCURRED_ON,
                NOT_OCCURRED_ON,
                OCCURRED_BEFORE,
                OCCURRED_AFTER,
                OCCURRED_BETWEEN);
        /**
         * The collection of operators used for DATETIME types.
         */
        private static final EnumSet<Operator> DATETIME_SET = EnumSet.of(
                OCCURRED_ON,
                NOT_OCCURRED_ON,
                OCCURRED_BEFORE,
                OCCURRED_AFTER,
                OCCURRED_BETWEEN);
        /**
         * The collection of operators used for FLOAT types.
         */
        private static final EnumSet<Operator> FLOAT_SET = EnumSet.of(
                IS,
                IS_NOT,
                LESS_THAN,
                GREATER_THAN,
                BETWEEN);
        /**
         * The collection of operators used for INTEGER types.
         */
        private static final EnumSet<Operator> INTEGER_SET = EnumSet.of(
                IS,
                IS_NOT,
                LESS_THAN,
                GREATER_THAN,
                BETWEEN);
        /**
         * The collection of operators used for ICON types.
         */
        private static final EnumSet<Operator> ICON_SET = EnumSet.of(
                IS,
                IS_NOT);
        /**
         * The collection of operators used for STRING types.
         */
        private static final EnumSet<Operator> STRING_SET = EnumSet.of(
                CONTAINS,
                NOT_CONTAINS,
                IS,
                IS_NOT,
                BEGINS_WITH,
                ENDS_WITH,
                REGEX);
        /**
         * The collection of operators used for TIME types.
         */
        private static final EnumSet<Operator> TIME_SET = EnumSet.of(
                OCCURRED_ON,
                NOT_OCCURRED_ON,
                OCCURRED_BEFORE,
                OCCURRED_AFTER,
                OCCURRED_BETWEEN);
    }
}
