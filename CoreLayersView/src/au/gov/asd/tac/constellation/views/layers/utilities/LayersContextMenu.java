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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javax.swing.JOptionPane;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ContextMenuProvider.class, position = 1100)
public class LayersContextMenu implements ContextMenuProvider {

    private static final String LAYERS_MENU = "Layers";
    private static final String ADD_TO_LAYER = "Add Selection to Layer";
    private static final String REMOVE_FROM_LAYER = "Remove Selection from Layer";
    
    private int enteredResult = 1;
    
    
    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList(LAYERS_MENU);
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (elementType == GraphElementType.VERTEX || elementType == GraphElementType.TRANSACTION) {
            return Arrays.asList(ADD_TO_LAYER, REMOVE_FROM_LAYER);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected) {
        switch (item) {
            case ADD_TO_LAYER:
                Platform.runLater(() -> {
                    final TextInputDialog layerInput = new TextInputDialog(); 
                    layerInput.setHeaderText("Enter a layer to add to");

                    final Optional<String> result = layerInput.showAndWait();
                    if (result.isPresent() && !layerInput.getEditor().getText().isBlank() && !layerInput.getEditor().getText().equals("1")) {
                        enteredResult = Integer.parseInt(layerInput.getEditor().getText());                
                        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(enteredResult, LayerAction.ADD))
                                .executeLater(GraphManager.getDefault().getActiveGraph());
                    } else{
                        JOptionPane.showMessageDialog(null, "Error: Cannot add to Layer 1");
                    }
                }); 
                break;
            case REMOVE_FROM_LAYER:
                Platform.runLater(() -> {
                    final TextInputDialog layerInput = new TextInputDialog(); 
                    layerInput.setHeaderText("Enter a layer to remove from");

                    final Optional<String> result = layerInput.showAndWait();
                    if (result.isPresent() && !layerInput.getEditor().getText().isBlank() && !layerInput.getEditor().getText().equals("1")) {
                        enteredResult = Integer.parseInt(layerInput.getEditor().getText());                
                        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(enteredResult, LayerAction.REMOVE))
                                .executeLater(GraphManager.getDefault().getActiveGraph());
                    } else{
                        JOptionPane.showMessageDialog(null, "Error: Cannot remove from Layer 1");
                    }
                }); 
                break;
            default:
                break;
        }
    }
}