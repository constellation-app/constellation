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
import au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ColorMode;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ColorMode.COLOR;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ColorMode.HEX;
import static au.gov.asd.tac.constellation.utilities.gui.field.ConstellationInputConstants.ColorMode.RGB;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.Button.ButtonType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.AutoCompleteSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputDropDown;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.InfoWindowSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.LeftButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Labeled;

/**
 * A {@link ConstellationInput} for managing {@link ConstellationColor} selection. 
 * This input provides the following {@link ConstellationInput} support features
 * <ul>
 * <li>{@link RightButtonSupport} - Triggers a drop down menu to select a {@link ContellationColor} from sorted list of hues.</li>
 * <li>{@link LeftButtonSupport} - Triggers a drop down menu to set the format that this input should display the coos text value.</li>
 * <li>{@link InfoWindowSupport} - Previews a square of the color represented by this input field.</li>
 * <li>{@link AutoCompleteSupport} - Provides a list of colors with a name that matches the text in the input field.</li>
 * </ul>
 * See referenced classes and interfaces for further details on inherited and implemented features.
 * 
 * TODO: {@link ColorPicker} provides an interactive swatch to select colors from. 
 * This is not accessible as manually and thus cant be triggered by this class and integrated into this input.
 * Create a Custom Constellation Color Picker pop up equivalent and replace the ConstellationColor Swatch drop down button with a  color picker
 * pop up that includes: RBG color generation, constellationColor selection, HSB color generation CMYK color generation.
 * 
 * @author capricornunicorn123
 */
public final class ColorInput extends ConstellationInput<ConstellationColor> implements RightButtonSupport, LeftButtonSupport, InfoWindowSupport, AutoCompleteSupport {
    
    ColorMode mode = ColorMode.COLOR;
    final Label label = new Label();
    
    public ColorInput(){
        label.setText(mode.toString());
        initialiseDepedantComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Local Private Methods">   
    private void setMode(final ColorMode mode){
        this.mode = mode;
        label.setText(mode.toString());
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
            label.setText(mode.toString());
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
    
    private List<ConstellationColor> getListOfSortedColors(){
        return ConstellationColor.NAMED_COLOR_LIST.stream()
                    .sorted((ConstellationColor o1, ConstellationColor o2) -> {
                        int hueValue = Double.compare(o1.getJavaFXColor().getHue(), o2.getJavaFXColor().getHue());
                        int satValue = Double.compare(o1.getJavaFXColor().getSaturation(), o2.getJavaFXColor().getSaturation());
                        int brightValue = Double.compare(o1.getJavaFXColor().getBrightness(), o2.getJavaFXColor().getBrightness());
                        if (hueValue != 0){
                            return hueValue;
                        } else if (satValue != 0){
                            return satValue;
                        } else {
                            return brightValue;
                        }
                    }).toList();
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
        format.setOnAction(value -> executeLeftButtonAction());
        final MenuItem select = new MenuItem("Select");
        select.setOnAction(value -> executeRightButtonAction());
        return Arrays.asList(format, select);
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Button Event Implementation">   
    @Override
    public LeftButton getLeftButton() {
        return new LeftButton(label, ButtonType.CHANGER) {
                @Override
                public EventHandler<? super MouseEvent> action() {
                    return event -> executeLeftButtonAction();
                }
        };        
    }
    
    @Override
    public RightButton getRightButton() {
        return new RightButton(new Label(ConstellationInputConstants.SELECT_BUTTON_LABEL), ButtonType.DROPDOWN) {
                @Override
                public EventHandler<? super MouseEvent> action() {
                    return event -> executeRightButtonAction();
                }
        };
    }
    
    @Override
    public void executeLeftButtonAction() {
        this.showDropDown(new ColorModeDropDown(this));
    }
    
    @Override
    public void executeRightButtonAction() {
        this.showDropDown(new ColorPickerDropDown(this));
    }
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Drop Down Implementation">    
    private class ColorModeDropDown extends ConstellationInputDropDown {
        
        public ColorModeDropDown(final ColorInput field){
            super(field);
            
            for (final ColorMode mode : ColorMode.values()){
                final Label label = new Label(mode.toString());
                
                label.setOnMouseClicked(event -> {
                    field.setMode(mode);
                });

                this.registerCustomMenuItem(label);
            }
        }        
    }    
    
    private class ColorPickerDropDown extends ConstellationInputDropDown {
        
    public ColorPickerDropDown(final ColorInput field) {
            super(field);
            
            getListOfSortedColors()    
                    .forEach(value -> {
                        
                final Labeled item = new Label(value.getName());

                final Rectangle icon = new Rectangle();
                icon.heightProperty().bind(item.heightProperty());
                icon.widthProperty().bind(item.heightProperty());
                icon.setFill(Paint.valueOf(value.getHtmlColor()));
                item.setGraphic(icon);
                
                item.setOnMouseClicked(event -> {
                    setValue(value);
                });
                
                this.registerCustomMenuItem(item);                
            });
        }        
    }    
    // </editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Info Window Implementation">   
    @Override
    public InfoWindow getInfoWindow() {
        final Rectangle colorPreview = new Rectangle(14, 14);
        colorPreview.setArcWidth(6);
        colorPreview.setArcHeight(6);
        
        final InfoWindow window = new InfoWindow(this){
            @Override
            protected void refreshWindow() {
                final ConstellationColor color = getColor();
                if (color == null) {
                    colorPreview.setFill(Color.TRANSPARENT);
                } else {
                    colorPreview.setFill(color.getJavaFXColor());
                }
            }
        };
        window.setWindowContents(colorPreview);
        return window;
    }
    //</editor-fold> 
    
    // <editor-fold defaultstate="collapsed" desc="Auto Complete Implementation">  
    @Override
    public List<MenuItem> getAutoCompleteSuggestions() {
        final List<MenuItem> suggestions = new ArrayList<>();
        if (!this.getText().isBlank()){
            getListOfSortedColors()
                    .stream()
                    .filter(value -> value.toString().startsWith(getText()))
                    .filter(value -> !value.toString().equals(getText()))
                    .forEach(value -> {
                        
                        final MenuItem item = new MenuItem(value.getName());
                        item.setOnAction(event -> {
                            this.setValue(value);
                        });
                        final Rectangle icon = new Rectangle(12, 12);
                        icon.setFill(value.getJavaFXColor());
                        item.setGraphic(icon);
                        suggestions.add(item);
                    });
        }
        return suggestions;
    }
    //</editor-fold> 
}
