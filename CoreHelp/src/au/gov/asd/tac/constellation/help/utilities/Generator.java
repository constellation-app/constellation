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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public static String baseDirectory = "";
    public static String tocDirectory = "";

    @Override
    public void run() {

        // DO NOT RUN IN EXECUTABLE, ONLY RUN IN IDE
        // TODO: Double check if this is necessary and also foolproof for other module suites.
        if (!"IDE(CORE)".equals(System.getProperty("constellation.environment"))) {
            return;
        }
        final String sep = File.separator;
        // Get the current directory and make the file within the help module.
        String userDir = System.getProperty("user.dir");
        String pattern = Pattern.quote(sep);
        String[] splitUserDir = userDir.split(pattern);
        while (!splitUserDir[splitUserDir.length - 1].contains("constellation")) {
            splitUserDir = Arrays.copyOfRange(splitUserDir, 0, splitUserDir.length - 1);
        }
        // split once more
        splitUserDir = Arrays.copyOfRange(splitUserDir, 0, splitUserDir.length - 1);
        tocDirectory = "constellation" + sep + "toc.md";
        String tocPath = String.join(sep, splitUserDir) + sep + tocDirectory;
        baseDirectory = String.join(sep, splitUserDir) + sep;

        // Create TOCGenerator with the location of the resources file
        final TOCGenerator tocGenerator = new TOCGenerator(tocPath);

        // Create the root node for application-wide table of contents
        final TreeNode root = new TreeNode(new TOCItem("Help Pages", ""));

        // Loop all providers and add files to the tocXMLFiles list
        final List<File> tocXMLFiles = new ArrayList<>();
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            if (StringUtils.isNotEmpty(provider.getHelpTOC())) {
                tocXMLFiles.add(new File(baseDirectory + provider.getHelpTOC()));
            }
        });

        // Generate Application-wide TOC based on each modules TOC
        // Stores it within the root TreeNode
        tocGenerator.convertXMLMappings(tocXMLFiles, root);
    }

}
