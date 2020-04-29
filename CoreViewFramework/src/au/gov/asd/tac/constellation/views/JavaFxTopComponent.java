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
package au.gov.asd.tac.constellation.views;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Pane;
import javax.swing.SwingUtilities;
import org.openide.windows.TopComponent;

/**
 * A generic JavaFX top component with graph listening enabled.
 *
 * @param <P> The class of {@link Pane} used by this {@link TopComponent} to
 * display content.
 *
 * @author cygnus_x-1
 */
public abstract class JavaFxTopComponent<P extends Pane> extends ListeningTopComponent<P> {

    protected JFXPanel jfxContainer = new JFXPanel();
    protected Scene scene;
    protected ScrollPane scrollPane;

    /**
     * A JavaFxTopComponent will have a ScrollPane by default, as it cannot know
     * the expected layout of the given pane. If you wish to remove the
     * horizontal scroll bar, you can override this method to return
     * ScrollBarPolicy.NEVER.
     *
     * @return a {@link ScrollBarPolicy} representing the desired horizontal
     * scroll bar policy.
     */
    protected ScrollBarPolicy getHorizontalScrollPolicy() {
        return ScrollBarPolicy.NEVER;
    }

    /**
     * A JavaFxTopComponent will have a ScrollPane by default, as it cannot know
     * the expected layout of the given pane. If you wish to remove the vertical
     * scroll bar, you can override this method to return ScrollBarPolicy.NEVER.
     *
     * @return a {@link ScrollBarPolicy} representing the desired vertical
     * scroll bar policy.
     */
    protected ScrollBarPolicy getVerticalScrollPolicy() {
        return ScrollBarPolicy.NEVER;
    }

    /**
     * This is where you pass in a custom Java FX stylesheet, or null if you
     * don't have one.
     *
     * @return a {@link String} representing the path to a stylesheet relative
     * to this class.
     */
    protected abstract String createStyle();

    @Override
    protected final void initContent() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(500, 500));
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            this.content = createContent();

            this.scrollPane = new ScrollPane(content);

            this.scene = new Scene(scrollPane);
            scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
            if (createStyle() != null) {
                scene.getStylesheets().add(getClass().getResource(createStyle()).toExternalForm());
            }

            scrollPane.setHbarPolicy(getHorizontalScrollPolicy());
            if (getHorizontalScrollPolicy() == ScrollBarPolicy.NEVER) {
                scrollPane.setFitToWidth(true);
            } else {
                scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
                    // TODO: fix a bug where the width of the scroll can grow infinitely
                    scrollPane.setFitToWidth(content.prefWidth(-1) <= newValue.getWidth());
                });
            }
            scrollPane.setVbarPolicy(getVerticalScrollPolicy());
            if (getVerticalScrollPolicy() == ScrollBarPolicy.NEVER) {
                scrollPane.setFitToHeight(true);
            } else {
                scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
                    scrollPane.setFitToHeight(content.prefHeight(-1) <= newValue.getHeight());
                });
            }

            // set the font on initialise
            updateFont();

            jfxContainer.setScene(scene);
            jfxContainer.setBackground(Color.red);
            SwingUtilities.invokeLater(() -> {
                add(jfxContainer, BorderLayout.CENTER);
                validate();
            });
        });
    }

    @Override
    protected void updateFont() {
        if (content != null) {
            Platform.runLater(() -> {
                content.setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
                content.setStyle(String.format("-fx-font-family:\"%s\";", FontUtilities.getOutputFontFamily()));
            });
        }
    }
}
