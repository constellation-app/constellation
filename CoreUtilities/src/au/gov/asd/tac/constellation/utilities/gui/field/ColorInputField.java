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
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author capricornunicorn123
 */
public class ColorInputField extends ConstellationInputField {
    
    ColorMode mode = ColorMode.COLOR;
    
    public ColorInputField(){
        super(ConstellationInputFieldLayoutConstants.DROPDOWN_INPUT_POPUP);
        this.setRightLabel("Swatch");
        this.setLeftLabel(mode.toString());
        this.registerRightButtonEvent(event -> {
            JFXPanel xp = new JFXPanel();
            final Scene scene = new Scene(new ConstellationColorPicker());

            xp.setScene(scene);
            //xp.setPreferredSize(new Dimension((int) scene.getWidth(), (int) scene.getHeight()));
            
            
            final Object[] options = {new JButton("Select"), DialogDescriptor.CANCEL_OPTION};
            final Object focus = DialogDescriptor.NO_OPTION;
            
            final DialogDescriptor dd = new DialogDescriptor(xp, "ChooseYOUR COLORRRR", true, options , focus, DialogDescriptor.DEFAULT_ALIGN, null, null);
            Object r = DialogDisplayer.getDefault().notify(dd);
        });
        
        this.registerLeftButtonEvent(event -> {
            this.showDropDown();
        });
    }
    
    public void setMode(ColorMode mode){
        this.mode = mode;
        this.setLeftLabel(mode.toString());
        this.setColor(getColor());
    }
    
    public void setColor(Color color){
        this.setColor(ConstellationColor.fromFXColor(color)); 
    }
    
    public void setColor(final ConstellationColor color){
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
    
    public ConstellationColor getColor(){        
        return getColor(this.getText());
    }
    
    public static ConstellationColor getColor(final String text){
        if (text.isBlank()){
            return null;
        } else if (text.contains(",")){
            StringBuilder sb = new StringBuilder();
            sb.append("RGB");
            String[] colors = text.split(",");
            for (String color : colors){
                String colorVal = color.split(":")[1];
                for (int i = 0 ; i < 3 - colorVal.length() ; i++){
                    sb.append("0");
                }
                sb.append(colorVal);
            }
            return ConstellationColor.getColorValue(sb.toString());
        } else {
            return ConstellationColor.getColorValue(text);
        }
    }
    
    @Override
    public ConstellationInputDropDown getDropDown() {
        return new ColorInputDropDown(this);
    }
    
    @Override
    public boolean isValid(String value){
        return getColor(value) != null;
    }
    
    private class ColorInputDropDown extends ConstellationInputDropDown {
        
        public ColorInputDropDown(final ColorInputField field){
            super(field);
            for (final ColorMode mode : ColorMode.values()){
                final Label label = new Label(mode.toString());
                
                label.setOnMouseClicked(event -> {
                    field.setMode(mode);
                });

                this.addMenuOption(label);
            }
        }        
    }
    
    public enum ColorMode {
        COLOR("Color"),
        HEX("HEX"),
        RGB("RGB");
        
        final String text;
        
        ColorMode(final String text){
            this.text = text;
        }
        
        @Override
        public String toString(){
            return this.text;
        }
    }
    
}
