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
package au.gov.asd.tac.constellation.graph.schema;

import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * A schema which performs no operations and is used simply as a fallback.
 *
 * @author sirius
 */
@ServiceProvider(service = SchemaFactory.class, position = Integer.MAX_VALUE)
public class BareSchemaFactory extends SchemaFactory implements Serializable {

    // Note: changing this value will break backwards compatibility!
    public static final String NAME = "au.gov.asd.tac.constellation.graph.schema.BasicSchemaFactory";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Bare Graph";
    }

    @Override
    public String getDescription() {
        return "A graph with no predefined attributes";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getRegisteredConcepts() {
        return Collections.unmodifiableSet(new HashSet<>());
    }

    @Override
    public boolean isPrimarySchema() {
        return false;
    }

    @Override
    public Schema createSchema() {
        return new BareSchema(this);
    }

    protected class BareSchema extends Schema implements Serializable {

        public BareSchema(final SchemaFactory factory) {
            super(factory);
        }
    }
}
