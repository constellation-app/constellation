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
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
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
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.collections.api.iterator.FloatIterator;
import org.eclipse.collections.api.list.primitive.MutableFloatList;
import org.eclipse.collections.api.map.primitive.MutableFloatObjectMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.tuple.primitive.FloatObjectPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.FloatObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Use a datetime attribute on transactions to split the nodes and transactions into layers on the z-axis.
 * <p>
 * Note that we create a new graph with Visual Schema and operate on that. We do this because the resulting graph has
 * duplicate node names which would otherwise be merged on an analytic schema. These nodes are duplicated and lots of
 * transactions are created for visual purposes.
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

    public static final String ARRANGE_2D_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "arrange_in_2d");
    private static final String ARRANGE_2D_NAME = "Arrange result in 2D";
    private static final String ARRANGE_2D_DESCRIPTION = "Arrange the results of layer by time into an organised 2D view";
    private static final boolean ARRANGE_2D_DEFAULT = false;

    public static final String PER_LAYER_DIRECTION_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "per_layer_direction");
    private static final String PER_LAYER_DIRECTION_NAME = "Direction to arrange new rows/columns in: ";

    public static final String NUM_ROWS_OR_COLS_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "num_rows_or_cols");
    private static final String NUM_ROWS_OR_COLS_NAME = "Number of: ";
    private static final int NUM_ROWS_OR_COLS_DEFAULT = 1;

    public static final String ROW_OR_COL_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "row_or_col");
    private static final String ROW_OR_COL_NAME = "";

    public static final String IN_LAYER_DIRECTION_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "in_layer_direction");
    private static final String IN_LAYER_DIRECTION_NAME = "Direction to arrange nodes in: ";

    public static final String LAYER_DIST_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "layer_dist");
    private static final String LAYER_DIST_NAME = "Distance bewteen layers: ";
    private static final int LAYER_DIST_DEFAULT = 10;

    public static final String ROW_OR_COL_DIST_PARAMETER_ID = PluginParameter.buildId(LayerByTimePlugin.class, "row_col_dist");
    private static final String ROW_OR_COL_DIST_NAME = "Distance between rows/columns: ";
    private static final int ROW_OR_COL_DIST_DEFAULT = 10;

    private static final String ORIGINAL_ID_LABEL = "layer_original_id";
    private static final String LAYER_NAME = "layer";
    private static final String NLAYERS = "layers";

    private static final String ROWS = "Rows";
    private static final String COLS = "Columns";

    private static final String X = "X";
    private static final String Y = "Y";

    private static final String X_RIGHT = "X (Right)";
    private static final String X_LEFT = "-X (Left)";
    private static final String Y_UP = "Y (Up)";
    private static final String Y_DOWN = "-Y (Down)";

    private static final Map<String, Integer> LAYER_INTERVALS = new HashMap<>();
    private static final Map<String, Integer> BIN_CALENDAR_UNITS = new HashMap<>();

    private static final HashMap<String, Vector3f> directionsMap = new HashMap<>();

    private static final Vector3f VEC_UP = new Vector3f(0, 1, 0);
    private static final Vector3f VEC_RIGHT = new Vector3f(1, 0, 0);

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

        // Populate directions hash map
        directionsMap.put(X_RIGHT, new Vector3f(1, 0, 0));
        directionsMap.put(X_LEFT, new Vector3f(-1, 0, 0));
        directionsMap.put(Y_UP, new Vector3f(0, 1, 0));
        directionsMap.put(Y_DOWN, new Vector3f(0, -1, 0));
    }

    // Quick and dirty way of mapping existing nodeid + layer number to new nodeid.
    private final MutableObjectIntMap<String> nodeDups = new ObjectIntHashMap<>();
    private final MutableFloatObjectMap<MutableIntList> transactionLayers = new FloatObjectHashMap<>();
    // Map nodeId to a list of layer numbers.
    private final MutableIntObjectMap<BitSet> nodeIdToLayers = new IntObjectHashMap<>();
    private MutableIntIntMap srcVxMap = new IntIntHashMap();
    private MutableIntIntMap dstVxMap = new IntIntHashMap();
    private final BitSet txToDelete = new BitSet();

    private PluginParameter<SingleChoiceParameterValue> dtAttrParam;
    private PluginParameter<DateTimeRangeParameterValue> dateRangeParam;

    private PluginParameter<BooleanParameterValue> arrange2DParam;
    private PluginParameter<SingleChoiceParameterValue> perLayerDirectionParameter;
    private PluginParameter<IntegerParameterValue> numRowsOrColsParam;
    private PluginParameter<SingleChoiceParameterValue> rowOrColParam;
    private PluginParameter<SingleChoiceParameterValue> inLayerDirectionParameter;
    private PluginParameter<IntegerParameterValue> distanceBetweenLayers;
    private PluginParameter<IntegerParameterValue> distanceBetweenRowsOrCols;

    @Override
    public PluginParameters createParameters() {
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

        arrange2DParam = BooleanParameterType.build(ARRANGE_2D_PARAMETER_ID);
        arrange2DParam.setName(ARRANGE_2D_NAME);
        arrange2DParam.setDescription(ARRANGE_2D_DESCRIPTION);
        arrange2DParam.setBooleanValue(ARRANGE_2D_DEFAULT);
        parameters.addParameter(arrange2DParam);
        arrange2DParam.addListener((oldValue, newValue) -> disableArrange2dOptions());

        numRowsOrColsParam = IntegerParameterType.build(NUM_ROWS_OR_COLS_PARAMETER_ID);
        numRowsOrColsParam.setName(NUM_ROWS_OR_COLS_NAME);
        numRowsOrColsParam.setIntegerValue(NUM_ROWS_OR_COLS_DEFAULT);
        numRowsOrColsParam.setVisible(false);
        parameters.addParameter(numRowsOrColsParam);

        rowOrColParam = SingleChoiceParameterType.build(ROW_OR_COL_PARAMETER_ID);
        rowOrColParam.setName(ROW_OR_COL_NAME);
        SingleChoiceParameterType.setOptions(rowOrColParam, new ArrayList<>(Arrays.asList(ROWS, COLS)));
        SingleChoiceParameterType.setChoice(rowOrColParam, COLS);
        rowOrColParam.setVisible(false);
        parameters.addParameter(rowOrColParam);
        rowOrColParam.addListener((oldValue, newValue) -> updatePerLayerDirectionChoices());

        perLayerDirectionParameter = SingleChoiceParameterType.build(PER_LAYER_DIRECTION_PARAMETER_ID);
        perLayerDirectionParameter.setName(PER_LAYER_DIRECTION_NAME);
        final List<String> directionList = new ArrayList<>(directionsMap.keySet());
        directionList.removeIf(s -> !s.contains(Y));
        SingleChoiceParameterType.setOptions(perLayerDirectionParameter, directionList);
        SingleChoiceParameterType.setChoice(perLayerDirectionParameter, Y_DOWN);
        perLayerDirectionParameter.setVisible(false);
        parameters.addParameter(perLayerDirectionParameter);

        inLayerDirectionParameter = SingleChoiceParameterType.build(IN_LAYER_DIRECTION_PARAMETER_ID);
        inLayerDirectionParameter.setName(IN_LAYER_DIRECTION_NAME);
        SingleChoiceParameterType.setOptions(inLayerDirectionParameter, new ArrayList<>(directionsMap.keySet()));
        SingleChoiceParameterType.setChoice(inLayerDirectionParameter, X_RIGHT);
        inLayerDirectionParameter.setVisible(false);
        parameters.addParameter(inLayerDirectionParameter);

        distanceBetweenLayers = IntegerParameterType.build(LAYER_DIST_PARAMETER_ID);
        distanceBetweenLayers.setName(LAYER_DIST_NAME);
        distanceBetweenLayers.setIntegerValue(LAYER_DIST_DEFAULT);
        distanceBetweenLayers.setVisible(false);
        parameters.addParameter(distanceBetweenLayers);

        distanceBetweenRowsOrCols = IntegerParameterType.build(ROW_OR_COL_DIST_PARAMETER_ID);
        distanceBetweenRowsOrCols.setName(ROW_OR_COL_DIST_NAME);
        distanceBetweenRowsOrCols.setIntegerValue(ROW_OR_COL_DIST_DEFAULT);
        distanceBetweenRowsOrCols.setVisible(false);
        parameters.addParameter(distanceBetweenRowsOrCols);

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

        final List<String> dateTimeAttributes = new ArrayList<>();
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final int attributeCount = rg.getAttributeCount(GraphElementType.TRANSACTION);
            for (int i = 0; i < attributeCount; i++) {
                final int attrId = rg.getAttribute(GraphElementType.TRANSACTION, i);
                final Attribute attr = new GraphAttribute(rg, attrId);
                if (attr.getAttributeType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME)) {
                    dateTimeAttributes.add(attr.getName());
                }
            }
        }

        SingleChoiceParameterType.setOptions(dtAttrParam, dateTimeAttributes);
        parameters.addController(DATETIME_ATTRIBUTE_PARAMETER_ID, (masterId, paramMap, change) -> {

            if (change != ParameterChange.VALUE) {
                return;
            }

            final String attrName = paramMap.get(DATETIME_ATTRIBUTE_PARAMETER_ID).getStringValue();
            try (final ReadableGraph rg = graph.getReadableGraph()) {
                final int attrId = rg.getAttribute(GraphElementType.TRANSACTION, attrName);
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
                    if (!nonNullDateTimeFound) {
                        final ZonedDateTime swap = min;
                        min = max;
                        max = swap;
                    }
                    dateRangeParam.setDateTimeRangeValue(new DateTimeRange(min, max));
                }
            }
        });
        if (!dateTimeAttributes.isEmpty()) {
            SingleChoiceParameterType.setChoice(dtAttrParam, dateTimeAttributes.get(0));
        }
    }

    @Override
    public void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException, InterruptedException {
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

        // Make a copy of the graph
        final Graph copy = copyGraph(rg);

        final Attribute dt = new GraphAttribute(rg, dtAttrOrigId);

        final WritableGraph wgcopy = copy.getWritableGraph("Layer by time", true);

        try {
            final int dtAttr = wgcopy.getAttribute(GraphElementType.TRANSACTION, dt.getName());

            // Parameters
            final boolean useIntervals = parameters.getParameters().get(LAYER_BY_PARAMETER_ID).getStringValue().equals(INTERVAL_METHOD);
            final ZonedDateTime[] startEnd = parameters.getParameters().get(DATE_RANGE_PARAMETER_ID).getDateTimeRangeValue().getZonedStartEnd();
            final ZonedDateTime start = startEnd[0];
            final ZonedDateTime end = startEnd[1];
            final boolean isTransactionLayers = parameters.getParameters().get(TRANSACTION_AS_LAYER_PARAMETER_ID).getBooleanValue();
            final boolean keepTxColors = parameters.getParameters().get(KEEP_TX_COLORS_PARAMETER_ID).getBooleanValue();
            final boolean drawTxGuides = parameters.getParameters().get(DRAW_TX_GUIDES_PARAMETER_ID).getBooleanValue();

            // new arrange in 2d params
            final boolean arrangeIn2d = parameters.getParameters().get(ARRANGE_2D_PARAMETER_ID).getBooleanValue();

            final Vector3f perLayerDirection = ((Vector3f) directionsMap.get(parameters.getParameters().get(PER_LAYER_DIRECTION_PARAMETER_ID).getStringValue())).copy();
            final int numRowsOrCols = parameters.getParameters().get(NUM_ROWS_OR_COLS_PARAMETER_ID).getIntegerValue();
            final int layerScale = parameters.getParameters().get(LAYER_DIST_PARAMETER_ID).getIntegerValue();
            final int columnScale = parameters.getParameters().get(ROW_OR_COL_DIST_PARAMETER_ID).getIntegerValue();

            final boolean isRow = ROWS.equals(parameters.getParameters().get(ROW_OR_COL_PARAMETER_ID).getStringValue());
            final Vector3f inLayerDirection = ((Vector3f) directionsMap.get(parameters.getParameters().get(IN_LAYER_DIRECTION_PARAMETER_ID).getStringValue())).copy();

            //Establish new attributes.
            //Create and store graph attributes.
            final LayerName defaultName = new LayerName(Graph.NOT_FOUND, "Default");
            final int timeLayerAttr = wgcopy.addAttribute(GraphElementType.TRANSACTION, LayerNameAttributeDescription.ATTRIBUTE_NAME, LAYER_NAME, "time layer", defaultName, null);
            wgcopy.addAttribute(GraphElementType.GRAPH, IntegerAttributeDescription.ATTRIBUTE_NAME, NLAYERS, "The number of layers to layer by time", 1, null);
            final int txColorAttr = wgcopy.getAttribute(GraphElementType.TRANSACTION, "color");
            final int txGuideline = wgcopy.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "layer_guideline", "This transaction is a layer guideline", false, null);
            final ConstellationColor guidelineColor = ConstellationColor.getColorValue(0.25F, 0.25F, 0.25F, 1F);
            wgcopy.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, ORIGINAL_ID_LABEL, "Original Node Id", -1, null);

            // Create lists of layers
            final List<Float> values = new ArrayList<>();
            final MutableIntObjectMap<MutableFloatList> remappedLayers = new IntObjectHashMap<>();
            final MutableIntObjectMap<String> displayNames = new IntObjectHashMap<>();
            if (useIntervals) {
                final int intervalUnit = LAYER_INTERVALS.get(parameters.getParameters().get(UNIT_PARAMETER_ID).getStringValue());
                final int intervalAmount = parameters.getParameters().get(AMOUNT_PARAMETER_ID).getIntegerValue();
                buildIntervals(wgcopy, values, remappedLayers, displayNames, dtAttr, start.toInstant(), end.toInstant(), intervalUnit, intervalAmount);
            } else {
                final int calendarUnit = BIN_CALENDAR_UNITS.get(parameters.getParameters().get(UNIT_PARAMETER_ID).getStringValue());
                final int binAmount = parameters.getParameters().get(AMOUNT_PARAMETER_ID).getIntegerValue();
                buildBins(wgcopy, values, remappedLayers, displayNames, dtAttr, start.toInstant(), end.toInstant(), calendarUnit, binAmount);
            }

            // Modify the copied graph to show our layers.
            float z = 0;
            final float step = getWidth(wgcopy) / values.size();

            final Vector3f rowColPosition = Vector3f.createZeroVector();
            final Vector3f layerPosition = Vector3f.createZeroVector();
            boolean newRowOrColStarted = false;
            final Vector3f nextRowOrColumnDirection = isRow ? VEC_UP.copy() : VEC_RIGHT.copy();

            // For 2d only right now, if the in-layer and per-layer directions are the same then we'll need to account for that so layers dont overlap
            // They're the same direction if their dot product equals their lenghts multiplied together
            final float dotProduct = Vector3f.dotProduct(inLayerDirection, perLayerDirection);
            final float productOfLengths = inLayerDirection.getLength() * perLayerDirection.getLength();

            final boolean isSameDirection = dotProduct == productOfLengths;
            final boolean isOppositeDirection = -dotProduct == productOfLengths;

            System.out.println("isSameDirection: " + isSameDirection + " isOppositeDirection: " + isOppositeDirection);

            // Scale according to the user's decided paramters
            inLayerDirection.scale(layerScale);
            perLayerDirection.scale(layerScale);
            final int maxNodesInLayer = findMaxLayerSize(wgcopy);
            nextRowOrColumnDirection.scale(columnScale * maxNodesInLayer);

            final int layersPerRowOrCol = (int) Math.floor((float) remappedLayers.keyValuesView().size() / numRowsOrCols);
            int layersPerRowOrColRemainder = remappedLayers.keyValuesView().size() % numRowsOrCols;

            int remainingLayers = layersPerRowOrColRemainder > 0 ? layersPerRowOrCol + 1 : layersPerRowOrCol;
            layersPerRowOrColRemainder -= 1;

            final Object[] transactionLayerKeyValues = transactionLayers.keyValuesView().toArray();

            // Sorting of array only matters for arrangeIn2d option. Has no effect on regular function but sorting is so fast it doesn't matter
            Arrays.sort(transactionLayerKeyValues);

            // Each layer
            for (int i = 0; i < transactionLayerKeyValues.length; i++) {
                final FloatObjectPair<MutableIntList> currentLayer = (FloatObjectPair) transactionLayerKeyValues[i];
                final IntObjectPair<MutableFloatList> remappedLayersKeyValue = findMatchingKeyValue(remappedLayers, currentLayer.getOne());

                if (remappedLayersKeyValue == null) {
                    continue;
                }

                final int nextLayerSize = (i + 1 < transactionLayerKeyValues.length) ? findLayerSize(wgcopy, (FloatObjectPair) transactionLayerKeyValues[i + 1]) : 0;

                final MutableIntSet nodesInLayer = new IntHashSet();
                for (int j = 0; j < currentLayer.getTwo().size(); j++) {
                    final int newLayer = remappedLayersKeyValue.getOne();
                    final int txId = currentLayer.getTwo().get(j);
                    final LayerName dn = new LayerName(newLayer, displayNames.get(newLayer));
                    wgcopy.setObjectValue(timeLayerAttr, txId, dn);

                    final float normLayer = newLayer / (remappedLayers.keySet().size() * 1F);

                    if (!keepTxColors) {
                        final Color heatmap = new Color(Color.HSBtoRGB((1 - normLayer) * 2F / 3F, 0.5F, 1));
                        wgcopy.setObjectValue(txColorAttr, txId, ConstellationColor.fromJavaColor(heatmap));
                    }

                    // makes these return a list of nodes that they affected
                    if (isTransactionLayers) {
                        transactionsAsLayers(wgcopy, txId, z, step, nodesInLayer);
                    } else {
                        nodesAsLayers(wgcopy, txId, newLayer, nodesInLayer);
                    }
                }

                if (arrangeIn2d) {
                    // For now, arrange the nodes just relative to the current step
                    final int xAttr = wgcopy.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
                    final int yAttr = wgcopy.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
                    final int zAttr = wgcopy.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Z.getName());

                    // Calc the position the next node group should be
                    final Vector3f nodePosition = layerPosition.copy();

                    final MutableIntIterator iterator = nodesInLayer.intIterator();
                    while (iterator.hasNext()) {
                        final int nodeId = iterator.next();

                        // Set position
                        wgcopy.setFloatValue(xAttr, nodeId, nodePosition.getX());
                        wgcopy.setFloatValue(yAttr, nodeId, nodePosition.getY());
                        wgcopy.setFloatValue(zAttr, nodeId, nodePosition.getZ());

                        nodePosition.add(inLayerDirection);

                    }

                    if (isSameDirection || isOppositeDirection) {
                        final Vector3f toAdd = perLayerDirection.copy();
                        final int scaleAmount = isSameDirection ? nodesInLayer.size() : nextLayerSize; // If isSameDirection is FALSE, isOppositeDirection must be TRUE
                        if (scaleAmount - 1 > 1) {
                            toAdd.scale(scaleAmount - 1);
                        }
                        layerPosition.add(toAdd);
                    }

                    remainingLayers -= 1;

                    // Start new row or column
                    newRowOrColStarted = false;
                    if (remainingLayers <= 0) {
                        newRowOrColStarted = true;
                        remainingLayers = layersPerRowOrColRemainder > 0 ? layersPerRowOrCol + 1 : layersPerRowOrCol;

                        rowColPosition.add(nextRowOrColumnDirection);
                        layerPosition.set(rowColPosition);

                        layersPerRowOrColRemainder -= 1;
                    }
                }

                if (isTransactionLayers) {
                    srcVxMap = dstVxMap;
                    dstVxMap = new IntIntHashMap();
                    z += step;
                }
                if (!newRowOrColStarted) {
                    layerPosition.add(perLayerDirection);
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
            final MutableIntList vertices = new IntArrayList();
            for (int position = 0; position < wgcopy.getVertexCount(); position++) {
                final int vertexId = wgcopy.getVertex(position);
                final int nTx = wgcopy.getVertexTransactionCount(vertexId);
                if (nTx == 0) {
                    vertices.add(vertexId);
                }
            }

            vertices.forEach(wgcopy::removeVertex);

            if (drawTxGuides) {
                interaction.setProgress(5, 6, "Draw guide lines", false);

                // Draw a grey vertical indicator line connecting the same nodes in each layer.
                // We have to do this after the "remove node without transactions" step because we're adding more transactions.
                if (!isTransactionLayers && remappedLayers.keySet().size() > 1) {
                    nodeIdToLayers.forEachKey(origNodeId -> {
                        int prevNodeId = -1;
                        final BitSet layers = nodeIdToLayers.get(origNodeId);
                        for (int layer = layers.nextSetBit(0); layer >= 0; layer = layers.nextSetBit(layer + 1)) {
                            final int nodeId = layer == 0 ? origNodeId : nodeDups.get(String.format("%s/%s", origNodeId, layer));
                            if (prevNodeId != -1) {
                                final int sTxId = wgcopy.addTransaction(prevNodeId, nodeId, false);
                                wgcopy.setBooleanValue(txGuideline, sTxId, true);
                                wgcopy.setObjectValue(txColorAttr, sTxId, guidelineColor);
                                final LayerName dn = new LayerName(1107, "Guideline"); // MAGIC NUMBER
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

    protected Graph copyGraph(final GraphReadMethods rg) throws InterruptedException {
        try {
            final Plugin copyGraphPlugin = PluginRegistry.get(InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
            final PluginParameters copyParams = copyGraphPlugin.createParameters();
            copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_SCHEMA_NAME_PARAMETER_ID).setStringValue(rg.getSchema().getFactory().getName());
            copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_ALL_PARAMETER_ID).setBooleanValue(true);
            copyParams.getParameters().get(CopyToNewGraphPlugin.COPY_KEYS_PARAMETER_ID).setBooleanValue(false);
            PluginExecution.withPlugin(copyGraphPlugin).withParameters(copyParams).executeNow(rg);
            return (Graph) copyParams.getParameters().get(CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID).getObjectValue();
        } catch (final PluginException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            return null;
        }
    }

    private void updatePerLayerDirectionChoices() {
        // update per layer direction based on if rows or columns is chosen
        final boolean isRow = ROWS.equals(rowOrColParam.getStringValue());

        final String substring = isRow ? X : Y;
        final List<String> directionList = new ArrayList<>(directionsMap.keySet());
        directionList.removeIf(s -> !s.contains(substring));
        SingleChoiceParameterType.setOptions(perLayerDirectionParameter, directionList);

        final String choice = isRow ? X_RIGHT : Y_DOWN;
        SingleChoiceParameterType.setChoice(perLayerDirectionParameter, choice);
    }

    private void disableArrange2dOptions() {
        final boolean enable = arrange2DParam.getBooleanValue();

        perLayerDirectionParameter.setVisible(enable);
        numRowsOrColsParam.setVisible(enable);
        rowOrColParam.setVisible(enable);
        inLayerDirectionParameter.setVisible(enable);
        distanceBetweenLayers.setVisible(enable);
        distanceBetweenRowsOrCols.setVisible(enable);
    }

    private int findMaxLayerSize(final GraphWriteMethods graph) {
        int max = 0;

        for (final FloatObjectPair<MutableIntList> currentLayer : transactionLayers.keyValuesView()) {
            final int layerSize = findLayerSize(graph, currentLayer);
            if (layerSize > max) {
                max = layerSize;
            }
        }
        return max;
    }

    private int findLayerSize(final GraphWriteMethods graph, final FloatObjectPair<MutableIntList> currentLayer) {

        final MutableIntSet nodesInLayer = new IntHashSet();
        for (int i = 0; i < currentLayer.getTwo().size(); i++) {
            final int txId = currentLayer.getTwo().get(i);
            final int srcVxId = graph.getTransactionSourceVertex(txId);
            final int dstVxId = graph.getTransactionDestinationVertex(txId);
            nodesInLayer.add(srcVxId);
            nodesInLayer.add(dstVxId);
        }

        return nodesInLayer.size();
    }

    private IntObjectPair<MutableFloatList> findMatchingKeyValue(final MutableIntObjectMap<MutableFloatList> remappedLayers, final float key) {
        for (final IntObjectPair<MutableFloatList> keyValue : remappedLayers.keyValuesView()) {
            if (keyValue.getTwo().contains(key)) {
                return keyValue;
            }
        }

        return null;
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
    private void buildIntervals(final GraphWriteMethods wgcopy, final List<Float> values, final MutableIntObjectMap<MutableFloatList> remappedLayers, final MutableIntObjectMap<String> displayNames, final int dtAttr, final Instant d1, final Instant d2, final int unit, final int amount) {
        // Convert to milliseconds.
        final long intervalLength = unit * amount * 1000L;
        final long d1t = d1.toEpochMilli();
        final long d2t = d2.toEpochMilli();

        final BitSet txUnused = new BitSet();
        for (int position = 0; position < wgcopy.getTransactionCount(); position++) {
            final int txId = wgcopy.getTransaction(position);

            // Only use transactions that have a datetime value set.
            final long date = wgcopy.getLongValue(dtAttr, txId);

            if (d1t <= date && date <= d2t) {

                final float layer = (float) (date - d1t) / intervalLength;
                if (!transactionLayers.containsKey(layer)) {
                    transactionLayers.put(layer, new IntArrayList());
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
            final MutableFloatList runningLayers = new FloatArrayList();
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
    private void buildBins(final GraphWriteMethods wgcopy, final List<Float> values, final MutableIntObjectMap<MutableFloatList> remappedLayers, final MutableIntObjectMap<String> displayNames, final int dtAttr, final Instant d1, final Instant d2, final int unit, final int binAmount) {
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
            if (d1t <= date && date <= d2t) {
                dtg.setTimeInMillis(date);
                dtg.setTimeZone(TimeZone.getTimeZone("UTC"));

                final float convUnit = dtg.get(unit);
                final float layer = convUnit / maxUnit;
                if (transactionLayers.containsKey(layer)) {
                    transactionLayers.get(layer).add(txId);
                } else {
                    final MutableIntList transactionIds = new IntArrayList();
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
            final MutableFloatList runningLayers = new FloatArrayList();
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
        for (final IntObjectPair<MutableFloatList> keyValue : remappedLayers.keyValuesView()) {
            final StringBuilder sb = new StringBuilder();
            final FloatIterator iter = keyValue.getTwo().floatIterator();
            while (iter.hasNext()) {
                final float layer = iter.next();
                for (int i = 0; i < transactionLayers.get(layer).size(); i++) {
                    final int txId = transactionLayers.get(layer).get(i);
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
            displayNames.put(keyValue.getOne(), sb.toString().substring(0, sb.toString().lastIndexOf(", ")));
        }
    }

    /**
     * Duplicates a node onto a specific layer, recording it in <code>nodeDups</code> and returning the duplicate node
     * id
     */
    private int getDuplicateNode(final GraphWriteMethods graph, final MutableObjectIntMap<String> nodeDups, final int nodeId, final int layer) {
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
    private void nodesAsLayers(final GraphWriteMethods graph, final int txId, final int layer, final MutableIntSet nodesInLayer) {
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

            //return new NodePair(dupSNodeId, dupDNodeId); // no clue if this is right
            nodesInLayer.add(dupSNodeId);
            nodesInLayer.add(dupDNodeId);
        } else {
            // In case we started with a non-flat layout, explicitly set z for the layer 0 nodes.
            graph.setFloatValue(zAttr, sNodeId, 0);
            graph.setFloatValue(zAttr, dNodeId, 0);

            // return new NodePair(sNodeId, dNodeId); // no clue if this is right
            nodesInLayer.add(sNodeId);
            nodesInLayer.add(dNodeId);
        }

    }

    /**
     * Procedure for reordering the graph using transactions as layers on the z-axis.
     *
     *
     *
     */
    private void transactionsAsLayers(final GraphWriteMethods graph, final int txId, final float z, final float step, final MutableIntSet nodesInLayer) {
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

        nodesInLayer.add(tlSrcVxId);
        nodesInLayer.add(tlDstVxId);
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
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        for (int position = 0; position < graph.getVertexCount(); position++) {
            final int vxId = graph.getVertex(position);

            final float x = graph.getFloatValue(xAttr, vxId);
            final float y = graph.getFloatValue(yAttr, vxId);

            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        return Math.max(maxX - minX, maxY - minY);
    }
}
