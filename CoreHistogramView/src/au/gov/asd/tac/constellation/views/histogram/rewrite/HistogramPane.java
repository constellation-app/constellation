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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.histogram.HistogramTopComponent;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.openide.util.HelpCtx;

/**
 *
 * @author Quasar985
 */
public class HistogramPane extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(HistogramPane.class.getName());
    private static final String HISTOGRAM_TOP_COMPONENT_CLASS_NAME = HistogramTopComponent.class.getName();

    private static final String NO_PLUGINS_SELECTED_TITLE = "No plugins selected.";

    // Styles
    private static final String HELP_STYLE = "-fx-border-color: transparent; -fx-background-color: transparent; -fx-effect: null;";

    // Images
    private static final ImageView HELP_ICON = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor()));

    // Tooltips
    private static final String HELP_TOOLTIP = "Display help for Data Access";

    private Button helpButton;
    //private final HistogramTopComponent2 topComponent;

    public HistogramPane(final HistogramController histogramContoller) {
        System.out.println("HistogramPane");

        //this.topComponent = topComponent;
        ////////////////////
        // Help Button
        ////////////////////
        helpButton = new Button("AAAAAAAAAAA", HELP_ICON);
        helpButton.paddingProperty().set(new Insets(0, 8, 0, 0));
        helpButton.setTooltip(new Tooltip(HELP_TOOLTIP));
        helpButton.setOnAction(actionEvent -> {
            System.out.println("THIS CALLED");
            new HelpCtx(HISTOGRAM_TOP_COMPONENT_CLASS_NAME).display();

            actionEvent.consume();
        });

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle(HELP_STYLE);
        System.out.println("HistogramPane finished help button");

        final VBox vbox = new VBox();
        vbox.prefWidthProperty().bind(this.widthProperty());

//        AnchorPane.setTopAnchor(vbox, 0.0);
//        AnchorPane.setBottomAnchor(vbox, 0.0);
//        AnchorPane.setLeftAnchor(vbox, 0.0);
//        AnchorPane.setRightAnchor(vbox, 0.0);
        vbox.getChildren().addAll(helpButton);

        // getChildren().add(vbox);
        System.out.println("HistogramPane finished vbox");
        this.setCenter(vbox);
        System.out.println("HistogramPane done");
    }

//    private void init() {
//
//        ////////////////////
//        // Help Button
//        ////////////////////
//        helpButton = new Button("", HELP_ICON);
//        helpButton.paddingProperty().set(new Insets(0, 8, 0, 0));
//        helpButton.setTooltip(new Tooltip(HELP_TOOLTIP));
//        helpButton.setOnAction(actionEvent -> {
//            System.out.println("THIS CALLED");
//            new HelpCtx(HISTOGRAM_TOP_COMPONENT_CLASS_NAME).display();
//
//            actionEvent.consume();
//        });
//        // Get rid of the ugly button look so the icon stands alone.
//        helpButton.setStyle(HELP_STYLE);
//
//        addUIComponents();
//    }
//
//    /**
//     * Adds the UI components constructed during initialization to the pane.
//     */
//    private void addUIComponents() {
//        final VBox vbox = new VBox(
//                helpButton
//        );
//        //VBox.setVgrow(getDataAccessTabPane().getTabPane(), Priority.ALWAYS);
//
//        AnchorPane.setTopAnchor(vbox, 0.0);
//        AnchorPane.setBottomAnchor(vbox, 0.0);
//        AnchorPane.setLeftAnchor(vbox, 0.0);
//        AnchorPane.setRightAnchor(vbox, 0.0);
//
//        vbox.prefWidthProperty().bind(this.widthProperty());
//
//        //getChildren().add(vbox);
//        this.setCenter(vbox);
//
//        //AnchorPane.setTopAnchor(getButtonToolbar().getOptionsToolbar(), 5.0);
//        // AnchorPane.setRightAnchor(getButtonToolbar().getOptionsToolbar(), 5.0);
//        //getChildren().add(getButtonToolbar().getOptionsToolbar());
//        // Modifies the menu sizes and positions as the overall pane size is
//        // either shrunk or grown
////        widthProperty().addListener((observable, oldValue, newValue) -> {
////            if (newValue.intValue() <= 460) {
////                getButtonToolbar().handleShrinkingPane();
////
////                getOptionsMenuBar().getMenuBar().setMinHeight(60);
////            } else {
////                getButtonToolbar().handleGrowingPane();
////
////                getOptionsMenuBar().getMenuBar().setMinHeight(36);
////            }
////        });
//    }

}
