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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author altair1673
 */
public class MenuButtonCheckCombobox {

    final MenuButton menuButton = new MenuButton();
    final Map<String, CheckBox> optionMap = new HashMap<>();
    final BooleanProperty itemClicked = new SimpleBooleanProperty(false);
    private static final Logger LOGGER = Logger.getLogger(MenuButtonCheckCombobox.class.getName());
    private boolean isSingleChoice = false;
    private boolean hideOnClick = false;

    public MenuButtonCheckCombobox(final List<String> options, final boolean isSingleChoice, final boolean hideOnClick) {
        this.isSingleChoice = isSingleChoice;
        this.hideOnClick = hideOnClick;
        options.forEach(option -> {
            
            final CheckBox optionCB = new CheckBox(option);

            optionCB.setOnAction(event -> {

                if (this.isSingleChoice) {
                    optionCB.setSelected(true);

                    optionMap.keySet().forEach(key -> {
                        if (optionMap.get(key).isSelected() && !key.equals(option)) {
                            optionMap.get(key).setSelected(false);
                        }
                    });
                }

                itemClicked.set(!itemClicked.get());
            });

            if (!optionMap.containsKey(option)) {
                optionMap.put(option, optionCB);
            } else {
                optionMap.replace(option, optionCB);
            }

            final CustomMenuItem mi = new CustomMenuItem(optionCB);
            mi.setHideOnClick(this.hideOnClick);
            menuButton.getItems().add(mi);
        });
    }

    public void selectItem(final String key) {
        if (optionMap.containsKey(key)) {
            optionMap.get(key).setSelected(true);
        }
    }

    public MenuButton getMenuButton() {
        return menuButton;
    }

    public Map<String, CheckBox> getOptionMap() {
        return optionMap;
    }

    public BooleanProperty getItemClicked() {
        return itemClicked;
    }

    public void setIcon(final String path) {
        final Image img = new Image(path);
        final ImageView iconView = new ImageView(img);
        menuButton.setGraphic(iconView);
    }
}
