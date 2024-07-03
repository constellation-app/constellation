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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterListParameterValue;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A dynamic list of {@link PluginParametersPane} that allow a dynamic list of
 * {@link PluginParameters} objects to be used as the value for a single
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType}.
 * <p>
 * This widget allows {@link PluginParametersPane} (with identical
 * {@link PluginParameters} objects backing them) to be dynamically added,
 * removed, edited, and ordered in a list. Performing any of these operations
 * will result in a corresponding operation on the underlying
 * {@link PluginParameter}.
 *
 * @see
 * au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType
 *
 * @author twilight_sparkle
 */
public class ParameterListInputPane extends BorderPane {

    private final TitledPane parameterTitledPane = new TitledPane();
    private final ScrollPane parameterScrollPane = new ScrollPane();
    private final VBox parameterItemPanes = new VBox();
    private final PluginParameter<ParameterListParameterValue> parameter;
    private Button addItemButton;

    private static final String ODD_PARAM_COLOR = "#404050;";
    private static final String EVEN_PARAM_COLOR = "#303040;";
    private static final String PARAM_COLOR_PROPERTY = "-fx-background-color:";
    private static final Logger LOGGER = Logger.getLogger(ParameterListInputPane.class.getName());

    private static final class ParameterItem extends HBox {

        public final Pane parameterPane;
        public final VBox buttonBar;
        public final Button removeItemButton;
        public final Button moveUpButton;
        public final Button moveDownButton;

        private ParameterItem(final Pane parameterPane) {
            this.parameterPane = parameterPane;
            buttonBar = new VBox();
            buttonBar.setMinWidth(45);
            removeItemButton = new Button(null, new ImageView(UserInterfaceIconProvider.CROSS.buildImage(16)));
            moveUpButton = new Button(null, new ImageView(UserInterfaceIconProvider.CHEVRON_UP.buildImage(16)));
            moveDownButton = new Button(null, new ImageView(UserInterfaceIconProvider.CHEVRON_DOWN.buildImage(16)));
            buttonBar.getChildren().addAll(removeItemButton, moveUpButton, moveDownButton);
            buttonBar.setSpacing(10);
            getChildren().addAll(parameterPane, buttonBar);
            HBox.setHgrow(parameterPane, Priority.ALWAYS);

        }

    }

    private void addParameterItemPaneToList(final ParameterItem parameterItemPane) {
        parameterItemPane.removeItemButton.setOnAction((ActionEvent ee) -> {
            enableParameterListEndpoints(true);
            ParameterListParameterType.removeFromList(parameter, parameterItemPane.parameterPane);
            ObservableList<Node> parameterItemPaneList = parameterItemPanes.getChildren();
            final int oldIndex = parameterItemPaneList.indexOf(parameterItemPane);
            parameterItemPaneList.remove(parameterItemPane);
            for (int i = oldIndex; i < parameterItemPaneList.size(); i++) {
                colorPaneByListPosition(((ParameterItem) parameterItemPaneList.get(i)).parameterPane, i);
            }
            enableParameterListEndpoints(false);
        });
        parameterItemPane.moveDownButton.setOnAction((ActionEvent ee) -> {
            enableParameterListEndpoints(true);
            ParameterListParameterType.moveDown(parameter, parameterItemPane.parameterPane);
            ObservableList<Node> parameterItemPaneList = parameterItemPanes.getChildren();
            final int currentIndex = parameterItemPaneList.indexOf(parameterItemPane);
            parameterItemPaneList.remove(currentIndex);
            parameterItemPaneList.add(currentIndex + 1, parameterItemPane);
            colorPaneByListPosition(((ParameterItem) parameterItemPaneList.get(currentIndex)).parameterPane, currentIndex);
            colorPaneByListPosition(parameterItemPane.parameterPane, currentIndex + 1);
            enableParameterListEndpoints(false);
        });
        parameterItemPane.moveUpButton.setOnAction((ActionEvent ee) -> {
            enableParameterListEndpoints(true);
            ParameterListParameterType.moveUp(parameter, parameterItemPane.parameterPane);
            ObservableList<Node> parameterItemPaneList = parameterItemPanes.getChildren();
            final int currentIndex = parameterItemPaneList.indexOf(parameterItemPane);
            parameterItemPaneList.remove(currentIndex);
            parameterItemPaneList.add(currentIndex - 1, parameterItemPane);
            colorPaneByListPosition(((ParameterItem) parameterItemPaneList.get(currentIndex)).parameterPane, currentIndex);
            colorPaneByListPosition(parameterItemPane.parameterPane, currentIndex - 1);
            enableParameterListEndpoints(false);
        });

        enableParameterListEndpoints(true);
        parameterItemPane.prefWidthProperty().bind(parameterItemPanes.prefWidthProperty());
        parameterItemPanes.getChildren().add(parameterItemPane);
        colorPaneByListPosition(parameterItemPane.parameterPane, parameterItemPanes.getChildren().size() - 1);
        enableParameterListEndpoints(false);
    }

