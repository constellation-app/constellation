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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
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
@Messages("StructuredGraphBuilderPlugin=Structured Graph Builder")
public class StructuredGraphBuilderPlugin extends SimpleEditPlugin {

    public static final String BACKBONE_SIZE_PARAMETER_ID = PluginParameter.buildId(StructuredGraphBuilderPlugin.class, "backbone_size");
    public static final String BACKBONE_DENSITY_PARAMETER_ID = PluginParameter.buildId(StructuredGraphBuilderPlugin.class, "backbone_density");
    public static final String RADIUS = PluginParameter.buildId(StructuredGraphBuilderPlugin.class, "radius");
    
    private final SecureRandom r = new SecureRandom();

    @Override
    public String getDescription() {
        return "Builds a random sphere graph.";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> backboneSize = IntegerParameterType.build(BACKBONE_SIZE_PARAMETER_ID);
        backboneSize.setName("Backbone size");
        backboneSize.setDescription("Backbone size");
        backboneSize.setIntegerValue(1000);
        IntegerParameterType.setMinimum(backboneSize, 0);
        params.addParameter(backboneSize);

        final PluginParameter<IntegerParameterValue> backboneDensity = IntegerParameterType.build(BACKBONE_DENSITY_PARAMETER_ID);
        backboneDensity.setName("Backbone density");
        backboneDensity.setDescription("Backbone density");
        backboneDensity.setIntegerValue(300);
        IntegerParameterType.setMinimum(backboneDensity, 0);
        params.addParameter(backboneDensity);

        final PluginParameter<IntegerParameterValue> radius = IntegerParameterType.build(RADIUS);
        radius.setName("Radius");
        radius.setDescription("Radius");
        radius.setIntegerValue(100);
        IntegerParameterType.setMinimum(radius, 0);
        params.addParameter(radius);

        return params;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        final Map<String, PluginParameter<?>> params = parameters.getParameters();

        final int backboneVertexCount = params.get(BACKBONE_SIZE_PARAMETER_ID).getIntegerValue();
        final int backboneDensity = params.get(BACKBONE_DENSITY_PARAMETER_ID).getIntegerValue();
        final int radiusFactor = params.get(RADIUS).getIntegerValue();

        long dt = new Date().getTime();

        //        InputOutput io = IOProvider.getDefault().getIO("Structured Graph", false);
        // Icons will be chosen from the provided defaults.
        final ArrayList<String> iconNames = new ArrayList<>(IconManager.getIconNames(null));

        // "Name" is already the primary key.
        final int vxNameAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vxIdentifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vxColorAttr = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vxForegroundIconAttr = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);
        final int vxBackgroundIconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int vxVisibilityAttr = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
        final int vxXAttr = VisualConcept.VertexAttribute.X.ensure(graph);
        final int vxYAttr = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int vxZAttr = VisualConcept.VertexAttribute.Z.ensure(graph);

        final int vxInterestingAttr = graph.addAttribute(GraphElementType.VERTEX, "boolean", "interesting", null, false, null);

        final int txColorAttr = VisualConcept.TransactionAttribute.COLOR.ensure(graph);

        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        float radius = (float) Math.sqrt(backboneVertexCount) * radiusFactor;

        for (int n = 0; n < backboneVertexCount; n++) {

            final int nodeId = graph.addVertex();
            graph.setStringValue(vxNameAttr, nodeId, "Node " + n);
            graph.setStringValue(vxIdentifierAttr, nodeId, "Node " + n);
            graph.setStringValue(vxBackgroundIconAttr, nodeId, "Background.Round Circle");
            graph.setStringValue(vxForegroundIconAttr, nodeId, getRandomIconName(iconNames, r));
            graph.setObjectValue(vxColorAttr, nodeId, randomColorWithAlpha(r));
            graph.setFloatValue(vxVisibilityAttr, nodeId, 1.0f);

            float x;
            float y;
            float z;
            float length;
            do {
                x = r.nextFloat() * 2 - 1;
                y = r.nextFloat() * 2 - 1;
                z = r.nextFloat() * 2 - 1;
                length = (float) Math.sqrt(x * x + y * y + z * z);
            } while (length > 1);

            graph.setFloatValue(vxXAttr, nodeId, x * radius);
            graph.setFloatValue(vxYAttr, nodeId, y * radius);
            graph.setFloatValue(vxZAttr, nodeId, z * radius);
        }

        float[] minDistances = new float[backboneVertexCount];
        Arrays.fill(minDistances, Float.MAX_VALUE);
        int[] closestNeighbour = new int[backboneVertexCount];

