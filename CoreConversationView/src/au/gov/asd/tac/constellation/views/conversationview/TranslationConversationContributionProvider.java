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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import org.openide.util.lookup.ServiceProvider;

/**
 * The TranslationConversationContributionProvider adds the contents of the
 * Content.Translated attribute to a message.
 *
 * @author sirius
 */
@ServiceProvider(service = ConversationContributionProvider.class)
public class TranslationConversationContributionProvider extends ConversationContributionProvider {

    private static final String DISPLAY_NAME = "Translation";

    private int contentAttribute = Graph.NOT_FOUND;

    public TranslationConversationContributionProvider() {
        super(DISPLAY_NAME, 100);
    }

    @Override
    public boolean isCompatibleWithGraph(final GraphReadMethods graph) {
        contentAttribute = ContentConcept.TransactionAttribute.CONTENT_TRANSLATED.get(graph);
        return contentAttribute != Graph.NOT_FOUND;
    }

    @Override
    public ConversationContribution createContribution(final GraphReadMethods graph, final ConversationMessage message) {
        final String text = graph.getStringValue(contentAttribute, message.getTransaction());
        return new TranslationContribution(graph.getId(), message, text);
    }

    private class TranslationContribution extends ConversationContribution {

        private final String graphId;
        private final int transactionId;
        private String text;

        private SelectableLabel translationLabel;
        private final TextArea editTranslationArea = new TextArea();
        private final Button editButton = new Button("Edit");
        private final Button createTranslationButton = new Button("Create Translation");
        private final Button saveButton = new Button("Save");
        private final Button cancelButton = new Button("Cancel");

