/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.translators.AbstractSizeTranslator;
import java.util.Arrays;
import java.util.List;
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

    public SizeVisualisation(final AbstractSizeTranslator<? extends AnalyticResult<?>, C> translator) {
        this.translator = translator;

        this.sizeButton = new ToggleButton("Size");
        sizeButton.setId("size-visualisation-button");
        sizeButton.setOnAction(event -> {
            final boolean reset = !sizeButton.isSelected();
            translator.executePlugin(reset);
        });
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
    public List<SchemaAttribute> getAffectedAttributes() {
        return Arrays.asList(
                VisualConcept.VertexAttribute.NODE_RADIUS,
                VisualConcept.TransactionAttribute.WIDTH);
    }
}
