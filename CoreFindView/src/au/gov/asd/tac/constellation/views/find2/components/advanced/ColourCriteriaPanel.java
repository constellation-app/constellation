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
package au.gov.asd.tac.constellation.views.find2.components.advanced;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColourCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
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
public class ColourCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final ColorPicker colourPicker = new ColorPicker();
    private final Rectangle colourRectangle = new Rectangle();
    private ComboBox<ConstellationColor> colorComboPicker;

    public ColourCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);

        /**
         * This creates a list of all the named constellation colours
         */
        final ObservableList<ConstellationColor> namedColors = FXCollections.observableArrayList();
        for (final ConstellationColor c : ConstellationColor.NAMED_COLOR_LIST) {
            namedColors.add(c);
        }
        colorComboPicker = new ComboBox<>(namedColors);

        /**
         * This listener updates the colour combo picker with the colour
         * selected in the colour picker.
         */
        colourPicker.valueProperty().addListener((o, oldValue, newValue) -> {
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
         * This takes all the colours within the named colours and updates the
         * item to contain a small rectangle representing of the said colour.
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
         * This listener updates the colour picker with the colour selected in
         * the colorComboPicker.
         */
        colorComboPicker.valueProperty().addListener((o, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                colourPicker.setValue(newValue.getJavaFXColor());
            }
        });

        // set action so that the rectangle changes to the selected colour
        colourPicker.setOnAction(action -> colourRectangle.setFill(colourPicker.getValue()));
        colorComboPicker.setOnAction(action -> colourRectangle.setFill(colourPicker.getValue()));

        setGridContent();

    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        // Default select the color blue for the color picker
        colourPicker.setValue(Color.BLUE);

        // set the sizes for a larger rectangle which mimics the color of the
        // color picker and set its size.
        colourRectangle.setWidth(50);
        colourRectangle.setHeight(50);
        colourRectangle.setFill(colourPicker.getValue());

        getHboxBot().getChildren().addAll(colourPicker, colorComboPicker);
        setCenter(colourRectangle);
    }

    /**
     * This returns a FindCriteriaValue, specifically a ColorCriteriaValue
     * containing this panes selections and the current constellationn colour
     * selected.
     *
     * @return
     */
    @Override
    public FindCriteriaValues getCriteriaValues() {
        final float red = (float) colourPicker.getValue().getRed();
        final float green = (float) colourPicker.getValue().getGreen();
        final float blue = (float) colourPicker.getValue().getBlue();
        final float opacity = (float) colourPicker.getValue().getOpacity();
        return new ColourCriteriaValues(getType(), getAttributeName(), getFilterChoiceBox().getSelectionModel().getSelectedItem(),
                ConstellationColor.getColorValue(red, green, blue, opacity));

    }

    /**
     * Gets the type of the panel
     *
     * @return
     */
    @Override
    public String getType() {
        return ColorAttributeDescription.ATTRIBUTE_NAME;
    }
}
