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
package au.gov.asd.tac.constellation.views.aiagent.agent;

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import com.fasterxml.jackson.databind.JsonNode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link Redactor}.
 */
public class RedactorNGTest {

    @Test
    public void testSecretNames() {
        assertTrue(Redactor.isSecretName("password"));
        assertTrue(Redactor.isSecretName("MyPlugin.api_key"));
        assertTrue(Redactor.isSecretName("accessToken"));
        assertFalse(Redactor.isSecretName("label"));
    }

    @Test
    public void testRedact() throws Exception {
        final JsonNode node = JsonFactoryUtilities.getObjectMapper().readTree(
                "{\"user\":\"bob\",\"password\":\"hunter2\",\"nested\":[{\"secret\":\"s\"}]}");
        final JsonNode redacted = Redactor.redact(node);
        assertEquals(redacted.get("user").asText(), "bob");
        assertEquals(redacted.get("password").asText(), Redactor.MASK);
        assertEquals(redacted.get("nested").get(0).get("secret").asText(), Redactor.MASK);
        // the original isn't changed
        assertEquals(node.get("password").asText(), "hunter2");
    }
}
