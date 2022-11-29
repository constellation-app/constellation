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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;

/**
 * The description of a graph attribute.
 *
 * @author sirius
 */
public interface Attribute {

    /**
     * The id of the attribute. This will be unique for all attributes in a
     * graph. However, if an attribute is removed from the graph, future
     * attributes may (and probably will) reuse this id.
     *
     * @return the id of the attribute.
     */
    public int getId();

    /**
     * Sets the id of this attribute.
     *
     * @param id the new id of this attribute.
     */
    public void setId(final int id);

    /**
     * Returns the element type that this attribute is associated with.
     *
     * @return the element type that this attribute is associated with.
     */
    public GraphElementType getElementType();

    /**
     * Sets the element type of this attribute.
     *
     * @param elementType the new element type of this attribute.
     */
    public void setElementType(final GraphElementType elementType);

    /**
     * The type of this attribute.
     * <p>
     * This is a String as returned by
     * {@link au.gov.asd.tac.constellation.graph.attribute.AttributeDescription#getName()}
     * from one of the registered AttributeDescription instances.
     *
     * @return The type of this attribute.
     */
    public String getAttributeType();

    /**
     * Sets the type of this attribute.
     *
     * @param attributeType the new type of the attribute.
     */
    public void setAttributeType(final String attributeType);

    /**
     * Return the name of the attribute. This name will be unique for all
     * attributes associated with the same element type in a graph. This is the
     * value that is presented to the user in the UI and the most common way in
     * which attributes are looked up in the graph.
     *
     * @return the name of the attribute.
     */
    public String getName();

    /**
     * Sets a new name for this attribute.
     *
     * @param name the new name for the attribute.
     * @see Attribute#getName()
     */
    public void setName(final String name);

    /**
     * Returns the description of an attribute. The description provides more
     * detailed information about the attribute such as how it is being used or
     * and constraints that should be observed.
     *
     * @return the description of an attribute.
     */
    public String getDescription();

    /**
     * Sets a new description of an attribute.
     *
     * @param description the new description of the attribute.
     * @see Attribute#getDescription()
     */
    public void setDescription(final String description);

    /**
     * Returns the current default value for this attribute. This is the value
     * that new elements will get when they are created.
     *
     * @return the current default value for this attribute.
     */
    public Object getDefaultValue();

    /**
     * Sets the new default value for this attribute. This will not change the
     * values of any existing elements but rather the value that new elements
     * will get given when they are created.
     *
     * @param defaultValue the new default value for the attribute.
     */
    public void setDefaultValue(final Object defaultValue);

    /**
     * Returns the class of the attribute description that defines this
     * attribute.
     *
     * @return the class of the attribute description that defines this
     * attribute.
     */
    public Class<? extends AttributeDescription> getDataType();

    /**
     * Sets the data type of this attribute.
     *
     * @param dataType the new datatype of this attribute.
     */
    public void setDataType(final Class<? extends AttributeDescription> dataType);

    /**
     * Returns the attribute merger for this attribute.
     *
     * @return the attribute merger for this attribute.
     */
    public GraphAttributeMerger getAttributeMerger();

    /**
     * Sets the attribute merger for this attribute.
     *
     * @param attributeMerger the attribute merger for this attribute.
     */
    public void setAttributeMerger(final GraphAttributeMerger attributeMerger);
}
