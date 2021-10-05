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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.advanced.GraphAttributePlugin;
import au.gov.asd.tac.constellation.views.find2.gui.FindViewPane;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.controlsfx.control.CheckComboBox;
import org.openide.util.Exceptions;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewController {

    // Layers view controller instance
    private static FindViewController instance = null;
    private FindViewTopComponent parentComponent;
    private static final Logger LOGGER = Logger.getLogger(FindViewController.class.getName());

    /**
     * Private constructor for singleton
     */
    private FindViewController() {
    }

    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized FindViewController getDefault() {
        if (instance == null) {
            instance = new FindViewController();
        }
        return instance;
    }

    public FindViewController init(final FindViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;
        return instance;
    }

    public void populateAttributes(GraphElementType type, ArrayList<Attribute> attributes, long attributeModificationCounter, CheckComboBox<String> inAttributesMenu) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();

        attributes.clear();
        final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
        final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

        // Wait for the search to find its results:
        try {
            future.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (attrPlugin.getAttributeModificationCounter() != attributeModificationCounter) {
            inAttributesMenu.getItems().clear();

            attributes = attrPlugin.getAttributes();
            for (Attribute a : attributes) {
                if (a.getAttributeType().equals("string")) {
                    inAttributesMenu.getItems().add(a.getName());
                }
            }
        }
    }

    public void disableFindView(FindViewPane pane, boolean disable) {
        pane.setDisable(disable);
        LOGGER.log(Level.SEVERE, "disabled find view" + disable);
    }

    // public because BasicFindPanel calls this on enter as well.
//    public void performBasicSearch(BasicFindRepalceParameters parameters) {
//        final ArrayList<Attribute> selectedAttributes = .getSelectedAttributes();
//        final String findString = basicFindPanel.getFindString();
//        final boolean regex = basicFindPanel.getRegex();
//        final boolean ignoreCase = basicFindPanel.getIgnorecase();
//        final boolean matchWholeWord = basicFindPanel.getExactMatch();
//        final BasicFindPlugin basicfindPlugin = new BasicFindPlugin(type, selectedAttributes, findString, regex, ignoreCase, matchWholeWord, chkAddToSelection.isSelected());
//        PluginExecution.withPlugin(basicfindPlugin).executeLater(graphNode.getGraph());
//    }
}
