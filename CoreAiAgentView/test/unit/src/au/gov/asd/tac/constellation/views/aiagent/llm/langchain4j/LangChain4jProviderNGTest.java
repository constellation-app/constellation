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

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.llm.ChatTurn;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmResponse;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolSpec;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for the LangChain4j adapter.
 */
public class LangChain4jProviderNGTest {

    @Test
    public void testToMessages() {
        final List<ChatMessage> messages = LangChain4jProvider.toMessages(List.of(
                new ChatTurn.Instructions("sys"),
                new ChatTurn.User("hi"),
                new ChatTurn.Assistant("", List.of(new ToolCall("c1", "tool", ""))),
                new ChatTurn.ToolResult("c1", "tool", "result"),
                new ChatTurn.Assistant("done", List.of())));
        assertEquals(messages.size(), 5);
        assertEquals(((SystemMessage) messages.get(0)).text(), "sys");
        assertEquals(((UserMessage) messages.get(1)).singleText(), "hi");
        final AiMessage call = (AiMessage) messages.get(2);
        assertNull(call.text());
        assertEquals(call.toolExecutionRequests().get(0).id(), "c1");
        // empty arguments are sent as an empty object
        assertEquals(call.toolExecutionRequests().get(0).arguments(), "{}");
        final ToolExecutionResultMessage result = (ToolExecutionResultMessage) messages.get(3);
        assertEquals(result.id(), "c1");
        assertEquals(result.text(), "result");
        assertEquals(((AiMessage) messages.get(4)).text(), "done");
    }

    @Test
    public void testToResponse() {
        final ChatResponse response = ChatResponse.builder()
                .aiMessage(AiMessage.from("text", List.of(
                        ToolExecutionRequest.builder().id("x").name("a").arguments("{}").build(),
                        ToolExecutionRequest.builder().name("b").arguments("{\"q\":1}").build())))
                .tokenUsage(new TokenUsage(7, 3))
                .finishReason(FinishReason.TOOL_EXECUTION)
                .build();
        final LlmResponse converted = LangChain4jProvider.toResponse(response);
        assertEquals(converted.text(), "text");
        assertEquals(converted.toolCalls().size(), 2);
        assertEquals(converted.toolCalls().get(0).id(), "x");
        // a missing id is filled in
        assertEquals(converted.toolCalls().get(1).id(), "call_1");
        assertEquals(converted.inputTokens(), 7);
        assertEquals(converted.outputTokens(), 3);
        assertEquals(converted.finishReason(), "TOOL_EXECUTION");
    }

    @Test
    public void testParseModels() throws IOException {
        final List<ModelInfo> openAi = LangChain4jProvider.parseModels("{\"data\":[{\"id\":\"b-model\"},{\"id\":\"a-model\"}]}");
        assertEquals(openAi.stream().map(ModelInfo::id).toList(), List.of("a-model", "b-model"));
        final List<ModelInfo> anthropic = LangChain4jProvider.parseModels("{\"data\":[{\"id\":\"claude\",\"display_name\":\"Claude\"}]}");
        assertEquals(anthropic.get(0).displayName(), "Claude");
        assertTrue(LangChain4jProvider.parseModels("{}").isEmpty());
    }

    @Test
    public void testParseContextWindow() throws IOException {
        final String body = "{\"models\":[{\"name\":\"gemma4:e4b\",\"model\":\"gemma4:e4b\",\"context_length\":4096}]}";
        assertEquals(LangChain4jProvider.parseContextWindow(body, "gemma4:e4b").getAsInt(), 4096);
        assertTrue(LangChain4jProvider.parseContextWindow(body, "other").isEmpty());
        assertTrue(LangChain4jProvider.parseContextWindow("{}", "gemma4:e4b").isEmpty());
    }

    @Test
    public void testToolSpecAdapter() throws IOException {
        final ToolSpec spec = new ToolSpec("query", "Query things", JsonFactoryUtilities.getObjectMapper().readTree("""
                {"type":"object","properties":{
                  "type":{"type":"string","enum":["vertex","transaction"],"description":"what"},
                  "limit":{"type":"integer"},
                  "ids":{"type":"array","items":{"type":"integer"}},
                  "filter":{"type":"object","properties":{"op":{"type":"string"}},"required":["op"]}
                },"required":["type"]}
                """));
        final ToolSpecification converted = ToolSpecAdapter.toToolSpecification(spec);
        assertEquals(converted.name(), "query");
        final JsonObjectSchema parameters = converted.parameters();
        assertEquals(parameters.required(), List.of("type"));
        assertTrue(parameters.properties().get("type") instanceof JsonEnumSchema);
        assertEquals(((JsonEnumSchema) parameters.properties().get("type")).enumValues(), List.of("vertex", "transaction"));
        assertTrue(parameters.properties().get("limit") instanceof JsonIntegerSchema);
        assertTrue(((JsonArraySchema) parameters.properties().get("ids")).items() instanceof JsonIntegerSchema);
        assertEquals(((JsonObjectSchema) parameters.properties().get("filter")).required(), List.of("op"));
    }

    /**
     * Every LangChain4j model or catalog built in this module must be given
     * our HTTP client builder, so requests use the Constellation security
     * context and proxy settings.
     */
    @Test
    public void testEveryModelBuilderUsesConstellationHttpClient() throws IOException {
        final Path relative = Path.of("src/au/gov/asd/tac/constellation/views/aiagent/llm/langchain4j/LangChain4jProvider.java");
        // tests run from somewhere under the module directory; search upwards for the source
        Path source = null;
        for (Path dir = Path.of("").toAbsolutePath(); dir != null && source == null; dir = dir.getParent()) {
            for (final Path candidate : List.of(dir.resolve(relative), dir.resolve("CoreAiAgentView").resolve(relative))) {
                if (Files.exists(candidate)) {
                    source = candidate;
                }
            }
        }
        assertTrue(source != null, "source not found from " + Path.of("").toAbsolutePath());
        final String text = Files.readString(source, StandardCharsets.UTF_8);
        final Matcher builders = Pattern.compile("(OpenAi\\w*|Anthropic\\w*)\\.builder\\(\\)").matcher(text);
        int count = 0;
        while (builders.find()) {
            count++;
            final String rest = text.substring(builders.end(), Math.min(text.length(), builders.end() + 120));
            assertTrue(rest.trim().startsWith(".httpClientBuilder(httpClientBuilder())"),
                    builders.group() + " is not built with httpClientBuilder(): " + rest);
        }
        assertTrue(count >= 2, "expected at least two model builders, found " + count);
        // the only raw HttpClient comes from ConstellationHttpClients
        assertTrue(!text.contains("HttpClient.newBuilder(") && !text.contains("HttpClient.newHttpClient("));
        assertTrue(text.contains("ConstellationHttpClients.newBuilder("));
    }
}
