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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * Tools Test.
 *
 * @author algol
 */
public class IOUtilitiesNGTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getGraphElementTypeString_null_type() {
        IoUtilities.getGraphElementTypeString(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void getGraphElementTypeString_unhandled_type() {
        IoUtilities.getGraphElementTypeString(GraphElementType.LINK);
    }
    
    @Test
    public void getGraphElementTypeString() {
        assertEquals(IoUtilities.getGraphElementTypeString(GraphElementType.GRAPH), "graph");
        assertEquals(IoUtilities.getGraphElementTypeString(GraphElementType.VERTEX), "vertex");
        assertEquals(IoUtilities.getGraphElementTypeString(GraphElementType.TRANSACTION), "transaction");
        assertEquals(IoUtilities.getGraphElementTypeString(GraphElementType.META), "meta");
    }
}
