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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class BlazePluginUtilitiesNGTest {
    
    private PluginParameters params;
    private BitSet vxIds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        params = new PluginParameters();
        
        final PluginParameter<ObjectParameterValue> vertexIdsParam = ObjectParameterType.build(VERTEX_IDS_PARAMETER_ID);
        params.addParameter(vertexIdsParam);
        
        vxIds = new BitSet();
        vxIds.set(0);
        vxIds.set(2);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of verticesParam method, of class BlazePluginUtilities. Passing no vertex ids
     */
    @Test
    public void testVerticesParamNoIdsPassed() {
        System.out.println("verticesParamNoIdsPassed");
        
        assertNull(BlazePluginUtilities.verticesParam(params));
    }
    
    /**
     * Test of verticesParam method, of class BlazePluginUtilities. Passing Bitset of ids
     */
    @Test
    public void testVerticesParamBitSetIds() {
        System.out.println("verticesParamBitSetIds");
        
        params.setObjectValue(VERTEX_IDS_PARAMETER_ID, vxIds);
        
        // should return the same bitset it was given
        assertEquals(BlazePluginUtilities.verticesParam(params), vxIds);
    }
    
    /**
     * Test of verticesParam method, of class BlazePluginUtilities. Passing Integer list of ids
     */
    @Test
    public void testVerticesParamIntegerListIds() {
        System.out.println("verticesParamIntegerListIds");
        
        final List<Integer> vxIdList = Arrays.asList(0, 2);
        params.setObjectValue(VERTEX_IDS_PARAMETER_ID, vxIdList);
        
        // should return the bitset equivalent of the integer list
        assertEquals(BlazePluginUtilities.verticesParam(params), vxIds);
    }
    
    /**
     * Test of verticesParam method, of class BlazePluginUtilities. Passing String list of ids
     */
    @Test
    public void testVerticesParamStringListIds() {
        System.out.println("verticesParamStringListIds");
        
        final List<String> vxIdList = Arrays.asList("0", "2");
        params.setObjectValue(VERTEX_IDS_PARAMETER_ID, vxIdList);
        
        // should return the bitset equivalent of the integer list
        assertEquals(BlazePluginUtilities.verticesParam(params), vxIds);
    }
}
