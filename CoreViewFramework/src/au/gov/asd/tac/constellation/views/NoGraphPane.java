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
package au.gov.asd.tac.constellation.views;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Create a blank view when a view is open without a graph being open at the same time. 
 * Stops the user from being able to interact with a view without a graph being open
 *
 * @author aldebaran30701
 */
public class NoGraphPane extends VBox {

    private final Label noGraphLabel;
    private final Button helpButton;

    private static final Insets PADDING = new Insets(0, 0, 0, 0);
    private static final int SPACING = 5;
    private static final String NO_GRAPH_LABEL = "Open or create a graph to enable the %s.";

    public NoGraphPane(final String viewName, final Button helpButton) {
        setSpacing(SPACING);
        setPadding(PADDING);

        this.noGraphLabel = new Label(String.format(NO_GRAPH_LABEL, viewName));
        this.helpButton = helpButton;

        this.getChildren().add(this.noGraphLabel);
        if (this.helpButton != null) {
            this.getChildren().add(this.helpButton);
        }
    }
}
