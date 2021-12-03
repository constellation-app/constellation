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
package au.gov.asd.tac.constellation.utilities.visual;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class VisualChangeBuilderNGTest {
    
    public VisualChangeBuilderNGTest() {
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
     * Test of testConstructor method, of class VisualChangeBuilder.
     */
    @Test
    public void testConstructor() {
        System.out.println("testConstructor");
        final VisualProperty property = mock(VisualProperty.class);
        VisualChangeBuilder prop = new VisualChangeBuilder(property);
        VisualChange vc = prop.build();
        
        assertEquals(vc.property, property);
    }
    
    /**
     * Test of generateNewId method, of class VisualChangeBuilder.
     */
    @Test
    public void testGenerateNewId() {
        System.out.println("generateNewId");
        
        long result = VisualChangeBuilder.generateNewId();
        long initialId = result;
        assertEquals(result, initialId);
        
        result = VisualChangeBuilder.generateNewId();
        assertEquals(result, initialId + 1);
        
        result = VisualChangeBuilder.generateNewId();
        assertEquals(result, initialId + 2);
    }

    /**
     * Test of withId method, of class VisualChangeBuilder.
     */
    @Test
    public void testWithId() {
        System.out.println("withId");
        final VisualProperty property = mock(VisualProperty.class);
        VisualChangeBuilder prop = new VisualChangeBuilder(property);
        VisualChangeBuilder result = prop.withId(1L);
        assertEquals(result.getId(), 1L);
        result = prop.withId(9875L);
        assertEquals(result.getId(), 9875L);
    }

    /**
     * Test of forItems method, of class VisualChangeBuilder.
     */
    @Test
    public void testForItems_int() {
        System.out.println("forItems_int");
        final VisualProperty property = mock(VisualProperty.class);
        VisualChangeBuilder prop = new VisualChangeBuilder(property);
        VisualChangeBuilder result = prop.forItems(1);
        assertEquals(result.getChangedItemsCount(), 1);
        result = prop.forItems(4578345);
        assertEquals(result.getChangedItemsCount(), 4578345);
    }

    /**
     * Test of forItems method, of class VisualChangeBuilder.
     */
    @Test
    public void testForItems_intArr() {
        System.out.println("forItems_arr");
        
        final int[] changeList = new int[25];
        for(int i=0;i<changeList.length;i++){
            changeList[i] = i;
        }
        
        final VisualProperty property = mock(VisualProperty.class);
        VisualChangeBuilder prop = new VisualChangeBuilder(property);
        VisualChangeBuilder result = prop.forItems(changeList);
        assertEquals(result.getChangedItems(), changeList);
        
        result = prop.forItems(null);
        assertEquals(result.getChangedItems(), null);
    }

    /**
     * Test of build method, of class VisualChangeBuilder.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        final VisualProperty property = mock(VisualProperty.class);
        final int[] changeList = new int[25];
        for(int i=0;i<changeList.length;i++){
            changeList[i] = i;
        }
        final long id = VisualChangeBuilder.generateNewId();
        VisualChangeBuilder prop = new VisualChangeBuilder(property).withId(id).forItems(changeList);
        VisualChange vc = prop.build();
        
        assertEquals(vc.getChangeList(), changeList);
        assertEquals(vc.id, id);
        assertEquals(vc.property, property);
    }
}
