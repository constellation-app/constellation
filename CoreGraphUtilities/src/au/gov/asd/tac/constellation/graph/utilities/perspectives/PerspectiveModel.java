/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.perspectives;

import au.gov.asd.tac.constellation.graph.utilities.perspectives.PerspectiveModel.Perspective;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.AbstractListModel;

/**
 *
 * @author algol
 */
class PerspectiveModel extends AbstractListModel<Perspective> {

    //needs to be declared as ArrayList for serializing
    final ArrayList<Perspective> perspectives;

    public PerspectiveModel() {
        perspectives = new ArrayList<>();
    }

    public PerspectiveModel(final PerspectiveModel other) {
        perspectives = (ArrayList<Perspective>) other.perspectives.stream().collect(Collectors.toList());
    }

    @Override
    public int getSize() {
        return perspectives.size();
    }

    @Override
    public Perspective getElementAt(final int index) {
        return perspectives.get(index);
    }

    /**
     * Add a perspective to this list.
     *
     * @param perspective The perspective that is to be inserted.
     *
     * @return The index of the added element.
     */
    public int addElement(final Perspective perspective) {
        final String label = perspective.label;
        perspectives.add(perspective);
        perspectives.sort((final Perspective p1, final Perspective p2) -> p1.label.compareTo(p2.label));

        fireContentsChanged(this, 0, perspectives.size() - 1);

        for (int i = 0;; i++) {
            if (label.equals(perspectives.get(i).label)) {
                return i;
            }
        }
    }

    /**
     * Remove the perspective at the given index.
     *
     * @param index The index of the perspective that is to be removed.
     */
    public void removeElementAt(final int index) {
        perspectives.remove(index);
        fireContentsChanged(this, 0, perspectives.size() - 1);
    }

    /**
     * Remove the perspective that matches the 'prototype' perspective.
     *
     * @param perspective The perspective that acts as a prototype for removal.
     */
    public void removeElement(final Perspective perspective) {
        final int index = perspectives.indexOf(perspective);
        perspectives.remove(perspective);
        fireContentsChanged(this, index, index);
    }

    protected String getNewLabel() {
        final String labelBase = "Perspective %d";
        int i = getSize() + 1;
        while (true) {
            final String label = String.format(labelBase, i);
            if (!exists(label)) {
                return label;
            }

            i++;
        }
    }

    /**
     * Find out if a label already exists in the model.
     *
     * @param label The label to search for.
     *
     * @return True if the label already exists in the model, false if it doesn't.
     */
    private boolean exists(final String label) {
        return perspectives.stream().anyMatch(p -> p.label.equals(label));
    }

    /**
     * An immutable class representing a perspective bookmark.
     */
    static final class Perspective {

        final String label;
        final int relativeTo;
        final Vector3f centre;
        final Vector3f eye;
        final Vector3f up;
        final Vector3f rotate;

        /**
         * Create a new Perspective.
         *
         * @param label
         * @param relativeTo
         * @param centre
         * @param eye
         * @param up
         * @param rotate
         */
        Perspective(final String label, final int relativeTo, final Vector3f centre, final Vector3f eye, final Vector3f up, final Vector3f rotate) {
            this.label = label;
            this.relativeTo = relativeTo;
            this.centre = new Vector3f(centre);
            this.eye = new Vector3f(eye);
            this.up = new Vector3f(up);
            this.rotate = new Vector3f(rotate);
        }

        /**
         * Create a new Perspective from another Perspective with a new label.
         *
         * @param label
         * @param perspective
         */
        Perspective(final String label, final Perspective perspective) {
            this.label = label;
            relativeTo = perspective.relativeTo;
            centre = new Vector3f(perspective.centre);
            eye = new Vector3f(perspective.eye);
            up = new Vector3f(perspective.up);
            rotate = new Vector3f(perspective.rotate);
        }

        @Override
        public String toString() {
            return relativeTo >= 0 ? String.format("%s (relative to %d)", label, relativeTo) : label;
        }
    }
}
