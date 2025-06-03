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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

/**
 * A Message represents the portion of a conversation contributed by a single
 * transaction in the graph. A message is rendered as a single bubble in the
 * conversation view.
 * <br><br>
 * Message objects don't directly contain the content that gets displayed in the
 * conversation view. Rather a message can have several contributions, each of
 * which can be used to create a visual representation of the content for the
 * conversation view.
 *
 * @see ConversationContribution
 * @see ConversationContributionProvider
 *
 * @author sirius
 */
public class ConversationMessage {

    private final int transaction;
    private final int sender;
    private final ConversationSide conversationSide;

    private ConversationDatetime datetime;
    private ConversationSender senderContent;
    private Color color;

    private Background background;
    private String backgroundColor;

    private final List<ConversationContribution> allContributions = new ArrayList<>();
    private final List<ConversationContribution> visibleContributions = new ArrayList<>();

    /**
     * Creates a new Message based on a transaction and sender.
     *
     * @param transaction The graph ID of the transaction that this message is
     * associated with.
     * @param sender The graph ID of the vertex that is the sender of this
     * message.
     * @param conversationSide a ConversationSide constant indicating which
     * 'side' of a conversation this message should appear on.
     */
    public ConversationMessage(final int transaction, final int sender, final ConversationSide conversationSide) {
        this.transaction = transaction;
        this.sender = sender;
        this.conversationSide = conversationSide;
    }

    /**
     * Create the list of visible contributions for this message so that it
     * contains all contributions which have not been created by one of the
     * providers in the supplied set.
     *
     * @param hiddenProviders A set of the names of the contribution providers
     * whose created contributions should be hidden.
     */
    public void filterContributions(final Set<String> hiddenProviders) {
        visibleContributions.clear();

        if (hiddenProviders == null) {

            visibleContributions.addAll(allContributions);

        } else {

            for (ConversationContribution contribution : allContributions) {
                if (!hiddenProviders.contains(contribution.getProvider().getName())) {
                    visibleContributions.add(contribution);
                }
            }
        }
    }

    /**
     * Get the graph ID of the transaction that this message is associated with.
     *
     * @return The ID of the transaction.
     */
    public int getTransaction() {
        return transaction;
    }

    /**
     * Get the graph ID of the vertex that is the sender of this message.
     *
     * @return The ID of the sender.
     */
    public int getSender() {
        return sender;
    }

    /**
     * Get the constant indicating which side of the conversation this message
     * is on.
     *
     * @return The ConversationSide constant for this message.
     */
    public ConversationSide getConversationSide() {
        return conversationSide;
    }

    /**
     * Get a representation of the date time at which this message occurred
     * within the context of the conversation.
     *
     * @return A ConversationDatetime object representing when this message
     * occurred.
     */
    public ConversationDatetime getDatetime() {
        return datetime;
    }

    /**
     * Set the representation of the date time at which this message occurred
     * within the context of the conversation.
     *
     * @param datetime A ConversationDatetime object representing when this
     * message occurred.
     */
    public void setDatetime(final ConversationDatetime datetime) {
        this.datetime = datetime;
    }

    /**
     * Get the visual representation of the sender information for this message.
     *
     * @return a ConversationSender object from which a javafx Region can be
     * created to display the sender information for this message.
     */
    public ConversationSender getSenderContent() {
        return senderContent;
    }

    /**
     * Set the visual representation of the sender information for this message.
     *
     * @param senderContent a ConversationSender object from which a javafx
     * Region can be created to display the sender information for this message.
     */
    public void setSenderContent(final ConversationSender senderContent) {
        this.senderContent = senderContent;
    }

    /**
     * Get the color of this message. This is used to color the text when this
     * message is rendered.
     *
     * @return A Color for this message.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the color of this message. This is used to color the text when this
     * message is rendered.
     *
     * @param color A Color for this message.
     */
    public void setColor(final Color color) {
        this.color = color;
    }

    /**
     * Get the background for this message. This is used as the background for
     * the bubble in which this message will be rendered.
     *
     * @return A Background for this message.
     */
    public Background getBackground() {
        return background;
    }

    /**
     * Set the background for this message. This is used as the background for
     * the bubble in which this message will be rendered.
     *
     * @param background A Background for this message.
     */
    public void setBackground(final Background background) {
        this.background = background;
    }

    /**
     * Get the background color for this message. This is used to color the
     * background of the bubble in which this message will be rendered.
     *
     * @return A Color for the background of this message.
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Set the background color for this message. This is used to color the
     * background of the bubble in which this message will be rendered.
     *
     * @param backgroundColor A Color for the background of this message.
     */
    public void setBackgroundColor(final String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Get the list of all contributions to this message. Contributions are
     * various ways of formatting the content of the message, capable of
     * constructing a javafx Region that can be added to the message's bubble.
     *
     * @return The list of all contributions.
     */
    public List<ConversationContribution> getAllContributions() {
        return allContributions;
    }

    /**
     * Get the list of visible contributions to this message, that is
     * contributions that should be rendered in the conversation view.
     * Contributions that are created by providers that are currently turned off
     * in the conversation view GUI will not be in this list.
     *
     * @return The list of all visible contributions.
     */
    public List<ConversationContribution> getVisibleContributions() {
        return Collections.unmodifiableList(visibleContributions);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.transaction;
        hash = 89 * hash + this.sender;
        hash = 89 * hash + Objects.hashCode(this.conversationSide);
        hash = 89 * hash + Objects.hashCode(this.datetime);
        hash = 89 * hash + Objects.hashCode(this.senderContent);
        hash = 89 * hash + Objects.hashCode(this.color);
        hash = 89 * hash + Objects.hashCode(this.background);
        hash = 89 * hash + Objects.hashCode(this.backgroundColor);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConversationMessage other = (ConversationMessage) obj;
        if (this.transaction != other.transaction) {
            return false;
        }
        if (this.sender != other.sender) {
            return false;
        }
        if (this.conversationSide != other.conversationSide) {
            return false;
        }
        if (!Objects.equals(this.datetime, other.datetime)) {
            return false;
        }
        if (!Objects.equals(this.senderContent, other.senderContent)) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        if (!Objects.equals(this.background, other.background)) {
            return false;
        }
        return Objects.equals(this.backgroundColor, other.backgroundColor);
    }
}
