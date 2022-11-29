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
package au.gov.asd.tac.constellation.webserver;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.functionality.browser.OpenInBrowserPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.webserver.DisplayRestDocAction")
@ActionRegistration(displayName = "#CTL_DisplayRestDocAction",
        iconBase = "au/gov/asd/tac/constellation/webserver/resources/displayRESTServerDocumentation.png")
@ActionReference(path = "Menu/Tools", position = 1600)
@Messages("CTL_DisplayRestDocAction=Display REST Server Documentation")
public final class DisplayRestDocAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        final int port = WebServer.start();
        final String url = String.format("http://localhost:%d/swagger-ui/index.html", port);
        PluginExecution.withPlugin(CorePluginRegistry.OPEN_IN_BROWSER)
                .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Rest API Swagger Documentation")
                .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, url)
                .executeLater(null);
    }
}
