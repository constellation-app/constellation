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
package au.gov.asd.tac.constellation.views.dataaccess.plugins;

import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType.PositionalDataAccessPluginType;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DataAccessPluginCoreTypeNGTest {

    public DataAccessPluginCoreTypeNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getPluginTypeList method, of class DataAccessPluginCoreType.
     */
    @Test
    public void testGetPluginTypeList() {
        System.out.println("getPluginTypeList");

        final DataAccessPluginCoreType instance = new DataAccessPluginCoreType();
        final List<PositionalDataAccessPluginType> pluginTypeList = instance.getPluginTypeList();
        assertEquals(pluginTypeList.size(), 9);

        assertEquals(pluginTypeList.get(0).type, "Favourites");
        assertEquals(pluginTypeList.get(1).type, "Import");
        assertEquals(pluginTypeList.get(2).type, "Extend");
        assertEquals(pluginTypeList.get(3).type, "Enrichment");
        assertEquals(pluginTypeList.get(4).type, "Clean");
        assertEquals(pluginTypeList.get(5).type, "Workflow");
        assertEquals(pluginTypeList.get(6).type, "Utility");
        assertEquals(pluginTypeList.get(7).type, "Experimental");
        assertEquals(pluginTypeList.get(8).type, "Developer");
    }

}
