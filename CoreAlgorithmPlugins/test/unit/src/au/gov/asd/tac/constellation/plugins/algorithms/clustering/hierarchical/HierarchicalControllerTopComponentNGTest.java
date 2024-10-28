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
public class HierarchicalControllerTopComponentNGTest {

    /**
     * Test of constructor method, of class HierarchicalControllerTopComponent.
     */
    @Test
    public void testConstructor() {
        System.out.println("testConstructor");
        final HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
        assertEquals(instance.getClass(), HierarchicalControllerTopComponent.class);
    }

//    /**
//     * Test of resultChanged method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testResultChanged() {
//        System.out.println("resultChanged");
//        final LookupEvent lev = null;
//        final HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        instance.resultChanged(lev);
//    }
//    
//    /**
//     * Test of updateSlider method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testUpdateSlider() {
//        System.out.println("updateSlider");
//        final Node[] mockNodes = {new GraphNode(new DualGraph(null), null, null, null)};
//        final Registry mockReg = mock(Registry.class);
//        when(mockReg.getActivatedNodes()).thenReturn(mockNodes);
//        assertEquals(mockReg.getActivatedNodes(), mockNodes);
//        
//        final HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        try (MockedStatic<TopComponent> staticTopComponent = Mockito.mockStatic(TopComponent.class)) {
//            staticTopComponent.when(TopComponent::getRegistry).thenReturn(mockReg);
//            assertEquals(TopComponent.getRegistry(), mockReg);
//            
//            instance.resultChanged(null);
//            instance.graphChanged(null);
//            instance.resultChanged(null);
//            instance.updateSlider();
//            
//        }
//        
//    }
//
//    /**
//     * Test of componentOpened method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testComponentOpened() {
//        System.out.println("componentOpened");
//        HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        instance.componentOpened();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of componentClosed method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testComponentClosed() {
//        System.out.println("componentClosed");
//        HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        instance.componentClosed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeProperties method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testWriteProperties() {
//        System.out.println("writeProperties");
//        Properties p = null;
//        HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        instance.writeProperties(p);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readProperties method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testReadProperties() {
//        System.out.println("readProperties");
//        Properties p = null;
//        HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        instance.readProperties(p);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of graphChanged method, of class HierarchicalControllerTopComponent.
//     */
//    @Test
//    public void testGraphChanged() {
//        System.out.println("graphChanged");
//        GraphChangeEvent evt = null;
//        HierarchicalControllerTopComponent instance = new HierarchicalControllerTopComponent();
//        instance.graphChanged(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
