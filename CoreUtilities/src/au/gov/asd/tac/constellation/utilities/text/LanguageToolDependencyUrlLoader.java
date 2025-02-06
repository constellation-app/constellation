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
package au.gov.asd.tac.constellation.utilities.text;

/**
 *
 * @author Auriga2
 */
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LanguageToolDependencyUrlLoader {

    private static final Logger LOGGER = Logger.getLogger(LanguageToolDependencyUrlLoader.class.getName());

    public static URL[] loadUrls() throws MalformedURLException {
        final ProtectionDomain domain = LanguageToolDependencyUrlLoader.class.getProtectionDomain();
        final File jarFolder;

        if (domain.getCodeSource() != null) {
            String classPath = domain.getCodeSource().getLocation().getPath();

            // Remove the "file:" prefix if it exists
            if (classPath.startsWith("file:")) {
                classPath = classPath.substring(5);
            }

            jarFolder = new File(URI.create(classPath).getPath()).getParentFile();

            final File libLanguageToolFolder = new File(jarFolder, "ext/languagetoolconf");

            if (!libLanguageToolFolder.exists() || !libLanguageToolFolder.isDirectory()) {
                LOGGER.log(Level.SEVERE, String.format("Can't locate the directory containing dependencies '%s'.", libLanguageToolFolder.getAbsolutePath()));
                return null;
            }

            final File[] libLanguageToolFiles = libLanguageToolFolder.listFiles((dir, name) -> name.endsWith(".jar"));

            // Create an array of URLs for LibA and its dependencies
            final URL[] libLanguageToolUrls = new URL[libLanguageToolFiles.length];
            for (int i = 0; i < libLanguageToolFiles.length; i++) {
                libLanguageToolUrls[i] = libLanguageToolFiles[i].toURI().toURL();
            }
            return libLanguageToolUrls;
        }
        return null;
    }
}
