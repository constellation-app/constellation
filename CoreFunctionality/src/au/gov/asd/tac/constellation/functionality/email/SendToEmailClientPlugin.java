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
package au.gov.asd.tac.constellation.functionality.email;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Send To Email Client Plugin
 *
 * @author arcturus
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.UTILITY})
@NbBundle.Messages("SendToEmailClientPlugin=Send To Email Client")
public class SendToEmailClientPlugin extends SimplePlugin {
    
    private static final Logger LOGGER = Logger.getLogger(SendToEmailClientPlugin.class.getName());

    public static final String TO_EMAIL_PARAMETER_ID = PluginParameter.buildId(SendToEmailClientPlugin.class, "to_email");
    public static final String CC_EMAIL_PARAMETER_ID = PluginParameter.buildId(SendToEmailClientPlugin.class, "cc_email");
    public static final String SUBJECT_PARAMETER_ID = PluginParameter.buildId(SendToEmailClientPlugin.class, "subject");
    public static final String BODY_PARAMETER_ID = PluginParameter.buildId(SendToEmailClientPlugin.class, "body");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> toEmailParameter = StringParameterType.build(TO_EMAIL_PARAMETER_ID);
        toEmailParameter.setName("To Email");
        toEmailParameter.setDescription("The to email recipient");
        parameters.addParameter(toEmailParameter);

        final PluginParameter<StringParameterValue> ccEmailParameter = StringParameterType.build(CC_EMAIL_PARAMETER_ID);
        ccEmailParameter.setName("CC Email");
        ccEmailParameter.setDescription("The cc email recipient");
        parameters.addParameter(ccEmailParameter);

        final PluginParameter<StringParameterValue> subjectParameter = StringParameterType.build(SUBJECT_PARAMETER_ID);
        subjectParameter.setName("Subject");
        subjectParameter.setDescription("The subject for the email");
        parameters.addParameter(subjectParameter);

        final PluginParameter<StringParameterValue> bodyParameter = StringParameterType.build(BODY_PARAMETER_ID);
        bodyParameter.setName("Body");
        bodyParameter.setDescription("The body of the email");
        parameters.addParameter(bodyParameter);

        return parameters;
    }

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String toEmail = parameters.getStringValue(TO_EMAIL_PARAMETER_ID);
        final String ccEmail = parameters.getStringValue(CC_EMAIL_PARAMETER_ID);
        final String subject = parameters.getStringValue(SUBJECT_PARAMETER_ID);
        final String body = parameters.getStringValue(BODY_PARAMETER_ID);

        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("mailto:")
                .append(toEmail)
                .append("?subject=")
                .append(encodeString(subject))
                .append("&cc=")
                .append(encodeString(ccEmail))
                .append("&body=")
                .append(encodeString(body));

            final URI uri = new URI(sb.toString());
            Desktop.getDesktop().mail(uri);
        } catch (final IOException | URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            NotifyDisplayer.display("Could not send email\n" + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    private String encodeString(final String content) throws UnsupportedEncodingException {
        return content != null ? URLEncoder.encode(content, StandardCharsets.UTF_8.name()).replace("+", "%20") : "";
    }

}
