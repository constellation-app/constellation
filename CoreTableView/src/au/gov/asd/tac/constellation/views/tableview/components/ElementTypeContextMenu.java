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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.plugins.UpdateStatePlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;

/**
 *
 * @author Quasar985
 */
public class ElementTypeContextMenu {

    private static final String TRANSACTION = "Transactions";
    private static final String VERTEX = "Vertices";
    private static final String EDGE = "Edges";
    private static final String LINK = "Links";

    private final Table table;

    private ContextMenu contextMenu;

    private CustomMenuItem transactionsMenu;
    private CustomMenuItem verticesMenu;
    private CustomMenuItem edgesMenu;
    private CustomMenuItem linksMenu;

    /**
     * Creates a new column visibility context menu.
     *
     * @param table the table that this menu was will be associated to
     */
    public ElementTypeContextMenu(final Table table) {
        this.table = table;
    }

    /**
     * Initializes the column visibility context menu. Until this method is called, all menu UI components will be null.
     */
    public void init() {
        contextMenu = new ContextMenu();

        transactionsMenu = createCustomMenu(TRANSACTION, e -> {
            handleStateChange(GraphElementType.TRANSACTION);
            e.consume();
        });

        verticesMenu = createCustomMenu(VERTEX, e -> {
            handleStateChange(GraphElementType.VERTEX);
            e.consume();
        });

        edgesMenu = createCustomMenu(EDGE, e -> {
            handleStateChange(GraphElementType.EDGE);
            e.consume();
        });

        linksMenu = createCustomMenu(LINK, e -> {
            handleStateChange(GraphElementType.LINK);
            e.consume();
        });

        contextMenu.getItems().addAll(transactionsMenu, verticesMenu, edgesMenu, linksMenu);
    }

    /**
     * Create a custom menu item that will be added to menu buttons on the context menu. Sets the associated text and
     * adds a listener for when it is clicked.
     *
     * @param title the title to be associated to the menu item
     * @param handler the action handler that will be called when the menu item is clicked
     * @return the created menu item
     */
    private CustomMenuItem createCustomMenu(final String title,
            final EventHandler<ActionEvent> handler) {
        final CustomMenuItem menuItem = new CustomMenuItem(new Label(title));

        menuItem.setHideOnClick(false);
        menuItem.setOnAction(handler);

        return menuItem;
    }

    private void handleStateChange(GraphElementType graphElementType) {
        System.out.println("handleStateChange " + graphElementType.toString());
        if (getTableViewTopComponent().getCurrentState() != null) {
            System.out.println("curretn state not null");
            final TableViewState newState = new TableViewState(getTableViewTopComponent().getCurrentState());
            newState.setElementType(graphElementType);

            PluginExecution.withPlugin(
                    new UpdateStatePlugin(newState)
            ).executeLater(getTableViewTopComponent().getCurrentGraph());
        }
    }

    /**
     * Convenience method for accessing the table view top component.
     *
     * @return the table view top component
     */
    private TableViewTopComponent getTableViewTopComponent() {
        return table.getParentComponent().getParentComponent();
    }

    /**
     * Gets the element type context menu.
     *
     * @return the element type context menu
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * Gets the transactions menu.
     *
     * @return the transactions menu
     */
    public CustomMenuItem getTransactionsMenu() {
        return transactionsMenu;
    }

    /**
     * Gets the vertices menu.
     *
     * @return the vertices menu
     */
    public CustomMenuItem getVerticesMenu() {
        return verticesMenu;
    }

    /**
     * Gets the edges menu.
     *
     * @return the edges menu
     */
    public CustomMenuItem getEdgesMenu() {
        return edgesMenu;
    }

    /**
     * Gets the links menu.
     *
     * @return the links menu
     */
    public CustomMenuItem getLinksMenu() {
        return linksMenu;
    }
}
