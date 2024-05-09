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
package au.gov.asd.tac.constellation.views.find.components.advanced.utilities;

import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.find.components.advanced.IconCriteriaPanel;
import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This class contains a window for selecting an icon.
 *
 * @author Atlas139mkm
 */
public class IconSelector extends Stage {

    private final IconCriteriaPanel parentComponent;

    private final VBox vbox = new VBox();
    private final HBox hbox = new HBox();
    private TreeView<String> categoryTreeView;
    private ListView<IconNode> iconListView;
    private ConstellationIcon icon;

    private final TreeItem<String> rootNode = new TreeItem<>("Icons");

    private final BorderPane buttonsBp = new BorderPane();
    private final HBox buttonsHbox = new HBox();
    private final Button selectButton = new Button("Select");
    private final Button cancelButton = new Button("Cancel");

    public IconSelector(final IconCriteriaPanel parentComponent, final ConstellationIcon icon) {
        this.parentComponent = parentComponent;
        this.icon = icon;
        setContent();
        setAlwaysOnTop(true);

        categoryTreeView.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue)
                -> populateIconsList(newValue.getValue())
        );

        // if a icon icon node is selected, save the icon and close the window
        selectButton.setOnAction(action -> {
            if (iconListView.getSelectionModel().getSelectedItem() != null) {
                parentComponent.getSelectedIcon();
                close();
            }
        });

        cancelButton.setOnAction(action
                -> close()
        );
    }

    /**
     * Sets the UI content for the window
     */
    private void setContent() {
        setTitle("Icon Selector");
        final BorderPane bp = new BorderPane();
        vbox.getChildren().add(hbox);

        populateCategoryTree();
        hbox.getChildren().addAll(categoryTreeView, iconListView);

        bp.setBottom(buttonsBp);

        buttonsHbox.getChildren().addAll(selectButton, cancelButton);
        buttonsBp.setRight(buttonsHbox);

        buttonsHbox.setSpacing(5);
        buttonsHbox.setPadding(new Insets(0, 10, 10, 0));

        vbox.setAlignment(Pos.CENTER);

        bp.setCenter(vbox);
        final Scene scene = new Scene(bp);
        scene.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());

        setScene(scene);

    }

    /**
     * Populates the tree containing all the icon categories
     */
    private void populateCategoryTree() {
        iconListView = new ListView<>();
        iconListView.setMinWidth(275);
        categoryTreeView = new TreeView<>(rootNode);
        categoryTreeView.setMinWidth(275);

        hbox.setPadding(new Insets(10));

        /**
         * For each constellation icon get its extended name, split it at "."
         * and add the first split to the categories set (It is a set to avoid
         * duplicate categories being added). For example if the extended name
         * is Flag.Australia. Flag will be saved as a category.
         */
        final List<String> categories = new ArrayList<>();
        for (final ConstellationIcon constellationIcon : IconManager.getIcons()) {
            final String[] splitName = constellationIcon.getExtendedName().split("\\.");

            categories.add(splitName[0]);
        }
        sort(categories);
        final Set<String> sortedCategories = new LinkedHashSet<>();
        sortedCategories.addAll(categories);

        final String[] iconName = icon.getExtendedName().split("\\.");

        /**
         * For each of the categories create a TreeItem and add it to rootNode
         */
        for (final String categoryName : sortedCategories) {
            final TreeItem<String> categoryTreeNode = new TreeItem<>(categoryName);
            rootNode.getChildren().add(categoryTreeNode);
            if (categoryName.equals(iconName[0])) {
                populateIconsList(categoryName);
                categoryTreeView.getSelectionModel().select(categoryTreeNode);
            }
        }
        rootNode.setExpanded(true);
    }

    /**
     * Populates the Icon list with the icons of the selected category. For
     * example electing Flags will populate the list with all the flag icons.
     *
     * @param categoryName
     */
    private void populateIconsList(final String categoryName) {
        iconListView.getItems().clear();

        /**
         * For each of the icons split the extended name at the "." to determine
         * what category its in. If its category matches the selected
         * categoryName add it to the list of iconNames.
         */
        final List<String> iconNames = new ArrayList<>();
        for (final ConstellationIcon constellationIcon : IconManager.getIcons()) {
            if (constellationIcon.getExtendedName().split("\\.")[0].equals(categoryName)) {
                iconNames.add(constellationIcon.getName());
            }
        }
        // sort the icons in alpabetical order
        sort(iconNames);
        final String[] iconName = icon.getExtendedName().split("\\.");

        // for each of those iconNames find the icon and create an IconNode
        for (final String name : iconNames) {
            IconNode node = new IconNode(IconManager.getIcon(name));
            iconListView.getItems().add(node);

            if ((iconName.length > 1 && name.equals(iconName[1])) || (iconName.length == 1 && name.equals(iconName[0]))) {
                iconListView.getSelectionModel().select(node);
            }
        }
    }

    /**
     * Gets the parent component, which is the IconCriteriaPanel
     *
     * @return
     */
    public IconCriteriaPanel getParentComponent() {
        return parentComponent;
    }

    /**
     * Gets the currently selected iconNodes icon
     *
     * @return
     */
    public ConstellationIcon selectIcon() {
        return iconListView.getSelectionModel().getSelectedItem().getIcon();
    }

}
