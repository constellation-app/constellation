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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.openide.util.Exceptions;

/**
 *
 * @author Delphinus8821
 */
public abstract class ThreadAllocator {

    protected int numberOfElements = -1;
    protected int numberOfThreads = -1;
    protected int numberOfElementsPerThread = -1;
    protected int numAllocated = 0;
    private CyclicBarrier barrier = null;

    // Note that the implementation of this methdo can be anything, but usually either a constant, or some expression involving numberOfElements
    protected abstract int calculateNumberOfThreads();

    // These two methods may be overwritten to throw UnsupportedOperationExceptions if the ThreadAllocator is not being used in conjunction with adaptors. 
    // protected abstract ThreadPhraseAdaptor getAdaptor();
    public abstract ThreadedPhraseAdaptor nextAdaptor();

    public final boolean hasMore() {
        return !(numAllocated == numberOfThreads);
    }

    public final int getNumAllocated() {
        return numAllocated;
    }

    public final int getLowerPos() {
        return numAllocated * numberOfElementsPerThread;
    }

    public final int getWorkload() {
        return (numAllocated == numberOfThreads - 1) ? numberOfElements - getLowerPos() : numberOfElementsPerThread;
    }

    public final int getNumberOfThreads() {
        return numberOfThreads;
    }

    private int calculateNumberOfElementsPerThread() {
        return (int) Math.ceil((double) numberOfElements / (double) numberOfThreads);
    }

    public final ThreadAllocator resetThreadAllocation(final int numberOfElements) {
        this.numberOfElements = numberOfElements;
        numAllocated = 0;
        numberOfThreads = calculateNumberOfThreads();
        numberOfElementsPerThread = calculateNumberOfElementsPerThread();
        barrier = new CyclicBarrier(numberOfThreads + 1);
        return this;
    }

    public final void indicateAllocated() {
        numAllocated++;
    }

    public final void waitOnOthers() {
        try {
            barrier.await();
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (final BrokenBarrierException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /*
	 * Factory for building threadallocators with a specific type of adaptor
     */
    public static ThreadAllocator buildThreadAllocator(final int maxThreads, final int maxElementsPerThread, final int numOfElements, final AdaptorFactory adaptorConnector) {
        return new ThreadAllocator() {
            @Override
            protected int calculateNumberOfThreads() {
                return Math.min(maxElementsPerThread, Math.min(maxThreads, (int) Math.ceil((double) numberOfElements / (double) maxElementsPerThread)));
            }

            @Override
            public ThreadedPhraseAdaptor nextAdaptor() {
                if (numAllocated == numberOfThreads) {
                    return null;
                }
                ThreadedPhraseAdaptor adaptor = adaptorConnector.getAdaptor(this);
                indicateAllocated();
                return adaptor;
            }
        }.resetThreadAllocation(numOfElements);
    }
}
