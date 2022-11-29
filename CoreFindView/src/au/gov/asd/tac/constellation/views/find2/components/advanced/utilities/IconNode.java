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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/**
 * This class contains the formatting used within the IconSelector for each of
 * the available icons.
 *
 * @author Atlas139mkm
 */
public class IconNode extends BorderPane {

    private final ImageView img = new ImageView();

    private ConstellationIcon icon;
    private String iconName;

    public IconNode(final ConstellationIcon icon) {
        this.icon = icon;
        this.iconName = icon.getName();

        setContent();
    }

    /**
     * Sets the UI content of the Icon Node
     */
    private void setContent() {
        setMinSize(100, 50);
        img.setImage(icon.buildImage(50));
        setLeft(img);
        setCenter(new Label(iconName));
    }

    /**
     * Gets the icon
     *
     * @return icon
     */
    public ConstellationIcon getIcon() {
        return icon;
    }

}
