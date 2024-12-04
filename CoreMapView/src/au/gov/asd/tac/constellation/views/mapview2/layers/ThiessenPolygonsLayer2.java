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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.ArcTree;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.SiteEvent;
import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.VoronoiEvent;

import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javafx.scene.Group;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * More efficient away of calculating closest zones but will implement in the
 * future
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer2 extends AbstractMapLayer {
    private final Group layer;
    private final Group arcLayer;
    private final Map<Integer, AbstractMarker> nodesOnScreen = new HashMap<>();

    private List<AbstractMarker> markers = new ArrayList<>();

    private final PriorityQueue<VoronoiEvent> eventQueue;
    private ArcTree arcTree;

    public ThiessenPolygonsLayer2(final MapView parent, final int id, final List<AbstractMarker> markers) {
        super(parent, id);

        layer = new Group();
        arcLayer = new Group();
        layer.getChildren().add(arcLayer);
        this.markers = markers;

        // Set up the event queue to order the voronoi event in ascending order based on their Y-coordinate
        eventQueue = new PriorityQueue<>((v1, v2) -> {
            if (v1.getYCoord() > v2.getYCoord()) {
                return 1;
            }

            return -1;
        });

    }

    @Override
    public void setUp() {
        layer.getChildren().clear();

        // If there is 1 marker then there is only one zone
        if (markers.size() == 1) {
            final ConstellationColor[] palette = ConstellationColor.createPalette(1);

            Rectangle r = new Rectangle();
            r.setX(0);
            r.setY(0);
            r.setWidth(MapView.MAP_WIDTH);
            r.setHeight(MapView.MAP_HEIGHT);
            r.setFill(palette[0].getJavaFXColor());
            r.setStroke(palette[0].getJavaFXColor());
            r.setOpacity(0.5);
            r.setMouseTransparent(true);
            layer.getChildren().add(r);

            // If there are more markers then put the coordinates inside of a SiteEvent and add the to the eventQueue priority queue
        } else if (markers.size() > 1) {
            for (final AbstractMarker m : markers) {
                if (m instanceof PointMarker || m instanceof UserPointMarker) {

                    double x = m.getX();
                    double y = m.getY();

                    final SiteEvent siteEvent = new SiteEvent(y, new Vec3(x, y));

                    eventQueue.add(siteEvent);
                }
            }

            calculateVoronoi();
        }

    }

    /**
     * Calculate the zones for each marker and add them to the generatedShapes
     * display layer
     */
    private void calculateVoronoi() {
        arcTree = new ArcTree(eventQueue);
        arcTree.run();
        arcLayer.getChildren().clear();
        layer.getChildren().clear();
        layer.getChildren().add(arcLayer);
        final List<Polygon> generatedShapes = arcTree.getCompletedShapes();
        generatedShapes.forEach(shape -> layer.getChildren().add(shape));
    }

    @Override
    public Group getLayer() {
        return layer;
    }

}
