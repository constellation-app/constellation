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
package au.gov.asd.tac.constellation.views.welcome;

import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.welcome.plugins.AddModeWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.DataAccessViewWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.DelimitedFileWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.GettingStartedWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.JDBCImportWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.JoinCommWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.OpenGraphWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.ProvideFeedbackWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.SelectionModeWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.SphereGraphWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.WhatsNewWelcomePlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates 2 lists of the plugins to be added to the welcome page
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = WelcomePageLayoutProvider.class, position = 1000)
@PluginInfo(tags = {PluginTags.WELCOME})
@NbBundle.Messages("WelcomePluginList=Welcome Plugin List")

public class WelcomePluginList extends WelcomePageLayoutProvider {

    /**
     * Gets the plugins for the top part of the welcome page
     *
     * @return a list of plugins
     */
    @Override
    public List<WelcomePluginInterface> getTopPlugins() {
        final List<WelcomePluginInterface> topPlugins = new ArrayList<>();
        topPlugins.add(new AddModeWelcomePlugin());
        topPlugins.add(new SelectionModeWelcomePlugin());
        topPlugins.add(new SphereGraphWelcomePlugin());
        topPlugins.add(new OpenGraphWelcomePlugin());
        topPlugins.add(new DataAccessViewWelcomePlugin());
        topPlugins.add(new DelimitedFileWelcomePlugin());
        topPlugins.add(new JDBCImportWelcomePlugin());

        return topPlugins;
    }

    /**
     * Gets the plugins for the side of the welcome page
     *
     * @return a list of plugins
     */
    @Override
    public List<WelcomePluginInterface> getSidePlugins() {
        final List<WelcomePluginInterface> sidePlugins = new ArrayList<>();
        sidePlugins.add(new GettingStartedWelcomePlugin());
        sidePlugins.add(new WhatsNewWelcomePlugin());
        sidePlugins.add(new ProvideFeedbackWelcomePlugin());
        sidePlugins.add(new JoinCommWelcomePlugin());

        return sidePlugins;
    }

}
