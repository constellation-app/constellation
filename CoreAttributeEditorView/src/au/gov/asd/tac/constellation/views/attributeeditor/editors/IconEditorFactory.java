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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.FileIconData;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeValueEditorFactory.class)
public class IconEditorFactory extends AttributeValueEditorFactory<ConstellationIcon> {

    @Override
    public AbstractEditor<ConstellationIcon> createEditor(final EditOperation editOperation, final DefaultGetter<ConstellationIcon> defaultGetter, final ValueValidator<ConstellationIcon> validator, final String editedItemName, final ConstellationIcon initialValue) {
        return new IconEditor(editOperation, defaultGetter, validator, editedItemName, initialValue);
    }

    @Override
    public String getAttributeType() {
        return IconAttributeDescription.ATTRIBUTE_NAME;
    }

    public class IconEditor extends AbstractEditor<ConstellationIcon> {

        private static final int BUTTON_SPACING = 10;

        private ListView<String> listView;
        private TreeView<IconNode> treeView;
        private TreeItem<IconNode> treeRoot;
        private TreeItem<IconNode> builtInItem;

        protected IconEditor(final EditOperation editOperation, final DefaultGetter<ConstellationIcon> defaultGetter, final ValueValidator<ConstellationIcon> validator, final String editedItemName, final ConstellationIcon initialValue) {
            super(editOperation, defaultGetter, validator, editedItemName, initialValue);
        }

        @Override
        public void updateControlsWithValue(final ConstellationIcon value) {
            if (value != null) {
                reloadUserDefinedIcons(value.getExtendedName());
                listView.getSelectionModel().select(value.getExtendedName());
            } else {
                reloadUserDefinedIcons(null);
            }
        }

        @Override
        protected ConstellationIcon getValueFromControls() {
            if (listView.getSelectionModel().isEmpty()) {
                return null;
            }
            return IconManager.getIcon(listView.getSelectionModel().getSelectedItem());
        }

        @Override
        protected Node createEditorControls() {
            final GridPane controls = new GridPane();
            controls.setAlignment(Pos.CENTER);
            controls.setVgap(CONTROLS_DEFAULT_VERTICAL_SPACING);

            final ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            controls.getColumnConstraints().add(cc);
            final RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            controls.getRowConstraints().add(rc);

            // build tree structure of icon
            final IconNode builtInNode = new IconNode("(Built-in)", IconManager.getIconNames(false));

            //convert structure to jfx treeview
            builtInItem = new TreeItem<>(builtInNode);
            addNode(builtInItem, builtInNode);

            // set listview factory to display icon
            listView = new ListView<>();
            listView.setCellFactory(param -> new IconNodeCell());
            listView.getStyleClass().add("rounded");
            listView.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
                update();
            });

            treeRoot = new TreeItem<>(new IconNode("Icons", new HashSet<>()));
            treeRoot.setExpanded(true);

            treeView = new TreeView<>();
            treeView.setShowRoot(true);
            treeView.setRoot(treeRoot);
            treeView.getStyleClass().add("rounded");
            treeView.setOnMouseClicked((MouseEvent event) -> {
                refreshIconList();
            });

            final SplitPane splitPane = new SplitPane();
            splitPane.setId("hiddenSplitter");
            splitPane.setOrientation(Orientation.HORIZONTAL);
            splitPane.getItems().add(treeView);
            splitPane.getItems().add(listView);
            controls.addRow(0, splitPane);

            final HBox addRemoveBox = createAddRemoveBox();
            controls.addRow(1, addRemoveBox);

