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
import java.io.File;
import java.util.Map;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.OnShowing;

/**
 * Generates help file mappings
 *
 * @author aldebaran30701
 */
@OnShowing()
public class Generator implements Runnable {

    @Override
    public void run() {
        var prefs = NbPreferences.forModule(Generator.class);
        prefs.put("onlineHelp", "true");

        // Get mappings of help pages
        final Map<String, String> mappings = HelpMapper.getMappings();

        // Get the current directory and make the file within the help module.
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final String tocPath = userDir + sep + ".." + sep + "CoreHelp" + sep + "src" + sep + "au" + sep
                + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "help" + sep + "toc.md";

        // Create TOCGenerator with the location of the resources file
        final TOCGenerator tocGenerator = new TOCGenerator(tocPath);

        // Loop all providers and generate Application-wide TOC based on each modules TOC
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            final File tocXMLFile = new File(provider.getHelpTOC());
            tocGenerator.convertXMLMappings(tocXMLFile);
        });

        // Generate .md file from hardcoded writes
        // TODO: Remove this test code and uncomment below.
        //tocGenerator.convertXMLMappingsTEST();
        // Uncomment this when implemented
        // address must locate the *-toc.xml file for each module
        // file must be of that address
        // tocGenerator will read that file and generate a markdown represenation of it
        //final String exampleXMLFileAddress = ".\\CoreAnalyticView\\src\\au\\gov\\asd\\tac\\constellation\\views\\analyticview\\docs\\analyticview-toc.xml";
        //final File exampleXMLFile = new File(exampleXMLFileAddress);
        //tocGenerator.convertXMLMappings(exampleXMLFile);
    }

}
