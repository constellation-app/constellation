/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AttributeValueTranslator;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.ClipboardUtilities;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AttributeEditorFactory;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AttributeEditorFactory.AttributeEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AttributeValueEditorFactory;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.ListSelectionEditorFactory;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.ListSelectionEditorFactory.ListSelectionEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.TimeZoneEditorFactory;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.AttributeValueEditOperation;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.CreateAttributeEditOperation;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.ModifyAttributeEditOperation;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.PrimaryKeyDefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.PrimaryKeyEditOperation;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.UpdateTimeZonePlugin;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javax.swing.BoxLayout;
import org.openide.util.NbPreferences;

/**
 * The AttributeEditorPanel provides the bulk of the user interface for
 * CONSTELLATION's 'attribute editor' view.
 *
 * @see AttributeEditorTopComponent
 * @author twinkle2_little
 */
public class AttributeEditorPanel extends javax.swing.JPanel {

    public static final String MIMETYPE = "application/x-constellation-attributenametype";
    public static final DataFormat ATTRIBUTE_NAME_DATA_FORMAT = new DataFormat(MIMETYPE);
    private static final double CELL_HEIGHT = 30;
    private static final double CELL_ITEM_HEIGHT = 20;
    private static final double CELL_ITEM_SPACING = 5;
    private static final int VISIBLE_ROWS = 10;
    private static final String[] HEADING_TITLES = {"Graph  (%d attributes%s)", "Node  (%d attributes%s)", "Transaction  (%d attributes%s)"};
    private static final String HIDDEN_ATTRIBUTES_INFORMATION = ", %d hidden";
    private static final GraphElementType[] ELEMENT_TYPES = {GraphElementType.GRAPH, GraphElementType.VERTEX, GraphElementType.TRANSACTION};
    private static final String DARK_THEME = "resources/Style-AttributeEditor-Dark.css";
    private static final String NO_VALUE_TEXT = "<No Value>";
    
    private static final String SCHEMA_ATTRIBUTE_COLOUR = "#333333";
    private static final String PRIMARY_KEY_ATTRIBUTE_COLOUR = "#8a1d1d";
    private static final String CUSTOM_ATTRIBUTE_COLOUR = "#1f4f8a";
    private static final String HIDDEN_ATTRIBUTE_COLOUR = "#999999";

    private JFXPanel container = new JFXPanel();
    private StackPane root;
    private ArrayList<VBox> valueTitledPaneContainers = new ArrayList<>();
    private VBox titledPaneHeadingsContainer;
    private final ScrollPane scrollPane = new ScrollPane();
    private final MenuBar optionsBar = new MenuBar();
    private final Menu optionsMenu = new Menu("Options");
    private final CheckMenuItem completeWithSchemaItem = new CheckMenuItem("Complete with Schema After Edits");
    private final Preferences prefs = NbPreferences.forModule(AttributePreferenceKey.class);
    private final AttributeEditorTopComponent topComponent;
    private final StringProperty[] headingTitleProperties = new StringProperty[3];
    private final Map<GraphElementType, List<String>> currentAttributeNames = new HashMap<>();
    private int currentFontSize;

    private enum HeadingType {

        GRAPH, NODE, TRANSACTION;
    }

    private static final AttributeEditorFactory ATTRIBUTE_EDITOR_FACTORY = new AttributeEditorFactory();
    private static final ListSelectionEditorFactory LIST_SELECTION_EDITOR_FACTORY = new ListSelectionEditorFactory();
    private static final TimeZoneEditorFactory UPDATE_TIME_ZONE_EDITOR_FACTORY = new TimeZoneEditorFactory();

//    private static final HashMap<String, AbstractAttributeHandler> attributeHandlerMap = new HashMap<>();
    private final TooltipPane tooltipPane = new TooltipPane();

    private void addCopyHandlersToListView(final ListView<Object> newList, final AttributeData attribute) {
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction((ActionEvent event) -> {
            copySelectedItems(newList, attribute.getDataType());
        });

        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        final ContextMenu ctxMenu = new ContextMenu(copyItem);
        newList.setContextMenu(ctxMenu);
        newList.setOnKeyPressed((KeyEvent event) -> {
            if (event.isControlDown() && (event.getCode() == KeyCode.C)) {
                copySelectedItems(newList, attribute.getDataType());
            }
        });
    }

    /**
     * Creates new form AttributeEditorPanel
     *
     * @param parent The TopComponent that the AttributeEditor is in.
     */
    public AttributeEditorPanel(AttributeEditorTopComponent parent) {

        initComponents();
        this.topComponent = parent;

        titledPaneHeadingsContainer = new VBox();
        for (int i = 0; i < HEADING_TITLES.length; i++) {
            VBox temp = new VBox();
            temp.setPadding(Insets.EMPTY);
            valueTitledPaneContainers.add(temp);
        }

        scrollPane.setContent(titledPaneHeadingsContainer);
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);
        add(container);
        scrollPane.setFitToWidth(true);

        final int height = this.getPreferredSize().height;

        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            root = new StackPane();

            final Scene scene = new Scene(root, 0, height);
            scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
            scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
            scene.getStylesheets().add(AttributeEditorPanel.class.getResource(DARK_THEME).toExternalForm());

