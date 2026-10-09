/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.tools;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A searchable index of the registered plugins.
 *
 * @author constellation
 */
public final class PluginCatalog {

    private static final Logger LOGGER = Logger.getLogger(PluginCatalog.class.getName());
    private static volatile List<Entry> entries;

    private PluginCatalog() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * What we know about a plugin.
     *
     * @param className the plugin class name.
     * @param name the display name.
     * @param alias the registry alias.
     * @param description the description, may be empty.
     * @param tags the plugin's tags.
     * @param dataAccessType the Data Access View category, or null if the
     * plugin isn't a Data Access View plugin.
     */
    public record Entry(String className, String name, String alias, String description, List<String> tags, String dataAccessType) {

        public boolean isLowLevel() {
            return tags.contains(PluginTags.LOW_LEVEL);
        }

        public boolean isDataAccess() {
            return dataAccessType != null;
        }
    }

    /**
     * @return every registered plugin, built once and cached.
     */
    public static List<Entry> entries() {
        List<Entry> result = entries;
        if (result == null) {
            synchronized (PluginCatalog.class) {
                result = entries;
                if (result == null) {
                    result = build();
                    entries = result;
                }
            }
        }
        return result;
    }

    private static List<Entry> build() {
        final List<Entry> result = new ArrayList<>();
        for (final String className : PluginRegistry.getPluginClassNames()) {
            try {
                final Plugin plugin = PluginRegistry.get(className);
                result.add(entry(className, plugin));
            } catch (final RuntimeException | LinkageError ex) {
                LOGGER.log(Level.FINE, "Couldn't index plugin {0}: {1}", new Object[]{className, ex.getMessage()});
            }
        }
        result.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(result);
    }

    static Entry entry(final String className, final Plugin plugin) {
        final String description = plugin.getDescription() == null ? "" : plugin.getDescription().replaceAll("\\s+", " ").trim();
        final String[] tags = plugin.getTags() == null ? new String[0] : plugin.getTags();
        final String davType = plugin instanceof DataAccessPlugin dap ? dap.getType() : null;
        return new Entry(className, plugin.getName() == null ? className : plugin.getName(), PluginRegistry.getAlias(className),
                description, Arrays.asList(tags), davType);
    }

    /**
     * Search the plugins by keywords.
     *
     * @param query space separated keywords; empty matches everything.
     * @param includeLowLevel include low level (internal) plugins.
     * @param limit the maximum number of results.
     * @return the best matches, best first.
     */
    public static List<Entry> search(final String query, final boolean includeLowLevel, final int limit) {
        final String[] words = query == null ? new String[0] : query.toLowerCase(Locale.ROOT).trim().split("[\\s,]+");
        record Scored(Entry entry, int score) {

        }
        final List<Scored> scored = new ArrayList<>();
        for (final Entry entry : entries()) {
            if (!includeLowLevel && entry.isLowLevel()) {
                continue;
            }
            int score = 0;
            final String name = entry.name().toLowerCase(Locale.ROOT);
            final String alias = entry.alias().toLowerCase(Locale.ROOT);
            final String description = entry.description().toLowerCase(Locale.ROOT);
            final String tags = String.join(" ", entry.tags()).toLowerCase(Locale.ROOT)
                    + (entry.dataAccessType() == null ? "" : " " + entry.dataAccessType().toLowerCase(Locale.ROOT));
            for (final String word : words) {
                if (word.isBlank()) {
                    continue;
                }
                if (name.contains(word)) {
                    score += 5;
                }
                if (alias.contains(word.replace(" ", ""))) {
                    score += 3;
                }
                if (tags.contains(word)) {
                    score += 2;
                }
                if (description.contains(word)) {
                    score += 1;
                }
            }
            if (score > 0 || words.length == 0 || words.length == 1 && words[0].isBlank()) {
                scored.add(new Scored(entry, score));
            }
        }
        scored.sort(Comparator.comparingInt(Scored::score).reversed().thenComparing(s -> s.entry().name(), String.CASE_INSENSITIVE_ORDER));
        return scored.stream().limit(limit).map(Scored::entry).toList();
    }

    /**
     * Find a plugin by class name, alias or display name.
     *
     * @param name the name.
     * @return a new instance of the plugin.
     * @throws IllegalArgumentException if there's no such plugin.
     */
    public static Plugin resolve(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A plugin name is required");
        }
        final String trimmed = name.trim();
        try {
            return PluginRegistry.get(trimmed);
        } catch (final IllegalArgumentException ex) {
            // try the display name next
        }
        final String compact = trimmed.replaceAll("[\\s_-]", "");
        for (final Entry entry : entries()) {
            if (entry.name().equalsIgnoreCase(trimmed) || entry.name().replaceAll("[\\s_-]", "").equalsIgnoreCase(compact)
                    || entry.alias().equalsIgnoreCase(compact)) {
                return PluginRegistry.get(entry.className());
            }
        }
        final List<String> suggestions = suggest(trimmed, 3);
        throw new IllegalArgumentException("No plugin named '" + name + "'."
                + (suggestions.isEmpty() ? "" : " Did you mean: " + String.join(", ", suggestions) + "?")
                + " Use search_plugins to find the right name.");
    }

    /**
     * The plugins whose names are closest to a (possibly misspelt) name.
     *
     * @param name the name.
     * @param limit the maximum number of suggestions.
     * @return suggestions, as "alias (display name)".
     */
    public static List<String> suggest(final String name, final int limit) {
        final String compact = name.replaceAll("[\\s_.-]", "").toUpperCase(Locale.ROOT);
        record Scored(Entry entry, int distance) {

        }
        return entries().stream()
                .filter(entry -> !entry.isLowLevel())
                .map(entry -> new Scored(entry, Math.min(distance(compact, entry.alias()),
                        distance(compact, entry.name().replaceAll("[\\s_.-]", "").toUpperCase(Locale.ROOT)))))
                .filter(scored -> scored.distance() <= Math.max(3, compact.length() / 3))
                .sorted(Comparator.comparingInt(Scored::distance))
                .limit(limit)
                .map(scored -> scored.entry().alias() + " (" + scored.entry().name() + ")")
                .toList();
    }

    /**
     * Levenshtein distance.
     */
    static int distance(final String a, final String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                final int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            final int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[b.length()];
    }

    /**
     * @param plugin a plugin.
     * @return the catalog entry for the plugin.
     */
    public static Entry describe(final Plugin plugin) {
        return entry(plugin.getClass().getName(), plugin);
    }
}
