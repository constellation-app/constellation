/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.datastructure;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * A data-structure for storing keyed value sets, designed to be used as a
 * generic object cache.
 *
 * @author cygnus_x-1
 *
 * @param <K> the type of keys in the cache
 * @param <V> the type of values in the cache
 */
public class ObjectCache<K extends Object, V extends Object> {

    protected final Map<K, Set<V>> CACHE;

    public ObjectCache() {
        CACHE = new ConcurrentHashMap<>();
    }

    public int size() {
        return CACHE.size();
    }

    public boolean contains(final K key) {
        return CACHE.containsKey(key);
    }

    public Set<K> keys() {
        return CACHE.keySet();
    }

    public Set<V> values() {
        return CACHE.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    public Set<V> get(final K key) {
        return CACHE.get(key);
    }

    public V getRandom(final K key) {
        return CACHE.get(key).iterator().next();
    }

    public void add(final K key, final V value) {
        if (!CACHE.containsKey(key)) {
            CACHE.put(key, new HashSet<>());
        }
        CACHE.get(key).add(value);
    }

    public Set<V> remove(final K key) {
        return CACHE.remove(key);
    }

    public void clear() {
        CACHE.clear();
    }

    public void forEach(final BiConsumer<? super K, ? super Set<V>> action) {
        Objects.requireNonNull(action);
        CACHE.entrySet().forEach(entry -> {
            final K key;
            final Set<V> value;
            try {
                key = entry.getKey();
                value = entry.getValue();
            } catch (IllegalStateException ex) {
                throw new ConcurrentModificationException(ex);
            }
            action.accept(key, value);
        });
    }

    @Override
    public String toString() {
        return CACHE.toString();
    }
}
