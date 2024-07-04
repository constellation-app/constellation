/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.testing.CoreTestingPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author capricornunicorn123
 */
@ActionID(category = "Developer", id = "au.gov.asd.tac.constellation.testing.construction.TestableGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_TestableGraphBuilderAction")
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Developer", position = 1000)
})
@NbBundle.Messages("CTL_TestableGraphBuilderAction=Testable Graph Builder")
public final class TestableGraphBuilderAction implements ActionListener {

    private final GraphNode context;

    public TestableGraphBuilderAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(CoreTestingPluginRegistry.TESTABLE_GRAPH_BUILDER)
                .interactively(false)
                .executeLater(context.getGraph());
    }
}