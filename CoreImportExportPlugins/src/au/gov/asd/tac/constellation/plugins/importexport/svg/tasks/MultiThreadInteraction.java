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
package au.gov.asd.tac.constellation.plugins.importexport.svg.tasks;

import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author capricornunicorn123
 */
public class MultiThreadInteraction {
    public final PluginInteraction interaction;
    public final List<ThreadWithCommonPluginInteraction> threads = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(MultiThreadInteraction.class.getName());
    
    
    public MultiThreadInteraction(final PluginInteraction interaction, final List<ThreadWithCommonPluginInteraction> threads){
        this.interaction = interaction;
        this.threads.addAll(threads);
    }
    
    public MultiThreadInteraction(final PluginInteraction interaction){
        this.interaction = interaction;
    }
    
    public void addThread(ThreadWithCommonPluginInteraction thread){
        threads.add(thread);
    }
    
    public void setProgress(final boolean cancellable) throws InterruptedException{
        int currentStep = 0;
        int totalSteps = 0;
        for (ThreadWithCommonPluginInteraction thread : threads) {
            currentStep += thread.getCurrentStep();
            totalSteps += thread.getTotalSteps();
        }
        interaction.setProgress(currentStep, totalSteps, cancellable);
    }

    public boolean isComplete() {
        return threads.stream().noneMatch(thread -> (!thread.isComplete()));
    }

    public void interrupt() {
        threads.forEach(thread -> {
            thread.interrupt();
        });
    }
    
}
