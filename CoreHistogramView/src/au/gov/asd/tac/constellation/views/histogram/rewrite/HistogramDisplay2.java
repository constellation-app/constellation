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
import au.gov.asd.tac.constellation.utilities.javafx.JavaFxUtilities;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.BinCollection;
import au.gov.asd.tac.constellation.views.histogram.BinIconMode;
import au.gov.asd.tac.constellation.views.histogram.BinSelectionMode;
import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.BitSet;
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
import javafx.scene.control.ListView;
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

/**
 * The HistogramDisplay provides a panel the actually shows the histogram bins with their associated bars and labels.
 *
 * @author sirius
 * @author antares
 * @author sol695510
 * @author Quasar985
 */
public class HistogramDisplay2 extends BorderPane {

    public static final String BACKGROUND_COLOR_STRING = "#444444";
    public static final Color BACKGROUND_COLOR = Color.decode(BACKGROUND_COLOR_STRING);
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
    private static final int GAP_BETWEEN_BARS = 5;
    private static final int MINIMUM_BAR_HEIGHT = 2;
    private static final int MAXIMUM_BAR_HEIGHT = 99;
    private static final int MINIMUM_BAR_WIDTH = 4;
    private static final int MINIMUM_SELECTED_WIDTH = 3;
    private static final int MINIMUM_TEXT_WIDTH = 150;
    private static final int MAXIMUM_TEXT_WIDTH = 300;
    private static final int PREFERRED_HEIGHT = 600;
    private static final int ROWS_SPACING = 5;
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
    private int prevDragEnd = -2; // Set to -2, so value is different to dragEnd
    private int dragStart = -1;
    private int dragEnd = -1;
    private boolean shiftDown;
    private boolean controlDown;
    private final ContextMenu copyMenu = new ContextMenu();

    private final VBox propertyColumn = new VBox();
    private final VBox barColumn = new VBox();
    private final VBox iconColumn = new VBox();
    private final HBox columns = new HBox();

    private final VBox barsHbox = new VBox(); // Holds the spacer and vbox containing the bars
    private final Pane barSpacer = new Pane(); // Matches the width of the property value column
    private final VBox barsVbox = new VBox(); // Holds just the bars, width of bars are based on barColum width

    private final VBox binCountsVbox = new VBox(); // Holds 

    // Pane that holds everything, stacks the bars on top
    //final StackPane stackPane = new StackPane();
    final VBox mainVBox = new VBox();

    private static final int COLUMNS_SPACING = 5;
    private double barsWidth = 0;
    private static final float FONT_SCALE_FACTOR = 0.66F;

    private static final String STYLE_SETTING = "-fx-background-color: %s;";
    private static final String HEADER_ROW_CSS_CLASS = "header-row";
    private static final String FONT_SIZE_CSS_PROPERTY = "-fx-font-size: ";

    private static final String TABLE_VIEW_CSS_CLASS = "histogramTable";

    // Table Vew
    private final ListView<StackPane> listView = new ListView<>();
    private final TableView<HistogramBar> tableView = new TableView<>();

    private double tableWidth = 0;
    private static final int VISIBLE_INDEX_EXTEND = 3;
    private int firstVisibleIndex;
    private int lastVisibleIndex;

    private static final int DEFAULT_FIRST_VISIBLE_INDEX = 0;
    private static final int DEFAULT_LAST_VISIBLE_INDEX = 40;

    // Table view columns
    private final TableColumn<HistogramBar, Node> iconCol = new TableColumn<>("Icon");
    private final TableColumn<HistogramBar, StackPane> barCol = new TableColumn<>("Bar");

    final HBox headerRow = new HBox();
    final HBox headerCountHBox = new HBox();

    final BitSet selectedRows = new BitSet(0);

    private int hoveredRowIndex = 0;

    private double prevScrollValue = 0;

    private int prevNumBars = 0;

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

        barColumn.widthProperty().addListener((obs, oldVal, newVal) -> drawBars((double) newVal));
        listView.widthProperty().addListener((obs, oldVal, newVal) -> drawBars((double) newVal));
        //tableView.widthProperty().addListener((obs, oldVal, newVal) -> drawBars((double) newVal * 0.69));

