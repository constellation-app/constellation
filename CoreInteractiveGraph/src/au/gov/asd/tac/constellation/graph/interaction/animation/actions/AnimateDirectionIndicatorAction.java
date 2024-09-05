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
import au.gov.asd.tac.constellation.graph.interaction.animation.DirectionIndicatorAnimation;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * An action for triggering a {@link DirectionIndiciatorAnimation}.
 * 
 * @author capricornunicorn123
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.graph.interaction.animation.actions.AnimateDirectionIndicatorAction")
@ActionRegistration(displayName = "#CTL_AnimateDirectionIndicatorAction", lazy = false)
@ActionReference(path = "Menu/Experimental/Animations", position = 0)
@Messages("CTL_AnimateDirectionIndicatorAction=Direction Indicators")
@ServiceProvider(service = AnimationMenuBaseAction.class)
public final class AnimateDirectionIndicatorAction extends AnimationMenuBaseAction implements ActionListener {

    public AnimateDirectionIndicatorAction() {
        super(Bundle.CTL_AnimateDirectionIndicatorAction());
    }
    
    @Override
    protected void updateValue() {
        if (menuButton.isSelected()) {
            AnimationUtilities.startAnimation(new DirectionIndicatorAnimation(), this.getContext().getGraph().getId());
        } else {
            stopAnimation(this.getContext().getGraph().getId());
        }
    }

    @Override
    protected void displayValue() {
        menuButton.setSelected(AnimationUtilities.isAnimating(DirectionIndicatorAnimation.NAME, this.getContext().getGraph().getId()));
    }
    
    @Override
    public void stopAnimation(final String graphId) {
        AnimationUtilities.stopAnimation(DirectionIndicatorAnimation.NAME, graphId);
    }
}
