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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link EndpointConfig}.
 */
public class EndpointConfigNGTest {

    private static EndpointConfig endpoint(final String url) {
        return new EndpointConfig("id", "name", EndpointKind.OPENAI_COMPATIBLE, url, ApiMode.CHAT_COMPLETIONS, "", Map.of(), false, false);
    }

    @Test
    public void testLocal() {
        assertTrue(endpoint("http://localhost:11434/v1").isLocal());
        assertTrue(endpoint("http://localhost:11434/v1").isLoopback());
        assertTrue(endpoint("http://127.0.0.1:8000/v1").isLoopback());
        assertTrue(endpoint("http://[::1]:8000/v1").isLoopback());
        assertTrue(endpoint("http://192.168.1.20/v1").isLocal());
        assertTrue(endpoint("http://10.0.0.5/v1").isLocal());
        assertTrue(endpoint("http://172.20.1.1/v1").isLocal());
        assertTrue(endpoint("https://vllm.internal/v1").isLocal());
        assertFalse(endpoint("http://172.32.1.1/v1").isLocal());
        assertFalse(endpoint("https://api.openai.com/v1").isLocal());
        assertFalse(endpoint("not a url").isLocal());
    }

    @Test
    public void testDefaults() {
        assertFalse(EndpointConfig.defaultRequireSecurityContext("http://localhost:11434/v1"));
        assertTrue(EndpointConfig.defaultRequireSecurityContext("https://api.openai.com/v1"));
        assertFalse(EndpointConfig.ollamaDefault().requireSecurityContext());
        assertTrue(EndpointConfig.openAiDefault().requireSecurityContext());
        assertEquals(EndpointConfig.ollamaDefault().apiKeyRef(), "constellation.aiagent.ollama-local.apiKey");
    }

    @Test
    public void testNullsAreNormalised() {
        final EndpointConfig config = new EndpointConfig(null, null, null, null, null, null, null, false, false);
        assertFalse(config.id().isBlank());
        assertEquals(config.kind(), EndpointKind.OPENAI_COMPATIBLE);
        assertEquals(config.apiMode(), ApiMode.CHAT_COMPLETIONS);
        assertTrue(config.extraHeaders().isEmpty());
    }
}
