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
package au.gov.asd.tac.constellation.views.aiagent.ui;

import au.gov.asd.tac.constellation.views.aiagent.agent.FakeLlmProvider;
import au.gov.asd.tac.constellation.views.aiagent.agent.ToolRegistry;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolContext;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A smoke test of the Assistant view: send a message, approve a tool call
 * on its card, and see the streamed answer rendered as Markdown.
 */
public class AgentViewPaneNGTest {

    private AgentViewController controller;
    private Stage stage;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (controller != null) {
            controller.shutdown();
        }
        if (stage != null) {
            FxToolkit.cleanupStages();
        }
    }

    private static void waitFor(final String what, final Callable<Boolean> condition) throws TimeoutException {
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, condition);
        } catch (final TimeoutException ex) {
            throw new TimeoutException("Timed out waiting for " + what);
        }
    }

    @Test
    public void testSendApproveAndRender() throws Exception {
        final AtomicInteger writes = new AtomicInteger();
        final AgentTool write = new AgentTool() {
            @Override
            public String name() {
                return "write";
            }

            @Override
            public String description() {
                return "Change the graph";
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
                return ToolRisk.MODIFIES_GRAPH;
            }

            @Override
            public ToolResult execute(final JsonNode args, final ToolContext context) {
                writes.incrementAndGet();
                return ToolResult.ok("written", "Wrote it");
            }
        };
        final FakeLlmProvider provider = new FakeLlmProvider()
                .thenCall("c1", "write", "{}")
                .thenText("Done, **all good**.");

        final AgentViewPane pane = WaitForAsyncUtils.asyncFx(AgentViewPane::new).get();
        controller = new AgentViewController(pane, new ToolRegistry(List.of(write)), (endpoint, key) -> provider, endpoint -> null);
        stage = FxToolkit.setupStage(s -> {
            s.setScene(new Scene(pane, 500, 700));
            s.show();
        });
        WaitForAsyncUtils.waitForFxEvents();

        // the model list comes from the provider
        waitFor("the model list", () -> "fake-model".equals(pane.getModelCombo().getValue()));
        assertTrue(pane.isTranscriptEmpty());

        WaitForAsyncUtils.asyncFx(() -> {
            pane.getInput().setText("please write");
            pane.getSendButton().fire();
        }).get();
        waitFor("the approval card", () -> pane.getToolCards().size() == 1 && pane.getToolCards().get(0).isAwaitingApproval());
        assertTrue(pane.getSendButton().isDisabled());
        assertFalse(pane.getStopButton().isDisabled());
        assertTrue(pane.getInput().getText().isEmpty());
        assertEquals(writes.get(), 0);

        final ToolCallCard card = pane.getToolCards().get(0);
        final Button approve = (Button) card.lookupAll(".button").stream()
                .filter(node -> node instanceof Button button && "Approve".equals(button.getText()))
                .findFirst().orElseThrow();
        WaitForAsyncUtils.asyncFx(approve::fire).get();

        waitFor("the answer", () -> !pane.getSendButton().isDisabled());
        assertEquals(writes.get(), 1);
        assertFalse(card.isAwaitingApproval());
        final AssistantMessageNode answer = pane.getAssistantMessages().get(pane.getAssistantMessages().size() - 1);
        assertTrue(answer.isFinished());
        assertEquals(answer.getText(), "Done, **all good**.");
        // rendered as Markdown: the bold run is its own Text node
        final boolean bold = answer.lookupAll(".md-bold").stream().anyMatch(node -> node instanceof Text text && "all good".equals(text.getText()));
        assertTrue(bold, "bold text not rendered");
        // the transcript can be exported
        final String markdown = WaitForAsyncUtils.asyncFx(controller::transcriptMarkdown).get();
        assertTrue(markdown.contains("**You:** please write"), markdown);
        assertTrue(markdown.contains("Tool call `write`"), markdown);
        assertTrue(markdown.contains("**Assistant:** Done"), markdown);

        // new chat clears the transcript
        WaitForAsyncUtils.asyncFx(() -> pane.getNewChatButton().fire()).get();
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(pane.isTranscriptEmpty());
    }

    @Test
    public void testDenyFromCard() throws Exception {
        final FakeLlmProvider provider = new FakeLlmProvider()
                .thenCall("c1", "write", "{}")
                .thenText("OK, I won't.");
        final AgentTool write = new AgentTool() {
            @Override
            public String name() {
                return "write";
            }

            @Override
            public String description() {
                return "Change the graph";
            }

            @Override
            public JsonNode inputSchema() {
                final ObjectNode schema = JsonNodeFactory.instance.objectNode();
                schema.put("type", "object");
                return schema;
            }

            @Override
            public ToolRisk risk() {
                return ToolRisk.MODIFIES_GRAPH;
            }

            @Override
            public ToolResult execute(final JsonNode args, final ToolContext context) {
                throw new AssertionError("should not run");
            }
        };
        final AgentViewPane pane = WaitForAsyncUtils.asyncFx(AgentViewPane::new).get();
        controller = new AgentViewController(pane, new ToolRegistry(List.of(write)), (endpoint, key) -> provider, endpoint -> null);
        stage = FxToolkit.setupStage(s -> {
            s.setScene(new Scene(pane, 500, 700));
            s.show();
        });
        waitFor("the model list", () -> "fake-model".equals(pane.getModelCombo().getValue()));
        WaitForAsyncUtils.asyncFx(() -> controller.send("write please")).get();
        waitFor("the approval card", () -> pane.getToolCards().size() == 1 && pane.getToolCards().get(0).isAwaitingApproval());

        final ToolCallCard card = pane.getToolCards().get(0);
        final List<Node> buttons = List.copyOf(card.lookupAll(".button"));
        WaitForAsyncUtils.asyncFx(() -> buttons.stream().filter(n -> "Deny…".equals(((Button) n).getText())).findFirst().map(n -> {
            ((Button) n).fire();
            return n;
        })).get();
        WaitForAsyncUtils.asyncFx(() -> buttons.stream().filter(n -> "Deny".equals(((Button) n).getText())).findFirst().map(n -> {
            ((Button) n).fire();
            return n;
        })).get();
        waitFor("the answer", () -> !pane.getSendButton().isDisabled());
        final String request = provider.getRequests().get(1).history().get(3).toString();
        assertTrue(request.contains("DENIED"), request);
    }
}
