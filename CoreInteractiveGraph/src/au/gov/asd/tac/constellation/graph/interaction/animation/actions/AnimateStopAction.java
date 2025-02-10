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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * An action for stopping all animations.
 * 
 * @author capricornunicorn123
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.graph.interaction.animation.actions.AnimateStopAction")
@ActionRegistration(displayName = "#CTL_AnimateStopAction", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Animations", position = 100, separatorBefore = 99),
    @ActionReference(path = "Shortcuts", name = "S-Escape")
})
@Messages("CTL_AnimateStopAction=Stop Animating")
public final class AnimateStopAction extends AnimationUtilityMenuBaseAction {

    public AnimateStopAction() {
        super(Bundle.CTL_AnimateStopAction());
    }

    @Override
    protected void updateValue() {
        if (this.getContext() != null && this.getContext().getGraph() != null
                && AnimationUtilities.isAnimating(this.getContext().getGraph().getId())) {
            AnimationUtilities.stopAllAnimations(this.getContext().getGraph().getId());
        }
    }

    @Override
    protected void displayValue() {
        // DoNothing
    }
}
