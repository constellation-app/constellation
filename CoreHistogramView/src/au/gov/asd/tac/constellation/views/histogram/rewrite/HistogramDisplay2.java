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
package au.gov.asd.tac.constellation.views.histogram.rewrite;

import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import javax.swing.event.MouseInputListener;

/**
 * The HistogramDisplay provides a panel the actually shows the histogram bins with their associated bars and labels.
 *
 * @author sirius
 * @author antares
 * @author sol695510
 */
public class HistogramDisplay2 extends BorderPane implements MouseInputListener, MouseWheelListener, KeyListener, PropertyChangeListener, ComponentListener {

    public static final Color BACKGROUND_COLOR = new Color(0x44, 0x44, 0x44);
    public static final String BACKGROUND_COLOR_STRING = "#424242";
    public static final Color BAR_COLOR = new Color(0.1176F, 0.5647F, 1.0F);
    public static final Color SELECTED_COLOR = Color.RED.darker();
    public static final Color ACTIVE_COLOR = Color.YELLOW;

    static final String NO_VALUE = "<No Value>";
    private static final String PROPERTY_VALUE = "Property Value";
    private static final String COUNT = "Count";
    private static final String TOTAL_BINS_COUNT = "Selected / Total Bin Count: ";

    // The color that shows where a bar would be if it was bigger.
    // This provides a guide to the user so they can click anywhere level with a bar,
    // even if the bar is really short.
    private static final Color CLICK_AREA_COLOR = BACKGROUND_COLOR.brighter();
    private static final Color ACTIVE_AREA_COLOR = CLICK_AREA_COLOR.brighter();
    private static final String NO_DATA = "<No Data>";
    private static final Font FONT = FontUtilities.getOutputFont();
    private static final int GAP_BETWEEN_BARS = 5;
    public static final int MINIMUM_BAR_HEIGHT = FontUtilities.getApplicationFontSize() <= 18 ? 18 : FontUtilities.getApplicationFontSize(); // Set back to private after histogram rewrite fully replaces old version
    public static final int MAXIMUM_BAR_HEIGHT = FontUtilities.getApplicationFontSize() <= 18 ? 18 : FontUtilities.getApplicationFontSize() + 10; // Set back to private after histogram rewrite fully replaces old version
    private static final int PREFERRED_BAR_LENGTH = 250;
    private static final int MINIMUM_BAR_WIDTH = 4;
    private static final int MINIMUM_SELECTED_WIDTH = 3;
    private static final int MINIMUM_TEXT_WIDTH = 100;
    private static final int PREFERRED_HEIGHT = 600;
    private static final int MIN_FONT_SIZE = FontUtilities.getApplicationFontSize();
    private static final int TOP_MARGIN = 3;
    private static final int BOTTOM_MARGIN = 3;
    private static final int LEFT_MARGIN = 3;
    private static final int RIGHT_MARGIN = 3;
    private static final int TEXT_TO_BAR_GAP = 10;
    private static final int MAX_USER_SET_BAR_HEIGHT = 99;
    private static final int MIN_USER_SET_BAR_HEIGHT = 2;
    private static final int ROWS_SPACING = 5;

    private final HistogramTopComponent2 topComponent;
    private int preferredHeight;
    private int iconPadding;
    private int barHeight;   // the vertical thickness of the bars
    private int userSetBarHeight = -1;   // the vertical thickness of the bars as set by the user
    private int barsWidth; // the length of the longest bar
    private int textWidth; // the width of the space allocated to text
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
    private final JPopupMenu copyMenu = new JPopupMenu();

