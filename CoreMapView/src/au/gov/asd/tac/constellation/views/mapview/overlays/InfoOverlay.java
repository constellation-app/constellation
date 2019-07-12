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
    private final int eventBoxColorSendingOn = 0xFF5bdae7;
    private final int eventBoxColorReceivingOn = 0xFFfc8720;

    // event lights
    private final float minimumTransparency = 0.3f;
    private final float transparencyDelta = 0.05f;
    private float panByListened = minimumTransparency;
    private float panToListened = minimumTransparency;
    private float zoomByListened = minimumTransparency;
    private float zoomToListened = minimumTransparency;
    private float panByBroadcasted = minimumTransparency;
    private float panToBroadcasted = minimumTransparency;
    private float zoomByBroadcasted = minimumTransparency;
    private float zoomToBroadcasted = minimumTransparency;

    @Override
    public void initialise(MapViewTileRenderer renderer, UnfoldingMap map, EventDispatcher eventDispatcher) {
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
        return renderer.getComponent().getX() + 10f;
    }

    @Override
    public float getY() {
        return renderer.getComponent().getY() + 10f;
    }

    @Override
    public void overlay() {

        // draw info overlay
        renderer.noStroke();
        renderer.fill(backgroundColor);
        renderer.rect(x, y, width, height);

        float yOffset = y + margin;

        // draw zoom info
        final String zoom = String.valueOf(map.getZoomLevel());
        drawLabeledValue("Zoom", zoom, x + 60, yOffset, valueBoxShortWidth);
        drawStepBar(map.getZoomLevel(), x + 95, yOffset + 5, ((MapProvider) map.mapDisplay.getMapProvider()).zoomLevels());

        // draw separator
        yOffset += valueBoxHeight + padding * 2;
        drawSeparator(yOffset);
        yOffset += padding * 2;

        // draw location info
        final Location mouseLocation = map.getLocation(renderer.mouseX, renderer.mouseY);
        final String mouseLatitude = PApplet.nf(mouseLocation.getLat(), 1, 3) + "°";
        final String mouseLongitude = PApplet.nf(mouseLocation.getLon(), 1, 3) + "°";
        drawLabel("Location", x + 60, yOffset);
        drawValue(mouseLatitude, x + 60, yOffset, valueBoxMediumWidth, false, false);
        drawValue(mouseLongitude, x + 60 + valueBoxMediumWidth + padding, yOffset, valueBoxMediumWidth, false, false);

        // if debug is active, draw additional info
        if (debug) {
            yOffset += valueBoxHeight + padding * 2;
            drawSeparator(yOffset);
            yOffset += padding * 2;

            final float debugHeight = (valueBoxHeight * 4) + (padding * 7) + 1;
            renderer.noStroke();
            renderer.fill(backgroundColor);
            renderer.rect(x, yOffset - 1, width, debugHeight);

            final String renderer = this.renderer.g.getClass().getSimpleName();
            drawLabeledValue("Renderer", renderer, x + 60, yOffset, valueBoxLongWidth);

            yOffset += valueBoxHeight + padding;

            final String provider = map.mapDisplay.getMapProvider().getClass().getSimpleName();
            drawLabeledValue("Provider", provider, x + 60, yOffset, valueBoxLongWidth);

            yOffset += valueBoxHeight + padding;

            final String mouseX = String.valueOf(this.renderer.mouseX) + "px";
            final String mouseY = String.valueOf(this.renderer.mouseY) + "px";
            drawLabel("Mouse", x + 60, yOffset);
            drawValue(mouseX, x + 60, yOffset, valueBoxMediumWidth, false, false);
            drawValue(mouseY, x + 60 + valueBoxMediumWidth + padding, yOffset, valueBoxMediumWidth, false, false);

            yOffset += valueBoxHeight + padding;

            final String fps = String.valueOf(PApplet.round(this.renderer.frameRate));
            drawLabeledValue("FPS", fps, x + 60, yOffset, valueBoxShortWidth);

            if (eventDispatcher != null) {
                yOffset += valueBoxHeight + padding * 2;
                drawSeparator(yOffset);
                yOffset += padding * 2;

                final float eventHeight = (eventBoxHeight * 2) + (padding * 5) + 1;
                this.renderer.fill(backgroundColor);
                this.renderer.rect(x, yOffset, width, eventHeight);

                this.renderer.fill(valueBoxColor);
                this.renderer.rect(x + margin, yOffset, width - margin * 2, 24 + padding * 2);

                final float xOffset = x + 80;

                yOffset += padding;

                drawLabeledEvent("Pan By", panByListened, panByBroadcasted, xOffset, yOffset, 3);
                drawLabeledEvent("Pan To", panToListened, panToBroadcasted, xOffset + 70, yOffset, 3);

                yOffset += eventBoxHeight;

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

    protected void drawLabeledEvent(final String label, final float listeningValue, final float broadcastingValue, final float x, final float y, final float valueBoxWidth) {
        final int alphaSend = (int) PApplet.map(broadcastingValue, 0, 1, 0, 255);
        drawEvent(x, y + 4, valueBoxWidth, renderer.color(eventBoxColorSendingOn, alphaSend));

        final int alphaReceive = (int) PApplet.map(listeningValue, 0, 1, 0, 255);
        drawEvent(x + 6, y + 4, valueBoxWidth, renderer.color(eventBoxColorReceivingOn, alphaReceive));

        final float labelX = x - padding - Math.min(renderer.textWidth(label.toUpperCase()), maxLabelLength);
        final float labelY = y + textSize - 3;
        renderer.textFont(font);
        renderer.textSize(8);
        renderer.noStroke();
        renderer.fill(textColor);
        renderer.text(label.toUpperCase(), labelX, labelY);
    }

    protected void drawEvent(final float x, final float y, final float valueBoxSize, final int color) {
        final float valueBoxX = x + padding;
        final float valueBoxY = y;
        renderer.fill(color);
        renderer.rect(valueBoxX, valueBoxY, valueBoxSize, valueBoxSize);
    }

    protected void fadeEventLights() {
        if (panByListened > minimumTransparency) {
            panByListened -= transparencyDelta;
        }
        if (panToListened > minimumTransparency) {
            panToListened -= transparencyDelta;
        }
        if (zoomByListened > minimumTransparency) {
            zoomByListened -= transparencyDelta;
        }
        if (zoomToListened > minimumTransparency) {
            zoomToListened -= transparencyDelta;
        }
        if (panByBroadcasted > minimumTransparency) {
            panByBroadcasted -= transparencyDelta;
        }
        if (panToBroadcasted > minimumTransparency) {
            panToBroadcasted -= transparencyDelta;
        }
        if (zoomByBroadcasted > minimumTransparency) {
            zoomByBroadcasted -= transparencyDelta;
        }
        if (zoomToBroadcasted > minimumTransparency) {
            zoomToBroadcasted -= transparencyDelta;
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
