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
package au.gov.asd.tac.constellation.plugins.importexport.text;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * The ExportStringPlugin simply saves a given String of text to a given file.
 * Both are provided as plugin parameters.
 * <p>
 * This exists as a plugin to make saving a string to a text file auditable.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@Messages("ExportToTextPlugin=Export to Text")
public class ExportToTextPlugin extends SimplePlugin {
    
    private static final Logger LOGGER = Logger.getLogger(ExportToTextPlugin.class.getName());

    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ExportToTextPlugin.class, "filename");
    public static final String TEXT_PARAMETER_ID = PluginParameter.buildId(ExportToTextPlugin.class, "text");

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String fileName = parameters.getParameters().get(FILE_NAME_PARAMETER_ID).getStringValue();
        final String text = parameters.getParameters().get(TEXT_PARAMETER_ID).getStringValue();

        final File file = new File(fileName);
        try {
            try (PrintWriter os = new PrintWriter(file, StandardCharsets.UTF_8.name())) {
                os.append(text);
            } catch (final UnsupportedEncodingException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } catch (final FileNotFoundException ex) {
            throw new PluginException(PluginNotificationLevel.ERROR, ex);
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> fileParam = StringParameterType.build(FILE_NAME_PARAMETER_ID);
        fileParam.setName("File Name");
        fileParam.setDescription("The file name");
        parameters.addParameter(fileParam);

        final PluginParameter<StringParameterValue> textParam = StringParameterType.build(TEXT_PARAMETER_ID);
        textParam.setName("Text");
        textParam.setDescription("The text to save to a file");
        parameters.addParameter(textParam);

        return parameters;
    }
}
