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
package au.gov.tac.constellation.views.mapview2.utillities;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

/**
 *
 * @author altair1673
 */
public class Parabola {

    private double focusX;
    private double focusY;
    private double directtrix;

    private Polyline parabola;

    private static final Logger LOGGER = Logger.getLogger("Parabola");

    public Parabola(double focusX, double focusY, double directtrix) {
        parabola = new Polyline();
        parabola.setStroke(Color.BLACK);
        parabola.setStrokeWidth(1);
        this.focusX = focusX;
        this.focusY = focusY;

        this.directtrix = directtrix + 80;

    }

    public double getY(double x) {
        return (Math.pow((x - focusX), 2) / (2 * (focusY - directtrix))) + ((focusY + directtrix) / 2);
    }

    /**
     * Calculates the points of the parabola and adds them to the Polyline
     * object
     */
    public void generateParabola() {
        for (double x = focusX - 300; x <= focusX + 300; ++x) {

            LOGGER.log(Level.SEVERE, "x of parabola: " + x + " y of parabola: " + getY(x));

            parabola.getPoints().addAll(new Double[]{x, getY(x)});
        }
    }

    public Polyline getParabola() {
        return parabola;
    }


}
