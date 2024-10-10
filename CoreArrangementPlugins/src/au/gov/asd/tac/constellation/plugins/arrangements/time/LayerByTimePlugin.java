/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.time;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LayerNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.LayerName;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Use a datetime attribute on transactions to split the nodes and transactions
 * into layers on the z-axis.
 * <p>
 * Note that we create a new graph with Visual Schema and operate on that. We do
 * this because the resulting graph has duplicate node names which would
 * otherwise be merged on an analytic schema. These nodes are duplicated and
 * lots of transactions are created for visual purposes.
 *
 * @author procyon
 * @author antares
 */
@ServiceProvider(service = Plugin.class)
@Messages("LayerByTimePlugin=Layer by Time")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class LayerByTimePlugin extends SimpleReadPlugin {

    private static final Logger LOGGER = Logger.getLogger(LayerByTimePlugin.class.getName());
    
    public static final String DATETIME_ATTRIBUTE_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "date_time_attribute");
    private static final String DATETIME_PARAMETER_ID_NAME = "Date-time attribute";
    private static final String DATETIME_ATTRIBUTE_PARAMETER_ID_DESCRIPTION = "The date-time attribute to use for the layered graph.";

    public static final String DATE_RANGE_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "date_range");
    private static final String DATE_RANGE_PARAMETER_ID_NAME = "Date Range";
    private static final String DATE_RANGE_PARAM_DESCRIPTION = "The date range over which to create the layered graph";
    private static final DateTimeRange DATE_RANGE_PARAMETER_ID_DEFAULT = new DateTimeRange(ZonedDateTime.now(), ZonedDateTime.now());

    public static final String LAYER_BY_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "layer_by");
    private static final String LAYER_BY_PARAMETER_ID_NAME = "Layer date-times by";
    private static final String LAYER_BY_PARAMETER_ID_DESCRIPTION = "The method by which to layer graph elements based on their date-times.";
    private static final String INTERVAL_METHOD = "Intervals";
    private static final String BIN_METHOD = "Bins";
    private static final List<String> LAYER_BY_PARAM_VALUES = Arrays.asList(INTERVAL_METHOD, BIN_METHOD);
    private static final String LAYER_BY_PARAMETER_ID_DEFAULT = INTERVAL_METHOD;

    public static final String AMOUNT_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "amount");
    private static final String AMOUNT_PARAMETER_ID_NAME = "Intervals";
    private static final String AMOUNT_PARAMETER_ID_DESCRIPTION = "Number of Intervals to layer the graph into";
    private static final int AMOUNT_PARAMETER_ID_DEFAULT = 1;

    public static final String UNIT_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "interval_unit");
    private static final String UNIT_PARAMETER_ID_NAME = "Unit";
    private static final String UNIT_PARAMETER_ID_DESCRIPTION = "Unit of time by which to layer graph";
    private static final String UNIT_PARAMETER_ID_INTERVAL_DEFAULT = "Days";
    private static final String UNIT_PARAMETER_ID_BIN_DEFAULT = "Day of Week";
    private static final String UNIT_PARAMETER_ID_DEFAULT = UNIT_PARAMETER_ID_INTERVAL_DEFAULT;

    public static final String TRANSACTION_AS_LAYER_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "transaction_as_layer");
    private static final String TRANSACTION_AS_LAYER_NAME = "Transactions as layers";
    private static final String TRANSACTION_AS_LAYER_DESCRIPTION = "Use transactions as layers instead of nodes";
    private static final boolean TRANSACTION_AS_LAYER_DEFAULT = false;

    public static final String KEEP_TX_COLORS_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "keep_tx_colors");
    private static final String KEEP_TX_COLORS_NAME = "Keep transaction colors";
    private static final String KEEP_TX_COLORS_DESCRIPTION = "If unticked, transaction colors will be determined by a color gradient across layers";
    private static final boolean KEEP_TX_COLORS_DEFAULT = true;

    public static final String DRAW_TX_GUIDES_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "draw_tx_guides");
    private static final String DRAW_TX_GUIDES_NAME = "Draw transaction guide lines";
    private static final String DRAW_TX_GUIDES_DESCRIPTION = "Show indicator lines connecting the same node across different layers";
    private static final boolean DRAW_TX_GUIDES_DEFAULT = false;

    private static final String ORIGINAL_ID_LABEL = "layer_original_id";
    private static final String LAYER_NAME = "layer";
    private static final String NLAYERS = "layers";

    private static final Map<String, Integer> LAYER_INTERVALS = new HashMap<>();
    private static final Map<String, Integer> BIN_CALENDAR_UNITS = new HashMap<>();

    static {
        LAYER_INTERVALS.put("Seconds", 1);
        LAYER_INTERVALS.put("Minutes", 60);
        LAYER_INTERVALS.put("Hours", 60 * 60);
        LAYER_INTERVALS.put("Days", 60 * 60 * 24);
        BIN_CALENDAR_UNITS.put("Second", Calendar.SECOND);
        BIN_CALENDAR_UNITS.put("Minute", Calendar.MINUTE);
        BIN_CALENDAR_UNITS.put("Hour", Calendar.HOUR_OF_DAY);
        BIN_CALENDAR_UNITS.put("Day of Week", Calendar.DAY_OF_WEEK);
        BIN_CALENDAR_UNITS.put("Day of Month", Calendar.DAY_OF_MONTH);
        BIN_CALENDAR_UNITS.put("Week", Calendar.WEEK_OF_YEAR);
        BIN_CALENDAR_UNITS.put("Month", Calendar.MONTH);
        BIN_CALENDAR_UNITS.put("Year", Calendar.YEAR);
    }

    // Quick and dirty way of mapping existing nodeid + layer number to new nodeid.
    private final Map<String, Integer> nodeDups = new HashMap<>();
    private final Map<Float, List<Integer>> transactionLayers = new HashMap<>();
    // Map nodeId to a list of layer numbers.
    private final Map<Integer, BitSet> nodeIdToLayers = new HashMap<>();
    private Map<Integer, Integer> srcVxMap = new HashMap<>();
    private Map<Integer, Integer> dstVxMap = new HashMap<>();
    private final BitSet txToDelete = new BitSet();

    private PluginParameter<SingleChoiceParameterValue> dtAttrParam;
    private PluginParameter<DateTimeRangeParameterValue> dateRangeParam;

    @Override
    public PluginParameters createParameters() {
        System.out.println("Creating parameters in LayerByTimePlugin...");
        final PluginParameters parameters = new PluginParameters();

        dtAttrParam = SingleChoiceParameterType.build(DATETIME_ATTRIBUTE_PARAMETER_ID);
        dtAttrParam.setName(DATETIME_PARAMETER_ID_NAME);
        dtAttrParam.setDescription(DATETIME_ATTRIBUTE_PARAMETER_ID_DESCRIPTION);
        dtAttrParam.setRequired(true);
        parameters.addParameter(dtAttrParam);

        dateRangeParam = DateTimeRangeParameterType.build(DATE_RANGE_PARAMETER_ID);
        dateRangeParam.setName(DATE_RANGE_PARAMETER_ID_NAME);
        dateRangeParam.setDescription(DATE_RANGE_PARAM_DESCRIPTION);
        dateRangeParam.setDateTimeRangeValue(DATE_RANGE_PARAMETER_ID_DEFAULT);
        parameters.addParameter(dateRangeParam);

        final PluginParameter<SingleChoiceParameterValue> layerByParam = SingleChoiceParameterType.build(LAYER_BY_PARAMETER_ID);
        layerByParam.setName(LAYER_BY_PARAMETER_ID_NAME);
        layerByParam.setDescription(LAYER_BY_PARAMETER_ID_DESCRIPTION);
        SingleChoiceParameterType.setOptions(layerByParam, LAYER_BY_PARAM_VALUES);
        SingleChoiceParameterType.setChoice(layerByParam, LAYER_BY_PARAMETER_ID_DEFAULT);
        parameters.addParameter(layerByParam);

        final PluginParameter<IntegerParameterValue> amountParam = IntegerParameterType.build(AMOUNT_PARAMETER_ID);
        amountParam.setName(AMOUNT_PARAMETER_ID_NAME);
        amountParam.setDescription(AMOUNT_PARAMETER_ID_DESCRIPTION);
        amountParam.setIntegerValue(AMOUNT_PARAMETER_ID_DEFAULT);
        parameters.addParameter(amountParam);

        final PluginParameter<SingleChoiceParameterValue> unitParam = SingleChoiceParameterType.build(UNIT_PARAMETER_ID);
        unitParam.setName(UNIT_PARAMETER_ID_NAME);
        unitParam.setDescription(UNIT_PARAMETER_ID_DESCRIPTION);
        SingleChoiceParameterType.setOptions(unitParam, new ArrayList<>(LAYER_INTERVALS.keySet()));
        SingleChoiceParameterType.setChoice(unitParam, UNIT_PARAMETER_ID_DEFAULT);
        parameters.addParameter(unitParam);

        final PluginParameter<BooleanParameterValue> transactionParam = BooleanParameterType.build(TRANSACTION_AS_LAYER_PARAMETER_ID);
        transactionParam.setName(TRANSACTION_AS_LAYER_NAME);
        transactionParam.setDescription(TRANSACTION_AS_LAYER_DESCRIPTION);
        transactionParam.setBooleanValue(TRANSACTION_AS_LAYER_DEFAULT);
        parameters.addParameter(transactionParam);

        final PluginParameter<BooleanParameterValue> keepTxColorsParam = BooleanParameterType.build(KEEP_TX_COLORS_PARAMETER_ID);
        keepTxColorsParam.setName(KEEP_TX_COLORS_NAME);
        keepTxColorsParam.setDescription(KEEP_TX_COLORS_DESCRIPTION);
        keepTxColorsParam.setBooleanValue(KEEP_TX_COLORS_DEFAULT);
        parameters.addParameter(keepTxColorsParam);

        final PluginParameter<BooleanParameterValue> drawTxGuidesParam = BooleanParameterType.build(DRAW_TX_GUIDES_PARAMETER_ID);
        drawTxGuidesParam.setName(DRAW_TX_GUIDES_NAME);
        drawTxGuidesParam.setDescription(DRAW_TX_GUIDES_DESCRIPTION);
        drawTxGuidesParam.setBooleanValue(DRAW_TX_GUIDES_DEFAULT);
        parameters.addParameter(drawTxGuidesParam);

        parameters.addController(LAYER_BY_PARAMETER_ID, (masterId, paramMap, change) -> {
            if (change == ParameterChange.VALUE) {
                if (paramMap.get(LAYER_BY_PARAMETER_ID).getStringValue().equals(INTERVAL_METHOD)) {
                    SingleChoiceParameterType.setOptions(unitParam, new ArrayList<>(LAYER_INTERVALS.keySet()));
                    SingleChoiceParameterType.setChoice(unitParam, UNIT_PARAMETER_ID_INTERVAL_DEFAULT);
                    parameters.getParameters().get(AMOUNT_PARAMETER_ID).setEnabled(true);
                } else if (paramMap.get(LAYER_BY_PARAMETER_ID).getStringValue().equals(BIN_METHOD)) {
                    SingleChoiceParameterType.setOptions(unitParam, new ArrayList<>(BIN_CALENDAR_UNITS.keySet()));
                    SingleChoiceParameterType.setChoice(unitParam, UNIT_PARAMETER_ID_BIN_DEFAULT);
                    parameters.getParameters().get(AMOUNT_PARAMETER_ID).setEnabled(false);
                }
            }
        });

        return parameters;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {

        final ReadableGraph rg = graph.getReadableGraph();
        final List<String> dateTimeAttributes = new ArrayList<>();
        try {
            final int attributeCount = rg.getAttributeCount(GraphElementType.TRANSACTION);
            for (int i = 0; i < attributeCount; i++) {
                final int attrId = rg.getAttribute(GraphElementType.TRANSACTION, i);
                final Attribute attr = new GraphAttribute(rg, attrId);
                if (attr.getAttributeType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME)) {
                    dateTimeAttributes.add(attr.getName());
                }
            }
        } finally {
            rg.release();
        }
        SingleChoiceParameterType.setOptions(dtAttrParam, dateTimeAttributes);
        parameters.addController(DATETIME_ATTRIBUTE_PARAMETER_ID, (masterId, paramMap, change) -> {
            if (change == ParameterChange.VALUE) {
                final String attrName = paramMap.get(DATETIME_ATTRIBUTE_PARAMETER_ID).getStringValue();
                final ReadableGraph rg2 = graph.getReadableGraph();
                try {
                    final int attrId = rg2.getAttribute(GraphElementType.TRANSACTION, attrName);
                    if (attrId == Graph.NOT_FOUND) {
                        return;
                    }

                    ZonedDateTime min = ZonedDateTime.ofInstant(Instant.now(), TimeZoneUtilities.UTC);
                    ZonedDateTime max = ZonedDateTime.ofInstant(Instant.EPOCH, TimeZoneUtilities.UTC);
                    final int txCount = rg.getTransactionCount();
                    boolean nonNullDateTimeFound = false;
                    for (int position = 0; position < txCount; position++) {
                        final int txId = rg.getTransaction(position);

                        // Ignore zero and "null" dates.
                        final ZonedDateTime dt = rg.getObjectValue(attrId, txId);
                        if (dt != null) {
                            nonNullDateTimeFound = true;
                            if (dt.toInstant().isBefore(min.toInstant())) {
                                min = dt;
                            }
                            if (dt.toInstant().isAfter(max.toInstant())) {
                                max = dt;
                            }
                        }
                    }
                    if (!nonNullDateTimeFound) {
                        final ZonedDateTime swap = min;
                        min = max;
                        max = swap;
                    }
                    dateRangeParam.setDateTimeRangeValue(new DateTimeRange(min, max));
                } finally {
                    rg2.release();
                }
            }
        });
        if (!dateTimeAttributes.isEmpty()) {
            SingleChoiceParameterType.setChoice(dtAttrParam, dateTimeAttributes.get(0));
        }
    }

    @Override
    public void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException, InterruptedException {
        System.out.println("Reading LayerByTimePlugin...");
        // We have the dtAttr from the original wg: we should have been passed the label, but never mind.
        // We need to get the label from the original, so we can get the dtAttr for the copy.
        final String dtAttrOrig = parameters.getParameters().get(DATETIME_ATTRIBUTE_PARAMETER_ID).getStringValue();
        if (dtAttrOrig == null) {
            interaction.notify(PluginNotificationLevel.ERROR, "A date-time attribute must be specified.");
            LOGGER.log(Level.SEVERE, "A date-time attribute must be specified.");
            return;
        }

        final int dtAttrOrigId = rg.getAttribute(GraphElementType.TRANSACTION, dtAttrOrig);
        if (dtAttrOrigId == Graph.NOT_FOUND) {
            interaction.notify(PluginNotificationLevel.ERROR, "A valid date-time attribute must be specified.");
            LOGGER.log(Level.SEVERE, "A valid date-time attribute must be specified.");
            return;
        }

        Graph copy;
        try {
            final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
            final PluginParameters copyParams = copyGraphPlugin.createParameters();
            copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_SCHEMA_NAME_PARAMETER_ID).setStringValue(rg.getSchema().getFactory().getName());
            copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_ALL_PARAMETER_ID).setBooleanValue(true);
            copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_KEYS_PARAMETER_ID).setBooleanValue(false);
            PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyParams).executeNow(rg);
            copy = (Graph) copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();
        } catch (final PluginException ex) {
            copy = null;
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        if (copy == null) {
            // The copy failed, drop out now.
            return;
        }

        final Attribute dt = new GraphAttribute(rg, dtAttrOrigId);

        final WritableGraph wgcopy = copy.getWritableGraph("Layer by time", true);
        try {
            final int dtAttr = wgcopy.getAttribute(GraphElementType.TRANSACTION, dt.getName());

            final boolean useIntervals = parameters.getParameters().get(LAYER_BY_PARAMETER_ID).getStringValue().equals(INTERVAL_METHOD);
            final ZonedDateTime[] startEnd = parameters.getParameters().get(DATE_RANGE_PARAMETER_ID).getDateTimeRangeValue().getZonedStartEnd();
            final ZonedDateTime start = startEnd[0];
            final ZonedDateTime end = startEnd[1];
            final boolean isTransactionLayers = parameters.getParameters().get(TRANSACTION_AS_LAYER_PARAMETER_ID).getBooleanValue();

            //Establish new attributes.
            //Create and store graph attributes.
            final LayerName defaultName = new LayerName(Graph.NOT_FOUND, "Default");
            final int timeLayerAttr = wgcopy.addAttribute(GraphElementType.TRANSACTION, LayerNameAttributeDescription.ATTRIBUTE_NAME, LAYER_NAME, "time layer", defaultName, null);
            wgcopy.addAttribute(GraphElementType.GRAPH, IntegerAttributeDescription.ATTRIBUTE_NAME, NLAYERS, "The number of layers to layer by time", 1, null);
            final int txColorAttr = wgcopy.getAttribute(GraphElementType.TRANSACTION, "color");
            final int txGuideline = wgcopy.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "layer_guideline", "This transaction is a layer guideline", false, null);
            final ConstellationColor guidelineColor = ConstellationColor.getColorValue(0.25F, 0.25F, 0.25F, 1F);
            wgcopy.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, ORIGINAL_ID_LABEL, "Original Node Id", -1, null);

            final List<Float> values = new ArrayList<>();
            final Map<Integer, List<Float>> remappedLayers = new HashMap<>();
            final Map<Integer, String> displayNames = new HashMap<>();
            if (useIntervals) {
                final int intervalUnit = LAYER_INTERVALS.get(parameters.getParameters().get(UNIT_PARAMETER_ID).getStringValue());
                final int intervalAmount = parameters.getParameters().get(AMOUNT_PARAMETER_ID).getIntegerValue();
                buildIntervals(wgcopy, values, remappedLayers, displayNames, dtAttr, start.toInstant(), end.toInstant(), intervalUnit, intervalAmount);
            } else {
                final int calendarUnit = BIN_CALENDAR_UNITS.get(parameters.getParameters().get(UNIT_PARAMETER_ID).getStringValue());
                final int binAmount = parameters.getParameters().get(AMOUNT_PARAMETER_ID).getIntegerValue();
                buildBins(wgcopy, values, remappedLayers, displayNames, dtAttr, start.toInstant(), end.toInstant(), calendarUnit, binAmount);
            }

            final boolean keepTxColors = parameters.getParameters().get(KEEP_TX_COLORS_PARAMETER_ID).getBooleanValue();

            final boolean drawTxGuides = parameters.getParameters().get(DRAW_TX_GUIDES_PARAMETER_ID).getBooleanValue();

            // Modify the copied graph to show our layers.
            int z = 0;
            final float step = getWidth(wgcopy) / values.size();
            for (final Entry<Integer, List<Float>> entry : remappedLayers.entrySet()) {
                for (final Entry<Float, List<Integer>> currentLayer : transactionLayers.entrySet()) {
                    if (entry.getValue().contains(currentLayer.getKey())) {
                        for (final int txId : currentLayer.getValue()) {
                            final float origLayer = currentLayer.getKey();
                            int newLayer = 0;
                            if (entry.getValue().contains(origLayer)) {
                                //Overwrite value
                                newLayer = entry.getKey();
                            }

                            final LayerName dn = new LayerName(newLayer, displayNames.get(newLayer));
                            wgcopy.setObjectValue(timeLayerAttr, txId, dn);

                            final float normLayer = newLayer / (remappedLayers.keySet().size() * 1F);

                            if (!keepTxColors) {
                                final Color heatmap = new Color(Color.HSBtoRGB((1 - normLayer) * 2F / 3F, 0.5F, 1));
                                final ConstellationColor color = ConstellationColor.getColorValue(heatmap.getRed() / 255F, heatmap.getGreen() / 255F, heatmap.getBlue() / 255F, 1F);
                                wgcopy.setObjectValue(txColorAttr, txId, color);
                            }

                            if (isTransactionLayers) {
                                transactionsAsLayers(wgcopy, txId, z, step);
                            } else {
                                nodesAsLayers(wgcopy, txId, newLayer);
                            }
                        }
                    }
                }
                if (isTransactionLayers) {
                    srcVxMap = dstVxMap;
                    dstVxMap = new HashMap<>();
                    z += step;
                }
            }

            //Remove any outstanding transactions flagged for deletion
            for (int txId = txToDelete.nextSetBit(0); txId >= 0; txId = txToDelete.nextSetBit(txId + 1)) {
                wgcopy.removeTransaction(txId);
            }

            // Get rid of all of the nodes that don't have any transactions.
            // By definition, the duplicates will have transactions between them, including the original layer
            // (because we just deleted transactions that belong in different layers, leaving only the transactions
            // that belong in the original layer).
            final List<Integer> vertices = new ArrayList<>();
            for (int position = 0; position < wgcopy.getVertexCount(); position++) {
                final int vertexId = wgcopy.getVertex(position);
                final int nTx = wgcopy.getVertexTransactionCount(vertexId);
                if (nTx == 0) {
                    vertices.add(vertexId);
                }
            }

            vertices.stream().forEach(wgcopy::removeVertex);

            if (drawTxGuides) {
                interaction.setProgress(5, 6, "Draw guide lines", false);

                // Draw a grey vertical indicator line connecting the same nodes in each layer.
                // We have to do this after the "remove node without transactions" step because we're adding more transactions.
                if (!isTransactionLayers && remappedLayers.keySet().size() > 1) {
                    nodeIdToLayers.keySet().stream().forEach(origNodeId -> {
                        int prevNodeId = -1;
                        final BitSet layers = nodeIdToLayers.get(origNodeId);
                        for (int layer = layers.nextSetBit(0); layer >= 0; layer = layers.nextSetBit(layer + 1)) {
                            final int nodeId = layer == 0 ? origNodeId : nodeDups.get(String.format("%s/%s", origNodeId, layer));
                            if (prevNodeId != -1) {
                                final int sTxId = wgcopy.addTransaction(prevNodeId, nodeId, false);
                                wgcopy.setBooleanValue(txGuideline, sTxId, true);
                                wgcopy.setObjectValue(txColorAttr, sTxId, guidelineColor);
                                final LayerName dn = new LayerName(1107, "Guideline");
                                wgcopy.setObjectValue(timeLayerAttr, sTxId, dn);
                            }
                            prevNodeId = nodeId;
                        }
                    });
                }
            }
        } finally {
            wgcopy.commit();
        }
    }

    /**
     *
     * @param wgcopy
     * @param values
     * @param remappedLayers
     * @param displayNames
     * @param dtAttr
     * @param d1
     * @param d2
     * @param unit The interval unit in seconds.
     * @param amount The number of interval units per layer.
     */
    private void buildIntervals(final GraphWriteMethods wgcopy, final List<Float> values, final Map<Integer, List<Float>> remappedLayers, final Map<Integer, String> displayNames, final int dtAttr, final Instant d1, final Instant d2, final int unit, final int amount) {

        // Convert to milliseconds.
        final long intervalLength = unit * amount * 1000L;
        final long d1t = d1.toEpochMilli();
        final long d2t = d2.toEpochMilli();

        final BitSet txUnused = new BitSet();
        for (int position = 0; position < wgcopy.getTransactionCount(); position++) {
            final int txId = wgcopy.getTransaction(position);

            // Only use transactions that have a datetime value set.
            final long date = wgcopy.getLongValue(dtAttr, txId);

            if (d1t <= date && date < d2t) {
                final float layer = (float) (date - d1t) / intervalLength;

                if (!transactionLayers.containsKey(layer)) {
                    transactionLayers.put(layer, new ArrayList<>());
                }

                transactionLayers.get(layer).add(txId);

                if (!values.contains(layer)) {
                    values.add(layer);
                }
            } else {
                txUnused.set(txId);
            }
        }

        for (int txId = txUnused.nextSetBit(0); txId >= 0; txId = txUnused.nextSetBit(txId + 1)) {
            wgcopy.removeTransaction(txId);
        }

        Collections.sort(values);

        for (int i = 0; i < values.size(); i++) {
            final List<Float> runningLayers = new ArrayList<>();
            runningLayers.add(values.get(i));
            remappedLayers.put(i, runningLayers);
        }

        // Now that we have the total number of layers, tell the graph so it can handle the visibility toggle.
        final int nLayersAttr = wgcopy.getAttribute(GraphElementType.GRAPH, NLAYERS);
        wgcopy.setIntValue(nLayersAttr, 0, remappedLayers.keySet().size());

        final SimpleDateFormat sdf = new SimpleDateFormat("EE yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < values.size(); i++) {
            final long layerBase = values.get(i).longValue() * intervalLength + d1t;
            final long layerEnd = layerBase + intervalLength - 1;
            displayNames.put(i, String.format("%s .. %s", sdf.format(layerBase), sdf.format(layerEnd)));
        }
    }

    /**
     * Build the values required to display layers as bins.
     *
     * @param wgcopy
     * @param values
     * @param remappedLayers
     * @param displayNames
     * @param dtAttr
     * @param d1
     * @param d2
     * @param unit
     * @param binAmount
     */
    private void buildBins(final GraphWriteMethods wgcopy, final List<Float> values, final Map<Integer, List<Float>> remappedLayers, final Map<Integer, String> displayNames, final int dtAttr, final Instant d1, final Instant d2, final int unit, final int binAmount) {
        final Calendar dtg = Calendar.getInstance();
        final float maxUnit = dtg.getMaximum(unit);

        if (binAmount > maxUnit) {
            throw new RuntimeException("The selected bin size, " + binAmount + " exceeds the number of values for the specified bin period, " + (int) maxUnit);
        }

        final long d1t = d1.toEpochMilli();
        final long d2t = d2.toEpochMilli();

        // Put each transaction in a layer corresponding to the bin unit.
        // Build a map (transactionLayers) of layerId -> list of transactions.
        for (int position = 0; position < wgcopy.getTransactionCount(); position++) {
            final int txId = wgcopy.getTransaction(position);

            // Only use transactions that have a datetime value set.
            final long date = wgcopy.getLongValue(dtAttr, txId);
            if (d1t <= date && date < d2t) {
                dtg.setTimeInMillis(date);
                dtg.setTimeZone(TimeZone.getTimeZone("UTC"));

                final float convUnit = dtg.get(unit);
                final float layer = convUnit / maxUnit;
                if (transactionLayers.containsKey(layer)) {
                    transactionLayers.get(layer).add(txId);
                } else {
                    final List<Integer> transactionIds = new ArrayList<>();
                    transactionIds.add(txId);
                    transactionLayers.put(layer, transactionIds);
                }
                if (!values.contains(layer)) {
                    values.add(layer);
                }
            } else {
                txToDelete.set(txId);
            }
        }

        for (int txId = txToDelete.nextSetBit(0); txId >= 0; txId = txToDelete.nextSetBit(txId + 1)) {
            wgcopy.removeTransaction(txId);
        }

        Collections.sort(values);

        int j = 0;

        // Handle the number of layers in regards to binning.
        // Build a map (remappedLayers) of layer number -> list of layerIds.
        for (int i = 0; i <= maxUnit && i < values.size(); i++) {
            //Create new layer
            final List<Float> runningLayers = new ArrayList<>();
            final int currentBinAmount = j + binAmount;
            for (; j < currentBinAmount && j < values.size(); j++) {
                //Add value to layer
                runningLayers.add(values.get(j));
            }
            if (!runningLayers.isEmpty()) {
                remappedLayers.put(i, runningLayers);
            }
            if (currentBinAmount > maxUnit && maxUnit % remappedLayers.values().size() == 0) {
                break;
            }
        }

        // Now that we have the total number of layers, tell the graph so it can handle the visibility toggle.
        final int nLayersAttr = wgcopy.getAttribute(GraphElementType.GRAPH, NLAYERS);
        wgcopy.setIntValue(nLayersAttr, 0, remappedLayers.keySet().size());
        txToDelete.clear();

        // Handle layer names of each layer.
        // Build a map (displayNames) of layer number -> label.
        for (final Entry<Integer, List<Float>> entry : remappedLayers.entrySet()) {
            final StringBuilder sb = new StringBuilder();
            for (final float layer : entry.getValue()) {
                for (int txId : transactionLayers.get(layer)) {
                    final Calendar cal = new GregorianCalendar();
                    final long date = wgcopy.getLongValue(dtAttr, txId);
                    cal.setTimeInMillis(date);
                    String displayName = cal.getDisplayName(unit, Calendar.LONG_FORMAT, Locale.ENGLISH);
                    if (displayName == null) {
                        displayName = String.valueOf(cal.get(unit));
                    }
                    if (sb.indexOf(displayName) == -1) {
                        sb.append(displayName).append(", ");
                        break;
                    }
                }
            }
            displayNames.put(entry.getKey(), sb.toString().substring(0, sb.toString().lastIndexOf(", ")));
        }
    }

    /**
     * Duplicates a node onto a specific layer, recording it in
     * <code>nodeDups</code> and returning the duplicate node id
     */
    private int getDuplicateNode(final GraphWriteMethods graph, final Map<String, Integer> nodeDups, final int nodeId, final int layer) {
        final String key = String.format("%d/%d", nodeId, layer);
        if (!nodeDups.containsKey(key)) {
            // There isn't a duplicate for this node in this layer, so let's create one.
            final int dupNodeId = graph.addVertex();
            nodeDups.put(key, dupNodeId);

            // Copy node attributes.
            copyAttributes(graph, nodeId, dupNodeId, GraphElementType.VERTEX);

            final int nodeAttr = graph.getAttribute(GraphElementType.VERTEX, ORIGINAL_ID_LABEL);
            graph.setIntValue(nodeAttr, dupNodeId, nodeId);
        }

        return nodeDups.get(key);
    }

    private void copyAttributes(final GraphWriteMethods graph, final int fromId, final int toId, final GraphElementType type) {
        switch (type) {
            case TRANSACTION -> {
                for (int i = 0; i < graph.getAttributeCount(GraphElementType.TRANSACTION); i++) {
                    final int attr = graph.getAttribute(GraphElementType.TRANSACTION, i);
                    final Object value = graph.getObjectValue(attr, fromId);
                    graph.setObjectValue(attr, toId, value);
                }   
            }
            case VERTEX -> {
                for (int i = 0; i < graph.getAttributeCount(GraphElementType.VERTEX); i++) {
                    final int attr = graph.getAttribute(GraphElementType.VERTEX, i);
                    final Object value = graph.getObjectValue(attr, fromId);
                    graph.setObjectValue(attr, toId, value);
                }   
            }
            default -> {
                // Do nothing 
            }
        }
    }

    /**
     * Procedure for reordering the graph using nodes as layers on the z-axis.
     */
    private void nodesAsLayers(final GraphWriteMethods graph, final int txId, final int layer) {
        final int xAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
        final int yAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0, null);
        final int zAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0, null);
        final int x2Attr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        final int y2Attr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        final int z2Attr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);
        final int sNodeId = graph.getTransactionSourceVertex(txId);
        graph.setFloatValue(x2Attr, sNodeId, graph.getFloatValue(xAttr, sNodeId));
        graph.setFloatValue(y2Attr, sNodeId, graph.getFloatValue(yAttr, sNodeId));
        graph.setFloatValue(z2Attr, sNodeId, graph.getFloatValue(zAttr, sNodeId));

        final int dNodeId = graph.getTransactionDestinationVertex(txId);
        graph.setFloatValue(x2Attr, dNodeId, graph.getFloatValue(xAttr, dNodeId));
        graph.setFloatValue(y2Attr, dNodeId, graph.getFloatValue(yAttr, dNodeId));
        graph.setFloatValue(z2Attr, dNodeId, graph.getFloatValue(zAttr, dNodeId));

        // The nodes on the ends of this transaction will be in this layer.
        if (!nodeIdToLayers.containsKey(sNodeId)) {
            nodeIdToLayers.put(sNodeId, new BitSet());
        }
        nodeIdToLayers.get(sNodeId).set(layer);
        if (!nodeIdToLayers.containsKey(dNodeId)) {
            nodeIdToLayers.put(dNodeId, new BitSet());
        }
        nodeIdToLayers.get(dNodeId).set(layer);

        // If this transaction belongs in a different layer, move it there by creating two duplicate nodes
        // in this layer and duplicating the transaction.
        if (layer > 0) {
            // To move this transaction to the correct layer, we need two new nodes.
            // Do they already exist?
            // Create (or fetch the already created) two duplicate nodes
            // move the duplicates up the z axis by layer
            final int dupSNodeId = getDuplicateNode(graph, nodeDups, sNodeId, layer);
            graph.setFloatValue(zAttr, dupSNodeId, layer * 20);
            final int dupDNodeId = getDuplicateNode(graph, nodeDups, dNodeId, layer);
            graph.setFloatValue(zAttr, dupDNodeId, layer * 20);

            final int edgeId = graph.getTransactionEdge(txId);
            final boolean isDirected = graph.getEdgeDirection(edgeId) != Graph.FLAT;
            final int dupTxId = graph.addTransaction(dupSNodeId, dupDNodeId, isDirected);

            // For each new node, remember which layer it was duplicated to.
            nodeIdToLayers.get(sNodeId).set(layer);
            nodeIdToLayers.get(dNodeId).set(layer);

            // We've "moved" this transaction by adding a duplicate of it to the correct layer.
            // Remember that we want to delete this transaction later.
            txToDelete.set(txId);
            // Copy transactionAttributes.
            copyAttributes(graph, txId, dupTxId, GraphElementType.TRANSACTION);
        } else {
            // In case we started with a non-flat layout, explicitly set z for the layer 0 nodes.
            graph.setFloatValue(zAttr, sNodeId, 0);
            graph.setFloatValue(zAttr, dNodeId, 0);
        }
    }

    /**
     * Procedure for reordering the graph using transactions as layers on the
     * z-axis.
     *
     *
     *
     */
    private void transactionsAsLayers(final GraphWriteMethods graph, final int txId, final int z, final float step) {
        final int xAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0, null);
        final int yAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0, null);
        final int zAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0, null);
        final int x2Attr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2", "x2", 0, null);
        final int y2Attr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2", "y2", 0, null);
        final int z2Attr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2", "z2", 0, null);
        final int nodeAttr = graph.getAttribute(GraphElementType.VERTEX, ORIGINAL_ID_LABEL);

        final int srcVxId = graph.getTransactionSourceVertex(txId);
        graph.setFloatValue(x2Attr, srcVxId, graph.getFloatValue(xAttr, srcVxId));
        graph.setFloatValue(y2Attr, srcVxId, graph.getFloatValue(yAttr, srcVxId));
        graph.setFloatValue(z2Attr, srcVxId, graph.getFloatValue(zAttr, srcVxId));

        final int dstVxId = graph.getTransactionDestinationVertex(txId);
        graph.setFloatValue(x2Attr, dstVxId, graph.getFloatValue(xAttr, dstVxId));
        graph.setFloatValue(y2Attr, dstVxId, graph.getFloatValue(yAttr, dstVxId));
        graph.setFloatValue(z2Attr, dstVxId, graph.getFloatValue(zAttr, dstVxId));

        final int tlSrcVxId;
        if (srcVxMap.containsKey(srcVxId)) {
            tlSrcVxId = srcVxMap.get(srcVxId);
        } else {
            tlSrcVxId = graph.addVertex();
            srcVxMap.put(srcVxId, tlSrcVxId);
            copyAttributes(graph, srcVxId, tlSrcVxId, GraphElementType.VERTEX);
            graph.setFloatValue(zAttr, tlSrcVxId, z);
            graph.setIntValue(nodeAttr, tlSrcVxId, srcVxId);
        }

        final int tlDstVxId;
        if (dstVxMap.containsKey(dstVxId)) {
            tlDstVxId = dstVxMap.get(dstVxId);
        } else {
            tlDstVxId = graph.addVertex();
            dstVxMap.put(dstVxId, tlDstVxId);
            copyAttributes(graph, dstVxId, tlDstVxId, GraphElementType.VERTEX);
            graph.setFloatValue(zAttr, tlDstVxId, z + step);
            graph.setIntValue(nodeAttr, tlDstVxId, dstVxId);
        }

        final int tlTxId = graph.addTransaction(tlSrcVxId, tlDstVxId, graph.getTransactionDirection(txId) != Graph.FLAT);
        copyAttributes(graph, txId, tlTxId, GraphElementType.TRANSACTION);
        graph.removeTransaction(txId);
    }

    /**
     * Determine the width of the graph.
     *
     * @param graph
     *
     * @return
     */
    private static float getWidth(final GraphReadMethods graph) {
        if (graph.getVertexCount() == 0) {
            return 0;
        }
        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxx = -Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;
        final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        for (int position = 0; position < graph.getVertexCount(); position++) {
            final int vxId = graph.getVertex(position);

            final float x = graph.getFloatValue(xAttr, vxId);
            final float y = graph.getFloatValue(yAttr, vxId);

            if (x < minx) {
                minx = x;
            }
            if (x > maxx) {
                maxx = x;
            }
            if (y < miny) {
                miny = y;
            }
            if (y > maxy) {
                maxy = y;
            }
        }
        return Math.max(maxx - minx, maxy - miny);
    }
}
