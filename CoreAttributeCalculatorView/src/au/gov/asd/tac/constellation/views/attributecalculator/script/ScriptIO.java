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
package au.gov.asd.tac.constellation.views.attributecalculator.script;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.views.attributecalculator.panes.AttributeCalculatorPane;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Save and load data access view plugin parameters.
 * <p>
 * Parameters are saved using their names as keys. Therefore, having two plugins
 * with the same parameter names is a bad idea. Parameter names should be
 * qualified with their simple class name: using the fully qualified name
 * (including the package) would cause saved parameters to be useless if classes
 * are refactored into different packages. Note that refactoring the class name
 * will also break a saved file, but we'll take our chances.
 *
 * @author algol
 */
public class ScriptIO {

    private static final String SAVED_SCRIPTS_DIR = "AttrCalcScripts";

    private static final String GLOBAL_OBJECT = "global";
    private static final String PLUGINS_OBJECT = "plugins";
    private static final String IS_ENABLED = "__is_enabled__";

    private static final String ELEMENT_TYPE_KEY = "elementType";
    private static final String ATTR_TO_SET_KEY = "attrToSet";
    private static final String ATTR_TYPE_KEY = "attrType";
    private static final String SCRIPT_KEY = "script";
    private static final String DESCRIPTION_KEY = "description";

