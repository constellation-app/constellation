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

import java.util.Map;
import org.openide.windows.OnShowing;

/**
 * Generates help file mappings
 *
 * @author aldebaran30701
 */
@OnShowing()
public class Generator implements Runnable {

    private static final String TOC_FILE_PATH = "src\\au\\gov\\asd\\tac\\constellation\\help\\toc.md";

    @Override
    public void run() {
        // Get mappings of help pages
        final Map<String, String> mappings = HelpMapper.getMappings();

        // Create TOCGenerator with the location of the resources file
        final TOCGenerator tocGenerator = new TOCGenerator(TOC_FILE_PATH);

        // Generate .md file from hardcoded writes
        // TODO: Remove this test code and uncomment below.
        tocGenerator.convertXMLMappingsTEST();

        // Uncomment this when implemented
        // address must locate the *-toc.xml file for each module
        // file must be of that address
        // tocGenerator will read that file and generate a markdown represenation of it
        //final String exampleXMLFileAddress = ".\\CoreAnalyticView\\src\\au\\gov\\asd\\tac\\constellation\\views\\analyticview\\docs\\analyticview-toc.xml";
        //final File exampleXMLFile = new File(exampleXMLFileAddress);
        //tocGenerator.convertXMLMappings(exampleXMLFile);
    }

}