            for (int i = 0; i < valueTitledPaneContainers.size(); i++) {
                headingTitleProperties[i] = new SimpleStringProperty();
                headingTitleProperties[i].setValue(String.format(HEADING_TITLES[i], 0, ""));
                HeadingType headingType = HeadingType.values()[i];
                TitledPane headerPane = createHeaderTitledPane(headingType, headingTitleProperties[i], valueTitledPaneContainers.get(i));
                headerPane.prefWidthProperty().bind(titledPaneHeadingsContainer.widthProperty());// check this

                titledPaneHeadingsContainer.getChildren().add(headerPane);
            }

            BorderPane borderPane = new BorderPane();

            completeWithSchemaItem.setStyle("-fx-fill: white;");
            completeWithSchemaItem.setSelected(false);
            optionsMenu.getItems().addAll(createColoursMenu(), completeWithSchemaItem);
            optionsBar.getMenus().add(optionsMenu);

            borderPane.setTop(optionsBar);

            borderPane.setCenter(scrollPane);

            root.getChildren().add(borderPane);
            root.getChildren().add(tooltipPane);

            container.setScene(scene);
        });

        updateEditorPanel(null);

        setFontSize();
    }

    void rebuildColourMenu() {
        Platform.runLater(() -> {
            optionsMenu.getItems().set(0, createColoursMenu());
        });
    }

    private MenuItem createColourMenuItem(final String itemName, final String correspondingPreference, final Color color) {
        final HBox schemaMenuNode = new HBox(CELL_ITEM_SPACING);
        final MenuItem schemaMenuItem = new MenuItem(null, schemaMenuNode);
        final Rectangle schemaMenuRect = new Rectangle(20, 20);
        final Text schemaMenuText = new Text(itemName);
        schemaMenuText.setStyle("-fx-fill: white; -fx-font-smoothing-type:lcd;");
        schemaMenuNode.getChildren().addAll(schemaMenuRect, schemaMenuText);
        schemaMenuRect.setFill(color);
        schemaMenuRect.setStroke(Color.LIGHTGREY);
        schemaMenuItem.setOnAction(e -> {
            final EditOperation editOperation = value -> {
                prefs.put(correspondingPreference, ((ConstellationColor) value).getHtmlColor());
            };
            @SuppressWarnings("unchecked") // return type of createEditor will actually be AbstractEditor<ConstellationColor>
            final AbstractEditor<ConstellationColor> editor = ((AbstractEditorFactory<ConstellationColor>) AttributeValueEditorFactory.getEditFactory(ColorAttributeDescription.ATTRIBUTE_NAME)).createEditor(editOperation, String.format("for %s", itemName), ConstellationColor.fromFXColor(color));
            final AttributeEditorDialog dialog = new AttributeEditorDialog(false, editor);
            dialog.showDialog();
        });
        return schemaMenuItem;
    }

    private Menu createColoursMenu() {
        final Menu coloursMenu = new Menu("Attribute Editor Colours");

        final Color schemaColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.SCHEMA_ATTRIBUTE_COLOUR, SCHEMA_ATTRIBUTE_COLOUR)).getJavaFXColor();
        coloursMenu.getItems().add(createColourMenuItem("Schema Attributes", AttributePreferenceKey.SCHEMA_ATTRIBUTE_COLOUR, schemaColour));

        final Color primaryKeyColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.PRIMARY_KEY_ATTRIBUTE_COLOUR, PRIMARY_KEY_ATTRIBUTE_COLOUR)).getJavaFXColor();
        coloursMenu.getItems().add(createColourMenuItem("Primary Key Attributes", AttributePreferenceKey.PRIMARY_KEY_ATTRIBUTE_COLOUR, primaryKeyColour));

        final Color customColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.CUSTOM_ATTRIBUTE_COLOUR, CUSTOM_ATTRIBUTE_COLOUR)).getJavaFXColor();
        coloursMenu.getItems().add(createColourMenuItem("Custom Attributes (Not in the Schema)", AttributePreferenceKey.CUSTOM_ATTRIBUTE_COLOUR, customColour));

        final Color hiddenColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTE_COLOUR, HIDDEN_ATTRIBUTE_COLOUR)).getJavaFXColor();
        coloursMenu.getItems().add(createColourMenuItem("Hidden Attributes", AttributePreferenceKey.HIDDEN_ATTRIBUTE_COLOUR, hiddenColour));

        final HBox restoreMenuNode = new HBox(5);
        final MenuItem restoreMenuItem = new MenuItem(null, restoreMenuNode);
        final Text restoreMenuText = new Text("Restore Default Colours");
        restoreMenuText.setStyle("-fx-fill: white; -fx-font-smoothing-type:lcd;");
        restoreMenuNode.getChildren().add(restoreMenuText);
        restoreMenuItem.setOnAction(e -> {
            prefs.put(AttributePreferenceKey.SCHEMA_ATTRIBUTE_COLOUR, SCHEMA_ATTRIBUTE_COLOUR);
            prefs.put(AttributePreferenceKey.PRIMARY_KEY_ATTRIBUTE_COLOUR, PRIMARY_KEY_ATTRIBUTE_COLOUR);
            prefs.put(AttributePreferenceKey.CUSTOM_ATTRIBUTE_COLOUR, CUSTOM_ATTRIBUTE_COLOUR);
            prefs.put(AttributePreferenceKey.HIDDEN_ATTRIBUTE_COLOUR, HIDDEN_ATTRIBUTE_COLOUR);
        });
        coloursMenu.getItems().add(restoreMenuItem);
        return coloursMenu;
    }

    private TitledPane createHeaderTitledPane(HeadingType headingType, StringProperty title, VBox container) {
        final TitledPane result = new TitledPane();
        result.setContent(container);
        final BorderPane headerGraphic = new BorderPane();
        final HBox optionsButtons = new HBox(5);
        optionsButtons.setPadding(new Insets(2));
        final Text heading = new Text();
        heading.textProperty().bind(title);
        heading.setStyle("-fx-font-family:\"Arial\"; -fx-font-weight:bold;");
        heading.setFill(Color.web("#e0e0e0"));
        final ToggleButton showAllToggle = new ToggleButton("Show all");
        showAllToggle.setAlignment(Pos.CENTER);
        showAllToggle.setTextAlignment(TextAlignment.CENTER);
        showAllToggle.setStyle("-fx-font-size: 12; -fx-background-insets: 0, 0; -fx-padding: 0");
        showAllToggle.setPrefSize(60, 12);
        showAllToggle.setMaxSize(60, 12);
        showAllToggle.setPadding(new Insets(5));
        showAllToggle.setTooltip(new Tooltip("Show hidden attributes"));
        final String key;
        final GraphElementType elementType;
        switch (headingType) {
            case GRAPH:
                key = AttributePreferenceKey.GRAPH_SHOW_ALL;
                elementType = GraphElementType.GRAPH;
                break;
            case NODE:
                key = AttributePreferenceKey.NODE_SHOW_ALL;
                elementType = GraphElementType.VERTEX;
                break;
            case TRANSACTION:
                key = AttributePreferenceKey.TRANSACTION_SHOW_ALL;
                elementType = GraphElementType.TRANSACTION;
                break;
            default:
                key = "";
                elementType = null;
                break;
        }
        showAllToggle.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            prefs.putBoolean(key, newValue);
        });
        showAllToggle.setSelected(prefs.getBoolean(key, false));

        final Button addMenu = new Button(null, new ImageView(UserInterfaceIconProvider.ADD.buildImage(16)));
        addMenu.setAlignment(Pos.CENTER);
        addMenu.setTextAlignment(TextAlignment.CENTER);
        addMenu.setStyle("-fx-background-color: #666666; -fx-background-radius: 2; -fx-font-size: 12; -fx-background-insets: 0, 0; -fx-padding: 0");
        addMenu.setPrefSize(18, 12);
        addMenu.setMaxSize(18, 12);
        addMenu.setPadding(new Insets(5));
        addMenu.setTooltip(new Tooltip("Add an attribute"));
        final ContextMenu addContextMenu = new ContextMenu();
        addMenu.setOnMouseClicked((MouseEvent event) -> {
            event.consume();
            addContextMenu.getItems().clear();
            if (elementType != null) {
                final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
                if (currentGraph != null) {
                    final Map<String, Set<SchemaAttribute>> categoryAttributes = new TreeMap<>();
                    final Set<SchemaAttribute> otherAttributes = new TreeSet<>();
                    final ReadableGraph rg = currentGraph.getReadableGraph();
                    try {
                        if (currentGraph.getSchema() != null) {
                            final SchemaFactory schemaFactory = currentGraph.getSchema().getFactory();
                            for (final SchemaAttribute attribute : schemaFactory.getRegisteredAttributes(elementType).values()) {
                                if (attribute.get(rg) == Graph.NOT_FOUND) {
                                    final Collection<SchemaConcept> concepts = SchemaConceptUtilities.getAttributeConcepts(attribute);
                                    if (concepts == null || concepts.isEmpty()) {
                                        otherAttributes.add(attribute);
                                    } else {
                                        for (final SchemaConcept concept : concepts) {
                                            Set<SchemaAttribute> attributeNames = categoryAttributes.get(concept.getName());
                                            if (attributeNames == null) {
                                                attributeNames = new TreeSet<>();
                                                categoryAttributes.put(concept.getName(), attributeNames);
                                            }
                                            attributeNames.add(attribute);
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        rg.release();
                    }

                    for (final Entry<String, Set<SchemaAttribute>> entry : categoryAttributes.entrySet()) {
                        final Menu submenu = new Menu(entry.getKey());
                        submenu.setStyle("-fx-text-fill: white;");
                        for (final SchemaAttribute attribute : entry.getValue()) {
                            final MenuItem item = new MenuItem(attribute.getName());
                            item.setOnAction((ActionEvent event1) -> {
                                PluginExecution.withPlugin(new AddAttributePlugin(attribute)).executeLater(currentGraph);
                            });
                            submenu.getItems().add(item);
                        }
                        addContextMenu.getItems().add(submenu);
                    }

                    if (!otherAttributes.isEmpty()) {
                        final Menu otherSubmenu = new Menu("Other");
                        for (final SchemaAttribute attribute : otherAttributes) {
                            final MenuItem item = new MenuItem(attribute.getName());
                            item.setOnAction((ActionEvent event1) -> {
                                PluginExecution.withPlugin(new AddAttributePlugin(attribute)).executeLater(currentGraph);
                            });
                            otherSubmenu.getItems().add(item);
                        }
                        addContextMenu.getItems().add(otherSubmenu);
                    }

                    final MenuItem customAttribute = new MenuItem("Custom");
                    customAttribute.setStyle("-fx-text-fill: white;");
                    customAttribute.setOnAction(ev -> {
                        createAttributeAction(elementType);
                    });
                    addContextMenu.getItems().add(customAttribute);
                }
            }
            addContextMenu.show(addMenu, event.getScreenX(), event.getScreenY());
        });

        final Button editKeyButton = new Button(null, new ImageView(UserInterfaceIconProvider.KEY.buildImage(16)));
        editKeyButton.setAlignment(Pos.CENTER);
        editKeyButton.setTextAlignment(TextAlignment.CENTER);
        editKeyButton.setStyle("-fx-background-color: #666666; -fx-background-radius: 2; -fx-font-size: 12; -fx-background-insets: 0, 0; -fx-padding: 0");
        editKeyButton.setPrefSize(18, 12);
        editKeyButton.setMaxSize(18, 12);
        editKeyButton.setPadding(new Insets(5));
        editKeyButton.setTooltip(new Tooltip("Edit primary key"));
        if (elementType != GraphElementType.GRAPH) {
            editKeyButton.setOnMouseClicked((MouseEvent event) -> {
                event.consume();
                editKeysAction(elementType);
            });
        } else {
            editKeyButton.setVisible(false);
        }

        optionsButtons.maxHeightProperty().bind(addMenu.heightProperty());
        optionsButtons.getChildren().addAll(showAllToggle, addMenu, editKeyButton);
//        optionsButtons.getChildren().addAll(showAllcb, addMenu, editKeyButton);
        headerGraphic.setLeft(heading);
        headerGraphic.setRight(optionsButtons);
        headerGraphic.prefWidthProperty().bind(scrollPane.widthProperty().subtract(45));
        BorderPane.setMargin(heading, new Insets(2, 0, 0, 0));
        BorderPane.setMargin(optionsButtons, Insets.EMPTY);
        result.setGraphic(headerGraphic);
        result.setId("heading");
        result.setExpanded(false);
        return result;
    }

    public void setFontSize() {
        currentFontSize = FontUtilities.getOutputFontSize();
        Platform.runLater(() -> {
            root.setStyle(String.format("-fx-font-size:%dpx;", currentFontSize));
        });
    }

    private class AddAttributePlugin extends SimpleEditPlugin {

        private final SchemaAttribute attribute;

        public AddAttributePlugin(SchemaAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public String getName() {
            return "Add Attribute";
        }

        @Override
        protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            attribute.ensure(graph);
        }
    }

    /**
     * Creates individual TitledPane within header title panes.
     *
     * @param attribute the attribute to display.
     * @param values the different values available for the attribute.
     * @param longestTitledWidth the width of the longest title in the pane.
     * @param hidden is the pane currently hidden.
     * @return a new TitledPane.
     */
    public TitledPane createAttributeTitlePane(AttributeData attribute, Object[] values, double longestTitledWidth, boolean hidden) {
        String attributeTitle = attribute.getAttributeName();
        final int spacing = 5;
        final int buttonSize = 45;
        final GridPane gridPane = new GridPane();
        gridPane.setHgap(spacing);
        final double titleWidth = longestTitledWidth + spacing;

        final AttributeTitledPane attributePane;
        if (!attribute.isKey()) {
            attributePane = new AttributeTitledPane(
                    e -> {
                        deleteAttributeAction(attribute.getElementType(), attributeTitle);
                    },
                    e -> {
                        modifyAttributeAction(attribute);
                    }
            );
        } else {
            attributePane = new AttributeTitledPane();
        }
        attributePane.setHidden(hidden);
        gridPane.prefWidthProperty().bind(attributePane.widthProperty());

        if (attribute.getDataType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME)) {
            attributePane.addMenuItem("Update time-zone of selection", e -> {
                updateTimeZoneAction(attribute);
            });
        }

        final boolean multiValue = values != null && values.length > 1;

        if (attribute.isKey()) {
            final String colour;
            if (hidden) {
                final ConstellationColor hiddenColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTE_COLOUR, HIDDEN_ATTRIBUTE_COLOUR));
                final ConstellationColor keyColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.PRIMARY_KEY_ATTRIBUTE_COLOUR, PRIMARY_KEY_ATTRIBUTE_COLOUR));
                colour = (ConstellationColor.getColorValue(hiddenColour.getRed() * 0.5f + keyColour.getRed() * 0.5f, hiddenColour.getGreen() * 0.5f + keyColour.getGreen() * 0.5f, hiddenColour.getBlue() * 0.5f + keyColour.getBlue() * 0.5f, 1f)).getHtmlColor();
            } else {
                colour = prefs.get(AttributePreferenceKey.PRIMARY_KEY_ATTRIBUTE_COLOUR, PRIMARY_KEY_ATTRIBUTE_COLOUR);
            }
            attributePane.setStyle(JavafxStyleManager.CSS_BASE_STYLE_PREFIX + colour + ";");
        } else if (!attribute.isSchema()) {
            final String colour;
            if (hidden) {
                final ConstellationColor hiddenColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTE_COLOUR, HIDDEN_ATTRIBUTE_COLOUR));
                final ConstellationColor customColour = ConstellationColor.fromHtmlColor(prefs.get(AttributePreferenceKey.CUSTOM_ATTRIBUTE_COLOUR, CUSTOM_ATTRIBUTE_COLOUR));
                colour = (ConstellationColor.getColorValue(hiddenColour.getRed() * 0.5f + customColour.getRed() * 0.5f, hiddenColour.getGreen() * 0.5f + customColour.getGreen() * 0.5f, hiddenColour.getBlue() * 0.5f + customColour.getBlue() * 0.5f, 1f)).getHtmlColor();
            } else {
                colour = prefs.get(AttributePreferenceKey.CUSTOM_ATTRIBUTE_COLOUR, CUSTOM_ATTRIBUTE_COLOUR);
            }
            attributePane.setStyle(JavafxStyleManager.CSS_BASE_STYLE_PREFIX + colour + ";");
        } else if (hidden) {
            final String hiddenColour = prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTE_COLOUR, HIDDEN_ATTRIBUTE_COLOUR);
            attributePane.setStyle(JavafxStyleManager.CSS_BASE_STYLE_PREFIX + hiddenColour + ";");
        } else {
            final String schemaColour = prefs.get(AttributePreferenceKey.SCHEMA_ATTRIBUTE_COLOUR, SCHEMA_ATTRIBUTE_COLOUR);
            attributePane.setStyle(JavafxStyleManager.CSS_BASE_STYLE_PREFIX + schemaColour + ";");
        }

        if (!multiValue) {
            attributePane.setCollapsible(false);
        } else {
            createMultiValuePane(attribute, attributePane, values);
        }

        Text attributeTitleText = createAttributeTitleLabel(attributeTitle);
        attributeTitleText.getStyleClass().add("attributeName");
        attributeTitleText.setTextAlignment(TextAlignment.RIGHT);

        //Value TextField
        Node attributeValueNode = createAttributeValueNode(values, attribute, attributePane, multiValue);

        //Edit Button
        Button editButton = new Button("Edit");
        editButton.setAlignment(Pos.CENTER);
        editButton.setMinWidth(buttonSize);
        AttributeValueEditorFactory<?> editorFactory = AttributeValueEditorFactory.getEditFactory(attribute.getDataType());
        if (editorFactory == null || values == null) {
            editButton.setDisable(true);
        } else {
            editButton.setOnMouseClicked(getEditValueHandler(attribute, editorFactory, values));
        }

        // If we don't do anything here, right-clicking on the Node will produce two context menus:
        // the one the Node has by default, and the one we added to the AttributeTitledPane.
        // We'll consume the context menu event so it doesn't bubble up to the TitledPane.
        // Ditto for the button.
        attributeValueNode.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        editButton.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);

        //Title
        ColumnConstraints titleConstraint = new ColumnConstraints(titleWidth);
        titleConstraint.setHalignment(HPos.RIGHT);

        //Value
        ColumnConstraints valueConstraint = new ColumnConstraints();
        valueConstraint.setHalignment(HPos.CENTER);
        valueConstraint.setHgrow(Priority.ALWAYS);
        valueConstraint.setFillWidth(true);

        //EditButton
        ColumnConstraints editConstraint = new ColumnConstraints(buttonSize);
        editConstraint.setHalignment(HPos.RIGHT);

        gridPane.getColumnConstraints().addAll(titleConstraint, valueConstraint, editConstraint);
        gridPane.add(attributeTitleText, 0, 0);
        gridPane.add(attributeValueNode, 1, 0);
        gridPane.add(editButton, 2, 0);

        attributePane.setAlignment(Pos.CENTER_RIGHT);
        attributePane.setGraphic(gridPane);
        attributePane.setTooltip(new Tooltip(attribute.getAttributeDescription()));

        attributePane.setExpanded(false);

        attributePane.setOnDragDetected(event -> {
            final Dragboard db = attributePane.startDragAndDrop(TransferMode.COPY);
            final ClipboardContent content = new ClipboardContent();
            final String data = String.format("%s:%s", attribute.getElementType().getShortLabel(), attribute.getAttributeName());
            content.put(ATTRIBUTE_NAME_DATA_FORMAT, data);
            content.putString(String.format("Attribute.Name=%s", data));
            db.setContent(content);

            event.consume();
        });

        return attributePane;

    }

    public void updateEditorPanel(final AttributeState state) {
        if (state != null) {
            Platform.runLater(() -> {
                clearHeaderTitledPanes();
                for (GraphElementType type : state.getGraphElements()) {
                    double longestTitleWidth = calcLongestTitle(state.getAttributeNames().get(type));
                    populateContentContainer(state, type, longestTitleWidth);
                }

                for (int i = 0; i < titledPaneHeadingsContainer.getChildren().size(); i++) {
                    TitledPane tp = (TitledPane) titledPaneHeadingsContainer.getChildren().get(i);
                    int count = ((VBox) tp.getContent()).getChildren().size();
                    final int totalAttrs = state.getAttributeCounts().get(ELEMENT_TYPES[i]);
                    final String attrCountDisplay = totalAttrs == count ? String.format(HEADING_TITLES[i], totalAttrs, "") : String.format(HEADING_TITLES[i], totalAttrs, String.format(HIDDEN_ATTRIBUTES_INFORMATION, totalAttrs - count));
                    headingTitleProperties[i].setValue(attrCountDisplay);
                    if (!state.getActiveGraphElements().isEmpty()) {
                        tp.setExpanded(state.getActiveGraphElements().contains(ELEMENT_TYPES[i]));
                    }
                }
            });
        }
    }

    /**
     * multi value pane showing multiple values for an attribute
     *
     * @param attribute
     * @param attributePane
     * @param values
     */
    private void createMultiValuePane(AttributeData attribute, TitledPane attributePane, Object[] values) {
        final VBox dataAndMoreButtonBox = new VBox(5); // 5 = spacing

        final ScrollPane multiValuePane = new ScrollPane();

        multiValuePane.setFitToWidth(true);

        final ObservableList<Object> listData = FXCollections.observableArrayList();

        if (values.length > VISIBLE_ROWS) {
            for (int i = 0; i < VISIBLE_ROWS; i++) {
                listData.add(values[i]);
            }
        } else {
            listData.addAll(values);
        }
        final ListView<Object> listView = createListView(attribute, listData);
        final boolean moreToLoad = values.length > VISIBLE_ROWS;
        int visibleRow = moreToLoad ? VISIBLE_ROWS : listData.size();
        listView.setPrefHeight((CELL_HEIGHT * visibleRow) + 2); // +2 because if it is == then there is still a scrollbar.
        multiValuePane.setPrefHeight((CELL_HEIGHT * visibleRow) + 1);
        multiValuePane.setContent(listView);
        multiValuePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        dataAndMoreButtonBox.setAlignment(Pos.CENTER);
        dataAndMoreButtonBox.setPadding(new Insets(0, 0, 5, 0));
        dataAndMoreButtonBox.getChildren().add(multiValuePane);
        if (moreToLoad) {
            Button loadMoreButton = createLoadMoreButton(dataAndMoreButtonBox, attribute);
            dataAndMoreButtonBox.getChildren().add(loadMoreButton);
        }
        dataAndMoreButtonBox.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.isShortcutDown() && (event.getCode() == KeyCode.A)) {
                listView.getSelectionModel().selectAll();
                event.consume();
            }
        });
        attributePane.setContent(dataAndMoreButtonBox);
    }

    private Button createLoadMoreButton(final VBox parent, final AttributeData attribute) {
        final Button loadMoreButton = new Button("Load all data");
        loadMoreButton.setPrefHeight(CELL_HEIGHT);
        loadMoreButton.setOnAction((ActionEvent t) -> {
            final Object[] moreData = topComponent.getMoreData(attribute);
            final ObservableList<Object> listData = FXCollections.observableArrayList();
            listData.addAll(moreData);

            final ListView<Object> newList = createListView(attribute, listData);
            parent.setPrefHeight(parent.getHeight() - loadMoreButton.getHeight());
            parent.getChildren().clear();
            parent.getChildren().add(newList);
        });
        return loadMoreButton;
    }

    private ListView<Object> createListView(final AttributeData attribute, ObservableList<Object> listData) {

        final ListView<Object> newList = new ListView<>(listData);
        newList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        newList.setCellFactory((ListView<Object> p) -> new AttributeValueCell(attribute.getDataType()));

        addCopyHandlersToListView(newList, attribute);
        newList.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (event.isShortcutDown() && (event.getCode() == KeyCode.C)) {
                copySelectedItems(newList, attribute.getDataType());
                event.consume();
            } else if (event.isShortcutDown() && (event.getCode() == KeyCode.A)) {
                event.consume();
            }
        });
        return newList;
    }

    private void copySelectedItems(final ListView<Object> list, final String dataType) {
        ObservableList<Object> selectedItems = list.getSelectionModel().getSelectedItems();
        AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(dataType);
        StringBuilder buffer = new StringBuilder();
        selectedItems.stream().map(item -> {
            if (item == null) {
                buffer.append(NO_VALUE_TEXT);
            } else {
                buffer.append(interaction.getDisplayText(item));
            }
            return item;
        }).forEach(_item -> buffer.append("\n"));

        ClipboardUtilities.copyToClipboard(buffer.toString());
    }

    /**
     * populates one of the header titled panes with attributes
     *
     * @param state containing the data of the attributes on the graph
     * @param type type of graphelement as each of these have their own headings
     * in the attribute calculator
     * @param longestTitleWidth used for layout calculations...(dodgy javafx
     * workaround)
     */
    private void populateContentContainer(AttributeState state, GraphElementType type, double longestTitleWidth) {
        int elementTypeIndex;
        boolean hidden = false;
//make into enum?
        switch (type) {
            case GRAPH:
                elementTypeIndex = 0;
                break;
            case VERTEX:
                elementTypeIndex = 1;
                break;
            case TRANSACTION:
                elementTypeIndex = 2;
                break;
            default:
                elementTypeIndex = -1;
                break;
        }
        if (elementTypeIndex > -1 && state != null) {
            ArrayList<AttributeData> attributeDataList = state.getAttributeNames().get(type);
            if (attributeDataList != null) {
                VBox header = valueTitledPaneContainers.get(elementTypeIndex);
                String hiddenAttributes = prefs.get(AttributePreferenceKey.HIDDEN_ATTRIBUTES, "");
                List<String> hiddenAttrList = StringUtilities.splitLabelsWithEscapeCharacters(hiddenAttributes, AttributePreferenceKey.SPLIT_CHAR_SET);
                Set<String> hiddenAttrSet = new HashSet<>(hiddenAttrList);

                currentAttributeNames.put(type, new ArrayList<>());
                List<String> attrNameList = currentAttributeNames.get(type);

                for (AttributeData data : attributeDataList) {
                    hidden = hiddenAttrSet.contains(data.getElementType().toString() + data.getAttributeName());
                    Object[] values = state.getAttributeValues().get(type.getLabel() + data.getAttributeName());
                    attrNameList.add(data.getAttributeName());
                    TitledPane attribute = createAttributeTitlePane(data, values, longestTitleWidth, hidden);
                    attribute.setMinWidth(0);
                    attribute.maxWidthProperty().bind(header.widthProperty());

                    header.getChildren().add(attribute);
                }
            }
        }
    }

    public void resetPanel() {
        Platform.runLater(() -> {
            clearHeaderTitledPanes();
            for (Node n : titledPaneHeadingsContainer.getChildren()) {
                TitledPane tp = (TitledPane) n;
                tp.setExpanded(false);
            }
        });
    }

    private void clearHeaderTitledPanes() {
        for (VBox box : valueTitledPaneContainers) {
            box.getChildren().clear();

        }

    }

    private void deleteAttributeAction(GraphElementType elementType, String attributeName) {
        SimpleEditPlugin deleteAttributePlugin = new SimpleEditPlugin() {

            @Override
            public String getName() {
                return "Attribute Editor: Remove Attribute";
            }

            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                graph.removeAttribute(graph.getAttribute(elementType, attributeName));
            }
        };
        PluginExecution.withPlugin(deleteAttributePlugin).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    private void createAttributeAction(GraphElementType elementType) {
        final EditOperation editOperation = new CreateAttributeEditOperation();
        final List<String> extantAttributeNames = currentAttributeNames.get(elementType);
        final ValueValidator<AttributePrototype> validator = v -> {
            return extantAttributeNames.contains(v.getAttributeName()) ? "An attribute with that name already exists." : null;
        };
        final AbstractEditor<AttributePrototype> editor = ATTRIBUTE_EDITOR_FACTORY.createEditor(editOperation, validator, String.format("Create %s attribute", elementType.getShortLabel()), AttributePrototype.getBlankPrototype(elementType));

        ((AttributeEditor) editor).setGraphElementType(elementType);
        ((AttributeEditor) editor).setTypeModifiable(true);
        final AttributeEditorDialog dialog = new AttributeEditorDialog(false, editor);
        dialog.showDialog();
    }

    private void modifyAttributeAction(final AttributeData attr) {
        final EditOperation editOperation = new ModifyAttributeEditOperation(attr);
        final List<String> extantAttributeNames = currentAttributeNames.get(attr.getElementType());
        final ValueValidator<AttributePrototype> validator = v -> {
            return extantAttributeNames.contains(v.getAttributeName()) && !attr.getAttributeName().equals(v.getAttributeName()) ? "An attribute with that name already exists." : null;
        };
        final AbstractEditor<AttributePrototype> editor = ATTRIBUTE_EDITOR_FACTORY.createEditor(editOperation, validator, String.format("Modify %s attribute %s", attr.getElementType().getShortLabel(), attr.getAttributeName()), attr);

        ((AttributeEditor) editor).setGraphElementType(attr.getElementType());
        ((AttributeEditor) editor).setTypeModifiable(false);
        final AttributeEditorDialog dialog = new AttributeEditorDialog(false, editor);
        dialog.showDialog();
    }

    private void updateTimeZoneAction(final AttributeData attr) {
        final EditOperation editOperation = zoneId -> 
                PluginExecution.withPlugin(new UpdateTimeZonePlugin((ZoneId) zoneId, attr)).executeLater(GraphManager.getDefault().getActiveGraph());
        final AbstractEditor<ZoneId> editor = UPDATE_TIME_ZONE_EDITOR_FACTORY.createEditor(editOperation, String.format("Set time-zone for attribute %s", attr.getAttributeName()), TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId());
        final AttributeEditorDialog dialog = new AttributeEditorDialog(true, editor);
        dialog.showDialog();
    }

    private void editKeysAction(GraphElementType elementType) {
        final List<String> currentKeyAttributes = new ArrayList<>();
        final List<String> allAttributes = new ArrayList<>();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                int[] keys = rg.getPrimaryKey(elementType);
                for (int key : keys) {
                    currentKeyAttributes.add(rg.getAttributeName(key));
                }
                for (int i = 0; i < rg.getAttributeCount(elementType); i++) {
                    allAttributes.add(rg.getAttributeName(rg.getAttribute(elementType, i)));
                }
            } finally {
                rg.release();
            }
            final EditOperation editOperation = new PrimaryKeyEditOperation(elementType);
            final DefaultGetter<List<String>> defaultGetter = new PrimaryKeyDefaultGetter(elementType);
            final AbstractEditor<List<String>> editor = LIST_SELECTION_EDITOR_FACTORY.createEditor(editOperation, defaultGetter, String.format("Edit primary key for %ss", elementType.getShortLabel()), currentKeyAttributes);
            ((ListSelectionEditor) editor).setPossibleItems(allAttributes);
            final AttributeEditorDialog dialog = new AttributeEditorDialog(true, editor);
            dialog.showDialog();
        }
    }

    private EventHandler<MouseEvent> getEditValueHandler(final AttributeData attributeData, final AttributeValueEditorFactory editorFactory, final Object[] values) {
        return e -> {
            final Object value = values.length == 1 ? values[0] : null;
            final AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(attributeData.getDataType());
            final String editType = editorFactory.getAttributeType();
            final AttributeValueTranslator fromTranslator = interaction.fromEditTranslator(editType);
            final AttributeValueTranslator toTranslator = interaction.toEditTranslator(editType);
            final ValueValidator<?> validator = interaction.fromEditValidator(editType);
            final EditOperation editOperation = new AttributeValueEditOperation(attributeData, completeWithSchemaItem.isSelected(), fromTranslator);
            final DefaultGetter<?> defaultGetter = attributeData::getDefaultValue;
            final AbstractEditor<?> editor = editorFactory.createEditor(editOperation, defaultGetter, validator, attributeData.getAttributeName(), toTranslator.translate(value));
            final AttributeEditorDialog dialog = new AttributeEditorDialog(true, editor);
            dialog.showDialog();
        };
    }

    private double getTextWidth(final String text) {
        // we need to manually scale the width using the font size against what we guess is the default, even though this seems unecessary.
        final Text t = new Text(text);
        t.getStyleClass().add("attributeName");
        return t.getLayoutBounds().getWidth() * (currentFontSize / 10.0);
    }

    private double calcLongestTitle(ArrayList<AttributeData> attributeData) {
        double maxWidth = 0;
        double currWidth = 0;
        if (attributeData != null) {
            for (AttributeData data : attributeData) {
                currWidth = getTextWidth(data.getAttributeName());
                if (maxWidth < currWidth) {
                    maxWidth = currWidth;
                }
            }
        }
        return maxWidth;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
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

    private Node createAttributeValueNode(final Object[] values, final AttributeData attribute, final AttributeTitledPane parent, final boolean multiValue) {

        boolean noneSelected = values == null;
        boolean isNull = !noneSelected && (values[0] == null);
        parent.setAttribute(attribute);

        AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(attribute.getDataType());

        final String displayText;
        final List<Node> displayNodes;
        if (multiValue) {
            displayText = "<Multiple Values>";
            displayNodes = Collections.emptyList();
        } else if (isNull) {
            displayText = NO_VALUE_TEXT;
            displayNodes = Collections.emptyList();
        } else if (noneSelected) {
            displayText = "<Nothing Selected>";
            displayNodes = Collections.emptyList();
        } else {
            displayText = interaction.getDisplayText(values[0]);
            displayNodes = interaction.getDisplayNodes(values[0], -1, CELL_ITEM_HEIGHT);
        }

        parent.setAttributeValue(displayText);
        final TextField attributeValueText = new TextField(displayText);
        attributeValueText.setEditable(false);
        if (noneSelected || isNull || multiValue) {
            attributeValueText.getStyleClass().add("undisplayedValue");
        }

        if (displayNodes.isEmpty()) {
            return attributeValueText;
        }

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER_RIGHT);
        gridPane.setPadding(Insets.EMPTY);
        gridPane.setHgap(CELL_ITEM_SPACING);
        ColumnConstraints displayNodeConstraint = new ColumnConstraints(CELL_ITEM_HEIGHT);
        displayNodeConstraint.setHalignment(HPos.LEFT);
        ColumnConstraints displayTextConstraint = new ColumnConstraints();
        displayTextConstraint.setHalignment(HPos.RIGHT);
        displayTextConstraint.setHgrow(Priority.ALWAYS);
        displayTextConstraint.setFillWidth(true);

        for (int i = 0; i < displayNodes.size(); i++) {
            final Node displayNode = displayNodes.get(i);
            gridPane.add(displayNode, i, 0);
            gridPane.getColumnConstraints().add(displayNodeConstraint);
        }

        gridPane.add(attributeValueText, displayNodes.size(), 0);
        gridPane.getColumnConstraints().add(displayTextConstraint);

        return gridPane;
    }

    private class AttributeValueCell extends ListCell<Object> {

        private final String attrDataType;

        public AttributeValueCell(final String attrDataType) {
            this.attrDataType = attrDataType;
        }

        @Override
        public void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            AbstractAttributeInteraction<?> interaction = AbstractAttributeInteraction.getInteraction(attrDataType);
            final String displayText;
            final List<Node> displayNodes;
            if (item == null) {
                displayText = NO_VALUE_TEXT;
                displayNodes = Collections.emptyList();
            } else {
                displayText = interaction.getDisplayText(item);
                displayNodes = interaction.getDisplayNodes(item, -1, CELL_HEIGHT - 1);
            }

            GridPane gridPane = new GridPane();
            gridPane.setHgap(CELL_ITEM_SPACING);
            ColumnConstraints displayNodeConstraint = new ColumnConstraints(CELL_HEIGHT - 1);
            displayNodeConstraint.setHalignment(HPos.CENTER);

            for (int i = 0; i < displayNodes.size(); i++) {
                final Node displayNode = displayNodes.get(i);
                gridPane.add(displayNode, i, 0);
                gridPane.getColumnConstraints().add(displayNodeConstraint);
            }

            setGraphic(gridPane);
            setPrefHeight(CELL_HEIGHT);
            setText(displayText);
        }
    }

    private Text createAttributeTitleLabel(final String attributeTitle) {
        //Title
        return new Text(attributeTitle + ":");
//        attributeTitleText.setStyle(String.format("-fx-font-size: %dpt;", fontSize));
    }

}
