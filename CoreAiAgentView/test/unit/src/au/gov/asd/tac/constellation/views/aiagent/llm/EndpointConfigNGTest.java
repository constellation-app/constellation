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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link EndpointConfig}.
 */
public class EndpointConfigNGTest {

    @Test
    public void testDefaults() {
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
