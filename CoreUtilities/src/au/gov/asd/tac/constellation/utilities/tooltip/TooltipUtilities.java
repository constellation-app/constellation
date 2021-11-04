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
package au.gov.asd.tac.constellation.utilities.tooltip;

import au.gov.asd.tac.constellation.utilities.tooltip.handlers.MouseEnteredHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.MouseExitedHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.MouseEnteredTextAreaHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.MouseEnteredHyperlinkHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.handlers.MouseMovedHandler;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider.TooltipDefinition;
import java.time.Duration;
import java.util.List;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextInputControl;
import javafx.util.Pair;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.event.MouseOverTextEvent;

/**
 * Handles tool tip rendering on TextInputControl objects.
 *
 * @author sirius
 * @author sol695510
 */
public class TooltipUtilities {

    public static void activateTextInputControl(final TextInputControl textInputControl, final TooltipPane tooltipPane) {
        textInputControl.setOnMouseEntered(new MouseEnteredHandler(textInputControl, tooltipPane));
        textInputControl.setOnMouseMoved(new MouseMovedHandler(textInputControl, tooltipPane));
        textInputControl.setOnMouseExited(new MouseExitedHandler(tooltipPane));
    }
    

    public static void activateTextInputControl(final Hyperlink hyperlink, final TooltipPane tooltipPane) {
        hyperlink.setOnMouseEntered(new MouseEnteredHyperlinkHandler(hyperlink, tooltipPane));
        hyperlink.setOnMouseExited(new MouseExitedHandler(tooltipPane));
    }

    public static void selectActiveArea(final TextInputControl control, final List<TooltipProvider.TooltipDefinition> definitions) {
        final Pair<Integer, Integer> result = findActiveArea(definitions);
        if (result.getKey() != Integer.MAX_VALUE && result.getValue() != Integer.MIN_VALUE) {
            control.selectRange(result.getKey(), result.getValue());
        }
    }

    public static void activateTextInputControl(final InlineCssTextArea textArea, final TooltipPane tooltipPane) {
        textArea.setMouseOverTextDelay(Duration.ofMillis(100));
        textArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, new MouseEnteredTextAreaHandler(textArea, tooltipPane));
        textArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, new MouseExitedHandler(tooltipPane));
    }

    public static void selectActiveArea(final InlineCssTextArea textArea, final List<TooltipProvider.TooltipDefinition> definitions) {
        final Pair<Integer, Integer> result = findActiveArea(definitions);
        if (result.getKey() != Integer.MAX_VALUE && result.getValue() != Integer.MIN_VALUE) {
            textArea.selectRange(result.getKey(), result.getValue());
        }
    }
    
    protected static Pair<Integer,Integer> findActiveArea(final List<TooltipProvider.TooltipDefinition> definitions){
        int s = Integer.MAX_VALUE;
        int e = Integer.MIN_VALUE;
        for (final TooltipDefinition definition : definitions) {
            final int start = definition.getStart();
            final int finish = definition.getFinish();
            if (start >= 0 && start < s) {
                s = start;
            }
            if (finish >= 0 && finish > e) {
                e = finish;
            }
        }
        return new Pair(s,e);
    }
}
