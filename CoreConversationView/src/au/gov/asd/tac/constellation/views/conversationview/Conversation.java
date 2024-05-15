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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.update.GraphUpdateController;
import au.gov.asd.tac.constellation.plugins.update.GraphUpdateManager;
import au.gov.asd.tac.constellation.plugins.update.MultiAttributeUpdateComponent;
import au.gov.asd.tac.constellation.plugins.update.UpdateComponent;
import au.gov.asd.tac.constellation.plugins.update.UpdateController;
import au.gov.asd.tac.constellation.views.conversationview.TextConversationContributionProvider.TextContribution;
import au.gov.asd.tac.constellation.views.conversationview.state.ConversationState;
import au.gov.asd.tac.constellation.views.conversationview.state.ConversationViewConcept;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 * A Conversation is model of the all the content for the Conversation View. It
 * comprises all the {@link ConversationMessage} objects associated with a
 * specific selection on the graph. A message corresponds to an individual
 * transaction in the graph. It can be formatted in a number of different ways
 * based on various 'providers'.
 * <br><br>
 * This class is responsible for updating and formatting the list of messages in
 * response to changes from both the graph and the conversation view GUI,
 * according to the following procedure.
 * {@link Conversation#messageProvider messageProvider} examines the current
 * selection on the graph and populates the list
 * {@link Conversation#allMessages allMessages} accordingly. A number of other
 * providers then proceed to filter this list of messages, adding formatting
 * details to the messages that survive at each stage. The output of this
 * process is {@link Conversation#resultMessages resultMessages}, a list that
 * {@link ConversationBox} listens to in order to display the conversation.
 * <br><br>
 * A number of update components determine when the list of all messages needs
 * repopulating or when these various stages of filtering need to be reapplied.
 * They are organised so that when multiple stages need updating the updates
 * happen in the correct order.
 * <br><br>
 * This class also updates the Conversation View GUI controls which allow the
 * selection of contribution providers and sender attributes in response to
 * graph changes.
 *
 * @see ConversationMessage
 * @see ConversationContribution
 * @see ConversationBox
 *
 * @author sirius
 * @author antares
 */
public class Conversation {
    
    private static final Logger LOGGER = Logger.getLogger(Conversation.class.getName());

    private static final Comparator<ConversationMessage> TEMPORAL_COMPARATOR = (ConversationMessage o1, ConversationMessage o2) -> o1.getDatetime().compareTo(o2.getDatetime());

    private static final int LOCK_STAGE = 0;
    private static final int UPDATE_STAGE = 1;
    private static final int JAVAFX_STAGE = 2;

    private final UpdateController<GraphReadMethods> updateController = new UpdateController<>();
    private final GraphUpdateController graphUpdateController = new GraphUpdateController(updateController);
    private final GraphUpdateManager graphUpdateManager = new GraphUpdateManager(graphUpdateController, 2);
    private final MultiAttributeUpdateComponent senderAttributes = new MultiAttributeUpdateComponent((graphUpdateController));

    private ConversationState conversationState = new ConversationState();

    private List<ConversationContributionProvider> compatibleContributionProviders;
    private ConversationMessageProvider messageProvider = new DefaultConversationMessageProvider();
    private ConversationDatetimeProvider datetimeProvider = new DefaultConversationDatetimeProvider();
    private ConversationSenderProvider senderProvider = new DefaultConversationSenderProvider();
    private ConversationColorProvider colorProvider = new DefaultConversationColorProvider();
    private ConversationBackgroundProvider backgroundProvider = new DefaultConversationBackgroundProvider();

    private final List<ConversationMessage> allMessages = new ArrayList<>();
    private final List<ConversationMessage> contributingMessages = new ArrayList<>();
    private final List<ConversationMessage> temporalMessages = new ArrayList<>();
    private final List<ConversationMessage> senderMessages = new ArrayList<>();
    private final List<ConversationMessage> visibleMessages = new ArrayList<>();
    private int pageNumber = 0;
    private int totalMessageCount = 0;
    private int totalPages = 0;
    private int contentPerPage = 25;

    private ObservableList<ConversationMessage> resultMessages = null;
    private ConversationContributionProviderListener contributorListener = null;
    private ConversationSenderAttributeListener senderAttributeListener = null;

    private Set<ConversationContributionProvider> contributingContributionProviders = new TreeSet<>();
    private List<String> possibleSenderAttributes = new ArrayList<>();

    // Thread names.
    private static final String CONVERSATION_VIEW_UPDATE_COLOR_THREAD_NAME = "Conversation View: Update Color Stage";
    private static final String CONVERSATION_VIEW_UPDATE_CONTRIBUTIONS_THREAD_NAME = "Conversation View: Update Contributions";
    private static final String CONVERSATION_VIEW_UPDATE_DATETIME_THREAD_NAME = "Conversation View: Update Datetime";
    private static final String CONVERSATION_VIEW_UPDATE_MESSAGE_THREAD_NAME = "Conversation View: Update Message in Conversation";
    private static final String CONVERSATION_VIEW_UPDATE_SENDER_THREAD_NAME = "Conversation View: Update Sender Attribute Stage";
    private static final String CONVERSATION_VIEW_VISIBILITY_THREAD_NAME = "Conversation View: Visibility Updater";

    /**
     * Create a new Conversation.
     */
    public Conversation() {
        conversationExistanceUpdater.dependOn(graphUpdateController.getNewGraphUpdateComponent());

        conversationStateUpdater.dependOn(conversationExistanceUpdater);
        conversationStateUpdater.dependOn(graphUpdateController.createAttributeUpdateComponent(ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE));

        possibleSenderAttributeUpdater.dependOn(conversationExistanceUpdater);
        possibleSenderAttributeUpdater.dependOn(graphUpdateController.getAttributeUpdateComponent());

        senderAttributeUpdater.dependOn(possibleSenderAttributeUpdater);
        senderAttributeUpdater.dependOn(graphUpdateController.getAttributeUpdateComponent());

        contributionProviderUpdater.dependOn(conversationExistanceUpdater);
        contributionProviderUpdater.dependOn(graphUpdateController.getAttributeUpdateComponent());

        messageUpdater.dependOn(conversationExistanceUpdater);
        messageUpdater.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.VertexAttribute.SELECTED));
        messageUpdater.dependOn(graphUpdateController.createAttributeUpdateComponent(VisualConcept.TransactionAttribute.SELECTED));

        contributionUpdater.dependOn(messageUpdater);

        datetimeUpdater.dependOn(contributionUpdater);

        senderUpdater.dependOn(datetimeUpdater);
        senderUpdater.dependOn(senderAttributes);

        backgroundUpdater.dependOn(senderUpdater);
        colorUpdater.dependOn(senderUpdater);

        visibilityUpdater.dependOn(backgroundUpdater);
        visibilityUpdater.dependOn(colorUpdater);

        resultUpdater.dependOn(visibilityUpdater);

        contributorUpdater.dependOn(resultUpdater);
    }
    
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(final int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    public void setTotalMessageCount(final int totalMessageCount) {
        this.totalMessageCount = totalMessageCount;
    }
    
    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }

    public int getContentPerPage() {
        return contentPerPage;
    }

    public void setContentPerPage(final int contentPerPage) {
        this.contentPerPage = contentPerPage;
    }

    /**
     * Updates the messages currently displayed when the page changes or the content per page drop down is updated
     * 
     * @param graph
     * @return 
     */
    public List<ConversationMessage> updateMessages(final GraphReadMethods graph) {
        visibilityUpdater.update(graph);
        return visibleMessages;
    }
    
    /**
     * Sets the listener in the GUI for changes to the list of attributes that
     * can be displayed for the sender.
     *
     * @param senderAttributeListener The listener to the list of sender
     * attributes
     */
    public void setSenderAttributeListener(final ConversationSenderAttributeListener senderAttributeListener) {
        updateController.lock();
        this.senderAttributeListener = senderAttributeListener;
        updateController.release();
    }

    /**
     * Get the GraphUpdateManager used to link graph listening to updating.
     *
     * @return The GraphUpdateManager.
     */
    public GraphUpdateManager getGraphUpdateManager() {
        return graphUpdateManager;
    }

    /**
     * Inspects the contents of the conversation state and registers any changes
     * that have occurred.
     */
    private UpdateComponent<GraphReadMethods> conversationExistanceUpdater = new UpdateComponent<GraphReadMethods>("Existance State", LOCK_STAGE) {

        @Override
        public boolean update(final GraphReadMethods graph) {
            return graph == null;
        }
    };
    /**
     * Inspects the contents of the conversation state and registers any changes
     * that have occurred.
     */
    private UpdateComponent<GraphReadMethods> conversationStateUpdater = new UpdateComponent<GraphReadMethods>("Conversation State", LOCK_STAGE) {

        @Override
        public boolean update(final GraphReadMethods graph) {
            ConversationState newConversationState;
            if (graph != null) {
                final int conversationStateAttribute = ConversationViewConcept.MetaAttribute.CONVERSATION_VIEW_STATE.get(graph);
                if (conversationStateAttribute == Graph.NOT_FOUND) {
                    newConversationState = new ConversationState();
                    newConversationState.setSenderAttributesToKeys(graph);
                } else {
                    newConversationState = (ConversationState) graph.getObjectValue(conversationStateAttribute, 0);
                    if (newConversationState == null) {
                        newConversationState = new ConversationState();
                        newConversationState.setSenderAttributesToKeys(graph);
                    }
                }
            } else {
                newConversationState = new ConversationState();
            }

            if (!conversationState.getHiddenContributionProviders().equals(newConversationState.getHiddenContributionProviders())) {
                updateController.registerChange(visibilityUpdater, false);
            }

            if (!conversationState.getSenderAttributes().equals(newConversationState.getSenderAttributes())) {

                senderAttributes.updateAttributes(GraphElementType.VERTEX, newConversationState.getSenderAttributes(), false);
                updateController.registerChange(senderUpdater, false);
                updateController.registerChange(senderAttributeUpdater, false);
            }

            conversationState = newConversationState;

            return false;
        }
    };

    /**
     * Updates the list of possible sender attributes so that it can be set in
     * the GUI. Needs to update each time the attributes on the graph change.
     */
    private UpdateComponent<GraphReadMethods> possibleSenderAttributeUpdater = new UpdateComponent<GraphReadMethods>("Possible Senders", LOCK_STAGE) {

        @Override
        public boolean update(final GraphReadMethods graph) {
            possibleSenderAttributes.clear();
            if (graph != null) {
                final int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
                for (int i = 0; i < attributeCount; i++) {
                    final int attributeId = graph.getAttribute(GraphElementType.VERTEX, i);
                    final Attribute attribute = new GraphAttribute(graph, attributeId);
                    if (!ObjectAttributeDescription.class.isAssignableFrom(attribute.getDataType())) {
                        possibleSenderAttributes.add(attribute.getName());
                    }
                }
            }
            return true;
        }
    };

    /**
     * Informs the listener to sender attributes of both the possible and
     * currently set sender attributes so that the listener can set them in the
     * GUI. Needs to update whenever the attributes on the graph, or the
     * selected sender attributes in GUI change.
     */
    private UpdateComponent<GraphReadMethods> senderAttributeUpdater = new UpdateComponent<GraphReadMethods>("Sender Attribute", JAVAFX_STAGE) {

        @Override
        public boolean updateThread(final Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                return false;
            } else {
                Platform.runLater(runnable);
                return true;
            }
        }

        @Override
        public boolean update(final GraphReadMethods graph) {
            if (senderAttributeListener != null) {
                senderAttributeListener.senderAttributesChanged(possibleSenderAttributes, conversationState.getSenderAttributes());
            }
            return true;
        }
    };

    /**
     * Updates the contribution providers for this Conversation. Needs to update
     * when the graph attributes change.
     */
    private UpdateComponent<GraphReadMethods> contributionProviderUpdater = new UpdateComponent<GraphReadMethods>("Contribution Providers", LOCK_STAGE) {
        @Override
        public boolean update(final GraphReadMethods graph) {
            compatibleContributionProviders = ConversationContributionProvider.getCompatibleProviders(graph);
            return true;
        }
    };

    /**
     * Updates the messages in this Conversation. Needs to update whenever the
     * graph selection changes.
     */
    private UpdateComponent<GraphReadMethods> messageUpdater = new UpdateComponent<GraphReadMethods>("Messages", LOCK_STAGE) {
        @Override
        public boolean update(GraphReadMethods graph) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);

                final Thread thread = new Thread(CONVERSATION_VIEW_UPDATE_MESSAGE_THREAD_NAME) {
                    @Override
                    public void run() {
                        messageProvider.setMaxContentPerPage(contentPerPage);
                        messageProvider.getMessages(graph, allMessages, pageNumber);
                        totalMessageCount = messageProvider.getTotalMessageCount();
                        if (totalMessageCount != 0) {
                            totalPages = (int) Math.ceil((double) totalMessageCount / contentPerPage);
                        }
                        latch.countDown();
                    }
                };
                thread.start();

                latch.await();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Messages update was interrupted");
                Thread.currentThread().interrupt();
                return false;
            }

            return true;
        }
    };

    /**
     * Updates the contribution stage of filtering and formatting for messages.
     * Needs to update whenever the graph selection changes.
     */
    private UpdateComponent<GraphReadMethods> contributionUpdater = new UpdateComponent<GraphReadMethods>("Contributions", LOCK_STAGE) {
        @Override
        public boolean update(final GraphReadMethods graph) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);

                final Thread thread = new Thread(CONVERSATION_VIEW_UPDATE_CONTRIBUTIONS_THREAD_NAME) {
                    @Override
                    public void run() {
                        contributingMessages.clear();
                        contributingContributionProviders.clear();

                        Platform.runLater(() -> {
                            for (final ConversationMessage message : allMessages) {
                                message.getAllContributions().clear();

                                for (final ConversationContributionProvider contributionProvider : compatibleContributionProviders) {
                                    final ConversationContribution contribution = contributionProvider.createContribution(graph, message);
                                    if (contribution != null) {
                                        contributingContributionProviders.add(contributionProvider);
                                        message.getAllContributions().add(contribution);
                                    }
                                }

                                if (!message.getAllContributions().isEmpty()) {
                                    contributingMessages.add(message);
                                }
                            }
                            latch.countDown();
                        });
                    }
                };
                thread.start();

                latch.await();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Message contributions update was interrupted");
                Thread.currentThread().interrupt();
                return false;
            }

            return true;
        }
    };

    /**
     * Updates the temporal stage of filtering and formatting for messages. This
     * includes setting date times that are used to sort messages in this
     * Conversation. Needs to update whenever the graph selection changes.
     */
    private UpdateComponent<GraphReadMethods> datetimeUpdater = new UpdateComponent<GraphReadMethods>("Datetime", LOCK_STAGE) {
        @Override
        public boolean update(final GraphReadMethods graph) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);

                final Thread thread = new Thread(CONVERSATION_VIEW_UPDATE_DATETIME_THREAD_NAME) {
                    @Override
                    public void run() {
                        temporalMessages.clear();
                        datetimeProvider.updateDatetimes(graph, contributingMessages);

                        for (final ConversationMessage message : contributingMessages) {
                            boolean thereIsTextContribution = false;

                            if (message != null) {
                                for (final ConversationContribution cont : message.getAllContributions()) {
                                    if (cont instanceof TextContribution) {
                                        thereIsTextContribution = true;
                                        break;
                                    }
                                }

                                // We only want to add messages that contain any content in them.
                                if (message.getDatetime() != null && thereIsTextContribution) {
                                    temporalMessages.add(message);
                                }
                            }
                        }

                        temporalMessages.sort(TEMPORAL_COMPARATOR);

                        latch.countDown();
                    }
                };
                thread.start();

                latch.await();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Message datetimes update was interrupted");
                Thread.currentThread().interrupt();
                return false;
            }

            return true;
        }
    };

    /**
     * Updates the sender attribute stage of filtering and formatting for
     * messages. Needs to update whenever the graph selection changes, or the
     * values of an attribute in the current list of senderAttributes changes.
     */
    private UpdateComponent<GraphReadMethods> senderUpdater = new UpdateComponent<GraphReadMethods>("Senders", LOCK_STAGE) {
        @Override
        public boolean update(final GraphReadMethods graph) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);

                final Thread thread = new Thread(CONVERSATION_VIEW_UPDATE_SENDER_THREAD_NAME) {
                    @Override
                    public void run() {
                        senderProvider.updateMessageSenders(graph, temporalMessages, conversationState.getSenderAttributes());
                        senderMessages.clear();

                        for (final ConversationMessage message : temporalMessages) {
                            if (message.getSenderContent() != null) {
                                senderMessages.add(message);
                            }
                        }

                        latch.countDown();
                    }
                };
                thread.start();

                latch.await();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Message senders update was interrupted");
                Thread.currentThread().interrupt();
                return false;
            }

            return true;
        }
    };

    /**
     * Updates the background stage of filtering and formatting for messages.
     * Updates whenever the senderUpdater does.
     */
    private UpdateComponent<GraphReadMethods> backgroundUpdater = new UpdateComponent<GraphReadMethods>("Backgrounds", LOCK_STAGE) {
        @Override
        public boolean update(final GraphReadMethods graph) {
            backgroundProvider.updateMessageBackgrounds(graph, senderMessages);
            return true;
        }
    };

    /**
     * Updates the color stage of filtering and formatting for messages. Updates
     * whenever the senderUpdater does.
     */
    private UpdateComponent<GraphReadMethods> colorUpdater = new UpdateComponent<GraphReadMethods>(LOCK_STAGE) {
        @Override
        public boolean update(final GraphReadMethods graph) {
            try {
                final CountDownLatch latch = new CountDownLatch(1);

                final Thread thread = new Thread(CONVERSATION_VIEW_UPDATE_COLOR_THREAD_NAME) {
                    @Override
                    public void run() {
                        colorProvider.updateMessageColors(graph, senderMessages);
                        latch.countDown();
                    }
                };
                thread.start();

                latch.await();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Message colors update was interrupted");
                Thread.currentThread().interrupt();
                return false;
            }

            return true;
        }
    };

    /**
     * Updates the visibility stage of filtering and formatting for messages.
     * Updates whenever the senderUpdater does.
     */
    private UpdateComponent<GraphReadMethods> visibilityUpdater = new UpdateComponent<GraphReadMethods>("Visibility", UPDATE_STAGE) {

        @Override
        public boolean updateThread(final Runnable runnable) {
            if (CONVERSATION_VIEW_VISIBILITY_THREAD_NAME.equals(Thread.currentThread().getName())) {
                return false;
            } else {
                new Thread(runnable, CONVERSATION_VIEW_VISIBILITY_THREAD_NAME).start();
                return true;
            }
        }

        @Override
        public boolean update(final GraphReadMethods graph) {
            visibleMessages.clear();
            int count = 0;

            for (final ConversationMessage message : senderMessages) {
                message.filterContributions(conversationState.getHiddenContributionProviders());
                final int minValue = pageNumber * contentPerPage;
                final int maxValue = minValue + contentPerPage;
                if (!message.getVisibleContributions().isEmpty() && visibleMessages.size() < contentPerPage && minValue < count && count < maxValue) {
                    visibleMessages.add(message);
                }
                count++;
            }
            totalPages = (int) Math.ceil((double) totalMessageCount / contentPerPage);

            return true;
        }
    };

    /**
     * Updates the final list of messages that will be actually displayed by the
     * GUI. Updates whenever the senderUpdater does.
     */
    private UpdateComponent<GraphReadMethods> resultUpdater = new UpdateComponent<GraphReadMethods>("Results", JAVAFX_STAGE) {

        @Override
        public boolean updateThread(final Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                return false;
            } else {
                Platform.runLater(runnable);
                return true;
            }
        }

        @Override
        public boolean update(final GraphReadMethods graph) {
            if (resultMessages != null) {
                resultMessages.setAll(visibleMessages);
            }
            return true;
        }
    };

    /**
     * Informs the listener to contributors of both the contributors that are
     * active in messages currently in the conversation so that the listener can
     * reflect this in the GUI. Update whenever the senderUpdater does.
     */
    private final UpdateComponent<GraphReadMethods> contributorUpdater = new UpdateComponent<GraphReadMethods>("Contributors", JAVAFX_STAGE) {

        @Override
        public boolean updateThread(final Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                return false;
            } else {
                Platform.runLater(runnable);
                return true;
            }
        }

        @Override
        public boolean update(final GraphReadMethods graph) {
            if (contributorListener != null) {
                final Map<String, Boolean> contributors = new TreeMap<>();
                for (ConversationContributionProvider contributor : contributingContributionProviders) {
                    contributors.put(contributor.getName(), !conversationState.getHiddenContributionProviders().contains(contributor.getName()));
                }
                contributorListener.contributorsChanged(contributors);
            }
            return true;
        }
    };

    /**
     * Set the resulting list of messages (post filtering and formatting) that
     * will be observed by the GUI.
     *
     * @param resultMessageList An ObservableList of messages.
     */
    public void setResultList(final ObservableList<ConversationMessage> resultMessageList) {
        updateController.lock();
        this.resultMessages = resultMessageList;
        updateController.release();

        updateController.registerChange(resultUpdater);
        updateController.update();
    }

    /**
     * Sets the listener in the GUI for changes to the contributors that are
     * active in messages currently in the conversation.
     *
     * @param contributorListener The listener to the currently active
     * contributors.
     */
    public void setContributorListener(final ConversationContributionProviderListener contributorListener) {
        updateController.lock();
        this.contributorListener = contributorListener;
        updateController.release();

        updateController.registerChange(contributorUpdater);
        updateController.update();
    }

    /**
     * Returns the current list of visible messages for the current
     * conversation.
     *
     * @return List of current visible messages.
     */
    protected final List<ConversationMessage> getVisibleMessages() {
        return Collections.unmodifiableList(visibleMessages);
    }
}
