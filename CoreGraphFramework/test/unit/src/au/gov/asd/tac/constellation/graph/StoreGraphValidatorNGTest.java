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
package au.gov.asd.tac.constellation.graph;

import java.util.HashMap;
import java.util.Map;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 * Store Graph Validator Test.
 *
 * @author sirius
 */
public class StoreGraphValidatorNGTest {

    private static final boolean VERBOSE = false;

    @Test
    public void validateGraphTest() {

        int vCapacity = 16;
        int tCapacity = 256;

        GraphWriteMethods graph = new StoreGraph(2, 2, 2, 2, 2);
        StoreGraphValidator validator = new StoreGraphValidator();

        for (int trial = 0; trial < 100000; trial++) {

            if (VERBOSE) {
                System.out.println("// Trial = " + trial);
            }

            validateGraph(validator, graph);

            int operation = (int) (Math.random() * 4);
            switch (operation) {

                // Add vertex
                case 0:
                    if (graph.getVertexCount() < vCapacity) {
                        int v = graph.addVertex();
                        validator.addVertex(v);

                        if (VERBOSE) {
                            System.out.println("g.addVertex(); // " + v);
                        }
                    }
                    break;

                // Add transaction
                case 1:
                    if (graph.getTransactionCount() < tCapacity) {
                        int source = (int) (Math.random() * graph.getVertexCapacity());
                        int destination = (int) (Math.random() * graph.getVertexCapacity());
                        boolean directed = Math.random() > 0.5;
                        if (graph.vertexExists(source) && graph.vertexExists(destination)) {
                            int t = graph.addTransaction(source, destination, directed);
                            int l = graph.getTransactionLink(t);
                            validator.addTransaction(source, destination, directed, t, l);

                            if (VERBOSE) {
                                System.out.println("g.addTransaction(" + source + ", " + destination + ", " + directed + "); // " + t);
                            }
                        }
                    }
                    break;

                case 2:
                    int t = (int) (Math.random() * graph.getTransactionCapacity());
                    if (graph.transactionExists(t)) {
                        graph.removeTransaction(t);
                        validator.removeTransaction(t);

                        if (VERBOSE) {
                            System.out.println("g.removeTransaction(" + t + ");");
                        }
                    }
                    break;

                // Remove vertex
                case 3:
                    int v = (int) (Math.random() * graph.getVertexCapacity());
                    if (graph.vertexExists(v)) {
                        graph.removeVertex(v);
                        validator.removeVertex(v);

                        if (VERBOSE) {
                            System.out.println("g.removeVertex(" + v + ");");
                        }
                    }
                    break;
            }
        }
    }

