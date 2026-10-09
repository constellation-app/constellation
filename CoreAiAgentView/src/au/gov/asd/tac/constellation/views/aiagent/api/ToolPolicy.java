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
 * Whether a tool call needs the user's approval before it runs.
 *
 * @author doubtsec
 */
public enum ToolPolicy {
    /**
     * Run without asking.
     */
    AUTO,
    /**
     * Ask the user to approve, deny or edit each call.
     */
    ASK,
    /**
     * Never run; the tool isn't offered to the model.
     */
    NEVER;

    /**
     * Parse a policy name, falling back to a default for unknown values.
     *
     * @param name the policy name.
     * @param fallback the policy to return if the name isn't recognised.
     * @return the policy.
     */
    public static ToolPolicy parse(final String name, final ToolPolicy fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return ToolPolicy.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (final IllegalArgumentException ex) {
            return fallback;
        }
    }
}
