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
import processing.core.PApplet;
import processing.core.PFont;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * An interactive, graphical overlay to be rendered in the Map View.
 *
 * @author cygnus_x-1
 */
public abstract class MapOverlay {

    protected MapViewTileRenderer renderer;
    protected UnfoldingMap map;
    protected EventDispatcher eventDispatcher;
    protected boolean enabled;
    protected boolean debug;

    // positions and sizes
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected final float padding = 4;
    protected final float margin = 14;
    protected final int valueBoxLongWidth = 120;
    protected final float valueBoxMediumWidth = (valueBoxLongWidth - padding) / 2;
    protected final float valueBoxShortWidth = (valueBoxMediumWidth - padding) / 2;
    protected final int valueBoxHeight = 15;
    protected final int eventBoxHeight = 12;
    protected final int stepBarWidth = 90;
    protected final int maxLabelLength = 40;
    protected final int maxValueLength = 100;

    // labels and images
    protected PFont font;
    protected PFont titleFont;
    protected final int textSize = 12;
    protected final int textColor = 0xFFFFFFFF;

    // colors
    protected final int backgroundColor = 0xF0222222;
    protected final int highlightColor = 0xF0DE2446;
    protected final int separatorColor = 0x32FFFFFF;
    protected final int stepOffColor = 0xFF666666;
    protected final int stepOnColor = 0xFFFFFFFF;
    protected final int valueBoxColor = 0x7F000000;

    public MapOverlay() {
        this.enabled = true;
        this.debug = false;
    }

    public void initialise(final MapViewTileRenderer renderer,
            final UnfoldingMap map, final EventDispatcher eventDispatcher) {
        this.renderer = renderer;
        this.map = map;
        this.eventDispatcher = eventDispatcher;

        this.width = getWidth();
        this.height = getHeight();
        this.x = getX();
        this.y = getY();

        this.titleFont = renderer.loadFont("Lato-Bold-14.vlw");
        this.font = renderer.loadFont("Lato-Regular-11.vlw");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public void draw() {
        if (renderer == null || map == null || !enabled) {
            return;
        }

        overlay();
    }

    @Override
    public String toString() {
        return getName();
    }

    public abstract String getName();

    public abstract float getX();

    public abstract float getY();

    public float getWidth() {
        return (margin * 2) + maxLabelLength + valueBoxLongWidth + (padding * 2);
    }

    public float getHeight() {
        return (margin * 2) + (valueBoxHeight * 2) + (padding * 4) + 1;
    }

    public abstract void overlay();

    public abstract void mouseMoved(final MouseEvent event);

    public abstract void mouseClicked(final MouseEvent event);

    public abstract void mousePressed(final MouseEvent event);

    public abstract void mouseDragged(final MouseEvent event);

    public abstract void mouseReleased(final MouseEvent event);

    public abstract void mouseWheel(final MouseEvent event);

    public abstract void keyPressed(final KeyEvent event);

    protected final void drawLabeledValue(final String label, final String value, final float x, final float y, final float valueBoxWidth) {
        drawLabel(label, x, y);
        drawValue(value, x, y, valueBoxWidth, true, false);
    }

    protected final void drawLabel(final String label, final float x, final float y) {
        final float labelX = x - padding - Math.min(renderer.textWidth(label), maxLabelLength);
        final float labelY = y + textSize - 1;
        renderer.noStroke();
        renderer.fill(textColor);
        renderer.text(label, labelX, labelY);
    }

    protected final void drawValue(final String value, final float x, final float y, final float valueBoxWidth, final boolean leftAlign, final boolean highlight) {
        if (!highlight) {
            renderer.noStroke();
        }

        final float valueBoxX = x + padding;
        final float valueBoxY = y;
        renderer.fill(valueBoxColor);
        renderer.rect(valueBoxX, valueBoxY, valueBoxWidth, valueBoxHeight);

        if (highlight) {
            renderer.stroke(highlightColor);
            renderer.strokeWeight(2);
            renderer.rect(valueBoxX, valueBoxY, valueBoxWidth, valueBoxHeight);
        }

        final float valueX = leftAlign ? x + (padding * 2)
                : (valueBoxX + valueBoxWidth) - (padding * 2) - Math.min(renderer.textWidth(value), maxValueLength);
        final float valueY = y + (textSize - 1);
        renderer.fill(textColor);
        renderer.text(value, valueX, valueY);
    }

    protected final void drawInfo(final String info, final float y, final float infoBoxWidth, final boolean leftAlign) {
        final float maxInfoLength = infoBoxWidth - (padding * 4);

        renderer.noStroke();

        int row = 0;
        int infoIndex = 0;
        String buffer = info;

        while (!buffer.isEmpty()) {
            final float infoBoxX = x + margin + padding;
            final float infoBoxY = y + (row * valueBoxHeight);
            renderer.fill(valueBoxColor);
            renderer.rect(infoBoxX, infoBoxY, infoBoxWidth, valueBoxHeight);

            int bufferIndex = buffer.length();
            while (renderer.textWidth(buffer) > maxInfoLength) {
                bufferIndex = buffer.lastIndexOf(" ");
                buffer = buffer.substring(0, bufferIndex);
            }

            if (buffer.contains("\n")) {
                bufferIndex = buffer.indexOf("\n") + 1;
                buffer = buffer.substring(0, bufferIndex);
            }

            final float valueX = leftAlign ? x + margin + (padding * 2)
                    : (infoBoxX + infoBoxWidth) - (padding * 2) - Math.min(renderer.textWidth(buffer), maxInfoLength);
            final float valueY = y + (textSize - 1) + (row * valueBoxHeight);
            renderer.fill(textColor);
            renderer.text(buffer, valueX, valueY);

            infoIndex += bufferIndex;
            buffer = info.substring(infoIndex, info.length());
            row++;
        }
    }

    protected final void drawStepBar(final int stepLevel, final float x, final float y, final int maxStepLevel) {
        final int stepWidth = PApplet.floor((stepBarWidth / maxStepLevel));
        renderer.noStroke();
        for (int i = 0; i < maxStepLevel; i++) {
            renderer.fill(i < stepLevel ? stepOnColor : stepOffColor);
            renderer.rect(x + i * stepWidth, y, stepWidth - 1, 6);
        }
    }

    protected final void drawSeparator(final float y) {
        renderer.noStroke();
        renderer.fill(separatorColor);
        renderer.rect(x + margin, y, width - (margin * 2), 1);
    }
}
