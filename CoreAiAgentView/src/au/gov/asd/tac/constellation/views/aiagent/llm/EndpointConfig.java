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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.UUID;

/**
 * The configuration of one LLM endpoint. API keys are not part of the
 * configuration; they are stored in the keyring under {@link #apiKeyRef()}.
 * <p>
 * There are no built-in endpoints: the user adds one before the assistant can
 * be used. Connections always use the current Constellation security context
 * (client certificate) when there is one, so no per-endpoint setting is
 * needed for mTLS.
 *
 * @param id a unique, stable id.
 * @param name a display name, e.g. "LiteLLM".
 * @param kind the kind of endpoint.
 * @param baseUrl the base URL, e.g. {@code http://localhost:4000/v1}.
 * @param defaultModel the model to select by default, may be empty.
 * @param extraHeaders extra HTTP headers, e.g. for gateways.
 *
 * @author doubtsec
 */
// ignore settings saved by earlier versions, e.g. apiMode and requireSecurityContext
@JsonIgnoreProperties(ignoreUnknown = true)
public record EndpointConfig(String id, String name, EndpointKind kind, String baseUrl, String defaultModel,
        Map<String, String> extraHeaders) {

    public EndpointConfig {
        id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        name = name == null ? "" : name;
        kind = kind == null ? EndpointKind.OPENAI_COMPATIBLE : kind;
        baseUrl = baseUrl == null ? "" : baseUrl.trim();
        defaultModel = defaultModel == null ? "" : defaultModel;
        extraHeaders = extraHeaders == null ? Map.of() : Map.copyOf(extraHeaders);
    }

    /**
     * @return the keyring key that holds this endpoint's API key.
     */
    public String apiKeyRef() {
        return "constellation.aiagent." + id + ".apiKey";
    }

    public EndpointConfig withDefaultModel(final String model) {
        return new EndpointConfig(id, name, kind, baseUrl, model, extraHeaders);
    }

    @Override
    public String toString() {
        return name.isBlank() ? baseUrl : name;
    }
}
