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
package au.gov.asd.tac.constellation.utilities.datastructure;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
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

    private boolean verbose = false;

    private Map<Class<?>, int[]> results = null;
    private long savedStringBytes = 0;

    private final Map<Object, Object> cache = new HashMap<>();
    
    /**
     * True to enable slower interactions with this class but more comprehensive
     * information from {@link #toString() toString}, false for faster but less
     * information.
     * 
     * @param verbose true to enable verbose mode, false otherwise.
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
        if (verbose) {
            results = new HashMap<>();
        } else {
            results = null;
        }
    }

    /**
     * Returns the cached instance of the specified immutable object.
     *
     * If the object is not currently in the cache then it is added and the
     * original object is returned. If the object is in the cache then the
     * cached version is returned and the original is thrown away.
     *
     * If null is passed then null is returned.
     *
     * @param <T> the type of object to de-duplicate.
     * @param immutableObject the object to de-duplicate.
     *
     * @return the de-duplicated object.
     */
    @SuppressWarnings("unchecked") // Cache always has object of same type
    public <T> T deduplicate(final T immutableObject) {
        if (immutableObject == null) {
            return null;
        }

        if (verbose) {
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
                    if (immutableObject instanceof String immutableString) {
                        savedStringBytes += 8 + 2 + immutableString.length() * 2;
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
        if (verbose) {
            final StringBuilder out = new StringBuilder();
            out.append("ImmutableObjectCache[entries = ").append(cache.size()).append("]\n");
            for (final Entry<Class<?>, int[]> e : results.entrySet()) {
                out.append("    ").append(e.getKey().getCanonicalName()).append(SeparatorConstants.COLON);
                out.append(" new = ").append(e.getValue()[0]);
                out.append(", old = ").append(e.getValue()[1]);
                out.append(", dedupe = ").append(e.getValue()[2]);
                out.append(SeparatorConstants.NEWLINE);
            }
            out.append("    saved String bytes = ").append(savedStringBytes).append(SeparatorConstants.NEWLINE);
            return out.toString();
        } else {
            return "ImmutableObjectCache[entries = " + cache.size() + "]";
        }
    }
}
