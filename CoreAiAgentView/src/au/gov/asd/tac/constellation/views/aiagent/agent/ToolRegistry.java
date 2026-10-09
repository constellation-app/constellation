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

import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.llm.ToolSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.openide.util.Lookup;

/**
 * The tools available to the agent.
 *
 * @author constellation
 */
public final class ToolRegistry {

    private static final Logger LOGGER = Logger.getLogger(ToolRegistry.class.getName());
    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,64}$");

    private final Map<String, AgentTool> tools = new LinkedHashMap<>();

    public ToolRegistry(final Collection<? extends AgentTool> tools) {
        for (final AgentTool tool : tools) {
            if (!VALID_NAME.matcher(tool.name()).matches()) {
                LOGGER.log(Level.WARNING, "Ignoring AI agent tool with invalid name: {0}", tool.name());
            } else if (this.tools.putIfAbsent(tool.name(), tool) != null) {
                LOGGER.log(Level.WARNING, "Ignoring duplicate AI agent tool: {0}", tool.name());
            }
        }
    }

    /**
     * @return a registry of every {@link AgentTool} registered in the default
     * lookup.
     */
    public static ToolRegistry fromLookup() {
        return new ToolRegistry(Lookup.getDefault().lookupAll(AgentTool.class));
    }

    public AgentTool get(final String name) {
        return name == null ? null : tools.get(name);
    }

    public List<AgentTool> getTools() {
        return Collections.unmodifiableList(new ArrayList<>(tools.values()));
    }

    /**
     * The specs of the tools that should be offered to the model.
     *
     * @param policy the policy that applies to each tool.
     * @return the specs of every tool whose policy isn't NEVER.
     */
    public List<ToolSpec> specs(final Function<AgentTool, ToolPolicy> policy) {
        final List<ToolSpec> specs = new ArrayList<>();
        tools.values().stream()
                .filter(tool -> policy.apply(tool) != ToolPolicy.NEVER)
                .forEach(tool -> specs.add(new ToolSpec(tool.name(), tool.description(), tool.inputSchema())));
        return specs;
    }
}
