/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI controller for the display of Data Access View categories.
 *
 * @author mimosa
 */
@OptionsPanelController.SubRegistration(
        location = "constellation",
        displayName = "#AdvancedOption_DisplayName_DataAccessView",
        keywords = "#AdvancedOption_Keywords_DataAccessView",
        keywordsCategory = "constellation/DataAccessView",
        position = 200
)
@org.openide.util.NbBundle.Messages({
    "AdvancedOption_DisplayName_DataAccessView=Data Access View",
    "AdvancedOption_Keywords_DataAccessView=DataAccessView"
})
public final class DataAccessViewCategoryPanelController extends OptionsPanelController {

    private DataAccessViewCategoryPanel thePanel;
    private List<String> visibleNow;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean panelRefreshed = false;
    private boolean reorderButtonPressed = false;
    private boolean orderChanged = false;

    /* This method enables to refresh the lists when the panel is opened */
    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);

        SwingUtilities.invokeLater(() -> {
            final DataAccessViewCategoryPanel panel = getPanel();
            if (!panelRefreshed) {
                panel.setVisibleCategory(panel.getVisibleResultList().toString());
                panel.setVisibleCategory(prefs.get(DataAccessViewPreferenceKeys.VISIBLE_DA_VIEW, DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW_DEFAULT));
                panel.setHiddenCategory(prefs.get(DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW, DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW_DEFAULT));
            }
        });
    }

    /* This method enables to write the contents of the lists to the file */
    @Override
    public void applyChanges() {
        if (isValid()) {
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            if (isChanged()) {
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
                final Preferences prefs = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);
                final DataAccessViewCategoryPanel panel = getPanel();
                prefs.put(DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW, panel.getHiddenCategory().toString().replace("[,", "["));
                prefs.put(DataAccessViewPreferenceKeys.VISIBLE_DA_VIEW, panel.getVisibleCategory().toString().replace("[,", "["));
                orderChanged = false;
                reorderButtonPressed = false;
                panelRefreshed = true;
            }
        }
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        final DataAccessViewCategoryPanel panel = getPanel();
        final List<String> visibleSide = panel.getVisibleCategory();
        final List<String> hiddenSide = panel.getHiddenCategory();
        return !visibleSide.equals(hiddenSide);
    }

    @Override
    public boolean isChanged() {
        final Preferences prefs = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);
        final DataAccessViewCategoryPanel panel = getPanel();
        final List<String> hiddenCategory = panel.getHiddenCategory();
        final List<String> visibleCategory = panel.getVisibleCategory();


        if (reorderButtonPressed && !visibleCategory.equals(visibleNow)) {
            orderChanged = true;
            visibleNow = panel.getVisibleCategory();
        }

        return !hiddenCategory.isEmpty() || orderChanged
                || !prefs.get(DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW, DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW_DEFAULT).isEmpty();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public DataAccessViewCategoryPanel getPanel() {
        if (thePanel == null) {
            thePanel = new DataAccessViewCategoryPanel(this);
            visibleNow = thePanel.getVisibleCategory();
        }
        return thePanel;
    }

    public void setReorderButtonPressed(final boolean reorderButtonPressed) {
        this.reorderButtonPressed = reorderButtonPressed;
    }

}
