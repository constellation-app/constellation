/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.utilities.headless.HeadlessUtilities;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import javafx.scene.paint.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * The HistogramDisplay provides a panel the actually shows the histogram bins with their associated bars and labels.
 *
 * @author sirius
 * @author antares
 * @author sol695510
 * @author Quasar985
 */
public class HistogramDisplay2 extends BorderPane {

    private static final String BACKGROUND_COLOR_STRING = "#444444";
    private static final Color BACKGROUND_COLOR = Color.web(BACKGROUND_COLOR_STRING);
    public static final Color BAR_COLOR = Color.rgb(30, 144, 255);
    public static final Color SELECTED_COLOR = Color.RED.darker();
    public static final Color ACTIVE_COLOR = Color.YELLOW;

    private static final String NO_VALUE = "<No Value>";
    private static final String PROPERTY_VALUE = "Property Value";
    private static final String COUNT = "Count";
    private static final String TOTAL_BINS_COUNT = "Selected / Total Bin Count: %d/%d";

    // The color that shows where a bar would be if it was bigger.
    // This provides a guide to the user so they can click anywhere level with a bar,
    // even if the bar is really short.
    private static final Color CLICK_AREA_COLOR = BACKGROUND_COLOR.brighter();
    private static final Color ACTIVE_AREA_COLOR = CLICK_AREA_COLOR.brighter();
    private static final String NO_DATA = "<No Data>";
    private static final int MINIMUM_BAR_HEIGHT = 2;
    private static final int MAXIMUM_BAR_HEIGHT = 99;
    private static final int MINIMUM_BAR_WIDTH = 4;
    private static final int MINIMUM_SELECTED_WIDTH = 3;
    private static final int MINIMUM_TEXT_WIDTH = 30;
    private static final int MAXIMUM_TEXT_WIDTH = 300;
    private static final int PREFERRED_HEIGHT = 600;
    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int BAR_PADDING = 30;
    private static final int ICON_PADDING = 10;

    private final HistogramTopComponent2 topComponent;
    private int barHeightBase = 18;
    private int barHeight = (barHeightBase * FontUtilities.getApplicationFontSize()) / DEFAULT_FONT_SIZE;   // the vertical thickness of the bars
    private BinCollection binCollection = null;
    private BinIconMode binIconMode = BinIconMode.NONE;
    private BinSelectionMode binSelectionMode = BinSelectionMode.ADD_TO_SELECTION;
    private int activeBin = -1;
    private int dragStart = -1;
    private int dragEnd = -1;
    private boolean shiftDown;
    private boolean controlDown;
    private final ContextMenu copyMenu = new ContextMenu();

    private final VBox mainVBox = new VBox();

    private static final float FONT_SCALE_FACTOR = 0.66F;

    private static final String STYLE_SETTING = "-fx-background-color: %s;";
    private static final String HEADER_ROW_CSS_CLASS = "header-row";
    private static final String FONT_SIZE_CSS_PROPERTY = "-fx-font-size: ";

    private final TableView<HistogramBar> tableView = new TableView<>();

    private double barWidth = 0;
    private static final int VISIBLE_INDEX_EXTEND = 3;

    private static final int DEFAULT_FIRST_VISIBLE_INDEX = 0;
    private static final int DEFAULT_LAST_VISIBLE_INDEX = 40;
    private int firstVisibleIndex = DEFAULT_FIRST_VISIBLE_INDEX;
    private int lastVisibleIndex = DEFAULT_LAST_VISIBLE_INDEX;

    // Table view columns
    private final TableColumn<HistogramBar, Node> iconCol = new TableColumn<>("Icon");
    private final TableColumn<HistogramBar, String> propertyCol = new TableColumn<>("Property");
    private final TableColumn<HistogramBar, StackPane> barCol = new TableColumn<>("Bar");

    private final HBox headerRow = new HBox();
    private final HBox headerCountHBox = new HBox();

    private double prevScrollValue = 0;

    private int prevNumBars = 0;

    // Need to take 3 because otherwise the right of the bar is cut off
    private static final int BAR_LENGTH_SUBTRACTION = 3;

    private double prevPropertyWidth = MINIMUM_TEXT_WIDTH;
    private static final int PROPERTY_WIDTH_PADDING = 30;

    private static final int GAP_BETWEEN_BARS = 6;

    public static Color getBackgroundColor() {
        return BACKGROUND_COLOR;
    }