        final TableColumn<HistogramBar, String> propertyCol = new TableColumn<>("Property");
        propertyCol.setCellValueFactory(new PropertyValueFactory<>("propertyName"));
        propertyCol.setResizable(false);
        propertyCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));// TODO: fix these magic numbers
        propertyCol.setMinWidth(MINIMUM_TEXT_WIDTH);
        propertyCol.setMaxWidth(MAXIMUM_TEXT_WIDTH);

        final PseudoClass noValueClass = PseudoClass.getPseudoClass("noValue");
        propertyCol.setCellFactory(tableColumn -> {
            final TableCell<HistogramBar, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    // Requires this for text to appear, for some reason
                    this.setText(item);

                    if (NO_VALUE.equals(item)) {
                        this.pseudoClassStateChanged(noValueClass, true);
                    } else {
                        this.pseudoClassStateChanged(noValueClass, false);
                    }
                }
            };
            return cell;
        });

        iconCol.setCellValueFactory(new PropertyValueFactory("icon"));
        iconCol.setResizable(false);
        //iconCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));// TODO: fix these magic numbers
        iconCol.setMinWidth(barHeight);
        iconCol.setPrefWidth(barHeight + ICON_PADDING);
        iconCol.setStyle("-fx-alignment: CENTER;");

        barCol.setCellValueFactory(new PropertyValueFactory("bar"));
        barCol.setResizable(false);
        barCol.setStyle("-fx-alignment: CENTER-LEFT;");
        // Automatically adjusts width of the column to fit
        tableView.widthProperty().addListener((obs, oldVal, newVal) -> barCol.setPrefWidth((double) newVal - propertyCol.getWidth() - iconCol.getWidth() - BAR_PADDING));

        // Update table whenever histogram is resized
        barCol.widthProperty().addListener((obs, oldVal, newVal) -> {
            //drawBars((double) newVal - 20);
            // TODO update this function because in this case the header doesnt need to be remade
            updateTable(true, (double) newVal - 3); // Need to take 3 because otherwise the right of the bar is cut off
            //resizeBars((double) newVal - 3);
        });

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

            row.hoverProperty().addListener(
                    (obs, oldVal, newVal) -> {
                        //if (newVal) {
                        hoveredRowIndex = row.getIndex();
                        //System.out.println("hoveredRowIndex " + hoveredRowIndex);
                        //}
                    });
            // WORKS KINDA
//            row.selectedProperty().addListener(
//                    (obs, oldVal, newVal) -> {
//
//                        final int bar = tableView.getSelectionModel().getSelectedIndex();
//                        //System.out.println("row: " + row + " row.selectedProperty() obs: " + obs + " oldVal: " + oldVal + " newVal: " + newVal + " bar: " + bar + " row.getIndex(): " + row.getIndex());
//                        System.out.println("bar: " + bar + " row.getIndex(): " + row.getIndex() + " newVal: " + newVal);
//
//                        if (row.getIndex() == -1) {
//                            return;
//                        }
//
//                        // NEW
//                        selectedRows.set(row.getIndex(), newVal);
//                        binSelectionMode.populateFromBitSet(binCollection.getBins(), selectedRows, topComponent);
//                        Platform.runLater(() -> updateTableBars(true, tableWidth));
//                    });
            return row;
        });
//        // TODO put listner on  selectedIndexProperty(), probably more reliable that this setup
//        tableView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
//            System.out.println("selectedIndexProperty oldVal: " + oldVal + " newVal: " + newVal + " shiftDown " + shiftDown + " controlDown " + controlDown);
//
//            // for shift clicked rows, theres a -1 in the new value between the two clicked rows
//            // But it's proabbly better to just listen for the shift key becuase there's no pattern like this to differentiate control click and normal click
//        });

        //TODO find a way to add these to cell factory instead of calling old factory in new
        final var barColCellFactroy = barCol.getCellFactory();
        barCol.setCellFactory(vf -> {
            final TableCell<HistogramBar, StackPane> cell = barColCellFactroy.call(vf);
            addDraggableToCell(cell);
            return cell;
        });

        final var iconColCellFactroy = iconCol.getCellFactory();
        iconCol.setCellFactory(vf -> {
            final TableCell<HistogramBar, Node> cell = iconColCellFactroy.call(vf);
            addDraggableToCell(cell);
            return cell;
        });

        final var propertyColCellFactroy = propertyCol.getCellFactory();
        propertyCol.setCellFactory(vf -> {
            final TableCell<HistogramBar, String> cell = propertyColCellFactroy.call(vf);
            addDraggableToCell(cell);
            return cell;
        });

        headerCountHBox.minWidthProperty().bind(barCol.widthProperty().add(BAR_PADDING));
        headerCountHBox.setAlignment(Pos.CENTER_RIGHT);

        headerRow.setAlignment(Pos.CENTER_RIGHT);
        headerRow.setMinHeight(barHeight);

        columns.getChildren().addAll(iconColumn, propertyColumn, barColumn);
        columns.setMouseTransparent(true);
        columns.setSpacing(COLUMNS_SPACING);
        HBox.setHgrow(columns, Priority.ALWAYS);

        iconColumn.setSpacing(ROWS_SPACING);

        propertyColumn.setMinWidth(MINIMUM_TEXT_WIDTH);
        propertyColumn.setSpacing(ROWS_SPACING);

        barColumn.setSpacing(ROWS_SPACING);
        HBox.setHgrow(barColumn, Priority.ALWAYS);

        //stackPane.getChildren().addAll(columns, barsHbox, binCountsVbox);