    private void validateGraph(StoreGraphValidator validator, GraphWriteMethods graph) {

        assertEquals(validator.getVertexCount(), graph.getVertexCount());
        assertEquals(validator.getLinkCount(), graph.getLinkCount());
        assertEquals(validator.getTransactionCount(), graph.getTransactionCount());

        IdCollector validatorIds = new IdCollector();
        IdCollector graphIds = new IdCollector();

        for (int v = 0; v < graph.getVertexCapacity(); v++) {
            assertEquals(validator.vertexExists(v), graph.vertexExists(v));
        }

        validatorIds.clear();
        graphIds.clear();
        for (int i = 0; i < graph.getVertexCount(); i++) {
            validatorIds.add(validator.getVertex(i));
            graphIds.add(graph.getVertex(i));

            int v = graph.getVertex(i);

            for (int p = 0; p < graph.getVertexCount(); p++) {
                int v2 = graph.getVertex(p);
                assertEquals("Get Link " + v + " - " + v2, validator.getLink(v, v2), graph.getLink(v, v2));
            }

            IdCollector validatorTransactions = new IdCollector();
            IdCollector graphTransactions = new IdCollector();

            assertEquals(validator.getVertexTransactionCount(v), graph.getVertexTransactionCount(v));

            for (int p = 0; p < graph.getVertexTransactionCount(v); p++) {
                validatorTransactions.add(validator.getVertexTransaction(v, p));
                graphTransactions.add(graph.getVertexTransaction(v, p));
            }

            assertEquals(validatorTransactions, graphTransactions);

            for (int direction = 0; direction < 3; direction++) {
                assertEquals(validator.getVertexTransactionCount(v, direction), graph.getVertexTransactionCount(v, direction));

                validatorTransactions.clear();
                graphTransactions.clear();

                for (int p = 0; p < graph.getVertexTransactionCount(v, direction); p++) {
                    validatorTransactions.add(validator.getVertexTransaction(v, direction, p));
                    graphTransactions.add(graph.getVertexTransaction(v, direction, p));
                }

                assertEquals(validatorTransactions, graphTransactions);
            }

            IdCollector validatorLinks = new IdCollector();
            IdCollector graphLinks = new IdCollector();

            assertEquals(validator.getVertexLinkCount(v), graph.getVertexLinkCount(v));

            for (int p = 0; p < graph.getVertexLinkCount(v); p++) {
                validatorLinks.add(validator.getVertexLink(v, p));
                graphLinks.add(graph.getVertexLink(v, p));
            }

            assertEquals(validatorLinks, graphLinks);
        }
        assertEquals(validatorIds, graphIds);

        for (int l = 0; l < graph.getLinkCapacity(); l++) {
            assertEquals(validator.linkExists(l), graph.linkExists(l));
        }

        validatorIds.clear();
        graphIds.clear();
        for (int i = 0; i < graph.getLinkCount(); i++) {
            validatorIds.add(validator.getLink(i));
            graphIds.add(graph.getLink(i));

            int l = graph.getLink(i);

            assertEquals(validator.getLinkLowVertex(l), graph.getLinkLowVertex(l));
            assertEquals(validator.getLinkHighVertex(l), graph.getLinkHighVertex(l));

            IdCollector validatorTransactions = new IdCollector();
            IdCollector graphTransactions = new IdCollector();

            assertEquals(validator.getLinkTransactionCount(l), graph.getLinkTransactionCount(l));

            for (int p = 0; p < graph.getLinkTransactionCount(l); p++) {
                validatorTransactions.add(validator.getLinkTransaction(l, p));
                graphTransactions.add(graph.getLinkTransaction(l, p));
            }

            assertEquals(validatorTransactions, graphTransactions);

            for (int direction = 0; direction < 3; direction++) {
                assertEquals(validator.getLinkTransactionCount(l, direction), graph.getLinkTransactionCount(l, direction));

                validatorTransactions.clear();
                graphTransactions.clear();

                for (int p = 0; p < graph.getLinkTransactionCount(l, direction); p++) {
                    validatorTransactions.add(validator.getLinkTransaction(l, direction, p));
                    graphTransactions.add(graph.getLinkTransaction(l, direction, p));
                }

                assertEquals(validatorTransactions, graphTransactions);
            }
        }
        assertEquals(validatorIds, graphIds);

        for (int t = 0; t < graph.getTransactionCapacity(); t++) {
            assertEquals(validator.transactionExists(t), graph.transactionExists(t));
        }

        validatorIds.clear();
        graphIds.clear();
        for (int i = 0; i < graph.getTransactionCount(); i++) {
            validatorIds.add(validator.getTransaction(i));
            graphIds.add(graph.getTransaction(i));

            int t = graph.getTransaction(i);
            assertEquals(validator.getTransactionDestinationVertex(t), graph.getTransactionDestinationVertex(t));
            assertEquals(validator.getTransactionSourceVertex(t), graph.getTransactionSourceVertex(t));
            assertEquals(validator.getTransactionDirection(t), graph.getTransactionDirection(t));
            assertEquals(validator.getTransactionLink(t), graph.getTransactionLink(t));
        }
        assertEquals(validatorIds, graphIds);
    }

    private class IdCollector {

        private final Map<Integer, Integer> ids = new HashMap<>();

        public void add(int id) {
            Integer count = ids.get(id);
            ids.put(id, count == null ? 1 : (count + 1));
        }

        public void clear() {
            ids.clear();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof IdCollector) {
                return ids.equals(((IdCollector) other).ids);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.ids != null ? this.ids.hashCode() : 0);
            return hash;
        }
    }
}
