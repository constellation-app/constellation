/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.analyticview.translators.AbstractHideTranslator;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

/**
 *
 * @author cygnus_x-1
 * @param <C>
 */
public class HideVisualisation<C> extends GraphVisualisation {

    private final AbstractHideTranslator<? extends AnalyticResult<?>, C> translator;
    private final HBox hidePanel;
    private final Slider hideSlider;
    private final ToggleButton hideButton;
    private boolean activated = false;

    public HideVisualisation(final AbstractHideTranslator<? extends AnalyticResult<?>, C> translator) {
        this.translator = translator;

        this.hideSlider = new Slider(0.0, 1.0, 0.0);
        hideSlider.setId("hide-visualisation-slider");
        hideSlider.setDisable(true);
        hideSlider.valueProperty().addListener((observable, oldValue, newValue) -> translator.executePlugin(false, newValue.floatValue()));

        this.hideButton = new ToggleButton("Hide");
        hideButton.setId("hide-visualisation-button");
        hideButton.setOnAction(event -> {
            activated = hideButton.isSelected();
            final float threshold = (float) hideSlider.getValue();
            translator.executePlugin(!activated, threshold);
            hideSlider.setDisable(!activated);
            AnalyticViewController.getDefault().updateGraphVisualisations(this, activated);
        });

        this.hidePanel = new HBox(5.0, hideButton, hideSlider);
    }

    @Override
    public void deactivate() {
        if (activated) {
            translator.executePlugin(activated, 0);
            activated = !activated;
        }
    }

    @Override
    public String getName() {
        return "Hide Elements";
    }

    @Override
    public Node getVisualisation() {
        return hidePanel;
    }

    @Override
    public List<SchemaAttribute> getAffectedAttributes() {
        return Arrays.asList(
                VisualConcept.VertexAttribute.VISIBILITY,
                VisualConcept.TransactionAttribute.VISIBILITY);
    }

    @Override
    public boolean isActive() {
        return activated;
    }

    @Override
    public void setSelected(final boolean selected) {
        hideButton.setSelected(selected);
        hideSlider.setDisable(selected);
        activated = selected;
    }
    
    @Override 
    public boolean equals(final Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        } else if (this == object || getClass() == object.getClass()) {
            return true;
        }
        
        final HideVisualisation newObject = (HideVisualisation) object;
        return translator == newObject.translator && hideButton == newObject.hideButton && activated == newObject.activated;
    }
    
    @Override 
    public int hashCode() {
        return Objects.hash(hideButton.getClass());
    }
}
