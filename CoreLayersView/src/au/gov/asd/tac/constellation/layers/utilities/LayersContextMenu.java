/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import org.openide.util.lookup.ServiceProvider;

/**
 * Add, delete selections to layers.
 * 
 * @author sol695510
 */

@ServiceProvider(service = ContextMenuProvider.class, position = 1100)
public class LayersContextMenu implements ContextMenuProvider {

    private static final String LAYERS_MENU = "Layers";
    private static final String ADD_REMOVE_LAYER = "Add/Remove Selection from Layer";
    private int enteredResult = 1;
    
    
    @Override
    public List<String> getMenuPath(GraphElementType elementType) {
        return Arrays.asList(LAYERS_MENU);
    }

    @Override
    public List<String> getItems(GraphReadMethods graph, GraphElementType elementType, int elementId) {
        if (elementType == GraphElementType.VERTEX || elementType == GraphElementType.TRANSACTION) {
            return Arrays.asList(ADD_REMOVE_LAYER);
        } else {
            return null;
        }
    }

    @Override
    public void selectItem(String item, Graph graph, GraphElementType elementType, int elementId, Vector3f unprojected) {
        switch (item) {
            case ADD_REMOVE_LAYER:
                Platform.runLater(() -> {
                TextInputDialog td = new TextInputDialog(); 
                td.setHeaderText("Enter a layer to add to");
                
                Optional<String> result = td.showAndWait();
                if (result.isPresent() && !td.getEditor().getText().equals("")) {
                    enteredResult = Integer.parseInt(td.getEditor().getText());                
                    PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(enteredResult)).executeLater(GraphManager.getDefault().getActiveGraph());
                }
            }); 
                break;
            default:
                break;
        }
    }
}