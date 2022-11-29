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
package au.gov.asd.tac.constellation.views.mapview.overlays;

import au.gov.asd.tac.constellation.views.mapview.MapViewTileRenderer;
import au.gov.asd.tac.constellation.views.mapview.providers.MapProvider;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.events.EventDispatcher;
import de.fhpotsdam.unfolding.events.MapEvent;
import de.fhpotsdam.unfolding.events.MapEventListener;
import de.fhpotsdam.unfolding.events.PanMapEvent;
import de.fhpotsdam.unfolding.events.ScopedListeners;
import de.fhpotsdam.unfolding.events.ZoomMapEvent;
import de.fhpotsdam.unfolding.geo.Location;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * An overlay providing information about the state of the Map View.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapOverlay.class)
public class InfoOverlay extends MapOverlay implements MapEventListener {

    // colors
    private static final int EVENT_BOX_COLOR_SENDING_ON = 0xFF5bdae7;
    private static final int EVENT_BOX_COLOR_RECEIVING_ON = 0xFFfc8720;

    // event lights
    private static final float MINIMUM_TRANSPARENCY = 0.3F;
    private static final float TRANSPARENCY_DELTA = 0.05F;
    private float panByListened = MINIMUM_TRANSPARENCY;
    private float panToListened = MINIMUM_TRANSPARENCY;
    private float zoomByListened = MINIMUM_TRANSPARENCY;
    private float zoomToListened = MINIMUM_TRANSPARENCY;
    private float panByBroadcasted = MINIMUM_TRANSPARENCY;
    private float panToBroadcasted = MINIMUM_TRANSPARENCY;
    private float zoomByBroadcasted = MINIMUM_TRANSPARENCY;
    private float zoomToBroadcasted = MINIMUM_TRANSPARENCY;

    public InfoOverlay() {
        this.enabled = true;
    }

    @Override
    public void initialise(final MapViewTileRenderer renderer,
            final UnfoldingMap map, final EventDispatcher eventDispatcher) {
        super.initialise(renderer, map, eventDispatcher);

        if (eventDispatcher != null) {
            eventDispatcher.register(InfoOverlay.this, "pan", map.getId());
            eventDispatcher.register(InfoOverlay.this, "zoom", map.getId());
        }
    }

    @Override
    public String getName() {
        return "Info Overlay";
    }

    @Override
    public float getX() {
        return renderer.getComponent().getX() + 10F;
    }

    @Override
    public float getY() {
        return renderer.getComponent().getY() + 10F;
    }

