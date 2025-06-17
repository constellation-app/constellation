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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * An action for stopping all animations.
 * 
 * @author capricornunicorn123
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.graph.interaction.animation.actions.AnimatePauseAction")
@ActionRegistration(displayName = "#CTL_AnimatePauseAction", lazy = false)
@ActionReference(path = "Menu/Experimental/Animations", position = 100, separatorBefore = 99)
@Messages("CTL_AnimatePauseAction=Pause Animating")
public final class AnimatePauseAction extends AnimationUtilityMenuBaseAction {

    public AnimatePauseAction() {
        super(Bundle.CTL_AnimatePauseAction());
    }

    @Override
    protected void updateValue() {
        AnimationUtilities.pauseAllAnimations(
                this.getContext().getGraph().getId(), 
                !AnimationUtilities.isGraphAnimationsPaused(this.getContext().getGraph().getId())
        );
        // trigger displayValue because meta attribute changes won't trigger it
        this.displayValue();
    }

    @Override
    protected void displayValue() {
        final boolean graphAnimationsPaused = AnimationUtilities.isGraphAnimationsPaused(this.getContext().getGraph().getId());
        menuButton.setText(graphAnimationsPaused ? "Resume Animating" : "Pause Animating");
    }
}
