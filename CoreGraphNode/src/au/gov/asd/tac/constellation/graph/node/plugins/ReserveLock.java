/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.node.plugins;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lock manager
 *
 * @author sirius
 */
public class ReserveLock {

    // Stores the current ReserveLock for each thread allowing ReserveLocks to be reentrantly acquired
    private static final ThreadLocal<ReserveLock> THREAD_LOCAL = new ThreadLocal<>();
    // A comparator allowing Reservables to be consistently sorted
    private static final Comparator<Reservable> RESERVABLE_COMPARATOR = (r1, r2) -> {
        int x = System.identityHashCode(r1);
        int y = System.identityHashCode(r2);
        if (x < y) {
            return -1;
        }
        return (x == y) ? 0 : 1;
    };
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Reservable[] reserved;
    private Reservable active = null;
    private int lockLevel = 1;

    public static ReserveLock createLock(final Reservable... reserved) throws InterruptedException {
        ReserveLock currentReserveLock = THREAD_LOCAL.get();

        if (currentReserveLock != null) {
            if (currentReserveLock.lock.isLocked()) {
                throw new IllegalMonitorStateException("reserving while reading or writing");
            }

            for (Reservable reservable : reserved) {
                if (reservable.currentLock != currentReserveLock) {
                    throw new IllegalMonitorStateException("reserving additional locks");
                }
            }

            currentReserveLock.lockLevel++;

            return currentReserveLock;
        } else {
            ReserveLock reserveLock = new ReserveLock(reserved);
            THREAD_LOCAL.set(reserveLock);
            return reserveLock;

        }
    }

    private ReserveLock(final Reservable... reserved) throws InterruptedException {

        this.reserved = Arrays.copyOf(reserved, reserved.length);
        Arrays.sort(this.reserved, RESERVABLE_COMPARATOR);

        int i = 0;
        try {
            while (i < this.reserved.length) {
                this.reserved[i].reserveSemaphore.acquire();
                this.reserved[i].currentLock = this;
                i++;
            }
        } catch (InterruptedException ex) {
            while (--i >= 0) {
                this.reserved[i].reserveSemaphore.release();
                this.reserved[i].currentLock = null;
            }
            throw ex;
        }
    }

    public void finish() {

        // Ensure that no thread is reading or writing using this lock
        if (lock.isLocked()) {
            throw new IllegalMonitorStateException("finishing lock while reading or writing");
        }

        // decrease the lock level and release the reserves if required
        if (--lockLevel == 0) {
            for (Reservable reservable : reserved) {
                reservable.reserveSemaphore.release();
                reservable.currentLock = null;
            }
            THREAD_LOCAL.remove();
        }
    }

    public final void startReading(final Reservable reservable) throws InterruptedException {
        if (lockLevel < 1) {
            throw new IllegalMonitorStateException("reading from finished lock");
        }
        lock.lockInterruptibly();
        try {
            reservable.readWriteLock.readLock().lockInterruptibly();
        } catch (InterruptedException ex) {
            lock.unlock();
            throw ex;
        }
        active = reservable;
    }

    public final void startWriting(final Reservable reservable) throws InterruptedException {
        if (lockLevel < 1) {
            throw new IllegalMonitorStateException("reading from finished lock");
        }
        if (reservable.currentLock != this) {
            throw new IllegalMonitorStateException("writing on lock that has not been reserved");
        }
        lock.lockInterruptibly();
        try {
            reservable.readWriteLock.writeLock().lockInterruptibly();
        } catch (InterruptedException ex) {
            lock.unlock();
            throw ex;
        }
        active = reservable;
    }

    public final void stopReading() {
        active.readWriteLock.readLock().unlock();
        lock.unlock();
    }

    public final void stopWriting() {
        active.readWriteLock.writeLock().unlock();
        lock.unlock();
    }

    public static final class Reservable {

        private ReserveLock currentLock = null;
        private Semaphore reserveSemaphore = new Semaphore(1, true);
        private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    }
}