    public HistogramDisplay2(HistogramTopComponent2 topComponent) {
        this.topComponent = topComponent;

        initializeSettings();
        initializeListeners();

        final JMenuItem copyValuesMenuItem = new JMenuItem("Copy Selected Property Values");
        copyValuesMenuItem.addActionListener(e -> copySelectedToClipboard(false));
        copyMenu.add(copyValuesMenuItem);

        final JMenuItem copyValuesAndCountsMenuItem = new JMenuItem("Copy Selected Property Values & Counts");
        copyValuesAndCountsMenuItem.addActionListener(e -> copySelectedToClipboard(true));
        copyMenu.add(copyValuesAndCountsMenuItem);

        setPrefHeight(PREFERRED_HEIGHT);
        setPrefWidth(MINIMUM_TEXT_WIDTH + PREFERRED_BAR_LENGTH + TEXT_TO_BAR_GAP + 2);

        //paintComponent();
    }

    public final void initializeSettings() {
        //setBackground(BACKGROUND_COLOR);
        setStyle("-fx-background-color: " + BACKGROUND_COLOR_STRING + ";");
        //this.setFocusable(true); // Set the Histogram View able to be focused.
        requestFocus(); // Focus the Histogram View so 'key' actions can be registered.
    }

    public final void initializeListeners() {
//        addMouseListener(this);
//        addMouseMotionListener(this);
//        addMouseWheelListener(this);
//        addComponentListener(this);
//        addKeyListener(this);
    }

//    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

//    @Override
    public Dimension getPreferredSize() {
        if (binCollection != null) {
            preferredHeight = preferredSize.height = calculateHeightAndBarWidth()[0];
        }

        return preferredSize;
    }

    public void setBinCollection(final BinCollection binCollection, final BinIconMode binIconMode) {
        System.out.println("setBinCollection " + binCollection);
        this.binCollection = binCollection;
        this.binIconMode = binIconMode;
        binCollectionOutOfDate = true;
        activeBin = -1;
        //repaint();
        Platform.runLater(() -> updateDisplay());
    }

    public void updateBinCollection() {
        binCollection.deactivateBins();
        activeBin = -1;
        //repaint();
        Platform.runLater(() -> updateDisplay());
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
        return Math.round(size / 2F) - metrics.getMaxDescent();
    }

    private double getPreferredTextWidth() {
        double minWidth = 0;
        for (final Bin bin : binCollection.getBins()) {
            final Label label = new Label(bin.toString());
            double width = label.getWidth();
            if (width > minWidth) {
                minWidth = width + 10;
            }
        }

        return minWidth;
    }

    /**
     * * calculate how large to draw the bars and the text. If the total width is not wide enough for the minimum text
     * length and the preferred bar length then draw the text at minimum length and give the rest to the bars. If the
     * total width is greater than the minimum text length + preferred bar length but not long enough for the preferred
     * text length + preferred bar length then set the bars to preferred length and give the rest to the text. Finally
     * if the total width is greater than the preferred text length + preferred bar length then set the text to the
     * preferred length and give the rest to the bars.**
     */
    private void calculateTextAndBarLength(final int padding) {
        // !!! need to rework this function, methinks
        final int parentWidth = topComponent.getWidth();
        final int preferredTextWidth = (int) Math.round(getPreferredTextWidth());
        textWidth = MINIMUM_TEXT_WIDTH;

        if (parentWidth < LEFT_MARGIN + padding + MINIMUM_TEXT_WIDTH + TEXT_TO_BAR_GAP + PREFERRED_BAR_LENGTH + RIGHT_MARGIN) {
            barsWidth = Math.max(1, parentWidth - LEFT_MARGIN - padding - MINIMUM_TEXT_WIDTH - TEXT_TO_BAR_GAP - RIGHT_MARGIN);

        } else { // Bars are at desired length. Expand text space unless it is already sufficient

            if (parentWidth < LEFT_MARGIN + padding + preferredTextWidth + TEXT_TO_BAR_GAP + PREFERRED_BAR_LENGTH + RIGHT_MARGIN) {
                barsWidth = PREFERRED_BAR_LENGTH;
                textWidth = parentWidth - LEFT_MARGIN - padding - PREFERRED_BAR_LENGTH - TEXT_TO_BAR_GAP - RIGHT_MARGIN;
            } else {
                textWidth = preferredTextWidth;
                barsWidth = parentWidth - LEFT_MARGIN - padding - preferredTextWidth - TEXT_TO_BAR_GAP - RIGHT_MARGIN;
            }
        }
    }

