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
package au.gov.asd.tac.constellation.graph.versioning;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.util.Lookup;

/**
 * Update Provider Manager
 *
 * @author twilight_sparkle
 */
public class UpdateProviderManager {

    private static final SortedMap<UpdateItem, Map<Integer, UpdateProvider>> UPDATE_PROVIDER_REGISTRY = new TreeMap<>();
    private static final Map<String, Integer> LATEST_VERSIONS = new HashMap<>();
    private static boolean isBuilt = false;

    public static synchronized SortedMap<UpdateItem, Map<Integer, UpdateProvider>> getRegisteredProviders() {
        buildUpdateProviders();
        return Collections.unmodifiableSortedMap(UPDATE_PROVIDER_REGISTRY);
    }

    public static synchronized Map<String, Integer> getLatestVersions() {
        buildUpdateProviders();
        return Collections.unmodifiableMap(LATEST_VERSIONS);
    }

    private static synchronized void buildUpdateProviders() {
        if (!isBuilt) {
            final Collection<? extends UpdateProvider> updateProviders = Lookup.getDefault().lookupAll(UpdateProvider.class);
            updateProviders.forEach(provider -> {
                final UpdateItem item = provider.getVersionedItem();
                if (!UPDATE_PROVIDER_REGISTRY.containsKey(item)) {
                    UPDATE_PROVIDER_REGISTRY.put(item, new HashMap<>());
                }
                UPDATE_PROVIDER_REGISTRY.get(item).put(provider.getFromVersionNumber(), provider);
            });

            updateProviders.forEach(provider -> {
                final String itemName = provider.getVersionedItem().getName();
                final int itemVersion = provider.getToVersionNumber();
                if (!LATEST_VERSIONS.containsKey(itemName) || LATEST_VERSIONS.get(itemName) < itemVersion) {
                    LATEST_VERSIONS.put(itemName, itemVersion);
                }
            });
            validateUpdateProviders();
            isBuilt = true;
        }
    }

    /**
     * Ensure that every provider for a given item has a to version strictly greater than its from version, and that there is another provider whose
     * from version matches its to version, or its to version is the highest of all providers.
     */
    private static void validateUpdateProviders() {
        UPDATE_PROVIDER_REGISTRY.values().forEach(itemProviders -> itemProviders.forEach((fromVersion, provider) -> {
            final String itemName = provider.getVersionedItem().getName();
            final int toVersion = provider.getToVersionNumber();
            if (fromVersion >= toVersion) {
                throw new UpdateProviderException(String.format("Found update provider %s with to-version %d not strictly greater than from-version %d.", itemName, toVersion, fromVersion));
            } else if (!itemProviders.containsKey(toVersion) && LATEST_VERSIONS.get(itemName) != toVersion) {
                throw new UpdateProviderException(String.format("Found update provider %s with to-version %d that is not the latest (%d), but no update provider with the corresponding from version.", itemName, toVersion, LATEST_VERSIONS.get(itemName)));
            }
        }));
    }

    private static class UpdateProviderException extends RuntimeException {

        public UpdateProviderException(final String message) {
            super(message);
        }
    }
}
