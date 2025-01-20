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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;

/**
 * An extension of a ContextMenu to provide features that enable its use as a
 * drop down menu in ConstellationInputFields. Do not add items using getItems()
 *
 * @author capricornunicorn123
 */
public class ConstellationInputDropDown extends ContextMenu {

    final ConstellationInput parent;

    public ConstellationInputDropDown(final ConstellationInput field) {
        parent = field;

        //Constrain drop down menus to a height of 200
        this.setMaxHeight(200);
        this.setPrefHeight(200);
        addEventHandler(Menu.ON_SHOWING, e -> {
            Node content = getSkin().getNode();
            if (content instanceof Region region) {
                region.setMaxHeight(getMaxHeight());
                // set the drop down context menu to parent width
                region.setMaxWidth(field.getWidth());
                // set the arrow bar transparent
                content.lookupAll(".scroll-arrow").forEach(
                        bar -> bar.setStyle("-fx-background-color: transparent;"));
                if (JavafxStyleManager.isDarkTheme()) {
                    Node up = content.lookup(".menu-up-arrow");
                    Node down = content.lookup(".menu-down-arrow");
                    up.setStyle("-fx-background-color: #e0e0e0;");
                    down.setStyle("-fx-background-color: #e0e0e0");
                }
            }
        });
    }

    /**
     * Takes a {@link Labeled} object and converts it to a {@link MenuItem}.
     *
     * The {@link MenuItem} is not added to the ConstextMenu. it is returned for
     * Input field specific modification before being added to the Context menu
     * using addMenuItems();
     *
     * This build method is important to correctly bind the width of the context
     * menu to the parent (but it currently doesn't work as they are both read
     * only)....
     *
     * @param text
     * @return
     */
    public CustomMenuItem registerCustomMenuItem(final Labeled text) {
        text.prefWidthProperty().bind(parent.widthProperty());
        CustomMenuItem item = new CustomMenuItem(text);
        getItems().add(item);
        return item;
    }
}
