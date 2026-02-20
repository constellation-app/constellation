/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQuery;
import au.gov.asd.tac.constellation.views.layers.query.BitMaskQueryCollection;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.openide.util.HelpCtx;

/**
 * Utilities class for layers view 
 *
 * @author aldebaran30701
 */
public class LayersUtilities {

    private static final Logger LOGGER = Logger.getLogger(LayersUtilities.class.getName());

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
        final Button helpDocumentationButton = new Button("", new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.SKY.getJavaColor())));
        helpDocumentationButton.paddingProperty().set(HELP_PADDING);
        helpDocumentationButton.setTooltip(new Tooltip("Display help for Layers View"));
        helpDocumentationButton.setOnAction(event -> new HelpCtx(LayersViewTopComponent.class.getName()).display());

        // Get rid of the ugly button look so the icon stands alone.
        helpDocumentationButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent; -fx-effect: null; ");

        return helpDocumentationButton;
    }

    public static void selectVisibleElements(final boolean selectionSetting){
        final Thread elementSelecter = new Thread(() -> directSelectVisibleElements(selectionSetting));
        elementSelecter.start();
    }
    
    public static void selectLayerElements(final int layerBitMap, final boolean selectionSetting, final boolean includeHidden) {
        final Thread layerSelecter = new Thread(() -> directSelectLayerElements(layerBitMap, selectionSetting, includeHidden));
        layerSelecter.start();
    }

    public static void allocateElementsForLayer(final int layerBitMap, final boolean allocationSetting, final boolean includeHidden) {
        final Thread layerSelecter = new Thread(() -> directAllocateElementsForLayer(layerBitMap, allocationSetting, includeHidden));
        layerSelecter.start();
    }

    public static void directAllocateElementsForLayer(final int layerBitMap, final boolean allocationSetting, final boolean includeHidden) {
        try {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            final WritableGraph wg = graph.getWritableGraph((allocationSetting ? "" : "De-") + "Allocate Elements with Layer Bitmap " + layerBitMap, true);
            final int selectedVertexID = VisualConcept.VertexAttribute.SELECTED.get(wg);
            final int selectedTransactionID = VisualConcept.TransactionAttribute.SELECTED.get(wg);
            final int vxLayerMaskAttr = LayersConcept.VertexAttribute.LAYER_MASK.get(wg);
            final int txLayerMaskAttr = LayersConcept.TransactionAttribute.LAYER_MASK.get(wg);
            final int vxbitmaskVisibilityAttrId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(wg);
            final int txbitmaskVisibilityAttrId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(wg);

            // Determine how many elements to scan
            final int vertexCount = wg.getVertexCount();
            final int transactionCount = wg.getTransactionCount();

            // Select matching Vertexs
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = wg.getVertex(vertexPosition);
                final boolean allowUpdate = includeHidden || wg.getFloatValue(vxbitmaskVisibilityAttrId, vertexId) == 1.0;
                if (allowUpdate && wg.getBooleanValue(selectedVertexID, vertexId)) {
                    final int actualBitmap = wg.getIntValue(vxLayerMaskAttr, vertexPosition);
                    final int removalBitmap = (actualBitmap & layerBitMap) == layerBitMap ? actualBitmap - layerBitMap : actualBitmap;
                    final int updatedBitmap = allocationSetting ? actualBitmap | layerBitMap : removalBitmap;
                    if (updatedBitmap != actualBitmap) {
                        wg.setIntValue(vxLayerMaskAttr, vertexId, updatedBitmap);
                    }
                }
            }

            // Select matching Transactions
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int txnId = wg.getTransaction(transactionPosition);
                boolean allowUpdate = includeHidden || wg.getFloatValue(txbitmaskVisibilityAttrId, txnId) == 1.0;
                if (allowUpdate && wg.getBooleanValue(selectedTransactionID, txnId)) {
                    final int actualBitmap = wg.getIntValue(txLayerMaskAttr, transactionPosition);
                    final int removalBitmap = (actualBitmap & layerBitMap) == layerBitMap ? actualBitmap - layerBitMap : actualBitmap;
                    final int updatedBitmap = allocationSetting ? actualBitmap | layerBitMap : removalBitmap;
                    if (updatedBitmap != actualBitmap) {
                        wg.setIntValue(txLayerMaskAttr, txnId, updatedBitmap);
                    }
                }
            }
            wg.commit();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "LayersUtilities.allocateElementsForLayer interrupted ...", ex);
            Thread.currentThread().interrupt();
        }
    }

    public static void directSelectVisibleElements(final boolean selectionSetting){
        try {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            final WritableGraph wg = graph.getWritableGraph((selectionSetting ? "" : "De-") + "Select All Visible Layer Elements", true);
            final int selectedVertexID = VisualConcept.VertexAttribute.SELECTED.get(wg);
            final int selectedTransactionID = VisualConcept.TransactionAttribute.SELECTED.get(wg);
            final int vxbitmaskVisibilityAttrId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(wg);
            final int txbitmaskVisibilityAttrId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(wg);

            // Determine how many elements to scan
            final int vertexCount = wg.getVertexCount();
            final int transactionCount = wg.getTransactionCount();

            // Select visible Vertices
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                if (wg.getFloatValue(vxbitmaskVisibilityAttrId, wg.getVertex(vertexPosition)) == 1.0) {
                    wg.setBooleanValue(selectedVertexID, wg.getVertex(vertexPosition), selectionSetting);
                }
            }
            // Select visible Transactions
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                if (wg.getFloatValue(txbitmaskVisibilityAttrId, wg.getVertex(transactionPosition)) == 1.0) {
                    wg.setBooleanValue(selectedTransactionID, wg.getTransaction(transactionPosition), selectionSetting);
                }
            }
            wg.commit();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "LayersUtilities.selectVisibleElements interrupted ...", ex);
            Thread.currentThread().interrupt();
        }
    }
    
    public static void directSelectLayerElements(final int layerBitMap, final boolean selectionSetting, final boolean includeHidden) {        
        try {                
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            final WritableGraph wg = graph.getWritableGraph((selectionSetting ? "" : "De-") + "Select Elements with Layer Bitmap " + layerBitMap, true);
            final int selectedVertexID = VisualConcept.VertexAttribute.SELECTED.get(wg);
            final int selectedTransactionID = VisualConcept.TransactionAttribute.SELECTED.get(wg);
            final int vxLayerMaskAttr = LayersConcept.VertexAttribute.LAYER_MASK.get(wg);
            final int txLayerMaskAttr = LayersConcept.TransactionAttribute.LAYER_MASK.get(wg);
            final int vxbitmaskVisibilityAttrId = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(wg);
            final int txbitmaskVisibilityAttrId = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(wg);

            // Determine how many elements to scan
            final int vertexCount = wg.getVertexCount();
            final int transactionCount = wg.getTransactionCount();
            final int stateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(wg);
            LayersViewState currentState = wg.getObjectValue(stateAttributeId, 0);

            long activeVQueriesMask = 0;
            List<Boolean> backupVVis = new ArrayList<>();
            long activeTQueriesMask = 0;
            List<Boolean> backupTVis = new ArrayList<>();
            if (currentState != null) {
                backupVVis = currentState.getVxQueriesCollection().getVisibilityList();
                activeVQueriesMask = currentState.getVxQueriesCollection().getActiveQueriesBitmask();
                final int bitValue = (int) (Math.log(layerBitMap)/ Math.log(2));
                List<Boolean> tempVis = new ArrayList<>();
                for (int i = 0; i < backupVVis.size(); i++) {
                    tempVis.add((i == bitValue));
                }
                currentState.getVxQueriesCollection().setVisibilities(tempVis);
                currentState.getVxQueriesCollection().setActiveQueries(layerBitMap);
                currentState.getVxQueriesCollection().selectMatchingElements(wg, true, selectionSetting);


                backupTVis = currentState.getTxQueriesCollection().getVisibilityList();
                activeTQueriesMask = currentState.getTxQueriesCollection().getActiveQueriesBitmask();
                tempVis.clear();
                for (int i = 0; i < backupVVis.size(); i++) {
                    tempVis.add((i == bitValue));
                }
                currentState.getTxQueriesCollection().setVisibilities(tempVis);
                currentState.getTxQueriesCollection().setActiveQueries(layerBitMap);
                currentState.getTxQueriesCollection().selectMatchingElements(wg, false, selectionSetting);
            }

            // Select matching Vertexs
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                boolean allowUpdate = includeHidden || wg.getFloatValue(vxbitmaskVisibilityAttrId, wg.getVertex(vertexPosition)) == 1.0;
                if (allowUpdate && (wg.getIntValue(vxLayerMaskAttr, wg.getVertex(vertexPosition)) & layerBitMap) == layerBitMap) {
                    wg.setBooleanValue(selectedVertexID, wg.getVertex(vertexPosition), selectionSetting);
                }
            }

            // Select matching Transactions
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                boolean allowUpdate = includeHidden || wg.getFloatValue(txbitmaskVisibilityAttrId, wg.getVertex(transactionPosition)) == 1.0;
                if (allowUpdate && (wg.getIntValue(txLayerMaskAttr, wg.getTransaction(transactionPosition)) & layerBitMap) == layerBitMap) {
                    wg.setBooleanValue(selectedTransactionID, wg.getTransaction(transactionPosition), selectionSetting);
                }
            }

            if (currentState != null) {
                currentState.getVxQueriesCollection().setVisibilities(backupVVis);                    
                currentState.getVxQueriesCollection().setActiveQueries(activeVQueriesMask);
                currentState.getVxQueriesCollection().update(wg);

                currentState.getTxQueriesCollection().setVisibilities(backupTVis);                    
                currentState.getTxQueriesCollection().setActiveQueries(activeTQueriesMask);
                currentState.getTxQueriesCollection().update(wg);
            }
            wg.commit();
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "LayersUtilities.selectLayerElements interrupted ...", ex);
            Thread.currentThread().interrupt();
        }
    }
    
}
