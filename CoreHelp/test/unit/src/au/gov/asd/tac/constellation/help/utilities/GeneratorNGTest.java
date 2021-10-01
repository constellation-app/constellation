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
//package au.gov.asd.tac.constellation.help.utilities;
//
//import au.gov.asd.tac.constellation.help.HelpPageProvider;
//import au.gov.asd.tac.constellation.help.utilities.toc.TOCGenerator;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import static org.mockito.Mockito.doReturn;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.when;
//import org.mockito.stubbing.Answer;
//import org.openide.util.Lookup;
//import static org.testng.Assert.assertEquals;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
///**
// *
// * @author aldebaran30701
// */
//public class GeneratorNGTest {
//
//    public GeneratorNGTest() {
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
//    }
//
//    @AfterMethod
//    public void tearDownMethod() throws Exception {
//    }
//
//    /**
//     * Test of run method, of class Generator.
//     */
//    @Test
//    public void testRunDevelopment() {
//        System.out.println("run in development");
//
//        final String previousProperty = System.getProperty("constellation.environment");
//
//        try {
//            // Test on IDE Version
//            System.setProperty("constellation.environment", "IDE(CORE)");
//
//            try (MockedStatic<Generator> generatorStaticMock = Mockito.mockStatic(Generator.class, Mockito.CALLS_REAL_METHODS)) {
//                final List<File> tocXMLFiles = new ArrayList<>();
//                final String layersTOC = "../constellation/src/au/gov/asd/tac/constellation/views/layers/docs/layers-view-toc.xml";
//                final String notesTOC = "../constellation/src/au/gov/asd/tac/constellation/views/notes/docs/notes-view-toc.xml";
//
//                tocXMLFiles.add(new File(System.getProperty("user.dir") + layersTOC));
//                tocXMLFiles.add(new File(System.getProperty("user.dir") + notesTOC));
//                generatorStaticMock.when(() -> Generator.getXMLFiles(Mockito.any())).thenReturn(tocXMLFiles);
//                generatorStaticMock.when(() -> Generator.getBaseDirectory()).thenCallRealMethod();
//
//                try (MockedStatic<TOCGenerator> tocgeneratorStaticMock = Mockito.mockStatic(TOCGenerator.class)) {
//                    tocgeneratorStaticMock.when(() -> TOCGenerator.createTOCFile(Mockito.anyString())).thenReturn(true);
//                    tocgeneratorStaticMock.when(() -> TOCGenerator.convertXMLMappings(Mockito.any(), Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);
//                    Generator generator = new Generator();
//                    generator.run();
//
//                    // verify that the toc file was called to be created, and that the xml mappings were to be converted
//                    tocgeneratorStaticMock.verify(() -> TOCGenerator.createTOCFile(Mockito.anyString()));
//                    tocgeneratorStaticMock.verify(() -> TOCGenerator.convertXMLMappings(Mockito.any(), Mockito.any()));
//
//                    generatorStaticMock.verify(times(1), () -> Generator.getBaseDirectory());
//                }
//            }
//        } finally {
//            // set back to previous property
//            if (previousProperty != null) {
//                System.setProperty("constellation.environment", previousProperty);
//            }
//        }
//
//    }
//
//    /**
//     * Test of run method, of class Generator.
//     */
//    @Test
//    public void testRunProduction() {
//        System.out.println("run in production");
//
//        final String previousProperty = System.getProperty("constellation.environment");
//
//        try {
//            // Test on production Version
//            System.setProperty("constellation.environment", "PRODUCTION");
//
//            try (MockedStatic<Generator> generatorStaticMock = Mockito.mockStatic(Generator.class, Mockito.CALLS_REAL_METHODS)) {
//                generatorStaticMock.when(() -> Generator.getBaseDirectory()).thenCallRealMethod();
//
//                try (MockedStatic<HelpMapper> helpMapperStaticMock = Mockito.mockStatic(HelpMapper.class)) {
//                    try (MockedStatic<TOCGenerator> tocgeneratorStaticMock = Mockito.mockStatic(TOCGenerator.class)) {
//                        tocgeneratorStaticMock.when(() -> TOCGenerator.createTOCFile(Mockito.anyString())).thenReturn(true);
//
//                        helpMapperStaticMock.when(() -> HelpMapper.updateMappings()).thenAnswer((Answer<Void>) invocation -> null);
//                        Generator generator = new Generator();
//                        generator.run();
//
//                        // verify that helpMappings was called once
//                        helpMapperStaticMock.verify(times(1), () -> HelpMapper.updateMappings());
//
//                        // verify that no method from TOCGenerator was called
//                        tocgeneratorStaticMock.verifyNoInteractions();
//
//                        generatorStaticMock.verify(times(1), () -> Generator.getBaseDirectory());
//                    }
//                }
//            }
//        } finally {
//            if (previousProperty != null) {
//                System.setProperty("constellation.environment", previousProperty);
//            }
//        }
//    }
//
//    /**
//     * Test of run method, of class Generator.
//     */
//    @Test
//    public void testGetXMLFiles() {
//        System.out.println("testGetXMLFiles");
//
//        try (MockedStatic<Lookup> lookupStaticMock = Mockito.mockStatic(Lookup.class)) {
//            final Lookup lookup = mock(Lookup.class);
//
//            final HelpPageProvider layersProvider = mock(HelpPageProvider.class);
//            final HelpPageProvider notesProvider = mock(HelpPageProvider.class);
//
//            final String baseDirectory = "c://baseDir/";
//            final String layersTOC = "../constellation/src/au/gov/asd/tac/constellation/views/layers/docs/layers-view-toc.xml";
//            final String notesTOC = "../constellation/src/au/gov/asd/tac/constellation/views/notes/docs/notes-view-toc.xml";
//
//            final String baseDirectoryExpected = "c:\\baseDir\\";
//            final String layersTOCExpected = "..\\constellation\\src\\au\\gov\\asd\\tac\\constellation\\views\\layers\\docs\\layers-view-toc.xml";
//            final String notesTOCExpected = "..\\constellation\\src\\au\\gov\\asd\\tac\\constellation\\views\\notes\\docs\\notes-view-toc.xml";
//
//            when(layersProvider.getHelpTOC())
//                    .thenReturn(layersTOC);
//            when(notesProvider.getHelpTOC())
//                    .thenReturn(notesTOC);
//
//            lookupStaticMock.when(Lookup::getDefault).thenReturn(lookup);
//            doReturn(List.of(
//                    layersProvider,
//                    notesProvider
//            ))
//                    .when(lookup).lookupAll(HelpPageProvider.class);
//
//            final List<File> tocXMLFiles = Generator.getXMLFiles(baseDirectory);
//
//            assertEquals(tocXMLFiles.size(), 2);
//            assertEquals(tocXMLFiles.get(0).getPath(), baseDirectoryExpected + layersTOCExpected);
//            assertEquals(tocXMLFiles.get(1).getPath(), baseDirectoryExpected + notesTOCExpected);
//        }
//    }
//
//    /**
//     * Test of getBaseDirectory method, of class Generator.
//     */
//    @Test
//    public void testGetBaseDirectory() {
//        System.out.println("testGetBaseDirectory");
//
//        final String previousUserDir = System.getProperty("user.dir");
//
//        final String userDir = "C:\\Users\\Username\\Constellation\\constellation\\CoreHelp";
//        final String expectedBaseDir = "C:\\Users\\Username\\Constellation\\";
//
//        final String userDir2 = "C:\\Users\\Username\\Constellation\\constellation\\CoreHelp\\src\\au\\gov\\asd\\tac\\";
//        final String expectedBaseDir2 = "C:\\Users\\Username\\Constellation\\";
//
//        try {
//            System.setProperty("user.dir", userDir);
//            assertEquals(Generator.getBaseDirectory(), expectedBaseDir);
//
//            System.setProperty("user.dir", userDir2);
//            assertEquals(Generator.getBaseDirectory(), expectedBaseDir2);
//        } finally {
//            // clean up and reset the property
//            if (previousUserDir != null) {
//                System.setProperty("user.dir", previousUserDir);
//            }
//        }
//    }
//
//}