        public TranslationContribution(final String graphId, final ConversationMessage message, final String text) {
            super(TranslationConversationContributionProvider.this, message);
            this.graphId = graphId;
            this.transactionId = message.getTransaction();
            this.text = text;
            editTranslationArea.setStyle("-fx-text-fill: #000000");
            editTranslationArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.DELETE) {
                    IndexRange selection = editTranslationArea.getSelection();
                    if (selection.getLength() == 0) {
                        editTranslationArea.deleteNextChar();
                    } else {
                        editTranslationArea.deleteText(selection);
                    }
                    e.consume();
                } else if (e.isShortcutDown() && e.isShiftDown() && (e.getCode() == KeyCode.RIGHT)) {
                    editTranslationArea.selectNextWord();
                    e.consume();
                } else if (e.isShortcutDown() && e.isShiftDown() && (e.getCode() == KeyCode.LEFT)) {
                    editTranslationArea.selectPreviousWord();
                    e.consume();
                } else if (e.isShortcutDown() && (e.getCode() == KeyCode.RIGHT)) {;
                    editTranslationArea.nextWord();
                    e.consume();
                } else if (e.isShortcutDown() && (e.getCode() == KeyCode.LEFT)) {
                    editTranslationArea.previousWord();
                    e.consume();
                } else if (e.isShiftDown() && (e.getCode() == KeyCode.RIGHT)) {
                    editTranslationArea.selectForward();
                    e.consume();
                } else if (e.isShiftDown() && (e.getCode() == KeyCode.LEFT)) {
                    editTranslationArea.selectBackward();
                    e.consume();
                } else if (e.isShortcutDown() && (e.getCode() == KeyCode.A)) {
                    editTranslationArea.selectAll();
                    e.consume();
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    e.consume();
                }
            });
        }

        @Override
        protected Region createContent(final TooltipPane tips) {

            final GridPane content = new GridPane();
            content.setStyle("-fx-background-insets: 0 0 0 0;");
            content.setHgap(2);
            content.setVgap(2);
            content.setPadding(new Insets(2));

            final ColumnConstraints column0Constraints = new ColumnConstraints();
            column0Constraints.setHgrow(Priority.ALWAYS);
            column0Constraints.setFillWidth(true);
            final ColumnConstraints column1Constraints = new ColumnConstraints();
            column1Constraints.setHgrow(Priority.NEVER);
            column1Constraints.setFillWidth(true);
            content.getColumnConstraints().addAll(column0Constraints, column1Constraints);

            final RowConstraints row0Constraints = new RowConstraints();
            row0Constraints.setVgrow(Priority.NEVER);
            final RowConstraints row1Constraints = new RowConstraints();
            row1Constraints.setVgrow(Priority.NEVER);
            final RowConstraints row2Constraints = new RowConstraints();
            row2Constraints.setVgrow(Priority.ALWAYS);
            content.getRowConstraints().addAll(row0Constraints, row1Constraints, row2Constraints);

            GridPane.setConstraints(saveButton, 1, 0);
            saveButton.setPrefWidth(55);
            saveButton.setMinWidth(Region.USE_PREF_SIZE);
            saveButton.setOnAction((ActionEvent event) -> {
                Graph graph = GraphNode.getGraph(graphId);
                if (graph != null) {
                    text = editTranslationArea.getText();
                    if (text.isEmpty()) {
                        text = null;
                    }
                    final Plugin plugin = new SetTranslationPlugin(transactionId, text);
                    PluginExecution.withPlugin(plugin).executeLater(graph);

                    content.getChildren().removeAll(editTranslationArea, saveButton, cancelButton);

                    if (text == null) {
                        content.getChildren().addAll(createTranslationButton);
                    } else {
                        translationLabel.setSelectableText(text);
                        content.getChildren().addAll(translationLabel, editButton);
                    }
                }
            });

            GridPane.setConstraints(cancelButton, 1, 1);
            cancelButton.setPrefWidth(55);
            cancelButton.setMinWidth(Region.USE_PREF_SIZE);
            GridPane.setFillWidth(cancelButton, true);
            cancelButton.setOnAction((ActionEvent event) -> {
                content.getChildren().removeAll(editTranslationArea, saveButton, cancelButton);
                if (text == null) {
                    content.getChildren().addAll(createTranslationButton);
                } else {
                    translationLabel.setSelectableText(text);
                    content.getChildren().addAll(translationLabel, editButton);
                }
            });

            GridPane.setConstraints(editTranslationArea, 0, 0, 1, 3);
            editTranslationArea.setWrapText(true);

            translationLabel = new SelectableLabel("", true, "-fx-font-style: italic;", tips, null);
            GridPane.setConstraints(translationLabel, 0, 0, 1, 3);

            GridPane.setConstraints(editButton, 1, 0);
            editButton.setMinWidth(Region.USE_PREF_SIZE);
            editButton.setOnAction((ActionEvent event) -> {
                editTranslationArea.setText(text == null ? "" : text);
                content.getChildren().removeAll(editButton, translationLabel);
                editTranslationArea.setText(text);
                content.getChildren().addAll(editTranslationArea, saveButton, cancelButton);
                editTranslationArea.selectAll();
                editTranslationArea.requestFocus();
            });

            GridPane.setConstraints(createTranslationButton, 1, 0);
            createTranslationButton.setOnAction((ActionEvent event) -> {
                editTranslationArea.setText("");
                content.getChildren().remove(createTranslationButton);
                content.getChildren().addAll(editTranslationArea, saveButton, cancelButton);
                editTranslationArea.requestFocus();
            });

            if (text != null) {
                translationLabel.setSelectableText(text);
                content.getChildren().addAll(translationLabel, editButton);
            } else {
                content.getChildren().add(createTranslationButton);
            }

            return content;
        }

        @Override
        public String toString() {
            return "Translation Contribution";
        }
    }

    private class SetTranslationPlugin extends SimpleEditPlugin {

        private final int transactionId;
        private final String translation;

        public SetTranslationPlugin(final int transactionId, final String translation) {
            this.transactionId = transactionId;
            this.translation = translation;
        }

        @Override
        public String getName() {
            return "Set Translation";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final int transactionAttributeId = ContentConcept.TransactionAttribute.CONTENT_TRANSLATED.ensure(graph);
            graph.setStringValue(transactionAttributeId, transactionId, translation);
        }
    }
}
