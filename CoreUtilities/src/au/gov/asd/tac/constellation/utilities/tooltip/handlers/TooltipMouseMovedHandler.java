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
package au.gov.asd.tac.constellation.utilities.tooltip.handlers;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipNode;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider;
import static au.gov.asd.tac.constellation.utilities.tooltip.TooltipUtilities.selectActiveArea;
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
 * Creates a event handler for when the mouse moves in an input control
 *
 * @author aldebaran30701
 */
public class TooltipMouseMovedHandler implements EventHandler<MouseEvent> {
    
    private final TextInputControl textInputControl;
    private final TooltipPane tooltipPane;
    private final int[] characterIndex = new int[1];
    private final TooltipNode[] tooltipNode = new TooltipNode[1];
    
    public TooltipMouseMovedHandler(final TextInputControl textInputControl, final TooltipPane tooltipPane) {
        this.textInputControl = textInputControl;
        this.tooltipPane = tooltipPane;
    }
    
    @Override
    public void handle(final MouseEvent event) {
        if (tooltipPane.isEnabled()) {
            final TextInputControlSkin<?> skin = (TextInputControlSkin<?>) textInputControl.getSkin();
            final HitInfo info = (skin instanceof TextAreaSkin) ? ((TextAreaSkin) skin).getIndex(event.getX(), event.getY()) : ((TextFieldSkin) skin).getIndex(event.getX(), event.getY());

            // If the mouse is over a different character then get new tooltips
            if (info.getCharIndex() != characterIndex[0]) {
                characterIndex[0] = info.getCharIndex();
                final List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getTooltips(textInputControl.getText(), characterIndex[0]);
                selectActiveArea(textInputControl, definitions);
                tooltipNode[0] = definitions.isEmpty() ? null : createTooltipNode(definitions);
            }

            // If we have a tooltip then reposition under mouse
            if (tooltipNode[0] == null) {
                tooltipPane.hideTooltip();
            } else {
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
