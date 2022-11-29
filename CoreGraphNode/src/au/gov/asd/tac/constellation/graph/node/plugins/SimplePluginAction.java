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
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * abstract class for all simple plugin actions
 *
 * @author sirius
 */
public abstract class SimplePluginAction extends AbstractAction {

    protected final GraphNode context;
    protected final String pluginName;
    protected final boolean interactive;

    protected SimplePluginAction(final GraphNode context, final String pluginName) {
        this(context, pluginName, false);
    }

    protected SimplePluginAction(final GraphNode context, final String pluginName, final boolean interactive) {
        this.context = context;
        this.pluginName = pluginName;
        this.interactive = interactive;
    }

    protected String getName() {
        return NbBundle.getMessage(getClass(), "CTL_" + getClass().getSimpleName());
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        beforePlugin(context);
        PluginExecution.withPlugin(PluginRegistry.get(pluginName)).interactively(interactive).executeLater(context.getGraph());
    }

    /**
     * Called on the EDT before edit(): no locking has been done.
     * <p>
     * This is where user interaction (dialog boxes etc) should be done.
     *
     * @param context the graph node that is current for this execution.
     */
    public void beforePlugin(final GraphNode context) {
    }
}
