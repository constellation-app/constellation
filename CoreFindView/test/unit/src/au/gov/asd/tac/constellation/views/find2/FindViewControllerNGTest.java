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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewControllerNGTest {

    public FindViewControllerNGTest() {
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
     * Test of getDefault method, of class FindViewController.
     */
    @Test
    public void testGetDefault() {
        FindViewController instance = FindViewController.getDefault();
        assertEquals(instance, FindViewController.getDefault());
    }

    /**
     * Test of init method, of class FindViewController.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        FindViewTopComponent parentComponent = null;
        FindViewController instance = null;
        FindViewController expResult = null;
        FindViewController result = instance.init(parentComponent);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of updateUI method, of class FindViewController.
     *
     * I need access to UI elements
     */
    @Test
    public void testUpdateUI() {
        System.out.println("updateUI");
        FindViewController instance = null;
        instance.updateUI();
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of populateAttributes method, of class FindViewController.
     *
     * I need a graph
     */
    @Test
    public void testPopulateAttributes() {
        System.out.println("populateAttributes");
        GraphElementType type = GraphElementType.VERTEX;
        List<Attribute> attributes = new ArrayList<>();
        long attributeModificationCounter = 0L;
        FindViewController instance = FindViewController.getDefault();
        List<String> expResult = new ArrayList<>();
        expResult.add("Label");
        expResult.add("Identifier");
        expResult.add("Source");
        List result = instance.populateAttributes(type, attributes, attributeModificationCounter);
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
    }


    /**
     * Test of updateBasicParameters method, of class FindViewController.
     */
    @Test
    public void testUpdateBasicParameters() {
        System.out.println("updateBasicParameters");
        BasicFindReplaceParameters parameters = null;
        FindViewController instance = null;
        instance.updateBasicFindParameters(parameters);
        // TODO review the generated test code and remove the default call to fail.
    }


    /**
     * Test of retriveMatchingElements method, of class FindViewController.
     */
    @Test
    public void testRetriveMatchingElements() {
        System.out.println("retriveMatchingElements");
        boolean selectAll = false;
        boolean getNext = false;
        FindViewController instance = null;
        instance.retriveMatchingElements(selectAll, getNext);
        // TODO review the generated test code and remove the default call to fail.
    }

}
