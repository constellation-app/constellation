/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilites;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Toggle the freeze graph view preference and toolbar icon.
 *
 * @author arcturus
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.graph.interaction.plugins.display.ToggleFreezeGraphViewAction")
@ActionRegistration(displayName = "#CTL_FreezeGraphViewListenerAction",
        iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/display/resources/freezeGraphViewAlternate.png",
        surviveFocusChange = true, lazy = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 500),
    @ActionReference(path = "Toolbars/Display", position = 800)
})
@Messages({
    "CTL_FreezeGraphViewListenerAction=Freeze Graph View",
    "CTL_UnfreezeGraphViewListenerAction=Unfreeze Graph View"
})
public final class ToggleFreezeGraphViewAction extends AbstractAction {

    private static final String ICON_ON_RESOURCE = "resources/freezeGraphView.png";
    private static final String ICON_OFF_RESOURCE = "resources/freezeGraphViewAlternate.png";
    private static final ImageIcon ICON_ON = new ImageIcon(ToggleFreezeGraphViewAction.class.getResource(ICON_ON_RESOURCE));
    private static final ImageIcon ICON_OFF = new ImageIcon(ToggleFreezeGraphViewAction.class.getResource(ICON_OFF_RESOURCE));

    private boolean graphFrozen;

    public ToggleFreezeGraphViewAction() {
        graphFrozen = PreferenceUtilites.isGraphViewFrozen();
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        graphFrozen = !PreferenceUtilites.isGraphViewFrozen();

        // update the preference and icon
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        prefs.put(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, String.valueOf(graphFrozen));
        putValue(Action.NAME, graphFrozen
                ? NbBundle.getMessage(ToggleFreezeGraphViewAction.class, "CTL_UnfreezeGraphViewListenerAction")
                : NbBundle.getMessage(ToggleFreezeGraphViewAction.class, "CTL_FreezeGraphViewListenerAction"));
        putValue(Action.SMALL_ICON, graphFrozen ? ICON_ON : ICON_OFF);
    }
}
