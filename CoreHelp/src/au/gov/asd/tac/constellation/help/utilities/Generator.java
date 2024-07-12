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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import au.gov.asd.tac.constellation.help.utilities.toc.TOCGenerator;
import au.gov.asd.tac.constellation.help.utilities.toc.TOCItem;
import au.gov.asd.tac.constellation.help.utilities.toc.TreeNode;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import org.openide.windows.OnShowing;

/**
 * Generates help file mappings and creates application-wide table of contents
 *
 * @author aldebaran30701
 */
@OnShowing()
public class Generator implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Generator.class.getName());
    private static String baseDirectory = "";
    private static String tocDirectory = "";
    private static String onlineTocDirectory = "";
    public static final String TOC_FILE_NAME = "toc.md";
    public static final String ROOT_NODE_NAME = "Constellation Documentation";

    public Generator() {
        // Intentionally left blank
    }

    /**
     * Generate a table of contents in dev versions of code
     */
    @Override
    public void run() {
        baseDirectory = getBaseDirectory();
        tocDirectory = String.format("ext%1$s%2$s", File.separator, TOC_FILE_NAME);
        onlineTocDirectory = getOnlineHelpTOCDirectory(baseDirectory);

        // First: create the TOCFile in the base directory for ONLINE help
        // Create the root node for application-wide table of contents
        TOCGenerator.createTOCFile(onlineTocDirectory);        
        final TreeNode<?> root = new TreeNode(new TOCItem(ROOT_NODE_NAME, ""));
        final List<File> tocXMLFiles = getXMLFiles(baseDirectory);
        try {
            TOCGenerator.convertXMLMappings(tocXMLFiles, root);
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, String.format("There was an error creating the documentation file %s "
                    + "- Documentation may not be complete", baseDirectory), ex);
        }

        // Second: Create TOCFile for OFFLINE help with the location of the resources file
        // Create the root node for application-wide table of contents
        TOCGenerator.createTOCFile(baseDirectory + tocDirectory);
        final TreeNode<?> rootOffline = new TreeNode(new TOCItem(ROOT_NODE_NAME, ""));
        try {
            TOCGenerator.convertXMLMappings(tocXMLFiles, rootOffline);
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, String.format("There was an error creating the documentation file %s "
                    + "- Documentation may not be complete.", baseDirectory + tocDirectory), ex);
        }
    }

    /**
     * get the directory that the table of contents is saved to
     *
     * @return a String path for the file location
     */
    public static String getTOCDirectory() {
        tocDirectory = String.format("ext%1$s%2$s", File.separator, TOC_FILE_NAME);
        return tocDirectory;
    }

    /**
     * get a list of the xml files using a lookup
     *
     * @param baseDirectory
     * @return
     */
    protected static List<File> getXMLFiles(final String baseDirectory) {
        // Loop all providers and add files to the tocXMLFiles list
        final List<File> tocXMLFiles = new ArrayList<>();
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            final String providerTOC = provider.getHelpTOC();
            if (StringUtils.isNotEmpty(providerTOC)) {
                tocXMLFiles.add(new File(baseDirectory + providerTOC));
            }
        });
        return tocXMLFiles;
    }

    /**
     * Get the base directory of the current project
     *
     * @return
     */
    public static String getBaseDirectory() {
        try {
            final String sep = File.separator;

            // Get the current directory and make the file within the base project directory.
            final String userDir = getResource();
            String[] splitUserDir = userDir.split(Pattern.quote(sep));
            while (!"ext".equals(splitUserDir[splitUserDir.length - 1])) {
                splitUserDir = Arrays.copyOfRange(splitUserDir, 0, splitUserDir.length - 1);
            }
            // split once more
            splitUserDir = Arrays.copyOfRange(splitUserDir, 0, splitUserDir.length - 1);

            baseDirectory = String.join(sep, splitUserDir) + sep;
        } catch (final IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "There was a problem retrieving the base directory for launching Offline Help.", ex);
        }
        return baseDirectory;
    }

    protected static String getResource() throws IllegalArgumentException {
        final URL sourceLocation = Generator.class.getProtectionDomain().getCodeSource().getLocation();
        final String pathLoc = sourceLocation.getPath();
        final URI uri = URI.create(pathLoc);
        final Path path = Paths.get(uri);
        final int jarIx = path.toString().lastIndexOf(File.separator);
        final String newPath = jarIx > -1 ? path.toString().substring(0, jarIx) : "";
        return newPath != null ? newPath + File.separator + "ext" : "";
    }
    
    protected static String getOnlineHelpTOCDirectory(final String filePath) {
        final int index = filePath.indexOf("constellation" + File.separator + "modules");
        if (index <= 0) {
            return filePath;
        } else {
            final String newPath = filePath.substring(0, index + 14);
            return newPath + TOC_FILE_NAME;
        }
    }

}