    private javafx.scene.paint.Color awtColorToFXColor(final Color awtColor) {
        final int r = awtColor.getRed();
        final int g = awtColor.getGreen();
        final int b = awtColor.getBlue();
        final int a = awtColor.getAlpha();
        final double opacity = a / 255.0;

        return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }

    //@Override
//    public void paintComponent(Graphics g) {
    public void updateDisplay() {
        //System.out.println("updateDisplay");
        //super.paintComponent(g);
        //Graphics2D g2 = (Graphics2D) g;

//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
//        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (binCollection == null) {
            //System.out.println("updateDisplay binCollection == null");

            // Not have rendering hints set here is deliberate.
            // The PropertySheet does the same thing.
//            final FontMetrics fm = g2.getFontMetrics();
//            final Rectangle2D bounds = fm.getStringBounds(NO_DATA, g2);
            //final int w = this.getParent().getWidth();
            //final int w = topComponent.getWidth();
            //final int h = getParent().getHeight();
            //final int h = topComponent.getHeight();
            // !!! Seems to draw "<no data>" in the middle of the display
//            g2.setColor(Color.LIGHT_GRAY);
//            g2.drawString(NO_DATA, (int) (w - bounds.getWidth()) / 2, (int) (h - bounds.getHeight()) / 2);
            final Label text = new Label("<No Data>");
            //text.setFill(Color.LIGHT_GRAY);// Should work but i think theres something wrong with imports or cache. different now its a label

//            final VBox viewPane = new VBox();
//            viewPane.getChildren().addAll(text);
            this.setCenter(text);

        } else if (binCollection.getBins().length == 0) {
            //System.out.println("updateDisplay binCollection.getBins().length == 0");
            // Draw nothing: there is data, but the user doesn't want to see it.
            this.setCenter(null);
        } else {
            //System.out.println("updateDisplay ELSE");

            Bin[] bins = binCollection.getBins();

            if (binCollectionOutOfDate) {

                final int[] dims = calculateHeightAndBarWidth();
                preferredHeight = dims[0];
                barHeight = dims[1];
                if (userSetBarHeight != -1) {
                    barHeight = userSetBarHeight;
                }

                iconPadding = (int) (binIconMode.getWidth() * barHeight);

                // !!! need to rework this function, methinks
                //calculateTextAndBarLength(g2, iconPadding);
                calculateTextAndBarLength(iconPadding);
                binCollectionOutOfDate = false;
            }

            // We want to get the width of the widest text so we know how much space to reserve for text.
            //final int correction = setFontToFit(g2, barHeight);
            final int correction = 0; // TODO remove when above is re done

            // !!! Seems to create the background of the display
            //g2.setColor(BACKGROUND_COLOR);
            //g2.fillRect(0, 0, (int) getWidth(), preferredHeight - 1);
            // !!! need to rework this function, methinks
            //calculateTextAndBarLength(g2, iconPadding);
            calculateTextAndBarLength(iconPadding);

            // !!! How round the edges should be
            final int arc = barHeight / 3;

            final int maxCount = binCollection.getMaxElementCount();
            System.out.println("maxCount: " + maxCount);
            if (maxCount > 0) {

                // the scale factor from histogram count to bar length in pixels
                final float scaleFactor = barsWidth / (float) maxCount;

                // !!! indexes (or maybe position) of first and last bar
                final int firstBar = getBarAtPoint(new Point(0, topComponent.getY()), true);
                final int lastBar = getBarAtPoint(new Point(0, topComponent.getY() + topComponent.getHeight()), true);

                final int barOffset = GAP_BETWEEN_BARS + barHeight;

                // !!! Position (screen position i think) of the leftmost and top most part of the bar
                //final int barLeft = LEFT_MARGIN + iconPadding + textWidth + TEXT_TO_BAR_GAP;
                int barTop = TOP_MARGIN + barOffset * (firstBar + 1); // (firstBar+1) to account for header

                // TODO: These could be static, methinks
                final Color barColor = binSelectionMode.getBarColor();
                final Color darkerBarColor = barColor.darker();
                //System.out.println("barColor: " + barColor + " darkerBarColor: " + darkerBarColor);

                final Color activatedBarColor = binSelectionMode.getActivatedBarColor();
                final Color darkerActivatedBarColor = activatedBarColor.darker();

                final Color selectedColor = binSelectionMode.getSelectedColor();
                final Color darkerSelectedColor = selectedColor.darker();

                final Color activatedSelectedColor = binSelectionMode.getActivatedSelectedColor();
                final Color darkerActivatedSelectedColor = activatedSelectedColor.darker();

                // Draw the histogram headers
                final VBox rows = new VBox(ROWS_SPACING);
                final HBox headerRow = new HBox();

                // ALT SETUP: Two columns, property values and bars
                final HBox columns = new HBox();
                final VBox propertyColumn = new VBox();
                final VBox barColumn = new VBox();

                propertyColumn.setMinWidth(MINIMUM_TEXT_WIDTH);
                propertyColumn.setSpacing(ROWS_SPACING);
                barColumn.setSpacing(ROWS_SPACING);

                columns.getChildren().addAll(propertyColumn, barColumn);

                final Label headerValue = new Label(PROPERTY_VALUE);
                final Label headerCount = new Label(COUNT);
                final Label headerTotalBins = new Label(TOTAL_BINS_COUNT);

                headerRow.setAlignment(Pos.CENTER);
                final Pane spacer1 = new Pane();
                final Pane spacer2 = new Pane();
                HBox.setHgrow(spacer1, Priority.ALWAYS);
                HBox.setHgrow(spacer2, Priority.ALWAYS);

                // Todo: Adjust spacing and see if we still needer headerCount
                headerRow.getChildren().addAll(headerValue, spacer1, headerCount, spacer2, headerTotalBins);

                propertyColumn.getChildren().add(headerValue);
                final HBox headerRow2 = new HBox();
                headerRow2.getChildren().addAll(headerCount, spacer2, headerTotalBins);
                barColumn.getChildren().add(headerRow2);

//                HBox.setHgrow(headerValue, Priority.ALWAYS);
//                HBox.setHgrow(headerCount, Priority.ALWAYS);
//                HBox.setHgrow(headerTotalBins, Priority.ALWAYS);
//                HBox.setHgrow(headerRow, Priority.ALWAYS);
                rows.getChildren().add(headerRow);

                // Draw the visible bars.
                for (int bar = firstBar; bar <= lastBar; bar++) {
                    final HBox row = new HBox();

                    final Bin bin = bins[bar];

                    /////// TEXT
                    // Category label text.
                    // TODO: Check if color needs to be implemented or whatver
                    final String category = bin.getLabel();
                    final String fittingString = getStringToFit(category == null ? NO_VALUE : category, textWidth);
                    final Label propertyValue = new Label(fittingString);
                    propertyValue.setMinHeight(barHeight);
                    row.getChildren().add(propertyValue);
                    propertyColumn.getChildren().add(propertyValue);

//                    final Pane spacer = new Pane();
//                    HBox.setHgrow(spacer, Priority.ALWAYS);
//                    row.getChildren().add(spacer);
                    final int selectedCount = bin.selectedCount;
                    final int elementCount = bin.elementCount;

                    // Always draw something, even if there aren't enough pixels to draw the actual length.
                    final int barLength = Math.max((int) (elementCount * scaleFactor), MINIMUM_BAR_WIDTH);
                    //System.out.println("elementCount: " + elementCount + " scaleFactor: " + scaleFactor + " barLength: " + barLength);

                    // Rectangle will be a stack pane with the different sections layer on top of each other
                    final StackPane rectBar = new StackPane();
                    // Draw the background of the bar
                    if (elementCount < maxCount) {
                        int backgroundStart = Math.max(0, barLength - 10);
                        //final Rectangle rect = new Rectangle(Double.valueOf(barsWidth - backgroundStart), Double.valueOf(barHeight));
                        final Rectangle rect = new Rectangle(Double.valueOf(barsWidth), Double.valueOf(barHeight));
                        rect.setArcHeight(arc);
                        rect.setArcWidth(arc);
                        rect.setFill(awtColorToFXColor(bar == activeBin ? ACTIVE_AREA_COLOR : CLICK_AREA_COLOR));

                        rectBar.getChildren().add(rect);
                    }

                    // Calculate the length of the selected component of the bar
                    final int selectedLength = (selectedCount > 0) ? Math.max(barLength * selectedCount / elementCount, MINIMUM_SELECTED_WIDTH) : 0;

                    // Draw the unselected component of the bar
                    if (selectedLength < barLength) {
                        int unselectedStart = Math.max(0, selectedLength - 10);

                        //Setting the linear gradient 
                        final Stop[] stops = bin.activated
                                ? new Stop[]{new Stop(0, awtColorToFXColor(activatedBarColor)), new Stop(1, awtColorToFXColor(darkerActivatedBarColor))}
                                : new Stop[]{new Stop(0, awtColorToFXColor(barColor)), new Stop(1, awtColorToFXColor(darkerBarColor))};

                        //final LinearGradient linearGradient = new LinearGradient(0, barTop, 0, barTop + barHeight, true, CycleMethod.NO_CYCLE, stops);
                        final LinearGradient linearGradient = new LinearGradient(0, 0, 0, barHeight, true, CycleMethod.NO_CYCLE, stops);

                        //System.out.println("Double.valueOf(barLength - unselectedStart) " + Double.valueOf(barLength - unselectedStart));
                        //final Rectangle rect = new Rectangle(Double.valueOf(barLength - unselectedStart), Double.valueOf(barHeight), linearGradient);
                        final Rectangle rect = new Rectangle(Double.valueOf(barLength), Double.valueOf(barHeight), linearGradient);
                        rect.setArcHeight(arc);
                        rect.setArcWidth(arc);

                        //row.getChildren().add(rect);
                        rectBar.getChildren().add(rect);
                        StackPane.setAlignment(rect, Pos.CENTER_LEFT);
                    }

                    // Draw the selected component of the bar
                    if (selectedLength > 0) {
                        //Setting the linear gradient 
                        final Stop[] stops = bin.activated
                                ? new Stop[]{new Stop(0, awtColorToFXColor(activatedSelectedColor)), new Stop(1, awtColorToFXColor(darkerActivatedSelectedColor))}
                                : new Stop[]{new Stop(0, awtColorToFXColor(selectedColor)), new Stop(1, awtColorToFXColor(darkerSelectedColor))};

                        final LinearGradient linearGradient = new LinearGradient(0, barTop, 0, barTop + barHeight, true, CycleMethod.NO_CYCLE, stops);

                        final Rectangle rect = new Rectangle(Double.valueOf(selectedLength), Double.valueOf(barHeight), linearGradient);
                        rect.setArcHeight(arc);
                        rect.setArcWidth(arc);

                        //row.getChildren().add(rect);
                        rectBar.getChildren().add(rect);
                        StackPane.setAlignment(rect, Pos.CENTER_LEFT);
                    }

                    // !!! draws an icon to the left of row/bar/whatever, if category has an icon. so far only color is supported (i think)
                    //binIconMode.draw(g2, bin, LEFT_MARGIN, barTop, barHeight);
                    // Move to the next bar
                    barTop += barOffset;

                    row.getChildren().add(rectBar);
                    barColumn.getChildren().add(rectBar);
                    // Add new row to this pane
                    rows.getChildren().add(row);
                }

                /*
                // Draw the text.
                if (correction != Integer.MAX_VALUE) {
                    final int parentWidth = topComponent.getWidth();
                    // !!! set colour for text?
                    //g2.setColor(Color.LIGHT_GRAY);
                    //GraphicsEnvironment.getLocalGraphicsEnvironment();

                    for (int bar = firstBar; bar <= lastBar; bar++) {
                        Bin bin = bins[bar];

                        final int y = TOP_MARGIN + ((bar + 1) * barOffset) + (barHeight / 2) + correction; // (bar+1) to account for header

                        // Category label text.
                        // TODO: reorganise with ternary if statemnts and whatnot
                        final String category = bin.getLabel();
                        if (category == null) {
                            // !!! set colour for text to be yellow, sets back to grey after
                            //g2.setColor(Color.YELLOW);
                            // !!! fits text in dispaly or something
                            //final String fittingString = getStringToFit(NO_VALUE, textWidth, g2);
                            // !!! Draws text
                            //g2.drawString(fittingString, LEFT_MARGIN + iconPadding, y);
                            // !!! sets back to grey here
                            //g2.setColor(Color.LIGHT_GRAY);

                            final String fittingString = getStringToFit(NO_VALUE, textWidth);
                            final Label propertyValue = new Label(fittingString);
                        } else {
                            // !!! same here, fit text and draw, should always be grey here
                            //final String fittingString = getStringToFit(category, textWidth, g2);
                            //g2.drawString(fittingString, LEFT_MARGIN + iconPadding, y);
                            final String fittingString = getStringToFit(category, textWidth);
                            final Label propertyValue = new Label(fittingString);

                        }

                        // Bin count text.
                        String binCount = Integer.toString(bin.elementCount);
                        if (bin.selectedCount > 0) {
                            binCount = Integer.toString(bin.selectedCount) + "/" + binCount;
                        }
                        // !!! write teh numbe rof bins to screen?
                        //final Rectangle2D bounds = fm.getStringBounds(binCount, g2);
                        //g2.drawString(binCount, (int) (parentWidth - bounds.getWidth() - RIGHT_MARGIN - 2), y);
                    }
                }
                 */
                //this.setCenter(rows);
                this.setCenter(columns);
            }
        }

