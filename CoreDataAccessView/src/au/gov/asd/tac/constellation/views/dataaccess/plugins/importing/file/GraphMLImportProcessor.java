/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.ProcessingException;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.utilities.xml.XmlUtilities;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin.RETRIEVE_TRANSACTIONS_PARAMETER_ID;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.GraphMLUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.DEFAULT_OPTION;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Importer for the GraphML file type
 *
 * @author canis_majoris
 * @author antares
 */
@ServiceProvider(service = GraphFileImportProcessor.class)
public class GraphMLImportProcessor implements GraphFileImportProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(GraphMLImportProcessor.class.getName());
    
    public static final String GRAPHML_TAG = "graphml";
    public static final String GRAPH_TAG = "graph";
    public static final String DIRECTION_TAG = "edgedefault";
    public static final String NODE_TAG = "node";
    public static final String DATA_TAG = "data";
    public static final String DEFAULT_TAG = "default";
    public static final String ID_TAG = "id";
    public static final String EDGE_TAG = "edge";
    public static final String EDGE_SRC_TAG = "source";
    public static final String EDGE_DST_TAG = "target";
    public static final String KEY_TAG = "key";
    public static final String KEY_NAME_TAG = "attr.name";
    public static final String KEY_TYPE_TAG = "attr.type";
    public static final String NAME_TYPE_DELIMITER = ",";
    public static final String KEY_FOR_TAG = "for";

    @Override
    public String getName() {
        return "GraphML";
    }

    @Override
    public ExtensionFilter getExtensionFilter() {
        return new ExtensionFilter("GraphML files", FileExtensionConstants.GRAPHML);
    }

    @Override
    public void process(final PluginParameters parameters, final File input, final RecordStore output) throws ProcessingException {
        if (input == null) {
            throw new ProcessingException("Please specify a file to read from");
        }
        
        if (output == null) {
            throw new ProcessingException("Please specify a record store to output to");
        }
        
        final RecordStore nodeRecords = new GraphRecordStore();
        final RecordStore edgeRecords = new GraphRecordStore();

        // Initialize variables
        final String filename = input.getPath();
        final boolean retrieveTransactions = parameters == null
                || parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID) == null
                || parameters.getParameters().get(RETRIEVE_TRANSACTIONS_PARAMETER_ID).getBooleanValue();
        final Map<String, String> nodeAttributes = new HashMap<>();
        final Map<String, String> transactionAttributes = new HashMap<>();
        final Map<String, String> defaultAttributes = new HashMap<>();
        final List<String> processingErrors = new ArrayList<>();

        try (final InputStream in = new FileInputStream(input)) {
            final XmlUtilities xml = new XmlUtilities();
            final Document document = xml.read(in, true);
            final Element documentElement = document.getDocumentElement();

            // Read attribute keys
            final NodeList keys = documentElement.getElementsByTagName(KEY_TAG);
            for (int index = 0; index < keys.getLength(); index++) {
                final Node key = keys.item(index);
                final NamedNodeMap attributes = key.getAttributes();
                final String id = attributes.getNamedItem(ID_TAG).getNodeValue();
                final String name = attributes.getNamedItem(KEY_NAME_TAG).getNodeValue()
                        + NAME_TYPE_DELIMITER
                        + attributes.getNamedItem(KEY_TYPE_TAG).getNodeValue();
                final String type = attributes.getNamedItem(KEY_FOR_TAG).getNodeValue();

                if (type.equals(NODE_TAG)) {
                    nodeAttributes.put(id, name);
                } else {
                    transactionAttributes.put(id, name);
                }
                
                // Check for default values
                if (key.hasChildNodes()) {
                    final NodeList children = key.getChildNodes();
                    for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
                        final Node childNode = children.item(childIndex);
                        if (childNode != null && childNode.getNodeName().equals(DEFAULT_TAG)) {
                            defaultAttributes.put(id, childNode.getTextContent());
                        }
                    }
                }
            }

            // Look for graphs
            final NodeList graphs = documentElement.getElementsByTagName(GRAPH_TAG);
            for (int index = 0; index < graphs.getLength(); index++) {
                final Node graph = graphs.item(index);
                final NamedNodeMap graphAttributes = graph.getAttributes();
                final String direction = graphAttributes.getNamedItem(DIRECTION_TAG).getNodeValue();
                final boolean undirected = "undirected".equals(direction);
                final Map<String, String> nodeIdToType = new HashMap<>();
                if (graph.hasChildNodes()) {
                    final NodeList children = graph.getChildNodes();
                    for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
                        final Node childNode = children.item(childIndex);
                        if (childNode != null) {
                            switch (childNode.getNodeName()) {
                                case NODE_TAG: {
                                    final NamedNodeMap attributes = childNode.getAttributes();
                                    final Node id = attributes.getNamedItem(ID_TAG);
                                    if (id == null){
                                        processingErrors.add("%s Node%s have a required identifier field.");
                                        continue;
                                    }
                                    final String stringID = id.getNodeValue();
                                    nodeRecords.add();
                                    nodeRecords.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, stringID);
                                    nodeRecords.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, SchemaVertexType.unknownType().getName());
                                    nodeRecords.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SOURCE, filename);
                                    for (final Entry<String, String> nodeAttributeEntry : nodeAttributes.entrySet()) {
                                        if (defaultAttributes.containsKey(nodeAttributeEntry.getKey())) {
                                            final String value = defaultAttributes.get(nodeAttributeEntry.getKey());
                                            final String attr = nodeAttributeEntry.getValue();
                                            final String attrName = attr.split(NAME_TYPE_DELIMITER)[0];
                                            final String attrType = attr.split(NAME_TYPE_DELIMITER)[1];
                                            GraphMLUtilities.addAttribute(nodeRecords, GraphRecordStoreUtilities.SOURCE, attrType, attrName, value);
                                        }
                                    }
                                    if (childNode.hasChildNodes()) {
                                        GraphMLUtilities.addAttributes(childNode, nodeAttributes, nodeRecords, GraphRecordStoreUtilities.SOURCE);
                                    }
                                    //store the type of each node id so that an edge can be matched to the correct source and destination
                                    if (retrieveTransactions) {
                                        nodeIdToType.put(stringID, nodeRecords.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE));
                                    }
                                    break;
                                }
                                case EDGE_TAG: {
                                    if (retrieveTransactions) {
                                        final NamedNodeMap attributes = childNode.getAttributes();
                                        final Node id = attributes.getNamedItem(ID_TAG);

                                        /* Constellation requires edge Idenitifers 
                                        ** but this isn't a requirement in the graphML specification
                                        ** http://graphml.graphdrawing.org/specification/xsd.html
                                        ** If no edge ID is given a UUID will be generated and assigned.
                                        */
                                        final String stringID = (id == null) ? UUID.randomUUID().toString() : id.getNodeValue();
                                        final Node source = attributes.getNamedItem(EDGE_SRC_TAG);
                                        final Node target = attributes.getNamedItem(EDGE_DST_TAG);

                                        // Error checking for required edge fields
                                        if(source == null){
                                            processingErrors.add("%s Edge%s have a required source field.");
                                            continue;
                                        }
                                        if(target == null){
                                            processingErrors.add("%s Edge%s have a required target field.");
                                            continue;
                                        }

                                        final String targetString = target.getNodeValue();
                                        final String sourceString = source.getNodeValue();
                                        
                                        
                                        edgeRecords.add();
                                        edgeRecords.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, sourceString);
                                        edgeRecords.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, targetString);
                                        edgeRecords.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, stringID);
                                        edgeRecords.set(GraphRecordStoreUtilities.TRANSACTION + AnalyticConcept.TransactionAttribute.SOURCE, filename);
                                        if (undirected) {
                                            edgeRecords.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.DIRECTED, false);
                                        }
                                        for (final Entry<String, String> transactionAttributeEntry : transactionAttributes.entrySet()) {
                                            if (defaultAttributes.containsKey(transactionAttributeEntry.getKey())) {
                                                final String value = defaultAttributes.get(transactionAttributeEntry.getKey());
                                                final String attr = transactionAttributeEntry.getValue();
                                                final String attrName = attr.split(NAME_TYPE_DELIMITER)[0];
                                                final String attrType = attr.split(NAME_TYPE_DELIMITER)[1];
                                                GraphMLUtilities.addAttribute(edgeRecords, GraphRecordStoreUtilities.TRANSACTION, attrType, attrName, value);
                                            }
                                        }
                                        if (childNode.hasChildNodes()) {
                                            GraphMLUtilities.addAttributes(childNode, transactionAttributes, edgeRecords, GraphRecordStoreUtilities.TRANSACTION);
                                        }
                                    }
                                    break;
                                }
                                default:
                                    break;
                            }
                        }
                    }
                    
                    // resolve the node types between edges so that each edge is correctly added to the graph
                    for (int i = 0; i < edgeRecords.size(); i++) {
                        edgeRecords.set(i, GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, 
                                nodeIdToType.get(edgeRecords.get(i, GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER)));
                        edgeRecords.set(i, GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE, 
                                nodeIdToType.get(edgeRecords.get(i, GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER)));
                    }
                }
            }
        } catch (final FileNotFoundException ex) {
            final String errorMsg = StringUtils.isEmpty(filename) ? "File not specified" : "File not found: " + filename;
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + errorMsg, "Import GraphML File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            final Throwable fnfEx = new FileNotFoundException(NotifyDisplayer.BLOCK_POPUP_FLAG + errorMsg);
            fnfEx.setStackTrace(ex.getStackTrace());
            LOGGER.log(Level.SEVERE, errorMsg, fnfEx);
        } catch (final TransformerException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } catch (final IOException ex) {
            final String errorMsg = StringUtils.isEmpty(filename) ? "File not specified " : "Error reading file: " + filename;
            NotifyDisplayer.display(new NotifyDescriptor("Error:\n" + errorMsg, "Import GraphML File", DEFAULT_OPTION, 
                    NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            final Throwable ioEx = new IOException(NotifyDisplayer.BLOCK_POPUP_FLAG + errorMsg);
            ioEx.setStackTrace(ex.getStackTrace());
            LOGGER.log(Level.SEVERE, errorMsg, ioEx);
        }

        output.add(nodeRecords);
        output.add(edgeRecords);

        // Add a warning for nodes and edges that were invalid snd not imported
        if(!processingErrors.isEmpty()){
            
            // Count distinct processing errors
            final Map<String, Long> processingErrorTypes = processingErrors.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            final String errorMsg = processingErrorTypes.entrySet().stream()
                    .map(e -> {
                        String plural = " doesn't";
                        final long errorCount = e.getValue();
                        if(errorCount > 1){
                            plural = "s don't";
                        }
                        
                        return String.format(e.getKey(), errorCount, plural);
                    })
                    .collect(Collectors.joining(SeparatorConstants.NEWLINE));

            NotifyDisplayer.display(new NotifyDescriptor("Warning - Some elements weren't able to be imported:\n" + errorMsg,
                    "Import GraphML File", DEFAULT_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION));
            LOGGER.log(Level.WARNING, errorMsg);
        }
    }
    
}
