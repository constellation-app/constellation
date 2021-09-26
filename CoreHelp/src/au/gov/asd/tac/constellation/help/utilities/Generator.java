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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import au.gov.asd.tac.constellation.help.utilities.toc.TOCGenerator;
import au.gov.asd.tac.constellation.help.utilities.toc.TOCItem;
import au.gov.asd.tac.constellation.help.utilities.toc.TreeNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;
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
    public static String baseDirectory = "";
    public static String tocDirectory = "";
    public static final String TOC_FILE_NAME = "toc.md";
    public static final String ROOT_NODE_NAME = "Constellation Documentation";

    // TODO: Remove this flag and all occurrences when in prod/dev.
    // Toggled to allow switching between dev and release versions
    private static final boolean DEBUG_FLAG = false;

    public Generator() {
        // Intentionally left blank
    }

    /**
     * Generate a table of contents in dev versions of code
     */
    @Override
    public void run() {

        // TODO: Possibly check if file exists?
        baseDirectory = getBaseDirectory();
        tocDirectory = String.format("constellation%1$s%2$s", File.separator, TOC_FILE_NAME);

        // TODO: Double check if this is necessary and also foolproof for other module suites.
        if (!"IDE(CORE)".equals(System.getProperty("constellation.environment")) || DEBUG_FLAG) {
            // Ensure that the mappings are generated for clicks on help icons within the application.
            HelpMapper.updateMappings();
            return;
        }

        // Create TOCGenerator with the location of the resources file
        // Create the root node for application-wide table of contents
        final TOCGenerator tocGenerator = new TOCGenerator(baseDirectory + tocDirectory);
        final TreeNode root = new TreeNode(new TOCItem(ROOT_NODE_NAME, ""));
        final List<File> tocXMLFiles = getXMLFiles(baseDirectory);

        try {
            tocGenerator.convertXMLMappings(tocXMLFiles, root);
        } catch (final IOException ex) {
            LOGGER.log(Level.WARNING, "There was an error creating the documentation file {0} - Documentation may not be complete", baseDirectory + tocDirectory);
            Exceptions.printStackTrace(ex);
        }
    }

    protected static List<File> getXMLFiles(final String baseDirectory) {
        // Loop all providers and add files to the tocXMLFiles list
        final List<File> tocXMLFiles = new ArrayList<>();
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            if (StringUtils.isNotEmpty(provider.getHelpTOC())) {
                tocXMLFiles.add(new File(baseDirectory + provider.getHelpTOC()));
            }
        });
        return tocXMLFiles;
    }

    protected static String getBaseDirectory() {
        final String sep = File.separator;
        // Get the current directory and make the file within the base project directory.
        final String userDir = System.getProperty("user.dir");
        String[] splitUserDir = userDir.split(Pattern.quote(sep));
        while (!splitUserDir[splitUserDir.length - 1].contains("constellation")) {
            splitUserDir = Arrays.copyOfRange(splitUserDir, 0, splitUserDir.length - 1);
        }
        // split once more
        splitUserDir = Arrays.copyOfRange(splitUserDir, 0, splitUserDir.length - 1);

        return String.join(sep, splitUserDir) + sep;
    }

}
