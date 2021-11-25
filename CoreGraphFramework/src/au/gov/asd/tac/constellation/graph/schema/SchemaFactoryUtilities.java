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
package au.gov.asd.tac.constellation.graph.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
public class SchemaFactoryUtilities {

    private static SchemaFactory DEFAULT_SCHEMA = null;
    private static Map<String, SchemaFactory> SCHEMA_FACTORIES = null;
    
    private SchemaFactoryUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static final SchemaFactory getDefaultSchemaFactory() {
        if (DEFAULT_SCHEMA == null) {
            DEFAULT_SCHEMA = Lookup.getDefault().lookup(SchemaFactory.class);
        }

        return DEFAULT_SCHEMA;
    }

    /**
     * Returns instances of all registered SchemaFactory classes mapped by name.
     * The map returned is unmodifiable and its iterators will return the
     * SchemaFactory classes in order of position (a value assigned when
     * registering a class as a {@link ServiceProvider}, highest first).
     *
     * @return Instances of all registered SchemaFactory classes mapped by their
     * names.
     */
    public static final Map<String, SchemaFactory> getSchemaFactories() {
        if (SCHEMA_FACTORIES == null) {
            final Collection<? extends SchemaFactory> schemaFactories = Lookup.getDefault().lookupAll(SchemaFactory.class);

            SCHEMA_FACTORIES = new LinkedHashMap<>();
            schemaFactories.forEach(schemaFactory -> SCHEMA_FACTORIES.put(schemaFactory.getName(), schemaFactory));

            SCHEMA_FACTORIES = Collections.unmodifiableMap(SCHEMA_FACTORIES);
        }

        return SCHEMA_FACTORIES;
    }

    /**
     * Get an instance of the SchemaFactory class with the specified name or
     * null if no SchemaFactory has been registered with that name.
     *
     * @param name The name of a registered SchemaFactory.
     *
     * @return An instance of the SchemaFactory with the specified name.
     */
    public static final SchemaFactory getSchemaFactory(final String name) {
        if (name == null) {
            return null;
        }

        return getSchemaFactories().get(name);
    }
}
