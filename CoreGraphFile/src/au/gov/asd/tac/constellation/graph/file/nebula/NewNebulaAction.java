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
package au.gov.asd.tac.constellation.graph.file.nebula;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.FileChooser.ExtensionFilter;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "File", id = "au.gov.asd.tac.constellation.graph.file.nebula.NewNebulaAction")
@ActionRegistration(displayName = "#CTL_NewNebulaAction", iconBase = "au/gov/asd/tac/constellation/graph/file/nebula/resources/nebula.png")
@ActionReference(path = "Menu/Experimental/Tools", position = 0)
@Messages("CTL_NewNebulaAction=New Nebula")
public final class NewNebulaAction implements ActionListener {
    
    private static final Logger LOGGER = Logger.getLogger(NewNebulaAction.class.getName());

    public static final String NEBULA_FILE_PARAMETER_ID = PluginParameter.buildId(NewNebulaAction.class, "nebula_file");
    public static final String COLOR_PARAMETER_ID = PluginParameter.buildId(NewNebulaAction.class, "color");

    @Override
    public void actionPerformed(final ActionEvent e) {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<FileParameterValue> fileParam = FileParameterType.build(NEBULA_FILE_PARAMETER_ID);
        fileParam.setName("Nebula file");
        FileParameterType.setFileFilters(fileParam, new ExtensionFilter("Nebula file", "*.nebula"));
        fileParam.getParameterValue().setKind(FileParameterKind.SAVE);
        fileParam.setHelpID("au.gov.asd.tac.constellation.file.nebula");
        params.addParameter(fileParam);

        final PluginParameter<ColorParameterValue> colorParam = ColorParameterType.build(COLOR_PARAMETER_ID);
        colorParam.setName("Nebula colour");
        params.addParameter(colorParam);

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_NewNebulaAction(), params);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            final FileParameterValue fpv = fileParam.getParameterValue();
            if (!fpv.get().isEmpty()) {
                final Properties props = new Properties();
                final ConstellationColor c = colorParam.getColorValue();
                props.setProperty("colour", String.format("%f,%f,%f", c.getRed(), c.getGreen(), c.getBlue()));

                File f = fpv.get().get(0);
                if (!StringUtils.endsWithIgnoreCase(f.getName(), FileExtensionConstants.NEBULA)) {
                    f = new File(f.getAbsoluteFile() + FileExtensionConstants.NEBULA);
                }

                try {
                    try (final FileOutputStream fos = new FileOutputStream(f)) {
                        props.store(fos, null);
                        NebulaDataObject.addRecent(f);
                    }
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
            }
        }
    }
}
