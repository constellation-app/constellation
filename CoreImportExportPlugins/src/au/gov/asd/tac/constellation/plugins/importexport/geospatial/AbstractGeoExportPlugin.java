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
package au.gov.asd.tac.constellation.plugins.importexport.geospatial;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import static au.gov.asd.tac.constellation.plugins.importexport.geospatial.AbstractGeoExportPlugin.ELEMENT_TYPE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.plugins.importexport.geospatial.AbstractGeoExportPlugin.OUTPUT_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ElementTypeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.GraphAttributeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.NotifyDescriptor;

/**
 * Abstract geo export plugin.
 *
 * @author cygnus_x-1
 */
public abstract class AbstractGeoExportPlugin extends SimpleReadPlugin {

    public static final String OUTPUT_PARAMETER_ID = PluginParameter.buildId(AbstractGeoExportPlugin.class, "output");
    public static final String SPATIAL_REFERENCE_PARAMETER_ID = PluginParameter.buildId(AbstractGeoExportPlugin.class, "spatial_reference");
    public static final String ELEMENT_TYPE_PARAMETER_ID = PluginParameter.buildId(AbstractGeoExportPlugin.class, "element_type");
    public static final String ATTRIBUTES_PARAMETER_ID = PluginParameter.buildId(AbstractGeoExportPlugin.class, "attributes");
    public static final String SELECTED_ONLY_PARAMETER_ID = PluginParameter.buildId(AbstractGeoExportPlugin.class, "selected_only");

    /**
     * A {@link ExtensionFilter} specifying the file extension of the exported
     * file.
     *
     * @return an {@link ExtensionFilter}
     */
    protected abstract ExtensionFilter getExportType();

    /**
     *
     * @param parameters
     * @param uuid
     * @param shapes
     * @param attributes
     * @param output
     * @throws IOException
     */
    protected abstract void exportGeo(final PluginParameters parameters, final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes, final File output) throws IOException;

    /**
     * Determines whether the spatial reference parameter is active for this
     * plugin.
     *
     * @return true is spatial reference should be included, false otherwise
     */
    protected boolean includeSpatialReference() {
        return false;
    }

    @Override
    @SuppressWarnings("fallthrough") //the fallthrough at the switch statement is intentional
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FileParameterValue> outputParameter = FileParameterType.build(OUTPUT_PARAMETER_ID);
        outputParameter.setName("Output File");
        outputParameter.setDescription("The name of the output file");
        outputParameter.setRequired(true);
        FileParameterType.setKind(outputParameter, FileParameterType.FileParameterKind.SAVE);
        FileParameterType.setFileFilters(outputParameter, getExportType());
        parameters.addParameter(outputParameter);

        if (includeSpatialReference()) {
            final PluginParameter<SingleChoiceParameterValue> spatialReferenceParameter = SingleChoiceParameterType.build(SPATIAL_REFERENCE_PARAMETER_ID, SpatialReferenceParameterValue.class);
            spatialReferenceParameter.setName("Spatial Reference");
            spatialReferenceParameter.setDescription("The spatial reference to use for the geopackage");
            final List<SpatialReferenceParameterValue> spatialReferences = Arrays.asList(Shape.SpatialReference.values()).stream()
                    .map(spatialReference -> new SpatialReferenceParameterValue(spatialReference)).collect(Collectors.toList());
            SingleChoiceParameterType.setOptionsData(spatialReferenceParameter, spatialReferences);
            SingleChoiceParameterType.setChoiceData(spatialReferenceParameter, spatialReferences.get(0));
            parameters.addParameter(spatialReferenceParameter);
        }

