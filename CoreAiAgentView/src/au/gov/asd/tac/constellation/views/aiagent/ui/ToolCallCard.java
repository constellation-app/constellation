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

import au.gov.asd.tac.constellation.utilities.json.JsonFactoryUtilities;
import au.gov.asd.tac.constellation.views.aiagent.agent.AgentEvent;
import au.gov.asd.tac.constellation.views.aiagent.agent.Decision;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolResult;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolRisk;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A tool call in the transcript. While a call is waiting for approval the card
 * shows Approve, Always allow, Deny and Edit JSON controls; Enter approves and
 * Escape denies when the card has focus.
 *
 * @author doubtsec
 */
public final class ToolCallCard extends VBox {

    private static final int MAX_RESULT_CHARS = 20_000;

    private final String callId;
    private final String toolName;
    private final Label status = new Label("…");
    private final Label meta = new Label();
    private final Label description = new Label();
    private final Label warning = new Label();
    private final Label resultSummary = new Label();
    private final Hyperlink argsLink = new Hyperlink("▸ arguments");
    private final Hyperlink resultLink = new Hyperlink("▸ result");
    private final TextArea argsArea = new TextArea();
    private final TextArea resultArea = new TextArea();
    private final HBox approvalButtons = new HBox();
    private final HBox denyRow = new HBox(6);
    private final TextField denyReason = new TextField();
    private final Label editError = new Label();
    private final HBox header;
    private Consumer<Decision> onDecision;
    private String originalArguments = "";
    private boolean awaitingApproval;
    private boolean editing;
    private String proposedGraphName;

    public ToolCallCard(final String callId, final String toolName) {
        this.callId = callId;
        this.toolName = toolName;
        getStyleClass().add("agent-tool-card");
        setFocusTraversable(false);

        status.getStyleClass().add("agent-tool-status");
        final Label name = new Label(toolName);
        name.getStyleClass().add("agent-tool-name");
        meta.getStyleClass().add("agent-tool-meta");
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        argsLink.getStyleClass().add("agent-link");
        argsLink.setOnAction(event -> setArgumentsVisible(!argsArea.isVisible()));
        header = new HBox(status, name, meta, spacer, argsLink);
        header.getStyleClass().add("agent-tool-header");

        description.getStyleClass().add("agent-tool-description");
        description.setVisible(false);
        description.setManaged(false);
        description.setMaxWidth(Double.MAX_VALUE);

        warning.getStyleClass().add("agent-tool-warning");
        warning.setVisible(false);
        warning.setManaged(false);
        warning.setMaxWidth(Double.MAX_VALUE);

        argsArea.getStyleClass().add("agent-tool-args");
        argsArea.setEditable(false);
        argsArea.setWrapText(true);
        argsArea.setPrefRowCount(4);
        argsArea.setVisible(false);
        argsArea.setManaged(false);

        editError.getStyleClass().add("agent-error-text");
        editError.setVisible(false);
        editError.setManaged(false);

        final Button approve = new Button("Approve");
        approve.getStyleClass().add("agent-primary");
        approve.setTooltip(new Tooltip("Run this call (Enter)"));
        approve.setOnAction(event -> approve());
        final Button always = new Button("Always allow");
        always.setTooltip(new Tooltip("Run this call, and run " + toolName + " without asking for the rest of this chat"));
        always.setOnAction(event -> decide(new Decision.AlwaysAllow()));
        final Button edit = new Button("Edit JSON");
        edit.setTooltip(new Tooltip("Edit the arguments before running"));
        edit.setOnAction(event -> startEditing());
        final Button deny = new Button("Deny…");
        deny.getStyleClass().add("agent-danger");
        deny.setTooltip(new Tooltip("Don't run this call (Esc)"));
        deny.setOnAction(event -> showDenyReason());
        approvalButtons.getChildren().addAll(approve, always, edit, deny);
        approvalButtons.getStyleClass().add("agent-approval-buttons");
        approvalButtons.setVisible(false);
        approvalButtons.setManaged(false);

        denyReason.setPromptText("Reason (optional) — sent to the assistant");
        HBox.setHgrow(denyReason, Priority.ALWAYS);
        denyReason.setOnAction(event -> decide(new Decision.Deny(denyReason.getText())));
        final Button confirmDeny = new Button("Deny");
        confirmDeny.getStyleClass().add("agent-danger");
        confirmDeny.setOnAction(event -> decide(new Decision.Deny(denyReason.getText())));
        final Button cancelDeny = new Button("Cancel");
        cancelDeny.setOnAction(event -> {
            denyRow.setVisible(false);
            denyRow.setManaged(false);
            requestFocus();
        });
        denyRow.getChildren().addAll(denyReason, confirmDeny, cancelDeny);
        denyRow.setVisible(false);
        denyRow.setManaged(false);

        resultSummary.getStyleClass().add("agent-tool-result");
        resultSummary.setVisible(false);
        resultSummary.setManaged(false);
        resultSummary.setMaxWidth(Double.MAX_VALUE);
        resultLink.getStyleClass().add("agent-link");
        resultLink.setOnAction(event -> {
            final boolean show = !resultArea.isVisible();
            resultArea.setVisible(show);
            resultArea.setManaged(show);
            resultLink.setText((show ? "▾" : "▸") + " result");
        });
        resultLink.setVisible(false);
        resultLink.setManaged(false);
        resultArea.getStyleClass().add("agent-tool-args");
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefRowCount(8);
        resultArea.setVisible(false);
        resultArea.setManaged(false);

        final HBox resultRow = new HBox(6, resultSummary, resultLink);
        HBox.setHgrow(resultSummary, Priority.ALWAYS);

        getChildren().addAll(header, description, warning, argsArea, editError, approvalButtons, denyRow, resultRow, resultArea);

        setOnKeyPressed(event -> {
            if (!awaitingApproval || event.getTarget() instanceof TextInputControl) {
                return;
            }
            if (event.getCode() == KeyCode.ENTER) {
                approve();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                decide(new Decision.Deny(""));
                event.consume();
            }
        });
    }

