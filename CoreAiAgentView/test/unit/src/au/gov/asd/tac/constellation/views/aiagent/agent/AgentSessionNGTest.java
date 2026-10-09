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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import au.gov.asd.tac.constellation.views.aiagent.llm.ChatTurn;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the agent loop, using scripted model responses.
 */
public class AgentSessionNGTest {

    private static final long TIMEOUT_SECONDS = 10;

    private final List<AgentEvent> events = new CopyOnWriteArrayList<>();
    private volatile Consumer<AgentEvent> onEvent = event -> {
    };
    private CountDownLatch finished;
    private AgentSession session;
    private EchoTool echo;
    private WriteTool write;
    private SlowTool slow;

    @BeforeMethod
    public void setUp() {
        events.clear();
        onEvent = event -> {
        };
        finished = new CountDownLatch(1);
        echo = new EchoTool();
        write = new WriteTool();
        slow = new SlowTool();
    }

    @AfterMethod
    public void tearDown() {
        if (session != null) {
            session.shutdown();
        }
    }

    private AgentSession session(final FakeLlmProvider provider, final int maxSteps, final Map<String, ToolPolicy> policies) {
        final AgentConfig config = new AgentConfig("fake-model", "SYSTEM", maxSteps, 2000, null, null, policies, 0);
        final AgentEnvironment environment = new AgentEnvironment() {
            @Override
            public Graph getActiveGraph() {
                return null;
            }

            @Override
            public String describeContext() {
                return "[Context] test";
            }

            @Override
            public String graphName(final Graph graph) {
                return "graph";
            }
        };
        session = new AgentSession(provider, config, new ToolRegistry(List.of(echo, write, slow, new BigTool())), environment, event -> {
            events.add(event);
            onEvent.accept(event);
            if (event instanceof AgentEvent.TurnFinished) {
                finished.countDown();
            }
        });
        return session;
    }

