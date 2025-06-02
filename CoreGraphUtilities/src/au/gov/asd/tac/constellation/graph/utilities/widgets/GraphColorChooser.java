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
package au.gov.asd.tac.constellation.graph.utilities.widgets;

import javax.swing.JColorChooser;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorChooserComponentFactory;

/**
 * A JColorChooser that adds a NamedColorPanel to the color selection panels.
 *
 * @author algol
 */
public class GraphColorChooser extends JColorChooser {

    public GraphColorChooser() {
        // Add our named color panel by getting the existing panels and putting ours in front.
        final AbstractColorChooserPanel[] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
        final AbstractColorChooserPanel[] morePanels = new AbstractColorChooserPanel[panels.length + 1];
        morePanels[0] = new NamedColorPanel();
        System.arraycopy(panels, 0, morePanels, 1, panels.length);

        setChooserPanels(morePanels);
    }
}
