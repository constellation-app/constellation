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
package au.gov.asd.tac.constellation.utilities.timer;

/**
 * A utility class to allow easy timing of sections of code.
 *
 * @author sirius
 */
public class Timer {

    private long startTime;

    protected long getStartTime() {
        return startTime;
    }
    
    /**
     * Starts the timer.
     *
     * @return this.
     */
    public Timer start() {
        startTime = System.nanoTime();
        return this;
    }

    /**
     * Stops the timer and prints a line to standard out showing the time that
     * has elapsed since the timer was started (in sec). The timer is
     * automatically reset to this time meaning that multiple calls to stop can
     * be made, each displaying the time since the last stop call.
     *
     * @param message a message to included at the beginning of the line.
     */
    public void stop(final String message) {
        final long endTime = System.nanoTime();
        final long difference = endTime - startTime;
        startTime = endTime;
        System.out.println(message + ": " + ((double) difference / 1000000000.0));
    }
}
