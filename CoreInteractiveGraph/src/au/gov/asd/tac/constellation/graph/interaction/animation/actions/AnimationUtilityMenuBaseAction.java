/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.node.gui.MenuBaseAction;

/**
 * An abstract class to provide action control functionality for Animations.
 *
 * @author capricornunicorn123
 */
public abstract class AnimationUtilityMenuBaseAction extends MenuBaseAction {

    public AnimationUtilityMenuBaseAction(final String label) {
        super();
        initMenuItem(label, false);
    }

    @Override
    public final boolean getEnabled() {
        final boolean isenabled;
        isenabled = AnimationUtilities.animationsEnabled() && this.getContext() != null &&  AnimationUtilities.isAnimating(this.getContext().getGraph().getId());
        
        setEnabled(isenabled);
        return isenabled;
    }

    @Override
    protected abstract void updateValue();

    @Override
    protected abstract void displayValue();
}
