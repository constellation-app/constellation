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
package au.gov.asd.tac.constellation.graph.interaction.animation.actions;

import au.gov.asd.tac.constellation.graph.interaction.animation.AnimationUtilities;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.gui.MenuBaseAction;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import au.gov.asd.tac.constellation.preferences.utilities.PreferenceUtilities;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.openide.util.NbPreferences;

/**
 * An abstract class to provide action control functionality for Animations.
 *
 * @author capricornunicorn123
 */
public abstract class AnimationMenuBaseAction extends MenuBaseAction implements PreferenceChangeListener {
    
    public AnimationMenuBaseAction(final String actionLabel) {
        super();
        this.initCheckBox(actionLabel, false);
        PreferenceUtilities.addPreferenceChangeListener(NbPreferences.forModule(GraphPreferenceKeys.class).absolutePath(), this);
    }
    
    @Override
    public final boolean getEnabled() {
        final boolean animationsEnabled = AnimationUtilities.animationsEnabled();
        setEnabled(animationsEnabled);
        return animationsEnabled;
    }
    
    @Override
    public void preferenceChange(final PreferenceChangeEvent event) {
        final GraphNode context = getContext();
        if (context != null && !isEnabled()) {
            if (menuButton != null){
                menuButton.setSelected(false);
            }
            stopAnimation(context.getGraph().getId());
        }
    }    
    
    protected abstract void stopAnimation(final String context);
}
