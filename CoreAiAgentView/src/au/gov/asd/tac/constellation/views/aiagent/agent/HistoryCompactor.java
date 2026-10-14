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
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the conversation history within a size budget by truncating tool
 * results and compacting old ones into one-line summaries.
 *
 * @author doubtsec
 */
public final class HistoryCompactor {

    static final int SUMMARY_CHARS = 160;
    // the most recent tool results are always kept in full
    static final int KEEP_RECENT_TOOL_RESULTS = 4;

    private HistoryCompactor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Truncate a tool result, adding a note saying how much was removed.
     *
     * @param text the result text.
     * @param maxChars the maximum length.
     * @return the possibly truncated text.
     */
    public static String truncate(final String text, final int maxChars) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + String.format(
                "%n[Result truncated: %d more characters not shown. Use the tool's offset/limit arguments, or a narrower query, to see more.]",
                text.length() - maxChars);
    }

    /**
     * @param history the history.
     * @return the total number of characters in the history.
     */
    public static int size(final List<ChatTurn> history) {
        int size = 0;
        for (final ChatTurn turn : history) {
            size += switch (turn) {
                case ChatTurn.Instructions t ->
                    t.text().length();
                case ChatTurn.User t ->
                    t.text().length();
                case ChatTurn.Assistant t ->
                    (t.text() == null ? 0 : t.text().length()) + t.toolCalls().stream().mapToInt(c -> c.arguments() == null ? 0 : c.arguments().length()).sum();
                case ChatTurn.ToolResult t ->
                    t.text().length();
            };
        }
        return size;
    }

    /**
     * Compact the oldest tool results, one at a time, until the history fits
     * the budget or only recent results remain.
     *
     * @param history the history.
     * @param maxChars the budget.
     * @return the compacted history (a new list), or the same list if nothing
     * needed compacting.
     */
    public static List<ChatTurn> compact(final List<ChatTurn> history, final int maxChars) {
        if (size(history) <= maxChars) {
            return history;
        }
        final List<ChatTurn> result = new ArrayList<>(history);
        final List<Integer> toolResultIndexes = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) instanceof ChatTurn.ToolResult) {
                toolResultIndexes.add(i);
            }
        }
        final int compactable = Math.max(0, toolResultIndexes.size() - KEEP_RECENT_TOOL_RESULTS);
        for (int n = 0; n < compactable && size(result) > maxChars; n++) {
            final int index = toolResultIndexes.get(n);
            final ChatTurn.ToolResult old = (ChatTurn.ToolResult) result.get(index);
            if (old.text().startsWith("[Earlier result compacted")) {
                continue;
            }
            final String firstLine = old.text().replaceAll("\\s+", " ").trim();
            final String summary = firstLine.length() > SUMMARY_CHARS ? firstLine.substring(0, SUMMARY_CHARS) + "..." : firstLine;
            result.set(index, new ChatTurn.ToolResult(old.callId(), old.toolName(),
                    "[Earlier result compacted to save space; call the tool again if you need it] " + summary));
        }
        return result;
    }
}
