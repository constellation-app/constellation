/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import org.apache.commons.lang3.StringUtils;
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
    private boolean moveButtonPressed = false;
    private boolean orderChanged = false;
    private boolean firstLoad = true;
    private boolean noEditsMade = true;
    private List<String> visibleOnFirstLoad;
    private List<String> hiddenOnFirstLoad;

    /* This method enables to refresh the lists when the panel is opened */
    @Override
    public void update() {
        final Preferences prefs = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);

        SwingUtilities.invokeLater(() -> {
            final DataAccessViewCategoryPanel panel = getPanel();
            if (!panelRefreshed) {
                panel.setVisibleCategory(panel.getVisibleResultList().toString());
                panel.setVisibleCategory(prefs.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV));
                panel.setHiddenCategory(prefs.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV));
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
                visibleNow = panel.getVisibleCategory();
                prefs.put(DataAccessViewPreferenceKeys.HIDDEN_DAV, panel.getHiddenCategory().toString().replace("[,", "["));
                prefs.put(DataAccessViewPreferenceKeys.VISIBLE_DAV, panel.getVisibleCategory().toString().replace("[,", "["));
                orderChanged = false;
                reorderButtonPressed = false;
                moveButtonPressed = false;
                panelRefreshed = true;
            }
        }
    }

    @Override
    public void cancel() {
        final DataAccessViewCategoryPanel panel = getPanel();
        final Preferences prefs = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);

        final String visibleItems = prefs.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV);
        final String hiddenItems = prefs.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV);

        if (StringUtils.isBlank(visibleItems.replace("[", "").replace("]", "")) && StringUtils.isBlank(hiddenItems.replace("[", "").replace("]", ""))) {
            panel.setVisibleCategory(visibleOnFirstLoad.toString());
            panel.setHiddenCategory(hiddenOnFirstLoad.toString());
            return;
        }

        panel.setVisibleCategory(visibleItems);
        panel.setHiddenCategory(hiddenItems);
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
        final DataAccessViewCategoryPanel panel = getPanel();
        final List<String> visibleCategory = panel.getVisibleCategory();

        if (reorderButtonPressed && !visibleCategory.equals(visibleNow)) {
            orderChanged = true;
            noEditsMade = false;
            visibleNow = panel.getVisibleCategory();
        }

        if (moveButtonPressed) {
            noEditsMade = false;
            firstLoad = false;
        }

        return visibleEntriesHaveChanged() || orderChanged;
    }

    private boolean visibleEntriesHaveChanged() {
        if (noEditsMade) {
            visibleOnFirstLoad = getPanel().getVisibleCategory();
            hiddenOnFirstLoad = getPanel().getHiddenCategory();
        }

        if (firstLoad) {
            visibleNow = getPanel().getVisibleCategory();
            return false;
        }

        if (getPanel().getVisibleCategory().size() == visibleNow.size()) {
            for (int i = 0; i < visibleNow.size(); ++i) {
                if (!getPanel().getVisibleCategory().get(i).equals(visibleNow.get(i))) {
                    return true;
                }
            }
            return false;
        }

        return true;
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

    public void setMoveButtonPressed(final boolean moveButtonPressed) {
        this.moveButtonPressed = moveButtonPressed;
    }
}
