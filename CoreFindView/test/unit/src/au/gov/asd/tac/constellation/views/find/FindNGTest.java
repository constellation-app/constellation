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
package au.gov.asd.tac.constellation.views.find;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.advanced.AdvancedFindPlugin;
import au.gov.asd.tac.constellation.views.find.advanced.FindResult;
import au.gov.asd.tac.constellation.views.find.advanced.FindRule;
import au.gov.asd.tac.constellation.views.find.advanced.FindTypeOperators;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openide.windows.TopComponent;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Find Test.
 *
 * @author algol
 */
public class FindNGTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vNameAttr, tNameAttr, vSelAttr, tSelAttr;
    private Graph graph;

    public FindNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new DualGraph(null);
        WritableGraph wg = graph.getWritableGraph("add", true);
        try {
            attrX = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0.0, null);
            if (attrX == Graph.NOT_FOUND) {
                fail();
            }

            attrY = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0.0, null);
            if (attrY == Graph.NOT_FOUND) {
                fail();
            }

            attrZ = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0.0, null);
            if (attrZ == Graph.NOT_FOUND) {
                fail();
            }

            vNameAttr = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            if (vNameAttr == Graph.NOT_FOUND) {
                fail();
            }

            tNameAttr = wg.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            if (tNameAttr == Graph.NOT_FOUND) {
                fail();
            }

            vSelAttr = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (vSelAttr == Graph.NOT_FOUND) {
                fail();
            }

            tSelAttr = wg.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (tSelAttr == Graph.NOT_FOUND) {
                fail();
            }

            vxId1 = wg.addVertex();
            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.1f);
            wg.setBooleanValue(vSelAttr, vxId1, false);
            wg.setStringValue(vNameAttr, vxId1, "name1");
            vxId2 = wg.addVertex();
            wg.setFloatValue(attrX, vxId2, 2.0f);
            wg.setFloatValue(attrY, vxId2, 2.2f);
            wg.setBooleanValue(vSelAttr, vxId2, true);
            wg.setStringValue(vNameAttr, vxId2, "name2");
            vxId3 = wg.addVertex();
            wg.setFloatValue(attrX, vxId3, 3.0f);
            wg.setFloatValue(attrY, vxId3, 3.3f);
            wg.setBooleanValue(vSelAttr, vxId3, false);
            wg.setStringValue(vNameAttr, vxId3, "name3");
            vxId4 = wg.addVertex();
            wg.setFloatValue(attrX, vxId4, 4.0f);
            wg.setFloatValue(attrY, vxId4, 4.4f);
            wg.setBooleanValue(vSelAttr, vxId4, true);
            wg.setStringValue(vNameAttr, vxId4, "name4");
            vxId5 = wg.addVertex();
            wg.setFloatValue(attrX, vxId5, 5.0f);
            wg.setFloatValue(attrY, vxId5, 5.5f);
            wg.setBooleanValue(vSelAttr, vxId5, false);
            wg.setStringValue(vNameAttr, vxId5, "name5");
            vxId6 = wg.addVertex();
            wg.setFloatValue(attrX, vxId6, 6.0f);
            wg.setFloatValue(attrY, vxId6, 6.60f);
            wg.setBooleanValue(vSelAttr, vxId6, true);
            wg.setStringValue(vNameAttr, vxId6, "name6");
            vxId7 = wg.addVertex();
            wg.setFloatValue(attrX, vxId7, 7.0f);
            wg.setFloatValue(attrY, vxId7, 7.7f);
            wg.setBooleanValue(vSelAttr, vxId7, false);
            wg.setStringValue(vNameAttr, vxId7, "name7");

            txId1 = wg.addTransaction(vxId1, vxId2, false);
            wg.setBooleanValue(tSelAttr, txId1, false);
            wg.setStringValue(tNameAttr, txId1, "name101");
            txId2 = wg.addTransaction(vxId1, vxId3, false);
            wg.setBooleanValue(tSelAttr, txId2, true);
            wg.setStringValue(tNameAttr, txId2, "name102");
            txId3 = wg.addTransaction(vxId2, vxId4, true);
            wg.setBooleanValue(tSelAttr, txId3, false);
            wg.setStringValue(tNameAttr, txId3, "name103");
            txId4 = wg.addTransaction(vxId4, vxId2, true);
            wg.setBooleanValue(tSelAttr, txId4, true);
            wg.setStringValue(tNameAttr, txId4, "name104");
            txId5 = wg.addTransaction(vxId5, vxId6, false);
            wg.setBooleanValue(tSelAttr, txId5, false);
            wg.setStringValue(tNameAttr, txId5, "name105");

        } finally {
            wg.commit();
        }
    }

    @Test
    public void findSingleNodeCaseSensitiveFindTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("string_content", "name1");
            values.put("string_case_sensitive", true);
            values.put("string_use_list", false);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.STRING, new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, vNameAttr)), FindTypeOperators.Operator.IS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode gn = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 1, results.size());
            assertTrue("node 'name1' found", nodeFound(rg, "name1", results));
        } finally {
            rg.release();
        }
    }

    @Test
    public void findSingleNodeCaseSensitiveNoFindTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("string_content", "Name1");
            values.put("string_case_sensitive", true);
            values.put("string_use_list", false);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.STRING, new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, vNameAttr)), FindTypeOperators.Operator.IS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);
            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 0, results.size());
        } finally {
            rg.release();
        }
    }

    @Test
    public void findSingleNodeIsTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("string_content", "name1");
            values.put("string_case_sensitive", false);
            values.put("string_use_list", false);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.STRING, new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, vNameAttr)), FindTypeOperators.Operator.IS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 1, results.size());
            assertTrue("node 'name1' found", nodeFound(rg, "name1", results));
        } finally {
            rg.release();
        }
    }

    @Test
    public void findSingleNodeContainTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("string_content", "e1");
            values.put("string_case_sensitive", false);
            values.put("string_use_list", false);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.STRING, new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, vNameAttr)), FindTypeOperators.Operator.CONTAINS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 1, results.size());
            assertTrue("node 'name1' found", nodeFound(rg, "name1", results));
        } finally {
            rg.release();
        }
    }

    @Test
    public void findListValuesTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("string_content", "name1,nAMe2,name3");
            values.put("string_case_sensitive", false);
            values.put("string_use_list", true);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.STRING, new GraphAttribute(rg, vNameAttr), FindTypeOperators.Operator.IS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 3, results.size());
            assertTrue("node 'name1' found", nodeFound(rg, "name1", results));
            assertTrue("node 'name2' found", nodeFound(rg, "name2", results));
            assertTrue("node 'name3' found", nodeFound(rg, "name3", results));
        } finally {
            rg.release();
        }
    }

    @Test
    public void findBooleanValuesTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("boolean_content", true);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.BOOLEAN, new GraphAttribute(rg, vSelAttr), FindTypeOperators.Operator.IS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 3, results.size());
            assertTrue("node 'name2' found", nodeFound(rg, "name2", results));
            assertTrue("node 'name4' found", nodeFound(rg, "name4", results));
            assertTrue("node 'name6' found", nodeFound(rg, "name6", results));
        } finally {
            rg.release();
        }
    }

    @Test
    public void findFloatValuesTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("float_first_item", 5.1f);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.FLOAT, new GraphAttribute(rg, attrX), FindTypeOperators.Operator.GREATER_THAN, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.VERTEX, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 2, results.size());
            assertTrue("node 'name6' found", nodeFound(rg, "name6", results));
            assertTrue("node 'name7' found", nodeFound(rg, "name7", results));
        } finally {
            rg.release();
        }
    }

    @Test
    public void findTransactionsTest() throws InterruptedException, PluginException {
        ArrayList<FindRule> rules = new ArrayList<>();
        ReadableGraph rg = graph.getReadableGraph();
        try {
            // setup find criteria / rules
            HashMap<String, Object> values = new HashMap<>();
            values.put("string_content", "name");
            values.put("string_case_sensitive", true);
            values.put("string_use_list", false);
            FindRule rule1 = new FindRule(FindTypeOperators.Type.STRING, new GraphAttribute(rg, tNameAttr), FindTypeOperators.Operator.CONTAINS, values);
            rules.add(rule1);

            // perform find search
            // need to create temporary GraphNode and skip the RemoteInit portion of the initialisation
            GraphNode aGraphNode = new GraphNode(graph, null, new TopComponent(), null);

            final AdvancedFindPlugin queryPlugin = new AdvancedFindPlugin(GraphElementType.TRANSACTION, rules, false);
            PluginExecution.withPlugin(queryPlugin).executeNow(graph);
            final List<FindResult> results = queryPlugin.getResults();

            // validate results
            assertEquals("result size", 5, results.size());
            assertTrue("tx 'name101' found", transactionFound(rg, "name101", results));
            assertTrue("tx 'name102' found", transactionFound(rg, "name102", results));
            assertTrue("tx 'name103' found", transactionFound(rg, "name103", results));
            assertTrue("tx 'name104' found", transactionFound(rg, "name104", results));
            assertTrue("tx 'name105' found", transactionFound(rg, "name105", results));
        } finally {
            rg.release();
        }
    }

    // determine whether the node of the specified name was part of the result set
    private boolean nodeFound(ReadableGraph graph, String base_name, List<FindResult> results) {
        int nameAttrId = graph.getAttribute(GraphElementType.VERTEX, "name");
        boolean found = false;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            int id = graph.getVertex(i);
            String name = graph.getStringValue(nameAttrId, id);
            if (base_name.equals(name)) {
                for (int j = 0; j < results.size(); j++) {
                    if (id == results.get(j).getID()) {
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    // determine whether the transaction of the specified name was part of the result set
    private boolean transactionFound(ReadableGraph graph, String base_name, List<FindResult> results) {
        int nameAttrId = graph.getAttribute(GraphElementType.TRANSACTION, "name");
        boolean found = false;
        for (int i = 0; i < graph.getTransactionCount(); i++) {
            int id = graph.getTransaction(i);
            String name = graph.getStringValue(nameAttrId, id);
            if (base_name.equals(name)) {
                for (int j = 0; j < results.size(); j++) {
                    if (id == results.get(j).getID()) {
                        found = true;
                    }
                }
            }
        }
        return found;
    }
}
