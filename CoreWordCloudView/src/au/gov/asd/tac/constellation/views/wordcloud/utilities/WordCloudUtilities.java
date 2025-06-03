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
package au.gov.asd.tac.constellation.views.wordcloud.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.wordcloud.ui.WordCloudTopComponent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.openide.util.HelpCtx;

/**
 * Word Cloud Utilities
 *
 * @author antares
 */
public class WordCloudUtilities {
    
    private static final Insets HELP_PADDING = new Insets(2, 0, 0, 0);
    
    public static Button createHelpButton() {
        final Button helpDocumentationButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor())));
        helpDocumentationButton.paddingProperty().set(HELP_PADDING);
        helpDocumentationButton.setTooltip(new Tooltip("Display help for Word Cloud View"));
        helpDocumentationButton.setOnAction(event -> new HelpCtx(WordCloudTopComponent.class.getName()).display());

        // Get rid of the ugly button look so the icon stands alone.
        helpDocumentationButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent; -fx-effect: null; ");

        return helpDocumentationButton;
    }
}
