/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author altair1673
 */
public class MapView extends StackPane {

    private WebEngine webEngine;
    private WebView webView;
    private static final Logger LOGGER = Logger.getLogger("Test");

    public MapView() {
        LOGGER.log(Level.SEVERE, "In MapView constructor");


        Platform.runLater(() -> {
            webView = new WebView();
            getChildren().add(webView);
            setUp();
        });

    }

    private void setUp() {

        webEngine = webView.getEngine();

        webEngine.loadContent("<div id='content'>" + getImgElement() + "</div>");

    }

    private String getImgElement() {
        String img = "";

        File imgFiles = new File("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapview\\resources\\world-map.jpg");

        //for (File fs : imgFiles.listFiles()) {
        img += "<img src=\"" + imgFiles.toURI().toString() + "\" width='950' height='740'/>";
        //}
        LOGGER.log(Level.SEVERE, imgFiles.toURI() + "");
        return img;

    }

}
