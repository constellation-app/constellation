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
package au.gov.asd.tac.constellation.views.mapview2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.CheckComboBox;

/**
 *
 * @author altair1673
 */
public class OptionChoiceBox<E> extends CheckComboBox {

    private final Logger LOGGER = Logger.getAnonymousLogger("Test");
    private final Map<MenuItem, E> menuItems;

    private boolean singleChoice = false;

    public OptionChoiceBox(final List<E> options, final boolean singleChoice) {
        super();
        menuItems = new HashMap<>();
        this.singleChoice = singleChoice;


        options.forEach(o -> {
            MenuItem menuItem = new MenuItem(o.toString());
            menuItems.put(menuItem, o);

            getItems().add(menuItem);

        });

    }

    /*public OptionChoiceBox(final List<E> options, String title, final boolean singleChoice)
    {
        OptionChoiceBox(options, singleChoice);
        setTitle(title);
    }*/

    private void onMenuItemClicked(MenuItem selectedMenuItem) {
        LOGGER.log(Level.SEVERE, selectedMenuItem.getText());
    }

}