    public ParameterListInputPane(final PluginParameter<ParameterListParameterValue> parameter) {

        this.parameter = parameter;

        Platform.runLater(() -> {
            parameterScrollPane.setContent(parameterItemPanes);
            parameterScrollPane.setPrefViewportHeight(200);
            parameterScrollPane.setPrefHeight(200);
            parameterScrollPane.setPrefWidth(550);
            parameterScrollPane.setMaxHeight(200);
            parameterScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            parameterItemPanes.prefWidthProperty().bind(parameterScrollPane.widthProperty());

            addItemButton = new Button(null, new ImageView(UserInterfaceIconProvider.ADD.buildImage(16)));
            addItemButton.setOnAction((ActionEvent e) -> {
                Pane parameterPane = ParameterListParameterType.addToList(parameter);
                ParameterItem parameterItemPane = new ParameterItem(parameterPane);
                addParameterItemPaneToList(parameterItemPane);
            });

            parameterTitledPane.setContent(parameterScrollPane);
            HBox heading = new HBox();
            heading.setSpacing(20);
            Label title = new Label(parameter.getName() + " : " + parameter.getDescription());
            heading.getChildren().addAll(title, addItemButton);
            parameterTitledPane.setGraphic(heading);
            parameterTitledPane.setCollapsible(false);
            setLeft(parameterTitledPane);
            setMargin(parameterTitledPane, new Insets(5));
        });

        PluginParameterListener ppl = (PluginParameter<?> parameter1, ParameterChange change) -> {
            switch (change) {
                case VALUE -> populateListFromParameterValue();
                case ENABLED -> parameterTitledPane.setDisable(!parameter1.isEnabled());
                case VISIBLE -> {
                    parameterTitledPane.setManaged(parameter1.isVisible());
                    parameterTitledPane.setVisible(parameter1.isVisible());
                }
                default -> LOGGER.log(Level.FINE, "ignoring parameter change type {0}.", change);
            }
        };
        parameter.addListener(ppl);
        populateListFromParameterValue();
    }

    // Used by listeners listening for parameter changes, and for initialisation of the gui from a loaded parameter value
    private void populateListFromParameterValue() {
        Platform.runLater(() -> {
            parameterItemPanes.getChildren().clear();
            List<Pane> parameterPanes = parameter.getParameterListValue().getPanes();
            for (Pane parameterPane : parameterPanes) {
                ParameterItem parameterItemPane = new ParameterItem(parameterPane);
                addParameterItemPaneToList(parameterItemPane);
            }
        });
    }

    private void enableParameterListEndpoints(boolean enabled) {
        if (parameterItemPanes.getChildren().isEmpty()) {
            return;
        }
        ((ParameterItem) parameterItemPanes.getChildren().get(0)).moveUpButton.setDisable(!enabled);
        ((ParameterItem) parameterItemPanes.getChildren().get(parameterItemPanes.getChildren().size() - 1)).moveDownButton.setDisable(!enabled);
    }

    private static void colorPaneByListPosition(final Pane pane, final int pos) {
        final String color = (pos % 2) == 0 ? EVEN_PARAM_COLOR : ODD_PARAM_COLOR;
        pane.setStyle(PARAM_COLOR_PROPERTY + color);
    }

}
