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
package au.gov.asd.tac.constellation.views.mapview2.markers;

import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author altair1673
 */
public abstract class AbstractMarker {

    protected int id;
    protected boolean isSelected = false;

    protected int xOffset;
    protected int yOffset;

    protected MapViewTopComponent parentComponent;

    public AbstractMarker(MapViewTopComponent parentComponent, int id, int xOffset, int yOffset) {
        this.parentComponent = parentComponent;
        this.id = id;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public int getID() {
        return id;
    }

    protected double longToX(double longitude, double minLong, double mapWidth, double lonDelta) {
        return (longitude - minLong) * (mapWidth / lonDelta);
    }

    protected double latToY(double lattitude, double mapWidth, double mapHeight) {
        lattitude = lattitude * (Math.PI / 180);
        double y = Math.log(Math.tan((Math.PI / 4) + (lattitude / 2)));
        y = (mapHeight / 2) - (mapWidth * y / (2 * Math.PI));

        return y;
    }

    public void setMarkerPosition(double mapWidth, double mapHeight) {

    }

    public SVGPath getMarker() {
        return null;
    }
}
