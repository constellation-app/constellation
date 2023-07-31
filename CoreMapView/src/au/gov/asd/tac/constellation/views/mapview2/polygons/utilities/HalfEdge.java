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

/**
 *
 * @author altair1673
 */
public class HalfEdge {
    private Arc parentArc = null;
    private BlineElement homeArc = null;

    private Vec3 start;
    private Vec3 dirVect;

    private boolean extendsUp = false;

    public HalfEdge(final Arc parentArc, final BlineElement homeArc, final Vec3 start, final Vec3 dirVect) {
        this.parentArc = parentArc;
        this.homeArc = homeArc;
        this.start = start;
        this.dirVect = dirVect;
    }

    public Vec3 getStart() {
        return start;
    }

    public void setStart(Vec3 start) {
        this.start = start;
    }

    public Vec3 getDirVect() {
        return dirVect;
    }

    public void setDirVect(Vec3 dirVect) {
        this.dirVect = dirVect;
    }

    public Arc getParentArc() {
        return parentArc;
    }

    public void setParentArc(Arc parentArc) {
        this.parentArc = parentArc;
    }

    public BlineElement getHomeArc() {
        return homeArc;
    }

    public void setHomeArc(BlineElement homeArc) {
        this.homeArc = homeArc;
    }

    public boolean extendsUp() {
        return extendsUp;
    }

    public void setExtendsUp(boolean extendsUp) {
        this.extendsUp = extendsUp;
    }

}
