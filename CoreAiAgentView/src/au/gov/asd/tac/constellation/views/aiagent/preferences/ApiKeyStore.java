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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.keyring.Keyring;

/**
 * Stores endpoint API keys. Keys are looked up in this order: keys entered
 * this session, the NetBeans keyring (macOS Keychain, Windows DPAPI or
 * libsecret), then, for Anthropic endpoints, the {@code ANTHROPIC_API_KEY}
 * environment variable. Keys are never written to preferences or logs.
 *
 * @author doubtsec
 */
public final class ApiKeyStore {

    private static final Logger LOGGER = Logger.getLogger(ApiKeyStore.class.getName());
    private static final Map<String, String> SESSION_KEYS = new ConcurrentHashMap<>();

    private ApiKeyStore() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the API key for an endpoint. May block while the keyring is unlocked,
     * so don't call this on the Swing or JavaFX thread.
     *
     * @param endpoint the endpoint.
     * @return the key, or null if none is available.
     */
    public static String read(final EndpointConfig endpoint) {
        final String sessionKey = SESSION_KEYS.get(endpoint.apiKeyRef());
        if (sessionKey != null) {
            return sessionKey;
        }
        try {
            final char[] key = Keyring.read(endpoint.apiKeyRef());
            if (key != null && key.length > 0) {
                return new String(key);
            }
        } catch (final RuntimeException | LinkageError ex) {
            LOGGER.log(Level.WARNING, "Couldn't read the API key from the keyring: {0}", ex.getMessage());
        }
        return environmentKey(endpoint.kind());
    }

    /**
     * Save an API key in the keyring, and for this session.
     *
     * @param endpoint the endpoint.
     * @param key the key; null or empty deletes the key.
     * @return true if the key was saved in the keyring, false if it is only
     * held for this session.
     */
    public static boolean save(final EndpointConfig endpoint, final String key) {
        if (key == null || key.isBlank()) {
            delete(endpoint);
            return true;
        }
        SESSION_KEYS.put(endpoint.apiKeyRef(), key);
        try {
            Keyring.save(endpoint.apiKeyRef(), key.toCharArray(), "Constellation Assistant API key for " + endpoint.name());
            return true;
        } catch (final RuntimeException | LinkageError ex) {
            LOGGER.log(Level.WARNING, "Couldn't save the API key in the keyring; it will be kept for this session only: {0}", ex.getMessage());
            return false;
        }
    }

    /**
     * Delete an endpoint's API key.
     *
     * @param endpoint the endpoint.
     */
    public static void delete(final EndpointConfig endpoint) {
        SESSION_KEYS.remove(endpoint.apiKeyRef());
        try {
            Keyring.delete(endpoint.apiKeyRef());
        } catch (final RuntimeException | LinkageError ex) {
            LOGGER.log(Level.WARNING, "Couldn't delete the API key from the keyring: {0}", ex.getMessage());
        }
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
