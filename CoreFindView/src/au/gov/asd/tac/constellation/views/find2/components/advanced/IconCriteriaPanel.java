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
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.views.find2.components.AdvancedFindTab;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.IconSelector;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

/**
 * Child criteria BorderPane for the attributes of type Icon.
 *
 * @author Atlas139mkm
 */
public class IconCriteriaPanel extends AdvancedCriteriaBorderPane {

    private ConstellationIcon chosenIcon;

    private final Button openIconsMenuButton = new Button("Select an Icon");
    private IconSelector iconSelctor;
    private Image image;
    private ImageView imageView;
    private Label imageLabel;

    public IconCriteriaPanel(final AdvancedFindTab parentComponent, final String type, final GraphElementType graphElementType) {
        super(parentComponent, type, graphElementType);
        setGridContent();

        openIconsMenuButton.setOnAction(action -> displayIconPicker());
    }

    /**
     * Sets the UI content of the pane
     */
    private void setGridContent() {
        // Set the default icon to the constellation icon
        chosenIcon = IconManager.getIcon("Constellation_Application_Icon");
        // Create an image of the icon to be displayed
        image = chosenIcon.buildImage(50);
        imageView = new ImageView(image);
        imageLabel = new Label(chosenIcon.getName());
        // create a button with the icons name and set its settings
        openIconsMenuButton.setText(chosenIcon.getName());
        openIconsMenuButton.setTextAlignment(TextAlignment.CENTER);
        openIconsMenuButton.setTextOverrun(OverrunStyle.ELLIPSIS);
        openIconsMenuButton.setMinWidth(194);
        openIconsMenuButton.setMaxWidth(194);
        openIconsMenuButton.setAlignment(Pos.CENTER);
        setCenter(imageView);
        getHboxBot().getChildren().addAll(openIconsMenuButton);

    }

    /**
     * Displays the Icon selector when called
     */
    private void displayIconPicker() {
        iconSelctor = new IconSelector(this);
        iconSelctor.showAndWait();
    }

    /**
     * Gets the selected icon from the icon selector and refreshes the criteria
     * panel with the newly selected icon
     */
    public void getSelectedIcon() {
        chosenIcon = iconSelctor.selectIcon();
        refreshImage(chosenIcon);
    }

    /**
     * Takes an Icon and sets the imagine on the criteria pane to that icon
     *
     * @param icon
     */
    private void refreshImage(ConstellationIcon icon) {
        image = icon.buildImage(50);
        imageView.setImage(image);
        openIconsMenuButton.setText(icon.getName());
    }

    @Override
    public String getType() {
        return "icon"; //To change body of generated methods, choose Tools | Templates.
    }

}
