/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.graph.visual.graphics.BBoxf;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.utilities.geospatial.Country;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that builds a random sphere graph.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("SphereGraphBuilderPlugin=Sphere Graph Builder")
public class SphereGraphBuilderPlugin extends SimpleEditPlugin {

    public static final String N_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "n");
    public static final String T_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "t");
    public static final String OPTION_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "option");
    public static final String ADD_CHARS_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "add_chars");
    public static final String USE_LABELS_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "use_labels");
    public static final String USE_RANDOM_ICONS_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "use_random_icons");
    public static final String USE_ALL_DISPLAYABLE_CHARS_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "use_all_displayable_chars");
    public static final String DRAW_MANY_TX_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "draw_many_tx");
    public static final String DRAW_MANY_DECORATORS_PARAMETER_ID = PluginParameter.buildId(SphereGraphBuilderPlugin.class, "draw_many_deco");

    @Override
    public String getDescription() {
        return "Builds a random sphere graph.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> n = IntegerParameterType.build(N_PARAMETER_ID);
        n.setName("Number of nodes");
        n.setDescription("The number of nodes on the graph");
        n.setIntegerValue(10);
        IntegerParameterType.setMinimum(n, 0);
        params.addParameter(n);

        final PluginParameter<IntegerParameterValue> t = IntegerParameterType.build(T_PARAMETER_ID);
        t.setName("Number of transactions");
        t.setDescription("The number of transactions on the graph");
        t.setIntegerValue(10);
        IntegerParameterType.setMinimum(t, 0);
        params.addParameter(t);

        ArrayList<String> modes = new ArrayList<>();
        modes.add("Random vertices");
        modes.add("1 path, next neighbour");
        modes.add("1 path, random vertices");

        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> option = SingleChoiceParameterType.build(OPTION_PARAMETER_ID);
        option.setName("Transaction options");
        option.setDescription("How to add transactions to the graph");
        SingleChoiceParameterType.setOptions(option, modes);
        SingleChoiceParameterType.setChoice(option, modes.get(0));
        params.addParameter(option);

        final PluginParameter<BooleanParameterValue> randomChars = BooleanParameterType.build(ADD_CHARS_PARAMETER_ID);
        randomChars.setName("Add random chars to vertex name");
        randomChars.setDescription("Add random chars to vertex name");
        randomChars.setBooleanValue(true);
        params.addParameter(randomChars);

        final PluginParameter<BooleanParameterValue> addLabels = BooleanParameterType.build(USE_LABELS_PARAMETER_ID);
        addLabels.setName("Add labels");
        addLabels.setDescription("Labels nodes and transactions");
        addLabels.setBooleanValue(true);
        params.addParameter(addLabels);

        final PluginParameter<BooleanParameterValue> allChars = BooleanParameterType.build(USE_ALL_DISPLAYABLE_CHARS_PARAMETER_ID);
        allChars.setName("All displayable chars");
        allChars.setDescription("Use all displayable chars in labels");
        allChars.setBooleanValue(false);
        params.addParameter(allChars);

        final PluginParameter<BooleanParameterValue> drawManyTx = BooleanParameterType.build(DRAW_MANY_TX_PARAMETER_ID);
        drawManyTx.setName("Draw many transactions");
        drawManyTx.setDescription("Draw lots of transactions between nodes");
        drawManyTx.setBooleanValue(false);
        params.addParameter(drawManyTx);

        final PluginParameter<BooleanParameterValue> drawManyDeco = BooleanParameterType.build(DRAW_MANY_DECORATORS_PARAMETER_ID);
        drawManyDeco.setName("Draw many decorators");
        drawManyDeco.setDescription("Draw lots of decorators on nodes");
        drawManyDeco.setBooleanValue(false);
        params.addParameter(drawManyDeco);

        final PluginParameter<BooleanParameterValue> randomIcons = BooleanParameterType.build(USE_RANDOM_ICONS_PARAMETER_ID);
        randomIcons.setName("Random icons and decorators");
        randomIcons.setDescription("Use random icons and decorators on nodes");
        randomIcons.setBooleanValue(true);
        params.addParameter(randomIcons);

        return params;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        // the default icon
        final String DEFAULT_ICON = "HAL-9000";
        // the first of a sequence of random characters.
        final int PLAY_CHARS = 0x261A;
        // the number of random characters.
        final int PLAY_CHARS_LEN = 86;

        // other variables
        final Random random = new Random();
        final IconRandomiser iconRandomiser = new IconRandomiser();
        final int vertexCount = graph.getVertexCount();

        // parameter values
        final Map<String, PluginParameter<?>> params = parameters.getParameters();
        final int nVx = params.get(N_PARAMETER_ID).getIntegerValue();
        final int nTx = params.get(T_PARAMETER_ID).getIntegerValue();
        final String option = params.get(OPTION_PARAMETER_ID).getStringValue();
        final boolean addChars = params.get(ADD_CHARS_PARAMETER_ID).getBooleanValue();
        final boolean useLabels = params.get(USE_LABELS_PARAMETER_ID).getBooleanValue();
        final boolean allDisplayableChars = params.get(USE_ALL_DISPLAYABLE_CHARS_PARAMETER_ID).getBooleanValue();
        final boolean drawManyTx = params.get(DRAW_MANY_TX_PARAMETER_ID).getBooleanValue();
        final boolean drawManyDecorators = params.get(DRAW_MANY_DECORATORS_PARAMETER_ID).getBooleanValue();
        final boolean useRandomIcons = params.get(USE_RANDOM_ICONS_PARAMETER_ID).getBooleanValue();

        // select some icons to put in the graph
        final List<String> iconLabels = new ArrayList<>();
        if (useRandomIcons) {
            IconManager.getIconNames(null).forEach((label) -> {
                iconLabels.add(label);
            });
        }

        // select some countries to put in the graph
        final List<String> countries = new ArrayList<>();
        for (final Country country : Country.values()) {
            countries.add(country.getDisplayName());
        }

        final int maxTxId = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.ensure(graph);
        if (drawManyTx) {
            graph.setIntValue(maxTxId, 0, 100);
        }

        final int vxLabelAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vxIdentifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vxTypeAttr = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vxRadiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vxDimmedAttr = VisualConcept.VertexAttribute.DIMMED.ensure(graph);
        final int vxVisibilityAttr = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
        final int vxColorAttr = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vxForegroundIconAttr = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);
        final int vxBackgroundIconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int attrBlaze = VisualConcept.VertexAttribute.BLAZE.ensure(graph);
        final int vxXAttr = VisualConcept.VertexAttribute.X.ensure(graph);
        final int vxYAttr = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int vxZAttr = VisualConcept.VertexAttribute.Z.ensure(graph);
        final int vxX2Attr = VisualConcept.VertexAttribute.X2.ensure(graph);
        final int vxY2Attr = VisualConcept.VertexAttribute.Y2.ensure(graph);
        final int vxZ2Attr = VisualConcept.VertexAttribute.Z2.ensure(graph);

        final int vxIsGoodAttr = graph.addAttribute(GraphElementType.VERTEX, "boolean", "isGood", null, false, null);
        final int vxCountry1Attr = SpatialConcept.VertexAttribute.COUNTRY.ensure(graph);
        final int vxCountry2Attr = graph.addAttribute(GraphElementType.VERTEX, "string", "Geo.Country2", null, null, null);
        final int vxDecoratorAttr = graph.addAttribute(GraphElementType.VERTEX, "string", "Custom Decorator", null, null, null);
        final int vxNormalisedAttr = graph.addAttribute(GraphElementType.VERTEX, "float", "Normalised", null, 0.0f, null);

        final int txIdAttr = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int txDirectedAttr = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);
        final int txWidthAttr = VisualConcept.TransactionAttribute.WIDTH.ensure(graph);
        final int txDimmedAttr = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);
        final int txVisibilityAttr = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);
        final int txColorAttr = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        final int txLineStyleAttr = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(graph);
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        // Add various labels and decorators.
        final List<GraphLabel> bottomLabels = new ArrayList<>();
        final List<GraphLabel> topLabels = new ArrayList<>();
        final List<GraphLabel> transactionLabels = new ArrayList<>();
        if (useLabels) {
            bottomLabels.add(new GraphLabel(VisualConcept.VertexAttribute.IDENTIFIER.getName(), ConstellationColor.LIGHT_BLUE));
            bottomLabels.add(new GraphLabel(VisualConcept.VertexAttribute.FOREGROUND_ICON.getName(), ConstellationColor.DARK_GREEN, 0.5f));
            topLabels.add(new GraphLabel(AnalyticConcept.VertexAttribute.TYPE.getName(), ConstellationColor.MAGENTA, 0.5f));
            topLabels.add(new GraphLabel(SpatialConcept.VertexAttribute.COUNTRY.getName(), ConstellationColor.DARK_ORANGE, 0.5f));
            transactionLabels.add(new GraphLabel(VisualConcept.TransactionAttribute.VISIBILITY.getName(), ConstellationColor.LIGHT_GREEN));
        }

        final VertexDecorators decorators;
        if (drawManyDecorators) {
            decorators = new VertexDecorators(graph.getAttributeName(vxIsGoodAttr), SpatialConcept.VertexAttribute.COUNTRY.getName(), graph.getAttributeName(vxCountry2Attr), graph.getAttributeName(vxDecoratorAttr));
        } else {
            decorators = new VertexDecorators(graph.getAttributeName(vxIsGoodAttr), null, null, null);
        }

        final int bottomLabelsAttr = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
        final int topLabelsAttr = VisualConcept.GraphAttribute.TOP_LABELS.ensure(graph);
        final int transactionLabelsAttr = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(graph);
        graph.setObjectValue(bottomLabelsAttr, 0, new GraphLabels(bottomLabels));
        graph.setObjectValue(topLabelsAttr, 0, new GraphLabels(topLabels));
        graph.setObjectValue(transactionLabelsAttr, 0, new GraphLabels(transactionLabels));

        final int decoratorsAttr = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);
        graph.setObjectValue(decoratorsAttr, 0, decorators);

        final ConstellationColor.PalettePhiIterator palette = new ConstellationColor.PalettePhiIterator(0, 0.75f, 0.75f);

        // Displayable characters for type.
        int c = 33;
        final Font font = FontUtilities.getOutputFont();
        final StringBuilder type = new StringBuilder();

        final int[] vxIds = new int[nVx];
        int vx = 0;
        while (vx < nVx) {
            final float v = nVx > 1 ? vx / (float) (nVx - 1) : 1f;
            final int vxId = graph.addVertex();

            // A label with a random CJK glyph.
            final String chars;
            if (addChars) {
                if (vx == 0) {
                    chars = "مرحبا عالم"; // "Hello world" in arabic, demonstrates unicode rendering
                } else {
                    chars = " " + (char) (PLAY_CHARS + random.nextInt(PLAY_CHARS_LEN)) + " " + (char) (0x4e00 + random.nextInt(256));
                }
            } else {
                chars = "";
            }
            final String label = "Node " + vxId + chars;

            if (allDisplayableChars) {
                type.setLength(0);
                while (type.length() < 10) {
                    if (font.canDisplay(c)) {
                        type.append((char) c);
                    }

                    // Unicode BMP only has so many characters.
                    c = (c + 1) % 65536;
                }
            }

            graph.setStringValue(vxLabelAttr, vxId, label);
            graph.setStringValue(vxIdentifierAttr, vxId, String.valueOf(vxId));
            graph.setStringValue(vxTypeAttr, vxId, type.toString());
            graph.setFloatValue(vxRadiusAttr, vxId, 1);
            graph.setFloatValue(vxVisibilityAttr, vxId, v);
            graph.setObjectValue(vxColorAttr, vxId, palette.next());
            if (useRandomIcons) {
                graph.setStringValue(vxForegroundIconAttr, vxId, iconLabels.get(random.nextInt(iconLabels.size())));
                graph.setStringValue(vxDecoratorAttr, vxId, iconLabels.get(random.nextInt(iconLabels.size())));
            } else {
                // If icons are non-random, make the first one null.
                graph.setStringValue(vxForegroundIconAttr, vxId, DEFAULT_ICON);
                graph.setStringValue(vxDecoratorAttr, vxId, null);
            }
            graph.setStringValue(vxBackgroundIconAttr, vxId, useRandomIcons ? (random.nextBoolean() ? "Background.Flat Square" : "Background.Flat Circle") : "Background.Flat Circle");

            if (vx == 0) {
                graph.setStringValue(vxForegroundIconAttr, vxId, null);
                graph.setObjectValue(attrBlaze, vxId, new Blaze(45, ConstellationColor.LIGHT_BLUE));
            }
            graph.setBooleanValue(vxIsGoodAttr, vxId, vx % 2 == 0);
            graph.setStringValue(vxCountry1Attr, vxId, countries.get(vx % countries.size()));
            graph.setStringValue(vxCountry2Attr, vxId, countries.get((vx + 1) % countries.size()));
            graph.setFloatValue(vxNormalisedAttr, vxId, (float) Math.random());

            vxIds[vx] = vxId;
            vx++;

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }

        // Dimmed nodes.
        if (nVx > 1) {
            graph.setBooleanValue(vxDimmedAttr, vxIds[nVx - 1], true);
            if (nVx > 2) {
                graph.setBooleanValue(vxDimmedAttr, vxIds[nVx - 2], true);
            }
        }

        if (nVx > 0) {
            // Create transactions between the nodes.
            final Date d = new Date();
            final int fourDays = 4 * 24 * 60 * 60 * 1000;
            if (option.equals("Random vertices")) {
                // Draw some lines between random nodes, but don't draw multiple lines between the same two nodes.
                for (int i = 0; i < nTx; i++) {
                    // Choose random positions, convert to correct vxIds.
                    // Concentrate most sources to just a few vertices; it looks a bit nicer than plain random.
                    int fromPosition = vertexCount + (random.nextFloat() < 0.9f ? random.nextInt((int) (Math.log10(nVx) * 5) + 1) : random.nextInt(nVx));
                    int toPosition;
                    do {
                        toPosition = vertexCount + random.nextInt(nVx);
                    } while (toPosition == fromPosition);

                    assert fromPosition != toPosition;

                    int fromVx = graph.getVertex(fromPosition);
                    int toVx = graph.getVertex(toPosition);

                    // Always draw from the lower numbered vertex to the higher numbered vertex.
                    if (toVx < fromVx) {
                        int tmp = fromVx;
                        fromVx = toVx;
                        toVx = tmp;
                    }

                    final int e = graph.addTransaction(fromVx, toVx, true);
                    graph.setLongValue(txDateTimeAttr, e, d.getTime() - random.nextInt(fourDays));
                    graph.setIntValue(txIdAttr, e, e);
                    graph.setObjectValue(txColorAttr, e, randomColor3(random));
                    graph.setFloatValue(txVisibilityAttr, e, (float) i / (nTx - 1));

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
            } else {
                if (option.equals("1 path, random vertices")) {
                    // Shuffle the vertices.
                    for (int i = nVx - 1; i > 0; i--) {
                        final int ix = random.nextInt(i);
                        final int t = vxIds[i];
                        vxIds[i] = vxIds[ix];
                        vxIds[ix] = t;
                    }
                }

                // Transactions from/to each vertex in ascending order, modulo nVx.
                for (int i = 0; i < nTx; i++) {
                    final int fromNode = vxIds[i % nVx];
                    final int toNode = vxIds[(i + 1) % nVx];
                    final int e = graph.addTransaction(fromNode, toNode, true);
                    graph.setLongValue(txDateTimeAttr, e, d.getTime() - random.nextInt(fourDays));
                    graph.setIntValue(txIdAttr, e, e);
                    graph.setObjectValue(txColorAttr, e, randomColor3(random));
                    graph.setFloatValue(txVisibilityAttr, e, (float) i / (nTx - 1));

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
            }
        }

        // Do a spherical layout.
        try {
            PluginExecution.withPlugin(ArrangementPluginRegistry.SPHERE).executeNow(graph);
        } catch (PluginException ex) {
            Exceptions.printStackTrace(ex);
        }

        // Find out where the extremes of the new sphere are and select every 10th vertex.
        final BBoxf box = new BBoxf();
        for (int position = 0; position < nVx; position++) {
            final int vxId = graph.getVertex(position);

            final float x = graph.getFloatValue(vxXAttr, vxId);
            final float y = graph.getFloatValue(vxYAttr, vxId);
            final float z = graph.getFloatValue(vxZAttr, vxId);
            box.add(x, y, z);

            graph.setBooleanValue(vxSelectedAttr, vxId, vxId % 10 == 0);
        }

        // Create nodes just outside the centres of the bounding box's sides.
        final float[] min = box.getMin();
        final float[] max = box.getMax();
        if (box.isEmpty()) {
            Arrays.fill(min, 0);
            Arrays.fill(max, 0);
        }
        char circleChar = 0x2460;
        int radius = 3;

        final int vx0 = graph.addVertex();
        ConstructionUtilities.setxyz(graph, vx0, vxXAttr, vxYAttr, vxZAttr, 0, 0, max[BBoxf.Z] + 2);
        ConstructionUtilities.setxyz(graph, vx0, vxX2Attr, vxY2Attr, vxZ2Attr, 0, 0, max[BBoxf.Z] + 2);
        graph.setStringValue(vxLabelAttr, vx0, "~Node " + vx0 + " " + circleChar++);
        graph.setStringValue(vxIdentifierAttr, vx0, String.valueOf(vx0));
        graph.setStringValue(vxTypeAttr, vx0, "~Type " + vx0);
        graph.setFloatValue(vxRadiusAttr, vx0, (nVx + radius++) / (float) (nVx + 6));
        graph.setFloatValue(vxVisibilityAttr, vx0, 1);
        graph.setObjectValue(vxColorAttr, vx0, randomColor3(random));
        graph.setStringValue(vxForegroundIconAttr, vx0, DEFAULT_ICON);
        graph.setStringValue(vxBackgroundIconAttr, vx0, "Background.Round Circle");
        graph.setStringValue(vxCountry1Attr, vx0, countries.get(vx0 % countries.size()));
        graph.setStringValue(vxCountry2Attr, vx0, countries.get((vx0 + 1) % countries.size()));
        graph.setFloatValue(vxNormalisedAttr, vx0, (float) Math.random());

        final int vx1 = graph.addVertex();
        ConstructionUtilities.setxyz(graph, vx1, vxXAttr, vxYAttr, vxZAttr, 0, 0, min[BBoxf.Z] - 2);
        ConstructionUtilities.setxyz(graph, vx1, vxX2Attr, vxY2Attr, vxZ2Attr, 0, 0, min[BBoxf.Z] - 2);
        graph.setStringValue(vxLabelAttr, vx1, "~Node " + vx1 + " " + circleChar++);
        graph.setStringValue(vxIdentifierAttr, vx1, String.valueOf(vx1));
        graph.setStringValue(vxTypeAttr, vx1, "~Type " + vx1);
        graph.setFloatValue(vxRadiusAttr, vx1, (nVx + radius++) / (float) (nVx + 6));
        graph.setFloatValue(vxVisibilityAttr, vx1, 1);
        graph.setObjectValue(vxColorAttr, vx1, randomColor3(random));
        graph.setStringValue(vxForegroundIconAttr, vx1, DEFAULT_ICON);
        graph.setStringValue(vxBackgroundIconAttr, vx1, "Background.Round Circle");
        graph.setStringValue(vxCountry1Attr, vx1, countries.get(vx1 % countries.size()));
        graph.setStringValue(vxCountry2Attr, vx1, countries.get((vx1 + 1) % countries.size()));
        graph.setFloatValue(vxNormalisedAttr, vx1, (float) Math.random());

        final int vx2 = graph.addVertex();
        ConstructionUtilities.setxyz(graph, vx2, vxXAttr, vxYAttr, vxZAttr, 0, max[BBoxf.Y] + 2, 0);
        ConstructionUtilities.setxyz(graph, vx2, vxX2Attr, vxY2Attr, vxZ2Attr, 0, max[BBoxf.Y] + 2, 0);
        graph.setStringValue(vxLabelAttr, vx2, "~Node " + vx2 + " " + circleChar++);
        graph.setStringValue(vxIdentifierAttr, vx2, String.valueOf(vx2));
        graph.setStringValue(vxTypeAttr, vx2, "~Type " + vx2);
        graph.setFloatValue(vxRadiusAttr, vx2, (nVx + radius++) / (float) (nVx + 6));
        graph.setFloatValue(vxVisibilityAttr, vx2, 1);
        graph.setObjectValue(vxColorAttr, vx2, randomColor3(random));
        graph.setStringValue(vxForegroundIconAttr, vx2, DEFAULT_ICON);
        graph.setStringValue(vxBackgroundIconAttr, vx2, "Background.Round Circle");
        graph.setStringValue(vxCountry1Attr, vx2, countries.get(vx2 % countries.size()));
        graph.setStringValue(vxCountry2Attr, vx2, countries.get((vx2 + 1) % countries.size()));
        graph.setFloatValue(vxNormalisedAttr, vx2, (float) Math.random());

        final int vx3 = graph.addVertex();
        ConstructionUtilities.setxyz(graph, vx3, vxXAttr, vxYAttr, vxZAttr, 0, min[BBoxf.Y] - 2, 0);
        ConstructionUtilities.setxyz(graph, vx3, vxX2Attr, vxY2Attr, vxZ2Attr, 0, min[BBoxf.Y] - 2, 0);
        graph.setStringValue(vxLabelAttr, vx3, "~Node " + vx3 + " " + circleChar++);
        graph.setStringValue(vxIdentifierAttr, vx3, String.valueOf(vx3));
        graph.setStringValue(vxTypeAttr, vx3, "~Type " + vx3);
        graph.setFloatValue(vxRadiusAttr, vx3, (nVx + radius++) / (float) (nVx + 6));
        graph.setFloatValue(vxVisibilityAttr, vx3, 1);
        graph.setObjectValue(vxColorAttr, vx3, randomColor3(random));
        graph.setStringValue(vxForegroundIconAttr, vx3, DEFAULT_ICON);
        graph.setStringValue(vxBackgroundIconAttr, vx2, "Background.Round Circle");
        graph.setStringValue(vxCountry1Attr, vx3, countries.get(vx3 % countries.size()));
        graph.setStringValue(vxCountry2Attr, vx3, countries.get((vx3 + 1) % countries.size()));
        graph.setFloatValue(vxNormalisedAttr, vx3, (float) Math.random());

        final int vx4 = graph.addVertex();
        ConstructionUtilities.setxyz(graph, vx4, vxXAttr, vxYAttr, vxZAttr, max[BBoxf.X] + 2, 0, 0);
        ConstructionUtilities.setxyz(graph, vx4, vxX2Attr, vxY2Attr, vxZ2Attr, max[BBoxf.X] + 2, 0, 0);
        graph.setStringValue(vxLabelAttr, vx4, "~Node " + vx4 + " " + circleChar++);
        graph.setStringValue(vxIdentifierAttr, vx4, String.valueOf(vx4));
        graph.setStringValue(vxTypeAttr, vx4, "~Type " + vx4);
        graph.setFloatValue(vxRadiusAttr, vx4, (nVx + radius++) / (float) (nVx + 6));
        graph.setFloatValue(vxVisibilityAttr, vx4, 1);
        graph.setObjectValue(vxColorAttr, vx4, randomColor3(random));
        graph.setStringValue(vxForegroundIconAttr, vx4, DEFAULT_ICON);
        graph.setStringValue(vxBackgroundIconAttr, vx2, "Background.Round Circle");
        graph.setStringValue(vxCountry1Attr, vx4, countries.get(vx4 % countries.size()));
        graph.setStringValue(vxCountry2Attr, vx4, countries.get((vx4 + 1) % countries.size()));
        graph.setFloatValue(vxNormalisedAttr, vx4, (float) Math.random());

        final int vx5 = graph.addVertex();
        ConstructionUtilities.setxyz(graph, vx5, vxXAttr, vxYAttr, vxZAttr, min[BBoxf.X] - 2, 0, 0);
        ConstructionUtilities.setxyz(graph, vx5, vxX2Attr, vxY2Attr, vxZ2Attr, min[BBoxf.X] - 2, 0, 0);
        graph.setStringValue(vxLabelAttr, vx5, "~Node " + vx5 + " " + circleChar++);
        graph.setStringValue(vxIdentifierAttr, vx5, String.valueOf(vx5));
        graph.setStringValue(vxTypeAttr, vx5, "~Type " + vx5);
        graph.setFloatValue(vxRadiusAttr, vx5, (nVx + radius++) / (float) (nVx + 6));
        graph.setFloatValue(vxVisibilityAttr, vx5, 1);
        graph.setObjectValue(vxColorAttr, vx5, randomColor3(random));
        graph.setStringValue(vxForegroundIconAttr, vx5, DEFAULT_ICON);
        graph.setStringValue(vxBackgroundIconAttr, vx2, "Background.Round Circle");
        graph.setStringValue(vxCountry1Attr, vx5, countries.get(vx5 % countries.size()));
        graph.setStringValue(vxCountry2Attr, vx5, countries.get((vx5 + 1) % countries.size()));
        graph.setFloatValue(vxNormalisedAttr, vx5, (float) Math.random());

        // Draw multiple lines with offsets between two fixed nodes.
        int txId;

        // Too many transactions to draw; different colors.
        final int lim1 = 16;
        for (int i = 0; i < lim1; i++) {
            final ConstellationColor rgb = ConstellationColor.getColorValue((float) i / lim1, 0, 1.0f - (float) i / lim1, 1f);
            if (i % 2 == 0) {
                txId = graph.addTransaction(vx0, vx1, true);
            } else {
                txId = graph.addTransaction(vx1, vx0, true);
            }
            graph.setIntValue(txIdAttr, txId, txId);
            graph.setObjectValue(txColorAttr, txId, rgb);
            graph.setFloatValue(txVisibilityAttr, txId, 1);
        }

        // Not too many transactions to draw; different colors.
        final int lim2 = 8;
        for (int i = 0; i < lim2; i++) {
            final ConstellationColor rgb = ConstellationColor.getColorValue(0.25f, 1.0f - (float) i / lim2, (float) i / lim2, 1f);
            if (i % 2 == 0) {
                txId = graph.addTransaction(vx1, vx2, true);
            } else {
                txId = graph.addTransaction(vx2, vx1, true);
            }
            graph.setIntValue(txIdAttr, txId, txId);
            graph.setObjectValue(txColorAttr, txId, rgb);
            graph.setFloatValue(txVisibilityAttr, txId, (i + 1) / (float) lim2);
            graph.setFloatValue(txWidthAttr, txId, 2f);
        }

        // Too many transactions to draw: same color.
        final int lim3 = 9;
        final ConstellationColor rgb3 = ConstellationColor.ORANGE;
        for (int i = 0; i < lim3; i++) {
            if (i == 0) {
                txId = graph.addTransaction(vx3, vx4, true);
                graph.setFloatValue(txVisibilityAttr, txId, 0.3f);
            } else if (i < 4) {
                txId = graph.addTransaction(vx3, vx4, false);
                graph.setFloatValue(txVisibilityAttr, txId, 0.6f);
            } else {
                txId = graph.addTransaction(vx4, vx3, true);
                graph.setFloatValue(txVisibilityAttr, txId, 0.9f);
            }
            graph.setIntValue(txIdAttr, txId, txId);
            graph.setObjectValue(txColorAttr, txId, rgb3);
        }

        // Undirected transactions.
        txId = graph.addTransaction(vx4, vx5, false);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.getColorValue(1f, 0f, 1f, 1f));
        graph.setBooleanValue(txDirectedAttr, txId, false);
        graph.setObjectValue(txLineStyleAttr, txId, LineStyle.DASHED);

        txId = graph.addTransaction(vx5, vx4, false);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.getColorValue(1f, 1f, 0f, 1f));
        graph.setBooleanValue(txDirectedAttr, txId, false);
        graph.setObjectValue(txLineStyleAttr, txId, LineStyle.DOTTED);

        // Directed diamond.
        txId = graph.addTransaction(vx1, vx5, true);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.PINK);
        graph.setObjectValue(txLineStyleAttr, txId, LineStyle.DIAMOND);

        // Loops.
        txId = graph.addTransaction(vx2, vx2, true);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.PINK);

        txId = graph.addTransaction(vx4, vx4, false);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setBooleanValue(txDirectedAttr, txId, false);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.LIGHT_BLUE);

        txId = graph.addTransaction(vx5, vx5, true);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.ORANGE);

        txId = graph.addTransaction(vx5, vx5, false);
        graph.setIntValue(txIdAttr, txId, txId);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.GREEN);

        // Dimmed transactions.
        txId = graph.addTransaction(vx0, vx5, true);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.RED);
        graph.setBooleanValue(txDimmedAttr, txId, true);
        txId = graph.addTransaction(vx0, vx5, false);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.GREEN);
        graph.setBooleanValue(txDirectedAttr, txId, false);
        graph.setBooleanValue(txDimmedAttr, txId, true);
        txId = graph.addTransaction(vx5, vx0, true);
        graph.setObjectValue(txColorAttr, txId, ConstellationColor.BLUE);
        graph.setBooleanValue(txDimmedAttr, txId, true);

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        interaction.setProgress(1, 0, "Completed successfully", true);
    }

    /**
     * Return a random RGB color.
     *
     * @return A random RGB color.
     */
    private static ConstellationColor randomColor3(Random r) {
        return ConstellationColor.getColorValue(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1f);
    }
}

class IconRandomiser {

    private static final int MAX_ICONS = 65535;

    private final ArrayList<String> iconNames;
    private final Random random;

    IconRandomiser() {
        random = new Random();
        Set<String> t = IconManager.getIconNames(null);
        if (t.size() > MAX_ICONS) {
            final Set<String> tt = new HashSet<>(t);
            final ArrayList<String> tl = new ArrayList<>(tt);
            while (tt.size() > MAX_ICONS) {
                final String name = tl.get(random.nextInt(tl.size()));
                tt.remove(name);
            }

            t = tt;
        }

        iconNames = new ArrayList<>(t);
        System.out.printf("Sphere decorator icons: %d\n", iconNames.size());
    }

    public String getRandomName() {
        return iconNames.get(random.nextInt(iconNames.size()));
    }
}
