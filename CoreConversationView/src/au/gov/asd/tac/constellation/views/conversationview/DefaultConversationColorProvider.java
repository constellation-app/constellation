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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import static au.gov.asd.tac.constellation.views.conversationview.ConversationSide.LEFT;
import static au.gov.asd.tac.constellation.views.conversationview.ConversationSide.RIGHT;
import java.util.List;
import javafx.scene.paint.Color;
import javax.swing.SwingUtilities;

/**
 * The DefaultConversationColorProvider colors bubbles so that bubbles from the
 * single participant on the left side are all colored the same while bubbles on
 * the right are colored by the sender of the message (each distinct sender gets
 * its own color).
 *
 * @author sirius
 */
public class DefaultConversationColorProvider implements ConversationColorProvider {

    @Override
    public void updateMessageColors(final GraphReadMethods graph, final List<ConversationMessage> messages) {
        assert !SwingUtilities.isEventDispatchThread();

        if (graph == null || messages.isEmpty()) {
            return; // Nothing to do.
        }

        final DefaultConversationColor color = new DefaultConversationColor(0.3F, 0.8F);

        // The position in the vertexColors array of each vertex (by position)
        // Each value is 1 greater than the true value to allow 0 to indicate no value.
        final int[] colorPositions = new int[graph.getVertexCount()];

        // The position just after the last left sender color
        int leftVertexCount = 0;

        // The position
        int rightVertexCount = colorPositions.length;

        for (final ConversationMessage message : messages) {
            final int sender = message.getSender();
            if (sender < colorPositions.length) {
                final int senderPosition = graph.getVertexPosition(sender);
                final int colorPosition = colorPositions[senderPosition];
                if (colorPosition == 0) {
                    switch (message.getConversationSide()) {
                        case LEFT:
                            colorPositions[senderPosition] = ++leftVertexCount;
                            break;
                        case RIGHT:
                            colorPositions[senderPosition] = rightVertexCount--;
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        final Color[] vertexColors = new Color[colorPositions.length];

        if (leftVertexCount > 0) {
            vertexColors[0] = Color.GREY;
            for (int i = 1; i < leftVertexCount; i++) {
                vertexColors[i] = color.createColor();
            }
        }

        for (int i = vertexColors.length - 1; i >= rightVertexCount; i--) {
            vertexColors[i] = color.createColor();
        }

        for (final ConversationMessage message : messages) {
            final int sender = message.getSender();
            if (sender < colorPositions.length) {
                final int senderPosition = graph.getVertexPosition(sender);
                final int colorPosition = colorPositions[senderPosition] - 1;
                final Color vertexColor = vertexColors[colorPosition];
                message.setColor(vertexColor);
            }
        }
    }

    private class DefaultConversationColor implements ConversationColor {

        private final float saturation;
        private final float brightness;

        private int hue = 0;
        private int total = 1;

        public DefaultConversationColor(final float saturation, final float brightness) {
            this.saturation = saturation;
            this.brightness = brightness;
        }

        @Override
        public Color createColor() {
            final Color color = Color.hsb(365.0 * hue / total, saturation, brightness);
            hue += 2;
            if (hue >= total) {
                hue = 1;
                total *= 2;
            }
            return color;
        }
    }
}
