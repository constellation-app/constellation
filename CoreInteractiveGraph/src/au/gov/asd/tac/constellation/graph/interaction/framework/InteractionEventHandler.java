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
package au.gov.asd.tac.constellation.graph.interaction.framework;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * An interface describing a given pattern of user interaction with a graph
 * based on mouse and keyboard events received from the EDT.
 * <p>
 * This interface extends all the standard AWT listeners for mouse and keyboard
 * events, but implementations need not do something with every gesture; no-ops
 * are fine.
 *
 * @author twilight_sparkle
 */
public interface InteractionEventHandler extends KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

    /**
     * Begin handling events received from the EDT such that their effect on the
     * graph will be seen.
     */
    public void startHandlingEvents();

    /**
     * Cease handling events received from the EDT such that they will no longer
     * affect the graph.
     */
    public void stopHandlingEvents();

}
