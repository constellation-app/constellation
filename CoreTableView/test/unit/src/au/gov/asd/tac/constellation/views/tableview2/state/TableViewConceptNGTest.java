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
package au.gov.asd.tac.constellation.views.tableview2.state;

import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewConcept;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewConceptNGTest {
    
    private TableViewConcept tableViewConcept;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        tableViewConcept = new TableViewConcept();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void getName() {
        assertEquals(tableViewConcept.getName(), "Table View");
    }
    
    @Test
    public void getParents() {
        assertEquals(tableViewConcept.getParents(), 
                Collections.unmodifiableSet(Set.of(
                        SchemaConcept.ConstellationViewsConcept.class)
                ));
    }
    
    @Test
    public void getSchemaAttributes() {
        assertEquals(tableViewConcept.getSchemaAttributes().iterator().next(), 
                Collections.unmodifiableCollection(List.of(
                        TableViewConcept.MetaAttribute.TABLE_VIEW_STATE)
                ).iterator().next());
    }
}
