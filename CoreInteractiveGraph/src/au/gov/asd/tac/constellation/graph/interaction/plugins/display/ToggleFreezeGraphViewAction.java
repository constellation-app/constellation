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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilites;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.actions.BooleanStateAction;

/**
 * Toggle the freeze graph view preference and toolbar icon.
 *
 * @author arcturus
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.display.ToggleFreezeGraphViewAction")
@ActionRegistration(displayName = "#CTL_ToggleFreezeGraphViewAction", surviveFocusChange = true, lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 500),
    @ActionReference(path = "Toolbars/Visualisation", position = 800)
})
@Messages("CTL_ToggleFreezeGraphViewAction=Freeze Graph View")
public final class ToggleFreezeGraphViewAction extends BooleanStateAction {

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final boolean freezeGraphView = PreferenceUtilites.isGraphViewFrozen();
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);

        // update the preference and icon
        prefs.put(ApplicationPreferenceKeys.FREEZE_GRAPH_VIEW, String.valueOf(!freezeGraphView));

        // this will trigger a call to iconResource() which will set the icon, so no point doing it twice; hence setting it to null
        putValue(Action.SMALL_ICON, null);

        setBooleanState(PreferenceUtilites.isGraphViewFrozen());
    }

    @Override
    protected void initialize() {
        super.initialize();
        setBooleanState(PreferenceUtilites.isGraphViewFrozen());
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleFreezeGraphViewAction.class, "CTL_ToggleFreezeGraphViewAction");
    }

    @Override
    protected String iconResource() {
        return PreferenceUtilites.isGraphViewFrozen()
                ? "au/gov/asd/tac/constellation/graph/interaction/plugins/display/resources/snowflake.png"
                : "au/gov/asd/tac/constellation/graph/interaction/plugins/display/resources/snowflake_alternate.png";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
