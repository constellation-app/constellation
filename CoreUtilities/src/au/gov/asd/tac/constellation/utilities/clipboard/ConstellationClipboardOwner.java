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
package au.gov.asd.tac.constellation.utilities.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

/**
 * A singleton instance of a clipboard for CONSTELLATION that can be used to
 * hold many application specific types of data, most notably a copy of a Graph.
 * <p>
 * We use a local clipboard instance because (a) nothing else knows how to deal
 * with a Graph, and (b) the system clipboard would require the graph to be
 * serialised. Therefore, cut/copy/paste of a graph only works locally.
 * <p>
 * We listen to the system clipboard for ownership loss. The user selecting some
 * text elsewhere overrides the local selection of a graph.
 *
 * @author algol
 */
public class ConstellationClipboardOwner implements ClipboardOwner {

    private static final Clipboard CLIPBOARD = new Clipboard("__graphclipboard__");
    private static final ConstellationClipboardOwner OWNER = new ConstellationClipboardOwner();

    private ConstellationClipboardOwner() {
    }

    /**
     * Get the single instance of the Graph Clipboard.
     *
     * @return A clipboard suitable for holding a Graph.
     */
    public static Clipboard getConstellationClipboard() {
        return CLIPBOARD;
    }

    /**
     * Get the single instance of the clipboard owner.
     *
     * @return The clipboard owner.
     */
    public static ClipboardOwner getOwner() {
        return OWNER;
    }

    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        ConstellationClipboardOwner.CLIPBOARD.setContents(null, null);
    }
}
