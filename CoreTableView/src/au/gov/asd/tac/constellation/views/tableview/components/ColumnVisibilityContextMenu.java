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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.tableview.TableViewTopComponent;
import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.api.UpdateMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

/**
 * Creates the column visibility context menu. This menu contains items that
 * allow the user to control which columns are visible in the table and which
 * are not. It provides some standard visibility options (like show all or show
 * none) plus allowing for individual column selection.
 *
 * @author formalhaunt
 */
public class ColumnVisibilityContextMenu {

    private static final String SPLIT_SOURCE = "Source";
    private static final String SPLIT_DESTINATION = "Destination";
    private static final String SPLIT_TRANSACTION = "Transaction";

    private static final String ALL_COLUMNS = "Show All Columns";
    private static final String DEFAULT_COLUMNS = "Show Default Columns";
    private static final String KEY_COLUMNS = "Show Key Columns";
    private static final String NO_COLUMNS = "Show No Columns";

    private static final String FILTER_CAPTION = "Filter:";

    private static final ImageView SPLIT_SOURCE_ICON = new ImageView(UserInterfaceIconProvider.MENU.buildImage(16));
    private static final ImageView SPLIT_DESTINATION_ICON = new ImageView(UserInterfaceIconProvider.MENU.buildImage(16));
    private static final ImageView SPLIT_TRANSACTION_ICON = new ImageView(UserInterfaceIconProvider.MENU.buildImage(16));

    private static final int WIDTH = 120;

    private final Table table;

    private ContextMenu contextMenu;

    private CustomMenuItem sourceVertexColumnsMenu;
    private CustomMenuItem destinationVertexColumnMenu;
    private CustomMenuItem tansactionColumnMenu;

    private CustomMenuItem showAllColumnsMenu;
    private CustomMenuItem showDefaultColumnsMenu;
    private CustomMenuItem showPrimaryColumnsMenu;
    private CustomMenuItem hideAllColumnsMenu;
    private static final Logger LOGGER = Logger.getLogger(ColumnVisibilityContextMenu.class.getName());

    /**
     * Creates a new column visibility context menu.
     *
     * @param table the table that this menu was will be associated to
     */
    public ColumnVisibilityContextMenu(final Table table) {
        this.table = table;
    }

