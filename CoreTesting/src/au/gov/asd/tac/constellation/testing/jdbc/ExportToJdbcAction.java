/*
 * Copyright 2010-2019 Australian Signals Directorate. All Rights Reserved.
 *
 * NOTICE: All information contained herein remains the property of the
 * Australian Signals Directorate. The intellectual and technical concepts
 * contained herein are proprietary to the Australian Signals Directorate and
 * are protected by copyright law. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from the Australian Signals Directorate.
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

@ActionID(category = "File", id = "au.gov.asd.tac.constellation.testing.jdbc.ExportToJdbcAction")
@ActionRegistration(displayName = "#CTL_ExportToJdbcAction", iconBase = "au/gov/asd/tac/constellation/testing/jdbc/export.png", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Export", position = 0)
@Messages({
    "CTL_ExportToJdbcAction=To JDBC...",
    "MSG_ExportToJdbc=Export to JDBC"
})
public final class ExportToJdbcAction implements ActionListener {

    final GraphNode context;

    public ExportToJdbcAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Graph graph = context.getGraph();
        final WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[]{new ConnectionPanelController(graph), new TablesPanelController(), new MappingPanelController(graph, true)};
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
        wd.setTitle(Bundle.MSG_ExportToJdbc());
        final Object result = DialogDisplayer.getDefault().notify(wd);
        if (result == DialogDescriptor.OK_OPTION) {
            final ExportToJdbcPlugin exporter = new ExportToJdbcPlugin(wd.data);
            PluginExecution.withPlugin(exporter).executeLater(graph);
        }
    }
}
