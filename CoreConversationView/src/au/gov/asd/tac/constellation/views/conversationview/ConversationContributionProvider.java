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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.Lookup;

/**
 * A ConversationContributionProvider is responsible for creating a
 * {@link ConversationContribution} for each {@link ConversationMessage}.
 * Implementations of this class should create the one type of Contribution for
 * all Messages. The typical pattern is to implement the Contribution as an
 * inner class of the implementation of ConversationContributionProvider that
 * creates it.
 * <br><br>
 * Implementations should also be registered as ServiceProviders of this class
 * as lookup is used to generate a list of all providers available to the
 * ConversationView.
 *
 * @see ConversationContribution
 * @see ConversationMessage
 * @author sirius
 */
public abstract class ConversationContributionProvider implements Comparable<ConversationContributionProvider> {

    private static List<ConversationContributionProvider> PROVIDERS = null;
    private static List<ConversationContributionProvider> U_PROVIDERS = null;

    private final String name;
    private final int priority;

    /**
     * Create a new ConversationContributionProvider.
     *
     * @param name The name of the provider.
     * @param priority The priority of the provider. Providers with lower values
     * for priority will appear earlier in the bubble for a message that has
     * multiple Contributions.
     */
    protected ConversationContributionProvider(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    /**
     * Get the name of this provider
     *
     * @return The name of this provider
     */
    public final String getName() {
        return name;
    }

    /**
     * Get the priority of this provider. Providers with lower values for
     * priority will appear earlier in the bubble for a message that has
     * multiple Contributions.
     *
     * @return The priority of this provider.
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * Returns whether or not this provider is compatible with the given graph.
     * A provider is considered to be compatible with a graph if it can create
     * contributions for messages derived from the graph. Implementations of
     * this method should quickly check the graph to check whether the type of
     * data pertaining to this provider exists on the graph. Usually this will
     * involve checking whether certain attributes or types exist on the graph.
     *
     * @param graph A graph to check compatibility with.
     * @return True if this provider can create contributions with the graph,
     * false otherwise.
     */
    public abstract boolean isCompatibleWithGraph(GraphReadMethods graph);

    /**
     * Constructs and returns a new Contribution for the given message and
     * graph. Implementations of this method should always return a single
     * specific implementation of Contribution; often this will be an inner
     * class of the provider. The Contribution should not have reference to the
     * graph, so any data the Contribution requires should be extracted from the
     * graph in this method and passed into the resulting Contribution.
     *
     * @param graph The graph that the contribution is being created for.
     * @param message The message that the Contribution belongs to.
     * @return A new Contribution for the given graph and message.
     */
    public abstract ConversationContribution createContribution(GraphReadMethods graph, ConversationMessage message);

    /**
     * Get a list of all providers by using lookup. This will find all providers
     * that have been registered as service providers of
     * ConversationContributionProbider.
     *
     * @return A list of all providers.
     */
    public static final synchronized List<ConversationContributionProvider> getProviders() {
        if (PROVIDERS == null) {
            PROVIDERS = new ArrayList<>(Lookup.getDefault().lookupAll(ConversationContributionProvider.class));
            PROVIDERS.sort((ConversationContributionProvider o1, ConversationContributionProvider o2) -> Integer.compare(o1.priority, o2.priority));
            U_PROVIDERS = Collections.unmodifiableList(PROVIDERS);
        }
        return U_PROVIDERS;
    }

    /**
     * Retrieves the list of all contribution providers compatible with the
     * given graph. This will first get all providers and then return a filtered
     * list of those providers for which
     * <code>isCompatibleWithGraph(graph)</code> is true.
     *
     * @param graph The graph to get compatible providers for.
     * @return A list of all providers that are compatible with the given graph.
     */
    public static List<ConversationContributionProvider> getCompatibleProviders(GraphReadMethods graph) {
        final List<ConversationContributionProvider> compatibleProviders = new ArrayList<>();
        if (graph != null) {
            for (ConversationContributionProvider provider : getProviders()) {
                if (provider.isCompatibleWithGraph(graph)) {
                    compatibleProviders.add(provider);
                }
            }
        }
        return compatibleProviders;
    }

    @Override
    public int compareTo(ConversationContributionProvider other) {
        return Integer.compare(priority, other.priority);
    }
}
