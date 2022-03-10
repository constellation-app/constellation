/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.openide.util.HelpCtx;

/**
 *
 * @author aldebaran30701
 */
public class LayersUtilities {

    private static final Insets HELP_PADDING = new Insets(2, 0, 0, 0);

    private LayersUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static int calculateCurrentLayerSelectionBitMask(final BitMaskQueryCollection vxQueriesCollection, final BitMaskQueryCollection txQueriesCollection) {
        int newBitmask = 0b0;
        final int iteratorEnd = Math.max(vxQueriesCollection.getHighestQueryIndex(), txQueriesCollection.getHighestQueryIndex());
        for (int position = 0; position <= iteratorEnd; position++) {
            final BitMaskQuery vxQuery = vxQueriesCollection.getQuery(position);
            final BitMaskQuery txQuery = txQueriesCollection.getQuery(position);

            if (vxQuery != null) {// can use vx
                newBitmask |= vxQuery.isVisible() ? (1 << vxQuery.getIndex()) : 0;
            } else if (txQuery != null) {// have to use tx
                newBitmask |= txQuery.isVisible() ? (1 << txQuery.getIndex()) : 0;
            } else {
                // cannot use any.
            }
        }
        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        if (newBitmask == 0) {
            newBitmask = 0b1;
        } else if (newBitmask > 1) {
            newBitmask = newBitmask & ~0b1;
        } else {
            // Do nothing
        }

        return newBitmask;
    }

    /**
     * Add a new additional layer if the space permits. Display a message if
     * there is no space.
     *
     * @param state
     */
    public static void addLayer(final LayersViewState state) {
        state.addLayer();
    }

    /**
     * Add a layer with a certain description
     *
     * @param state
     * @param description
     */
    public static void addLayer(final LayersViewState state, final String description) {
        state.addLayer(description);
    }

    /**
     * Add a layer at a certain position. Will override the description of a
     * layer if the position was taken. If the position is open, a new layer
     * will be added with the description.
     *
     * @param state - the state to alter
     * @param description the layer description
     * @param layerNumber the layer to add to
     */
    public static void addLayerAt(final LayersViewState state, final String description, final int layerNumber) {
        state.addLayerAt(layerNumber, description);
    }

    public static Button createHelpButton() {
        final Button helpDocumentationButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())));
        helpDocumentationButton.paddingProperty().set(HELP_PADDING);
        helpDocumentationButton.setTooltip(new Tooltip("Display help for Layers View"));
        helpDocumentationButton.setOnAction(event -> new HelpCtx(LayersViewTopComponent.class.getName()).display());

        // Get rid of the ugly button look so the icon stands alone.
        helpDocumentationButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");

        return helpDocumentationButton;
    }
}
