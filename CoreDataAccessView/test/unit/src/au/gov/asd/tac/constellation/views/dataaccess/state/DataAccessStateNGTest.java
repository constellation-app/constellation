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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DataAccessStateNGTest extends ConstellationTest {

    private DataAccessState dataAccessState;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessState = new DataAccessState();
    }

    @Test
    public void testConstructorEmptyList() {
        assertEquals(dataAccessState.getState(), Collections.emptyList());
    }

    @Test
    public void testNewTabNewHashMap() {
        dataAccessState.newTab();
        assertEquals(dataAccessState.getState(), List.of(new HashMap<>()));
    }

    @Test
    public void testAddPutsInLastTab() {
        final String key = "testKey";
        final String value = "testValue";

        dataAccessState.newTab();
        dataAccessState.newTab();
        dataAccessState.add(key, value);

        // Verify two tabs
        assertEquals(dataAccessState.getState().size(), 2);

        // Verify first tab has no entries
        assertTrue(dataAccessState.getState().get(0).isEmpty());

        // Verify the second tab has one entry and it is the expected key/value pair
        assertEquals(dataAccessState.getState().get(1).size(), 1);
        assertEquals(dataAccessState.getState().get(1).get(key), value);
    }
    
    @Test
    public void equality() {
        EqualsVerifier.forClass(DataAccessState.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
