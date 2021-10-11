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
package au.gov.asd.tac.constellation.views.dataaccess.state;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessConceptNGTest {
    private DataAccessConcept dataAccessConcept;
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
       dataAccessConcept = new DataAccessConcept();
    }
    
    @Test
    public void getName() {
        assertEquals(dataAccessConcept.getName(), "Data Access");
    }
    
    @Test
    public void getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = dataAccessConcept.getParents();
        
        assertEquals(parentSet, Set.of(SchemaConcept.ConstellationViewsConcept.class));
    }
    
    @Test
    public void metadataAttribute() {
        assertEquals(DataAccessConcept.MetaAttribute.DATAACCESS_STATE.getElementType(), GraphElementType.META);
        assertEquals(DataAccessConcept.MetaAttribute.DATAACCESS_STATE.getAttributeType(), "dataaccess_state");
        assertEquals(DataAccessConcept.MetaAttribute.DATAACCESS_STATE.getName(), "dataaccess_state");
        assertEquals(DataAccessConcept.MetaAttribute.DATAACCESS_STATE.getDescription(), "The current state of the data access with relation to this graph");
    }
    
    @Test
    public void getSchemaAttributes() {
        final Collection<SchemaAttribute> graphAttributes = dataAccessConcept.getSchemaAttributes();
        
        assertEquals(graphAttributes, List.of(DataAccessConcept.MetaAttribute.DATAACCESS_STATE));
    }
}
