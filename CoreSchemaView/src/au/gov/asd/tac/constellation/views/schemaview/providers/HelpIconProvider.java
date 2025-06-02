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
package au.gov.asd.tac.constellation.views.schemaview.providers;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.openide.util.HelpCtx;

/**
 *
 * @author Auriga2
 */
public class HelpIconProvider {

    public static Button populateHelpIcon(final String className, final String toolTipText) {
        final ImageView helpImage = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));
        final Button helpButton = new Button("", helpImage);
        helpButton.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null; ");
        helpButton.paddingProperty().set(new Insets(0, 8, 0, 0));
        helpButton.setTooltip(new Tooltip("Display help for " + toolTipText));
        helpButton.setOnAction(event -> new HelpCtx(className).display());

        return helpButton;
    }

    public static synchronized void populateHelpIconWithCaption(final String className, final String toolTipText, final Label caption, final HBox schemaLabelAndHelp) {
        schemaLabelAndHelp.getChildren().clear();
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        final Button helpButton = HelpIconProvider.populateHelpIcon(className, toolTipText);
        schemaLabelAndHelp.getChildren().addAll(caption, spacer, helpButton);
    }
}
