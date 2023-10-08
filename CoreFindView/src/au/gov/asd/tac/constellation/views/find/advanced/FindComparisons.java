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

import java.awt.Color;
import java.util.Calendar;
import java.util.Date;

/**
 * This class is the holder class for the collection of currently supported
 * attribute type comparisons within the Find classes.
 * <p>
 * Currently supported attribute type comparisons include:
 * <ul>
 * <li><code>Boolean</code></li>
 * <li><code>Color</code></li>
 * <li><code>Date</code></li>
 * <li><code>DateTime</code></li>
 * <li><code>Float</code></li>
 * <li><code>Icon</code></li>
 * <li><code>String</code></li>
 * <li><code>Time</code></li>
 * </ul>
 *
 * @see BooleanComparisons
 * @see ColorComparisons
 * @see DateComparisons
 * @see DateTimeComparisons
 * @see FloatComparisons
 * @see IconComparisons
 * @see StringComparisons
 * @see TimeComparisons
 *
 * @author betelgeuse
 */
public class FindComparisons {

    private FindComparisons(){        
    }
    
    /**
     * Collection of currently allowed Boolean comparison operations.
     *
     * @see FindComparisons
     */
    public static class BooleanComparisons {
        
        private BooleanComparisons(){            
        }

        /**
         * Determines whether a given <code>Boolean</code> matches the
         * <code>Boolean</code> returned for a given element on the active
         * graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Boolean</code> matches,
         * <code>false</code> if it does not.
         */
        public static boolean evaluateIs(final boolean item, final boolean comparison) {
            return item == comparison;
        }
    }

    /**
     * Collection of currently allowed Color comparison operations.
     *
     * @see FindComparisons
     * @see Color
     */
    public static class ColorComparisons {

        private ColorComparisons() {
        }

        /**
         * Determines whether a given <code>Color</code> matches the
         * <code>Color</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Color</code> matches,
         * <code>false</code> if it does not.
         * @see Color
         */
        public static boolean evaluateIs(final Color item, final Color comparison) {
            return item != null && comparison.equals(item);
        }

        /**
         * Determines whether a given <code>Color</code> does not match the
         * <code>Color</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Color</code> does not match,
         * <code>false</code> if it does.
         * @see Color
         */
        public static boolean evaluateIsNot(final Color item, final Color comparison) {
            return item == null || !comparison.equals(item);
        }
    }

    /**
     * Collection of currently allowed Date comparison operations.
     *
     * @see FindComparisons
     * @see Date
     */
    public static class DateComparisons {

        private DateComparisons() {
        }

        /**
         * Determines whether a given <code>Date</code> matches the
         * <code>Date</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Date</code> matches,
         * <code>false</code> if it does not.
         * @see Date
         */
        public static boolean evaluateOccurredOn(final Date item, final Date comparison) {
            return item != null && comparison != null && item.equals(comparison);
        }

        /**
         * Determines whether a given <code>Date</code> does not match the
         * <code>Date</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Date</code> does not match,
         * <code>false</code> if it does.
         * @see Date
         */
        public static boolean evaluateNotOccurredOn(final Date item, final Date comparison) {
            return item == null || comparison == null || !item.equals(comparison);
        }

        /**
         * Determines whether a given <code>Date</code> occurred before the
         * <code>Date</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Date</code> occurred before,
         * <code>false</code> if it did not.
         * @see Date
         */
        public static boolean evaluateBefore(final Date item, final Date comparison) {
            return item != null && comparison != null && item.before(comparison);
        }

        /**
         * Determines whether a given <code>Date</code> occurred after the
         * <code>Date</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Date</code> occurred after,
         * <code>false</code> if it did not.
         * @see Date
         */
        public static boolean evaluateAfter(final Date item, final Date comparison) {
            return item != null && comparison != null && item.after(comparison);
        }

        /**
         * Determines whether a given <code>Date</code> occurred between the two
         * <code>Date</code>s provided.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparisonA The query value.
         * @param comparisonB The second query value.
         * @return <code>true</code> if the <code>Date</code> occurred between
         * two comparisons, <code>false</code> if it did not.
         * @see Date
         */
        public static boolean evaluateBetween(final Date item, final Date comparisonA, final Date comparisonB) {
            boolean result;

            if (item != null) {
                final Date lower;
                final Date upper;

                if (comparisonA.before(comparisonB)) {
                    lower = comparisonA;
                    upper = comparisonB;
                } else {
                    lower = comparisonB;
                    upper = comparisonA;
                }

                result = item.equals(lower) || item.equals(upper);
                result |= item.after(lower) && item.before(upper);
            } else {
                result = false;
            }

            return result;
        }
    }

