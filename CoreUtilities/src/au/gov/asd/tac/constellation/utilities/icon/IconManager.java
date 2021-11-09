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
package au.gov.asd.tac.constellation.utilities.icon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * A centralised place for the management of all icons in CONSTELLATION. This is
 * achieved by using {@link Lookup} to discover any registered
 * {@link ConstellationIconProvider} classes, and providing methods for
 * accessing these icons.
 *
 * @author cygnus_x-1
 */
public class IconManager {

    private static List<? extends ConstellationIconProvider> PROVIDERS = null;
    private static CustomIconProvider CUSTOM_PROVIDER = null;
    private static Map<String, ConstellationIcon> CACHE = null;

    /**
     * Find all {@link ConstellationIconProvider} instances using
     * {@link Lookup}, and return them ordered by priority (ie. lowest
     * {@link ServiceProvider} 'position' value).
     *
     * @return A {@link Collection} of {@link ConstellationIconProvider}.
     */
    public static synchronized List<? extends ConstellationIconProvider> getIconProviders() {
        if (PROVIDERS == null) {
            List<? extends ConstellationIconProvider> providers = new ArrayList<>(Lookup.getDefault().lookupAll(ConstellationIconProvider.class));
            PROVIDERS = Collections.unmodifiableList(providers);
        }
        return PROVIDERS;
    }

    /**
     * Find the single highest priority (ie. lowest {@link ServiceProvider}
     * 'position' value) {@link CustomIconProvider}.
     *
     * @return A {@link CustomIconProvider}.
     */
    public static synchronized CustomIconProvider getCustomProvider() {
        if (CUSTOM_PROVIDER == null) {
            CUSTOM_PROVIDER = Lookup.getDefault().lookup(CustomIconProvider.class);
        }
        return CUSTOM_PROVIDER;
    }

    /**
     * Get the {@link Set} of {@link String} objects representing the names of
     * every icon managed by this IconManager.
     *
     * @param editable If true the result set will be filtered to only editable
     * icons, if false the results set will be filtered to only built-in icons,
     * if null all icons will be returned.
     * @return A {@link Set} of {@link String} objects representing icon names.
     */
    public static Set<String> getIconNames(final Boolean editable) {
        return getCache().values().stream().filter(icon -> {
            return editable == null || editable == icon.isEditable();
        }).map(icon -> {
            return icon.getExtendedName();
        }).collect(Collectors.toSet());
    }

    /**
     * Check if the given icon name matches an icon held by any of the
     * {@link ConstellationIconProvider} objects managed by this IconManager.
     *
     * @param name A {@link String} representing the name of the icon to look
     * for.
     * @return True if the icon was found, false otherwise.
     */
    public static boolean iconExists(final String name) {
        return getCache().containsKey(name);
    }

    /**
     * Get an icon with the given name from any of the
     * {@link ConstellationIconProvider} objects managed by this IconManager.
     *
     * @param name A {@link String} representing the name of the icon to look
     * for.
     * @return A {@link ConstellationIcon} if the icon was found, null
     * otherwise.
     */
    public static ConstellationIcon getIcon(final String name) {
        final ConstellationIcon icon = getCache().get(name);
        return icon == null ? createMissingIcon(name) : icon;
    }

    /**
     * Get the {@link Set} of {@link ConstellationIcon} objects representing
     * every icon managed by this IconManager.
     *
     * @return A {@link Set} of {@link ConstellationIcon} objects representing
     * icons.
     */
    public static Set<ConstellationIcon> getIcons() {
        return new HashSet<>(getCache().values());
    }

    /**
     * Use the chosen {@link CustomIconProvider} to add a user-defined icon.
     *
     * Note that adding an icon calls on the icon cache, so the rebuild cache
     * flag must be set after this has occurred.
     *
     * @param icon A {@link ConstellationIcon} representing a user-defined icon.
     * @return True if the icon was successfully added, false otherwise.
     */
    public static boolean addIcon(ConstellationIcon icon) {
        final boolean iconAdded = IconManager.getCustomProvider().addIcon(icon);
        rebuildCache();
        return iconAdded;
    }

    /**
     * Use the chosen {@link CustomIconProvider} to remove a user-defined icon.
     *
     * Note that removing an icon calls on the icon cache, so the rebuild cache
     * flag must be set after this has occurred.
     *
     * @param iconName A {@link String} representing the name of a previously
     * added user-defined icon.
     * @return True if the icon was successfully removed, false otherwise.
     */
    public static boolean removeIcon(String iconName) {
        final boolean iconRemoved = IconManager.getCustomProvider().removeIcon(iconName);
        rebuildCache();
        return iconRemoved;
    }

    /**
     * Get the icon cache.
     *
     * @return a {@link Map} of icon name to icon representing all icons from
     * all loaded icon providers.
     */
    protected static synchronized Map<String, ConstellationIcon> getCache() {
        if (CACHE == null) {
            final Map<String, ConstellationIcon> iconNames = new HashMap<>();
            final Map<String, ConstellationIcon> iconExtendedNames = new HashMap<>();
            final Map<String, ConstellationIcon> iconAliases = new HashMap<>();

            for (final ConstellationIconProvider iconProvider : getIconProviders()) {
                for (final ConstellationIcon icon : iconProvider.getIcons()) {
                    iconNames.put(icon.getName(), icon);
                    iconExtendedNames.put(icon.getExtendedName(), icon);
                    for (final String alias : icon.getAliases()) {
                        iconAliases.put(alias, icon);
                    }
                }
            }

            CACHE = new HashMap<>();
            CACHE.putAll(iconAliases);
            CACHE.putAll(iconNames);
            CACHE.putAll(iconExtendedNames);
        }

        return CACHE;
    }

    /**
     * Flag that the icon cache needs to be rebuilt.
     */
    private static synchronized void rebuildCache() {
        CACHE = null;
    }

    /**
     * Build an icon to represent a missing icon.
     *
     * @param name a {@link String} representing the name of a missing icon.
     * @return an icon.
     */
    private static ConstellationIcon createMissingIcon(final String name) {
        final ConstellationIcon missingIcon = new ConstellationIcon.Builder(name, DefaultIconProvider.UNKNOWN.getIconData()).build();
        CACHE.put(name, missingIcon);
        return missingIcon;
    }
}
