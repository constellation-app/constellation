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
package au.gov.asd.tac.constellation.utilities.datastructure;

import java.util.HashMap;
import java.util.Map;

/**
 * The ImmutableObjectCache de-duplicates instances of the same immutable object
 * so that a single instance can be used in all locations. It is important that
 * the objects passed to the cache are immutable or else strange bugs will start
 * appearing.
 *
 * @author sirius
 */
public class ImmutableObjectCache {

    private final Map<Object, Object> cache = new HashMap<>();

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
    public <T> T deduplicate(final T immutableObject) {
        if (immutableObject == null) {
            return null;
        }

        final Object cachedInstance = cache.get(immutableObject);
        if (cachedInstance == null) {
            cache.put(immutableObject, immutableObject);
            return immutableObject;
        } else {
            return (T) cachedInstance;
        }
    }

    @Override
    public String toString() {
        return "ImmutableObjectCache[entries = " + cache.size() + "]";
    }
}
