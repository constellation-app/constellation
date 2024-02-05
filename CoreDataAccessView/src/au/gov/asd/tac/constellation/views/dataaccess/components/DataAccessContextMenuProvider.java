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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import java.util.List;
import javafx.scene.control.Tab;
import javax.swing.ImageIcon;

/**
 * An interface to provide contributions to the context menu in the renderer.
 * <p>
 * The user can right-click on something in the graph and expect to see a pop-up
 * menu. The renderer uses a Lookup on this provider to find plugins to build
 * the menu with.
 *
 * @author capricornunicorn123
 */
public interface DataAccessContextMenuProvider {

    /**
     * Returns a list of the sub-menus in which to place the items from this
     * provider. If all or part of this sub-menu sequence already exists in the
     * context menu then the items will be merged into the already existing
     * sub-menus. If the items should be placed directly into the context menu
     * then null or an empty list can be returned.
     *
     *
     * @return a list of the sub-menus for the items.
     */
    public List<String> getMenuPath();

    /**
     * Returns a list of items that should be placed into the context menu. The
     * items should all be unique as it is these values that will be provided to
     * the selectItem method if the user clicks on an item.
     *
     * @return a list of items to be placed into the context menu.
     */
    public List<String> getItems();

    /**
     * Returns a list of icons to use for menu items provided in getItems
     * method. For context menus wanting to add icons, override this default
     * method and return list of ImageIcon objects to be displayed. a value of
     * null assigned to a given index will result in no icon being displayed for
     * that item.
     *
     * @return a list of icons to be placed into the context menu aligned to
     * items provided by getItems.
     */
    default public List<ImageIcon> getIcons() {
        return null;
    }

    /**
     * This method is called when a user selects an item in the context menu.
     * The returned item will be from the list provided by the getItems method.
     *
     * @param item the item that has been selected.
     * occurred.
     */
    public void selectItem(final DataAccessTabPane  pane , final Tab tab);
}
