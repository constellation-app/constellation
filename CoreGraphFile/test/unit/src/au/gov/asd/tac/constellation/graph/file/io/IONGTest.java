/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttributeMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.mergers.ConcatenatedSetGraphAttributeMerger;
import au.gov.asd.tac.constellation.utilities.gui.TextIoProgress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * I/O Test.
 *
 * @author algol
 */
public class IONGTest {

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
     * Saving and loading an attribute with a non-null default value.
     * @throws java.io.IOException
     * @throws au.gov.asd.tac.constellation.graph.file.io.GraphParseException
     */
    @Test
    public void saveLoadNonNullDefault() throws IOException, GraphParseException {
        final String name = "0000-00-00-00-000000";
        File graphFile = File.createTempFile(name, ".star");

        final String nameAttrLabel = "name";
        final String nndAttrLabel = "nnd";
        final String defaultValue = "A non-null value";

        final StoreGraph graph = new StoreGraph();
        int nameAttrId = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, nameAttrLabel, nameAttrLabel, "", null);
        final int nndAttrId = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, nndAttrLabel, "All nulls", defaultValue, null);
        int v0 = graph.addVertex();
        Assert.assertEquals(graph.getStringValue(nameAttrId, v0), "");
        Assert.assertNotNull(graph.getStringValue(nndAttrId, v0));
        Assert.assertEquals(graph.getStringValue(nndAttrId, v0), defaultValue);
        int v1 = graph.addVertex();
        graph.setStringValue(nameAttrId, v1, "V1");
        graph.setStringValue(nndAttrId, v1, null);
        Assert.assertNull(graph.getStringValue(nndAttrId, v1));

        GraphJsonWriter writer = new GraphJsonWriter();
        writer.writeGraphToZip(graph, graphFile.getPath(), new TextIoProgress(false));

        try {
            final Graph newGraph = new GraphJsonReader().readGraphZip(graphFile, new TextIoProgress(false));
            try (final ReadableGraph rg = newGraph.getReadableGraph()) {
                nameAttrId = rg.getAttribute(GraphElementType.VERTEX, nameAttrLabel);
                final int nattrId = rg.getAttribute(GraphElementType.VERTEX, nndAttrLabel);

                v0 = rg.getVertex(0);
                Assert.assertEquals(rg.getStringValue(nameAttrId, v0), "");
                final String val0 = rg.getStringValue(nattrId, v0);
                Assert.assertEquals(val0, defaultValue);

                v1 = rg.getVertex(1);
                Assert.assertEquals(rg.getStringValue(nameAttrId, v1), "V1");
                final String nval1 = rg.getStringValue(nattrId, v1);
                Assert.assertNull(nval1, "Expecting the default non-null value");               
            }
        } finally {
            graphFile.delete();
        }
    }

    /**
     * Tests that saving and loading a Constellation file preserves the
     * attribute mergers specified on each attribute, both in the null case, and
     * the non-null case.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws GraphParseException
     */
    @Test
    public void attributeMergerSaveTest() throws IOException, InterruptedException, GraphParseException {
        final StoreGraph storeGraph = new StoreGraph();
        storeGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "defaultMergerAttribute", null, null, GraphAttributeMerger.getDefault().getId());
        storeGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "customMergerAttribute", null, null, ConcatenatedSetGraphAttributeMerger.ID);
        storeGraph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "noMergerAttribute", null, null, null);
        final GraphJsonWriter writer = new GraphJsonWriter();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeGraphToStream(storeGraph, out, false, Arrays.asList(GraphElementType.GRAPH, GraphElementType.VERTEX, GraphElementType.TRANSACTION, GraphElementType.META));
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        GraphJsonReader reader = new GraphJsonReader();
        final Graph graph = reader.readGraph(in, -1, null);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int defaultMergerAttributeId = rg.getAttribute(GraphElementType.VERTEX, "defaultMergerAttribute");
            assert rg.getAttributeMerger(defaultMergerAttributeId) == GraphAttributeMerger.getDefault();
            final int customMergerAttributeId = rg.getAttribute(GraphElementType.VERTEX, "customMergerAttribute");
            assert rg.getAttributeMerger(customMergerAttributeId) == GraphAttributeMerger.getMergers().get(ConcatenatedSetGraphAttributeMerger.ID);
            final int noMergerAttributeId = rg.getAttribute(GraphElementType.VERTEX, "noMergerAttribute");
            assert rg.getAttributeMerger(noMergerAttributeId) == null;
        }
    }
}
