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

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Export a graph to an ArcGIS compatible Shapefile file.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@Messages("ExportToShapefilePlugin=Export to Shapefile")
public class ExportToShapefilePlugin extends AbstractGeoExportPlugin {

    public static final String GEOMETRY_TYPE_PARAMETER_ID = PluginParameter.buildId(ExportToShapefilePlugin.class, "shape_type");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = super.createParameters();

        final PluginParameter<SingleChoiceParameterValue> geometryTypeParameter = SingleChoiceParameterType.build(GEOMETRY_TYPE_PARAMETER_ID, GeometryTypeParameterValue.class);
        geometryTypeParameter.setName("Geometry Type");
        geometryTypeParameter.setDescription("The GeometryType enum value to export");
        geometryTypeParameter.setRequired(true);
        final List<GeometryTypeParameterValue> geometryTypeOptions = new ArrayList<>();
        Arrays.asList(GeometryType.values()).forEach(geometryType -> geometryTypeOptions.add(new GeometryTypeParameterValue(geometryType)));
        SingleChoiceParameterType.setOptionsData(geometryTypeParameter, geometryTypeOptions);
        SingleChoiceParameterType.setChoiceData(geometryTypeParameter, geometryTypeOptions.get(0));
        parameters.addParameter(geometryTypeParameter);

        return parameters;
    }

    @Override
    protected ExtensionFilter getExportType() {
        return new ExtensionFilter("Shapefile", FileExtensionConstants.SHAPE);
    }

    @Override
    protected void exportGeo(final PluginParameters parameters, final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes, final File output) throws IOException {
        final ParameterValue geometryTypePV = parameters.getSingleChoice(GEOMETRY_TYPE_PARAMETER_ID);
        final ParameterValue spatialReferencePV = parameters.getSingleChoice(SPATIAL_REFERENCE_PARAMETER_ID);
        
        if (geometryTypePV instanceof GeometryTypeParameterValue geometryTypeParameterValue && spatialReferencePV instanceof SpatialReferenceParameterValue spatialReferenceParameterValue) {
            final GeometryType geometryType = geometryTypeParameterValue.getGeometryType();
            final SpatialReference spatialReference = spatialReferenceParameterValue.getSpatialReference();
            Shape.generateShapefile(uuid, geometryType, shapes, attributes, output, spatialReference);
        }
    }

    @Override
    protected boolean includeSpatialReference() {
        return true;
    }

    public static final class GeometryTypeParameterValue extends ParameterValue {

        private GeometryType geometryType;

        public GeometryTypeParameterValue() {
            this.geometryType = null;
        }

        public GeometryTypeParameterValue(final GeometryType geometryType) {
            this.geometryType = geometryType;
        }

        @Override
        public final String validateString(final String s) {
            try {
                GeometryType.valueOf(s);
            } catch (IllegalArgumentException ex) {
                return String.format("%s is not a valid shape type (Expected Box, Line, Multi_Polygon, Point, Polygon).", s);
            }
            return null;
        }

        @Override
        public final boolean setStringValue(final String s) {
            final GeometryType type = GeometryType.valueOf(s);
            final boolean equal = Objects.equals(type, geometryType);
            if (!equal) {
                geometryType = type;
            }
            return equal;
        }

        @Override
        public final Object getObjectValue() {
            return geometryType;
        }

        @Override
        public final boolean setObjectValue(final Object o) {
            if (o instanceof GeometryType type) {
                final boolean equal = Objects.equals(type, geometryType);
                if (!equal) {
                    geometryType = type;
                }
                return equal;
            }
            return false;
        }

        public GeometryType getGeometryType() {
            return geometryType;
        }

        @Override
        protected final ParameterValue createCopy() {
            return new GeometryTypeParameterValue(geometryType);
        }

        @Override
        public final String toString() {
            return geometryType == null ? "No Value" : geometryType.name();
        }
    }
}
