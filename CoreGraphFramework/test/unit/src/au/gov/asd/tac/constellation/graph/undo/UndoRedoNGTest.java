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
package au.gov.asd.tac.constellation.graph.undo;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import javax.swing.undo.UndoManager;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Undo Redo Test.
 *
 * @author algol
 */
public class UndoRedoNGTest {

    private int attrX;
    private int attrY;
    private int attrZ;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    private int vxId7;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    private int txId5;
    
    private int vNameAttr;
    private int tNameAttr;
    private int vSelAttr;
    private int tSelAttr;
    
    private Graph graph;
    private UndoManager undoMgr;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new DualGraph(null);
        undoMgr = new UndoManager();
        graph.setUndoManager(undoMgr);

        WritableGraph wg = graph.getWritableGraph("original load", true);
        try {
            attrX = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0.0, null);
            attrY = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0.0, null);
            attrZ = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0.0, null);

            vNameAttr = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            tNameAttr = wg.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            vSelAttr = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            tSelAttr = wg.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            
            vxId1 = wg.addVertex();
            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.0f);
            wg.setBooleanValue(vSelAttr, vxId1, false);
            wg.setStringValue(vNameAttr, vxId1, "name1");
            
            vxId2 = wg.addVertex();
            wg.setFloatValue(attrX, vxId2, 5.0f);
            wg.setFloatValue(attrY, vxId2, 1.0f);
            wg.setBooleanValue(vSelAttr, vxId2, true);
            wg.setStringValue(vNameAttr, vxId2, "name2");
            
            vxId3 = wg.addVertex();
            wg.setFloatValue(attrX, vxId3, 1.0f);
            wg.setFloatValue(attrY, vxId3, 5.0f);
            wg.setBooleanValue(vSelAttr, vxId3, false);
            wg.setStringValue(vNameAttr, vxId3, "name3");
            
            vxId4 = wg.addVertex();
            wg.setFloatValue(attrX, vxId4, 5.0f);
            wg.setFloatValue(attrY, vxId4, 5.4f);
            wg.setBooleanValue(vSelAttr, vxId4, true);
            wg.setStringValue(vNameAttr, vxId4, "name4");
            
            vxId5 = wg.addVertex();
            wg.setFloatValue(attrX, vxId5, 15.0f);
            wg.setFloatValue(attrY, vxId5, 15.5f);
            wg.setBooleanValue(vSelAttr, vxId5, false);
            wg.setStringValue(vNameAttr, vxId5, "name5");
            
            vxId6 = wg.addVertex();
            wg.setFloatValue(attrX, vxId6, 26.0f);
            wg.setFloatValue(attrY, vxId6, 26.60f);
            wg.setBooleanValue(vSelAttr, vxId6, true);
            wg.setStringValue(vNameAttr, vxId6, "name6");
            
            vxId7 = wg.addVertex();
            wg.setFloatValue(attrX, vxId7, 37.0f);
            wg.setFloatValue(attrY, vxId7, 37.7f);
            wg.setBooleanValue(vSelAttr, vxId7, false);
            wg.setStringValue(vNameAttr, vxId7, "name7");

            txId1 = wg.addTransaction(vxId1, vxId2, true);
            wg.setBooleanValue(tSelAttr, txId1, false);
            wg.setStringValue(tNameAttr, txId1, "name101");

            txId2 = wg.addTransaction(vxId1, vxId3, true);
            wg.setBooleanValue(tSelAttr, txId2, true);
            wg.setStringValue(tNameAttr, txId2, "name102");

            txId3 = wg.addTransaction(vxId2, vxId4, true);
            wg.setBooleanValue(tSelAttr, txId3, false);
            wg.setStringValue(tNameAttr, txId3, "name103");

            txId4 = wg.addTransaction(vxId4, vxId2, true);
            wg.setBooleanValue(tSelAttr, txId4, true);
            wg.setStringValue(tNameAttr, txId4, "name104");

            txId5 = wg.addTransaction(vxId5, vxId6, true);
            wg.setBooleanValue(tSelAttr, txId5, false);
            wg.setStringValue(tNameAttr, txId5, "name105");
        } finally {
            wg.commit();
        }
    }
}