    @Override
    public void overlay() {

        // draw info overlay
        renderer.noStroke();
        renderer.fill(BACKGROUND_COLOR);
        renderer.rect(x, y, width, height);

        float yOffset = y + MARGIN;

        // draw zoom info
        final String zoom = String.valueOf(map.getZoomLevel());
        drawLabeledValue("Zoom", zoom, x + 60, yOffset, VALUE_BOX_SHORT_WIDTH);
        drawStepBar(map.getZoomLevel(), x + 95, yOffset + 5,
                ((MapProvider) map.mapDisplay.getMapProvider()).zoomLevels());

        // draw separator
        yOffset += VALUE_BOX_HEIGHT + PADDING * 2;
        drawSeparator(yOffset);
        yOffset += PADDING * 2;

        // draw location info
        final Location mouseLocation = map.getLocation(renderer.mouseX, renderer.mouseY);
        final String mouseLatitude = PApplet.nf(mouseLocation.getLat(), 1, 3) + "°";
        final String mouseLongitude = PApplet.nf(mouseLocation.getLon(), 1, 3) + "°";
        drawLabel("Location", x + 60, yOffset);
        drawValue(mouseLatitude, x + 60, yOffset, VALUE_BOX_MEDIUM_WIDTH, false, false);
        drawValue(mouseLongitude, x + 60 + VALUE_BOX_MEDIUM_WIDTH + PADDING, yOffset, VALUE_BOX_MEDIUM_WIDTH, false, false);

        // if debug is on, draw additional info
        if (debug) {
            yOffset += VALUE_BOX_HEIGHT + PADDING * 2;
            drawSeparator(yOffset);
            yOffset += PADDING * 2;

            final float debugHeight = (VALUE_BOX_HEIGHT * 4) + (PADDING * 7) + 1;
            renderer.noStroke();
            renderer.fill(BACKGROUND_COLOR);
            renderer.rect(x, yOffset - 1, width, debugHeight);

            final String renderer = this.renderer.g.getClass().getSimpleName();
            drawLabeledValue("Renderer", renderer, x + 60, yOffset, VALUE_BOX_LONG_WIDTH);

            yOffset += VALUE_BOX_HEIGHT + PADDING;

            final String provider = map.mapDisplay.getMapProvider().getClass().getSimpleName();
            drawLabeledValue("Provider", provider, x + 60, yOffset, VALUE_BOX_LONG_WIDTH);

            yOffset += VALUE_BOX_HEIGHT + PADDING;

            final String mouseX = String.valueOf(this.renderer.mouseX) + "px";
            final String mouseY = String.valueOf(this.renderer.mouseY) + "px";
            drawLabel("Mouse", x + 60, yOffset);
            drawValue(mouseX, x + 60, yOffset, VALUE_BOX_MEDIUM_WIDTH, false, false);
            drawValue(mouseY, x + 60 + VALUE_BOX_MEDIUM_WIDTH + PADDING, yOffset, VALUE_BOX_MEDIUM_WIDTH, false, false);

            yOffset += VALUE_BOX_HEIGHT + PADDING;

            final String fps = String.valueOf(PApplet.round(this.renderer.frameRate));
            drawLabeledValue("FPS", fps, x + 60, yOffset, VALUE_BOX_SHORT_WIDTH);

            if (eventDispatcher != null) {
                yOffset += VALUE_BOX_HEIGHT + PADDING * 2;
                drawSeparator(yOffset);
                yOffset += PADDING * 2;

                final float eventHeight = (EVENT_BOX_HEIGHT * 2) + (PADDING * 5) + 1;
                this.renderer.fill(BACKGROUND_COLOR);
                this.renderer.rect(x, yOffset, width, eventHeight);

                this.renderer.fill(VALUE_BOX_COLOR);
                this.renderer.rect(x + MARGIN, yOffset, width - MARGIN * 2, 24 + PADDING * 2);

                final float xOffset = x + 80;

                yOffset += PADDING;

                drawLabeledEvent("Pan By", panByListened, panByBroadcasted, xOffset, yOffset, 3);
                drawLabeledEvent("Pan To", panToListened, panToBroadcasted, xOffset + 70, yOffset, 3);

                yOffset += EVENT_BOX_HEIGHT;

                drawLabeledEvent("Zoom By", zoomByListened, zoomByBroadcasted, xOffset, yOffset, 3);
                drawLabeledEvent("Zoom To", zoomToListened, zoomToBroadcasted, xOffset + 70, yOffset, 3);

                fadeEventLights();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mousePressed(MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        // DO NOTHING
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // DO NOTHING
    }

    @Override
    public String getId() {
        return getName();
    }

    protected void drawLabeledEvent(final String label, final float listeningValue,
            final float broadcastingValue, final float x, final float y, final float valueBoxWidth) {
        final int alphaSend = (int) PApplet.map(broadcastingValue, 0, 1, 0, 255);
        drawEvent(x, y + 4, valueBoxWidth, renderer.color(EVENT_BOX_COLOR_SENDING_ON, alphaSend));

        final int alphaReceive = (int) PApplet.map(listeningValue, 0, 1, 0, 255);
        drawEvent(x + 6, y + 4, valueBoxWidth, renderer.color(EVENT_BOX_COLOR_RECEIVING_ON, alphaReceive));

        final float labelX = x - PADDING - Math.min(renderer.textWidth(label.toUpperCase()), MAX_LABEL_LENGTH);
        final float labelY = y + TEXT_SIZE - 3;
        renderer.textFont(font);
        renderer.textSize(8);
        renderer.noStroke();
        renderer.fill(TEXT_COLOR);
        renderer.text(label.toUpperCase(), labelX, labelY);
    }

    protected void drawEvent(final float x, final float y, final float valueBoxSize, final int color) {
        final float valueBoxX = x + PADDING;
        final float valueBoxY = y;
        renderer.fill(color);
        renderer.rect(valueBoxX, valueBoxY, valueBoxSize, valueBoxSize);
    }

    protected void fadeEventLights() {
        if (panByListened > MINIMUM_TRANSPARENCY) {
            panByListened -= TRANSPARENCY_DELTA;
        }
        if (panToListened > MINIMUM_TRANSPARENCY) {
            panToListened -= TRANSPARENCY_DELTA;
        }
        if (zoomByListened > MINIMUM_TRANSPARENCY) {
            zoomByListened -= TRANSPARENCY_DELTA;
        }
        if (zoomToListened > MINIMUM_TRANSPARENCY) {
            zoomToListened -= TRANSPARENCY_DELTA;
        }
        if (panByBroadcasted > MINIMUM_TRANSPARENCY) {
            panByBroadcasted -= TRANSPARENCY_DELTA;
        }
        if (panToBroadcasted > MINIMUM_TRANSPARENCY) {
            panToBroadcasted -= TRANSPARENCY_DELTA;
        }
        if (zoomByBroadcasted > MINIMUM_TRANSPARENCY) {
            zoomByBroadcasted -= TRANSPARENCY_DELTA;
        }
        if (zoomToBroadcasted > MINIMUM_TRANSPARENCY) {
            zoomToBroadcasted -= TRANSPARENCY_DELTA;
        }
    }

    @Override
    public void onManipulation(final MapEvent mapEvent) {
        if (map.getId().equals(mapEvent.getScopeId())) {
            if (mapEvent.getSubType().equals(PanMapEvent.PAN_BY)) {
                panByBroadcasted = 1;
            }
            if (mapEvent.getSubType().equals(PanMapEvent.PAN_TO)) {
                panToBroadcasted = 1;
            }
            if (mapEvent.getSubType().equals(ZoomMapEvent.ZOOM_BY_LEVEL)) {
                zoomByBroadcasted = 1;
            }
            if (mapEvent.getSubType().equals(ZoomMapEvent.ZOOM_TO_LEVEL)) {
                zoomToBroadcasted = 1;
            }
        }

        if (eventDispatcher != null) {
            final List<ScopedListeners> scopedListenersList = eventDispatcher.typedScopedListeners.get(mapEvent.getType());
            scopedListenersList.forEach(scopedListeners -> {
                if (scopedListeners.isInScope(mapEvent)) {
                    scopedListeners.listeners.forEach(listener -> {
                        if (listener instanceof UnfoldingMap) {
                            final UnfoldingMap listeningMap = (UnfoldingMap) listener;
                            if (map.getId().equals(listeningMap.getId())) {
                                if (mapEvent.getSubType().equals(PanMapEvent.PAN_BY)) {
                                    panByListened = 1;
                                }
                                if (mapEvent.getSubType().equals(PanMapEvent.PAN_TO)) {
                                    panToListened = 1;
                                }
                                if (mapEvent.getSubType().equals(ZoomMapEvent.ZOOM_BY_LEVEL)) {
                                    zoomByListened = 1;
                                }
                                if (mapEvent.getSubType().equals(ZoomMapEvent.ZOOM_TO_LEVEL)) {
                                    zoomToListened = 1;
                                }
                            }
                        }
                    });
                }
            });
        }
    }
}
