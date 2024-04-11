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
package au.gov.asd.tac.constellation.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * An adaption of the {@link PluginInteraction} Interface to facilitate the interactions that are being contributed to by multiple runnable tasks.
 * This implementation 
 * Weaknesses, interactions run across different thread pools.
 * 
 * @author capricornunicorn123
 */
public class MultiTaskInteraction {
    public final PluginInteraction interaction;
    public final List<SharedInteractionRunnable> tasks = new ArrayList<>();
    
    
    public MultiTaskInteraction(final PluginInteraction interaction, final List<SharedInteractionRunnable> tasks){
        this.interaction = interaction;
        this.tasks.addAll(tasks);
    }
    
    public MultiTaskInteraction(final PluginInteraction interaction){
        this.interaction = interaction;
    }
    
    /**
     * Registers a tasks to this MultiTaskInteraction.
     * @param task 
     */
    public void addTask(SharedInteractionRunnable task){
        tasks.add(task);
    }
    
    /**
     * Updates the progress of an interaction based on the size and level of completion of registered tasks.
     * @param cancellable
     * @throws InterruptedException 
     */
    public void setProgress(final boolean cancellable) throws InterruptedException {

        int currentStep = 0;
        int totalSteps = 0;
        for (SharedInteractionRunnable task : tasks) {
            currentStep += task.getCurrentStep();
            totalSteps += task.getTotalSteps();
        }
        interaction.setProgress(currentStep, totalSteps, cancellable);
        
        // Give progress reporting mechanisms time to update before the calling class is able to update progress again
        Thread.sleep(500);
    }

    /**
     * Determines if all tasks registered have completed.
     * @return 
     */
    public boolean isComplete() {
        return tasks.stream().noneMatch(task -> (!task.isComplete()));
    }
    
    /**
     * An Interface for runnable tasks that share a common Interaction.
     * This interface enables registration of tasks to the MultiTaskInteraction.
     */
    public interface SharedInteractionRunnable extends Runnable{
    
        /**
         * The total discrete steps anticipated for this task.
         * @return 
         */
        public int getTotalSteps();   

        /**
         * How many steps have completed.
         * @return 
         */
        public int getCurrentStep();
        
        /**
         * Determines if the registered tasks are complete.
         * @return 
         */
        public boolean isComplete();
    
    }
    
}
