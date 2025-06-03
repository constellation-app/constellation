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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * The HistogramDisplay provides a panel the actually shows the histogram bins with their associated bars and labels.
 *
 * @author sirius
 * @author antares
 * @author sol695510
 */
public class HistogramDisplay2 extends BorderPane implements MouseWheelListener, KeyListener, PropertyChangeListener, ComponentListener {

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
    private final ContextMenu copyMenu = new ContextMenu();

    final VBox propertyColumn = new VBox();
    final VBox barColumn = new VBox();

    //private final static javafx.scene.paint.Color LIGHT_GREY = javafx.scene.paint.Color.color(192, 192, 192);
    public HistogramDisplay2(final HistogramTopComponent2 topComponent) {
        this.topComponent = topComponent;

        initializeSettings();
        initializeListeners();

        final MenuItem copyValuesMenuItem = new MenuItem("Copy Selected Property Values");
        copyValuesMenuItem.setOnAction(e -> copySelectedToClipboard(false));
        copyMenu.getItems().add(copyValuesMenuItem);

        final MenuItem copyValuesAndCountsMenuItem = new MenuItem("Copy Selected Property Values & Counts");
        copyValuesAndCountsMenuItem.setOnAction(e -> copySelectedToClipboard(true));
        copyMenu.getItems().add(copyValuesAndCountsMenuItem);

        setPrefHeight(PREFERRED_HEIGHT);
        setPrefWidth(MINIMUM_TEXT_WIDTH + PREFERRED_BAR_LENGTH + TEXT_TO_BAR_GAP + 2);

//        barColumn.widthProperty().addListener((obs, oldVal, newVal) -> {
//            drawBars(binCollection.getBins(), binCollection.getMaxElementCount());
//        });
//        propertyColumn.widthProperty().addListener((obs, oldVal, newVal) -> {
//            drawBars(binCollection.getBins(), binCollection.getMaxElementCount());
//        });
        // Set up mouse listeners
        this.setOnMouseClicked(e -> handleMouseClicked(e));
        this.setOnMousePressed(e -> handleMousePressed(e));
        this.setOnMouseDragged(e -> handleMouseDragged(e));
        this.setOnMouseReleased(e -> handleMouseReleased(e));
        this.setOnMouseEntered(e -> handleMouseEntered(e));
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
        //System.out.println("setBinCollection " + binCollection);
        this.binCollection = binCollection;
        this.binIconMode = binIconMode;
        binCollectionOutOfDate = true;
        activeBin = -1;
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
            final double width = label.getWidth();
            //System.out.println("Label width: " + width);
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
        //System.out.println("calculateTextAndBarLength");
        // !!! need to rework this function, methinks
        final int parentWidth = topComponent.getWidth();
        final int preferredTextWidth = (int) Math.round(getPreferredTextWidth());
//        System.out.println("parentWidth: " + parentWidth);
//        System.out.println("preferredTextWidth: " + preferredTextWidth);
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

        if (textWidth < MINIMUM_TEXT_WIDTH) {
            textWidth = MINIMUM_TEXT_WIDTH;
        }
        //System.out.println("textWidth: " + textWidth);
    }

    private javafx.scene.paint.Color awtColorToFXColor(final Color awtColor) {
        final int r = awtColor.getRed();
        final int g = awtColor.getGreen();
        final int b = awtColor.getBlue();
        final int a = awtColor.getAlpha();
        final double opacity = a / 255.0;

        return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }

