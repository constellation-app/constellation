/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.attribute.interaction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javafx.scene.Node;
import org.openide.util.Lookup;

/**
 * A class that facilitate common interactions between attributes and
 * CONSTELLATION's various views.
 * <p>
 * The methods here facilitate basic operations that are required for attributes
 * to be properly displayed and manipulated by views such as AttributeEditor,
 * Histogram, and Table View. However unlike AttributeDescription, this is not a
 * mandatory class for an attribute type. In the absence of an Interaction
 * class, views should still have some graceful default behaviour (and
 * importantly not throw any errors), although the default behaviour need not be
 * of any use for the type of attribute concerned.
 * <p>
 * Note that operations specific to certain types of attributes or specialised
 * views are not included here and should still be handled by the concerned
 * views themselves.
 *
 * @author twilight_sparkle
 * @param <T>
 */
public abstract class AbstractAttributeInteraction<T> {

    /**
     * Get the priority associated with this interactions. There may be multiple
     * interactions defined for a given attribute type. When this is the case,
     * typically only the interaction with the highest priority should be used.
     *
     * @return The priority of this interaction.
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Get the name of the attribute type this interaction is for.
     * <p>
     * This should match the value of getName() in the corresponding
     * AttributeDescription.
     *
     * @return the name of the attribute type this interaction is for.
     */
    public abstract String getDataType();

    /**
     * Get the text that best visually represents a particular value of this
     * attribute type.
     * <p>
     * Note that this might differ substantially from the getStringValue()
     * method provided by the AttributeDescription, as that method is intended
     * for internal manipulation of the attribute rather than presentation of
     * the attribute in a view. In particular, there is no need for this method
     * to return a string from which the original value of the attribute can be
     * recovered.
     *
     * @param value an object representing the value the attribute.
     * @return the text that best visually represents a particular value of this
     * attribute type.
     */
    public abstract String getDisplayText(final Object value);

    /**
     * Get a list of nodes that visually represent a particular value of this
     * attribute type. This method should be able to create said nodes to fit
     * the dimensions specified by the requesting view.
     *
     * @param value The attribute value for which to get a visual
     * representation.
     * @param width the width the nodes should be set to, or a negative value to
     * indicate no preference.
     * @param height the height the nodes should be set to, or a negative value
     * to indicate no preference.
     * @return a list of nodes that visually represent a particular value of
     * this attribute type.
     */
    public List<Node> getDisplayNodes(final Object value, final double width, final double height) {
        return Collections.emptyList();
    }

    /**
     * A list of names of attribute types, in order of which this attribute
     * would be preferred to be edited by any view that allows editing of
     * attribute values. If this list is empty, the attribute is only ever
     * edited as its native type.
     * <p>
     * For any type in this list which is not the native type of this attribute,
     * this class should also return appropriate AttributeValueTranslators when
     * toEditTranslator() and fromEditTranslator() are called with that type.
     * <p>
     * This could be used, for example, to allow editing date time attributes as
     * strings if no date-time editor is available to the application (or a
     * particular view).
     *
     * @return a list of names of attribute types.
     */
    public List<String> getPreferredEditTypes() {
        return Collections.emptyList();
    }

    /**
     * Get the translator which converts from native type to the specified type
     * for editing.
     * <p>
     * When overriding this method, if no specific translator matches in the
     * concrete class, super.toEditTranslator() should be called to return the
     * identity.
     *
     * @param dataType the data type.
     * @return the translator which converts from native type to the specified
     * type for editing.
     */
    public AttributeValueTranslator toEditTranslator(final String dataType) {
        return AttributeValueTranslator.IDENTITY;
    }

    /**
     * Get the translator which converts from the specified type to the native
     * type after editing.
     * <p>
     * When overriding this method, if no specific translator matches in the
     * concrete class, super.fromEditTranslator() should be called to return the
     * identity.
     *
     * @param dataType the date type.
     * @return the translator which converts from the specified type to the
     * native type after editing.
     */
    public AttributeValueTranslator fromEditTranslator(final String dataType) {
        if (dataType.equals(getDataType())) {
            return AttributeValueTranslator.getNativeTranslator(dataType);
        }
        return AttributeValueTranslator.IDENTITY;
    }

