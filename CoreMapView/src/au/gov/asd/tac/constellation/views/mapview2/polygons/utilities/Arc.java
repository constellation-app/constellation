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
package au.gov.asd.tac.constellation.views.mapview2.polygons.utilities;

import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;

/**
 *
 * @author altair1673
 */
public class Arc extends BlineElement {
    private final Vec3 focus;

    private final Polyline pl = new Polyline();


    public Arc(final Vec3 focus) {
        this.focus = focus;
        pl.setStroke(Color.BLACK);
    }

    public Vec3 getFocus() {
        return focus;
    }

    public double getY(final double x, final double directtrix) {
        return (Math.pow((x - focus.getX()), 2) / (2 * (focus.getY() - directtrix))) + ((focus.getY() + directtrix) / 2);
        /*final double a = 1.0 / (2.0 * (focus.getY() - directtrix));
        double c = (focus.getY() + directtrix) * 0.5;

        final double w = x - focus.getX();

        return a * w * w - c;*/
    }

    public void calculateArc(final double min, final double max, final double directrix) {
        pl.getPoints().clear();
        for (double i = min; i <= max; i = i + 0.05) {
            pl.getPoints().addAll(new Double[]{i, getY(i, directrix)});
        }
    }

    public Polyline getArc() {
        return pl;
    }

}
