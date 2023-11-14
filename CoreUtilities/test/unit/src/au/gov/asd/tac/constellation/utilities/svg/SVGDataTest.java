/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.svg;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link SVGData}
 * 
 * @author capricornunicorn123
 */
public class SVGDataTest {
        final String typeRect = "rect";
        final String typeCircle = "circle";
        final String typeSVG = "svg";
        
        final String id1 = "a";
        final String id2 = "b";
        final String id3 = "c";
        final String id4 = "d";
        final String id5 = "e";
        
        final String x1 = "32";
        final String x2 = "64";
        final String y1 = "128";
        final String y2 = "256";
        
        final String color1 = "#ffffff";
        final String color2 = "#000000";
        
        final SVGData dataSVG1;
        final SVGData dataSVG2;
        final SVGData dataSVG3;
        final SVGData dataSVG4;
        final SVGData dataRect;
        final SVGData dataCircle;
        
        final String dataSVG4String = String.format("\n<svg id=\"%s\" x=\"%s\" y=\"%s\" fill=\"%s\" />", id1, x1, y1, color1);
        
        
    public SVGDataTest() {
        
        dataSVG1 = new SVGData(typeSVG, null, null);
        dataSVG2 = new SVGData(typeSVG, null, null);
        dataSVG3 = new SVGData(typeSVG, null, null);
        dataSVG4 = new SVGData(typeSVG, null, null);
        dataRect = new SVGData(typeRect, null, null);
        dataCircle = new SVGData(typeCircle, null, null);       
    }
    
    @BeforeClass
    public static void setUpClass() {

    }
    
    @AfterClass
    public static void tearDownClass() {
        
    }
    
    @Before
    public void setUp() {
        dataSVG1.setAttribute(SVGAttributeConstant.ID, id1);
        dataSVG2.setAttribute(SVGAttributeConstant.ID, id2);
        dataSVG3.setAttribute(SVGAttributeConstant.ID, id3);
        dataCircle.setAttribute(SVGAttributeConstant.ID, id4);
        dataRect.setAttribute(SVGAttributeConstant.ID, id5);
        
        dataSVG2.setParent(dataSVG1);
        dataSVG3.setParent(dataSVG2);
        dataRect.setParent(dataSVG2);
        dataCircle.setParent(dataSVG2);
        
        dataSVG4.setAttribute(SVGAttributeConstant.ID, id1);
        dataSVG4.setAttribute(SVGAttributeConstant.X, x1);
        dataSVG4.setAttribute(SVGAttributeConstant.Y, y1);
        dataSVG4.setAttribute(SVGAttributeConstant.FILL_COLOR, color1);
        
        dataSVG1.setAttribute(SVGAttributeConstant.X, x2);
        dataSVG1.setAttribute(SVGAttributeConstant.Y, y2);
        dataSVG1.setAttribute(SVGAttributeConstant.STROKE_COLOR, color2);

    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getType method, of class SVGData.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        
        assertEquals(typeSVG, dataSVG1.getType());
        assertEquals(typeRect, dataRect.getType());

    }

    /**
     * Test of getChild method, of class SVGData.
     */
    @Test
    public void testGetChild() {
        System.out.println("getChild");
        
        // Test retriving a child 1 level below the element.
        assertEquals(dataSVG1.getChild(id2), dataSVG2);
        
        // Test retriving a child 2 levels below the element.
        assertEquals(dataSVG1.getChild(id3), dataSVG3);
        
        // Test retriving a non existant child.
        assertNull(dataSVG2.getChild(id1));
    }

    /**
     * Test of removeChild method, of class SVGData.
     */
    @Test
    public void testRemoveChild() {
        System.out.println("removeChild");
        
        // Test retreiving child 2 levels below the element.
        assertEquals(dataSVG1.getChild(id3), dataSVG3);
        
        // Test removing a child 2 level below the element.
        dataSVG1.removeChild(id3);
        assertNull(dataSVG1.getChild(id3));
        
        // Test retriving a child 1 level below the element.
        assertEquals(dataSVG1.getChild(id2), dataSVG2);
        
        // Test removing a child 1 level below the element.
        dataSVG1.removeChild(id2);
        assertNull(dataSVG1.getChild(id2));    

    }

