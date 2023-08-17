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
package au.gov.asd.tac.constellation.views.find.components.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.find.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FindCriteriaValues;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

/**
 * Child criteria BorderPane for the attributes of type color.
 *
 * @author Atlas139mkm
 */
public class ColorCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final ColorPicker colorPicker = new ColorPicker();
    private final Rectangle colorRectangle = new Rectangle();
    private ComboBox<ConstellationColor> colorComboPicker;

    public ColorCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);

        /**
         * This creates a list of all the named constellation colors
         */
        final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList();
        for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
            namedColors.add(c);
        }
        colorComboPicker = new ComboBox<>(namedColors);

        /**
         * This listener updates the color combo picker with the color
         * selected in the color picker.
         *
         * NOTE - This is taken from the attribute editor for the color picker
         */
        colorPicker.valueProperty().addListener((o, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                boolean foundNamedColor = false;
                for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
                    final Color fxc = c.getJavaFXColor();
                    if (newValue.equals(fxc)) {
                        colorComboPicker.setValue(c);
                        foundNamedColor = true;
                        break;
                    }
                }
                if (!foundNamedColor) {
                    colorComboPicker.setValue(null);
                }
            }
        });

        /**
         * This takes all the colors within the named colors and updates the
         * item to contain a small rectangle representing of the said color.
         */
        final Callback<ListView<ConstellationColor>, ListCell<ConstellationColor>> cellFactory = (final ListView<ConstellationColor> p) -> new ListCell<ConstellationColor>() {
            @Override
            protected void updateItem(final ConstellationColor item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    final Rectangle r = new Rectangle(12, 12, item.getJavaFXColor());
                    r.setStroke(Color.BLACK);
                    setText(item.getName());
                    setGraphic(r);
                }
            }
        };
        colorComboPicker.setCellFactory(cellFactory);
        colorComboPicker.setButtonCell(cellFactory.call(null));

        /**
         * This listener updates the color picker with the color selected in
         * the colorComboPicker.
         */
        colorComboPicker.valueProperty().addListener((o, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                colorPicker.setValue(newValue.getJavaFXColor());
            }
        });

        // set action so that the rectangle changes to the selected color
        colorPicker.setOnAction(action -> colorRectangle.setFill(colorPicker.getValue()));
        colorComboPicker.setOnAction(action -> colorRectangle.setFill(colorPicker.getValue()));

        setGridContent();

    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        // Default select the color blue for the color picker
        colorPicker.setValue(Color.BLUE);

        // set the sizes for a larger rectangle which mimics the color of the
        // color picker and set its size.
        colorRectangle.setWidth(50);
        colorRectangle.setHeight(50);
        colorRectangle.setFill(colorPicker.getValue());

        getHboxBot().getChildren().addAll(colorPicker, colorComboPicker);
        setCenter(colorRectangle);
    }

    /**
     * This returns a FindCriteriaValue, specifically a ColorCriteriaValue
     * containing this panes selections and the current constellationn color
     * selected.
     *
     * @return
     */
    @Override
    public FindCriteriaValues getCriteriaValues() {
        final float red = (float) colorPicker.getValue().getRed();
        final float green = (float) colorPicker.getValue().getGreen();
        final float blue = (float) colorPicker.getValue().getBlue();
        final float opacity = (float) colorPicker.getValue().getOpacity();
        return new ColorCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(),
                ConstellationColor.getColorValue(red, green, blue, opacity));

    }

    /**
     * Overrides the parents getType function to return the correct type name
     * being "color"
     *
     * @return
     */
    @Override
    public String getType() {
        return ColorAttributeDescription.ATTRIBUTE_NAME;
    }
}
