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
package au.gov.asd.tac.constellation.utilities.tooltip;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipProvider.TooltipDefinition;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

/**
 *
 * @author sirius
 */
public class TooltipNode extends VBox {

    public TooltipNode() {

        setPadding(new Insets(3));
        setFillWidth(true);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 5;");
    }

    public final void setTooltips(List<TooltipDefinition> tooltips) {
        getChildren().clear();
        for (TooltipDefinition tooltip : tooltips) {
            getChildren().add(tooltip.getNode());
        }
    }
}
