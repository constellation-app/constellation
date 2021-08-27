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

import org.openide.windows.OnShowing;

/**
 * Generates help file mappings
 *
 * @author aldebaran30701
 */
@OnShowing()
public class Generator implements Runnable {

    private static final String TOC_FILE_PATH = "./resources/toc.md";

    @Override
    public void run() {
        // Get mappings of help pages
        var mappings = HelpMapper.getMappings();

        // Generate TOC dymanically from mappings
        final TOCGenerator tocGenerator = new TOCGenerator(TOC_FILE_PATH);

        //generateTableOfContents(mappings);
    }

}
