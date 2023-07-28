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

/**
 *
 * @author altair1673
 */
public class BlineElement {
    private BlineElement parent = null;
    private BlineElement left = null;
    private BlineElement right = null;

    private HalfEdge leftEdge = null;
    private HalfEdge rightEdge = null;

    public BlineElement getParent() {
        return parent;
    }

    public void setParent(BlineElement parent) {
        this.parent = parent;
    }

    public BlineElement getLeft() {
        return left;
    }

    public void setLeft(BlineElement left) {
        this.left = left;
    }

    public BlineElement getRight() {
        return right;
    }

    public void setRight(BlineElement right) {
        this.right = right;
    }

    public HalfEdge getLeftEdge() {
        return leftEdge;
    }

    public void setLeftEdge(HalfEdge leftEdge) {
        this.leftEdge = leftEdge;
    }

    public HalfEdge getRightEdge() {
        return rightEdge;
    }

    public void setRightEdge(HalfEdge rightEdge) {
        this.rightEdge = rightEdge;
    }

    public void setParentFromItem(BlineElement item) {
        if (item.getParent() == null) {
            return;
        }

        if (item.getParent().getLeft() == item) {
            item.getParent().setLeft(this);
        } else {
            item.getParent().setRight(this);
        }
    }
}
