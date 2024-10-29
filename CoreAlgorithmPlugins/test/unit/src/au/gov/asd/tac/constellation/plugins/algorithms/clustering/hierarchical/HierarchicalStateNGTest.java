/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class HierarchicalStateNGTest {

    /**
     * Test of constructor method, of class HierarchicalState.
     */
    @Test
    public void testConstructor() {
        System.out.println("getCurrentStep");
        final int steps = 101;
        final int currentStep = 202;
        final FastNewman.Group[] groups = {};
        final int vxCapacity = 303;

        final HierarchicalState instance = new HierarchicalState(steps, currentStep, groups, vxCapacity);

        assertEquals(instance.getSteps(), steps);
        assertEquals(instance.getCurrentStep(), currentStep);
        assertEquals(instance.getGroups(), groups);
        assertEquals(instance.getClusterNumbers().length, vxCapacity);
        assertEquals(instance.getClusterSeenBefore().length, vxCapacity);
    }

    /**
     * Test of getCurrentStep method, of class HierarchicalState.
     */
    @Test
    public void testGetCurrentStep() {
        System.out.println("getCurrentStep");
        final HierarchicalState instance = new HierarchicalState();
        final int currentStep = 404;

        instance.setCurrentStep(currentStep);
        assertEquals(instance.getCurrentStep(), currentStep);
    }

    /**
     * Test of getOptimumStep method, of class HierarchicalState.
     */
    @Test
    public void testGetOptimumStep() {
        System.out.println("getOptimumStep");
        final HierarchicalState instance = new HierarchicalState();
        final int optimumStep = 505;

        instance.setOptimumStep(optimumStep);
        assertEquals(instance.getOptimumStep(), optimumStep);
    }

    /**
     * Test of isExcludeSingleVertices method, of class HierarchicalState.
     */
    @Test
    public void testIsExcludeSingleVertices() {
        System.out.println("isExcludeSingleVertices");
        final HierarchicalState instance = new HierarchicalState();
        final boolean excludeSingleVertices = true;

        instance.setExcludeSingleVertices(excludeSingleVertices);
        assertEquals(instance.isExcludeSingleVertices(), excludeSingleVertices);
    }

    /**
     * Test of isExcludedElementsDimmed method, of class HierarchicalState.
     */
    @Test
    public void testIsExcludedElementsDimmed() {
        System.out.println("isExcludedElementsDimmed");
        final HierarchicalState instance = new HierarchicalState();
        final boolean excludeElementsDimmed = true;

        instance.setExcludedElementsDimmed(excludeElementsDimmed);
        assertEquals(instance.isExcludedElementsDimmed(), excludeElementsDimmed);
    }

    /**
     * Test of getExcludedElementsState method, of class HierarchicalState.
     */
    @Test
    public void testGetExcludedElementsState() {
        System.out.println("getExcludedElementsState");
        final HierarchicalState instance = new HierarchicalState();
        final HierarchicalState.ExcludedState excludedElementsState = HierarchicalState.ExcludedState.SHOW;

        instance.setExcludedElementsState(excludedElementsState);
        assertEquals(instance.getExcludedElementsState(), excludedElementsState);
    }

    /**
     * Test of getClusterNumbers method, of class HierarchicalState.
     */
    @Test
    public void testGetClusterNumbers() {
        System.out.println("getClusterNumbers");
        final HierarchicalState instance = new HierarchicalState();
        final int[] expResult = {606};

        instance.setClusterNumbers(expResult);
        assertEquals(instance.getClusterNumbers(), expResult);
    }

    /**
     * Test of getClusterSeenBefore method, of class HierarchicalState.
     */
    @Test
    public void testGetClusterSeenBefore() {
        System.out.println("getClusterSeenBefore");
        final HierarchicalState instance = new HierarchicalState();
        final int[] expResult = {707};

        instance.setClusterSeenBefore(expResult);
        assertEquals(instance.getClusterSeenBefore(), expResult);
    }

    /**
     * Test of getRedrawCount method, of class HierarchicalState.
     */
    @Test
    public void testGetRedrawCount() {
        System.out.println("getRedrawCount");
        final HierarchicalState instance = new HierarchicalState();
        final int expResult = 808;

        instance.setRedrawCount(expResult);
        assertEquals(instance.getRedrawCount(), expResult);
    }

    /**
     * Test of getModificationCounter method, of class HierarchicalState.
     */
    @Test
    public void testGetModificationCounter() {
        System.out.println("getModificationCounter");
        final HierarchicalState instance = new HierarchicalState();
        final long expResult = 909L;

        instance.setModificationCounter(expResult);
        assertEquals(instance.getModificationCounter(), expResult);
    }

    /**
     * Test of getStrucModificationCount method, of class HierarchicalState.
     */
    @Test
    public void testGetStrucModificationCount() {
        System.out.println("getStrucModificationCount");
        final HierarchicalState instance = new HierarchicalState();
        final long expResult = 10010L;

        instance.setStrucModificationCount(expResult);
        assertEquals(instance.getStrucModificationCount(), expResult);
    }

    /**
     * Test of isInteractive method, of class HierarchicalState.
     */
    @Test
    public void testIsInteractive() {
        System.out.println("isInteractive");
        final HierarchicalState instance = new HierarchicalState();
        final boolean expResult = true;

        instance.setInteractive(expResult);
        assertEquals(instance.isInteractive(), expResult);
    }

    /**
     * Test of getSteps method, of class HierarchicalState.
     */
    @Test
    public void testGetSteps() {
        System.out.println("getSteps");
        final HierarchicalState instance = new HierarchicalState();
        final int expResult = 11011;

        instance.setSteps(expResult);
        assertEquals(instance.getSteps(), expResult);
    }

    /**
     * Test of getGroups method, of class HierarchicalState.
     */
    @Test
    public void testGetGroups() {
        System.out.println("getGroups");
        final HierarchicalState instance = new HierarchicalState();
        final FastNewman.Group[] expResult = {};

        instance.setGroups(expResult);
        assertEquals(instance.getGroups(), expResult);
    }

    /**
     * Test of isColored method, of class HierarchicalState.
     */
    @Test
    public void testIsColored() {
        System.out.println("isColored");
        final HierarchicalState instance = new HierarchicalState();
        final boolean expResult = false;

        instance.setColored(expResult);
        assertEquals(instance.isColored(), expResult);
    }

    /**
     * Test of getCurrentNumOfClusters method, of class HierarchicalState.
     */
    @Test
    public void testGetCurrentNumOfClusters() {
        System.out.println("getCurrentNumOfClusters");
        final HierarchicalState instance = new HierarchicalState();
        final FastNewman.Group[] groupArray = {new FastNewman.Group()};
        final int expResult = groupArray.length;

        instance.setGroups(groupArray);
        assertEquals(instance.getCurrentNumOfClusters(), expResult);
    }

    /**
     * Test of toString method, of class HierarchicalState.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        final HierarchicalState instance = new HierarchicalState();
        final int steps = 6;
        final int currentStep = 3;
        final String expResult = String.format("CoI[steps=%d,currentStep=%d]", steps, currentStep);

        instance.setSteps(steps);
        instance.setCurrentStep(currentStep);
        assertEquals(instance.toString(), expResult);
    }
}
