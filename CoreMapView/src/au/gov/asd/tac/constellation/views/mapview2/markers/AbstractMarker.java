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

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

/**
 *
 * @author altair1673
 */
public abstract class AbstractMarker {

    protected final SVGPath markerPath;
    protected int markerID = 0;
    protected List<Integer> idList = new ArrayList();
    protected boolean isSelected = false;

    protected int xOffset;
    protected int yOffset;

    protected MapView parent;

    public static enum MarkerType {
        POINT_MARKER,
        LINE_MARKER,
        POLYGON_MARKER,
        CLUSTER_MARKER
    }

    protected MarkerType type;

    public AbstractMarker(MapView parent, int markerID, int nodeId, int xOffset, int yOffset, MarkerType type) {
        this.markerID = markerID;
        this.parent = parent;
        idList.add(nodeId);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.type = type;

        markerPath = new SVGPath();
    }

    public int getWeight() {
        return idList.size();
    }

    public MarkerType getType() {
        return type;
    }

    public void addNodeID(int id) {
        idList.add(id);
    }

    public List<Integer> getIdList() {
        return idList;
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

    protected double XToLong(double x, double minLong, double mapWidth, double lonDelta) {
        double longitude = (x / (mapWidth / lonDelta)) + minLong;
        return longitude;
    }

    protected double YToLat(double y, double mapWidth, double mapHeight) {

        y = ((-y + (mapHeight / 2)) * (2 * Math.PI)) / mapWidth;
        y = (Math.atan(Math.exp(y)) - (Math.PI / 4)) * 2;
        double lattitude = y / (Math.PI / 180);
        return lattitude;
    }

    public void setMarkerPosition(double mapWidth, double mapHeight) {

    }

    public Shape getMarker() {
        return markerPath;
    }

    public int getMarkerId() {
        return markerID;
    }


}
