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
package au.gov.asd.tac.constellation.views.aiagent.api;

/**
 * The result of running an {@link AgentTool}. The text is what the model sees;
 * the summary is a short line for the user interface.
 *
 * @param text the text returned to the model (usually JSON or a compact table).
 * @param summary a one line summary for display, may be null.
 * @param error true if the tool failed; the text then describes the failure so
 * the model can correct itself.
 *
 * @author doubtsec
 */
public record ToolResult(String text, String summary, boolean error) {

    /**
     * A successful result.
     *
     * @param text the text returned to the model.
     * @return the result.
     */
    public static ToolResult ok(final String text) {
        return new ToolResult(text, null, false);
    }

    /**
     * A successful result with a summary for display.
     *
     * @param text the text returned to the model.
     * @param summary a one line summary.
     * @return the result.
     */
    public static ToolResult ok(final String text, final String summary) {
        return new ToolResult(text, summary, false);
    }

    /**
     * A failed result. The message is returned to the model so it can change
     * course.
     *
     * @param message what went wrong.
     * @return the result.
     */
    public static ToolResult error(final String message) {
        return new ToolResult("Error: " + message, message, true);
    }
}
