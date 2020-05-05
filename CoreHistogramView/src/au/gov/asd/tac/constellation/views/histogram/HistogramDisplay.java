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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.MouseInputListener;

/**
 * The HistogramDisplay provides a panel the actually shows the histogram bins
 * with their associated bars and labels.
 *
 * @author sirius
 * @author antares
 */
public class HistogramDisplay extends JPanel implements MouseInputListener, MouseWheelListener,  KeyListener, PropertyChangeListener, ComponentListener {

    public static final Color BACKGROUND_COLOR = new Color(0x44, 0x44, 0x44);
    public static final Color BAR_COLOR = new Color(0.1176f, 0.5647f, 1.0f);
    public static final Color SELECTED_COLOR = Color.RED.darker();
    public static final Color ACTIVE_COLOR = Color.YELLOW;

    static final String NO_VALUE = "<No Value>";
    private static final String PROPERTY_VALUE = "Property Value";
    private static final String COUNT = "Count";

    // The color that shows where a bar would be if it was bigger.
    // This provides a guide to the user so they can click anywhere level with a bar,
    // even if the bar is really short.
    private static final Color CLICK_AREA_COLOR = BACKGROUND_COLOR.brighter();
    private static final Color ACTIVE_AREA_COLOR = CLICK_AREA_COLOR.brighter();
    private static final String NO_DATA = "<No Data>";
    private static final Font FONT = FontUtilities.getOutputFont();
    private static final int GAP_BETWEEN_BARS = 5;
    static final int MINIMUM_BAR_HEIGHT = FontUtilities.getOutputFontSize() <= 18 ? 18 : FontUtilities.getOutputFontSize();
    static final int MAXIMUM_BAR_HEIGHT = FontUtilities.getOutputFontSize() <= 18 ? 18 : FontUtilities.getOutputFontSize() + 10;
    private static final int PREFERRED_BAR_LENGTH = 200;
    private static final int MINIMUM_BAR_WIDTH = 4;
    private static final int MINIMUM_SELECTED_WIDTH = 3;
    private static final int MINIMUM_TEXT_WIDTH = 80;
    private static final int PREFERRED_HEIGHT = 600;
    private static final int MIN_FONT_SIZE = FontUtilities.getOutputFontSize();
    private static final int TOP_MARGIN = 3;
    private static final int BOTTOM_MARGIN = 3;
    private static final int LEFT_MARGIN = 3;
    private static final int RIGHT_MARGIN = 3;
    private static final int TEXT_TO_BAR_GAP = 10;
    private final HistogramTopComponent topComponent;
    private int preferredHeight;
    private int iconPadding;
    private int barHeight;   // the vertical thickness of the bars
    private int barsWidth; //the length of the longest bar
    private int textWidth; //the width of the space allocated to text
    private float scaleFactor; //the scale factor from histogram count to bar length in pixels
    private int preferredTextWidth = 80; // this will be modified when the component is painted and the actual size of the text is measured.
    private final Dimension preferredSize = new Dimension(MINIMUM_TEXT_WIDTH + PREFERRED_BAR_LENGTH + TEXT_TO_BAR_GAP + 2, PREFERRED_HEIGHT);
    private BinCollection binCollection = null;
    private BinIconMode binIconMode = BinIconMode.NONE;
    private BinSelectionMode binSelectionMode;
    private int activeBin = -1;
    private int dragStart = -1;
    private int dragEnd = -1;
    private boolean shiftDown;
    private boolean controlDown;
    private boolean binCollectionOutOfDate = true;

    public HistogramDisplay(HistogramTopComponent topComponent) {

        this.topComponent = topComponent;

        initializeSettings();
        initializeListeners();
    }
    
    public final void initializeSettings(){
        setBackground(BACKGROUND_COLOR);
    }
    
    public final void initializeListeners(){
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addComponentListener(this);
    }
        

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        if (binCollection != null) {
            preferredHeight = preferredSize.height = calculateHeightAndBarWidth()[0];
        }

