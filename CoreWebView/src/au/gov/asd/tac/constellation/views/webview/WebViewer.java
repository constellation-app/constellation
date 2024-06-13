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
package au.gov.asd.tac.constellation.views.webview;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * present a web viewer to the user
 *
 * @author betelgeuse
 */
public class WebViewer extends JFXPanel {

    private static final String DEFAULT_URL = "http://www.google.com";

    private final BorderPane root = new BorderPane();

    public WebViewer() {
        super();

        init(DEFAULT_URL);
    }

    public WebViewer(final String url) {
        super();

        init(url);
    }

    public void loadURL(final String url) {
        if (!root.getChildren().isEmpty()) {
            Platform.runLater(() -> {
                WebView webView = (WebView) root.getCenter();
                final WebEngine webEngine = webView.getEngine();
                webEngine.load(url);
            });
        } else {
            init(url);
        }
    }

    private void init(final String url) {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            Scene scene1 = new Scene(root);
            setScene(scene1);
            WebView webView = new WebView();
            webView.setMaxWidth(Double.MAX_VALUE);
            webView.setMaxHeight(Double.MAX_VALUE);
            root.setCenter(webView);
            webView.setContextMenuEnabled(false);
            final WebEngine webEngine = webView.getEngine();
            webEngine.load(url);
            webEngine.locationProperty().addListener((final ObservableValue<? extends String> observable, final String oldValue, final String newValue)
                    -> webEngine.load(newValue));
            EventHandler<ActionEvent> goAction = (final ActionEvent event) -> webEngine.load(url.startsWith("http://")                        ? url
                    : "http://" + url);
        });
    }
}
