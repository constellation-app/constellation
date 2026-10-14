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
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointKind;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.SkipException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Integration test against a real endpoint, run once through the Anthropic
 * API and once through the OpenAI-compatible API for each model. Skipped
 * unless these environment variables are set:
 * <ul>
 * <li>{@code LLM_TEST_URL}: the base URL of a server that speaks both APIs,
 * e.g. {@code http://localhost:4000/v1} for LiteLLM;</li>
 * <li>{@code LLM_TEST_MODELS}: a comma separated list of models that support
 * tool calling;</li>
 * <li>{@code LLM_TEST_API_KEY}: the API key (optional);</li>
 * <li>{@code LLM_TEST_KINDS}: the endpoint kinds to test (optional, default
 * {@code ANTHROPIC,OPENAI_COMPATIBLE}).</li>
 * </ul>
 */
public class LlmIntegrationNGTest {

    @DataProvider(name = "endpoints")
    public Object[][] endpoints() {
        final String url = System.getenv("LLM_TEST_URL");
        final String models = System.getenv("LLM_TEST_MODELS");
        if (url == null || url.isBlank() || models == null || models.isBlank()) {
            throw new SkipException("Set LLM_TEST_URL and LLM_TEST_MODELS to run the LLM integration test");
        }
        final String kinds = System.getenv().getOrDefault("LLM_TEST_KINDS", "ANTHROPIC,OPENAI_COMPATIBLE");
        final List<Object[]> result = new ArrayList<>();
        for (final String kind : kinds.split(",")) {
            for (final String model : models.split(",")) {
                final EndpointKind endpointKind = EndpointKind.valueOf(kind.trim());
                result.add(new Object[]{new EndpointConfig("test-" + endpointKind, endpointKind.toString(), endpointKind, url, "",
                    Map.of("X-Constellation-Test", "1")), model.trim()});
            }
        }
        return result.toArray(Object[][]::new);
    }

    private static String apiKey() {
        return System.getenv("LLM_TEST_API_KEY");
    }

    @Test(dataProvider = "endpoints")
    public void testListModels(final EndpointConfig endpoint, final String model) throws Exception {
        final List<ModelInfo> models = new LangChain4jProvider(endpoint, apiKey()).listModels();
        assertTrue(models.stream().anyMatch(m -> m.id().equals(model)), endpoint.kind() + ": " + models);
    }

    @Test(dataProvider = "endpoints")
    public void testToolCallingRoundTrip(final EndpointConfig endpoint, final String model) throws Exception {
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
        final CountDownLatch[] done = {new CountDownLatch(1)};
        final AgentSession session = new AgentSession(new LangChain4jProvider(endpoint, apiKey()),
                new AgentConfig(model, "You are a helpful assistant. Use tools when they are relevant.", 5, 4000, 0.0, "", Map.of(), 0),
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
                done[0].countDown();
            }
        });
        final String label = endpoint.kind() + " " + model + ": ";
        try {
            session.send("What is the secret number? Reply with just the number.");
            assertTrue(done[0].await(5, TimeUnit.MINUTES), label + "no response");
            AgentEvent.TurnFinished finished = (AgentEvent.TurnFinished) events.get(events.size() - 1);
            assertEquals(finished.reason(), FinishReason.STOP, label + events);
            assertTrue(calls.get() >= 1, label + "the model didn't call the tool");
            assertTrue(answer(events).contains("4817"), label + answer(events));
            assertTrue(events.stream().anyMatch(AgentEvent.TextDelta.class::isInstance), label + "nothing was streamed");

            // a second turn sends the whole history back, including the tool call and its result
            events.clear();
            done[0] = new CountDownLatch(1);
            session.send("What is the secret number plus one? Reply with just the number.");
            assertTrue(done[0].await(5, TimeUnit.MINUTES), label + "no response to the second message");
            finished = (AgentEvent.TurnFinished) events.get(events.size() - 1);
            assertEquals(finished.reason(), FinishReason.STOP, label + events);
            assertTrue(answer(events).contains("4818"), label + answer(events));
        } finally {
            session.shutdown();
        }
    }

    private static String answer(final List<AgentEvent> events) {
        return String.join("", events.stream().filter(AgentEvent.AssistantMessageFinished.class::isInstance)
                .map(e -> ((AgentEvent.AssistantMessageFinished) e).text()).toList());
    }
}