    public static Color getBarColor() {
        return BAR_COLOR;
    }

    public static Color getSelectedColor() {
        return SELECTED_COLOR;
    }

    public static Color getActiveColor() {
        return ACTIVE_COLOR;
    }

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

        propertyCol.setCellValueFactory(new PropertyValueFactory<>("propertyName"));
        propertyCol.setResizable(false);
        propertyCol.setMinWidth(MINIMUM_TEXT_WIDTH);
        propertyCol.setMaxWidth(MAXIMUM_TEXT_WIDTH);

        final PseudoClass noValueClass = PseudoClass.getPseudoClass("no-value");
        propertyCol.setCellFactory(tableColumn -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Requires this for text to appear, for some reason
                this.setText(item);

                this.pseudoClassStateChanged(noValueClass, NO_VALUE.equals(item));
            }
        });

        iconCol.setCellValueFactory(new PropertyValueFactory("icon"));
        iconCol.setResizable(false);
        iconCol.setMinWidth(barHeight);
        iconCol.setPrefWidth(barHeight + ICON_PADDING);
        iconCol.setStyle("-fx-alignment: CENTER;");

        barCol.setCellValueFactory(new PropertyValueFactory("bar"));
        barCol.setResizable(false);
        barCol.setStyle("-fx-alignment: CENTER-LEFT;");

        // Automatically adjusts width of the column to fit
        tableView.widthProperty().addListener((obs, oldVal, newVal) -> barCol.setPrefWidth((double) newVal - Math.max(prevPropertyWidth, MINIMUM_TEXT_WIDTH) - iconCol.getWidth() - BAR_PADDING));

        // Update table whenever histogram is resized
        barCol.widthProperty().addListener((obs, oldVal, newVal) -> updateTable(true, (double) newVal - BAR_LENGTH_SUBTRACTION));

        tableView.getColumns().setAll(iconCol, propertyCol, barCol);
        tableView.getStyleClass().add("noheader");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        VBox.setVgrow(tableView, Priority.ALWAYS);

        //Workaround to attach listener to the table view's scroll bar
        tableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                initilizeScrollListener();
            }
        });

        tableView.setRowFactory(tv -> {
            final TableRow<HistogramBar> row = new TableRow<>();

            // handle drag start
            row.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
                event.consume();
                row.startFullDrag();
            });

            // handle selecting items when the mouse-drag enters the row
            row.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
                event.consume();
                if (event.getGestureSource() != row) {
                    final int newDragEnd = row.getIndex();
                    binSelectionMode.mouseDragged(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, newDragEnd);
                    dragEnd = newDragEnd;

                    // Select the dragged row on the table view too
                    tableView.getSelectionModel().select(newDragEnd);

                    // Only need to update bars
                    updateTableBars();
                    updateHeader(barHeight * FONT_SCALE_FACTOR);
                }
            });
            return row;
        });

        headerCountHBox.minWidthProperty().bind(barCol.widthProperty().add(BAR_PADDING));
        headerCountHBox.setAlignment(Pos.CENTER_RIGHT);

        headerRow.setAlignment(Pos.CENTER_RIGHT);
        headerRow.setMinHeight(barHeight);

        mainVBox.getChildren().addAll(headerRow, tableView);

        this.setCenter(mainVBox);

        updateBarHeight();

        initializeListeners();
    }

    private void initializeSettings() {
        setStyle(String.format(STYLE_SETTING, BACKGROUND_COLOR_STRING));
        requestFocus(); // Focus the Histogram View so 'key' actions can be registered.
    }

    private void initializeListeners() {
        this.setOnMouseEntered(e -> handleMouseEntered());
        this.setOnKeyPressed(e -> handleKeyPressed(e));

        tableView.setOnMouseClicked(e -> handleMouseClicked(e));
        tableView.setOnMousePressed(e -> handleMousePressed(e));
        tableView.setOnMouseReleased(e -> handleMouseReleased(e));
    }

    private void initilizeScrollListener() {
        final ScrollBar verticalBar = (ScrollBar) tableView.lookup(".scroll-bar:vertical");
        if (verticalBar == null) {
            return;
        }

        recalculateVisibleIndexes(prevScrollValue);

        verticalBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            recalculateVisibleIndexes((double) newVal);
            updateTable(true, barWidth);
        });
    }

    public void setBinCollection(final BinCollection binCollection, final BinIconMode binIconMode) {
        this.binCollection = binCollection;
        this.binIconMode = binIconMode;
        activeBin = -1;
        dragStart = -1;
        dragEnd = -1;
        tableView.getSelectionModel().clearSelection();
        Platform.runLater(() -> updateDisplay());
    }

    // For testing
    protected BinCollection getBinCollection() {
        return binCollection;
    }

    // For testing
    protected BinIconMode getBinIconMode() {
        return binIconMode;
    }

    public void updateBinCollection() {
        binCollection.deactivateBins();
        activeBin = -1;
        dragStart = -1;
        dragEnd = -1;
        tableView.getSelectionModel().clearSelection();
        Platform.runLater(() -> updateDisplay());
    }

    public void setBinSelectionMode(final BinSelectionMode binSelectionMode) {
        this.binSelectionMode = binSelectionMode;
    }

    public BinSelectionMode getBinSelectionMode() {
        return binSelectionMode;
    }

    public synchronized void updateDisplay() {
        if (Platform.isFxApplicationThread()) {
            updateHeaderAndTable();
        } else {
            Platform.runLater(() -> updateHeaderAndTable());
        }
    }

    private synchronized void updateHeaderAndTable() {
        recalculateVisibleIndexes(prevScrollValue);

        final double maxPropertyWidth = calculateLongestPropertyWidth(isRebuildRequired());
        propertyCol.setPrefWidth(maxPropertyWidth);

        barCol.setPrefWidth(tableView.getWidth() - maxPropertyWidth - iconCol.getWidth() - BAR_PADDING);

        updateHeader(barHeight * FONT_SCALE_FACTOR);
        updateTable(true, barWidth);
    }

    private boolean isRebuildRequired() {
        if (binCollection == null) {
            return true;
        }
        return (prevNumBars != binCollection.getBins().length) || tableView.getItems().isEmpty();
    }

    private synchronized void updateTable(final boolean updateBinCounts, final double width) {
        if (HeadlessUtilities.isHeadless()) {
            return;
        }

        if (binCollection == null) {
            // No data, so just have text saying so
            this.setCenter(new Label(NO_DATA));
            return;
        }

        if (binCollection.getBins().length == 0) {
            // Draw nothing: there is data, but the user doesn't want to see it.
            this.setCenter(null);
            return;
        }

        final int maxCount = binCollection.getMaxElementCount();
        if (maxCount < 1) {
            this.setCenter(new Label(NO_DATA));
            return;
        }

        barWidth = width;

        iconCol.setPrefWidth(barHeight + 10);

        // There is data and the user wants to see it
        final Bin[] bins = binCollection.getBins();

        final double fontSize = barHeight * FONT_SCALE_FACTOR;

        final ObservableList<HistogramBar> listOfHistogrambars = FXCollections.observableArrayList();

        final boolean rebuildRows = isRebuildRequired();

        for (int i = 0; i < bins.length; i++) {
            final Bin bin = bins[i];

            // Property Text
            final String propertyString;
            if (i >= firstVisibleIndex && i <= lastVisibleIndex) {
                final String category = bin.getLabel();
                propertyString = category == null ? NO_VALUE : category;
            } else {
                propertyString = null;
            }

            // Bar
            final StackPane rectBar = (i >= firstVisibleIndex && i <= lastVisibleIndex) ? constructBar(bin, maxCount, updateBinCounts, width, i, fontSize) : null;

            // Icon
            final Node icon = (i >= firstVisibleIndex && i <= lastVisibleIndex) ? binIconMode.createFXIcon(bin, barHeight) : null;

            // If rebuilding all row object put into list, else update row data
            if (rebuildRows) {
                listOfHistogrambars.add(new HistogramBar(icon, propertyString, rectBar, bin.getSelectedCount(), maxCount));
            } else {
                final HistogramBar histogramBar = tableView.getItems().get(i);
                histogramBar.setIcon(icon);
                histogramBar.setPropertyName(propertyString);
                histogramBar.setBar(rectBar);
            }
        }

        if (rebuildRows) {
            tableView.getItems().clear();
            tableView.setItems(listOfHistogrambars);
        }

        prevNumBars = bins.length;
        this.setCenter(mainVBox);
    }

    private void updateTableBars() {
        updateTableBars(true, barWidth);
    }

    private void updateTableBars(final boolean updateBinCounts, final double width) {
        final Bin[] bins = binCollection.getBins();
        barWidth = width;

        final int maxCount = binCollection.getMaxElementCount();
        if (maxCount < 1) {
            return;
        }

        final double fontSize = barHeight * FONT_SCALE_FACTOR;
        for (int i = 0; i < bins.length; i++) {
            final Bin bin = bins[i];

            // Bars
            final StackPane rectBar = (i >= firstVisibleIndex && i <= lastVisibleIndex) ? constructBar(bin, maxCount, updateBinCounts, width, i, fontSize) : null;

            tableView.getItems().get(i).setBar(rectBar);
        }
    }

    private void recalculateVisibleIndexes(final double scrollValue) {
        prevScrollValue = scrollValue;

        final int numItems = tableView.getItems().size();

        //get height (what the user can see)
        final float tableHeight = (float) tableView.getHeight();

        if (tableHeight == 0 && numItems > 0) {
            firstVisibleIndex = DEFAULT_FIRST_VISIBLE_INDEX;
            lastVisibleIndex = DEFAULT_LAST_VISIBLE_INDEX;
            return;
        }

        final float heightPerRow = barHeight;

        // get full height of tableView
        final float fullHeight = heightPerRow * numItems;

        final float lower = (fullHeight - tableHeight) * (float) scrollValue;
        final float upper = lower + tableHeight;

        firstVisibleIndex = (int) Math.ceil(lower / heightPerRow) - VISIBLE_INDEX_EXTEND;
        lastVisibleIndex = (int) Math.ceil(upper / heightPerRow) + VISIBLE_INDEX_EXTEND;
    }

    private double calculateLongestPropertyWidth(final boolean rebuildRows) {
        if (binCollection == null || tableView.getItems().isEmpty()) {
            return 0;
        }
        final Bin[] bins = binCollection.getBins();

        final ObservableList<HistogramBar> listOfHistogrambars = FXCollections.observableArrayList();
        for (int i = 0; i < bins.length; i++) {
            final Bin bin = bins[i];

            // Property Text
            final String category = bin.getLabel();
            final String propertyString = category == null ? NO_VALUE : category;

            // If rebuilding all row object put into list, else update row data
            if (rebuildRows) {
                listOfHistogrambars.add(new HistogramBar(null, propertyString, null, bin.getSelectedCount(), 0));
            } else {
                final HistogramBar histogramBar = tableView.getItems().get(i);
                histogramBar.setPropertyName(propertyString);
            }
        }

        if (rebuildRows) {
            tableView.getItems().clear();
            tableView.setItems(listOfHistogrambars);
        }

        // Create a Text object to measure string width
        final Text text = new Text();
        double maxWidth = 0;

        // Iterate through items in the TableView
        for (final HistogramBar item : tableView.getItems()) {
            // Get the string representation for the specific column
            final String cellValue = propertyCol.getCellData(item);
            text.setText(cellValue);
            double currentWidth = text.getLayoutBounds().getWidth();
            if (currentWidth > maxWidth) {
                maxWidth = currentWidth;
            }
        }

        prevPropertyWidth = maxWidth + PROPERTY_WIDTH_PADDING;
        return prevPropertyWidth;
    }

    private synchronized void updateHeader(final double fontSize) {
        if (HeadlessUtilities.isHeadless()) {
            return;
        }

        final Label headerValue = new Label(PROPERTY_VALUE);
        final Label headerCount = new Label(COUNT);

        final int numSelectedBins;
        final int numTotalBins;

        if (binCollection == null) {
            numSelectedBins = 0;
            numTotalBins = 0;
        } else {
            numSelectedBins = binCollection.getSelectedBins() != null ? binCollection.getSelectedBins().length : 0;
            numTotalBins = binCollection.getBins() != null ? binCollection.getBins().length : 0;
        }

        final Label headerTotalBins = new Label(String.format(TOTAL_BINS_COUNT, numSelectedBins, numTotalBins));

        // Set styling
        headerValue.getStyleClass().add(HEADER_ROW_CSS_CLASS);
        headerCount.getStyleClass().add(HEADER_ROW_CSS_CLASS);
        headerTotalBins.getStyleClass().add(HEADER_ROW_CSS_CLASS);

        headerValue.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);
        headerCount.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);
        headerTotalBins.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);

        headerValue.setMinHeight(barHeight);

        final Pane spacer = new Pane();
        final Pane spacer2 = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        headerCountHBox.getChildren().clear();
        headerCountHBox.getChildren().addAll(headerCount, spacer, headerTotalBins);

        headerRow.getChildren().clear();
        headerRow.getChildren().addAll(headerValue, spacer2, headerCountHBox);
    }

    private StackPane constructBar(final Bin bin, final int maxCount, final boolean updateBinCounts, final double width, final int barIndex, final double fontSize) {
        final int selectedCount = bin.getSelectedCount();
        final int elementCount = bin.getElementCount();

        final float lengthPerElement = (float) width / maxCount;
        final int arc = barHeight / 3;

        // Setup bar colours
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

        // Rectangle will be a stack pane with the different sections layer on top of each other
        final StackPane rectBar = new StackPane();

        // Draw the background of the bar
        if (elementCount < maxCount) {
            final Rectangle rect = new Rectangle(width, Double.valueOf(barHeight));

            rect.setArcHeight(arc);
            rect.setArcWidth(arc);
            rect.setFill(barIndex == activeBin ? ACTIVE_AREA_COLOR : CLICK_AREA_COLOR);

            rectBar.getChildren().add(rect);
            StackPane.setAlignment(rect, Pos.CENTER_LEFT);
        }

        // Calculate the length of the selected component of the bar
        final int selectedLength = (selectedCount > 0) ? Math.max(barLength * selectedCount / elementCount, MINIMUM_SELECTED_WIDTH) : 0;

        // Draw the unselected component of the bar
        if (selectedLength < barLength) {
            //Setting the linear gradient 
            final Stop[] stops = bin.getIsActivated()
                    ? new Stop[]{new Stop(0, activatedBarColor), new Stop(1, darkerActivatedBarColor)}
                    : new Stop[]{new Stop(0, barColor), new Stop(1, darkerBarColor)};

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
            final Stop[] stops = bin.getIsActivated()
                    ? new Stop[]{new Stop(0, activatedSelectedColor), new Stop(1, darkerActivatedSelectedColor)}
                    : new Stop[]{new Stop(0, selectedColor), new Stop(1, darkerSelectedColor)};

            final LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

            final Rectangle rect = new Rectangle(Double.valueOf(selectedLength), Double.valueOf(barHeight), linearGradient);
            rect.setArcHeight(arc);
            rect.setArcWidth(arc);

            rectBar.getChildren().add(rect);
            StackPane.setAlignment(rect, Pos.CENTER_LEFT);
        }

        // Draw bin count text
        if (updateBinCounts) {
            final String binCount = (bin.getSelectedCount() > 0) ? Integer.toString(bin.getSelectedCount()) + "/" + Integer.toString(bin.getElementCount()) : Integer.toString(bin.getElementCount());
            final Label binCountlabel = new Label(binCount);

            binCountlabel.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);
            binCountlabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("bar-bin-count"), true); // Set styling
            binCountlabel.setMinHeight(barHeight);

            rectBar.getChildren().add(binCountlabel);
            StackPane.setAlignment(binCountlabel, Pos.CENTER_RIGHT);
        }

        return rectBar;
    }

    private void updateBarHeight() {
        barHeight = (barHeightBase * FontUtilities.getApplicationFontSize()) / DEFAULT_FONT_SIZE;
        tableView.setStyle(FONT_SIZE_CSS_PROPERTY + barHeight * FONT_SCALE_FACTOR);
        tableView.setFixedCellSize(barHeight + GAP_BETWEEN_BARS);
    }

    /**
     * Decrease height of barHeight
     *
     */
    public void decreaseBarHeight() {
        barHeightBase -= 2;
        if (barHeightBase < MINIMUM_BAR_HEIGHT) {
            barHeightBase = MINIMUM_BAR_HEIGHT;
        }

        updateBarHeight();

        updateDisplay();
    }

    /**
     * Increase height of barHeight
     *
     */
    public void increaseBarHeight() {
        barHeightBase += 2;
        if (barHeightBase > MAXIMUM_BAR_HEIGHT) {
            barHeightBase = MAXIMUM_BAR_HEIGHT;
        }

        updateBarHeight();

        updateDisplay();
    }

    // For testing
    protected int getBarHeightBase() {
        return barHeightBase;
    }

    /**
     * Copy the values of the selected bars on the Histogram to the clipboard.
     * <p>
     * Iterates through the current collection of bins, bins representing the bars, and determines if they are selected
     * by checking their selectedCount value, 1 if selected, 0 if not selected.
     *
     * @param includeCounts True if the counts corresponding to the values are also to be copied to the clipboard.
     */
    protected void copySelectedToClipboard(final boolean includeCounts) {
        final StringBuilder buf = new StringBuilder();
        for (final Bin bin : binCollection.getBins()) {
            // Check if the bar(s) on the Histogram are selected.
            if (bin.getSelectedCount() > 0) {
                final String label = bin.getLabel() != null ? bin.getLabel() : HistogramDisplay2.NO_VALUE;
                if (includeCounts) {
                    buf.append(String.format("%s\t%d%n", label, bin.getElementCount()));
                } else {
                    buf.append(String.format("%s%n", label));
                }
            }
        }

        final StringSelection ss = new StringSelection(buf.toString());
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
    }

    protected void handleMouseClicked(final MouseEvent e) {
        if (binCollection != null && e.getButton() == MouseButton.SECONDARY) {
            copyMenu.show(this, e.getScreenX(), e.getScreenY());
        }
    }

    protected void handleMousePressed(final MouseEvent e) {
        if (binCollection == null || e.getButton() != MouseButton.PRIMARY) {
            return;
        }

        shiftDown = e.isShiftDown();
        controlDown = e.isControlDown();

        final int bar = tableView.getSelectionModel().getSelectedIndex();
        if (bar == -1) {
            return;
        }

        dragStart = (shiftDown && activeBin >= 0) ? activeBin : bar;
        dragEnd = bar;

        binSelectionMode.mousePressed(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd);

        // Only need to update bars
        updateTableBars();

        updateHeader(barHeight * FONT_SCALE_FACTOR);
    }

    protected void handleMouseReleased(final MouseEvent e) {
        this.requestFocus();
        if (binCollection == null || e.getButton() != MouseButton.PRIMARY || (dragStart == -1 && dragEnd == -1)) {
            return;
        }

        binSelectionMode.mouseReleased(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, topComponent);
        activeBin = dragStart == dragEnd ? dragStart : -1;

        // Only need to update bars
        updateTableBars();

        updateHeader(barHeight * FONT_SCALE_FACTOR);
    }

    protected void handleMouseEntered() {
        this.requestFocus(); // Focus the Histogram View so 'key' actions can be registered.
    }

    protected void handleKeyPressed(final KeyEvent e) {
        if (binCollection != null
                && this.isFocused() // Check if Histogram Display is focused before allowing Ctrl + C to be registered.
                && ((e.isControlDown()) && (e.getCode() == KeyCode.C))) {
            copySelectedToClipboard(false);
        }
    }

    public static class HistogramBar {

        // Icon
        private ObjectProperty<Node> icon;

        public final void setIcon(final Object value) {
            iconProperty().set(value);
        }

        public Object getIcon() {
            return iconProperty().get();
        }

        public ObjectProperty iconProperty() {
            if (icon == null) {
                icon = new SimpleObjectProperty<>(this, "icon");
            }
            return icon;
        }

        // PropertyName
        private StringProperty propertyName;

        public final void setPropertyName(final String value) {
            propertyNameProperty().set(value);
        }

        public String getPropertyName() {
            return propertyNameProperty().get();
        }

        public StringProperty propertyNameProperty() {
            if (propertyName == null) {
                propertyName = new SimpleStringProperty(this, "propertyName");
            }
            return propertyName;
        }

        // Bar
        private ObjectProperty<StackPane> bar;

        public final void setBar(final Object value) {
            barProperty().set(value);
        }

        public Object getBar() {
            return barProperty().get();
        }

        public ObjectProperty barProperty() {
            if (bar == null) {
                bar = new SimpleObjectProperty<>(this, "bar");
            }
            return bar;
        }

        // Stats
        private int selectedCount = 0;
        private int totalCount = 0;

        public int getSelectedCount() {
            return selectedCount;
        }

        public final void setSelectedCount(final int newValue) {
            selectedCount = newValue;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public final void setTotalCount(final int newValue) {
            totalCount = newValue;
        }

        public boolean isBarUpdateRequired(final int selectedCount, final int maxCount) {
            return getBar() == null || getSelectedCount() != selectedCount || getTotalCount() != maxCount;
        }

        public HistogramBar(final Node icon, final String propertName, final StackPane bar, final int selectedCount, final int totalCount) {
            setIcon(icon);
            setPropertyName(propertName);
            setBar(bar);
            setSelectedCount(selectedCount);
            setTotalCount(totalCount);
        }
    }
}
