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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the Word Cloud view.
 *
 * @author twilight_sparkle
 * @author Delphinus8821
 */
@TopComponent.Description(
        preferredID = "WordCloudTopComponent",
        iconBase = "au/gov/asd/tac/constellation/views/wordcloud/ui/resources/word_cloud.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.wordcloud.ui.WordCloudTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 2000),
    @ActionReference(path = "Shortcuts", name = "CS-W")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_WordCloudAction",
        preferredID = "WordCloudTopComponent"
)
@Messages({
    "CTL_WordCloudAction=Word Cloud",
    "CTL_WordCloudTopComponent=Word Cloud View",
    "HINT_WordCloudTopComponent=Word Cloud View"
})
public final class WordCloudTopComponent extends JavaFxTopComponent<WordCloudPane> {

    private final JFXPanel panel = new JFXPanel();
    private final WordCloudController controller;
    private WordCloudPane wordCloudPane;
    private static final int PREF_WIDTH = 500;
    private static final int PREF_HEIGHT = 950;
    private Graph graph = null;

    public WordCloudTopComponent() {
        initComponents();
        setName(Bundle.CTL_WordCloudTopComponent());
        setToolTipText(Bundle.HINT_WordCloudTopComponent());

        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);

        controller = WordCloudController.getDefault().init(WordCloudTopComponent.this);

        // Populate the jfx container
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            wordCloudPane = createContent();
            controller.setWordCloudPane(wordCloudPane);
            final Scene scene = new Scene(wordCloudPane);
            scene.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
            panel.setScene(scene);

            // Update word cloud pane's size when window size changes
            scene.heightProperty().addListener((obv, oldVal, newVal) -> 
                wordCloudPane.setContentHeight(newVal.intValue()));
        });
    }

    @Override
    protected String createStyle() {
        return JavafxStyleManager.isDarkTheme()
                ? "resources/word-cloud-dark.css"
                : "resources/word-cloud-light.css";
    }

    @Override
    protected WordCloudPane createContent() {
        wordCloudPane = new WordCloudPane(controller);
        return wordCloudPane;
    }

    @Override
    public void handleNewGraph(final Graph graph) {
        if (this.graph != graph || controller.isControllerIntialising()) {

            // Remove change listener from previous graph
            if (this.graph != null) {
                this.graph.removeGraphChangeListener(this);
                this.graph = null;
            }

            if (graph != null) {
                // Add listener to new graph 
                this.graph = graph;
                this.graph.addGraphChangeListener(this);

                controller.updateActiveGraph(graph);

            } else {
                controller.setAttributeSelectionEnabled(false);
            }

            final List<String> vertTextAttributes = controller.getVertTextAttributes();
            final List<String> transTextAttributes = controller.getTransTextAttributes();

            // Update the word cloud, button state and parameters on the controlled WordCloudPane.
            controller.createWordsOnPane();
            controller.updateButtonsOnPane();
            controller.updateParametersOnPane(vertTextAttributes, transTextAttributes);
        }
    }

    @Override
    public void handleGraphOpened(final Graph graph) {
        controller.updateGraph();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
