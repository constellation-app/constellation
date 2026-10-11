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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The kind of LLM endpoint.
 *
 * @author doubtsec
 */
public enum EndpointKind {
    /**
     * The Anthropic Messages API.
     */
    ANTHROPIC("Anthropic"),
    /**
     * The OpenAI Chat Completions API, as served by OpenAI, Azure OpenAI,
     * Ollama, vLLM, LM Studio, LiteLLM and others.
     */
    OPENAI_COMPATIBLE("OpenAI-compatible");

    private final String label;

    EndpointKind(final String label) {
        this.label = label;
    }

    /**
     * Read a kind from saved settings. Kinds that are no longer supported
     * (OpenAI and Azure OpenAI, which are now configured as OpenAI-compatible)
     * map to {@link #OPENAI_COMPATIBLE}.
     *
     * @param name the saved name.
     * @return the kind.
     */
    @JsonCreator
    public static EndpointKind fromJson(final String name) {
        return ANTHROPIC.name().equals(name) ? ANTHROPIC : OPENAI_COMPATIBLE;
    }

    @Override
    public String toString() {
        return label;
    }
}
