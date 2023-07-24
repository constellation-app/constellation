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

/**
 *
 * @author altair1673
 */
public abstract class BeachLineElement {

    protected final Vec3 start = new Vec3();
    protected final Vec3 end = new Vec3();

    protected double spawnX;

    private BeachLineElement left = null;
    private BeachLineElement right = null;
    private BeachLineElement parent = null;

    protected BeachLineElement(final Vec3 start, final Vec3 end, final double spawnX) {
        this.start.setX(start.getX());
        this.start.setY(start.getY());

        this.end.setX(end.getX());
        this.end.setY(end.getY());

        this.spawnX = spawnX;
    }

    public void setStart(final double x, final double y) {
        start.setX(x);
        start.setY(y);
    }

    public void setEnd(final double x, final double y) {
        end.setX(x);
        end.setY(y);
    }

    public Vec3 getStart() {
        return start;
    }

    public Vec3 getEnd() {
        return end;
    }

    public double getSpawnX() {
        return spawnX;
    }

    public BeachLineElement getLeft() {
        return left;
    }

    public void setLeft(final BeachLineElement left) {
        this.left = left;
    }

    public BeachLineElement getRight() {
        return right;
    }

    public void setRight(final BeachLineElement right) {
        this.right = right;
    }

    public BeachLineElement getParent() {
        return parent;
    }

    public void setParent(final BeachLineElement parent) {
        this.parent = parent;
    }

    public void setParentFromItem(final BeachLineElement item) {
        if (item.getParent() == null) {
            return;
        }

        if (item.getParent().getLeft() == item) {
            item.getParent().setLeft(this);
        } else if (item.getParent().getRight() == item) {
            item.getParent().setRight(this);
        }
    }

}