        // We need to revalidate here because it's entirely possible that the number of bars has changed since the
        // previous paintComponent(), so the vertical scrollbar may need to be rejiggered.
        //revalidate();
    }

    // !! Seems to fit a given tring to the desired width, shortening if needed
    // TODO: this function has some stink to it, take a look and fix
    private String getStringToFit(final String original, final int width) {

        // Will the entire string fit?
        final double widthOfText = new Label(original).getWidth();

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
                } else if (new Label(original.substring(0, max) + "...").getWidth() > width) {
                    break;
                } else {
                    // Do nothing
                }
            }
        }

        while (min < max - 1) {
            final int mid = (min + max) >>> 1;
            if (new Label(original.substring(0, mid) + "...").getWidth() <= width) {
                min = mid;
            } else {
                max = mid;
            }
        }

        String result = original.substring(0, min) + "...";

        // Sometimes even 1 character is too wide
        if (min == 1) {
            while (!result.isEmpty() && new Label(result).getWidth() > width) {
                result = result.substring(1);
            }
        }

        return result;
    }

    /**
     * Determine the bar that is under the specified point.
     * <p>
     * The bar number is mathematically calculated based on the position of bar 0, the current bar height, and the
     * specified position. If bounded is false, no attempt is made to limit the bar number to the actual number of bins,
     * so the value returned may be less than zero or greater than the number of bins. If bounded is true, the bar
     * number is bounded by the number of bins (between 0 and bins-1 inclusive). by the actual number of bins
     *
     * @param p A Point on the bar that will be returned.
     * @param bounded is the return value bounded by the number of bins?
     *
     * @return The index of the prospective bar under the Point, even if that bar doesn't exist.
     */
    private int getBarAtPoint(Point p, boolean bounded) {
        int n = (int) ((p.y - 2 + GAP_BETWEEN_BARS / 2F) / (GAP_BETWEEN_BARS + barHeight)) - 1;
        if (bounded) {
            n = Math.min(Math.max(n, 0), binCollection.getBins().length - 1);
        }

        return n;
    }

    /**
     * Decrease height of barHeight
     *
     */
    public void decreaseBarHeight() {
        if (userSetBarHeight == -1 && barHeight > 2) {
            userSetBarHeight = barHeight - 2;
        } else if (userSetBarHeight > MIN_USER_SET_BAR_HEIGHT) {
            userSetBarHeight -= 2;
        }
        barHeight = userSetBarHeight;
        //repaint();
        updateDisplay();
    }

    /**
     * Increase height of barHeight
     *
     */
    public void increaseBarHeight() {
        if (userSetBarHeight == -1) {
            userSetBarHeight = barHeight + 2;
        } else if (userSetBarHeight < MAX_USER_SET_BAR_HEIGHT) {
            userSetBarHeight += 2;
        }
        barHeight = userSetBarHeight;
        //repaint();
        updateDisplay();
    }

    /**
     * Copy the values of the selected bars on the Histogram to the clipboard.
     * <p>
     * Iterates through the current collection of bins, bins representing the bars, and determines if they are selected
     * by checking their selectedCount value, 1 if selected, 0 if not selected.
     *
     * @param includeCounts True if the counts corresponding to the values are also to be copied to the clipboard.
     */
    private void copySelectedToClipboard(final boolean includeCounts) {
        final StringBuilder buf = new StringBuilder();
        for (final Bin bin : binCollection.getBins()) {
            // Check if the bar(s) on the Histogram are selected.
            if (bin.selectedCount > 0) {
                final String label = bin.getLabel() != null ? bin.getLabel() : HistogramDisplay2.NO_VALUE;
                if (includeCounts) {
                    buf.append(String.format("%s\t%d%n", label, bin.elementCount));
                } else {
                    buf.append(String.format("%s%n", label));
                }
            }
        }

        final StringSelection ss = new StringSelection(buf.toString());
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//        if (binCollection != null && e.getButton() == MouseEvent.BUTTON3) {
//            copyMenu.show(this, e.getX(), e.getY());
//        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (binCollection != null
                && e.getButton() == MouseEvent.BUTTON1) {
            final Point pointOnHistogram = e.getPoint();
            final int bar = getBarAtPoint(pointOnHistogram, false);

            shiftDown = e.isShiftDown();
            controlDown = e.isControlDown();

            dragStart = (shiftDown && activeBin >= 0) ? activeBin : bar;
            dragEnd = bar;
            binSelectionMode.mousePressed(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd);
            //repaint();
            updateDisplay();
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (binCollection != null
                && e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
            final Point pointOnHistogram = e.getPoint();
            final int bar = getBarAtPoint(pointOnHistogram, false);

            final int newDragEnd = bar;
            binSelectionMode.mouseDragged(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, newDragEnd);
            dragEnd = newDragEnd;
            //repaint();
            updateDisplay();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (binCollection != null
                && e.getButton() == MouseEvent.BUTTON1) {
            binSelectionMode.mouseReleased(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, topComponent);
            activeBin = dragStart == dragEnd ? dragStart : -1;
            //repaint();
            updateDisplay();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.requestFocus(); // Focus the Histogram View so 'key' actions can be registered.
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
//        if (binCollection != null
//                && this.isFocusOwner() // Check if Histogram Display is focused before allowing Ctrl + C to be registered.
//                && ((e.isControlDown()) && (e.getKeyCode() == KeyEvent.VK_C))) {
//            copySelectedToClipboard(false);
//        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Override required, intentionally left blank
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //this.repaint();
        updateDisplay();
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
//        final JViewport scrollpane = (JViewport) getParent();
//        final Point pos = scrollpane.getViewPosition();
//        final int y;
//        final int SCROLL_HEIGHT = 50;
//        if (e.getWheelRotation() < 0) {
//            y = pos.y - (e.getScrollAmount() * SCROLL_HEIGHT);
//        } else {
//            y = pos.y + (e.getScrollAmount() * SCROLL_HEIGHT);
//        }
//
//        scrollpane.setViewPosition(new Point(0, Math.max(0, y)));
    }
}
