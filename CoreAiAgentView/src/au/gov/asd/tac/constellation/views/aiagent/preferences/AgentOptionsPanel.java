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
package au.gov.asd.tac.constellation.views.aiagent.preferences;

import au.gov.asd.tac.constellation.utilities.https.ConstellationHttpClients;
import au.gov.asd.tac.constellation.views.aiagent.agent.SystemPrompt;
import au.gov.asd.tac.constellation.views.aiagent.agent.ToolRegistry;
import au.gov.asd.tac.constellation.views.aiagent.api.AgentTool;
import au.gov.asd.tac.constellation.views.aiagent.api.ToolPolicy;
import au.gov.asd.tac.constellation.views.aiagent.llm.ApiMode;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointConfig;
import au.gov.asd.tac.constellation.views.aiagent.llm.EndpointKind;
import au.gov.asd.tac.constellation.views.aiagent.llm.LlmProviders;
import au.gov.asd.tac.constellation.views.aiagent.llm.ModelInfo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

/**
 * The AI Assistant options panel: endpoints and API keys, behaviour, per-tool
 * approval policies and the system prompt.
 *
 * @author doubtsec
 */
public final class AgentOptionsPanel extends JPanel {

    private static final String DEFAULT_POLICY = "Default";
    private static final String KEY_HINT = "Leave blank to keep the saved key";

    // endpoints tab
    private final DefaultListModel<EndpointConfig> endpointModel = new DefaultListModel<>();
    private final JList<EndpointConfig> endpointList = new JList<>(endpointModel);
    private final JTextField nameField = new JTextField(14);
    private final JComboBox<EndpointKind> kindCombo = new JComboBox<>(EndpointKind.values());
    private final JTextField baseUrlField = new JTextField(16);
    private final JComboBox<ApiMode> apiModeCombo = new JComboBox<>(ApiMode.values());
    private final JTextField defaultModelField = new JTextField(14);
    private final JTextArea headersArea = new JTextArea(3, 16);
    private final JPasswordField apiKeyField = new JPasswordField(12);
    private final JButton clearKeyButton = new JButton("Clear");
    private final JCheckBox requireContextCheck = new JCheckBox("Require client certificate (mTLS)");
    private final JCheckBox forceHttp11Check = new JCheckBox("Force HTTP/1.1");
    private final JButton testButton = new JButton("Test connection");
    private final JTextArea testResult = new JTextArea(4, 16);
    private final Map<String, String> pendingKeys = new HashMap<>();
    private final List<String> clearedKeys = new ArrayList<>();
    private int editingIndex = -1;
    private boolean loadingForm;

