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
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author algol
 */
public class MappingPanelController implements WizardDescriptor.ExtendedAsynchronousValidatingPanel<WizardDescriptorData> {

    private final Graph graph;
    private final boolean isExporting;
    private MappingPanel panel = null;

    public MappingPanelController(final Graph graph, final boolean isExporting) {
        this.graph = graph;
        this.isExporting = isExporting;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            panel = new MappingPanel(graph, isExporting);
        }

        return panel;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(MappingPanelController.class.getName());
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
    }

    @Override
    public void readSettings(final WizardDescriptorData settings) {
        panel.setData(settings.data);
    }

    @Override
    public void storeSettings(final WizardDescriptorData settings) {
        final JdbcData data = settings.data;
        data.vxMappings = JdbcData.copy(panel.getVxModelValues());
        data.txMappings = JdbcData.copy(panel.getTxModelValues());
    }

    @Override
    public void prepareValidation() {
    }

    @Override
    public void validate() throws WizardValidationException {

    }

    @Override
    public void finishValidation() {
    }
}
