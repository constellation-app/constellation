/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.NodeBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapUndirected.NodeFactoryUndirected;
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
public class TreeDataNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of addNewNode method, of class TreeData.
     */
    @Test
    public void testAddNewNode() {
        System.out.println("addNewNode");
        
        final NodeFactoryUndirected nodeFactory = new NodeFactoryUndirected();
        final TreeData instance = new TreeData(nodeFactory);
        final NodeBase root = instance.getRoot();
        
        assertEquals(root.getChildDegree(), 0);
        assertEquals(instance.getNumLeafNodes(), 0);
        
        final int result = instance.addNewNode(2, "newNode", 0.5F, 0.8F);
        
        assertEquals(result, 2);
        assertEquals(root.getChildDegree(), 1);
        assertEquals(instance.getNumLeafNodes(), 1);
    }

    /**
     * Test of addClonedNode method, of class TreeData.
     */
    @Test
    public void testAddClonedNode() {
        System.out.println("addClonedNode");
        
        final NodeFactoryUndirected nodeFactory = new NodeFactoryUndirected();
        final TreeData instance = new TreeData(nodeFactory);
        final NodeBase root = instance.getRoot();
        
        assertEquals(root.getChildDegree(), 0);
        assertEquals(instance.getNumLeafNodes(), 0);
        
        final NodeBase node = nodeFactory.createNode("newNode", 0.5F, 0.8F);
        
        instance.addClonedNode(node);
        
        assertEquals(root.getChildDegree(), 1);
        assertEquals(instance.getNumLeafNodes(), 1);
        assertEquals(instance.getFirstLeaf(), node);
    }

    /**
     * Test of addEdge method, of class TreeData.
     */
    @Test
    public void testAddEdge() {
        System.out.println("addEdge");
        
        final NodeFactoryUndirected nodeFactory = new NodeFactoryUndirected();
        final TreeData instance = new TreeData(nodeFactory);
        instance.addNewNode(0, "newNode", 0.5F, 0.8F);
        instance.addNewNode(1, "newNode2", 0.2F, 0.1F);
        
        final NodeBase leaf = instance.getFirstLeaf();
        
        assertEquals(leaf.getOutEdges().size(), 0);
        
        instance.addEdge(0, 1, 3F, 2F);
        
        assertEquals(leaf.getOutEdges().size(), 1);
    }
}
