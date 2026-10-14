/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * An assistant message in the transcript. Text is shown as plain text while it
 * streams and rendered as Markdown when the message is complete.
 *
 * @author doubtsec
 */
public final class AssistantMessageNode extends VBox {

    private final String messageId;
    private final StringBuilder text = new StringBuilder();
    private final StringBuilder reasoning = new StringBuilder();
    private final Label status = new Label("Thinking…");
    private final VBox body = new VBox(6);
    private final Text streamingText = new Text();
    private final TextFlow streamingFlow = new TextFlow(streamingText);
    private final Text reasoningText = new Text();
    private final TitledPane reasoningPane;
    private boolean finished;

    public AssistantMessageNode(final String messageId) {
        this.messageId = messageId;
        getStyleClass().add("agent-assistant");

        final Label role = new Label("Assistant");
        role.getStyleClass().add("agent-role");

        status.getStyleClass().add("agent-thinking");

        final TextFlow reasoningFlow = new TextFlow(reasoningText);
        reasoningPane = new TitledPane("Reasoning", reasoningFlow);
        reasoningPane.getStyleClass().add("agent-reasoning");
        reasoningPane.setExpanded(false);
        reasoningPane.setAnimated(false);
        reasoningPane.setVisible(false);
        reasoningPane.setManaged(false);

        streamingFlow.getStyleClass().add("agent-streaming");
        body.getStyleClass().add("agent-assistant-body");
        body.getChildren().add(status);
        body.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(role, reasoningPane, body);

        final MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(event -> {
            final ClipboardContent content = new ClipboardContent();
            content.putString(text.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });
        final ContextMenu menu = new ContextMenu(copy);
        body.setOnContextMenuRequested(event -> menu.show(body, event.getScreenX(), event.getScreenY()));
    }

    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text.toString();
    }

    public boolean isFinished() {
        return finished;
    }

    public void appendText(final String delta) {
        if (text.isEmpty()) {
            body.getChildren().setAll(streamingFlow);
        }
        text.append(delta);
        streamingText.setText(text.toString());
    }

    public void appendReasoning(final String delta) {
        reasoning.append(delta);
        reasoningText.setText(reasoning.toString());
        reasoningPane.setVisible(true);
        reasoningPane.setManaged(true);
        if (text.isEmpty()) {
            status.setText("Reasoning…");
        }
    }

    public void setStatus(final String message) {
        if (text.isEmpty() && !finished) {
            status.setText(message);
        }
    }

    /**
     * The message is complete: render it as Markdown, or hide the body if the
     * model only asked for tool calls.
     *
     * @param fullText the complete text.
     * @param fullReasoning the complete reasoning, may be null.
     */
    public void finish(final String fullText, final String fullReasoning) {
        finished = true;
        if (fullText != null && !fullText.equals(text.toString())) {
            text.setLength(0);
            text.append(fullText);
        }
        if (fullReasoning != null && !fullReasoning.isBlank() && reasoning.isEmpty()) {
            appendReasoning(fullReasoning);
        }
        if (text.toString().isBlank()) {
            body.setVisible(false);
            body.setManaged(false);
            if (reasoning.isEmpty()) {
                setVisible(false);
                setManaged(false);
            }
        } else {
            body.getChildren().setAll(MarkdownFxRenderer.render(text.toString()));
        }
    }

    /**
     * The turn ended before this message finished.
     *
     * @param reason a short note to show.
     */
    public void abandon(final String reason) {
        if (finished) {
            return;
        }
        finished = true;
        if (text.isEmpty()) {
            status.setText(reason);
        } else {
            body.getChildren().setAll(MarkdownFxRenderer.render(text.toString()));
            final Label note = new Label(reason);
            note.getStyleClass().add("agent-thinking");
            body.getChildren().add(note);
        }
    }
}
