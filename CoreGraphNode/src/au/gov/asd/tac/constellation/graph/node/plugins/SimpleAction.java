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
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * abstract class for all simple actions
 *
 * @author sirius
 */
public abstract class SimpleAction extends AbstractAction {

    protected final GraphNode context;

    protected SimpleAction(final GraphNode context) {
        this.context = context;
    }

    protected String getName() {
        return NbBundle.getMessage(getClass(), "CTL_" + getClass().getSimpleName());
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(new SimplePlugin(getName()) {
            @Override
            protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                SimpleAction.this.edit(graphs, interaction, parameters);
            }
        }).interactively(true).executeLater(context.getGraph());
    }

    protected abstract void edit(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException;
}
