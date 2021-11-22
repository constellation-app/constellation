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

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import javafx.scene.layout.Region;

/**
 * Contribution objects are the graphical representation of the content in a
 * {@link ConversationMessage} (corresponding to a single transaction). They are
 * created by a {@link ConversationContributionProvider}. Each provider can
 * create at most one Contribution for a given message. All Contributions for a
 * single message are displayed in a single bubble in the Conversation View.
 *
 * @see ConversationContributionProvider
 * @see ConversationMessage
 * @author sirius
 */
public abstract class ConversationContribution {

    private final ConversationContributionProvider provider;
    private final ConversationMessage message;
    private Region content = null;

    /**
     * Create a new Contribution.
     *
     * @param provider The provider that created this contribution.
     * @param message The message that this contribution belongs to.
     */
    protected ConversationContribution(final ConversationContributionProvider provider, final ConversationMessage message) {
        this.provider = provider;
        this.message = message;
    }

    /**
     * Get the provider that created this Contribution.
     *
     * @return This contribution's ConversationContributionProvider.
     */
    public final ConversationContributionProvider getProvider() {
        return provider;
    }

    /**
     * Get the message that this contribution belongs to.
     *
     * @return The Message this contribution belongs to.
     */
    public final ConversationMessage getMessage() {
        return message;
    }

    /**
     * Get the text that this contribution is associated to.
     *
     * @return The text that this contribution is associated to.
     */
    protected abstract String getText();

    /**
     * Gets the javafx Region that is used to display this Contribution inside a
     * bubble in the Conversation View. This method will create the region if it
     * has not previously been created.
     *
     * @param tips The Conversation View's tooltip pane that can be used to add
     * tooltips for this Contribution.
     * @return A javafx Region displaying this contribution.
     */
    public final Region getContent(final TooltipPane tips) {
        if (content == null) {
            content = createContent(tips);
        }
        return content;
    }

    /**
     * Creates the javafx region used to display this contribution. This method
     * needs to be implemented in order to display and format the relevant
     * content as desired for a given Contribution.
     *
     * @param tips The Conversation View's tooltip pane that can be used to add
     * tooltips for this Contribution.
     * @return A javafx Region displaying this contribution.
     */
    protected abstract Region createContent(final TooltipPane tips);
}
