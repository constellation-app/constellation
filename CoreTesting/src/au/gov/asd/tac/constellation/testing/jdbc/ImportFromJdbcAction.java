/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing.jdbc;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import javax.swing.JComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File", id = "au.gov.asd.tac.constellation.testing.jdbc.ImportFromJdbcAction")
@ActionRegistration(displayName = "#CTL_ImportFromJdbcAction", iconBase = "au/gov/asd/tac/constellation/testing/jdbc/import.png", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Import", position = 0)
@Messages({
    "CTL_ImportFromJdbcAction=From JDBC...",
    "MSG_ImportFromJdbc=Import from JDBC"
})
public final class ImportFromJdbcAction implements ActionListener {

    final GraphNode context;

    public ImportFromJdbcAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();

        final WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[]{new ConnectionPanelController(graph), new TablesPanelController(), new MappingPanelController(graph, false)};
        final String[] steps = new String[panels.length];
        int i = 0;
        for (final WizardDescriptor.Panel<WizardDescriptor> panel : panels) {
            final JComponent jc = (JComponent) panel.getComponent();
            steps[i] = jc.getName();
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            i++;
        }
        final WizardDescriptorData wd = new WizardDescriptorData(panels);
        wd.setTitleFormat(new MessageFormat("{0}"));
        wd.setTitle(Bundle.MSG_ImportFromJdbc());
        final Object result = DialogDisplayer.getDefault().notify(wd);
        if (result == DialogDescriptor.OK_OPTION) {
            final ImportFromJdbcPlugin exporter = new ImportFromJdbcPlugin(wd.data);
            PluginExecution.withPlugin(exporter).executeLater(graph);
        }
    }
}
