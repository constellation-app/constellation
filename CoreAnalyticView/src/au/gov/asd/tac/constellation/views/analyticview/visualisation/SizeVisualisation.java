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
import au.gov.asd.tac.constellation.views.analyticview.translators.AbstractSizeTranslator;
import au.gov.asd.tac.constellation.views.analyticview.translators.AnalyticTranslator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;

/**
 *
 * @author cygnus_x-1
 * @param <C>
 */
public class SizeVisualisation<C> extends GraphVisualisation {

    private final AbstractSizeTranslator<? extends AnalyticResult<?>, C> translator;
    private final ToggleButton sizeButton;
    private boolean activated = false;
    
    // Maps of the sizes of the vertices and transactions before the plugin is run
    private Map<Integer, Float> vertexSizes = new HashMap<>();
    private Map<Integer, Float> transactionSizes = new HashMap<>();

    public SizeVisualisation(final AbstractSizeTranslator<? extends AnalyticResult<?>, C> translator) {
        this.translator = translator;

        this.sizeButton = new ToggleButton("Size");
        sizeButton.setId("size-visualisation-button");
        sizeButton.setOnAction(event -> {
            activated = sizeButton.isSelected(); 
            this.translator.setVertexSizes(vertexSizes);
            this.translator.setTransactionSizes(transactionSizes);
            this.translator.executePlugin(!activated);
            vertexSizes = this.translator.getVertexSizes();
            transactionSizes = this.translator.getVertexSizes();
            AnalyticViewController.getDefault().updateGraphVisualisations(this, activated);
            AnalyticViewController.getDefault().writeState();
        });
    }

    @Override
    public void deactivate() {
        if (activated) {
            translator.setVertexSizes(vertexSizes);
            translator.setTransactionSizes(transactionSizes);
            translator.executePlugin(activated);
            activated = !activated;
            vertexSizes = translator.getVertexSizes();
            transactionSizes = translator.getVertexSizes();
            AnalyticViewController.getDefault().updateGraphVisualisations(this, activated);
            AnalyticViewController.getDefault().writeState();
        }
    }

    @Override
    public String getName() {
        return "Size Elements";
    }

    @Override
    public Node getVisualisation() {
        return sizeButton;
    }
    
    @Override 
    public AnalyticTranslator getTranslator() {
        return translator;
    }

    @Override
    public List<SchemaAttribute> getAffectedAttributes() {
        return Arrays.asList(
                VisualConcept.VertexAttribute.NODE_RADIUS,
                VisualConcept.TransactionAttribute.WIDTH);
    }

    @Override
    public boolean isActive() {
        return activated;
    }

    @Override
    public void setSelected(final boolean selected) {
        sizeButton.setSelected(selected);
        activated = selected;
    }
    
    @Override 
    public boolean equals(final Object object) {
        return (object != null && getClass() == object.getClass());
    }
    
    @Override 
    public int hashCode() {
        return Objects.hash(sizeButton.getClass());
    }
}
