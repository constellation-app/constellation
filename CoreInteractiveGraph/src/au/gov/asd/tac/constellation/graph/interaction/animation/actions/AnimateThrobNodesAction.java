/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.animation.Animation;
import au.gov.asd.tac.constellation.graph.interaction.animation.ThrobbingNodeAnimation;
import au.gov.asd.tac.constellation.graph.interaction.plugins.display.ToggleDrawFlagPlugin;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.gui.MenuBaseAction;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/*
 * adding animation motion to graph elements
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.graph.interaction.animation.actions.AnimateThrobNodesAction")
@ActionRegistration(displayName = "#CTL_AnimateThrobNodesAction", lazy = false)
@ActionReference(path = "Menu/Experimental/Animations", position = 0)
@Messages("CTL_AnimateThrobNodesAction=Throb Nodes")
public final class AnimateThrobNodesAction extends MenuBaseAction implements ActionListener {

    public AnimateThrobNodesAction() {
        super();
        this.initCheckBox(Bundle.CTL_AnimateThrobNodesAction(), false);
    }
    
    @Override
    protected void updateValue() {
        if ( menuButton.isSelected()){
            Animation.startAnimation(new ThrobbingNodeAnimation(), this.getContext().getGraph());
        } else {
            Animation.stopAnimation(ThrobbingNodeAnimation.NAME);
        }
        
    }

    @Override
    protected void displayValue() {
        final String id = GraphManager.getDefault().getActiveGraph().getId();
        menuButton.setSelected(Animation.isAnimating(ThrobbingNodeAnimation.NAME, id));
    }
}
