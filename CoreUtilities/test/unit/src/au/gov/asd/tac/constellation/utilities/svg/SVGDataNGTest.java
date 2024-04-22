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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * Tests for {@link SVGData}
 * 
 * @author capricornunicorn123
 */
public class SVGDataNGTest {

    SVGData svgObjectBlank1;
    SVGData svgObjectBlank2;
    SVGData svgObjectBlank3;
    SVGData svgObjectBlank4;

    final SVGTypeConstants typeRect = SVGTypeConstants.RECT;
    final SVGTypeConstants typeSVG = SVGTypeConstants.SVG;
        
    public SVGDataNGTest() {    

    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @BeforeMethod
    public void setUp() {
    }        
    
    @AfterMethod
    public void tearDown() {
    }

    /**
     * Test of getType(), of class SVGData.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        svgObjectBlank1 = new SVGData(typeSVG, null, null);
        svgObjectBlank4 = new SVGData(typeRect, null, null);  
        
        assertEquals(typeSVG.getTypeString(), svgObjectBlank1.getType());
        assertEquals(typeRect.getTypeString(), svgObjectBlank4.getType());
    }
    
    /**
     * Test of getType(), of class SVGData.
     */
    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testSetType() throws IllegalArgumentException {
        System.out.println("setType");
        SVGData badObject = new SVGData(null, null, null);
    }

    /**
     * Test of getChild(), setChild(), removeChild(), getAllChildren(), customSortOrder() of class SVGData.
     */
    @Test
    public void testGetSetChildParent() {
        System.out.println("getSetChildParent");
        
        //Set object IDs
        String id1 = "blank1";
        String id2 = "blank2";
        String id3 = "blank3";
        String id4 = "blank4";
        
        svgObjectBlank1 = new SVGData(typeSVG, null, null);
        svgObjectBlank2 = new SVGData(typeSVG, null, null);
        svgObjectBlank3 = new SVGData(typeRect, null, null);
        svgObjectBlank4 = new SVGData(typeRect, null, null);  
        
        svgObjectBlank1.setAttribute(SVGAttributeConstants.ID,id1);
        svgObjectBlank2.setAttribute(SVGAttributeConstants.ID,id2);
        svgObjectBlank3.setAttribute(SVGAttributeConstants.ID,id3);
        svgObjectBlank4.setAttribute(SVGAttributeConstants.ID,id4);
        
        //Ensure Objects have no parent by default
        assertNull(svgObjectBlank1.getParent());
        assertTrue(svgObjectBlank1.getAllChildren().isEmpty());
        assertNull(svgObjectBlank2.getParent());
        assertTrue(svgObjectBlank2.getAllChildren().isEmpty());
        assertNull(svgObjectBlank3.getParent());
        assertTrue(svgObjectBlank3.getAllChildren().isEmpty());
        
        //Set Parent Child Relationship - svgObjectBlank1(svgObjectBlank2(svgObjectBlank3))
        svgObjectBlank2.setParent(svgObjectBlank1);
        svgObjectBlank3.setParent(svgObjectBlank2);
        
        //Test the parent child relationship structure has been correctly created
        assertNull(svgObjectBlank1.getParent());
        assertEquals(svgObjectBlank1.getChild(id2),svgObjectBlank2);
        assertTrue(svgObjectBlank1.getAllChildren().size() == 1);
        assertEquals(svgObjectBlank2.getParent(), svgObjectBlank1);
        assertEquals(svgObjectBlank2.getChild(id3),svgObjectBlank3);
        assertTrue(svgObjectBlank2.getAllChildren().size() == 1);
        assertEquals(svgObjectBlank3.getParent(), svgObjectBlank2);
        assertTrue(svgObjectBlank3.getAllChildren().isEmpty());
        
        // Test getting a child 2 levels down 
        assertEquals(svgObjectBlank1.getChild(id3), svgObjectBlank3);
        
        // Test removing a child 2 level below the element.
        assertEquals(svgObjectBlank3, svgObjectBlank1.removeChild(id3));
        assertNull(svgObjectBlank1.getParent());
        assertEquals(svgObjectBlank1.getChild(id2), svgObjectBlank2);
        assertTrue(svgObjectBlank1.getAllChildren().size() == 1);
        assertEquals(svgObjectBlank2.getParent(), svgObjectBlank1);
        assertNull(svgObjectBlank2.getChild(id3));
        assertTrue(svgObjectBlank2.getAllChildren().isEmpty());
        assertNull(svgObjectBlank3.getParent());
        assertTrue(svgObjectBlank3.getAllChildren().isEmpty());
        
        // Test remove a child 1 levels below the element
        assertEquals(svgObjectBlank2, svgObjectBlank1.removeChild(id2));
        assertNull(svgObjectBlank1.getParent());
        assertNull(svgObjectBlank1.getChild(id2));
        assertTrue(svgObjectBlank1.getAllChildren().isEmpty());
        assertNull(svgObjectBlank2.getParent());
        assertTrue(svgObjectBlank2.getAllChildren().isEmpty());
        
//        // Test removing a non existant child.
//        assertNull(svgObjectBlank1.removeChild(id2));
        
        // Set Parent Child Relationship - svgObjectBlank1(svgObjectBlank2, svgObjectBlank3)
        svgObjectBlank2.setParent(svgObjectBlank1);
        svgObjectBlank3.setParent(svgObjectBlank1);
                
        //Test the parent child relationship structure has been correctly created
        assertNull(svgObjectBlank1.getParent());
        assertEquals(svgObjectBlank1.getChild(id2), svgObjectBlank2);
        assertTrue(svgObjectBlank1.getAllChildren().size() == 2);
        assertEquals(svgObjectBlank2.getParent(), svgObjectBlank1);
        assertTrue(svgObjectBlank2.getAllChildren().isEmpty());
        assertEquals(svgObjectBlank3.getParent(), svgObjectBlank1);
        assertTrue(svgObjectBlank3.getAllChildren().isEmpty());
             
        // Set Parent Child Relationship - svgObjectBlank1(svgObjectBlank2, svgObjectBlank3, svgObjectBlank4)
        svgObjectBlank4.setParent(svgObjectBlank1);
        
        // Test getAllChildren with a custom sort order.
        svgObjectBlank4.setAttribute(SVGAttributeConstants.CUSTOM_SORT_ORDER, "1");
        svgObjectBlank3.setAttribute(SVGAttributeConstants.CUSTOM_SORT_ORDER, "3");
        svgObjectBlank2.setAttribute(SVGAttributeConstants.CUSTOM_SORT_ORDER, "2");
        List<SVGData> children = svgObjectBlank1.getAllChildren();
        assertEquals(children.get(2), svgObjectBlank4);
        assertEquals(children.get(1), svgObjectBlank2);
        assertEquals(children.get(0), svgObjectBlank3);
    }

    /**
     * Test of setAttribute(), getAttribute(), of class SVGData.
     */
    @Test
    public void testGetSetAttribute() {
        System.out.println("setAttribute");
        
        svgObjectBlank1 = new SVGData(typeSVG, null, null);
        
        final String width = "64";
        final String color = "#ff00ff";
        final String x = "256";
        final String viewbox = "0, 0, 256, 256";
        final String dashArray = "10 20 10";
        final String sortOrder = "4"; 
        
        //Test attribute values present in attribute map 
        assertNull(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.WIDTH.getName()));
        assertNull(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.X.getName()));
        assertNull(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.FILL_COLOR.getName()));
        svgObjectBlank1.setAttribute(SVGAttributeConstants.WIDTH, width);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.X, x);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.FILL_COLOR, color);
        assertEquals(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.WIDTH.getName()), width);
        assertEquals(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.X.getName()), x);
        assertEquals(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.FILL_COLOR.getName()), color);
        
        //Test attribute values not present in attribute map 
        assertNull(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.VIEW_BOX.getName()));
        assertNull(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.DASH_ARRAY.getName()));
        assertNull(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.CUSTOM_SORT_ORDER.getName()));
        svgObjectBlank1.setAttribute(SVGAttributeConstants.VIEW_BOX, viewbox);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.DASH_ARRAY, dashArray);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.CUSTOM_SORT_ORDER, sortOrder);
        assertEquals(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.VIEW_BOX.getName()), viewbox);
        assertEquals(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.DASH_ARRAY.getName()), dashArray);
        assertEquals(svgObjectBlank1.getAttributeValue(SVGAttributeConstants.CUSTOM_SORT_ORDER.getName()), sortOrder);
    }

    /**
     * Test of setContent(), of class SVGData.
     */
    @Test
    public void testGetSetContent() {
        System.out.println("setContent");
        
        svgObjectBlank1 = new SVGData(typeSVG, null, null); 
        
        final String content = "This is some default content";
        assertNull(svgObjectBlank1.getContent());
        svgObjectBlank1.setContent(content);
        assertEquals(svgObjectBlank1.getContent(), content);
        
    }

    /**
     * Test of toString(), cleanAttributes(), method, of class SVGData.
     */
    @Test
    public void testPresentation() {
        System.out.println("toString");
        
        svgObjectBlank1 = new SVGData(typeSVG, null, null);
        svgObjectBlank2 = new SVGData(typeSVG, null, null);
        svgObjectBlank4 = new SVGData(typeRect, null, null);  
        
        final String parentID = "parent";
        final String childID = "child";
        final String x = "64";
        final String y = "32";
        final String width = "128";     
        final String height = "256";  
        final String content = "Some Conent";
        final String viewBox = "0 0 256 256";      
        final String expectedString = String.format(""
                + "\n<%s %s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\" %s=\"%s\">\n"
                + "\t<%s %s=\"%s\" />\n"
                + "\t<%s %s=\"%s\">%s\n"
                + "\t</%s>\n"
                + "</%s>",
                typeSVG.getTypeString(),
                SVGAttributeConstants.ID.getName(), parentID,
                SVGAttributeConstants.X.getName(), x,
                SVGAttributeConstants.Y.getName(), y,
                SVGAttributeConstants.WIDTH.getName(), width,
                SVGAttributeConstants.HEIGHT.getName(), height,
                SVGAttributeConstants.VIEW_BOX.getName(), viewBox,
                typeSVG.getTypeString(),
                SVGAttributeConstants.WIDTH.getName(), width,
                typeRect.getTypeString(),
                SVGAttributeConstants.ID.getName(), childID,
                content,
                typeRect.getTypeString(),
                typeSVG.getTypeString()
                );
        
        //Set attributes and contnet in a random order.
        svgObjectBlank1.setAttribute(SVGAttributeConstants.Y, y);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.HEIGHT, height);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.VIEW_BOX, viewBox);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.ID, parentID);
        
        svgObjectBlank1.setAttribute(SVGAttributeConstants.WIDTH, width);
        svgObjectBlank1.setAttribute(SVGAttributeConstants.X, x);
        
        svgObjectBlank4.setAttribute(SVGAttributeConstants.ID, childID);
        svgObjectBlank4.setContent(content);
        svgObjectBlank2.setParent(svgObjectBlank1);
        svgObjectBlank4.setParent(svgObjectBlank1);
        
        svgObjectBlank2.setAttribute(SVGAttributeConstants.WIDTH, width);
        
        assertEquals(CommonTests.getString(svgObjectBlank1), expectedString);

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
}