    /**
     * Collection of currently allowed DateTime comparison operations.
     *
     * @see FindComparisons
     */
    public static class DateTimeComparisons {

        private DateTimeComparisons() {
        }

        /**
         * Determines whether a given <code>Calendar</code> matches the
         * <code>Calendar</code> returned for a given element on the active
         * graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Calendar</code> matches,
         * <code>false</code> if it does not.
         * @see Calendar
         */
        public static boolean evaluateOccurredOn(final Calendar item, final Calendar comparison) {
            return item != null && comparison != null && item.equals(comparison);
        }

        /**
         * Determines whether a given <code>Calendar</code> does not match the
         * <code>Calendar</code> returned for a given element on the active
         * graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Calendar</code> does not
         * match, <code>false</code> if it does.
         * @see Calendar
         */
        public static boolean evaluateNotOccurredOn(final Calendar item, final Calendar comparison) {
            return item == null || comparison == null || !item.equals(comparison);
        }

        /**
         * Determines whether a given <code>Calendar</code> occurred before the
         * <code>Calendar</code> returned for a given element on the active
         * graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Calendar</code> occurred
         * before, <code>false</code> if it did not.
         * @see Calendar
         */
        public static boolean evaluateBefore(final Calendar item, final Calendar comparison) {
            return item != null && comparison != null && item.before(comparison);
        }

        /**
         * Determines whether a given <code>Calendar</code> occurred after the
         * <code>Calendar</code> returned for a given element on the active
         * graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Calendar</code> occurred
         * after, <code>false</code> if it did not.
         * @see Calendar
         */
        public static boolean evaluateAfter(final Calendar item, final Calendar comparison) {
            return item != null && comparison != null && item.after(comparison);
        }

        /**
         * Determines whether a given <code>Calendar</code> occurred between the
         * two <code>Calendar</code>s provided
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparisonA The query value.
         * @param comparisonB The second query value.
         * @return <code>true</code> if the <code>Calendar</code> occurred
         * between the two comparisons, <code>false</code> if it did not.
         * @see Calendar
         */
        public static boolean evaluateBetween(final Calendar item, final Calendar comparisonA, final Calendar comparisonB) {
            if (item != null) {
                final Calendar lower;
                final Calendar upper;

                if (comparisonA.before(comparisonB)) {
                    lower = comparisonA;
                    upper = comparisonB;
                } else {
                    lower = comparisonB;
                    upper = comparisonA;
                }

                boolean result = false;
                result = item.equals(lower) || item.equals(upper);
                result |= item.after(lower) && item.before(upper);

                return result;
            } else {
                return false;
            }
        }
    }

    /**
     * Collection of currently allowed Float comparison operations.
     *
     * @see FindComparisons
     */
    public static class FloatComparisons {

        public FloatComparisons() {
        }

        /**
         * Determines whether a given <code>Float</code> matches the
         * <code>Float</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Float</code> matches,
         * <code>false</code> if it does not.
         */
        public static boolean evaluateIs(final float item, final float comparison) {
            return item == comparison;
        }

        /**
         * Determines whether a given <code>Float</code> does not match the
         * <code>Float</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Float</code> does not match,
         * <code>false</code> if it does.
         */
        public static boolean evaluateIsNot(final float item, final float comparison) {
            return item != comparison;
        }

        /**
         * Determines whether the <code>Float</code> returned from the graph is
         * less than the comparison <code>Float</code>.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Float</code> is less than the
         * comparison, <code>false</code> if it is not.
         */
        public static boolean evaluateLessThan(final float item, final float comparison) {
            return item < comparison;
        }

        /**
         * Determines whether the <code>Float</code> returned from the graph is
         * greater than the comparison <code>Float</code>.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Float</code> is greater than
         * the comparison, <code>false</code> if it is not.
         */
        public static boolean evaluateGreaterThan(final float item, final float comparison) {
            return item > comparison;
        }

        /**
         * Determines whether the <code>Float</code> returned from the graph is
         * between the two comparison <code>Float</code>s.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparisonA The query value.
         * @param comparisonB The second query value.
         * @return <code>true</code> if the <code>Float</code> is between the
         * comparisons, <code>false</code> if it is not.
         */
        public static boolean evaluateBetween(final float item, final float comparisonA, final float comparisonB) {
            final float lower = Math.min(comparisonA, comparisonB);
            final float upper = Math.max(comparisonA, comparisonB);

            return lower <= item && item <= upper;
        }
    }

    /**
     * Collection of currently allowed Int comparison operations.
     *
     * @see IntComparisons
     */
    public static class IntComparisons {

