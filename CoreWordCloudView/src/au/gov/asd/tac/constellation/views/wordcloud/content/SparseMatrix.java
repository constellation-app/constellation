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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/*
 * @author twilight_sparkle
 */
public class SparseMatrix<N extends Number> {

    private final ConcurrentNavigableMap<Integer, ConcurrentNavigableMap<Integer, N>> data;
    private final N noEntryVal;
    private final ArithmeticHandler<N> calc;

    public SparseMatrix(final N noEntryVal, final ArithmeticHandler<N> typer) {
        data = new ConcurrentSkipListMap<>();
        this.noEntryVal = noEntryVal;
        this.calc = typer;
    }

    public static SparseMatrix constructMatrix(final Number noEntryVal) {
        if (noEntryVal instanceof Integer value) {
            return new SparseMatrix<>(value, IntegerArithmeticHandler.INSTANCE);
        } else if (noEntryVal instanceof Float value) {
            return new SparseMatrix<>(value, FloatArithmeticHandler.INSTANCE);
        } else {
            return null;
        }
    }
    
    public ArithmeticHandler<N> getCalc() {
        return calc;
    }
    
    public ConcurrentNavigableMap<Integer, ConcurrentNavigableMap<Integer, N>> getData() {
        return data;
    }

    public void putCell(final int i, final int j, final N val) {
        ConcurrentNavigableMap<Integer, N> submap = data.get(i);
        if (submap == null) {
            submap = new ConcurrentSkipListMap<>();
            data.put(i, submap);
        }
        submap.put(j, val);
    }

    // NB: creates the i-th column if it doesn't exist while trying to get the ij-th cell
    public N getCell(final int i, final int j) {
        ConcurrentNavigableMap<Integer, N> submap = data.get(i);
        if (submap == null) {
            submap = new ConcurrentSkipListMap<>();
            data.put(i, submap);
        }
        return submap.get(j);
    }

    public N getCellPrimitive(final int i, final int j) {
        final N cell = getCell(i, j);
        return cell == null ? noEntryVal : cell;
    }

    public void clearCell(final int i, final int j) {
        ConcurrentNavigableMap<Integer, N> submap = data.get(i);
        submap.remove(j);
        if (submap.isEmpty()) {
            data.remove(i);
        }
    }

    public Integer[] getColumnKeys() {
        final Integer[] keys = new Integer[0];
        return data.keySet().toArray(keys);
    }

    public int getNumColumns() {
        return data.size();
    }

    public static final int TAXICAB_DISTANCE = 1;
    public static final int EUCLIDIAN_DISTANCE = 2;
    public static final int EUCLIDIAN_DISTANCE_OVER_COMMONALITY = 0;

    public float getEuclidianDistanceBetweenColumns(final int key1, final int key2) {
        return getDistanceBetweenColumns(key1, key2, EUCLIDIAN_DISTANCE);
    }

    public float getCommonalityDistanceBetweenColumns(final int key1, final int key2) {
        return getDistanceBetweenColumns(key1, key2, EUCLIDIAN_DISTANCE_OVER_COMMONALITY);
    }

    public float getDistanceBetweenColumns(final int key1, final int key2, final int method) {
        if (key1 == key2) {
            return 0;
        }

        final MatrixColumnIterator iter1 = getColumn(key1);
        final MatrixColumnIterator iter2 = getColumn(key2);
        N distance = calc.getZero();
        N commonality = calc.getZero();
        ElementValuePair<N> entry1 = iter1.next();
        ElementValuePair<N> entry2 = iter2.next();
        boolean entry1Finished = false;
        boolean entry2Finished = false;

        while (!entry1Finished || !entry2Finished) {
            if (entry1.el == entry2.el) {
                N componentDistance = calc.difference(entry1.val, entry2.val);
                if (method != TAXICAB_DISTANCE) {
                    componentDistance = calc.square(componentDistance);
                }
                distance = calc.add(distance, componentDistance);
                commonality = calc.add(commonality, calc.min(entry1.val, entry2.val));
                if (iter1.hasNext()) {
                    entry1 = iter1.next();
                } else {
                    entry1Finished = true;
                }
                if (iter2.hasNext()) {
                    entry2 = iter2.next();
                } else {
                    entry2Finished = true;
                }
            } else if (!entry2Finished && (entry1.el > entry2.el || entry1Finished)) {
                N componentDistance = entry2.val;
                if (method != TAXICAB_DISTANCE) {
                    componentDistance = calc.square(componentDistance);
                }
                distance = calc.add(distance, componentDistance);
                if (iter2.hasNext()) {
                    entry2 = iter2.next();
                } else {
                    entry2Finished = true;
                }
            } else if (!entry1Finished && (entry1.el < entry2.el || entry2Finished)) {
                N componentDistance = entry1.val;
                if (method != TAXICAB_DISTANCE) {
                    componentDistance = calc.square(componentDistance);
                }
                distance = calc.add(distance, componentDistance);
                if (iter1.hasNext()) {
                    entry1 = iter1.next();
                } else {
                    entry1Finished = true;
                }
            }
        }
        if (method != TAXICAB_DISTANCE) {
            distance = calc.sqrt(distance);
        }

        if (method == EUCLIDIAN_DISTANCE_OVER_COMMONALITY) {
            distance = calc.scale(distance, 1 + commonality.floatValue());
        }
        return distance.floatValue();
    }

