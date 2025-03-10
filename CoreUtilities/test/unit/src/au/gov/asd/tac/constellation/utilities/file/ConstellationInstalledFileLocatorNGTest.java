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
package au.gov.asd.tac.constellation.utilities.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.openide.modules.InstalledFileLocator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class ConstellationInstalledFileLocatorNGTest {

    /**
     * The file is returned if it is successfully located by
     * InstalledFileLocator.
     *
     * @throws IOException necessary to construct a temp File
     */
    @Test
    public void testLocate() throws IOException {
        try (final MockedStatic<InstalledFileLocator> locatorMockedStatic = mockStatic(InstalledFileLocator.class)) {
            // mock the file locator to successfully return a temp file
            final InstalledFileLocator locator = mock(InstalledFileLocator.class);
            locatorMockedStatic.when(() -> InstalledFileLocator.getDefault()).thenReturn(locator);
            final File tmp = File.createTempFile("tmp", ".tmp");
            when(locator.locate(anyString(), anyString(), anyBoolean())).thenReturn(tmp);

            assertSame(ConstellationInstalledFileLocator.locate("", "", null), tmp);
        }
    }

    /**
     * If InstalledFileLocator can't find the file it can still be loaded by the
     * hacky backup method.
     *
     * @throws IOException if paths or the dummy URL can't be created
     */
    @Test
    public void testFileExists() throws IOException {
        try (final MockedStatic<InstalledFileLocator> locatorMockedStatic = mockStatic(InstalledFileLocator.class);
                final MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
            // mock the file locator to return null
            final InstalledFileLocator locator = mock(InstalledFileLocator.class);
            locatorMockedStatic.when(() -> InstalledFileLocator.getDefault()).thenReturn(locator);
            when(locator.locate(anyString(), anyString(), anyBoolean())).thenReturn(null);
            // mock the codeSource location to return a URI
            final ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
            final CodeSource codeSource = mock(CodeSource.class);
            when(protectionDomain.getCodeSource()).thenReturn(codeSource);
            when(codeSource.getLocation()).thenReturn(URI.create("http://dummy/").toURL());
            // create a temp file with two parent directories
            final Path dir1 = Files.createTempDirectory("dir1");
            final Path dir2 = Files.createTempDirectory(dir1, "dir2");
            final Path dirRelative = Files.createTempDirectory(dir1, "testRelative");
            final String relative = dirRelative.getFileName().toString();
            final Path tmpFile = Files.createTempFile(dir2, "tmp", ".tmp");
            // mock paths to return the temp file
            pathsMockedStatic.when(() -> Paths.get(any())).thenReturn(tmpFile);

            assertEquals(ConstellationInstalledFileLocator.locate(relative, "", protectionDomain), dirRelative.toFile());
        }
    }

    /**
     * A file that cannot be located by InstalledFileLocator or the hacky backup
     * method causes a RuntimeException to be thrown.
     *
     * @throws IOException if a dummy URL can't be created
     */
    @Test(expectedExceptions = {RuntimeException.class}, expectedExceptionsMessageRegExp = "Couldn't find file.*")
    public void testFileNotExist() throws IOException {
        try (final MockedStatic<InstalledFileLocator> locatorMockedStatic = mockStatic(InstalledFileLocator.class);
                final MockedStatic<Paths> pathsMockedStatic = mockStatic(Paths.class)) {
            // mock the file locator to return null
            final InstalledFileLocator locator = mock(InstalledFileLocator.class);
            locatorMockedStatic.when(() -> InstalledFileLocator.getDefault()).thenReturn(locator);
            when(locator.locate(anyString(), anyString(), anyBoolean())).thenReturn(null);
            // mock the codeSource location to return a URI
            final ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
            final CodeSource codeSource = mock(CodeSource.class);
            when(protectionDomain.getCodeSource()).thenReturn(codeSource);
            when(codeSource.getLocation()).thenReturn(URI.create("http://dummy/").toURL());
            // mock paths to return a temp file
            final Path tmpFile = Files.createTempFile("tmp", ".tmp");
            pathsMockedStatic.when(() -> Paths.get(any())).thenReturn(tmpFile);

            ConstellationInstalledFileLocator.locate("dummy", "", protectionDomain);
        }
    }

    /**
     * A RuntimeException wrapping an URISyntaxException is thrown when the
     * location of the code source obtained from the ProtectionDomain parameter
     * is an invalid URI.
     *
     * @throws IOException necessary to construct a bogus URL
     */
    @Test(expectedExceptions = {RuntimeException.class}, expectedExceptionsMessageRegExp = ".*URISyntaxException.*")
    public void testIncorrectUri() throws IOException {
        try (final MockedStatic<InstalledFileLocator> locatorMockedStatic = mockStatic(InstalledFileLocator.class)) {
            // mock the file locator to return null
            final InstalledFileLocator locator = mock(InstalledFileLocator.class);
            locatorMockedStatic.when(() -> InstalledFileLocator.getDefault()).thenReturn(locator);
            when(locator.locate(anyString(), anyString(), anyBoolean())).thenReturn(null);
            // mock the codeSource location to return a syntactically incorrect URI
            final ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
            final CodeSource codeSource = mock(CodeSource.class);
            when(protectionDomain.getCodeSource()).thenReturn(codeSource);
            // TODO: Investigate options for removing URL constructor use given it has been deprecated
            // issue is that the recommended switch (via URI object) doesn't work (would throw the exception here instead of in the function we're testing) 
            // since the URI we're generating is obviously invalid (that's the point of this test)
            when(codeSource.getLocation()).thenReturn(new URL("http://dummy/a?b^c"));

            ConstellationInstalledFileLocator.locate("", "", protectionDomain);
        }
    }
}
