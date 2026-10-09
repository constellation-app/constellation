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

import au.gov.asd.tac.constellation.views.aiagent.llm.ChatTurn;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolCall;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for {@link HistoryCompactor}.
 */
public class HistoryCompactorNGTest {

    @Test
    public void testTruncate() {
        assertEquals(HistoryCompactor.truncate("short", 10), "short");
        final String truncated = HistoryCompactor.truncate("x".repeat(100), 10);
        assertTrue(truncated.startsWith("xxxxxxxxxx\n[Result truncated: 90 more characters"), truncated);
        assertEquals(HistoryCompactor.truncate(null, 10), "");
    }

    @Test
    public void testNoCompactionWithinBudget() {
        final List<ChatTurn> history = List.of(new ChatTurn.Instructions("s"), new ChatTurn.User("u"));
        assertSame(HistoryCompactor.compact(history, 1000), history);
    }

    @Test
    public void testCompactsOldestToolResultsFirst() {
        final List<ChatTurn> history = new ArrayList<>();
        history.add(new ChatTurn.Instructions("system"));
        history.add(new ChatTurn.User("question"));
        for (int i = 0; i < 8; i++) {
            history.add(new ChatTurn.Assistant("", List.of(new ToolCall("c" + i, "tool", "{}"))));
            history.add(new ChatTurn.ToolResult("c" + i, "tool", Integer.toString(i).repeat(1000)));
        }
        final List<ChatTurn> compacted = HistoryCompactor.compact(history, 5000);
        assertEquals(compacted.size(), history.size());
        final ChatTurn.ToolResult first = (ChatTurn.ToolResult) compacted.get(3);
        assertTrue(first.text().startsWith("[Earlier result compacted"), first.text());
        assertEquals(first.callId(), "c0");
        // the most recent results are kept in full
        final ChatTurn.ToolResult last = (ChatTurn.ToolResult) compacted.get(compacted.size() - 1);
        assertEquals(last.text().length(), 1000);
        assertTrue(HistoryCompactor.size(compacted) < HistoryCompactor.size(history));
    }
}