    public void calculateCentreOfColumns(final Integer[] keys, final int keyToPlaceCentre) {
        final ConcurrentNavigableMap<Integer, N> centreMap = new ConcurrentSkipListMap<>();
        final int numberOfColumns = keys.length;
        for (final Integer key : keys) {
            final MatrixColumnIterator iter = getColumn(key);
            while (iter.hasNext()) {
                final ElementValuePair<N> entry = iter.next();
                if (centreMap.get(entry.el) == null) {
                    centreMap.put(entry.el, calc.scale(entry.val, numberOfColumns));
                } else {
                    centreMap.put(entry.el, calc.add(calc.scale(entry.val, numberOfColumns), centreMap.get(entry.el)));
                }
            }
        }
        data.put(keyToPlaceCentre, centreMap);
    }

    public MatrixColumnIterator getColumn(final int key) {
        return new MatrixColumnIterator(data.get(key));
    }

    public ConcurrentNavigableMap<Integer, N> getColumnMap(final int key) {
        return data.get(key);
    }

    public boolean hasColumn(final int key) {
        return data.get(key) != null;
    }

    public N[] getColumnAsExpandedArray(final int key, final int fullColumnSize) {
        final N[] column = calc.makeArray(fullColumnSize);
        data.get(key).values().toArray(column);
        Arrays.fill(column, getColumnSize(key), column.length, calc.getZero());
        return column;
    }

    public N[] getColumnAsArray(final int key) {
        final N[] column = calc.makeArray(getColumnSize(key));
        data.get(key).values().toArray(column);
        return column;
    }

    public N getColumnSum(final int key) {
        N sum = noEntryVal;
        final MatrixColumnIterator iter = getColumn(key);
        while (iter.hasNext()) {
            sum = calc.add(sum, iter.next().val);
        }
        return sum;
    }

    public Set<Integer> getColumnElementUnion(final Iterable<Integer> keySet) {
        final Set<Integer> elements = new HashSet<>();
        for (final int key : keySet) {
            if (data.containsKey(key)) {
                elements.addAll(data.get(key).keySet());
            }
        }
        return elements;
    }

    public Set<Integer> getColumnElementIntersection(final Iterable<Integer> keySet) {
        final Set<Integer> elements = new HashSet<>();
        for (final int key : keySet) {
            if (elements.isEmpty() && data.containsKey(key)) {
                elements.addAll(data.get(key).keySet());
            } else {
                final Iterator<Integer> iter = elements.iterator();
                while (iter.hasNext()) {
                    int element = iter.next();
                    if (!data.containsKey(key) || !data.get(key).containsKey(element)) {
                        iter.remove();
                    }
                }
            }
            if (elements.isEmpty()) {
                break;
            }
        }
        return elements;
    }

    public N[] getConstituentExtendedColumnAsArray(final int key, final Iterable<Integer> elements) {
        if (data.get(key) == null) {
            return null;
        }
        final Map<Integer, N> subcolumn = new ConcurrentSkipListMap<>(data.get(key));
        for (final int element : elements) {
            if (!subcolumn.keySet().contains(element)) {
                subcolumn.put(element, calc.getZero());
            }
        }
        final N[] subcolumnAsArray = calc.makeArray(subcolumn.size());
        subcolumn.values().toArray(subcolumnAsArray);
        return subcolumnAsArray;
    }

    public N getLargestColumnSum() {
        return getLargestColumnSum(data.keySet());
    }

    public N getLargestColumnSum(final Set<Integer> keys) {
        N largestColumnSum = noEntryVal;
        for (final int i : keys) {
            final N colSum = getColumnSum(i);
            largestColumnSum = calc.max(colSum, largestColumnSum);
        }
        return largestColumnSum;
    }

    public int getColumnSize(final int key) {
        return data.get(key).size();
    }

    public int getLargestColumnSize() {
        int largestColumnSize = 0;
        for (final int i : getColumnKeys()) {
            int colSize = getColumnSize(i);
            if (colSize > largestColumnSize) {
                largestColumnSize = colSize;
            }
        }
        return largestColumnSize;
    }