        return preferredSize;
    }

    public void setBinCollection(BinCollection binCollection, BinIconMode binIconMode) {
        this.binCollection = binCollection;
        this.binIconMode = binIconMode;
        binCollectionOutOfDate = true;
        activeBin = -1;
        repaint();
    }

    public void updateBinCollection() {
        binCollection.deactivateBins();
        activeBin = -1;
        repaint();
    }

    public void setBinSelectionMode(BinSelectionMode binSelectionMode) {
        this.binSelectionMode = binSelectionMode;
    }

    /**
     *
     *
     * @return {height,barwidth}
     */
    private int[] calculateHeightAndBarWidth() {
        int[] result = new int[2];
        int n = binCollection.getBins().length + 1;

        int sizeAtMinBarThickness = TOP_MARGIN + (n * MINIMUM_BAR_HEIGHT) + ((n - 1) * GAP_BETWEEN_BARS) + BOTTOM_MARGIN;
        if (sizeAtMinBarThickness > PREFERRED_HEIGHT) {
            result[0] = sizeAtMinBarThickness;
            result[1] = MINIMUM_BAR_HEIGHT;
        } else {
            result[0] = PREFERRED_HEIGHT;
            if (n == 0) {
                result[1] = 0;
            } else {
                int barThickness = ((PREFERRED_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN) - (n * GAP_BETWEEN_BARS)) / n;
                result[1] = Math.min(MAXIMUM_BAR_HEIGHT, barThickness);
            }
        }

        return result;
    }

    private int setFontToFit(final Graphics g, final int barSize) {
        int fontSize = MAXIMUM_BAR_HEIGHT - 4;
        int size;
        Font font;
        FontMetrics metrics;

        do {
            font = FONT.deriveFont((float) fontSize);
            metrics = g.getFontMetrics(font);
            size = metrics.getMaxAscent() + metrics.getMaxDescent();
        } while (--fontSize > MIN_FONT_SIZE && size > barSize - 4);

        g.setFont(font);
        return Math.round(size / 2f) - metrics.getMaxDescent();
    }

    private int getPreferredTextWidth(Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        int maxWidth = metrics.stringWidth(PROPERTY_VALUE); // Property Value is the largest string that always appears in a histogram
        for (Bin bin : binCollection.getBins()) {
            final String label = bin.toString();
            int width = label == null ? 0 : metrics.stringWidth(label);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        return maxWidth;
    }

    /**
     * * calculate how large to draw the bars and the text. If the total width
     * is not wide enough for the minimum text length and the preferred bar
     * length then draw the text at minimum length and give the rest to the
     * bars. If the total width is greater than the minimum text length +
     * preferred bar length but not long enough for the preferred text length +
     * preferred bar length then set the bars to preferred length and give the
     * rest to the text. Finally if the total width is greater than the
     * preferred text length + preferred bar length then set the text to the
     * preferred length and give the rest to the bars.**
     */
    private void calculateTextAndBarLength(Graphics g, int padding) {
        int parentWidth = getParent().getWidth();
        preferredTextWidth = getPreferredTextWidth(g);

        if (parentWidth < LEFT_MARGIN + padding + MINIMUM_TEXT_WIDTH + TEXT_TO_BAR_GAP + PREFERRED_BAR_LENGTH + RIGHT_MARGIN) {
            textWidth = MINIMUM_TEXT_WIDTH;
            barsWidth = Math.max(1, parentWidth - LEFT_MARGIN - padding - MINIMUM_TEXT_WIDTH - TEXT_TO_BAR_GAP - RIGHT_MARGIN);

        } else { //bars are at desired length. Expand text space unless it is already sufficient

            if (parentWidth < LEFT_MARGIN + padding + preferredTextWidth + TEXT_TO_BAR_GAP + PREFERRED_BAR_LENGTH + RIGHT_MARGIN) {
                barsWidth = PREFERRED_BAR_LENGTH;
                textWidth = parentWidth - LEFT_MARGIN - padding - PREFERRED_BAR_LENGTH - TEXT_TO_BAR_GAP - RIGHT_MARGIN;
            } else {
                textWidth = preferredTextWidth;
                barsWidth = parentWidth - LEFT_MARGIN - padding - preferredTextWidth - TEXT_TO_BAR_GAP - RIGHT_MARGIN;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (binCollection == null) {

            // Not have rendering hints set here is deliberate.
            // The PropertySheet does the same thing.
            final FontMetrics fm = g2.getFontMetrics();
            final int w = this.getParent().getWidth();
            final int h = getParent().getHeight();
            final Rectangle2D bounds = fm.getStringBounds(NO_DATA, g2);
            g2.setColor(Color.LIGHT_GRAY);

            g2.drawString(NO_DATA, (int) (w - bounds.getWidth()) / 2, (int) (h - bounds.getHeight()) / 2);

        } else if (binCollection.getBins().length == 0) {
            // Draw nothing: there is data, but the user doesn't want to see it.
        } else {

            Bin[] bins = binCollection.getBins();

            if (binCollectionOutOfDate) {

                final int[] dims = calculateHeightAndBarWidth();
                preferredHeight = dims[0];
                barHeight = dims[1];
                
                iconPadding = (int) (binIconMode.getWidth() * barHeight);

                calculateTextAndBarLength(g2, iconPadding);
                binCollectionOutOfDate = false;
            }

            // We want to get the width of the widest text so we know how much space to reserve for text.
            final int correction = setFontToFit(g2, barHeight);

            g2.setColor(BACKGROUND_COLOR);
            g2.fillRect(0, 0, getWidth(), preferredHeight - 1);

            final int arc = barHeight / 3;

            final int maxCount = binCollection.getMaxElementCount();
            if (maxCount > 0) {

                scaleFactor = barsWidth / (float) maxCount;

                // Only draw the bars that are visible
                JViewport viewPort = (JViewport) getParent();
                Rectangle viewSize = viewPort.getViewRect();
                final int firstBar = this.getBarAtPoint(new Point(0, viewSize.y), true);
                final int lastBar = this.getBarAtPoint(new Point(0, viewSize.y + viewSize.height), true);

                final int barOffset = GAP_BETWEEN_BARS + barHeight;

                final int barLeft = LEFT_MARGIN + iconPadding + textWidth + TEXT_TO_BAR_GAP;
                int barTop = TOP_MARGIN + barOffset * (firstBar + 1); // (firstBar+1) to account for header

                Color barColor = binSelectionMode.getBarColor();
                Color darkerBarColor = barColor.darker();

                Color activatedBarColor = binSelectionMode.getActivatedBarColor();
                Color darkerActivatedBarColor = activatedBarColor.darker();

                Color selectedColor = binSelectionMode.getSelectedColor();
                Color darkerSelectedColor = selectedColor.darker();

                Color activatedSelectedColor = binSelectionMode.getActivatedSelectedColor();
                Color darkerActivatedSelectedColor = activatedSelectedColor.darker();

                // Draw the histogram headers
                g2.setColor(Color.WHITE);

                final String headerStringValue = getStringToFit(PROPERTY_VALUE, textWidth, g2);
                final String headerStringCount = getStringToFit(COUNT, barsWidth, g2);
                g2.drawString(headerStringValue, LEFT_MARGIN + iconPadding, TOP_MARGIN + (barHeight / 2) + correction);
                g2.drawString(headerStringCount, barLeft, TOP_MARGIN + (barHeight / 2) + correction);

                // Draw the visible bars.
                for (int bar = firstBar; bar <= lastBar; bar++) {
                    Bin bin = bins[bar];

                    final int selectedCount = bin.selectedCount;
                    final int elementCount = bin.elementCount;

                    // Always draw something, even if there aren't enough pixels to draw the actual length.
                    final int barLength = Math.max((int) (elementCount * scaleFactor), MINIMUM_BAR_WIDTH);

                    // Draw the background
                    if (elementCount < maxCount) {
                        g2.setColor(bar == activeBin ? ACTIVE_AREA_COLOR : CLICK_AREA_COLOR);
                        int backgroundStart = Math.max(0, barLength - 10);
                        g2.fillRoundRect(barLeft + backgroundStart, barTop, barsWidth - backgroundStart, barHeight, arc, arc);
                    }

                    // Calculate the length of the selected component of the bar
                    int selectedLength = 0;
                    if (selectedCount > 0) {
                        selectedLength = Math.max(barLength * selectedCount / elementCount, MINIMUM_SELECTED_WIDTH);
                    }

                    // Draw the unselected component of the bar
                    if (selectedLength < barLength) {
                        Paint paint = bin.activated
                                ? new GradientPaint(0, barTop, activatedBarColor, 0, (float) barTop + barHeight, darkerActivatedBarColor)
                                : new GradientPaint(0, barTop, barColor, 0, (float) barTop + barHeight, darkerBarColor);
                        g2.setPaint(paint);
                        int unselectedStart = Math.max(0, selectedLength - 10);
                        g2.fillRoundRect(barLeft + unselectedStart, barTop, barLength - unselectedStart, barHeight, arc, arc);
                    }

                    // Draw the selected component of the bar
                    if (selectedLength > 0) {
                        Paint paint = bin.activated
                                ? new GradientPaint(0, barTop, activatedSelectedColor, 0, (float) barTop + barHeight, darkerActivatedSelectedColor)
                                : new GradientPaint(0, barTop, selectedColor, 0, (float) barTop + barHeight, darkerSelectedColor);
                        g2.setPaint(paint);
                        g2.fillRoundRect(barLeft, barTop, selectedLength, barHeight, arc, arc);
                    }

                    binIconMode.draw(g2, bin, LEFT_MARGIN, barTop, barHeight);

                    // Move to the next bar
                    barTop += barOffset;
                }

                // Draw the text.
                if (correction != Integer.MAX_VALUE) {
                    final FontMetrics fm = g2.getFontMetrics();
                    final int parentWidth = getParent().getWidth();
                    g2.setColor(Color.LIGHT_GRAY);
                    GraphicsEnvironment.getLocalGraphicsEnvironment();

                    for (int bar = firstBar; bar <= lastBar; bar++) {
                        Bin bin = bins[bar];

                        final int y = TOP_MARGIN + ((bar + 1) * barOffset) + (barHeight / 2) + correction; //(bar+1) to account for header

                        // Category label text.
                        String category = bin.getLabel();
                        if (category == null) {
                            g2.setColor(Color.YELLOW);
                            final String fittingString = getStringToFit(NO_VALUE, textWidth, g2);
                            g2.drawString(fittingString, LEFT_MARGIN + iconPadding, y);
                            g2.setColor(Color.LIGHT_GRAY);
                        } else {
                            final String fittingString = getStringToFit(category, textWidth, g2);
                            g2.drawString(fittingString, LEFT_MARGIN + iconPadding, y);
                        }

                        // Bin count text.
                        String binCount = Integer.toString(bin.elementCount);
                        if (bin.selectedCount > 0) {
                            binCount = Integer.toString(bin.selectedCount) + "/" + binCount;
                        }
                        final Rectangle2D bounds = fm.getStringBounds(binCount, g2);
                        g2.drawString(binCount, (int) (parentWidth - bounds.getWidth() - RIGHT_MARGIN - 2), y);
                    }
                }
            }
        }

        // We need to revalidate here because it's entirely possible that the number of bars has changed since the
        // previous paintComponent(), so the vertical scrollbar may need to be rejiggered.
        revalidate();
    }

    private String getStringToFit(String original, int width, Graphics g) {
        FontMetrics metrics = g.getFontMetrics();

        // Will the entire string fit?
        int widthOfText = metrics.stringWidth(original);
        if (widthOfText <= width) {
            return original;
        }

        // The largest length that we know will fit
        int min = 1;

        // The smallest length that we know is too big
        int max;

        if (original.length() < 1000) {
            max = original.length();
        } else {
            max = 1;
            while (true) {
                min = max;
                max <<= 1;
                if (max >= original.length()) {
                    max = original.length();
                    break;
                } else if (metrics.stringWidth(original.substring(0, max) + "...") > width) {
                    break;
                }
            }
        }

        while (min < max - 1) {
            final int mid = (min + max) >>> 1;
            if (metrics.stringWidth(original.substring(0, mid) + "...") <= width) {
                min = mid;
            } else {
                max = mid;
            }
        }

        String result = original.substring(0, min) + "...";

        // Sometimes even 1 character is too wide
        if (min == 1) {
            while (!result.isEmpty() && metrics.stringWidth(result) > width) {
                result = result.substring(1);
            }
        }

        return result;
    }

    /**
     * Determine the bar that is under the specified point.
     * <p>
     * The bar number is mathematically calculated based on the position of bar
     * 0, the current bar height, and the specified position. If bounded is
     * false, no attempt is made to limit the bar number to the actual number of
     * bins, so the value returned may be less than zero or greater than the
     * number of bins. If bounded is true, the bar number is bounded by the
     * number of bins (between 0 and bins-1 inclusive). by the actual number of
     * bins
     *
     * @param p A Point on the bar that will be returned.
     * @param bounded is the return value bounded by the number of bins?
     *
     * @return The index of the prospective bar under the Point, even if that
     * bar doesn't exist.
     */
    private int getBarAtPoint(Point p, boolean bounded) {
        int n = (int) ((p.y - 2 + GAP_BETWEEN_BARS / 2f) / (GAP_BETWEEN_BARS + barHeight)) - 1;
        if (bounded) {
            n = Math.min(Math.max(n, 0), binCollection.getBins().length - 1);
        }

        return n;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (binCollection != null) {

            Bin[] bins = binCollection.getBins();

            final Point pointOnHistogram = e.getPoint();
            final int bar = getBarAtPoint(pointOnHistogram, false);

            shiftDown = e.isShiftDown();
            controlDown = e.isControlDown();

            dragStart = (shiftDown && activeBin >= 0) ? activeBin : bar;
            dragEnd = bar;

            binSelectionMode.mousePressed(shiftDown, controlDown, bins, dragStart, dragEnd);

            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (binCollection != null) {

            Bin[] bins = binCollection.getBins();

            final Point pointOnHistogram = e.getPoint();
            final int bar = getBarAtPoint(pointOnHistogram, false);
            int newDragEnd = bar;
            binSelectionMode.mouseDragged(shiftDown, controlDown, bins, dragStart, dragEnd, newDragEnd);
            dragEnd = newDragEnd;
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (binCollection != null) {
            binSelectionMode.mouseReleased(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, topComponent);
            activeBin = dragStart == dragEnd ? dragStart : -1;
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        binCollectionOutOfDate = true;
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void componentShown(ComponentEvent e) {
        binCollectionOutOfDate = true;
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final JViewport scrollpane = (JViewport) getParent();
        final Point pos = scrollpane.getViewPosition();
        final int y;
        final int SCROLL_HEIGHT = 50;
        if (e.getWheelRotation() < 0){
             y = pos.y - (e.getScrollAmount() * SCROLL_HEIGHT);
        } else {
            y = pos.y + (e.getScrollAmount() * SCROLL_HEIGHT);
        }
        
        scrollpane.setViewPosition(new Point(0, Math.max(0, y)));
    }
}
