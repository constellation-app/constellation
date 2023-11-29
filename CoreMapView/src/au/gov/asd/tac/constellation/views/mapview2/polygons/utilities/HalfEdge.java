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
    private Vec3 parentFocus = null;
    private Vec3 homeFocus = null;

    private Vec3 start;
    private Vec3 dirVect;

    private boolean extendsUp = false;
    private boolean complete = false;

    public HalfEdge(final Vec3 parentArc, final Vec3 homeArc, final Vec3 start, final Vec3 dirVect) {
        this.parentFocus = parentArc;
        this.homeFocus = homeArc;
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

    public Vec3 getParentFocus() {
        return parentFocus;
    }

    public void setParentFocus(final Vec3 parentFocus) {
        this.parentFocus = parentFocus;
    }

    public Vec3 getHomeFocus() {
        return homeFocus;
    }

    public void setHomeFocus(final Vec3 homeFocus) {
        this.homeFocus = homeFocus;
    }

    public boolean extendsUp() {
        return extendsUp;
    }

    public void setExtendsUp(boolean extendsUp) {
        this.extendsUp = extendsUp;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }


}
