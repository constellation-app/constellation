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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.tooltip.TooltipPane;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

/**
 * A speech bubble that holds all the contributions relating to a single
 * transaction in the Conversation View.
 *
 * @author sirius
 */
public class ConversationBubble extends VBox {

    public static final double CORNER_RADIUS = 20;
    public static final double PADDING = 10;
    public static final double TAIL_OFFSET = 40;
    public static final double TAIL_WIDTH = 10;
    public static final double TAIL_HEIGHT = 18;
    public static final double TAIL_TIP_OFFSET = 25;
    public static final double TAIL_X_RADIUS = 50;
    public static final double TAIL_Y_RADIUS = 50;

    private final Rectangle bubbleGraphic;
    private final Path tail;
    private final Line tailTop;
    private static final boolean DARK_MODE = JavafxStyleManager.isDarkTheme();

    /**
     * Creates a new Bubble.
     *
     * @param contents The contents of a Bubble. Can be null.
     * @param message the {@link ConversationMessage} to display in the bubble.
     * @param tipsPane A tool tips pane, e.g. to provide translation.
     */
    public ConversationBubble(final List<Region> contents, final ConversationMessage message, final TooltipPane tipsPane) {

        setCache(true);
        setCacheHint(CacheHint.SPEED);

        setMinWidth(200);
        setMinHeight(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);
        setSpacing(-5);

        final VBox bubbleContent = new VBox();
        bubbleContent.setAlignment(Pos.CENTER_LEFT);
        bubbleContent.setPadding(new Insets(2, 2, 0, 2));
        bubbleContent.setMaxWidth(USE_PREF_SIZE);
        bubbleContent.setMinWidth(200);

        // The bubble graphic.
        bubbleGraphic = new Rectangle(0, 0);
        bubbleGraphic.setArcHeight(CORNER_RADIUS);
        bubbleGraphic.setArcWidth(CORNER_RADIUS);
        bubbleGraphic.widthProperty().bind(bubbleContent.widthProperty());
        bubbleGraphic.heightProperty().bind(bubbleContent.heightProperty());
        bubbleGraphic.setManaged(false);
        bubbleContent.getChildren().add(bubbleGraphic);

        Region previousContent = null;
        for (final Region content : contents) {
            if (previousContent != null) {
                final Pane separator = new Pane();
                separator.setPrefHeight(3);
                separator.prefWidthProperty().bind(bubbleContent.widthProperty().subtract(5));
                separator.setMaxWidth(USE_PREF_SIZE);
                separator.setStyle("-fx-background-color: black; -fx-background-insets: 2 0 0 0;");
                bubbleContent.getChildren().addAll(separator, content);
            } else {
                bubbleContent.getChildren().add(content);
            }
            previousContent = content;
        }

        tail = new Path();
        final MoveTo start;
        final ArcTo curve1;
        final ArcTo curve2;
        if (message.getConversationSide() == ConversationSide.LEFT) {
            start = new MoveTo(TAIL_OFFSET, 0);
            start.yProperty().bind(bubbleContent.heightProperty());

            curve1 = new ArcTo(TAIL_X_RADIUS, TAIL_Y_RADIUS, 0, TAIL_OFFSET - TAIL_TIP_OFFSET, 0, false, true);
            curve1.yProperty().bind(heightProperty());

            curve2 = new ArcTo(TAIL_X_RADIUS, TAIL_Y_RADIUS, 0, TAIL_OFFSET - TAIL_WIDTH, 0, false, false);
            curve2.yProperty().bind(bubbleContent.heightProperty());

            tailTop = new Line(TAIL_OFFSET - 2, 0, TAIL_OFFSET - TAIL_WIDTH + 1, 0);
            tailTop.startYProperty().bind(bubbleGraphic.heightProperty());
            tailTop.endYProperty().bind(bubbleGraphic.heightProperty());
        } else {
            start = new MoveTo(0, 0);
            start.xProperty().bind(widthProperty().subtract(TAIL_OFFSET));
            start.yProperty().bind(bubbleContent.heightProperty());

            curve1 = new ArcTo(TAIL_X_RADIUS, TAIL_Y_RADIUS, 0, 0, 0, false, false);
            curve1.xProperty().bind(widthProperty().add(TAIL_TIP_OFFSET - TAIL_OFFSET));
            curve1.yProperty().bind(heightProperty());

            curve2 = new ArcTo(TAIL_X_RADIUS, TAIL_Y_RADIUS, 0, 0, 0, false, true);
            curve2.xProperty().bind(widthProperty().add(TAIL_WIDTH - TAIL_OFFSET));
            curve2.yProperty().bind(bubbleContent.heightProperty());

            tailTop = new Line(0, 0, 0, 0);
            tailTop.startXProperty().bind(bubbleGraphic.widthProperty().subtract(TAIL_OFFSET).add(1));
            tailTop.startYProperty().bind(bubbleGraphic.heightProperty());
            tailTop.endXProperty().bind(bubbleGraphic.widthProperty().add(TAIL_WIDTH - TAIL_OFFSET).add(-2));
            tailTop.endYProperty().bind(bubbleGraphic.heightProperty());
        }

        tail.getElements().addAll(start, curve1, curve2);
        tail.setManaged(false);
        tailTop.setManaged(false);
        getChildren().addAll(tail, tailTop);

        final BorderPane timeContent = new BorderPane();
        final Region timeLabel = message.getDatetime().createContent();
        if (message.getConversationSide() == ConversationSide.LEFT) {
            timeContent.setRight(timeLabel);
        } else {
            timeContent.setLeft(timeLabel);
        }
        timeContent.setPadding(new Insets(5, 0, 0, 0));

        getChildren().addAll(bubbleContent, timeContent);

        setColor(message.getColor());
    }

    public final void setColor(final Color color) {
        Color bottomColor = DARK_MODE ? color.darker() : color;
        Color topColor = DARK_MODE ? color.brighter() : color.brighter().brighter();

        Stop[] stops = new Stop[]{
            new Stop(0, topColor),
            new Stop(1, bottomColor)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

        bubbleGraphic.setStroke(color);
        bubbleGraphic.setFill(gradient);

        tail.setFill(bottomColor);
        tail.setStroke(color);
        tailTop.setStroke(bottomColor); // Erase the border of the buble where the tail joins.
    }
}