    public static void saveScript(final AttributeCalculatorPane acp, String name, String description) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);

        final TextField scriptNameField;
        if (name != null) {
            scriptNameField = new TextField(name);
        } else {
            scriptNameField = new TextField();
        }

        final Label scriptNameLabel = new Label("Script Name:");
        scriptNameLabel.setAlignment(Pos.TOP_LEFT);
        scriptNameLabel.setLabelFor(scriptNameField);

        final TextArea descriptionField;
        if (name != null) {
            descriptionField = new TextArea(description);
        } else {
            descriptionField = new TextArea();
        }

        final Label descriptionLabel = new Label("A Description of What the Script Does:");
        descriptionLabel.setAlignment(Pos.TOP_LEFT);
        descriptionLabel.setLabelFor(descriptionField);

        final VBox details = new VBox();
        details.getChildren().addAll(scriptNameLabel, scriptNameField, descriptionLabel, descriptionField);

        dialog.setResizable(false);
        dialog.setTitle("Query names");
        dialog.setHeaderText("Enter a name and description for the script.");
        dialog.getDialogPane().setContent(details);
        final Optional<ButtonType> option = dialog.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            saveScript(scriptNameField.getText(), acp.getElementType().getLabel(), acp.getAttribute(), acp.getAttributeType(), acp.getScript(), descriptionField.getText());
        }
    }

    private static void saveScript(final String scriptName, final String graphElementType, final String attrToSet, final String attrType, final String script, final String description) {
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File savedScriptsDir = new File(userDir, SAVED_SCRIPTS_DIR);
        if (!savedScriptsDir.exists()) {
            savedScriptsDir.mkdir();
        }

        if (!savedScriptsDir.isDirectory()) {
            final String msg = String.format("Can't create data access directory '%s'.", savedScriptsDir);
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        // A JSON document to store everything in;
        // an array of objects where each array element is a tab, and the objects are the parameters in each tab.
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode node = mapper.createObjectNode();
        node.put(ELEMENT_TYPE_KEY, graphElementType);
        node.put(ATTR_TO_SET_KEY, attrToSet);
        node.put(ATTR_TYPE_KEY, attrType);
        node.put(SCRIPT_KEY, script);
        node.put(DESCRIPTION_KEY, description);

        // Do the actual saving
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.CLOSE_CLOSEABLE, true);
        final File f = new File(savedScriptsDir, encode(scriptName + ".json"));
        boolean go = true;
        if (f.exists()) {
            final String msg = String.format("A script with the name '%s' already exists. Do you want to overwrite it?", scriptName);
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Script file exists");
            alert.setContentText(msg);
            Optional<ButtonType> action = alert.showAndWait();

            go = action.equals(ButtonType.YES);
        }

        if (go) {
            try {
                mapper.writeValue(f, node);
                StatusDisplayer.getDefault().setStatusText(String.format("Script saved to %s.", f.getPath()));
            } catch (IOException ex) {
                final String msg = String.format("Can't save script: %s", ex.getMessage());
                final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    public static SortedMap<String, String[]> getScriptNamesAndDescriptions() {

        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        final String userDir = ApplicationPreferenceKeys.getUserDir(prefs);
        final File savedScriptsDir = new File(userDir, SAVED_SCRIPTS_DIR);
        final Map<String, String[]> namesToDescriptions = new HashMap<>();
        final String[] names;
        final ObjectMapper mapper = new ObjectMapper();

        namesToDescriptions.putAll(AbstractScriptLoader.getDefault().getScripts());

        // Add user scripts after default scripts so that they overwrite defaults if desired
        if (savedScriptsDir.isDirectory()) {
            names = savedScriptsDir.list((File dir, String name) -> {
                return name.toLowerCase().endsWith(".json");
            });
        } else {
            names = new String[0];
        }

        final Set<String> userScriptNames = new HashSet<>();
        for (String name : names) {
            final String visibleName = decode(name.substring(0, name.length() - 5));
            userScriptNames.add(visibleName);
            JsonNode node;
            final String fileName;
            try {
                File scriptFile = new File(savedScriptsDir, name);
                fileName = scriptFile.getPath();
                node = mapper.readTree(scriptFile);
            } catch (final IOException ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            final String description = node.get(DESCRIPTION_KEY).textValue();
            namesToDescriptions.put(visibleName, new String[]{fileName, description});
        }

        SortedMap<String, String[]> namesToDescriptionsSorted = new TreeMap<>((String s1, String s2) -> {
            if (userScriptNames.contains(s1) && !userScriptNames.contains(s2)) {
                return 1;
            } else if (userScriptNames.contains(s2) && !userScriptNames.contains(s1)) {
                return -1;
            }
            return s1.compareTo(s2);
        });
        namesToDescriptionsSorted.putAll(namesToDescriptions);
        return namesToDescriptionsSorted;
    }

    public static void deleteScript(final String fileName, final AttributeCalculatorPane acp) {
        File f = new File(fileName);
        if (f.getPath().contains("_inbuilt_script")) {
            final NotifyDescriptor nd = new NotifyDescriptor.Message("Can't delete inbuilt script.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } else {
            final String msg = String.format("Are you sure you want to delete '%s'?", decode(f.getName().substring(0, f.getName().length() - 5)));
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Delete?");
            alert.setContentText(msg);
            final Optional<ButtonType> action = alert.showAndWait();
            final boolean del = action.equals(ButtonType.YES);
            if (del) {
                f.delete();
            }
        }
    }

    public static void loadScript(final String fileName, final AttributeCalculatorPane acp) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode node = mapper.readTree(new File(fileName));

            final GraphElementType graphElementType = GraphElementType.getValue(node.get(ELEMENT_TYPE_KEY).textValue());
            final String attrToSet = node.get(ATTR_TO_SET_KEY).textValue();
            final String attrType = node.get(ATTR_TYPE_KEY).textValue();
            final String script = node.get(SCRIPT_KEY).textValue();

            acp.setScriptAndDestination(graphElementType, attrToSet, attrType, script);
        } catch (final IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Encode a String so it can be used as a filename.
     *
     * @param s The String to be encoded.
     *
     * @return The encoded String.
     */
    public static String encode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (final char c : s.toCharArray()) {
            if (isValidFileCharacter(c)) {
                b.append(c);
            } else {
                b.append(String.format("_%04x", (int) c));
            }
        }

        return b.toString();
    }

    /**
     * Decode a String that has been encoded by {@link #encode(String)}.
     *
     * @param s The String to be decoded.
     *
     * @return The decoded String.
     */
    public static String decode(final String s) {
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c != '_') {
                b.append(c);
            } else {
                final String hex = s.substring(i + 1, Math.min(i + 5, s.length()));
                if (hex.length() == 4) {
                    try {
                        final int value = Integer.parseInt(hex, 16);
                        b.append((char) value);
                        i += 4;
                    } catch (final NumberFormatException ex) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }

        return b.toString();
    }

    static boolean isValidFileCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || c == '-' || c == '.';
    }
}
