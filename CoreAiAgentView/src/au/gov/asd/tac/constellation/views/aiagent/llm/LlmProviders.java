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

import au.gov.asd.tac.constellation.views.aiagent.llm.langchain4j.LangChain4jProvider;

/**
 * Creates {@link LlmProvider}s for endpoints.
 *
 * @author doubtsec
 */
public final class LlmProviders {

    private LlmProviders() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create a provider for an endpoint. Providers build their HTTP clients
     * lazily, so creating one doesn't connect to anything.
     *
     * @param endpoint the endpoint.
     * @param apiKey the API key, may be null for endpoints that don't need one.
     * @return the provider.
     */
    public static LlmProvider create(final EndpointConfig endpoint, final String apiKey) {
        return new LangChain4jProvider(endpoint, apiKey);
    }
}
