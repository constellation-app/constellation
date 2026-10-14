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
package au.gov.asd.tac.constellation.views.aiagent.preferences;

import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointKind;
import java.util.prefs.Preferences;

/**
 * Stores endpoint API keys in the Assistant's preferences, with its other
 * settings, so they are kept between sessions. If no key is saved for an
 * Anthropic endpoint, the {@code ANTHROPIC_API_KEY} environment variable is
 * used.
 *
 * @author doubtsec
 */
public final class ApiKeyStore {

    private ApiKeyStore() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the API key for an endpoint.
     *
     * @param endpoint the endpoint.
     * @return the key, or null if none is available.
     */
    public static String read(final EndpointConfig endpoint) {
        return read(AgentSettings.preferences(), endpoint);
    }

    /**
     * Get the API key for an endpoint from a preferences node.
     *
     * @param prefs the preferences.
     * @param endpoint the endpoint.
     * @return the key, or null if none is available.
     */
    static String read(final Preferences prefs, final EndpointConfig endpoint) {
        final String key = prefs.get(endpoint.apiKeyRef(), "");
        return key.isBlank() ? environmentKey(endpoint.kind()) : key;
    }

    /**
     * Save an API key.
     *
     * @param endpoint the endpoint.
     * @param key the key; null or empty deletes the key.
     */
    public static void save(final EndpointConfig endpoint, final String key) {
        save(AgentSettings.preferences(), endpoint, key);
    }

    /**
     * Save an API key in a preferences node.
     *
     * @param prefs the preferences.
     * @param endpoint the endpoint.
     * @param key the key; null or empty deletes the key.
     */
    static void save(final Preferences prefs, final EndpointConfig endpoint, final String key) {
        if (key == null || key.isBlank()) {
            delete(prefs, endpoint);
        } else {
            prefs.put(endpoint.apiKeyRef(), key.trim());
        }
    }

    /**
     * Delete an endpoint's API key.
     *
     * @param endpoint the endpoint.
     */
    public static void delete(final EndpointConfig endpoint) {
        delete(AgentSettings.preferences(), endpoint);
    }

    /**
     * Delete an endpoint's API key from a preferences node.
     *
     * @param prefs the preferences.
     * @param endpoint the endpoint.
     */
    static void delete(final Preferences prefs, final EndpointConfig endpoint) {
        prefs.remove(endpoint.apiKeyRef());
    }

    /**
     * @param kind the endpoint kind.
     * @return the API key from the environment for the kind, or null.
     */
    static String environmentKey(final EndpointKind kind) {
        // an OpenAI-compatible endpoint could be any server, so it never gets a key from the environment
        if (kind != EndpointKind.ANTHROPIC) {
            return null;
        }
        final String value = System.getenv("ANTHROPIC_API_KEY");
        return value == null || value.isBlank() ? null : value;
    }
}
