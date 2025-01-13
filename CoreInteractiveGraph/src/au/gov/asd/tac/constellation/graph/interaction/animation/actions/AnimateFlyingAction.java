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
import au.gov.asd.tac.constellation.graph.interaction.animation.FlyingAnimation;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * An action for triggering a {@link FlyingAnimation}.
 * 
 * @author capricornunicorn123
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.graph.interaction.animation.actions.AnimateFlyingAction")
@ActionRegistration(displayName = "#CTL_AnimateFlyingAction", lazy = false)
@ActionReference(path = "Menu/Experimental/Animations", position = 0)
@Messages("CTL_AnimateFlyingAction=Fly Through")
@ServiceProvider(service = AnimationMenuBaseAction.class)
public final class AnimateFlyingAction extends AnimationMenuBaseAction implements ActionListener {

    public AnimateFlyingAction() {
        super(Bundle.CTL_AnimateFlyingAction());
    }
    
    @Override
    protected void updateValue() {
        if (menuButton.isSelected()) {
            AnimationUtilities.startAnimation(new FlyingAnimation(), this.getContext().getGraph().getId());
        } else {
            stopAnimation(this.getContext().getGraph().getId());
        }
    }

    @Override
    protected void displayValue() {
        menuButton.setSelected(AnimationUtilities.isAnimating(FlyingAnimation.NAME, this.getContext().getGraph().getId()));
    }
    
    @Override
    protected void stopAnimation(final String graphId) {
        AnimationUtilities.stopAnimation(FlyingAnimation.NAME, graphId);
    }
}