            return controls;
        }

        private void reloadUserDefinedIcons(final String iconFile) {
            final IconNode userNode = new IconNode("(User-defined)", IconManager.getIconNames(true));
            final TreeItem<IconNode> userItem = new TreeItem<>(userNode);
            this.addNode(userItem, userNode);
            treeRoot.getChildren().clear();
            treeRoot.getChildren().add(builtInItem);
            treeRoot.getChildren().add(userItem);
            if (iconFile != null && !iconFile.isEmpty()) {
                TreeItem<IconNode> currentIcon = findIconNode(treeRoot, iconFile);
                treeView.getSelectionModel().select(currentIcon);
            }
            refreshIconList(iconFile);
        }

        private List<File> pngWalk(File path) {
            final List<File> files = new ArrayList<>();
            pngWalk(path, files);
            return files;
        }

        private List<File> pngWalk(final File path, final List<File> files) {
            final File[] filesInPath = path.listFiles((File pathname) -> {
                if (pathname.isDirectory()) {
                    return true;
                } else {
                    final String filename = pathname.getAbsolutePath();
                    return filename.endsWith(".png") || filename.endsWith(".PNG");
                }
            });
            for (final File file : filesInPath) {
                if (file.isDirectory()) {
                    pngWalk(file, files);
                } else {
                    files.add(file);
                }
            }
            return files;
        }

        private TreeItem<IconNode> findIconNode(TreeItem<IconNode> node, String value) {
            final IconNode iconNode = node.getValue();
            if (iconNode.iconExists(value)) {
                return node;
            } else {
                for (TreeItem<IconNode> child : node.getChildren()) {
                    final TreeItem<IconNode> result = findIconNode(child, value);
                    if (result != null) {
                        return result;
                    }
                }
                return null;
            }

        }

        private void refreshIconList() {
            refreshIconList(null);
        }

        private void refreshIconList(final String iconFile) {
            listView.getItems().clear();
            if (treeView.getSelectionModel().getSelectedItem() != null) {
                final IconNode node = treeView.getSelectionModel().getSelectedItem().getValue();
                if (node != null) {
                    final String[] icons = treeView.getSelectionModel().getSelectedItem().getValue().getIconLabels();
                    listView.setItems(FXCollections.observableArrayList(icons));
                    listView.getSelectionModel().clearSelection();
                    if (iconFile != null) {
                        listView.getSelectionModel().select(iconFile);
                        Platform.runLater(() -> {
                            listView.scrollTo(iconFile);
                            listView.requestFocus();
                        });
                    }
                }
            }
        }

        private void addNode(TreeItem<IconNode> item, IconNode node) {
            if (node.getChildren().length == 0) {
                return;
            }
            for (IconNode childNode : node.getChildren()) {
                final TreeItem<IconNode> childItem = new TreeItem<>(childNode);
                item.getChildren().add(childItem);
                addNode(childItem, childNode);
            }
        }

        private HBox createAddRemoveBox() {
            //add/remove button
            final HBox addRemoveBox = new HBox(BUTTON_SPACING);
            addRemoveBox.setAlignment(Pos.CENTER_RIGHT);
            final Button addFilesButton = new Button("Add File(s)...");
            final Button addDirButton = new Button("Add Directory...");
            final Button removeButton = new Button("Remove");
            addFilesButton.setOnAction(event -> {
                final FileChooser addIconChooser = new FileChooser();
                addIconChooser.setTitle("Add new Icon(s)");
                addIconChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG images", "*.*png", "*.*PNG"));
                final List<File> selectedFiles = addIconChooser.showOpenMultipleDialog(null);
                String lastIconName = null;
                if (selectedFiles != null) {
                    for (File file : selectedFiles) {
                        final String iconName = file.getName().substring(0, file.getName().length() - 4);
                        final ConstellationIcon customIcon = new ConstellationIcon.Builder(iconName, new FileIconData(file)).build();
                        if (IconManager.addIcon(customIcon)) {
                            lastIconName = file.getName().substring(0, file.getName().length() - 4);
                        }
                    }
                    if (lastIconName != null) {
                        reloadUserDefinedIcons(lastIconName);
                    }
                }
            });
            addDirButton.setOnAction(event -> {
                final DirectoryChooser addFolderChooser = new DirectoryChooser();
                addFolderChooser.setTitle("Add new Icons");
                final File selectedFolder = addFolderChooser.showDialog(null);
                if (selectedFolder != null) {
                    final List<File> selectedFiles = pngWalk(selectedFolder);
                    if (!selectedFiles.isEmpty()) {
                        String lastIconFile = null;
                        for (final File file : selectedFiles) {
                            final String iconName = file.getName().substring(0, file.getName().length() - 4);
                            final ConstellationIcon customIcon = new ConstellationIcon.Builder(iconName, new FileIconData(file)).build();
                            if (IconManager.addIcon(customIcon)) {
                                lastIconFile = file.getName().substring(0, file.getName().length() - 4);
                            }
                        }
                        if (lastIconFile != null) {
                            reloadUserDefinedIcons(lastIconFile);
                        }
                    }
                }
            });
            removeButton.setOnAction(event -> {
                final boolean iconRemoved = IconManager.removeIcon(listView.getSelectionModel().getSelectedItem());
                if (iconRemoved) {
                    reloadUserDefinedIcons("");
                }
            });
            addRemoveBox.getChildren().addAll(addFilesButton, addDirButton, removeButton);
            return addRemoveBox;
        }
    }

    private class IconNodeCell extends ListCell<String> {

        private static final double RECT_SIZE = 56;
        private static final double SPACING = 20;

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                final GridPane gridPane = new GridPane();
                gridPane.setHgap(0);
                gridPane.setAlignment(Pos.TOP_LEFT);

                // icon
                final ConstellationIcon icon = IconManager.getIcon(item);
                final Image iconImage = icon.buildImage();
                final ImageView imageView = new ImageView(iconImage);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(RECT_SIZE);
                final ColumnConstraints titleConstraint = new ColumnConstraints(RECT_SIZE);
                titleConstraint.setHalignment(HPos.CENTER);
                gridPane.getColumnConstraints().addAll(titleConstraint);
                gridPane.add(imageView, 0, 0);

                // dimension text
                if (iconImage != null) {
                    final int width = (int) (iconImage.getWidth());
                    final int height = (int) (iconImage.getHeight());
                    final Text dimensionText = new Text(String.format("(%dx%d)", width, height));
                    dimensionText.setFill(Color.web("#d3d3d3"));
                    gridPane.add(dimensionText, 0, 1);
                }

                // icon name
                final String displayableItem = icon.getExtendedName();
                final String[] splitItem = displayableItem.split("\\.");
                String iconName = splitItem[splitItem.length - 1];
                if (iconName.isEmpty()) {
                    iconName = "(no icon)";
                }
                this.setText(iconName);

                // tooltip
                final Tooltip tt = new Tooltip(item);
                this.setTooltip(tt);

                this.setGraphic(gridPane);
                this.setPrefHeight(RECT_SIZE + SPACING);
            } else {
                this.setText(null);
                this.setGraphic(null);
            }
        }
    }

    public class IconNode {

        private final String name;
        private final IconNode parent;
        private final TreeSet<String> icons = new TreeSet<>();
        private final TreeMap<String, IconNode> children = new TreeMap<>();

        public IconNode(String name, IconNode parent) {
            this.name = name;
            this.parent = parent;
        }

        public IconNode(String name, Set<String> iconLabels) {
            this.name = name;
            this.parent = null;
            for (final String iconName : iconLabels) {
                final String[] splitString = iconName.split("\\.");
                if (splitString.length == 1) {
                    icons.add(iconName);
                } else if (splitString.length > 1) {
                    IconNode currentNode = this;
                    // every element up to the last element is a category, the last element is the icon name.
                    for (int i = 0; i < splitString.length - 1; i++) {
                        final IconNode childNode = new IconNode(splitString[i], currentNode);
                        final IconNode tempNode = currentNode.addChild(childNode);
                        if (tempNode == null) {
                            currentNode = childNode;
                        } else {
                            currentNode = tempNode;
                        }
                    }
                    currentNode.icons.add(iconName);
                }
            }
        }

        public int getIconCount() {
            return icons.size();
        }

        public String[] getIconLabels() {
            String[] type = new String[0];
            return icons.toArray(type);
        }

        public IconNode getParent() {
            return parent;
        }

        public IconNode[] getChildren() {
            IconNode[] type = new IconNode[0];
            return children.values().toArray(type);
        }

        public IconNode addChild(IconNode child) {
            return children.get(child.getName()) == null ? children.put(child.getName(), child) : children.get(child.getName());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IconNode other = (IconNode) obj;
            return (this.getName() == null) ? (other.getName() == null) : this.name.equals(other.name);
        }

        @Override
        public String toString() {
            return getName();
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        public boolean iconExists(String iconName) {
            return icons.contains(iconName);
        }
    }
}
