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
package au.gov.asd.tac.constellation.graph.versioning;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;

/**
 * Schema Update Provider
 *
 * @author twilight_sparkle
 */
public abstract class SchemaUpdateProvider implements UpdateProvider {

    private static final int SCHEMA_UPDATE_PRIORITY = 100;
    private final SchemaUpdateItem updateItem;

    public final class SchemaUpdateItem extends UpdateItem {

        private final int schemaHierarchyPriority;

        public SchemaUpdateItem() {
            int numParentSchemas = 0;
            Class<?> schemaClass = getSchema().getClass();
            while (!schemaClass.equals(SchemaFactory.class)) {
                schemaClass = schemaClass.getSuperclass();
                numParentSchemas++;
            }
            schemaHierarchyPriority = numParentSchemas;
        }

        @Override
        public boolean appliesToGraph(final StoreGraph graph) {
            return getSchema().getClass().isAssignableFrom(graph.getSchema().getFactory().getClass());
        }

        @Override
        public int getPriority() {
            return SCHEMA_UPDATE_PRIORITY + schemaHierarchyPriority;

        }

        @Override
        public String getName() {
            return getSchema().getName();
        }
    }

    protected SchemaUpdateProvider() {
        updateItem = new SchemaUpdateItem();
    }

    @Override
    public UpdateItem getVersionedItem() {
        return updateItem;
    }

    protected abstract SchemaFactory getSchema();

    @Override
    public void configure(final StoreGraph graph) {
        // do nothing
    }

    @Override
    public void update(final StoreGraph graph) {
        schemaUpdate(graph);
    }

    protected abstract void schemaUpdate(final StoreGraph graph);
}
