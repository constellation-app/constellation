/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/**
 * The consolidated dialog is similar to {@link MultipleMatchesDialog}, however
 * can be presented to the user once, avoiding displaying separate dialogs
 * repeatedly. Objects are grouped using accordion panes, with each pane
 * displaying options.
 *
 * @author arcturus
 * @param <K>
 * @param <V>
 */
public class ConsolidatedDialog<K, V> extends ConstellationDialog {

    /**
     * An interface that must be implemented which is a type of the object
     * passed into the {@link ObservableList}.
     *
     * @param <K> The type of the item key
     * @param <V> The type of the item value
     */
    public interface Container<K, V> {

        public K getKey();

        public V getValue();
    }

    private final List<Pair<K, V>> selectedObjects = new ArrayList<>();

    private final Label helpMessage = new Label();
    private final Button useButton;

    /**
     * A generic multiple matches dialog which can be used to display a
     * collection of ObservableList items
     *
     * @param title The title of the dialog
     * @param observableMap The map of {@code ObservableList} objects
     * @param message The (help) message that will be displayed at the top of
     * the dialog window
     * @param listItemHeight The height of each list item. If you have a single
     * row of text then set this to 24.
     */
    public ConsolidatedDialog(String title, Map<String, ObservableList<Container<K, V>>> observableMap, String message, int listItemHeight) {
        final BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #DDDDDD;-fx-border-color: #3a3e43;-fx-border-width: 4px;");

        // add a title
        final Label titleLabel = new Label();
        titleLabel.setText(title);
        titleLabel.setStyle("-fx-font-size: 11pt;-fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPadding(new Insets(5));
        root.setTop(titleLabel);

        final Accordion accordion = new Accordion();
        for (String identifier : observableMap.keySet()) {
            final ObservableList<Container<K, V>> objects = observableMap.get(identifier);
            ListView<Container<K, V>> listView = new ListView<>(objects);
            listView.setEditable(false);
            listView.setPrefHeight((listItemHeight * objects.size()));
            listView.getSelectionModel().selectedItemProperty().addListener(event -> {
                listView.getItems().get(0).getKey();
                final Container<K, V> container = listView.getSelectionModel().getSelectedItem();
                if (container != null) {
                    selectedObjects.add(new Pair<>(container.getKey(), container.getValue()));
                } else {
                    // the object was unselected so go through the selected objects and remove them all because we don't know which one was unselected
                    for (Container<K, V> object : listView.getItems()) {
                        selectedObjects.remove(new Pair<>(object.getKey(), object.getValue()));
                    }
                }
            });

            final TitledPane titledPane = new TitledPane(identifier, listView);
            accordion.getPanes().add(titledPane);
        }

        final HBox help = new HBox();
        help.getChildren().add(helpMessage);
        help.getChildren().add(new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.AZURE.getJavaColor())));
        help.setPadding(new Insets(10, 0, 10, 0));
        helpMessage.setText(message + "\n\nNote: To deselect hold Ctrl when you click.");
        helpMessage.setStyle("-fx-font-size: 11pt;");
        helpMessage.setWrapText(true);
        helpMessage.setPadding(new Insets(5));

        final VBox box = new VBox();
        box.getChildren().add(help);

        // add the multiple matching accordion
        box.getChildren().add(accordion);

        final ScrollPane scroll = new ScrollPane();
        scroll.setContent(box);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(scroll);

        final FlowPane buttonPane = new FlowPane();
        buttonPane.setAlignment(Pos.BOTTOM_RIGHT);
        buttonPane.setPadding(new Insets(5));
        buttonPane.setHgap(5);
        root.setBottom(buttonPane);

        useButton = new Button("Continue");
        buttonPane.getChildren().add(useButton);
        accordion.expandedPaneProperty().set(null);

        final Scene scene = new Scene(root);
        fxPanel.setScene(scene);
        fxPanel.setPreferredSize(new Dimension(500, 500));
    }

    public List<Pair<K, V>> getSelectedObjects() {
        return selectedObjects;
    }

    public void setUseButtonAction(EventHandler<ActionEvent> event) {
        useButton.setOnAction(event);
    }
}
