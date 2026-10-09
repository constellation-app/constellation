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
package au.gov.asd.tac.constellation.views.aiagent.llm;

import java.util.Map;
import java.util.UUID;

/**
 * The configuration of one LLM endpoint. API keys are not part of the
 * configuration; they are stored in the keyring under {@link #apiKeyRef()}.
 *
 * @param id a unique, stable id.
 * @param name a display name, e.g. "Ollama".
 * @param kind the kind of endpoint.
 * @param baseUrl the base URL, e.g. {@code http://localhost:11434/v1}.
 * @param apiMode the OpenAI API dialect to use.
 * @param defaultModel the model to select by default, may be empty.
 * @param extraHeaders extra HTTP headers, e.g. for gateways.
 * @param requireSecurityContext refuse to connect without a Constellation
 * security context (client certificate).
 * @param forceHttp11 use HTTP/1.1, for gateways that request client
 * certificates through TLS renegotiation.
 *
 * @author doubtsec
 */
public record EndpointConfig(String id, String name, EndpointKind kind, String baseUrl, ApiMode apiMode,
        String defaultModel, Map<String, String> extraHeaders, boolean requireSecurityContext, boolean forceHttp11) {

    public EndpointConfig {
        id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        name = name == null ? "" : name;
        kind = kind == null ? EndpointKind.OPENAI_COMPATIBLE : kind;
        baseUrl = baseUrl == null ? "" : baseUrl.trim();
        apiMode = apiMode == null ? ApiMode.CHAT_COMPLETIONS : apiMode;
        defaultModel = defaultModel == null ? "" : defaultModel;
        extraHeaders = extraHeaders == null ? Map.of() : Map.copyOf(extraHeaders);
    }

    /**
     * An Ollama endpoint on this machine.
     *
     * @return the configuration.
     */
    public static EndpointConfig ollamaDefault() {
        return new EndpointConfig("ollama-local", "Ollama", EndpointKind.OPENAI_COMPATIBLE,
                "http://localhost:11434/v1", ApiMode.CHAT_COMPLETIONS, "", Map.of(), false, false);
    }

    /**
     * The OpenAI API.
     *
     * @return the configuration.
     */
    public static EndpointConfig openAiDefault() {
        return new EndpointConfig("openai", "OpenAI", EndpointKind.OPENAI,
                "https://api.openai.com/v1", ApiMode.RESPONSES, "", Map.of(), true, false);
    }

    /**
     * @return the keyring key that holds this endpoint's API key.
     */
    public String apiKeyRef() {
        return "constellation.aiagent." + id + ".apiKey";
    }

    public EndpointConfig withDefaultModel(final String model) {
        return new EndpointConfig(id, name, kind, baseUrl, apiMode, model, extraHeaders, requireSecurityContext, forceHttp11);
    }

    @Override
    public String toString() {
        return name.isBlank() ? baseUrl : name;
    }
}
