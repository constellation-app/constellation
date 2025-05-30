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
package au.gov.asd.tac.constellation.utilities.visual;

import au.gov.asd.tac.constellation.utilities.memory.MemoryManager;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class VisualManagerNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getVisualComponent method, of class VisualManager.
     */
    @Test
    public void testGetVisualComponent() {
        System.out.println("getVisualComponent");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final Component canvas = mock(Component.class);
            when(processor.getCanvas()).thenReturn(canvas);

            final VisualAccess access = mock(VisualAccess.class);
            final VisualManager instance = new VisualManager(access, processor);

            final Component result = instance.getVisualComponent();
            assertEquals(result, canvas);
            verify(processor, times(1)).getCanvas();
        }
    }

    /**
     * Test of destroy method, of class VisualManager.
     */
    @Test
    public void testDestroy() {
        System.out.println("destroy");

        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            final VisualProcessor processor = mock(VisualProcessor.class);
            doNothing().when(processor).stopVisualising();
            doNothing().when(processor).destroyCanvas();

            final VisualAccess access = mock(VisualAccess.class);
            final VisualManager instance = new VisualManager(access, processor);

            instance.destroy();

            // Verify that the processor had the following methods called.
            verify(processor, times(1)).stopVisualising();
            verify(processor, times(1)).destroyCanvas();
        }
    }

    /**
     * Test of startProcessing method, of class VisualManager.
     */
    @Test
    public void testStartProcessing() {
        System.out.println("startProcessing");
        try (final MockedStatic<CompletableFuture> completableFutureMockedStatic = mockStatic(CompletableFuture.class)) {
            final CompletableFuture<Void> processingFuture = mock(CompletableFuture.class);
            completableFutureMockedStatic.when(() -> CompletableFuture.runAsync(Mockito.any(Runnable.class), Mockito.any(ExecutorService.class))).thenReturn(processingFuture);
            try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
                memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
                memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
                final VisualProcessor processor = mock(VisualProcessor.class);
                final VisualAccess access = mock(VisualAccess.class);
                final VisualManager instance = new VisualManager(access, processor);

                // trigger state to being stopped
                instance.stopProcessing();
                assertTrue(instance.isRendererIdle()); // defaults to true
                assertFalse(instance.isProcessing());

                // call method in test
                instance.startProcessing();

                // Verify the state
                assertTrue(instance.isProcessing());
                assertTrue(instance.isRendererIdle());
                assertSame(instance.getProcessingFuture(), processingFuture);
                completableFutureMockedStatic.verify(() -> CompletableFuture.runAsync(Mockito.any(Runnable.class), Mockito.any(ExecutorService.class)), times(1));
            }
        }
    }

    /**
     * Test of testProcess method, of class VisualManager.
     *
     * TODO: This method currently runs in a loop as the tested method contains a while true.
     */
    @Test
    public void testProcess() {
        System.out.println("testProcess");

        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);

            final VisualProcessor processor = mock(VisualProcessor.class);
            doNothing().when(processor).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());

            final VisualManager instance = spy(new VisualManager(access, processor));
            final PriorityBlockingQueue<VisualOperation> queue = new PriorityBlockingQueue<>();

            final VisualChange vc = mock(VisualChange.class);

            final List<VisualChange> changes = new ArrayList<>();
            final VisualOperation op = mock(VisualOperation.class);
            changes.add(vc);
            when(op.getVisualChanges()).thenReturn(changes);
            doNothing().when(op).apply();
            queue.add(op);
            when(instance.getOperations()).thenReturn(queue);
            when(instance.isRendererIdle()).thenReturn(true);
            when(instance.isProcessing()).thenReturn(true, false, false);
            when(instance.getProcessor()).thenReturn(processor);

            instance.process();

            verify(op, times(1)).apply();
            verify(op, times(1)).getVisualChanges();
            verify(instance, times(3)).isProcessing();
            verify(instance, Mockito.atLeastOnce()).getOperations();
            verify(processor, times(0)).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());
        }
    }

    @Test
    public void testProcess2() {
        System.out.println("testProcess2");

        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);

            final VisualProcessor processor = mock(VisualProcessor.class);
            doNothing().when(processor).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());

            final VisualManager instance = spy(new VisualManager(access, processor));
            final PriorityBlockingQueue<VisualOperation> queue = new PriorityBlockingQueue<>();
            final PriorityBlockingQueue<VisualOperation> queue2 = new PriorityBlockingQueue<>();

            final VisualChange vc = mock(VisualChange.class);

            final List<VisualChange> changes2 = new ArrayList<>();
            final VisualOperation op = mock(VisualOperation.class);
            changes2.add(vc);
            when(op.getVisualChanges()).thenReturn(changes2);
            doNothing().when(op).apply();
            queue.add(instance.indigenousChangesUpdateOperation);
            queue2.add(op);
            when(instance.getOperations()).thenReturn(queue, queue2);
            when(instance.isRendererIdle()).thenReturn(true);
            when(instance.isProcessing()).thenReturn(true, false, false);
            when(instance.getProcessor()).thenReturn(processor);

            instance.process();

            verify(instance, times(3)).isProcessing();
            verify(instance, Mockito.atLeastOnce()).getOperations();
            verify(processor, times(0)).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());
        }
    }

    /**
     * Test of stopProcessing method, of class VisualManager.
     */
    @Test
    public void testStopProcessing() {
        System.out.println("stopProcessing");

        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualAccess access = mock(VisualAccess.class);
            final VisualManager instance = spy(new VisualManager(access, processor));
            doNothing().when(instance).cancelProcessing(Mockito.eq(true));

            instance.stopProcessing();

            // Verify that the processor had the following methods called.
            verify(instance).cancelProcessing(Mockito.eq(true));
            assertFalse(instance.isProcessing());
        }
    }

    /**
     * Test of addOperation method, of class VisualManager.
     */
    @Test
    public void testAddOperation() {
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualAccess access = mock(VisualAccess.class);
            final VisualOperation op = mock(VisualOperation.class);
            final VisualManager instance = new VisualManager(access, processor);
            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(op);

            instance.addOperation(op);

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
            assertTrue(queueAfter.contains(op));
        }
    }

    /**
     * Test of exportToImage method, of class VisualManager.
     */
    @Test
    public void testExportToImage() {
        System.out.println("exportToImage");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final File imageFile = mock(File.class);
            final VisualOperation op = mock(VisualOperation.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            when(processor.exportToImage(Mockito.eq(imageFile))).thenReturn(op);
            final VisualManager instance = new VisualManager(access, processor);
            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(op);

            instance.exportToImage(imageFile);

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
            assertTrue(queueAfter.contains(op));
            verify(processor, times(1)).exportToImage(Mockito.eq(imageFile));
        }
    }

    /**
     * Test of exportToBufferedImage method, of class VisualManager.
     */
    @Test
    public void testExportToBufferedImage() {
        System.out.println("exportToBufferedImage");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final BufferedImage[] imageFile = new BufferedImage[555];
            final Semaphore waiter = mock(Semaphore.class);

            final VisualOperation op = mock(VisualOperation.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            when(processor.exportToBufferedImage(Mockito.eq(imageFile), Mockito.eq(waiter))).thenReturn(op);
            final VisualManager instance = new VisualManager(access, processor);

            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(op);

            instance.exportToBufferedImage(imageFile, waiter);

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
            assertTrue(queueAfter.contains(op));
            verify(processor, times(1)).exportToBufferedImage(Mockito.eq(imageFile), Mockito.eq(waiter));
        }
    }

    /**
     * Test of refreshVisualProcessor method, of class VisualManager.
     */
    @Test
    public void testRefreshVisualProcessor() {
        System.out.println("refreshVisualProcessor");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualManager instance = new VisualManager(access, processor);
            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(instance.refreshProcessorOperation);

            instance.refreshVisualProcessor();

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
            assertTrue(queueAfter.contains(instance.refreshProcessorOperation));
        }
    }

    /**
     * Test of updateFromIndigenousChanges method, of class VisualManager.
     */
    @Test
    public void testUpdateFromIndigenousChanges() {
        System.out.println("updateFromIndigenousChanges");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualManager instance = new VisualManager(access, processor);
            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(instance.indigenousChangesUpdateOperation);

            instance.updateFromIndigenousChanges();

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
            assertTrue(queueAfter.contains(instance.indigenousChangesUpdateOperation));
        }
    }

    /**
     * Test of addSingleChangeOperation method, of class VisualManager.
     */
    @Test
    public void testAddSingleChangeOperation() {
        System.out.println("addSingleChangeOperation");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualChange vc = mock(VisualChange.class);
            final VisualManager instance = new VisualManager(access, processor);
            final VisualOperation op = instance.constructSingleChangeOperation(vc);
            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(op);

            instance.addSingleChangeOperation(vc);

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
        }
    }

    /**
     * Test of addMultiChangeOperation method, of class VisualManager.
     */
    @Test
    public void testAddMultiChangeOperation() {
        System.out.println("addMultiChangeOperation");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final List<VisualChange> changes = new ArrayList<>();
            final VisualAccess access = mock(VisualAccess.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualChange vc = mock(VisualChange.class);
            final VisualManager instance = new VisualManager(access, processor);
            final VisualOperation op = instance.constructSingleChangeOperation(vc);
            changes.add(vc);

            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(op);

            instance.addMultiChangeOperation(changes);

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
        }
    }

    /**
     * Test of signifyProcessorIdle method, of class VisualManager.
     */
    @Test
    public void testSignifyProcessorIdle() {
        System.out.println("signifyProcessorIdle");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final VisualProcessor processor = mock(VisualProcessor.class);
            final VisualManager instance = new VisualManager(access, processor);
            final PriorityBlockingQueue<VisualOperation> queueBefore = instance.getOperations();
            final int size = queueBefore.size();
            final boolean containsOP = queueBefore.contains(instance.signifyProcessorIdleOperation);

            instance.signifyProcessorIdle();

            final PriorityBlockingQueue<VisualOperation> queueAfter = instance.getOperations();

            assertEquals(queueAfter.size(), size + 1);
            assertFalse(containsOP);
            assertTrue(queueAfter.contains(instance.signifyProcessorIdleOperation));
        }
    }

    @Test
    public void testSetRefreshLatch() {
        System.out.println("setRefreshLatch");
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            final VisualAccess access = mock(VisualAccess.class);
            final VisualProcessor processor = mock(VisualProcessor.class);

            final VisualManager instance = new VisualManager(access, processor);

            instance.setRefreshLatch(null);
        }
    }

    @Test
    public void testProcessCountDown() {
        System.out.println("testProcessCountDown");
        final VisualAccess access = mock(VisualAccess.class);
        final VisualProcessor processor = mock(VisualProcessor.class);
        final VisualManager instance = spy(new VisualManager(access, processor));

        processCountDownHelper(instance, processor, new CountDownLatch(1));

        verify(instance, times(3)).isProcessing();
        verify(instance, Mockito.atLeastOnce()).getOperations();
        verify(processor, times(1)).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    @Test
    public void testProcessCountDownZero() {
        System.out.println("testProcessCountDownZero");
        final VisualAccess access = mock(VisualAccess.class);
        final VisualProcessor processor = mock(VisualProcessor.class);
        final VisualManager instance = spy(new VisualManager(access, processor));

        processCountDownHelper(instance, processor, new CountDownLatch(0));

        verify(instance, times(3)).isProcessing();
        verify(instance, Mockito.atLeastOnce()).getOperations();
        verify(processor, times(1)).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    @Test
    public void testProcessCountDownNull() {
        System.out.println("testProcessCountDownZero");
        final VisualAccess access = mock(VisualAccess.class);
        final VisualProcessor processor = mock(VisualProcessor.class);
        final VisualManager instance = spy(new VisualManager(access, processor));

        processCountDownHelper(instance, processor, null);

        verify(instance, times(3)).isProcessing();
        verify(instance, Mockito.atLeastOnce()).getOperations();
        verify(processor, times(1)).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    private void processCountDownHelper(final VisualManager instance, final VisualProcessor processor, final CountDownLatch latch) {
        try (final MockedStatic<MemoryManager> memoryManagerMockedStatic = mockStatic(MemoryManager.class)) {
            memoryManagerMockedStatic.when(() -> MemoryManager.newObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);
            memoryManagerMockedStatic.when(() -> MemoryManager.finalizeObject(Mockito.eq(VisualManager.class))).thenAnswer((Answer<Void>) invocation -> null);

            doNothing().when(processor).update(Mockito.any(Collection.class), Mockito.any(VisualAccess.class), Mockito.anyBoolean(), Mockito.anyBoolean());

            final PriorityBlockingQueue<VisualOperation> queue = new PriorityBlockingQueue<>();
            final PriorityBlockingQueue<VisualOperation> queue2 = new PriorityBlockingQueue<>();

            final VisualChange vc = mock(VisualChange.class);

            final List<VisualChange> changes2 = new ArrayList<>();
            final VisualOperation op = mock(VisualOperation.class);
            changes2.add(vc);
            when(op.getVisualChanges()).thenReturn(changes2);
            doNothing().when(op).apply();
            queue.add(instance.signifyProcessorIdleOperation);
            queue2.add(instance.refreshProcessorOperation);

            when(instance.getOperations()).thenReturn(queue, queue2);
            when(instance.isRendererIdle()).thenReturn(true);
            when(instance.isProcessing()).thenReturn(true, true, false);
            when(instance.getProcessor()).thenReturn(processor);

            instance.setRefreshLatch(latch);

            instance.process();
        }
    }
}