    // behaviour tab
    private final JComboBox<EndpointConfig> defaultEndpointCombo = new JComboBox<>();
    private final JSpinner maxStepsSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));
    private final JSpinner maxResultSpinner = new JSpinner(new SpinnerNumberModel(12000, 1000, 200000, 1000));
    private final JTextField temperatureField = new JTextField(6);
    private final JComboBox<String> reasoningCombo = new JComboBox<>(new String[]{"", "none", "low", "medium", "high"});
    private final JCheckBox includeSelectionCheck = new JCheckBox("Include selection counts in the context sent with each message");
    private final JCheckBox debugCheck = new JCheckBox("Log a redacted summary of each model request (for debugging)");

    // tools tab
    private final JPanel toolRows = new JPanel(new GridBagLayout());
    private final Map<String, JComboBox<String>> toolPolicyCombos = new LinkedHashMap<>();

    // system prompt tab
    private final JTextArea promptArea = new JTextArea(18, 40);

    private AgentSettings loaded;

    public AgentOptionsPanel() {
        super(new BorderLayout());
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Endpoints", buildEndpointsTab());
        tabs.addTab("Behaviour", buildBehaviourTab());
        tabs.addTab("Tools", buildToolsTab());
        tabs.addTab("System Prompt", buildPromptTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildEndpointsTab() {
        endpointList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        endpointList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                commitForm();
                loadForm(endpointList.getSelectedIndex());
            }
        });
        final JButton add = new JButton("Add");
        add.addActionListener(event -> {
            commitForm();
            endpointModel.addElement(new EndpointConfig(UUID.randomUUID().toString(), "New endpoint", EndpointKind.OPENAI_COMPATIBLE,
                    "http://localhost:11434/v1", ApiMode.CHAT_COMPLETIONS, "", Map.of(), false, false));
            endpointList.setSelectedIndex(endpointModel.size() - 1);
            refreshDefaultEndpoints();
        });
        final JButton remove = new JButton("Remove");
        remove.addActionListener(event -> {
            final int index = endpointList.getSelectedIndex();
            if (index >= 0 && endpointModel.size() > 1) {
                editingIndex = -1;
                clearedKeys.add(endpointModel.get(index).id());
                endpointModel.remove(index);
                endpointList.setSelectedIndex(Math.max(0, index - 1));
                refreshDefaultEndpoints();
            }
        });
        final JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        listButtons.add(add);
        listButtons.add(remove);
        final JPanel listPanel = new JPanel(new BorderLayout(0, 4));
        final JScrollPane listScroll = new JScrollPane(endpointList);
        listScroll.setPreferredSize(new Dimension(140, 200));
        listPanel.add(listScroll, BorderLayout.CENTER);
        listPanel.add(listButtons, BorderLayout.SOUTH);

        kindCombo.addActionListener(event -> {
            if (!loadingForm && kindCombo.getSelectedItem() == EndpointKind.OPENAI) {
                apiModeCombo.setSelectedItem(ApiMode.RESPONSES);
            }
        });
        headersArea.setLineWrap(false);
        apiKeyField.setToolTipText(KEY_HINT + ". Keys are stored in the operating system keyring, never in preferences.");
        requireContextCheck.setToolTipText("Only connect using the client certificate from Constellation's security context; "
                + "refuse to connect if there isn't one");
        forceHttp11Check.setToolTipText("For gateways that request client certificates through TLS renegotiation, which HTTP/2 doesn't allow");
        clearKeyButton.addActionListener(event -> {
            if (editingIndex >= 0) {
                final String id = endpointModel.get(editingIndex).id();
                pendingKeys.remove(id);
                clearedKeys.add(id);
                apiKeyField.setText("");
                testResult.setText("The saved key will be removed when you apply.");
            }
        });
        testResult.setEditable(false);
        testResult.setLineWrap(true);
        testResult.setWrapStyleWord(true);
        testResult.setOpaque(false);
        testButton.addActionListener(event -> testConnection());

        final JPanel form = new JPanel(new GridBagLayout());
        int row = 0;
        row = addRow(form, row, "Name:", nameField);
        row = addRow(form, row, "Kind:", kindCombo);
        row = addRow(form, row, "Base URL:", baseUrlField);
        row = addRow(form, row, "API:", apiModeCombo);
        row = addRow(form, row, "Default model:", defaultModelField);
        row = addRow(form, row, "Extra headers:", new JScrollPane(headersArea));
        final JPanel keyRow = new JPanel(new BorderLayout(4, 0));
        keyRow.add(apiKeyField, BorderLayout.CENTER);
        keyRow.add(clearKeyButton, BorderLayout.EAST);
        row = addRow(form, row, "API key:", keyRow);
        row = addRow(form, row, "", requireContextCheck);
        row = addRow(form, row, "", forceHttp11Check);
        final JPanel testRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        testRow.add(testButton);
        row = addRow(form, row, "", testRow);
        addRow(form, row, "", testResult);

        final JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(listPanel, BorderLayout.WEST);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildBehaviourTab() {
        final JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        int row = 0;
        row = addRow(form, row, "Default endpoint:", defaultEndpointCombo);
        row = addRow(form, row, "Maximum steps per message:", maxStepsSpinner);
        row = addRow(form, row, "Maximum tool result size (characters):", maxResultSpinner);
        temperatureField.setToolTipText("Blank for the model default");
        row = addRow(form, row, "Default temperature:", temperatureField);
        reasoningCombo.setToolTipText("Blank for the model default");
        row = addRow(form, row, "Default reasoning effort:", reasoningCombo);
        row = addRow(form, row, "", includeSelectionCheck);
        row = addRow(form, row, "", debugCheck);
        final GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        form.add(new JLabel(), filler);
        return form;
    }

    private JComponent buildToolsTab() {
        final JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("<html>AUTO runs the tool without asking, ASK asks you to approve each call, "
                + "NEVER hides the tool from the assistant.</html>"), BorderLayout.NORTH);
        final JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(toolRows, BorderLayout.NORTH);
        final JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadToolRows(final Map<String, ToolPolicy> overrides) {
        toolRows.removeAll();
        toolPolicyCombos.clear();
        int row = 0;
        for (final String header : new String[]{"Tool", "Risk", "Policy"}) {
            final JLabel label = new JLabel("<html><b>" + header + "</b></html>");
            toolRows.add(label, toolCell(row, java.util.Arrays.asList("Tool", "Risk", "Policy").indexOf(header)));
        }
        row++;
        for (final AgentTool tool : ToolRegistry.fromLookup().getTools()) {
            final JLabel name = new JLabel(tool.name());
            name.setToolTipText(tool.description());
            toolRows.add(name, toolCell(row, 0));
            toolRows.add(new JLabel(tool.risk().getLabel()), toolCell(row, 1));
            final JComboBox<String> combo = new JComboBox<>(new String[]{
                DEFAULT_POLICY + " (" + tool.defaultPolicy().name() + ")", ToolPolicy.AUTO.name(), ToolPolicy.ASK.name(), ToolPolicy.NEVER.name()});
            final ToolPolicy override = overrides.get(tool.name());
            combo.setSelectedIndex(override == null ? 0 : override.ordinal() + 1);
            toolPolicyCombos.put(tool.name(), combo);
            toolRows.add(combo, toolCell(row, 2));
            row++;
        }
        toolRows.revalidate();
        toolRows.repaint();
    }

    private static GridBagConstraints toolCell(final int row, final int column) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(2, 0, 2, 12);
        constraints.weightx = column == 0 ? 1 : 0;
        return constraints;
    }

    private JComponent buildPromptTab() {
        promptArea.setLineWrap(true);
        promptArea.setColumns(40);
        promptArea.setWrapStyleWord(true);
        final JButton reset = new JButton("Reset to default");
        reset.addActionListener(event -> promptArea.setText(SystemPrompt.getDefault()));
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(reset);
        final JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("The instructions sent to the model at the start of every conversation."), BorderLayout.NORTH);
        panel.add(new JScrollPane(promptArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private static int addRow(final JPanel panel, final int row, final String label, final JComponent component) {
        final GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.NORTHEAST;
        labelConstraints.insets = new Insets(4, 0, 4, 8);
        panel.add(new JLabel(label), labelConstraints);
        final GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.anchor = GridBagConstraints.WEST;
        fieldConstraints.insets = new Insets(4, 0, 4, 0);
        panel.add(component, fieldConstraints);
        return row + 1;
    }

    // ----- loading and saving -----
    /**
     * Show settings in the panel.
     *
     * @param settings the settings.
     */
    public void load(final AgentSettings settings) {
        loaded = settings;
        pendingKeys.clear();
        clearedKeys.clear();
        editingIndex = -1;
        endpointModel.clear();
        settings.endpoints().forEach(endpointModel::addElement);
        refreshDefaultEndpoints();
        for (int i = 0; i < defaultEndpointCombo.getItemCount(); i++) {
            if (defaultEndpointCombo.getItemAt(i).id().equals(settings.defaultEndpointId())) {
                defaultEndpointCombo.setSelectedIndex(i);
            }
        }
        endpointList.setSelectedIndex(endpointModel.isEmpty() ? -1 : 0);
        loadForm(endpointList.getSelectedIndex());

        maxStepsSpinner.setValue(settings.maxSteps());
        maxResultSpinner.setValue(settings.maxToolResultChars());
        temperatureField.setText(settings.temperature() == null ? "" : settings.temperature().toString());
        reasoningCombo.setSelectedItem(settings.reasoningEffort());
        includeSelectionCheck.setSelected(settings.includeSelection());
        debugCheck.setSelected(settings.debugLogging());

        loadToolRows(settings.toolPolicies());

        promptArea.setText(SystemPrompt.resolve(settings.systemPromptOverride()));
        promptArea.setCaretPosition(0);
    }

    /**
     * Read the settings shown in the panel.
     *
     * @return the settings.
     */
    public AgentSettings toSettings() {
        commitForm();
        final List<EndpointConfig> endpoints = new ArrayList<>();
        for (int i = 0; i < endpointModel.size(); i++) {
            endpoints.add(endpointModel.get(i));
        }
        final Map<String, ToolPolicy> policies = new LinkedHashMap<>();
        toolPolicyCombos.forEach((tool, combo) -> {
            if (combo.getSelectedIndex() > 0) {
                policies.put(tool, ToolPolicy.values()[combo.getSelectedIndex() - 1]);
            }
        });
        // keep overrides for tools that aren't installed at the moment
        if (loaded != null) {
            loaded.toolPolicies().forEach(policies::putIfAbsent);
        }
        Double temperature = null;
        if (!temperatureField.getText().isBlank()) {
            try {
                temperature = Double.valueOf(temperatureField.getText().trim());
            } catch (final NumberFormatException ex) {
                temperature = loaded == null ? null : loaded.temperature();
            }
        }
        final String prompt = promptArea.getText().equals(SystemPrompt.getDefault()) ? "" : promptArea.getText();
        final EndpointConfig defaultEndpoint = (EndpointConfig) defaultEndpointCombo.getSelectedItem();
        return new AgentSettings(endpoints, defaultEndpoint == null ? "" : defaultEndpoint.id(), (Integer) maxStepsSpinner.getValue(),
                (Integer) maxResultSpinner.getValue(), includeSelectionCheck.isSelected(), prompt, policies,
                temperature, Objects.toString(reasoningCombo.getSelectedItem(), ""), debugCheck.isSelected());
    }

    /**
     * @return true if API keys were entered or cleared.
     */
    public boolean hasKeyChanges() {
        return !pendingKeys.isEmpty() || !clearedKeys.isEmpty();
    }

    /**
     * Save entered API keys to the keyring and delete cleared ones. May block
     * while the keyring is unlocked.
     *
     * @param endpoints the endpoints being saved.
     */
    public void saveKeys(final List<EndpointConfig> endpoints) {
        for (final EndpointConfig endpoint : endpoints) {
            if (pendingKeys.containsKey(endpoint.id())) {
                ApiKeyStore.save(endpoint, pendingKeys.get(endpoint.id()));
            } else if (clearedKeys.contains(endpoint.id())) {
                ApiKeyStore.delete(endpoint);
            }
        }
        if (loaded != null) {
            loaded.endpoints().stream()
                    .filter(old -> endpoints.stream().noneMatch(e -> e.id().equals(old.id())))
                    .forEach(ApiKeyStore::delete);
        }
        pendingKeys.clear();
        clearedKeys.clear();
    }

    private void refreshDefaultEndpoints() {
        final EndpointConfig selected = (EndpointConfig) defaultEndpointCombo.getSelectedItem();
        defaultEndpointCombo.removeAllItems();
        for (int i = 0; i < endpointModel.size(); i++) {
            defaultEndpointCombo.addItem(endpointModel.get(i));
        }
        if (selected != null) {
            for (int i = 0; i < defaultEndpointCombo.getItemCount(); i++) {
                if (defaultEndpointCombo.getItemAt(i).id().equals(selected.id())) {
                    defaultEndpointCombo.setSelectedIndex(i);
                }
            }
        }
    }

    private void loadForm(final int index) {
        loadingForm = true;
        try {
            editingIndex = index;
            final boolean enabled = index >= 0;
            for (final JComponent component : new JComponent[]{nameField, kindCombo, baseUrlField, apiModeCombo, defaultModelField,
                headersArea, apiKeyField, clearKeyButton, requireContextCheck, forceHttp11Check, testButton}) {
                component.setEnabled(enabled);
            }
            testResult.setText("");
            apiKeyField.setText("");
            if (!enabled) {
                return;
            }
            final EndpointConfig endpoint = endpointModel.get(index);
            nameField.setText(endpoint.name());
            kindCombo.setSelectedItem(endpoint.kind());
            baseUrlField.setText(endpoint.baseUrl());
            apiModeCombo.setSelectedItem(endpoint.apiMode());
            defaultModelField.setText(endpoint.defaultModel());
            final StringBuilder headers = new StringBuilder();
            endpoint.extraHeaders().forEach((name, value) -> headers.append(name).append(": ").append(value).append('\n'));
            headersArea.setText(headers.toString());
            requireContextCheck.setSelected(endpoint.requireSecurityContext());
            forceHttp11Check.setSelected(endpoint.forceHttp11());
            final String pending = pendingKeys.get(endpoint.id());
            apiKeyField.setText(pending == null ? "" : pending);
        } finally {
            loadingForm = false;
        }
    }

    private void commitForm() {
        if (loadingForm || editingIndex < 0 || editingIndex >= endpointModel.size()) {
            return;
        }
        final EndpointConfig old = endpointModel.get(editingIndex);
        final Map<String, String> headers = new LinkedHashMap<>();
        for (final String line : headersArea.getText().split("\n")) {
            final int colon = line.indexOf(':');
            if (colon > 0) {
                headers.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
            }
        }
        final EndpointConfig updated = new EndpointConfig(old.id(), nameField.getText().trim(), (EndpointKind) kindCombo.getSelectedItem(),
                baseUrlField.getText().trim(), (ApiMode) apiModeCombo.getSelectedItem(), defaultModelField.getText().trim(), headers,
                requireContextCheck.isSelected(), forceHttp11Check.isSelected());
        final String key = new String(apiKeyField.getPassword());
        if (!key.isBlank()) {
            pendingKeys.put(old.id(), key.trim());
            clearedKeys.remove(old.id());
        }
        if (!updated.equals(old)) {
            loadingForm = true;
            try {
                endpointModel.set(editingIndex, updated);
                refreshDefaultEndpoints();
            } finally {
                loadingForm = false;
            }
        }
    }

    private void testConnection() {
        commitForm();
        if (editingIndex < 0) {
            return;
        }
        final EndpointConfig endpoint = endpointModel.get(editingIndex);
        final String pending = pendingKeys.get(endpoint.id());
        testButton.setEnabled(false);
        testResult.setText("Connecting to " + endpoint.baseUrl() + "…");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                final StringBuilder result = new StringBuilder();
                result.append("Security context: ").append(ConstellationHttpClients.describeSecurityContext()).append('\n');
                try {
                    final String key = pending != null ? pending : clearedKeys.contains(endpoint.id()) ? null : ApiKeyStore.read(endpoint);
                    final List<ModelInfo> models = LlmProviders.create(endpoint, key).listModels();
                    result.append(String.format("Connected. %d model%s available", models.size(), models.size() == 1 ? "" : "s"));
                    if (!models.isEmpty()) {
                        result.append(": ").append(String.join(", ", models.stream().limit(12).map(ModelInfo::id).toList()))
                                .append(models.size() > 12 ? ", …" : "");
                    }
                } catch (final Exception ex) {
                    result.append("Failed: ").append(ex.getMessage() == null ? ex.toString() : ex.getMessage());
                }
                return result.toString();
            }

            @Override
            protected void done() {
                testButton.setEnabled(true);
                try {
                    testResult.setText(get());
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (final java.util.concurrent.ExecutionException ex) {
                    testResult.setText("Failed: " + ex.getCause().getMessage());
                }
            }
        }.execute();
    }
}
