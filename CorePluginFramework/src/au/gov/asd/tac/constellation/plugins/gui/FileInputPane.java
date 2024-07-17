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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.ENABLED;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VALUE;
import static au.gov.asd.tac.constellation.plugins.parameters.ParameterChange.VISIBLE;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.FileInput;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.TextType;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javafx.application.Platform;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputListener;


/**
 * A text-box and file chooser that together allows the selection or manual
 * entry of a number files, which is the GUI element corresponding to a
 * {@link PluginParameter} of
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType}.
 * <p>
 * Entering file names manually or making a selection with the file chooser will
 * update the object value of the underlying {@link PluginParameter}.
 *
 * @see au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType
 *
 * @author ruby_crucis
 * @author capricornunicorn123
 */
public final class FileInputPane extends ParameterInputPane<FileParameterValue, List<File>> {

    public static final File DEFAULT_DIRECTORY = new File(System.getProperty("user.home"));
    private static final Logger LOGGER = Logger.getLogger(FileInputPane.class.getName());

    public FileInputPane(final PluginParameter<FileParameterValue> parameter) {
        this(parameter, 1);
    }
    
    /**
     * Primary constructor
     *
     * @param parameter parameter to link to value
     * @param suggestedHeight suggested hight (in lines)
     */
    public FileInputPane(final PluginParameter<FileParameterValue> parameter, int suggestedHeight) {
        super(suggestedHeight > 1 ? new FileInput(parameter.getParameterValue().getKind(), TextType.MULTILINE, suggestedHeight) : new FileInput(parameter.getParameterValue().getKind()), parameter); 

        final FileParameterType.FileParameterValue pv = parameter.getParameterValue();
        
        ((FileInput) input).setFileFilter(FileParameterType.getFileFilters(parameter));
        ((FileInput) input).setAcceptAll(FileParameterType.isAcceptAllFileFilterUsed(parameter));
        
        setFieldValue(pv.get());
    }

    @Override
    public ConstellationInputListener getFieldChangeListener(PluginParameter<FileParameterValue> parameter) {
        return (ConstellationInputListener<List<File>>) (List<File> newValue) -> {
            parameter.setStringValue(input.getText());
        };
    }

    @Override
    public PluginParameterListener getPluginParameterListener() {
        return (PluginParameter<?> pluginParameter, ParameterChange change) -> Platform.runLater(() -> {
            switch (change) {
                case VALUE -> {

                    // Do not retrigger the fieled listner if this event was triggered by the input listner.
                    final String param = pluginParameter.getStringValue();
                    if (!input.getText().equals(param)) {
                        input.setText(param);
                    }
                }
                case PROPERTY -> {
                    ((FileInput) input).setFileFilter(FileParameterType.getFileFilters(parameter));
                }
                case ENABLED -> updateFieldEnablement();
                case VISIBLE -> updateFieldVisability();
            }
        });
    }
}
