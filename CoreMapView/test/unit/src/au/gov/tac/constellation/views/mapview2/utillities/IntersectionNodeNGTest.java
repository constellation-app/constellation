/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.tac.constellation.views.mapview2.utillities;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class IntersectionNodeNGTest {

    public IntersectionNodeNGTest() {
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
     * Test of getRelevantMarkers method, of class IntersectionNode.
     */
    @Test
    public void testGetRelevantMarkers() {
        System.out.println("getRelevantMarkers");
        IntersectionNode instance = new IntersectionNode(5, 6);
        instance.addRelevantMarker(54);

        List<Integer> expResult = new ArrayList();
        expResult.add(54);

        List result = instance.getRelevantMarkers();
        assertEquals(result.get(0), expResult.get(0));
    }

    /**
     * Test of getConnectedPoints method, of class IntersectionNode.
     */
    @Test
    public void testGetConnectedPoints() {
        System.out.println("getConnectedPoints");
        IntersectionNode instance = new IntersectionNode(2, 3);
        List<IntersectionNode> expResult = new ArrayList<>();
        expResult.add(new IntersectionNode(4, 5));
        instance.addConnectedPoint(new IntersectionNode(4, 5));

        List<IntersectionNode> result = instance.getConnectedPoints();
        assertEquals(result.get(0).getX(), expResult.get(0).getX());
        assertEquals(result.get(0).getY(), expResult.get(0).getY());
    }

    /**
     * Test of addRelevantMarker method, of class IntersectionNode.
     */
    @Test
    public void testAddRelevantMarker() {
        System.out.println("addRelevantMarker");
        Integer id = 54;
        IntersectionNode instance = new IntersectionNode();
        instance.addRelevantMarker(id);

        assertEquals(instance.getRelevantMarkers().contains(id), true);
        instance.addRelevantMarker(id);
        assertEquals(instance.getRelevantMarkers().size(), 1);
    }

    /**
     * Test of addConnectedPoint method, of class IntersectionNode.
     */
    @Test
    public void testAddConnectedPoint() {
        System.out.println("addConnectedPoint");
        IntersectionNode otherNode = new IntersectionNode(5, 5);
        IntersectionNode instance = new IntersectionNode(4, 4);
        instance.addConnectedPoint(null);

        assertEquals(instance.getConnectedPoints().size(), 0);

        instance.addConnectedPoint(instance);

        assertEquals(instance.getConnectedPoints().size(), 0);

        instance.addConnectedPoint(otherNode);

        assertEquals(instance.getConnectedPoints().get(0).getKey(), otherNode.getKey());
        assertEquals(instance.getConnectedPoints().size(), 1);

        instance.addConnectedPoint(otherNode);
        assertEquals(instance.getConnectedPoints().size(), 1);
    }

    /**
     * Test of addContainedPoint method, of class IntersectionNode.
     */
    @Test
    public void testAddContainedPoint() {
        System.out.println("addContainedPoint");
        double x = 6.0;
        double y = 7.0;
        IntersectionNode instance = new IntersectionNode();
        instance.addContainedPoint(x, y);

        assertEquals(instance.getContainedPoints().get(0).getX(), x);
        assertEquals(instance.getContainedPoints().get(0).getY(), y);
    }

    /**
     * Test of getContainedPoints method, of class IntersectionNode.
     */
    @Test
    public void testGetContainedPoints() {
        System.out.println("getContainedPoints");
        IntersectionNode instance = new IntersectionNode(5, 6);
        instance.addContainedPoint(7, 8);
        List<Vec3> expResult = new ArrayList<Vec3>();
        expResult.add(new Vec3(7, 8));

        assertEquals(instance.getContainedPoints().get(0).getX(), expResult.get(0).getX());
        assertEquals(instance.getContainedPoints().get(0).getY(), expResult.get(0).getY());
    }

    /**
     * Test of getKey method, of class IntersectionNode.
     */
    @Test
    public void testGetKey() {
        System.out.println("getKey");
        IntersectionNode instance = new IntersectionNode(5, 6);
        String expResult = "5.0,6.0";
        String result = instance.getKey();
        assertEquals(result, expResult);
    }

    /**
     * Test of getX method, of class IntersectionNode.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");
        IntersectionNode instance = new IntersectionNode(5, 6);
        double expResult = 5.0;
        double result = instance.getX();
        assertEquals(result, expResult);
    }

    /**
     * Test of getY method, of class IntersectionNode.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        IntersectionNode instance = new IntersectionNode(5, 6);
        double expResult = 6.0;
        double result = instance.getY();
        assertEquals(result, expResult);
    }

}
