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
 * Populates the list of Urls of the LanguageTool Dependencies, required by the
 * LanguagetoolClassLoader. The list of the jars used by Languagetool, as in
 * LanguagetoolDependencies.txt file, are populated from the common location,
 * ignoring the version numbers. Indriya1.3 and uom-lib-common-1.1 are loaded
 * from languagetoolconf folder to avoid dependency conflicts.
 *
 * @author Auriga2
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageToolDependencyUrlLoader {

    private static final Logger LOGGER = Logger.getLogger(LanguageToolDependencyUrlLoader.class.getName());
    // Regex to extract the base name without version of a jar file(e.g., library-name-1.0.0.jar -> library-name)
    private static final Pattern JAR_BASE_NAME = Pattern.compile("^(.*?)-[0-9]+(?:\\.[0-9]+)*(?:-[a-zA-Z0-9-.]+)?\\.jar$");

    public static List<URL> loadUrls() throws MalformedURLException {
        final ProtectionDomain domain = LanguageToolDependencyUrlLoader.class.getProtectionDomain();

        if (domain.getCodeSource() != null) {
            String classPath = domain.getCodeSource().getLocation().getPath();

            // Remove the "file:" prefix if it exists
            if (classPath.startsWith("file:")) {
                classPath = classPath.substring(5);
            }

            final File jarFolder = new File(URI.create(classPath).getPath()).getParentFile();

            final File dependencyFolder = new File(jarFolder, "ext");
            final File languageToolIndriyaFolder = new File(jarFolder, "ext/languagetoolconf");

            if (!languageToolIndriyaFolder.exists() || !languageToolIndriyaFolder.isDirectory()) {
                LOGGER.log(Level.SEVERE, String.format("Can't locate the directory containing LanguageTool dependencies- '%s'.", languageToolIndriyaFolder.getAbsolutePath()));
                return null;
            }

            final Set<String> languagetoolDependenciesList = populateLanguagetoolDependenciesListFromFile();

            final List<URL> libLanguageToolUrls = populateFilteredJarUrls(dependencyFolder, languagetoolDependenciesList);

            // Add indriya v1.3 and other conflicting deps from the languagetoolconf folder
            for (final File file : languageToolIndriyaFolder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    libLanguageToolUrls.add(file.toURI().toURL());
                }
            }
            return libLanguageToolUrls;
        }
        return null;
    }

    private static Set<String> populateLanguagetoolDependenciesListFromFile() {
        final String languagetoolDependenciesListFile = "LanguagetoolDependencies.txt";
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(LanguageToolDependencyUrlLoader.class.getResourceAsStream(languagetoolDependenciesListFile), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> (line.length() > 0 && !line.startsWith("#")))
                    .map(LanguageToolDependencyUrlLoader::extractBaseName)
                    .collect(Collectors.toSet());

        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, String.format("Can't load the dependencies file- '%s'.", languagetoolDependenciesListFile));
            return null;
        }
    }

    private static List<URL> populateFilteredJarUrls(final File dependencyFolder, final Set<String> languagetoolDependencies) {
        List<URL> libLanguageToolUrls = new ArrayList<>();

        if (!dependencyFolder.exists() || !dependencyFolder.isDirectory()) {
            LOGGER.log(Level.SEVERE, String.format("Can't locate the directory containing common dependencies- '%s'.", dependencyFolder.getAbsolutePath()));
            return null;
        }

        final File[] files = dependencyFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files != null) {
            libLanguageToolUrls = Stream.of(files)
                    .filter(file -> languagetoolDependencies.contains(extractBaseName(file.getName())))
                    .map(file -> {
                        try {
                            return file.toURI().toURL();
                        } catch (final MalformedURLException e) {
                            LOGGER.log(Level.SEVERE, String.format("MalformedURLException thrown populating the URL of '%s'.", file.getName()));
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        }

        return libLanguageToolUrls;
    }

    private static String extractBaseName(final String fileName) {
        final Matcher m = JAR_BASE_NAME.matcher(fileName);
        if (m.matches()) {
            return m.group(1); // Base name without version
        }
        return fileName; // If the pattern doesn't match, return the full file name
    }
}
