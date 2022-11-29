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
package au.gov.asd.tac.constellation.utilities.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A trivial text-based implementation of IoProgress.
 *
 * @author algol
 */
public class TextIoProgress implements IoProgress {

    private final boolean display;
    private static final Logger LOGGER = Logger.getLogger(TextIoProgress.class.getName());

    /**
     *
     * @param display If true, text will be displayed on stdout, otherwise,
     * nothing will be displayed.
     */
    public TextIoProgress(final boolean display) {
        this.display = display;
    }

    @Override
    public void start() {
        if (display) {
            LOGGER.log(Level.INFO, "start");
        }
    }

    @Override
    public void start(final int workunits) {
        if (display) {
            LOGGER.log(Level.INFO, "start({0})", workunits);
        }
    }

    @Override
    public void progress(final int workunit) {
        if (display) {
            LOGGER.log(Level.INFO, "progress({0})", workunit);
        }
    }

    @Override
    public void progress(final String message) {
        if (display) {
            LOGGER.log(Level.INFO, "progress({0})", message);
        }
    }

    @Override
    public void progress(final String message, final int workunit) {
        if (display) {
            final String log = String.format("progress(%s,%d)%n", message, workunit);
            LOGGER.log(Level.INFO, log);
        }
    }

    @Override
    public void finish() {
        if (display) {
            LOGGER.log(Level.INFO, "finish()");
        }
    }
}
