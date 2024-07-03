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
package au.gov.asd.tac.constellation.functionality.support;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.support.SupportHandler;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default help message if no {@link SupportHandler} is found.
 *
 * @author arcturus
 */
@ServiceProvider(service = SupportHandler.class, position = 1000)
public class DefaultSupportAction implements SupportHandler {

    private static final String GITHUB_URL = "https://github.com/constellation-app/constellation/issues";

    @Override
    public void supportAction() {
        PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
                .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Open Github Repository")
                .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, GITHUB_URL)
                .executeLater(null);
    }
}
