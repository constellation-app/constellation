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

import au.gov.asd.tac.constellation.views.mapview2.MapView;

/**
 *
 * @author altair1673
 */
public class BeachLine {
    private BeachLineElement root;

    public BeachLine() {
        root = new Parabola(new Vec3(-100, -100), new Vec3(MapView.MAP_WIDTH + 100, -100), MapView.MAP_WIDTH / 2, new Vec3(MapView.MAP_WIDTH / 2, -35));
    }

    public void splitArc(final Parabola element) {
        insertNewArcSequence(root, element);
    }

    private void insertNewArcSequence(final BeachLineElement currentNode, final Parabola element) {
        if (element.getSpawnX() <= currentNode.getEnd().getX() && element.getSpawnX() >= currentNode.getStart().getX()) {
            if (currentNode.getLeft() == null && currentNode.getRight() == null && currentNode instanceof Parabola) {
                final Parabola splitArc = (Parabola) currentNode;

                final double intersectionPoint = splitArc.getY(element.getSpawnX(), element.getSite().getY());

                final Edge eLeft = new Edge(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(element.getSpawnX(), intersectionPoint), element.getSpawnX());

                final Edge eRight = new Edge(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(element.getSpawnX(), intersectionPoint), element.getSpawnX());

                final Parabola arcLeft = new Parabola(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(splitArc.getStart()), splitArc.getSpawnX(), splitArc.getSite());

                final Parabola arcRight = new Parabola(new Vec3(element.getSpawnX(), intersectionPoint), new Vec3(splitArc.getEnd()), splitArc.getSpawnX(), splitArc.getSite());

                eLeft.setRight(eRight);
                eLeft.setLeft(arcLeft);
                eRight.setRight(arcRight);
                eRight.setLeft(element);
            }

        } else if (element.getSpawnX() > currentNode.getEnd().getX()) {
            insertNewArcSequence(currentNode.getRight(), element);
        } else if (element.getSpawnX() < currentNode.getStart().getX()) {
            insertNewArcSequence(currentNode.getLeft(), element);
        }

    }

}
