/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview.overlays;

import au.gov.asd.tac.constellation.views.mapview.MapViewTileRenderer;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import org.openide.util.lookup.ServiceProvider;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * An overlay providing a holistic view of the map with a viewport indicating
 * the area you are currently viewing.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapOverlay.class)
public class OverviewOverlay extends MapOverlay {

    private final Viewport viewport = new Viewport();
    private UnfoldingMap overviewMap;
    private float deltaX;
    private float deltaY;

    public OverviewOverlay() {
        super();
        this.enabled = false;
    }

    @Override
    public String getName() {
        return "Overview Overlay";
    }

    @Override
    public float getX() {
        return renderer.getComponent().getX() + renderer.getComponent().getWidth() - width - 10f;
    }

    @Override
    public float getY() {
        return renderer.getComponent().getY() + renderer.getComponent().getHeight() - height - 10f;
    }

    @Override
    public float getWidth() {
        return super.getWidth() * 1.4f;
    }

    @Override
    public float getHeight() {
        return super.getHeight() * 3.5f;
    }

    @Override
    public void initialise(final MapViewTileRenderer renderer,
            final UnfoldingMap map, final EventDispatcher eventDispatcher) {
        super.initialise(renderer, map, eventDispatcher);
        this.overviewMap = new UnfoldingMap(renderer,
                x + padding, y + padding, width - (padding * 2), height - (padding * 2));
    }

    @Override
    public void overlay() {
        renderer.noStroke();
        renderer.fill(backgroundColor);
        renderer.rect(x, y, width, height);

        if (!overviewMap.mapDisplay.getMapProvider().equals(map.mapDisplay.getMapProvider())) {
            overviewMap.mapDisplay.setMapProvider(map.mapDisplay.getMapProvider());
        }
        overviewMap.draw();

        final ScreenPosition topLeft = overviewMap.getScreenPosition(map.getTopLeftBorder());
        final ScreenPosition bottomRight = overviewMap.getScreenPosition(map.getBottomRightBorder());
        viewport.setDimension(topLeft, bottomRight);
        viewport.draw();
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        if (viewport.isOver(renderer.mouseX, renderer.mouseY)) {
            viewport.isDragging = true;
            deltaX = renderer.mouseX - viewport.x;
            deltaY = renderer.mouseY - viewport.y;
        }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        if (viewport.isDragging) {
            viewport.x = renderer.mouseX - deltaX;
            viewport.y = renderer.mouseY - deltaY;

            viewport.update(0);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        viewport.isDragging = false;
    }

    @Override
    public void mouseWheel(final MouseEvent event) {
        if (viewport.isOver(renderer.mouseX, renderer.mouseY)) {
            viewport.update(event.getCount());
        }
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        // DO NOTHING
    }

    class Viewport {

        private float x = 0f;
        private float y = 0f;
        private float width = 0f;
        private float height = 0f;
        private boolean isDragging = false;

        public boolean isOver(final float checkX, final float checkY) {
            return checkX > x && checkY > y && checkX < x + width && checkY < y + height;
        }

        public void setDimension(final ScreenPosition topLeft, final ScreenPosition bottomRight) {
            this.x = topLeft.x;
            this.y = topLeft.y;
            this.width = bottomRight.x - topLeft.x;
            this.height = bottomRight.y - topLeft.y;
        }

        public void draw() {
            renderer.noFill();
            renderer.strokeWeight(2);
            renderer.stroke(highlightColor);

            float viewportX = Math.min(Math.max(x, getX()), getX() + getWidth());
            float viewportWidth = x < getX() ? Math.max(width - (getX() - x), 0)
                    : x + width > getX() + getWidth() ? Math.max(width - ((x + width) - (getX() + getWidth())), 0)
                            : width;

            float viewportY = Math.min(Math.max(y, getY()), getY() + getHeight());
            float viewportHeight = y < getY() ? Math.max(height - (getY() - y), 0)
                    : y + height > getY() + getHeight() ? Math.max(height - ((y + height) - (getY() + getHeight())), 0)
                            : height;

            renderer.rect(viewportX, viewportY, viewportWidth, viewportHeight);
        }

        public void update(final int zoom) {
            final Location newLocation = overviewMap.mapDisplay.getLocation(
                    viewport.x + viewport.width / 2, viewport.y + viewport.height / 2);
            final int newZoomLevel = zoom > 0 ? map.getZoomLevel() - 1
                    : zoom < 0 ? map.getZoomLevel() + 1
                            : map.getZoomLevel();
            map.setTweening(false);
            map.zoomAndPanTo(newZoomLevel, newLocation);
            map.setTweening(true);
        }
    }
}
