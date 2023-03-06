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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import javafx.scene.Group;

/**
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
        currentGraph = parent.getCurrentGraph();
        this.id = id;
    }

    // Class to hold a location in lattitude and longitude and its corresponding x and y values
    protected class Location {

        private double lat;
        private double lon;

        private double x = 0;
        private double y = 0;

        public Location(final double lat, final double lon) {
            this.lat = lat;
            this.lon = lon;

            x = lon;
            y = lat;

        }

        public double getLat() {
            return lat;
        }

        public void setLat(final double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(final double lon) {
            this.lon = lon;
        }

        public double getX() {
            return x;
        }

        public void setX(final double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(final double y) {
            this.y = y;
        }
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