    /**
     * Initializes the column visibility context menu. Until this method is
     * called, all menu UI components will be null.
     */
    public void init() {
        contextMenu = new ContextMenu();

        showAllColumnsMenu = createCustomMenu(ALL_COLUMNS, e -> {
            getActiveTableReference().updateVisibleColumns(
                    getTableViewTopComponent().getCurrentGraph(),
                    getTableViewTopComponent().getCurrentState(),
                    extractColumnAttributes(table.getColumnIndex()),
                    UpdateMethod.REPLACE
            );
            e.consume();
        });

        showDefaultColumnsMenu = createCustomMenu(DEFAULT_COLUMNS, e -> {
            getActiveTableReference().updateVisibleColumns(
                    getTableViewTopComponent().getCurrentGraph(),
                    getTableViewTopComponent().getCurrentState(),
                    extractColumnAttributes(table.getColumnIndex().stream()
                            .filter(column -> Character.isUpperCase(
                            column.getAttribute().getName().charAt(0))
                            )
                            .collect(Collectors.toList())),
                    UpdateMethod.REPLACE
            );
            e.consume();
        });

        showPrimaryColumnsMenu = createCustomMenu(KEY_COLUMNS, e -> {
            LOGGER.log(Level.SEVERE, "Calling show kwys");
            if (getTableViewTopComponent().getCurrentGraph() != null) {
                final Set<GraphAttribute> keyAttributes = new HashSet<>();
                final ReadableGraph readableGraph = getTableViewTopComponent().getCurrentGraph().getReadableGraph();
                try {
                    final int[] vertexKeys = readableGraph.getPrimaryKey(GraphElementType.VERTEX);
                    for (final int vertexKey : vertexKeys) {
                        keyAttributes.add(new GraphAttribute(readableGraph, vertexKey));
                    }
                    final int[] transactionKeys = readableGraph.getPrimaryKey(GraphElementType.TRANSACTION);
                    for (final int transactionKey : transactionKeys) {
                        keyAttributes.add(new GraphAttribute(readableGraph, transactionKey));
                    }
                } finally {
                    readableGraph.release();
                }
                //keyAttributes.forEach(attribute -> LOGGER.log(Level.SEVERE, "" + attribute.getId()));
                getActiveTableReference().updateVisibleColumns(
                        getTableViewTopComponent().getCurrentGraph(),
                        getTableViewTopComponent().getCurrentState(),
                        extractColumnAttributes(
                                table.getColumnIndex().stream()
                                        .filter(column -> keyAttributes.stream()
                                        .anyMatch(keyAttribute -> keyAttribute.equals(column.getAttribute()))
                                        )
                                        .collect(Collectors.toList())
                        ),
                        UpdateMethod.REPLACE
                );
                e.consume();
            }
        });

        hideAllColumnsMenu = createCustomMenu(NO_COLUMNS, e -> {
            table.getColumnIndex().forEach(column -> column.getTableColumn().setVisible(false));
            getActiveTableReference().updateVisibleColumns(
                    getTableViewTopComponent().getCurrentGraph(),
                    getTableViewTopComponent().getCurrentState(),
                    Collections.emptyList(),
                    UpdateMethod.REPLACE
            );
            e.consume();
        });

        contextMenu.getItems().addAll(showAllColumnsMenu, showDefaultColumnsMenu, showPrimaryColumnsMenu,
                hideAllColumnsMenu, new SeparatorMenuItem());

        // This next section basically creates three menus. One for each element type, vertex vource,
        // vertex destination and transactions. All columns are attributes of one of these entities.
        // The columns are split and added to their respective menus. Each menu also gets a filter
        // text box.
        final MenuButton sourceVertexColumnsButton = createMenuButton(SPLIT_SOURCE, SPLIT_SOURCE_ICON);
        final MenuButton destinationVertexColumnsButton = createMenuButton(SPLIT_DESTINATION, SPLIT_DESTINATION_ICON);
        final MenuButton transactionColumnsButton = createMenuButton(SPLIT_TRANSACTION, SPLIT_TRANSACTION_ICON);

        final List<CustomMenuItem> columnCheckboxesSource = new ArrayList<>();
        final List<CustomMenuItem> columnCheckboxesDestination = new ArrayList<>();
        final List<CustomMenuItem> columnCheckboxesTransaction = new ArrayList<>();

        // Create the filter items and add them to the button
        final CustomMenuItem columnFilterSource = createColumnFilterMenu(columnCheckboxesSource);
        final CustomMenuItem columnFilterDestination = createColumnFilterMenu(columnCheckboxesDestination);
        final CustomMenuItem columnFilterTransaction = createColumnFilterMenu(columnCheckboxesTransaction);

        sourceVertexColumnsButton.getItems().add(columnFilterSource);
        destinationVertexColumnsButton.getItems().add(columnFilterDestination);
        transactionColumnsButton.getItems().add(columnFilterTransaction);

        // Generate check boxes for each column and separate them into their groups
        table.getColumnIndex().forEach(columnTuple -> {
            final String columnHeading = columnTuple.getAttributeNamePrefix();
            if (columnHeading != null) {
                switch (columnHeading) {
                    case GraphRecordStoreUtilities.SOURCE:
                        columnCheckboxesSource.add(createColumnVisibilityMenu(columnTuple));
                        break;
                    case GraphRecordStoreUtilities.DESTINATION:
                        columnCheckboxesDestination.add(createColumnVisibilityMenu(columnTuple));
                        break;
                    case GraphRecordStoreUtilities.TRANSACTION:
                        columnCheckboxesTransaction.add(createColumnVisibilityMenu(columnTuple));
                        break;
                    default:
                        break;
                }
            }
        });

        // Add the check boxes to the button (which already have the filter added)
        // and add the button to a new menu which can be added to the context menu
        sourceVertexColumnsMenu = createDynamicColumnMenu(sourceVertexColumnsButton, columnCheckboxesSource);
        destinationVertexColumnMenu = createDynamicColumnMenu(destinationVertexColumnsButton, columnCheckboxesDestination);
        tansactionColumnMenu = createDynamicColumnMenu(transactionColumnsButton, columnCheckboxesTransaction);

        Optional.ofNullable(sourceVertexColumnsMenu).ifPresent(menu -> contextMenu.getItems().add(menu));
        Optional.ofNullable(destinationVertexColumnMenu).ifPresent(menu -> contextMenu.getItems().add(menu));
        Optional.ofNullable(tansactionColumnMenu).ifPresent(menu -> contextMenu.getItems().add(menu));
    }

