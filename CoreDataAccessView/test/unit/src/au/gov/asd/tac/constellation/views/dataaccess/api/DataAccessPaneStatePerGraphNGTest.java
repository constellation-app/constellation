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
package au.gov.asd.tac.constellation.views.dataaccess.api;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessPaneStatePerGraphNGTest {
    
    @Test
    public void equality() {
        EqualsVerifier.forClass(DataAccessPaneStatePerGraph.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void init() {
        final DataAccessPaneStatePerGraph state = new DataAccessPaneStatePerGraph();
        assertEquals(state.isQueriesRunning(), false);
        assertEquals(state.isExecuteButtonIsGo(), true);
        assertTrue(state.getRunningPlugins().isEmpty());
    }
}
