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
package au.gov.asd.tac.constellation.graph.file.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import au.gov.asd.tac.constellation.graph.schema.BareSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProviderManager;
import au.gov.asd.tac.constellation.utilities.gui.IoProgress;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.openide.util.Cancellable;

/**
 * Write a graph in JSON format.
 *
 * @author algol
 */
public final class GraphJsonWriter implements Cancellable {
    
    private static final Logger LOGGER = Logger.getLogger(GraphJsonWriter.class.getName());

    /**
     * The current file format version.
     */
    public static final int VERSION = 2;
    private static final List<GraphElementType> ELEMENT_TYPES_FILE_ORDER = Arrays.asList(GraphElementType.GRAPH, GraphElementType.VERTEX, GraphElementType.TRANSACTION, GraphElementType.META);
    private static final int REPORT_INTERVAL = 10000;
    private IoProgress progress;
    private int counter;
    private volatile boolean isCancelled;
    private final GraphByteWriter byteWriter;
    private final HashMap<String, AbstractGraphIOProvider> graphIoProviders = new HashMap<>();

    private static final String DEFAULT_FIELD = "default";

    /**
     * Construct a new GraphJsonWriter.
     */
    public GraphJsonWriter() {
        byteWriter = new GraphByteWriter();
        for (AbstractGraphIOProvider agiop : AbstractGraphIOProvider.getProviders()) {
            graphIoProviders.put(agiop.getName(), agiop);
        }
    }

    /**
     * Serialise a graph to a file with all elements written.
     * <p>
     * The graph is written to a text file. Ancillary files are written to the
     * same directory. All element values are written, whether they are the same
     * as their respective defaults or not.
     *
     * @param graph The graph to be written.
     * @param path The file path to write the graph file to.
     * @param progress Progress indicator.
     *
     * @return True if the writer was cancelled, false otherwise.
     *
     * @throws IOException If an I/O error occurs.
     */
    public boolean writeGraphFile(final GraphReadMethods graph, final String path, final IoProgress progress) throws IOException {
        final File gf = new File(path);
        try (FileOutputStream fos = new FileOutputStream(gf)) {
            writeGraphToStream(graph, fos, true, ELEMENT_TYPES_FILE_ORDER);
        }

        try {
            if (!isCancelled) {
                final File parentDir = gf.getParentFile();
                for (final Map.Entry<String, File> entry : byteWriter.getFileMap().entrySet()) {
                    final String reference = entry.getKey();
                    final File fbin = entry.getValue();

                    try (FileOutputStream binout = new FileOutputStream(new File(parentDir, reference))) {
                        GraphByteWriter.copy(new FileInputStream(fbin), binout);
                    }
                }
            }
        } finally {
            byteWriter.reset();
        }

        return isCancelled;
    }

    /**
     * Serialise a graph to a zip file.
     * <p>
     * Only element values that are different to their defaults are written.
     * This saves spaces when writing and noticeable time when reading.
     *
     * @param graph The graph to serialise.
     * @param path The path name of the file to write the graph to.
     * @param progress A progress indicator.
     *
     * @return True if the user cancelled the write, false otherwise.
     *
     * @throws IOException If there was a problem writing.
     */
    public boolean writeGraphToZip(final GraphReadMethods graph, final String path, final IoProgress progress) throws IOException {
        final OutputStream out = new FileOutputStream(path);
        return writeGraphToZip(graph, out, progress);
    }

    public boolean writeTemplateToZip(final GraphReadMethods graph, final String path, final IoProgress progress) throws IOException {
        final OutputStream out = new FileOutputStream(path);
        return writeGraphToZip(graph, out, progress, Arrays.asList(GraphElementType.GRAPH));
    }

    public boolean writeGraphToZip(final GraphReadMethods graph, final OutputStream out, final IoProgress progress) throws IOException {
        return writeGraphToZip(graph, out, progress, ELEMENT_TYPES_FILE_ORDER);
    }

