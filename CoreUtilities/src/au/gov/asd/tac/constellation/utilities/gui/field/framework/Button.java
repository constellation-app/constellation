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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 
 * @author capricornunicorn123
 */
public abstract class Button extends StackPane {
    
    private final int endCellPrefWidth = 50;
    private final int defaultCellHeight = 22;
    
    private final Color dkButtonColor = Color.color(0.11, 0.35, 0.65);
    private final Color dkOptionColor = Color.color(0.4, 0.4, 0.4);
    
    private final Color ltButtonColor = Color.color(0.19, 0.5, 0.97);
    private final Color ltOptionColor = Color.color(0.255, 0.568, 0.91);
    
    private final Rectangle background = new Rectangle(endCellPrefWidth, defaultCellHeight); 
    
    public Button(final Label label, final ButtonType type) {        
        final Color color = switch (type){
            case POPUP -> JavafxStyleManager.isDarkTheme() ? dkButtonColor : ltButtonColor;
            default -> JavafxStyleManager.isDarkTheme() ? dkOptionColor : ltOptionColor;
        };
        background.setFill(color);
        background.setArcHeight(7);
        background.setOnMouseEntered(event -> background.setFill(color.brighter()));
        background.setOnMouseExited(event -> background.setFill(color));
        background.setOnMouseClicked(action());
        label.setMouseTransparent(true);
        label.setPrefWidth(endCellPrefWidth);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: white;");
        
        this.getChildren().addAll(background, label);
    }
    
    public DoubleProperty getHeightProperty(){
        return background.heightProperty();
    }
    
    public abstract EventHandler<? super MouseEvent> action();
    
    public enum ButtonType{
        POPUP,
        DROPDOWN,
        CHANGER;
    }
}
