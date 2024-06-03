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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 * AttributeData instances hold information about attributes as they exist in a
 * specific graph. This includes information about their ID, current
 * modification counter, and whether or not they are part of the primary key.
 * <br>
 * The Attribute Editor maintains a collection of these objects as part of its
 * data model, which is an {@link AttributeState} object. Doing so facilitates
 * operations that only require information about attributes and not their
 * values, such as highlighting schema and key attributes in the editor, or
 * adding and removing attributes on the graph.
 * <br>
 * AttributeData objects are for the most part immutable. When anything about
 * the attribute itself changes, a new AttributeData object should be created.
 * The exception to this is the attribute's modification counter, which only
 * represents a change in the attribute's values. This change may be reflected
 * in the existing AttributeData object by calling
 * {@link #attibuteValueHasChanged attibuteValueHasChanged()}.
 *
 * @author twinkle2_little
 */
public class AttributeData extends AttributePrototype {

    private final int attributeId;
    private long modificationCount;
    private final boolean isKey;
    private final boolean isSchema;
    private boolean isKeepExpanded;

    /**
     * Create a new AttributeData object corresponding to an attribute as it
     * exists in a specific graph.
     *
     * @param name The name of the attribute
     * @param description The description for the attribute
     * @param id The id of the attribute within the graph.
     * @param modCount The current modification counter for the number of
     * changes to values for the attribute.
     * @param elementType The {@link GraphElementType} constant for the graph
     * element that the attribute belongs to.
     * @param dataType The name of the type of the attribute; this should be the
     * same as
     * {@link au.gov.asd.tac.constellation.graph.attribute.AttributeDescription#getName()}
     * for the description class corresponding to the attribute.
     * @param defaultValue The default value for the attribute.
     * @param isKey whether or not the attribute is part of the primary key for
     * the relevant graph element in the graph.
     * @param isSchema whether or not the attribute belongs to the graph's
     * schema.
     */
    public AttributeData(final String name, final String description, final int id, final long modCount, final GraphElementType elementType, final String dataType, final Object defaultValue, final boolean isKey, final boolean isSchema) {
        super(name, description, elementType, dataType, defaultValue);
        this.attributeId = id;
        this.modificationCount = modCount;
        this.isKey = isKey;
        this.isSchema = isSchema;
    }

    /**
     * Get the ID of the attribute in the graph.
     *
     * @return The ID of the attribute in the graph. This will be a non-negative
     * integer.
     */
    public int getAttributeId() {
        return attributeId;
    }

    /**
     * Called by the attribute editor to indicate that the attribute this object
     * represents has had a change to its values.
     *
     * @param newModCount The new modification counter for the attribute
     * @return True if newModCount was different to the current mod count, false
     * otherwise.
     */
    public boolean attibuteValueHasChanged(final long newModCount) {
        final boolean changed = newModCount != getModificationCount();
        modificationCount = newModCount;
        return changed;
    }

    /**
     * Get the current modification counter for the attribute. This tracks the
     * modifications to the attribute's values on the graph, and should stay in
     * sync with the corresponding mod counter on the graph itself.
     *
     * @return the modification counter for the attribute.
     */
    public long getModificationCount() {
        return modificationCount;
    }

    /**
     * Is the attribute part of the primary key for the relevant graph element
     * on the grpah?
     *
     * @return True if the attribute is part of the primary key, false
     * otherwise.
     */
    public boolean isKey() {
        return isKey;
    }

    /**
     * Does the attribute belong to the graph's schema.
     *
     * @return True if the attribute belongs to the graph's schema, false
     * otherwise.
     */
    public boolean isSchema() {
        return isSchema;
    }

    /**
     * Should multiple values of this attribute be always expanded .
     *
     * @return True if the attribute values should always be expanded, false
     * otherwise.
     */
    public boolean isKeepExpanded() {
        return isKeepExpanded;
    }

    public void setKeepExpanded(final boolean isKeepExpanded) {
        this.isKeepExpanded = isKeepExpanded;
    }
}
