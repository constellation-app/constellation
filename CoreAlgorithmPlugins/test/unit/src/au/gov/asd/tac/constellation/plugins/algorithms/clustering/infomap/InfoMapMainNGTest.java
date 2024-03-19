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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree.TreeData;
import au.gov.asd.tac.constellation.utilities.gui.TextIoProgress;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Info Map Main Test.
 *
 * @author algol
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class InfoMapMainNGTest extends ConstellationTest {

    public InfoMapMainNGTest() {
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

    private static void runInfoMap(final Config config, final GraphReadMethods rg) throws FileNotFoundException {
        final InfoMapContext context = new InfoMapContext(config, rg);
        context.getInfoMap().run();

        final InfomapBase infomap = context.getInfoMap();
        final TreeData treeData = infomap.getTreeData();
        System.out.printf("*Vertices %d\n", treeData.getNumLeafNodes());
        for (final NodeBase node : treeData.getLeaves()) {
            final int index = node.getParent().getIndex();
            System.out.printf("position=%d vxId=%d cluster=%d\n", node.getOriginalIndex(), rg.getVertex(node.getOriginalIndex()), index + 1);
        }
    }

    /**
     * Test of InfoMapMain.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testMain() throws Exception {
        final String fnam = "ninetriangles" + GraphDataObject.FILE_EXTENSION;

        // A test graph.
        Graph graph;
        try (InputStream inStream = InfoMapMainNGTest.class.getResourceAsStream(fnam)) {
            graph = new GraphJsonReader().readGraphZip(fnam, inStream, new TextIoProgress(true));
        }
        final Config conf = new Config();
        conf.setNoFileOutput(false);
        conf.setVerbosity(1);

        conf.setNetworkFile(fnam);
        conf.setOutDirectory(System.getProperty("java.io.tmpdir"));
        conf.setPrintClu(true);
        conf.setPrintNodeRanks(true);
        conf.setPrintFlowNetwork(true);
        conf.setVerbosity(1);
        conf.setNumTrials(5);

        System.out.printf("fastHierarchicalSolution %d\n", conf.getFastHierarchicalSolution());
        System.out.printf("Parsing %s network from file '%s'... ", conf.isUndirected() ? "undirected" : "directed", conf.getNetworkFile());

        conf.setConnectionType(Config.ConnectionType.TRANSACTIONS);

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            System.out.printf("vertices=%d, Transactions=%d edges=%d links=%d\n",
                    rg.getVertexCount(), rg.getTransactionCount(), rg.getEdgeCount(), rg.getLinkCount());
            runInfoMap(conf, rg);
        } finally {
            rg.release();
        }
    }
}
