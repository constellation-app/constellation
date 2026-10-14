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

import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointKind;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link AgentSettings}.
 */
public class AgentSettingsNGTest {

    private Preferences prefs;

    @BeforeMethod
    public void setUp() {
        prefs = Preferences.userRoot().node("constellation-aiagent-test-" + UUID.randomUUID());
    }

    @AfterMethod
    public void tearDown() throws BackingStoreException {
        prefs.removeNode();
    }

    @Test
    public void testDefaults() {
        final AgentSettings settings = AgentSettings.load(prefs);
        // there are no built-in endpoints or models: the user has to configure one
        assertTrue(settings.endpoints().isEmpty());
        assertFalse(settings.isConfigured());
        assertNull(settings.defaultEndpoint());
        assertEquals(settings.maxSteps(), AgentPreferenceKeys.MAX_STEPS_DEFAULT);
        assertNull(settings.temperature());
        assertTrue(settings.includeSelection());
        assertTrue(settings.toolPolicies().isEmpty());
    }

    @Test
    public void testRoundTrip() {
        final EndpointConfig vllm = new EndpointConfig("vllm", "vLLM", EndpointKind.OPENAI_COMPATIBLE, "http://gpu.lan:8000/v1",
                "qwen", Map.of("X-Org", "abc"));
        final AgentSettings settings = new AgentSettings(List.of(vllm), "vllm", 7, 5000, false, "custom prompt",
                Map.of("run_plugin", ToolPolicy.AUTO, "add_note", ToolPolicy.NEVER), 0.3, "low", true);
        settings.save(prefs);
        final AgentSettings loaded = AgentSettings.load(prefs);
        assertEquals(loaded, settings);
        assertEquals(loaded.defaultEndpoint(), vllm);
        assertTrue(loaded.isConfigured());
    }

    @Test
    public void testApiKeysAreSavedInPreferencesAndSurviveSettingsSave() {
        final EndpointConfig claude = new EndpointConfig("claude", "Claude", EndpointKind.ANTHROPIC,
                "https://api.anthropic.com/v1", "", Map.of());
        final EndpointConfig local = new EndpointConfig("local", "Local", EndpointKind.OPENAI_COMPATIBLE,
                "http://localhost:4000/v1", "", Map.of());
        ApiKeyStore.save(prefs, claude, " secret-key ");
        assertEquals(prefs.get(AgentPreferenceKeys.API_KEY_PREFIX + "claude", null), "secret-key");
        // saving the other settings keeps the key
        new AgentSettings(List.of(claude, local), "claude", 5, 5000, true, "", Map.of(), null, "", false).save(prefs);
        assertEquals(ApiKeyStore.read(prefs, claude), "secret-key");
        // an OpenAI-compatible endpoint never takes a key from the environment
        assertNull(ApiKeyStore.read(prefs, local));
        // a blank key deletes the saved one
        ApiKeyStore.save(prefs, claude, "");
        assertNull(prefs.get(AgentPreferenceKeys.API_KEY_PREFIX + "claude", null));
    }
}
