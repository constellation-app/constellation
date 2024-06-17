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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.plugins.MultiTaskInteraction.SharedInteractionRunnable;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link MultiTaskInteraction}
 * 
 * @author capricornunicorn123
 */
public class MultiTaskInteractionNGTest {
    
    private PluginInteraction interactionMock;
    private ExecutorService threadPool;
    
    public MultiTaskInteractionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        interactionMock = mock(TextPluginInteraction.class);
        
        doNothing().when(interactionMock).setProgress(anyInt(), anyInt(), anyBoolean());
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        threadPool.shutdown();
    }

    /**
     * Test of addTask method, of class MultiTaskInteraction.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testTaskRegistration() throws InterruptedException {
        
        final MultiTaskInteraction mti = new MultiTaskInteraction(interactionMock);
        
        assertEquals(mti.getTaskCount(), 0);
        
        // Add a task to the MultiTaskInteraction
        final TestTask task1 = new TestTask();
        mti.addTask(task1);
        assertEquals(mti.getTaskCount(), 1);
        
        // Add 2 more tasks to the MultiTaskInteraction
        final TestTask task2 = new TestTask();
        final TestTask task3 = new TestTask();
        mti.addTask(task2);
        mti.addTask(task3);
        assertEquals(mti.getTaskCount(), 3);
        
        //Run these tasks
        threadPool.submit(task1);
        threadPool.submit(task2);
        threadPool.submit(task3);
        
        //Wait for the tasks to be completed
        mti.waitForTasksToComplete();
        
        //Ensure the MultiTaskInteraction has references to the complted tasks
        assertEquals(mti.getTaskCount(), 3);
    }
    
    
    private class TestTask implements SharedInteractionRunnable{
        private int currentStep = 0;
        private final int totalSteps = 10;

        @Override
        public int getTotalSteps() {
            return totalSteps;
        }

        @Override
        public int getCurrentStep() {
            return currentStep;
        }

        @Override
        public boolean isComplete() {
            return currentStep > totalSteps;
        }

        @Override
        public void run() {
            while(currentStep <= totalSteps) {
                currentStep++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
