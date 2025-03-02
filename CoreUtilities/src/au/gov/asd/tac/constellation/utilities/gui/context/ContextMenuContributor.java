/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.context;

import java.util.List;
import javafx.scene.control.MenuItem;

/**
 * A Context Menu Contributor is a way for objects to provide {@link MenuItem}
 * to a {@link ContextMenu} without managing the {@link ContextMenu} themselves.
 * this enables classes that have access to child classes that implement this
 * interface to request contributions and construct a {@link ContextMenu} that
 * has representative actions from a variety of different child classes.
 *
 * This feature has been designed for use in {@link ConstellationInputField} but
 * may have future benefit to UI elements in the view framework.
 *
 * @author capricornunicorn123
 */
public interface ContextMenuContributor {

    /**
     * Should return a list of {@link MenuItem} for a {@link ContextMenu} that
     * pertain to this class exclusively. This method should not return items
     * that my pertain to a sub class, a super class or child class.
     *
     * @return
     */
    public List<MenuItem> getLocalMenuItems();

    /**
     * Should return a list of {@link MenuItem} for a {@link ContextMenu} that
     * pertain to this class and all classes that are within this classes
     * control. If you were to imagine a tree of contributors, this method would
     * return items of this node and all nodes below it.
     *
     * @return
     */
    public List<MenuItem> getAllMenuItems();

}
