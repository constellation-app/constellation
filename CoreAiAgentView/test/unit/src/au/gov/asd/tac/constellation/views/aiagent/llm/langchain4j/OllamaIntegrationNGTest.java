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
package au.gov.asd.tac.constellation.views.aiagent.llm.langchain4j;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentConfig;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentEnvironment;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentEvent;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentSession;
import au.gov.asd.tac.constellation.views.aiagent.agent.FinishReason;
import au.gov.asd.tac.constellation.views.aiagent.agent.ToolRegistry;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.aiagent.llm.ApiMode;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointKind;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.SkipException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration test against a local Ollama server. Skipped unless the
 * environment variable {@code OLLAMA_TEST_MODEL} names a model that supports
 * tool calling (e.g. {@code gemma4:e4b}); {@code OLLAMA_TEST_URL} overrides
 * the default {@code http://localhost:11434/v1}.
 */
public class OllamaIntegrationNGTest {

    private String model;
    private EndpointConfig endpoint;

    @BeforeClass
    public void setUpClass() {
        model = System.getenv("OLLAMA_TEST_MODEL");
        if (model == null || model.isBlank()) {
            throw new SkipException("Set OLLAMA_TEST_MODEL to run the Ollama integration test");
        }
        final String url = System.getenv().getOrDefault("OLLAMA_TEST_URL", "http://localhost:11434/v1");
        endpoint = new EndpointConfig("ollama-test", "Ollama", EndpointKind.OPENAI_COMPATIBLE, url, ApiMode.CHAT_COMPLETIONS, model,
                Map.of(), false, false);
    }

    @Test
    public void testListModels() throws Exception {
        final List<ModelInfo> models = new LangChain4jProvider(endpoint, null).listModels();
        assertTrue(models.stream().anyMatch(m -> m.id().equals(model)), models.toString());
    }

    @Test
    public void testToolCallingRoundTrip() throws Exception {
        final AtomicInteger calls = new AtomicInteger();
        final AgentTool tool = new AgentTool() {
            @Override
            public String name() {
                return "get_secret_number";
            }

            @Override
            public String description() {
                return "Returns the secret number. Always call this to find out the secret number.";
            }

            @Override
            public JsonNode inputSchema() {
                final ObjectNode schema = JsonNodeFactory.instance.objectNode();
                schema.put("type", "object");
                schema.putObject("properties");
                return schema;
            }

            @Override
            public ToolRisk risk() {
                return ToolRisk.READ_ONLY;
            }

            @Override
            public ToolResult execute(final JsonNode args, final ToolContext context) {
                calls.incrementAndGet();
                return ToolResult.ok("The secret number is 4817.");
            }
        };
        final List<AgentEvent> events = new CopyOnWriteArrayList<>();
        final CountDownLatch done = new CountDownLatch(1);
        final AgentSession session = new AgentSession(new LangChain4jProvider(endpoint, null),
                new AgentConfig(model, "You are a helpful assistant. Use tools when they are relevant.", 5, 4000, 0.0, "none", Map.of(), 0),
                new ToolRegistry(List.of(tool)),
                new AgentEnvironment() {
            @Override
            public Graph getActiveGraph() {
                return null;
            }

            @Override
            public String describeContext() {
                return null;
            }

            @Override
            public String graphName(final Graph graph) {
                return null;
            }
        }, event -> {
            events.add(event);
            if (event instanceof AgentEvent.TurnFinished) {
                done.countDown();
            }
        });
        try {
            session.send("What is the secret number? Reply with just the number.");
            assertTrue(done.await(5, TimeUnit.MINUTES), "no response from Ollama");
            final AgentEvent.TurnFinished finished = (AgentEvent.TurnFinished) events.get(events.size() - 1);
            assertEquals(finished.reason(), FinishReason.STOP, events.toString());
            assertTrue(calls.get() >= 1, "the model didn't call the tool");
            final String answer = events.stream().filter(AgentEvent.AssistantMessageFinished.class::isInstance)
                    .map(e -> ((AgentEvent.AssistantMessageFinished) e).text()).reduce("", String::concat);
            assertTrue(answer.contains("4817"), answer);
            assertTrue(events.stream().anyMatch(AgentEvent.TextDelta.class::isInstance), "nothing was streamed");
        } finally {
            session.shutdown();
        }
    }
}
