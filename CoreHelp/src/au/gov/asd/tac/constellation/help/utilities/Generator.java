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
import org.openide.modules.Places;
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
    private static String onlineTocDirectory = "";
    public static final String TOC_FILE_NAME = "toc.md";
    public static final String ROOT_NODE_NAME = "Constellation Documentation";

    /**
     * This is the system property that is set to true in order to make the AWT
     * thread run in headless mode for tests, etc.
     */
    private static final String AWT_HEADLESS_PROPERTY = "java.awt.headless";

    /**
     * Generate a table of contents in dev versions of code
     */
    @Override
    public void run() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(System.getProperty(AWT_HEADLESS_PROPERTY))) {
            return;
        }
        baseDirectory = getBaseDirectory();
        
        // To update the online help TOC file change the boolean to true
        // Must also run adaptors when updating online help so those links aren't removed from the TOC
        // Reset back to false after updating the TOC file 
        final boolean updateOnlineHelp = false;
 
        if (updateOnlineHelp) {
            onlineTocDirectory = getOnlineHelpTOCDirectory(baseDirectory) + TOC_FILE_NAME;

            // First: create the TOCFile in the base directory for ONLINE help
            // Create the online root node for application-wide table of contents
            TOCGenerator.createTOCFile(onlineTocDirectory);
            final TreeNode<TOCItem> root = new TreeNode<>(new TOCItem(ROOT_NODE_NAME, ""));
            final List<File> tocXMLFiles = getXMLFiles(baseDirectory);
            try {
                TOCGenerator.convertXMLMappings(tocXMLFiles, root);
            } catch (final IOException ex) {
                LOGGER.log(Level.WARNING, String.format("There was an error creating the documentation file %s "
                        + "- Documentation may not be complete", baseDirectory), ex);
            }
        }

        // Second: Create TOCFile for OFFLINE help with the location of the resources file
        // Create the offline root node for application-wide table of contents
        TOCGenerator.createTOCFile(getTOCDirectory());
        final TreeNode<TOCItem> rootOffline = new TreeNode<>(new TOCItem(ROOT_NODE_NAME, ""));
        final List<File> tocXMLFiles = getXMLFiles(baseDirectory);
        try {
            TOCGenerator.convertXMLMappings(tocXMLFiles, rootOffline);
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, String.format("There was an error creating the documentation file %s "
                    + "- Documentation may not be complete.", getTOCDirectory()), ex);
        }
    }

    /**
     * Get the directory that the table of contents is saved to
     *
     * @return a String path for the file location
     */
    public static String getTOCDirectory() {
        return Places.getUserDirectory() + File.separator + TOC_FILE_NAME;
    }

    /**
     * Get a list of the xml files using a lookup
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
    
    public static String getOnlineHelpTOCDirectory(final String filePath) {
        // include "modules" in the check, because looking for "constellation" alone can match earlier in the path
        // ie. /home/constellation/test/rc1/constellation/modules/ext/
        int index = filePath.indexOf("constellation" + File.separator + "modules");
        if (index <= 0) {
            index = filePath.indexOf("constellation" + File.separator);
        }
        return index <= 0 ? filePath : filePath.substring(0, index + 14);
    }

}
