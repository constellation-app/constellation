/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

/**
 * This enumeration lists all possible statuses of a node with regards to
 * composites.
 * <p>
 * A node can be a composite, be part of a composite (that is currently
 * expanded), be the leader of a composite (which is currently expanded), or
 * have nothing to do with composites.
 * <p>
 * This class is commonly used for visualisation purposes, where a node is to be
 * represented differently depending on its categorisation into the options
 * above.
 *
 * @see CompositeNodeState#getStatus()
 *
 * @author twilight_sparkle
 */
public enum CompositeStatus {

    NOT_A_COMPOSITE(0, "Normal node"),
    IS_A_COMPOSITE(1, "Composite node"),
    LEADER_OF_A_COMPOSITE(2, "Composite constituent"),
    PART_OF_A_COMPOSITE(3, "Composite constituent"),;

    public final int id;
    public final String compositeName;

    CompositeStatus(final int id, final String name) {
        this.id = id;
        this.compositeName = name;
    }

    public static final CompositeStatus getCompositeStatus(final int statusId) {
        switch (statusId) {
            case 0:
                return NOT_A_COMPOSITE;
            case 1:
                return IS_A_COMPOSITE;
            case 2:
                return LEADER_OF_A_COMPOSITE;
            case 3:
                return PART_OF_A_COMPOSITE;
            default:
                return null;
        }
    }
}
