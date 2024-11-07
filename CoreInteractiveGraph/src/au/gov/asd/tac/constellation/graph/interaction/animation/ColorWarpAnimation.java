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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.SetColorValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.HashMap;
import java.util.Map;

/**
 * Cause the colors on the graph to warp through hues.
 * 
 * @author capricornunicorn123
 */
public final class ColorWarpAnimation extends Animation {
    
    public static final String NAME = "Color Warp Animation";
    
    // Attribut references
    private int vertexColorAttr;
    private int transactionColorAttr;

    // The current destination vertex.
    private final Map<Integer, ConstellationColor> vertexOriginals = new HashMap<>();
    private final Map<Integer, ConstellationColor> transactionOriginals = new HashMap<>();

    public ColorWarpAnimation() {

    }

    @Override
    public void initialise(final GraphWriteMethods wg) {
        vertexColorAttr = VisualConcept.VertexAttribute.COLOR.get(wg);
        transactionColorAttr = VisualConcept.TransactionAttribute.COLOR.get(wg);
         
        // dont initilise the animation if there is less than 2 nodes
        if (wg.getVertexCount() <= 1) {
            stop();
        } else{
            for (int vertexPosition = 0 ; vertexPosition < wg.getVertexCount(); vertexPosition++) {
                final int vertexID = wg.getVertex(vertexPosition);
                vertexOriginals.put(vertexID, wg.getObjectValue(vertexColorAttr, vertexID));
            }
            
            for (int transactionPosition = 0 ; transactionPosition < wg.getTransactionCount(); transactionPosition++) {
                final int transactionID = wg.getTransaction(transactionPosition);
               transactionOriginals.put(transactionID, wg.getObjectValue(transactionColorAttr, transactionID));
            }            
        }
    }

    @Override
    public void animate(final GraphWriteMethods wg) {
        
        final SetColorValuesOperation colorVerticesOperation = new SetColorValuesOperation(wg, GraphElementType.VERTEX, vertexColorAttr);
        final SetColorValuesOperation colorTransactionsOperation = new SetColorValuesOperation(wg, GraphElementType.TRANSACTION, transactionColorAttr);

        // Do not animate unless there is more than 1 node
        if (wg.getVertexCount() > 0) {
            
            for (int vertexPosition = 0 ; vertexPosition < wg.getVertexCount(); vertexPosition++) {
                final int vertexID = wg.getVertex(vertexPosition);
                colorVerticesOperation.setValue(vertexID, this.getNextColor(wg.getObjectValue(vertexColorAttr, vertexID)));
                wg.executeGraphOperation(colorVerticesOperation);        
            }
            
            for (int transactionPosition = 0 ; transactionPosition < wg.getTransactionCount(); transactionPosition++) {
                final int transactionID = wg.getTransaction(transactionPosition);
                colorTransactionsOperation.setValue(transactionID, this.getNextColor(wg.getObjectValue(transactionColorAttr, transactionID)));
                wg.executeGraphOperation(colorTransactionsOperation);
            }
        }
    }

    @Override
    public void reset(final GraphWriteMethods wg) {
        
        // Reset Verticies back to their original color
        for (int vertexPosition = 0 ; vertexPosition < wg.getVertexCount(); vertexPosition++) {
            final int vertexID = wg.getVertex(vertexPosition);
            wg.setObjectValue(vertexColorAttr, vertexID, vertexOriginals.get(vertexID));
        }
         
        // Reset Transactions back to their original color
        for (int transactionPosition = 0 ; transactionPosition < wg.getTransactionCount(); transactionPosition++) {
            final int transactionID = wg.getTransaction(transactionPosition);
            wg.setObjectValue(transactionColorAttr, transactionID, transactionOriginals.get(transactionID));
        }
    }

    @Override
    public long getIntervalInMillis() {
        return 15;
    }

    private ConstellationColor getNextColor(final ConstellationColor color) {
   
        javafx.scene.paint.Color col = color.getJavaFXColor();
        double hue = col.getHue() <= 360 ? col.getHue() + 5 : 0;
        double sat = col.getSaturation();
        double bright = col.getBrightness();
        javafx.scene.paint.Color newCol = javafx.scene.paint.Color.hsb(hue, sat, bright);
        
        return ConstellationColor.fromFXColor(newCol);
    }

    @Override
    protected String getName() {
        return NAME;
    }
    
    @Override
    public void setFinalFrame(final GraphWriteMethods wg){
        //Do Nothing
    }
}
