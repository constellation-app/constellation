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
package au.gov.asd.tac.constellation.plugins.update;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.update.GraphUpdateController.AttributeUpdateComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension of {@link UpdateComponent} that will update with changes to the
 * value of one of several graph attributes. It is tied to a
 * {@link GraphUpdateController} which will register changes for this
 * UpdateComponent.
 * <p>
 * This component is used in a similar manner to the
 * {@link AttributeUpdateComponent} created by
 * {@link GraphUpdateController#createAttributeUpdateComponent createAttributeUpdateComponent},
 * except that it facilitates having multiple attributes of interest
 * simultaneously so that this component will be updated when there is change to
 * the value of any of the attributes of interest.
 * {@link MultiAttributeUpdateComponent#updateAttributes(java.util.List) updateAttributes}
 * should be called to set the attributes of interest.
 *
 * @see GraphUpdateController
 * @see AttributeUpdateComponent
 * @see UpdateComponent
 * @author sirius
 */
public class MultiAttributeUpdateComponent extends UpdateComponent<GraphReadMethods> {

    private final GraphUpdateController graphUpdateController;

    private final List<AttributeUpdateComponent> attributeUpdateComponents = new ArrayList<>();

    /**
     * Creates a new MultiAttributeUpdateComponent tied to the specified
     * GraphUpdateController
     *
     * @param graphUpdateController The GraphUpdateController to register
     * changes for this component.
     */
    public MultiAttributeUpdateComponent(GraphUpdateController graphUpdateController) {
        this.graphUpdateController = graphUpdateController;
    }

    @Override
    public boolean update(GraphReadMethods updateState) {
        return true;
    }

    /**
     * Set the attributes of interest, thereby ensuring this component will
     * update in response to changes in the values of the attributes on the
     * current graph. The update controller is locked while the attributes are
     * set.
     *
     * @param elementType The element type of the attributes
     * @param attributes The list of labels of the attributes
     */
    public void updateAttributes(GraphElementType elementType, List<String> attributes) {
        updateAttributes(elementType, attributes, true);
    }

    /**
     * Set the attributes of interest, thereby ensuring this component will
     * update in response to changes in the values of the attributes on the
     * current graph. The update controller is locked while the attributes are
     * being set.
     *
     * @param attributes The SchemaAttribute of interest.
     */
    public void updateAttributes(List<SchemaAttribute> attributes) {
        updateAttributes(attributes, true);
    }

    /**
     * Set the attributes of interest, thereby ensuring this component will
     * update in response to changes in the values of the attributes on the
     * current graph.
     *
     * @param elementType The element type of the attributes
     * @param attributes A list of the labels of the attributes
     * @param lock Whether or not to lock the update controller so that the
     * update cycle can't begin while the component's attributes of interest are
     * being set
     */
    public void updateAttributes(GraphElementType elementType, List<String> attributes, boolean lock) {

        if (lock) {
            graphUpdateController.getUpdateController().lock();
        }

        try {
            int attributeCount = 0;
            for (String attribute : attributes) {
                if (attributeUpdateComponents.size() <= attributeCount) {
                    AttributeUpdateComponent attributeUpdateComponent = graphUpdateController.createAttributeUpdateComponent(elementType, attribute, false);
                    attributeUpdateComponents.add(attributeUpdateComponent);
                    dependOn(attributeUpdateComponent);
                } else {
                    attributeUpdateComponents.get(attributeCount).setAttribute(elementType, attribute, false);
                }
                attributeCount++;
            }
            while (attributeCount < attributeUpdateComponents.size()) {
                attributeUpdateComponents.get(attributeCount++).disable(false);
            }

        } finally {
            if (lock) {
                graphUpdateController.getUpdateController().release();
            }
        }
    }

    /**
     * Set the attributes of interest, thereby ensuring this component will
     * update in response to changes in the values of the attributes on the
     * current graph.
     *
     * @param attributes A list of the SchemaAttributes of interest
     * @param lock Whether or not to lock the update controller so that the
     * update cycle can't begin while the attributes are being set.
     */
    public void updateAttributes(List<SchemaAttribute> attributes, boolean lock) {

        if (lock) {
            graphUpdateController.getUpdateController().lock();
        }

        try {
            int attributeCount = 0;
            for (SchemaAttribute attribute : attributes) {
                if (attributeUpdateComponents.size() <= attributeCount) {
                    AttributeUpdateComponent attributeUpdateComponent = graphUpdateController.createAttributeUpdateComponent(attribute, false);
                    attributeUpdateComponents.add(attributeUpdateComponent);
                    dependOn(attributeUpdateComponent);
                } else {
                    attributeUpdateComponents.get(attributeCount).setAttribute(attribute, false);
                }
                attributeCount++;
            }
            while (attributeCount < attributeUpdateComponents.size()) {
                attributeUpdateComponents.get(attributeCount++).disable(false);
            }

        } finally {
            if (lock) {
                graphUpdateController.getUpdateController().release();
            }
        }
    }

}
