/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributecalculator.tutorial;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.attributecalculator.AttributeCalculatorTopComponent;
import au.gov.asd.tac.constellation.views.attributecalculator.panes.AttributeCalculatorPane;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.netbeans.api.javahelp.Help;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author twilight_sparkle
 */
public abstract class AbstractCalculatorTutorial {

    private Stage tutorialDialog = null;
    private final VBox content = new VBox();
    private final WebView page = new WebView();
    private HBox buttonBar;
    private Button contentsButton;
    private Button nextButton;
    private Button prevButton;

    private final String LOCAL_STYLE = "resources/attribute-calculator.css";

    private int currentPage = 0;
    private int lastPage;

    protected abstract List<String> getPages();

    protected String getContentsPageName() {
        return "Contents.html";
    }

    private void loadCurrentPage() {
        loadPage(getPages().get(currentPage));
    }

    private void loadPage(final String pageName) {
        page.getEngine().load(getClass().getResource("resources/" + pageName).toExternalForm());
    }

    private void pageChanged() {
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage == lastPage);
    }

    /**
     * return the instance of the class based on a Lookup
     *
     * @return instance of Auditor
     */
    public static AbstractCalculatorTutorial getDefault() {
        AbstractCalculatorTutorial tutorial = Lookup.getDefault().lookup(AbstractCalculatorTutorial.class);
        if (tutorial == null) {
            tutorial = new DefaultCalculatorTutorial();
        }

        return tutorial;
    }

    public void displayCalculatorTutorial(AttributeCalculatorPane acp) {

        page.getEngine().locationProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            newValue = newValue.split("/")[newValue.split("/").length - 1];
            currentPage = getPages().indexOf(newValue);
            pageChanged();
        });
        lastPage = getPages().size() - 1;

        if (tutorialDialog != null) {
            if (tutorialDialog.showingProperty().get()) {
                tutorialDialog.requestFocus();
            } else {
                tutorialDialog.show();
            }
            return;
        }

        content.setPadding(Insets.EMPTY);

        tutorialDialog = new Stage();
        tutorialDialog.initStyle(StageStyle.UNIFIED);
        Scene scene = new Scene(content);
        scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
        scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
        scene.getStylesheets().add(AttributeCalculatorTopComponent.class.getResource(LOCAL_STYLE).toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        tutorialDialog.setScene(scene);
        tutorialDialog.setResizable(false);
        tutorialDialog.initOwner(acp.getScene().getWindow());
        tutorialDialog.initModality(Modality.NONE);
        tutorialDialog.setTitle("Attribute Calculator Tutorial");

        buttonBar = new HBox();
        buttonBar.setPadding(new Insets(0, 10, 10, 0));
        contentsButton = new Button("Contents");
        prevButton = new Button("< Prev");
        prevButton.setOnAction((ActionEvent event) -> {
            if (currentPage != 0) {
                currentPage--;
                loadCurrentPage();
            }
        });
        nextButton = new Button("Next >");
        nextButton.setOnAction((ActionEvent event) -> {
            if (currentPage < getPages().size() - 1) {
                currentPage++;
                loadCurrentPage();
            }
        });
        contentsButton.setOnAction((ActionEvent event) -> {
            loadPage(getContentsPageName());
        });
        buttonBar.getChildren().addAll(contentsButton, prevButton, nextButton);
        buttonBar.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBar.setSpacing(5);

        loadCurrentPage();
        content.getChildren().addAll(page, buttonBar);
        VBox.setVgrow(page, Priority.ALWAYS);

        tutorialDialog.show();
    }

    public void openCalculatorHelp() {

        final Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null) {
            final String helpId = getClass().getPackage().getName();
            if (help.isValidID(helpId, true)) {
                new HelpCtx(helpId).display();
            }
        }
    }
}