        final PluginParameter<SingleChoiceParameterValue> elementTypeParameter = SingleChoiceParameterType.build(ELEMENT_TYPE_PARAMETER_ID, ElementTypeParameterValue.class);
        elementTypeParameter.setName("Element Type");
        elementTypeParameter.setDescription("The graph element type");
        elementTypeParameter.setRequired(true);
        final List<ElementTypeParameterValue> elementTypes = new ArrayList<>();
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.TRANSACTION));
        elementTypes.add(new ElementTypeParameterValue(GraphElementType.VERTEX));
        SingleChoiceParameterType.setOptionsData(elementTypeParameter, elementTypes);
        SingleChoiceParameterType.setChoiceData(elementTypeParameter, new ElementTypeParameterValue(GraphElementType.VERTEX));
        parameters.addParameter(elementTypeParameter);

        final PluginParameter<MultiChoiceParameterValue> attributesParameter = MultiChoiceParameterType.build(ATTRIBUTES_PARAMETER_ID, GraphAttributeParameterValue.class);
        attributesParameter.setName("Attributes");
        attributesParameter.setDescription("The list of attribute names to include in the export");
        attributesParameter.setEnabled(false);
        parameters.addParameter(attributesParameter);

        final PluginParameter<BooleanParameterValue> selectedOnlyParameter = BooleanParameterType.build(SELECTED_ONLY_PARAMETER_ID);
        selectedOnlyParameter.setName("Selected Only");
        selectedOnlyParameter.setDescription("If True, only export the selected nodes. The default is False.");
        parameters.addParameter(selectedOnlyParameter);

        parameters.addController(ELEMENT_TYPE_PARAMETER_ID, (master, params, change) -> {
            if (change == ParameterChange.VALUE) {
                final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
                if (activeGraph != null) {

                    // create options by getting attributes for the chosen element type from the graph
                    final List<GraphAttributeParameterValue> attributeOptions = new ArrayList<>();
                    final ReadableGraph readableGraph = activeGraph.getReadableGraph();
                    try {
                        final ParameterValue pv = params.get(master.getId()).getSingleChoice();
                        assert (pv instanceof ElementTypeParameterValue);
                        final GraphElementType elementType = ((ElementTypeParameterValue) pv).getGraphElementType();
                        switch (elementType) {
                            case TRANSACTION:
                                final int transactionAttributeCount = readableGraph.getAttributeCount(GraphElementType.TRANSACTION);
                                for (int attributePosition = 0; attributePosition < transactionAttributeCount; attributePosition++) {
                                    final int attributeId = readableGraph.getAttribute(GraphElementType.TRANSACTION, attributePosition);
                                    final GraphAttribute graphAttribute = new GraphAttribute(readableGraph, attributeId);
                                    attributeOptions.add(new GraphAttributeParameterValue(graphAttribute));
                                }
                            // fall through
                            case VERTEX:
                                final int vertexAttributeCount = readableGraph.getAttributeCount(GraphElementType.VERTEX);
                                for (int attributePosition = 0; attributePosition < vertexAttributeCount; attributePosition++) {
                                    final int attributeId = readableGraph.getAttribute(GraphElementType.VERTEX, attributePosition);
                                    final GraphAttribute graphAttribute = new GraphAttribute(readableGraph, attributeId);
                                    attributeOptions.add(new GraphAttributeParameterValue(graphAttribute));
                                }
                                break;
                            default:
                                return;
                        }
                    } finally {
                        readableGraph.release();
                    }

                    // create choices by deselecting lowercase attributes by default
                    final List<GraphAttributeParameterValue> attributeChoices = attributeOptions.stream()
                            .filter(attributeOption -> !((GraphAttribute) attributeOption.getObjectValue()).getName().matches("[a-z]{1}.*"))
                            .collect(Collectors.toList());

                    // sort options and choices lists
                    Collections.sort(attributeOptions);
                    Collections.sort(attributeChoices);

                    // update attributes parameter
                    @SuppressWarnings("unchecked") // Attrbutes_Parameter is created as a MultiChoice parameter in this class on line 137.
                    final PluginParameter<MultiChoiceParameterValue> updatedAttributesParameter = (PluginParameter<MultiChoiceParameterValue>) params.get(ATTRIBUTES_PARAMETER_ID);
                    MultiChoiceParameterType.setOptionsData(updatedAttributesParameter, attributeOptions);
                    MultiChoiceParameterType.setChoicesData(updatedAttributesParameter, attributeChoices);
                    updatedAttributesParameter.setEnabled(true);
                }
            }
        });

        return parameters;
    }

    @Override
    public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        if (parameters.getStringValue(OUTPUT_PARAMETER_ID) == null) {
            NotifyDisplayer.display("Invalid output file provided, cannot be empty", NotifyDescriptor.ERROR_MESSAGE);
            return;
        }
        if (parameters.getSingleChoice(ELEMENT_TYPE_PARAMETER_ID) == null) {
            NotifyDisplayer.display("Invalid element type provided, cannot be empty", NotifyDescriptor.ERROR_MESSAGE);
            return;
        }
        final File output = new File(parameters.getStringValue(OUTPUT_PARAMETER_ID));
        final GraphElementType elementType = (GraphElementType) ((ElementTypeParameterValue) parameters.getSingleChoice(ELEMENT_TYPE_PARAMETER_ID)).getObjectValue();
        final List<GraphAttribute> graphAttributes = parameters.getMultiChoiceValue(ATTRIBUTES_PARAMETER_ID).getChoicesData().stream()
                .map(attributeChoice -> (GraphAttribute) ((GraphAttributeParameterValue) attributeChoice).getObjectValue())
                .collect(Collectors.toList());
        final boolean selectedOnly = parameters.getBooleanValue(SELECTED_ONLY_PARAMETER_ID);

        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int vertexLatitudeAttributeId = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
        final int vertexLongitudeAttributeId = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
        final int vertexShapeAttributeId = SpatialConcept.VertexAttribute.SHAPE.get(graph);
        final int transactionIdentifierAttributeId = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        final int transactionLatitudeAttributeId = SpatialConcept.TransactionAttribute.LATITUDE.get(graph);
        final int transactionLongitudeAttributeId = SpatialConcept.TransactionAttribute.LONGITUDE.get(graph);
        final int transactionShapeAttributeId = SpatialConcept.TransactionAttribute.SHAPE.get(graph);

        final Map<String, String> shapes = new HashMap<>();
        final Map<String, Map<String, Object>> attributes = new HashMap<>();

        switch (elementType) {
            case VERTEX:
                final int vertexCount = graph.getVertexCount();
                for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                    final int vertexId = graph.getVertex(vertexPosition);
                    final boolean vertexSelected = graph.getBooleanValue(vertexSelectedAttributeId, vertexId);
                    final String vertexIdentifier = graph.getStringValue(vertexIdentifierAttributeId, vertexId);
                    final Float vertexLatitude = vertexLatitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(vertexLatitudeAttributeId, vertexId);
                    final Float vertexLongitude = vertexLongitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(vertexLongitudeAttributeId, vertexId);
                    final String vertexShape = vertexShapeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getStringValue(vertexShapeAttributeId, vertexId);

                    // if the vertex represents a valid geospatial shape, record it
                    boolean shapeFound = false;
                    if ((!selectedOnly || vertexSelected) && StringUtils.isNotBlank(vertexShape)
                            && Shape.isValidGeoJson(vertexShape)) {
                        shapes.put(vertexIdentifier, vertexShape);
                        shapeFound = true;
                    } else if ((!selectedOnly || vertexSelected) && vertexLatitude != null && vertexLongitude != null) {
                        try {
                            final String vertexPoint = Shape.generateShape(vertexIdentifier, GeometryType.POINT, Arrays.asList(Tuple.create((double) vertexLongitude, (double) vertexLatitude)));
                            shapes.put(vertexIdentifier, vertexPoint);
                            shapeFound = true;
                        } catch (final IOException ex) {
                            throw new PluginException(PluginNotificationLevel.ERROR, ex);
                        }
                    } else {
                        // Do nothing
                    }

                    // ... and record all its attributes
                    if (shapeFound) {
                        final Map<String, Object> attributeMap = new HashMap<>();
                        for (final GraphAttribute graphAttribute : graphAttributes) {
                            final Object attributeValue = graph.getObjectValue(graphAttribute.getId(), vertexId);
                            attributeMap.put(graphAttribute.getName(), attributeValue);
                        }
                        attributes.put(vertexIdentifier, attributeMap);
                    }
                }
                break;
            case TRANSACTION:
                final int transactionCount = graph.getTransactionCount();
                for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                    final int transactionId = graph.getTransaction(transactionPosition);
                    final boolean transactionSelected = graph.getBooleanValue(transactionSelectedAttributeId, transactionId);
                    final String transactionIdentifier = graph.getStringValue(transactionIdentifierAttributeId, transactionId);
                    final Float transactionLatitude = transactionLatitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(transactionLatitudeAttributeId, transactionId);
                    final Float transactionLongitude = transactionLongitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(transactionLongitudeAttributeId, transactionId);
                    final String transactionShape = transactionShapeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getStringValue(transactionShapeAttributeId, transactionId);

                    final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
                    final String sourceVertexIdentifier = graph.getStringValue(vertexIdentifierAttributeId, sourceVertexId);
                    final Float sourceVertexLatitude = vertexLatitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(vertexLatitudeAttributeId, sourceVertexId);
                    final Float sourceVertexLongitude = vertexLongitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(vertexLongitudeAttributeId, sourceVertexId);
                    final String sourceVertexShape = vertexShapeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getStringValue(vertexShapeAttributeId, sourceVertexId);

                    final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);
                    final String destinationVertexIdentifier = graph.getStringValue(vertexIdentifierAttributeId, destinationVertexId);
                    final Float destinationVertexLatitude = vertexLatitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(vertexLatitudeAttributeId, destinationVertexId);
                    final Float destinationVertexLongitude = vertexLongitudeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getObjectValue(vertexLongitudeAttributeId, destinationVertexId);
                    final String destinationVertexShape = vertexShapeAttributeId == GraphConstants.NOT_FOUND ? null
                            : graph.getStringValue(vertexShapeAttributeId, destinationVertexId);

                    // if the transaction represents a valid geospatial shape, record it
                    boolean shapeFound = false;
                    if ((!selectedOnly || transactionSelected) && StringUtils.isNotBlank(transactionShape)
                            && Shape.isValidGeoJson(transactionShape)) {
                        shapes.put(transactionIdentifier, transactionShape);
                        shapeFound = true;
                    } else if ((!selectedOnly || transactionSelected) && transactionLatitude != null && transactionLongitude != null) {
                        try {
                            final String transactionPoint = Shape.generateShape(transactionIdentifier, GeometryType.POINT, Arrays.asList(Tuple.create((double) transactionLongitude, (double) transactionLatitude)));
                            shapes.put(transactionIdentifier, transactionPoint);
                            shapeFound = true;
                        } catch (final IOException ex) {
                            throw new PluginException(PluginNotificationLevel.ERROR, ex);
                        }
                    } else {
                        // Do nothing
                    }

                    // ... and record all its attributes
                    if (shapeFound) {
                        final Map<String, Object> attributeMap = new HashMap<>();
                        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
                        for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                            final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);
                            final String transactionAttributeName = graph.getAttributeName(transactionAttributeId);
                            if (Character.isUpperCase(transactionAttributeName.charAt(0))) {
                                final Object transactionAttributeValue = graph.getObjectValue(transactionAttributeId, transactionId);
                                attributeMap.put(GraphRecordStoreUtilities.TRANSACTION + transactionAttributeName, transactionAttributeValue);
                            }
                        }
                        final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
                        for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
                            final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);
                            final String sourceVertexAttributeName = graph.getAttributeName(vertexAttributeId);
                            if (Character.isUpperCase(sourceVertexAttributeName.charAt(0))) {
                                final Object sourceVertexAttributeValue = graph.getObjectValue(vertexAttributeId, sourceVertexId);
                                attributeMap.put(GraphRecordStoreUtilities.SOURCE + sourceVertexAttributeName, sourceVertexAttributeValue);
                            }
                            final String destinationVertexAttributeName = graph.getAttributeName(vertexAttributeId);
                            if (Character.isUpperCase(destinationVertexAttributeName.charAt(0))) {
                                final Object destinationVertexAttributeValue = graph.getObjectValue(vertexAttributeId, destinationVertexId);
                                attributeMap.put(GraphRecordStoreUtilities.DESTINATION + destinationVertexAttributeName, destinationVertexAttributeValue);
                            }
                        }
                        attributes.put(transactionIdentifier, attributeMap);
                    }

                    // if the source vertex represents a valid geospatial shape, record it
                    shapeFound = false;
                    if ((!selectedOnly || transactionSelected) && StringUtils.isNotBlank(sourceVertexShape)
                            && Shape.isValidGeoJson(sourceVertexShape)) {
                        shapes.put(sourceVertexIdentifier, sourceVertexShape);
                        shapeFound = true;
                    } else if ((!selectedOnly || transactionSelected) && sourceVertexLatitude != null && sourceVertexLongitude != null) {
                        try {
                            final String vertexPoint = Shape.generateShape(sourceVertexIdentifier, GeometryType.POINT, Arrays.asList(Tuple.create((double) sourceVertexLongitude, (double) sourceVertexLatitude)));
                            shapes.put(sourceVertexIdentifier, vertexPoint);
                            shapeFound = true;
                        } catch (final IOException ex) {
                            throw new PluginException(PluginNotificationLevel.ERROR, ex);
                        }
                    } else {
                        // Do nothing
                    }

                    // ... and record all its attributes
                    if (shapeFound) {
                        final Map<String, Object> attributeMap = new HashMap<>();
                        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
                        for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                            final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);
                            final String transactionAttributeName = graph.getAttributeName(transactionAttributeId);
                            if (Character.isUpperCase(transactionAttributeName.charAt(0))) {
                                final Object transactionAttributeValue = graph.getObjectValue(transactionAttributeId, transactionId);
                                attributeMap.put(GraphRecordStoreUtilities.TRANSACTION + transactionAttributeName, transactionAttributeValue);
                            }
                        }
                        final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
                        for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
                            final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);
                            final String sourceVertexAttributeName = graph.getAttributeName(vertexAttributeId);
                            if (Character.isUpperCase(sourceVertexAttributeName.charAt(0))) {
                                final Object sourceVertexAttributeValue = graph.getObjectValue(vertexAttributeId, sourceVertexId);
                                attributeMap.put(GraphRecordStoreUtilities.SOURCE + sourceVertexAttributeName, sourceVertexAttributeValue);
                            }
                            final String destinationVertexAttributeName = graph.getAttributeName(vertexAttributeId);
                            if (Character.isUpperCase(destinationVertexAttributeName.charAt(0))) {
                                final Object destinationVertexAttributeValue = graph.getObjectValue(vertexAttributeId, destinationVertexId);
                                attributeMap.put(GraphRecordStoreUtilities.DESTINATION + destinationVertexAttributeName, destinationVertexAttributeValue);
                            }
                        }
                        attributes.put(sourceVertexIdentifier, attributeMap);
                    }

                    // if the destination vertex represents a valid geospatial shape, record it
                    shapeFound = false;
                    if ((!selectedOnly || transactionSelected) && StringUtils.isNotBlank(destinationVertexShape)
                            && Shape.isValidGeoJson(destinationVertexShape)) {
                        shapes.put(destinationVertexIdentifier, destinationVertexShape);
                        shapeFound = true;
                    } else if ((!selectedOnly || transactionSelected) && destinationVertexLatitude != null && destinationVertexLongitude != null) {
                        try {
                            final String vertexPoint = Shape.generateShape(destinationVertexIdentifier, GeometryType.POINT, Arrays.asList(Tuple.create((double) destinationVertexLongitude, (double) destinationVertexLatitude)));
                            shapes.put(destinationVertexIdentifier, vertexPoint);
                            shapeFound = true;
                        } catch (final IOException ex) {
                            throw new PluginException(PluginNotificationLevel.ERROR, ex);
                        }
                    } else {
                        // Do nothing
                    }

                    // ... and record all its attributes
                    if (shapeFound) {
                        final Map<String, Object> attributeMap = new HashMap<>();
                        final int transactionAttributeCount = graph.getAttributeCount(GraphElementType.TRANSACTION);
                        for (int transactionAttributePosition = 0; transactionAttributePosition < transactionAttributeCount; transactionAttributePosition++) {
                            final int transactionAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, transactionAttributePosition);
                            final String transactionAttributeName = graph.getAttributeName(transactionAttributeId);
                            if (Character.isUpperCase(transactionAttributeName.charAt(0))) {
                                final Object transactionAttributeValue = graph.getObjectValue(transactionAttributeId, transactionId);
                                attributeMap.put(GraphRecordStoreUtilities.TRANSACTION + transactionAttributeName, transactionAttributeValue);
                            }
                        }
                        final int vertexAttributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
                        for (int vertexAttributePosition = 0; vertexAttributePosition < vertexAttributeCount; vertexAttributePosition++) {
                            final int vertexAttributeId = graph.getAttribute(GraphElementType.VERTEX, vertexAttributePosition);
                            final String sourceVertexAttributeName = graph.getAttributeName(vertexAttributeId);
                            if (Character.isUpperCase(sourceVertexAttributeName.charAt(0))) {
                                final Object sourceVertexAttributeValue = graph.getObjectValue(vertexAttributeId, sourceVertexId);
                                attributeMap.put(GraphRecordStoreUtilities.SOURCE + sourceVertexAttributeName, sourceVertexAttributeValue);
                            }
                            final String destinationVertexAttributeName = graph.getAttributeName(vertexAttributeId);
                            if (Character.isUpperCase(destinationVertexAttributeName.charAt(0))) {
                                final Object destinationVertexAttributeValue = graph.getObjectValue(vertexAttributeId, destinationVertexId);
                                attributeMap.put(GraphRecordStoreUtilities.DESTINATION + destinationVertexAttributeName, destinationVertexAttributeValue);
                            }
                        }
                        attributes.put(destinationVertexIdentifier, attributeMap);
                    }
                }
                break;
            default:
                throw new PluginException(PluginNotificationLevel.ERROR, "Invalid element type");
        }

        try {            
            //Check for valid path
            if(isValidPath(output)) {
              exportGeo(parameters, GraphNode.getGraphNode(graph.getId()).getDisplayName(), shapes, attributes, output);  
            }            
        } catch (final IOException ex) {
            throw new PluginException(PluginNotificationLevel.ERROR, ex);
        }

        ConstellationLoggerHelper.exportPropertyBuilder(
                this,
                GraphRecordStoreUtilities.getVertices(graph, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                output,
                ConstellationLoggerHelper.SUCCESS
        );
    }
    
    private boolean isValidPath(File output) {
        if(StringUtils.isEmpty(output.getPath())) {
            NotifyDisplayer.display("Invalid output file provided, cannot be empty", NotifyDescriptor.ERROR_MESSAGE);
            return false;
        }
        if(output.isDirectory() || (!output.isDirectory() 
                && output.getParentFile() != null && output.getParentFile().exists())) {
            return true;
        } else {
            NotifyDisplayer.display("Invalid file path", NotifyDescriptor.ERROR_MESSAGE);
            return false;
        }        
    }
} 
