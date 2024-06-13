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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import javafx.scene.Group;

/**
 * Parent class to all layers
 *
 * @author altair1673
 */
public abstract class AbstractMapLayer {

    protected MapView parent;
    protected Graph currentGraph = null;
    protected boolean isShowing = false;

    protected int id;

    protected AbstractMapLayer(final MapView parent, final int id) {
        this.parent = parent;
        currentGraph = GraphManager.getDefault().getActiveGraph();
        this.id = id;
    }

    public void setUp() {
    }

    public boolean isShowing() {
        return isShowing;
    }

    public int getId() {
        return id;
    }

    public void setIsShowing(final boolean showing) {
        isShowing = showing;
    }

    public Group getLayer() {
        return null;
    }


}