        for (int s = 0; s < backboneVertexCount; s++) {
            for (int d = s + 1; d < backboneVertexCount; d++) {
                int source = graph.getVertex(s);
                int destination = graph.getVertex(d);

                float sx = graph.getFloatValue(vxXAttr, source);
                float sy = graph.getFloatValue(vxYAttr, source);
                float sz = graph.getFloatValue(vxZAttr, source);

                float dx = graph.getFloatValue(vxXAttr, destination);
                float dy = graph.getFloatValue(vxYAttr, destination);
                float dz = graph.getFloatValue(vxZAttr, destination);

                float x = sx - dx;
                float y = sy - dy;
                float z = sz - dz;

                float distance = (float) Math.sqrt(x * x + y * y + z * z);
                if (minDistances[s] > distance) {
                    minDistances[s] = distance;
                    closestNeighbour[s] = d;
                }
                if (minDistances[d] > distance) {
                    minDistances[d] = distance;
                    closestNeighbour[d] = s;
                }

                int transactionCount = (int) (radius / (distance * distance) * r.nextInt(backboneDensity));

                for (int t = 0; t < transactionCount; t++) {
                    createRandomTransaction(graph, source, destination, txDateTimeAttr, txColorAttr, r, dt);
                }
            }
        }

        for (int s = 0; s < backboneVertexCount; s++) {
            int source = graph.getVertex(s);
            int destination = closestNeighbour[s];
            createRandomTransaction(graph, source, destination, txDateTimeAttr, txColorAttr, r, dt);

            float cx = graph.getFloatValue(vxXAttr, source);
            float cy = graph.getFloatValue(vxYAttr, source);
            float cz = graph.getFloatValue(vxZAttr, source);

            float minDistance = minDistances[s];
            float pendants = minDistance * minDistance * r.nextFloat() / backboneVertexCount;

            double interestDensity = r.nextDouble() * 0.9;
            interestDensity *= interestDensity * interestDensity * interestDensity * interestDensity;

            for (int p = 0; p < pendants; p++) {
                int pendant = graph.addVertex();
                graph.setStringValue(vxNameAttr, pendant, "Node " + pendant);
                graph.setStringValue(vxIdentifierAttr, pendant, "Node " + pendant);
                graph.setStringValue(vxBackgroundIconAttr, pendant, "Background.Round Circle_64");
                graph.setStringValue(vxForegroundIconAttr, pendant, getRandomIconName(iconNames, r));
                graph.setObjectValue(vxColorAttr, pendant, randomColorWithAlpha(r));
                graph.setFloatValue(vxVisibilityAttr, pendant, 1.0f);

                float x = r.nextFloat() * 2 - 1;
                float y = r.nextFloat() * 2 - 1;
                float z = r.nextFloat() * 2 - 1;
                float length = (float) Math.sqrt(x * x + y * y + z * z);

                float pendantRadius = minDistance * 0.7f * r.nextFloat();
                graph.setFloatValue(vxXAttr, pendant, cx + x / length * pendantRadius);
                graph.setFloatValue(vxYAttr, pendant, cy + y / length * pendantRadius);
                graph.setFloatValue(vxZAttr, pendant, cz + z / length * pendantRadius);

                graph.setBooleanValue(vxInterestingAttr, pendant, r.nextDouble() < interestDensity);

                createRandomTransaction(graph, source, pendant, txDateTimeAttr, txColorAttr, r, dt);
            }
        }

        int selectedPosition = r.nextInt(backboneVertexCount);
        int selectedVertex = graph.getVertex(selectedPosition);
        graph.setBooleanValue(vxSelectedAttr, selectedVertex, true);

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        interaction.setProgress(1, 0, "Completed successfully", true);

    }

    static void createRandomTransaction(final GraphWriteMethods graph, final int source, final int destination, final int attrTxDatetime, final int colorAttr, SecureRandom r, long dt) {
        int transaction = 0;
        switch (r.nextInt(3)) {
            case 0:
                transaction = graph.addTransaction(source, destination, true);
                break;
            case 1:
                transaction = graph.addTransaction(destination, source, true);
                break;
            case 2:
                transaction = graph.addTransaction(source, destination, false);
                break;
            default:
                break;
        }

        graph.setLongValue(attrTxDatetime, transaction, dt++);
        graph.setObjectValue(colorAttr, transaction, randomColorWithAlpha(r));
    }

    private static String getRandomIconName(final ArrayList<String> iconNames, SecureRandom r) {
        int i;
        do {
            i = r.nextInt(iconNames.size());
        } while (iconNames.get(i).endsWith("_64") || iconNames.get(i).equals(DefaultIconProvider.HIGHLIGHTED.getExtendedName()) || iconNames.get(i).equals(DefaultIconProvider.UNKNOWN.getExtendedName()) || iconNames.get(i).equals(DefaultIconProvider.NOISE.getExtendedName()));

        return iconNames.get(i);
    }

    private static ConstellationColor randomColorWithAlpha(SecureRandom r) {
        return ConstellationColor.getColorValue(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1.0f);
    }
}
