/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Internal Data Access view state used for tracking the panels status for a
 * specific graph. Each graph has its own state with the data access view.
 * 
 * @author formalhaunt
 */
public final class DataAccessPaneStatePerGraph {
    private boolean queriesRunning = false;
    private boolean executeButtonIsGo = true;
    private Map<Future<?>, String> runningPlugins = new HashMap<>();

    /**
     * Gets the flag indicating if there are any queries running for the graph.
     *
     * @return true if there are queries currently running, false otherwise
     */
    public boolean isQueriesRunning() {
        return queriesRunning;
    }

    /**
     * Sets the flag indicating if there is any queries running for the graph.
     *
     * @param queriesRunning true if there are queries currently running, false otherwise
     */
    public void setQueriesRunning(final boolean queriesRunning) {
        this.queriesRunning = queriesRunning;
    }

    /**
     * Gets the flag indicating if the execute button on the Data Access view is
     * in a go "state". The button should be enabled and have the text "Go".
     *
     * @return true if the execute button is in a go "state", false otherwise
     */
    public boolean isExecuteButtonIsGo() {
        return executeButtonIsGo;
    }

    /**
     * Sets the flag indicating if the execute button on the Data Access view is
     * in a go "state". The button should be enabled and have the text "Go".
     *
     * @param executeButtonIsGo true if the execute button is in a go "state", false otherwise
     */
    public void setExecuteButtonIsGo(final boolean executeButtonIsGo) {
        this.executeButtonIsGo = executeButtonIsGo;
    }

    /**
     * Gets the currently running plugins for the graph. The map contains the
     * {@link Future} for the running plugin and the name of the plugin.
     *
     * @return a map containing references to all the running plugins on the
     *     graph
     */
    public Map<Future<?>, String> getRunningPlugins() {
        return runningPlugins;
    }

    /**
     * Sets the currently running plugins for the graph. The map contains the
     * {@link Future} for the running plugin and the name of the plugin.
     *
     * @param runningPlugins a map containing references to all the running
     *     plugins on the graph
     */
    public void setRunningPlugins(final Map<Future<?>, String> runningPlugins) {
        this.runningPlugins = runningPlugins;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final DataAccessPaneStatePerGraph rhs = (DataAccessPaneStatePerGraph) obj;

        return new EqualsBuilder()
                .append(isQueriesRunning(), rhs.isQueriesRunning())
                .append(isExecuteButtonIsGo(), rhs.isExecuteButtonIsGo())
                .append(getRunningPlugins(), rhs.getRunningPlugins())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isQueriesRunning())
                .append(isExecuteButtonIsGo())
                .append(getRunningPlugins())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("queriesRunning", isQueriesRunning())
                .append("executeButtonIsGo", isExecuteButtonIsGo())
                .append("runningPlugins", getRunningPlugins())
                .toString();
    }
}
