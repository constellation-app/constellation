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
package au.gov.asd.tac.constellation.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * An adaptation of the {@link PluginInteraction} interface to facilitate interactions that are 
 * contributed to by multiple runnable tasks.
 * 
 * <p>This class does not substitute the need for plugins to communicate with the {@link PluginInteraction} but 
 * instead provides a simple way of updating the {@link PluginInteraction} from multiple threads.</P>
 * 
 * <p>This implementation has strengths and weaknesses that contributors should be aware of when using. 
 * Unlike single threaded uses of the {@link PluginInteraction}, the running plugin does not update by 
 * calling methods directly on the PluginInteraction. Instead, all running Tasks will implement the 
 * {@link SharedInteractionRunnable} and the MultiTaskInteraction will request updates on the 
 * completeness of their execution. It is then the MultiTaskInteraction that collates the status of all
 * running tasks and updates the {@link PluginInteraction} of execution completeness.</p>
 * 
 * <p>As {@link PluginInteraction} instances expect to be updated chronologically. Each step should be finished 
 * before commencing to the next step. To achieve this, the method {@code waitForTasksToComplete()} should 
 * be called after all tasks are scheduled so that the calling thread is forced to wait for all tasks to complete 
 * and prevent external calls to the {@link PluginInteraction}.</p>
 * 
 * @author capricornunicorn123
 */
public class MultiTaskInteraction {
    private final PluginInteraction interaction;
    private final List<SharedInteractionRunnable> tasks = new ArrayList<>();
    
    
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
    public void addTask(final SharedInteractionRunnable task){
        tasks.add(task);
    }
    
    /**
     * Determines how many tasks have been registered to this MultiTaskInteraction.
     * 
     * @return 
     */
    public int getTaskCount(){
        return tasks.size();
    }
    
    /**
     * Updates the PluginInteraction of the progress of the running tasks and continues to do so until all tasks are complete.
     * This method is intended to block further execution on the calling thread until these tasks have completed.
     * 
     * @throws InterruptedException 
     */
    public void waitForTasksToComplete() throws InterruptedException {
        // Wait until all tasks are complete
        while (!isComplete()){
            setProgress(true);
            
            // Give progress reporting mechanisms time to update before the calling class is able to update progress again
            Thread.sleep(500);
        }
    }
    
    /**
     * Updates the progress of an interaction based on the size and level of completion of registered tasks.
     * @param cancellable
     * @throws InterruptedException 
     */
    private void setProgress(final boolean cancellable) throws InterruptedException {
        int currentStep = 0;
        int totalSteps = 0;
        for (SharedInteractionRunnable task : tasks) {
            currentStep += task.getCurrentStep();
            totalSteps += task.getTotalSteps();
        }
        interaction.setProgress(currentStep, totalSteps, cancellable);
    }

    /**
     * Determines if all tasks registered have completed.
     * @return 
     */
    private boolean isComplete() {
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
