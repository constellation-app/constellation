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
 * How risky running an {@link AgentTool} is. The risk determines the default
 * approval policy for the tool.
 *
 * @author constellation
 */
public enum ToolRisk {
    /**
     * Only reads data. Auto-approved by default.
     */
    READ_ONLY("read only", ToolPolicy.AUTO),
    /**
     * Changes the graph through an undoable plugin. Asks by default.
     */
    MODIFIES_GRAPH("modifies graph · undoable", ToolPolicy.ASK),
    /**
     * Queries an external system (may consume quota or reveal the query).
     * Asks by default.
     */
    EXTERNAL_QUERY("external query", ToolPolicy.ASK),
    /**
     * Writes files or makes other changes that can't be undone. Asks by
     * default.
     */
    DESTRUCTIVE("destructive · not undoable", ToolPolicy.ASK);

    private final String label;
    private final ToolPolicy defaultPolicy;

    ToolRisk(final String label, final ToolPolicy defaultPolicy) {
        this.label = label;
        this.defaultPolicy = defaultPolicy;
    }

    /**
     * @return a short human readable label for display on approval cards.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the policy used for tools of this risk unless overridden.
     */
    public ToolPolicy getDefaultPolicy() {
        return defaultPolicy;
    }
}