    /**
     * Test of getAllChildren method, of class SVGData.
     */
    @Test
    public void testGetAllChildren() {
        System.out.println("getAllChildren");
        
        assertTrue(dataSVG1.getAllChildren().contains(dataSVG2));
        assertTrue(dataSVG2.getAllChildren().contains(dataRect));
        assertEquals(3, dataSVG2.getAllChildren().size());
        assertTrue(dataRect.getAllChildren().isEmpty());
        
        dataSVG3.setAttribute(SVGAttributeConstant.CUSTOM_SORT_ORDER, "1");
        dataRect.setAttribute(SVGAttributeConstant.CUSTOM_SORT_ORDER, "3");
        dataCircle.setAttribute(SVGAttributeConstant.CUSTOM_SORT_ORDER, "2");
        
        List<SVGData> children = dataSVG2.getAllChildren();
        
        assertEquals(children.get(2), dataSVG3);
        assertEquals(children.get(1), dataCircle);
        assertEquals(children.get(0), dataRect);
    }

    /**
     * Test of setParent method, of class SVGData.
     */
    @Test
    public void testSetParent() {
        System.out.println("setParent");
        testGetSetParent();
    }

    /**
     * Test of getParent method, of class SVGData.
     */
    @Test
    public void testGetParent() {
        System.out.println("getParent");
        testGetSetParent();

    }

    /**
     * Test of setAttribute method, of class SVGData.
     */
    @Test
    public void testSetAttribute() {
        System.out.println("setAttribute");
        testGetSetAttribute();
    }

    /**
     * Test of getAttributeValue method, of class SVGData.
     */
    @Test
    public void testGetAttributeValue() {
        System.out.println("getAttributeValue");
        testGetSetAttribute();
    }

    /**
     * Test of setContent method, of class SVGData.
     */
    @Test
    public void testSetContent() {
        System.out.println("setContent");
        testGetSetContent();
        
    }

    /**
     * Test of getContent method, of class SVGData.
     */
    @Test
    public void testGetContent() {
        System.out.println("getContent");
        testGetSetContent();
        
    }

    /**
     * Test of toString method, of class SVGData.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        assertEquals(dataSVG4String, dataSVG4.toString());
    }

    /**
     * Test of cleanAttributes method, of class SVGData.
     */
    @Test
    public void testCleanAttributes() {
        System.out.println("cleanAttributes");
        dataSVG4.setAttribute(SVGAttributeConstant.FILTER, null);
        System.out.println("toString");
        assertEquals(dataSVG4String, dataSVG4.toString());

    }

    /**
     * Test of loadFromTemplate method, of class SVGData.
     */
    @Test
    public void testLoadFromTemplate() {
        SVGData loadedData = SVGData.loadFromTemplate(TestingSVGFile.TESTING_TEMPLATE_COMPLIANT);
        assertNotNull(loadedData);
        CommonTests.testLoadedData(loadedData);
    }
    
    private void testGetSetContent() {
        final String content = "1";
        assertNull(dataSVG1.getContent());
        dataSVG1.setContent(content);
        assertEquals(dataSVG1.getContent(), content);
    }

    private void testGetSetAttribute() {
        assertNull(dataSVG1.getAttributeValue(SVGAttributeConstant.WIDTH.getName()));
        assertNull(dataSVG1.getAttributeValue(SVGAttributeConstant.VIEW_BOX.getName()));
        
        dataSVG1.setAttribute(SVGAttributeConstant.WIDTH, "42");
        dataSVG1.setAttribute(SVGAttributeConstant.VIEW_BOX, "0, 0, 256, 256");
 
        assertEquals(dataSVG1.getAttributeValue(SVGAttributeConstant.WIDTH.getName()), "42");
        assertEquals(dataSVG1.getAttributeValue(SVGAttributeConstant.VIEW_BOX.getName()), "0, 0, 256, 256");
    }

    private void testGetSetParent() {
        assertNull(dataSVG4.getParent());
        dataSVG4.setParent(dataSVG1);
        assertEquals(dataSVG4.getParent(), dataSVG1);
        assertTrue(dataSVG1.getAllChildren().contains(dataSVG4));
    }
    
}
