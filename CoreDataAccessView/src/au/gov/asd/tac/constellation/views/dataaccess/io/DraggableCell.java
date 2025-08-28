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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 *
 * @author twilight_sparkle
 */
public class DraggableCell<T> extends ListCell<T> {

    public Image getCellDragImage() {
        return null;
    }

    public DraggableCell() {
        final ListCell<T> thisCell = this;

        setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            final ObservableList<T> items = getListView().getItems();

            final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            final ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(items.indexOf(getItem())));
            if (getCellDragImage() != null) {
                dragboard.setDragView(getCellDragImage());
            }
            dragboard.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
                setOpacity(0.3);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            boolean success = false;
            if (event.getGestureSource() instanceof DraggableCell dCell && dCell.getListView() == getListView()) {

                final Dragboard dragboard = event.getDragboard();

                if (dragboard.hasString()) {
                    final ObservableList<T> items = getListView().getItems();
                    final int draggedIndex = Integer.parseInt(dragboard.getString());
                    final T thisItem = getItem();
                    final int thisIndex = items.indexOf(thisItem);

                    items.set(thisIndex, items.get(draggedIndex));
                    items.set(draggedIndex, thisItem);
                    getListView().setItems(FXCollections.observableList(getListView().getItems()));
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        setOnDragDone(DragEvent::consume);
    }

    @Override
    protected void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            setText(item.toString());
        } else {
            setText(null);
        }
    }

}
