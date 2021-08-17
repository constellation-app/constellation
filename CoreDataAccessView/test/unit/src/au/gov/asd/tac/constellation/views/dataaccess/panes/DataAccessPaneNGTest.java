///*
// * Copyright 2010-2021 Australian Signals Directorate
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package au.gov.asd.tac.constellation.views.dataaccess.panes;
//
//import au.gov.asd.tac.constellation.graph.Graph;
//import java.awt.GraphicsEnvironment;
//import java.util.Map;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.control.MenuItem;
//import javafx.scene.control.Tab;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.fail;
//import org.testng.SkipException;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
///**
// *
// * @author arcturus
// */
//public class DataAccessPaneNGTest {
//
//    public DataAccessPaneNGTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        // TODO Find a better solution for this. Because of this limitation these tests
//        //      will not be run on the CI server.
//        if (!GraphicsEnvironment.isHeadless()) {
//            // Interestingly once you throw the skip exception it doesn't call the tear down class
//            // so we need to instantiate the static mocks only once we know we will be running the
//            // tests.
//            new JFXPanel();
//        } else {
//            throw new SkipException("This class requires the build to have a display present.");
//        }
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//
//    @BeforeMethod
//    public void setUpMethod() throws Exception {
//    }
//
//    @AfterMethod
//    public void tearDownMethod() throws Exception {
//    }
//
//    /**
//     * Test of sortPlugins method, of class DataAccessPane.
//     */
//    @Test
//    public void testSortPlugins() {
//        System.out.println("sortPlugins");
//        Map<String, Integer> typesWithPosition = null;
//        DataAccessPane instance = null;
//        instance.sortPlugins(typesWithPosition);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of lookupPlugins method, of class DataAccessPane.
//     */
//    @Test
//    public void testLookupPlugins() {
//        System.out.println("lookupPlugins");
//        Map expResult = null;
//        Map result = DataAccessPane.lookupPlugins();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of newTab method, of class DataAccessPane.
//     */
//    @Test
//    public void testNewTab() {
//        System.out.println("newTab");
//
//        DataAccessPane dataAcessPane = new DataAccessPane();
//        QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
//        MenuItem menuItem = mock(MenuItem.class);
//
//        dataAcessPane.newTab(queryPhasePane);
//
////        verify(queryPhasePane, times(1)).addGraphDependentMenuItems(menuItem, menuItem, menuItem);
//        verify(queryPhasePane, times(1)).addPluginDependentMenuItems(menuItem);
//
//    }
//
//    /**
//     * Test of getCurrentTab method, of class DataAccessPane.
//     */
//    @Test
//    public void testGetCurrentTab() {
//        System.out.println("getCurrentTab");
//        DataAccessPane instance = null;
//        Tab expResult = null;
//        Tab result = instance.getCurrentTab();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeTabs method, of class DataAccessPane.
//     */
//    @Test
//    public void testRemoveTabs() {
//        System.out.println("removeTabs");
//        DataAccessPane instance = null;
//        instance.removeTabs();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of update method, of class DataAccessPane.
//     */
//    @Test
//    public void testUpdate_0args() {
//        System.out.println("update");
//        DataAccessPane instance = null;
//        instance.update();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of update method, of class DataAccessPane.
//     */
//    @Test
//    public void testUpdate_Graph() {
//        System.out.println("update");
//        Graph graph = null;
//        DataAccessPane instance = null;
//        instance.update(graph);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hierarchicalUpdate method, of class DataAccessPane.
//     */
//    @Test
//    public void testHierarchicalUpdate() {
//        System.out.println("hierarchicalUpdate");
//        DataAccessPane instance = null;
//        instance.hierarchicalUpdate();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getQueryPhasePane method, of class DataAccessPane.
//     */
//    @Test
//    public void testGetQueryPhasePane() {
//        System.out.println("getQueryPhasePane");
//        Tab tab = null;
//        QueryPhasePane expResult = null;
//        QueryPhasePane result = DataAccessPane.getQueryPhasePane(tab);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validityChanged method, of class DataAccessPane.
//     */
//    @Test
//    public void testValidityChanged() {
//        System.out.println("validityChanged");
//        boolean enabled = false;
//        DataAccessPane instance = null;
//        instance.validityChanged(enabled);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of qualityControlRuleChanged method, of class DataAccessPane.
//     */
//    @Test
//    public void testQualityControlRuleChanged() {
//        System.out.println("qualityControlRuleChanged");
//        boolean canRun = false;
//        DataAccessPane instance = null;
//        instance.qualityControlRuleChanged(canRun);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//}
