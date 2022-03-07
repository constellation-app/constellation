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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.io;

import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractUncachedStringIOProvider;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ConnectionModeAttributeDescription;
import org.openide.util.lookup.ServiceProvider;

/**
 * IOProvider for attributes described by
 * {@link au.gov.asd.tac.constellation.graph.visual.attribute.ConnectionModeAttributeDescription}
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractGraphIOProvider.class)
public class ConnectionModeIOProvider extends AbstractUncachedStringIOProvider {

    /**
     * Get a string representing the type of data that this provider handles.
     * 
     * @return A unique name indicating the type of data handled by this
     * provider.
     */
    @Override
    public String getName() {
        return ConnectionModeAttributeDescription.ATTRIBUTE_NAME;
    }
}
