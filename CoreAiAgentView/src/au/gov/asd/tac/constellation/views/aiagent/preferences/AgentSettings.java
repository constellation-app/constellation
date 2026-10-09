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

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * A snapshot of the AI agent's settings.
 *
 * @param endpoints the configured endpoints.
 * @param defaultEndpointId the id of the endpoint to select by default.
 * @param maxSteps the maximum number of model calls per user message.
 * @param maxToolResultChars the maximum length of a tool result sent to the
 * model.
 * @param requireLocalOnly refuse endpoints that aren't local or private.
 * @param includeSelection include selection counts in the context header.
 * @param systemPromptOverride replacement system prompt, empty for the default.
 * @param toolPolicies per-tool policy overrides.
 * @param temperature the default temperature, or null for the model default.
 * @param reasoningEffort the default reasoning effort, empty for the model
 * default.
 * @param debugLogging log redacted request summaries.
 *
 * @author constellation
 */
public record AgentSettings(List<EndpointConfig> endpoints, String defaultEndpointId, int maxSteps, int maxToolResultChars,
        boolean requireLocalOnly, boolean includeSelection, String systemPromptOverride, Map<String, ToolPolicy> toolPolicies,
        Double temperature, String reasoningEffort, boolean debugLogging) {

    private static final Logger LOGGER = Logger.getLogger(AgentSettings.class.getName());

    public AgentSettings {
        endpoints = endpoints == null ? List.of() : List.copyOf(endpoints);
        defaultEndpointId = defaultEndpointId == null ? "" : defaultEndpointId;
        systemPromptOverride = systemPromptOverride == null ? "" : systemPromptOverride;
        toolPolicies = toolPolicies == null ? Map.of() : Map.copyOf(toolPolicies);
        reasoningEffort = reasoningEffort == null ? "" : reasoningEffort;
    }

    /**
     * @return the preferences node used by the AI agent.
     */
    public static Preferences preferences() {
        return NbPreferences.forModule(AgentPreferenceKeys.class);
    }

    /**
     * Load the settings from the module preferences.
     *
     * @return the settings.
     */
    public static AgentSettings load() {
        return load(preferences());
    }

    /**
     * Load the settings from a preferences node.
     *
     * @param prefs the preferences.
     * @return the settings.
     */
    public static AgentSettings load(final Preferences prefs) {
        final ObjectMapper mapper = JsonFactoryUtilities.getObjectMapper();
        List<EndpointConfig> endpoints = new ArrayList<>();
        final String endpointsJson = prefs.get(AgentPreferenceKeys.ENDPOINTS, "");
        if (!endpointsJson.isBlank()) {
            try {
                endpoints = mapper.readValue(endpointsJson, new TypeReference<List<EndpointConfig>>() {
                });
            } catch (final JsonProcessingException ex) {
                LOGGER.log(Level.WARNING, "Couldn't read the AI assistant endpoints; using defaults", ex);
            }
        }
        if (endpoints.isEmpty()) {
            endpoints = List.of(EndpointConfig.ollamaDefault(), EndpointConfig.openAiDefault());
        }

        final Map<String, ToolPolicy> policies = new LinkedHashMap<>();
        final String policiesJson = prefs.get(AgentPreferenceKeys.TOOL_POLICIES, "");
        if (!policiesJson.isBlank()) {
            try {
                final Map<String, String> raw = mapper.readValue(policiesJson, new TypeReference<Map<String, String>>() {
                });
                raw.forEach((tool, policy) -> policies.put(tool, ToolPolicy.parse(policy, ToolPolicy.ASK)));
            } catch (final JsonProcessingException ex) {
                LOGGER.log(Level.WARNING, "Couldn't read the AI assistant tool policies", ex);
            }
        }

        final String temperatureText = prefs.get(AgentPreferenceKeys.TEMPERATURE, "");
        Double temperature = null;
        if (!temperatureText.isBlank()) {
            try {
                temperature = Double.valueOf(temperatureText);
            } catch (final NumberFormatException ex) {
                LOGGER.log(Level.WARNING, "Ignoring invalid temperature {0}", temperatureText);
            }
        }

        final boolean localOnly = Boolean.getBoolean(AgentPreferenceKeys.REQUIRE_LOCAL_ONLY_SYSTEM_PROPERTY)
                || prefs.getBoolean(AgentPreferenceKeys.REQUIRE_LOCAL_ONLY, AgentPreferenceKeys.REQUIRE_LOCAL_ONLY_DEFAULT);

        return new AgentSettings(endpoints,
                prefs.get(AgentPreferenceKeys.DEFAULT_ENDPOINT, endpoints.get(0).id()),
                Math.max(1, prefs.getInt(AgentPreferenceKeys.MAX_STEPS, AgentPreferenceKeys.MAX_STEPS_DEFAULT)),
                Math.max(1000, prefs.getInt(AgentPreferenceKeys.MAX_TOOL_RESULT_CHARS, AgentPreferenceKeys.MAX_TOOL_RESULT_CHARS_DEFAULT)),
                localOnly,
                prefs.getBoolean(AgentPreferenceKeys.INCLUDE_SELECTION, AgentPreferenceKeys.INCLUDE_SELECTION_DEFAULT),
                prefs.get(AgentPreferenceKeys.SYSTEM_PROMPT_OVERRIDE, ""),
                policies,
                temperature,
                prefs.get(AgentPreferenceKeys.REASONING_EFFORT, ""),
                prefs.getBoolean(AgentPreferenceKeys.DEBUG_LOGGING, AgentPreferenceKeys.DEBUG_LOGGING_DEFAULT));
    }

    /**
     * Save the settings to the module preferences.
     */
    public void save() {
        save(preferences());
    }

    /**
     * Save the settings to a preferences node.
     *
     * @param prefs the preferences.
     */
    public void save(final Preferences prefs) {
        final ObjectMapper mapper = JsonFactoryUtilities.getObjectMapper();
        try {
            prefs.put(AgentPreferenceKeys.ENDPOINTS, mapper.writeValueAsString(endpoints));
            final Map<String, String> raw = new LinkedHashMap<>();
            toolPolicies.forEach((tool, policy) -> raw.put(tool, policy.name()));
            prefs.put(AgentPreferenceKeys.TOOL_POLICIES, mapper.writeValueAsString(raw));
        } catch (final JsonProcessingException ex) {
            LOGGER.log(Level.WARNING, "Couldn't save the AI assistant settings", ex);
        }
        prefs.put(AgentPreferenceKeys.DEFAULT_ENDPOINT, defaultEndpointId);
        prefs.putInt(AgentPreferenceKeys.MAX_STEPS, maxSteps);
        prefs.putInt(AgentPreferenceKeys.MAX_TOOL_RESULT_CHARS, maxToolResultChars);
        prefs.putBoolean(AgentPreferenceKeys.REQUIRE_LOCAL_ONLY, requireLocalOnly);
        prefs.putBoolean(AgentPreferenceKeys.INCLUDE_SELECTION, includeSelection);
        prefs.put(AgentPreferenceKeys.SYSTEM_PROMPT_OVERRIDE, systemPromptOverride);
        prefs.put(AgentPreferenceKeys.TEMPERATURE, temperature == null ? "" : temperature.toString());
        prefs.put(AgentPreferenceKeys.REASONING_EFFORT, reasoningEffort);
        prefs.putBoolean(AgentPreferenceKeys.DEBUG_LOGGING, debugLogging);
    }

    /**
     * @return the default endpoint, or the first endpoint if the default id is
     * unknown.
     */
    public EndpointConfig defaultEndpoint() {
        return endpoints.stream().filter(e -> e.id().equals(defaultEndpointId)).findFirst().orElse(endpoints.isEmpty() ? null : endpoints.get(0));
    }

    /**
     * Whether the user has acknowledged that data may be sent to an endpoint.
     *
     * @param endpoint the endpoint.
     * @return true if acknowledged.
     */
    public static boolean isAcknowledged(final EndpointConfig endpoint) {
        return preferences().getBoolean(AgentPreferenceKeys.ACKNOWLEDGED_PREFIX + endpoint.id() + "." + endpoint.host(), false);
    }

    /**
     * Record that the user has acknowledged that data may be sent to an
     * endpoint.
     *
     * @param endpoint the endpoint.
     */
    public static void acknowledge(final EndpointConfig endpoint) {
        preferences().putBoolean(AgentPreferenceKeys.ACKNOWLEDGED_PREFIX + endpoint.id() + "." + endpoint.host(), true);
    }
}
