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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * A pane which displays a heading and a collapsible list of plug-in panes.
 *
 * @author ruby_crucis
 */
public class HeadingPane extends TitledPane implements PluginParametersPaneListener {

    private final List<DataSourceTitledPane> dataSources = new ArrayList<>();
    private final PluginParametersPaneListener top;

    private static final Color GREEN = Color.web("#5E9656");
    private static final Color GREY = Color.web("#e6e6e6").deriveColor(1, 1, 1, 0.3);
    private static final double SQUARE_SIZE = 14; // Smaller makes it obvious that they are not center aligned

    private final FlowPane boxes = new FlowPane();

    /**
     * Primary constructor.
     *
     * @param headingText Text to display
     * @param plugins Plug-ins to include
     * @param top a component (e.g. the parent in the layout tree) which should
     * be informed when plug-ins are selected or deselected.
     * @param globalParamLabels the labels of the global parameters.
     */
    public HeadingPane(String headingText, List<DataAccessPlugin> plugins, PluginParametersPaneListener top, final Set<String> globalParamLabels) {
        this.top = top;
        boxes.setHgap(5);
        plugins.stream().forEach((_item) -> {
            boxes.getChildren().add(makeInactiveSquare());
        });
        setText(headingText);
        setGraphic(boxes);
        setContentDisplay(ContentDisplay.RIGHT);
        setGraphicTextGap(5);
        setCollapsible(true);
        setExpanded(DataAccessPreferences.isExpanded(headingText, true));
        getStyleClass().add("titled-pane-heading");
        VBox sources = new VBox();
        sources.setPadding(Insets.EMPTY);
        setContent(sources);
        for (DataAccessPlugin plugin : plugins) {
            final DataSourceTitledPane dataSourcePane = new DataSourceTitledPane(plugin, null, this, globalParamLabels);
            dataSources.add(dataSourcePane);
            sources.getChildren().add(dataSourcePane);
        }

        expandedProperty().addListener((ChangeListener) (final ObservableValue observable, final Object oldValue, final Object newValue) -> {
            DataAccessPreferences.setExpanded(headingText, (boolean) newValue);
        });
    }

    public List<DataSourceTitledPane> getDataSources() {
        return dataSources;
    }

    @Override
    public void validityChanged(boolean enabled) {
        top.hierarchicalUpdate();

        // If set because a user is editing, it's already expanded so it doesn't matter.
        // If set otherwise (loading a template, for example), convenient.
        if (enabled) {
            setExpanded(true);
        }
    }

    @Override
    public void hierarchicalUpdate() {
        int count = 0;
        for (DataSourceTitledPane pane : dataSources) {
            if (pane.isQueryEnabled()) {
                count++;
            }
        }
        boxes.getChildren().clear();
        for (int i = 0; i < count; i++) {
            boxes.getChildren().add(makeActiveSquare());
        }
        for (int i = 0; i < dataSources.size() - count; i++) {
            boxes.getChildren().add(makeInactiveSquare());

        }
        top.hierarchicalUpdate();
    }

    /**
     * Make a square to indicate that a plug-in is checked
     *
     * @return
     */
    private Shape makeActiveSquare() {
        return makeSquare(GREEN, Color.web("#1a4d1a")); // Color.web("#336633")
    }

    /**
     * Make a ghost square to indicate how many plug-ins are not checked.
     *
     * @return
     */
    private Shape makeInactiveSquare() {
        return makeSquare(GREY, GREY);
    }

    private Shape makeSquare(Color colour, Color border) {
        Stop[] stops = new Stop[]{new Stop(0, colour), new Stop(0.95, colour.deriveColor(1, 1, .75, 1)), new Stop(1.0, colour.deriveColor(1, 1, 0.5, 1))};
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
        square.setStroke(border);
        square.setFill(gradient);
        return square;
    }
}
