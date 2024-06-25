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
package au.gov.asd.tac.constellation.utilities.gui.field;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColorPicker;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ColorMode;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ColorMode.COLOR;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ColorMode.HEX;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.ColorMode.RGB;
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputFieldConstants.LayoutConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * A {@link ConstellationInputField} for managing {@link ConstellationColor} selection. 
 * 
 * @author capricornunicorn123
 */
public final class ColorInputField extends ConstellationInputField<ConstellationColor> {
    
    ColorMode mode = ColorMode.COLOR;
    
    public ColorInputField(){
        super(LayoutConstants.DROPDOWN_INPUT_POPUP);
        this.setRightLabel(ConstellationInputFieldConstants.SWATCH_BUTTON_LABEL);
        this.setLeftLabel(mode.toString());  
    }

    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    private void setMode(final ColorMode mode){
        this.mode = mode;
        this.setLeftLabel(mode.toString());
        this.setColor(getColor());
    }
    
    private void updateMode(final String text){
        final ColorMode localMode;
        
        //Determine the mode from the sting
        if (text.contains(",")){
            localMode = ColorMode.RGB;
        } else if (text.contains("#")){
            localMode = ColorMode.HEX;
        } else if (text.contains(":")){
            localMode = ColorMode.COLOR;
        } else {
            localMode = null;
        }
        
        //Update the mode only if it could be determined
        if (localMode != null) {
            this.mode = localMode;
            this.setLeftLabel(localMode.toString());
        }
    }
    
    private void setColor(final ConstellationColor color){
        if (color != null){
            switch (mode){
                case COLOR -> {
                    if (color.getName() != null) {
                       this.setText(color.getName()); 
                    } else {
                        this.setMode(ColorMode.HEX);
                    }
                }
                case HEX -> this.setText(color.getHtmlColor());
                case RGB -> this.setText(String.format(
                        "Red:%s, Green:%s, Blue:%s", 
                        (int) (color.getRed() * 255), 
                        (int) (color.getGreen() * 255), 
                        (int) (color.getBlue() * 255))
                );
            }
        }
    }
    
    private ConstellationColor getColor(){        
        return getColor(this.getText());
    }
    
    /**
     * Gets a ConstellationColor Object representing the color represented by plain text.
     * the plain text can be interpreted from three different formats representing
     * - the name of a ConstellationColor
     * - a hex value with a "#" followed by 6 hex digits
     * - an RGB value with the Text "Red: , Green: , and Blue: "
     * @param text
     * @return 
     */
    private ConstellationColor getColor(final String text){
        final ConstellationColor color;
        if (text.isBlank()){
            return null;
        } else if (text.contains(",")){
            final StringBuilder sb = new StringBuilder();
            sb.append("RGB");
            
            final String[] colors = text.split(",");
            
            //threre should only be 3 colors
            if (colors.length != 3){
                return null;
            }
            for (int colorIndex = 0 ; colorIndex < 3 ; colorIndex++) {
                final String[] colorPair = colors[colorIndex].split(":");
                //The color should have a 
                if (colorPair.length != 2){
                    return null;
                }
                
                final String expectedKey = switch(colorIndex){
                    case 0 -> "RED";
                    case 1 -> "GREEN";
                    case 2 -> "BLUE";
                    default -> null;
                };
                
                if (!colorPair[0].strip().toUpperCase().equals(expectedKey) || colorPair[1].isBlank()) {
                    return null;
                }
                
                final int colorVal = Integer.parseInt(colorPair[1].strip());
                
                if (colorVal > 255 || colorVal < 0){
                    return null;
                }
                
                sb.append(String.format("%03d", colorVal));         
            }

            color = ConstellationColor.getColorValue(sb.toString());
        } else {
            color = ConstellationColor.getColorValue(text);

        }                     
        return color;
    }
    // </editor-fold>   
    
    // <editor-fold defaultstate="collapsed" desc="Value Modification & Validation Implementation"> 
    @Override
    public ConstellationColor getValue() {
        return this.getColor();
    }

    @Override
    public void setValue(final ConstellationColor value) {
        this.setColor(value);
    }    
    
    @Override
    public boolean isValid(){
        final String value = this.getText();
        if (value.isBlank() || getColor(value) != null){
            updateMode(value);
            return true;
        }
        return false;
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="ContextMenuContributor Implementation"> 
    @Override
    public List<MenuItem> getLocalMenuItems() {
        final MenuItem format = new MenuItem("Format");
        format.setOnAction(value -> getLeftButtonEventImplementation().handle(null));
        final MenuItem swatch = new MenuItem("Swatch");
        swatch.setOnAction(value -> getRightButtonEventImplementation().handle(null));
        return Arrays.asList(format);
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public EventHandler<MouseEvent> getRightButtonEventImplementation() {
        return event -> {
            JFXPanel xp = new JFXPanel();
            final Scene scene = new Scene(new ConstellationColorPicker());

            xp.setScene(scene);
            //xp.setPreferredSize(new Dimension((int) scene.getWidth(), (int) scene.getHeight()));
            
            final Object[] options = {new JButton("Select"), DialogDescriptor.CANCEL_OPTION};
            final Object focus = DialogDescriptor.NO_OPTION;
            
            final DialogDescriptor dd = new DialogDescriptor(xp, "ChooseYOUR COLORRRR", true, options , focus, DialogDescriptor.DEFAULT_ALIGN, null, null);
            Object r = DialogDisplayer.getDefault().notify(dd);
        };
    }

    @Override
    public EventHandler<MouseEvent> getLeftButtonEventImplementation() {
        return event -> this.showDropDown(getDropDown());
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Drop Down Implementation"> 
    @Override
    public ContextMenu getDropDown() {
        return new ColorInputDropDown(this);
    }
    
    private class ColorInputDropDown extends ConstellationInputDropDown {
        
        public ColorInputDropDown(final ColorInputField field){
            super(field);
            
            final List<MenuItem> items = new ArrayList<>();
            for (final ColorMode mode : ColorMode.values()){
                final Label label = new Label(mode.toString());
                
                label.setOnMouseClicked(event -> {
                    field.setMode(mode);
                });

                items.add(this.buildCustomMenuItem(label));
            }
            this.addMenuItems(items);
        }        
    }    
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Info Window Implementation">   
    @Override
    public InputInfoWindow getInputInfoWindow() {
         return new colorInputInfoWondow(this);
    }
    
    private class colorInputInfoWondow extends InputInfoWindow{
        Rectangle label = new Rectangle(14, 14);
        private colorInputInfoWondow(ConstellationInputField parent){
            super(parent);
            
            label.setArcWidth(6);
            label.setArcHeight(6);

            this.getChildren().add(label);
        }
        
        @Override
        protected void refreshWindow() {
            ConstellationColor color = getColor();
            if (color == null) {
                label.setFill(Color.TRANSPARENT);
            } else {
                label.setFill(color.getJavaFXColor());
            }
        }
    }   
    //</editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation"> 
    @Override
    protected List<MenuItem> getAutoCompleteSuggestions() {
        return null;
    }
    // </editor-fold> 
}
