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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.GlobalParameters;
import java.util.Collections;
import java.util.Set;
import javafx.scene.control.TitledPane;

/**
 * A pane containing the global parameters.
 *
 * @author cygnus_x-1
 * @author ruby_crucis
 */
public class GlobalParametersPane extends TitledPane {

    private final PluginParameters params;

    public GlobalParametersPane(final PluginParameters presetParams) {
        setText("Global Parameters");
        setExpanded(true);
        setCollapsible(false);
        getStyleClass().add("titled-pane-heading");

        params = GlobalParameters.getParameters(presetParams);

        setContent(PluginParametersPane.buildPane(params, null, null));
    }

    /**
     * The global parameters.
     *
     * @return The global parameters.
     */
    public PluginParameters getParams() {
        return params;
    }

    /**
     * The labels of the global parameters.
     *
     * @return The labels of the global parameters.
     */
    public Set<String> getParamLabels() {
        return Collections.unmodifiableSet(params.getParameters().keySet());
    }
}