//        stackPane.getChildren().add(listView);
        //stackPane.getChildren().add(tableView);
        mainVBox.getChildren().addAll(headerRow, tableView);

        barsVbox.setSpacing(ROWS_SPACING);
        barsVbox.setMouseTransparent(true);

        HBox.setHgrow(barSpacer, Priority.ALWAYS);
        barsHbox.setAlignment(Pos.TOP_RIGHT);
        barsHbox.getChildren().addAll(barSpacer, barsVbox);

        propertyColumn.widthProperty().addListener((obs, oldVal, newVal) -> barSpacer.setMaxWidth((double) newVal));

        binCountsVbox.setAlignment(Pos.TOP_RIGHT);
        binCountsVbox.setSpacing(ROWS_SPACING);

//        this.setCenter(stackPane);
        this.setCenter(mainVBox);

        updateBarHeight();

        initializeListeners();
    }

    private void initializeSettings() {
        setStyle(String.format(STYLE_SETTING, BACKGROUND_COLOR_STRING));
        requestFocus(); // Focus the Histogram View so 'key' actions can be registered.
    }

    private void initializeListeners() {
        // Set up mouse listeners
//        this.setOnMouseClicked(e -> handleMouseClicked(e));
//        this.setOnMousePressed(e -> handleMousePressed(e));
//        this.setOnMouseDragged(e -> handleMouseDragged(e));
//        this.setOnMouseReleased(e -> handleMouseReleased(e));
        this.setOnMouseEntered(e -> handleMouseEntered());

        this.setOnKeyPressed(e -> handleKeyPressed(e));

        tableView.setOnMouseClicked(e -> handleMouseClicked(e));
        tableView.setOnMousePressed(e -> handleMousePressed(e));
        tableView.setOnMouseDragged(e -> handleMouseDragged(e));
        tableView.setOnMouseReleased(e -> handleMouseReleased(e));
    }

    private void initilizeScrollListener() {
        final ScrollBar verticalBar = (ScrollBar) tableView.lookup(".scroll-bar:vertical");
        if (verticalBar == null) {
            return;
        }

        recalculateVisibleIndexes(prevScrollValue);

        verticalBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Scroll value listener");
            recalculateVisibleIndexes((double) newVal);
            updateTable(true, tableWidth);
        });
    }

    private void addDraggableToCell(final TableCell<?, ?> cell) {

        // handle drag start
        cell.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            System.out.println("DRAG DETECTED " + cell.getIndex());
            event.consume();
            cell.startFullDrag();
        });

        // handle selecting items when the mouse-drag enters the cell
        cell.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            System.out.println("MOUSE DRAG ENTERED " + cell.getIndex());
            event.consume();
            if (event.getGestureSource() != cell) {
                final int newDragEnd = cell.getIndex();
                binSelectionMode.mouseDragged(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, newDragEnd);
                setDragEnd(newDragEnd);

                // Only need to update bars
                updateTableBars();
                updateHeader(barHeight * FONT_SCALE_FACTOR);
            }
        });

    }

    public void setBinCollection(final BinCollection binCollection, final BinIconMode binIconMode) {
        this.binCollection = binCollection;
        this.binIconMode = binIconMode;
        activeBin = -1;
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
        Platform.runLater(() -> updateDisplay());
    }

    public void setBinSelectionMode(final BinSelectionMode binSelectionMode) {
        this.binSelectionMode = binSelectionMode;
    }

    public BinSelectionMode getBinSelectionMode() {
        return binSelectionMode;
    }

    private void setDragEnd(final int newValue) {
        prevDragEnd = dragEnd;
        dragEnd = newValue;
    }

    private boolean requireUpdate() {
        return prevDragEnd != dragEnd;// drag start doesnt change per mouseDragged fire
    }

    public synchronized void updateDisplay() {
        //        updateIcons();
//        updatePropertyText();
//        updateBars(true);
        recalculateVisibleIndexes(prevScrollValue);

        updateHeader(barHeight * FONT_SCALE_FACTOR);
        updateTable(true, tableWidth);
    }

    private synchronized void updateTable(final boolean updateBinCounts, final double width) {
        System.out.println("updateTable " + firstVisibleIndex + " " + lastVisibleIndex);
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

        // TODO check what should display in this case, probably empty bars idk
        final int maxCount = binCollection.getMaxElementCount();
        if (maxCount < 1) {
            return;
        }

        tableWidth = width;

        iconCol.setPrefWidth(barHeight + 10);

        // There is data and the user wants to see it
        final Bin[] bins = binCollection.getBins();

        final double fontSize = barHeight * FONT_SCALE_FACTOR;
//        updateHeader(bins.length, fontSize);

        final ObservableList<HistogramBar> listOfHistogrambars = FXCollections.observableArrayList();

        // TODO move elsewhere if this works
        tableView.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);

        System.out.println("prevNumBars != bins.length " + (prevNumBars != bins.length) + " tableView.getItems().isEmpty() " + tableView.getItems().isEmpty());
        final boolean rebuildRows = (prevNumBars != bins.length) || tableView.getItems().isEmpty();

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
            final StackPane rectBar;
