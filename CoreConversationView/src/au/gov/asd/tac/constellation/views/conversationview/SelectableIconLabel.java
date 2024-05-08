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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * The combination of a SelectableLabel and and an icon. This is useful for
 * displaying the sender information of a bubble.
 *
 * @author sirius
 */
public class SelectableIconLabel extends HBox {

    public SelectableIconLabel(final String text, boolean wrapText, String style, final TooltipPane tipsPane, final Image icon) {

        if (icon != null) {
            final ImageView imageView = new ImageView(icon);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            getChildren().add(imageView);
        }

        if (text != null) {
            final SelectableLabel label = new SelectableLabel(text, wrapText, style, tipsPane, null);
            getChildren().add(label);
        }
    }
}
