/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Help pages for the AI Assistant view.
 *
 * @author constellation
 */
@ServiceProvider(service = HelpPageProvider.class, position = 3100)
@NbBundle.Messages("AgentViewHelpProvider=AI Assistant View Help Provider")
public class AgentViewHelpProvider extends HelpPageProvider {

    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreAiAgentView" + SEP;

    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("au.gov.asd.tac.constellation.views.aiagent.ui.AgentViewTopComponent", MODULE_PATH + "ai-assistant-view.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "ai-assistant-toc.xml";
    }
}