    /**
     * Gets the column visibility context menu.
     *
     * @return the column visibility context menu
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * Gets the menu item that when clicked will show all columns.
     *
     * @return the show all columns menu item
     */
    public CustomMenuItem getShowAllColumnsMenu() {
        return showAllColumnsMenu;
    }

    /**
     * Gets the menu item that when clicked will show the default columns. This
     * means any column with a name that starts with a capital letter. All
     * column names that start with a lowercase letter will be excluded.
     *
     * @return the get default columns menu item
     */
    public CustomMenuItem getShowDefaultColumnsMenu() {
        return showDefaultColumnsMenu;
    }

    /**
     * Gets the menu item that when clicked will show only columns are used to
     * uniquely identify vertices and transactions. This means the "primary"
     * attributes.
     *
     * @return the show only primary columns menu item
     */
    public CustomMenuItem getShowPrimaryColumnsMenu() {
        return showPrimaryColumnsMenu;
    }

    /**
     * Gets the menu item that when clicked will hide all columns in the table.
     *
     * @return the hide all menu item
     */
    public CustomMenuItem getHideAllColumnsMenu() {
        return hideAllColumnsMenu;
    }

    /**
     * Get the menu item that holds check boxes for all source vertex related
     * columns.
     *
     * @return the source vertex columns menu item
     */
    public CustomMenuItem getSourceVertexColumnsMenu() {
        return sourceVertexColumnsMenu;
    }

    /**
     * Get the menu item that holds check boxes for all the destination vertex
     * related columns.
     *
     * @return the destination vertex columns menu item
     */
    public CustomMenuItem getDestinationVertexColumnMenu() {
        return destinationVertexColumnMenu;
    }

    /**
     * Get the menu item that holds check boxes for all the transaction related
     * columns.
     *
     * @return the transaction columns menu item
     */
    public CustomMenuItem getTransactionColumnMenu() {
        return tansactionColumnMenu;
    }

    /**
     * Creates a menu item that wraps a check box representing the current
     * visibility state of the passed column. When the check box is toggled it
     * will modify the visibility of that column as needed. Each check box will
     * also have the column name next to it.
     *
     * @param column the column to create the check box for
     * @return the created menu item wrapping the check box
     */
    protected CustomMenuItem createColumnVisibilityMenu(final Column column) {
        final CheckBox columnCheckbox = new CheckBox(column.getTableColumn().getText());
        columnCheckbox.selectedProperty().bindBidirectional(column.getTableColumn().visibleProperty());

        columnCheckbox.setOnAction(e -> {
            getActiveTableReference().updateVisibleColumns(
                    getTableViewTopComponent().getCurrentGraph(),
                    getTableViewTopComponent().getCurrentState(),
                    extractColumnAttributes(column),
                    ((CheckBox) e.getSource()).isSelected() ? UpdateMethod.ADD : UpdateMethod.REMOVE
            );
            e.consume();
        });

        final CustomMenuItem columnVisibility = new CustomMenuItem(columnCheckbox);
        columnVisibility.setHideOnClick(false);
        columnVisibility.setId(column.getTableColumn().getText());

        return columnVisibility;
    }

    /**
     * Creates a menu item that wraps a filter text field. As the user types in
     * the text field the column names matching the typed text will be made
     * visible and those that do not match the text will be hidden.
     * <p/>
     * Takes a list of menu items wrapping check boxes that represent the
     * columns to be filtered. These menu items will have been created by
     * {@link #createColumnVisibilityMenu(au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple)}.
     * <p/>
     * The filter in this menu item will only apply to those passed columns.
     *
     * @param columnCheckboxes the check boxes representing the columns that
     * this filter menu will filter
     * @return the created filter menu item
     */
    protected CustomMenuItem createColumnFilterMenu(final List<CustomMenuItem> columnCheckboxes) {
        final Label label = new Label(FILTER_CAPTION);
        final TextField textField = new TextField();
        final HBox box = new HBox();

        box.getChildren().addAll(label, textField);

        final CustomMenuItem menuItem = new CustomMenuItem(box);
        menuItem.setHideOnClick(false);

        textField.setOnKeyReleased(
                new ColumnFilterKeyReleasedEventHandler(columnCheckboxes));

        return menuItem;
    }

