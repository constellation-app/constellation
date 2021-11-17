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
package au.gov.asd.tac.constellation.functionality.admin;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * DevOps Notification
 * <p>
 * If you have a {@code ConstellationLogger} configured you can use reference
 * this plugin when you need to notify your DevOps (Developers and
 * Administrators) about something.
 * <pre>
 * ConstellationLogger.getDefault().pluginError(PluginRegistry.get(CorePluginRegistry.DEV_OPS_NOTIFICATION), new Exception("Something went wrong, missing a thing"));
 * </pre>
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages({"DevOpsNotificationPlugin=DevOps Notification"})
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.DEVELOPER})
public class DevOpsNotificationPlugin extends SimplePlugin {

    @Override
    public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        // do nothing
    }

}
