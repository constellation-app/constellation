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
package au.gov.asd.tac.constellation.utilities.tooltip.handlers;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipNode;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider;
import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseEvent;

/**
 * Creates a event handler for when the mouse enters a Hyperlink
 *
 * @author aldebaran30701
 */
public class TooltipMouseEnteredHyperlinkHandler implements EventHandler {
    
    private static final double HYPERLINK_TOOLTIP_VERTICAL_GAP = 15.0;
    
    private final Hyperlink hyperlink;
    private final TooltipPane tooltipPane;
    private final TooltipNode[] tooltipNode = new TooltipNode[1];
    
    
    public TooltipMouseEnteredHyperlinkHandler(final Hyperlink hyperlink, final TooltipPane tooltipPane) {
        this.hyperlink = hyperlink;
        this.tooltipPane = tooltipPane;
    }
    
    @Override
    public void handle(final Event event) {
        if (tooltipPane.isEnabled()) {
            final List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getAllTooltips(hyperlink.getText());
            hyperlink.requestFocus();
            if (!definitions.isEmpty()) {
                tooltipNode[0] = createTooltipNode(definitions);
                if (event instanceof MouseEvent mouseEvent) {
                    final Point2D location = hyperlink.localToScene(mouseEvent.getX(), mouseEvent.getY() + hyperlink.getHeight() + HYPERLINK_TOOLTIP_VERTICAL_GAP);
                    tooltipPane.showTooltip(tooltipNode[0], location.getX(), location.getY());
                }
            }
        }
    }
    
    protected static TooltipNode createTooltipNode(final List<TooltipProvider.TooltipDefinition> definitions){
        final TooltipNode ttn = new TooltipNode();
        ttn.setTooltips(definitions);
        return ttn;
    }
}
