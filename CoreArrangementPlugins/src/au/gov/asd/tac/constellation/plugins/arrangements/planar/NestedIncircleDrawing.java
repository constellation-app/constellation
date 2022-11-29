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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class NestedIncircleDrawing {

    static class Point {

        double x;
        double y;

        public Point() {
            this(0, 0);
        }

        public Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        public void add(final Point p) {
            x += p.x;
            y += p.y;
        }

        public void scale(final double factor) {
            x *= factor;
            y *= factor;
        }

        public double getDistanceFromLine(final Point lineA, final Point lineB) {
            return 0;
        }

    }

    public static Point getPolygonCentre(final List<Point> polygon) {
        Point centre = new Point();
        for (Point point : polygon) {
            centre.add(point);
        }
        centre.scale(1 / ((double) polygon.size()));
        return centre;
    }

    public static Point getIncircleRadius(final List<Point> polygon) {

        return new Point();
    }

}