        private IntComparisons() {
        }

        /**
         * Determines whether a given <code>Int</code> matches the
         * <code>Int</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Int</code> matches,
         * <code>false</code> if it does not.
         */
        public static boolean evaluateIs(final int item, final int comparison) {
            return item == comparison;
        }

        /**
         * Determines whether a given <code>Int</code> does not match the
         * <code>Int</code> returned for a given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Int</code> does not match,
         * <code>false</code> if it does.
         */
        public static boolean evaluateIsNot(final int item, final int comparison) {
            return item != comparison;
        }

        /**
         * Determines whether the <code>Int</code> returned from the graph is
         * less than the comparison <code>Int</code>.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Int</code> is less than the
         * comparison, <code>false</code> if it is not.
         */
        public static boolean evaluateLessThan(final int item, final int comparison) {
            return item < comparison;
        }

        /**
         * Determines whether the <code>Int</code> returned from the graph is
         * greater than the comparison <code>Int</code>.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Int</code> is greater than the
         * comparison, <code>false</code> if it is not.
         */
        public static boolean evaluateGreaterThan(final int item, final int comparison) {
            return item > comparison;
        }

        /**
         * Determines whether the <code>Int</code> returned from the graph is
         * between the two comparison <code>Int</code>s.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparisonA The query value.
         * @param comparisonB The second query value.
         * @return <code>true</code> if the <code>Int</code> is between the
         * comparisons, <code>false</code> if it is not.
         */
        public static boolean evaluateBetween(final int item, final int comparisonA, final int comparisonB) {
            final float lower = Math.min(comparisonA, comparisonB);
            final float upper = Math.max(comparisonA, comparisonB);

            return lower <= item && item <= upper;
        }
    }

    /**
     * Collection of currently allowed Icon comparison operations.
     *
     * @see FindComparisons
     */
    public static class IconComparisons {

        private IconComparisons() {
        }

        /**
         * Determines whether a given icon (represented by its
         * <code>String</code> name) matches the icon returned for a given
         * element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the icon matches, <code>false</code> if
         * it does not.
         */
        public static boolean evaluateIs(final String item, final String comparison) {
            return item != null && comparison != null && comparison.equals(item);
        }

        /**
         * Determines whether a given icon (represented by its
         * <code>String</code> name) does not match the icon returned for a
         * given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the icon does not match,
         * <code>false</code> if it does.
         */
        public static boolean evaluateIsNot(final String item, final String comparison) {
            return item == null || comparison == null || !comparison.equals(item);
        }
    }

    /**
     * Collection of currently allowed String comparison operations.
     *
     * @see FindComparisons
     */
    public static class StringComparisons {