    private FinishReason runTurn(final String text) throws InterruptedException {
        finished = new CountDownLatch(1);
        session.send(text);
        assertTrue(finished.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), "turn did not finish");
        return ((AgentEvent.TurnFinished) events.get(events.size() - 1)).reason();
    }

    private <T extends AgentEvent> List<T> eventsOf(final Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }

    private void decideWhenProposed(final Decision decision) {
        onEvent = event -> {
            if (event instanceof AgentEvent.ToolCallProposed proposed && proposed.awaitingApproval()) {
                assertTrue(session.decide(proposed.call().id(), decision));
            }
        };
    }

    @Test
    public void testTextOnlyResponse() throws InterruptedException {
        final FakeLlmProvider provider = new FakeLlmProvider().thenText("Hello there");
        session(provider, 5, Map.of());
        assertEquals(runTurn("hi"), FinishReason.STOP);

        final String streamed = eventsOf(AgentEvent.TextDelta.class).stream().map(AgentEvent.TextDelta::delta).reduce("", String::concat);
        assertEquals(streamed, "Hello there");
        final List<ChatTurn> history = session.getHistory();
        assertEquals(history.size(), 3);
        assertEquals(((ChatTurn.Instructions) history.get(0)).text(), "SYSTEM");
        assertEquals(((ChatTurn.User) history.get(1)).text(), "[Context] test\n\nhi");
        assertEquals(((ChatTurn.Assistant) history.get(2)).text(), "Hello there");
        assertEquals(eventsOf(AgentEvent.Usage.class).size(), 1);
        // the tool specs were offered
        assertEquals(provider.getRequests().get(0).tools().size(), 4);
    }

    @Test
    public void testAutoToolThenAnswer() throws InterruptedException {
        final FakeLlmProvider provider = new FakeLlmProvider()
                .thenCall("c1", "echo", "{\"text\":\"ping\"}")
                .thenText("Done");
        session(provider, 5, Map.of());
        assertEquals(runTurn("go"), FinishReason.STOP);

        assertEquals(echo.calls.get(), 1);
        final AgentEvent.ToolCallProposed proposed = eventsOf(AgentEvent.ToolCallProposed.class).get(0);
        assertFalse(proposed.awaitingApproval());
        final AgentEvent.ToolCallFinished done = eventsOf(AgentEvent.ToolCallFinished.class).get(0);
        assertEquals(done.result().text(), "echo: ping");
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertEquals(result.callId(), "c1");
        assertEquals(result.text(), "echo: ping");
        // the second request included the tool result
        assertEquals(provider.getRequests().get(1).history().size(), 4);
    }

    @Test
    public void testApprove() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "write", "{}").thenText("ok"), 5, Map.of());
        decideWhenProposed(new Decision.Approve());
        assertEquals(runTurn("write"), FinishReason.STOP);
        assertEquals(write.calls.get(), 1);
        assertTrue(eventsOf(AgentEvent.ToolCallProposed.class).get(0).awaitingApproval());
    }

    @Test
    public void testDenyIsReturnedToModel() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "write", "{}").thenText("ok"), 5, Map.of());
        decideWhenProposed(new Decision.Deny("not now"));
        assertEquals(runTurn("write"), FinishReason.STOP);
        assertEquals(write.calls.get(), 0);
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertTrue(result.text().contains("DENIED"), result.text());
        assertTrue(result.text().contains("not now"), result.text());
        assertEquals(eventsOf(AgentEvent.ToolCallRejected.class).size(), 1);
    }

    @Test
    public void testEditArguments() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "write", "{\"value\":\"a\"}").thenText("ok"), 5, Map.of());
        decideWhenProposed(new Decision.Edit("{\"value\":\"b\"}"));
        assertEquals(runTurn("write"), FinishReason.STOP);
        assertEquals(write.lastValue, "b");
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertTrue(result.text().contains("edited the arguments"), result.text());
    }

    @Test
    public void testInvalidEditIsRejected() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "write", "{}").thenText("ok"), 5, Map.of());
        decideWhenProposed(new Decision.Edit("{not json"));
        assertEquals(runTurn("write"), FinishReason.STOP);
        assertEquals(write.calls.get(), 0);
    }

    @Test
    public void testAlwaysAllow() throws InterruptedException {
        session(new FakeLlmProvider()
                .thenCall("c1", "write", "{}")
                .thenCall("c2", "write", "{}")
                .thenText("ok"), 5, Map.of());
        decideWhenProposed(new Decision.AlwaysAllow());
        assertEquals(runTurn("write twice"), FinishReason.STOP);
        assertEquals(write.calls.get(), 2);
        final List<AgentEvent.ToolCallProposed> proposals = eventsOf(AgentEvent.ToolCallProposed.class);
        assertTrue(proposals.get(0).awaitingApproval());
        assertFalse(proposals.get(1).awaitingApproval());
        assertTrue(session.isAlwaysAllowed("write"));
    }

    @Test
    public void testStopWhileWaitingForApproval() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "write", "{}").thenText("never"), 5, Map.of());
        onEvent = event -> {
            if (event instanceof AgentEvent.ToolCallProposed) {
                new Thread(session::cancel).start();
            }
        };
        assertEquals(runTurn("write"), FinishReason.CANCELLED);
        assertEquals(write.calls.get(), 0);
        // the cancelled call still has a result, so the history stays valid
        assertTrue(session.getHistory().get(session.getHistory().size() - 1) instanceof ChatTurn.ToolResult);
    }

    @Test
    public void testStopWhileStreaming() throws InterruptedException {
        final FakeLlmProvider provider = new FakeLlmProvider().then(new FakeLlmProvider.Hang());
        session(provider, 5, Map.of());
        onEvent = event -> {
            if (event instanceof AgentEvent.ModelCallStarted) {
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    session.cancel();
                }).start();
            }
        };
        assertEquals(runTurn("hang"), FinishReason.CANCELLED);
        assertTrue(provider.wasCancelled());
    }

    @Test
    public void testStopWhileToolRuns() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "slow", "{}").thenText("never"), 5, Map.of());
        onEvent = event -> {
            if (event instanceof AgentEvent.ToolCallStarted) {
                new Thread(() -> {
                    try {
                        slow.started.await(5, TimeUnit.SECONDS);
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    session.cancel();
                }).start();
            }
        };
        assertEquals(runTurn("slow"), FinishReason.CANCELLED);
        assertTrue(slow.interrupted);
    }

    @Test
    public void testUnknownToolIsReportedToModel() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "nope", "{}").thenText("sorry"), 5, Map.of());
        assertEquals(runTurn("x"), FinishReason.STOP);
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertTrue(result.text().startsWith("Error: Unknown tool"), result.text());
    }

    @Test
    public void testMalformedArguments() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "echo", "{\"text\": ").thenText("sorry"), 5, Map.of());
        assertEquals(runTurn("x"), FinishReason.STOP);
        assertEquals(echo.calls.get(), 0);
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertTrue(result.text().contains("not valid JSON"), result.text());
    }

    @Test
    public void testSchemaValidationListsProvidedArguments() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "echo", "{\"message\":\"hi\"}").thenText("sorry"), 5, Map.of());
        assertEquals(runTurn("x"), FinishReason.STOP);
        assertEquals(echo.calls.get(), 0);
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertTrue(result.text().contains("arguments.text is required"), result.text());
        assertTrue(result.text().contains("you provided: message"), result.text());
    }

    @Test
    public void testArgumentAliasesAreNormalised() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "echo", "{\"value\":\"hi\"}").thenText("ok"), 5, Map.of());
        assertEquals(runTurn("x"), FinishReason.STOP);
        assertEquals(echo.calls.get(), 1);
    }

    @Test
    public void testNeverPolicyHidesTool() throws InterruptedException {
        final FakeLlmProvider provider = new FakeLlmProvider().thenCall("c1", "echo", "{\"text\":\"x\"}").thenText("ok");
        session(provider, 5, Map.of("echo", ToolPolicy.NEVER));
        assertEquals(runTurn("x"), FinishReason.STOP);
        assertTrue(provider.getRequests().get(0).tools().stream().noneMatch(spec -> spec.name().equals("echo")));
        assertEquals(echo.calls.get(), 0);
    }

    @Test
    public void testAskPolicyOverride() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "echo", "{\"text\":\"x\"}").thenText("ok"), 5, Map.of("echo", ToolPolicy.ASK));
        decideWhenProposed(new Decision.Approve());
        assertEquals(runTurn("x"), FinishReason.STOP);
        assertTrue(eventsOf(AgentEvent.ToolCallProposed.class).get(0).awaitingApproval());
    }

    @Test
    public void testMaxSteps() throws InterruptedException {
        session(new FakeLlmProvider()
                .thenCall("c1", "echo", "{\"text\":\"1\"}")
                .thenCall("c2", "echo", "{\"text\":\"2\"}")
                .thenCall("c3", "echo", "{\"text\":\"3\"}"), 2, Map.of());
        assertEquals(runTurn("loop"), FinishReason.MAX_STEPS);
        assertEquals(echo.calls.get(), 2);
        assertEquals(eventsOf(AgentEvent.Notice.class).size(), 1);
    }

    @Test
    public void testProviderError() throws InterruptedException {
        session(new FakeLlmProvider().then(new FakeLlmProvider.Fail("boom")), 5, Map.of());
        assertEquals(runTurn("x"), FinishReason.ERROR);
        final AgentEvent.Error error = eventsOf(AgentEvent.Error.class).get(0);
        assertTrue(error.error().getMessage().contains("boom"));
        // the session can be used again afterwards
        assertFalse(session.isRunning());
    }

    @Test
    public void testToolResultsAreTruncated() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "big", "{}").thenText("ok"), 5, Map.of());
        assertEquals(runTurn("big"), FinishReason.STOP);
        final ChatTurn.ToolResult result = (ChatTurn.ToolResult) session.getHistory().get(3);
        assertTrue(result.text().length() < 2300, "length " + result.text().length());
        assertTrue(result.text().contains("Result truncated"));
    }

    @Test
    public void testDuplicateCallIdsAreMadeUnique() throws InterruptedException {
        session(new FakeLlmProvider()
                .thenCall("call_0", "echo", "{\"text\":\"1\"}")
                .thenCall("call_0", "echo", "{\"text\":\"2\"}")
                .thenText("ok"), 5, Map.of());
        assertEquals(runTurn("x"), FinishReason.STOP);
        final List<AgentEvent.ToolCallFinished> finishedCalls = eventsOf(AgentEvent.ToolCallFinished.class);
        assertEquals(finishedCalls.size(), 2);
        assertFalse(finishedCalls.get(0).call().id().equals(finishedCalls.get(1).call().id()));
    }

    @Test
    public void testNewChatClearsHistory() throws Exception {
        session(new FakeLlmProvider().thenText("one").thenText("two"), 5, Map.of());
        runTurn("first");
        assertEquals(session.getHistory().size(), 3);
        session.newChat().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(session.getHistory().isEmpty());
        runTurn("second");
        assertEquals(session.getHistory().size(), 3);
    }

    @Test
    public void testRedactsSecretsInProposals() throws InterruptedException {
        session(new FakeLlmProvider().thenCall("c1", "write", "{\"value\":\"x\",\"api_key\":\"s3cret\"}").thenText("ok"), 5, Map.of());
        decideWhenProposed(new Decision.Deny(""));
        runTurn("x");
        final AgentEvent.ToolCallProposed proposed = eventsOf(AgentEvent.ToolCallProposed.class).get(0);
        assertFalse(proposed.prettyArguments().contains("s3cret"));
        assertNotNull(proposed.prettyArguments());
    }

    @Test
    public void testNudgesWhenModelAnnouncesButDoesNotAct() throws InterruptedException {
        final FakeLlmProvider provider = new FakeLlmProvider()
                .thenText("I will now run the echo tool.")
                .thenCall("c1", "echo", "{\"text\":\"x\"}")
                .thenText("I will now run it again.")
                .thenText("I will now run it once more.")
                .thenText("never reached");
        session(provider, 10, Map.of());
        assertEquals(runTurn("go"), FinishReason.STOP);
        assertEquals(echo.calls.get(), 1);
        // nudged again after the tool call, but never twice in a row
        assertEquals(provider.getRequests().size(), 4);
        assertTrue(session.getHistory().stream().anyMatch(t -> t instanceof ChatTurn.User u && u.text().equals(AgentSession.CONTINUE_NUDGE)));
    }

    @Test
    public void testEmptyResponseIsRetriedOnce() throws InterruptedException {
        final FakeLlmProvider provider = new FakeLlmProvider().thenText("").thenText("").thenText("never");
        session(provider, 10, Map.of());
        assertEquals(runTurn("go"), FinishReason.STOP);
        assertEquals(provider.getRequests().size(), 2);
    }

    @Test
    public void testAnnouncesAction() {
        assertTrue(AgentSession.announcesAction("Sure. I will now calculate the degree centrality."));
        assertTrue(AgentSession.announcesAction("Attempting to run the grid layout again:"));
        assertTrue(AgentSession.announcesAction("Let me run the plugin."));
        assertTrue(AgentSession.announcesAction("I will first compute the degree centrality for the graph."));
        assertTrue(AgentSession.announcesAction("Next, I will select those vertices."));
        assertFalse(AgentSession.announcesAction("I will be happy to help with anything else."));
        assertFalse(AgentSession.announcesAction("The top vertex is Alice."));
        assertFalse(AgentSession.announcesAction("Should I run it? I will now wait for you?"));
        assertFalse(AgentSession.announcesAction(""));
    }

    @Test
    public void testWarnsWhenContextWindowIsFull() throws InterruptedException {
        // the fake reports 10 input tokens per call
        session(new FakeLlmProvider().withContextWindow(10).thenText("one").thenText("two"), 5, Map.of());
        runTurn("a");
        runTurn("b");
        final List<AgentEvent.Notice> notices = eventsOf(AgentEvent.Notice.class);
        assertEquals(notices.size(), 1, notices.toString());
        assertTrue(notices.get(0).message().contains("OLLAMA_CONTEXT_LENGTH"));
    }

    @Test
    public void testNoContextWarningWithRoom() throws InterruptedException {
        session(new FakeLlmProvider().withContextWindow(100_000).thenText("one"), 5, Map.of());
        runTurn("a");
        assertTrue(eventsOf(AgentEvent.Notice.class).isEmpty());
    }

    // ----- test tools -----
    private static ObjectNode schema(final String required) {
        final ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.put("type", "object");
        final ObjectNode properties = schema.putObject("properties");
        properties.putObject("text").put("type", "string");
        properties.putObject("value").put("type", "string");
        properties.putObject("api_key").put("type", "string");
        if (required != null) {
            schema.putArray("required").add(required);
        }
        return schema;
    }

    private static final class EchoTool implements AgentTool {

        final AtomicInteger calls = new AtomicInteger();

        @Override
        public String name() {
            return "echo";
        }

        @Override
        public String description() {
            return "Echo text";
        }

        @Override
        public JsonNode inputSchema() {
            return schema("text");
        }

        @Override
        public ToolRisk risk() {
            return ToolRisk.READ_ONLY;
        }

        @Override
        public JsonNode normaliseArguments(final JsonNode args) {
            if (args instanceof ObjectNode object && object.has("value") && !object.has("text")) {
                final ObjectNode copy = object.deepCopy();
                copy.set("text", copy.remove("value"));
                return copy;
            }
            return args;
        }

        @Override
        public ToolResult execute(final JsonNode args, final ToolContext context) {
            calls.incrementAndGet();
            return ToolResult.ok("echo: " + args.get("text").asText());
        }
    }

    private static final class WriteTool implements AgentTool {

        final AtomicInteger calls = new AtomicInteger();
        volatile String lastValue;

        @Override
        public String name() {
            return "write";
        }

        @Override
        public String description() {
            return "Change something";
        }

        @Override
        public JsonNode inputSchema() {
            return schema(null);
        }

        @Override
        public ToolRisk risk() {
            return ToolRisk.MODIFIES_GRAPH;
        }

        @Override
        public ToolResult execute(final JsonNode args, final ToolContext context) {
            calls.incrementAndGet();
            lastValue = args.path("value").asText(null);
            return ToolResult.ok("written");
        }
    }

    private static final class SlowTool implements AgentTool {

        final CountDownLatch started = new CountDownLatch(1);
        volatile boolean interrupted;

        @Override
        public String name() {
            return "slow";
        }

        @Override
        public String description() {
            return "Takes a long time";
        }

        @Override
        public JsonNode inputSchema() {
            return schema(null);
        }

        @Override
        public ToolRisk risk() {
            return ToolRisk.READ_ONLY;
        }

        @Override
        public ToolResult execute(final JsonNode args, final ToolContext context) throws InterruptedException {
            started.countDown();
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            } catch (final InterruptedException ex) {
                interrupted = true;
                throw ex;
            }
            return ToolResult.ok("slow");
        }
    }

    private static final class BigTool implements AgentTool {

        @Override
        public String name() {
            return "big";
        }

        @Override
        public String description() {
            return "Returns a lot";
        }

        @Override
        public JsonNode inputSchema() {
            return schema(null);
        }

        @Override
        public ToolResult execute(final JsonNode args, final ToolContext context) {
            return ToolResult.ok("x".repeat(10_000));
        }

        @Override
        public ToolRisk risk() {
            return ToolRisk.READ_ONLY;
        }
    }

    static ToolCall call(final String id, final String name, final String arguments) {
        return new ToolCall(id, name, arguments);
    }
}