    /**
     * Serialise a graph to a zip file with element writing optimised.
     * <p>
     * The OutputStream will be wrapped in a ZipOutputStream and the graph and
     * any ancillary files will be written as ZipEntry files.
     *
     * @param graph The graph to serialise.
     * @param out The OutputStream to write a zip file to.
     * @param progress A progress indicator.
     * @param elementTypes The GraphElementTypes to serialise.
     *
     * @return True if the user cancelled the write, false otherwise.
     *
     * @throws IOException If there was a problem writing.
     */
    public boolean writeGraphToZip(final GraphReadMethods graph, final OutputStream out, final IoProgress progress, final List<GraphElementType> elementTypes) throws IOException {
        this.progress = progress;

        try (ZipOutputStream zout = new ZipOutputStream(out)) {
            final ZipEntry zentry = new ZipEntry("graph" + GraphFileConstants.FILE_EXTENSION);
            zout.putNextEntry(zentry);
            writeGraphToStream(graph, zout, false, elementTypes);
            try {
                if (!isCancelled) {
                    for (Map.Entry<String, File> entry : byteWriter.getFileMap().entrySet()) {
                        final String reference = entry.getKey();
                        final File f = entry.getValue();

                        final ZipEntry ze = new ZipEntry(reference);
                        zout.putNextEntry(ze);
                        GraphByteWriter.copy(new FileInputStream(f), zout);
                    }
                }
            } finally {
                byteWriter.reset();
            }
        }

        return isCancelled;
    }

    /**
     * Serialise a graph in JSON format to an OutputStream.
     * <p>
     * The graph elements are written in the order GRAPH, VERTEX, TRANSACTION,
     * META. Each element type will be written, even if there are no elements of
     * that type.
     * <p>
     * Ancillary files are not written: only the JSON is done here.
     * <p>
     * Originally, the vertices were written to JSON using the position as the
     * id, so vx_id_ = 0, 1, 2, ... . This required everything else (in
     * particular the transaction writing code, but also implementers of
     * AbstractGraphIOProvider.writeObject()) to know about the mapping from
     * graph vertex id to JSON vertex id. Then I realised that is was much
     * easier to write the actual vertex id to JSON, because it doesn't matter
     * what the numbers are in the file, and since the file and JSON ids are the
     * same, there's no need to keep a mapping.
     *
     * @param graph The graph to serialise.
     * @param out The OutputStream to write to.
     * @param verbose Determines whether to write default values of attributes
     * or not.
     * @param elementTypes The GraphElementTypes to serialise.
     *
     * @return True if the user cancelled the write, false otherwise.
     *
     * @throws IOException If an I/O error occurs.
     */
    public boolean writeGraphToStream(final GraphReadMethods graph, final OutputStream out, final boolean verbose, final List<GraphElementType> elementTypes) throws IOException {
        // Get a new JSON writer.
        // Don't close the underlying zip stream automatically.
        final JsonGenerator jg = new JsonFactory().createGenerator(out, JsonEncoding.UTF8);
        jg.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jg.useDefaultPrettyPrinter();

        counter = 0;
        isCancelled = false;

        try {
            final int total = graph.getVertexCount() + graph.getTransactionCount();
            if (progress != null) {
                progress.start(total);
            }

            jg.writeStartArray();

            jg.writeStartObject();

            //write version number
            jg.writeNumberField("version", VERSION);

            //write versioned items
            jg.writeObjectFieldStart("versionedItems");
            for (Entry<String, Integer> itemVersion : UpdateProviderManager.getLatestVersions().entrySet()) {
                jg.writeNumberField(itemVersion.getKey(), itemVersion.getValue());
            }
            jg.writeEndObject();

            Schema schema = graph.getSchema();

            //write schema
            jg.writeStringField("schema", schema == null ? new BareSchemaFactory().getName() : schema.getFactory().getName());

            //write global modCounts
            final long globalModCount = graph.getGlobalModificationCounter();
            final long structModCount = graph.getStructureModificationCounter();
            final long attrModCount = graph.getStructureModificationCounter();
            jg.writeNumberField("global_mod_count", globalModCount);
            jg.writeNumberField("structure_mod_count", structModCount);
            jg.writeNumberField("attribute_mod_count", attrModCount);
            jg.writeEndObject();
            for (GraphElementType elementType : ELEMENT_TYPES_FILE_ORDER) {
                if (!isCancelled) {
                    writeElements(jg, graph, elementType, verbose, elementTypes.contains(elementType));
                }
            }

            jg.writeEndArray();
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } finally {
            jg.close();

            if (progress != null) {
                progress.finish();
            }
        }

        return isCancelled;
    }

