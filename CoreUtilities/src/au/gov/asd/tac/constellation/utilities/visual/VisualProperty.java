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
package au.gov.asd.tac.constellation.utilities.visual;

/**
 * A list of properties that represent various changes to data that will have an
 * effect on its visualisation. The primary use of these constants is in the
 * creation of {@link VisualChange} objects.
 * <p>
 * The property type indicates the data type used to represent property values,
 * whilst the element type indicates the element type that the property ranges
 * over. In the case of properties that represent things like adding or removing
 * elements, the element type is <code>NONE</code>.
 * <p>
 * Currently property and element types are unused, but are left here for
 * clarity and possible future use.
 *
 * @author twilight_sparkle
 * @author antares
 */
public enum VisualProperty {

    BACKGROUND_COLOR(PropertyType.OBJECT, ElementType.GRAPH),
    HIGHLIGHT_COLOR(PropertyType.OBJECT, ElementType.GRAPH),
    DRAW_FLAGS(PropertyType.OBJECT, ElementType.GRAPH),
    CAMERA(PropertyType.OBJECT, ElementType.GRAPH),
    BLAZE_SIZE(PropertyType.FLOAT, ElementType.GRAPH),
    BLAZE_OPACITY(PropertyType.FLOAT, ElementType.GRAPH),
    CONNECTIONS_OPACITY(PropertyType.FLOAT, ElementType.GRAPH),
    CONNECTIONS_MOTION(PropertyType.FLOAT, ElementType.GRAPH),
    VISIBLE_ABOVE_THRESHOLD(PropertyType.BOOLEAN, ElementType.GRAPH),
    VISIBILITY_THRESHOLD(PropertyType.INT, ElementType.GRAPH),
    TOP_LABEL_SIZE(PropertyType.FLOAT, ElementType.TOP_LABELS),
    TOP_LABEL_COLOR(PropertyType.OBJECT, ElementType.TOP_LABELS),
    TOP_LABEL_TEXT(PropertyType.OBJECT, ElementType.TOP_LABEL),
    BOTTOM_LABEL_SIZE(PropertyType.FLOAT, ElementType.BOTTOM_LABELS),
    BOTTOM_LABEL_COLOR(PropertyType.OBJECT, ElementType.BOTTOM_LABELS),
    BOTTOM_LABEL_TEXT(PropertyType.OBJECT, ElementType.BOTTOM_LABEL),
    CONNECTION_LABEL_SIZE(PropertyType.FLOAT, ElementType.CONNECTION_LABELS),
    CONNECTION_LABEL_COLOR(PropertyType.OBJECT, ElementType.CONNECTION_LABELS),
    CONNECTION_LABEL_TEXT(PropertyType.OBJECT, ElementType.CONNECTION_LABEL),
    VERTEX_X(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_Y(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_Z(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_X2(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_Y2(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_Z2(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_COLOR(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_BACKGROUND_ICON(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_FOREGROUND_ICON(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_SELECTED(PropertyType.BOOLEAN, ElementType.VERTEX),
    VERTEX_VISIBILITY(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_DIM(PropertyType.BOOLEAN, ElementType.VERTEX),
    VERTEX_RADIUS(PropertyType.FLOAT, ElementType.VERTEX),
    VERTEX_BLAZED(PropertyType.BOOLEAN, ElementType.VERTEX),
    VERTEX_BLAZE_ANGLE(PropertyType.INT, ElementType.VERTEX),
    VERTEX_BLAZE_COLOR(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_NW_DECORATOR(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_NE_DECORATOR(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_SE_DECORATOR(PropertyType.OBJECT, ElementType.VERTEX),
    VERTEX_SW_DECORATOR(PropertyType.OBJECT, ElementType.VERTEX),
    CONNECTION_COLOR(PropertyType.OBJECT, ElementType.CONNECTION),
    CONNECTION_SELECTED(PropertyType.BOOLEAN, ElementType.CONNECTION),
    CONNECTION_DIRECTED(PropertyType.BOOLEAN, ElementType.CONNECTION),
    CONNECTION_VISIBILITY(PropertyType.FLOAT, ElementType.CONNECTION),
    CONNECTION_DIM(PropertyType.BOOLEAN, ElementType.CONNECTION),
    CONNECTION_LINESTYLE(PropertyType.OBJECT, ElementType.CONNECTION),
    CONNECTION_WIDTH(PropertyType.FLOAT, ElementType.CONNECTION),
    TOP_LABELS_REBUILD(PropertyType.STATUS, ElementType.NONE),
    BOTTOM_LABELS_REBUILD(PropertyType.STATUS, ElementType.NONE),
    CONNECTION_LABELS_REBUILD(PropertyType.STATUS, ElementType.NONE),
    VERTICES_ADDED(PropertyType.STATUS, ElementType.NONE),
    VERTICES_REMOVED(PropertyType.STATUS, ElementType.NONE),
    VERTICES_REBUILD(PropertyType.STATUS, ElementType.NONE),
    CONNECTIONS_ADDED(PropertyType.STATUS, ElementType.NONE),
    CONNECTIONS_REMOVED(PropertyType.STATUS, ElementType.NONE),
    CONNECTIONS_REBUILD(PropertyType.STATUS, ElementType.NONE),
    // Used to signify that a change has occurred outside the visual model.
    // Usually this is state information specific to a VisualProcessor.
    EXTERNAL_CHANGE(PropertyType.STATUS, ElementType.NONE),;

    public enum PropertyType {

        FLOAT,
        INT,
        OBJECT,
        BOOLEAN,
        STATUS,
        TASK;
    }

    public enum ElementType {

        GRAPH,
        VERTEX,
        CONNECTION,
        TOP_LABELS,
        BOTTOM_LABELS,
        CONNECTION_LABELS,
        TOP_LABEL,
        BOTTOM_LABEL,
        CONNECTION_LABEL,
        NONE;
    }

    public final PropertyType propertyType;
    public final ElementType elementType;

    private VisualProperty(final PropertyType propertyType, final ElementType elementType) {
        this.propertyType = propertyType;
        this.elementType = elementType;
    }

}
