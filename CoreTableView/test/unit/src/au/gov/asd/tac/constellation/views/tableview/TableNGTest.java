/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.tableview;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import java.util.HashMap;
import static org.testng.Assert.fail;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Table Test.
 *
 * @author altair
 */
public class TableNGTest {

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

    @Test
    public void checkColumnClassTest() throws InterruptedException {
        final DualGraph graph = new DualGraph(null);
        final int xAttr, yAttr, zAttr;
        final int selectedAttr;
        final HashMap<String, String> fields = new HashMap<>();

        WritableGraph wg = graph.getWritableGraph("", true);
        try {
            xAttr = wg.addAttribute(GraphElementType.VERTEX, "float", "x", "x", 0.0, null);
            if (xAttr == Graph.NOT_FOUND) {
                fail();
            }
            fields.put("x", "float");

            yAttr = wg.addAttribute(GraphElementType.VERTEX, "float", "y", "y", 0.0, null);
            if (yAttr == Graph.NOT_FOUND) {
                fail();
            }
            fields.put("y", "float");

            zAttr = wg.addAttribute(GraphElementType.VERTEX, "float", "z", "z", 0.0, null);
            if (zAttr == Graph.NOT_FOUND) {
                fail();
            }
            fields.put("z", "float");

            selectedAttr = wg.addAttribute(GraphElementType.VERTEX, "boolean", "selected", "selected", false, null);
            if (selectedAttr == Graph.NOT_FOUND) {
                fail();
            }
            fields.put("selected", "boolean");
        } finally {
            wg.commit();
        }

        GraphTableModel m = new GraphTableModel(graph, GraphElementType.VERTEX);
        ReadableGraph rg = graph.getReadableGraph();
        try {
            for (int position = 0; position < rg.getAttributeCount(GraphElementType.VERTEX); position++) {
                final int attrId = rg.getAttribute(GraphElementType.VERTEX, position);
                final Attribute attr = new GraphAttribute(rg, attrId);

                if (!attr.getAttributeType().equals("float") && !attr.getAttributeType().equals("boolean")) {
                    AssertJUnit.fail("Forgot a native type!");
                }

                AssertJUnit.assertEquals(
                        String.format("expected class=[%s] actual class=[%s]", attr.getAttributeType(), fields.get(attr.getName())),
                        attr.getAttributeType(),
                        fields.get(attr.getName()));
            }
        } finally {
            rg.release();
        }
    }
}