//            System.out.println("i " + i);
//            // If not visible, set null
//            if (i < firstVisibleIndex && i > lastVisibleIndex) {
//                System.out.println("not visible");
//                rectBar = null;
//            } else {
//
//                // Should the bar be updated?
//                // if we need to completely rebuild all rows
//                if (rebuildRows) {
//                    System.out.println("rebuildRows");
//                    rectBar = constructBar(bin, maxCount, updateBinCounts, width, i, fontSize);
//                } else {
//                    final HistogramBar histogramBar = tableView.getItems().get(i);
//                    // If the bar is already built and requires an update
//                    if (tableView.getItems().get(i).isBarUpdateRequired(bin.selectedCount, maxCount)) {
//                        System.out.println("Required update");
//
//                        rectBar = constructBar(bin, maxCount, updateBinCounts, width, i, fontSize);
//
//                        // Also update selected and total counts
//                        histogramBar.setSelectedCount(bin.selectedCount);
//                        histogramBar.setTotalCount(maxCount);
//                    } else {
//                        System.out.println("Stayed the same");
//                        // Otherwise the bar stays the same
//                        rectBar = (StackPane) histogramBar.getBar();
//                    }
//                }
//            }

//            final HistogramBar histogramBar = tableView.getItems().get(i);
            //final boolean rebuildBar = rebuildRows || histogramBar.getSelectedCount() != bin.selectedCount || histogramBar.getTotalCount() != maxCount;
            rectBar = (i >= firstVisibleIndex && i <= lastVisibleIndex) ? constructBar(bin, maxCount, updateBinCounts, width, i, fontSize) : null;
            // Icon
            final Node icon = (i >= firstVisibleIndex && i <= lastVisibleIndex) ? binIconMode.createFXIcon(bin, barHeight) : null;

            // If rebuilding all row object put into list, else update row data
            if (rebuildRows) {
                listOfHistogrambars.add(new HistogramBar(icon, propertyString, rectBar, bin.selectedCount, maxCount, width));
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
        updateTableBars(true, tableWidth);
    }

    private void updateTableBars(final boolean updateBinCounts, final double width) {
        //System.out.println("updateTableBars");
        final Bin[] bins = binCollection.getBins();
        tableWidth = width;

        // TODO check what should display in this case, probably empty bars idk
        final int maxCount = binCollection.getMaxElementCount();
        if (maxCount < 1) {
            return;
        }

        final double fontSize = barHeight * FONT_SCALE_FACTOR;
        final boolean rebuildRows = (prevNumBars != bins.length) || tableView.getItems().isEmpty();
        for (int i = 0; i < bins.length; i++) {
            final Bin bin = bins[i];

            // Bars
            // TODO port over what i did in update bars
            final StackPane rectBar = (i >= firstVisibleIndex && i <= lastVisibleIndex) ? constructBar(bin, maxCount, updateBinCounts, width, i, fontSize) : null;
// Bar
//            final StackPane rectBar;
//            System.out.println("i " + i);
//            // If not visible, set null
//            if (i < firstVisibleIndex && i > lastVisibleIndex) {
//                System.out.println("not visible");
//                rectBar = null;
//            } else {
//
//                // Should the bar be updated?
//                // if we need to completely rebuild all rows
//                if (rebuildRows) {
//                    System.out.println("rebuildRows");
//                    rectBar = constructBar(bin, maxCount, updateBinCounts, width, i, fontSize);
//                } else {
//                    final HistogramBar histogramBar = tableView.getItems().get(i);
//                    // If the bar is already built and requires an update
//                    if (tableView.getItems().get(i).isBarUpdateRequired(bin.selectedCount, maxCount)) {
//                        System.out.println("Required update");
//
//                        rectBar = constructBar(bin, maxCount, updateBinCounts, width, i, fontSize);
//
//                        // Also update selected and total counts
//                        histogramBar.setSelectedCount(bin.selectedCount);
//                        histogramBar.setTotalCount(maxCount);
//                    } else {
//                        System.out.println("Stayed the same");
//                        // Otherwise the bar stays the same
//                        rectBar = (StackPane) histogramBar.getBar();
//                    }
//                }
//            }

            tableView.getItems().get(i).setBar(rectBar);
        }
    }

    private void resizeBars(final double width) {
        System.out.println("resize bars");
        for (final HistogramBar bar : tableView.getItems()) {
            bar.adjustBarWidth(width);
        }
    }

    private void recalculateVisibleIndexes(final double scrollValue) {
        System.out.println("recomputeVisibleIndexes " + scrollValue);
        prevScrollValue = scrollValue;

        final int numItems = tableView.getItems().size();

        //get height (what the user can see)
        final float tableHeight = (float) tableView.getHeight();

        if (tableHeight == 0 && numItems > 0) {
            firstVisibleIndex = DEFAULT_FIRST_VISIBLE_INDEX;
            lastVisibleIndex = DEFAULT_LAST_VISIBLE_INDEX;
            return;
        }

        final float heightPerRow = (float) barHeight;

        // get full height of tableView
        final float fullHeight = (float) heightPerRow * numItems;

        final float lower = (fullHeight - tableHeight) * (float) scrollValue;
        final float upper = lower + tableHeight;

        System.out.println("tableHeight " + tableHeight + " heightPerRow " + heightPerRow + " tableView.getItems().size() " + tableView.getItems().size());

        firstVisibleIndex = (int) Math.ceil(lower / heightPerRow) - VISIBLE_INDEX_EXTEND;
        lastVisibleIndex = (int) Math.ceil(upper / heightPerRow) + VISIBLE_INDEX_EXTEND;
    }

    private void updateHeader(final double fontSize) {
        final Label headerValue = new Label(PROPERTY_VALUE);
        final Label headerCount = new Label(COUNT);
        final Label headerTotalBins;
        if (binCollection != null) {
            headerTotalBins = new Label(TOTAL_BINS_COUNT + binCollection.getSelectedBins().length + "/" + binCollection.getBins().length);
        } else {
            headerTotalBins = new Label(TOTAL_BINS_COUNT + "0/0");
        }
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
        final int selectedCount = bin.selectedCount;
        //System.out.println("constructBar barIndex: " + barIndex + " selectedCount: " + selectedCount);
        final int elementCount = bin.elementCount;

        final float lengthPerElement = (float) width / maxCount;
        final int arc = barHeight / 3;

        // Setup bar colours
        final javafx.scene.paint.Color barColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getBarColor());
        final javafx.scene.paint.Color darkerBarColor = barColor.darker();

        final javafx.scene.paint.Color activatedBarColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getActivatedBarColor());
        final javafx.scene.paint.Color darkerActivatedBarColor = activatedBarColor.darker();

        final javafx.scene.paint.Color selectedColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getSelectedColor());
        final javafx.scene.paint.Color darkerSelectedColor = selectedColor.darker();

        final javafx.scene.paint.Color activatedSelectedColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getActivatedSelectedColor());
        final javafx.scene.paint.Color darkerActivatedSelectedColor = activatedSelectedColor.darker();

        // Always draw something, even if there aren't enough pixels to draw the actual length.
        final int barLength = Math.max((int) (elementCount * lengthPerElement), MINIMUM_BAR_WIDTH);

        // Rectangle will be a stack pane with the different sections layer on top of each other
        final StackPane rectBar = new StackPane();

        // Draw the background of the bar
        if (elementCount < maxCount) {
            final Rectangle rect = new Rectangle(width, Double.valueOf(barHeight));

            rect.setArcHeight(arc);
            rect.setArcWidth(arc);
            rect.setFill(JavaFxUtilities.awtColorToFXColor(barIndex == activeBin ? ACTIVE_AREA_COLOR : CLICK_AREA_COLOR));

            rectBar.getChildren().add(rect);
            StackPane.setAlignment(rect, Pos.CENTER_LEFT);
        }

        // Calculate the length of the selected component of the bar
        final int selectedLength = (selectedCount > 0) ? Math.max(barLength * selectedCount / elementCount, MINIMUM_SELECTED_WIDTH) : 0;

        // Draw the unselected component of the bar
        if (selectedLength < barLength) {
            //Setting the linear gradient 
            final Stop[] stops = bin.activated
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
            final Stop[] stops = bin.activated
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
            final String binCount = (bin.selectedCount > 0) ? Integer.toString(bin.selectedCount) + "/" + Integer.toString(bin.elementCount) : Integer.toString(bin.elementCount);
            final Label binCountlabel = new Label(binCount);
            //binCountlabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("bar-bin-count"), true); // Set styling
            binCountlabel.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);

            binCountlabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("barBinCount"), true); // Set styling

            binCountlabel.setMinHeight(barHeight);
            //binCountsVbox.getChildren().add(binCountlabel);// OLD
            rectBar.getChildren().add(binCountlabel);
            StackPane.setAlignment(binCountlabel, Pos.CENTER_RIGHT);
        }

        return rectBar;
    }

    private void updatePropertyText() {
        if (binCollection == null) {
            // No data, so just have text saying so
            final Label text = new Label(NO_DATA);
            this.setCenter(text);
        } else if (binCollection.getBins().length == 0) {
            // Draw nothing: there is data, but the user doesn't want to see it.
            this.setCenter(null);
        } else {
            // There is data and the user wants to see it
            final Bin[] bins = binCollection.getBins();
            final int maxCount = binCollection.getMaxElementCount();

            if (maxCount > 0) {
                // Two columns, property values and bars
                barColumn.getChildren().clear();
                propertyColumn.getChildren().clear();

                final Label headerValue = new Label(PROPERTY_VALUE);
                final Label headerCount = new Label(COUNT);
                final Label headerTotalBins = new Label(TOTAL_BINS_COUNT + binCollection.getSelectedBins().length + "/" + bins.length);

                // Set styling
                headerValue.getStyleClass().add(HEADER_ROW_CSS_CLASS);
                headerCount.getStyleClass().add(HEADER_ROW_CSS_CLASS);
                headerTotalBins.getStyleClass().add(HEADER_ROW_CSS_CLASS);

                final double fontSize = barHeight * FONT_SCALE_FACTOR;
                headerValue.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);
                headerCount.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);
                headerTotalBins.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);

                headerValue.setMinHeight(barHeight);

                final Pane spacer = new Pane();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                propertyColumn.getChildren().add(headerValue);
                final HBox headerRow = new HBox();
                headerRow.setAlignment(Pos.CENTER_RIGHT);
                headerRow.setMinHeight(barHeight);
                headerRow.getChildren().addAll(headerCount, spacer, headerTotalBins);
                barColumn.getChildren().add(headerRow);

                final Label[] propertyValuesArray = new Label[bins.length];

                // Populate category text column
                for (int bar = 0; bar < bins.length; bar++) {
                    final Bin bin = bins[bar];
                    final String category = bin.getLabel();
                    final Label propertyValue;

                    if (category == null) {
                        // Make text yellow, and <no value>
                        propertyValue = new Label(NO_VALUE);
                        // Sets a psuedo class for css that changes the text colour to yellow
                        propertyValue.getStyleClass().add("no-value");
                    } else {
                        // Regular grey text
                        propertyValue = new Label(category);
                        propertyValue.setTextFill(javafx.scene.paint.Color.grayRgb(192));
                        propertyValue.getStyleClass().add("histogram-text");
                    }

                    // For some reason, setting font size with setFont() doesn't work. So the styling is set like this
                    propertyValue.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);
                    propertyValue.setMinHeight(barHeight);
                    propertyValuesArray[bar] = propertyValue;
                }
                //propertyColumn.getChildren().addAll(propertyValuesArray);
                //this.setCenter(stackPane);
                this.setCenter(mainVBox);
            }
        }
    }

    private void updateIcons() {
        iconColumn.getChildren().clear();

        if (binIconMode == BinIconMode.NONE) {
            return;
        }

        // Create an empty rectangle to pad the icons down one
        final Rectangle emptyIcon = new Rectangle(0, Double.valueOf(barHeight));
        iconColumn.getChildren().add(emptyIcon);

        // For each bin, add icon to column
        for (final Bin bin : binCollection.getBins()) {
            final Node icon = binIconMode.createFXIcon(bin, barHeight);
            if (icon != null) {
                iconColumn.getChildren().add(icon);
            } else {
                // Create an empty rectangle to pad the icons down one
                final Rectangle empty = new Rectangle(0, Double.valueOf(barHeight));
                iconColumn.getChildren().add(empty);
            }
        }
    }

    private void updateBars() {
        updateBars(false);
    }

    private void updateBars(final boolean updateBinCounts) {
        // If nothing has changed, dont need to update
        if (!requireUpdate()) {
            return;
        }

        drawBars(updateBinCounts);
    }

    private void drawBars(final double width) {
        drawBars(false, width);
    }

    private void drawBars(final boolean updateBinCounts) {
        drawBars(updateBinCounts, barsWidth);
    }

    private void drawBars(final boolean updateBinCounts, final double width) {
        if (binCollection == null || binSelectionMode == null) {
            return;
        }

        final Bin[] bins = binCollection.getBins();
        final int maxCount = binCollection.getMaxElementCount();
        barsWidth = width;

        final float lengthPerElement = (float) width / maxCount;
        final int arc = barHeight / 3;

        // Setup bar colours
        final javafx.scene.paint.Color barColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getBarColor());
        final javafx.scene.paint.Color darkerBarColor = barColor.darker();

        final javafx.scene.paint.Color activatedBarColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getActivatedBarColor());
        final javafx.scene.paint.Color darkerActivatedBarColor = activatedBarColor.darker();

        final javafx.scene.paint.Color selectedColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getSelectedColor());
        final javafx.scene.paint.Color darkerSelectedColor = selectedColor.darker();

        final javafx.scene.paint.Color activatedSelectedColor = JavaFxUtilities.awtColorToFXColor(binSelectionMode.getActivatedSelectedColor());
        final javafx.scene.paint.Color darkerActivatedSelectedColor = activatedSelectedColor.darker();

        final double fontSize = barHeight * FONT_SCALE_FACTOR;

        final StackPane[] barsArray = new StackPane[bins.length + 1]; // one extra for blank (could replace with header)

        final ObservableList<StackPane> listOfBars = FXCollections.observableArrayList();
        final ObservableList<HistogramBar> listOfHistogrambars = FXCollections.observableArrayList();

        //final TableView table = new TableView<>();
        // Create an empty rectangle to pad the bars array
        final Rectangle emptyRect = new Rectangle(0, Double.valueOf(barHeight));
        final StackPane emptyPane = new StackPane();
        emptyPane.getChildren().add(emptyRect);
        barsArray[0] = emptyPane;// empty

        listOfBars.add(emptyPane);

        if (updateBinCounts) {
            binCountsVbox.getChildren().clear();
            final Label emptyLabel = new Label();
            emptyLabel.setMinHeight(barHeight);
            binCountsVbox.getChildren().add(emptyLabel);
        }

        // For each bar
        for (int bar = 0; bar < bins.length; bar++) {
            final Bin bin = bins[bar];

            final int selectedCount = bin.selectedCount;
            final int elementCount = bin.elementCount;

            // Always draw something, even if there aren't enough pixels to draw the actual length.
            final int barLength = Math.max((int) (elementCount * lengthPerElement), MINIMUM_BAR_WIDTH);

            // Rectangle will be a stack pane with the different sections layer on top of each other
            final StackPane rectBar = new StackPane();

            // Draw the background of the bar
            if (elementCount < maxCount) {
                final Rectangle rect = new Rectangle(width, Double.valueOf(barHeight));

                rect.setArcHeight(arc);
                rect.setArcWidth(arc);
                rect.setFill(JavaFxUtilities.awtColorToFXColor(bar == activeBin ? ACTIVE_AREA_COLOR : CLICK_AREA_COLOR));

                rectBar.getChildren().add(rect);
            }

            // Calculate the length of the selected component of the bar
            final int selectedLength = (selectedCount > 0) ? Math.max(barLength * selectedCount / elementCount, MINIMUM_SELECTED_WIDTH) : 0;

            // Draw the unselected component of the bar
            if (selectedLength < barLength) {
                //Setting the linear gradient 
                final Stop[] stops = bin.activated
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
                final Stop[] stops = bin.activated
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
                final String binCount = (bin.selectedCount > 0) ? Integer.toString(bin.selectedCount) + "/" + Integer.toString(bin.elementCount) : Integer.toString(bin.elementCount);
                final Label binCountlabel = new Label(binCount);
                binCountlabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("bar-bin-count"), true); // Set styling
                binCountlabel.setStyle(FONT_SIZE_CSS_PROPERTY + fontSize);

                binCountlabel.setMinHeight(barHeight);
                binCountsVbox.getChildren().add(binCountlabel);
                StackPane.setAlignment(binCountlabel, Pos.CENTER_RIGHT);
            }

            // Finally, put the bar in array
            barsArray[bar + 1] = rectBar;
            listOfBars.add(rectBar);
            listOfHistogrambars.add(new HistogramBar(null, "", rectBar));
        }

        listView.getItems().clear();
        listView.getItems().addAll(listOfBars);

        tableView.getItems().clear();
        tableView.setItems(listOfHistogrambars);

        barsVbox.getChildren().clear();