    /**
     * Write elements of a Graph to JSON.
     *
     * @param jg The JsonGenerator to use for writing.
     * @param graph The graph.
     * @param elementType The GraphElementType being written.
     * @param writeData If false, write out the attributes but not the data for
     * the given element type.
     *
     * @throws IOException
     */
    private void writeElements(final JsonGenerator jg, final GraphReadMethods graph, final GraphElementType elementType, final boolean verbose, final boolean writeData) throws Exception {
        final String elementTypeLabel = IoUtilities.getGraphElementTypeString(elementType);

        if (progress != null) {
            progress.progress("Writing " + elementTypeLabel + " elements...");
        }

        final AbstractGraphIOProvider[] ioProviders = new AbstractGraphIOProvider[graph.getAttributeCapacity()];

        final ArrayList<Attribute> attrs = new ArrayList<>();
        for (int position = 0; position < graph.getAttributeCount(elementType); position++) {
            final int attrId = graph.getAttribute(elementType, position);
            final Attribute attr = new GraphAttribute(graph, attrId);

            ioProviders[attrId] = graphIoProviders.get(attr.getAttributeType());
            // Don't write non-META object types; we don't know what they are.
            if (!"object".equals(attr.getAttributeType()) || elementType == GraphElementType.META) {
                attrs.add(attr);
            }
        }

        // Here we go.
        jg.writeStartObject();
        jg.writeArrayFieldStart(elementTypeLabel);
        jg.writeStartObject();
        jg.writeArrayFieldStart("attrs");

        // Write the attributes.
        for (Attribute attr : attrs) {
            jg.writeStartObject();
            jg.writeStringField("label", attr.getName());
            jg.writeStringField("type", attr.getAttributeType());

            if (attr.getDescription() != null) {
                jg.writeStringField("descr", attr.getDescription());
            }

            // TODO: this is really horrible. We should not just be getting the default value as whatever type the description feels like giving us and then writing out it as a number (after introspecting its type) or its toString() value.
            // This should be done in a safe, extensible and verifiable manner, and more importantly, in a manner consistent with the way the attribute values themselves are written out (using IO providers). The long term solution to this
            // is probably not to just change the code here (or add in some default writing/reading stuff in IO providers), but to actually integrate the getting and setting of defaults into the getting and setting of
            // actual attribute values inside the attribute descriptions.
            if (attr.getDefaultValue() != null && isNumeric(attr)) {
                jg.writeNumberField(DEFAULT_FIELD, ((Number) attr.getDefaultValue()).doubleValue());
            } else if (attr.getDefaultValue() != null && "boolean".equals(attr.getAttributeType())) {
                jg.writeBooleanField(DEFAULT_FIELD, (Boolean) attr.getDefaultValue());
            } else if (attr.getDefaultValue() != null) {
                jg.writeStringField(DEFAULT_FIELD, attr.getDefaultValue().toString());
            } else {
                // Do nothing
            }

            if (attr.getAttributeMerger() != null) {
                jg.writeStringField("merger", attr.getAttributeMerger().getId());
            }

            jg.writeNumberField("mod_count", graph.getValueModificationCounter(attr.getId()));

            jg.writeEndObject();
        }

        jg.writeEndArray();

        // Check for optional key.
        if (elementType == GraphElementType.VERTEX || elementType == GraphElementType.TRANSACTION) {
            final int[] key = graph.getPrimaryKey(elementType);
            if (key.length > 0) {
                // Write the labels of the key attributes.
                jg.writeArrayFieldStart("key");

                for (int i = 0; i < key.length; i++) {
                    final Attribute attr = new GraphAttribute(graph, key[i]);
                    jg.writeString(attr.getName());
                }

                jg.writeEndArray();
            }
        }

        jg.writeEndObject();

        // Write the main graph data (graph, vertex, transaction).
        jg.writeStartObject();
        jg.writeArrayFieldStart("data");

        if (!writeData) {
        } else if (elementType == GraphElementType.GRAPH || elementType == GraphElementType.META) {
            jg.writeStartObject();
            for (Attribute attr : attrs) {
                final AbstractGraphIOProvider ioProvider = ioProviders[attr.getId()];
                if (ioProvider != null) {

                    // Get the provider to write its data into an ObjectNode.
                    // If they didn't write anything, don't write the data to the JSON.
                    ioProvider.writeObject(attr, 0, jg, graph, byteWriter, verbose);
                } else {
                    final Object value = graph.getObjectValue(attr.getId(), 0);
                    final String className = value != null ? value.getClass().getName() : "<null>";
                    final String msg = String.format("No I/O provider found for object type %s, attribute %s", className, attr);
                    throw new Exception(msg);
                }

            }

            jg.writeEndObject();
        } else if (elementType == GraphElementType.VERTEX) {
            for (int position = 0; position < graph.getVertexCount(); position++) {
                final int vxId = graph.getVertex(position);

                jg.writeStartObject();
                jg.writeNumberField(GraphFileConstants.VX_ID, vxId);
                for (Attribute attr : attrs) {
                    final AbstractGraphIOProvider ioProvider = ioProviders[attr.getId()];
                    if (ioProvider != null) {
                        // Get the provider to write its data into an ObjectNode.
                        // If they didn't write anything, don't write the data to the JSON.
                        ioProvider.writeObject(attr, vxId, jg, graph, byteWriter, verbose);
                    } else {
                        throw new Exception("No IO provider found for attribute type: " + attr.getAttributeType());
                    }

                }

                jg.writeEndObject();

                counter++;
                if (counter % REPORT_INTERVAL == 0 && isCancelled) {
                    return;
                } else if (counter % REPORT_INTERVAL == 0 && progress != null) {
                    progress.progress(counter);
                } else {
                    // Do nothing
                }
            }
        } else if (elementType == GraphElementType.TRANSACTION) {
            for (int position = 0; position < graph.getTransactionCount(); position++) {
                final int txId = graph.getTransaction(position);

                jg.writeStartObject();
                jg.writeNumberField(GraphFileConstants.TX_ID, txId);
                jg.writeNumberField(GraphFileConstants.SRC, graph.getTransactionSourceVertex(txId));
                jg.writeNumberField(GraphFileConstants.DST, graph.getTransactionDestinationVertex(txId));
                jg.writeBooleanField(GraphFileConstants.DIR, graph.getTransactionDirection(txId) != Graph.UNDIRECTED);
                for (Attribute attr : attrs) {
                    final AbstractGraphIOProvider ioProvider = ioProviders[attr.getId()];
                    if (ioProvider != null) {
                        // Get the provider to write its data into an ObjectNode.
                        // If they didn't write anything, don't write the data to the JSON.
                        ioProvider.writeObject(attr, txId, jg, graph, byteWriter, verbose);
                    } else {
                        throw new Exception("No IO provider found for attribute type: " + attr.getAttributeType());
                    }
                }

                jg.writeEndObject();

                counter++;
                if (counter % REPORT_INTERVAL == 0 && isCancelled) {
                    return;
                } else if (counter % REPORT_INTERVAL == 0 && progress != null) {
                    progress.progress(counter);
                }
            }
        } else {
            // Do nothing
        }

        jg.writeEndArray();
        jg.writeEndObject();

        jg.writeEndArray();
        jg.writeEndObject();
    }

    /**
     * Is the given Attribute numeric?
     *
     * @param attr An Attribute.
     *
     * @return true if the attribute is numeric, false otherwise.
     */
    private static boolean isNumeric(final Attribute attr) {
        final String type = attr.getAttributeType();

        return "integer".equals(type) || "float".equals(type);
    }

    @Override
    public boolean cancel() {
        isCancelled = true;

        return true;
    }
}
