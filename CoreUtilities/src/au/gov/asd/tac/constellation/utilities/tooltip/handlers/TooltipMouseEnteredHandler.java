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
package au.gov.asd.tac.constellation.utilities.tooltip.handlers;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipNode;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipUtilities;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.HitInfo;

/**
 * Creates a event handler for when the mouse enters an input control
 *
 * @author aldebaran30701
 */
public class TooltipMouseEnteredHandler implements EventHandler<MouseEvent> {
    
    private final TextInputControl textInputControl;
    private final TooltipPane tooltipPane;
    private final int[] characterIndex = new int[1];
    private final TooltipNode[] tooltipNode = new TooltipNode[1];
    
    
    public TooltipMouseEnteredHandler(final TextInputControl textInputControl, final TooltipPane tooltipPane) {
        this.textInputControl = textInputControl;
        this.tooltipPane = tooltipPane;
    }
    
    @Override
    public void handle(final MouseEvent event) {
        if (tooltipPane.isEnabled()) {
            final TextInputControlSkin<?> skin = (TextInputControlSkin<?>) textInputControl.getSkin();
            final HitInfo info = (skin instanceof TextAreaSkin) ? ((TextAreaSkin) skin).getIndex(event.getX(), event.getY()) : ((TextFieldSkin) skin).getIndex(event.getX(), event.getY());
            characterIndex[0] = info.getCharIndex();
            final List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getTooltips(textInputControl.getText(), characterIndex[0]);
            textInputControl.requestFocus();
            TooltipUtilities.selectActiveArea(textInputControl, definitions);
            if (!definitions.isEmpty()) {
                tooltipNode[0] = createTooltipNode(definitions);
                final Point2D location = textInputControl.localToScene(event.getX(), textInputControl.getHeight());
                tooltipPane.showTooltip(tooltipNode[0], location.getX(), location.getY());
            }
        }
    }
    
    protected static TooltipNode createTooltipNode(final List<TooltipProvider.TooltipDefinition> definitions){
        final TooltipNode ttn = new TooltipNode();
        ttn.setTooltips(definitions);
        return ttn;
    }
}
