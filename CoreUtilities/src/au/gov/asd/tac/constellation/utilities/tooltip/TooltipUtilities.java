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

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider.TooltipDefinition;
import java.time.Duration;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.text.HitInfo;
import javafx.stage.Popup;
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

        final int[] characterIndex = new int[1];
        final TooltipNode[] tooltipNode = new TooltipNode[1];

        textInputControl.setOnMouseEntered(event -> {
            if (tooltipPane.isEnabled()) {
                TextInputControlSkin<?> skin = (TextInputControlSkin<?>) textInputControl.getSkin();
                HitInfo info = (skin instanceof TextAreaSkin) ? ((TextAreaSkin) skin).getIndex(event.getX(), event.getY()) : ((TextFieldSkin) skin).getIndex(event.getX(), event.getY());
                characterIndex[0] = info.getCharIndex();
                List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getTooltips(textInputControl.getText(), characterIndex[0]);
                textInputControl.requestFocus();
                selectActiveArea(textInputControl, definitions);
                if (!definitions.isEmpty()) {
                    tooltipNode[0] = new TooltipNode();
                    tooltipNode[0].setTooltips(definitions);
                    Point2D location = textInputControl.localToScene(event.getX(), textInputControl.getHeight());
                    tooltipPane.showTooltip(tooltipNode[0], location.getX(), location.getY());
                }
            }
        });

        textInputControl.setOnMouseMoved(event -> {
            if (tooltipPane.isEnabled()) {
                TextInputControlSkin<?> skin = (TextInputControlSkin<?>) textInputControl.getSkin();
                HitInfo info = (skin instanceof TextAreaSkin) ? ((TextAreaSkin) skin).getIndex(event.getX(), event.getY()) : ((TextFieldSkin) skin).getIndex(event.getX(), event.getY());

                // If the mouse is over a different character then get new tooltips
                if (info.getCharIndex() != characterIndex[0]) {
                    characterIndex[0] = info.getCharIndex();
                    List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getTooltips(textInputControl.getText(), characterIndex[0]);
                    selectActiveArea(textInputControl, definitions);
                    if (definitions.isEmpty()) {
                        tooltipNode[0] = null;
                    } else {
                        tooltipNode[0] = new TooltipNode();
                        tooltipNode[0].setTooltips(definitions);
                    }
                }

                // If we have a tooltip then reposition under mouse
                if (tooltipNode[0] == null) {
                    tooltipPane.hideTooltip();
                } else {
                    Point2D location = textInputControl.localToScene(event.getX(), textInputControl.getHeight());
                    tooltipPane.showTooltip(tooltipNode[0], location.getX(), location.getY());
                }
            }
        });

        textInputControl.setOnMouseExited(event -> {
            if (tooltipPane.isEnabled()) {
                tooltipPane.hideTooltip();
            }
        });
    }

    private static final double HYPERLINK_TOOLTIP_VERTICAL_GAP = 15.0;

    public static void activateTextInputControl(final Hyperlink hyperlink, final TooltipPane tooltipPane) {

        final TooltipNode[] tooltipNode = new TooltipNode[1];

        hyperlink.setOnMouseEntered(event -> {
            if (tooltipPane.isEnabled()) {
                List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getAllTooltips(hyperlink.getText());
                hyperlink.requestFocus();
                if (!definitions.isEmpty()) {
                    tooltipNode[0] = new TooltipNode();
                    tooltipNode[0].setTooltips(definitions);
                    Point2D location = hyperlink.localToScene(event.getX(), event.getY() + hyperlink.getHeight() + HYPERLINK_TOOLTIP_VERTICAL_GAP);
                    tooltipPane.showTooltip(tooltipNode[0], location.getX(), location.getY());
                }
            }
        });

        hyperlink.setOnMouseExited(event -> {
            if (tooltipPane.isEnabled()) {
                tooltipPane.hideTooltip();
            }
        });
    }

    private static void selectActiveArea(final TextInputControl control, final List<TooltipProvider.TooltipDefinition> definitions) {
        int s = Integer.MAX_VALUE;
        int e = Integer.MIN_VALUE;
        for (final TooltipDefinition definition : definitions) {
            if (definition.getStart() >= 0 && definition.getStart() < s) {
                s = definition.getStart();
            }
            if (definition.getFinish() >= 0 && definition.getFinish() > e) {
                e = definition.getFinish();
            }
        }
        if (s != Integer.MAX_VALUE && e != Integer.MIN_VALUE) {
            control.selectRange(s, e);
        }
    }

    public static void activateTextInputControl(final InlineCssTextArea textArea, final TooltipPane tooltipPane) {

        final int[] characterIndex = new int[1];
        final TooltipNode[] tooltipNode = new TooltipNode[1];

        // PLACEHOLDER CODE TO IMITATE TOOLTIPPANE POPUP. REMOVE LATER ONCE TOOLTIPPANE CONFIRMED WORKING.
        final Popup popup = new Popup();
        final Label popupMsg = new Label();
        popupMsg.setStyle(
                "-fx-background-color: black;"
                + "-fx-text-fill: white;"
                + "-fx-padding: 5;");
        popup.getContent().add(popupMsg);
        // ^

        textArea.setMouseOverTextDelay(Duration.ofMillis(100));
        textArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, event -> {
            if (tooltipPane.isEnabled()) {
                characterIndex[0] = event.getCharacterIndex();
                List<TooltipProvider.TooltipDefinition> definitions = TooltipProvider.getTooltips(textArea.getText(), characterIndex[0]);
                textArea.requestFocus();
                selectActiveArea(textArea, definitions);
                if (!definitions.isEmpty()) {
                    tooltipNode[0] = new TooltipNode();
                    tooltipNode[0].setTooltips(definitions);
                    Point2D location = event.getScreenPosition();
                    tooltipPane.showTooltip(tooltipNode[0], location.getX(), location.getY());
                }

                // PLACEHOLDER CODE TO IMITATE TOOLTIPPANE POPUP. REMOVE LATER ONCE TOOLTIPPANE CONFIRMED WORKING.
                popupMsg.setText(
                        "Character '" + textArea.getText(characterIndex[0], characterIndex[0] + 1)
                        + "' at Index '" + characterIndex[0] + "'");
                final Point2D pos = event.getScreenPosition();
                popup.show(textArea, pos.getX(), pos.getY() + 10);
                // ^
            }
        });

        textArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, event -> {
            if (tooltipPane.isEnabled()) {
                tooltipPane.hideTooltip();

                // PLACEHOLDER CODE TO IMITATE TOOLTIPPANE POPUP. REMOVE LATER ONCE TOOLTIPPANE CONFIRMED WORKING.
                popup.hide();
                // ^
            }
        });
    }

    private static void selectActiveArea(final InlineCssTextArea textArea, final List<TooltipProvider.TooltipDefinition> definitions) {
        int s = Integer.MAX_VALUE;
        int e = Integer.MIN_VALUE;
        for (final TooltipDefinition definition : definitions) {
            if (definition.getStart() >= 0 && definition.getStart() < s) {
                s = definition.getStart();
            }
            if (definition.getFinish() >= 0 && definition.getFinish() > e) {
                e = definition.getFinish();
            }
        }
        if (s != Integer.MAX_VALUE && e != Integer.MIN_VALUE) {
            textArea.selectRange(s, e);
        }
    }
}
