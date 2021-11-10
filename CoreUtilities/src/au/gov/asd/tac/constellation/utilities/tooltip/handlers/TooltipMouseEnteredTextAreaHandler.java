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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

/**
 * Creates a event handler for when the mouse enters a Hyperlink
 *
 * @author aldebaran30701
 */
public class TooltipMouseEnteredTextAreaHandler implements EventHandler {
    
    private final InlineCssTextArea textArea;
    private final TooltipPane tooltipPane;
    private final TooltipNode[] tooltipNode = new TooltipNode[1];
    private final int[] characterIndex = new int[1];
    
    public TooltipMouseEnteredTextAreaHandler(final InlineCssTextArea textArea, final TooltipPane tooltipPane) {
        this.textArea = textArea;
        this.tooltipPane = tooltipPane;
    }
    
    @Override
    public void handle(final Event event) {
        if(event instanceof MouseOverTextEvent){
            final MouseOverTextEvent mote = (MouseOverTextEvent)event;
            if (tooltipPane.isEnabled()) {
                characterIndex[0] = mote.getCharacterIndex();
                final List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getTooltips(textArea.getText(), characterIndex[0]);
                textArea.requestFocus();
                TooltipUtilities.selectActiveArea(textArea, definitions);
                if (!definitions.isEmpty()) {
                    tooltipNode[0] = createTooltipNode(definitions);
                    final Point2D location = mote.getScreenPosition();
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
