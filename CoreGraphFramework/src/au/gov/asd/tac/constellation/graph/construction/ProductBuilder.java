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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class ProductBuilder extends GraphBuilder {

    private static final ProductType PRODUCT_DEFAULT = ProductType.CARTESIAN_PRODUCT;

    public enum ProductType {

        CARTESIAN_PRODUCT, // (x, y)~(x', y') <==> (x~x' and y=y') or (x=x' and y~y')
        DIRECT_PRODUCT, // (x, y)~(x', y') <==> x~x' and y~y'
        STRONG_PRODUCT, // (x, y)~(x', y') <==> x~=x' and y~=y' (union of direct and cartesian)
        LEFT_COMPLETE_PRODUCT, // (x, y)~(x', y') <==> x~x'
        RIGHT_COMPLETE_PRODUCT;     // (x, y)~(x', y') <==> y~y'
    }

    public static ProductBuilder formProduct(final GraphWriteMethods graph, final GraphReadMethods g1, final GraphReadMethods g2) {
        return formProduct(graph, g1, g2, PRODUCT_DEFAULT);
    }

    public static ProductBuilder formProduct(final GraphReadMethods g1, final GraphReadMethods g2, ProductType productType) {
        return formProduct(new StoreGraph(), g1, g2, productType);
    }

    public static ProductBuilder formProduct(final GraphWriteMethods graph, final GraphReadMethods g1, final GraphReadMethods g2, ProductType productType) {

        // Record the original vertices of both operands before we change anything.
        int[] g1Vertices = new int[g1.getVertexCount()];
        for (int i = 0; i < g1.getVertexCount(); i++) {
            g1Vertices[i] = g1.getVertex(i);
        }
        int[] g2Vertices = new int[g2.getVertexCount()];
        for (int i = 0; i < g2.getVertexCount(); i++) {
            g2Vertices[i] = g2.getVertex(i);
        }

        // Set up the two views for the vertex set of the product.
        final int[][] leftGraphs = new int[g2.getVertexCount()][];
        for (int i = 0; i < leftGraphs.length; i++) {
            leftGraphs[i] = new int[g1.getVertexCount()];
        }
        final int[][] rightGraphs = new int[g1.getVertexCount()][];
        for (int i = 0; i < rightGraphs.length; i++) {
            rightGraphs[i] = new int[g2.getVertexCount()];
        }

        // Construct the vertex set of the product
        for (int i = 0; i < g1.getVertexCount(); i++) {
            for (int j = 0; j < g2.getVertexCount(); j++) {
                leftGraphs[j][i] = rightGraphs[i][j] = constructVertex(graph);
            }
        }

        // Used to store transactions as they are calculated
        final List<Integer> transactionsList = new ArrayList<>();

        // Iterate through each vertex in g1
        for (int i = 0; i < g1Vertices.length; i++) {

            // Form transactions where y~y'
            switch (productType) {
                case CARTESIAN_PRODUCT:
                    for (int k = 0; k < g2.getVertexCount(); k++) {
                        for (int l = 0; l < g2.getVertexNeighbourCount(g2Vertices[k]); l++) {

                            // We now have g2Vertices[k] ~ g2Vertices[pos2]
                            final int pos2 = g2.getVertexPosition(g2.getVertexNeighbour(g2Vertices[k], l));

                            // x=x' and y~y'
                            addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, i, pos2, false, true);
                        }
                    }
                    break;
                case STRONG_PRODUCT:
                    for (int k = 0; k < g2.getVertexCount(); k++) {
                        for (int l = 0; l < g2.getVertexNeighbourCount(g2Vertices[k]); l++) {

                            // We now have g2Vertices[k] ~ g2Vertices[pos2]
                            final int pos2 = g2.getVertexPosition(g2.getVertexNeighbour(g2Vertices[k], l));

                            // x=x' and y~y'
                            addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, i, pos2, false, true);
                        }
                    }
                    break;
                case RIGHT_COMPLETE_PRODUCT:
                    for (int j = i - 1; j >= 0; j--) {
                        for (int k = 0; k < g2.getVertexCount(); k++) {
                            for (int l = 0; l < g2.getVertexNeighbourCount(g2Vertices[k]); l++) {

                                // We now have g2Vertices[k] ~ g2Vertices[pos2]
                                final int pos2 = g2.getVertexPosition(g2.getVertexNeighbour(g2Vertices[k], l));

                                //x free, y~y'
                                addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, j, pos2, false, true);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }

            for (int j = 0; j < g1.getVertexNeighbourCount(g1Vertices[i]); j++) {

                // we now have g1Vertices[i] ~ g1Vertices[pos]
                final int pos = g1.getVertexPosition(g1.getVertexNeighbour(g1Vertices[i], j));

                //Form transactions where x~x'
                switch (productType) {
                    case CARTESIAN_PRODUCT:
                        for (int k = 0; k < g2.getVertexCount(); k++) {

                            // x~x' and y=y'
                            addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, pos, k, true, false);
                        }
                        break;
                    case DIRECT_PRODUCT:
                        for (int k = 0; k < g2.getVertexCount(); k++) {
                            for (int l = 0; l < g2.getVertexNeighbourCount(g2Vertices[k]); l++) {

                                // We now have g2Vertices[k] ~ g2Vertices[pos2]
                                final int pos2 = g2.getVertexPosition(g2.getVertexNeighbour(g2Vertices[k], l));

                                // x~x' and y~y'
                                addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, pos, pos2, true, true);
                            }
                        }
                        break;
                    case STRONG_PRODUCT:
                        for (int k = 0; k < g2.getVertexCount(); k++) {

                            // x~x' and y=y'
                            addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, pos, k, true, false);

                            for (int l = 0; l < g2.getVertexNeighbourCount(g2Vertices[k]); l++) {

                                // We now have g2Vertices[k] ~ g2Vertices[pos2]
                                final int pos2 = g2.getVertexPosition(g2.getVertexNeighbour(g2Vertices[k], l));

                                // x~x' and y~y'
                                addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, pos, pos2, true, true);
                            }
                        }
                        break;
                    case LEFT_COMPLETE_PRODUCT:
                        for (int k = 0; k < g2.getVertexCount(); k++) {
                            for (int l = k - 1; l >= 0; l--) {

                                // x~x', y free
                                addLinkProduct(graph, g1, g2, transactionsList, rightGraphs, g1Vertices, g2Vertices, i, k, pos, l, true, false);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        final int[] transactions = new int[transactionsList.size()];
        int currentTrans = 0;
        for (int transID : transactionsList) {
            transactions[currentTrans++] = transID;
        }

        return new ProductBuilder(graph, leftGraphs, rightGraphs, transactions);

    }

    private static void addLinkProduct(final GraphWriteMethods graph, final GraphReadMethods g1, final GraphReadMethods g2, final List<Integer> transactions, final int[][] rightGraphs, final int[] g1Vertices, final int[] g2Vertices, final int x, final int y, final int x2, final int y2, final boolean g1Counts, final boolean g2Counts) {

        // we only consider neighbour relations from a vertex of lower index to a vertex of higher index
        if (g1Vertices[x2] < g1Vertices[x] || g2Vertices[y2] < g1Vertices[y]) {
            return;
        }

        final int g1Link = g1.getLink(g1Vertices[x], g1Vertices[x2]);
        final int g2Link = g2.getLink(g2Vertices[y], g2Vertices[y2]);
        final boolean g1Reverse = g1Counts && g1.getLinkLowVertex(g1Link) != g1Vertices[x];
        final boolean g2Reverse = g2Counts && g2.getLinkLowVertex(g2Link) != g2Vertices[y];

        final int g1Flat = !g1Counts ? 1 : g1.getLinkTransactionCount(g1Link, Graph.FLAT);

        final int g1Uphill;
        if (!g1Counts) {
            g1Uphill = 1;
        } else {
            g1Uphill = g1Reverse ? g1.getLinkTransactionCount(g1Link, Graph.DOWNHILL) : g1.getLinkTransactionCount(g1Link, Graph.UPHILL);
        }

        final int g1Downhill;
        if (!g1Counts) {
            g1Downhill = 1;
        } else {
            g1Downhill = g1Reverse ? g1.getLinkTransactionCount(g1Link, Graph.UPHILL) : g1.getLinkTransactionCount(g1Link, Graph.DOWNHILL);
        }

        final int g2Flat = !g2Counts ? 1 : g2.getLinkTransactionCount(g2Link, Graph.FLAT);

        final int g2Uphill;
        if (!g2Counts) {
            g2Uphill = 1;
        } else {
            g2Uphill = g2Reverse ? g2.getLinkTransactionCount(g2Link, Graph.DOWNHILL) : g2.getLinkTransactionCount(g2Link, Graph.UPHILL);
        }

        final int g2Downhill;
        if (!g2Counts) {
            g2Downhill = 1;
        } else {
            g2Downhill = g2Reverse ? g2.getLinkTransactionCount(g2Link, Graph.UPHILL) : g2.getLinkTransactionCount(g2Link, Graph.DOWNHILL);
        }

        final int flat = g1Flat * g2Flat;
        final int uphill = g1Uphill * g2Uphill;
        final int downhill = g1Downhill * g2Downhill;

        for (int i = 0; i < flat; i++) {
            transactions.add(constructTransaction(graph, rightGraphs[x][y], rightGraphs[x2][y2], false));
        }
        for (int i = 0; i < uphill; i++) {
            transactions.add(constructTransaction(graph, rightGraphs[x][y], rightGraphs[x2][y2], true));
        }
        for (int i = 0; i < downhill; i++) {
            transactions.add(constructTransaction(graph, rightGraphs[x2][y2], rightGraphs[x][y], true));
        }

    }

    public final int[] nodes;
    public final int[] transactions;
    public final int[][] leftGraphs;
    public final int[][] rightGraphs;

    private ProductBuilder(final GraphWriteMethods graph, final int[][] leftGraphs, final int[][] rightGraphs, final int[] transactions) {
        super(graph);
        this.leftGraphs = leftGraphs;
        this.rightGraphs = rightGraphs;
        nodes = squashGrouping(leftGraphs);
        this.transactions = transactions;
    }

}