    public String getCallId() {
        return callId;
    }

    public String getToolName() {
        return toolName;
    }

    public boolean isAwaitingApproval() {
        return awaitingApproval;
    }

    public void setOnDecision(final Consumer<Decision> onDecision) {
        this.onDecision = onDecision;
    }

    /**
     * Show the proposal.
     *
     * @param event the proposal.
     */
    public void propose(final AgentEvent.ToolCallProposed event) {
        description.setText(event.description());
        description.setVisible(!event.description().equals(toolName));
        description.setManaged(description.isVisible());
        originalArguments = event.prettyArguments();
        argsArea.setText(originalArguments);
        argsArea.setPrefRowCount(Math.max(2, Math.min(10, (int) originalArguments.lines().count())));
        proposedGraphName = event.graphName();

        if (event.risk() != ToolRisk.READ_ONLY) {
            final Label badge = new Label(event.risk().getLabel());
            badge.getStyleClass().addAll("agent-badge", "risk-" + event.risk().name().toLowerCase(Locale.ROOT));
            header.getChildren().add(3, badge);
        }

        awaitingApproval = event.awaitingApproval();
        if (awaitingApproval) {
            getStyleClass().add("pending");
            status.setText("?");
            status.getStyleClass().add("waiting");
            meta.setText("needs approval" + (proposedGraphName == null ? "" : " · graph \"" + proposedGraphName + "\""));
            approvalButtons.setVisible(true);
            approvalButtons.setManaged(true);
            setArgumentsVisible(true);
            setFocusTraversable(true);
            AgentViewPane.requestFocusWhenShowing(this);
        } else {
            meta.setText("");
        }
    }

    /**
     * The call has started, possibly with arguments the user edited.
     *
     * @param arguments the arguments it runs with.
     */
    public void started(final String arguments) {
        if (arguments != null && !arguments.isBlank()) {
            final String pretty = prettyResult(arguments);
            if (!pretty.equals(originalArguments)) {
                argsArea.setText(pretty);
                description.setText(description.getText() + " (arguments edited)");
            }
        }
        started();
    }

    public void started() {
        status.setText("○");
        status.getStyleClass().remove("waiting");
        meta.setText("running…");
    }

    public void progress(final String message) {
        meta.setText(message);
    }