    public void removeColumn(final int key) {
        data.remove(key);
    }

    /**
     * Constructs a view of this matrix which is a map linking tokens to the sets of elements with these tokens. 
     * This is a deep constructions, that is changes to the returned map do not affect the matrix from which it was created and vice versa.
     */
    public Map<Integer, Set<Integer>> constructTokenSets() {
        final Map<Integer, Set<Integer>> tokenSets = new HashMap<>();
        final Integer[] tokens = getColumnKeys();
        for (final Integer token : tokens) {
            final Set<Integer> tokenSet = new HashSet<>();
            final SparseMatrix<N>.MatrixColumnIterator iter = getColumn(token);
            while (iter.hasNext()) {
                tokenSet.add(iter.next().el);
            }
            tokenSets.put(token, tokenSet);
        }
        return tokenSets;
    }

    public static final class ElementValuePair<N extends Number> {

        public final int el;
        public final N val;

        public ElementValuePair(final int index, final N val) {
            this.el = index;
            this.val = val;
        }
    }
    
    
    protected abstract static class ArithmeticHandler<N extends Number> {

        public abstract N getZero();

        public abstract N max(final N n1, final N n2);

        public abstract N min(final N n1, final N n2);

        public abstract N add(final N n1, final N n2);

        public abstract N difference(final N n1, final N n2);

        public abstract N square(final N n1);

        public abstract N sqrt(final N n1);

        public abstract N scale(final N val, final float scale);

        public abstract N[] makeArray(final int size);
    }
    

    protected static class IntegerArithmeticHandler extends ArithmeticHandler<Integer> {

        protected static final ArithmeticHandler<Integer> INSTANCE = new IntegerArithmeticHandler();

        @Override
        public Integer getZero() {
            return 0;
        }

        @Override
        public Integer max(final Integer n1, final Integer n2) {
            return Math.max(n1, n2);
        }

        @Override
        public Integer min(final Integer n1, final Integer n2) {
            return Math.min(n1, n2);
        }

        @Override
        public Integer add(final Integer n1, final Integer n2) {
            return n1 + n2;
        }

        @Override
        public Integer difference(final Integer n1, final Integer n2) {
            return Math.abs(n1 - n2);
        }

        @Override
        public Integer square(final Integer n1) {
            return (int) Math.round(Math.pow(n1.doubleValue(), 2.0));
        }

        @Override
        public Integer sqrt(final Integer n1) {
            return (int) Math.round(Math.sqrt(n1.doubleValue()));
        }

        @Override
        public Integer scale(final Integer val, final float scale) {
            return Math.round(val / scale);
        }

        @Override
        public Integer[] makeArray(final int size) {
            return new Integer[size];
        }
    }
    

    protected static class FloatArithmeticHandler extends ArithmeticHandler<Float> {

        protected static final ArithmeticHandler<Float> INSTANCE = new FloatArithmeticHandler();

        @Override
        public Float getZero() {
            return 0.0F;
        }

        @Override
        public Float max(final Float n1, final Float n2) {
            return Math.max(n1, n2);
        }

        @Override
        public Float min(final Float n1, final Float n2) {
            return Math.min(n1, n2);
        }

        @Override
        public Float add(final Float n1, final Float n2) {
            return n1 + n2;
        }

        @Override
        public Float difference(final Float n1, final Float n2) {
            return Math.abs(n1 - n2);
        }

        @Override
        public Float square(final Float n1) {
            return (float) Math.round(Math.pow(n1.doubleValue(), 2.0));
        }

        @Override
        public Float sqrt(final Float n1) {
            return (float) Math.round(Math.sqrt(n1.doubleValue()));
        }

        @Override
        public Float scale(final Float val, final float scale) {
            return val / scale;
        }

        @Override
        public Float[] makeArray(final int size) {
            return new Float[size];
        }
    }
    

    public class MatrixColumnIterator implements Iterator {

        private final Iterator<Integer> keyIter;
        private final Iterator<N> valIter;
        private int currentKey;
        private final int size;
        private final ConcurrentNavigableMap<Integer, N> submap;

        protected MatrixColumnIterator(final ConcurrentNavigableMap<Integer, N> submap) {
            this.submap = submap;
            this.size = submap.size();
            keyIter = submap.keySet().iterator();
            valIter = submap.values().iterator();
        }

        public MatrixColumnIterator tailIterator() {
            return new MatrixColumnIterator(submap.tailMap(currentKey, false));
        }

        public int getSize() {
            return size;
        }

        @Override
        public boolean hasNext() {
            return keyIter.hasNext();
        }

        @Override
        public ElementValuePair<N> next() {
            currentKey = keyIter.next();
            final N val = valIter.next();
            return new ElementValuePair<>(currentKey, val);
        }

        @Override
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }
}
