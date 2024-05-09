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
package au.gov.asd.tac.constellation.views.dataaccess.state;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessStateAttributeDescriptionNGTest {
    
    @Test
    public void init() {
        final DataAccessStateAttributeDescription description = new DataAccessStateAttributeDescription();
        
        assertEquals(description.getName(), "dataaccess_state");
    }
}
