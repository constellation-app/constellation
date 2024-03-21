/*
* Copyright 2010-2023 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The glass responsible for the manipulation of a graph to produce the animation effect.
 * Each graph will only have one GraphAnimator.
 * @author capricornunicorn123
 */
public class GraphAnimator implements Runnable{
    
    private boolean running = false;
    private final Map<String, Animation> pendingAnimations = new HashMap<String, Animation>();
    private final Map<String, Animation> activeAnimations = new HashMap<String, Animation>();
    private final Graph graphNode;
    private final String animatorId;
    
    public GraphAnimator(Graph graphNode) {
        this.graphNode = graphNode;
        this.animatorId = String.format("%s-Graph Animator", graphNode.getGraph().getId());
    }

    public void addAnimation(final Animation animation) {
        final String animationName = animation.getName();
        if (!pendingAnimations.containsKey(animationName) && !activeAnimations.containsKey(animationName)){
            pendingAnimations.put(animationName, animation);
        }
    }
    
    public void removeAnimation(final Animation animation){
        this.pendingAnimations.remove(animation.getName());
        
    }
    
    public void stopAnimatingNow() {
        
    }
    
    public void stopAnimating() {
        
    }
    
    /**
     * Handles the variables that persist for the life-cycle of the animation.
     */
    @Override
    public void run() {
        running = true;
        if (this.graphNode != null) {
            while(!pendingAnimations.isEmpty() && !activeAnimations.isEmpty()) {
                try {
                    WritableGraph wg = graphNode.getWritableGraph(animatorId, true);
                    
                    initialisePendingAnimations(wg);

                    animateFrame(wg);
                    
                    wg.commit();
                    
                    Thread.sleep(getIntervalInMillis());
                    
                } catch (InterruptedException ex) {

//                } finally {
//                    if (lockGraphSafely(graph)) {                    
//                        reset(wg);
//                        wg.commit();
//                    }
//                    AnimationUtilities.notifyComplete(this);
                }     
            }          
        }
        running = false;
    }
    
    public boolean isRunning(){
        return running;
    }
    
    private void initialisePendingAnimations(WritableGraph wg) {
        pendingAnimations.keySet().forEach(animationName -> {
            Animation animation = pendingAnimations.get(animationName);
            animation.initialise(wg);
            activeAnimations.put(animationName, animation);
        });
        
        pendingAnimations.clear();
    }
    
    private void animateFrame(WritableGraph rg){
        
        List<ThreeTuple<Integer, Integer, Object>> allGraphWrites = new ArrayList<>();
        List<String> finnishedAnimations = new ArrayList<>();
        
        // Animate eatch frame of the runing animations
        for (String animationName : activeAnimations.keySet()) {
            List<ThreeTuple<Integer, Integer, Object>> animationGraphWrites = activeAnimations.get(animationName).animate(rg);
            if (animationGraphWrites != null){
                allGraphWrites.addAll(animationGraphWrites);
            } else {
                finnishedAnimations.add(animationName);
            }
        }
        
        // Write to the graph
        allGraphWrites.forEach(writeDetails -> {
            rg.setObjectValue(writeDetails.getFirst(), writeDetails.getSecond(), writeDetails.getThird());
        });
        
        // Clear FinnishedAnimations
        finnishedAnimations.forEach(finnishedAnimation -> {
            activeAnimations.remove(finnishedAnimation);
        });        
    }
    
    /**
     * Get the interval in milliseconds between each frame of the animation.
     * Note: Standard film frame rate is 24 fps which equates to 41.6 milliseconds per frame  
     *
     * @return The interval between frames for this animation.
     */
    public long getIntervalInMillis() {
        return 35;
    }

    void stopAnimation(String animationName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
