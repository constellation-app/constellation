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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.WorkflowQueryPlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that tests the various types of parameters (including
 * the global parameters).
 * <p>
 * Each of the parameter types should have a matching GUI input.
 *
 * @author mimosa
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)
})
@Messages("TestParametersBatched=Test Parameters (Batched)")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.DEVELOPER, PluginTags.MODIFY})
public class TestParametersBatched extends WorkflowQueryPlugin implements DataAccessPlugin {

    @Override
    public String getType() {
        return DataAccessPluginCoreType.DEVELOPER;
    }

    @Override
    public int getPosition() {
        return Integer.MAX_VALUE - 10;
    }

    @Override
    public String getDescription() {
        return "Test the various input UIs";
    }

    @Override
    public List<String> getWorkflow() {
        final List<String> batchPlugin = new ArrayList<>();
        batchPlugin.add("TestParameters");
        return batchPlugin;
    }

    @Override
    public String getErrorHandlingPlugin() {
        return "SelectAll";
    }
}
