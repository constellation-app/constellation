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
package au.gov.asd.tac.constellation.views.mapview.overlays;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
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
    protected boolean active;
    protected boolean debug;

    // positions and sizes
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected static final float PADDING = 4;
    protected static final float MARGIN = 14;
    protected static final int VALUE_BOX_LONG_WIDTH = 120;
    protected static final float VALUE_BOX_MEDIUM_WIDTH = (VALUE_BOX_LONG_WIDTH - PADDING) / 2;
    protected static final float VALUE_BOX_SHORT_WIDTH = (VALUE_BOX_MEDIUM_WIDTH - PADDING) / 2;
    protected static final int VALUE_BOX_HEIGHT = 15;
    protected static final int EVENT_BOX_HEIGHT = 12;
    protected static final int STEP_BAR_WIDTH = 90;
    protected static final int MAX_LABEL_LENGTH = 40;
    protected static final int MAX_VALUE_LENGTH = 100;

    // labels and images
    protected PFont font;
    protected PFont titleFont;
    protected static final int TEXT_SIZE = 12;
    protected static final int TEXT_COLOR = 0xFFFFFFFF;

    // colors
    protected static final int BACKGROUND_COLOR = 0xF0222222;
    protected static final int HIGHLIGHT_COLOR = 0xF0DE2446;
    protected static final int SEPARATOR_COLOR = 0x32FFFFFF;
    protected static final int STEP_OFF_COLOR = 0xFF666666;
    protected static final int STEP_ON_COLOR = 0xFFFFFFFF;
    protected static final int VALUE_BOX_COLOR = 0x7F000000;

    protected MapOverlay() {
        this.enabled = false;
        this.active = false;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
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
        return (MARGIN * 2) + MAX_LABEL_LENGTH + VALUE_BOX_LONG_WIDTH + (PADDING * 2);
    }

    public float getHeight() {
        return (MARGIN * 2) + (VALUE_BOX_HEIGHT * 2) + (PADDING * 4) + 1;
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
        final float labelX = x - PADDING - Math.min(renderer.textWidth(label), MAX_LABEL_LENGTH);
        final float labelY = y + TEXT_SIZE - 1;
        renderer.noStroke();
        renderer.fill(TEXT_COLOR);
        renderer.text(label, labelX, labelY);
    }

    protected final void drawValue(final String value, final float x, final float y, final float valueBoxWidth, final boolean leftAlign, final boolean highlight) {
        if (!highlight) {
            renderer.noStroke();
        }

        final float valueBoxX = x + PADDING;
        final float valueBoxY = y;
        renderer.fill(VALUE_BOX_COLOR);
        renderer.rect(valueBoxX, valueBoxY, valueBoxWidth, VALUE_BOX_HEIGHT);

        if (highlight) {
            renderer.stroke(HIGHLIGHT_COLOR);
            renderer.strokeWeight(2);
            renderer.rect(valueBoxX, valueBoxY, valueBoxWidth, VALUE_BOX_HEIGHT);
        }

        final float valueX = leftAlign ? x + (PADDING * 2)
                : (valueBoxX + valueBoxWidth) - (PADDING * 2) - Math.min(renderer.textWidth(value), MAX_VALUE_LENGTH);
        final float valueY = y + (TEXT_SIZE - 1);
        renderer.fill(TEXT_COLOR);
        renderer.text(value, valueX, valueY);
    }

    protected final void drawInfo(final String info, final float y, final float infoBoxWidth, final boolean leftAlign) {
        final float maxInfoLength = infoBoxWidth - (PADDING * 4);

        renderer.noStroke();

        int row = 0;
        int infoIndex = 0;
        String buffer = info;

        while (!buffer.isEmpty()) {
            final float infoBoxX = x + MARGIN + PADDING;
            final float infoBoxY = y + (row * VALUE_BOX_HEIGHT);
            renderer.fill(VALUE_BOX_COLOR);
            renderer.rect(infoBoxX, infoBoxY, infoBoxWidth, VALUE_BOX_HEIGHT);

            int bufferIndex = buffer.length();
            while (renderer.textWidth(buffer) > maxInfoLength) {
                bufferIndex = buffer.lastIndexOf(' ');
                buffer = buffer.substring(0, bufferIndex);
            }

            if (buffer.contains(SeparatorConstants.NEWLINE)) {
                bufferIndex = buffer.indexOf('\n') + 1;
                buffer = buffer.substring(0, bufferIndex);
            }

            final float valueX = leftAlign ? x + MARGIN + (PADDING * 2)
                    : (infoBoxX + infoBoxWidth) - (PADDING * 2) - Math.min(renderer.textWidth(buffer), maxInfoLength);
            final float valueY = y + (TEXT_SIZE - 1) + (row * VALUE_BOX_HEIGHT);
            renderer.fill(TEXT_COLOR);
            renderer.text(buffer, valueX, valueY);

            infoIndex += bufferIndex;
            buffer = info.substring(infoIndex, info.length());
            row++;
        }
    }

    protected final void drawStepBar(final int stepLevel, final float x, final float y, final int maxStepLevel) {
        final int stepWidth = PApplet.floor((STEP_BAR_WIDTH / maxStepLevel));
        renderer.noStroke();
        for (int i = 0; i < maxStepLevel; i++) {
            renderer.fill(i < stepLevel ? STEP_ON_COLOR : STEP_OFF_COLOR);
            renderer.rect(x + i * stepWidth, y, stepWidth - 1, 6);
        }
    }

    protected final void drawSeparator(final float y) {
        renderer.noStroke();
        renderer.fill(SEPARATOR_COLOR);
        renderer.rect(x + MARGIN, y, width - (MARGIN * 2), 1);
    }
}
