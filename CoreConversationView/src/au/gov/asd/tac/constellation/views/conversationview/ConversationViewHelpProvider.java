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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the conversation view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 900)
@NbBundle.Messages("ConversationViewHelpProvider=Conversation View Help Provider")
public class ConversationViewHelpProvider extends HelpPageProvider {

    private static final String CODEBASE_NAME = "constellation";
    private static final String SEP = File.separator;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String conversationModulePath = ".." + SEP + "ext" + SEP + "docs" + SEP + "CoreConversationView" + SEP + "src" + SEP + "au" + SEP + "gov"
                + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "views" + SEP + "conversationview" + SEP;

        map.put("au.gov.asd.tac.constellation.views.conversationview.ConversationBox", conversationModulePath + "conversation-view.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        final String conversationViewPath;
        conversationViewPath = "ext" + SEP + "docs" + SEP + "CoreConversationView" + SEP + "src" + SEP + "au" + SEP + "gov" + SEP + "asd" + SEP
                + "tac" + SEP + CODEBASE_NAME + SEP + "views" + SEP + "conversationview" + SEP + "conversationview-toc.xml";
        return conversationViewPath;
    }
}
