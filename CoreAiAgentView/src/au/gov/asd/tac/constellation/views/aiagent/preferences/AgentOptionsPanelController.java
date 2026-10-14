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
package au.gov.asd.tac.constellation.views.aiagent.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Registers the Assistant options under Tools &gt; Options &gt;
 * Constellation.
 *
 * @author doubtsec
 */
@OptionsPanelController.SubRegistration(
        id = "AiAssistant",
        location = "constellation",
        displayName = "#AgentOptions_DisplayName",
        keywords = "#AgentOptions_Keywords",
        keywordsCategory = "constellation/AiAssistant",
        position = 1300)
@org.openide.util.NbBundle.Messages({
    "AgentOptions_DisplayName=Assistant",
    "AgentOptions_Keywords=ai assistant llm openai anthropic model agent endpoint"
})
public final class AgentOptionsPanelController extends OptionsPanelController implements HelpCtx.Provider {

    private AgentOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    @Override
    public void update() {
        getPanel().load(AgentSettings.load());
    }

    @Override
    public void applyChanges() {
        if (!isValid()) {
            // otherwise the dialog closes and the changes are silently dropped
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                    "The Assistant settings weren't saved: every endpoint needs a base URL.", NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        final AgentOptionsPanel optionsPanel = getPanel();
        final AgentSettings settings = optionsPanel.toSettings();
        // checked before saving the keys, which clears the pending key changes
        final boolean changed = isChanged();
        if (optionsPanel.hasKeyChanges()) {
            optionsPanel.saveKeys(settings.endpoints());
        }
        if (changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
            settings.save();
        }
    }

    @Override
    public void cancel() {
        // nothing to do: changes are only kept in the panel until applied
    }

    @Override
    public boolean isValid() {
        final AgentSettings settings = getPanel().toSettings();
        // no endpoints is valid: the assistant then asks the user to configure one
        return settings.endpoints().stream().noneMatch(e -> e.baseUrl().isBlank());
    }

    @Override
    public boolean isChanged() {
        return !getPanel().toSettings().equals(AgentSettings.load()) || getPanel().hasKeyChanges();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    private AgentOptionsPanel getPanel() {
        if (panel == null) {
            panel = new AgentOptionsPanel();
            // lets the Options dialog disable OK while an endpoint has no base URL
            panel.setOnChange(() -> {
                pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, null, null);
            });
        }
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("au.gov.asd.tac.constellation.views.aiagent.ui.AgentViewTopComponent");
    }
}
