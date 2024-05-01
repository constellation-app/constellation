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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Lookup;

/**
 *
 * @author twilight_sparkle
 * @param <V>
 */
public abstract class AttributeValueEditorFactory<V> extends AbstractEditorFactory<V> {

    public abstract String getAttributeType();

    protected int getPriority() {
        return 0;
    }

    private static Map<String, AttributeValueEditorFactory<?>> typeHandlers = null;

    @SuppressWarnings("rawtypes")
    public static void createTypeHandlers() {
        final Collection<? extends AttributeValueEditorFactory> handlers = Lookup.getDefault().lookupAll(AttributeValueEditorFactory.class);
        typeHandlers = new HashMap<>();
        handlers.forEach(handler -> {
            final String type = handler.getAttributeType();
            if (!typeHandlers.containsKey(type)) {
                typeHandlers.put(type, handler);
            } else {
                if (typeHandlers.get(type).getPriority() >= handler.getPriority()) {
                } else {
                    typeHandlers.put(type, handler);
                }
            }
        });
    }

    public static AttributeValueEditorFactory<?> getEditFactory(final String attributeType) {
        if (typeHandlers == null) {
            createTypeHandlers();
        }
        final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(attributeType);
        final List<String> editTypes = new ArrayList<>();
        editTypes.add(attributeType);
        editTypes.addAll(interaction.getPreferredEditTypes());

        String chosenEditType = null;
        for (final String editType : editTypes) {
            if (typeHandlers.containsKey(editType)) {
                chosenEditType = editType;
                break;
            }
        }

        return chosenEditType == null ? null : typeHandlers.get(chosenEditType);
    }
}
