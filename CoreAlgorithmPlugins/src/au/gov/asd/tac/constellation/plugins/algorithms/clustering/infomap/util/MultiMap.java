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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Quick std::multimap copy, sufficient for our purposes.
 *
 * @author algol
 * @param <K> the type of the keys in this map.
 * @param <V> the type of the values in this map.
 */
public class MultiMap<K, V> {

    private final TreeMap<K, List<V>> map;

    public MultiMap() {
        map = new TreeMap<>();
    }

    public MultiMap(final Comparator<K> comparator) {
        map = new TreeMap<>(comparator);
    }

    public void put(final K key, final V value) {
        if (map.containsKey(key)) {
            final List<V> list = map.get(key);
            list.add(value);
        } else {
            final List<V> list = new ArrayList<>();
            list.add(value);
            map.put(key, list);
        }
    }

    public List<V> get(final K key) {
        return map.get(key);
    }

    public Iterable<Map.Entry<K, V>> entrySet() {
        return () -> new MultiMapIterator<>(map);
    }

    public int size() {
        int s = 0;
        for (final Map.Entry<K, List<V>> entry : map.entrySet()) {
            s += entry.getValue().size();
        }

        return s;
    }

    private static final class MultiMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Map.Entry<K, List<V>>> iterator;
        private Map.Entry<K, List<V>> currentEntry;
        private int currentIndex;

        private MultiMapIterator(final Map<K, List<V>> map) {
            iterator = map.entrySet().iterator();
            if (iterator.hasNext()) {
                currentEntry = iterator.next();
                currentIndex = 0;
            } else {
                currentEntry = null;
                currentIndex = -1;
            }
        }

        @Override
        public boolean hasNext() {
            return currentEntry != null && currentIndex >= 0;
        }

        @Override
        public Map.Entry<K, V> next() {
            final List<V> list = currentEntry.getValue();
            final Map.Entry<K, V> entry = new MapEntry<>(currentEntry.getKey(), list.get(currentIndex));
            currentIndex++;
            if (currentIndex == list.size()) {
                if (iterator.hasNext()) {
                    currentEntry = iterator.next();
                    currentIndex = 0;
                } else {
                    currentEntry = null;
                    currentIndex = -1;
                }
            }

            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private static final class MapEntry<K, V> implements Map.Entry<K, V> {

        private final K key;
        private final V value;

        private MapEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