//        barsVbox.getChildren().addAll(barsArray);

        // barsVbox.getChildren().add(listView);
//        table.setItems(listOfBars);
//        barsVbox.getChildren().add(table);
        barsVbox.setMaxWidth(width);
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

    private void updateBarHeight() {
        barHeight = (barHeightBase * FontUtilities.getApplicationFontSize()) / DEFAULT_FONT_SIZE;
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

    protected void handleMouseClicked(final MouseEvent e) {
        if (binCollection != null && e.getButton() == MouseButton.SECONDARY) {
            copyMenu.show(this, e.getScreenX(), e.getScreenY());
        }
    }

//    /**
//     * Function to handle when the user presses their mouse button on the display Made protected for testing
//     */
//    protected void handleMousePressed(final MouseEvent e) {
//        System.out.println("handleMousePressed " + e);
//        if (binCollection != null && e.getButton() == MouseButton.PRIMARY) {
//            final Point pointOnHistogram = new Point((int) Math.round(e.getX()), (int) Math.round(e.getY()));
//            final int bar = getBarAtPoint(pointOnHistogram, false);
//
//            shiftDown = e.isShiftDown();
//            controlDown = e.isControlDown();
//
//            dragStart = (shiftDown && activeBin >= 0) ? activeBin : bar;
//            setDragEnd(bar);
//
//            binSelectionMode.mousePressed(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd);
//
//            // Only need to update bars
//            updateBars();
//        }
//    }
    protected void handleMousePressed(final MouseEvent e) {
        System.out.println("handleMousePressed");
        if (binCollection != null && e.getButton() == MouseButton.PRIMARY) {
            shiftDown = e.isShiftDown();
            controlDown = e.isControlDown();

            //System.out.println(tableView.getSelectionModel().getSelectedIndex() + " shiftDown " + shiftDown + " controlDown " + controlDown);
            final int bar = tableView.getSelectionModel().getSelectedIndex();

            dragStart = (shiftDown && activeBin >= 0) ? activeBin : bar;
            setDragEnd(bar);

            binSelectionMode.mousePressed(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd);

            // Only need to update bars
            updateTableBars();

            updateHeader(barHeight * FONT_SCALE_FACTOR);
        }
    }

//    protected void handleMouseDragged(final MouseEvent e) {
//        if (binCollection != null && e.isPrimaryButtonDown()) {
//            final Point pointOnHistogram = new Point((int) Math.round(e.getX()), (int) Math.round(e.getY()));
//            final int bar = getBarAtPoint(pointOnHistogram, false);
//
//            final int newDragEnd = bar;
//            binSelectionMode.mouseDragged(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, newDragEnd);
//            setDragEnd(newDragEnd);
//
//            // Only need to update bars
//            updateBars();
//        }
//    }
    protected void handleMouseDragged(final MouseEvent e) {
//        //System.out.println("handleMouseDragged hoveredRowIndex:" + hoveredRowIndex);
//        if (binCollection != null && e.isPrimaryButtonDown()) {
//            final int bar = tableView.getSelectionModel().getSelectedIndex();
//
//            final int newDragEnd = bar;
//            binSelectionMode.mouseDragged(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, newDragEnd);
//            setDragEnd(newDragEnd);
//
//            // Only need to update bars
//            updateTableBars();
//            updateHeader(barHeight * FONT_SCALE_FACTOR);
//        }
    }

//    protected void handleMouseReleased(final MouseEvent e) {
//        this.requestFocus();
//        if (binCollection != null && e.getButton() == MouseButton.PRIMARY) {
//            binSelectionMode.mouseReleased(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, topComponent);
//            activeBin = dragStart == dragEnd ? dragStart : -1;
//
//            // Only need to update bars
//            updateBars();
//        }
//    }
    protected void handleMouseReleased(final MouseEvent e) {
        Platform.runLater(() -> {
            System.out.println("handleMouseReleased");
            System.out.println("hoveredRowIndex " + hoveredRowIndex);
            this.requestFocus();
            if (binCollection != null && e.getButton() == MouseButton.PRIMARY) {
                setDragEnd(hoveredRowIndex);
                System.out.println("dragStart " + dragStart + " dragEnd " + dragEnd);

                binSelectionMode.mouseReleased(shiftDown, controlDown, binCollection.getBins(), dragStart, dragEnd, topComponent);
                activeBin = dragStart == dragEnd ? dragStart : -1;

                // Only need to update bars
                updateTableBars();

                updateHeader(barHeight * FONT_SCALE_FACTOR);
            }
        });
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

    public class HistogramBar {

        // Icon
        private ObjectProperty<Node> icon;

        final public void setIcon(final Object value) {
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

        final public void setPropertyName(final String value) {
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

        final public void setBar(final Object value) {
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

        private double barWidth = -1;

        public void setBarWidth(final double newValue) {
            barWidth = newValue;
        }

        public void adjustBarWidth(final double width) {
            final StackPane pane = (StackPane) barProperty().get();
            if (pane == null) {
                return;
            }
            final double ratio = width / barWidth;
            System.out.println("width " + width + " barWidth " + barWidth + " ratio " + ratio);
            for (final Node child : pane.getChildren()) {
                if (child instanceof Rectangle rectangle) {
                    final double currentWidth = rectangle.getWidth();
                    rectangle.setWidth(currentWidth * ratio);
                }
            }

            setBarWidth(width);
        }

        // Stats
        private int selectedCount = 0;
        private int totalCount = 0;

        public int getSelectedCount() {
            return selectedCount;
        }

        final public void setSelectedCount(final int newValue) {
            selectedCount = newValue;
        }

        public int getTotalCount() {
            return totalCount;
        }

        final public void setTotalCount(final int newValue) {
            totalCount = newValue;
        }

        public boolean isBarUpdateRequired(final int selectedCount, final int maxCount) {
            return getBar() == null || getSelectedCount() != selectedCount || getTotalCount() != maxCount;
        }

        // TODO see what uses this and remove
        public HistogramBar(final Node icon, final String propertName, final StackPane bar) {
            setIcon(icon);
            setPropertyName(propertName);
            setBar(bar);
        }

        public HistogramBar(final Node icon, final String propertName, final StackPane bar, final int selectedCount, final int totalCount, final double barWidth) {
            setIcon(icon);
            setPropertyName(propertName);
            setBar(bar);
            setSelectedCount(selectedCount);
            setTotalCount(totalCount);
            setBarWidth(barWidth);
        }
    }
}
