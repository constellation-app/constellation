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
package au.gov.asd.tac.constellation.functionality.dialog;

import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.ClipboardUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Dimension;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Display a dialog containing a table containing 2 columns of ItemsRow
 * <p>
 * The dialog will have the following buttons of which the "OK" button can be
 * overridden:
 * <ul>
 * <li>OK</li>
 * <li>Cancel</li>
 * <li>Copy Selection to Clipboard</li>
 * <li>Select All</li>
 * </ul>
 *
 * @param <T> the type of the elements in the dialog.
 *
 * @author sirius
 */
public class ItemsDialog<T> extends ConstellationDialog {
    
    private static final Logger LOGGER = Logger.getLogger(ItemsDialog.class.getName());

    private TableView<ItemsRow<T>> table;
    private final Button okButton;
    private final Button cancelButton;

    public ItemsDialog(final Window owner, final String title, final String helpText, final String labelColumnHeading, final String descriptionColumnHeading, ObservableList<ItemsRow<T>> rows) {
        final BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #DDDDDD;-fx-border-color: #3a3e43;-fx-border-width: 4px;");

        // add a title
        final Label titleLabel = new Label();
        titleLabel.setText(title);
        titleLabel.setStyle("-fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPadding(new Insets(5));
        root.setTop(titleLabel);

        // add a help icon and message
        final Label helpMessage = new Label();
        helpMessage.setText(helpText);
        helpMessage.setStyle("-fx-font-size: 11pt;");
        helpMessage.setWrapText(true);
        helpMessage.setPadding(new Insets(5));

        final HBox help = new HBox();
        help.getChildren().add(helpMessage);
        help.getChildren().add(new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.AZURE.getJavaColor())));
        help.setPadding(new Insets(10, 0, 10, 0));

        // table
        final TableColumn<ItemsRow<T>, String> labelColumn = new TableColumn<>(labelColumnHeading);
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));

        final TableColumn<ItemsRow<T>, String> descriptionColumn = new TableColumn<>(descriptionColumnHeading);
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        table = new TableView<>();
        table.getColumns().addAll(labelColumn, descriptionColumn);
        table.setItems(rows);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().clearSelection();
        table.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change<? extends Integer> c) -> {
            try {
                List<ItemsRow<T>> selectedRows = table.getSelectionModel().getSelectedItems();
                selectRows(selectedRows);
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, "Thread was interrupted");
                Thread.currentThread().interrupt();
            }
        });

        final VBox box = new VBox();
        if (helpText.isEmpty()) {
            box.getChildren().add(help);
        }
        box.getChildren().add(table);
        root.setCenter(box);

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5));
        buttonPane.setHgap(5);
        root.setBottom(buttonPane);

        final Button copyToClipboardButton = new Button("Copy Selection to Clipboard");
        copyToClipboardButton.setOnAction((ActionEvent event) -> {
            final StringBuilder sb = new StringBuilder();

            final ObservableList<ItemsRow<T>> selectedRows = table.getSelectionModel().getSelectedItems();
            for (ItemsRow<?> r : selectedRows) {
                sb.append(String.format("%s,%s\n", r.labelProperty().getValue(), r.descriptionProperty().getValue()));
            }

            ClipboardUtilities.copyToClipboard(sb.toString());
        });
        buttonPane.getChildren().add(copyToClipboardButton);

        final Button selectAllButton = new Button("Select All");
        selectAllButton.setOnAction((ActionEvent event) -> table.getSelectionModel().selectAll());
        buttonPane.getChildren().add(selectAllButton);

        okButton = new Button("Continue");
        buttonPane.getChildren().add(okButton);

        cancelButton = new Button("Cancel");
        cancelButton.setOnAction((ActionEvent event) -> hideDialog());
        buttonPane.getChildren().add(cancelButton);

        final Scene scene = new Scene(root);
        fxPanel.setScene(scene);
        fxPanel.setPreferredSize(new Dimension(500, 500));
    }

    protected void selectRows(List<ItemsRow<T>> rows) throws InterruptedException {
        // Method overriden LeadNodeSelectionDialog
    }

    public void setOkButtonAction(EventHandler<ActionEvent> event) {
        okButton.setOnAction(event);
    }

    public void setCancelButtonAction(EventHandler<ActionEvent> event) {
        cancelButton.setOnAction(event);
    }
}