    public void finished(final ToolResult result, final long millis) {
        endApproval();
        status.getStyleClass().removeAll("waiting", "ok", "error");
        status.getStyleClass().add(result.error() ? "error" : "ok");
        status.setText(result.error() ? "✗" : "✓");
        meta.setText(millis < 1000 ? millis + " ms" : String.format("%.1f s", millis / 1000.0));
        final String summary = result.summary() != null ? result.summary() : abbreviate(result.text(), 140);
        resultSummary.setText(summary);
        resultSummary.setVisible(!summary.isBlank());
        resultSummary.setManaged(!summary.isBlank());
        resultArea.setText(prettyResult(result.text()));
        resultLink.setVisible(result.text() != null && !result.text().isBlank());
        resultLink.setManaged(resultLink.isVisible());
    }

    public void rejected(final String reason, final String rawArguments) {
        if (originalArguments.isEmpty() && rawArguments != null && !rawArguments.isBlank()) {
            // rejected before it was proposed (e.g. invalid arguments): show what the model sent
            originalArguments = prettyResult(rawArguments);
            argsArea.setText(originalArguments);
            argsArea.setPrefRowCount(Math.max(2, Math.min(10, (int) originalArguments.lines().count())));
        }
        endApproval();
        getStyleClass().add("rejected");
        status.getStyleClass().removeAll("waiting", "ok");
        status.getStyleClass().add("error");
        status.setText("✕");
        meta.setText("not run");
        resultSummary.setText(reason);
        resultSummary.setVisible(true);
        resultSummary.setManaged(true);
    }

    /**
     * Warn that the active graph changed while this call was waiting.
     *
     * @param graphName the new active graph, may be null.
     */
    public void activeGraphChanged(final String graphName) {
        if (!awaitingApproval || proposedGraphName == null || proposedGraphName.equals(graphName)) {
            warning.setVisible(false);
            warning.setManaged(false);
            return;
        }
        warning.setText(String.format("⚠ The active graph changed to %s since this was proposed; it will run on the active graph.",
                graphName == null ? "(none)" : "\"" + graphName + "\""));
        warning.setVisible(true);
        warning.setManaged(true);
    }

    private void endApproval() {
        awaitingApproval = false;
        editing = false;
        getStyleClass().remove("pending");
        approvalButtons.setVisible(false);
        approvalButtons.setManaged(false);
        denyRow.setVisible(false);
        denyRow.setManaged(false);
        editError.setVisible(false);
        editError.setManaged(false);
        warning.setVisible(false);
        warning.setManaged(false);
        argsArea.setEditable(false);
        setArgumentsVisible(false);
        setFocusTraversable(false);
    }

    private void setArgumentsVisible(final boolean visible) {
        argsArea.setVisible(visible);
        argsArea.setManaged(visible);
        argsLink.setText((visible ? "▾" : "▸") + " arguments");
    }

    private void startEditing() {
        editing = true;
        setArgumentsVisible(true);
        argsArea.setEditable(true);
        argsArea.requestFocus();
    }

    private void showDenyReason() {
        denyRow.setVisible(true);
        denyRow.setManaged(true);
        denyReason.requestFocus();
    }

    private void approve() {
        if (editing && !argsArea.getText().equals(originalArguments)) {
            try {
                JsonFactoryUtilities.getObjectMapper().readTree(argsArea.getText());
            } catch (final JsonProcessingException ex) {
                editError.setText("Not valid JSON: " + ex.getOriginalMessage());
                editError.setVisible(true);
                editError.setManaged(true);
                return;
            }
            decide(new Decision.Edit(argsArea.getText()));
        } else {
            decide(new Decision.Approve());
        }
    }

    private void decide(final Decision decision) {
        if (!awaitingApproval) {
            return;
        }
        endApproval();
        status.setText("…");
        meta.setText(decision instanceof Decision.Deny ? "denied" : "approved");
        if (onDecision != null) {
            onDecision.accept(decision);
        }
    }

    private static String prettyResult(final String text) {
        if (text == null) {
            return "";
        }
        String pretty = text;
        final String trimmed = text.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                pretty = JsonFactoryUtilities.getObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(JsonFactoryUtilities.getObjectMapper().readTree(trimmed));
            } catch (final JsonProcessingException ex) {
                pretty = text;
            }
        }
        return abbreviate(pretty, MAX_RESULT_CHARS);
    }

    private static String abbreviate(final String text, final int max) {
        if (text == null) {
            return "";
        }
        final String singleLine = max < 1000 ? text.replaceAll("\\s+", " ").trim() : text;
        return singleLine.length() <= max ? singleLine : singleLine.substring(0, max) + "…";
    }
}
