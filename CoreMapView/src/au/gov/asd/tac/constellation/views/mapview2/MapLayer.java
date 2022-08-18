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
package au.gov.asd.tac.constellation.views.mapview2;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTileRenderer;
import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PConstants;
import processing.core.PImage;
import processing.event.MouseEvent;

/**
 * A static, visual layer to be rendered in the Map View.
 *
 * @author cygnus_x-1
 */
public abstract class MapLayer {

    private static final int WAIT_MILLIS = 100;

    protected boolean enabled;
    protected au.gov.asd.tac.constellation.views.mapview2.MapViewTileRenderer renderer;
    protected UnfoldingMap map;
    protected Graph graph;
    protected PImage layer;

    private boolean mouseIsDragging = false;
    private boolean mouseIsReleased = false;
    private int releaseTime = 0;
    private boolean mouseIsScrolling = false;
    private int scrollTime = 0;

    protected MapLayer() {
        this.enabled = false;
        this.graph = null;
        this.layer = null;
    }

    public void initialise(final au.gov.asd.tac.constellation.views.mapview2.MapViewTileRenderer renderer, final UnfoldingMap map) {
        this.renderer = renderer;
        this.map = map;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(final Graph graph) {
        this.graph = graph;
    }

    public void draw() {
        if (renderer == null || map == null || !enabled) {
            return;
        }

        if (requiresUpdate()
                || (mouseIsReleased && renderer.millis() - releaseTime > WAIT_MILLIS)
                || (mouseIsScrolling && renderer.millis() - scrollTime > WAIT_MILLIS)) {
            new Thread(() -> {
                // allow time for animation to finish
                if (map.isTweening()) {
                    try {
                        Thread.sleep(WAIT_MILLIS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }

                // update layer
                layer = update();
            }).start();
            mouseIsReleased = false;
            mouseIsScrolling = false;
        }

        if (layer != null) {
            renderer.image(layer, 0, 0);
        }
    }

    public void mouseDragged(final MouseEvent event) {
        if (event.getButton() == PConstants.RIGHT) {
            layer = null;
            mouseIsDragging = true;
        }
    }

    public void mouseReleased(final MouseEvent event) {
        if (mouseIsDragging && event.getButton() == PConstants.RIGHT) {
            layer = null;
            mouseIsDragging = false;
            mouseIsReleased = true;
            releaseTime = renderer.millis();
        }
    }

    public void mouseWheel(final MouseEvent event) {
        if (!mouseIsDragging && event.getCount() != 0) {
            layer = null;
            mouseIsScrolling = true;
            scrollTime = renderer.millis();
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public abstract String getName();

    public abstract boolean requiresUpdate();

    public abstract PImage update();
}
