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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.io.InputStream;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Tests for {@link SVGObject}
 * 
 * @author capricornunicorn123
 */
public class SVGObjectTest {
    final String typeSVG = "svg";
    
    public SVGObjectTest() { 
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {

    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setParent(), getParent(), removeChild(), getAllChildren(), setSortOrderValue() of class SVGObject.
     */
    @Test
    public void testGetSetRemoveParentChild() {
        System.out.println("setParent");
        
        final SVGObject svgObjectBlank1 = new SVGObject(new SVGData(typeSVG, null, null));
        final SVGObject svgObjectBlank2 = new SVGObject(new SVGData(typeSVG, null, null));
        final SVGObject svgObjectBlank3 = new SVGObject(new SVGData(typeSVG, null, null));
        final SVGObject svgObjectBlank4 = new SVGObject(new SVGData(typeSVG, null, null));

        
        //Set object IDs
        String id1 = "blank1";
        String id2 = "blank2";
        String id3 = "blank3";
        svgObjectBlank1.setID(id1);
        svgObjectBlank2.setID(id2);
        svgObjectBlank3.setID(id3);
        
        //Ensure Objects have no parent by default
        assertNull(svgObjectBlank1.getParent());
        assertTrue(svgObjectBlank1.toSVGData().getAllChildren().isEmpty());
        assertNull(svgObjectBlank2.getParent());
        assertTrue(svgObjectBlank2.toSVGData().getAllChildren().isEmpty());
        assertNull(svgObjectBlank3.getParent());
        assertTrue(svgObjectBlank3.toSVGData().getAllChildren().isEmpty());
        
        //Set Parent Child Relationship - svgObjectBlank1(svgObjectBlank2(svgObjectBlank3))
        svgObjectBlank2.setParent(svgObjectBlank1);
        svgObjectBlank3.setParent(svgObjectBlank2);
        
        //Test the parent child relationship structure has been correctly created
        assertNull(svgObjectBlank1.getParent());
        assertEquals(svgObjectBlank1.getChild(id2).toSVGData(),svgObjectBlank2.toSVGData());
        assertTrue(svgObjectBlank1.toSVGData().getAllChildren().size() == 1);
        assertEquals(svgObjectBlank2.getParent().toSVGData(), svgObjectBlank1.toSVGData());
        assertEquals(svgObjectBlank2.getChild(id3).toSVGData(),svgObjectBlank3.toSVGData());
        assertTrue(svgObjectBlank2.toSVGData().getAllChildren().size() == 1);
        assertEquals(svgObjectBlank3.getParent().toSVGData(), svgObjectBlank2.toSVGData());
        assertTrue(svgObjectBlank3.toSVGData().getAllChildren().isEmpty());
        
        // Test getting a child 2 levels down 
        assertEquals(svgObjectBlank1.getChild(id3).toSVGData(),svgObjectBlank3.toSVGData());
        
        // Test removing a child 2 level below the element.
        svgObjectBlank1.removeChild(id3);
        assertNull(svgObjectBlank1.getParent());
        assertEquals(svgObjectBlank1.getChild(id2).toSVGData(),svgObjectBlank2.toSVGData());
        assertTrue(svgObjectBlank1.toSVGData().getAllChildren().size() == 1);
        assertEquals(svgObjectBlank2.getParent().toSVGData(), svgObjectBlank1.toSVGData());
        assertNull(svgObjectBlank2.getChild(id3));
        assertTrue(svgObjectBlank2.toSVGData().getAllChildren().isEmpty());
        assertNull(svgObjectBlank3.getParent());
        assertTrue(svgObjectBlank3.toSVGData().getAllChildren().isEmpty());
        
        // Test remove a child 1 levels below the element
        svgObjectBlank1.removeChild(id2);
        assertNull(svgObjectBlank1.getParent());
        assertNull(svgObjectBlank1.getChild(id2));
        assertTrue(svgObjectBlank1.toSVGData().getAllChildren().isEmpty());
        assertNull(svgObjectBlank2.getParent());
        assertTrue(svgObjectBlank2.toSVGData().getAllChildren().isEmpty());
        
        // Set Parent Child Relationship - svgObjectBlank1(svgObjectBlank2, svgObjectBlank3)
        svgObjectBlank2.setParent(svgObjectBlank1);
        svgObjectBlank3.setParent(svgObjectBlank1);
                
        //Test the parent child relationship structure has been correctly created
        assertNull(svgObjectBlank1.getParent());
        assertEquals(svgObjectBlank1.getChild(id2).toSVGData(),svgObjectBlank2.toSVGData());
        assertTrue(svgObjectBlank1.toSVGData().getAllChildren().size() == 2);
        assertEquals(svgObjectBlank2.getParent().toSVGData(), svgObjectBlank1.toSVGData());
        assertTrue(svgObjectBlank2.toSVGData().getAllChildren().isEmpty());
        assertEquals(svgObjectBlank3.getParent().toSVGData(), svgObjectBlank1.toSVGData());
        assertTrue(svgObjectBlank3.toSVGData().getAllChildren().isEmpty());
            
        // Set Parent Child Relationship - svgObjectBlank1(svgObjectBlank2, svgObjectBlank3, svgObjectBlank4)
        svgObjectBlank4.setParent(svgObjectBlank1);
        
        // Test getAllChildren with a custom sort order.
        svgObjectBlank4.setSortOrderValue(1F);
        svgObjectBlank3.setSortOrderValue(3F);
        svgObjectBlank2.setSortOrderValue(2F);
        List<SVGData> children = svgObjectBlank1.toSVGData().getAllChildren();
        assertEquals(children.get(2), svgObjectBlank4.toSVGData());
        assertEquals(children.get(1), svgObjectBlank2.toSVGData());
        assertEquals(children.get(0), svgObjectBlank3.toSVGData());
    }
    
    /**
     * Test of setParent() method, of class SVGObject.
     */
    @Test(expectedExceptions=ArrayIndexOutOfBoundsException.class)
    public void testSetParentError() throws ArrayIndexOutOfBoundsException {
        System.out.println("setID");
        final SVGObject svgObjectBlank1 = new SVGObject(new SVGData(typeSVG, null, null));
        final SVGObject svgObjectBlank2 = new SVGObject(new SVGData(typeSVG, null, null));
        final SVGObject svgObjectBlank3 = new SVGObject(new SVGData(typeSVG, null, null));

        String id1 = "blank1";
        String id2 = "blank2";
        
        //Test setting a child to a parent whilst the parent has a child with the same id
        svgObjectBlank1.setID(id1);
        svgObjectBlank2.setID(id2);
        svgObjectBlank3.setID(id2);
        svgObjectBlank2.setParent(svgObjectBlank1);
        
        //This should throw an exception 
        svgObjectBlank3.setParent(svgObjectBlank1);      
    }


    /**
     * Test of setContent() method, of SVGObject.
     */
    @Test
    public void testSetContent() {
        System.out.println("setContent");
        
        final SVGObject svgObjectBlank1 = new SVGObject(new SVGData(typeSVG, null, null));
        final String content = "This is some defualt content";
        assertNull(svgObjectBlank1.toSVGData().getContent());
        svgObjectBlank1.setContent(content);
        assertEquals(svgObjectBlank1.toSVGData().getContent(), content);
    }

    /**
     * Test of setID(), of class SVGObject.
     */
    @Test
    public void testGetSetID() {
        System.out.println("setID");

        final SVGObject svgObjectBlank1 = new SVGObject(new SVGData(typeSVG, null, null));

        
        String idString = "blank1";
        Integer idInteger = 123;
        
        // Test object has no id
        assertNull(svgObjectBlank1.getID());
        
        //Set object ID as String
        svgObjectBlank1.setID(idString);
        assertEquals(svgObjectBlank1.getID(), idString);
        
        //Set object ID as Integer
        svgObjectBlank1.setID(idInteger);
        assertEquals(svgObjectBlank1.getID(), idInteger.toString());        
    }
    

    


    /**
     * Test of getHeight(), getWidth(), setDimension() setDimensionScale(), of class SVGObject.
     */
    @Test
    public void testGetSetDimensions() {     
        System.out.println("getHeight");

        final SVGObject svgObjectBlank1 = new SVGObject(new SVGData(typeSVG, null, null));

    
        float height = 256F;
        float width = 128F;
        Assert.assertEquals(0.0F, svgObjectBlank1.getHeight());
        Assert.assertEquals(0.0F, svgObjectBlank1.getWidth());
        svgObjectBlank1.setDimension(width, height);
        Assert.assertEquals(height, svgObjectBlank1.getHeight());  
        Assert.assertEquals(width, svgObjectBlank1.getWidth());  
        
        //TODO - setDimensionScale() Tests
    }

    /**
     * Test of getXPosition(), getYPosition(), setPosition(), of class SVGObject.
     */
    @Test
    public void testGetSetPosition() {
        System.out.println("getXPosition");

        final SVGObject svgObjectBlank1 = new SVGObject(new SVGData(typeSVG, null, null));
    
        float defaultValue = 0.0F;
        float xF = 256F;
        float yF = 128F;
        Assert.assertEquals(defaultValue, svgObjectBlank1.getXPosition());
        Assert.assertEquals(defaultValue, svgObjectBlank1.getYPosition());
        svgObjectBlank1.setPosition(xF, yF);
        Assert.assertEquals(xF, svgObjectBlank1.getXPosition());  
        Assert.assertEquals(yF, svgObjectBlank1.getYPosition());
        svgObjectBlank1.setPosition(defaultValue, defaultValue);
        
        double xD = 256D;
        double yD = 128D;
        Assert.assertEquals(defaultValue, svgObjectBlank1.getXPosition());
        Assert.assertEquals(defaultValue, svgObjectBlank1.getYPosition());
        svgObjectBlank1.setPosition(xD, yD);
        Assert.assertEquals(xF, svgObjectBlank1.getXPosition());  
        Assert.assertEquals(yF, svgObjectBlank1.getYPosition());  
    }

    /**
     * Test of setFontSize(), setBaseline(), of class SVGObject.
     */
    @Test
    public void testFont() {
        //TODO
    }

    /**
     * Test of setTransformation method, of class SVGObject.
     */
    @Test
    public void testSetTransformation() {
        //TODO
    }

    /**
     * Test of setStrokeStyle(), setStrokeArray() method, of class SVGObject.
     */
    @Test
    public void testStroke() {
        //TODO
    }

    /**
     * Test of toSVGData(), of class SVGObject.
     */
    @Test
    public void testToSVGData() {
        System.out.println("toSVGData");
        
        final SVGData svgDataBlank1 = new SVGData(typeSVG, null, null);
        final SVGObject svgObjectBlank1 = new SVGObject(svgDataBlank1);

        assertEquals(svgObjectBlank1.toSVGData(), svgDataBlank1);
    }

    /**
     * Test of loadFromTemplate(), of class SVGObject.
     */
    @Test
    public void testLoadFromTemplate() {
        //TODO
    }

    /**
     * Test of loadFromInputStream(), of class SVGObject.
     */
    @Test
    public void testLoadFromInputStream() {
        //TODO
    }

    /**
     * Test of setViewBox(), of class SVGObject.
     */
    @Test
    public void testSetViewBox() {
        //TODO
    }

    /**
     * Test of saturateSVG(), setFillColor(), setStrokeColor(), applyGrayScaleFileter(), of class SVGObject.
     */
    @Test
    public void testColor() {
        //TODO
    }


    /**
     * Test of setPoints(), of class SVGObject.
     */
    @Test
    public void testSetPoints() {
        //TODO
    }
   
}
