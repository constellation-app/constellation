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
package au.gov.asd.tac.constellation.views.mapview.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationLineMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationMultiMarker;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationPolygonMarker;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerCache;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * A plugin to create nodes on a graph and copy copy custom marker data to them.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(tags = {PluginTags.MODIFY})
@NbBundle.Messages("CopyCustomMarkersToGraphPlugin=Copy Custom Markers to Graph")
public class CopyCustomMarkersToGraphPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vertexLatitudeAttributeId = SpatialConcept.VertexAttribute.LATITUDE.ensure(graph);
        final int vertexLongitudeAttributeId = SpatialConcept.VertexAttribute.LONGITUDE.ensure(graph);
        final int vertexPrecisionAttributeId = SpatialConcept.VertexAttribute.PRECISION.ensure(graph);
        final int vertexShapeAttributeId = SpatialConcept.VertexAttribute.SHAPE.ensure(graph);

        final MarkerCache markerCache = MarkerCache.getDefault();
        for (final ConstellationAbstractMarker marker : markerCache.getCustomMarkers()) {
            final String markerId = marker.getId() == null ? marker.toString() : marker.getId();

            final int vertexId = graph.addVertex();
            graph.setStringValue(vertexIdentifierAttributeId, vertexId, markerId);
            graph.setObjectValue(vertexTypeAttributeId, vertexId, AnalyticConcept.VertexType.LOCATION);
            graph.setFloatValue(vertexLatitudeAttributeId, vertexId, marker.getLocation().getLat());
            graph.setFloatValue(vertexLongitudeAttributeId, vertexId, marker.getLocation().getLon());
            graph.setFloatValue(vertexPrecisionAttributeId, vertexId, (float) marker.getRadius());

            try {
                final Shape.GeometryType geometryType = marker instanceof ConstellationMultiMarker ? Shape.GeometryType.MULTI_POLYGON
                        : marker instanceof ConstellationPolygonMarker ? Shape.GeometryType.POLYGON
                                : marker instanceof ConstellationLineMarker ? Shape.GeometryType.LINE
                                        : Shape.GeometryType.POINT;
                final List<Tuple<Double, Double>> coordinates = marker.getLocations().stream()
                        .map(location -> Tuple.create((double) location.getLon(), (double) location.getLat()))
                        .collect(Collectors.toList());
                final String shape = Shape.generateShape(markerId, geometryType, coordinates);
                graph.setStringValue(vertexShapeAttributeId, vertexId, shape);
            } catch (final IOException ex) {
                throw new PluginException(PluginNotificationLevel.ERROR, ex);
            }
        }

        PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
    }
}