    public void updateDisplay() {
        if (binCollection == null) {
            // No data, so just have text saying so
            final Label text = new Label("<No Data>");
            this.setCenter(text);
        } else if (binCollection.getBins().length == 0) {
            // Draw nothing: there is data, but the user doesn't want to see it.
            this.setCenter(null);
        } else {
            // There is data and the user wants to see it
            final Bin[] bins = binCollection.getBins();

            // If bin collection out of date, recalculate lengths TODO: double check this is actaully what it does
            if (binCollectionOutOfDate) {

                final int[] dims = calculateHeightAndBarWidth();
                preferredHeight = dims[0];
                barHeight = dims[1];
                if (userSetBarHeight != -1) {
                    barHeight = userSetBarHeight;
                }

                iconPadding = (int) (binIconMode.getWidth() * barHeight);

                // !!! need to rework this function, methinks
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
            //System.out.println("maxCount: " + maxCount);

            if (maxCount > 0) {
                // the scale factor from histogram count to bar length in pixels
                final float scaleFactor = (barsWidth - textWidth) / (float) maxCount;

                // !!! indexes (or maybe position) of first and last bar
                final int firstBar = getBarAtPoint(new Point(0, topComponent.getY()), true);
                final int lastBar = getBarAtPoint(new Point(0, topComponent.getY() + topComponent.getHeight()), true);

                final int barOffset = GAP_BETWEEN_BARS + barHeight;

                // !!! Position (screen position i think) of the leftmost and top most part of the bar
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

                // Two columns, property values and bars
                final HBox columns = new HBox();

                HBox.setHgrow(columns, Priority.ALWAYS);

                barColumn.getChildren().clear();
                propertyColumn.getChildren().clear();

                propertyColumn.setMinWidth(MINIMUM_TEXT_WIDTH);
                propertyColumn.setPrefWidth(MINIMUM_TEXT_WIDTH * 1.5);
                propertyColumn.setSpacing(ROWS_SPACING);
                barColumn.setSpacing(ROWS_SPACING);

                HBox.setHgrow(barColumn, Priority.ALWAYS);

                columns.getChildren().addAll(propertyColumn, barColumn);

                final Label headerValue = new Label(PROPERTY_VALUE);
                final Label headerCount = new Label(COUNT);
                final Label headerTotalBins = new Label(TOTAL_BINS_COUNT + binCollection.getSelectedBins().length + "/" + bins.length);

                final Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                propertyColumn.getChildren().add(headerValue);
                final HBox headerRow2 = new HBox();
                headerRow2.getChildren().addAll(headerCount, spacer, headerTotalBins);
                barColumn.getChildren().add(headerRow2);

                // Populate category text column
                for (final Bin bin : bins) {
                    final String category = bin.getLabel();
                    final Label propertyValue;

                    if (category == null) {
                        // Make text yellow, and <no value>
                        propertyValue = new Label(getStringToFit(NO_VALUE, textWidth));
                        // Sets a psuedo class for css that changes the text colour to yellow
                        propertyValue.pseudoClassStateChanged(PseudoClass.getPseudoClass("no-value"), true);
                    } else {
                        // Regular grey text
                        propertyValue = new Label(getStringToFit(category, textWidth));
                        propertyValue.setTextFill(javafx.scene.paint.Color.grayRgb(192));
                    }

                    propertyValue.setMinHeight(barHeight);
                    propertyColumn.getChildren().add(propertyValue);
                }

                this.setCenter(columns);
            }
        }

        // Run function to draw the histogram bars later as currently the widths of columns are 0, in code
        Platform.runLater(() -> {
            if (binCollection != null) {
                drawBars(binCollection.getBins(), binCollection.getMaxElementCount());
            }
        });
    }

    private void drawBars(final Bin[] bins, final int maxCount) {
        final double width = barColumn.getWidth();
        //System.out.println("DRAW BARS propertyColumn width: " + propertyColumn.getWidth() + " barColumn width: " + barColumn.getWidth() + " width: " + width);

        final float lengthPerElement = (float) width / (float) maxCount; //(barsWidth - textWidth) / (float) maxCount;
        final int arc = barHeight / 3;
        //final Node header =  barColumn.getChildren().get(0);
        //barColumn.getChildren().clear();
        //barColumn.getChildren().add(header);

//        final Label headerCount = new Label(COUNT);
//        final Label headerTotalBins = new Label(TOTAL_BINS_COUNT);
//        final Pane spacer2 = new Pane();
//        HBox.setHgrow(spacer2, Priority.ALWAYS);
//        final HBox headerRow2 = new HBox();
//        headerRow2.getChildren().addAll(headerCount, spacer2, headerTotalBins);
//        barColumn.getChildren().add(headerRow2);
        //for (int bar = firstBar; bar <= lastBar; bar++) {
        for (int bar = 0; bar < bins.length; bar++) {
            final Bin bin = bins[bar];

            final int selectedCount = bin.selectedCount;
            final int elementCount = bin.elementCount;

            // TODO: These could be static, methinks
            final Color barColor = binSelectionMode.getBarColor();
            final Color darkerBarColor = barColor.darker();

            final Color activatedBarColor = binSelectionMode.getActivatedBarColor();
            final Color darkerActivatedBarColor = activatedBarColor.darker();

            final Color selectedColor = binSelectionMode.getSelectedColor();
            final Color darkerSelectedColor = selectedColor.darker();

            final Color activatedSelectedColor = binSelectionMode.getActivatedSelectedColor();
            final Color darkerActivatedSelectedColor = activatedSelectedColor.darker();

            // Always draw something, even if there aren't enough pixels to draw the actual length.
            final int barLength = Math.max((int) (elementCount * lengthPerElement), MINIMUM_BAR_WIDTH);
            //System.out.println("elementCount: " + elementCount + " scaleFactor: " + scaleFactor + " barLength: " + barLength);

            // Rectangle will be a stack pane with the different sections layer on top of each other
            final StackPane rectBar = new StackPane();

            // Draw the background of the bar
            if (elementCount < maxCount) {
                //int backgroundStart = Math.max(0, barLength - 10);
                final Rectangle rect = new Rectangle(width, Double.valueOf(barHeight));

                rect.setArcHeight(arc);
                rect.setArcWidth(arc);
                //System.out.println("bar == activeBin: " + (bar == activeBin));
                rect.setFill(awtColorToFXColor(bar == activeBin ? ACTIVE_AREA_COLOR : CLICK_AREA_COLOR));

                rectBar.getChildren().add(rect);
            }

            // Calculate the length of the selected component of the bar
            final int selectedLength = (selectedCount > 0) ? Math.max(barLength * selectedCount / elementCount, MINIMUM_SELECTED_WIDTH) : 0;

            // Draw the unselected component of the bar
            if (selectedLength < barLength) {
                //int unselectedStart = Math.max(0, selectedLength - 10);

                //Setting the linear gradient 
                final Stop[] stops = bin.activated
                        ? new Stop[]{new Stop(0, awtColorToFXColor(activatedBarColor)), new Stop(1, awtColorToFXColor(darkerActivatedBarColor))}
                        : new Stop[]{new Stop(0, awtColorToFXColor(barColor)), new Stop(1, awtColorToFXColor(darkerBarColor))};

                final LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

                final Rectangle rect = new Rectangle(Double.valueOf(barLength), Double.valueOf(barHeight), linearGradient);
                rect.setArcHeight(arc);
                rect.setArcWidth(arc);

                rectBar.getChildren().add(rect);
                StackPane.setAlignment(rect, Pos.CENTER_LEFT);
            }

            // Draw the selected component of the bar
            if (selectedLength > 0) {
                //Setting the linear gradient 
                final Stop[] stops = bin.activated
                        ? new Stop[]{new Stop(0, awtColorToFXColor(activatedSelectedColor)), new Stop(1, awtColorToFXColor(darkerActivatedSelectedColor))}
                        : new Stop[]{new Stop(0, awtColorToFXColor(selectedColor)), new Stop(1, awtColorToFXColor(darkerSelectedColor))};

                final LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

                final Rectangle rect = new Rectangle(Double.valueOf(selectedLength), Double.valueOf(barHeight), linearGradient);
                rect.setArcHeight(arc);
                rect.setArcWidth(arc);

                rectBar.getChildren().add(rect);
                StackPane.setAlignment(rect, Pos.CENTER_LEFT);
            }

            // Draw bin count text
            final String binCount = (bin.selectedCount > 0) ? Integer.toString(bin.selectedCount) + "/" + Integer.toString(bin.elementCount) : Integer.toString(bin.elementCount);
            final Label binCountlabel = new Label(binCount);
            binCountlabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("bar-bin-count"), true); // Set styling
            rectBar.getChildren().add(binCountlabel);
            StackPane.setAlignment(binCountlabel, Pos.CENTER_RIGHT);

//            row.getChildren().add(rectBar);
            barColumn.getChildren().add(rectBar);
        }
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
    private int getBarAtPoint(final Point p, final boolean bounded) {
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

    private void handleMouseClicked(final javafx.scene.input.MouseEvent e) {
        if (binCollection != null && e.getButton() == MouseButton.SECONDARY) {
            copyMenu.show(this, e.getScreenX(), e.getScreenY());
        }
    }

    private void handleMousePressed(final javafx.scene.input.MouseEvent e) {
        if (binCollection != null && e.getButton() == MouseButton.PRIMARY) {
            final Point pointOnHistogram = new Point((int) Math.round(e.getX()), (int) Math.round(e.getY())); // May need to be getScreenX(), no actually
            final int bar = getBarAtPoint(pointOnHistogram, false);

            shiftDown = e.isShiftDown();
            controlDown = e.isControlDown();

            dragStart = (shiftDown && activeBin >= 0) ? activeBin : bar;
            dragEnd = bar;
            binSelectionMode.mousePressed(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd);

            updateDisplay();
        }
    }

    // TODO: Not working properly
    private void handleMouseDragged(final javafx.scene.input.MouseEvent e) {
        System.out.println("handleMouseDragged");
        if (binCollection != null && e.isPrimaryButtonDown()) { // Not sure if same as e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK
            final Point pointOnHistogram = new Point((int) Math.round(e.getX()), (int) Math.round(e.getY())); // May need to be getScreenX()
            final int bar = getBarAtPoint(pointOnHistogram, false);

            final int newDragEnd = bar;
            binSelectionMode.mouseDragged(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, newDragEnd);
            dragEnd = newDragEnd;

            updateDisplay();
        }
    }

    private void handleMouseReleased(final javafx.scene.input.MouseEvent e) {
        if (binCollection != null && e.getButton() == MouseButton.PRIMARY) {
            binSelectionMode.mouseReleased(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, topComponent);
            activeBin = dragStart == dragEnd ? dragStart : -1;

            updateDisplay();
        }
    }

    private void handleMouseEntered(final javafx.scene.input.MouseEvent e) {
        this.requestFocus(); // Focus the Histogram View so 'key' actions can be registered.
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
