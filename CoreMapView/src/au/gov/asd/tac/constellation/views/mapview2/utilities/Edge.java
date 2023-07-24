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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author altair1673
 */
public class Edge extends BeachLineElement {

    private Vec3 dirVect = new Vec3();

    private final List<Parabola> relatedPoints = new ArrayList<>();

    private boolean isGrowing = true;

    public Edge(final Vec3 start, final Vec3 end, final double spawnX) {
        super(start, end, spawnX);
    }

    public Edge(final Edge other) {
        this(other.getStart(), other.getEnd(), other.getSpawnX());
        super.setLeft(other.getLeft());
        super.setRight(other.getRight());
    }

    public void addPoint(final Parabola point) {
        relatedPoints.add(point);
    }

    public boolean isIsGrowing() {
        return isGrowing;
    }

    public void setIsGrowing(boolean isGrowing) {
        this.isGrowing = isGrowing;
    }

    public Vec3 getDirVect() {
        return dirVect;
    }

    public void setDirVect(Vec3 dirVect) {
        this.dirVect = dirVect;
    }



}
