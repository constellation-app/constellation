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
import javafx.scene.shape.Line;

/**
 *
 * @author altair1673
 */
public class BaseLine extends BlineElement {

    private Vec3 start;
    private Vec3 end;

    private Vec3 dirVect;

    public BaseLine(final Vec3 start, final Vec3 end) {
        super();
        this.start = start;
        this.end = end;
    }

    public Vec3 getStart() {
        return start;
    }

    public void setStart(Vec3 start) {
        this.start = start;
    }

    public Vec3 getEnd() {
        return end;
    }

    public void setEnd(Vec3 end) {
        this.end = end;
    }

    public Vec3 getDirVect() {
        return dirVect;
    }

    public void setDirVect(Vec3 dirVect) {
        this.dirVect = dirVect;
    }

    public Line getLine() {
        final Line l = new Line();
        l.setStartX(start.getX());
        l.setStartY(start.getY());

        l.setEndX(end.getX());
        l.setEndY(end.getY());

        l.setFill(Color.RED);
        l.setStrokeWidth(10);

        return l;
    }

}
