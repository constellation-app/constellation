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
package au.gov.asd.tac.constellation.graph.visual.contextmenu;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * An interface to provide contributions to the context menu in the renderer.
 * <p>
 * The user can right-click on something in the graph and expect to see a pop-up
 * menu. The renderer uses a Lookup on this provider to find plugins to build
 * the menu with.
 *
 * @author sirius
 */
public interface ContextMenuProvider {

    /**
     * Returns a list of the sub-menus in which to place the items from this
     * provider. If all or part of this sub-menu sequence already exists in the
     * context menu then the items will be merged into the already existing
     * sub-menus. If the items should be placed directly into the context menu
     * then null or an empty list can be returned.
     *
     * @param elementType the type of element that has been clicked on.
     *
     * @return a list of the sub-menus for the items.
     */
    public List<String> getMenuPath(final GraphElementType elementType);

    /**
     * Returns a list of items that should be placed into the context menu. The
     * items should all be unique as it is these values that will be provided to
     * the selectItem method if the user clicks on an item.
     *
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @return a list of items to be placed into the context menu.
     */
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int elementId);

    /**
     * Returns a list of icons to use for menu items provided in getItems
     * method. For context menus wanting to add icons, override this default
     * method and return list of ImageIcon objects to be displayed. a value of
     * null assigned to a given index will result in no icon being displayed for
     * that item.
     *
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @return a list of icons to be placed into the context menu aligned to
     * items provided by getItems.
     */
    public default List<ImageIcon> getIcons(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        return null;
    }

    /**
     * This method is called when a user selects an item in the context menu.
     * The returned item will be from the list provided by the getItems method.
     *
     * @param item the item that has been selected.
     * @param graph the graph that has been right-clicked on.
     * @param elementType the type of element that has been right-clicked on.
     * @param elementId the id of the element that has been right-clicked on.
     * @param unprojected the unprojected location of the mouse where the click
     * occurred.
     */
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected);
}