    /**
     * Convenience method for accessing the active table reference.
     *
     * @return the active table reference
     */
    private ActiveTableReference getActiveTableReference() {
        return table.getParentComponent().getActiveTableReference();
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
     * Takes the first two parts of the {@link ThreeTuple} and places them in a
     * new {@link Tuple}, returning the new {@link Tuple} as a list.
     *
     * @param column the {@link ThreeTuple} to convert
     * @return the generated list containing the new {@link Tuple}
     */
    private List<Tuple<String, Attribute>> extractColumnAttributes(final Column column) {
        return extractColumnAttributes(List.of(column));
    }

    /**
     * Iterates through the columns and takes the first two parts of the
     * {@link ThreeTuple} and places them in a new {@link Tuple}, returning the
     * new {@link Tuple}s as a list.
     *
     * @param columns the {@link ThreeTuple}s to convert
     * @return the generated list of {@link Tuple}s
     */
    private List<Tuple<String, Attribute>> extractColumnAttributes(final List<Column> columns) {
        return columns.stream()
                .map(column
                        -> Tuple.create(
                        column.getAttributeNamePrefix(),
                        column.getAttribute())
                )
                .collect(Collectors.toList());
    }

    /**
     * Creates a dynamic menu that lists columns as check boxes. Which columns
     * are displayed are specified by the passed list. They are added to the
     * passed button and then the button is added to the new menu.
     *
     * @param button the button that will hold the column check boxes and be
     * added to the new menu
     * @param columnCheckboxes a list of check boxes representing columns to be
     * added to the menu
     * @return the created menu item or null if column check boxes is empty
     */
    private CustomMenuItem createDynamicColumnMenu(final MenuButton button,
            final List<CustomMenuItem> columnCheckboxes) {
        if (!columnCheckboxes.isEmpty()) {
            button.getItems().addAll(columnCheckboxes);

            final CustomMenuItem menuItem = new CustomMenuItem(button);
            menuItem.setHideOnClick(false);

            return menuItem;
        }
        return null;
    }

    /**
     * Create a custom menu item that will be added to menu buttons on the
     * context menu. Sets the associated text and adds a listener for when it is
     * clicked.
     *
     * @param title the title to be associated to the menu item
     * @param handler the action handler that will be called when the menu item
     * is clicked
     * @return the created menu item
     */
    private CustomMenuItem createCustomMenu(final String title,
            final EventHandler<ActionEvent> handler) {
        final CustomMenuItem menuItem = new CustomMenuItem(new Label(title));

        menuItem.setHideOnClick(false);
        menuItem.setOnAction(handler);

        return menuItem;
    }

    /**
     * Creates a menu button to be added to the context menu. Sets the icon and
     * max width.
     *
     * @param title the text to be associated with the menu button
     * @param icon the icon to display on the menu button
     * @return the created menu button
     */
    private MenuButton createMenuButton(final String title, final ImageView icon) {
        final MenuButton button = new MenuButton();

        button.setText(title);
        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);

        return button;
    }

    /**
     * A key event handler that deals with a user typing in a column filter
     * field. Based on the filter text, the handler will hide and show the
     * columns it searches across.
     */
    class ColumnFilterKeyReleasedEventHandler implements EventHandler<KeyEvent> {

        private final List<CustomMenuItem> columnCheckboxes;

        /**
         * Creates a new column filter handler.
         *
         * @param columnCheckboxes the column check boxes that will be searched
         * for a match when a user types in the filter text box
         */
        public ColumnFilterKeyReleasedEventHandler(final List<CustomMenuItem> columnCheckboxes) {
            this.columnCheckboxes = columnCheckboxes;
        }

        /**
         * Get the text field that triggered the event and extract the filter
         * text. Then iterate through all the column check boxes associated with
         * the filter and identify any columns that contain the filter text in
         * their name.
         * <p/>
         * If the filter text is found, the column will be made visible,
         * otherwise it will be hidden.
         *
         * @param event the key release event that triggered this handler
         */
        @Override
        public void handle(final KeyEvent event) {
            final String filterTerm = ((TextField) event.getSource()).getText().toLowerCase().trim();
            columnCheckboxes.forEach(item -> {
                final String columnName = item.getId().toLowerCase();
                item.setVisible(filterTerm.isBlank() || columnName.contains(filterTerm));
            });
            event.consume();
        }

    }
}
