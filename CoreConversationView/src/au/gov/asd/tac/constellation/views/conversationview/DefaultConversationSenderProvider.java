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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;

/**
 * The DefaultConversationSenderProvider creates a sender display based on the
 * attributes the user has selected. It also adds the vertices icon if
 * available.
 *
 * @author sirius
 */
public class DefaultConversationSenderProvider implements ConversationSenderProvider {

    private static final Logger LOGGER = Logger.getLogger(DefaultConversationSenderProvider.class.getName());

    @Override
    public void updateMessageSenders(GraphReadMethods graph, List<ConversationMessage> messages, List<String> senderAttributes) {
        assert !SwingUtilities.isEventDispatchThread();

        if (graph == null || messages.isEmpty()) {
            return; //Nothing to do
        }
        try {
            // Get the icon attribute if it exists
            final int iconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.get(graph);

            // Find all the requested sender attributes
            final List<Integer> senderAttributeIds = new ArrayList<>(senderAttributes.size());
            for (final String senderAttribute : senderAttributes) {
                final int senderAttributeId = graph.getAttribute(GraphElementType.VERTEX, senderAttribute);
                if (senderAttributeId != Graph.NOT_FOUND) {
                    senderAttributeIds.add(senderAttributeId);
                }
            }

            // Process each message
            for (final ConversationMessage message : messages) {

                boolean validSender = false;
                List<String> senderLabels = new ArrayList<>(senderAttributeIds.size());

                for (int senderAttributeId : senderAttributeIds) {
                    final String senderLabel = graph.getStringValue(senderAttributeId, message.getSender());
                    if (StringUtils.isBlank(senderLabel)) {
                        senderLabels.add(SeparatorConstants.HYPHEN);
                    } else {
                        senderLabels.add(senderLabel);
                        validSender = true;
                    }
                }

                Image iconImage = null;
                if (iconAttribute != Graph.NOT_FOUND) {
                    final String iconName = graph.getStringValue(iconAttribute, message.getSender());
                    if (iconName != null) {
                        final ConstellationIcon icon = IconManager.getIcon(iconName);
                        if (icon != null) {
                            iconImage = icon.buildImage();
                        }
                    }
                }

                if (!validSender) {
                    senderLabels = Arrays.asList(String.valueOf(message.getSender()));
                }

                message.setSenderContent(new DefaultConversationSender(senderLabels, iconImage));
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private static class DefaultConversationSender implements ConversationSender {

        private final List<String> senderLabels;
        private final Image iconImage;

        public DefaultConversationSender(final List<String> senderLabels, final Image iconImage) {
            this.senderLabels = senderLabels;
            this.iconImage = iconImage;
        }

        @Override
        public Region createContent() {
            Region region = null;
            try {
                if (senderLabels.size() == 1) {
                    region = new SelectableLabel(senderLabels.get(0), false, "-fx-text-fill: #cccccc;", null, null);
                } else {
                    final VBox content = new VBox(-5.0);
                    content.setAlignment(Pos.CENTER_LEFT);
                    for (final String senderLabel : senderLabels) {
                        content.getChildren().add(new SelectableLabel(senderLabel, false, "-fx-text-fill: #cccccc;", null, null));
                    }
                    region = content;
                }

                if (iconImage != null) {
                    final BorderPane borderPane = new BorderPane();
                    borderPane.setCenter(region);

                    final ImageView iconView = new ImageView(iconImage);
                    iconView.setFitHeight(32);
                    iconView.setPreserveRatio(true);
                    borderPane.setLeft(iconView);

                    region = borderPane;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            return region;
        }
    }
}
