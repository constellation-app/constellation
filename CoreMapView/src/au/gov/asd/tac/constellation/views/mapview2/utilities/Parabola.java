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

import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

/**
 * Class that represents a parabola
 *
 * @author altair1673
 */
public class Parabola {

    private final double focusX;
    private final double focusY;
    private final double directtrix;

    private final Polyline parabolaLine;

    public Parabola(final double focusX, final double focusY, final double directtrix) {
        parabolaLine = new Polyline();
        parabolaLine.setStroke(Color.BLACK);
        parabolaLine.setStrokeWidth(1);
        this.focusX = focusX;
        this.focusY = focusY;

        this.directtrix = directtrix + 80;

    }

    public double getY(final double x) {
        return (Math.pow((x - focusX), 2) / (2 * (focusY - directtrix))) + ((focusY + directtrix) / 2);
    }

    /**
     * Calculates the points of the parabolaLine and adds them to the Polyline
     * object
     */
    public void generateParabola() {
        for (double x = focusX - 300; x <= focusX + 300; x++) {
            parabolaLine.getPoints().addAll(new Double[]{x, getY(x)});
        }
    }

    public Polyline getParabola() {
        return parabolaLine;
    }


}
