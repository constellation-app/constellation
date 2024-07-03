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
package au.gov.asd.tac.constellation.views.analyticview.visualisation;

import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewController;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.translators.AbstractColorTranslator;
import au.gov.asd.tac.constellation.views.analyticview.translators.AnalyticTranslator;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author cygnus_x-1
 * @param <C>
 */
public class ColorVisualisation<C> extends GraphVisualisation {

    private final AbstractColorTranslator<? extends AnalyticResult<?>, C> translator;
    private final ToggleButton colorButton;
    private boolean activated = false;

    public ColorVisualisation(final AbstractColorTranslator<? extends AnalyticResult<?>, C> translator) {
        this.translator = translator;

        this.colorButton = new ToggleButton("Color");
        colorButton.setId("color-visualisation-button");
        colorButton.setOnAction(event -> {
            activated = colorButton.isSelected();
            this.translator.executePlugin(!activated);
            AnalyticViewController.getDefault().updateGraphVisualisations(this, activated);
            AnalyticViewController.getDefault().writeState();
        });
    }

    @Override
    public void deactivate(final boolean reset) {
        if (reset) {
            translator.executePlugin(reset);
            activated = !reset;
            colorButton.setSelected(activated);
            AnalyticViewController.getDefault().updateGraphVisualisations(this, activated);
        }
    }

    @Override
    public String getName() {
        return "Color Elements";
    }

    @Override
    public Node getVisualisation() {
        return colorButton;
    }
    
    @Override 
    public AnalyticTranslator getTranslator() {
        return translator;
    }

    @Override
    public List<SchemaAttribute> getAffectedAttributes() {
        return Arrays.asList(
                VisualConcept.VertexAttribute.OVERLAY_COLOR,
                VisualConcept.TransactionAttribute.OVERLAY_COLOR,
                VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE,
                VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE);
    }

    @Override
    public boolean isActive() {
        return activated;
    }

    @Override
    public void setSelected(final boolean selected) {
        colorButton.setSelected(selected);
        activated = selected;
    }
    
    @Override 
    public boolean equals(final Object object) {
        return (object != null && getClass() == object.getClass());
    }
    
    @Override 
    public int hashCode() {
        return Objects.hash(colorButton.getClass());
    }
}
