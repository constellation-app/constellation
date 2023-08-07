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

import au.gov.asd.tac.constellation.views.mapview2.polygons.utilities.EdgeEvent;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

/**
 * Class that represents a parabola
 *
 * @author altair1673
 */
public class Parabola extends BeachLineElement {

    private final Vec3 site = new Vec3();
    private EdgeEvent currentEdgeEvent = null;

    private List<Edge> createdEdges = new ArrayList<>();

    public Parabola(final Vec3 start, final Vec3 end, final double spawnX, final Vec3 focus) {
        super(start, end, spawnX);

        this.site.setX(focus.getX());
        this.site.setY(focus.getY());
    }

    public double getY(final double x, final double directtrix) {
        return (Math.pow((x - site.getX()), 2) / (2 * (site.getY() - directtrix))) + ((site.getX() + directtrix) / 2);
    }

    public double getXPositiveIntersection(final Parabola other, final double directrix) {
        return (other.getSite().getX() * (site.getY() - directrix) - site.getX() * (other.getSite().getY() - directrix) + Math.sqrt((getSite().getY() - directrix) * (other.getSite().getY() - directrix) * (Math.pow(site.getX() - other.getSite().getX(), 2) + ((Math.pow(site.getY() - other.getSite().getY(), 2)))))) / (getSite().getY() - other.getSite().getY());
    }

    public double getXNegativeIntersection(final Parabola other, final double directrix) {
        return (other.getSite().getX() * (site.getY() - directrix) - site.getX() * (other.getSite().getY() - directrix) - Math.sqrt((getSite().getY() - directrix) * (other.getSite().getY() - directrix) * (Math.pow(site.getX() - other.getSite().getX(), 2) + ((Math.pow(site.getY() - other.getSite().getY(), 2)))))) / (getSite().getY() - other.getSite().getY());
    }

    public Vec3 getSite() {
        return site;
    }

    public Polyline getParabola() {
        final Polyline p = new Polyline();

        for (double i = start.getX(); i < end.getX(); ++i) {
            p.getPoints().addAll(new Double[]{i, getY(i, site.getY() + 80)});
        }

        p.setStroke(Color.BLACK);
        return p;
    }

    public void addEdges(final Edge e) {
        createdEdges.add(e);
    }

    public List<Edge> getCreatedEdges() {
        return createdEdges;
    }

    public EdgeEvent getCurrentEdgeEvent() {
        return currentEdgeEvent;
    }

    public void setCurrentEdgeEvent(EdgeEvent currentEdgeEvent) {
        this.currentEdgeEvent = currentEdgeEvent;
    }

}
