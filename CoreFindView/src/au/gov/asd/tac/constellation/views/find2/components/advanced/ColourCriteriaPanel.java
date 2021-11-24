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
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Child criteria BorderPane for the attributes of type color.
 *
 * @author Atlas139mkm
 */
public class ColourCriteriaPanel extends AdvancedCriteriaBorderPane {

    private final ColorPicker colourPicker = new ColorPicker();
    private final Rectangle colourRectangle = new Rectangle();

    public ColourCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();

        colourPicker.setOnAction(action -> colourRectangle.setFill(colourPicker.getValue()));
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        // Default select the color blue for the color picker
        colourPicker.setValue(Color.BLUE);
        colourPicker.setMinWidth(194);
        getHboxBot().getChildren().add(colourPicker);

        // set the sizes for a larger rectangle which mimics the color of the
        // color picker and set its size.
        colourRectangle.setWidth(50);
        colourRectangle.setHeight(50);
        colourRectangle.setFill(colourPicker.getValue());
        setCenter(colourRectangle);
    }

    @Override
    public String getType() {
        return "colour"; //To change body of generated methods, choose Tools | Templates.
    }
}
