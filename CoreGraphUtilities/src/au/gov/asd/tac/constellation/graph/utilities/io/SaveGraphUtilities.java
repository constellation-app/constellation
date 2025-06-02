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
package au.gov.asd.tac.constellation.graph.utilities.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonWriter;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.TextIoProgress;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Save Graph Utilities
 *
 * @author arcturus
 */
public class SaveGraphUtilities {
    
    private SaveGraphUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Save the graph to the temporary directory for debugging.
     * <p>
     * The main use case of this method is debugging unit tests and so this
     * method does extra checks to make sure one can interact with the graph
     * build from a crude unit test.
     *
     * @param graph The graph
     * @param filename The filename excluding the file extension
     * @param makeInteractable If true, will modify the graph and add attributes
     * to make sure the graph can be opened
     * @throws IOException
     * @throws InterruptedException
     */
    public static void saveGraphToTemporaryDirectory(final Graph graph, final String filename, boolean makeInteractable) throws IOException, InterruptedException {
        final File saveCreatedGraph = File.createTempFile(filename, FileExtensionConstants.STAR);
        final WritableGraph wg = graph.getWritableGraph("make graph interactable", true);
        try {
            if (makeInteractable) {
                makeGraphInteractable(wg);
            }
        } finally {
            wg.commit();
        }

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final GraphJsonWriter writer = new GraphJsonWriter();
            writer.writeGraphToZip(rg, saveCreatedGraph.getPath(), new TextIoProgress(false));
        } finally {
            rg.release();
        }
    }

    /**
     * Save the graph to the temporary directory for debugging.
     * <p>
     * The main use case of this method is debugging unit tests and so this
     * method does extra checks to make sure one can interact with the graph
     * build from a crude unit test.
     *
     * @param graph The graph
     * @param file The graph file
     * @param makeInteractable If true, will modify the graph and add attributes
     * to make sure the graph can be opened
     * @throws IOException
     * @throws InterruptedException
     */
    public static void saveGraphToTemporaryDirectory(final Graph graph, final File file, boolean makeInteractable) throws IOException, InterruptedException {
        final WritableGraph wg = graph.getWritableGraph("make graph interactable", true);
        try {
            if (makeInteractable) {
                makeGraphInteractable(wg);
            }
        } finally {
            wg.commit();
        }

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final GraphJsonWriter writer = new GraphJsonWriter();
            writer.writeGraphToZip(rg, file.getPath(), new TextIoProgress(false));
        } finally {
            rg.release();
        }
    }

    /**
     * Save the graph to the temporary directory for debugging.
     * <p>
     * The main use case of this method is debugging unit tests and so this
     * method does extra checks to make sure one can interact with the graph
     * build from a crude unit test.
     *
     * @param graph The graph
     * @param filename The filename excluding the file extension
     * @throws IOException
     */
    public static void saveGraphToTemporaryDirectory(final StoreGraph graph, final String filename) throws IOException {
        final File saveCreatedGraph = File.createTempFile(filename, FileExtensionConstants.STAR);

        makeGraphInteractable(graph);

        final GraphJsonWriter writer = new GraphJsonWriter();
        writer.writeGraphToZip(graph, saveCreatedGraph.getPath(), new TextIoProgress(false));
    }

    /**
     * Save the graph to the temporary directory for debugging.
     * <p>
     * The main use case of this method is debugging unit tests and so this
     * method does extra checks to make sure one can interact with the graph
     * build from a crude unit test.
     *
     * @param graph The graph
     * @param file The graph file
     * @throws IOException
     */
    public static void saveGraphToTemporaryDirectory(final StoreGraph graph, final File file) throws IOException {
        makeGraphInteractable(graph);

        final GraphJsonWriter writer = new GraphJsonWriter();
        writer.writeGraphToZip(graph, file.getPath(), new TextIoProgress(false));
    }

    private static void makeGraphInteractable(final GraphWriteMethods graph) {
        // add attributes required for interaction

        // graph
        VisualConcept.GraphAttribute.CAMERA.ensure(graph);
        VisualConcept.GraphAttribute.CONNECTION_MODE.ensure(graph);
        VisualConcept.GraphAttribute.DRAWING_MODE.ensure(graph);

        // vertex
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        VisualConcept.VertexAttribute.SELECTED.ensure(graph);

        // transaction
        VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        final int bottomLabelsAttribute = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
        final GraphLabels newBottomLabels = new GraphLabels(Arrays.asList(new GraphLabel(VisualConcept.VertexAttribute.LABEL.getName(), ConstellationColor.AZURE)));
        graph.setObjectValue(bottomLabelsAttribute, 0, newBottomLabels);
    }
}
