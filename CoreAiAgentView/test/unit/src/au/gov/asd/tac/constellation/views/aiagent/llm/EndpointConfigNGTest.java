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

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link EndpointConfig}.
 */
public class EndpointConfigNGTest {

    @Test
    public void testOnlyAnthropicAndOpenAiCompatibleAreSupported() {
        assertEquals(Arrays.asList(EndpointKind.values()), List.of(EndpointKind.ANTHROPIC, EndpointKind.OPENAI_COMPATIBLE));
    }

    @Test
    public void testApiKeyRef() {
        final EndpointConfig config = new EndpointConfig("litellm", "LiteLLM", EndpointKind.ANTHROPIC, "http://localhost:4000/v1", "", Map.of());
        assertEquals(config.apiKeyRef(), "aiagent.apiKey.litellm");
    }

    @Test
    public void testNullsAreNormalised() {
        final EndpointConfig config = new EndpointConfig(null, null, null, null, null, null);
        assertFalse(config.id().isBlank());
        assertEquals(config.kind(), EndpointKind.OPENAI_COMPATIBLE);
        assertEquals(config.defaultModel(), "");
        assertTrue(config.extraHeaders().isEmpty());
    }

    @Test
    public void testReadsSettingsFromEarlierVersions() throws Exception {
        final String json = "[{\"id\":\"openai\",\"name\":\"OpenAI\",\"kind\":\"OPENAI\",\"baseUrl\":\"https://api.openai.com/v1\","
                + "\"apiMode\":\"RESPONSES\",\"defaultModel\":\"\",\"extraHeaders\":{\"X-Org\":\"abc\"},"
                + "\"requireSecurityContext\":true,\"forceHttp11\":false},"
                + "{\"id\":\"claude\",\"name\":\"Claude\",\"kind\":\"ANTHROPIC\",\"baseUrl\":\"https://api.anthropic.com/v1\"}]";
        final EndpointConfig[] configs = JsonFactoryUtilities.getObjectMapper().readValue(json, EndpointConfig[].class);
        assertEquals(configs[0], new EndpointConfig("openai", "OpenAI", EndpointKind.OPENAI_COMPATIBLE, "https://api.openai.com/v1", "",
                Map.of("X-Org", "abc")));
        assertEquals(configs[1].kind(), EndpointKind.ANTHROPIC);
    }
}
