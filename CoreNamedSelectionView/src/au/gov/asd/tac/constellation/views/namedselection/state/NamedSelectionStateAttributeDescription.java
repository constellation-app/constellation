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
package au.gov.asd.tac.constellation.views.namedselection.state;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class used in registration of NamedSelection for saving and loading to/from a
 * graph.
 *
 * @author betelgeuse
 */
@ServiceProvider(service = AttributeDescription.class)
public class NamedSelectionStateAttributeDescription extends ObjectAttributeDescription {

    public static final String ATTRIBUTE_NAME = NamedSelectionState.ATTRIBUTE_NAME;

    public NamedSelectionStateAttributeDescription() {
        super(ATTRIBUTE_NAME);
    }
}
