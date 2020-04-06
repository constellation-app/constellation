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
package au.gov.asd.tac.constellation.graph.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The ImmutableObjectCache de-duplicates instances of the same immutable object
 * so that a single instance can be used in all locations. It is important that
 * the objects passed to the cache are immutable or else strange bugs will start
 * appearing.
 *
 * @author sirius
 */
public class ImmutableObjectCache {

    private static final boolean VERBOSE = false;

    private final Map<Class<?>, int[]> results;
    private long savedStringBytes = 0;

    private final Map<Object, Object> cache = new HashMap<>();

    public ImmutableObjectCache() {
        if (VERBOSE) {
            results = new HashMap<>();
        } else {
            results = null;
        }
    }

    /**
     * Returns the caches instance of the specified immutable object.
     *
     * If the object is not currently in the cache then it is added and the
     * original object is returned. If the object is in the cache then the
     * cached version is returned and the original is thrown away.
     *
     * If null is passed then null is returned.
     *
     * @param <T> the type of object to deduplicate.
     * @param immutableObject the object to deduplicate.
     *
     * @return the deduplicated object.
     */
    @SuppressWarnings("unchecked") // Cache always has object of same type
    public <T> T deduplicate(T immutableObject) {
        if (immutableObject == null) {
            return null;
        }

        if (VERBOSE) {
            int[] classResult = results.get(immutableObject.getClass());
            if (classResult == null) {
                classResult = new int[3];
                results.put(immutableObject.getClass(), classResult);
            }

            final Object cachedInstance = cache.get(immutableObject);
            if (cachedInstance == null) {
                cache.put(immutableObject, immutableObject);
                classResult[0]++;
                return immutableObject;
            } else {
                if (cachedInstance == immutableObject) {
                    classResult[1]++;
                } else {
                    classResult[2]++;
                    if (immutableObject instanceof String) {
                        savedStringBytes += 8 + 2 + ((String) immutableObject).length() * 2;
                    }
                }
                return (T) cachedInstance;
            }

        } else {
            final Object cachedInstance = cache.get(immutableObject);
            if (cachedInstance == null) {
                cache.put(immutableObject, immutableObject);
                return immutableObject;
            } else {
                return (T) cachedInstance;
            }
        }
    }

    @Override
    public String toString() {
        if (VERBOSE) {
            final StringBuilder out = new StringBuilder();
            out.append("ImmutableObjectCache[entries = ").append(cache.size()).append("]\n");
            for (Entry<Class<?>, int[]> e : results.entrySet()) {
                out.append("    ").append(e.getKey().getCanonicalName()).append(":");
                out.append(" new = ").append(e.getValue()[0]);
                out.append(", old = ").append(e.getValue()[1]);
                out.append(", dedupe = ").append(e.getValue()[2]);
                out.append("\n");
            }
            out.append("    saved String bytes = ").append(savedStringBytes).append("\n");
            return out.toString();
        } else {
            return "ImmutableObjectCache[entries = " + cache.size() + "]";
        }
    }
}
