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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import au.gov.asd.tac.constellation.help.utilities.toc.TOCGenerator;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author aldebaran30701
 */
public class GeneratorNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of run method, of class Generator.
     */
    @Test
    public void testRun() {
        System.out.println("testing run");

        final String previousProperty = System.getProperty("constellation.environment");
        final String previousHeadlessPorperty = System.getProperty("java.awt.headless");

        try {
            // Test on IDE Version
            System.setProperty("constellation.environment", "IDE(CORE)");
            
            // This particular test needs to NOT be in headless mode
            System.setProperty("java.awt.headless", "false");
 
            try (final MockedStatic<Generator> generatorStaticMock = Mockito.mockStatic(Generator.class, Mockito.CALLS_REAL_METHODS)) {
                final List<File> tocXMLFiles = new ArrayList<>();
                final String layersTOC = "../ext/docs/CoreLayersView/src/au/gov/asd/tac/constellation/views/layers/layers-view-toc.xml";
                final String notesTOC  = "../ext/docs/CoreNotesView/src/au/gov/asd/tac/constellation/views/notes/notes-view-toc.xml";

                tocXMLFiles.add(new File(System.getProperty("user.dir") + layersTOC));
                tocXMLFiles.add(new File(System.getProperty("user.dir") + notesTOC));
                tocXMLFiles.add(new File("/non_consty_path/missing_file.md"));
                generatorStaticMock.when(() -> Generator.getXMLFiles(Mockito.any())).thenReturn(tocXMLFiles);
                generatorStaticMock.when(() -> Generator.getBaseDirectory()).thenCallRealMethod();
                generatorStaticMock.when(() -> Generator.getResource()).thenCallRealMethod();

                try (final MockedStatic<TOCGenerator> tocgeneratorStaticMock = Mockito.mockStatic(TOCGenerator.class)) {
                    tocgeneratorStaticMock.when(() -> TOCGenerator.createTOCFile(Mockito.anyString())).thenReturn(true);
                    tocgeneratorStaticMock.when(() -> TOCGenerator.convertXMLMappings(Mockito.any(), Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);
                    final Generator generator = new Generator();
                    System.out.println("prop : " + System.getProperty("constellation.environment"));
                    generator.run();

                    // verify that the toc file was called to be created, and that the xml mappings were to be converted
                    tocgeneratorStaticMock.verify(() -> TOCGenerator.createTOCFile(Mockito.anyString()), times(1));
                    tocgeneratorStaticMock.verify(() -> TOCGenerator.convertXMLMappings(Mockito.any(), Mockito.any()), times(1));

                    generatorStaticMock.verify(() -> Generator.getBaseDirectory(), times(2));
                }
            }
        } finally {
            // set back to previous property
            if (previousProperty != null) {
                System.setProperty("constellation.environment", previousProperty);
            }
            if (previousHeadlessPorperty != null) {
                System.setProperty("java.awt.headless", previousHeadlessPorperty);
            }
        }
    }
    
    /**
     * Test of run method, of class Generator.
     */
    @Test
    public void testGetXMLFiles() {
        System.out.println("testGetXMLFiles");

        try (MockedStatic<Lookup> lookupStaticMock = Mockito.mockStatic(Lookup.class)) {
            final Lookup lookup = mock(Lookup.class);

            final HelpPageProvider layersProvider = mock(HelpPageProvider.class);
            final HelpPageProvider notesProvider = mock(HelpPageProvider.class);
            String sep = File.separator;

            final String baseDirectory = "c:" + sep + "baseDir" + sep;
            final String layersTOC = ".." + sep + "ext" + sep + "docs" + sep + "CoreLayersVie" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep
                    + "tac" + sep + "constellation" + sep + "views" + sep + "layers" + sep + "layers-view-toc.xml";
            final String notesTOC = ".." + sep + "ext" + sep + "docs" + sep + "CoreNotesVie" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep
                    + "tac" + sep + "constellation" + sep + "views" + sep + "notes" + sep + "notes-view-toc.xml";

            when(layersProvider.getHelpTOC())
                    .thenReturn(layersTOC);
            when(notesProvider.getHelpTOC())
                    .thenReturn(notesTOC);

            lookupStaticMock.when(Lookup::getDefault).thenReturn(lookup);
            doReturn(List.of(
                    layersProvider,
                    notesProvider
            ))
                    .when(lookup).lookupAll(HelpPageProvider.class);

            final List<File> tocXMLFiles = Generator.getXMLFiles(baseDirectory);
            assertEquals(tocXMLFiles.size(), 2);
            assertEquals(tocXMLFiles.get(0).getPath(), baseDirectory + layersTOC);
            assertEquals(tocXMLFiles.get(1).getPath(), baseDirectory + notesTOC);
        }
    }

    /**
     * Test of getBaseDirectory method, of class Generator. Tests all available
     * file system roots, with module and class specific locations.
     * @throws java.net.URISyntaxException
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testGetBaseDirectory() throws URISyntaxException, MalformedURLException {
        System.out.println("testGetBaseDirectory");

        String sep = File.separator;

        try (MockedStatic<Generator> generatorStaticMock = Mockito.mockStatic(Generator.class, Mockito.CALLS_REAL_METHODS);
                MockedStatic<Paths> pathsStaticMock = Mockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS)) {
            // loop over all possible file roots on the file system
            for (final File file : File.listRoots()) {
                // try within base level of help module
                final String userDir = file.getPath() + "Users" + sep + "Username" + sep + "Constellation" + sep + "ext" + sep + "CoreHelp";

                // mock paths.get to return a path which is a mock - then return the user dir on call to toString()
                final Path pathUserDir = mock(Path.class);
                when(pathUserDir.toString()).thenReturn(userDir);
                pathsStaticMock.when(() -> Paths.get(Mockito.any())).thenReturn(pathUserDir);

                // mock url and uri so when generator.getResource is returned it calls the paths mock above
                final URL urlUserDir = mock(URL.class);
                final URI urIUserDir = mock(URI.class);
                when(urlUserDir.toURI()).thenReturn(urIUserDir);
                generatorStaticMock.when(() -> Generator.getResource()).thenReturn(userDir);

                final String expectedBaseDir = file.getPath() + "Users" + sep + "Username" + sep + "Constellation" + sep;
                assertEquals(Generator.getBaseDirectory(), expectedBaseDir);

                // try within help module package at a deeper level
                final String userDir2 = file.getPath() + "Users" + sep + "Username" + sep + "Constellation" + sep + "ext" + sep + "CoreHelp"
                        + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac";

                // mock paths.get to return a path which is a mock - then return the user dir on call to toString()
                final Path pathUserDir2 = mock(Path.class);
                when(pathUserDir2.toString()).thenReturn(userDir2);
                pathsStaticMock.when(() -> Paths.get(Mockito.any())).thenReturn(pathUserDir2);

                // mock url and uri so when generator.getResource is returned it calls the paths mock above
                final URL urlUserDir2 = mock(URL.class);
                final URI urIUserDir2 = mock(URI.class);
                when(urlUserDir2.toURI()).thenReturn(urIUserDir2);
                generatorStaticMock.when(() -> Generator.getResource()).thenReturn(userDir2);

                final String expectedBaseDir2 = file.getPath() + "Users" + sep + "Username" + sep + "Constellation" + sep;
                assertEquals(Generator.getBaseDirectory(), expectedBaseDir2);

                // try with file struct containing constellation
                final String userDir3 = file.getPath() + "Users" + sep + "Username" + sep + "constellation" + sep + "Constellation" + sep + "ext" + sep + "CoreHelp"
                        + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac";

                // mock paths.get to return a path which is a mock - then return the user dir on call to toString()
                final Path pathUserDir3 = mock(Path.class);
                when(pathUserDir3.toString()).thenReturn(userDir3);
                pathsStaticMock.when(() -> Paths.get(Mockito.any())).thenReturn(pathUserDir3);

                // mock url and uri so when generator.getResource is returned it calls the paths mock above
                final URL urlUserDir3 = mock(URL.class);
                final URI urIUserDir3 = mock(URI.class);
                when(urlUserDir3.toURI()).thenReturn(urIUserDir3);
                generatorStaticMock.when(() -> Generator.getResource()).thenReturn(userDir3);

                final String expectedBaseDir3 = file.getPath() + "Users" + sep + "Username" + sep + "constellation" + sep + "Constellation" + sep;
                assertEquals(Generator.getBaseDirectory(), expectedBaseDir3);
            }
        }
    }
}
