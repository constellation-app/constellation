/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.CompositeNodeStateAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.TransactionTypeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.VertexTypeAttributeDescription;
import au.gov.asd.tac.constellation.utilities.BrandingUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * A SchemaConcept for elements which support analysis of a graph.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class, position = 500)
public class DuplicateConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Classified";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(SchemaConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }
  public static class TransactionType {
        //duplicate appear as separate entity
        public static final SchemaTransactionType DUPLICATE = new SchemaTransactionType.Builder("Duplicate")
                .setDescription("A transaction representing a communication between two entities, eg. a phone made a call to another phone")
                .setColor(ConstellationColor.EMERALD)
                .build();
        //copy appear as separate entity
        public static final SchemaTransactionType COPY = new SchemaTransactionType.Builder(AnalyticConcept.TransactionType.COMMUNICATION, "Copy")
                .setColor(ConstellationColor.EMERALD)
                .build();
        //override replaces communication  
        public static final SchemaTransactionType OVERRIDE = new SchemaTransactionType.Builder(AnalyticConcept.TransactionType.COMMUNICATION, "Override", true)
                .setColor(ConstellationColor.EMERALD)
                .build();
        //Child type Call appears
        public static final SchemaTransactionType CALL = new SchemaTransactionType.Builder("Call")
                .setSuperType(AnalyticConcept.TransactionType.COMMUNICATION)
                .build();
         //OverrideCall replaces Call   
        public static final SchemaTransactionType OVERRIDECALL = new SchemaTransactionType.Builder(TransactionType.CALL, "OverrideCall", true)
                .build();        
    }

    @Override
    public List<SchemaTransactionType> getSchemaTransactionTypes() {
        final List<SchemaTransactionType> schemaTransactionTypes = new ArrayList<>();
        schemaTransactionTypes.add(TransactionType.DUPLICATE);
        schemaTransactionTypes.add(TransactionType.COPY);
        schemaTransactionTypes.add(TransactionType.OVERRIDE);
        schemaTransactionTypes.add(TransactionType.CALL);
        schemaTransactionTypes.add(TransactionType.OVERRIDECALL);
        return Collections.unmodifiableList(schemaTransactionTypes);
    }

    @Override
    //overridden types
    public List<SchemaTransactionType> getOverwrittenSchemaTransactionTypes() {
        final List<SchemaTransactionType> overwrittenTypes = new ArrayList<>();
        overwrittenTypes.add(AnalyticConcept.TransactionType.COMMUNICATION);
        overwrittenTypes.add(TransactionType.CALL);
        return Collections.unmodifiableList(overwrittenTypes);
    }
}
