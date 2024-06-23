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

import javafx.scene.control.ContentDisplay;

/**
 * A suite of constants used by {@link ConstellationInputField} objects and related sub classes.
 * 
 * @author capricornunicorn123
 */
public class ConstellationInputFieldConstants {
   
    //Button labels
    public final static String SELECT_BUTTON_LABEL = "Select"; 
    public final static String NEXT_BUTTON_LABEL = "Next"; 
    public final static String PREVIOUS_BUTTON_LABEL = "Prev"; 
    public final static String SWATCH_BUTTON_LABEL = "Swatch"; 
    public final static String HIDE_BUTTON_LABEL = "Hide"; 
    public final static String SHOW_BUTTON_LABEL = "Show"; 
    
    /**
     * A representation of the different layouts that a ConstellationInputField can take. 
     * INPUT represents the input area of the field.
     * DROPDWN represents a button that triggers a drop down menu on the field. 
     * POPUP represents a button that triggers a pop up window.     
     * UPDATER represents a button that updates the value of the Field when pressed.
     * 
     * The combination of these representative words represents their order in the ConstellationInputField
     */
    public enum LayoutConstants {        
        INPUT(false, ContentDisplay.CENTER),
        INPUT_DROPDOWN(false, ContentDisplay.CENTER, ContentDisplay.RIGHT),
        INPUT_POPUP(true, ContentDisplay.CENTER, ContentDisplay.RIGHT),
        DROPDOWN_INPUT_POPUP(true, ContentDisplay.LEFT, ContentDisplay.CENTER, ContentDisplay.RIGHT),
        UPDATER_INPUT_UPDATER(false, ContentDisplay.LEFT, ContentDisplay.CENTER, ContentDisplay.RIGHT);
        
        private final ContentDisplay[] areas;
        private final boolean hasButton;

        private LayoutConstants(final boolean hasButton, final ContentDisplay... areas) {
            this.areas = areas;
            this.hasButton = hasButton;
        }
        
        public ContentDisplay[] getAreas() {
            return this.areas;
        }
        
        public boolean hasButton(){
            return hasButton;
        }
    }
    
    public enum TextType {
        SECRET,
        SINGLELINE,
        MULTILINE;
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
    
        /**
     * Represents the types of ChoiceInputFields available in Constellation.
     */
    public enum ChoiceType {
        
        SINGLE_DROPDOWN(LayoutConstants.INPUT_DROPDOWN),
        SINGLE_SPINNER(LayoutConstants.UPDATER_INPUT_UPDATER),
        MULTI(LayoutConstants.INPUT_DROPDOWN);
        
        LayoutConstants layout;
        
        private ChoiceType(LayoutConstants layout) {
            this.layout = layout;
        }
        
        public LayoutConstants getLayout(){
            return this.layout;
        }
    }
    
    /**
     * Describes the method of file selection for a parameter of this type.
     * Consider merging with File Chooser Mode
     */
    public enum FileInputKind {

        /**
         * Allows selection of multiple files. Displays "Open" on the button.
         */
        OPEN_MULTIPLE("Open"),
        /**
         * Allows selection of multiple files. Displays "..." on the button.
         */
        OPEN_MULTIPLE_OBSCURED("..."),
        /**
         * Allows selection of a single file only. Displays "Open" on the button.
         */
        OPEN("Open"),
        /**
         * Allows selection of a single file only. Displays "..." on the button.
         */
        OPEN_OBSCURED("..."),
        /**
         * Allows selection of a file, or entry of a non-existing but valid file
         * path. Displays "Save" on the button.
         */
        SAVE("Save"),
                /**
         * Allows selection of a file, or entry of a non-existing but valid file
         * path. Displays "..." on the button.
         */
        SAVE_OBSCURED("..."),;

        
        private final String text;
        
        private FileInputKind(final String text){
            this.text = text;
        }
        
        @Override
        public String toString(){
            return text;
        }
    }
}
