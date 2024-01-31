/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/*
 * @author twilight_sparkle
 */
public class TaggedSparseMatrix<N extends Number> extends SparseMatrix<N> {

    protected final ConcurrentNavigableMap<Integer, Boolean> tags;
    private final Map<Boolean, Set<Integer>> taggedColumns;

    public TaggedSparseMatrix(final N noEntryVal, final ArithmeticHandler<N> typer) {
        super(noEntryVal, typer);
        tags = new ConcurrentSkipListMap<>();
        taggedColumns = new HashMap<>();
        taggedColumns.put(true, new HashSet<>());
        taggedColumns.put(false, new HashSet<>());
    }

    public static TaggedSparseMatrix constructMatrix(final Number noEntryValue) {
        if (noEntryValue instanceof Integer) {
            return new TaggedSparseMatrix<>((Integer) noEntryValue, IntegerArithmeticHandler.INSTANCE);
        } else if (noEntryValue instanceof Float) {
            return new TaggedSparseMatrix<>((Float) noEntryValue, FloatArithmeticHandler.INSTANCE);
        } else {
            return null;
        }
    }

    public void tagColumn(final int key, final boolean tag) {
        // You can't tag a column which doesn't exist, or which is already tagged
        if (!getData().containsKey(key) || tags.containsKey(key)) {
            return;
        }
        tags.put(key, tag);
        taggedColumns.get(tag).add(key);
    }

    public Set<Integer> getColumnsWithTag(final boolean tag) {
        return new HashSet<>(taggedColumns.get(tag));
    }

    public N getLargestColumnSumWithTag(final boolean tag) {
        return getLargestColumnSum(getColumnsWithTag(tag));
    }

    public boolean hasTag(final int key) {
        return tags.containsKey(key);
    }

    public boolean isTag(final int key) {
        return tags.get(key);
    }

    @Override
    public void removeColumn(final int key) {
        super.removeColumn(key);
        tags.remove(key);
    }

    @Override
    public void clearCell(final int i, final int j) {
        super.clearCell(i, j);
        if (!getData().containsKey(i)) {
            tags.remove(i);
        }
    }

}
