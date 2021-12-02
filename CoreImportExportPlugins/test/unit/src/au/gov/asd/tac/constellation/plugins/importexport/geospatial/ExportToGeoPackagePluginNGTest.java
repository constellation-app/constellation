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

import static au.gov.asd.tac.constellation.plugins.importexport.geospatial.AbstractGeoExportPlugin.SPATIAL_REFERENCE_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference;
import java.io.File;
import java.util.Map;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol695510
 */
public class ExportToGeoPackagePluginNGTest {

    private static MockedStatic<Shape> shapeMockStatic;
    private static PluginParameters parametersMock;
    private static Map<String, String> shapesMock;
    private static Map<String, Map<String, Object>> attributesMock;
    private static SpatialReferenceParameterValue spatialReferenceParameterValueMock;
    private static SpatialReference spatialReferenceMock;

    public ExportToGeoPackagePluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        shapeMockStatic = Mockito.mockStatic(Shape.class);
        parametersMock = Mockito.mock(PluginParameters.class);
        shapesMock = Mockito.mock(Map.class);
        attributesMock = Mockito.mock(Map.class);
        spatialReferenceParameterValueMock = Mockito.mock(SpatialReferenceParameterValue.class);
        spatialReferenceMock = Mockito.mock(SpatialReference.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        shapeMockStatic.close();
    }

    /**
     * Test of exportGeo method, of class ExportToGeoPackagePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExportGeo() throws Exception {
        System.out.println("testExportGeo");

        final ExportToGeoPackagePlugin instance = new ExportToGeoPackagePlugin();

        final PluginParameters parameters = parametersMock;
        final String uuid = "";
        final Map<String, String> shapes = shapesMock;
        final Map<String, Map<String, Object>> attributes = attributesMock;
        final File output = File.createTempFile("testGeoPackageFile", ".gpkg");

        doReturn(spatialReferenceParameterValueMock).when(parametersMock).getSingleChoice(SPATIAL_REFERENCE_PARAMETER_ID);
        doReturn(spatialReferenceMock).when(spatialReferenceParameterValueMock).getSpatialReference();

        instance.exportGeo(parameters, uuid, shapes, attributes, output);

        shapeMockStatic.verify(() -> Shape.generateGeoPackage(uuid, shapes, attributes, output, spatialReferenceMock));
    }

    /**
     * Test of exportGeo method, of class ExportToGeoPackagePlugin, when an
     * assertion error is thrown.
     *
     * @throws java.lang.Exception
     */
    @Test(expectedExceptions = AssertionError.class)
    public void testExportGeo_assertionError() throws Exception {
        System.out.println("testExportGeo_assertionError");

        final ExportToGeoPackagePlugin instance = new ExportToGeoPackagePlugin();

        final PluginParameters parameters = parametersMock;
        final String uuid = "";
        final Map<String, String> shapes = shapesMock;
        final Map<String, Map<String, Object>> attributes = attributesMock;
        final File output = File.createTempFile("testGeoPackageFile", ".gpkg");

        instance.exportGeo(parameters, uuid, shapes, attributes, output);
    }
}