        private StringComparisons() {
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph matches the comparison <code>String</code>.
         * <p>
         * This operation can be optionally case sensitive.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @param isCaseSensitive Whether or not to perform a case sensitive
         * version of this operation.
         * @return <code>true</code> if this comparison matches (taking into
         * consideration case sensitivity if necessary), <code>false</code> if
         * it does not.
         */
        public static boolean evaluateIs(final String item, final String comparison, final boolean isCaseSensitive) {
            if (!isCaseSensitive) {
                return item != null && item.equalsIgnoreCase(comparison);
            } else {
                return item != null && item.equals(comparison);
            }
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph does not match the comparison <code>String</code>.
         * <p>
         * This operation can be optionally case sensitive.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @param isCaseSensitive Whether or not to perform a case sensitive
         * version of this operation.
         * @return <code>true</code> if this comparison does not match (taking
         * into consideration case sensitivity if necessary), <code>false</code>
         * if it does.
         */
        public static boolean evaluateIsNot(final String item, final String comparison, final boolean isCaseSensitive) {
            if (!isCaseSensitive) {
                return item == null || !item.equalsIgnoreCase(comparison);
            } else {
                return item == null || !item.equals(comparison);
            }
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph contains the comparison <code>String</code>.
         * <p>
         * This operation can be optionally case sensitive.
         *
         * @param item The value returned for a particular GraphElement.
         * @param substring The query value.
         * @param isCaseSensitive Whether or not to perform a case sensitive
         * version of this operation.
         * @return <code>true</code> if this item contains the comparison
         * (taking into consideration case sensitivity if necessary),
         * <code>false</code> if it does not.
         */
        public static boolean evaluateContains(final String item, final String substring, final boolean isCaseSensitive) {
            if (!isCaseSensitive) {
                return item != null && item.toLowerCase().contains(substring.toLowerCase());
            } else {
                return item != null && item.contains(substring);
            }
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph doesn't contain the comparison <code>String</code>.
         * <p>
         * This operation can be optionally case sensitive.
         *
         * @param item The value returned for a particular GraphElement.
         * @param substring The query value.
         * @param isCaseSensitive Whether or not to perform a case sensitive
         * version of this operation.
         * @return <code>true</code> if this item does not contain the
         * comparison (taking into consideration case sensitivity if necessary),
         * <code>false</code> if does.
         */
        public static boolean evaluateNotContains(final String item, final String substring, final boolean isCaseSensitive) {
            if (!isCaseSensitive) {
                return item == null || !item.toLowerCase().contains(substring.toLowerCase());
            } else {
                return item == null || !item.contains(substring);
            }
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph begins with the comparison <code>String</code>.
         * <p>
         * This operation can be optionally case sensitive.
         *
         * @param item The value returned for a particular GraphElement.
         * @param prefix The query value.
         * @param isCaseSensitive Whether or not to perform a case sensitive
         * version of this operation.
         * @return <code>true</code> if this item begins with the comparison
         * (taking into consideration case sensitivity if necessary),
         * <code>false</code> if it does not.
         */
        public static boolean evaluateBeginsWith(final String item, final String prefix, final boolean isCaseSensitive) {
            if (!isCaseSensitive) {
                return item != null && item.toLowerCase().startsWith(prefix.toLowerCase());
            } else {
                return item != null && item.startsWith(prefix);
            }
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph ends with the comparison <code>String</code>.
         * <p>
         * This operation can be optionally case sensitive.
         *
         * @param item The value returned for a particular GraphElement.
         * @param suffix The query value.
         * @param isCaseSensitive Whether or not to perform a case sensitive
         * version of this operation.
         * @return <code>true</code> if this item ends with the comparison
         * (taking into consideration case sensitivity if necessary),
         * <code>false</code> if it does not.
         */
        public static boolean evaluateEndsWith(final String item, final String suffix, final boolean isCaseSensitive) {
            if (!isCaseSensitive) {
                return item != null && item.toLowerCase().endsWith(suffix.toLowerCase());
            } else {
                return item != null && item.endsWith(suffix);
            }
        }

        /**
         * Determines whether the returned <code>String</code> from the active
         * graph matches the given regular expression.
         *
         * @param item The value returned for a particular GraphElement.
         * @param regex The regular expression to evaluate.
         * @return <code>true</code> if this item matches the given regular
         * expression, <code>false</code> if it does not.
         */
        public static boolean evaluateRegex(final String item, final String regex) {
            return item != null && item.matches(regex);
        }
    }

    /**
     * Collection of currently allowed Time comparison operations.
     *
     * @see FindComparisons
     */
    public static class TimeComparisons {

        private TimeComparisons() {
        }

        /**
         * Determines whether a given <code>Long</code> (which represents a
         * given time), matches the <code>Long</code> returned for a given
         * element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Long</code> matches,
         * <code>false</code> if it does not.
         */
        public static boolean evaluateOccurredOn(final long item, final long comparison) {
            return item == comparison;
        }

        /**
         * Determines whether a given <code>Long</code> (which represents a
         * given time), does not match the <code>Long</code> returned for a
         * given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Long</code> does not match,
         * <code>false</code> if it does.
         */
        public static boolean evaluateNotOccurredOn(final long item, final long comparison) {
            return item != comparison;
        }

        /**
         * Determines whether a given <code>Long</code> (which represents a
         * given time), occurred before the <code>Long</code> returned for a
         * given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Long</code> occurred before,
         * <code>false</code> if it did not.
         */
        public static boolean evaluateBefore(final long item, final long comparison) {
            return item < comparison;
        }

        /**
         * Determines whether a given <code>Long</code> (which represents a
         * given time), occurred after the <code>Long</code> returned for a
         * given element on the active graph.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparison The query value.
         * @return <code>true</code> if the <code>Long</code> occurred after,
         * <code>false</code> if it did not.
         */
        public static boolean evaluateAfter(final long item, final long comparison) {
            return item > comparison;
        }

        /**
         * Determines whether a given <code>Long</code> (which represents a
         * given time), occurred between the two <code>Long</code>s provided.
         *
         * @param item The value returned for a particular GraphElement.
         * @param comparisonA The query value.
         * @param comparisonB The second query value.
         * @return <code>true</code> if the <code>Long</code> occurred between,
         * <code>false</code> if it did not.
         */
        public static boolean evaluateBetween(final long item, final long comparisonA, final long comparisonB) {
            final long lower = Math.min(comparisonA, comparisonB);
            final long upper = Math.max(comparisonA, comparisonB);

            return lower <= item && item <= upper;
        }
    }
}