    /**
     * Get the validator that will validate values from the specified type after
     * editing, prior to translation into the native type.
     * <p>
     * When overriding this method, if no specific validator matches in the
     * concrete class, super.fromEditValidator() should be called to return the
     * default validator (which validates all values). A common use case for
     * overriding this method is when it is desirable for an attribute type to
     * be 'edited' as a string, mainly for display purposes, but it does not
     * make sense to convert this string back into the desired type, hence a
     * validator that always fails can be used.
     *
     * @param dataType the data type.
     * @return the validator that will validate values from the specified type
     * after editing, prior to translation into the native type.
     */
    public ValueValidator<T> fromEditValidator(final String dataType) {
        return ValueValidator.getAlwaysSucceedValidator();
    }

    /**
     * Whether or not attribute values of this type are comparable.
     * <p>
     * This can be used by views to determine whether values of this attribute
     * may be ordered. When this method returns false, the view must order these
     * attribute values manually, noting that the ordering may as well be random
     * as this means they are truly not comparable.
     * {@link AttributeValueTranslator#IDENTITY}
     *
     * @return Whether or not attribute values of this type are comparable.
     */
    public boolean isComparable() {
        return getAllImplementedInterfaces(getValueType()).contains(Comparable.class);
    }

    private static Set<Class<?>> getAllImplementedInterfaces(final Class<?> concreteClass) {
        final Set<Class<?>> interfaces = new HashSet<>();
        final Queue<Class<?>> classesToCheck = new LinkedList<>();
        classesToCheck.add(concreteClass);
        while (!classesToCheck.isEmpty()) {
            final Class<?> currentClass = classesToCheck.remove();
            interfaces.addAll(Arrays.asList(currentClass.getInterfaces()));
            classesToCheck.addAll(Arrays.asList(currentClass.getInterfaces()));
            if (currentClass.getSuperclass() != null) {
                classesToCheck.add(currentClass.getSuperclass());
            }
        }
        return interfaces;
    }

    protected abstract Class<T> getValueType();

    /**
     * Get a comparable version of the supplied attribute value.
     * <p>
     * This method may be used by views that need to sort attribute values. When
     * the underlying attribute value is natively comparable, it is expected
     * that this method should act as the identity function on its argument.
     * <p>
     * Note that this should throw an UnsupportedOperationException when and
     * only when {@link #isComparable isComparable()} returns false.
     *
     * @param value an object representing the attribute value.
     * @return a comparable version of the supplied attribute value.
     */
    public Comparable<?> getComparable(final Object value) {
        if (isComparable()) {
            return (Comparable<?>) value;
        }
        throw new UnsupportedOperationException();
    }

    public final Comparator<Object> getComparator() {
        if (isComparable()) {
            return createObjectComparator(createComparator());
        }
        return hashcodeComparator;
    }

    protected Comparator<T> createComparator() {
        return valueComparator;
    }

    @SuppressWarnings("unchecked")
    private Comparator<Object> createObjectComparator(final Comparator<T> comparator) {
        return (o1, o2) -> {
            try {
                return comparator.compare((T) o1, (T) o2);
            } catch (final ClassCastException ex) {
                return 0;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private final Comparator<T> valueComparator = (o1, o2) -> {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        } else if (o2 == null) {
            return 1;
        } else {
            return ((Comparable<T>) o1).compareTo(o2);
        }
    };
    private final Comparator<Object> hashcodeComparator = (o1, o2) -> {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        } else if (o2 == null) {
            return 1;
        } else {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    };

    private static final Map<String, AbstractAttributeInteraction<?>> ALL_INTERACTIONS = new HashMap<>();

    /**
     * Get the interaction for the specified data type.
     * <p>
     * Uses lookup to find the right interaction. The Interactions of each type
     * returned by this method are singletons. If no interaction for the given
     * type is found, the default interaction is returned.
     *
     * @param dataType the data type.
     * @return the interaction for the specified data type.
     */
    @SuppressWarnings("rawtypes")
    public static AbstractAttributeInteraction getInteraction(final String dataType) {
        if (ALL_INTERACTIONS.isEmpty()) {
            final Collection<? extends AbstractAttributeInteraction> attributeInteractions = Lookup.getDefault().lookupAll(AbstractAttributeInteraction.class);
            attributeInteractions.forEach(interaction -> {
                if (!ALL_INTERACTIONS.containsKey(interaction.getDataType()) || ALL_INTERACTIONS.get(interaction.getDataType()).getPriority() < interaction.getPriority()) {
                    ALL_INTERACTIONS.put(interaction.getDataType(), interaction);
                }
            });
        }

        return ALL_INTERACTIONS.containsKey(dataType) ? ALL_INTERACTIONS.get(dataType) : DefaultAttributeInteraction.DEFAULT_ATTRIBUTE_INTERACTION;
    }
}
