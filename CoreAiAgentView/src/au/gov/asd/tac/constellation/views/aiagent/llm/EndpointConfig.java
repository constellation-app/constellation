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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The configuration of one LLM endpoint. API keys are not part of the
 * configuration; they are stored in the keyring under {@link #apiKeyRef()}.
 *
 * @param id a unique, stable id.
 * @param name a display name, e.g. "Ollama (local)".
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
 * @author constellation
 */
public record EndpointConfig(String id, String name, EndpointKind kind, String baseUrl, ApiMode apiMode,
        String defaultModel, Map<String, String> extraHeaders, boolean requireSecurityContext, boolean forceHttp11) {

    private static final Pattern PRIVATE_IPV4 = Pattern.compile(
            "^(10\\.\\d+|192\\.168|172\\.(1[6-9]|2\\d|3[01])|169\\.254)\\.\\d+\\.\\d+$");

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
     * A local Ollama endpoint.
     *
     * @return the configuration.
     */
    public static EndpointConfig ollamaDefault() {
        return new EndpointConfig("ollama-local", "Ollama (local)", EndpointKind.OPENAI_COMPATIBLE,
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

    /**
     * @return the host part of the base URL, or an empty string if it can't
     * be parsed.
     */
    @JsonIgnore
    public String host() {
        try {
            final String host = URI.create(baseUrl).getHost();
            return host == null ? "" : host;
        } catch (final IllegalArgumentException ex) {
            return "";
        }
    }

    /**
     * @return true if the endpoint is on this machine or a private network,
     * judged from the host name or address in the base URL without a DNS
     * lookup.
     */
    @JsonIgnore
    public boolean isLocal() {
        return isLoopbackHost(host()) || isPrivateHost(host());
    }

    /**
     * @return true if the endpoint is on this machine.
     */
    @JsonIgnore
    public boolean isLoopback() {
        return isLoopbackHost(host());
    }

    public EndpointConfig withDefaultModel(final String model) {
        return new EndpointConfig(id, name, kind, baseUrl, apiMode, model, extraHeaders, requireSecurityContext, forceHttp11);
    }

    @Override
    public String toString() {
        return name.isBlank() ? baseUrl : name;
    }

    /**
     * Whether a host name or literal address refers to this machine.
     *
     * @param host the host.
     * @return true for localhost and loopback addresses.
     */
    public static boolean isLoopbackHost(final String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        final String h = stripBrackets(host).toLowerCase(Locale.ROOT);
        return "localhost".equals(h) || h.endsWith(".localhost") || h.startsWith("127.") || "::1".equals(h)
                || "0:0:0:0:0:0:0:1".equals(h);
    }

    /**
     * Whether a host name or literal address is on a private network.
     *
     * @param host the host.
     * @return true for RFC 1918 and link-local IPv4 addresses, IPv6 unique
     * local addresses, and {@code .local} or {@code .internal} names.
     */
    public static boolean isPrivateHost(final String host) {
        if (host == null || host.isBlank()) {
            return false;
        }
        final String h = stripBrackets(host).toLowerCase(Locale.ROOT);
        return PRIVATE_IPV4.matcher(h).matches() || h.startsWith("fc") && h.contains(":") || h.startsWith("fd") && h.contains(":")
                || h.endsWith(".local") || h.endsWith(".internal") || h.endsWith(".lan");
    }

    /**
     * The default for {@link #requireSecurityContext()}: on for endpoints that
     * aren't on this machine.
     *
     * @param baseUrl the endpoint's base URL.
     * @return the default.
     */
    public static boolean defaultRequireSecurityContext(final String baseUrl) {
        try {
            return !isLoopbackHost(URI.create(baseUrl.trim()).getHost());
        } catch (final IllegalArgumentException ex) {
            return true;
        }
    }

    private static String stripBrackets(final String host) {
        return host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
    }
}
