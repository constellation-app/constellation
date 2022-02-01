///*
// * Copyright 2010-2021 Australian Signals Directorate
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package au.gov.asd.tac.constellation.plugins.importexport.geospatial;
//
//import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
//import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.Map;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import static org.testng.Assert.assertEquals;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
///**
// *
// * @author sol695510
// */
//public class ExportToGeoJsonPluginNGTest {
//
//    private static MockedStatic<Shape> shapeStaticMock;
//    private static PluginParameters parametersMock;
//    private static Map<String, String> shapesMock;
//    private static Map<String, Map<String, Object>> attributesMock;
//
//    public ExportToGeoJsonPluginNGTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }
//
//    @BeforeMethod
//    public void setUpMethod() throws Exception {
//        shapeStaticMock = Mockito.mockStatic(Shape.class);
//        parametersMock = Mockito.mock(PluginParameters.class);
//        shapesMock = Mockito.mock(Map.class);
//        attributesMock = Mockito.mock(Map.class);
//    }
//
//    @AfterMethod
//    public void tearDownMethod() throws Exception {
//        shapeStaticMock.close();
//    }
//
//    /**
//     * Test of exportGeo method, of class ExportToGeoJsonPlugin.
//     *
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testExportGeo() throws Exception {
//        System.out.println("testExportGeo");
//
//        final ExportToGeoJsonPlugin instance = new ExportToGeoJsonPlugin();
//
//        final PluginParameters parameters = parametersMock;
//        final String uuid = "";
//        final Map<String, String> shapes = shapesMock;
//        final Map<String, Map<String, Object>> attributes = attributesMock;
//        final File output = File.createTempFile("testJSONFile", ".json");
//
//        final String geoJson = "someJSON";
//
//        shapeStaticMock.when(() -> Shape.generateShapeCollection(uuid, shapes, attributes)).thenReturn(geoJson);
//
//        instance.exportGeo(parameters, uuid, shapes, attributes, output);
//
//        final BufferedReader reader = new BufferedReader(new FileReader(output));
//
//        final String expResult = geoJson;
//        final String result = reader.readLine();
//
//        assertEquals(result, expResult);
//    }
//}
