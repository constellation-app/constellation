/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.plugins;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle.Messages;

/**
 * This class provides access to the Attributes of particular element on any
 * given graph.
 *
 * @author betelgeuse
 * @see SimpleReadPlugin
 */
@PluginInfo(pluginType = PluginType.SEARCH, tags = {"SEARCH"})
@Messages("GraphAttributePlugin=Find: Retrieve Attributes")
public final class GraphAttributePlugin extends SimpleReadPlugin {

    private final GraphElementType type;
    private final List<Attribute> attributes;
    private long attributeModificationCounter;

    /**
     * Constructs a new <code>GraphAttributePlugin</code>, and passes in the
     * existing known attributes.
     *
     * @param type The type of GraphElements that we are interested in getting
     * the attributes of.
     * @param attributes The list of currently identified attributes. (If any).
     * @param attributeModificationCounter The counter that lets us know if any
     * changes have been made to attributes on the graph.
     *
     * @see GraphElementType
     * @see List
     * @see Attribute
     */
    public GraphAttributePlugin(final GraphElementType type, final List<Attribute> attributes,
            final long attributeModificationCounter) {
        this.type = type;
        this.attributes = attributes;
        this.attributeModificationCounter = attributeModificationCounter;
    }

    @Override
    public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        retrieveAttributes(graph);
    }

    /**
     * Returns the current attributes for the graph.
     *
     * @return List of identified attributes.
     * @see ArrayList
     * @see Attribute
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns the current attribute modification counter, which can be used to
     * determine if any changes have occurred on the graph's attributes.
     *
     * @return The attribute modification counter.
     */
    public long getAttributeModificationCounter() {
        return attributeModificationCounter;
    }

    /**
     * Helper method that performs the attribute query operation.
     * <p>
     * Sets any found attributes to an internal variable which can be later
     * returned through use of the <code>getAttributes()</code> method.
     *
     * @param graph The graph to retrieve attributes for.
     *
     * @see Graph
     */
    private void retrieveAttributes(final GraphReadMethods graph) {
        final long amc = graph.getAttributeModificationCounter();
        attributes.clear();

        final int attrCount = graph.getAttributeCount(type);
        for (int position = 0; position < attrCount; position++) {
            final int attr = graph.getAttribute(type, position);
            final Attribute candidate = new GraphAttribute(graph, attr);

            attributes.add(candidate);
        }

        attributeModificationCounter = amc;
    }
}
